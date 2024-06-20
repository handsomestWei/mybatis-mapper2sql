# mybatis-mapper2sql
提取mybatis mapper xml sql，自动mock sql参数

## 简介
+ 快速启动，只需提供xml文件，无需依赖环境加载dao层class，无需把整个屎山项目跑起来。
+ 自动mock生成sql参数，尽可能。
+ 支持扫描文件目录批量提取，生成sql文件。
+ 支持配置jdbc连接，自动执行sql，记录执行是否异常。

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
	<!-- read:packages -->
    <password>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</password>
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
#### 示例代码
```java
// 指定扫描的mapper文件路径，也可以是整个项目
String resource = "D:\\test-mapper.xml";

// 提取sql，自动mock参数
List<MapperSqlInfo> infos = SqlUtil.parseMapper(resource, DbType.postgresql, true);

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
#### 示例代码
```java
// 指定扫描的文件夹路径，可以是整个项目
String resource = "D:\\xxxProject";
// 指定输出目录
String outPutDir = "D:\\xxxProject-sql";
// 配置jdbc连接，sql测试用
JdbcConnProperties properties = new JdbcConnProperties(jdbcString, urlString, userName, password);

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