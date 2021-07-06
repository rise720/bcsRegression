package com.cathay.bcsRegression.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.text.csv.CsvData;
import cn.hutool.core.text.csv.CsvReader;
import cn.hutool.core.text.csv.CsvRow;
import cn.hutool.core.text.csv.CsvUtil;
import cn.hutool.core.util.StrUtil;
import com.cathay.bcsRegression.constant.CommonConstant;
import com.cathay.bcsRegression.dao.bcs.BcsSqlMapper;
import com.cathay.bcsRegression.dao.regression.TestCaseMapper;
import com.cathay.bcsRegression.dao.regression.TestLogMapper;
import com.cathay.bcsRegression.dao.regression.TestProcedureMapper;
import com.cathay.bcsRegression.dao.regression.TestTaskMapper;
import com.cathay.bcsRegression.entity.TestCase;
import com.cathay.bcsRegression.entity.TestLog;
import com.cathay.bcsRegression.entity.TestProcedure;
import com.cathay.bcsRegression.entity.TestTask;
import com.cathay.bcsRegression.service.RegressionTestService;
import com.cathay.bcsRegression.vo.FileDataVo;
import com.cathay.bcsRegression.vo.TestLogVo;
import com.cathay.bcsRegression.vo.TestTaskVo;
import com.cathay.common.vo.ReturnVo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.charset.Charset;
import java.util.*;

/**
 * @author 0100065352
 */
@Service
@Slf4j
public class RegressionTestServiceImpl implements RegressionTestService {

    @Autowired
    private TestTaskMapper testTaskMapper;
    @Autowired
    private TestCaseMapper testCaseMapper;
    @Autowired
    private TestLogMapper testLogMapper;
    @Autowired
    private TestProcedureMapper testProcedureMapper;
    @Autowired
    private BcsSqlMapper bcsSqlMapper;
    @Value("${task.upload.charset}")
    private String charSetName;
    @Value("${task.execute.config.path}")
    private String configPropertiesPath;


    @Override
    public ReturnVo<String> submitTest(List<Long> testCaseIdList) {
        ReturnVo<String> returnVo = new ReturnVo<String>().failure();

        //检查
        if(null == testCaseIdList || 0 >= testCaseIdList.size()){
            return returnVo.failure("未获取到测试任务信息！");
        }

        for(Long testCaseId : testCaseIdList){
            if(null == testCaseId || 0 >= testCaseId){
                return returnVo.failure("未获取到测试用例id！");
            }
            if(null == testCaseMapper.selectByPrimaryKey(testCaseId)){
                return returnVo.failure("未检索到测试用例，请确认测试用例是否已被删除！");
            }
        }

        //新增记录
        TestTask testTask = new TestTask();
        testTask.setBatchId(System.currentTimeMillis());
        testTask.setCurrentProcedureSeq(0);
        testTask.setStatus(0);
        for(Long testCaseId : testCaseIdList){
            testTask.setCaseId(testCaseId);
            if(0 == testTaskMapper.insert(testTask)){
                return returnVo.failure("新增测试任务记录失败！");
            }
        }

        return returnVo.success(String.valueOf(testTask.getBatchId()));
    }

    @Override
    public ReturnVo<List<TestTaskVo>> queryTest(String taskId) {
        ReturnVo<List<TestTaskVo>> returnVo = new ReturnVo<List<TestTaskVo>>().failure();

        try{
            TestTask testTask = new TestTask();
            testTask.setBatchId(Long.valueOf(taskId));
            List<TestTask> testTaskList = testTaskMapper.selectList(testTask);
            List<TestTaskVo> testTaskVoList = new ArrayList<>();
            testTaskList.forEach(task -> {
                TestTaskVo vo = new TestTaskVo();
                vo.setBatchId(task.getBatchId());
                vo.setCaseId(task.getCaseId());
                vo.setStatus(task.getStatus());
                testTaskVoList.add(vo);
            });
            return returnVo.success(testTaskVoList);
        }catch(Exception e){
            return returnVo.failure();
        }
    }

