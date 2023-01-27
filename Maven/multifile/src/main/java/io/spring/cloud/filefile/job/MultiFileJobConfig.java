package io.spring.cloud.filefile.job;

import io.spring.cloud.filefile.dto.DeptDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.separator.SimpleRecordSeparatorPolicy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;


@Slf4j
@RequiredArgsConstructor
@Configuration
public class MultiFileJobConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private static final int chunkSize = 5;

    @Bean
    public Job MultiJob(){
        return jobBuilderFactory.get("MultiJob")
                //.incrementer(new RunIdIncrementer())
                .start(MultiJob_buildStep(null))
                .build();
    }
    @Bean
    @JobScope
    public Step MultiJob_buildStep(@Value("#{jobParameters[version]}") String version){

        log.info("----");
        log.info(version);
        log.info("----");



        return stepBuilderFactory.get("MultiJob_buildStep")
                .<DeptDTO, DeptDTO>chunk(chunkSize)
                .reader(multiJob_Reader(null))
                .processor(multiJob_processor(null))
                .writer(multiJob_writer(null))
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemReader<DeptDTO> multiJob_Reader(@Value ("#{jobParameters[inputFilename]}")String inputFilename) {
        return new FlatFileItemReaderBuilder<DeptDTO>()
                .name("multiJob_Reader")
                .resource(new ClassPathResource("csvInput/" + inputFilename))
                .delimited().delimiter(",")
                .names("deptNo", "dName", "loc", "etc")
                .targetType(DeptDTO.class)
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
    public ItemProcessor<DeptDTO, DeptDTO> multiJob_processor(@Value("#{jobParameters[version]}")String version) {
        log.info("processor: {}" ,version);
        return deptDTO -> new DeptDTO(deptDTO.getDeptNo(), deptDTO.getDName(), deptDTO.getLoc(), deptDTO.getEtc());
    }


    @Bean
    @StepScope
    public FlatFileItemWriter<DeptDTO> multiJob_writer(@Value("#{jobParameters[outputFileName]}") String outputFileName){
        return new FlatFileItemWriterBuilder<DeptDTO>()
                .name("multiJob_writer")
                .resource(new FileSystemResource("sample/"+outputFileName))
                .lineAggregator(item -> {
                    return item.getDeptNo()+"-------"+item.getDName()+"-------"+item.getLoc()+"-------"+item.getEtc();
                })
                .build();



    }


}
