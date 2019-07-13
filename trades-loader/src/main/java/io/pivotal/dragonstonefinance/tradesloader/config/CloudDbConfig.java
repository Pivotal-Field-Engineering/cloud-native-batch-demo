package io.pivotal.dragonstonefinance.tradesloader.config;

import org.springframework.beans.factory.annotation.Value;
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
    public DataSource dataSource(@Value("${io.pivotal.dataflow-db-service-name:mysql}") String serviceName) {
        return connectionFactory().dataSource(serviceName);
    }

    @Bean
    public DataSource appDataSource() {
        return connectionFactory().dataSource("app-db");
    }

}