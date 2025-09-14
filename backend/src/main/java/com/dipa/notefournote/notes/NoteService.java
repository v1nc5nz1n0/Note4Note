package com.dipa.notefournote.notes;

import java.util.List;
import java.util.UUID;

public interface NoteService {

    NoteResponse createNote(CreateNoteRequest request, String username);

    List<NoteResponse> findAllNotesByUsername(String username);

    NoteResponse findNoteById(UUID noteId, String username);

    NoteResponse updateNote(UUID noteId, UpdateNoteRequest request, String username);

    void deleteNote(UUID noteId, String username);

    void shareNote(UUID noteId, ShareNoteRequest request, String ownerUsername);

}