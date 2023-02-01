package io.spring.cloud.dbfile.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Dept {

    Integer dept_no;
    String d_name;
    String loc;
    String etc;


    public Dept(String d_name, String loc) {
    }
}
