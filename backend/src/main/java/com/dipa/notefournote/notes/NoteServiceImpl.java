package com.dipa.notefournote.notes;

import com.dipa.notefournote.exception.NoteNotFoundException;
import com.dipa.notefournote.users.UserEntity;
import com.dipa.notefournote.users.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoteServiceImpl implements NoteService {

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final NoteMapper noteMapper;


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

        return noteMapper.toResponse(savedNote);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NoteResponse> findAllNotesByUsername(String username) {
        log.info("Fetching all notes for user '{}'", username);

        final UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        final List<NoteResponse> notes = noteRepository.findAllByUser(user).stream()
                .map(noteMapper::toResponse)
                .toList();

        log.info("Fetched all notes for user '{}': {}", username, notes);
        return notes;
    }

    @Override
    @Transactional(readOnly = true)
    public NoteResponse findNoteById(UUID noteId, String username) {
        log.info("Fetching note with id '{}' for user: '{}'", noteId, username);

        final UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        final NoteResponse note = noteRepository.findByIdAndUser(noteId, user)
                .map(noteMapper::toResponse)
                .orElseThrow(() -> new NoteNotFoundException("Note not found with id: " + noteId));

        log.info("Fetched note for user '{}': {}", username, note);
        return note;
    }

    @Override
    @Transactional
    public NoteResponse updateNote(UUID noteId, UpdateNoteRequest request, String username) {
        log.info("Updating note with id '{}' for user: '{}'", noteId, username);

        final UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        final NoteEntity note = noteRepository.findByIdAndUser(noteId, user)
                .orElseThrow(() -> new NoteNotFoundException("Note not found with id: " + noteId));

        note.setTitle(request.title());
        note.setContent(request.content());

        final NoteEntity updatedEntity = noteRepository.save(note);
        final NoteResponse updatedNote = noteMapper.toResponse(updatedEntity);

        log.info("Updated note with id '{}' for user: '{}'", noteId, username);
        return updatedNote;
    }

    @Override
    @Transactional
    public void deleteNote(UUID noteId, String username) {
        log.info("Deleting note with id '{}' for user: '{}'", noteId, username);

        final UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        final NoteEntity note = noteRepository.findByIdAndUser(noteId, user)
                .orElseThrow(() -> new NoteNotFoundException("Note not found with id: " + noteId));

        noteRepository.delete(note);

        log.info("Deleted note with id '{}' for user: '{}'", noteId, username);
    }

}