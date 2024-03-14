package com.wjy.mapper2sql.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.ibatis.builder.xml.XMLMapperEntityResolver;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.parsing.XPathParser;

/**
 * @author weijiayu
 * @date 2024/3/13 18:07
 */
public class FileUtil {

    public static boolean isMapperXml(String filePath) {
        try {
            try (InputStream in = new FileInputStream(filePath)) {
                XPathParser parser = new XPathParser(in, true, null, new XMLMapperEntityResolver());
                XNode context = parser.evalNode("/mapper");
                if (context == null) {
                    return false;
                }
                String namespace = context.getStringAttribute("namespace");
                if (namespace == null || namespace.equals("")) {
                    return false;
                }
                return true;
            }
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isDirAndMks(File file) {
        try {
            if (file.exists()) {
                if (!file.isDirectory()) {
                    return false;
                }
            } else if (!file.mkdirs()) {
                return false;
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
