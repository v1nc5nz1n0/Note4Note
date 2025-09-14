package com.dipa.notefournote.notes;

import com.dipa.notefournote.users.UserEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "note_shares", uniqueConstraints = {
        // Avoid sharing note with same user
        @UniqueConstraint(columnNames = {"note_id", "shared_with_user_id"})
})
public class NoteShare {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "note_id", nullable = false)
    private NoteEntity note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shared_with_user_id", nullable = false)
    private UserEntity sharedWithUser;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime sharedAt;

    public NoteShare(NoteEntity note, UserEntity sharedWithUser) {
        this.note = note;
        this.sharedWithUser = sharedWithUser;
    }

}