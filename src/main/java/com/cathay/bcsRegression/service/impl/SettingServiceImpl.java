package com.cathay.bcsRegression.service.impl;

import cn.hutool.core.util.StrUtil;
import com.cathay.bcsRegression.constant.CommonConstant;
import com.cathay.bcsRegression.dao.bcs.BcsSqlMapper;
import com.cathay.bcsRegression.dao.regression.TestCaseMapper;
import com.cathay.bcsRegression.dao.regression.TestModuleMapper;
import com.cathay.bcsRegression.dao.regression.TestProcedureMapper;
import com.cathay.bcsRegression.dao.regression.TestTableMapper;
import com.cathay.bcsRegression.entity.TestCase;
import com.cathay.bcsRegression.entity.TestModule;
import com.cathay.bcsRegression.entity.TestProcedure;
import com.cathay.bcsRegression.entity.TestTable;
import com.cathay.bcsRegression.service.SettingService;
import com.cathay.bcsRegression.vo.TestCaseVo;
import com.cathay.bcsRegression.vo.TestModuleVo;
import com.cathay.bcsRegression.vo.TestProcedureVo;
import com.cathay.bcsRegression.vo.TestTableVo;
import com.cathay.common.vo.ReturnVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * @author 0100065352
 */
@Service
public class SettingServiceImpl implements SettingService {

    @Autowired
    private TestTableMapper testTableMapper;
    @Autowired
    private TestModuleMapper testModuleMapper;
    @Autowired
    private TestCaseMapper testCaseMapper;
    @Autowired
    private TestProcedureMapper testProcedureMapper;
    @Autowired
    private BcsSqlMapper bcsSqlMapper;


    @Override
    public ReturnVo<String> saveTestTable(TestTableVo testTableVo) {
        ReturnVo<String> returnVo = new ReturnVo<String>().failure();

        //检查
        if(null == testTableVo || StringUtils.isBlank(testTableVo.getTableId()) || StringUtils.isBlank(testTableVo.getTableSchema())){
            return returnVo.failure("表名或schema不能为空！");
        }

        //id及schema转大写
        testTableVo.setTableId(testTableVo.getTableId().trim().toUpperCase());
        testTableVo.setTableSchema(testTableVo.getTableSchema().trim().toUpperCase());

        //获取表对应的键值
        List<Map<String, Object>> mapList = bcsSqlMapper.selectList(StrUtil.format(CommonConstant.BCS_SQL_GET_TABLE_KEYS, testTableVo.getTableSchema(), testTableVo.getTableId()));
        if(null == mapList|| 0 == mapList.size()){
            return returnVo.failure("未获取到表的key，请确认表名是否正确！");
        }
        StringJoiner sj = new StringJoiner(",");
        mapList.forEach(map -> map.forEach((key, value) -> sj.add(String.valueOf(value))));
        String keys = sj.toString();

        //根据tableId检索，存在更新，不存在插入
        TestTable testTable = testTableMapper.selectByTableId(testTableVo.getTableId());
        boolean isNew = null == testTable;
        if(isNew){
            testTable = new TestTable();
        }
        testTable.setTableId(testTableVo.getTableId());
        testTable.setTableName(testTableVo.getTableName());
        testTable.setTableSchema(testTableVo.getTableSchema());
        testTable.setTableKeys(keys);

        //保存操作
        int row = isNew ? testTableMapper.insert(testTable) : testTableMapper.update(testTable);
        if(row == 0){
            return returnVo.failure("保存表信息失败！");
        }
        return returnVo.success("保存表信息成功！");
    }


