package io.pivotal.dragonstonefinance.auditorapi.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Trade {

    @Id
    private Long tradeId;
    private String accountNumber;
    private String symbol;
    private BigDecimal amount;
    private BigDecimal shares;
    private String rating;
    private Date tradeDateTime;
    private Date updateDateTime;

}
