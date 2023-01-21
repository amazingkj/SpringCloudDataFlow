package io.spring.cloud.filefile.job;

import io.spring.cloud.filefile.dto.Dept;
import io.spring.cloud.filefile.dto.DeptDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.PassThroughLineAggregator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

import javax.sql.DataSource;


@Slf4j
@RequiredArgsConstructor
@Configuration
public class FileFileJobConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final DataSource dataSource;

    private static final int chunkSize = 5;

    @Bean
    public Job FileFileJob(){
        return jobBuilderFactory.get("FileFileJob")
                .incrementer(new RunIdIncrementer())
                .start(FileFileJob_buildStep())
                .build();
    }
    @Bean
    public Step FileFileJob_buildStep(){
        return stepBuilderFactory.get("FileFileJob_buildStep")
                .<DeptDTO, Dept>chunk(chunkSize)
                .reader(FileDBJob_FileReader())
                .writer(FileDBJob_FileWriter())
              //  .writer(Dept -> Dept.stream().forEach(i -> {
           // log.debug((i.toString()));}))
                .build();
    }

    @Bean
    public FlatFileItemReader<DeptDTO> FileDBJob_FileReader(){
        FlatFileItemReader<DeptDTO> flatFileItemReader = new FlatFileItemReader<>();
        flatFileItemReader.setResource(new ClassPathResource("csvInput/fileJob_input.csv"));
        //flatFileItemReader.setLineMapper(((line, lineNumber) -> new OneDto(lineNumber+","+line)));

        DefaultLineMapper<DeptDTO> dtoDefaultLineMapper = new DefaultLineMapper<>();

        DelimitedLineTokenizer delimitedLineTokenizer = new DelimitedLineTokenizer();
        delimitedLineTokenizer.setNames("deptNo","dName", "loc");//dto 컬럼명
        delimitedLineTokenizer.setDelimiter(",");//구분자

        BeanWrapperFieldSetMapper<DeptDTO> beanWrapperFieldSetMapper = new BeanWrapperFieldSetMapper<>();
        beanWrapperFieldSetMapper.setTargetType(DeptDTO.class);

        dtoDefaultLineMapper.setLineTokenizer(delimitedLineTokenizer);
        dtoDefaultLineMapper.setFieldSetMapper(beanWrapperFieldSetMapper);
        flatFileItemReader.setLineMapper(dtoDefaultLineMapper);

        return flatFileItemReader;
    }

    @Bean
    public FlatFileItemWriter<Dept> FileDBJob_FileWriter(){
        return new FlatFileItemWriterBuilder<Dept>()
                .name("FileDBJob_FileWriter")
                .resource(new FileSystemResource("output/fileJob_output.csv"))
                .lineAggregator(new PassThroughLineAggregator<>())
                .build();
    }



}
