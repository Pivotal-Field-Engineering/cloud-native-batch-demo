package io.pivotal.dragonstonefinance.tradesloader.config;

import io.pivotal.dragonstonefinance.tradesloader.domain.Trade;
import io.pivotal.dragonstonefinance.tradesloader.mapper.fieldset.TradeFieldSetMapper;
import io.pivotal.dragonstonefinance.tradesloader.processor.TradeItemProcessor;
import io.pivotal.dragonstonefinance.tradesloader.tasklet.FileDeletingTasklet;
import lombok.extern.java.Log;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Log
@Configuration
public class TradesLoaderJobConfig extends BaseJobConfig {

    @Bean
    public Job tradesLoaderJob() {
        return jobBuilderFactory.get("tradesLoaderJob")
            .incrementer(new RunIdIncrementer())
            .start(loadData())
            .next(deleteFile())
            .build();
    }

    public Step loadData() {
        return stepBuilderFactory.get("load-data")
            .<Trade, Trade>chunk(10)
            .reader(reader(null))
            .processor(processor())
            .writer(jdbcWriter())
            .build();
    }

    public Step deleteFile() {

        FileDeletingTasklet task = new FileDeletingTasklet();
        task.setResourceLoader(resourceLoader);

        return stepBuilderFactory.get("delete-source-file")
            .tasklet(task)
            .build();
    }

    @Bean
    @StepScope
    public ItemStreamReader<Trade> reader(@Value("#{jobParameters['localFilePath']}") String filePath) {

        log.info("localFilePath=" + filePath);

        if (StringUtils.isEmpty(filePath)) {
            throw new IllegalArgumentException("Must supply localFilePath as a job parameter.");
        }

        if (!filePath.matches("[a-z]+:.*")) {
            filePath = "file:" + filePath;
        }

        return new FlatFileItemReaderBuilder<Trade>()
            .name("reader")
            .resource(resourceLoader.getResource(filePath))
            .delimited()
            .names(new String[]{"AccountNumber", "Symbol", "Amount", "Shares", "TradeDateTime", "UpdateDateTime"})
            .fieldSetMapper(new TradeFieldSetMapper())
            .linesToSkip(1)
            .build();
    }

    @Bean
    public ItemProcessor<Trade, Trade> processor() {
        return new TradeItemProcessor();
    }

    @Bean
    public ItemWriter<Trade> jdbcWriter() {
        return new JdbcBatchItemWriterBuilder<Trade>()
            .beanMapped()
            .dataSource(this.dataSource)
            .sql("INSERT INTO trade " +
                "(trade_id, account_number, symbol, amount, shares, rating, update_date_time) " +
                "VALUES " +
                "(:tradeId, :accountNumber, :symbol, :amount, :shares, :rating, :updateDateTime)")
            .build();
    }


}
