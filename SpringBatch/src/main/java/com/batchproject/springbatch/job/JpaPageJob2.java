package com.batchproject.springbatch.job;

import com.batchproject.springbatch.domain.Dept;
import com.batchproject.springbatch.domain.Dept2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class JpaPageJob2 {


    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;

    private int chunksSize = 10;


    //DB to DB
    @Bean
    public Job JpaPageJob2_batchBuild() {
        return jobBuilderFactory.get("jpaPageJob2")
                .start(JpaPageJob2_step1()).build();

    }

    @Bean
    public Step JpaPageJob2_step1() {
        return stepBuilderFactory.get("jpaPageJob2_step1")
                .<Dept,Dept2>chunk(chunksSize)
                .reader(jpaPageJob2_dbItemReader())
                .processor(jpaPageJob2_processor())
                .writer(jpaPageJob2_dbItemWriter())
                .build();
    }
    //한 번 가공을 거치고 싶을 때  (dept db 에서 dept2 db 로)
    private ItemProcessor<Dept, Dept2> jpaPageJob2_processor(){
        return dept -> {
            return new Dept2(dept.getDeptNo(), "New "+dept.getDName(), "new loc "+dept.getLoc());
        };
    }
    @Bean
    public JpaPagingItemReader<Dept> jpaPageJob2_dbItemReader(){
        return new JpaPagingItemReaderBuilder<Dept>()
                .name("jpaPageJob2_dbItemReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(chunksSize)
                .queryString("SELECT d from Dept d order by dept_no asc")
                .build();
    }
    @Bean
    public ItemWriter<Dept2> jpaPageJob2_dbItemWriter(){
        JpaItemWriter<Dept2> jpaItemWriter = new JpaItemWriter<>();
        jpaItemWriter.setEntityManagerFactory(entityManagerFactory);
        return jpaItemWriter;
    }



}
