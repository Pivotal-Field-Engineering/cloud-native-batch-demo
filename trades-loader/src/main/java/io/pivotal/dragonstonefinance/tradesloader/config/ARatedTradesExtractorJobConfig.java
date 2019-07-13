package io.pivotal.dragonstonefinance.tradesloader.config;

import io.pivotal.dragonstonefinance.tradesloader.domain.Trade;
import lombok.extern.java.Log;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

@Log
@Configuration
public class ARatedTradesExtractorJobConfig extends BaseJobConfig {

    @Bean
    public Job aRatedTradesExtractorJob() {
        return jobBuilderFactory.get("aRatedTradesExtractorJob")
            .incrementer(new RunIdIncrementer())
            .start(writeFile())
            .next(moveFile())
            .build();
    }


    public Step writeFile() {
        return stepBuilderFactory.get("write-file")
            .<Trade, Trade>chunk(10)
            .reader(jdbcCursorItemReader())
            .writer(fileWriter(null, null))
            .build();
    }

    private Step moveFile() {
        return stepBuilderFactory.get("copy-file")
            .tasklet(new Tasklet() {

                @Override
                public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {

                    JobParameters jobParameters = chunkContext.getStepContext().getStepExecution()
                        .getJobParameters();

                    String localFilePath = jobParameters.getString("localFilePath");
                    String tempDir = jobParameters.getString("tempDir");

                    String fullFilePath = localFilePath + getTargetFileName();
                    String tempFilePath = getLocalTempFile(tempDir);

                    File dir = new File(localFilePath);
                    if(!dir.exists()) {
                        log.info(String.format("Target directory, %s, does not exist, so creating it now.", localFilePath));
                        dir.mkdirs();
                    } else {
                        log.info(String.format("Target directory, %s, does exists.  We are good to go.", localFilePath));
                    }

                    log.info(String.format("Copying from %s to %s", tempFilePath, fullFilePath));

                    Files.copy(Paths.get(tempFilePath), Paths.get(fullFilePath), StandardCopyOption.REPLACE_EXISTING);

                    return RepeatStatus.FINISHED;

                }
            })
            .build();
    }


    @Bean(destroyMethod="")
    public JdbcCursorItemReader jdbcCursorItemReader() {

        // Note: This is terrible date logic.  But ok for demo that requires consistent sql between h2 and mysql
        Calendar cal = Calendar.getInstance();
        Date currentDate = cal.getTime();
        cal.add(Calendar.DAY_OF_MONTH, 1);
        Date tomorrow = cal.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        JdbcCursorItemReader jdbcCursorItemReader = new JdbcCursorItemReader<>();

        String sql = "select " +
            "   trade_id, account_number, amount, rating, shares, symbol, trade_date_time, update_date_time " +
            "from " +
            "   trade " +
            "where " +
            "   update_date_time >= '" + sdf.format(currentDate) + "' AND " +
            "   update_date_time < '" + sdf.format(tomorrow) + "' AND " +
            "   rating = 'A'" +
            "";

        log.info(String.format("Sql statement: %s", sql));

        jdbcCursorItemReader.setSql(sql);
        jdbcCursorItemReader.setDataSource(dataSource);
        jdbcCursorItemReader.setRowMapper(new BeanPropertyRowMapper<>(Trade.class));
        return jdbcCursorItemReader;
    }

    @Bean
    @StepScope
    public FlatFileItemWriter<Trade> fileWriter(@Value("#{jobParameters['localFilePath']}") String filePath, @Value("#{jobParameters['tempDir']}") String tempDir)
    {

        FlatFileItemWriter<Trade> writer = new FlatFileItemWriter<>();

        String tempFilePath = getLocalTempFile(tempDir);

        log.info(String.format("Writing to temp file: %s", tempFilePath));

        writer.setResource(new FileSystemResource(tempFilePath));

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

    private String getLocalTempFile(String tempDir) {

        return tempDir + getTempFileName();

    }

    private String getTempFileName() {

        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        return "a-rated-trades-" + formatter.format(new Date()) + ".txt";

    }

    private String getTargetFileName() {

        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        return "a-rated-trades-" + formatter.format(new Date()) + ".txt";

    }


}
