# TimeWheel - 基于时间轮算法的定时任务调度系统

## 个人简介
前某大型保险公司、大型金融服务机构系统开发工程师,架构岗位, 5年开发经验、5年架构经验, 私人邮箱342829672@qq.com,目前上海在职，欢迎互相交流经验。

擅长服务日志治理、服务追踪链路设计、高性能订阅服务、starter sdk 开发等。

## 项目简介

TimeWheel 是一个基于时间轮（Timing Wheel）算法实现的高性能定时任务调度系统，专为处理大量定时任务而设计。该项目借鉴了 Kafka 中时间轮的实现思想，提供了高效、可靠的任务调度能力。

系统支持个人订阅服务，用户可以创建、管理各种定时任务，如邮件订阅等。通过时间轮算法，系统能够高效处理大量定时任务，避免了传统定时任务调度器在任务量大时的性能瓶颈问题。

该项目为本人设计、开发、和部署，稳定运行2年, 目前订阅量约为34万条, 按秒级推送定时任务。

已对关键企业级业务内容做了脱敏处理,目前您看到的是脱离业务信息的纯技术框架。

## 核心特性

- **高性能时间轮算法**：基于 Kafka 时间轮实现，支持大量定时任务的高效调度
- **灵活的任务管理**：支持任务的创建、更新、删除和查询
- **邮件订阅服务**：内置邮件订阅功能，支持个性化邮件模板
- **多种调度方式**：支持 Cron 表达式和固定时间间隔的任务调度
- **双模式支持**：支持时间轮模式和延迟队列模式
- **分布式支持**：可扩展为分布式任务调度系统
- **监控集成**：集成了 Spring Boot Actuator，提供系统监控能力

## 技术栈

- **核心框架**：Spring Boot 2.0.7
- **数据库**：MySQL 8.0
- **持久层**：MyBatis
- **连接池**：Druid
- **模板引擎**：Thymeleaf
- **JSON处理**：Fastjson
- **定时任务**：自实现时间轮算法
- **监控**：Spring Boot Actuator + Micrometer

## 系统架构

```
┌─────────────────┐    ┌──────────────────┐    ┌──────────────────┐
│   Controller    │    │   Service        │    │   Time Wheel     │
│  (任务管理接口)  │───▶│  (业务逻辑处理)   │───▶│  (任务调度核心)   │
└─────────────────┘    └──────────────────┘    └──────────────────┘
                              │                         │
                              ▼                         ▼
                      ┌───────────────┐      ┌──────────────────┐
                      │   Database    │      │   Delay Queue    │
                      │   (MySQL)     │      │  (延迟任务队列)   │
                      └───────────────┘      └──────────────────┘
```

## 核心组件

1. **时间轮核心**：
   - [TimingWheel](src/main/java/com/riven/common/time/kafka/TimingWheel.java)：时间轮实现
   - [SystemTimer](src/main/java/com/riven/common/time/kafka/SystemTimer.java)：系统定时器
   - [TimerTaskList](src/main/java/com/riven/common/time/kafka/TimerTaskList.java)：任务列表
   - [TimerTaskEntry](src/main/java/com/riven/common/time/kafka/TimerTaskEntry.java)：任务条目

2. **任务管理**：
   - [TimeWheelTaskManager](src/main/java/com/riven/common/context/TimeWheelTaskManager.java)：时间轮任务管理器
   - [DelayQueueTaskManager](src/main/java/com/riven/common/context/DelayQueueTaskManager.java)：延迟队列任务管理器

3. **业务实体**：
   - [SubscriptionInfo](src/main/java/com/riven/common/entity/SubscriptionInfo.java)：订阅信息
   - [WheelTask](src/main/java/com/riven/common/entity/WheelTask.java)：时间轮任务
   - [SendMail](src/main/java/com/riven/common/job/SendMail.java)：邮件发送任务

## 时间轮模式 vs 延迟队列模式

### 时间轮模式 (Time Wheel)

时间轮算法是一种高效的定时任务调度算法，特别适合处理大量定时任务的场景。它通过将时间抽象成一个环形结构，每个槽位代表一个时间间隔，任务被放置在对应的时间槽中。

**优势：**
1. **高性能**：插入和取消操作的时间复杂度为O(1)
2. **内存效率**：相比为每个任务创建定时器，时间轮大大减少了内存占用
3. **时间精度可控**：通过调整tickMs和wheelSize参数可以控制时间精度
4. **适合大量任务**：能够高效处理大量定时任务

**适用场景：**
- 需要处理大量定时任务的场景
- 对任务调度性能有较高要求的系统
- 任务执行时间相对集中的场景

### 延迟队列模式 (Delay Queue)

延迟队列模式基于Java的DelayQueue实现，每个任务都有一个延迟时间，只有当延迟时间到达后任务才会被取出执行。

**优势：**
1. **实现简单**：基于JDK提供的DelayQueue实现，逻辑清晰
2. **精确执行**：任务会在精确的延迟时间后执行
3. **资源消耗低**：对于任务量不大的场景，资源消耗较少

**适用场景：**
- 任务量相对较少的场景
- 对任务执行时间精度要求较高的场景
- 简单的延迟任务处理需求

### 模式切换

系统支持在两种模式之间切换，通过配置文件中的`start.type`参数控制：

