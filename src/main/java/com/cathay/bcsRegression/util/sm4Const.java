package com.cathay.bcsRegression.util;

public class sm4Const {

    public static final String ENCODING = "UTF-8";
    public static final String ALGORITHM_NAME = "SM4";
    // 加密算法/分组加密模式/分组填充方式
    // PKCS5Padding-以8个字节为一组进行分组加密
    // 定义分组加密模式使用：PKCS5Padding
    public static final String ALGORITHM_NAME_ECB_PADDING = "SM4/ECB/PKCS5Padding";
    // 128-32位16进制；256-64位16进制
    public static final int DEFAULT_KEY_SIZE = 128;


    // 128-32位16进制；256-64位16进制
    public static final String DEFAULT_KEY = "76C63280C2806ED1F47B859DE501215B";
}
