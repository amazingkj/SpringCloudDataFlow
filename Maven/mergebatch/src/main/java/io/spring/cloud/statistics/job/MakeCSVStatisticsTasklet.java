package io.spring.cloud.statistics.job;

import io.spring.cloud.statistics.dto.AggregatedStatistics;
import io.spring.cloud.statistics.dto.StatisticsEntityRepository;
import io.spring.cloud.statistics.util.CustomCSVWriter;
import io.spring.cloud.statistics.util.LocalDateTimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@StepScope
public class MakeCSVStatisticsTasklet implements Tasklet {

    private final StatisticsEntityRepository statisticsRepository;

    @Value("#{jobParameters[from]}")
    private String fromString;
    @Value("#{jobParameters[to]}")
    private String toString;

    public MakeCSVStatisticsTasklet( StatisticsEntityRepository statisticsRepository) {
        this.statisticsRepository = statisticsRepository;
    }


    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

        final LocalDateTime from = LocalDateTime.parse(fromString);
        final LocalDateTime to = LocalDateTime.parse(toString);

        log.warn(" tasklet from {}",from);
        log.warn(" tasklet to {}",to);


        final List<AggregatedStatistics> statisticsList = statisticsRepository.findByStatisticsAtBetweenAndGroupBy(from, to);

        log.warn(" statisticsList  {}",statisticsList);
        List<String[]> data = new ArrayList<>();
        data.add(new String[]{"statisticsAt", "ORG_ID_NAVER_Count", "ORG_ID_DAUM_Count", "ORG_ID_ICONLOOP_Count", "ORG_ID_ECHO_Count"});
        for (AggregatedStatistics statistics : statisticsList) {
            data.add(new String[]{
                    LocalDateTimeUtils.format(statistics.getStatisticsAt()),
                    String.valueOf(statistics.getAllCount()),
                    String.valueOf(statistics.getORG_ID_NAVER_Count()),
                    String.valueOf(statistics.getORG_ID_DAUM_Count()),
                    String.valueOf(statistics.getORG_ID_ICONLOOP_Count()),
                    String.valueOf(statistics.getORG_ID_ECHO_Count()),
            });
        }
        CustomCSVWriter.write("daily_statistics_" + LocalDateTimeUtils.format(from, LocalDateTimeUtils.YYYY_MM_DD) + ".csv", data);
        return RepeatStatus.FINISHED;

    }
}
