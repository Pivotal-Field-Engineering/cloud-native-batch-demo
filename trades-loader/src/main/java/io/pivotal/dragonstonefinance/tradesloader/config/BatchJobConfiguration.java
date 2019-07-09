package io.pivotal.dragonstonefinance.tradesloader.config;

import io.pivotal.dragonstonefinance.tradesloader.domain.Trade;
import io.pivotal.dragonstonefinance.tradesloader.mapper.fieldset.TradeFieldSetMapper;
import io.pivotal.dragonstonefinance.tradesloader.processor.TradeItemProcessor;
import io.pivotal.dragonstonefinance.tradesloader.tasklet.FileDeletingTasklet;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;

@Configuration
@EnableBatchProcessing
public class BatchJobConfiguration {

    private final DataSource dataSource;

    private final ResourceLoader resourceLoader;

    private final JobBuilderFactory jobBuilderFactory;

    private final StepBuilderFactory stepBuilderFactory;

    @Autowired
    public BatchJobConfiguration(@Qualifier(value = "appDataSource") final DataSource dataSource, final JobBuilderFactory jobBuilderFactory,
                                 final StepBuilderFactory stepBuilderFactory,
                                 final ResourceLoader resourceLoader) {
        this.dataSource = dataSource;
        this.resourceLoader = resourceLoader;
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
    }

    @Bean
    @StepScope
    public ItemStreamReader<Trade> reader(@Value("#{jobParameters['localFilePath']}") String filePath) {

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
            .names(new String[]{"AccountNumber", "Symbol", "Amount", "Shares", "TradeDateTime"})
            .fieldSetMapper(new TradeFieldSetMapper())
            .linesToSkip(1)
            .build();
    }

    @Bean
    public ItemProcessor<Trade, Trade> processor() {
        return new TradeItemProcessor();
    }

    @Bean
    public ItemWriter<Trade> writer() {
        return new JdbcBatchItemWriterBuilder<Trade>()
            .beanMapped()
            .dataSource(this.dataSource)
            .sql("INSERT INTO trade " +
                "(trade_id, account_number, symbol, amount, shares, rating, update_date_time) " +
                "VALUES " +
                "(:tradeId, :accountNumber, :symbol, :amount, :shares, :rating, :updateDateTime)")
            .build();
    }

    @Bean
    public Job tradesLoaderJob() {
        return jobBuilderFactory.get("tradesLoaderJob")
            .incrementer(new RunIdIncrementer())
            .start(step1())
            .next(step2())
            .build();
    }

    @Bean
    public Step step1() {
        return stepBuilderFactory.get("load-data")
            .<Trade, Trade>chunk(10)
            .reader(reader(null))
            .processor(processor())
            .writer(writer())
            .build();
    }

    @Bean
    public Step step2() {

        FileDeletingTasklet task = new FileDeletingTasklet();
        task.setResourceLoader(resourceLoader);

        return stepBuilderFactory.get("delete-source-file")
            .tasklet(task)
            .build();
    }

}
