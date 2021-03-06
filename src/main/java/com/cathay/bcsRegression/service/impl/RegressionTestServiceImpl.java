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

        //??????
        if(null == testCaseIdList || 0 >= testCaseIdList.size()){
            return returnVo.failure("?????????????????????????????????");
        }

        for(Long testCaseId : testCaseIdList){
            if(null == testCaseId || 0 >= testCaseId){
                return returnVo.failure("????????????????????????id???");
            }
            if(null == testCaseMapper.selectByPrimaryKey(testCaseId)){
                return returnVo.failure("?????????????????????????????????????????????????????????????????????");
            }
        }

        //????????????
        TestTask testTask = new TestTask();
        testTask.setBatchId(System.currentTimeMillis());
        testTask.setCurrentProcedureSeq(0);
        testTask.setStatus(0);
        for(Long testCaseId : testCaseIdList){
            testTask.setCaseId(testCaseId);
            if(0 == testTaskMapper.insert(testTask)){
                return returnVo.failure("?????????????????????????????????");
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

        //??????????????????????????????????????????????????????????????????????????????????????????id
        List<TestTask> testTaskList = testTaskMapper.selectNextProcess();
        if(null == testTaskList || 0 >= testTaskList.size()){
            return;
        }

        //?????????????????????????????????????????????
        for(TestTask testTask : testTaskList){
            TestTaskVo testTaskVo = new TestTaskVo();
            testTaskVo.setBatchId(testTask.getBatchId());
            testTaskVo.setCaseId(testTask.getCaseId());
            testTaskVo.setVersion(testTask.getVersion());

            saveLog(new TestLogVo(testTaskVo, CommonConstant.Log_LEVEL_INFO, "????????????????????????..."));

            //????????????????????????
            TestCase testCase = testCaseMapper.selectByPrimaryKey(testTask.getCaseId());
            if(null == testCase){
                log.error("??????????????????????????????");
                saveLog(new TestLogVo(testTaskVo, CommonConstant.Log_LEVEL_ERROR, "??????????????????????????????"));
                testTaskVo.setStatus(CommonConstant.TASK_FINISH_STATUS_FAILURE);
                finTestTask(testTaskVo);
                continue;
            }
            testTaskVo.setCaseName(testCase.getCaseName());

            //??????????????????
            TestProcedure testProcedure = new TestProcedure();
            testProcedure.setFromCaseId(testTaskVo.getCaseId());
            List<TestProcedure> testProcedureList = testProcedureMapper.selectList(testProcedure);
            if(null == testProcedureList || 0 >= testProcedureList.size()){
                log.error("??????????????????????????????");
                saveLog(new TestLogVo(testTaskVo, CommonConstant.Log_LEVEL_ERROR, "??????????????????????????????"));
                testTaskVo.setStatus(CommonConstant.TASK_FINISH_STATUS_FAILURE);
                finTestTask(testTaskVo);
                continue;
            }

            //????????????????????????
            ReturnVo<String> procedureReturnVo = null;
            for(TestProcedure procedure : testProcedureList){
                try{
                    switch(procedure.getType()){
                        //??????sql
                        case CommonConstant.PROCEDURE_TYPE_STRSQL:
                            saveLog(new TestLogVo(testTaskVo, CommonConstant.Log_LEVEL_INFO, StrUtil.format("[?????????{}]????????????SQL??????...", procedure.getProcedureSeq())));
                            procedureReturnVo = doStrSql(procedure);
                            break;
                        //??????????????????
                        case CommonConstant.PROCEDURE_TYPE_FILE_INSERT:
                            saveLog(new TestLogVo(testTaskVo, CommonConstant.Log_LEVEL_INFO, StrUtil.format("[?????????{}]??????????????????????????????...", procedure.getProcedureSeq())));
                            procedureReturnVo = doFileDataImport(procedure);
                            break;
                        //????????????
                        case CommonConstant.PROCEDURE_TYPE_PROGRAM_EXECUTE:
                            saveLog(new TestLogVo(testTaskVo, CommonConstant.Log_LEVEL_INFO, StrUtil.format("[?????????{}]??????????????????...", procedure.getProcedureSeq())));
                            procedureReturnVo = doProgramExecute(procedure);
                            break;
                        //??????????????????
                        case CommonConstant.PROCEDURE_TYPE_FILE_CHECK:
                            saveLog(new TestLogVo(testTaskVo, CommonConstant.Log_LEVEL_INFO, StrUtil.format("[?????????{}]????????????????????????...", procedure.getProcedureSeq())));
                            procedureReturnVo = doFileCheck(testTaskVo, procedure);
                            break;
                        default:
                            procedureReturnVo = new ReturnVo<String>().failure("??????????????????????????????");
                    }
                    Assert.notNull(procedureReturnVo);
                    saveLog(new TestLogVo(testTaskVo, procedureReturnVo.isSuccess() ? CommonConstant.Log_LEVEL_INFO : CommonConstant.Log_LEVEL_ERROR, procedureReturnVo.getData()));
                    //???????????????????????????????????????????????????
                    if(procedureReturnVo.isFailure()){
                        break;
                    }
                }catch(Exception e){
                    saveLog(new TestLogVo(testTaskVo, CommonConstant.Log_LEVEL_ERROR, StrUtil.format("????????????????????????????????????{}", e.getMessage())));
                    if(null == procedureReturnVo) {
                        procedureReturnVo = new ReturnVo<>();
                    }
                    procedureReturnVo.failure("????????????????????????????????????{}");
                    break;
                }
            }

            //??????TASK??????
            testTaskVo.setStatus(procedureReturnVo.isSuccess() ? CommonConstant.TASK_FINISH_STATUS_SUCCESS : CommonConstant.TASK_FINISH_STATUS_FAILURE);
            finTestTask(testTaskVo);
         }
    }

    private ReturnVo<String> doProgramExecute(TestProcedure testProcedure) {
        ReturnVo<String> returnVo = new ReturnVo<String>().failure();

        //json????????? {"className":"com.cathay.XX.XXX","batchName":"aaa"}
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
            return returnVo.failure(StrUtil.format("[?????????{}]?????????????????????????????????Json?????????{}", testProcedure.getProcedureSeq(), e.getMessage()));
        }
        //????????????
        String[] args = {configPropertiesPath,batchName,className};
        try{
            ReturnVo<String> serviceReturnVo =  BcsBatchService.execute(args);
            if(serviceReturnVo.isFailure()){
                return returnVo.failure(StrUtil.format("[?????????{}]??????????????????????????????{}", testProcedure.getProcedureSeq(), serviceReturnVo.getData()));
            }
        }catch( Exception e){
            return returnVo.failure(StrUtil.format("[?????????{}]??????????????????????????????{}", testProcedure.getProcedureSeq(), e.getMessage()));
        }
        return returnVo.success(StrUtil.format("[?????????{}]?????????????????????", testProcedure.getProcedureSeq()));
    }


    private ReturnVo<String> doFileCheck(TestTaskVo testTaskVo, TestProcedure testProcedure) {
        ReturnVo<String> returnVo = new ReturnVo<String>().failure();

        //json????????? FileDataVo
        List<FileDataVo> fileList;
        try{
            ObjectMapper mapper = new ObjectMapper();
            fileList = mapper.readValue(testProcedure.getDetail(), new TypeReference<List<FileDataVo>>(){});
            if(null == fileList || 0 == fileList.size()){
                return returnVo.failure(StrUtil.format("[?????????{}]??????????????????????????????????????????????????????", testProcedure.getProcedureSeq()));
            }
        }catch(Exception e){
            return returnVo.failure(StrUtil.format("[?????????{}]??????????????????????????????????????????????????????{}", testProcedure.getProcedureSeq(), e.getMessage()));
        }

        //?????????????????????????????????????????????
        CsvReader csvReader = CsvUtil.getReader();
        csvReader.setFieldSeparator(',');
        List<CsvRow> rows;
        StringJoiner incorrectFileJoiner = new StringJoiner(",");
        for(FileDataVo importVo : fileList){
            //schema???tableid?????????
            importVo.setSchema(importVo.getSchema().toUpperCase().trim());
            importVo.setTableId(importVo.getTableId().toUpperCase().trim());
            //????????????????????????
            List<Map<String, Object>> mapList;
            try{
                //??????????????????key??????
                mapList = bcsSqlMapper.selectList(StrUtil.format(CommonConstant.BCS_SQL_GET_TABLE_KEY_FIELDS, CommonConstant.BCS_SQL_TABLE_FIELDS1, CommonConstant.BCS_SQL_TABLE_FIELDS2, importVo.getSchema(), importVo.getTableId()));
                Assert.notEmpty(mapList);
            }catch(Exception e){
                return returnVo.failure(StrUtil.format("[?????????{}]???????????????????????????????????????id???????????????schema???{}???tableId???{}", testProcedure.getProcedureSeq(), importVo.getSchema(), importVo.getTableId()));
            }

            //?????????
            try{
                CsvData csvData = csvReader.read(new File(importVo.getFileUrl()), Charset.forName(charSetName));
                rows = csvData.getRows();
            }catch(Exception e){
                return returnVo.failure(StrUtil.format("[?????????{}]????????????????????????????????????csv????????????????????????????????????{}", testProcedure.getProcedureSeq(), importVo.getFileUrl()));
            }

            //??????????????????????????????????????????
            if(null == rows || 2 > rows.size()){
                return returnVo.failure(StrUtil.format("[?????????{}]????????????????????????????????????????????????????????????{}", testProcedure.getProcedureSeq(), importVo.getFileUrl()));
            }

            //????????????????????????
            //???????????????1???
            List<String> fieldList = rows.get(0).getRawList();

            //????????????2?????????
            int incorrectRowNum = 0;
            for(int i = 1; i < rows.size(); i++){
                List<String> dataList = rows.get(i).getRawList();

                //1.??????????????????????????????
                Map<String, String> dataMap = new HashMap<>();
                for(int j = 0; j < dataList.size(); j++){
                    dataMap.put(fieldList.get(j).toUpperCase().trim(), dataList.get(j).toUpperCase().trim());
                }

                //2.??????key?????????db??????????????????
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
                    //??????key????????????????????????????????????????????????
                    Assert.isTrue(1 == selectMapList.size());
                    fromDbMap = selectMapList.get(0);
                }catch(Exception e){
                    saveLog(new TestLogVo(testTaskVo, CommonConstant.Log_LEVEL_ERROR, StrUtil.format("[?????????{}][??????:{}][?????????{}]????????????????????????????????????key???????????????????????????", testProcedure.getProcedureSeq(), importVo.getTableId(), i + 1)));
                    incorrectRowNum ++;
                    continue;
                }

                //3. ???????????????????????????????????????
                StringJoiner incorrectJoiner = new StringJoiner(",");
                try{
                    for(Map.Entry<String, String> entry : dataMap.entrySet()){
                        String fromDbData = String.valueOf(fromDbMap.get(entry.getKey()));
                        boolean isBlankFromDbData = isDbBlank(fromDbData);
                        boolean isBlankData = isDbBlank(entry.getValue());
                        //???????????????????????????
                        if(isBlankFromDbData && isBlankData){
                            continue;
                        }
                        //??????????????????????????????????????????
                        if(!isBlankFromDbData && !isBlankData && fromDbData.trim().equals(entry.getValue().trim())){
                            continue;
                        }
                        //???????????????????????????,??????????????????????????????5???
                        if(incorrectJoiner.length() <= 5){
                            incorrectJoiner.add(entry.getKey());
                        }
                    }
                }catch(Exception e){
                    saveLog(new TestLogVo(testTaskVo, CommonConstant.Log_LEVEL_ERROR, StrUtil.format("[?????????{}][??????:{}][?????????{}]??????????????????????????????{}", testProcedure.getProcedureSeq(), importVo.getTableId(), i + 1, e.getMessage())));
                    incorrectRowNum ++;
                    continue;
                }
                if(incorrectJoiner.length() > 0){
                    saveLog(new TestLogVo(testTaskVo, CommonConstant.Log_LEVEL_ERROR, StrUtil.format("[?????????{}][??????:{}][?????????{}]???????????????????????????????????????{}", testProcedure.getProcedureSeq(), importVo.getTableId(), i + 1, incorrectJoiner.toString())));
                    incorrectRowNum ++;
                }
            }
            if(incorrectRowNum > 0){
                incorrectFileJoiner.add(importVo.getTableId());
                saveLog(new TestLogVo(testTaskVo, CommonConstant.Log_LEVEL_ERROR, StrUtil.format("[?????????{}][??????:{}]??????????????????????????????????????????????????????{}????????????????????????{}", testProcedure.getProcedureSeq(), importVo.getTableId(), rows.size(), incorrectRowNum)));
            }else{
                saveLog(new TestLogVo(testTaskVo, CommonConstant.Log_LEVEL_INFO, StrUtil.format("[?????????{}][??????:{}]???????????????????????????????????????????????????{}", testProcedure.getProcedureSeq(), importVo.getTableId(), rows.size())));
            }
        }
        if(incorrectFileJoiner.length() > 0){
            return returnVo.failure(StrUtil.format("[?????????{}]????????????????????????????????????????????????????????????????????????{}", testProcedure.getProcedureSeq(), incorrectFileJoiner.toString()));
        }
        return returnVo.success(StrUtil.format("[?????????{}]??????????????????????????????????????????????????????", testProcedure.getProcedureSeq()));
    }

    private boolean isDbBlank(String val){
        return null == val || "".equals(val.trim()) || "NULL".equalsIgnoreCase(val.trim());
    }


    /**
     *????????????????????????
     */
    private ReturnVo<String> doFileDataImport(TestProcedure testProcedure) {
        ReturnVo<String> returnVo = new ReturnVo<String>().failure();

        //json????????? FileDataVo
        List<FileDataVo> fileList;
        try{
            ObjectMapper mapper = new ObjectMapper();
            fileList = mapper.readValue(testProcedure.getDetail(), new TypeReference<List<FileDataVo>>(){});
            if(null == fileList || 0 == fileList.size()){
                return returnVo.failure(StrUtil.format("[?????????{}]??????????????????????????????????????????????????????", testProcedure.getProcedureSeq()));
            }
        }catch(Exception e){
            return returnVo.failure(StrUtil.format("[?????????{}]??????????????????????????????????????????????????????{}", testProcedure.getProcedureSeq(), e.getMessage()));
        }

        //?????????????????????????????????SQL??????
        CsvReader csvReader = CsvUtil.getReader();
        csvReader.setFieldSeparator(',');
        List<CsvRow> rows;
        for(FileDataVo importVo : fileList){
            //????????????????????????
            List<Map<String, Object>> mapList;
            try{
                mapList = bcsSqlMapper.selectList(StrUtil.format(CommonConstant.BCS_SQL_GET_TABLE_FIELDS, CommonConstant.BCS_SQL_TABLE_FIELDS1, CommonConstant.BCS_SQL_TABLE_FIELDS2, importVo.getSchema().toUpperCase().trim(), importVo.getTableId().toUpperCase().trim()));
                Assert.notEmpty(mapList);
            }catch(Exception e){
                return returnVo.failure(StrUtil.format("[?????????{}]?????????????????????????????????????????????id???????????????schema???{}???tableId???{}", testProcedure.getProcedureSeq(), importVo.getSchema(), importVo.getTableId()));
            }

            //?????????
            try{
                CsvData csvData = csvReader.read(new File(importVo.getFileUrl()), Charset.forName(charSetName));
                rows = csvData.getRows();
            }catch(Exception e){
                return returnVo.failure(StrUtil.format("[?????????{}]??????????????????????????????????????????csv????????????????????????????????????{}", testProcedure.getProcedureSeq(), importVo.getFileUrl()));
            }

            //??????????????????????????????????????????
            if(null == rows || 2 > rows.size()){
                return returnVo.failure(StrUtil.format("[?????????{}]??????????????????????????????????????????????????????????????????{}", testProcedure.getProcedureSeq(), importVo.getFileUrl()));
            }

            //??????INSERT SQL????????????
            //???????????????1???
            List<String> fieldList = rows.get(0).getRawList();
            StringJoiner fieldJoiner = new StringJoiner(",", "(",")");
            fieldList.forEach(fieldJoiner::add);
            String strFieldSql = fieldJoiner.toString();

            //????????????2?????????
            StringJoiner dataListJoiner = new StringJoiner(",");
            for(int i = 1; i < rows.size(); i++){
                List<String> dataList = rows.get(i).getRawList();
                //????????????
                StringJoiner dataJoiner = new StringJoiner(",", "(",")");
                for(int j = 0; j < dataList.size(); j++){
                    String data = dataList.get(j);
                    //?????????????????????????????????NULL
                    if(StrUtil.isBlank(data)){
                        dataJoiner.add("NULL");
                        continue;
                    }
                    //??????????????????
                    String field = fieldList.get(j);
                    String type = null;
                    for(Map<String, Object> map : mapList){
                        if(field.trim().equals(map.get(CommonConstant.BCS_SQL_TABLE_FIELDS1))){
                            type = String.valueOf(map.get(CommonConstant.BCS_SQL_TABLE_FIELDS2));
                            break;
                        }
                    }
                    if(type == null){
                        return returnVo.failure(StrUtil.format("[?????????{}]?????????????????????????????????????????????????????????????????????{}??????????????????{}", testProcedure.getProcedureSeq(), field, importVo.getFileUrl()));
                    }
                    //?????????????????????????????????????????????
                    if(Arrays.asList(CommonConstant.BCS_SQL_DIGIT_TYPE).contains(type)){
                        dataJoiner.add(data);
                    }else{
                        dataJoiner.add("'" + data + "'");
                    }

                }
                dataListJoiner.add(dataJoiner.toString());
            }
            String strDataSql = dataListJoiner.toString();

            //??????
            String strSql = StrUtil.format("INSERT INTO {}.{} {} VALUES {}", importVo.getSchema(), importVo.getTableId(), strFieldSql, strDataSql);

            //??????sql
            try {
                Assert.isTrue(bcsSqlMapper.insert(strSql) > 0);
            }catch(Exception e){
                return returnVo.failure(StrUtil.format("[?????????{}]?????????????????????????????????SQL??????????????????????????????{}????????????{}???SQL:{}", testProcedure.getProcedureSeq(), importVo.getFileUrl(), e.getMessage(), strSql));
            }
        }
        return returnVo.success(StrUtil.format("[?????????{}]?????????????????????????????????", testProcedure.getProcedureSeq()));
    }

    /**
     * ??????sql??????
     */
    private ReturnVo<String> doStrSql(TestProcedure testProcedure) {
        ReturnVo<String> returnVo = new ReturnVo<String>().failure();

        //??????SQL??????detail???json????????? [{"INSERT":"SELECT * XXXX"},{"UPDATE":"UPDATE XXX SET"}]
        Map<String, String> sqlMap;
        try{
            ObjectMapper mapper = new ObjectMapper();
            sqlMap = mapper.readValue(testProcedure.getDetail(), new TypeReference<Map<String, String>>(){});
            if(null == sqlMap || 0 == sqlMap.size()){
                return returnVo.failure(StrUtil.format("[?????????{}]??????SQL???????????????????????????SQL?????????", testProcedure.getProcedureSeq()));
            }
        }catch(Exception e){
            return returnVo.failure(StrUtil.format("[?????????{}]??????SQL?????????????????????SQL???????????????{}", testProcedure.getProcedureSeq(), e.getMessage()));
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
                        return returnVo.failure(StrUtil.format("[?????????{}]??????SQL???????????????????????????????????????", testProcedure.getProcedureSeq()));
                }
            } catch (Exception e) {
                return returnVo.failure(StrUtil.format("[?????????{}]??????SQL??????????????????????????????{}??? ????????????: {}", testProcedure.getProcedureSeq(), entry.getValue(), e.getMessage()));
            }
        }

        return returnVo.success(StrUtil.format("[?????????{}]??????SQL???????????????", testProcedure.getProcedureSeq()));
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
            log.error("??????TEST_LOG???????????????????????????{0}", e);
        }
    }

    /**
     *??????????????????
     */
    private void finTestTask(TestTaskVo testTaskVo){
        String msg;
        String msgLevel;
        if (testTaskVo.getStatus() == CommonConstant.TASK_FINISH_STATUS_SUCCESS) {
            msg = "????????????????????????";
            msgLevel = CommonConstant.Log_LEVEL_INFO;
        } else {
            msg = "????????????????????????";
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
            log.error("??????TEST_TASK???????????????????????????{0}", e);
        }
        saveLog(new TestLogVo(testTaskVo, msgLevel, msg));
    }
}
