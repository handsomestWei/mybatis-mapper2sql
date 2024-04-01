package com.wjy.mapper2sql;

import java.io.FileInputStream;
import java.util.List;
import java.util.Properties;

import com.alibaba.druid.DbType;
import com.alibaba.druid.util.StringUtils;
import com.wjy.mapper2sql.bo.JdbcConnProperties;
import com.wjy.mapper2sql.bo.MapperSqlInfo;
import com.wjy.mapper2sql.util.OutPutUtil;

/**
 * @author weijiayu
 * @date 2024/3/29 16:36
 */
public class Main {

    public static void main(String[] args) throws Exception {
        // print help
        if (args != null && args.length > 0) {
            String arg0 = args[0];
            if ("-h".equalsIgnoreCase(arg0) || "help".equalsIgnoreCase(arg0) || "--help".equalsIgnoreCase(arg0)) {
                printHelp();
                return;
            }
        }

        // get property
        String propertiesFilePath = System.getProperty("pFile");
        if (StringUtils.isEmpty(propertiesFilePath)) {
            printHelp();
            return;
        }
        Properties properties = new Properties();
        properties.load(new FileInputStream(propertiesFilePath));
        String workDir = properties.getProperty("workDir");
        String outPutDir = properties.getProperty("outPutDir");
        String dbTypeName = properties.getProperty("dbType");
        boolean mockFlag = Boolean.getBoolean(properties.getProperty("mock", "true"));
        if (isEmpty(workDir, outPutDir, dbTypeName)) {
            System.out.println("mapper2sql run fail!");
            System.out.println("property [workDir, outPutDir, dbType] value must be not null");
            return;
        }

        // check dbType
        DbType dbType = DbType.of(dbTypeName);
        if (dbType == null) {
            System.out.println("mapper2sql run fail!");
            System.out.println("property dbType value[" + dbTypeName + "]is not support in com.alibaba.druid.DbType");
            return;
        }

        // check dbConnect
        boolean connectFlag = false;
        String dbDriver = properties.getProperty("dbDriver");
        String jdbcUrl = properties.getProperty("jdbcUrl");
        String dbUser = properties.getProperty("dbUser");
        String dbPwd = properties.getProperty("dbPwd");
        if (isEmpty(dbDriver, jdbcUrl, dbUser, dbPwd)) {
            connectFlag = false;
        } else {
            connectFlag = true;
        }

        // run
        System.out.println("mapper2sql is running ...");
        if (connectFlag) {
            JdbcConnProperties jdbcConnProperties = new JdbcConnProperties(dbDriver, jdbcUrl, dbUser, dbPwd);
            List<MapperSqlInfo> mapperSqlInfos = SqlUtil.parseMapperAndRunTest(workDir, dbType, jdbcConnProperties);
            OutPutUtil.toFile(outPutDir, mapperSqlInfos);
        } else {
            List<MapperSqlInfo> mapperSqlInfos = SqlUtil.parseMapper(workDir, dbType, mockFlag);
            OutPutUtil.toFile(outPutDir, mapperSqlInfos);
        }
        System.out.println("mapper2sql run completed! you can open " + outPutDir);
    }

    private static boolean isEmpty(String... params) {
        for (String param : params) {
            if (StringUtils.isEmpty(param)) {
                return true;
            }
        }
        return false;
    }

    private static void printHelp() {
        System.out.println("===========================usage=================================");
        System.out.println("mapper2sql, extract sql from mybatis mapper xml");
        System.out.println("\nsupport========================================================");
        System.out.println("* folder scan and batch extract");
        System.out.println("* auto mock sql param");
        System.out.println("* sql test by jdbc connect and execute");
        System.out.println("\nrun========================================================");
        System.out.println("* eg1 [normal]: java -jar -DpFile=d:\\xxx.properties ./mapper2sql-1.0.0.jar");
        System.out.println(
            "* eg2 [run and test]: java -classpath ./Dm8JdbcDriver18-8.1.1.49.jar -jar -DpFile=d:\\xxx.properties ./mapper2sql-1.0.0.jar");
        System.out.println("\nproperties========================================================");
        System.out.println("* run eg1, the normal properties eg:\n" + "workDir=d:\\\\xxxProject\n"
            + "outPutDir=d:\\\\xxxProject-sql\n" + "dbType=postgresql\n" + "mock=true\n");
        System.out.println("* run eg2, the jdbc connect properties is must, eg:\n"
            + "dbDriver=dm.jdbc.driver.DmDriver\n" + "jdbcUrl=jdbc:dm://xx\n" + "dbUser=xxx\n" + "dbPwd=youKnow");
    }
}
