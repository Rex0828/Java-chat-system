# CLAUDE.md

## 项目概览

**项目名称：** Java SWT 聊天系统（Java SWT Chat System）

**项目背景：** 本项目是大学《Java 网络编程》课程项目，旨在通过实践掌握 Java Socket 网络编程、SWT 图形界面开发、多线程并发处理等核心技术。项目从零开始，逐步构建一个功能完善的实时聊天系统。

**项目类型：** Java 桌面应用

## 技术栈

| 技术 | 版本 / 说明 |
|------|-------------|
| JDK | Java 21 |
| GUI 框架 | SWT (Standard Widget Toolkit) |
| 网络通信 | java.net.Socket (TCP) |
| 并发 | java.util.concurrent + 原生 Thread |
| 构建工具 | 手动编译 / IDE (Eclipse/IntelliJ IDEA) |
| 版本控制 | Git + GitHub |
| 编码 | UTF-8 |

### SWT 依赖说明

SWT 是 Eclipse 基金会维护的原生 GUI 工具包，需要引入平台对应的 JAR 包：
- Windows: `swt-4.x-win32-win32-x86_64.jar`
- 可通过 Maven 中央仓库或 Eclipse SWT 官网下载
- 运行时需指定 `-Djava.library.path` 指向 SWT 原生库

## 目录规划

```
Java-chat-system/
├── README.md                    # 项目说明
├── CLAUDE.md                    # 本文件 — AI 助手指南
├── .gitignore                   # Git 忽略规则
├── lib/                         # 第三方依赖 (SWT JAR 等)
│   └── swt.jar                  # SWT 库（按平台选择）
├── src/
│   ├── client/                  # 客户端模块
│   │   ├── ChatClient.java      # 客户端主入口
│   │   ├── ui/                  # SWT 界面层
│   │   │   ├── LoginWindow.java       # 登录窗口
│   │   │   ├── ChatWindow.java        # 聊天主窗口（单聊/群聊切换）
│   │   │   ├── UserListPanel.java     # 在线用户列表面板
│   │   │   └── MessageArea.java       # 消息显示区域组件
│   │   ├── network/             # 客户端网络层
│   │   │   ├── ServerConnection.java  # 与服务端的 TCP 连接管理
│   │   │   └── MessageHandler.java    # 客户端消息收发处理
│   │   └── model/               # 客户端数据模型
│   │       ├── User.java              # 用户信息模型
│   │       └── ChatMessage.java       # 消息模型
│   ├── server/                  # 服务端模块
│   │   ├── ChatServer.java      # 服务端主入口
│   │   ├── network/             # 服务端网络层
│   │   │   ├── ClientHandler.java     # 每个客户端的独立线程处理器
│   │   │   ├── ConnectionPool.java    # 连接池 / 在线用户管理
│   │   │   └── MessageRouter.java     # 消息路由（广播/私聊分发）
│   │   ├── service/             # 服务端业务层
│   │   │   ├── AuthService.java       # 用户认证（登录/注册）
│   │   │   └── ChatService.java       # 聊天业务逻辑
│   │   └── model/               # 服务端数据模型
│   │       ├── User.java              # 用户模型（含密码/状态）
│   │       └── ChatMessage.java       # 消息模型
│   └── common/                  # 公共模块（客户端与服务端共享）
│       ├── protocol/            # 通信协议定义
│       │   ├── MessageType.java       # 消息类型枚举
│       │   ├── Request.java           # 请求封装
│       │   └── Response.java          # 响应封装
│       └── util/                # 公共工具类
│           ├── IOUtils.java           # IO 流工具
│           └── Constants.java         # 全局常量（端口、缓冲区大小等）
├── docs/                        # 文档
│   ├── protocol.md              # 通信协议设计文档
│   └── architecture.md          # 架构设计文档
└── test/                        # 测试
    ├── client/
    └── server/
```

## 通信协议设计（核心约定）

系统基于 TCP 长连接，使用 JSON 格式序列化消息。每条消息包含以下基本字段：

```json
{
  "type": "LOGIN|LOGOUT|CHAT|BROADCAST|PRIVATE|USER_LIST|ERROR",
  "sender": "用户名",
  "receiver": "目标用户名（私聊时必填，广播时为空）",
  "content": "消息正文",
  "timestamp": 1700000000000
}
```

### 消息类型说明

| 类型 | 方向 | 说明 |
|------|------|------|
| `LOGIN` | C → S | 客户端登录请求 |
| `LOGIN_ACK` | S → C | 服务端登录应答 |
| `LOGOUT` | C → S | 客户端登出通知 |
| `CHAT` | C ↔ S | 公共聊天消息 |
| `BROADCAST` | S → C | 服务端广播消息 |
| `PRIVATE` | C → S → C | 私聊消息（服务端转发） |
| `USER_LIST` | S → C | 在线用户列表推送 |
| `ERROR` | S → C | 错误信息 |

