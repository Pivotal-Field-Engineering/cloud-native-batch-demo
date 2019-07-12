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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.sql.DataSource;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TradesLoaderApplication.class)
@ContextConfiguration(classes = {ARatedTradesExtractorJobTest.BatchJobTestConfiguration.class})
public class ARatedTradesExtractorJobTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Before
    public void setUp() throws Exception {
        jdbcTemplate.update("delete from trade");
    }

    @Test
    public void testExtraction() throws Exception {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        jdbcTemplate.update("insert into trade " +
            "   (trade_id, account_number, amount, rating, shares, symbol, trade_date_time, update_date_time)" +
            "values" +
            "   (1, 'abc', 12, 'A', 12, 'ABC', '2019-06-26T01:01:00.000Z','" + sdf.format(new Date()) + "')");

        JobExecution jobExecution = jobLauncherTestUtils.launchJob(new JobParametersBuilder().addString(
            "localFilePath", "target/").toJobParameters());

        assertEquals("Incorrect batch status", BatchStatus.COMPLETED, jobExecution.getStatus());

        assertEquals("Invalid number of step executions", 1, jobExecution.getStepExecutions().size());

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
                public void setJob(@Qualifier("aRatedTradesExtractorJob") Job job) {
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
