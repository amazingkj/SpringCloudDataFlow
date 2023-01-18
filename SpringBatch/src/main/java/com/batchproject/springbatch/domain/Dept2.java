package com.batchproject.springbatch.domain;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Getter
@Setter
@ToString
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Dept2 {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer deptNo;
    String dName;
    String loc;

}
