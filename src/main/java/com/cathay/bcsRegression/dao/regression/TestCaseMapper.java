package com.cathay.bcsRegression.dao.regression;

import com.cathay.bcsRegression.entity.TestCase;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TestCaseMapper {
    int deleteByPrimaryKey(Long id);

    int insert(TestCase record);

    TestCase selectByPrimaryKey(Long id);

    int update(TestCase record);

    int updateByPrimaryKey(TestCase record);
}