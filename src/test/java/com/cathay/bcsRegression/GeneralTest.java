package com.cathay.bcsRegression;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GeneralTest {

    @Test
    public void checkHashmapDefaultSize(){
        Map<String, Object> checkMap = new HashMap<>();
        checkMap.put("1", "1");
        checkMap.put("2", "1");
        checkMap.put("3", "1");
        checkMap.put("4", "1");
        checkMap.put("5", "1");
        checkMap.put("6", "1");
        checkMap.put("7", "1");
        checkMap.put("8", "1");
        checkMap.put("9", "1");
        checkMap.put("10", "1");
        checkMap.put("11", "1");
        checkMap.put("12", "1");
        checkMap.put("13", "1");
        checkMap.put("14", "1");
        checkMap.put("15", "1");
        checkMap.put("16", "1");
        checkMap.put("17", "1");
        checkMap.put("18", "1");
        checkMap.put("19", "1");
        checkMap.put("111", "1");
        checkMap.put("112", "1");
        checkMap.put("113", "1");
        checkMap.put("114", "1");
        checkMap.put("115", "1");

        System.out.println(checkMap.size());


    }
}
