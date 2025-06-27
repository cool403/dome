package com.lifelover.dome.db;

public class DefaultDbAccessTest {


    public static void main(String[] args) {
        DbConfig dbConfig = new DbConfig("jdbc:sqlite:dome.db");
        DefaultDbAccess defaultDbAccess = new DefaultDbAccess(dbConfig);
    }
}
