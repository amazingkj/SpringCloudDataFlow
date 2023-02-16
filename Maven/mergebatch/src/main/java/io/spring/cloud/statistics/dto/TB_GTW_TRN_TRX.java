package io.spring.cloud.statistics.dto;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;


@Getter
@Setter
@AllArgsConstructor
@Builder
@ToString
@Entity(name = "logdata")
@Table(name = "logdata")
public class TB_GTW_TRN_TRX {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 기본 키 생성을 DB에 위임합니다. (AUTO_INCREMENT)
    private Integer logSeq;
    private String TRN_DAY;
    private LocalDateTime TRN_DT;
    private String TRN_TM;
    private String TRN_ITR_GID;
    private String TRN_GID;
    private String ED_DT;
    private String ED_TM;
    private String ENGINE;
    private String API_ID;
    private String API_NM;
    private String TRAN_TYPE;
    private String PRE_FLOW_ID;
    private String POST_FLOW_ID;
    private String RULE_ID;
    private String PROC_STS;
    private String ERR_MSG;
    private String TRN_DTL_SEQ;
    private String HST_PBLS_STP;
    private String DAT;
    private String DAT_LENGTH;
    private String SOURCE_URI;
    private String TARGET_URL;
    private String SRC_METHOD_TYPE;
    private String TGT_METHOD_TYPE;
    @Enumerated(EnumType.STRING)
    private ORG_ID ORG_ID;

    public TB_GTW_TRN_TRX() {

    }

    public enum ORG_ID{
        NAVER, ICONLOOP, DAUM, ECHO
    }

    public enum SRC_METHOD_TYPE_Status {
        GET, POST
    }
    // TRN_DT 기준, yyyy-MM-HH 00:00:00
    public LocalDateTime getStatisticsAt() {
        return this.TRN_DT.withHour(0).withMinute(0).withSecond(0).withNano(0);

    }

//    public LocalDateTime getStatisticsAt() {
//        return this.TRN_DT.withHour(0).withMinute(0).withSecond(0).withNano(0);
//
//    }



}
