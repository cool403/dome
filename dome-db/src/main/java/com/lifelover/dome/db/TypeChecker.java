package com.lifelover.dome.db;

import java.util.HashSet;
import java.util.Set;

public class TypeChecker {
    private TypeChecker(){

    }

    private static final Set<Class<?>> MAPPABLE_TYPES = new HashSet<>();

    static{
        MAPPABLE_TYPES.add(String.class);
        MAPPABLE_TYPES.add(java.math.BigDecimal.class);
        MAPPABLE_TYPES.add(Boolean.class);
        MAPPABLE_TYPES.add(Byte.class);
        MAPPABLE_TYPES.add(Short.class);
        MAPPABLE_TYPES.add(Integer.class);
        MAPPABLE_TYPES.add(Long.class);
        MAPPABLE_TYPES.add(Float.class);
        MAPPABLE_TYPES.add(Double.class);
        MAPPABLE_TYPES.add(java.sql.Date.class);
        MAPPABLE_TYPES.add(java.sql.Time.class);
        MAPPABLE_TYPES.add(java.sql.Timestamp.class);
        MAPPABLE_TYPES.add(java.sql.Clob.class);
        MAPPABLE_TYPES.add(java.sql.Blob.class);

        MAPPABLE_TYPES.add(java.util.Date.class);
    }

    public static boolean isMappableToSqlType(Class<?> clazz){
        return MAPPABLE_TYPES.contains(clazz);
    }
}