    @Override
    public ReturnVo<List<TestLog>> queryLog(String taskId) {
        ReturnVo<List<TestLog>> returnVo = new ReturnVo<List<TestLog>>().failure();

        try{
            TestLog testLog = new TestLog();
            testLog.setBatchId(Long.valueOf(taskId));
            List<TestLog> testLogList =  testLogMapper.selectList(testLog);
            return returnVo.success(testLogList);
        }catch(Exception e){
            return returnVo.failure();
        }
    }

    @Override
    public void doTest() {

        //任务可能有多个，每次只执行一个任务，获取该任务的所有测试用例id
        List<TestTask> testTaskList = testTaskMapper.selectNextProcess();
        if(null == testTaskList || 0 >= testTaskList.size()){
            return;
        }

        //遍历任务中的所有用例，执行测试
        for(TestTask testTask : testTaskList){
            TestTaskVo testTaskVo = new TestTaskVo();
            testTaskVo.setBatchId(testTask.getBatchId());
            testTaskVo.setCaseId(testTask.getCaseId());
            testTaskVo.setVersion(testTask.getVersion());

            saveLog(new TestLogVo(testTaskVo, CommonConstant.Log_LEVEL_INFO, "开始执行测试任务..."));

            //获取测试用例信息
            TestCase testCase = testCaseMapper.selectByPrimaryKey(testTask.getCaseId());
            if(null == testCase){
                log.error("未获取到测试用例信息");
                saveLog(new TestLogVo(testTaskVo, CommonConstant.Log_LEVEL_ERROR, "未获取到测试用例信息"));
                testTaskVo.setStatus(CommonConstant.TASK_FINISH_STATUS_FAILURE);
                finTestTask(testTaskVo);
                continue;
            }
            testTaskVo.setCaseName(testCase.getCaseName());

            //获取测试步骤
            TestProcedure testProcedure = new TestProcedure();
            testProcedure.setFromCaseId(testTaskVo.getCaseId());
            List<TestProcedure> testProcedureList = testProcedureMapper.selectList(testProcedure);
            if(null == testProcedureList || 0 >= testProcedureList.size()){
                log.error("未获取到测试步骤信息");
                saveLog(new TestLogVo(testTaskVo, CommonConstant.Log_LEVEL_ERROR, "未获取到测试步骤信息"));
                testTaskVo.setStatus(CommonConstant.TASK_FINISH_STATUS_FAILURE);
                finTestTask(testTaskVo);
                continue;
            }

            //循环执行测试步骤
            ReturnVo<String> procedureReturnVo = null;
            for(TestProcedure procedure : testProcedureList){
                try{
                    switch(procedure.getType()){
                        //执行sql
                        case CommonConstant.PROCEDURE_TYPE_STRSQL:
                            saveLog(new TestLogVo(testTaskVo, CommonConstant.Log_LEVEL_INFO, StrUtil.format("[步骤：{}]准备执行SQL脚本...", procedure.getProcedureSeq())));
                            procedureReturnVo = doStrSql(procedure);
                            break;
                        //执行文件导入
                        case CommonConstant.PROCEDURE_TYPE_FILE_INSERT:
                            saveLog(new TestLogVo(testTaskVo, CommonConstant.Log_LEVEL_INFO, StrUtil.format("[步骤：{}]准备执行文件数据导入...", procedure.getProcedureSeq())));
                            procedureReturnVo = doFileDataImport(procedure);
                            break;
                        //执行程序
                        case CommonConstant.PROCEDURE_TYPE_PROGRAM_EXECUTE:
                            saveLog(new TestLogVo(testTaskVo, CommonConstant.Log_LEVEL_INFO, StrUtil.format("[步骤：{}]准备执行程序...", procedure.getProcedureSeq())));
                            procedureReturnVo = doProgramExecute(procedure);
                            break;
                        //执行数据校对
                        case CommonConstant.PROCEDURE_TYPE_FILE_CHECK:
                            saveLog(new TestLogVo(testTaskVo, CommonConstant.Log_LEVEL_INFO, StrUtil.format("[步骤：{}]准备执行数据校对...", procedure.getProcedureSeq())));
                            procedureReturnVo = doFileCheck(testTaskVo, procedure);
                            break;
                        default:
                            procedureReturnVo = new ReturnVo<String>().failure("测试步骤类型不正确！");
                    }
                    Assert.notNull(procedureReturnVo);
                    saveLog(new TestLogVo(testTaskVo, procedureReturnVo.isSuccess() ? CommonConstant.Log_LEVEL_INFO : CommonConstant.Log_LEVEL_ERROR, procedureReturnVo.getData()));
                    //一个步骤出错，整个测试用例全部停止
                    if(procedureReturnVo.isFailure()){
                        break;
                    }
                }catch(Exception e){
                    saveLog(new TestLogVo(testTaskVo, CommonConstant.Log_LEVEL_ERROR, StrUtil.format("执行测试步骤异常！错误：{}", e.getMessage())));
                    if(null == procedureReturnVo) {
                        procedureReturnVo = new ReturnVo<>();
                    }
                    procedureReturnVo.failure("执行测试步骤异常！错误：{}");
                    break;
                }
            }

            //更新TASK状态
            testTaskVo.setStatus(procedureReturnVo.isSuccess() ? CommonConstant.TASK_FINISH_STATUS_SUCCESS : CommonConstant.TASK_FINISH_STATUS_FAILURE);
            finTestTask(testTaskVo);
         }
    }

