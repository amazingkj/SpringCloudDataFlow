package io.spring.cloud.quartz.quartz;

import io.spring.cloud.quartz.parameters.CustomJobParameter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.*;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Slf4j
@Getter
@AllArgsConstructor
public class QuartzJobLauncher extends QuartzJobBean {

    private final RestTemplate restTemplate;
    private final CustomJobParameter jobParameter;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        log.info("==================START====================");

        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("name", jobParameter.getName());

        log.info("getName {}", jobParameter.getName());
        //parameters.add("argument", jobParameter.getArgument());
        //parameters.add("properties", jobParameter.getProperties());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        //headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        HttpEntity Entity = new HttpEntity<>(parameters, headers);

        URI uri = UriComponentsBuilder
                .fromUriString("http://localhost:9393")
                .path("/tasks/executions")
                .build()
                .encode()
                .toUri();

        log.info("uri");

        ResponseEntity<String> exchange = restTemplate.exchange(uri, HttpMethod.POST, Entity, String.class);

        log.info("getStatusCode : {}", exchange.getStatusCode());
        log.info("getHeaders: {}",exchange.getHeaders());
        log.info("exchange : {}", exchange);
        log.info("==================DONE====================");

    }

}
