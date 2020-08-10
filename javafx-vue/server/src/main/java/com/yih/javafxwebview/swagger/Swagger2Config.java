package com.yih.javafxwebview.swagger;


import com.fasterxml.classmate.TypeResolver;
import com.yih.javafxwebview.model.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.Tags;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class Swagger2Config {

    @Autowired
    private TypeResolver typeResolver;

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2).groupName("first")
                .select()
                .apis(RequestHandlerSelectors
                        .basePackage("com.yih.javafxwebview.controller"))
                .paths(PathSelectors.regex(".*test*"))
                .build().apiInfo(apiEndPointsInfo())
                .additionalModels(typeResolver.resolve(Model.class)/*, typeResolver.resolve(XXX.class), ...*/);
    }
    private ApiInfo apiEndPointsInfo() {
        return new ApiInfoBuilder().title("Spring Boot REST API")
                .description("Employee Management REST API")
                .contact(new Contact("yihchu", "", "yihchu694800 687@163.com"))
                .license("Apache 2.0")
                .licenseUrl("http://www.apache.org/licenses/LICENSE-2.0.html")
                .version("1.0.0")
                .build();
    }

//    @Bean
//    public Docket api2() {
//        return new Docket(DocumentationType.SWAGGER_2).groupName("second")
//                .select()
//                .apis(RequestHandlerSelectors.basePackage("com.yih.javafxwebview.controller1"))
//                .paths(PathSelectors.any())
//                .build()
//                .additionalModels(typeResolver.resolve(Model.class)/*, typeResolver.resolve(XXX.class), ...*/);
//    }

}
