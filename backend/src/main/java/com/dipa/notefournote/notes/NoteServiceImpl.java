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

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoteServiceImpl implements NoteService {

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final NoteMapper noteMapper;
    private final NoteShareRepository noteShareRepository;

    @Override
    @Transactional
    public NoteResponse createNote(CreateNoteRequest request, String username) {
        log.info("Creating a new note for user '{}' with title: '{}'", request.title(), username);

        final UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        final NoteEntity newNote = noteMapper.toEntity(request);
        newNote.setUser(user);

        final NoteEntity savedNote = noteRepository.save(newNote);
        log.info("Note created successfully for user '{}' with id: {}", username, savedNote.getId());

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

        note.setTitle(request.title());
        note.setContent(request.content());

        final NoteEntity updatedEntity = noteRepository.save(note);
        final NoteResponse updatedNote = noteMapper.toResponse(updatedEntity, username);

        log.info("Updated note with id '{}' for user: '{}'", noteId, username);
        return updatedNote;
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
        noteShareRepository.save(shareNote);

        log.info("Shared note '{}' from user '{}' to: '{}'", noteId, ownerUsername, request.username());
    }

}