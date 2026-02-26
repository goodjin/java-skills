# RESTful API 设计规范

## 问题
如何设计规范的 RESTful API？

## 解决方案

### URL 设计

```java
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @GetMapping              // 查询列表
    public ResponseEntity<List<User>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {}

    @GetMapping("/{id}")     // 查询单个
    public ResponseEntity<User> get(@PathVariable Long id) {}

    @PostMapping             // 创建
    public ResponseEntity<User> create(@Valid @RequestBody User user) {}

    @PutMapping("/{id}")     // 更新
    public ResponseEntity<User> update(
            @PathVariable Long id,
            @Valid @RequestBody User user) {}

    @DeleteMapping("/{id}")  // 删除
    public ResponseEntity<Void> delete(@PathVariable Long id) {}
}
```

### 状态码

| 状态码 | 含义 |
|--------|------|
| 200 | OK |
| 201 | Created |
| 204 | No Content |
| 400 | Bad Request |
| 401 | Unauthorized |
| 403 | Forbidden |
| 404 | Not Found |
| 500 | Internal Error |

### 最佳实践

- URL 用名词复数: `/users` 而非 `/getUsers`
- 版本控制: `/api/v1/`
- 分页: `?page=0&size=10`
- 过滤: `?status=active`
