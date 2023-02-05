package io.spring.cloud.filewriter.job;

import com.thoughtworks.xstream.XStream;
import io.spring.cloud.filewriter.dto.Dept;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.FormatterLineAggregator;
import org.springframework.batch.item.json.JacksonJsonObjectMarshaller;
import org.springframework.batch.item.json.JsonFileItemWriter;
import org.springframework.batch.item.json.builder.JsonFileItemWriterBuilder;
import org.springframework.batch.item.xml.StaxEventItemWriter;
import org.springframework.batch.item.xml.builder.StaxEventItemWriterBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.oxm.xstream.XStreamMarshaller;

import java.io.IOException;
import java.util.*;


@Slf4j
@RequiredArgsConstructor
@Configuration
public class FileWriterJobConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private static final int chunkSize = 300;
    private static final String FILE_PATH = "C:/amazing/DataflowProject/Maven/resources/";


    @Bean
    public Job FileWriterJob() throws Exception {
        return jobBuilderFactory.get("FileWriterJob")
                .incrementer(new DailyJobTimestamper())
                .start(startStep())
                     .listener(new FileWriterJobExecutionListener())
                     .validator(new ParameterValidator())
                .next(decider())
                .from(decider())
                     .on("csv")
                     .to(CsvWriter_buildStep())
                .from(decider())
                     .on("xml")
                     .to(XmlWriter_buildStep())
                .from(decider())
                      .on("json")
                      .to(JSonWriter_buildStep())
                .from(decider())
                     .on("txt")
                     .to(FixedTxtWriter_buildStep())
                .end()
                .build();
    }

    @Bean
    public Step startStep() {
        return stepBuilderFactory.get("startStep")
                .tasklet((contribution, chunkContext) -> {
                    log.info(">>>>> Start!");
                    return RepeatStatus.FINISHED;
                })
                .listener(new FileWriterStepExecutionListener())
                .build();
    }
    @Bean
    public Step CsvWriter_buildStep() throws Exception {
        return stepBuilderFactory.get("CsvWriter_buildStep")
                .<Dept, Dept>chunk(chunkSize)
                .reader(itemReader())
                .writer(csvItemWriter(null))
                .listener(new FileWriterStepExecutionListener())
                .build();
    }
    @Bean
    public Step XmlWriter_buildStep() throws Exception {
        return stepBuilderFactory.get("XmlWriter_buildStep")
                .<Dept, Dept>chunk(chunkSize)
                .reader(itemReader())
                .writer(XmlItemWriter(null))
                .listener(new FileWriterStepExecutionListener())
                .build();
    }
    @Bean
    public Step JSonWriter_buildStep() throws Exception {
        return stepBuilderFactory.get("JSonWriter_buildStep")
                .<Dept, Dept>chunk(chunkSize)
                .reader(itemReader())
                .writer(JsonItemWriter(null))
                .listener(new FileWriterStepExecutionListener())
                .build();
    }

    @Bean
    public Step FixedTxtWriter_buildStep() throws Exception {
        return stepBuilderFactory.get("FixedTxtWriter_buildStep")
                .<Dept, Dept>chunk(chunkSize)
                .reader(itemReader())
                .writer(FixedLength_writer(null))
                .listener(new FileWriterStepExecutionListener())
                .build();
    }
    @Bean
    public JobExecutionDecider decider(){
        return new FileWriterDecider();
    }


    /*Reader*/
    private ItemReader<Dept> itemReader() {
        return new CustomItemReader<>(getItems());
    }

    private List<Dept> getItems() {
        List<Dept> items = new ArrayList<>();

        for (int i = 1; i < 1001; i++) {
            items.add(new Dept(i ,"test name" + i, "test address"+ i, "ect"+ i));
        }

        return items;
    }

    /*Writer*/
    @Bean
    @StepScope
    public FlatFileItemWriter<Dept> csvItemWriter(@Value("#{jobParameters[outputfile]}") String outputfile) throws Exception {

        BeanWrapperFieldExtractor<Dept> fieldExtractor = new BeanWrapperFieldExtractor<>();
        fieldExtractor.setNames(new String[] {"dept_no", "d_name", "loc", "etc"});

        DelimitedLineAggregator<Dept> lineAggregator = new DelimitedLineAggregator<>();
        lineAggregator.setDelimiter(",");
        lineAggregator.setFieldExtractor(fieldExtractor);

        log.info("outputfile parameter: {}",outputfile);
        FlatFileItemWriter<Dept> itemWriter = new FlatFileItemWriterBuilder<Dept>()
                .name("csvFileItemWriter")
                .encoding("UTF-8")
                .resource(new FileSystemResource(FILE_PATH + outputfile))
                .lineAggregator(lineAggregator)
                .append(false)
                .build();

        itemWriter.afterPropertiesSet();

        return itemWriter;
    }

    @Bean
    @StepScope
    public StaxEventItemWriter<Dept> XmlItemWriter(@Value("#{jobParameters[outputfile]}") String outputfile) {

        log.info("outputfile parameter: {}",outputfile);

        return new StaxEventItemWriterBuilder<Dept>()
                .name("XmlItemWriter")
                .resource(new FileSystemResource(FILE_PATH + outputfile))
                .marshaller(itemMarshaller())
                .rootTagName("Depts")
                .overwriteOutput(true)
                .encoding("UTF-8")
                .build();
    }

    public XStreamMarshaller itemMarshaller() {
        XStreamMarshaller marshaller = new XStreamMarshaller();

        Map<String, Class> aliases = new HashMap<>();
        aliases.put("Dept", Dept.class); // tag 전체 타입
        aliases.put("dept_no", Integer.class); // 두번째부터는 각 항목의 타입
        aliases.put("d_name", String.class);
        aliases.put("loc", String.class);
        aliases.put("etc", String.class);

        marshaller.setAliases(aliases);
        marshaller.afterPropertiesSet();

        //보안 프레임워크 구성 에러 해결
        XStream xStream = marshaller.getXStream();
        xStream.allowTypes(new Class[] {
                Dept.class
        });

        return marshaller;
    }

    @Bean
    @StepScope
    public JsonFileItemWriter<Dept> JsonItemWriter(@Value("#{jobParameters[outputfile]}") String outputfile) throws IOException {
        log.info("outputfile parameter: {}",outputfile);

        return new JsonFileItemWriterBuilder<Dept>()
                .name("JsonItemWriter")
                .jsonObjectMarshaller(new JacksonJsonObjectMarshaller<>())
                .resource(new FileSystemResource(FILE_PATH+outputfile))
                .append(false)
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemWriter<Dept> FixedLength_writer(@Value("#{jobParameters[outputfile]}") String outputfile){

        log.info("outputfile parameter: {}",outputfile);

        BeanWrapperFieldExtractor<Dept> fieldExtractor = new BeanWrapperFieldExtractor<>();
        fieldExtractor.setNames(new String[] {"dept_no", "d_name", "loc", "etc"});
        fieldExtractor.afterPropertiesSet();

        FormatterLineAggregator<Dept> formatterLineAggregator = new FormatterLineAggregator<>();
        formatterLineAggregator.setFieldExtractor(fieldExtractor);
        formatterLineAggregator.setFormat("%-5s,%15s,%25s,%-10s");

        return new FlatFileItemWriterBuilder<Dept>()
                .name("FixedLength_writer")
                .encoding("UTF-8")
                .resource(new FileSystemResource(FILE_PATH+outputfile))
                .lineAggregator(formatterLineAggregator)
                .append(false)
                .build();
    }




}
