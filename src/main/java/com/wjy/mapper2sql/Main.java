package com.wjy.mapper2sql;

import com.alibaba.druid.DbType;
import com.wjy.mapper2sql.bo.JdbcConnProperties;
import com.wjy.mapper2sql.bo.MapperSqlInfo;
import com.wjy.mapper2sql.util.OutPutUtil;
import org.apache.commons.lang3.StringUtils;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.FileInputStream;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;

/**
 * @author weijiayu
 * @date 2024/3/29 16:36
 */
@Command(name = "mapper2sql", mixinStandardHelpOptions = true, version = "1.2.0", description = "Extract SQL from MyBatis mapper XML files with automatic parameter mocking", headerHeading = "@|bold,blue Usage Examples|@:%n%n", synopsisHeading = "%n", descriptionHeading = "%n@|bold,blue Description|@:%n%n", optionListHeading = "%n@|bold,blue Options|@:%n", parameterListHeading = "%n@|bold,blue Parameters|@:%n", commandListHeading = "%n@|bold,blue Commands|@:%n", header = {
        "mapper2sql - Extract SQL from MyBatis mapper XML files",
        "",
        "Author: https://github.com/handsomestWei/",
        "",
        "Features:",
        "  * Folder scan and batch extract",
        "  * Auto mock SQL parameters",
        "  * SQL test by JDBC connect and execute",
        ""
}, footer = {
        "",
        "@|bold,blue Examples|@:",
        "",
        "  # Basic usage with properties file:",
        "  mapper2sql --properties config.properties",
        "",
        "  # Direct parameters:",
        "  mapper2sql --work-dir /path/to/project --output-dir /path/to/output --db-type postgresql",
        "",
        "  # With database connection for testing:",
        "  mapper2sql --work-dir /path/to/project --output-dir /path/to/output --db-type postgresql ",
        "    --db-driver org.postgresql.Driver --jdbc-url jdbc:postgresql://localhost:5432/testdb ",
        "    --db-user postgres --db-password password --test",
        ""
})
public class Main implements Callable<Integer> {

    @Option(names = { "-p",
            "--properties" }, description = "Properties file path containing configuration", paramLabel = "<file>")
    private String propertiesFile;

    @Option(names = { "-w",
            "--work-dir" }, description = "Working directory to scan for mapper XML files", paramLabel = "<directory>")
    private String workDir;

    @Option(names = { "-o",
            "--output-dir" }, description = "Output directory for generated SQL files", paramLabel = "<directory>")
    private String outputDir;

    @Option(names = { "-t",
            "--db-type" }, description = "Database type (postgresql, mysql, oracle, etc.)", paramLabel = "<type>")
    private String dbType;

    @Option(names = { "-m", "--mock" }, description = "Enable parameter mocking (default: true)", defaultValue = "true")
    private boolean mock;

    @Option(names = { "--db-driver" }, description = "JDBC driver class name", paramLabel = "<driver>")
    private String dbDriver;

    @Option(names = { "--jdbc-url" }, description = "JDBC connection URL", paramLabel = "<url>")
    private String jdbcUrl;

    @Option(names = { "--db-user" }, description = "Database username", paramLabel = "<username>")
    private String dbUser;

    @Option(names = { "--db-password" }, description = "Database password", paramLabel = "<password>")
    private String dbPassword;

    @Option(names = { "--test" }, description = "Execute SQL tests after extraction")
    private boolean test;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {
        try {
            // Load properties if specified
            if (StringUtils.isNotEmpty(propertiesFile)) {
                loadPropertiesFromFile();
            }

            // Validate required parameters
            if (!validateParameters()) {
                return 1;
            }

            // Parse database type
            DbType dbTypeEnum = parseDbType();
            if (dbTypeEnum == null) {
                return 1;
            }

            // Execute the main logic
            executeMapper2Sql(dbTypeEnum);

            return 0;
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            return 1;
        }
    }

    private void loadPropertiesFromFile() throws Exception {
        Properties properties = new Properties();
        properties.load(new FileInputStream(propertiesFile));

        // Load parameters from properties file
        workDir = properties.getProperty("workDir", workDir);
        outputDir = properties.getProperty("outPutDir", outputDir);
        dbType = properties.getProperty("dbType", dbType);
        mock = Boolean.parseBoolean(properties.getProperty("mock", String.valueOf(mock)));
        test = Boolean.parseBoolean(properties.getProperty("test", String.valueOf(test)));
        dbDriver = properties.getProperty("dbDriver", dbDriver);
        jdbcUrl = properties.getProperty("jdbcUrl", jdbcUrl);
        dbUser = properties.getProperty("dbUser", dbUser);
        dbPassword = properties.getProperty("dbPwd", dbPassword);
    }

    private boolean validateParameters() {
        if (StringUtils.isEmpty(workDir)) {
            System.err.println("Error: work-dir is required");
            return false;
        }
        if (StringUtils.isEmpty(outputDir)) {
            System.err.println("Error: output-dir is required");
            return false;
        }
        if (StringUtils.isEmpty(dbType)) {
            System.err.println("Error: db-type is required");
            return false;
        }
        return true;
    }

    private DbType parseDbType() {
        DbType dbTypeEnum = DbType.of(dbType);
        if (dbTypeEnum == null) {
            System.err.println("Error: Unsupported database type: " + dbType);
            return null;
        }
        return dbTypeEnum;
    }

    private void executeMapper2Sql(DbType dbTypeEnum) throws Exception {
        System.out.println("mapper2sql is running...");

        List<MapperSqlInfo> mapperSqlInfos;

        // Check if database connection is available for testing
        boolean hasDbConnection = StringUtils.isNotEmpty(dbDriver) &&
                StringUtils.isNotEmpty(jdbcUrl) &&
                StringUtils.isNotEmpty(dbUser) &&
                StringUtils.isNotEmpty(dbPassword);

        if (hasDbConnection && test) {
            // Execute with database connection and testing
            JdbcConnProperties jdbcConnProperties = new JdbcConnProperties(dbDriver, jdbcUrl, dbUser, dbPassword);
            mapperSqlInfos = SqlUtil.parseMapperAndRunTest(workDir, dbTypeEnum, jdbcConnProperties);
            System.out.println("SQL extraction and testing completed.");
        } else if (hasDbConnection) {
            // Execute with database connection but no testing
            JdbcConnProperties jdbcConnProperties = new JdbcConnProperties(dbDriver, jdbcUrl, dbUser, dbPassword);
            mapperSqlInfos = SqlUtil.parseMapper(workDir, dbTypeEnum, mock, jdbcConnProperties);
            System.out.println("SQL extraction completed with database connection.");
        } else {
            // Execute without database connection
            mapperSqlInfos = SqlUtil.parseMapper(workDir, dbTypeEnum, mock);
            System.out.println("SQL extraction completed (resultMap only).");
        }

        // Output results
        OutPutUtil.toFile(outputDir, mapperSqlInfos);
        System.out.println("Results saved to: " + outputDir);
    }
}
