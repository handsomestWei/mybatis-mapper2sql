package com.wjy.mapper2sql.parse;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.session.Configuration;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.SQLUtils;
import com.wjy.mapper2sql.bo.MapperSqlInfo;
import com.wjy.mapper2sql.parse.param.SimpleSqlParamMap;
import com.wjy.mapper2sql.parse.type.SimpleTypeAliasRegistry;
import com.wjy.mapper2sql.util.ReflectUtil;

/**
 * @author weijiayu
 * @date 2024/3/9 14:06
 */
public class SqlParse {

    public static MapperSqlInfo parseMapperFile(String filePath, DbType dbType) throws Exception {
        Configuration configuration = createConfiguration();
        XMLMapperBuilder mapperParser = createMapperParser(filePath, configuration);
        mapperParser.parse();

        MapperSqlInfo info = new MapperSqlInfo(filePath, getNamespace(mapperParser), dbType.name());
        info.setPropertyResultMappings(getPropertyResultMappings(mapperParser));

        for (MappedStatement mp : configuration.getMappedStatements().stream().collect(Collectors.toSet())) {
            String sqlId = mp.getId();
            sqlId = sqlId.substring(sqlId.lastIndexOf('.') + 1);

            String sql = mp.getBoundSql(new SimpleSqlParamMap()).getSql();
            sql += ";";
            sql = SQLUtils.format(sql, dbType);
            info.getSqlIdMap().put(sqlId, sql);
        }
        return info;
    }

    private static Configuration createConfiguration() throws NoSuchFieldException, IllegalAccessException {
        Configuration configuration = new Configuration();
        ReflectUtil.setFieldValue(configuration, "typeAliasRegistry", new SimpleTypeAliasRegistry());
        return configuration;
    }

    private static XMLMapperBuilder createMapperParser(String filePath, Configuration configuration)
        throws IOException {
        XMLMapperBuilder mapperParser;
        try (InputStream in = new FileInputStream(filePath)) {
            mapperParser = new XMLMapperBuilder(in, configuration, filePath, configuration.getSqlFragments());
        }
        return mapperParser;
    }

    private static String getNamespace(XMLMapperBuilder mapperParser)
        throws NoSuchFieldException, IllegalAccessException {
        MapperBuilderAssistant builderAssistant =
            (MapperBuilderAssistant)ReflectUtil.getFieldValue(mapperParser, "builderAssistant");
        return builderAssistant.getCurrentNamespace();
    }

    private static List<ResultMapping> getPropertyResultMappings(XMLMapperBuilder mapperParser) {
        try {
            Set<ResultMap> resultMapSet =
                mapperParser.getConfiguration().getResultMaps().stream().collect(Collectors.toSet());
            return resultMapSet.iterator().next().getPropertyResultMappings();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
