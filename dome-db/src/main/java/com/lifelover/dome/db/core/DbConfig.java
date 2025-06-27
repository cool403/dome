package com.lifelover.dome.db.core;

import java.io.InputStream;

import com.lifelover.dome.db.ext.DateArgumentFactory;
import com.lifelover.dome.db.ext.DateColumnMapper;
import org.jdbi.v3.core.Jdbi;

public class DbConfig {
    private final Jdbi jdbi;
    private String sqlPath = "schema.sql";

    // sqlite url 格式：jdbc:sqlite:D:\dome-db\dome-db.db
    public DbConfig(String jdbcUrl, String sqlPath){
        this.jdbi = Jdbi.create(jdbcUrl);
        this.sqlPath = sqlPath;
        // 暂时不使用SqlObjectPlugin，因为需要手动创建Dao类
        // jdbi.installPlugin(new SqlObjectPlugin());
        //sqlite 没有原生的时间格式，时间都是存储text
        this.jdbi.registerColumnMapper(new DateColumnMapper());
        this.jdbi.registerArgument(new DateArgumentFactory());
    }

    public DbConfig(String jdbcUrl){
        this(jdbcUrl, "schema.sql");
    }

    public Jdbi getJdbi() {
        return jdbi;
    }

    public void init(String sqlPath){
        InputStream is = this.getClass().getResourceAsStream(sqlPath);
        if (is == null) {
            throw new RuntimeException("无法找到sql文件");
        }
        try {
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
