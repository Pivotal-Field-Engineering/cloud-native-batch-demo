package io.pivotal.dragonstonefinance.ratingsloader.tasklet;

import lombok.extern.java.Log;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.UnexpectedJobExecutionException;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.Assert;

import java.io.File;

@Log
public class FileDeletingTasklet implements Tasklet, InitializingBean {
    private ResourceLoader resourceLoader;

    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

        String localFilePath = chunkContext.getStepContext().getStepExecution()
            .getJobParameters().getString("localFilePath");

        if (!localFilePath.matches("[a-z]+:.*")) {
            localFilePath = "file:" + localFilePath;
        }


        File file = resourceLoader.getResource(localFilePath).getFile();
        log.info("Deleting file: " + localFilePath);
        boolean deleted = file.delete();
        if (!deleted) {
            throw new UnexpectedJobExecutionException("Could not delete file " + file.getPath());
        }
        return RepeatStatus.FINISHED;
    }

    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(resourceLoader, "resourceLoader must be set");
    }
}
