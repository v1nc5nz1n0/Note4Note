package com.dipa.notefournote.notes;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NoteSearchRepository extends MongoRepository<NoteDocument, String>, CustomNoteSearchRepository {
}