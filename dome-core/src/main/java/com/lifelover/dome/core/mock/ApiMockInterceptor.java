package com.lifelover.dome.core.mock;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import com.lifelover.dome.core.config.ConfigLoader;
import com.lifelover.dome.core.helpers.JsonUtil;
import com.lifelover.dome.db.core.DbAccess;
import com.lifelover.dome.db.entity.ApiConfigs;
import com.lifelover.dome.db.entity.ApiRecords;
import com.lifelover.dome.db.helper.ApiType;
import com.lifelover.dome.db.helper.MockType;

public class ApiMockInterceptor {

    private static ScriptEngine scriptEngine = null;

    static {
        try {
            // 这里一定要加try catch，否则会报错，jdk11开始nashorn被移除
            scriptEngine = new ScriptEngineManager().getEngineByName("nashorn");
        } catch (Exception e) {
            System.err.println("[dome agent] 初始化scriptEngine失败, 确认jdk版本是否大于11");
            e.printStackTrace();
        }
    }

    private ApiMockInterceptor() {

    }

    /**
     * 模拟接口
     * 
     * @param apiMockContext 参数
     * @return 模拟报文
     */
    public static ApiRecords mock(ApiMockContext apiMockContext) {
        // 只有contentype是json的才进行mock
        String contentType = apiMockContext.getContentType();
        if (contentType != null && !contentType.toLowerCase().contains("json")) {
            System.err.println("[dome agent] 目前只有contentType是application/Json才支持mock, 当前contentType: " + contentType);
            return null;
        }
        // 根据httpUrl 和httpMethod获取api信息
        String httpUrl = apiMockContext.getHttpUrl();
        String httpMethod = apiMockContext.getHttpMethod();
        DbAccess dbAccess = ConfigLoader.getAgentConfig().getDbAccess();
        if (dbAccess == null) {
            return null;
        }
        ApiConfigs apiConfig = dbAccess.getApiConfig(httpUrl, httpMethod, ApiType.of(apiMockContext.getApiType()));
        // 没配置api信息的也不做mock处理
        if (apiConfig == null) {
            System.err.println("[dome agent] 没有找到对应的api信息, 不进行mock");
            return null;
        }
        String isMockEnabled = apiConfig.getIsMockEnabled();
        if (YesOrNo.N.equals(isMockEnabled)) {
            System.out.println("[dome agent] 当前接口未开启mock");
            return null;
        }
        String mockType = apiConfig.getMockType();
        // 判断是否是REPLAY
        if (MockType.REPLAY.name().equals(mockType)) {
            return apiConfig.getReplayApiRecords();
        }
        // 静态响应
        if (MockType.STATIC.name().equals(mockType)) {
            ApiRecords apiRecords = new ApiRecords();
            apiRecords.setResponseBody(apiConfig.getStaticResponse());
            return apiRecords;
        }
        // 动态响应，就是执行一段脚本
        if (MockType.DYNAMIC.name().equals(mockType) && scriptEngine != null) {
            ApiRecords apiRecords = new ApiRecords();
            String dynamicResponse = apiConfig.getDynamicRule();
            try {
                // 设置上下文
                scriptEngine.put("params", apiMockContext);
                // js脚本返回的应该是一个js对象{}
                Object result = scriptEngine.eval(dynamicResponse);
                // 默认返回json
                apiRecords.setResponseBody(JsonUtil.toJson(result));
            } catch (Exception e) {
                System.err.println("[dome agent] 执行动态脚本失败: " + e.getMessage());
                e.printStackTrace();
            }
            return apiRecords;
        }
        System.err.println("[dome agent] 未支持的mock类型: " + mockType);
        return null;
    }
}
