package com.cathay.bcsRegression.vo;

import cn.hutool.core.util.StrUtil;
import com.cathay.bcsRegression.constant.CommonConstant;
import com.cathay.bcsRegression.entity.TestCase;
import com.cathay.bcsRegression.entity.TestTask;
import lombok.Data;
import org.apache.commons.lang.StringUtils;

import java.util.Date;

/**
 * @author 0100065352
 */
@Data
public class TestLogVo {

    private Long batchId;

    private Long caseId;

    private Long caseName;

    private String msgLevel;

    private String message;

    public TestLogVo(TestTaskVo testTaskVo, String msgLevel, String message) {
        this.batchId = testTaskVo.getBatchId();
        this.caseId = testTaskVo.getCaseId();
        this.msgLevel = msgLevel;
        this.message = StringUtils.isBlank(testTaskVo.getCaseName()) ?
                StrUtil.format("[{}][ID:{}] {}", this.msgLevel, this.batchId, message) :
                StrUtil.format("[{}][ID:{}][Case:{}] {}", this.msgLevel, this.batchId, testTaskVo.getCaseName(), message);
    }

}