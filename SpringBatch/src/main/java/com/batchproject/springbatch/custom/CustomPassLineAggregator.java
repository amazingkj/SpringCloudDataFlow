package com.batchproject.springbatch.custom;

import com.batchproject.springbatch.dto.OneDto;
import org.springframework.batch.item.file.transform.LineAggregator;

public class CustomPassLineAggregator<T> implements LineAggregator<T> {


    @Override
    public String aggregate(T item) {

        if(item instanceof OneDto){
            return item.toString()+"_item";
        }

        return item.toString();
    }
}
