package io.spring.cloud.quartz.quartz;

import io.spring.cloud.quartz.parameters.CustomJobParameter;
import lombok.AllArgsConstructor;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import static org.quartz.CronScheduleBuilder.cronSchedule;
@Configuration
@AllArgsConstructor
public class QuartzConfiguration {
    private final CustomJobParameter jobParameter;

    //쿼츠 잡 빈 등록, QuartzJobLauncher 등록
    @Bean
    public JobDetail quartzJobDetail() {
        return JobBuilder.newJob(QuartzJobLauncher.class) // JobDetail을 정의하는 JobBuilder
                .storeDurably() //트리거가 존재하지 않더라도 해당 Job 인스턴스가 삭제되지 않도록 유지하는 옵션
                .build();

    }

    //동작 일정 등록하는 트리거
    @Bean
    public Trigger jobTrigger() {

       // SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.simpleSchedule()
       //         .withIntervalInSeconds(5).withRepeatCount(4);

        return TriggerBuilder.newTrigger()
                .forJob(quartzJobDetail())      //quartzJob
                .withSchedule(cronSchedule(jobParameter.getCronschedule()))  //schedule "5 * * * * ?"
                .build();                       //trigger

    }





}
