package com.syy.usercenter.streamdomain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
 
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Book {
    //id
    private Long id;
    //书名
    private String name;
    //分类
    private String category;
    //评分
    private Integer score;
    //简介
    private String intro;
}