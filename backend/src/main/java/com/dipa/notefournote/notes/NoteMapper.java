package com.dipa.notefournote.notes;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface NoteMapper {

    NoteResponse toResponse(NoteEntity entity);

    // Ignoring fields that are not available in DTO class
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    NoteEntity toEntity(CreateNoteRequest request);

    default NoteResponse toResponse(NoteEntity note, String currentUsername) {

        final String ownerUsername = note.getUser().getUsername();
        final NoteOwnership ownership = ownerUsername.equals(currentUsername)
                ? NoteOwnership.OWNED
                : NoteOwnership.SHARED_WITH_ME;

        final Set<String> sharedWithUsernames = ownership == NoteOwnership.OWNED
                ? note.getShares().stream()
                                  .map(share -> share.getSharedWithUser().getUsername())
                                  .collect(Collectors.toSet())
                : Set.of();

        return NoteResponse.builder()
                           .id(note.getId())
                           .title(note.getTitle())
                           .content(note.getContent())
                           .createdAt(note.getCreatedAt())
                           .updatedAt(note.getUpdatedAt())
                           .ownerUsername(ownerUsername)
                           .ownership(ownership)
                           .sharedWith(sharedWithUsernames)
                           .build();
    }

}