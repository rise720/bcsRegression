package com.cathay.bcsRegression.constant;

/**
 * @author 0100065352
 */
public class CommonConstant {

    public static final String BCS_SQL_GET_TABLE_KEYS = "SELECT k.COLNAME AS KEY FROM SYSCAT.KEYCOLUSE k WHERE k.TABSCHEMA='{}' AND k.TABNAME='{}'";
    public static final String BCS_SQL_GET_TABLE_FIELDS = "SELECT c.COLNAME AS {}, c.TYPENAME AS {} FROM SYSCAT.COLUMNS c WHERE c.TABSCHEMA='{}' AND c.TABNAME='{}'";
    public static final String BCS_SQL_GET_TABLE_KEY_FIELDS = "SELECT c.COLNAME AS {}, c.TYPENAME AS {} FROM SYSCAT.COLUMNS c WHERE c.TABSCHEMA='{}' AND c.TABNAME='{}' AND c.KEYSEQ > 0";
    public static final String[] BCS_SQL_DIGIT_TYPE = {"DECIMAL", "INTEGER", "BIGINT", "SMALLINT", "DOUBLE"};

    public static final String BCS_SQL_TABLE_FIELDS1 = "FIELD";
    public static final String BCS_SQL_TABLE_FIELDS2 = "TYPE";

    public static final String Log_LEVEL_INFO = "INFO";
    public static final String Log_LEVEL_WARN = "WARN";
    public static final String Log_LEVEL_ERROR = "ERROR";

    public static final int TASK_FINISH_STATUS_SUCCESS = 2;
    public static final int TASK_FINISH_STATUS_FAILURE = 9;

    public static final int PROCEDURE_TYPE_STRSQL = 1;
    public static final int PROCEDURE_TYPE_FILE_INSERT = 2;
    public static final int PROCEDURE_TYPE_PROGRAM_EXECUTE = 3;
    public static final int PROCEDURE_TYPE_FILE_CHECK = 4;

    public static final String PROCEDURE_STRSQL_SELECT = "SELECT";
    public static final String PROCEDURE_STRSQL_INSERT = "INSERT";
    public static final String PROCEDURE_STRSQL_UPDATE = "UPDATE";
    public static final String PROCEDURE_STRSQL_DELETE = "DELETE";

    public static final String PROCEDURE_EXECUTE_BATCHNAME = "testBatch";


}
