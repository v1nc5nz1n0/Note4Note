package com.dipa.notefournote.notes;

import com.dipa.notefournote.users.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NoteRepository extends JpaRepository<NoteEntity, UUID> {

    List<NoteEntity> findAllByUser(UserEntity user);

}