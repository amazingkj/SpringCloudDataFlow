package io.spring.cloud.statistics.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Entity(name = "StatisticsEntity")
@Table(name = "StatisticsEntity")
public class StatisticsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 기본 키 생성을 DB에 위임합니다. (AUTO_INCREMENT)
    @Column(name= "statisticsSeq")
    private Integer statisticsSeq;
    @Column(name= "statisticsAt")
    private LocalDateTime statisticsAt; // 일 단위

    @Column(name= "allCount")
    private long allCount;
    @Column(name= "ORG_ID_NAVER_Count")
    private long ORG_ID_NAVER_Count;
    @Column(name= "ORG_ID_ICONLOOP_Count")
    private long ORG_ID_ICONLOOP_Count;
    @Column(name= "ORG_ID_DAUM_Count")
    private long ORG_ID_DAUM_Count;
    @Column(name= "ORG_ID_ECHO_Count")
    private long ORG_ID_ECHO_Count;
    @Column(name= "SRC_METHOD_TYPE_Count")
    private long SRC_METHOD_TYPE_Count;


    public static StatisticsEntity create(final TB_GTW_TRN_TRX logdata) {
        StatisticsEntity statisticsEntity = new StatisticsEntity();
        statisticsEntity.setStatisticsAt(logdata.getStatisticsAt());

        statisticsEntity.setAllCount(1);

        if (TB_GTW_TRN_TRX.ORG_ID.NAVER.equals(logdata.getORG_ID())) {
            statisticsEntity.setORG_ID_NAVER_Count(1);

        }else if(TB_GTW_TRN_TRX.ORG_ID.ICONLOOP.equals(logdata.getORG_ID())){
            statisticsEntity.setORG_ID_ICONLOOP_Count(1);

        }else if(TB_GTW_TRN_TRX.ORG_ID.DAUM.equals(logdata.getORG_ID())){
            statisticsEntity.setORG_ID_DAUM_Count(1);

        }else if(TB_GTW_TRN_TRX.ORG_ID.ECHO.equals(logdata.getORG_ID())){
            statisticsEntity.setORG_ID_ECHO_Count(1);
        }

        return statisticsEntity;

    }

    public void add(final TB_GTW_TRN_TRX logdata) {
        this.allCount++;

        if (TB_GTW_TRN_TRX.ORG_ID.NAVER.equals(logdata.getORG_ID())) {
            this.ORG_ID_NAVER_Count++;

        }else if(TB_GTW_TRN_TRX.ORG_ID.ICONLOOP.equals(logdata.getORG_ID())){
            this.ORG_ID_ICONLOOP_Count++;

        }else if(TB_GTW_TRN_TRX.ORG_ID.DAUM.equals(logdata.getORG_ID())){
            this.ORG_ID_DAUM_Count++;

        }else if(TB_GTW_TRN_TRX.ORG_ID.ECHO.equals(logdata.getORG_ID())){
            this.ORG_ID_ECHO_Count++;
        }

        if (TB_GTW_TRN_TRX.SRC_METHOD_TYPE_Status.GET.equals(logdata.getSRC_METHOD_TYPE())) {
            this.SRC_METHOD_TYPE_Count++;

        }


    }

}
