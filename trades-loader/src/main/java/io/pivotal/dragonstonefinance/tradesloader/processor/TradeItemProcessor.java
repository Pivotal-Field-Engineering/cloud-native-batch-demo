package io.pivotal.dragonstonefinance.tradesloader.processor;

import io.pivotal.dragonstonefinance.tradesloader.domain.Trade;
import lombok.extern.java.Log;
import org.springframework.batch.item.ItemProcessor;

import java.util.Date;

@Log
public class TradeItemProcessor implements ItemProcessor<Trade, Trade> {

    @Override
    public Trade process(Trade trade) throws Exception {

        //TODO: Call out to the rating api

        Trade processedTrade = new Trade(
            null,
            trade.getAccountNumber().toUpperCase(),
            trade.getSymbol().toUpperCase(),
            trade.getAmount(),
            trade.getShares(),
            null,
            trade.getUpdateDateTime()

        );

        log.info("Processed: " + trade + " into: " + processedTrade);

        return processedTrade;

    }
}
