package org.example.library.controller;

import org.example.library.entity.User;
import org.example.library.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value; // ✨ 新增导入
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin(originPatterns = "${cors.allowed.origin-patterns}", allowCredentials = "true")
@RequestMapping("/api")
public class LoginController {

    @Autowired
    private UserRepository userRepository;

    // ✨ 核心修改：动态读取 Python 地址，不再写死
    @Value("${python.service.url}")
    private String PYTHON_SERVICE_URL;

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

            // 使用动态注入的 PYTHON_SERVICE_URL
            Map pythonResult = restTemplate.postForObject(PYTHON_SERVICE_URL + "/recognize", pythonReq, Map.class);

            if (pythonResult != null && (Boolean) pythonResult.get("success")) {
                String faceId = (String) pythonResult.get("face_id");
                User user = userRepository.findByFaceId(faceId);

                if (user != null) {
                    response.put("success", true);
                    response.put("userName", user.getUsername());
                } else {
                    response.put("success", false);
                    response.put("message", "库中未找到匹配人脸");
                }
            } else {
                response.put("success", false);
                response.put("message", "识别失败：人脸不匹配或未检测到人脸");
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "后台服务开小差了，请稍后再试");
        }
        return response;
    }

    // ==========================================
    // 2. 人脸注册接口
    // ==========================================
    @PostMapping("/register/face")
    public Map<String, Object> faceRegister(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");
        String base64Image = request.get("image");
        Map<String, Object> response = new HashMap<>();

        try {
            RestTemplate restTemplate = new RestTemplate();
            Map<String, String> pythonReq = new HashMap<>();
            pythonReq.put("username", username);
            pythonReq.put("image_base64", base64Image);

            // 使用动态注入的 PYTHON_SERVICE_URL
            Map pythonResult = restTemplate.postForObject(PYTHON_SERVICE_URL + "/register", pythonReq, Map.class);

            if (pythonResult != null && (Boolean) pythonResult.get("success")) {
                String newFaceId = (String) pythonResult.get("face_id");

                User newUser = new User();
                newUser.setUsername(username);
                newUser.setPassword(password);
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
            response.put("message", "注册失败：AI 引擎连接中断");
        }
        return response;
    }
}