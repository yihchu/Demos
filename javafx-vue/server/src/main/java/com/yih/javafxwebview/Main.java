package com.yih.javafxwebview;

import com.yih.javafxwebview.model.XMLModel;
import javafx.application.Application;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

@SpringBootApplication
@Slf4j
public class Main {

    public static void main(String[] argv) {
        ApplicationContext context = new ClassPathXmlApplicationContext("test.xml");
        XMLModel obj = (XMLModel) context.getBean("xmlModel");
        obj.selfIntroduce();
        SpringApplication.run(Main.class, argv);
        Application.launch(FXApplication.class, argv);

    }

}
