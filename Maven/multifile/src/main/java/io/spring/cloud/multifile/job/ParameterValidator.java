package io.spring.cloud.multifile.job;

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
        String inputfile = jobParameters.getString("inputfile");
       // String multifile = jobParameters.getString("multifile");

        if (!StringUtils.hasText(outputfile))
            throw new JobParametersInvalidException("outputfile is missing");

        if (!StringUtils.hasText(inputfile))
            throw new JobParametersInvalidException("inputfile is missing");

      //  if (!StringUtils.hasText(multifile))
      //     throw new JobParametersInvalidException("multifile is missing");



    }
}