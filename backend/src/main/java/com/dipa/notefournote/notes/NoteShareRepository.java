package com.dipa.notefournote.notes;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface NoteShareRepository extends JpaRepository<NoteShare, UUID> {
}