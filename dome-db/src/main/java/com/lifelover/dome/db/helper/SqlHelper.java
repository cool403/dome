package com.lifelover.dome.db.helper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SqlHelper {
    private SqlHelper() {
    }


    private static String camelToSnakeCase(String input){
        if (input == null) {
            return null;
        }
        return input.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }

    public static <T> String insertSql(Class<T> clazz, String...ignoreFields) {
        if (clazz == null) {
            return null;
        }
        final List<String> ignoreLst = new ArrayList<>();
        if (ignoreFields != null) {
            ignoreLst.addAll(Arrays.asList(ignoreFields));
        }
        String className = clazz.getSimpleName();
        String tableName = camelToSnakeCase(className);
        Field[] fields = clazz.getDeclaredFields();
        StringBuilder prefixSb = new StringBuilder("insert into ").append(tableName).append("(");
        StringBuilder suffixSb = new StringBuilder("(");
        for (Field field : fields) {
            String fieldName = field.getName();
            Class<?> fieldType = field.getType();
            if (ignoreLst.contains(fieldName)) {
                continue;
            }
            //判断是否是可映射sqlType字段
            if (!TypeChecker.isMappableToSqlType(fieldType)) {
                continue;
            }
            prefixSb.append(camelToSnakeCase(fieldName)).append(",");
            suffixSb.append(":").append(fieldName).append(",");
        }
        return prefixSb.append(") values").append(suffixSb).append(")").toString().replace(",)", ")");
    }


    public static String updateSql(Object entity, String primaryKey, Object primaryKeValue, String...forcedToNullColumns){
        if (entity == null || primaryKey == null) {
            throw new RuntimeException("无法生成更新sql失败，只支持entity和主键更新");
        }
        final List<String> forcedColumns = new ArrayList<>();
        if (forcedToNullColumns != null) {
            forcedColumns.addAll(Arrays.asList(forcedToNullColumns));
        }
        final String tableName = camelToSnakeCase(entity.getClass().getSimpleName());
        StringBuilder sqlSb = new StringBuilder("update ").append(tableName).append(" set ");
        //获取所有非空字段
        final Field[] fields = entity.getClass().getDeclaredFields();
        try {
            for (Field field : fields) {
                Class<?> fieldType = field.getClass();
                String fieldName = field.getName();
                if (TypeChecker.isMappableToSqlType(fieldType)) {
                    continue;
                }
                field.setAccessible(true);
                Object fieldValue = field.get(entity);
                if (fieldValue == null && !forcedColumns.contains(fieldName)) {
                    continue;
                }
                sqlSb.append(camelToSnakeCase(fieldName)).append("=").append(":").append(fieldName).append(",");
            }
            sqlSb.append("where").append(camelToSnakeCase(primaryKey)).append("=").append(primaryKeValue);
            return sqlSb.toString().replace(",where", " where ");
        } catch (Exception e) {
            throw new RuntimeException("生成更新sql出错!", e);
        }
    }
}
