package com.dipa.notefournote.users;

import com.dipa.notefournote.notes.NoteEntity;
import com.dipa.notefournote.notes.NoteShare;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @OneToMany(mappedBy = "user")
    private Set<NoteEntity> notes = new HashSet<>();

    @OneToMany(mappedBy = "sharedWithUser")
    private Set<NoteShare> receivedShares = new HashSet<>();

}