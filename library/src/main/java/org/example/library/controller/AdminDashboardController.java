package org.example.library.controller; // 确保包名是你的真实包名

import org.example.library.entity.User;
import org.example.library.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    @Autowired
    private UserRepository userRepository;

    /**
     * 接口 1：获取所有普通用户列表
     * 对应前端：点击 "USERS" 按钮弹出的爆炸面板
     */
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        // 直接利用 Spring Data JPA 自带的 findAll() 方法查出所有的用户
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }

    /**
     * 接口 2：物理删除用户 (DELETE 功能)
     */
    @PostMapping("/users/{id}/delete")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 判断用户是否存在
            if (userRepository.existsById(id)) {
                // 💥 物理超度：直接从数据库中删除！
                userRepository.deleteById(id);

                response.put("success", true);
                response.put("message", "物理抹除成功：该用户已彻底消失！");
            } else {
                response.put("success", false);
                response.put("message", "找不到该用户！");
            }
        } catch (Exception e) {
            // ⚠️ 避坑提醒：如果这个用户已经在书架里收藏了书（中间表有关联数据）
            // 直接删用户可能会报外键约束错误。如果出现这行报错，咱们稍后在中间表加个级联删除就行。
            response.put("success", false);
            response.put("message", "删除失败：请检查是否有外键关联数据！");
        }

        return ResponseEntity.ok(response);
    }
    // 记得在类最上面自动注入 BookRepository
    @Autowired
    private org.example.library.repository.BookRepository bookRepository;

    // ==========================================
    // 书籍管理模块接口 (GLOBAL BOOK DB)
    // ==========================================

    // 1. 获取所有书籍
    @GetMapping("/books")
    public ResponseEntity<List<org.example.library.entity.Book>> getAllBooks() {
        return ResponseEntity.ok(bookRepository.findAll());
    }

    // 2. 上架新书 (Mint Book)
    @PostMapping("/books/add")
    public ResponseEntity<Map<String, Object>> addBook(@RequestBody org.example.library.entity.Book book) {
        Map<String, Object> response = new HashMap<>();

        // 设置默认状态为 1 (正常)
        book.setStatus(1);

        bookRepository.save(book); // 保存到数据库

        response.put("success", true);
        response.put("message", "新书《" + book.getTitle() + "》上架成功！");
        return ResponseEntity.ok(response);
    }

    // 3. 物理下架书籍 (Delete)
    @PostMapping("/books/{id}/delete")
    public ResponseEntity<Map<String, Object>> deleteBook(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (bookRepository.existsById(id)) {
                bookRepository.deleteById(id);
                response.put("success", true);
                response.put("message", "书籍已被彻底销毁！");
            } else {
                response.put("success", false);
                response.put("message", "找不到该书！");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "删除失败：可能有用户正在借阅或收藏该书！");
        }
        return ResponseEntity.ok(response);
    }
    // 记得在类最上面自动注入 AdminRepository
    @Autowired
    private org.example.library.repository.AdminRepository adminRepository;

    // ==========================================
    // 模块 3：工作人员管理接口 (STAFF ACCESS)
    // ==========================================

    // 1. 获取所有管理员
    @GetMapping("/staff")
    public ResponseEntity<List<org.example.library.entity.Admin>> getAllStaff() {
        return ResponseEntity.ok(adminRepository.findAll());
    }

    // 2. 添加新管理员 (雇佣)
    @PostMapping("/staff/add")
    public ResponseEntity<Map<String, Object>> addStaff(@RequestBody org.example.library.entity.Admin admin) {
        Map<String, Object> response = new HashMap<>();
        // 简单校验一下用户名是否已存在可以加在这里，为了演示咱们直接保存
        adminRepository.save(admin);
        response.put("success", true);
        response.put("message", "新管理员 [" + admin.getUsername() + "] 入职成功！");
        return ResponseEntity.ok(response);
    }

    // 3. 撤销管理员权限 (物理删除)
    @PostMapping("/staff/{id}/delete")
    public ResponseEntity<Map<String, Object>> deleteStaff(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (adminRepository.existsById(id)) {
                adminRepository.deleteById(id);
                response.put("success", true);
                response.put("message", "已撤销该员工所有系统权限！");
            } else {
                response.put("success", false);
                response.put("message", "找不到该员工！");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "撤销失败：系统错误");
        }
        return ResponseEntity.ok(response);
    }
    // ==========================================
    // 模块 4：热度排行接口 (真实订阅数据)
    // ==========================================

    @GetMapping("/trending")
    public ResponseEntity<List<Map<String, Object>>> getTrendingBooks() {

        // 1. 直接调用我们刚写好的终极 SQL 查询
        List<Map<String, Object>> rawList = bookRepository.findTop3TrendingBooks();
        List<Map<String, Object>> trendingList = new ArrayList<>();

        // 2. 防弹处理：统一格式化为前端需要的小写 key
        for (Map<String, Object> raw : rawList) {
            Map<String, Object> map = new HashMap<>();

            // 兼容大小写的取值方式
            Object title = raw.get("title") != null ? raw.get("title") : raw.get("TITLE");
            Object hotness = raw.get("hotness") != null ? raw.get("hotness") : raw.get("HOTNESS");

            map.put("title", title);
            map.put("hotness", hotness);

            trendingList.add(map);
        }

        return ResponseEntity.ok(trendingList);
    }
}