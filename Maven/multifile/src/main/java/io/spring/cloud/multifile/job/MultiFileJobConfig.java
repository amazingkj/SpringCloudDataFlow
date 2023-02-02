package io.spring.cloud.multifile.job;

import com.thoughtworks.xstream.XStream;
import io.spring.cloud.multifile.dto.Dept;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.separator.SimpleRecordSeparatorPolicy;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.FormatterLineAggregator;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.batch.item.xml.builder.StaxEventItemReaderBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.oxm.xstream.XStreamMarshaller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


@Slf4j
@RequiredArgsConstructor
@Configuration
public class MultiFileJobConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private static final int chunkSize = 5;
    private static final String FILE_PATH = "C:/amazing/DataflowProject/Maven/resources/";
    private final ResourceLoader resourceLoader;

    @Bean
    public Job MultiJob() throws Exception {
        return jobBuilderFactory.get("MultiJob")
                .incrementer(new DailyJobTimestamper())
                .start(startStep())
                    .listener(new MultiFileJobExecutionListener())
                    .validator(new ParameterValidator())
                .next(MultiResourceJob_buildStep(null))
                .next(decider())
                .from(decider())
                    .on("csv")
                    .to(MultiJobcsv_buildStep(null))
                .from(decider())
                    .on("xml")
                    .to(MultiJobxml_buildStep(null))
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
            .build();
}
    @Bean
    @JobScope
    public Step MultiJobcsv_buildStep(@Value("#{jobParameters[version]}") String version){

        log.info("----csv");
        log.info(version);
        log.info("----");

        return stepBuilderFactory.get("MultiJobcsv_buildStep")
                .<Dept, Dept>chunk(chunkSize)
                .reader(multiJob_Reader(null))
                .processor(multiJob_processor(null))
                .writer(FixedLength_writer(null))
                .listener(new MultiFileStepExecutionListener())
                .build();
    }

    @Bean
    @JobScope
    public Step MultiJobxml_buildStep(@Value("#{jobParameters[version]}") String version){

        log.info("----xml");
        log.info(version);
        log.info("----");

        return stepBuilderFactory.get("MultiJobxml_buildStep")
                .<Dept, Dept>chunk(chunkSize)
                .reader(customXmlItemReader(null))
                .writer(FixedLength_writer(null))
                .listener(new MultiFileStepExecutionListener())
                .build();
    }

    @Bean
    @JobScope
    public Step MultiResourceJob_buildStep(@Value("#{jobParameters[version]}") String version) throws Exception {

        log.info("----multi");
        log.info(version);
        log.info("----");

        return stepBuilderFactory.get("MultiResourceJob_buildStep")
                .<Dept, Dept>chunk(chunkSize)
                .reader(multiResourceJob_Reader())
                .processor(multiJob_processor(null))
                .writer(FixedLength_writer(null))
                .listener(new MultiFileStepExecutionListener())
                .build();
    }


    @Bean
    public JobExecutionDecider decider(){
        return new MultifileDecider();
    }


    /* multi */
    @Bean
    public MultiResourceItemReader<Dept> multiResourceJob_Reader() throws Exception {
        MultiResourceItemReader<Dept> multiResourceItemReader = new MultiResourceItemReader<>();
        multiResourceItemReader.setResources(
                ResourcePatternUtils
                        .getResourcePatternResolver(this.resourceLoader)
                        .getResources("file:///"+FILE_PATH+"/multi/*.txt")
        );
                multiResourceItemReader.setDelegate(multiFileItemReader());

        return multiResourceItemReader;
    }


    @Bean
    public FlatFileItemReader<Dept> multiFileItemReader() {
        return new FlatFileItemReaderBuilder<Dept>()
                .name("multiFileItemReader")
                .encoding("UTF-8")
                .delimited().delimiter(",")
                .names("dept_no", "d_name", "loc", "etc")
                .targetType(Dept.class)
                .recordSeparatorPolicy(new SimpleRecordSeparatorPolicy(){

                    @Override
                    public String postProcess(String record) {

                        log.info("{}", record);

                        //파싱 대상이아니면 무시
                        if (record.indexOf(",") == -1) {
                            return null;
                        }

                        return record.trim(); //깔끔하게 공백 제거
                    }

                }).build();
    }


    /*csv*/
    @Bean
    @StepScope
    public FlatFileItemReader<Dept> multiJob_Reader(@Value ("#{jobParameters[inputfile]}")String inputfile) {
        return new FlatFileItemReaderBuilder<Dept>()
                .name("multiJob_Reader")
                .resource(new FileSystemResource(FILE_PATH+inputfile))
                .encoding("UTF-8")
                .delimited().delimiter(",")
                .names("dept_no", "d_name", "loc", "etc")
                .targetType(Dept.class)
                .recordSeparatorPolicy(new SimpleRecordSeparatorPolicy(){

                    @Override
                    public String postProcess(String record) {

                        log.info("policy: {}", record);

                        //파싱 대상이아니면 무시
                        if (record.indexOf(",") == -1) {
                            return null;
                        }

                        return record.trim(); //깔끔하게 공백 제거
                    }

                }).build();
    }


    @Bean
    @StepScope
    public ItemProcessor<Dept, Dept> multiJob_processor(@Value("#{jobParameters[version]}")String version) {
       return dept -> new Dept(dept.getDept_no(), dept.getD_name(), dept.getLoc(), dept.getEtc());
    }


    /*xml*/
    @Bean
    @StepScope
    public StaxEventItemReader<Dept> customXmlItemReader(@Value("#{jobParameters[inputfile]}") String inputfile) {
        return new StaxEventItemReaderBuilder<Dept>()
                .name("customXmlItemReader")
                .encoding("UTF-8")
                .resource(new FileSystemResource(FILE_PATH+inputfile))
                .addFragmentRootElements("Dept")
                .unmarshaller(itemMarshaller())
                .build();
    }

    public XStreamMarshaller itemMarshaller() {
        Map<String, Class<?>> aliases = new HashMap<>();

        aliases.put("Dept", Dept.class); // tag 전체 타입
        aliases.put("dept_no", Integer.class); // 두번째부터는 각 항목의 타입
        aliases.put("d_name", String.class);
        aliases.put("loc", String.class);
        aliases.put("etc", String.class);

        XStreamMarshaller xStreamMarshaller = new XStreamMarshaller();
        xStreamMarshaller.setAliases(aliases);

        //보안 프레임워크 구성 에러 해결
        XStream xStream = xStreamMarshaller.getXStream();
        xStream.allowTypes(new Class[] {
                Dept.class
        });

        return xStreamMarshaller;
    }


    /*Writer*/
    @Bean
    @StepScope
    public FlatFileItemWriter<Dept> FixedLength_writer(@Value("#{jobParameters[outputfile]}") String outputfile){

        BeanWrapperFieldExtractor<Dept> fieldExtractor = new BeanWrapperFieldExtractor<>();
        fieldExtractor.setNames(new String[] {"dept_no", "d_name", "loc", "etc"});
        fieldExtractor.afterPropertiesSet();

        FormatterLineAggregator<Dept> formatterLineAggregator = new FormatterLineAggregator<>();
        formatterLineAggregator.setFieldExtractor(fieldExtractor);
        formatterLineAggregator.setFormat("%-5s,%15s,%5s,%5s");

        return new FlatFileItemWriterBuilder<Dept>()
                .name("FixedLength_writer")
                .encoding("UTF-8")
                .resource(new FileSystemResource(FILE_PATH+outputfile))
                .lineAggregator(formatterLineAggregator)
                .append(true)
                .build();
    }







}
