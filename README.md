# Dome项目

## 项目简介
Dome是一个基于Java字节码插桩技术的HTTP请求监控和Mock框架。它能够拦截并监控应用程序中的HTTP请求，支持对请求进行模拟响应。

## 项目结构
- `dome-sdk`: 包含共享工具和接口的SDK模块
- `dome-core`: 核心实现模块，包含字节码插桩逻辑
- `dome-db`: 数据库相关模块，用于存储监控数据
- `dome-sdk`: SDK模块，提供基础接口和工具

## 主要功能
1. HTTP请求监控
   - 支持拦截Spring MVC、OkHttp、RestTemplate、Feign等HTTP客户端
   - 收集请求URL、方法、响应时间等指标
   - 支持自定义数据收集器

2. Mock功能
   - 支持对HTTP请求进行模拟响应
   - 可以配置不同的Mock规则
   - 支持按需开启/关闭Mock功能

3. 数据收集与上报
   - 支持将监控数据上报到指定收集器
   - 可配置上报地址和超时时间
   - 支持自定义数据格式

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

## 项目模块
1. **dome-sdk**: 包含核心接口和工具的基础SDK
   - 提供基础工具类
   - 定义插件接口
   - 提供数据模型

2. **dome-core**: 核心实现模块
   - 字节码插桩逻辑
   - 插件管理
   - 数据收集与上报

3. **dome-db**: 数据库模块
   - 数据存储
   - 数据查询
   - 数据库初始化

4. **dome-sdk**: SDK模块
   - 提供API接口
   - 工具类
   - 配置管理
