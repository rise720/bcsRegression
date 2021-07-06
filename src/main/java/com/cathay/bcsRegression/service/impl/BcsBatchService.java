package com.cathay.bcsRegression.service.impl;


import com.cathay.common.trx.BatchBean;
import com.cathay.common.util.DATE;
import com.cathay.common.util.DBService;
import com.cathay.common.vo.ReturnVo;
import com.igsapp.common.trx.ServiceException;
import com.igsapp.db.DataSet;
import com.igsapp.ext.monitor.LogProxy;
import com.igsapp.wibc.dataobj.SystemContext;
import com.igsapp.wibc.service.ServiceFactoryBase;
import org.apache.log4j.Category;

import java.io.File;
import java.io.FileInputStream;
import java.util.Date;
import java.util.Properties;

public class BcsBatchService {
    static Category Log;
    private static Properties systemProp;
    private static final String defaultConfigFile = "C:/usr/cxlcs_std/config/init/config.properties";
    private static final String defaultConfigFileUnix = "/usr/cxlcs_std/config/init/config.properties";
    public static final int CODE_BATCH_ENV_PARAMETER = -90;
    public static final int CODE_EBAF_SERVICE = -91;
    public static final int CODE_REFLECTION = -92;
    public static final int CODE_RUNTIME_EXCEPTION = -93;
    public static final int CODE_ERROR = -94;
    public static final int CODE_EXCEPTION = -99;
    private static String beanName;
    private static String taskName;
    private static final String strSQL = "com.cathay.common.service.BcsBatchService";

    static {
        Class var10000 = null;
        if (var10000 == null) {
            try {
                var10000 = Class.forName("com.cathay.common.service.BatchController");
            } catch (ClassNotFoundException var0) {
                throw new NoClassDefFoundError(var0.getMessage());
            }

        }

        Log = Category.getInstance(var10000.getName());
        systemProp = new Properties();
    }

    public BcsBatchService() {
    }

    public static ReturnVo<String> initEnv(String file) throws ServiceException {
        ReturnVo<String> returnVo = new ReturnVo<String>().failure();
        if (file == null) {
            file = File.separatorChar == '/' ? "C:/usr/cxlcs_std/config/init/config.properties" : "/usr/cxlcs_std/config/init/config.properties";
        }

        String tmp = (new File((new File(file)).getParent())).getParent();
        String configFile = tmp + File.separator + "init" + File.separator + "config.properties";
        Log.debug("load init properties:" + configFile);

        try {
            Properties p = new Properties();
            p.load(new FileInputStream(configFile));
            systemProp.putAll(p);
        } catch (Exception var4) {
            Log.error(var4);
            return returnVo.failure(var4.getMessage());
        }

        SystemContext sysContext = SystemContext.getInstance();
        sysContext.setProperties(systemProp);
        ServiceFactoryBase.getInstance();
        return returnVo.success();
    }

    public static void initEnv() throws ServiceException {
        initEnv((String)null);
    }

    public static ReturnVo<String> execute(String[] args) {
        ReturnVo<String> returnVo = new ReturnVo<String>().failure();

        if (args == null || args.length < 3) {
            return returnVo.failure("参数个数错误");
        }

        for(int i = 0; i < args.length; ++i) {
            Log.debug("param" + i + ":" + args[i]);
        }

        LogProxy.setEnable(false);
        System.setErr(System.out);

        try {
            if (args[0].length() < 5 || !"properties".equalsIgnoreCase(args[0].substring(args[0].length() - "properties".length()))) {
                return returnVo.failure("参数0必须为设定档config.properties");
            }

            if (args[1] != null) {
                taskName = args[1];
            }

            if (!args[2].startsWith("com") && !args[2].startsWith("test")) {
                return returnVo.failure("参数2(full classname)值为null或package不是com.xxx.xxx");
            } else {
                beanName = args[2];
            }

            ReturnVo<String> initEnvReturn = initEnv(args[0]);
            if(initEnvReturn.isFailure()){
                return returnVo.failure(initEnvReturn.getData());
            }
            ReturnVo<String> executeReturn = executeBean(args);
            if(executeReturn.isFailure()){
                return returnVo.failure(executeReturn.getData());
            }
        } catch (ServiceException var2) {
            return returnVo.failure(var2.getMessage());
        }

        return returnVo.success("执行程序成功！");

    }

