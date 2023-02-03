package io.spring.cloud.filewriter.job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.util.StringUtils;

@Slf4j
public class FileWriterDecider implements JobExecutionDecider {

    @Override
        public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {
            JobParameters jobParameters = jobExecution.getJobParameters();

            String outputfile = jobParameters.getString("outputfile");


            if(StringUtils.endsWithIgnoreCase(outputfile, "csv")){
                log.info("outputfile extension : .csv");
                return new FlowExecutionStatus("csv");

            }else if(StringUtils.endsWithIgnoreCase(outputfile, "txt")) {
                log.info("outputfile extension : .txt");
                return new FlowExecutionStatus("txt");

            }else if(StringUtils.endsWithIgnoreCase(outputfile, "json")){
                log.info("outputfile extension : .json");
                return new FlowExecutionStatus("json");

            }else if(StringUtils.endsWithIgnoreCase(outputfile, "xml")){
                log.info("outputfile extension : .xml");
                return new FlowExecutionStatus("xml");
            };


        return null;
    }


}
