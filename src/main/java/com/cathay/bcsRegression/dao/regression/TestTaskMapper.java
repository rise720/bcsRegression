package com.cathay.bcsRegression.dao.regression;

import com.cathay.bcsRegression.entity.TestTask;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TestTaskMapper {
    int deleteByPrimaryKey(Long id);

    int insert(TestTask record);

    TestTask selectByPrimaryKey(Long id);

    int update(TestTask record);

    int updateByPrimaryKey(TestTask record);

    /**
     * 检索下一批需要处理的测试任务
     */
    List<TestTask> selectNextProcess();

    List<TestTask> selectList(TestTask record);

}