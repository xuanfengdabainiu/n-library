# 📚 智能图书馆管理系统 (AI-Powered Library System)

本项目是一个前后端分离 + AI 视觉引擎的综合微服务项目。

## 🏗️ 项目架构 (Monorepo)
- `library/` : 后端核心服务 (Spring Boot + Gradle + MySQL)
- `python/` : AI 视觉引擎 (人脸识别 / 目标检测)
- `前端静态文件` : 放置于后端的 `resources/static` 目录下

## 🚀 核心技术栈
- **后端**: Java 17, Spring Boot 3, JPA, Hibernate
- **AI 引擎**: Python, YOLOv8, DeepFace
- **前端**: HTML5, CSS3, 原生 JavaScript
- **部署**: Docker, 阿里云 CentOS

## 🛠️ 如何在本地运行

### 1. 启动 Java 后端
1. 进入 `library` 目录。
2. 使用 IDEA 打开 `build.gradle` 并刷新依赖。
3. 配置好本地的 MySQL 数据库连接。
4. 运行 `LibraryApplication.java` (默认端口: 8080)。

### 2. 启动 Python AI 引擎
1. 进入 `python` 目录。
2. 安装依赖: `pip install -r requirements.txt`
3. 运行服务: `python server.py` (默认端口: 5000)。