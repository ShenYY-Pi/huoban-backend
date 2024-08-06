package com.syy.usercenter.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用删除请求参数
 */
@Data
public class DeleteRequest implements Serializable {
    private static final long serialVersionUID = 8868491008960586880L;
    private long id;
}
