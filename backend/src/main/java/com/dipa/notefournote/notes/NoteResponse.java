package com.dipa.notefournote.notes;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record NoteResponse(
        UUID id,
        String title,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String ownerUsername,
        NoteOwnership ownership,
        Set<String> sharedWithUsernames,
        Set<String> tags
) {}