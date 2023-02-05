package io.spring.cloud.filedb.job;

import io.spring.cloud.filedb.dto.Dept;
import io.spring.cloud.filedb.dto.DeptDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.json.JacksonJsonObjectReader;
import org.springframework.batch.item.json.JsonItemReader;
import org.springframework.batch.item.json.builder.JsonItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

    private static final String FILE_PATH = "C:/amazing/DataflowProject/Maven/resources/";


    @Bean
    public Job FileDBJob() throws Exception {
        return jobBuilderFactory.get("FileDBJob")
                .incrementer(new RunIdIncrementer())
                .start(JsonFileDBJob_buildStep())
                .listener(new FiletoDBJobExecutionListener())
                .validator(new ParameterValidator())
                .build();
    }


    @Bean
    public Step JsonFileDBJob_buildStep() throws Exception {
        return stepBuilderFactory.get("JsonFileDBJob_buildStep")
                .<DeptDTO, Dept>chunk(chunkSize)
                .reader(customJsonItemReader(null))
                .writer(jdbcBatchItemWriter())
                .build();
    }

    /*json*/
    @Bean
    @StepScope
    public JsonItemReader<DeptDTO> customJsonItemReader(@Value("#{jobParameters[inputfile]}") String inputfile){
        return new JsonItemReaderBuilder<DeptDTO>()
                .name("customJsonItemReader")
                .jsonObjectReader(new JacksonJsonObjectReader<>(DeptDTO.class))
                .resource(new FileSystemResource(FILE_PATH+inputfile))
                .build();
    }


    private ItemWriter<Dept> jdbcBatchItemWriter() {
        JdbcBatchItemWriter<Dept> itemWriter = new JdbcBatchItemWriterBuilder<Dept>()
                .dataSource(dataSource)
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .sql("insert into dept(dept_no, d_name, loc, etc) values(:dept_no, :d_name, :loc, :etc)")
                .build();

        itemWriter.afterPropertiesSet();

        return itemWriter;
    }


}
