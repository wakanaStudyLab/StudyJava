package sample;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * ============================================================================
 * 16: 柔軟なコンストラクタ設計 & モダンJava総仕上げ (Flexible Constructors & Modern Idioms)
 * ============================================================================
 * 
 * 【他言語経験者（Rust, C#, Go）向け要点】
 * 1. Statements before super(...) (Flexible Constructor Bodies - JEP 482):
 *    - 従来の Java は「`super(...)` または `this(...)` はコンストラクタの最初の1行目に
 *      書かなければならない」という厳しい構文制限がありました。
 *    - このため、「引数が不正なら親クラスを初期化する前に即座に例外を投げたい」
 *      「引数を前処理・正規化してから親コンストラクタに渡したい」場合に、
 *      不格好な `static private` ヘルパーメソッドを経由する裏技が必要でした。
 *    - Java 22+ (JEP 482) でこの制約が撤廃され、親コンストラクタ呼び出しの前に
 *      任意の検証文や計算ロジックを実行できるようになりました。
 * 
 * 2. モダンJava（Java 21〜26）による設計スタイルの変革:
 *    - 「Java は冗長でボイラープレートが多い」というのは過去の誤解です。
 *    - `record`, `sealed interface`, `var`, パターンマッチング, 仮想スレッドを
 *      組み合わせることで、Rust や Go と同等以上にクリーンかつ表現力の高い
 *      アーキテクチャを驚くほど少ないコード行数で構築できます。
 */
public class FlexibleConstructorAndModernLanguage {

    public static void run() {
        System.out.println("=== 1. Statements before super(...) (JEP 482) ===");
        demonstrateFlexibleConstructor();

        System.out.println("\n=== 2. Modern Clean Architecture Idiom (Zero Boilerplate) ===");
        demonstrateModernArchitecture();
    }

    // --- 基底クラス ---
    public static class Entity {
        private final String id;
        private final Instant createdAt;

        public Entity(String id, Instant createdAt) {
            this.id = Objects.requireNonNull(id, "id must not be null");
            this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        }

        public String id() { return id; }
        public Instant createdAt() { return createdAt; }
    }

    // --- サブクラス (親コンストラクタ呼び出し前の前処理) ---
    public static class SecureUserAccount extends Entity {
        private final String email;
        private final String role;

        public SecureUserAccount(String rawEmail, String role) {
            // [New Feature in Java 22+]
            // super() を呼ぶ前に、引数の完全なバリデーションと正規化を実行！
            if (rawEmail == null || !rawEmail.contains("@")) {
                throw new IllegalArgumentException("Invalid email address: " + rawEmail);
            }
            if (role == null || role.isBlank()) {
                throw new IllegalArgumentException("Role cannot be empty");
            }

            // 引数の前処理
            String normalizedEmail = rawEmail.trim().toLowerCase();
            String generatedId = "usr_" + UUID.nameUUIDFromBytes(normalizedEmail.getBytes()).toString().substring(0, 8);
            Instant timestamp = Instant.now();

            // 前処理済みの安全な値で親コンストラクタを呼び出す
            super(generatedId, timestamp);

            this.email = normalizedEmail;
            this.role = role.toUpperCase();
        }

        public String email() { return email; }
        public String role() { return role; }

        @Override
        public String toString() {
            return String.format("SecureUserAccount[id=%s, email=%s, role=%s, created=%s]",
                id(), email, role, createdAt());
        }
    }

    private static void demonstrateFlexibleConstructor() {
        var user = new SecureUserAccount("   Alice.Dev@Example.COM   ", "admin");
        System.out.println("  Instantiated entity with pre-validated constructor:");
        System.out.println("  " + user);

        // バリデーションの確認
        try {
            new SecureUserAccount("invalid-email-string", "user");
        } catch (IllegalArgumentException e) {
            System.out.println("  Successfully caught invalid instantiation before super(): " + e.getMessage());
        }
    }

    // =========================================================================
    // モダンJavaによる表現力: イミュータブル・イベント駆動・エラー処理の統合
    // =========================================================================

    public sealed interface Result<T, E> {
        record Success<T, E>(T value) implements Result<T, E> {}
        record Failure<T, E>(E error) implements Result<T, E> {}

        default boolean isSuccess() {
            return this instanceof Success;
        }
    }

    public record AuditLog(String traceId, String action, boolean allowed) {}

    private static Result<AuditLog, String> authorizeAction(String userRole, String action) {
        // switch 式による認可判定
        return switch (userRole) {
            case "ADMIN" -> new Result.Success<>(new AuditLog(UUID.randomUUID().toString(), action, true));
            case "USER" -> !action.startsWith("DELETE")
                ? new Result.Success<>(new AuditLog(UUID.randomUUID().toString(), action, true))
                : new Result.Failure<>("Regular USER is not authorized to perform: " + action);
            default -> new Result.Failure<>("Unknown role: " + userRole);
        };
    }

    private static void demonstrateModernArchitecture() {
        var actions = new String[] { "READ_DATA", "UPDATE_PROFILE", "DELETE_DATABASE" };
        String role = "USER";

        System.out.println("  Evaluating permissions for role '" + role + "':");

        for (String act : actions) {
            Result<AuditLog, String> outcome = authorizeAction(role, act);

            // Result 型に対する完全なパターンマッチング (Rust の match result と同等)
            String logMessage = switch (outcome) {
                case Result.Success(AuditLog log) -> 
                    "    [ALLOWED] Action: " + log.action() + " | TraceID: " + log.traceId();
                case Result.Failure(String err) -> 
                    "    [DENIED]  Error: " + err;
            };

            System.out.println(logMessage);
        }
    }
}
