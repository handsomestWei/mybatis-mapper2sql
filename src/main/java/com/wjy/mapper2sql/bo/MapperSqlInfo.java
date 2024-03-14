package com.wjy.mapper2sql.bo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.ibatis.mapping.ResultMapping;

import lombok.Data;

/**
 * @author weijiayu
 * @date 2024/3/9 15:37
 */
@Data
public class MapperSqlInfo {

    private String filePath;
    private String namespace;
    private String dbTypeName;
    private List<ResultMapping> propertyResultMappings = new ArrayList<>();
    private HashMap<String, String> sqlIdMap = new HashMap<>();
    private HashMap<String, SqlTestResultInfo> SqlTestResultInfoMap = new HashMap<>();

    public MapperSqlInfo() {}

    public MapperSqlInfo(String filePath, String namespace, String dbTypeName) {
        this.filePath = filePath;
        this.namespace = namespace;
        this.dbTypeName = dbTypeName;
    }

    @Data
    public class SqlTestResultInfo {

        private Boolean result = false;
        private String msg = "";

        public SqlTestResultInfo(Boolean result, String msg) {
            this.result = result;
            this.msg = msg;
        }
    }

}
