package com.cathay.bcsRegression.vo;

import lombok.Data;

import java.util.Date;

/**
 * @author 0100065352
 */
@Data
public class TestTaskVo {

    private Long batchId;

    private Long caseId;

    private String caseName;

    private Integer status;

    private Integer version;
}