package io.spring.cloud.dbfile.job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.JobParametersValidator;
import org.springframework.util.StringUtils;

@Slf4j
public class ParameterValidator implements JobParametersValidator {

    @Override
    public void validate(JobParameters jobParameters) throws JobParametersInvalidException {

        String inputfile = jobParameters.getString("inputfile");

        if (!StringUtils.hasText(inputfile)) {
            throw new JobParametersInvalidException("Error : inputfile is missing");
        }else if(!StringUtils.endsWithIgnoreCase(inputfile, "json")){
            log.warn("inputfile isn't json file extension");
            throw new JobParametersInvalidException("Error : inputfile isn't json file extension");
        }


    }
}