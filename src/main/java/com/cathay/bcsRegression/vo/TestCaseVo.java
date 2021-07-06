package com.cathay.bcsRegression.vo;

import lombok.Data;

import java.util.Date;

@Data
public class TestCaseVo {
    private Long id;

    private String caseName;

    private Long fromModuleId;
}