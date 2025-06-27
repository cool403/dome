package com.lifelover.dome.db;

import com.lifelover.dome.db.core.DbConfig;
import com.lifelover.dome.db.core.DefaultDbAccess;
import com.lifelover.dome.db.entity.ApiConfigs;

import java.util.Date;

public class DefaultDbAccessTest {


    public static void main(String[] args) {
        //测试建表
        String sqlPath = "/home/mawdx/mywork/dome/dome-db/src/main/resources/schema.sql";
        DbConfig dbConfig = new DbConfig("jdbc:sqlite:/home/mawdx/mywork/dome/dome-db/src/main/resources/dome1.db", sqlPath);
        DefaultDbAccess defaultDbAccess = new DefaultDbAccess(dbConfig);
        ApiConfigs apiConfigs = defaultDbAccess.getApiConfig("/helloworld", "GET");
        System.out.println(apiConfigs);

        ApiConfigs updateItem = new ApiConfigs();
        updateItem.setMockType("REPLAY");
        updateItem.setId(apiConfigs.getId());
        defaultDbAccess.updateApiConfig(updateItem);
        ApiConfigs apiConfigs1 = defaultDbAccess.getApiConfig("/helloworld", "GET");
        System.out.println(apiConfigs1);

        ApiConfigs insertItem = new ApiConfigs();
        insertItem.setHttpUrl("/helloworld1");
        insertItem.setHttpMethod("POST");
        insertItem.setIsMockEnabled("1");
        insertItem.setMockType("PROXY");
        insertItem.setCreatedAt(new Date());
        insertItem.setUpdatedAt(new Date());
        defaultDbAccess.addApiConfig(insertItem);

        dbConfig.init();
    }
}
