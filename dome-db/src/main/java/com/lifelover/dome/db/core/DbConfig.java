package com.lifelover.dome.db.core;

import com.lifelover.dome.db.ext.DateArgumentFactory;
import com.lifelover.dome.db.ext.DateColumnMapper;
import org.jdbi.v3.core.Jdbi;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class DbConfig {
    private static  final String JDBC_URL = "jdbc:sqlite:%s";
    private final Jdbi jdbi;
    private String sqlPath = "schema.sql";

    // sqlite url 格式：jdbc:sqlite:D:\dome-db\dome-db.db
    public DbConfig(String jdbcUrl, String sqlPath){
        this.jdbi = Jdbi.create(String.format(JDBC_URL, jdbcUrl));
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

    public void init(){
//        InputStream is = getClass().getResourceAsStream(this.sqlPath);
//        if (is == null) {
//            throw new RuntimeException("未读取到sql脚本文件");
//        }
        try {
//            InputStream is = Files.newInputStream(new File(this.sqlPath).toPath());
//            StringBuilder out = new StringBuilder(4096);
//            InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
//            char[] buffer = new char[4096];
//            int charsRead;
//            while ((charsRead = reader.read(buffer)) != -1) {
//                out.append(buffer, 0, charsRead);
//            }
//            final String sqlScript = out.toString();
            jdbi.useHandle(handle -> {
                // 自动分割并执行每条SQL语句
                handle.createScript(CreateTable.INIT_SQL).executeAsSeparateStatements();
            });
            System.out.println("[dome agent] 初始化建表成功.");
        } catch (Exception e) {
            throw new RuntimeException("初始化建表失败.",e);
        }
    }
}
