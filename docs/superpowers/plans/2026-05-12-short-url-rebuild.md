# 短链接服务重构 — 实施计划

> **代理执行说明：** 使用 `superpowers:subagent-driven-development` 技能按任务逐一执行。步骤使用复选框 `- [ ]` 跟踪进度。

**目标：** 将现有 Spring Boot 2.4 + Thymeleaf 单体短链接服务，重构为 Maven 多模块 + Vue 3 前后端分离 + 多级缓存 + 用户体系的现代化短链接平台

**架构：** 5 个 Maven 模块（common/core/api/admin/web）+ Vue 3 前端 + Docker Compose 编排（MySQL + Redis + Backend + Nginx）

**技术栈：** Spring Boot 3.4, Java 17, MyBatis-Plus, Spring Security 6, Caffeine, Redisson, Vue 3 + Vite + TypeScript, ECharts

---

### Task 0：项目初始化

**文件：**
- 创建：`pom.xml`（父 POM）
- 创建：`.gitignore`
- 创建：`README.md`
- 创建：`short-url-common/pom.xml`
- 创建：`short-url-core/pom.xml`
- 创建：`short-url-api/pom.xml`
- 创建：`short-url-admin/pom.xml`
- 创建：`short-url-web/pom.xml`
- 创建：`docs/design.md`（前端设计规范）

- [ ] **Step 1：创建 Maven 父 POM**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.4.5</version>
        <relativePath/>
    </parent>

    <groupId>top.naccl</groupId>
    <artifactId>short-url</artifactId>
    <version>1.0.0</version>
    <packaging>pom</packaging>
    <name>short-url</name>
    <description>短链接服务 - 简历项目</description>

    <modules>
        <module>short-url-common</module>
        <module>short-url-core</module>
        <module>short-url-api</module>
        <module>short-url-admin</module>
        <module>short-url-web</module>
    </modules>

    <properties>
        <java.version>17</java.version>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <mybatis-plus.version>3.5.9</mybatis-plus.version>
        <redisson.version>3.41.0</redisson.version>
        <hutool.version>5.8.34</hutool.version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>com.baomidou</groupId>
                <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
                <version>${mybatis-plus.version}</version>
            </dependency>
            <dependency>
                <groupId>org.redisson</groupId>
                <artifactId>redisson-spring-boot-starter</artifactId>
                <version>${redisson.version}</version>
            </dependency>
            <dependency>
                <groupId>cn.hutool</groupId>
                <artifactId>hutool-all</artifactId>
                <version>${hutool.version}</version>
            </dependency>
        </dependencies>
    </dependencyManagement>
</project>
```

- [ ] **Step 2：创建 .gitignore**

```
target/
node_modules/
.env
*.log
.DS_Store
.idea/
*.iml
*.swp
logs/
```

- [ ] **Step 3：创建 docs/design.md（前端设计规范）**

```markdown
# 前端设计规范

## 配色

- 主色：#4F46E5（靛蓝）
- 成功：#10B981（翠绿）
- 警告：#F59E0B（琥珀）
- 错误：#EF4444（红）
- 背景：#F9FAFB（浅灰）
- 卡片：#FFFFFF
- 文字主：#111827
- 文字次：#6B7280

## 字体

- 字体族：Inter, system-ui, -apple-system, sans-serif
- 字号层级：12/14/16/18/20/24/30px

## 间距

- 4/8/12/16/20/24/32/40px 八点网格

## 圆角

- 小：6px（按钮、输入框）
- 中：12px（卡片）
- 大：16px（弹窗、模态框）

## 组件风格

- 按钮：填充主色，圆角 6px，hover 加深
- 输入框：浅灰边框，聚焦主色描边
- 卡片：白底，阴影 `0 1px 3px rgba(0,0,0,0.1)`
- 表格：无边框，斑马条纹，hover 高亮

## 布局

- 最大内容宽度 1200px，居中
- 侧边导航 240px
- 顶部导航 64px

## 图标

- 使用 Heroicons（https://heroicons.com）
```

- [ ] **Step 4：创建各子模块 pom.xml**

```xml
<!-- short-url-common/pom.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>top.naccl</groupId>
        <artifactId>short-url</artifactId>
        <version>1.0.0</version>
    </parent>
    <artifactId>short-url-common</artifactId>
    <dependencies>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>cn.hutool</groupId>
            <artifactId>hutool-all</artifactId>
        </dependency>
        <dependency>
            <groupId>org.apache.commons</groupId>
            <artifactId>commons-lang3</artifactId>
        </dependency>
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
        </dependency>
    </dependencies>
</project>
```

```xml
<!-- short-url-core/pom.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" ...>
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>top.naccl</groupId>
        <artifactId>short-url</artifactId>
        <version>1.0.0</version>
    </parent>
    <artifactId>short-url-core</artifactId>
    <dependencies>
        <dependency>
            <groupId>top.naccl</groupId>
            <artifactId>short-url-common</artifactId>
            <version>${project.version}</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>
        <dependency>
            <groupId>org.redisson</groupId>
            <artifactId>redisson-spring-boot-starter</artifactId>
        </dependency>
        <dependency>
            <groupId>com.github.ben-manes.caffeine</groupId>
            <artifactId>caffeine</artifactId>
        </dependency>
        <dependency>
            <groupId>org.mybatis.spring.boot</groupId>
            <artifactId>mybatis-spring-boot-starter</artifactId>
            <version>3.0.4</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

```xml
<!-- short-url-api/pom.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<project ...>
    <parent>...</parent>
    <artifactId>short-url-api</artifactId>
    <dependencies>
        <dependency>
            <groupId>top.naccl</groupId>
            <artifactId>short-url-core</artifactId>
            <version>${project.version}</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
    </dependencies>
</project>
```

