package com.dipa.notefournote.notes;

import com.dipa.notefournote.common.dto.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.UUID;


@Slf4j
@RestController
@RequestMapping("/api/v1/notes")
@RequiredArgsConstructor
@Tag(name = "2. Notes Management", description = "API per la gestione delle note")
@SecurityRequirement(name = "bearerAuth")
public class NoteController {

    private final NoteService noteService;

    @Operation(summary = "Crea una nuova nota")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Nota creata con successo"),
            @ApiResponse(responseCode = "400", description = "Errore di validazione")
    })
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

    @Operation(summary = "Ottiene tutte le note associate all'utente", description = "Restituisce una lista con tutte le note di cui l'utente ha visibilità (create da lui e convidise da altri con lui).")
    @ApiResponse(responseCode = "200", description = "Lista di note recuperata con successo")
    @GetMapping
    public ResponseEntity<List<NoteResponse>> getAllNotes(Authentication authentication) {

        final String username = authentication.getName();
        log.debug("Received request to get all notes from user: '{}'", username);

        final List<NoteResponse> notes = noteService.findAllNotesByUsername(username);

        log.debug("Successfully fetched notes for user: '{}'", username);
        return ResponseEntity.ok(notes);
    }

    @Operation(summary = "Trova una nota tramite ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Nota trovata"),
            @ApiResponse(responseCode = "403", description = "Accesso non autorizzato", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Nota non trovata", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{noteId}")
    public ResponseEntity<NoteResponse> getNoteById(@Parameter(description = "ID della nota") @PathVariable UUID noteId,
                                                    Authentication authentication) {

        final String username = authentication.getName();
        log.debug("Received request from user '{}' to get note: '{}'", username, noteId);

        final NoteResponse note = noteService.findNoteById(noteId, username);

        log.debug("Successfully fetched note for user: '{}'", username);
        return ResponseEntity.ok(note);
    }

    @Operation(summary = "Aggiorna una nota esistente", description = "Sostituisce completamente una nota di cui l'utente è proprietario.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Nota aggiornata con successo"),
            @ApiResponse(responseCode = "400", description = "Errore di validazione"),
            @ApiResponse(responseCode = "403", description = "Accesso non autorizzato (solo il proprietario può modificare)"),
            @ApiResponse(responseCode = "404", description = "Nota non trovata")
    })
    @PutMapping("/{noteId}")
    public ResponseEntity<NoteResponse> updateNote(@Parameter(description = "ID della nota da aggiornare") @PathVariable UUID noteId,
                                                   @Valid @RequestBody UpdateNoteRequest request,
                                                   Authentication authentication) {

        final String username = authentication.getName();
        log.debug("Received request to update note '{}' from user: '{}'", noteId, username);

        final NoteResponse updatedNote = noteService.updateNote(noteId, request, username);

        log.debug("Successfully updated note '{}' for user: '{}'", noteId, username);
        return ResponseEntity.ok(updatedNote);
    }

    @Operation(summary = "Cancella una nota mediante ID", description = "Elimina una nota di cui l'utente è proprietario.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Nota cancellata con successo"),
            @ApiResponse(responseCode = "403", description = "Accesso non autorizzato (solo il proprietario può cancellare)"),
            @ApiResponse(responseCode = "404", description = "Nota non trovata")
    })
    @DeleteMapping("/{noteId}")
    public ResponseEntity<Void> deleteNote(@Parameter(description = "ID della nota da cancellare") @PathVariable UUID noteId,
                                           Authentication authentication) {

        final String username = authentication.getName();
        log.debug("Received request to delete note '{}' from user '{}'", noteId, username);

        noteService.deleteNote(noteId, username);

        log.debug("Successfully deleted note '{}' for user: '{}'", noteId, username);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Condivide una nota con un altro utente", description = "Permette al proprietario di una nota di condividerla con un altro utente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Nota condivisa con successo"),
            @ApiResponse(responseCode = "400", description = "Errore di validazione (es. condivisione con se stessi)"),
            @ApiResponse(responseCode = "403", description = "Accesso non autorizzato (solo il proprietario può condividere)"),
            @ApiResponse(responseCode = "404", description = "Nota o utente non trovato")
    })
    @PostMapping("/{noteId}/share")
    public ResponseEntity<Void> shareNote(
            @Parameter(description = "ID della nota da condividere") @PathVariable UUID noteId,
            @Valid @RequestBody ShareNoteRequest request,
            Authentication authentication) {

        final String ownerUsername = authentication.getName();
        log.debug("Received request from user '{}' to share note '{}' with: '{}'", ownerUsername, noteId, request.username());

        noteService.shareNote(noteId, request, ownerUsername);

        log.debug("Successfully shared note '{}' from user '{}' to: '{}'", noteId, ownerUsername, request.username());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Cerca tra le note dell'utente", description = "Filtra le note (di proprietà e in condivisione) per testo (su titolo e contenuto) e/o per tag.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ricerca completata con successo"),
            @ApiResponse(responseCode = "400", description = "Nessun criterio di ricerca fornito")
    })
    @GetMapping("/search")
    public ResponseEntity<List<NoteResponse>> searchNotes(
            @Parameter(description = "Testo da cercare nel titolo e nel contenuto") @RequestParam(name = "text", required = false) String text,
            @Parameter(description = "Set di tag per cui filtrare (logica AND)") @RequestParam(name = "tags", required = false) Set<String> tags,
            Authentication authentication) {

        final String username = authentication.getName();
        log.debug("Received request from user '{}' to search notes with: '{}' (text) | {} (tags)", username, text, tags);

        final List<NoteResponse> results = noteService.searchNotes(text, tags, username);

        log.debug("Successfully search notes for '{}' user with '{}' (text) | {} (tags): {}", username, text, tags, results);
        return ResponseEntity.ok(results);
    }

}