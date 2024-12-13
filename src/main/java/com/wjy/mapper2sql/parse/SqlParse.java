package com.wjy.mapper2sql.parse;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.parsing.GenericTokenParser;
import org.apache.ibatis.scripting.xmltags.*;
import org.apache.ibatis.session.Configuration;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.SQLUtils;
import com.wjy.mapper2sql.bo.MapperSqlInfo;
import com.wjy.mapper2sql.parse.param.SimpleSqlParamMap;
import com.wjy.mapper2sql.parse.token.SimpleTokenHandler;
import com.wjy.mapper2sql.parse.type.SimpleTypeAliasRegistry;
import com.wjy.mapper2sql.parse.xmltag.SimpleIfSqlNode;
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

            String sql = parseSql(mp, configuration);
            sql += ";";
            sql = SQLUtils.format(sql, dbType);
            info.getSqlIdMap().put(sqlId, sql);
        }
        return info;
    }

    private static Configuration createConfiguration() throws NoSuchFieldException, IllegalAccessException {
        Configuration configuration = new Configuration();
        // 自定义别名处理器，跳过class加载
        ReflectUtil.setFieldValueMaxDeep1(configuration, "typeAliasRegistry", new SimpleTypeAliasRegistry());
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
            (MapperBuilderAssistant)ReflectUtil.getFieldValueMaxDeep1(mapperParser, "builderAssistant");
        return builderAssistant.getCurrentNamespace();
    }

    private static List<ResultMapping> getPropertyResultMappings(XMLMapperBuilder mapperParser) {
        List<ResultMapping> resultMappingList = new ArrayList<>();
        try {
            try {
                Set<ResultMap> resultMapSet =
                        mapperParser.getConfiguration().getResultMaps().stream().collect(Collectors.toSet());
                if (!resultMapSet.isEmpty()) {
                    // 合并所有结果集
                    Iterator<ResultMap> it = resultMapSet.iterator();
                    while (it.hasNext()) {
                        resultMappingList.addAll(it.next().getPropertyResultMappings());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultMappingList;
    }

    /**
     * @see DynamicSqlSource#getBoundSql
     * @see org.apache.ibatis.builder.SqlSourceBuilder#parse
     */
    private static String parseSql(MappedStatement mp, Configuration configuration) throws Exception {
        SqlSource sqlSource = mp.getSqlSource();
        String staticSql = "";
        if (sqlSource instanceof DynamicSqlSource) {
            // 存储写有“${}”或者具有动态sql标签的sql信息
            // 1、自定义参数对象，递归解析时支持无限套娃，用来适配ognl表达式的getProperties校验
            DynamicContext context = new DynamicContext(configuration, new SimpleSqlParamMap());
            SqlNode rootSqlNode = (SqlNode)ReflectUtil.getFieldValueMaxDeep1(sqlSource, "rootSqlNode");
            // 2、重置if标签，自动满足test条件
            rootSqlNode = resetIfSqlNode(rootSqlNode);
            rootSqlNode.apply(context);
            String originalSql = context.getSql();
            // 3、自定义token处理器，跳过参数类型校验
            GenericTokenParser parser = new GenericTokenParser("#{", "}", new SimpleTokenHandler());
            staticSql = parser.parse(originalSql);
        } else {
            // 存储只有“#{}”或者没有标签的纯文本sql信息
            staticSql = mp.getBoundSql(new SimpleSqlParamMap()).getSql();
        }

        // TODO 自定义参数中设定了equals为true，在ognl表达式中被设值为class.toString
        // 例${price} => com.wjy.mapper2sql.parse.param.SimpleSqlParamMap@1 => ?
        String regx = "com.wjy.mapper2sql.parse.param.SimpleSqlParamMap@\\d+";
        staticSql = staticSql.replaceAll(regx, "?");
        return staticSql;
    }

    private static SqlNode resetIfSqlNode(SqlNode sqlNode) {
        try {
            String fieldName = "contents";
            if (sqlNode instanceof IfSqlNode) {
                sqlNode = new SimpleIfSqlNode((SqlNode)ReflectUtil.getFieldValueMaxDeep1(sqlNode, fieldName));
            } else if (sqlNode instanceof TrimSqlNode) {
                sqlNode = (TrimSqlNode)sqlNode;
            } else if (sqlNode instanceof ChooseSqlNode) {
                sqlNode = (ChooseSqlNode)sqlNode;
                fieldName = "ifSqlNodes";
            }
            // 处理子节点
            Object fieldValue = ReflectUtil.getFieldValueMaxDeep1(sqlNode, fieldName);
            if (fieldValue == null) {
                return sqlNode;
            }
            if (fieldValue instanceof List) {
                List<SqlNode> contents = (List<SqlNode>)fieldValue;
                for (int i = 0; i < contents.size(); i++) {
                    SqlNode subSqlNode = contents.get(i);
                    contents.set(i, resetIfSqlNode(subSqlNode));
                }
            } else if (fieldValue instanceof SqlNode) {
                ReflectUtil.setFieldValueMaxDeep1(sqlNode, fieldName, resetIfSqlNode((SqlNode)fieldValue));
            }
        } catch (NoSuchFieldException e) {
            // ignore
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sqlNode;
    }
}
