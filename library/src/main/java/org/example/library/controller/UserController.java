package org.example.library.controller;

import org.example.library.entity.User;
import org.example.library.repository.UserRepository;
import org.example.library.repository.UserBookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    // ✨ 注入你设计的中间表 Repository
    @Autowired
    private UserBookRepository userBookRepository;
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getUserStats(@RequestParam String username) {
        Map<String, Object> response = new HashMap<>();

        // 1. 获取用户信息
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        // 💰 获取波普币 (防止 null 报错)
        response.put("popCoins", user.getPopCoins() != null ? user.getPopCoins() : 0);

        // 📚 统计藏书数量
        int bookCount = userBookRepository.countByUserId(user.getId());
        response.put("collectedBooks", bookCount);

        // ✨ 核心修改：把密码字段塞进去发给前端
        response.put("password", user.getPassword());

        // 📅 计算加入天数
        if (user.getRegisterTime() != null) {
            java.time.LocalDate regDate = user.getRegisterTime().toLocalDate();
            java.time.LocalDate today = java.time.LocalDate.now();
            long days = java.time.temporal.ChronoUnit.DAYS.between(regDate, today);
            response.put("joinedDays", days <= 0 ? 1 : days);
        } else {
            response.put("joinedDays", 1);
        }

        return ResponseEntity.ok(response);
    }
    // ✨ 新增：狂暴点钞机接口，点一次加一个波普币！
    @PostMapping("/add-coin")
    public ResponseEntity<String> addPopCoin(@RequestParam String username) {
        User user = userRepository.findByUsername(username);
        if (user != null) {
            // 获取当前硬币，如果是 null 就当 0 算，然后 +1
            int currentCoins = user.getPopCoins() != null ? user.getPopCoins() : 0;
            user.setPopCoins(currentCoins + 1);

            // 保存回数据库
            userRepository.save(user);
            return ResponseEntity.ok("SUCCESS");
        }
        return ResponseEntity.notFound().build();
    }
    // ✨ 升级版：支持自定义加币数量的接口
    @PostMapping("/reward-coins")
    public ResponseEntity<Map<String, Object>> rewardCoins(@RequestParam String username, @RequestParam int amount) {
        User user = userRepository.findByUsername(username);
        Map<String, Object> response = new HashMap<>();

        if (user != null) {
            int currentCoins = user.getPopCoins() != null ? user.getPopCoins() : 0;
            user.setPopCoins(currentCoins + amount); // 💥 加上指定数量（比如 10）
            userRepository.save(user);

            response.put("success", true);
            response.put("newBalance", user.getPopCoins());
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.notFound().build();
    }

    // ✨ 新增：接收 Python 处理好的 Face ID 并绑定到数据库
    @PostMapping("/update-face-id")
    public ResponseEntity<String> updateFaceId(@RequestParam String username, @RequestParam String faceId) {
        User user = userRepository.findByUsername(username);
        if (user != null) {
            user.setFaceId(faceId);
            userRepository.save(user);
            return ResponseEntity.ok("SUCCESS");
        }
        return ResponseEntity.notFound().build();
    }
    // ✨ 新增：真正修改用户登录密码的接口
    @PostMapping("/update-password")
    public ResponseEntity<String> updatePassword(@RequestParam String username, @RequestParam String newPassword) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        // 直接更新 password 字段
        user.setPassword(newPassword);
        userRepository.save(user);

        return ResponseEntity.ok("SUCCESS");
    }
}