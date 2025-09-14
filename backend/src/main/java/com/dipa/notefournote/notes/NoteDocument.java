package com.dipa.notefournote.notes;

import jakarta.persistence.Id;
import lombok.Data;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Data
@Document(collection = "notes_index")
public class NoteDocument {

    @Id
    private String id;

    @TextIndexed(weight = 2f)
    @Field
    private String title;

    @TextIndexed
    @Field
    private String content;

    @Field
    private List<String> tags;

    @Field
    private String ownerUsername;

    @Field
    private List<String> sharedWithUsernames;

}