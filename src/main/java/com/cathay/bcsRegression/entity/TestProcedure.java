package com.cathay.bcsRegression.entity;

import lombok.Data;

import java.util.Date;

@Data
public class TestProcedure {
    private Long id;

    private Long fromCaseId;

    private Integer procedureSeq;

    private Integer type;

    private String detail;

    private Integer version;

    private Integer isDelete;

    private String createdBy;

    private Date createdTime;

    private String updatedBy;

    private Date updatedTime;
}