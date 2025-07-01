package com.lifelover.dome.core.mock;

import com.lifelover.dome.core.config.ConfigLoader;
import com.lifelover.dome.db.core.DbAccess;
import com.lifelover.dome.db.entity.ApiConfigs;
import com.lifelover.dome.db.entity.ApiRecords;
import com.lifelover.dome.db.helper.MockType;

public class ApiMockInterceptor {

    private  ApiMockInterceptor(){

    }

    /**
     * 模拟接口
     * @param apiMockContext 参数
     * @return 模拟报文
     */
    public static ApiRecords mock(ApiMockContext apiMockContext){
        //只有contentype是json的才进行mock
        String contentType = apiMockContext.getContentType();
        if (contentType != null && !contentType.toLowerCase().contains("json")) {
            System.err.println("[dome agent] 目前只有contentType是application/Json才支持mock");
            return null;
        }
        //根据httpUrl 和httpMethod获取api信息
        String httpUrl = apiMockContext.getHttpUrl();
        String httpMethod = apiMockContext.getHttpMethod();
        DbAccess dbAccess = ConfigLoader.getAgentConfig().getDbAccess();
        if (dbAccess == null) {
            return null;
        }
        ApiConfigs apiConfig = dbAccess.getApiConfig(httpUrl, httpMethod);
        //没配置api信息的也不做mock处理
        if (apiConfig == null) {
            return null;
        }
        String isMockEnabled = apiConfig.getIsMockEnabled();
        if (YesOrNo.N.equals(isMockEnabled)) {
            System.out.println("[dome agent] 当前接口未开启mock");
            return null;
        }
        String mockType = apiConfig.getMockType();
        //判断是否是REPLAY
        if (MockType.REPLAY.name().equals(mockType)) {
            return apiConfig.getReplayApiRecords();
        }
        //静态响应
        if (MockType.STATIC.name().equals(mockType)) {
            ApiRecords apiRecords = new ApiRecords();
            apiRecords.setResponseBody(apiConfig.getStaticResponse());
            return apiRecords;
        }
        //动态响应，就是执行一段脚本
        if (MockType.DYNAMIC.name().equals(mockType)) {
            //todo   
        }
        System.err.println("[dome agent] 未支持的mock类型: " + mockType);
        return null;
    }
}
