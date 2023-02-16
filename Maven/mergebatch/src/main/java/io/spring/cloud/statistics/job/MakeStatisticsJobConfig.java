package io.spring.cloud.statistics.job;

import io.spring.cloud.statistics.dto.StatisticsEntity;
import io.spring.cloud.statistics.dto.StatisticsEntityRepository;
import io.spring.cloud.statistics.dto.TB_GTW_TRN_TRX;
import io.spring.cloud.statistics.util.LocalDateTimeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.*;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;
import java.time.LocalDateTime;
import java.util.*;


@Slf4j
@RequiredArgsConstructor
@Configuration
public class MakeStatisticsJobConfig {
    private final StatisticsEntityRepository statisticsEntityRepository;
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;

    private static final int chunkSize = 5;

    private final MakeCSVStatisticsTasklet makeCSVStatisticsTasklet;

    private static final String FILE_PATH = "C:/amazing/DataflowProject/Maven/resources/";


    @Bean
    public Job MakeStatisticsJob() throws Exception {
        return jobBuilderFactory.get("MakeStatisticsJob")
                .incrementer(new DailyJobTimestamper())
                .start(MakeStatisticsJob_buildStep())
                .next(makeCSVStatisticsStep())
                .listener(new JobExecutionListener())
                //.validator(new ParameterValidator())
                .build();
    }

    @Bean
    public Step MakeStatisticsJob_buildStep() throws Exception {
        return stepBuilderFactory.get("MakeStatisticsJob_buildStep")
                .<TB_GTW_TRN_TRX, TB_GTW_TRN_TRX>chunk(chunkSize)
                .reader(addStatisticsItemReader(null,null))
                //.processor(DBFileJob_processor())
                .writer(addStatisticsItemWriter())
                .build();
    }

    @Bean
    public Step makeCSVStatisticsStep() {
        return this.stepBuilderFactory.get("makeCSVStatisticsStep")
                .tasklet(makeCSVStatisticsTasklet)
                .build();
    }



    @Bean
    @StepScope
    public JpaCursorItemReader<TB_GTW_TRN_TRX> addStatisticsItemReader(@Value("#{jobParameters[from]}") String fromString, @Value("#{jobParameters[to]}") String toString) {
        log.warn("fromString {}",fromString);
        log.warn("toString {}",toString);

        LocalDateTime from = LocalDateTime.parse(fromString);
        LocalDateTime to = LocalDateTime.parse(toString);
        //LocalDateTime from = LocalDateTimeUtils.parse(fromString);
       // LocalDateTime to = LocalDateTime.parse(toString);
        log.warn("from {}",from);
        log.warn("to {}",to);

        //final LocalDateTime from = LocalDateTimeUtils.parse(fromString);
        //final LocalDateTime to = LocalDateTimeUtils.parse(toString);

        return new JpaCursorItemReaderBuilder<TB_GTW_TRN_TRX>()
                .name("addStatisticsItemReader")
                .entityManagerFactory(entityManagerFactory)
                // JobParameter를 받아 종료 일시(TRN_DT) 기준으로 통계 대상 logdata 조회
                .queryString("select b from logdata b where b.TRN_DT between :from and :to")
                .parameterValues(Map.of("from", from, "to", to))
                .build();
    }

    @Bean
    public ItemWriter<TB_GTW_TRN_TRX> addStatisticsItemWriter() {
        return logdataEntities -> {
            Map<LocalDateTime, StatisticsEntity> statisticsEntityMap = new LinkedHashMap<>();

            for (TB_GTW_TRN_TRX logdata : logdataEntities) {
                final LocalDateTime statisticsAt = logdata.getStatisticsAt();
                StatisticsEntity statisticsEntity = statisticsEntityMap.get(statisticsAt);

                if (statisticsEntity == null) {
                    statisticsEntityMap.put(statisticsAt, StatisticsEntity.create(logdata));

                } else {
                    statisticsEntity.add(logdata);

                }

            }
            final List<StatisticsEntity> statisticsEntities = new ArrayList<>(statisticsEntityMap.values());
            statisticsEntityRepository.saveAll(statisticsEntities);

        };
    }


}
