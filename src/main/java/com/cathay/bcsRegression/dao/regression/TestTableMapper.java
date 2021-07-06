package com.cathay.bcsRegression.dao.regression;

import com.cathay.bcsRegression.entity.TestCase;
import com.cathay.bcsRegression.entity.TestTable;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TestTableMapper {
    int deleteByPrimaryKey(Long id);

    int insert(TestTable record);

    TestTable selectByTableId(String tableId);

    List<TestCase> selectList(TestTable record);

    int update(TestTable record);

    int updateByPrimaryKey(TestTable record);
}