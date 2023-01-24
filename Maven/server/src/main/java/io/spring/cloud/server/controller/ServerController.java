package io.spring.cloud.server.controller;

import io.spring.cloud.server.dto.Dept;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/server")
public class ServerController {

    @GetMapping("")
    public Dept get(@RequestParam String d_name, @RequestParam String loc){
        Dept dept = new Dept();
        dept.setD_name(d_name);
        dept.setLoc(loc);
        log.info("setD_name : {}",d_name);
        log.info("setLoc : {}", loc);
        return dept;
    }


    @PostMapping("")
    public Dept post(@RequestBody Dept d_name){
        return d_name;
    }

    @PostMapping("header")
    public Dept header(@RequestHeader(value = "x-authorization") String header, @RequestBody Dept d_name){
        log.info("header : {}",header);
        log.info("body : {}", d_name);

        return d_name;
    }
}
