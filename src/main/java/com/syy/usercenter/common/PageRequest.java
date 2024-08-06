package com.syy.usercenter.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用分页请求参数
 */
@Data
public class PageRequest implements Serializable {
    private static final long serialVersionUID = 8868491008960586880L;
    /**
     * 页面大小
     */
    protected int pageSize = 1000;

    /**
     * 页面是第几页
     */
    protected int pageNum = 1;
}
