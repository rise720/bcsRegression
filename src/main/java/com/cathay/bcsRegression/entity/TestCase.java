package com.cathay.bcsRegression.entity;

import lombok.Data;

import java.util.Date;

@Data
public class TestCase {
    private Long id;

    private String caseId;

    private String caseName;

    private Long fromModuleId;

    private Integer version;

    private Integer isDelete;

    private String createdBy;

    private Date createdTime;

    private String updatedBy;

    private Date updatedTime;
}