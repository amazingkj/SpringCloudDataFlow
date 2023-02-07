package io.spring.cloud.quartz.quartz;

import io.spring.cloud.quartz.parameters.CustomJobParameter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.*;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Objects;

@Slf4j
@Getter
@AllArgsConstructor
public class QuartzJobLauncher extends QuartzJobBean {

    private final RestTemplate restTemplate;
    @Autowired
    private final CustomJobParameter jobParameter;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        log.info("==================START====================");

        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();

        log.info("getName {}, getArgument {}, getProperties {}",
                jobParameter.getName(), jobParameter.getArgument(), jobParameter.getProperties());

        try {
            if(!"NONE".equals(jobParameter.getName())){
                parameters.add("name", jobParameter.getName());
            } else { throw new JobParametersInvalidException("Name is missing"); }
        } catch (JobParametersInvalidException e) {
            log.warn("Name is missing");
            throw new RuntimeException(e);
        }

        //StringUtils.hasText (X) , !StringUtils.isEmpty (X), !Objects.isNull (X)


        if(!"NONE".equals(jobParameter.getArgument())) {
            parameters.add("argument", jobParameter.getArgument());
            log.info("++++++ getArgument {}", jobParameter.getArgument());
        }

        if(!"NONE".equals(jobParameter.getProperties())){
            parameters.add("properties", jobParameter.getProperties());
            log.info("++++++ getProperties {}", jobParameter.getProperties());
        }


        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity Entity = new HttpEntity<>(parameters, headers);

        log.info("Entity {}", Entity);

        URI uri = UriComponentsBuilder
                .fromUriString("http://localhost:9393")
                .path("/tasks/executions")
                .build()
                .encode()
                .toUri();

        log.info(String.valueOf(uri));

        ResponseEntity<String> exchange = restTemplate.exchange(uri, HttpMethod.POST, Entity, String.class);

        log.info("getStatusCode : {}", exchange.getStatusCode());
        log.info("getHeaders: {}",exchange.getHeaders());
        log.info("exchange : {}", exchange);
        log.info("==================DONE====================");

    }

}
