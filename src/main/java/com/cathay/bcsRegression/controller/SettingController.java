package com.cathay.bcsRegression.controller;


import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.cathay.bcsRegression.service.SettingService;
import com.cathay.bcsRegression.vo.TestCaseVo;
import com.cathay.bcsRegression.vo.TestModuleVo;
import com.cathay.bcsRegression.vo.TestProcedureVo;
import com.cathay.bcsRegression.vo.TestTableVo;
import com.cathay.common.vo.ReturnVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * 相关设定的控制类
 * @author 0100065352
 */
@RestController
@RequestMapping("/set")
@Slf4j
public class SettingController {

    @Autowired
    private SettingService settingService;

    @Value("${task.upload.rootPath}")
    private String uploadRootPath;


    @PostMapping("/saveTestTable")
    public ReturnVo<String> saveTestTable(@RequestBody List<TestTableVo> testTableVoList){
        log.info("收到保存测试用表信息的请求");
        ReturnVo<String> returnVo = new ReturnVo<String>().failure();

        if( null == testTableVoList || 0 == testTableVoList.size()){
            return returnVo.failure("未获取到测试用表信息！");
        }

        //处理失败次数
        int failureNum = 0;
        //处理失败原因
        String errMsg = "";
        for(TestTableVo testTableVo : testTableVoList){
            try{
                ReturnVo<String> serviceReturnVo = settingService.saveTestTable(testTableVo);
                if(serviceReturnVo.isFailure()){
                    failureNum += 1;
                    errMsg = serviceReturnVo.getMessage();
                }
            }catch(Exception e){
                log.error("保存测试用表信息失败！", e);
                failureNum += 1;
                errMsg = StrUtil.format("保存测试用表信息失败！原因：{}", e.getMessage());
            }
        }
        return failureNum == 0 ? returnVo.success("保存测试表信息成功！") : returnVo.failure(StrUtil.format(
                "收到保存测试用表信息的总计数为：{}，保存失败数为：{}，错误原因：{}", testTableVoList.size(), failureNum, errMsg));
    }


    @PostMapping("/saveTestModule")
    public ReturnVo<String> saveTestModule(@RequestBody TestModuleVo testModuleVo){
        log.info("收到保存测试模块信息的请求");
        ReturnVo<String> returnVo = new ReturnVo<String>().failure();

        try{
            ReturnVo<String> serviceReturnVo = settingService.saveTestModule(testModuleVo);
            if(serviceReturnVo.isFailure()){
                return returnVo.failure(serviceReturnVo.getMessage());
            }
        }catch(Exception e){
            log.error("保存测试模块信息失败！", e);
            return returnVo.failure(StrUtil.format("保存测试模块信息失败！原因：{}", e.getMessage()));
        }
        return returnVo.success("保存测试模块信息成功！");
    }


    @PostMapping("/saveTestCase")
    public ReturnVo<String> saveTestCase(@RequestBody TestCaseVo testCaseVo){
        log.info("收到保存测试用例的请求");
        ReturnVo<String> returnVo = new ReturnVo<String>().failure();

        try{
            ReturnVo<String> serviceReturnVo = settingService.saveTestCase(testCaseVo);
            if(serviceReturnVo.isFailure()){
                return returnVo.failure(serviceReturnVo.getMessage());
            }
        }catch(Exception e){
            log.error("保存测试用例信息失败！", e);
            return returnVo.failure(StrUtil.format("保存测试用例失败！原因：{}", e.getMessage()));
        }
        return returnVo.success("保存测试用例信息成功！");
    }


    @PostMapping("/saveTestProcedure")
    @Transactional
    public ReturnVo<String> saveTestProcedure(@RequestBody List<TestProcedureVo> testProcedureVoList){
        log.info("收到保存测试步骤的请求");
        ReturnVo<String> returnVo = new ReturnVo<String>().failure();

        try{
            ReturnVo<String> serviceReturnVo = settingService.saveTestProcedure(testProcedureVoList);
            if(serviceReturnVo.isFailure()){
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return returnVo.failure(serviceReturnVo.getMessage());
            }
        }catch(Exception e){
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.error("保存测试步骤信息失败！", e);
            return returnVo.failure(StrUtil.format("保存测试步骤失败！原因：{}", e.getMessage()));
        }
        return returnVo.success("保存测试步骤信息成功！");
    }


    @PostMapping("/uploadFile")
    public ReturnVo<String> uploadFile(MultipartFile file){
        log.info("收到文件上传请求");
        ReturnVo<String> returnVo = new ReturnVo<String>().failure();

        //检查
        if(file.isEmpty()){
            return returnVo.failure("未获取到上传文件！");
        }

        //获取文件类型
        int fileTypeIndex = file.getOriginalFilename().lastIndexOf(".");
        if(0 > fileTypeIndex ){
            return returnVo.failure("未获取到上传文件的文件类型！");
        }
        String fileType = file.getOriginalFilename().substring(fileTypeIndex).trim();

        //目标文件格式：/xxxx/yyyyMMdd/timestamp.xxx
        Date today = new Date();
        File destinationFile = new File(uploadRootPath + DateUtil.format(today, DatePattern.PURE_DATE_PATTERN) + File.separator + today.getTime() + fileType);
        if(!destinationFile.getParentFile().exists()){
            destinationFile.getParentFile().mkdirs();
        }
        try{
            file.transferTo(destinationFile);
        }catch(IOException e){
            return returnVo.failure(StrUtil.format("上传文件失败！原因：{}", e.getMessage()));
        }
        //返回目标文件地址
        return returnVo.success(destinationFile.getAbsolutePath());
    }
}