## 开发路线

### 第一阶段：登录系统 (Phase 1)

**目标：** 实现用户登录界面和服务端认证。

- [ ] 搭建项目目录结构，配置 SWT 依赖
- [ ] 实现 `LoginWindow` — 登录界面（用户名 + 密码）
- [ ] 实现 `ChatServer` — 服务端启动监听
- [ ] 实现 `ServerConnection` — 客户端 TCP 连接
- [ ] 实现 `AuthService` — 服务端用户验证逻辑
- [ ] 定义 `LOGIN` / `LOGIN_ACK` / `ERROR` 消息协议
- [ ] 测试：客户端连接服务端、登录成功/失败反馈

**关键点：**
- 初次运行时使用硬编码用户或简单文件存储
- 每步完成后进行功能测试，不跨步

### 第二阶段：单用户聊天 (Phase 2)

**目标：** 单个客户端与服务端之间收发消息。

- [ ] 实现 `ChatWindow` — 聊天主窗口（消息输入框 + 发送按钮 + 消息显示区）
- [ ] 实现客户端 `MessageHandler` — 消息发送与接收线程
- [ ] 实现服务端 `ClientHandler` — 单客户端消息回显（Echo）
- [ ] 定义 `CHAT` 消息协议
- [ ] 测试：客户端发送消息 → 服务端接收并返回 → 客户端显示

**关键点：**
- 客户端接收消息需要独立线程（避免阻塞 UI）
- 这是"回声"阶段 — 服务端原样返回消息

### 第三阶段：多用户聊天 (Phase 3)

**目标：** 多个客户端同时连接，共享聊天室。

- [ ] 实现 `ConnectionPool` — 管理所有在线客户端连接
- [ ] 完善 `ClientHandler` — 每条连接一个线程
- [ ] 每个 ClientHandler 收到消息后转发给所有在线用户
- [ ] 实现 `UserListPanel` — 显示在线用户列表
- [ ] 定义 `USER_LIST` 消息协议，用户上下线时全员推送
- [ ] 测试：启动服务端 → 连接 3 个客户端 → 任一客户端发言，其余均可见

**关键点：**
- `ConnectionPool` 需要线程安全（使用 `ConcurrentHashMap`）
- 用户上下线时广播在线列表更新

### 第四阶段：广播消息 (Phase 4)

**目标：** 完善广播机制，支持系统通知和消息格式化。

- [ ] 实现 `MessageRouter` — 统一的消息路由分发
- [ ] 区分普通聊天消息与系统广播（如："张三 加入了聊天室"）
- [ ] 完善消息显示 — 发送者标识 + 时间戳
- [ ] 定义 `BROADCAST` 消息协议
- [ ] 实现 `ChatService` — 服务端聊天业务封装
- [ ] 测试：验证多人聊天、加入/离开通知、消息格式

**关键点：**
- 广播消息与普通消息使用不同颜色/样式区分
- `MessageRouter` 是后续私聊的基础

### 第五阶段：私聊系统 (Phase 5)

**目标：** 实现用户之间的点对点私聊。

- [ ] 完善 `ChatWindow` — 增加私聊窗口/面板切换
- [ ] 在 `UserListPanel` 中双击用户打开私聊窗口
- [ ] 实现 `MessageRouter` 的私聊路由 — 根据 `receiver` 字段定点转发
- [ ] 定义 `PRIVATE` 消息协议
- [ ] 私聊窗口独立显示，不影响公共聊天
- [ ] 测试：User A 与 User B 私聊，User C 看不到私聊内容

**关键点：**
- 私聊窗口生命周期管理（打开/关闭/多窗口）
- 服务端仅做转发，不存储私聊内容

## 代码规范

### 命名规范

| 类型 | 规范 | 示例 |
|------|------|------|
| 类名 | PascalCase | `ChatServer`, `LoginWindow` |
| 方法名 | camelCase | `sendMessage()`, `onUserLogin()` |
| 变量名 | camelCase | `clientSocket`, `onlineUsers` |
| 常量 | UPPER_SNAKE_CASE | `DEFAULT_PORT`, `MAX_USERS` |
| 包名 | 全小写 | `client.ui`, `server.network` |
| 枚举值 | UPPER_SNAKE_CASE | `LOGIN`, `BROADCAST` |

### 编码约定

