# mybatis-mapper2sql
提取mybatis mapper xml sql，自动mock sql参数。

## 简介
+ 快速启动，`只需提供xml文件`，`无需依赖环境加载dao层class`，无需把整个屎山项目跑起来。
+ sql解析提取基于原生`mybatis`框架，确保稳定和可靠性。
+ 自动mock生成sql参数，尽可能。
+ 支持扫描文件目录批量提取，生成sql文件。
+ 支持配置jdbc连接，自动执行sql，记录执行是否异常。

适用于`sql预审`、`数据库迁移适配`等场景。

## 配套产品
+ Intellij Plugin插件 [mybatis-mapper2sql-plugin](https://github.com/handsomestWei/mybatis-mapper2sql-plugin)：[插件市场下载](https://plugins.jetbrains.com/plugin/25584-mybatis-mapper2sql/)
+ MCP server

## 使用示例
[参考](/src/test/java/com/wjy/mapper2sql/MainTest.java)

### 保留参数占位符
#### 代码示例
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
#### 输出结果示例
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
+ 方式二：配置jdbc连接，运行时从表结构动态获取字段和类型，作为xml字段定义信息缺失的补充。

目前支持select语句的参数mock

#### 代码示例
```java
// 指定扫描的mapper文件路径，也可以是整个项目
String resource = "D:\\test-mapper.xml";

// 提取sql，自动mock参数
// 字段信息用于类型推断和参数mock：优先解析在mapper xml文件的resultMap标签定义的字段信息
// 如果配置了jdbc连接，且连接可用，则运行时从表结构动态获取字段和类型，作为字段定义信息缺失的补充
JdbcConnProperties properties = new JdbcConnProperties(jdbcDriver, urlString, userName, password);
List<MapperSqlInfo> infos = SqlUtil.parseMapper(resource, DbType.postgresql, true, properties);
// List<MapperSqlInfo> infos = SqlUtil.parseMapper(resource, DbType.postgresql, true);

// 结果输出到控制台
for (MapperSqlInfo mapperSqlInfo : infos) {
    OutPutUtil.toStdOut(mapperSqlInfo);
}
```
#### 输出结果示例
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
注意：mock参数后，运行时可能会命中部分更新删除sql，影响数据库数据。

#### 代码示例
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

## 附：命令行方式使用
使用jar方式运行，部署和使用更便捷。

### 基本用法
注意：依赖的jar包放置在同级目录的`lib`文件夹内。运行时的jdbc驱动也需要手动放置到该文件夹内。
```bash
# 查看帮助信息
java -jar mapper2sql-x.y.z.jar --help

# 使用properties文件定义
java -jar mapper2sql-x.y.z.jar --properties config.properties
```

### 命令行参数说明
- `-p, --properties`: 配置文件路径
- `-w, --work-dir`: 工作目录（扫描mapper XML文件）
- `-o, --output-dir`: 输出目录
- `-t, --db-type`: 数据库类型（postgresql, mysql, oracle等）
- `-m, --mock`: 启用参数模拟（默认true）
- `--db-driver`: JDBC驱动类名
- `--jdbc-url`: JDBC连接URL
- `--db-user`: 数据库用户名
- `--db-password`: 数据库密码
- `--test`: 执行SQL测试
- `-h, --help`: 显示帮助信息
- `-V, --version`: 显示版本信息

### Properties文件配置示例
[参考](/src/main/resources/mapper2sql.properties)

## 附：集成方式依赖配置
将mybatis-mapper2sql集成到其他工程。
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
