package io.pivotal.dragonstonefinance.tradesloader.processor;

import io.pivotal.dragonstonefinance.tradesloader.domain.Rating;
import io.pivotal.dragonstonefinance.tradesloader.domain.Trade;
import io.pivotal.dragonstonefinance.tradesloader.service.RatingService;
import lombok.extern.java.Log;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;

import java.util.Date;

@Log
public class TradeItemProcessor implements ItemProcessor<Trade, Trade> {

    @Autowired
    private RatingService ratingService;

    @Override
    public Trade process(Trade trade) throws Exception {

        String rating = ratingService.getRating(trade.getSymbol());

        Trade processedTrade = new Trade(
            null,
            trade.getAccountNumber().toUpperCase(),
            trade.getSymbol().toUpperCase(),
            trade.getAmount(),
            trade.getShares(),
            rating,
            trade.getUpdateDateTime()

        );

        log.info("Processed: " + trade + " into: " + processedTrade);

        return processedTrade;

    }
}
