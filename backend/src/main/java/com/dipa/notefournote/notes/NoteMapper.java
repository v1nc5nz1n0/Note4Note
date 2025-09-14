package com.dipa.notefournote.notes;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NoteMapper {

    NoteResponse toResponse(NoteEntity entity);

    // Ignoring fields that are not available in DTO class
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    NoteEntity toEntity(CreateNoteRequest request);

}