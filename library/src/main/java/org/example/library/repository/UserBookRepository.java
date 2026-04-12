package org.example.library.repository;

import org.example.library.entity.UserBook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UserBookRepository extends JpaRepository<UserBook, Long> {

    // 查找某个用户的所有关联记录
    List<UserBook> findByUserId(Long userId);

    // 检查是否已经收藏过
    boolean existsByUserIdAndBookId(Long userId, Long bookId);

    @Transactional
        // 删除操作必须加这个注解
    void deleteByUserIdAndBookId(Long userId, Long bookId);

    // ✨ 核心新增：根据 userId 统计该用户在中间表里收藏了多少本书！
    int countByUserId(Long userId);
}