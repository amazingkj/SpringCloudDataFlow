package io.spring.cloud.dbfile.job;

import io.spring.cloud.dbfile.dto.Dept;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.json.JacksonJsonObjectReader;
import org.springframework.batch.item.json.JsonItemReader;
import org.springframework.batch.item.json.builder.JsonItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;


@Slf4j
@RequiredArgsConstructor
@Configuration
public class FileHttpJobConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private static final int chunkSize = 5;
    private final RestTemplate restTemplate;

    private static final String FILE_PATH = "C:/amazing/DataflowProject/Maven/resources/";

    @Bean
    public Job FileHttpJob() throws Exception {
        return jobBuilderFactory.get("FileHttpJob")
                .incrementer(new DailyJobTimestamper())
                .start(FileHttpJob_buildStep())
                .listener(new FiletoHttpJobExecutionListener())
                .validator(new ParameterValidator())
                .build();
    }

    @Bean
    public Step FileHttpJob_buildStep() throws Exception {
        return stepBuilderFactory.get("FileHttpJob_buildStep")
                .<Dept, Dept>chunk(chunkSize)
                .reader(customJsonItemReader(null))
                .processor(customItemWriterProcessor())
                .writer(customItemWriter())
                .build();
    }


    /*json*/
    // input = "dept_JsonInput.json"
    @Bean
    @StepScope
    public JsonItemReader<Dept> customJsonItemReader(@Value("#{jobParameters[inputfile]}") String inputfile) throws Exception {
         return new JsonItemReaderBuilder<Dept>()
                .name("customJsonItemReader")
                .jsonObjectReader(new JacksonJsonObjectReader<>(Dept.class))
                .resource(new FileSystemResource(FILE_PATH+inputfile))
                .build();
    }

    public ItemProcessor<Dept, Dept> customItemWriterProcessor() {
        return dept -> new Dept(dept.getDept_no(), dept.getD_name(), dept.getLoc(), dept.getEtc());
    }
    public ItemWriter<Dept> customItemWriter() {
        return new ItemWriter<>() {
            HttpHeaders headers = new HttpHeaders();

            @Override
            public void write(List<? extends Dept> items) throws Exception {
                for (Dept item : items) {
                    log.info("item : {}", item);

                    HttpEntity<Dept> entity = new HttpEntity<>(item, headers);
                    ResponseEntity<Dept> response = restTemplate.postForEntity("http://localhost:9090/server/post", entity, Dept.class);

                    log.info("Status code is : {} ", response.getStatusCode());
                    log.info("getStatusCode : {}",response.getStatusCode());
                    log.info("getHeaders: {}",response.getHeaders());
                    log.info("getBody : {}",response.getBody());

                }

            }
        };
    }



}
