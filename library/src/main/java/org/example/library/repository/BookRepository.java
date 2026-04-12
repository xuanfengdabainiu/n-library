package org.example.library.repository;

import org.example.library.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Map;

public interface BookRepository extends JpaRepository<Book, Long> {

    // 查找用户的书架列表
    List<Book> findByUsername(String username);

    // ✨ 核心修复：添加这个方法，让 JPA 帮我们统计该用户的藏书总数！
    int countByUsername(String username);

    // 根据 ISBN 查找具体的某一本书
    Book findByIsbn(String isbn);

    // 在数据库中随机抽取一条记录
    @Query(value = "SELECT * FROM books ORDER BY RAND() LIMIT 1", nativeQuery = true)
    Book findRandomBook();

    // ==========================================
    // ✨ 真实热度排行查询
    // ==========================================
    @Query(value = "SELECT b.title AS title, COUNT(ub.book_id) AS hotness " +
            "FROM user_book_relation ub " +
            "JOIN books b ON ub.book_id = b.id " +
            "GROUP BY ub.book_id, b.title " +
            "ORDER BY hotness DESC " +
            "LIMIT 3", nativeQuery = true)
    List<Map<String, Object>> findTop3TrendingBooks();
}