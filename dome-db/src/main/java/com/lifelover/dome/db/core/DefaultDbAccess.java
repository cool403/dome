package com.lifelover.dome.db.core;

import com.lifelover.dome.db.helper.ApiType;
import com.lifelover.dome.db.helper.MockType;
import com.lifelover.dome.db.helper.SqlHelper;
import com.lifelover.dome.db.entity.ApiConfigs;
import com.lifelover.dome.db.entity.ApiRecords;
import org.jdbi.v3.core.Jdbi;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import java.util.Optional;

public class DefaultDbAccess implements DbAccess {
    private final Jdbi jdbi;

    public DefaultDbAccess(DbConfig dbConfig) {
        this.jdbi = dbConfig.getJdbi();
    }

    @Override
    public long addApiConfig(ApiConfigs apiConfigs) {
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
        final Date now = new Date();
        jdbi.useHandle(handle -> {
            handle.createUpdate(SqlHelper.insertSql(ApiRecords.class, "id"))
                    .bindBean(apiRecords)
                    .execute();
            ApiConfigs apiConfigs = getApiConfig(apiRecords.getHttpUrl(), apiRecords.getHttpMethod(),ApiType.of(apiRecords.getApiType()));
            //不存在则创建
            if (apiConfigs == null) {
                apiConfigs = new ApiConfigs();
                apiConfigs.setApiType(apiRecords.getApiType());
                parseHttpUrl(apiRecords.getHttpUrl(), apiConfigs);
                apiConfigs.setHttpMethod(apiRecords.getHttpMethod());
                apiConfigs.setMockType(MockType.REPLAY.name());
                //默认不开启回放
                apiConfigs.setIsMockEnabled("0");
                apiConfigs.setCreatedAt(now);
                apiConfigs.setUpdatedAt(now);
                addApiConfig(apiConfigs);
            }
        });
        return 1;
    }

    private void parseHttpUrl(String httpUrl, ApiConfigs apiConfigs){
        URI uri;
        try {
            uri = new URI(httpUrl);
            //需要拼接schema,前提schema不为空
            if (uri.getScheme() != null) {
                apiConfigs.setHost(uri.getScheme() + "://" + uri.getHost());
            }else{
                apiConfigs.setHost(uri.getHost());
            }
            apiConfigs.setHttpUrl(uri.getPath());
            return;
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        apiConfigs.setHttpUrl(httpUrl);
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
    public ApiConfigs getApiConfig(String httpUrl, String httpMethod, ApiType apiType) {
        if (httpUrl == null || httpMethod == null) {
            return null;
        }
        //httpUrl需要处理下
        try {
            URI uri = new URI(httpUrl);
            httpUrl = uri.getPath();
        } catch (Exception e) {
            e.printStackTrace();
        }
        final String parsedUrl = httpUrl;
        return jdbi.withHandle(handle -> {
            // 首先通过唯一组合键值获取ApiConfig
            Optional<ApiConfigs> apiConfigsOptional = handle
                    .createQuery("select * from api_configs where http_url = :httpUrl and http_method= :httpMethod and api_type = :apiType")
                    .bind("httpMethod", httpMethod)
                    .bind("httpUrl", parsedUrl)
                    .bind("apiType", apiType.name())
                    .mapToBean(ApiConfigs.class)
                    .findOne();
            apiConfigsOptional.ifPresent(it -> {
                final String mockType = it.getMockType();
                // 然后通过类型判断是否需要关联获取重放流量信息
                if (MockType.REPLAY.name().equals(mockType)) {
                    final String replayRecordId = it.getReplayRecordId();
                    handle.createQuery("select * from api_records where id = :id")
                            .bind("id", replayRecordId)
                            .mapToBean(ApiRecords.class)
                            .findOne().ifPresent(it::setReplayApiRecords);
                }
            });
            return apiConfigsOptional.orElse(null);
        });

    }
}
