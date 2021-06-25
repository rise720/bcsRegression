package com.cathay.bcsRegression.dao;


import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * 执行自定义的sql
 * @author 0100065352
 */
@Mapper
public interface SqlMapper {

    /**
     * 增
     * @param statement
     * @return
     */
    Integer insert(String statement);

    /**
     * 删
     * @param statement
     * @return
     */
    Integer delete(String statement);

    /**
     * 改
     * @param statement
     * @return
     */
    Integer update(String statement);

    /**
     * 查列表
     * @param statement
     * @return
     */
    List<Map<String, Object>> selectList(String statement);

    /**
     * 查唯一
     * @param statement
     * @return
     */
    String selectOne(String statement);
}