```xml
<!-- short-url-admin/pom.xml -->
<project ...>
    <parent>...</parent>
    <artifactId>short-url-admin</artifactId>
    <dependencies>
        <dependency>
            <groupId>top.naccl</groupId>
            <artifactId>short-url-core</artifactId>
            <version>${project.version}</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
    </dependencies>
</project>
```

```xml
<!-- short-url-web/pom.xml（启动模块，依赖所有子模块） -->
<project ...>
    <parent>...</parent>
    <artifactId>short-url-web</artifactId>
    <dependencies>
        <dependency>
            <groupId>top.naccl</groupId>
            <artifactId>short-url-api</artifactId>
            <version>${project.version}</version>
        </dependency>
        <dependency>
            <groupId>top.naccl</groupId>
            <artifactId>short-url-admin</artifactId>
            <version>${project.version}</version>
        </dependency>
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
        </dependency>
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-thymeleaf</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 5：清理旧代码并初始化 git**

```bash
# 清理旧源码（保留 docs/ 等配置文件）
rm -rf src/ target/

# 初始化 git（如果已有 .git 先清掉）
rm -rf .git
git init
git add .
git commit -m "chore: 初始化多模块项目骨架"
```

- [ ] **Step 6：创建 README.md**

```markdown
# Short URL - 短链接服务

基于 Spring Boot 3 + Vue 3 的短链接生成与管理平台。

## 技术栈

**后端：** Spring Boot 3.4, MyBatis-Plus, Spring Security, Caffeine, Redis, Redisson
**前端：** Vue 3 + Vite + TypeScript, Pinia, Axios, ECharts
**部署：** Docker Compose (MySQL + Redis + Nginx)

## 功能

- 长链接转短链接（MurmurHash + base62）
- 多级缓存（Caffeine 本地 + Redis 分布式）
- 分布式布隆过滤器防缓存穿透
- 用户注册登录（JWT）
- 自定义短链
- 访问统计看板（PV / UV / 设备 / 来源）
- Docker Compose 一键启动

## 快速开始

### 前置要求

- JDK 17+
- Docker & Docker Compose

### 启动

```bash
# 构建并启动所有服务
docker compose up -d

# 访问
# 前端：http://localhost:8080
# API：http://localhost:8060
```

### 本地开发

```bash
# 后端启动
cd short-url-web
mvn spring-boot:run

# 前端启动
cd frontend
npm install
npm run dev
```

## 项目结构

```
short-url/
├── short-url-common/    # 公共 DTO、实体、工具类
├── short-url-core/      # 核心引擎（缓存、布隆过滤器、短码生成）
├── short-url-api/       # 对外 API（重定向、生成短链）
├── short-url-admin/     # 管理后台 API（用户、统计）
├── short-url-web/       # 启动入口 + Security 配置
└── frontend/            # Vue 3 前端
```

## 架构

```
请求 → RedisBloomFilter → Caffeine L1 → Redis L2 → DB
```

