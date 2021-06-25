package com.cathay.bcsRegression.util;


import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;

@RunWith(SpringRunner.class)
@SpringBootTest
class SM4UtilTest {
	@SpyBean
	private ArrayList<String> b;
//	@Mock
//	private List<String> c;


//	void test() throws Exception {
//		byte[] bytes = SM4Util.generateKey();
//		System.out.println(Base64.getEncoder().encodeToString(bytes));
//		List<String> list = Mockito.mock(ArrayList.class);
//		list.add("1");
//		Mockito.verify(list).add("1");
//		Mockito.when(list.get(1)).thenReturn("3");
//		System.out.println(list.get(1));
//		Mockito.when(list.get(Mockito.anyInt())).thenReturn("hello");
//		System.out.println(list.get(10000));
//		
//	}
	
	@Test
	void test2() {
		Mockito.doReturn("222").when(b).get(Mockito.anyInt());
		Mockito.doReturn(true).when(b).add("2");
		b.add("2");
//		Mockito.when(b.get(Mockito.anyInt())).thenReturn("11111111");
		System.out.println(b.get(999));
	}

}
