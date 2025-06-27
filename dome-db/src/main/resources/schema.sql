-- 请求记录表（存储每笔实际请求信息）
CREATE TABLE api_records (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    http_url TEXT NOT NULL,
    http_method TEXT NOT NULL CHECK (http_method IN ('GET', 'POST', 'PUT', 'DELETE', 'PATCH', 'HEAD', 'OPTIONS')),
    query_params text,
    request_body TEXT,
    response_body TEXT,
    trace_id TEXT NOT NULL,
    req_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    res_time TIMESTAMP,
    http_status INTEGER,
    headers TEXT,  -- 可以存储JSON格式的请求/响应头
    response_headers TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);


-- 接口配置表（定义每个接口的mock行为）
CREATE TABLE api_configs (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    http_url TEXT NOT NULL,
    http_method TEXT NOT NULL CHECK (http_method IN ('GET', 'POST', 'PUT', 'DELETE', 'PATCH', 'HEAD', 'OPTIONS')),
    is_mock_enabled BOOLEAN NOT NULL DEFAULT 0,  -- 0表示禁用，1表示启用
    mock_type TEXT NOT NULL CHECK (mock_type IN ('REPLAY', 'STATIC', 'DYNAMIC', 'PROXY')),  -- 流量重放、静态响应、动态规则、代理到真实服务
    static_response TEXT,  -- 当mock_type为STATIC时使用
    dynamic_rule TEXT,  -- 当mock_type为DYNAMIC时使用，可存储JSON格式规则
    replay_record_id INTEGER,  -- 当mock_type为REPLAY时，关联到request_records表的id
    delay INTEGER DEFAULT 0,  -- 模拟延迟，毫秒
    description TEXT,  -- 接口描述
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (http_url, http_method)  -- 确保URL+Method组合唯一
);