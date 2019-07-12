package io.pivotal.dragonstonefinance.tradesloader.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Trade {

    private Long tradeId;
    private String accountNumber;
    private String symbol;
    private BigDecimal amount;
    private BigDecimal shares;
    private String rating;
    private Date tradeDateTime;
    private Date updateDateTime;

}
