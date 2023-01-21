package io.spring.cloud.dbfile.job;

import io.spring.cloud.dbfile.dto.Dept;
import io.spring.cloud.dbfile.dto.DeptDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.PassThroughLineAggregator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import javax.sql.DataSource;


@Slf4j
@RequiredArgsConstructor
@Configuration
public class DBFileJobConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final DataSource dataSource;
    private static final int chunkSize = 5;

    @Bean
    public Job DBFileJob() throws Exception {
        return jobBuilderFactory.get("DBFileJob")
                //.incrementer(new RunIdIncrementer())
                .start(DBFileJob_buildStep())
                .build();
    }

    @Bean
    public Step DBFileJob_buildStep() throws Exception {
        return stepBuilderFactory.get("DBFileJob_buildStep")
                .<Dept, Dept>chunk(chunkSize)
                .reader(jdbcCursorItemReader())
                .writer(csvFileItemWriter())
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

    private ItemWriter<Dept> csvFileItemWriter() throws Exception {
        BeanWrapperFieldExtractor<Dept> fieldExtractor = new BeanWrapperFieldExtractor<>();
        fieldExtractor.setNames(new String[] {"dept_no", "d_name", "loc", "etc"});

        DelimitedLineAggregator<Dept> lineAggregator = new DelimitedLineAggregator<Dept>();
        lineAggregator.setDelimiter(",");
        lineAggregator.setFieldExtractor(fieldExtractor);

        FlatFileItemWriter<Dept> itemWriter = new FlatFileItemWriterBuilder<Dept>()
                .name("csvFileItemWriter")
                .encoding("UTF-8")
                .resource(new FileSystemResource("output/test-output.csv"))
                .lineAggregator(lineAggregator)
                .headerCallback(writer -> writer.write("번호,이름,거주지,비고"))
                .footerCallback(writer -> writer.write("-------------------\n"))
                .append(true)
                .build();

        itemWriter.afterPropertiesSet();

        return itemWriter;
    }



}
