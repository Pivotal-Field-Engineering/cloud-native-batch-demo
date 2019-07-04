package io.pivotal.dragonstonefinance.ratingsloader.mapper.fieldset;

import io.pivotal.dragonstonefinance.ratingsloader.domain.Rating;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

public class RatingFieldSetMapper implements FieldSetMapper<Rating> {
    @Override
    public Rating mapFieldSet(FieldSet fieldSet) throws BindException {
        return new Rating(fieldSet.readString(0),
            fieldSet.readString(1),
            fieldSet.readString(2),
            null);
    }
}
