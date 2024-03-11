package com.wjy.mapper2sql.bo;

import java.io.File;
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

    private String fileName;
    private String filePath;
    private String namespace;
    private String dbTypeName;
    private HashMap<String, String> sqlIdMap = new HashMap<>();
    private List<ResultMapping> propertyResultMappings = new ArrayList<>();

    public MapperSqlInfo() {}

    public MapperSqlInfo(String filePath, String namespace, String dbTypeName) {
        this.fileName = new File(filePath).getName();
        this.filePath = filePath;
        this.namespace = namespace;
        this.dbTypeName = dbTypeName;
    }

}
