package io.pivotal.dragonstonefinance.tradesloader.tasklet;

import lombok.extern.java.Log;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

@Log
public class HelloWorldTasklet implements Tasklet {
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

        log.info("Hello World!");
        return RepeatStatus.FINISHED;
    }

}
