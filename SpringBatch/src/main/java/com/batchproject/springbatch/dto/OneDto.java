package com.batchproject.springbatch.dto;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class OneDto {
    private String one;

    @Override
    public String toString(){
        return one;
    }
}
