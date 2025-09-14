package com.dipa.notefournote.notes;

import com.dipa.notefournote.exception.NoteAccessDeniedException;
import com.dipa.notefournote.exception.NoteNotFoundException;
import com.dipa.notefournote.users.UserEntity;
import com.dipa.notefournote.users.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoteServiceImpl implements NoteService {

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final NoteMapper noteMapper;
    private final NoteShareRepository noteShareRepository;
    private final NoteSearchRepository noteSearchRepository;
    private final TagRepository tagRepository;

    @Override
    @Transactional
    public NoteResponse createNote(CreateNoteRequest request, String username) {
        log.info("Creating a new note for user '{}' with title: '{}'", request.title(), username);

        final UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        final NoteEntity newNote = noteMapper.toEntity(request);
        newNote.setUser(user);

        // Persists new tags (if any) before saving the note entity
        final Set<TagEntity> tags = request.tags().stream()
                .map(tagName -> tagRepository
                        .findByName(tagName)
                        .orElseGet(() -> tagRepository.saveAndFlush(new TagEntity(tagName))))
                .collect(Collectors.toSet());
        newNote.setTags(tags);

        final NoteEntity savedNote = noteRepository.saveAndFlush(newNote);
        log.info("Note created successfully for user '{}' with id: '{}'", username, savedNote.getId());

        log.debug("Synchronizing creation to MongoDB note with id: '{}'", savedNote.getId());
        final NoteDocument document = noteMapper.toDocument(savedNote);
        final NoteDocument syncDocument = noteSearchRepository.save(document);
        log.debug("Synchronized creation to MongoDB note with id: '{}'", syncDocument.getId());

        return noteMapper.toResponse(savedNote, username);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NoteResponse> findAllNotesByUsername(String username) {
        log.info("Fetching all notes for user '{}'", username);

        final UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        final Stream<NoteEntity> ownedNotes = noteRepository.findAllByUser(user).stream();
        final Stream<NoteEntity> sharedNotes = user.getReceivedShares().stream().map(NoteShare::getNote);
        final List<NoteResponse> notes = Stream.concat(ownedNotes, sharedNotes)
                .distinct()
                .sorted(Comparator.comparing(NoteEntity::getUpdatedAt).reversed())
                .map(note -> noteMapper.toResponse(note, username))
                .toList();

        log.info("Fetched all notes for user '{}': {}", username, notes);
        return notes;
    }

    @Override
    @Transactional(readOnly = true)
    public NoteResponse findNoteById(UUID noteId, String username) {
        log.info("Fetching note with id '{}' for user: '{}'", noteId, username);

        final NoteEntity noteEntity = noteRepository.findById(noteId)
                .orElseThrow(() -> new NoteNotFoundException("Note not found with id: " + noteId));

        final boolean isOwner = noteEntity.getUser().getUsername().equals(username);
        final boolean isSharedWithUser = noteEntity.getShares().stream()
                .anyMatch(share -> share.getSharedWithUser().getUsername().equals(username));

        if (!isOwner && !isSharedWithUser) {
            throw new NoteAccessDeniedException("User does not have access to this note");
        }

        final NoteResponse note = noteMapper.toResponse(noteEntity, username);

        log.info("Fetched note for user '{}': {}", username, note);
        return note;
    }

    @Override
    @Transactional
    public NoteResponse updateNote(UUID noteId, UpdateNoteRequest request, String username) {
        log.info("Updating note with id '{}' for user: '{}'", noteId, username);

        final NoteEntity note = noteRepository.findById(noteId)
                .orElseThrow(() -> new NoteNotFoundException("Note not found with id: " + noteId));

        if (!note.getUser().getUsername().equals(username)) {
            throw new NoteAccessDeniedException("Only the owner can update the note");
        }

        // Persists new tags (if any) before saving the note entity
        final Set<TagEntity> tags = request.tags().stream()
                .map(tagName -> tagRepository
                        .findByName(tagName)
                        .orElseGet(() -> tagRepository.saveAndFlush(new TagEntity(tagName))))
                .collect(Collectors.toSet());
        note.setTags(tags);
        note.setTitle(request.title());
        note.setContent(request.content());

        final NoteEntity updatedEntity = noteRepository.saveAndFlush(note);

        log.debug("Synchronizing update to MongoDB note with id: '{}'", updatedEntity.getId());
        final NoteDocument document = noteMapper.toDocument(updatedEntity);
        final NoteDocument syncDocument = noteSearchRepository.save(document);
        log.debug("Synchronized update to MongoDB note with id: '{}'", syncDocument.getId());

        log.info("Updated note with id '{}' for user: '{}'", noteId, username);
        return noteMapper.toResponse(updatedEntity, username);
    }

    @Override
    @Transactional
    public void deleteNote(UUID noteId, String username) {
        log.info("Deleting note with id '{}' for user: '{}'", noteId, username);

        final NoteEntity note = noteRepository.findById(noteId)
                .orElseThrow(() -> new NoteNotFoundException("Note not found with id: " + noteId));

        if (!note.getUser().getUsername().equals(username)) {
            throw new NoteAccessDeniedException("Only the owner can delete the note");
        }

        noteRepository.delete(note);

        log.debug("Synchronizing deletion to MongoDB note with id: '{}'", noteId);
        noteSearchRepository.deleteById(noteId.toString());
        log.debug("Synchronized deletion to MongoDB note with id: '{}'", noteId);


        log.info("Deleted note with id '{}' for user: '{}'", noteId, username);
    }

    @Override
    @Transactional
    public void shareNote(UUID noteId, ShareNoteRequest request, String ownerUsername) {
        log.info("Sharing note '{}' from user '{}' to: '{}'", noteId, ownerUsername, request.username());

        final UserEntity owner = userRepository.findByUsername(ownerUsername)
                .orElseThrow(() -> new UsernameNotFoundException("Owner user not found: " + ownerUsername));

        final UserEntity userToShareWith = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new UsernameNotFoundException("User to share with not found: " + request.username()));

        final NoteEntity note = noteRepository.findById(noteId)
                .orElseThrow(() -> new NoteNotFoundException("Note not found with id: " + noteId));

        if (!note.getUser().getId().equals(owner.getId())) {
            throw new NoteAccessDeniedException("Only the owner can share the note");
        }

        if (owner.getId().equals(userToShareWith.getId())) {
            throw new IllegalArgumentException("You cannot share a note with yourself");
        }

        final NoteShare shareNote = new NoteShare(note, userToShareWith);
        note.getShares().add(shareNote);
        noteShareRepository.save(shareNote);
        log.debug("Synchronizing share to MongoDB note with id: '{}'", shareNote.getId());
        final NoteDocument document = noteMapper.toDocument(note);
        final NoteDocument syncDocument = noteSearchRepository.save(document);
        log.debug("Synchronized share to MongoDB note with id: '{}'", syncDocument.getId());

        log.info("Shared note '{}' from user '{}' to: '{}'", noteId, ownerUsername, request.username());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NoteResponse> searchNotes(String text, Set<String> tags, String username) {
        log.info("Searching notes by text: '{}', tags: {}, for user: '{}'", text, tags, username);

        // Searching is performed on MongoDB (results are ordered)
        final List<NoteDocument> searchResults = noteSearchRepository.searchNotes(text, tags, username);
        final List<UUID> noteIds = searchResults.stream()
                .map(doc -> UUID.fromString(doc.getId()))
                .toList();

        if (noteIds.isEmpty()) return List.of();

        // IDs found on MongoDB are used to query relational database
        final Map<UUID, NoteEntity> notesMap = noteRepository.findAllById(noteIds).stream()
                .collect(Collectors.toMap(NoteEntity::getId, Function.identity()));

        // Preserve MongoDB query result order (map acts as intermediate bucket)
        final List<NoteResponse> matchNotes = noteIds.stream()
                .map(notesMap::get)
                .filter(Objects::nonNull)
                .map(note -> noteMapper.toResponse(note, username))
                .toList();

        log.info("Found {} notes matching search criteria.", matchNotes.size());
        return matchNotes;
    }

}