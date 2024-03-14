package com.wjy.mapper2sql.parse.xmltag;

import org.apache.ibatis.scripting.xmltags.DynamicContext;
import org.apache.ibatis.scripting.xmltags.SqlNode;

/**
 * @author weijiayu
 * @date 2024/3/14 16:55
 */
public class SimpleIfSqlNode implements SqlNode {

    private SqlNode contents;

    public SimpleIfSqlNode(SqlNode contents) {
        this.contents = contents;
    }

    /**
     * @see org.apache.ibatis.scripting.xmltags.IfSqlNode#apply
     */
    @Override
    public boolean apply(DynamicContext context) {
        contents.apply(context);
        return true;
    }
}
