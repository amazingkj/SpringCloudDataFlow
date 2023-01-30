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
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.separator.SimpleRecordSeparatorPolicy;
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
    public Job FileDBJob() throws Exception {
        return jobBuilderFactory.get("FileDBJob")
                .incrementer(new RunIdIncrementer())
                .start(FileDBJob_buildStep())
                .build();
    }

    @Bean
    public Step FileDBJob_buildStep() throws Exception {
        return stepBuilderFactory.get("FileDBJob_buildStep")
                .<DeptDTO, Dept>chunk(chunkSize)
                .reader(FileDBJob_FileReader())
                .writer(jdbcBatchItemWriter())
                .build();
    }

    @Bean
    public FlatFileItemReader<DeptDTO> FileDBJob_FileReader(){
        return new FlatFileItemReaderBuilder<DeptDTO>()
                .name("FileDBJob_FileReader")
                .encoding("UTF-8")
                .resource(new ClassPathResource("csvInput/new_input.csv"))
                .delimited().delimiter(",")
                .names("deptNo","dName", "loc")
                .targetType(DeptDTO.class)
                .recordSeparatorPolicy(new SimpleRecordSeparatorPolicy() {
                    @Override
                    public String postProcess(String record) {
                        return record.trim();
                    }
                })
                .build();
    }



    private ItemWriter<Dept> jdbcBatchItemWriter() {
        JdbcBatchItemWriter<Dept> itemWriter = new JdbcBatchItemWriterBuilder<Dept>()
                .dataSource(dataSource)
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .sql("insert into dept(dept_no, d_name, loc, etc) values(:deptNo, :dName, :loc, :etc)")
                .build();

        itemWriter.afterPropertiesSet();

        return itemWriter;
    }


}
