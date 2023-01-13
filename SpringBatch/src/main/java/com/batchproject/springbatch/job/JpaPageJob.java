package com.batchproject.springbatch.job;

import com.batchproject.springbatch.domain.Dept;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class JpaPageJob {


    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;

    private int chunksSize = 10;

    @Bean
    public Job JpaPageJob_batchBuild() {
        return jobBuilderFactory.get("jpaPageJob")
                .start(JpaPageJob_step1()).build();

    }

    @Bean
    public Step JpaPageJob_step1() {
        return stepBuilderFactory.get("jpaPageJob_step1")
                .<Dept,Dept>chunk(chunksSize)
                .reader(jpaPageJob1_dbItemReader())
                .writer(jpaPageJob1_printItemWriter())
                .build();

    }

    @Bean
    public JpaPagingItemReader<Dept> jpaPageJob1_dbItemReader(){
        return new JpaPagingItemReaderBuilder<Dept>()
                .name("jpaPageJob1_dbItemReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(chunksSize)
                .queryString("SELECT d from Dept d order by dept_no asc")
                .build();
    }
    @Bean
    public ItemWriter<Dept> jpaPageJob1_printItemWriter(){
        return  list -> {
            for(Dept dept: list){
                log.debug(dept.toString());
            }
        };
    }



}
