package com.yih.javafxwebview.controller;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(value="JavaFxWeb App", tags = {"Main"})
@RestController
@RequestMapping("api")
@Slf4j
public class Controller {


//    @Value("${spring.profiles.active}")
//    private String environment;


    static {
        log.info("Serving ...");
    }

    @ApiOperation(value = "For Test", response = String.class)
    @GetMapping("/test")
    public String test() {

//        log.info(">>>>> environment = {}", environment);

        return "Hello World";
    }

}
