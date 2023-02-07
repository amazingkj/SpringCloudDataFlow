package io.spring.cloud.dbfile.job;

import io.spring.cloud.dbfile.dto.Dept;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
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
import org.springframework.batch.item.file.transform.FormatterLineAggregator;
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
                .incrementer(new DailyJobTimestamper())
                .start(DBFileJob_buildStep())
                .listener(new JobExecutionListener())
                .validator(new ParameterValidator())
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

    /*cursorReader*/
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
    public FlatFileItemWriter<Dept> DBFileJob_FileWriter(@Value("#{jobParameters[outputfile]}") String outputfile){

        BeanWrapperFieldExtractor<Dept> fieldExtractor = new BeanWrapperFieldExtractor<>();
        fieldExtractor.setNames(new String[] {"dept_no", "d_name", "loc", "etc"});
        fieldExtractor.afterPropertiesSet();

        FormatterLineAggregator<Dept> formatterLineAggregator = new FormatterLineAggregator<>();
        formatterLineAggregator.setFieldExtractor(fieldExtractor);
        formatterLineAggregator.setFormat("%-5s#%15s#%5s#%5s");

        return new FlatFileItemWriterBuilder<Dept>()
                .name("DBFileJob_FileWriter")
                .resource(new FileSystemResource(FILE_PATH+outputfile))
                .lineAggregator(formatterLineAggregator)
                .build();
    }




}
