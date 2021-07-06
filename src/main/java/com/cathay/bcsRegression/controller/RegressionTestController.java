package com.cathay.bcsRegression.controller;

import cn.hutool.core.util.StrUtil;
import com.cathay.bcsRegression.entity.TestLog;
import com.cathay.bcsRegression.service.RegressionTestService;
import com.cathay.bcsRegression.vo.TestLogVo;
import com.cathay.bcsRegression.vo.TestTaskVo;
import com.cathay.common.vo.ReturnVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


/**
 * 执行测试相关操作的控制类
 * @author 0100065352
 */
@RestController
@RequestMapping("/regression")
@Slf4j
@Transactional
public class RegressionTestController {

    @Autowired
    private RegressionTestService regressionTestService;

    @PostMapping("/submitTask")
    @Transactional
    public ReturnVo<String> submitTask(@RequestBody List<Long> testCaseIdList){
        log.info("收到提交测试任务的请求");
        ReturnVo<String> returnVo = new ReturnVo<String>().failure();

        try{
            ReturnVo<String> serviceReturnVo = regressionTestService.submitTest(testCaseIdList);
            if(serviceReturnVo.isFailure()){
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return returnVo.failure(serviceReturnVo.getMessage());
            }
            return returnVo.success(serviceReturnVo.getData());
        }catch(Exception e){
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.error("提交测试任务失败！", e);
            return returnVo.failure(StrUtil.format("提交测试任务失败！原因：{}", e.getMessage()));
        }
    }

    @PostMapping("/queryTask")
    public ReturnVo<List<TestTaskVo>> queryTask(@RequestBody String taskId){
        log.info("收到查询测试任务的请求");
        try{
            return regressionTestService.queryTest(taskId);
        }catch(Exception e){
            log.error("查询测试任务失败！", e);
            return new ReturnVo<List<TestTaskVo>>().failure();
        }
    }

    @PostMapping("/queryLog")
    public ReturnVo<List<TestLog>> queryLog(@RequestBody String taskId){
        log.info("收到查询测试任务的请求");
        try{
            return regressionTestService.queryLog(taskId);
        }catch(Exception e){
            log.error("查询测试任务失败！", e);
            return new ReturnVo<List<TestLog>>().failure();
        }
    }

}
