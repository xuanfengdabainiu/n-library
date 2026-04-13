package org.example.library.controller;

import org.example.library.entity.*;
import org.example.library.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(originPatterns = "${cors.allowed.origin-patterns}", allowCredentials = "true")
public class UserBookController {

    @Autowired private BookRepository bookRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private UserBookRepository userBookRepository;

    // 1. 添加到我的书单
    @PostMapping("/add-book")
    public String addToMyList(@RequestBody Map<String, String> data) {
        String username = data.get("username");
        String isbn = data.get("isbn");

        // A. 找到用户
        User user = userRepository.findByUsername(username);
        // B. 检查书库里是否有这本书（通过ISBN）
        Book book = bookRepository.findByIsbn(isbn);

        if (book == null) {
            // 如果是全球搜索来的新书，先存入公共书库
            book = new Book();
            book.setIsbn(isbn);
            book.setTitle(data.get("title"));
            book.setAuthor(data.get("author"));
            book.setCoverUrl(data.get("coverUrl"));

            // --- 关键修复：把前端传来的阅读链接存入实体类！ ---
            book.setReadUrl(data.get("readUrl"));

            book = bookRepository.save(book);
        }

        // C. 绑定用户和书
        if (!userBookRepository.existsByUserIdAndBookId(user.getId(), book.getId())) {
            userBookRepository.save(new UserBook(user.getId(), book.getId()));
            return "SUCCESS";
        }
        return "ALREADY_ADDED";
    }

    // 2. 获取我的私人书单
    @GetMapping("/books")
    public List<Book> getMyBooks(@RequestParam String username) {
        User user = userRepository.findByUsername(username);
        List<UserBook> relations = userBookRepository.findByUserId(user.getId());

        List<Book> myBooks = new ArrayList<>();
        for (UserBook rel : relations) {
            bookRepository.findById(rel.getBookId()).ifPresent(myBooks::add);
        }
        return myBooks;
    }
    // 3. 从我的私人书单中移除书籍 (不删总库！)
    @PostMapping("/remove-book")
    public String removeFromMyList(@RequestBody Map<String, String> data) {
        String username = data.get("username");
        // 注意前端传过来的是字符串，我们需要转成 Long
        Long bookId = Long.parseLong(data.get("bookId"));

        // 找到对应用户
        User user = userRepository.findByUsername(username);
        if (user != null) {
            // 仅仅斩断关联表里的记录，绝对不碰 books 表！
            userBookRepository.deleteByUserIdAndBookId(user.getId(), bookId);
            return "SUCCESS";
        }
        return "USER_NOT_FOUND";
    }
    // 4. 获取今日随机推荐
    @GetMapping("/random-book")
    public Book getRandomBook() {
        return bookRepository.findRandomBook();
    }
}