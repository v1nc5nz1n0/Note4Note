package com.dipa.notefournote.notes;

import org.mapstruct.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface NoteMapper {

    @Mapping(target = "ownerUsername", ignore = true)
    @Mapping(target = "ownership", ignore = true)
    @Mapping(target = "sharedWith", ignore = true)
    @Mapping(source = "tags", target = "tags", qualifiedByName = "mapTagsToNames")
    NoteResponse toResponse(NoteEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "shares", ignore = true)
    @Mapping(source = "tags", target = "tags", qualifiedByName = "mapNamesToTags")
    NoteEntity toEntity(CreateNoteRequest request);

    @Mapping(source = "user.username", target = "ownerUsername")
    @Mapping(source = "tags", target = "tags", qualifiedByName = "mapTagsToNamesAsList")
    @Mapping(source = "shares", target = "sharedWithUsernames", qualifiedByName = "mapSharesToUsernames")
    NoteDocument toDocument(NoteEntity entity);

    @AfterMapping
    default void setEmptyTags(@MappingTarget NoteDocument document) {
        if (document.getTags() == null) {
            document.setTags(new ArrayList<>());
        }
    }

    @Named("mapTagsToNames")
    static Set<String> mapTagsToNames(Set<TagEntity> tags) {
        return tags.stream()
                   .map(TagEntity::getName)
                   .collect(Collectors.toSet());
    }

    @Named("mapTagsToNamesAsList")
    static List<String> mapTagsToNamesAsList(Set<TagEntity> tags) {
        return tags.stream()
                .map(TagEntity::getName)
                .toList();
    }

    @Named("mapNamesToTags")
    static Set<TagEntity> mapNamesToTags(Set<String> names) {
        return names.stream()
                .map(TagEntity::new)
                .collect(Collectors.toSet());
    }

    @Named("mapSharesToUsernames")
    static List<String> mapSharesToUsernames(Set<NoteShare> shares) {
        return shares.stream()
                .map(share -> share.getSharedWithUser().getUsername())
                .toList();
    }

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

        final Set<String> tagNames = note.getTags().stream()
                .map(TagEntity::getName)
                .collect(Collectors.toSet());

        return NoteResponse.builder()
                           .id(note.getId())
                           .title(note.getTitle())
                           .content(note.getContent())
                           .createdAt(note.getCreatedAt())
                           .updatedAt(note.getUpdatedAt())
                           .ownerUsername(ownerUsername)
                           .ownership(ownership)
                           .sharedWith(sharedWithUsernames)
                           .tags(tagNames)
                           .build();
    }

}