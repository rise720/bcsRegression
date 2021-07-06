package com.cathay.bcsRegression.entity;

import lombok.Data;

import java.util.Date;

@Data
public class TestTable {
    private Long id;

    private String tableId;

    private String tableName;

    private String tableSchema;

    private String tableKeys;

    private Integer version;

    private Integer isDelete;

    private String createdBy;

    private Date createdTime;

    private String updatedBy;

    private Date updatedTime;

}