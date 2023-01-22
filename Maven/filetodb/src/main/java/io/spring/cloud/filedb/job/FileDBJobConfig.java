package io.spring.cloud.filedb.job;

import io.spring.cloud.filedb.dto.Dept;
import io.spring.cloud.filedb.dto.DeptDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
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
public class FileDBJobConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final DataSource dataSource;
    private static final int chunkSize = 5;

    @Bean
    public Job FileDBJob(){
        return jobBuilderFactory.get("FileDBJob")
                .incrementer(new RunIdIncrementer())
                .start(FileDBJob_buildStep())
                .build();
    }

    @Bean
    public Step FileDBJob_buildStep(){
        return stepBuilderFactory.get("FileDBJob_buildStep")
                .<DeptDTO, Dept>chunk(chunkSize)
                .reader(FileDBJob_FileReader())
                .writer(FileDBJob_FileWriter())
                .writer(jdbcBatchItemWriter())
              //  .writer(Dept -> Dept.stream().forEach(i -> {
           // log.debug((i.toString()));}))
                .build();
    }

    @Bean
    public FlatFileItemReader<DeptDTO> FileDBJob_FileReader(){
        FlatFileItemReader<DeptDTO> flatFileItemReader = new FlatFileItemReader<>();
        flatFileItemReader.setResource(new ClassPathResource("csvInput/FileDBJob_input.csv")); //resource/csvInput/~- 경로에서 읽어오는 자료
        //flatFileItemReader.setLineMapper(((line, lineNumber) -> new OneDto(lineNumber+","+line)));

        DefaultLineMapper<DeptDTO> lineMapper = new DefaultLineMapper<>();
        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setNames("deptNo","dName", "loc");//dto 컬럼명
        lineTokenizer.setDelimiter(",");//구분자

        BeanWrapperFieldSetMapper<DeptDTO> beanWrapperFieldSetMapper = new BeanWrapperFieldSetMapper<>();
        beanWrapperFieldSetMapper.setTargetType(DeptDTO.class);

        lineMapper.setLineTokenizer(lineTokenizer);
        lineMapper.setFieldSetMapper(beanWrapperFieldSetMapper);
        flatFileItemReader.setLineMapper(lineMapper);

        return flatFileItemReader;
    }

    @Bean
    public FlatFileItemWriter<Dept> FileDBJob_FileWriter(){
        return new FlatFileItemWriterBuilder<Dept>()
                .name("FileDBJob_FileWriter")
                .resource(new FileSystemResource("output/FileDBJob_output.csv"))
                .lineAggregator(new PassThroughLineAggregator<>())
                .build();
    }

    private ItemWriter<Dept> jdbcBatchItemWriter() {
        JdbcBatchItemWriter<Dept> itemWriter = new JdbcBatchItemWriterBuilder<Dept>()
                .dataSource(dataSource)
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .sql("insert into dept(dept_no, d_name, loc, etc) values(:deptNo, :dName, :loc, etc)")
                .build();

        itemWriter.afterPropertiesSet();

        return itemWriter;
    }


}
