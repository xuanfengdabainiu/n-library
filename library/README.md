📚 智能图书管理与交流系统 (Personal Book System)
LanguageFramework

本项目是一套结合了 Java 后端架构与 Python 计算机视觉的智能化图书管理系统。旨在通过技术手段提升个人图书收藏的数字化管理体验，并加入 AI 识别功能。

🛠 项目架构
本项目采用多语言协同开发模式：

library/ (Java): 核心业务逻辑层。负责用户权限、书籍信息维护、借阅/交流逻辑以及网页接口对接。
Python 模块 (main.py, server.py): AI 增强层。集成了人脸识别/图像处理（Face1/YOLO）算法，用于书籍封面自动识别或用户身份验证。
🚀 核心功能
 书籍云端管理：基于 Java 实现的高效 CRUD 操作。
 AI 识别模块：Python 后端提供基于 YOLO/人脸识别的 API 接口。
📦 快速开始
1. Java 环境配置
JDK 1.8+
Maven 3.6+
运行：cd library && mvn spring-boot:run
2. Python 环境配置
Python 3.8+
依赖：pip install -r requirements.txt
运行：python server.py
🤝 开发者
牛哥 (xuanfengdabainiu)
注：本项目为毕业设计作品，持续优化中。
