package org.example.library.repository;

import org.example.library.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // ✨ 赠送一个实用方法：获取最近的 50 条聊天记录
    // Spring Data JPA 会自动把它翻译成类似：
    // SELECT * FROM chat_messages ORDER BY timestamp DESC LIMIT 50;
    List<ChatMessage> findTop50ByOrderByTimestampDesc();
}