    private ReturnVo<String> doProgramExecute(TestProcedure testProcedure) {
        ReturnVo<String> returnVo = new ReturnVo<String>().failure();

        //json格式： {"className":"com.cathay.XX.XXX","batchName":"aaa"}
        String className;
        String batchName;
        try{
            ObjectMapper mapper = new ObjectMapper();
            JsonNode tree = mapper.readTree(testProcedure.getDetail());
            className = tree.get("className").asText();
            if(tree.has("batchName")){
                batchName = tree.get("batchName").asText();
            }else{
                batchName = CommonConstant.PROCEDURE_EXECUTE_BATCHNAME;
            }
        }catch(Exception e){
            return returnVo.failure(StrUtil.format("[步骤：{}]执行程序失败，解析程序Json失败，{}", testProcedure.getProcedureSeq(), e.getMessage()));
        }
        //执行程序
        String[] args = {configPropertiesPath,batchName,className};
        try{
            ReturnVo<String> serviceReturnVo =  BcsBatchService.execute(args);
            if(serviceReturnVo.isFailure()){
                return returnVo.failure(StrUtil.format("[步骤：{}]执行程序失败，原因：{}", testProcedure.getProcedureSeq(), serviceReturnVo.getData()));
            }
        }catch( Exception e){
            return returnVo.failure(StrUtil.format("[步骤：{}]执行程序失败，原因：{}", testProcedure.getProcedureSeq(), e.getMessage()));
        }
        return returnVo.success(StrUtil.format("[步骤：{}]执行程序成功！", testProcedure.getProcedureSeq()));
    }


