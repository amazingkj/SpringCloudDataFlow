package io.spring.cloud.multifile.job;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.util.Date;

@Slf4j
public class MultifileDecider implements JobExecutionDecider {

    @Override
        public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {
            JobParameters jobParameters = jobExecution.getJobParameters();

            String outputfile = jobParameters.getString("outputfile");
            String inputfile = jobParameters.getString("inputfile");


            if(StringUtils.endsWithIgnoreCase(inputfile, "csv")){
                log.info("inputfile extension :csv");
                return new FlowExecutionStatus("csv");

            }else if(StringUtils.endsWithIgnoreCase(inputfile, "txt")) {
                log.info("inputfile extension :txt");
                return new FlowExecutionStatus("txt");

            }else if(StringUtils.endsWithIgnoreCase(inputfile, "json")){
                log.info("inputfile extension :json");
                return new FlowExecutionStatus("json");

            }else if(StringUtils.endsWithIgnoreCase(inputfile, "xml")){
                log.info("inputfile extension :xml");
                return new FlowExecutionStatus("xml");
            };


        return null;
    }


}
