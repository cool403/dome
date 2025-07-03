# Dome项目

## 项目简介
Dome是一个基于Java字节码插桩技术的HTTP请求监控和Mock框架。它能够拦截并监控应用程序中的HTTP请求，支持对请求进行模拟响应。

## 项目结构
- `dome-sdk`: 包含共享工具和接口的SDK模块
- `dome-core`: 核心实现模块，包含字节码插桩逻辑
- `dome-db`: 数据库相关模块，用于存储监控数据

## 主要功能
1. HTTP请求监控
   - 支持拦截Spring MVC、OkHttp、RestTemplate、Feign等HTTP客户端
   - 收集请求URL、方法、响应时间等指标
   - 支持自定义数据收集器

2. Mock功能
   - 支持对HTTP请求进行模拟响应
   - 可以配置不同的Mock规则
   - 支持按需开启/关闭Mock功能

## 前置条件
- JDK 1.8+
- Maven 3.6+

## 构建项目
```bash
mvn clean install
```

## 使用方法
1. 在目标应用中添加Dome Agent
```bash
java -javaagent:path/to/dome-core.jar=debug=true,interval=5000 -jar your-app.jar
```

2. 配置参数说明
   - `debug`: 是否开启调试模式
   - `interval`: 数据上报间隔（毫秒）
   - `collectorUrl`: 数据收集器地址

## 数据收集结构
当配置了自定义数据收集器地址时，Dome会将监控数据以JSON格式上报。上报数据结构如下：

```json
{
    "traceId": "唯一追踪ID",
    "metricTime": "监控时间戳",
    "reqTime": "请求开始时间",
    "respTime": "响应结束时间",
    "httpMethod": "HTTP请求方法",
    "httpStatus": "HTTP响应状态码",
    "httpUrl": "请求URL",
    "queryParams": "查询参数",
    "requestBody": "请求体内容",
    "responseBody": "响应体内容",
    "apiType": "API类型（INT/EXT）"
}
```

### 字段说明
- `traceId`: 唯一追踪ID，用于关联请求链路
- `metricTime`: 监控时间戳，毫秒级时间戳
- `reqTime`: 请求开始时间，毫秒级时间戳
- `respTime`: 响应结束时间，毫秒级时间戳
- `httpMethod`: HTTP请求方法（GET/POST/PUT/DELETE等）
- `httpStatus`: HTTP响应状态码
- `httpUrl`: 完整的请求URL
- `queryParams`: URL中的查询参数
- `requestBody`: 请求体内容（JSON格式）
- `responseBody`: 响应体内容（JSON格式）
- `apiType`: API类型，取值为：
  - `INT`: 内部API
  - `EXT`: 外部API

### 上报方式
- HTTP POST请求
- JSON格式请求体
- 请求超时时间：150毫秒

### 示例
```json
{
    "traceId": "123456789",
    "metricTime": 1688352606000,
    "reqTime": 1688352606000,
    "respTime": 1688352606500,
    "httpMethod": "POST",
    "httpStatus": "200",
    "httpUrl": "http://example.com/api/user",
    "queryParams": "id=123",
    "requestBody": "{\"name\":\"test\",\"age\":20}",
    "responseBody": "{\"code\":\"ok\",\"data\":{}}",
    "apiType": "EXT"
}
```

### 注意事项
1. 所有字符串内容会进行必要的转义处理
2. 时间戳均为毫秒级
3. 请求体和响应体内容会保持原始JSON格式
4. 数据上报为异步操作，不影响主业务流程

## 依赖信息
### 主要依赖
1. **字节码操作**
   - `byte-buddy`: 1.10.22
     - 用于实现字节码插桩功能

2. **JSON处理**
   - `gson`: 2.13.1
     - 用于JSON数据的序列化和反序列化

3. **数据库**
   - `jdbi3-core`: 3.39.1
     - SQL查询构建和执行工具
   - `sqlite-jdbc`: 3.50.1.0
     - SQLite数据库驱动

4. **构建工具**
   - `maven-assembly-plugin`: 3.3.0
     - 用于打包包含依赖的JAR文件
   - `maven-shade-plugin`: 3.3.0
     - 用于处理依赖冲突和重定位类

### 依赖管理
项目使用Maven进行依赖管理，所有模块的依赖版本都由父POM统一管理，确保版本一致性。
