package io.spring.cloud.statistics.util;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileWriter;
import java.util.List;
import com.opencsv.CSVWriter;
@Slf4j
public class CustomCSVWriter  {


    public static int write(final String fileName, List<String[]> data) {
        int rows = 0;
        try (CSVWriter writer = new CSVWriter(new FileWriter(fileName))) {
            writer.writeAll(data);
            rows = data.size();
            log.info("CustomCSVWriter - write: CSV 파일 생성 성공, fileName: {}", fileName);
        } catch (Exception e) {
            log.error("CustomCSVWriter - write: CSV 파일 생성 실패, fileName: {}", fileName);

        }
        return rows;

    }
}
