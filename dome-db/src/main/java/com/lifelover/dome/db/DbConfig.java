package com.lifelover.dome.db;

import org.jdbi.v3.core.Jdbi;

public class DbConfig {
    private final Jdbi jdbi;

    // sqlite url 格式：jdbc:sqlite:D:\dome-db\dome-db.db
    public DbConfig(String url){
        jdbi = Jdbi.create(url);
        // 暂时不使用SqlObjectPlugin，因为需要手动创建Dao类
        // jdbi.installPlugin(new SqlObjectPlugin());
    }

    public Jdbi getJdbi() {
        return jdbi;
    }
}
