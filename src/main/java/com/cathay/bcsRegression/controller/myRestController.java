package com.cathay.bcsRegression.controller;

import com.cathay.bcsRegression.dao.SqlMapper;
import com.cathay.bcsRegression.service.impl.BatchServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

@RestController
@RequestMapping("/my")
//@Slf4j
public class myRestController {

	@Autowired
	public SqlMapper sqlMapper;




	@PostMapping("/test")
	public String test(){
		String sql = "SELECT * FROM cxlat.dtatq120 FETCH first 10 rows only";
//		String strResult = sqlMapper.selectOne(sql);
		List<Map<String, Object>> selectList = sqlMapper.selectList(sql);
		selectList.forEach(mapObject -> mapObject.forEach((key, value) -> System.out.println(new StringJoiner("--").add("key:").add(key).add(",value").add(String.valueOf(value)))));

		BatchServiceImpl.main(null);

		return "11";
	}
	
}
