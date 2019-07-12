package io.pivotal.dragonstonefinance.tradesloader.config;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ResourceLoader;

import javax.sql.DataSource;

@EnableBatchProcessing
abstract class BaseJobConfig {

    @Autowired
    @Qualifier(value = "appDataSource")
    protected final DataSource dataSource = null;

    @Autowired
    protected final ResourceLoader resourceLoader = null;

    @Autowired
    protected final JobBuilderFactory jobBuilderFactory = null;

    @Autowired
    protected final StepBuilderFactory stepBuilderFactory = null;


}
