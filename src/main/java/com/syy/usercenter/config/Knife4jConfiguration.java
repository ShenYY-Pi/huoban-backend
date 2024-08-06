package com.syy.usercenter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

@Configuration
@EnableSwagger2WebMvc
public class Knife4jConfiguration {

//    @Bean(value = "dockerBean")
//    public Docket dockerBean() {
//        //指定使用Swagger2规范
//        Docket docket=new Docket(DocumentationType.SWAGGER_2)
//                .apiInfo(new ApiInfoBuilder()
//                        .title("伙伴匹配系统")
//                        //描述字段支持Markdown语法
//                        .description("伙伴匹配系统接口测试")
//                        .version("1.0")
//                        .build())
//                .select()
//                //这里指定Controller扫描包路径
//                .apis(RequestHandlerSelectors.basePackage("com.syy.usercenter.controller"))
//                .paths(PathSelectors.any())
//                .build();
//        return docket;
//    }

    @Bean
    public Docket defaultApi2() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(new ApiInfoBuilder()
                        .title("伙伴匹配系统")
                        .description("伙伴匹配系统接口测试")
                        .version("1.0")
                        .build())
                .select()
                // 指定 Controller 扫描包路径
                .apis(RequestHandlerSelectors.basePackage("com.syy.usercenter.controller"))
                .paths(PathSelectors.any())
                .build();
    }
}
