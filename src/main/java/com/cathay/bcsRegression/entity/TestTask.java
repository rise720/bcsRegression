package com.cathay.bcsRegression.entity;

import lombok.Data;

import java.util.Date;

@Data
public class TestTask {
    private Long id;

    private Long batchId;

    private Long caseId;

    private Integer status;

    private Integer currentProcedureSeq;

    private String logUrl;

    private Integer version;

    private Integer isDelete;

    private String createdBy;

    private Date createdTime;

    private String updatedBy;

    private Date updatedTime;
}