package com.lifelover.dome.db.core;

import com.lifelover.dome.db.entity.ApiConfigs;
import com.lifelover.dome.db.entity.ApiRecords;

public interface DbAccess {

    /**
     * 添加api配置
     * @param apiConfigs
     * @return
     */
    long addApiConfig(ApiConfigs apiConfigs);

    /**
     * 添加api记录
     * @param apiRecords
     * @return
     */
    long addApiRecords(ApiRecords apiRecords);

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