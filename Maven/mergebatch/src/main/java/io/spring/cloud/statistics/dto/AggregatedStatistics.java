package io.spring.cloud.statistics.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class AggregatedStatistics {

    private LocalDateTime statisticsAt; // 일 단위
    private long allCount;
    private long ORG_ID_NAVER_Count;
    private long ORG_ID_ICONLOOP_Count;
    private long ORG_ID_DAUM_Count;
    private long ORG_ID_ECHO_Count;
    private long SRC_METHOD_TYPE_Count;

    public void merge(final AggregatedStatistics statistics) {
        this.allCount += statistics.getAllCount();
        this.ORG_ID_NAVER_Count += statistics.getORG_ID_NAVER_Count();
        this.ORG_ID_ICONLOOP_Count += statistics.getORG_ID_ICONLOOP_Count();
        this.ORG_ID_DAUM_Count += statistics.getORG_ID_DAUM_Count();
        this.ORG_ID_ECHO_Count += statistics.getORG_ID_ECHO_Count();


    }
}
