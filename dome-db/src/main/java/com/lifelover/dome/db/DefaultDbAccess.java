package com.lifelover.dome.db;

import java.util.List;

import org.jdbi.v3.core.Jdbi;

public class DefaultDbAccess implements DbAccess {
    private final Jdbi jdbi;

    public DefaultDbAccess(DbConfig dbConfig) {
        this.jdbi = dbConfig.getJdbi();
    }

    @Override
    public long apiConfig(ApiConfigs apiConfigs) {
        if(apiConfigs == null){
            return 0;
        }
        return 0;
    }

    @Override
    public long addApiRecords(List<ApiRecords> lst) {
        if(lst == null || lst.isEmpty()){
            return 0;
        }
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'addApiRecords'");
    }

    @Override
    public int updateApiConfig(ApiConfigs apiConfigs) {
        if(apiConfigs == null){
            return 0;
        }
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateApiConfig'");
    }

    @Override
    public ApiConfigs getApiConfig(String httpUrl, String httpMethod) {
        if(httpUrl == null || httpMethod == null){
            return null;
        }
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getApiConfig'");
    }
}
