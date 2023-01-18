package com.batchproject.springbatch.job;

import com.batchproject.springbatch.custom.CustomPassLineAggregator;
import com.batchproject.springbatch.domain.Dept;
import com.batchproject.springbatch.domain.Dept2;
import com.batchproject.springbatch.dto.OneDto;
import com.batchproject.springbatch.dto.TwoDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

import javax.persistence.EntityManagerFactory;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class TxtToDBJob {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;
    private static final int chunkSize = 5;

    @Bean
    public Job TxtToDBJob_batchBuild(){
        return jobBuilderFactory.get("TxtToDBJob")
                .incrementer(new RunIdIncrementer())
                .start(TxtToDBJob_batchStep1())
                .build();
    }

    @Bean
    public Step TxtToDBJob_batchStep1(){
        return stepBuilderFactory.get("TxtToDBJob_batchStep1")
                .<Dept2, Dept>chunk(chunkSize)
                .reader(TxtToDBJob_FileReader())
                .writer(TxtToDBJob_FileWriter())
                .writer(TxtToDBJob_dbItemWriter())
                .build();
    }

    @Bean
    public FlatFileItemReader<Dept2> TxtToDBJob_FileReader(){
        FlatFileItemReader<Dept2> flatFileItemReader = new FlatFileItemReader<>();
        flatFileItemReader.setResource(new ClassPathResource("source2/TxtToDBJob_input.txt")); //resource/source/tex- 경로에서 읽어오는 자료
        //flatFileItemReader.setLineMapper(((line, lineNumber) -> new OneDto(lineNumber+","+line)));

        DefaultLineMapper<Dept2> dtoDefaultLineMapper = new DefaultLineMapper<>();

        DelimitedLineTokenizer delimitedLineTokenizer = new DelimitedLineTokenizer();
        delimitedLineTokenizer.setNames("deptNo","dName", "loc");//dto 컬럼명
        delimitedLineTokenizer.setDelimiter(",");//구분자

        BeanWrapperFieldSetMapper<Dept2> beanWrapperFieldSetMapper = new BeanWrapperFieldSetMapper<>();
        beanWrapperFieldSetMapper.setTargetType(Dept2.class);

        dtoDefaultLineMapper.setLineTokenizer(delimitedLineTokenizer);
        dtoDefaultLineMapper.setFieldSetMapper(beanWrapperFieldSetMapper);
        flatFileItemReader.setLineMapper(dtoDefaultLineMapper);

        return flatFileItemReader;
    }

    @Bean
    public FlatFileItemWriter TxtToDBJob_FileWriter(){
        return new FlatFileItemWriterBuilder<Dept>()
                .name("txtTocvsJob_FileWriter")
                .resource(new FileSystemResource("output/TxtToDBJob_output.csv"))
                .lineAggregator(new CustomPassLineAggregator<>())
                .build();
    }

    @Bean
    public ItemWriter<Dept> TxtToDBJob_dbItemWriter(){
        JpaItemWriter<Dept> jpaItemWriter = new JpaItemWriter<>();
        jpaItemWriter.setEntityManagerFactory(entityManagerFactory);

        log.debug("TxtToDBJob_dbItemWriter success");
        return jpaItemWriter;
    }


}
