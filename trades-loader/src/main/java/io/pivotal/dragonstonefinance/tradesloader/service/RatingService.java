package io.pivotal.dragonstonefinance.tradesloader.service;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import io.pivotal.dragonstonefinance.tradesloader.domain.Rating;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.CircuitBreaker;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


@Service
@Log
public class RatingService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${pivotal.downstream-protocol:http}")
    protected String downstreamProtocol;

    @HystrixCommand(fallbackMethod = "getRatingFallback")
    public String getRating(String symbol) {

        log.info(String.format("Attempting to retrieve rating for %s", symbol));

        Rating rating = restTemplate.getForObject(downstreamProtocol + "://ratings-api/ratings/{symbol}", Rating.class, symbol);

        log.info(String.format("Retrieved rating of %s for symbol %s", rating.getValue(), symbol));

        return rating.getValue();

    }

    public String getRatingFallback(String symbol) {
        log.warning(String.format("Failed to retrieve rating for %s.  Returning ERROR", symbol));
        return "ERROR";
    }




}
