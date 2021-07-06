package com.cathay.bcsRegression.dao.regression;

import com.cathay.bcsRegression.entity.TestModule;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TestModuleMapper {
    int deleteByPrimaryKey(Long id);

    int insert(TestModule record);

    TestModule selectByPrimaryKey(Long id);

    int update(TestModule record);
}