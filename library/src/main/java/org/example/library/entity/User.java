package org.example.library.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    private String password;

    @Column(name = "pop_coins")
    private Integer popCoins;

    // ✨ 统一映射数据库的 register_time，用于 Stats 卡片计算
    @Column(name = "register_time", insertable = false, updatable = false)
    private LocalDateTime registerTime;

    // 🤖 连接 Python AI 的关键字段
    @Column(name = "face_id")
    private String faceId;

    public User() {}

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Getter and Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Integer getPopCoins() { return popCoins; }
    public void setPopCoins(Integer popCoins) { this.popCoins = popCoins; }

    public LocalDateTime getRegisterTime() { return registerTime; }
    public void setRegisterTime(LocalDateTime registerTime) { this.registerTime = registerTime; }

    public String getFaceId() { return faceId; }
    public void setFaceId(String faceId) { this.faceId = faceId; }

    @Override
    public String toString() {
        return "User{" + "id=" + id + ", username='" + username + '\'' +
                ", popCoins=" + popCoins + ", registerTime=" + registerTime + '}';
    }
}