package io.pivotal.dragonstonefinance.tradesloader.config;

import org.springframework.cloud.config.java.AbstractCloudConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

@Configuration
@Profile("cloud")
public class CloudDbConfig extends AbstractCloudConfig {

    @Primary
    @Bean
    public DataSource dataSource() {
        return connectionFactory().dataSource("mysql");
    }

    @Bean
    public DataSource appDataSource() {
        return connectionFactory().dataSource("app-db");
    }

}