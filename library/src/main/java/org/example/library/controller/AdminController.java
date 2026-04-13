package org.example.library.controller;

import org.example.library.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(originPatterns = "${cors.allowed.origin-patterns}", allowCredentials = "true")
public class AdminController {

    // 假设你有一个 AdminRepository 用于操作 admins 表
    @Autowired
    private AdminRepository adminRepository;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> adminLogin(@RequestBody Map<String, String> payload) {
        String username = payload.get("username");
        String password = payload.get("password");

        Map<String, Object> response = new HashMap<>();

        try {
            // 去 admins 表里查询账号
           org.example.library.entity.Admin admin = adminRepository.findByUsername(username);

            // 校验密码（如果你数据库里是加密的，这里记得用 passwordEncoder 校验）
            if (admin != null && admin.getPassword().equals(password)) {
                response.put("success", true);
                response.put("userName", admin.getUsername()); // 返回管理员名字供前端存储
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "权限拒绝：账号或密码错误");
                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "服务器内部错误");
            return ResponseEntity.status(500).body(response);
        }
    }
}