    public static ReturnVo<String> executeBean(String[] args) {
        ReturnVo<String> returnVo = new ReturnVo<String>().failure();
        Log.warn("<<< run batch beanName [" + beanName + "] start " + new Date() + ">>>");
        long lngBegTime = 0L;
        long lngUseTime = 0L;
        String strBegTime = "";
        String strEndTime = "";
        BatchBean tx = null;

        try {
            tx = (BatchBean)Class.forName(beanName).newInstance();
            BatchBean.setTaskName(taskName);
        } catch (Exception var11) {
            Log.error(var11);
            return returnVo.failure(var11.getMessage());
        }

        try {
            String[] beanArgs = new String[args.length - 3];
            System.arraycopy(args, 3, beanArgs, 0, beanArgs.length);
            lngBegTime = System.currentTimeMillis();
            strBegTime = DATE.getDBTimeStamp();
            tx.execute(beanArgs);
            lngUseTime = System.currentTimeMillis() - lngBegTime;
            strEndTime = DATE.getDBTimeStamp();
            String tmpPara = "";

            for(int i = 0; i < beanArgs.length; ++i) {
                tmpPara = tmpPara + beanArgs[i] + " ";
            }

//            logTime(args[1], args[2], strBegTime, strEndTime, lngUseTime, tx.getExitCode(), tmpPara.trim());
            return returnVo.success();
//            exit(tx.getExitCode());
        } catch (RuntimeException var12) {
            return returnVo.failure(var12.getMessage());
        } catch (Error var13) {
            Log.error(var13);
            return returnVo.failure(var13.getMessage());
        } catch (Exception var14) {
            Log.error(var14);
            return returnVo.failure(var14.getMessage());
        }

    }

//    private static void logTime(String strJCLName, String strCLSName, String strBegTime, String strEndTime, long lngUseTime, int intExitCode, String para) {
//        String strPeriod = "";
//        String strBusTP = "";
//        String strJobName = "";
//        DataSet ds = DBService.createDataSet();
//
//        try {
//            strJobName = strCLSName.substring(strCLSName.lastIndexOf(".") + 1);
//            strPeriod = strJCLName.length() >= 6 ? strJCLName.substring(4, 5) : "";
//            strBusTP = strJCLName.length() >= 5 ? strJCLName.substring(2, 4) : "";
//            ds.setField("JCL_NAME", strJCLName);
//            ds.setField("CLS_NAME", strCLSName);
//            ds.setField("BEG_TIME", strBegTime);
//            ds.setField("END_TIME", strEndTime);
//            ds.setField("JOB_NAME", strJobName);
//            ds.setField("USE_TIME", String.valueOf(lngUseTime));
//            ds.setField("PERIOD", strPeriod);
//            ds.setField("BUS_TP", strBusTP);
//            ds.setField("EXIT_CODE", String.valueOf(intExitCode));
//            ds.setField("PARAMETERS", para);
//            ds.update("com.cathay.common.service.BcsBatchService");
//        } catch (Exception var13) {
//            var13.printStackTrace();
//        }
//
//    }

    public static void exit(int code) {
        LogProxy.removeFromMonitorNow();
        Log.warn("<<< run batch beanName [" + beanName + "]   end. " + new Date() + ">>>");
        System.exit(code);
    }

    public static String getErrMsg(int exitcode) {
        String ret = "";
        switch(exitcode) {
            case -99:
                ret = "other Exception";
            case -98:
            case -97:
            case -96:
            case -95:
            default:
                break;
            case -94:
                ret = "Java Error";
                break;
            case -93:
                ret = "Java Runtime Exception";
                break;
            case -92:
                ret = "reflection Exception";
                break;
            case -91:
                ret = "ebaf环境初始错误";
                break;
            case -90:
                ret = "批次程式环境参数错误";
        }

        return ret;
    }
}
