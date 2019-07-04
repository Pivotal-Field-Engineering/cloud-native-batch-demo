package io.pivotal.dragonstonefinance.ratingsapi.repostitory;

import io.pivotal.dragonstonefinance.ratingsapi.domain.Rating;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RatingRepository extends JpaRepository<Rating, String> { }
