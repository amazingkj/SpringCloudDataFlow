package io.spring.cloud.ftp.job;

import com.thoughtworks.xstream.XStream;
import io.spring.cloud.ftp.dto.Dept;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.core.step.tasklet.Tasklet;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.oxm.xstream.XStreamMarshaller;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@Slf4j
@RequiredArgsConstructor
@Configuration
public class FTPJobConfig {



    @Autowired
    private final CustomJobParameter jobParameter;

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private static final int chunkSize = 5;
    private static final String C_FILE_PATH = "C:/amazing/DataflowProject/Maven/resources/";
    private final ResourceLoader resourceLoader;

    @Bean
    public Job MultiJob() throws Exception {
        return jobBuilderFactory.get("MultiJob")
                .incrementer(new DailyJobTimestamper())
                .start(startStep())
                .next(MultiResourceJob_buildStep())
                .next(MultiFTP_tasklet_buildStep())
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
    public Step MultiResourceJob_buildStep() throws Exception {

        return stepBuilderFactory.get("MultiResourceJob_buildStep")
                .<Dept, Dept>chunk(chunkSize)
                .reader(multiResourceJob_Reader())
                .processor(multiJob_processor())
                .writer(FixedLength_writer(null))
                .listener(new FileStepExecutionListener())
                .build();
    }

    @Bean
    public Step MultiFTP_tasklet_buildStep() {
        return stepBuilderFactory.get("MultiFTP_tasklet_buildStep")
                .tasklet(taskletConfig()).build();

    }

    @Bean
    public FTpTasklet taskletConfig() {
        FTpTasklet taskletConfig = new FTpTasklet(jobParameter);
        return taskletConfig;
    }


    @Bean
    public Step MultiFTP_buildStep() throws Exception {
        return stepBuilderFactory.get("MultiFTP_buildStep")
            .<Dept, Dept>chunk(chunkSize)
                .reader(multiResourceJob_Reader()) //reader ?????? ??????
                .processor(multiJob_processor())
                .writer(FixedLength_writer(null))
                .listener(new FileStepExecutionListener())
                .build();

    }

    /* multi */
    @Bean
    public MultiResourceItemReader<Dept> multiResourceJob_Reader() throws Exception {
        MultiResourceItemReader<Dept> multiResourceItemReader = new MultiResourceItemReader<>();
        multiResourceItemReader.setResources(
                ResourcePatternUtils
                        .getResourcePatternResolver(this.resourceLoader)
                        .getResources("file:///"+C_FILE_PATH+"/multi/*.txt")  //????????? txt ????????? ????????? .. ?????? ??????
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

                       // log.info("{}", record);

                        //?????? ?????????????????? ??????
                        if (record.indexOf(",") == -1) {
                            return null;
                        }

                        return record.trim(); //???????????? ?????? ??????
                    }

                }).build();
    }


        /*csv*/
    /*
    @Bean
    @StepScope
    public FlatFileItemReader<Dept> multiJob_Reader(@Value ("#{jobParameters[inputfile]}")String inputfile) {
        return new FlatFileItemReaderBuilder<Dept>()
                .name("multiJob_Reader")
                .resource(new FileSystemResource(C_FILE_PATH+inputfile))
                .encoding("UTF-8")
                .delimited().delimiter(",")
                .names("dept_no", "d_name", "loc", "etc")
                .targetType(Dept.class)
                .recordSeparatorPolicy(new SimpleRecordSeparatorPolicy(){

                    @Override
                    public String postProcess(String record) {

                        log.info("policy: {}", record);

                        //?????? ?????????????????? ??????
                        if (record.indexOf(",") == -1) {
                            return null;
                        }

                        return record.trim(); //???????????? ?????? ??????
                    }

                }).build();
    }
                */

    @Bean
    @StepScope
    public ItemProcessor<Dept, Dept> multiJob_processor() {
       return dept -> new Dept(dept.getDept_no(), dept.getD_name(), dept.getLoc(), dept.getEtc());
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

        String dateFormat = DateFormatUtils.format(new Date(), "yyyyMMdd");

        return new FlatFileItemWriterBuilder<Dept>()
                .name("FixedLength_writer")
                .encoding("UTF-8")
                .resource(new FileSystemResource(jobParameter.getFILE_PATH() + jobParameter.getFILE_PREFIX()+ dateFormat + outputfile))
                .lineAggregator(formatterLineAggregator)
                .append(true)
                .build();
    }







}
