package com.lifelover.dome.db;

import java.util.List;

public interface DbAccess {

    /**
     * 添加api配置
     * @param apiConfigs
     * @return
     */
    long apiConfig(ApiConfigs apiConfigs);

    /**
     * 添加api记录
     * @param lst
     * @return
     */
    long addApiRecords(List<ApiRecords> lst);

    /**
     * 更新api配置
     * @param apiConfigs
     * @return
     */
    int updateApiConfig(ApiConfigs apiConfigs);

    /**
     * 获取api配置
     * @param httpUrl
     * @param httpMethod
     * @return
     */
    ApiConfigs getApiConfig(String httpUrl, String httpMethod);

}