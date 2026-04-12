package org.example.library.repository; // 注意改成你自己的包名

import org.example.library.entity.Admin; // 引入刚才建的 Admin 实体类
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {

    // 💡 关键：靠这句代码，Spring Boot 就会自动帮你写出 "根据用户名查管理员" 的 SQL 语句
    Admin findByUsername(String username);
}