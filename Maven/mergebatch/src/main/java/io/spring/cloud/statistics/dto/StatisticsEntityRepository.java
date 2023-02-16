package io.spring.cloud.statistics.dto;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface StatisticsEntityRepository extends JpaRepository<StatisticsEntity, Integer> {
    @Query(value = "SELECT (s.statisticsAt, SUM(s.allCount), SUM(s.ORG_ID_NAVER_Count), SUM(s.ORG_ID_DAUM_Count), SUM(s.ORG_ID_ICONLOOP_Count), SUM(s.ORG_ID_ECHO_Count)) " +
            "        FROM StatisticsEntity s " +
            "        WHERE s.statisticsAt BETWEEN :from AND :to " +
            "     GROUP BY s.statisticsAt", nativeQuery = true)
    List<AggregatedStatistics> findByStatisticsAtBetweenAndGroupBy(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

}