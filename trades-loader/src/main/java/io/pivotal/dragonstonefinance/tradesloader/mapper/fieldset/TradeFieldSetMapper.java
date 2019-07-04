package io.pivotal.dragonstonefinance.tradesloader.mapper.fieldset;

import io.pivotal.dragonstonefinance.tradesloader.domain.Trade;
import lombok.extern.java.Log;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Log
public class TradeFieldSetMapper implements FieldSetMapper<Trade> {

    public static DateFormat fileDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    @Override
    public Trade mapFieldSet(FieldSet fieldSet) throws BindException {


        Date tradeDateTime = null;
        String updateDateTime = null;
        try {
            tradeDateTime = fileDateFormat.parse(fieldSet.readString(4));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return new Trade(null,
            fieldSet.readString(0),
            fieldSet.readString(1),
            BigDecimal.valueOf(fieldSet.readDouble(2)),
            BigDecimal.valueOf(fieldSet.readDouble(3)),
            null,
            tradeDateTime);
    }
}
