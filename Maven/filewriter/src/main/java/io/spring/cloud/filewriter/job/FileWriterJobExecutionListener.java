package io.spring.cloud.filewriter.job;


import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecution;

@Slf4j
public class FileWriterJobExecutionListener implements JobExecutionListener {


    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("file writer (*.csv,xml,json,fixed) 배치 프로그램");
        log.info("==========================");
    }
    @Override
    public void afterJob(JobExecution jobExecution) {

        long time = jobExecution.getEndTime().getTime() - jobExecution.getStartTime().getTime();
        int sum = jobExecution.getStepExecutions().stream()
                .mapToInt(StepExecution::getWriteCount)
                .sum();

        log.info("==========================");
        log.info("총 데이터 처리 {}건, 처리 시간 {}millis", sum, time);

    }


}
