package io.spring.cloud.dbfile.job;

import io.spring.cloud.dbfile.UniqueRunIdIncrementer;
import io.spring.cloud.dbfile.dto.Dept;
import io.spring.cloud.dbfile.dto.DeptDTO;
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
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.PassThroughLineAggregator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;


@Slf4j
@RequiredArgsConstructor
@Configuration
public class DBFileJobConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final DataSource dataSource;
    private static final int chunkSize = 5;

    private static final String FILE_PATH = "C:/amazing/DataflowProject/Maven/resources/";


    @Bean
    public Job DBFileJob() throws Exception {
        return jobBuilderFactory.get("DBFileJob")
                .incrementer(new UniqueRunIdIncrementer())
                .start(DBFileJob_buildStep())
                .listener(new JobExecutionListener())
                .build();
    }

    @Bean
    public Step DBFileJob_buildStep() throws Exception {
        return stepBuilderFactory.get("DBFileJob_buildStep")
                .<Dept, Dept>chunk(chunkSize)
                .reader(jdbcCursorItemReader())
                .processor(DBFileJob_processor())
                .writer(DBFileJob_FileWriter(null))
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

    public ItemProcessor<Dept, Dept> DBFileJob_processor() {
         return dept -> new Dept(dept.getDept_no(), dept.getD_name(), dept.getLoc(), dept.getEtc());
    }
    @Bean
    @StepScope
    public FlatFileItemWriter<Dept> DBFileJob_FileWriter(@Value("#{jobParameters[output]}") String output){
        return new FlatFileItemWriterBuilder<Dept>()
                .name("DBFileJob_FileWriter")
                .resource(new FileSystemResource(FILE_PATH+output))
                .lineAggregator(new PassThroughLineAggregator<>())
                .build();
    }




}
