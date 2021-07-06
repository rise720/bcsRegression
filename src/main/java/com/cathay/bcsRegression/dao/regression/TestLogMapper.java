package com.cathay.bcsRegression.dao.regression;

import com.cathay.bcsRegression.entity.TestLog;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TestLogMapper {
    int deleteByPrimaryKey(Long id);

    int insert(TestLog record);

    TestLog selectByPrimaryKey(Long id);

    List<TestLog> selectList(TestLog record);

    int updateByPrimaryKeySelective(TestLog record);

    int updateByPrimaryKey(TestLog record);
}