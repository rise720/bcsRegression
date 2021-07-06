package com.cathay.bcsRegression.entity;

import lombok.Data;

import java.util.Date;

@Data
public class TestLog {
    private Long id;

    private Long batchId;

    private Long caseId;

    private String msgLevel;

    private String message;

    private Integer version;

    private Integer isDelete;

    private String createdBy;

    private Date createdTime;

    private String updatedBy;

    private Date updatedTime;

}