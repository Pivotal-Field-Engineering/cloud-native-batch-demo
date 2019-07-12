package io.pivotal.dragonstonefinance.tradesloader;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TradesLoaderApplication.class)
@ContextConfiguration(classes = {TradesLoaderJobTest.BatchJobTestConfiguration.class})
public class TradesLoaderJobTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Before
    public void setUp() throws Exception {
        jdbcTemplate.update("delete from trade");
    }

    @Test
    public void testLoading() throws Exception {

        JobExecution jobExecution = jobLauncherTestUtils.launchJob(new JobParametersBuilder().addString(
            "localFilePath", "classpath:data.csv").toJobParameters());

        assertEquals("Incorrect batch status", BatchStatus.COMPLETED, jobExecution.getStatus());

        assertEquals("Invalid number of step executions", 2, jobExecution.getStepExecutions().size());

        List<Map<String, Object>> TradesList = jdbcTemplate.queryForList(
            "select * from trade");

        assertEquals("Incorrect number of results", 3, TradesList.size());

    }

    @Configuration
    @EnableAutoConfiguration
    public static class BatchJobTestConfiguration {

        @Autowired
        @Qualifier("appDataSource")
        private DataSource dataSource;

        @Bean
        public JobLauncherTestUtils jobLauncherTestUtils() {

            return new JobLauncherTestUtils() {
                @Override
                @Autowired
                public void setJob(@Qualifier("tradesLoaderJob") Job job) {
                    super.setJob(job);
                }
            };

        }

        @Bean
        public JdbcTemplate jdbcTemplate() {
            JdbcTemplate jdbcTemplate = new JdbcTemplate();
            jdbcTemplate.setDataSource(dataSource);
            return jdbcTemplate;
        }

    }
}
