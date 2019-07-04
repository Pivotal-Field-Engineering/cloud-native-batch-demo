package io.pivotal.dragonstonefinance.ratingsloader;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.util.AssertionErrors.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = RatingsLoaderApplication.class)
@ContextConfiguration(classes = { RatingsLoaderApplicationTests.BatchJobTestConfiguration.class })
public class RatingsLoaderApplicationTests {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void testBatchDataProcessing() throws Exception {

        JobExecution jobExecution = jobLauncherTestUtils.launchJob(new JobParametersBuilder().addString(
            "localFilePath", "classpath:data.csv").toJobParameters());

        assertEquals("Incorrect batch status", BatchStatus.COMPLETED, jobExecution.getStatus());

        assertEquals("Invalid number of step executions", 1, jobExecution.getStepExecutions().size());

        List<Map<String, Object>> ratingsList = jdbcTemplate.queryForList(
            "select symbol, value, analyst from rating");

        assertEquals("Incorrect number of results", 3, ratingsList.size());

        for (Map<String, Object> rating : ratingsList) {
            assertNotNull("Received null rating", rating);

            String symbol = (String) rating.get("symbol");
            assertEquals("Invalid symbol: " + symbol, symbol.toUpperCase(), symbol);

            String value = (String) rating.get("value");
            assertEquals("Invalid value: " + value, value.toUpperCase(), value);

            String analyst = (String) rating.get("analyst");
            assertEquals("Invalid analyst: " + value, analyst.toUpperCase(), analyst);
        }
    }

    @Configuration
    @EnableAutoConfiguration
    public static class BatchJobTestConfiguration {

        @Autowired
        @Qualifier("appDataSource")
        private DataSource dataSource;

        @Bean
        public JobLauncherTestUtils jobLauncherTestUtils() {
            return new JobLauncherTestUtils();
        }

        @Bean
        public JdbcTemplate jdbcTemplate() {
            JdbcTemplate jdbcTemplate = new JdbcTemplate();
            jdbcTemplate.setDataSource(dataSource);
            return jdbcTemplate;
        }

    }
}