    private ReturnVo<String> doFileCheck(TestTaskVo testTaskVo, TestProcedure testProcedure) {
        ReturnVo<String> returnVo = new ReturnVo<String>().failure();

        //json格式： FileDataVo
        List<FileDataVo> fileList;
        try{
            ObjectMapper mapper = new ObjectMapper();
            fileList = mapper.readValue(testProcedure.getDetail(), new TypeReference<List<FileDataVo>>(){});
            if(null == fileList || 0 == fileList.size()){
                return returnVo.failure(StrUtil.format("[步骤：{}]执行数据校对失败，未获取到校对文件！", testProcedure.getProcedureSeq()));
            }
        }catch(Exception e){
            return returnVo.failure(StrUtil.format("[步骤：{}]执行数据校对失败，解析校对文件失败，{}", testProcedure.getProcedureSeq(), e.getMessage()));
        }

        //多文件循环读取，并生成数据校对
        CsvReader csvReader = CsvUtil.getReader();
        csvReader.setFieldSeparator(',');
        List<CsvRow> rows;
        StringJoiner incorrectFileJoiner = new StringJoiner(",");
        for(FileDataVo importVo : fileList){
            //schema及tableid转大写
            importVo.setSchema(importVo.getSchema().toUpperCase().trim());
            importVo.setTableId(importVo.getTableId().toUpperCase().trim());
            //获取表字段及类型
            List<Map<String, Object>> mapList;
            try{
                //获取表对应的key字段
                mapList = bcsSqlMapper.selectList(StrUtil.format(CommonConstant.BCS_SQL_GET_TABLE_KEY_FIELDS, CommonConstant.BCS_SQL_TABLE_FIELDS1, CommonConstant.BCS_SQL_TABLE_FIELDS2, importVo.getSchema(), importVo.getTableId()));
                Assert.notEmpty(mapList);
            }catch(Exception e){
                return returnVo.failure(StrUtil.format("[步骤：{}]执行数据校对失败，请确认表id是否正确，schema：{}，tableId：{}", testProcedure.getProcedureSeq(), importVo.getSchema(), importVo.getTableId()));
            }

            //读文件
            try{
                CsvData csvData = csvReader.read(new File(importVo.getFileUrl()), Charset.forName(charSetName));
                rows = csvData.getRows();
            }catch(Exception e){
                return returnVo.failure(StrUtil.format("[步骤：{}]执行数据校对失败，请确认csv文件格式正确，文件地址：{}", testProcedure.getProcedureSeq(), importVo.getFileUrl()));
            }

            //包括字段名，至少要有两行数据
            if(null == rows || 2 > rows.size()){
                return returnVo.failure(StrUtil.format("[步骤：{}]执行数据校对失败，文件无内容，文件地址：{}", testProcedure.getProcedureSeq(), importVo.getFileUrl()));
            }

            //逐行数据校对开始
            //字段名在第1行
            List<String> fieldList = rows.get(0).getRawList();

            //数据从第2行开始
            int incorrectRowNum = 0;
            for(int i = 1; i < rows.size(); i++){
                List<String> dataList = rows.get(i).getRawList();

                //1.获取每行数据的键值对
                Map<String, String> dataMap = new HashMap<>();
                for(int j = 0; j < dataList.size(); j++){
                    dataMap.put(fieldList.get(j).toUpperCase().trim(), dataList.get(j).toUpperCase().trim());
                }

                //2.根据key值，从db抽取比对数据
                Map<String, Object> fromDbMap;
                StringJoiner sj = new StringJoiner(" AND ", " WHERE ", "");
                try{
                    mapList.forEach(map -> map.forEach((key, type) -> {
                        String data = dataMap.get(key);
                        if(null != data){
                            if(Arrays.asList(CommonConstant.BCS_SQL_DIGIT_TYPE).contains(String.valueOf(type))){
                                sj.add(data);
                            }else{
                                sj.add("'" + data + "'");
                            }
                        }
                    }));
                    Assert.isTrue(sj.length() > 0);
                    String strSelectSql = StrUtil.format("SELECT * FROM {}.{} {}", importVo.getSchema(), importVo.getTableId(), sj.toString());
                    List<Map<String, Object>> selectMapList = bcsSqlMapper.selectList(strSelectSql);
                    //根据key可以检索到，且只能检索到一条数据
                    Assert.isTrue(1 == selectMapList.size());
                    fromDbMap = selectMapList.get(0);
                }catch(Exception e){
                    saveLog(new TestLogVo(testTaskVo, CommonConstant.Log_LEVEL_ERROR, StrUtil.format("[步骤：{}][表名:{}][行号：{}]数据校对不一致，无法根据key获取到正确的表数据", testProcedure.getProcedureSeq(), importVo.getTableId(), i + 1)));
                    incorrectRowNum ++;
                    continue;
                }

                //3. 所有字段通过字符串形式比较
                StringJoiner incorrectJoiner = new StringJoiner(",");
                try{
                    for(Map.Entry<String, String> entry : dataMap.entrySet()){
                        String fromDbData = String.valueOf(fromDbMap.get(entry.getKey()));
                        boolean isBlankFromDbData = isDbBlank(fromDbData);
                        boolean isBlankData = isDbBlank(entry.getValue());
                        //都为空，则校验通过
                        if(isBlankFromDbData && isBlankData){
                            continue;
                        }
                        //都不为空，但相同，则校验通过
                        if(!isBlankFromDbData && !isBlankData && fromDbData.trim().equals(entry.getValue().trim())){
                            continue;
                        }
                        //其他情况校验不通过,记录字段名，但不超过5个
                        if(incorrectJoiner.length() <= 5){
                            incorrectJoiner.add(entry.getKey());
                        }
                    }
                }catch(Exception e){
                    saveLog(new TestLogVo(testTaskVo, CommonConstant.Log_LEVEL_ERROR, StrUtil.format("[步骤：{}][表名:{}][行号：{}]数据校对失败，原因：{}", testProcedure.getProcedureSeq(), importVo.getTableId(), i + 1, e.getMessage())));
                    incorrectRowNum ++;
                    continue;
                }
                if(incorrectJoiner.length() > 0){
                    saveLog(new TestLogVo(testTaskVo, CommonConstant.Log_LEVEL_ERROR, StrUtil.format("[步骤：{}][表名:{}][行号：{}]数据校对未通过！包括字段：{}", testProcedure.getProcedureSeq(), importVo.getTableId(), i + 1, incorrectJoiner.toString())));
                    incorrectRowNum ++;
                }
            }
            if(incorrectRowNum > 0){
                incorrectFileJoiner.add(importVo.getTableId());
                saveLog(new TestLogVo(testTaskVo, CommonConstant.Log_LEVEL_ERROR, StrUtil.format("[步骤：{}][表名:{}]数据校对完毕，数据不一致！总记录数：{}，未通过记录数：{}", testProcedure.getProcedureSeq(), importVo.getTableId(), rows.size(), incorrectRowNum)));
            }else{
                saveLog(new TestLogVo(testTaskVo, CommonConstant.Log_LEVEL_INFO, StrUtil.format("[步骤：{}][表名:{}]数据校对完毕，数据一致！总记录数：{}", testProcedure.getProcedureSeq(), importVo.getTableId(), rows.size())));
            }
        }
        if(incorrectFileJoiner.length() > 0){
            return returnVo.failure(StrUtil.format("[步骤：{}]文件数据校对全部完毕，部分文件数据不一致！表名：{}", testProcedure.getProcedureSeq(), incorrectFileJoiner.toString()));
        }
        return returnVo.success(StrUtil.format("[步骤：{}]文件数据校对全部完毕，数据全部一致！", testProcedure.getProcedureSeq()));
    }

