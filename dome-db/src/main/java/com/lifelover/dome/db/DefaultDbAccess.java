package com.lifelover.dome.db;

import org.jdbi.v3.core.Jdbi;

public class DefaultDbAccess implements DbAccess {
    private final Jdbi jdbi;

    public DefaultDbAccess(DbConfig dbConfig) {
        this.jdbi = dbConfig.getJdbi();
    }

    @Override
    public long apiConfig(ApiConfigs apiConfigs) {
        if (apiConfigs == null) {
            return 0;
        }
        jdbi.useHandle(handle -> {
            handle.createUpdate(SqlHelper.insertSql(ApiConfigs.class, "id"))
                    .bindBean(apiConfigs)
                    .execute();
        });
        return 1;
    }

    @Override
    public long addApiRecords(ApiRecords apiRecords) {
        if (apiRecords == null) {
            return 0;
        }
        jdbi.useHandle(handle -> {
            handle.createUpdate(SqlHelper.insertSql(ApiRecords.class, "id"))
                    .bindBean(apiRecords)
                    .execute();
        });
        return 1;
    }

    @Override
    public int updateApiConfig(ApiConfigs apiConfigs) {
        if (apiConfigs == null) {
            return 0;
        }
        jdbi.useHandle(handle -> {
            handle.createUpdate(SqlHelper.updateSql(apiConfigs, "id", apiConfigs.getId()))
                    .bindBean(apiConfigs)
                    .execute();
        });
        return 1;
    }

    @Override
    public ApiConfigs getApiConfig(String httpUrl, String httpMethod) {
        if (httpUrl == null || httpMethod == null) {
            return null;
        }
        return jdbi.withHandle(handle -> {
            // 首先通过唯一组合键值获取ApiConfig
            final ApiConfigs apiConfigs = handle
                    .createQuery("select * from api_configs where http_url = :httpUrl and http_method= :httpMethod")
                    .bind(httpMethod, httpMethod)
                    .bind(httpUrl, httpUrl)
                    .mapToBean(ApiConfigs.class)
                    .one();
            if (apiConfigs == null) {
                return apiConfigs;
            }
            final String mockType = apiConfigs.getMockType();
            // 然后通过类型判断是否需要关联获取重放流量信息
            if (MockType.REPLAY.name().equals(mockType)) {
                final String replayRecordId = apiConfigs.getReplayRecordId();
                final ApiRecords apiRecords = handle.createQuery("select * from api_records where id = :id")
                        .bind("id", replayRecordId)
                        .mapToBean(ApiRecords.class)
                        .one();
                apiConfigs.setReplayApiRecords(apiRecords);
            }
            return apiConfigs;
        });

    }
}
