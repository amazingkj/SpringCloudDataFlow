package io.spring.cloud.dbfile.job;

import io.spring.cloud.dbfile.dto.Dept;
import io.spring.cloud.dbfile.dto.Dept2;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.support.transaction.TransactionAwareProxyFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;


@Slf4j
@RequiredArgsConstructor
@Configuration
public class DBHttpJobConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final DataSource dataSource;
    private static final int chunkSize = 5;
    private final RestTemplate restTemplate;

    @Bean
    public Job DBHttpJob() throws Exception {
        return jobBuilderFactory.get("DBHttpJob")
                .incrementer(new RunIdIncrementer())
                .start(DBHttpJob_buildStep())
                .build();
    }

    @Bean
    public Step DBHttpJob_buildStep() throws Exception {
        return stepBuilderFactory.get("DBHttpJob_buildStep")
                .<Dept, Dept2>chunk(chunkSize)
                .reader(jdbcCursorItemReader())
                .processor(customItemWriterProcessor())
                .writer(customItemWriter2())
                .build();
    }

    private JdbcCursorItemReader<Dept> jdbcCursorItemReader() throws Exception {
        JdbcCursorItemReader<Dept> itemReader = new JdbcCursorItemReaderBuilder<Dept>()
                .name("jdbcCursorItemReader")
                .dataSource(dataSource)
                .sql("select dept_no, d_name, loc, etc from dept order by dept_no asc")
                .rowMapper((rs, rowNum) -> new Dept(
                        rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4)))
                .build();

        itemReader.afterPropertiesSet();
        return itemReader;
    }


    public ItemProcessor<Dept, Dept2> customItemWriterProcessor() {
        return dept -> new Dept2(dept.getDept_no(), dept.getD_name(), dept.getLoc(), dept.getEtc());
    }


    public ItemWriter<Dept2> customItemWriter2() {
        return new ItemWriter<>() {
            HttpHeaders headers = new HttpHeaders();

            @Override
            public void write(List<? extends Dept2> items) throws Exception {
                for (Dept2 item : items) {
                    log.info("item : {}", item);

                    HttpEntity<Dept2> entity = new HttpEntity<>(item, headers);
                    ResponseEntity<Dept2> response = restTemplate.postForEntity("http://localhost:9090/server/post", entity, Dept2.class);

                    log.info("Status code is : {} ", response.getStatusCode());
                    log.info("getStatusCode : {}",response.getStatusCode());
                    log.info("getHeaders: {}",response.getHeaders());
                    log.info("getBody : {}",response.getBody());

                }

            }
        };
    }



}