    private boolean isDbBlank(String val){
        return null == val || "".equals(val.trim()) || "NULL".equalsIgnoreCase(val.trim());
    }


    /**
     *执行文件数据导入
     */
    private ReturnVo<String> doFileDataImport(TestProcedure testProcedure) {
        ReturnVo<String> returnVo = new ReturnVo<String>().failure();

        //json格式： FileDataVo
        List<FileDataVo> fileList;
        try{
            ObjectMapper mapper = new ObjectMapper();
            fileList = mapper.readValue(testProcedure.getDetail(), new TypeReference<List<FileDataVo>>(){});
            if(null == fileList || 0 == fileList.size()){
                return returnVo.failure(StrUtil.format("[步骤：{}]执行文件数据导入失败，未获取到文件！", testProcedure.getProcedureSeq()));
            }
        }catch(Exception e){
            return returnVo.failure(StrUtil.format("[步骤：{}]执行文件数据导入失败，解析文件失败，{}", testProcedure.getProcedureSeq(), e.getMessage()));
        }

        //多文件循环读取，并生成SQL导入
        CsvReader csvReader = CsvUtil.getReader();
        csvReader.setFieldSeparator(',');
        List<CsvRow> rows;
        for(FileDataVo importVo : fileList){
            //获取表字段及类型
            List<Map<String, Object>> mapList;
            try{
                mapList = bcsSqlMapper.selectList(StrUtil.format(CommonConstant.BCS_SQL_GET_TABLE_FIELDS, CommonConstant.BCS_SQL_TABLE_FIELDS1, CommonConstant.BCS_SQL_TABLE_FIELDS2, importVo.getSchema().toUpperCase().trim(), importVo.getTableId().toUpperCase().trim()));
                Assert.notEmpty(mapList);
            }catch(Exception e){
                return returnVo.failure(StrUtil.format("[步骤：{}]执行文件数据导入失败，请确认表id是否正确，schema：{}，tableId：{}", testProcedure.getProcedureSeq(), importVo.getSchema(), importVo.getTableId()));
            }

            //读文件
            try{
                CsvData csvData = csvReader.read(new File(importVo.getFileUrl()), Charset.forName(charSetName));
                rows = csvData.getRows();
            }catch(Exception e){
                return returnVo.failure(StrUtil.format("[步骤：{}]执行文件数据导入失败，请确认csv文件格式正确，文件地址：{}", testProcedure.getProcedureSeq(), importVo.getFileUrl()));
            }

            //包括字段名，至少要有两行数据
            if(null == rows || 2 > rows.size()){
                return returnVo.failure(StrUtil.format("[步骤：{}]执行文件数据导入失败，文件无内容，文件地址：{}", testProcedure.getProcedureSeq(), importVo.getFileUrl()));
            }

            //拼接INSERT SQL逻辑开始
            //字段名在第1行
            List<String> fieldList = rows.get(0).getRawList();
            StringJoiner fieldJoiner = new StringJoiner(",", "(",")");
            fieldList.forEach(fieldJoiner::add);
            String strFieldSql = fieldJoiner.toString();

            //数据从第2行开始
            StringJoiner dataListJoiner = new StringJoiner(",");
            for(int i = 1; i < rows.size(); i++){
                List<String> dataList = rows.get(i).getRawList();
                //字段解析
                StringJoiner dataJoiner = new StringJoiner(",", "(",")");
                for(int j = 0; j < dataList.size(); j++){
                    String data = dataList.get(j);
                    //数据为空的字段直接拼接NULL
                    if(StrUtil.isBlank(data)){
                        dataJoiner.add("NULL");
                        continue;
                    }
                    //获取字段类型
                    String field = fieldList.get(j);
                    String type = null;
                    for(Map<String, Object> map : mapList){
                        if(field.trim().equals(map.get(CommonConstant.BCS_SQL_TABLE_FIELDS1))){
                            type = String.valueOf(map.get(CommonConstant.BCS_SQL_TABLE_FIELDS2));
                            break;
                        }
                    }
                    if(type == null){
                        return returnVo.failure(StrUtil.format("[步骤：{}]执行文件数据导入失败，表字段名不准确，字段名：{}，文件地址：{}", testProcedure.getProcedureSeq(), field, importVo.getFileUrl()));
                    }
                    //数字型不加单引号，其他类型需要
                    if(Arrays.asList(CommonConstant.BCS_SQL_DIGIT_TYPE).contains(type)){
                        dataJoiner.add(data);
                    }else{
                        dataJoiner.add("'" + data + "'");
                    }

                }
                dataListJoiner.add(dataJoiner.toString());
            }
            String strDataSql = dataListJoiner.toString();

            //拼接
            String strSql = StrUtil.format("INSERT INTO {}.{} {} VALUES {}", importVo.getSchema(), importVo.getTableId(), strFieldSql, strDataSql);

            //执行sql
            try {
                Assert.isTrue(bcsSqlMapper.insert(strSql) > 0);
            }catch(Exception e){
                return returnVo.failure(StrUtil.format("[步骤：{}]执行文件数据导入失败，SQL执行失败！文件地址：{}，错误：{}，SQL:{}", testProcedure.getProcedureSeq(), importVo.getFileUrl(), e.getMessage(), strSql));
            }
        }
        return returnVo.success(StrUtil.format("[步骤：{}]执行文件数据导入成功！", testProcedure.getProcedureSeq()));
    }

