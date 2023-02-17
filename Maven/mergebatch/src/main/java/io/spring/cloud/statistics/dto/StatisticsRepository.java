package io.spring.cloud.statistics.dto;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface StatisticsRepository extends JpaRepository<StatisticsEntity, Integer> {
    @Query(value = "SELECT new io.spring.cloud.statistics.dto.AggregatedStatistics(s.statisticsAt, SUM(s.allCount), SUM(s.ORG_ID_NAVER_Count), SUM(s.ORG_ID_DAUM_Count), SUM(s.ORG_ID_ICONLOOP_Count), SUM(s.ORG_ID_ECHO_Count))" +
            "      FROM StatisticsEntity s " +
            "        WHERE s.statisticsAt BETWEEN :from AND :to " +
            "     GROUP BY s.statisticsAt")
    List<AggregatedStatistics> findByStatisticsAtBetweenAndGroupBy(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    //objectlist로 반환 되기 때문에, AggregatedStatistics로 셋팅
//    @Query(value = "SELECT new io.spring.cloud.statistics.dto.AggregatedStatistics(s.statisticsAt, SUM(s.allCount), SUM(s.ORG_ID_NAVER_Count), SUM(s.ORG_ID_DAUM_Count), SUM(s.ORG_ID_ICONLOOP_Count), SUM(s.ORG_ID_ECHO_Count)) " +
//            "        FROM StatisticsEntity s " +
//            "        WHERE s.statisticsAt BETWEEN :from AND :to " +
//            "     GROUP BY s.statisticsAt", nativeQuery = true)
//    List<AggregatedStatistics> findByStatisticsAtBetweenAndGroupBy(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

}