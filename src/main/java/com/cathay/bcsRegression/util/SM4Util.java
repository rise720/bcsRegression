package com.cathay.bcsRegression.util;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.util.Arrays;

import static com.cathay.bcsRegression.util.sm4Const.*;

//@Slf4j
public class SM4Util {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }



    /**
     * 生成ECB暗号
     * @explain ECB模式（电子密码本模式：Electronic codebook）
     * @param algorithmName
     *            算法名称
     * @param mode
     *            模式
     * @param key
     * @return
     * @throws Exception
     */
    private static Cipher generateEcbCipher(String algorithmName, int mode, byte[] key) throws Exception {
        Cipher cipher = Cipher.getInstance(algorithmName, BouncyCastleProvider.PROVIDER_NAME);
        Key sm4Key = new SecretKeySpec(key, ALGORITHM_NAME);
        cipher.init(mode, sm4Key);
        return cipher;
    }
    /**
     * 自动生成密钥
     * @explain
     * @return
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     */
    public static byte[] generateKey() throws Exception {
        return generateKey(DEFAULT_KEY_SIZE);
    }

    /**
     * @explain
     * @param keySize
     * @return
     * @throws Exception
     */
    public static byte[] generateKey(int keySize) throws Exception {
        KeyGenerator kg = KeyGenerator.getInstance(ALGORITHM_NAME, BouncyCastleProvider.PROVIDER_NAME);
        kg.init(keySize, new SecureRandom());
        return kg.generateKey().getEncoded();
    }


    /**
     * sm4加密
     * @explain 加密模式：ECB
     *          密文长度不固定，会随着被加密字符串长度的变化而变化
     * @param hexKey
     *            16进制密钥（忽略大小写）
     * @param paramStr
     *            待加密字符串
     * @return 返回16进制的加密字符串
     * @throws Exception
     */
    public static String encryptEcb(String hexKey, String paramStr) throws Exception {
        String cipherText = "";
        // 16进制字符串-->byte[]
        byte[] keyData = ByteUtils.fromHexString(hexKey);
        // String-->byte[]
        byte[] srcData = paramStr.getBytes(ENCODING);
        // 加密后的数组
        byte[] cipherArray = encrypt_Ecb_Padding(keyData, srcData);
        // byte[]-->hexString
        cipherText = ByteUtils.toHexString(cipherArray);
        return cipherText;
    }

    /**
     * 加密模式之Ecb
     * @explain
     * @param key
     * @param data
     * @return
     * @throws Exception
     */
    public static byte[] encrypt_Ecb_Padding(byte[] key, byte[] data) throws Exception {
        Cipher cipher = generateEcbCipher(ALGORITHM_NAME_ECB_PADDING, Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(data);
    }


    /**
     * sm4解密
     * @explain 解密模式：采用ECB
     * @param hexKey
     *            16进制密钥
     * @param cipherText
     *            16进制的加密字符串（忽略大小写）
     * @return 解密后的字符串
     * @throws Exception
     */
    public static String decryptEcb(String hexKey, String cipherText) throws Exception {
        // 用于接收解密后的字符串
        String decryptStr = "";
        // hexString-->byte[]
        byte[] keyData = ByteUtils.fromHexString(hexKey);
        // hexString-->byte[]
        byte[] cipherData = ByteUtils.fromHexString(cipherText);
        // 解密
        byte[] srcData = decrypt_Ecb_Padding(keyData, cipherData);
        // byte[]-->String
        decryptStr = new String(srcData, ENCODING);
        return decryptStr;
    }

    /**
     * 解密
     * @explain
     * @param key
     * @param cipherText
     * @return
     * @throws Exception
     */
    public static byte[] decrypt_Ecb_Padding(byte[] key, byte[] cipherText) throws Exception {
        Cipher cipher = generateEcbCipher(ALGORITHM_NAME_ECB_PADDING, Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(cipherText);
    }

    /**
     * 校验加密前后的字符串是否为同一数据
     * @explain
     * @param hexKey
     *            16进制密钥（忽略大小写）
     * @param cipherText
     *            16进制加密后的字符串
     * @param paramStr
     *            加密前的字符串
     * @return 是否为同一数据
     * @throws Exception
     */
    public static boolean verifyEcb(String hexKey, String cipherText, String paramStr) throws Exception {
        // 用于接收校验结果
        boolean flag = false;
        // hexString-->byte[]
        byte[] keyData = ByteUtils.fromHexString(hexKey);
        // 将16进制字符串转换成数组
        byte[] cipherData = ByteUtils.fromHexString(cipherText);
        // 解密
        byte[] decryptData = decrypt_Ecb_Padding(keyData, cipherData);
        // 将原字符串转换成byte[]
        byte[] srcData = paramStr.getBytes(ENCODING);
        // 判断2个数组是否一致
        flag = Arrays.equals(decryptData, srcData);
        return flag;
    }

    public static void main(String[] args) {
        try {

//            log.info("全渠道:" + encryptEcb(DEFAULT_KEY, "全渠道"));
//            log.info("341126197709218366:" + encryptEcb(DEFAULT_KEY, "341126197709218366"));
//            log.info("6216261000000000018:" + encryptEcb(DEFAULT_KEY, "6216261000000000018"));
////            log.info(encryptEcb(DEFAULT_KEY, ""));
////            log.info(encryptEcb(DEFAULT_KEY, "6216261000000000018"));
//            log.info("林丽：" + encryptEcb(DEFAULT_KEY, "林丽"));
//            log.info("110102198309283546：" + encryptEcb(DEFAULT_KEY, "110102198309283546"));
//
//            log.info("杨云：" + encryptEcb(DEFAULT_KEY, "杨云"));
//            log.info("110102199308283425：" + encryptEcb(DEFAULT_KEY, "110102199308283425"));
//
//            log.info("王兰：" + encryptEcb(DEFAULT_KEY, "王兰"));
//            log.info("15010219830213354X：" + encryptEcb(DEFAULT_KEY, "15010219830213354X"));
//
//			log.info("张帆：" + encryptEcb(DEFAULT_KEY, "张帆"));
//			log.info("120102199308283546：" + encryptEcb(DEFAULT_KEY, "120102199308283546"));
//
//            log.info("622301197807137737：" + encryptEcb(DEFAULT_KEY, "622301197807137737"));
//            log.info("120301197807137737：" + encryptEcb(DEFAULT_KEY, "120301197807137737"));
//
//            log.info("金文浩:" + encryptEcb(DEFAULT_KEY, "金文浩"));
//            log.info("310228198307200813:" + encryptEcb(DEFAULT_KEY, "310228198307200813"));
//            log.info("13761005046:" + encryptEcb(DEFAULT_KEY, "13761005046"));
//            log.info("6222031001028765886:" + encryptEcb(DEFAULT_KEY, "6222031001028765886"));
//
//            log.info("夏丽丽:" + encryptEcb(DEFAULT_KEY, "夏丽丽"));
//            log.info("420528199303281026:" + encryptEcb(DEFAULT_KEY, "420528199303281026"));
//            log.info("15750756779:" + encryptEcb(DEFAULT_KEY, "15750756779"));
//            log.info("6212264100038836684:" + encryptEcb(DEFAULT_KEY, "6212264100038836684"));
//
//            log.info("杨悦:" + encryptEcb(DEFAULT_KEY, "杨悦"));
//            log.info("120105197902194833:" + encryptEcb(DEFAULT_KEY, "120105197902194833"));
//            log.info("13332077168:" + encryptEcb(DEFAULT_KEY, "13332077168"));
//            log.info("6212260302033295510:" + encryptEcb(DEFAULT_KEY, "6212260302033295510"));



//			log.info(encryptEcb(DEFAULT_KEY, "622301197807137737"));
//			log.info(encryptEcb(DEFAULT_KEY, "120301197807137737"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
