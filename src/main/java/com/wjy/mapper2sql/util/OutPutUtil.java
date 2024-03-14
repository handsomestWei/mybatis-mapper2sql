package com.wjy.mapper2sql.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.wjy.mapper2sql.bo.MapperSqlInfo;

import lombok.NonNull;

/**
 * @author weijiayu
 * @date 2024/3/10 0:43
 */
public class OutPutUtil {

    public static void toStdOut(@NonNull MapperSqlInfo info) {
        List<String> fmtList = formatMapperSqlInfo(info);
        try (PrintStream ps = System.out) {
            for (String s : fmtList) {
                ps.println(s);
            }
        }
    }

    public static boolean toFile(@NonNull String outPutPath, @NonNull List<MapperSqlInfo> MapperSqlInfos)
        throws Exception {
        if (MapperSqlInfos == null || MapperSqlInfos.isEmpty()) {
            return false;
        }
        File baseFile = new File(outPutPath);
        if (!FileUtil.isDirAndMks(baseFile)) {
            return false;
        }
        String baseFilePath = baseFile.getPath();
        for (MapperSqlInfo mapperSqlInfo : MapperSqlInfos) {
            String fileName = new File(mapperSqlInfo.getFilePath()).getName();
            try (BufferedWriter bfw = new BufferedWriter(new FileWriter(baseFilePath + "\\" + fileName + ".sql"))) {
                List<String> fmtList = formatMapperSqlInfo(mapperSqlInfo);
                for (String s : fmtList) {
                    bfw.write(s);
                    bfw.newLine();
                }
            }
        }
        return true;
    }

    public static boolean toReport(@NonNull String outPutPath, @NonNull List<MapperSqlInfo> MapperSqlInfos)
        throws Exception {
        if (MapperSqlInfos == null || MapperSqlInfos.isEmpty()) {
            return false;
        }
        File baseFile = new File(outPutPath);
        if (!FileUtil.isDirAndMks(baseFile)) {
            return false;
        }
        // TODO 生成excel或html测试报告
        return true;
    }

    private static List<String> formatMapperSqlInfo(MapperSqlInfo info) {
        List<String> fmtList = new LinkedList<>();
        fmtList.add(String.format("---namespace=[%s], dbType=[%s], file=[%s]", info.getNamespace(),
            info.getDbTypeName(), info.getFilePath()));
        fmtList.add("");
        for (Map.Entry<String, String> entry : info.getSqlIdMap().entrySet()) {
            String sqlId = entry.getKey();
            String sql = entry.getValue();

            MapperSqlInfo.SqlTestResultInfo resultInfo = info.getSqlTestResultInfoMap().get(sqlId);
            String testResult = "unknown";
            String testResultMsg = "";
            if (resultInfo != null) {
                testResult = resultInfo.getResult().toString();
                testResultMsg = resultInfo.getMsg().replace("\n", "");
            }
            fmtList.add(String.format("---id=[%s], testResult=[%s], testMsg=[%s]", sqlId, testResult, testResultMsg));
            fmtList.add(sql);
            fmtList.add("");
        }
        return fmtList;
    }
}
