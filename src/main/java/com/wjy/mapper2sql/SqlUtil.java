package com.wjy.mapper2sql;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.parser.SQLParserUtils;
import com.wjy.mapper2sql.bo.JdbcConnProperties;
import com.wjy.mapper2sql.bo.MapperSqlInfo;
import com.wjy.mapper2sql.mock.SqlMock;
import com.wjy.mapper2sql.parse.SqlParse;
import com.wjy.mapper2sql.util.FileUtil;
import com.wjy.mapper2sql.util.JdbcConnUtil;
import com.wjy.mapper2sql.util.MybatisUtil;

import lombok.NonNull;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.type.JdbcType;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author weijiayu
 * @date 2024/3/11 10:20
 */
public class SqlUtil {

    /**
     * 从xml的resultMap定义，获取字段名称和类型
     */
    public static List<MapperSqlInfo> parseMapper(@NonNull String filePath, @NonNull DbType dbType, boolean isMockParam)
            throws Exception {
        List<MapperSqlInfo> mapperSqlInfos = new LinkedList<>();
        try (Stream<Path> paths = Files.walk(Paths.get(filePath))) {
            paths.map(path -> path.toString()).filter(fp -> FileUtil.isMapperXml(fp)).forEach(fp -> {
                try {
                    mapperSqlInfos.add(parseMapperFile(fp, dbType, isMockParam, null));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
        return mapperSqlInfos;
    }

    /**
     * 从xml的resultMap定义，获取字段名称和类型
     * 增强：自动连接数据库从表结构获取最新字段类型作为补充
     */
    public static List<MapperSqlInfo> parseMapper(@NonNull String filePath, @NonNull DbType dbType, boolean isMockParam,
            JdbcConnProperties connProperties) throws Exception {
        List<MapperSqlInfo> mapperSqlInfos = new LinkedList<>();
        try (Connection conn = connProperties == null ? null
                : JdbcConnUtil.newConnect(connProperties.getJdbcDriver(),
                        connProperties.getJdbcUrl(), connProperties.getUserName(),
                        connProperties.getPassword());
                Stream<Path> paths = Files.walk(Paths.get(filePath))) {
            paths.map(path -> path.toString()).filter(fp -> FileUtil.isMapperXml(fp)).forEach(fp -> {
                try {
                    mapperSqlInfos.add(parseMapperFile(fp, dbType, isMockParam, conn));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
        return mapperSqlInfos;
    }

    public static List<MapperSqlInfo> parseMapper(@NonNull String filePath, @NonNull DbType dbType, boolean isMockParam,
            Connection conn) throws Exception {
        List<MapperSqlInfo> mapperSqlInfos = new LinkedList<>();
        try (Stream<Path> paths = Files.walk(Paths.get(filePath))) {
            paths.map(path -> path.toString()).filter(fp -> FileUtil.isMapperXml(fp)).forEach(fp -> {
                try {
                    mapperSqlInfos.add(parseMapperFile(fp, dbType, isMockParam, conn));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
        return mapperSqlInfos;
    }

    /*
     * 解析xml，并自动执行sql测试，记录执行结果
     */
    public static List<MapperSqlInfo> parseMapperAndRunTest(@NonNull String filePath, @NonNull DbType dbType,
            JdbcConnProperties connProperties) throws Exception {
        List<MapperSqlInfo> mapperSqlInfos = parseMapper(filePath, dbType, true, connProperties);
        Connection conn = null;
        try {
            conn = JdbcConnUtil.newConnect(connProperties.getJdbcDriver(), connProperties.getJdbcUrl(),
                    connProperties.getUserName(), connProperties.getPassword());
            runTest(conn, mapperSqlInfos);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return mapperSqlInfos;
    }

    public static List<MapperSqlInfo> parseMapperAndRunTest(@NonNull String filePath, @NonNull DbType dbType,
            Connection conn) throws Exception {
        List<MapperSqlInfo> mapperSqlInfos = parseMapper(filePath, dbType, true, conn);
        runTest(conn, mapperSqlInfos);
        return mapperSqlInfos;
    }

    private static MapperSqlInfo parseMapperFile(@NonNull String filePath, @NonNull DbType dbType,
            boolean isMockParam, Connection conn) throws Exception {
        // 1、解析mapper xml文件，并提取出带占位符?的sql
        MapperSqlInfo mapperSqlInfo = SqlParse.parseMapperFile(filePath, dbType);
        if (!isMockParam) {
            return mapperSqlInfo;
        }

        // 2、从xml的resultMap定义获取字段名称和类型
        HashMap<String, JdbcType> columnJdbcTypeMap = mergeResultMappings(mapperSqlInfo.getPropertyResultMappings());
        HashMap<String, String> sqlIdMap = mapperSqlInfo.getSqlIdMap();
        if (sqlIdMap.isEmpty() || columnJdbcTypeMap.isEmpty()) {
            return mapperSqlInfo;
        }

        // 3、sql参数mock
        // sql格式化：对sql做AST语法解析整形，方便后续解析
        // 占位符?类型推断：在表达式里找到?最可能匹配的表字段，在结果集里查找字段对应的jdbc类型， 并mock值
        HashMap<String, HashMap<String, JdbcType>> tmpTableColumnJdbcTypeMap = new HashMap<>();
        for (Map.Entry<String, String> entry : sqlIdMap.entrySet()) {
            try {
                String sqlId = entry.getKey();
                String sql = entry.getValue();
                // 4、补充从数据库表结构获取最新字段类型，解决xml内部字段类型定义缺失和旧数据问题
                if (conn != null) {
                    // TODO token仅支持提取select语句from后的表名
                    List<String> tableNameList = SQLParserUtils.getTables(sql, dbType);
                    for (String tableName : tableNameList) {
                        HashMap<String, JdbcType> tableColumnJdbcTypeMap = new HashMap<>();
                        if (tmpTableColumnJdbcTypeMap.containsKey(tableName)) {
                            // 优先从缓存中获取
                            tableColumnJdbcTypeMap = tmpTableColumnJdbcTypeMap.get(tableName);
                        } else {
                            tableColumnJdbcTypeMap = MybatisUtil.getTableColumnType(tableName, conn);
                            tmpTableColumnJdbcTypeMap.put(tableName, tableColumnJdbcTypeMap);
                        }
                        columnJdbcTypeMap.putAll(tableColumnJdbcTypeMap);
                    }
                }
                sqlIdMap.put(sqlId, SqlMock.mockSql(sql, dbType, "?", columnJdbcTypeMap));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return mapperSqlInfo;
    }

    private static HashMap<String, JdbcType> mergeResultMappings(List<ResultMapping> propertyResultMappings) {
        HashMap<String, JdbcType> columnJdbcTypeMap = new HashMap<>();
        if (propertyResultMappings == null) {
            return columnJdbcTypeMap;
        }
        for (ResultMapping rm : propertyResultMappings) {
            columnJdbcTypeMap.put(rm.getColumn(), rm.getJdbcType());
        }
        return columnJdbcTypeMap;
    }

    private static void runTest(Connection conn, List<MapperSqlInfo> mapperSqlInfos) throws Exception {
        for (MapperSqlInfo mapperSqlInfo : mapperSqlInfos) {
            for (Map.Entry<String, String> entry : mapperSqlInfo.getSqlIdMap().entrySet()) {
                boolean result = true;
                String msg = "";
                String sqlId = entry.getKey();
                String sql = entry.getValue();
                try {
                    // 只要不抛异常
                    boolean rs = conn.createStatement().execute(sql);
                } catch (Throwable t) {
                    result = false;
                    msg = t.getMessage();
                }
                mapperSqlInfo.getSqlTestResultInfoMap().put(sqlId,
                        mapperSqlInfo.new SqlTestResultInfo(result, msg));
            }
        }
    }
}
