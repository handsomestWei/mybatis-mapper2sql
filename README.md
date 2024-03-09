# mybatis-mapper2sql
提取mybatis mapper.xml文件中的sql

## 特点
+ 方便快捷，只需提供xml文件，无其他依赖。不用做class扫描加载把整个项目工程跑起来。可用于sql预审、数据库迁移语法验证等场景。
+ 与其他语言重写xml解析不同，基于原生mybatis做sql提取，保证准确。

## 使用示例
```java
import com.alibaba.druid.DbType;
import com.wjy.mapper2sql.bo.MapperSqlInfo;
import com.wjy.mapper2sql.util.OutPutUtil;

public class Main {

    public static void main(String[] args) throws Exception {
        // mapper文件路径
        String resource = "D:\\test-mapper.xml";
        // 提取sql，需传入数据库类型，用于sql格式化整形
        MapperSqlInfo info = new SqlParse().parseMapperFile(resource, DbType.postgresql, true);
        // 结果输出到控制台
        OutPutUtil.printf(info);
    }
}
```

## 效果
### 提取结果
```sql
---file=[test-mapper.xml], dbType=[postgresql], namespace=[com.xxx.dao.TestDao]

---id=[selectByParam]
SELECT d_id, index_code
FROM tb_xxx
WHERE create_time IS NOT NULL
	AND index_code IN (?)
ORDER BY d_id DESC
LIMIT ? OFFSET (? - 1) * ?

---id=[delete]
DELETE FROM tb_xxx
WHERE d_id IN (?)
```

### 源文件
<details>
  <summary>test-mapper.xml</summary>
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
</details>
