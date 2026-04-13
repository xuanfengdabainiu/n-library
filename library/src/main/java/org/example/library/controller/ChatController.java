package org.example.library.controller;

import org.example.library.entity.ChatMessage;
import org.example.library.entity.User;
import org.example.library.repository.ChatMessageRepository;
import org.example.library.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Controller
@CrossOrigin(originPatterns = "${cors.allowed.origin-patterns}", allowCredentials = "true")
public class ChatController {

    @Autowired
    private UserRepository userRepository;

    // ✨ 修改点 1：替换为我们刚刚真正创建的仓库接口
    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * 前端通过 /app/send 发送消息到这里
     */
    @MessageMapping("/send")
    @Transactional // ✨ 关键：保证扣钱和发消息是一个原子操作
    public void handleMessage(Map<String, String> payload) {
        String username = payload.get("sender");
        String content = payload.get("content");

        // 1. 获取用户信息
        User user = userRepository.findByUsername(username);

        // 2. 核心拦截：金币检查
        if (user != null && user.getPopCoins() >= 10) {
            // A. 扣除 10 金币
            user.setPopCoins(user.getPopCoins() - 10);
            userRepository.save(user);

            // B. 持久化存储聊天记录 (存入 book_chat 表)
            ChatMessage msg = new ChatMessage(username, content);

            // ✨ 修改点 2：解除注释，真正的持久化入库！
            chatMessageRepository.save(msg);

            // C. 广播给所有人 (频道：/topic/public)
            messagingTemplate.convertAndSend("/topic/public", msg);
        } else {
            // D. 余额不足：在控制台打印拦截日志
            System.out.println("⚠️ 拦截提示：用户 [" + username + "] 余额不足，强行发言被系统驳回！");
        }
    }

    // ==========================================
    // ✨ 修改点 3：新增“拉取历史聊天记录”接口
    // 用户每次刚打开网页进群时，调用这个接口看以前的消息
    // ==========================================
    @GetMapping("/api/chat/history")
    @ResponseBody
    public List<ChatMessage> getChatHistory() {
        // 调用 Repository 获取最近的 50 条消息
        List<ChatMessage> history = chatMessageRepository.findTop50ByOrderByTimestampDesc();

        // 因为数据库查出来是最新消息在最上面，而聊天窗口应该是最新消息在最底下
        // 所以我们用 Java 自带的方法把它反转一下顺序
        Collections.reverse(history);
        return history;
    }
}