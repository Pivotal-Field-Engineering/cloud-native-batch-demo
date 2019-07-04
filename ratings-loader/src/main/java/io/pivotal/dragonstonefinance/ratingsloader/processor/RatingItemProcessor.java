package io.pivotal.dragonstonefinance.ratingsloader.processor;

import io.pivotal.dragonstonefinance.ratingsloader.domain.Rating;
import lombok.extern.java.Log;
import org.springframework.batch.item.ItemProcessor;

import java.util.Date;

@Log
public class RatingItemProcessor implements ItemProcessor<Rating, Rating> {
    @Override
    public Rating process(Rating rating) throws Exception {

        Rating processedRating = new Rating(
            rating.getSymbol().toUpperCase(),
            rating.getValue().toUpperCase(),
            rating.getAnalyst().toUpperCase(),
            new Date()
        );

        log.info("Processed: " + rating + " into: " + processedRating);

        return processedRating;

    }
}
