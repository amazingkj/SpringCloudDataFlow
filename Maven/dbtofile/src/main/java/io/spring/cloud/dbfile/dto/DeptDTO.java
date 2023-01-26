package io.spring.cloud.dbfile.dto;

import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Data
public class DeptDTO {

    Integer deptNo;
    String dName;
    String loc;

    String etc;


}
