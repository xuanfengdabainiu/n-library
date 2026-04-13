# 📚 POP! Library - 智能波普风社交图书馆

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.0+-brightgreen.svg)
![Java](https://img.shields.io/badge/Java-17-blue.svg)
![Python](https://img.shields.io/badge/Python-3.9+-yellow.svg)
![MySQL](https://img.shields.io/badge/MySQL-8.0-orange.svg)
![Docker](https://img.shields.io/badge/Docker-Ready-blue.svg)

> **"不只是一座图书馆，更是一个带电波的波普艺术社交圈！"**

`POP! Library` 是一个结合了 **AI 视觉识别**与 **全双工 WebSocket 通讯** 的现代化微服务项目。本项目采用极其抓人眼球的**美式复古波普艺术 (Pop Art) UI 设计**，打破传统图书管理系统的沉闷，为用户提供沉浸式、游戏化的“Read-to-Earn (阅读赚币)”社交阅读体验。

---

## ✨ 核心特色功能 (Key Features)

### 🎭 极客与艺术的碰撞 (Pop Art UI)
- **狂野前端体验**：高饱和度色彩、美漫对讲机风格、3D 卡片翻转、爆炸与震动特效。
- **复古打字机密码锁**：模拟老式打字机输入交互，打造极致的用户沉浸感。
- **全方位防线**：前端绝对冻结拦截 + 后端跨域白名单，安全感拉满。

### 🤖 AI 人脸识别身份验证 (Face-ID Auth)
- **无感登录**：集成 Python 视觉引擎 (DeepFace)，用户只需面对镜头即可秒级解锁账户。
- **动态绑卡**：新用户扫描人脸注册，自动与 MySQL 数据库中的特征码 (Face ID) 绑定。

### 💬 实时波普对讲机 (Live Chat & Economy)
- **全双工通讯**：基于 WebSocket (SockJS + STOMP) 构建的实时弹幕群聊。
- **Read-to-Earn 经济系统**：用户通过阅读书籍（获取 10 🪙 波普币），在聊天室发言每次扣除 10 🪙。
- **强一致性拦截**：如果余额不足，前端触发输入框疯狂震动与“破产弹窗”，后端原子级事务拦截。

### 👑 沉浸式管理员中控台 (Admin Dashboard)
- 提供最高权限管理：管理全站用户数据、图书库存、收编员工。
- **大厂级特效**：删除数据触发全屏 `BOOM!` 爆炸，模块切换采用“重力砸落”、“翻书”等史诗级动效。
- **实时热度排行 (Trending Top 3)**：基于真实用户收藏数据，动态生成书籍领奖台。

---

## 🏗️ 架构与技术栈 (Tech Stack)

本项目采用 **Monorepo** 架构，前后端分离，AI 引擎独立解耦。

### ☕ 后端核心服务 (Java)
- **核心框架**: Spring Boot 3.x, Spring Web
- **数据持久化**: Spring Data JPA, Hibernate
- **数据库**: MySQL 8.0.45
- **实时通讯**: Spring WebSocket, STOMP
- **构建工具**: Gradle

### 🐍 AI 视觉引擎 (Python)
- **框架**: Flask / FastAPI
- **计算机视觉**: OpenCV, DeepFace
- **交互方式**: 提供 RESTful API 供 Java 跨服务调用 (`/recognize`, `/register`)

### 🎨 前端交互 (Frontend)
- **核心**: HTML5, CSS3 (原生响应式 + 动画), Vanilla JavaScript
- **连接层**: SockJS, Stomp.js
- **UI 组件**: FontAwesome 6.4, Dicebear (生成复古头像)

### ☁️ 运维与部署 (DevOps)
- **容器化**: Docker
- **网关代理**: Nginx (支持 WSS 协议升级与反向代理)
- **安全传输**: Let's Encrypt SSL 证书 (HTTPS/WSS)

---

## 📂 目录结构 (Directory Structure)

```text
├── library/                 # ☕ Java Spring Boot 后端工程
│   ├── src/main/java/org/example/library/
│   │   ├── config/          # WebSocket 与 CORS 跨域安全配置
│   │   ├── controller/      # API 路由控制器 (Chat, Login, User, Admin)
│   │   ├── entity/          # JPA 实体类映射 (User, Book, ChatMessage等)
│   │   └── repository/      # 数据访问层接口 (CRUD)
│   ├── src/main/resources/  # 配置文件 (application-dev / prod)
│   └── build.gradle         # Gradle 依赖配置
├── python/                  # 🐍 Python AI 人脸识别微服务
│   ├── server.py            # AI 引擎入口
│   └── requirements.txt     # Python 依赖清单
├── static/                  # 🎨 前端页面源文件 (HTML/CSS/JS)
│   ├── index.html, chat.html, admin-index.html ...
└── pop_library.sql          # 💾 MySQL 数据库结构与初始数据备份
```

---

## 🚀 如何在本地运行 (Quick Start)

### 1. 初始化数据库
在本地 MySQL (端口 3306) 中创建名为 `pop_library` 的数据库。

### 2. 启动 Python AI 引擎
```bash
cd python
pip install -r requirements.txt
python server.py  # 默认运行在 5000 端口
```

### 3. 启动 Java 后端
使用 IDEA / Eclipse 打开 `library` 文件夹：
1. 刷新 Gradle 依赖。
2. 在 `application-dev.properties` 中配置你的本地数据库账号密码。
3. 运行 `LibraryApplication.java`。
4. 在浏览器打开 `http://localhost:8080/login.html` 即可开始体验！

---

## 🌐 线上服务器部署指南 (Docker + Nginx)

项目已完全支持容器化，可通过 Docker 部署至云服务器（如 Aliyun / AWS）。

**1. 构建 Java 生产镜像:**
```bash
cd library
./gradlew bootJar
sudo docker build -t pop_project_java-backend:v1.0 .
```

**2. 启动容器 (开启 Prod 生产环境保护):**
```bash
sudo docker run -d \
  --name pop_java_system \
  -p 8080:8080 \
  --network pop_project_default \
  -v /home/ubuntu/pop_project/python_app/users:/app/users \
  pop_project_java-backend:v1.0 \
  java -jar app.jar --spring.profiles.active=prod
```

**3. 配置 Nginx 以支持 WebSocket 穿透:**
在服务器的 Nginx `location /` 配置块中添加以下指令，以支持长连接协议升级：
```nginx
proxy_http_version 1.1;
proxy_set_header Upgrade $http_upgrade;
proxy_set_header Connection "upgrade";
```

---

## 📜 许可证 (License)
本项目采用 [MIT License](LICENSE) 开源。欢迎 Fork 和 PR！如果你觉得这个项目酷毙了，请给我点个 ⭐️ **Star**！