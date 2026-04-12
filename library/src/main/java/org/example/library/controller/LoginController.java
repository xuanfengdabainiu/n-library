package org.example.library.controller;

import org.example.library.entity.User;
import org.example.library.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api")
public class LoginController {

    @Autowired
    private UserRepository userRepository;

    private final String PYTHON_SERVICE_URL = "http://pop_face_system:8000/api";

    // ==========================================
    // 1. 刷脸登录接口
    // ==========================================
    @PostMapping("/login/face")
    public Map<String, Object> faceLogin(@RequestBody Map<String, String> request) {
        String base64Image = request.get("image");
        Map<String, Object> response = new HashMap<>();

        try {
            // A. 把照片发给 Python 进行特征匹配
            RestTemplate restTemplate = new RestTemplate();
            Map<String, String> pythonReq = new HashMap<>();
            pythonReq.put("image_base64", base64Image);

            Map pythonResult = restTemplate.postForObject(PYTHON_SERVICE_URL + "/recognize", pythonReq, Map.class);

            // B. Python 识别成功后，Java 去数据库核对身份
            if (pythonResult != null && (Boolean) pythonResult.get("success")) {
                String recognizedFaceId = (String) pythonResult.get("face_id"); // 假设 Python 返回唯一的 face_id

                // 在 MySQL 中查找绑定了该人脸的用户
                User user = userRepository.findByFaceId(recognizedFaceId);

                if (user != null) {
                    response.put("success", true);
                    response.put("userName", user.getUsername());
                    response.put("message", "波普图书馆欢迎你！");
                } else {
                    response.put("success", false);
                    response.put("message", "人脸已识别，但未绑定账号");
                }
            } else {
                response.put("success", false);
                response.put("message", "面部匹配失败，你是特工吗？");
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "后台服务开小差了...");
        }
        return response;
    }

    // ==========================================
    // 2. 刷脸注册接口 (核心新增)
    // ==========================================
    @PostMapping("/register/face")
    public Map<String, Object> faceRegister(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");
        String base64Image = request.get("image");
        Map<String, Object> response = new HashMap<>();

        try {
            // A. 先把照片发给 Python，让它提取人脸特征并保存
            RestTemplate restTemplate = new RestTemplate();
            Map<String, String> pythonReq = new HashMap<>();
            pythonReq.put("username", username);
            pythonReq.put("image_base64", base64Image);

            Map pythonResult = restTemplate.postForObject(PYTHON_SERVICE_URL + "/register", pythonReq, Map.class);

            if (pythonResult != null && (Boolean) pythonResult.get("success")) {
                String newFaceId = (String) pythonResult.get("face_id");

                // B. Python 处理成功后，Java 将用户信息存入 MySQL
                User newUser = new User();
                newUser.setUsername(username);
                newUser.setPassword(password); // 实际项目建议加密：passwordEncoder.encode(password)
                newUser.setFaceId(newFaceId);

                userRepository.save(newUser);

                response.put("success", true);
                response.put("userName", username);
                response.put("message", "注册成功！账号已与人脸绑定");
            } else {
                response.put("success", false);
                response.put("message", "Python 无法解析此面部数据");
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "注册失败，请检查网络");
        }
        return response;
    }
}