package com.batchproject.springbatch.job;

import com.batchproject.springbatch.custom.CustomPassLineAggregator;
import com.batchproject.springbatch.dto.OneDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class txtTocsvJob {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private static final int chunkSize = 5;

    @Bean
    public Job txtTocvsJob_batchBuild(){
        return jobBuilderFactory.get("txtTocvsJob")
                .incrementer(new RunIdIncrementer())
                .start(txtTocvsJob_batchStep1())
                .build();
    }

    @Bean
    public Step txtTocvsJob_batchStep1(){
        return stepBuilderFactory.get("txtTocvsJob_batchStep1")
                .<OneDto, OneDto>chunk(chunkSize)
                .reader(txtTocvsJob_FileReader())
                .writer(txtTocvsJob_FileWriter()).build();
    }
    @Bean
    public FlatFileItemReader<OneDto> txtTocvsJob_FileReader(){
        FlatFileItemReader<OneDto> flatFileItemReader = new FlatFileItemReader<>();
        flatFileItemReader.setResource(new ClassPathResource("source/txtTocvsJob_input.txt")); //resource/source/tex- 경로에서 읽어오는 자료
        flatFileItemReader.setLineMapper(((line, lineNumber) -> new OneDto(lineNumber+","+line)));

    return flatFileItemReader;
    }
    @Bean
    public FlatFileItemWriter txtTocvsJob_FileWriter(){
        return new FlatFileItemWriterBuilder<OneDto>()
                .name("txtTocvsJob_FileWriter")
                .resource(new FileSystemResource("output/txtTocvsJob_output.csv"))
                .lineAggregator(new CustomPassLineAggregator<>())
                .build();
    }

}
