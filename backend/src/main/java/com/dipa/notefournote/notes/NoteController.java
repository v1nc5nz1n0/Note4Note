package com.dipa.notefournote.notes;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;


@Slf4j
@RestController
@RequestMapping("/api/v1/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;

    @PostMapping
    public ResponseEntity<NoteResponse> createNote(@Valid @RequestBody CreateNoteRequest request,
            Authentication authentication) {

        final String username = authentication.getName();
        log.debug("Received request to create note from user: '{}'", username);

        final NoteResponse createdNote = noteService.createNote(request, username);

        final URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdNote.id())
                .toUri();

        log.debug("Successfully created note for user: '{}'", username);
        return ResponseEntity.created(location).body(createdNote);
    }

    @GetMapping
    public ResponseEntity<List<NoteResponse>> getAllNotes(Authentication authentication) {

        final String username = authentication.getName();
        log.debug("Received request to get all notes from user: '{}'", username);

        final List<NoteResponse> notes = noteService.findAllNotesByUsername(username);

        log.debug("Successfully fetched notes for user: '{}'", username);
        return ResponseEntity.ok(notes);
    }

    @GetMapping("/{noteId}")
    public ResponseEntity<NoteResponse> getNoteById(@PathVariable UUID noteId, Authentication authentication) {

        final String username = authentication.getName();
        log.debug("Received request from user '{}' to get note: '{}'", username, noteId);

        final NoteResponse note = noteService.findNoteById(noteId, username);

        log.debug("Successfully fetched note for user: '{}'", username);
        return ResponseEntity.ok(note);
    }

    @PutMapping("/{noteId}")
    public ResponseEntity<NoteResponse> updateNote(@PathVariable UUID noteId,
                                                   @Valid @RequestBody UpdateNoteRequest request,
                                                   Authentication authentication) {

        final String username = authentication.getName();
        log.debug("Received request to update note '{}' from user: '{}'", noteId, username);

        final NoteResponse updatedNote = noteService.updateNote(noteId, request, username);

        log.debug("Successfully updated note '{}' for user: '{}'", noteId, username);
        return ResponseEntity.ok(updatedNote);
    }

    @DeleteMapping("/{noteId}")
    public ResponseEntity<Void> deleteNote(@PathVariable UUID noteId, Authentication authentication) {

        final String username = authentication.getName();
        log.debug("Received request to delete note '{}' from user '{}'", noteId, username);

        noteService.deleteNote(noteId, username);

        log.debug("Successfully deleted note '{}' for user: '{}'", noteId, username);
        return ResponseEntity.noContent().build();
    }

}