1. **UTF-8 编码** — 所有源码文件使用 UTF-8，避免中文乱码
2. **线程安全** — 共享资源使用 `ConcurrentHashMap`、`AtomicInteger` 等线程安全类；UI 更新必须通过 `Display.asyncExec()`
3. **资源释放** — Socket、Stream、Reader/Writer 必须在 `finally` 块或 try-with-resources 中关闭
4. **异常处理** — 不允许吞掉异常（空 catch 块）；网络异常需记录日志并通知 UI
5. **日志** — 不使用 `System.out.println` 做调试，使用 `java.util.logging.Logger` 统一日志
6. **单文件行数** — 单个类不超过 500 行，超过则拆分类
7. **注释** — 所有 public 方法必须有 Javadoc；复杂逻辑需有行内注释
8. **SWT 资源** — Color、Font、Image 等 SWT 资源使用后必须 `dispose()`
9. **连接超时** — Socket 连接必须设置超时（`connect(SocketAddress, timeout)`），默认 5 秒
10. **心跳机制** — 客户端与服务端实现简单心跳（每 30 秒发送 PING/PONG），检测断线

### SWT 线程模型（重要）

SWT 有严格的 UI 线程限制：

```
所有 SWT 组件操作必须在 Display 线程（UI 线程）中执行
非 UI 线程更新界面必须使用：
  display.asyncExec(() -> { /* 更新 UI */ });
  display.syncExec(() -> { /* 更新 UI */ });
```

### Git 提交规范

```
<type>: <subject>

<body>
```

**Type 类型：**
- `feat` — 新功能
- `fix` — Bug 修复
- `refactor` — 重构
- `docs` — 文档更新
- `style` — 代码格式（不影响功能）
- `test` — 测试相关

**示例：**
```
feat: 实现用户登录窗口

- 添加 LoginWindow 类，包含用户名和密码输入框
- 连接 ChatServer 进行登录验证
- 登录成功后跳转到 ChatWindow
```

### .gitignore 建议

```gitignore
# 编译输出
bin/
out/
target/
*.class

# IDE
.idea/
*.iml
.settings/
.project
.classpath
.metadata/

# 系统文件
.DS_Store
Thumbs.db

# SWT 原生库（各平台需自行下载）
*.dll
*.so
*.dylib

# 日志
*.log
```

## 未来扩展方向

完成五个阶段后，可考虑以下扩展：

| 方向 | 说明 | 难度 |
|------|------|------|
| 文件传输 | 支持客户端之间发送文件（图片/文档） | ⭐⭐⭐ |
| 聊天记录持久化 | 使用 SQLite 存储聊天历史记录 | ⭐⭐ |
| 用户注册系统 | 完善注册功能，密码哈希存储 (bcrypt) | ⭐⭐ |
| GUI 美化 | 自定义 SWT 控件样式、表情包支持 | ⭐⭐ |
| NIO 重构 | 将 BIO Socket 模型升级为 Java NIO（非阻塞） | ⭐⭐⭐ |
| 群组聊天 | 支持创建/加入群组，群组消息独立路由 | ⭐⭐⭐ |
| 好友系统 | 添加好友、好友列表、在线状态 | ⭐⭐ |
| 消息加密 | 端到端加密（RSA + AES 混合加密） | ⭐⭐⭐⭐ |
| 语音聊天 | 基于 UDP 的实时语音传输 | ⭐⭐⭐⭐⭐ |
| 服务端管理面板 | Web 管理端 — 在线用户监控、消息统计 | ⭐⭐⭐⭐ |

## 开发环境配置

### 最低要求

- JDK 21+
- Eclipse IDE（推荐，SWT 支持最好）或 IntelliJ IDEA
- Git 2.x

### 快速启动步骤

1. 克隆仓库
2. 下载对应平台的 SWT JAR 放入 `lib/` 目录
3. 在 IDE 中配置 SWT JAR 到 Build Path
4. 先启动 `ChatServer`（默认端口 8888）
5. 再启动 `ChatClient` 实例（可启动多个模拟多用户）
6. 在登录界面输入用户名登录

### 运行参数

```
服务端 JVM 参数：无需特殊参数
客户端 JVM 参数：-Djava.library.path=./lib
```

## 注意事项

1. **本课程项目的核心是掌握网络编程原理**，不追求生产级性能，但追求代码清晰、结构合理
2. **每个阶段完成后打 Git Tag**（如 `v1.0-login`、`v2.0-single-chat`），便于回顾和回滚
3. **服务端与客户端同时开发时，先定协议再编码**，避免两端不兼容
4. **测试时在同一台机器上启动多个客户端**，使用不同端口连接即可
5. **遇到 SWT 问题先查 Eclipse SWT Snippets**（官方代码片段库），常见问题都有现成解决方案
