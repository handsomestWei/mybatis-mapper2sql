package com.wjy.mapper2sql.parse.token;

import org.apache.ibatis.parsing.TokenHandler;

/**
 * @author weijiayu
 * @date 2024/3/14 14:40
 */
public class SimpleTokenHandler implements TokenHandler {

    /**
     * @see org.apache.ibatis.builder.SqlSourceBuilder.ParameterMappingTokenHandler#handleToken
     */
    @Override
    public String handleToken(String content) {
        return "?";
    }
}
