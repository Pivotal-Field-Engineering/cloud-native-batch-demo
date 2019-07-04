package io.pivotal.dragonstonefinance.ratingsloader.config;

import io.pivotal.dragonstonefinance.ratingsloader.domain.Rating;
import io.pivotal.dragonstonefinance.ratingsloader.mapper.fieldset.RatingFieldSetMapper;
import io.pivotal.dragonstonefinance.ratingsloader.processor.RatingItemProcessor;
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
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;

@Configuration
@EnableBatchProcessing
public class BatchJobConfiguration {

    @Autowired
    @Qualifier(value = "appDataSource")
    private final DataSource appDataSource = null;

    @Autowired
    private final ResourceLoader resourceLoader = null;

    @Autowired
    private final JobBuilderFactory jobBuilderFactory = null;

    @Autowired
    private final StepBuilderFactory stepBuilderFactory = null;

//    @Autowired
//    public BatchJobConfiguration(@Qualifier(value="appDataSource") final DataSource appDataSource, final JobBuilderFactory jobBuilderFactory,
//                              final StepBuilderFactory stepBuilderFactory,
//                              final ResourceLoader resourceLoader) {
//        this.appDataSource = appDataSource;
//        this.resourceLoader = resourceLoader;
//        this.jobBuilderFactory = jobBuilderFactory;
//        this.stepBuilderFactory = stepBuilderFactory;
//    }

    @Bean
    @StepScope
    public ItemStreamReader<Rating> reader(@Value("#{jobParameters['localFilePath']}") String filePath) {

        if (StringUtils.isEmpty(filePath)) {
            throw new IllegalArgumentException("Must supply localFilePath as a job parameter.");
        }

        if (!filePath.matches("[a-z]+:.*")) {
            filePath = "file:" + filePath;
        }

        return new FlatFileItemReaderBuilder<Rating>()
            .name("reader")
            .resource(resourceLoader.getResource(filePath))
            .delimited()
            .names(new String[]{"symbol", "value", "analyst"})
            .fieldSetMapper(new RatingFieldSetMapper())
            .build();
    }

    @Bean
    public ItemProcessor<Rating, Rating> processor() {
        return new RatingItemProcessor();
    }

    @Bean
    public ItemWriter<Rating> writer() {
        return new JdbcBatchItemWriterBuilder<Rating>()
            .beanMapped()
            .dataSource(this.appDataSource)
            .sql("REPLACE INTO rating (symbol, value, analyst, update_date_time) VALUES (:symbol, :value, :analyst, :updateDateTime)")
            .build();
    }

    @Bean
    public Job ratingsLoaderJob() {
        return jobBuilderFactory.get("ratingsLoaderJob")
            .incrementer(new RunIdIncrementer())
            .flow(step1())
            .end()
            .build();
    }

    @Bean
    public Step step1() {
        return stepBuilderFactory.get("load")
            .<Rating, Rating>chunk(10)
            .reader(reader(null))
            .processor(processor())
            .writer(writer())
            .build();
    }


}
