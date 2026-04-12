package org.example.library.repository;

import org.example.library.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // JPA 的魔法：只要方法名写对，它会自动帮你生成 SQL 语句！
    User findByUsername(String username);

    User findByFaceId(String faceId);

}