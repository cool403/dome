package com.lifelover.dome.db.core;

public final class CreateTable {
    private CreateTable(){

    }


    public static final String INIT_SQL = "-- 请求记录表（存储每笔实际请求信息）\n" +
            "CREATE TABLE if not exists api_records (\n" +
            "    id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
            "    http_url TEXT NOT NULL,\n" +
            "    http_method TEXT NOT NULL CHECK (http_method IN ('GET', 'POST', 'PUT', 'DELETE', 'PATCH', 'HEAD', 'OPTIONS')),\n" +
            "    query_params text,\n" +
            "    request_body TEXT,\n" +
            "    response_body TEXT,\n" +
            "    trace_id TEXT,\n" +
            "    req_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,\n" +
            "    res_time TIMESTAMP,\n" +
            "    http_status INTEGER,\n" +
            "    headers TEXT,  -- 可以存储JSON格式的请求/响应头\n" +
            "    response_headers TEXT,\n" +
            "    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP\n" +
            ");\n" +
            "\n" +
            "\n" +
            "-- 接口配置表（定义每个接口的mock行为）\n" +
            "CREATE TABLE if not exists api_configs (\n" +
            "    id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
            "    http_url TEXT NOT NULL,\n" +
            "    http_method TEXT NOT NULL CHECK (http_method IN ('GET', 'POST', 'PUT', 'DELETE', 'PATCH', 'HEAD', 'OPTIONS')),\n" +
            "    is_mock_enabled BOOLEAN NOT NULL DEFAULT 0,  -- 0表示禁用，1表示启用\n" +
            "    mock_type TEXT CHECK (mock_type IN ('REPLAY', 'STATIC', 'DYNAMIC', 'PROXY')),  -- 流量重放、静态响应、动态规则、代理到真实服务\n" +
            "    static_response TEXT,  -- 当mock_type为STATIC时使用\n" +
            "    dynamic_rule TEXT,  -- 当mock_type为DYNAMIC时使用，可存储JSON格式规则\n" +
            "    replay_record_id INTEGER,  -- 当mock_type为REPLAY时，关联到request_records表的id\n" +
            "    delay INTEGER DEFAULT 0,  -- 模拟延迟，毫秒\n" +
            "    description TEXT,  -- 接口描述\n" +
            "    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,\n" +
            "    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,\n" +
            "    UNIQUE (http_url, http_method)  -- 确保URL+Method组合唯一\n" +
            ");";
}