    /**
     * 执行sql脚本
     */
    private ReturnVo<String> doStrSql(TestProcedure testProcedure) {
        ReturnVo<String> returnVo = new ReturnVo<String>().failure();

        //执行SQL中，detail的json格式： [{"INSERT":"SELECT * XXXX"},{"UPDATE":"UPDATE XXX SET"}]
        Map<String, String> sqlMap;
        try{
            ObjectMapper mapper = new ObjectMapper();
            sqlMap = mapper.readValue(testProcedure.getDetail(), new TypeReference<Map<String, String>>(){});
            if(null == sqlMap || 0 == sqlMap.size()){
                return returnVo.failure(StrUtil.format("[步骤：{}]执行SQL脚本失败，未获取到SQL脚本！", testProcedure.getProcedureSeq()));
            }
        }catch(Exception e){
            return returnVo.failure(StrUtil.format("[步骤：{}]执行SQL脚本失败，解析SQL脚本失败，{}", testProcedure.getProcedureSeq(), e.getMessage()));
        }

        for(Map.Entry<String, String> entry : sqlMap.entrySet()) {
            try{
                switch (entry.getKey().toUpperCase()) {
                    case CommonConstant.PROCEDURE_STRSQL_SELECT:
                        bcsSqlMapper.selectList(entry.getValue());
                        break;
                    case CommonConstant.PROCEDURE_STRSQL_INSERT:
                        bcsSqlMapper.insert(entry.getValue());
                        break;
                    case CommonConstant.PROCEDURE_STRSQL_UPDATE:
                        bcsSqlMapper.update(entry.getValue());
                        break;
                    case CommonConstant.PROCEDURE_STRSQL_DELETE:
                        bcsSqlMapper.delete(entry.getValue());
                        break;
                    default:
                        return returnVo.failure(StrUtil.format("[步骤：{}]执行SQL脚本失败，操作类型不正确！", testProcedure.getProcedureSeq()));
                }
            } catch (Exception e) {
                return returnVo.failure(StrUtil.format("[步骤：{}]执行SQL脚本失败，脚本语句：{}， 错误内容: {}", testProcedure.getProcedureSeq(), entry.getValue(), e.getMessage()));
            }
        }

        return returnVo.success(StrUtil.format("[步骤：{}]执行SQL脚本成功！", testProcedure.getProcedureSeq()));
    }


