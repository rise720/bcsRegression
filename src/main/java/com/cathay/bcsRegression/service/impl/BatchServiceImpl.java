package com.cathay.bcsRegression.service.impl;


import com.cathay.ac.o0.batch.ACO0_Z030;
import com.cathay.common.service.BatchControllerM;
import org.springframework.stereotype.Service;

/**
 * @author 0100065352
 */
@Service
public class BatchServiceImpl{
    private static String  configPropertiesPath = "C:\\localProjects\\bcsJar\\config\\init\\config.properties";
    private static String batchName = "JAACDO001_1";
    private static String className = "com.cathay.ac.o0.batch.ACO0_Z030";

    public static void doBatch(){
        try {
            System.out.println(System.getProperty("java.class.path"));
//            InputStream in = BatchServiceImpl.class.getClassLoader().getResourceAsStream("C:\\localProjects\\bcsJar\\batch2018\\lib\\aaa.property");
//            Properties properties=new Properties();
//            properties.load(in);
//            String str = properties.getProperty("ttt");
//            System.out.println(str);
            ACO0_Z030 batchProcess = new ACO0_Z030();
            batchProcess.execute(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // /home/cxlcs/runbatch.sh JAACDO001_1 com.cathay.ac.o0.batch.ACO0_Z030
//        doBatch();
        String[] controllerArgs = {configPropertiesPath,batchName,className};
        BatchControllerM.main(controllerArgs);
    }
}
