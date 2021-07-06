package com.cathay.bcsRegression.service;


import com.cathay.bcsRegression.entity.TestLog;
import com.cathay.bcsRegression.vo.TestLogVo;
import com.cathay.bcsRegression.vo.TestTaskVo;
import com.cathay.common.vo.ReturnVo;

import java.util.List;

/**
 * @author 0100065352
 */
public interface RegressionTestService {

    /**
     * 提交测试任务
     */
    ReturnVo<String> submitTest(List<Long> testCaseIdList);

    /**
     * 执行测试任务
     */
    void doTest();

    ReturnVo<List<TestTaskVo>> queryTest(String taskId);

    ReturnVo<List<TestLog>> queryLog(String taskId);
}
