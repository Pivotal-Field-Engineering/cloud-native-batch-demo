package io.pivotal.dragonstonefinance.tradesloader.config;

import io.pivotal.dragonstonefinance.tradesloader.domain.Trade;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

@Configuration
public class ARatedTradesExtractorJobConfig extends BaseJobConfig {

    @Bean
    public Job aRatedTradesExtractorJob() {
        return jobBuilderFactory.get("aRatedTradesExtractorJob")
            .incrementer(new RunIdIncrementer())
            .start(writeFile())
            .build();
    }

    public Step writeFile() {
        return stepBuilderFactory.get("write-file")
            .<Trade, Trade>chunk(10)
            .reader(jdbcCursorItemReader())
            .writer(fileWriter(null))
            .build();
    }



    @Bean
    public JdbcCursorItemReader jdbcCursorItemReader() {


        // THIS IS TERRIBLE DATE LOGIC
        Calendar cal = Calendar.getInstance();
        Date currentDate = cal.getTime();
        cal.add(Calendar.DAY_OF_MONTH, 1);
        Date tomorrow = cal.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        JdbcCursorItemReader jdbcCursorItemReader = new JdbcCursorItemReader<>();
        jdbcCursorItemReader.setSql("select " +
            "   trade_id, account_number, amount, rating, shares, symbol, trade_date_time, update_date_time " +
            "from " +
            "   trade " +
            "where " +
            "   update_date_time >= '" + sdf.format(currentDate) + "' AND " +
            "   update_date_time < '" + sdf.format(tomorrow) + "' AND " +
            "   rating = 'A'");
        jdbcCursorItemReader.setDataSource(dataSource);
        jdbcCursorItemReader.setRowMapper(new BeanPropertyRowMapper<>(Trade.class));
        return jdbcCursorItemReader;
    }

    @Bean
    @StepScope
    public FlatFileItemWriter<Trade> fileWriter(@Value("#{jobParameters['localFilePath']}") String filePath)
    {
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

        formatter.format(new Date());
        //Create fileWriter instance
        FlatFileItemWriter<Trade> writer = new FlatFileItemWriter<>();

        Resource outputResource = new FileSystemResource(filePath + "ARatedTrades-" + formatter.format(new Date()) + ".csv");

        //Set output file location
        writer.setResource(outputResource);

        writer.setShouldDeleteIfExists(true);
        writer.setAppendAllowed(false);

        //Name field values sequence based on object properties
        writer.setLineAggregator(new DelimitedLineAggregator<Trade>() {
            {
                setDelimiter(",");
                setFieldExtractor(new BeanWrapperFieldExtractor<Trade>() {
                    {
                        setNames(new String[] { "tradeId", "accountNumber", "symbol", "amount", "shares", "rating", "tradeDateTime", "updateDateTime" });
                    }
                });
            }
        });
        return writer;
    }


}
