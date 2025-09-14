package com.dipa.notefournote.notes;

import com.dipa.notefournote.exception.InvalidSearchCriteriaException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;

@Slf4j
@Repository
@RequiredArgsConstructor
public class CustomNoteSearchRepositoryImpl implements CustomNoteSearchRepository {

    private final MongoTemplate mongoTemplate;

    @Override
    public List<NoteDocument> searchNotes(String text, Set<String> tags, String username) {

        final boolean isTextPresent = StringUtils.hasText(text);
        final boolean areTagsPresent = tags != null && !tags.isEmpty();

        if (!isTextPresent && !areTagsPresent) {
            throw new InvalidSearchCriteriaException("At least one search criterion (text or tags) must be provided.");
        }

        final Query query = new Query();
        final Criteria userAccessCriteria = new Criteria().orOperator(
                Criteria.where("ownerUsername").is(username),
                Criteria.where("sharedWithUsernames").is(username)
        );
        query.addCriteria(userAccessCriteria);

        if (StringUtils.hasText(text)) {
            query.addCriteria(TextCriteria.forDefaultLanguage().matching(text));
        }

        if (tags != null && !tags.isEmpty()) {
            query.addCriteria(Criteria.where("tags").all(tags));
        }
        log.debug("Search query as JSON: {}", query.getQueryObject().toJson());

        return mongoTemplate.find(query, NoteDocument.class);
    }

}
