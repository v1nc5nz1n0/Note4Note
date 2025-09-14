package com.dipa.notefournote.notes;

import java.util.List;
import java.util.Set;

public interface CustomNoteSearchRepository {

    List<NoteDocument> searchNotes(String text, Set<String> tags, String username);

}