# mybatis-mapper2sql
提取mybatis mapper xml sql，自动mock sql参数

## 简介
+ 快速启动，只需提供xml文件，无需依赖环境加载dao层class，无需把整个屎山项目跑起来。
+ 自动mock生成sql参数，尽可能。
+ 支持扫描文件目录批量提取，生成sql文件。
+ 支持配置jdbc连接，自动执行sql，记录执行是否异常。   
[配套intellij plugin插件市场下载](https://plugins.jetbrains.com/plugin/25584-mybatis-mapper2sql/)

## 使用方式
pom.xml增加依赖
```
<dependency>
    <groupId>com.wjy</groupId>
    <artifactId>mapper2sql</artifactId>
    <version>${last.version}</version>
</dependency>
```
maven setting.xml servers标签增加一组git仓库下载配置
```
<server>
    <id>mybatis-mapper2sql</id>
    <username>handsomestWei</username>
	<!-- read:packages PAT, you need use AES decrypt with key handsomestWei -->
    <password>U2FsdGVkX1/0nXhVlZqZtTu7TPfm79znYQvarNZM/BF/nU6dx2FGwKfez62X1D78
NA4KK30oSplZVJ5+GdvKTA==</password>
</server>
```

## 使用示例
### 保留参数占位符
#### 示例代码
```java
// 指定扫描的mapper文件路径，也可以是整个项目
String resource = "D:\\test-mapper.xml";

// 提取sql，保留sql里的?占位符
List<MapperSqlInfo> infos = SqlUtil.parseMapper(resource, DbType.postgresql, false);

// 结果输出到控制台
for (MapperSqlInfo mapperSqlInfo : infos) {
    OutPutUtil.toStdOut(mapperSqlInfo);
}
```
#### 输出结果
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
### 支持自动mock参数
#### 使用前提
+ 方式一：在mapper xml中，使用resultMap标签定义字段名称和jdbcType类型。 
+ 方式二：配置jdbc连接，运行时从表结构动态获取字段和类型。适用于未定义resultMap的场景。   

注意：目前仅支持select语句的参数mock

#### 示例代码
```java
// 指定扫描的mapper文件路径，也可以是整个项目
String resource = "D:\\test-mapper.xml";

// 提取sql，自动mock参数
// 使用方式一
List<MapperSqlInfo> infos = SqlUtil.parseMapper(resource, DbType.postgresql, true);

// 使用方式二
// JdbcConnProperties properties = new JdbcConnProperties(jdbcDriver, urlString, userName, password);
// List<MapperSqlInfo> infos = SqlUtil.parseMapper(resource, DbType.postgresql, true, properties);

// 结果输出到控制台
for (MapperSqlInfo mapperSqlInfo : infos) {
    OutPutUtil.toStdOut(mapperSqlInfo);
}
```
#### 输出结果
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
### 支持自动执行sql
#### 使用前提
在工程中，添加对应jdbc驱动依赖。

#### 示例代码
```java
// 指定扫描的文件夹路径，可以是整个项目
String resource = "D:\\xxxProject";
// 指定输出目录
String outPutDir = "D:\\xxxProject-sql";
// 配置jdbc连接，sql测试用
JdbcConnProperties properties = new JdbcConnProperties(jdbcDriver, urlString, userName, password);

// 提取sql，自动mock参数，自动执行sql，记录执行结果
List<MapperSqlInfo> infos = SqlUtil.parseMapperAndRunTest(resource, DbType.postgresql, properties);

// 结果输出到目录下文件
OutPutUtil.toFile(outPutDir, infos);
```
#### 输出结果
```sql
---file=[d:\test-mapper.xml], dbType=[postgresql], namespace=[com.xxx.dao.TestDao]

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
