package io.spring.cloud.quartz.parameters;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Slf4j
@Getter
@NoArgsConstructor
@Component
public class CustomJobParameter {

    private String cronschedule;
    private String name;
    private String properties ;
    private String argument;

    @Value("${custom.task.cronschedule}")
    public void setCronschedule(String cronschedule) {
        this.cronschedule = cronschedule;
    }

    @Value("${custom.task.name}")
    public void setName(String name) {
        this.name = name;
    }

    /*
    @Value("${custom.task.properties}")
    public void setProperties(String properties) {
        this.properties = properties;
    }

    @Value("${custom.task.argument}")
    public void setArgument(String argument) {
        this.argument = argument;
    }
    */

}