    @Override
    public ReturnVo<String> saveTestModule(TestModuleVo testModuleVo) {
        ReturnVo<String> returnVo = new ReturnVo<String>().failure();

        //检查
        if(null == testModuleVo || StringUtils.isBlank(testModuleVo.getModuleName())){
            return returnVo.failure("模块名不能为空！");
        }

        //id为空或0 为需新增记录；id不为空但检索不到，仍为新增记录；能检索到为更新
        TestModule testModule = (null == testModuleVo.getId() || 0L >= testModuleVo.getId()) ? null : testModuleMapper.selectByPrimaryKey(testModuleVo.getId());
        boolean isNew = null == testModule;
        if(isNew){
            testModule = new TestModule();
        }
        testModule.setModuleName(testModuleVo.getModuleName());

        //保存操作
        int row = isNew ? testModuleMapper.insert(testModule) : testModuleMapper.update(testModule);
        if(row == 0){
            return returnVo.failure("保存模块信息失败！");
        }
        return returnVo.success("保存模块信息成功！");
    }


    @Override
    public ReturnVo<String> saveTestCase(TestCaseVo testCaseVo) {
        ReturnVo<String> returnVo = new ReturnVo<String>().failure();

        //检查
        if(null == testCaseVo || StringUtils.isBlank(testCaseVo.getCaseName()) || null == testCaseVo.getFromModuleId()){
            return returnVo.failure("用例名及所属模块id不能为空！");
        }
        if(null == testModuleMapper.selectByPrimaryKey(testCaseVo.getFromModuleId())){
            return returnVo.failure("所属模块id不存在！");
        }

        //id为空或0 为需新增记录；id不为空但检索不到，仍为新增记录；能检索到为更新
        TestCase testCase = (null == testCaseVo.getId() || 0L >= testCaseVo.getId()) ? null : testCaseMapper.selectByPrimaryKey(testCaseVo.getId());
        boolean isNew = null == testCase;
        if(isNew){
            testCase = new TestCase();
        }
        testCase.setCaseName(testCaseVo.getCaseName());
        testCase.setFromModuleId(testCaseVo.getFromModuleId());

        //保存操作
        int row = isNew ? testCaseMapper.insert(testCase) : testCaseMapper.update(testCase);
        if(row == 0){
            return returnVo.failure("保存用例信息失败！");
        }
        return returnVo.success("保存用例信息成功！");
    }


    @Override
    public ReturnVo<String> saveTestProcedure(List<TestProcedureVo> testProcedureVoList) {
        ReturnVo<String> returnVo = new ReturnVo<String>().failure();

        //检查
        if(null == testProcedureVoList || 0 == testProcedureVoList.size()){
            return returnVo.failure("未获取到测试步骤信息！");
        }
        Long caseId = null;
        for(TestProcedureVo testProcedureVo : testProcedureVoList){
            if(null == testProcedureVo.getProcedureSeq()){
                return returnVo.failure("测试步骤序号不能为空！");
            }
            if(StringUtils.isBlank(testProcedureVo.getDetail())){
                return returnVo.failure("测试步骤逻辑不能为空！");
            }
            if(null == testProcedureVo.getType()){
                return returnVo.failure("测试步骤处理类型不能为空！");
            }
            if(null == testProcedureVo.getFromCaseId()){
                return returnVo.failure("所属用例id不能为空！");
            }
            if(null != caseId  && caseId.longValue() != testProcedureVo.getFromCaseId().longValue()){
                return returnVo.failure("所属用例id不一致！");
            }
            if(null == caseId){
                caseId = testProcedureVo.getFromCaseId();
            }
        }
        if(null == testCaseMapper.selectByPrimaryKey(caseId)){
            return returnVo.failure("所属用例id不存在！");
        }

        //根据用例id先删除，后新增
        TestProcedure testProcedure = new TestProcedure();
        testProcedure.setFromCaseId(caseId);
        testProcedureMapper.delete(testProcedure);

        for(TestProcedureVo testProcedureVo : testProcedureVoList){
            testProcedure.setProcedureSeq(testProcedureVo.getProcedureSeq());
            testProcedure.setDetail(testProcedureVo.getDetail());
            testProcedure.setFromCaseId(testProcedureVo.getFromCaseId());
            testProcedure.setType(testProcedureVo.getType());
            if(0 == testProcedureMapper.insert(testProcedure)){
                return returnVo.failure("保存测试步骤信息失败！");
            }
        }

        return returnVo.success("保存测试步骤信息成功！");
    }
}