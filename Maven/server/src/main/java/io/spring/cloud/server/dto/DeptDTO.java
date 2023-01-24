package io.spring.cloud.server.dto;

import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class DeptDTO {

    Integer deptNo;
    String dName;
    String loc;

    String etc;


}