    private void saveLog(TestLogVo testLogVo){
        TestLog testLog = new TestLog();
        testLog.setBatchId(testLogVo.getBatchId());
        testLog.setCaseId(testLogVo.getCaseId());
        testLog.setMsgLevel(testLogVo.getMsgLevel());
        testLog.setMessage(testLogVo.getMessage());
        try{
            testLogMapper.insert(testLog);
        }catch(Exception e){
            log.error("新增TEST_LOG表记录失败！错误：{0}", e);
        }
    }

    /**
     *结束当前用例
     */
    private void finTestTask(TestTaskVo testTaskVo){
        String msg;
        String msgLevel;
        if (testTaskVo.getStatus() == CommonConstant.TASK_FINISH_STATUS_SUCCESS) {
            msg = "测试任务处理完毕";
            msgLevel = CommonConstant.Log_LEVEL_INFO;
        } else {
            msg = "测试任务异常终止";
            msgLevel = CommonConstant.Log_LEVEL_ERROR;
        }
        TestTask testTask = new TestTask();
        testTask.setCaseId(testTaskVo.getCaseId());
        testTask.setBatchId(testTaskVo.getBatchId());
        testTask.setStatus(testTaskVo.getStatus());
        testTask.setVersion(testTaskVo.getVersion());
        try{
            testTaskMapper.update(testTask);
        }catch(Exception e){
            log.error("更新TEST_TASK表记录失败！错误：{0}", e);
        }
        saveLog(new TestLogVo(testTaskVo, msgLevel, msg));
    }
}