```yaml
start:
  type: time_wheel # 或 delay_queue
```

## CRUD 操作详解

### 创建任务 (Create)

通过`POST /admin/task/add`接口创建新任务：

```json
{
  "userId": 1001,
  "referType": "personal",
  "sendType": 1,
  "subscriptionType": "email",
  "specDateCron": "09:00:00",
  "toEmail": "user@example.com",
  "personalParam": "个性化参数"
}
```

系统会根据配置的调度模式（时间轮或延迟队列）将任务添加到相应的任务管理器中。

### 查询任务 (Read)

通过`POST /admin/task/select`接口查询任务：

```json
{
  "taskId": 123,
  "userId": 1001
}
```

可以根据taskId或userId查询任务信息。

### 更新任务 (Update)

通过`POST /admin/task/update`接口更新任务：

```json
{
  "taskId": 123,
  "userId": 1001,
  "specDateCron": "10:00:00",
  "toEmail": "newuser@example.com"
}
```

更新任务时，系统会先取消原有任务，再创建新任务。

### 删除任务 (Delete)

通过`POST /admin/task/delete`接口删除任务：

```json
{
  "taskId": 123,
  "userId": 1001
}
```

删除任务时，系统会从任务管理器中移除对应的任务。

## 系统设计分析

### 时间轮实现

时间轮实现参考了Kafka的设计，包含以下核心组件：

1. **TimingWheel**：时间轮主体，包含多个时间槽
2. **TimerTaskList**：时间槽中的任务列表，实现Delayed接口
3. **TimerTaskEntry**：任务条目，包装具体的任务
4. **SystemTimer**：系统定时器，负责推进时间轮

时间轮采用分层设计，当任务的延迟时间超出当前层的表示范围时，会自动创建上层时间轮。

### 任务管理

系统提供两种任务管理器：
1. **TimeWheelTaskManager**：管理时间轮模式下的任务
2. **DelayQueueTaskManager**：管理延迟队列模式下的任务

两种管理器都使用ConcurrentHashMap存储任务，支持并发访问。

### 线程池设计

系统使用自定义的线程池来执行任务，通过[MdcAwareRunnable](src/main/java/com/riven/common/config/MdcAwareRunnable.java)包装任务，确保日志追踪ID的传递。

### 容错机制

系统实现了重试机制，通过[RetryService](src/main/java/com/riven/common/service/RetryService.java)和[RetryPolicy](src/main/java/com/riven/common/config/RetryPolicy.java)提供可配置的重试策略。

## 分布式部署方案

### 单体部署

在单体部署模式下，所有组件运行在同一个JVM进程中，适用于小型应用或测试环境。

### 分布式部署

在分布式部署中，可以通过以下方式扩展系统：

1. **数据库共享**：
   - 多个应用实例共享同一个数据库
   - 通过`client_id`字段区分不同实例的数据
   - 使用数据库行锁保证任务不会被重复执行

2. **任务分片**：
   - 根据任务属性（如用户ID）进行分片
   - 不同的应用实例负责不同分片的任务
   - 减少任务冲突和重复执行

3. **协调机制**：
   - 使用分布式锁（如Redis、Zookeeper）协调任务执行
   - 确保同一任务在分布式环境中只被执行一次

4. **负载均衡**：
   - 使用负载均衡器分发API请求
   - 通过一致性哈希等算法保证请求的均匀分布

### 高可用方案

1. **应用实例冗余**：
   - 部署多个应用实例
   - 使用健康检查机制自动剔除故障实例

2. **数据库主从复制**：
   - 配置MySQL主从复制
   - 读写分离提高数据库性能

3. **消息队列集成**：
   - 集成消息队列（如RabbitMQ、Kafka）
   - 异步处理任务执行结果

### 监控和运维

系统集成了Spring Boot Actuator，提供健康检查和指标监控：
- `/actuator/health` - 健康检查
- `/actuator/info` - 应用信息
- `/actuator/metrics` - 系统指标

## 快速开始

### 环境要求

- Java 8+
- MySQL 5.7+
- Maven 3.6+

### 配置步骤

1. 克隆项目到本地：
```bash
git clone <repository-url>
```

2. 创建数据库：
```sql
CREATE DATABASE personal_subscription CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

3. 执行数据库脚本：
```bash
# 执行 src/main/resources/create.sql
```

4. 修改数据库配置：
编辑 `src/main/resources/application-dev.yml` 文件：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/personal_subscription?useSSL=false&serverTimezone=Asia/Shanghai
    username: your_username
    password: your_password
```

5. 启动项目：
```bash
mvn spring-boot:run
```

6. 访问应用：
```
http://localhost:12344
```

## API 接口

### 任务管理接口

- `POST /admin/task/add` - 添加任务
- `POST /admin/task/update` - 更新任务
- `POST /admin/task/delete` - 删除任务
- `POST /admin/task/select` - 查询任务

## 监控端点

系统集成了 Spring Boot Actuator，提供以下监控端点：

- `/actuator/health` - 健康检查
- `/actuator/info` - 应用信息
- `/actuator/metrics` - 系统指标

## 贡献指南

欢迎提交 Issue 和 Pull Request 来帮助改进项目。

## 许可证

本项目采用 Apache License 2.0 许可证。
