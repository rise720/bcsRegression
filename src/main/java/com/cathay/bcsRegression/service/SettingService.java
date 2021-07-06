package com.cathay.bcsRegression.service;

import com.cathay.bcsRegression.vo.TestCaseVo;
import com.cathay.bcsRegression.vo.TestModuleVo;
import com.cathay.bcsRegression.vo.TestProcedureVo;
import com.cathay.bcsRegression.vo.TestTableVo;
import com.cathay.common.vo.ReturnVo;

import java.util.List;

/**
 * 设定用服务
 * @author 0100065352
 */
public interface SettingService {

    /**
     * 保存测试用表的信息
     */
    ReturnVo<String> saveTestTable(TestTableVo testTableVo);

    /**
     * 保存测试模块信息
     */
    ReturnVo<String> saveTestModule(TestModuleVo testModuleVo);

    /**
     * 保存测试用例信息
     */
    ReturnVo<String> saveTestCase(TestCaseVo testCaseVo);

    /**
     * 保存测试步骤信息
     */
    ReturnVo<String> saveTestProcedure(List<TestProcedureVo> testProcedureVoList);

}

