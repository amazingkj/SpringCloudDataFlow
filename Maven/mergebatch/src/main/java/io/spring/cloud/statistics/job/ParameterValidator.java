package io.spring.cloud.statistics.job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.JobParametersValidator;
import org.springframework.util.StringUtils;

@Slf4j
public class ParameterValidator implements JobParametersValidator {

    @Override
    public void validate(JobParameters jobParameters) throws JobParametersInvalidException {


        String outputfile = jobParameters.getString("outputfile");

        if (!StringUtils.hasText(outputfile)) {
            throw new JobParametersInvalidException("Error : outputfile is missing");
        }else if(!StringUtils.endsWithIgnoreCase(outputfile, "txt")){
            log.warn("outputfile isn't txt file extension");
            throw new JobParametersInvalidException("Error : outputfile isn't txt file extension");
        }


    }
}