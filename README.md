# mybatis-mapper2sql
提取mybatis mapper xml sql，自动mock sql参数

## 简介
+ 快速启动，只需提供xml文件，无需依赖环境加载dao层class
+ 自动mock生成sql参数，尽可能
+ 支持扫描文件目录批量提取，生成sql文件
+ 支持配置jdbc连接，自动执行sql，记录执行是否异常

## 使用示例
### 1、保留参数占位符
```java
String resource = "D:\\test-mapper.xml";
List<MapperSqlInfo> infos = SqlUtil.parseMapper(resource, DbType.postgresql, false);
for (MapperSqlInfo mapperSqlInfo : infos) {
    OutPutUtil.toStdOut(mapperSqlInfo);
}
```
```sql
---namespace=[com.xxx.dao.TestDao], dbType=[postgresql], file=[D:\test-mapper.xml]

---id=[selectByParam], testResult=[unknown], testMsg=[]
SELECT d_id, index_code
FROM tb_xxx
WHERE create_time IS NOT NULL
	AND index_code IN (?)
ORDER BY d_id DESC
LIMIT ? OFFSET (? - 1) * ?;

---id=[delete], testResult=[unknown], testMsg=[]
DELETE FROM tb_xxx
WHERE d_id IN (?);
```
### 2、自动mock参数
```java
String resource = "D:\\test-mapper.xml";
List<MapperSqlInfo> infos = SqlUtil.parseMapper(resource, DbType.postgresql, true);
for (MapperSqlInfo mapperSqlInfo : infos) {
    OutPutUtil.toStdOut(mapperSqlInfo);
}
```
```sql
---file=[test-mapper.xml], dbType=[postgresql], namespace=[com.xxx.dao.TestDao]

---id=[selectByParam], testResult=[unknown], testMsg=[]
SELECT d_id, index_code
FROM tb_xxx
WHERE create_time IS NOT NULL
	AND index_code IN ('46af')
ORDER BY d_id DESC
LIMIT 4 OFFSET (0 - 1) * 8;

---id=[delete], testResult=[unknown], testMsg=[]
DELETE FROM tb_xxx
WHERE d_id IN ('474e');
```
### 3、自动执行sql
```java
String resource = "D:\\test-mapper.xml";
JdbcConnProperties properties = new JdbcConnProperties(jdbcString, urlString, userName, password);
 List<MapperSqlInfo> infos = SqlUtil.parseMapperAndRunTest(resource, DbType.postgresql, properties);
for (MapperSqlInfo mapperSqlInfo : infos) {
    OutPutUtil.toStdOut(mapperSqlInfo);
}
```
```sql
---file=[test-mapper.xml], dbType=[postgresql], namespace=[com.xxx.dao.TestDao]

---id=[selectByParam], testResult=[false], testMsg=[表tb_xxx不存在]
SELECT d_id, index_code
FROM tb_xxx
WHERE create_time IS NOT NULL
	AND index_code IN ('46af')
ORDER BY d_id DESC
LIMIT 4 OFFSET (0 - 1) * 8;

---id=[delete], testResult=[false], testMsg=[表tb_xxx不存在]
DELETE FROM tb_xxx
WHERE d_id IN ('474e');
```

## 源文件
```xml
<?xml version="1.0" encoding="utf-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.xxx.dao.TestDao">
    <resultMap type="XxxRecord" id="XxxRecordMap">
        <id property="id" column="d_id" jdbcType="VARCHAR"/>
        <result property="indexCode" column="index_code" jdbcType="VARCHAR"/>
		<result property="createTime" column="create_time" jdbcType="VARCHAR"/>
    </resultMap>

    <delete id="delete" parameterType="java.util.List">
        <![CDATA[ delete from tb_xxx where d_id in ]]>
        <foreach item="item" index="index" collection="list" open="(" separator="," close=")">
            #{item}
        </foreach>
    </delete>

    <!-- 分页查询 -->
    <select id="selectByParam" parameterType="DynamicQueryParam" resultMap="XxxRecordMap">
        <![CDATA[ select ]]>
        <include refid="fieldSql"/>
		 <![CDATA[ from tb_xxx where create_time is not null]]>
        <include refid="conditionSql"></include>
        <include refid="orderSql"></include>
        <include refid="pageSql"></include>
    </select>

    <sql id="conditionSql">
        <choose>
            <when test="queryParam.indexCodes != null">
                <![CDATA[  and index_code in ]]>
                <foreach item="indexCode" index="index" collection="queryParam.indexCodes" open="(" separator=","
                         close=")">
                    #{indexCode}
                </foreach>
            </when>
        </choose>
    </sql>

    <sql id="pageSql">
        <![CDATA[ limit #{pageSize} offset (#{pageNo} - 1) * #{pageSize}]]>
    </sql>

    <sql id="orderSql">
        <![CDATA[order by d_id desc]]>
    </sql>

    <sql id="fieldSql">
        <![CDATA[d_id ,index_code]]>
    </sql>
</mapper>
```
