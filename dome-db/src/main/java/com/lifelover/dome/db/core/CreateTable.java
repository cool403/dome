package com.lifelover.dome.db.core;

public final class CreateTable {
    private CreateTable(){

    }


    public static final String INIT_SQL = "-- 请求记录表（存储每笔实际请求信息）\r\n" + //
                "CREATE TABLE if not exists api_records (\r\n" + //
                "    id INTEGER PRIMARY KEY AUTOINCREMENT,\r\n" + //
                "    http_url TEXT NOT NULL,\r\n" + //
                "    http_method TEXT NOT NULL CHECK (http_method IN ('GET', 'POST', 'PUT', 'DELETE', 'PATCH', 'HEAD', 'OPTIONS')),\r\n" + //
                "    query_params text,\r\n" + //
                "    request_body TEXT,\r\n" + //
                "    response_body TEXT,\r\n" + //
                "    trace_id TEXT,\r\n" + //
                "    req_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,\r\n" + //
                "    res_time TIMESTAMP,\r\n" + //
                "    http_status TEXT,\r\n" + //
                "    headers TEXT,  -- 可以存储JSON格式的请求/响应头\r\n" + //
                "    response_headers TEXT,\r\n" + //
                "    api_type text CHECK (api_type IN ('INT', 'EXT')),\r\n" + //
                "    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP\r\n" + //
                ");\r\n" + //
                "\r\n" + //
                "\r\n" + //
                "-- 接口配置表（定义每个接口的mock行为）\r\n" + //
                "CREATE TABLE if not exists api_configs (\r\n" + //
                "    id INTEGER PRIMARY KEY AUTOINCREMENT,\r\n" + //
                "    http_url TEXT NOT NULL,\r\n" + //
                "    host TEXT,\r\n" + //
                "    http_method TEXT NOT NULL CHECK (http_method IN ('GET', 'POST', 'PUT', 'DELETE', 'PATCH', 'HEAD', 'OPTIONS')),\r\n" + //
                "    is_mock_enabled BOOLEAN NOT NULL DEFAULT 0,  -- 0表示禁用，1表示启用\r\n" + //
                "    mock_type TEXT CHECK (mock_type IN ('REPLAY', 'STATIC', 'DYNAMIC', 'PROXY')),  -- 流量重放、静态响应、动态规则、代理到真实服务\r\n" + //
                "    static_response TEXT,  -- 当mock_type为STATIC时使用\r\n" + //
                "    dynamic_rule TEXT,  -- 当mock_type为DYNAMIC时使用，可存储JSON格式规则\r\n" + //
                "    replay_record_id INTEGER,  -- 当mock_type为REPLAY时，关联到request_records表的id\r\n" + //
                "    delay INTEGER DEFAULT 0,  -- 模拟延迟，毫秒\r\n" + //
                "    description TEXT,  -- 接口描述\r\n" + //
                "    api_type text CHECK (api_type IN ('INT', 'EXT')), --- INT服务本身提供的接口, EXT调用外部的接口\r\n" + //
                "    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,\r\n" + //
                "    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,\r\n" + //
                "    UNIQUE (http_url, http_method, api_type)  -- 确保URL+Method组合唯一\r\n" + //
                ");";
}