```

---

### Task 1：short-url-common — 公共基座

**文件位置：** `short-url-common/src/main/java/top/naccl/dwz/common/`

- [ ] **Step 1：创建统一响应类 ApiResponse**

```java
package top.naccl.dwz.common.dto;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private int code;
    private String msg;
    private T data;

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(200, "success", data);
    }

    public static <T> ApiResponse<T> ok(String msg, T data) {
        return new ApiResponse<>(200, msg, data);
    }

    public static <T> ApiResponse<T> fail(int code, String msg) {
        return new ApiResponse<>(code, msg, null);
    }
}
```

- [ ] **Step 2：创建实体类（UrlMap、User、VisitLog、DailyStats）**

```java
// entity/UrlMap.java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UrlMap {
    private Long id;
    private String shortCode;
    private String longUrl;
    private Long userId;
    private String title;
    private Boolean isCustom;
    private Boolean isActive;
    private LocalDateTime expireTime;
    private Integer views;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public UrlMap(String shortCode, String longUrl) {
        this.shortCode = shortCode;
        this.longUrl = longUrl;
        this.views = 0;
        this.isCustom = false;
        this.isActive = true;
    }
}
```

```java
// entity/User.java
@Data
public class User {
    private Long id;
    private String username;
    private String password;
    private String nickname;
    private String email;
    private String role;      // USER / ADMIN
    private Boolean isActive;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
```

```java
// entity/VisitLog.java
@Data
public class VisitLog {
    private Long id;
    private String shortCode;
    private LocalDateTime visitTime;
    private String ip;
    private String userAgent;
    private String referer;
    private String country;
    private String city;
    private String deviceType;  // PC / MOBILE / TABLET
    private String browser;
    private String os;
}
```

```java
// entity/DailyStats.java
@Data
public class DailyStats {
    private Long id;
    private String shortCode;
    private LocalDate statsDate;
    private Integer pv;
    private Integer uv;
    private Integer ipCount;
}
```

- [ ] **Step 3：创建枚举类**

```java
// enums/ResultCode.java
@Getter
@AllArgsConstructor
public enum ResultCode {
    SUCCESS(200, "成功"),
    BAD_REQUEST(400, "请求参数有误"),
    UNAUTHORIZED(401, "未登录"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    CONFLICT(409, "短链已存在"),
    TOO_MANY_REQUESTS(429, "请求过于频繁"),
    ERROR(500, "服务器内部错误");

    private final int code;
    private final String msg;
}
```

```java
// enums/RoleEnum.java
public enum RoleEnum {
    USER, ADMIN
}
```

```java
// enums/DeviceTypeEnum.java
public enum DeviceTypeEnum {
    PC, MOBILE, TABLET
}
```

- [ ] **Step 4：创建异常类和全局异常处理器**

```java
// exception/ApiException.java
@Getter
public class ApiException extends RuntimeException {
    private final int code;
    public ApiException(ResultCode resultCode) {
        super(resultCode.getMsg());
        this.code = resultCode.getCode();
    }
    public ApiException(int code, String msg) {
        super(msg);
        this.code = code;
    }
}
```

```java
// exception/GlobalExceptionHandler.java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ApiException.class)
    public ApiResponse<Void> handleApiException(ApiException e) {
        return ApiResponse.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(org.springframework.dao.DuplicateKeyException.class)
    public ApiResponse<Void> handleDuplicateKey() {
        return ApiResponse.fail(409, "短链已存在");
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleException(Exception e) {
        return ApiResponse.fail(500, "服务器内部错误");
    }
}
```

- [ ] **Step 5：创建常量类**

```java
// constant/CacheConstants.java
public interface CacheConstants {
    String BLOOM_FILTER_KEY = "bloom:short-url";
    String URL_MAPPING_KEY = "url:";
    String LOCK_KEY_PREFIX = "lock:url:";
    String EMPTY_VALUE = "___EMPTY___";

    long CAFFEINE_MAX_SIZE = 10_000;
    long CAFFEINE_EXPIRE_MINUTES = 15;

    long REDIS_BASE_TTL_SECONDS = 1800;    // 30min
    long REDIS_TTL_RANDOM_RANGE = 300;     // ±5min
    long NULL_VALUE_TTL_SECONDS = 300;     // 空值缓存 5min
}
```

```java
// constant/ShortUrlConstants.java
public interface ShortUrlConstants {
    int TARGET_LENGTH = 6;
    int MAX_RETRY = 5;
    String DUPLICATE_SALT = "*";
}
```

- [ ] **Step 6：创建工具类**

将原有工具类迁移并增强：

```java
// util/HashUtils.java — 保留 MurmurHash 逻辑
public class HashUtils {
    public static String murmurHash32ToBase62(String str) {
        int hash = cn.hutool.core.util.HashUtil.murmur32(str.getBytes(StandardCharsets.UTF_8));
        return Base62Utils.encode(Math.abs((long) hash));
    }
}
```

```java
// util/Base62Utils.java
public class Base62Utils {
    private static final String BASE62 = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int BASE = 62;

    public static String encode(long value) {
        StringBuilder sb = new StringBuilder();
        while (value > 0) {
            sb.append(BASE62.charAt((int) (value % BASE)));
            value /= BASE;
        }
        while (sb.length() < 6) sb.append('0');
        return sb.reverse().toString();
    }
}
```

```java
// util/UrlUtils.java
public class UrlUtils {
    private static final Pattern URL_PATTERN =
        Pattern.compile("^(https?://)?([\\w.-]+)+(:\\d+)?(/[\\w./%-]*)*\\??[\\w&=.-]*$");
    private static final Pattern CUSTOM_CODE_PATTERN =
        Pattern.compile("^[a-zA-Z][a-zA-Z0-9]{3,15}$");

    public static boolean checkURL(String url) {
        return url != null && URL_PATTERN.matcher(url).matches();
    }

    public static boolean validateCustomCode(String code) {
        return code != null && CUSTOM_CODE_PATTERN.matcher(code).matches();
    }
}
```

- [ ] **Step 7：创建 ConfigurationProperties 类**

```java
// config/ShortUrlProperties.java
@ConfigurationProperties(prefix = "short-url")
@Data
public class ShortUrlProperties {
    private String host = "http://localhost:8060";
    private int bloomExpectedInsertions = 1_000_000;
    private double bloomFalsePositiveRate = 0.01;
}
```

- [ ] **Step 8：提交**

```bash
git add short-url-common/
git commit -m "feat: 添加 short-url-common 公共模块"
```

---

### Task 2：short-url-core — 核心业务引擎

- [ ] **Step 1：创建 RedisBloomFilter**

```java
// bloom/RedisBloomFilter.java
@Component
public class RedisBloomFilter {
    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String BLOOM_KEY = CacheConstants.BLOOM_FILTER_KEY;
    // 2^24 bits ≈ 2MB
    private static final long BIT_SIZE = 1L << 24;
    // 5 hash functions
    private static final int[] SEEDS = {1, 3, 5, 7, 11};

    public boolean add(String key) {
        boolean existed = true;
        for (int seed : SEEDS) {
            long index = hash(key, seed) % BIT_SIZE;
            if (Boolean.FALSE.equals(redisTemplate.opsForValue().setBit(BLOOM_KEY, index, true))) {
                existed = false;
            }
        }
        return existed; // true=已存在, false=新增
    }

    public boolean mightContain(String key) {
        for (int seed : SEEDS) {
            long index = hash(key, seed) % BIT_SIZE;
            if (Boolean.FALSE.equals(redisTemplate.opsForValue().getBit(BLOOM_KEY, index))) {
                return false;
            }
        }
        return true;
    }

    private long hash(String key, int seed) {
        return cn.hutool.core.util.HashUtil.murmur32(
            (key + seed).getBytes(StandardCharsets.UTF_8));
    }

    /** 应用启动时从 DB 重建过滤器 */
    @PostConstruct
    public void init() {
        // 通过 ApplicationContext 获取 UrlMapper 加载所有短码
        // 由 web 模块启动后调用
    }
}
```

- [ ] **Step 2：创建 MultiLevelCacheService**

```java
// cache/MultiLevelCacheService.java
@Slf4j
@Service
public class MultiLevelCacheService {
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private RedisBloomFilter bloomFilter;

    private final Cache<String, String> caffeineCache = Caffeine.newBuilder()
        .maximumSize(CacheConstants.CAFFEINE_MAX_SIZE)
        .expireAfterWrite(CacheConstants.CAFFEINE_EXPIRE_MINUTES, TimeUnit.MINUTES)
        .build();

    /**
     * 带多层防护的缓存查询
     * @param key      短链码
     * @param loader   DB 加载函数
     * @return 长链接，null 表示不存在
     */
    public String getWithProtection(String key, Supplier<String> loader) {
        // 1. 布隆过滤器前置拦截（防穿透）
        if (!bloomFilter.mightContain(key)) {
            return null;
        }

        // 2. 查询 L1 Caffeine
        String value = caffeineCache.getIfPresent(key);
        if (value != null) {
            if (CacheConstants.EMPTY_VALUE.equals(value)) return null;
            return value;
        }

        // 3. 查询 L2 Redis
        value = redisTemplate.opsForValue().get(CacheConstants.URL_MAPPING_KEY + key);
        if (value != null) {
            caffeineCache.put(key, value); // 回填 L1
            if (CacheConstants.EMPTY_VALUE.equals(value)) return null;
            return value;
        }

        // 4. 互斥锁加载 DB（防击穿）
        RLock lock = redissonClient.getLock(CacheConstants.LOCK_KEY_PREFIX + key);
        try {
            if (lock.tryLock(2, 10, TimeUnit.SECONDS)) {
                // Double-check
                value = redisTemplate.opsForValue().get(CacheConstants.URL_MAPPING_KEY + key);
                if (value != null) {
                    caffeineCache.put(key, value);
                    return CacheConstants.EMPTY_VALUE.equals(value) ? null : value;
                }

                // 查 DB
                value = loader.get();
                if (value != null) {
                    // 过期时间加随机偏移（防雪崩）
                    long ttl = CacheConstants.REDIS_BASE_TTL_SECONDS
                        + ThreadLocalRandom.current().nextLong(CacheConstants.REDIS_TTL_RANDOM_RANGE);
                    redisTemplate.opsForValue().set(CacheConstants.URL_MAPPING_KEY + key, value, ttl, TimeUnit.SECONDS);
                } else {
                    // 空值缓存（防穿透）
                    redisTemplate.opsForValue().set(
                        CacheConstants.URL_MAPPING_KEY + key,
                        CacheConstants.EMPTY_VALUE,
                        CacheConstants.NULL_VALUE_TTL_SECONDS,
                        TimeUnit.SECONDS);
                }
                caffeineCache.put(key, value != null ? value : CacheConstants.EMPTY_VALUE);
                return value;
            } else {
                // 未获取到锁，等待后重试
                Thread.sleep(50);
                return getWithProtection(key, loader);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /** 写入缓存（生成短链后调用） */
    public void set(String key, String value) {
        long ttl = CacheConstants.REDIS_BASE_TTL_SECONDS
            + ThreadLocalRandom.current().nextLong(CacheConstants.REDIS_TTL_RANDOM_RANGE);
        redisTemplate.opsForValue().set(CacheConstants.URL_MAPPING_KEY + key, value, ttl, TimeUnit.SECONDS);
        caffeineCache.put(key, value);
    }

    /** 清除缓存（删除短链时调用） */
    public void evict(String key) {
        caffeineCache.invalidate(key);
        redisTemplate.delete(CacheConstants.URL_MAPPING_KEY + key);
    }
}
```

- [ ] **Step 3：创建短码生成器**

```java
// shortcode/ShortCodeGenerator.java
public interface ShortCodeGenerator {
    String generate(String longUrl);
}
```

```java
// shortcode/HashShortCodeGenerator.java
@Component
public class HashShortCodeGenerator implements ShortCodeGenerator {
    @Autowired
    private RedisBloomFilter bloomFilter;

    @Override
    public String generate(String longUrl) {
        return generateWithRetry(longUrl, 0);
    }

    private String generateWithRetry(String longUrl, int retryCount) {
        if (retryCount > ShortUrlConstants.MAX_RETRY) {
            throw new ApiException(ResultCode.ERROR.getCode(), "短码生成冲突过多，请稍后重试");
        }
        String salt = retryCount > 0 ? String.valueOf(retryCount) : "";
        String hash = HashUtils.murmurHash32ToBase62(longUrl + salt);
        String shortCode = StringUtils.leftPad(hash, ShortUrlConstants.TARGET_LENGTH, '0');

        // Redis 布隆过滤器预检
        if (bloomFilter.mightContain(shortCode)) {
            return generateWithRetry(longUrl, retryCount + 1);
        }
        return shortCode;
    }
}
```

```java
// shortcode/CustomShortCodeValidator.java
@Component
public class CustomShortCodeValidator {
    @Autowired
    private RedisBloomFilter bloomFilter;

    public void validate(String code) {
        if (!UrlUtils.validateCustomCode(code)) {
            throw new ApiException(ResultCode.BAD_REQUEST.getCode(),
                "自定义短链必须以字母开头，4-16位字母数字");
        }
        if (bloomFilter.mightContain(code)) {
            throw new ApiException(ResultCode.CONFLICT);
        }
    }
}
```

- [ ] **Step 4：创建 UrlMappingService 核心业务**

```java
// service/UrlMappingService.java
public interface UrlMappingService {
    String getLongUrlByShortCode(String shortCode);
    String createShortUrl(String longUrl, Long userId, String customCode);
    void updateViews(String shortCode);
}
```

```java
// service/impl/UrlMappingServiceImpl.java
@Slf4j
@Service
public class UrlMappingServiceImpl implements UrlMappingService {
    @Autowired
    private UrlMapper urlMapper;
    @Autowired
    private MultiLevelCacheService cacheService;
    @Autowired
    private RedisBloomFilter bloomFilter;
    @Autowired
    private HashShortCodeGenerator codeGenerator;
    @Autowired
    private CustomShortCodeValidator customValidator;

    @Override
    public String getLongUrlByShortCode(String shortCode) {
        return cacheService.getWithProtection(shortCode, () -> {
            UrlMap urlMap = urlMapper.selectByShortCode(shortCode);
            return urlMap != null && urlMap.getIsActive() ? urlMap.getLongUrl() : null;
        });
    }

    @Override
    public String createShortUrl(String longUrl, Long userId, String customCode) {
        // 自定义短链
        if (StringUtils.isNotBlank(customCode)) {
            customValidator.validate(customCode);
            return saveUrlMap(customCode, longUrl, userId, true);
        }

        // 自动生成
        String shortCode = codeGenerator.generate(longUrl);
        return saveUrlMap(shortCode, longUrl, userId, false);
    }

    private String saveUrlMap(String shortCode, String longUrl, Long userId, boolean isCustom) {
        try {
            UrlMap urlMap = new UrlMap(shortCode, longUrl);
            urlMap.setUserId(userId);
            urlMap.setIsCustom(isCustom);
            urlMapper.insert(urlMap);

            bloomFilter.add(shortCode);
            cacheService.set(shortCode, longUrl);
            return shortCode;
        } catch (DuplicateKeyException e) {
            if (isCustom) {
                throw new ApiException(ResultCode.CONFLICT);
            }
            // 布隆过滤器误判，用盐值重试
            return createShortUrl(longUrl + ShortUrlConstants.DUPLICATE_SALT, userId, null);
        }
    }

    @Override
    public void updateViews(String shortCode) {
        urlMapper.incrementViews(shortCode);
    }
}
```

- [ ] **Step 5：创建 VisitLogger（异步访问日志）**

```java
// visit/VisitLogger.java
@Slf4j
@Component
public class VisitLogger {
    @Autowired
    private VisitLogMapper visitLogMapper;
    @Autowired
    private DailyStatsMapper dailyStatsMapper;

    @Async
    public void logVisit(String shortCode, HttpServletRequest request) {
        try {
            VisitLog log = new VisitLog();
            log.setShortCode(shortCode);
            log.setVisitTime(LocalDateTime.now());
            log.setIp(IpAddressUtils.getIpAddress(request));
            log.setUserAgent(request.getHeader("User-Agent"));
            log.setReferer(request.getHeader("Referer"));
            // 解析 UA
            String ua = log.getUserAgent();
            if (ua != null) {
                log.setDeviceType(ua.contains("Mobile") ? "MOBILE" : "PC");
                log.setBrowser(ua.contains("Chrome") ? "Chrome" : "Other");
                log.setOs(ua.contains("Windows") ? "Windows" : "Other");
            }
            visitLogMapper.insert(log);

            // 更新日统计
            LocalDate today = LocalDate.now();
            dailyStatsMapper.upsertPv(shortCode, today);
        } catch (Exception e) {
            log.error("记录访问日志失败", e);
        }
    }
}
```

- [ ] **Step 6：提交**

```bash
git add short-url-core/
git commit -m "feat: 添加 short-url-core 核心引擎模块"
```

---

### Task 3：short-url-api — 对外 API

- [ ] **Step 1：创建 RedirectController**

```java
@Controller
public class RedirectController {
    @Autowired private UrlMappingService urlMappingService;
    @Autowired private VisitLogger visitLogger;

    @GetMapping("/{shortCode}")
    public String redirect(@PathVariable String shortCode, HttpServletRequest request) {
        String longUrl = urlMappingService.getLongUrlByShortCode(shortCode);
        if (longUrl != null) {
            urlMappingService.updateViews(shortCode);
            visitLogger.logVisit(shortCode, request);
            return "redirect:" + longUrl;
        }
        return "redirect:/";
    }
}
```

- [ ] **Step 2：创建 ShortenController**

```java
@RestController
@RequestMapping("/api/v1")
public class ShortenController {
    @Autowired private UrlMappingService urlMappingService;
    @Value("${short-url.host}") private String host;

    @PostMapping("/shorten")
    public ApiResponse<ShortenResponse> shorten(@RequestBody ShortenRequest request) {
        if (!UrlUtils.checkURL(request.getLongUrl())) {
            return ApiResponse.fail(400, "URL 格式有误");
        }
        String longUrl = request.getLongUrl();
        if (!longUrl.startsWith("http")) {
            longUrl = "http://" + longUrl;
        }
        String shortCode = urlMappingService.createShortUrl(longUrl, null, request.getCustomCode());
        return ApiResponse.ok(new ShortenResponse(shortCode, host + shortCode, longUrl));
    }
}
```

- [ ] **Step 3：创建 RateLimitInterceptor**

```java
@Component
public class RateLimitInterceptor implements HandlerInterceptor {
    @Autowired private StringRedisTemplate redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 从原项目 AccessLimitInterceptor 简化而来
        // 基于 IP 限频，使用 Redis 计数器
        String ip = IpAddressUtils.getIpAddress(request);
        String key = "rate:shorten:" + ip;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            redisTemplate.expire(key, 10, TimeUnit.SECONDS);
        }
        if (count != null && count > 5) {
            response.setStatus(429);
            response.getWriter().write("{\"code\":429,\"msg\":\"请求过于频繁\"}");
            return false;
        }
        return true;
    }
}
```

- [ ] **Step 4：提交**

```bash
git add short-url-api/
git commit -m "feat: 添加 short-url-api 对外 API 模块"
```

---

### Task 4：short-url-admin + Security

- [ ] **Step 1：创建 DTO 类**

```java
// 在 common 模块添加 request DTO
// dto/request/LoginRequest.java
@Data
public class LoginRequest {
    private String username;
    private String password;
}

// dto/request/RegisterRequest.java
@Data
public class RegisterRequest {
    private String username;
    private String password;
    private String nickname;
}
```

- [ ] **Step 2：创建 JwtTokenProvider**

```java
// web/security/JwtTokenProvider.java（放在 web 模块）
@Component
public class JwtTokenProvider {
    @Value("${short-url.jwt.secret:defaultSecretKeyChangeInProduction123456}")
    private String jwtSecret;
    @Value("${short-url.jwt.expiration:86400000}")
    private long jwtExpiration;

    public String generateToken(User user) {
        Date now = new Date();
        return Jwts.builder()
            .subject(user.getId().toString())
            .claim("username", user.getUsername())
            .claim("role", user.getRole())
            .issuedAt(now)
            .expiration(new Date(now.getTime() + jwtExpiration))
            .signWith(getSigningKey())
            .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }
}
```

- [ ] **Step 3：创建 JwtAuthenticationFilter**

```java
// web/security/JwtAuthenticationFilter.java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Autowired private JwtTokenProvider jwtTokenProvider;
    @Autowired private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String token = extractToken(request);
        if (token != null) {
            try {
                Claims claims = jwtTokenProvider.parseToken(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(claims.getSubject());
                UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (Exception e) {
                SecurityContextHolder.clearContext();
            }
        }
        chain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
```

- [ ] **Step 4：创建 SecurityConfig**

```java
// web/security/SecurityConfig.java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    @Autowired private JwtAuthenticationFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/{shortCode:[a-zA-Z0-9]{4,8}}").permitAll()
                .requestMatchers("/api/v1/shorten").permitAll()
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/api/v1/admin/**").authenticated()
                .anyRequest().permitAll()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
```

- [ ] **Step 5：创建 AuthController**

```java
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    @Autowired private AuthService authService;

    @PostMapping("/register")
    public ApiResponse<UserVO> register(@RequestBody @Valid RegisterRequest request) {
        return ApiResponse.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }
}
```

- [ ] **Step 6：创建 AuthService**

```java
@Service
public class AuthService {
    @Autowired private UserMapper userMapper;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtTokenProvider jwtTokenProvider;

    @Transactional
    public UserVO register(RegisterRequest request) {
        if (userMapper.existsByUsername(request.getUsername())) {
            throw new ApiException(ResultCode.CONFLICT.getCode(), "用户名已存在");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname());
        user.setRole("USER");
        user.setIsActive(true);
        userMapper.insert(user);
        return UserVO.from(user);
    }

    public LoginResponse login(LoginRequest request) {
        User user = userMapper.findByUsername(request.getUsername());
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ApiException(ResultCode.UNAUTHORIZED.getCode(), "用户名或密码错误");
        }
        String token = jwtTokenProvider.generateToken(user);
        return new LoginResponse(token, UserVO.from(user));
    }
}
```

- [ ] **Step 7：创建 UrlManageController 和 StatsController**

```java
// UrlManageController.java — 短链 CRUD + 自定义
@RestController
@RequestMapping("/api/v1/admin/urls")
public class UrlManageController {
    @Autowired private UrlMappingService urlMappingService;
    @Autowired private UrlMapper urlMapper;

    @GetMapping
    public ApiResponse<PageResult<UrlMap>> list(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        // 按用户分页查询（MyBatis-Plus Page）
        Page<UrlMap> result = urlMapper.selectPageByUserId(
            new Page<>(page, size), Long.valueOf(userDetails.getUsername()));
        return ApiResponse.ok(PageResult.of(result));
    }

    @PostMapping("/custom")
    public ApiResponse<ShortenResponse> createCustom(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody CustomShortenRequest request) {
        String shortCode = urlMappingService.createShortUrl(
            request.getLongUrl(), Long.valueOf(userDetails.getUsername()), request.getCustomCode());
        return ApiResponse.ok(new ShortenResponse(shortCode, host + shortCode, request.getLongUrl()));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        UrlMap urlMap = urlMapper.selectById(id);
        if (urlMap != null) {
            urlMapper.deleteById(id);
            cacheService.evict(urlMap.getShortCode());
        }
        return ApiResponse.ok(null);
    }
}
```

```java
// StatsController.java — 统计看板
@RestController
@RequestMapping("/api/v1/admin/stats")
public class StatsController {
    @Autowired private UrlMapper urlMapper;
    @Autowired private DailyStatsMapper dailyStatsMapper;

    @GetMapping("/overview")
    public ApiResponse<StatsOverviewVO> overview(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.valueOf(userDetails.getUsername());
        StatsOverviewVO vo = new StatsOverviewVO();
        vo.setTotalUrls(urlMapper.countByUserId(userId));
        vo.setTotalViews(urlMapper.sumViewsByUserId(userId));
        // 今日新增链接数、今日访问量
        LocalDate today = LocalDate.now();
        vo.setTodayNewUrls(urlMapper.countByUserIdAndDate(userId, today));
        vo.setTodayViews(dailyStatsMapper.sumPvByUserIdAndDate(userId, today));
        return ApiResponse.ok(vo);
    }

    @GetMapping("/daily")
    public ApiResponse<List<DailyStatsVO>> daily(
            @RequestParam(required = false) String shortCode,
            @RequestParam(defaultValue = "7") int days) {
        LocalDate since = LocalDate.now().minusDays(days);
        List<DailyStats> stats = dailyStatsMapper.selectByCodeAndDate(shortCode, since);
        return ApiResponse.ok(stats.stream().map(DailyStatsVO::from).toList());
    }
}
```

- [ ] **Step 8：创建 Mapper 接口**

```java
// web/mapper/UrlMapper.java（MyBatis-Plus）
@Mapper
public interface UrlMapper extends BaseMapper<UrlMap> {
    String getLongUrlByShortUrl(@Param("shortCode") String shortCode);
    void incrementViews(@Param("shortCode") String shortCode);
    List<UrlMap> selectByUserId(@Param("userId") Long userId);
}
```

- [ ] **Step 9：提交**

```bash
git add short-url-admin/ short-url-web/src/main/java/.../security/ short-url-web/src/main/java/.../mapper/
git commit -m "feat: 添加用户认证和管理 API"
```

---

### Task 5：数据层配置

- [ ] **Step 1：创建 application-dev.yml**

```yaml
# short-url-web/src/main/resources/application-dev.yml
server:
  port: 8060

spring:
  datasource:
    url: jdbc:mysql://localhost:3307/dwz?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: root123
    driver-class-name: com.mysql.cj.jdbc.Driver
  data:
    redis:
      host: localhost
      port: 6379
      password: redis123
      database: 1

mybatis-plus:
  mapper-locations: classpath*:mapper/*.xml
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl

short-url:
  host: http://localhost:8060
  jwt:
    secret: MySecretKeyForJWT2026LocalDevEnvironment
    expiration: 86400000
```

- [ ] **Step 2：创建 Mapper XML**

```xml
<!-- web/src/main/resources/mapper/UrlMapper.xml -->
<mapper namespace="top.naccl.dwz.web.mapper.UrlMapper">
    <select id="getLongUrlByShortUrl" resultType="String">
        select lurl from url_map where short_code = #{shortCode} and is_active = 1
    </select>
    <update id="incrementViews">
        update url_map set views = views + 1 where short_code = #{shortCode}
    </update>
</mapper>
```

- [ ] **Step 3：创建 MySQL 初始化脚本**

```sql
-- docker/mysql/init/init.sql
CREATE DATABASE IF NOT EXISTS dwz DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE dwz;

CREATE TABLE IF NOT EXISTS url_map (
  id          BIGINT       NOT NULL AUTO_INCREMENT,
  short_code  VARCHAR(10)  NOT NULL COMMENT '短链码',
  long_url    VARCHAR(2048) NOT NULL COMMENT '原始长链接',
  user_id     BIGINT       DEFAULT NULL COMMENT '所属用户',
  title       VARCHAR(255) DEFAULT NULL COMMENT '链接标题',
  is_custom   TINYINT(1)   DEFAULT 0 COMMENT '是否自定义',
  is_active   TINYINT(1)   DEFAULT 1 COMMENT '是否启用',
  expire_time DATETIME     DEFAULT NULL COMMENT '过期时间',
  views       INT          DEFAULT 0 COMMENT '总访问次数',
  create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_short_code (short_code),
  KEY idx_user_id (user_id),
  KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS user (
  id          BIGINT       NOT NULL AUTO_INCREMENT,
  username    VARCHAR(50)  NOT NULL COMMENT '用户名',
  password    VARCHAR(255) NOT NULL COMMENT 'BCrypt加密密码',
  nickname    VARCHAR(50)  DEFAULT NULL COMMENT '显示昵称',
  email       VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  role        VARCHAR(20)  DEFAULT 'USER' COMMENT '角色',
  is_active   TINYINT(1)   DEFAULT 1 COMMENT '是否激活',
  create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS visit_log (
  id          BIGINT       NOT NULL AUTO_INCREMENT,
  short_code  VARCHAR(10)  NOT NULL COMMENT '短链码',
  visit_time  DATETIME     NOT NULL COMMENT '访问时间',
  ip          VARCHAR(50)  DEFAULT NULL COMMENT 'IP地址',
  user_agent  VARCHAR(500) DEFAULT NULL COMMENT 'User-Agent',
  referer     VARCHAR(500) DEFAULT NULL COMMENT '来源',
  country     VARCHAR(100) DEFAULT NULL COMMENT '国家',
  city        VARCHAR(100) DEFAULT NULL COMMENT '城市',
  device_type VARCHAR(10)  DEFAULT NULL COMMENT '设备类型',
  browser     VARCHAR(50)  DEFAULT NULL COMMENT '浏览器',
  os          VARCHAR(50)  DEFAULT NULL COMMENT '操作系统',
  PRIMARY KEY (id),
  KEY idx_short_code (short_code),
  KEY idx_visit_time (visit_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS daily_stats (
  id          BIGINT       NOT NULL AUTO_INCREMENT,
  short_code  VARCHAR(10)  NOT NULL COMMENT '短链码',
  stats_date  DATE         NOT NULL COMMENT '统计日期',
  pv          INT          DEFAULT 0 COMMENT '页面访问量',
  uv          INT          DEFAULT 0 COMMENT '独立访客',
  ip_count    INT          DEFAULT 0 COMMENT '独立IP数',
  PRIMARY KEY (id),
  UNIQUE KEY uk_code_date (short_code, stats_date),
  KEY idx_stats_date (stats_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

- [ ] **Step 4：提交**

```bash
git add short-url-web/src/main/resources/ docker/mysql/
git commit -m "feat: 添加数据层配置和初始化脚本"
```

---

### Task 6：Vue 3 前端（参考 docs/design.md 风格）

- [ ] **Step 1：初始化 Vue 3 + Vite 项目**

```bash
cd frontend
npm create vite@latest . -- --template vue-ts
npm install vue-router pinia axios echarts vue-echarts @vueuse/core
```

- [ ] **Step 2：配置路由和 Axios**

```typescript
// router/index.ts
const routes = [
  { path: '/', name: 'Home', component: () => import('../views/Home.vue') },
  { path: '/login', name: 'Login', component: () => import('../views/Login.vue') },
  { path: '/register', name: 'Register', component: () => import('../views/Register.vue') },
  { path: '/dashboard', name: 'Dashboard', component: () => import('../views/Dashboard.vue'), meta: { requiresAuth: true } },
  { path: '/url-list', name: 'UrlList', component: () => import('../views/UrlList.vue'), meta: { requiresAuth: true } },
  { path: '/url-detail/:code', name: 'UrlDetail', component: () => import('../views/UrlDetail.vue'), meta: { requiresAuth: true } },
];
```

- [ ] **Step 3：创建 Pinia store**

```typescript
// stores/user.ts
export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '');
  const user = ref<any>(null);

  const isLoggedIn = computed(() => !!token.value);

  async function login(username: string, password: string) {
    const res = await authApi.login({ username, password });
    token.value = res.data.token;
    user.value = res.data.user;
    localStorage.setItem('token', res.data.token);
  }

  function logout() {
    token.value = '';
    user.value = null;
    localStorage.removeItem('token');
  }

  return { token, user, isLoggedIn, login, logout };
});
```

- [ ] **Step 4：首页组件（短链生成 + 展示）**

```vue
<!-- views/Home.vue -->
<script setup lang="ts">
const longUrl = ref('');
const shortUrl = ref('');
const loading = ref(false);

async function generate() {
  loading.value = true;
  try {
    const res = await urlApi.shorten({ longUrl: longUrl.value });
    shortUrl.value = res.data.shortUrl;
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <div class="home">
    <div class="card">
      <h1>短链接生成器</h1>
      <div class="input-group">
        <input v-model="longUrl" placeholder="输入长链接，例如：https://example.com/very/long/url" />
        <button @click="generate" :disabled="loading">生成</button>
      </div>
      <div v-if="shortUrl" class="result">
        <label>短链接：</label>
        <a :href="shortUrl" target="_blank">{{ shortUrl }}</a>
        <button @click="copy(shortUrl)">复制</button>
      </div>
    </div>
  </div>
</template>
```

- [ ] **Step 5：登录/注册页面**

```vue
<!-- views/Login.vue, views/Register.vue — 标准表单，JWT 存储 -->
```

- [ ] **Step 6：Dashboard 概览页面（统计卡片 + ECharts）**

```vue
<!-- views/Dashboard.vue -->
<script setup lang="ts">
const overview = ref<any>({});
const dailyData = ref<any[]>([]);

onMounted(async () => {
  overview.value = (await statsApi.getOverview()).data;
  dailyData.value = (await statsApi.getDaily({ days: 7 })).data;
});
</script>

<template>
  <div class="dashboard">
    <div class="stats-grid">
      <div class="stat-card">
        <span class="label">总链接数</span>
        <span class="value">{{ overview.totalUrls }}</span>
      </div>
      <div class="stat-card">
        <span class="label">总访问量</span>
        <span class="value">{{ overview.totalViews }}</span>
      </div>
      <div class="stat-card">
        <span class="label">今日新增</span>
        <span class="value">{{ overview.todayNewUrls }}</span>
      </div>
      <div class="stat-card">
        <span class="label">今日访问</span>
        <span class="value">{{ overview.todayViews }}</span>
      </div>
    </div>
    <v-chart :option="chartOption" class="chart" />
  </div>
</template>
```

- [ ] **Step 7：短链列表页 + 详情页**

支持表格展示短链、复制、删除、查看统计详情。使用 Pinia 和 Vue Router。

- [ ] **Step 8：Nginx 配置 + Dockerfile**

```nginx
# frontend/nginx/nginx.conf
server {
    listen 80;

    # 短链重定向（正则匹配 4-8 位字母数字短码）
    location ~ ^/[a-zA-Z0-9]{4,8}$ {
        proxy_pass http://backend:8060;
    }
    location /api/ {
        proxy_pass http://backend:8060;
    }
    location / {
        root /usr/share/nginx/html;
        index index.html;
        try_files $uri $uri/ /index.html;
    }
}
```

```dockerfile
# frontend/Dockerfile
FROM node:20-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx/nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
```

- [ ] **Step 9：提交**

```bash
git add frontend/
git commit -m "feat: 添加 Vue 3 前端"
```

---

### Task 7：Docker Compose 编排

- [ ] **Step 1：创建 Backend Dockerfile**

```dockerfile
# docker/backend/Dockerfile
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build
COPY pom.xml .
COPY short-url-common/pom.xml short-url-common/
COPY short-url-core/pom.xml short-url-core/
COPY short-url-api/pom.xml short-url-api/
COPY short-url-admin/pom.xml short-url-admin/
COPY short-url-web/pom.xml short-url-web/
RUN mvn dependency:go-offline -B
COPY . .
RUN mvn package -DskipTests -B

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /build/short-url-web/target/*.jar app.jar
EXPOSE 8060
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]
```

- [ ] **Step 2：创建 docker-compose.yml**

```yaml
version: "3.8"

services:
  mysql:
    image: mysql:8.0
    container_name: shorturl-mysql
    ports:
      - "3307:3306"
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD:-root123}
    volumes:
      - ./docker/mysql/init:/docker-entrypoint-initdb.d
      - mysql-data:/var/lib/mysql
    networks:
      - shorturl-net

  redis:
    image: redis:7-alpine
    container_name: shorturl-redis
    ports:
      - "6379:6379"
    command: redis-server --requirepass ${REDIS_PASSWORD:-redis123}
    volumes:
      - redis-data:/data
    networks:
      - shorturl-net

  backend:
    build:
      context: .
      dockerfile: docker/backend/Dockerfile
    container_name: shorturl-backend
    ports:
      - "8060:8060"
    depends_on:
      - mysql
      - redis
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/dwz?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
      SPRING_DATASOURCE_PASSWORD: ${MYSQL_ROOT_PASSWORD:-root123}
      SPRING_DATA_REDIS_PASSWORD: ${REDIS_PASSWORD:-redis123}
    networks:
      - shorturl-net

  frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile
    container_name: shorturl-frontend
    ports:
      - "8080:80"
    depends_on:
      - backend
    networks:
      - shorturl-net

volumes:
  mysql-data:
  redis-data:

networks:
  shorturl-net:
    driver: bridge
```

- [ ] **Step 3：创建 .env 文件**

```
MYSQL_ROOT_PASSWORD=root123
REDIS_PASSWORD=redis123
```

- [ ] **Step 4：提交**

```bash
git add docker/ docker-compose.yml .env
git commit -m "feat: 添加 Docker Compose 编排"
```

---

### Task 8：联调测试与最终提交

- [ ] **Step 1：清理旧的目标目录和前端依赖**

```bash
rm -rf short-url-web/target/ frontend/node_modules/
```

- [ ] **Step 2：完整的 .gitignore 确认**

```bash
cat .gitignore
# 包含：target/, node_modules/, .env, *.log, .DS_Store, .idea/, *.iml, logs/
```

- [ ] **Step 3：本地 Maven 构建验证**

```bash
# 使用 JDK 17
export JAVA_HOME=D:/jdk/jdk17  # 或实际路径
mvn clean compile -DskipTests
```

- [ ] **Step 4：Docker Compose 启动验证**

```bash
docker compose up -d --build
docker compose ps  # 确认所有服务运行正常
```

- [ ] **Step 5：最终提交**

```bash
git add -A
git commit -m "feat: 完成短链接服务重构"
```
