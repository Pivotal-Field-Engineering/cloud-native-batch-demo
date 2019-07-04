package io.pivotal.dragonstonefinance.auditorapi.repostitory;

import io.pivotal.dragonstonefinance.auditorapi.domain.Trade;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TradeRepository extends JpaRepository<Trade, String> { }
