package com.cathay.bcsRegression.dao.regression;

import com.cathay.bcsRegression.entity.TestProcedure;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TestProcedureMapper {
    int deleteByPrimaryKey(Long id);

    int delete(TestProcedure record);

    int insert(TestProcedure record);

    TestProcedure selectByPrimaryKey(Long id);

    List<TestProcedure> selectList(TestProcedure record);

    int updateByPrimaryKeySelective(TestProcedure record);

    int updateByPrimaryKey(TestProcedure record);
}