package sample;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Optional;

/**
 * ============================================================================
 * 03: 例外処理・リソース管理・Optional (Exception & Resource Management)
 * ============================================================================
 * 
 * 【他言語経験者（Rust, C#, Go）向け要点】
 * 1. 検査例外 (Checked Exception) vs 非検査例外 (Unchecked Exception):
 *    - Java特有の概念。`Exception` 派生は明示的な `try-catch` または `throws` 宣言が必須。
 *    - `RuntimeException` 派生（非検査例外）は宣言不要（C#, Rust, Goのエラー処理感に近い）。
 *    - 【モダンJavaのベストプラクティス】
 *      現代の設計（Spring Boot等）では、ボイラープレート化を防ぐため「非検査例外 (RuntimeException)」を
 *      独自例外の基底にするのが主流です。
 * 
 * 2. try-with-resources (AutoCloseable):
 *    - C#の `using` ステートメント、Goの `defer f.Close()`、Rustの `Drop` トレイトに相当。
 *    - `AutoCloseable` インターフェースを実装したクラスは、ブロック脱出時に例外発生有無にかかわらず
 *      確実に `close()` が自動呼び出しされます。
 * 
 * 3. Optional<T> (Java 8+):
 *    - Rustの `Option<T>`、C#の Nullable 参照型に相当。
 *    - 【重要作法】
 *      - Optional は「メソッドの戻り値として値が無い可能性を呼び出し側に意識させる」ために設計されました。
 *      - フィールド変数やメソッド引数、コレクションの要素として Optional を使うのはアンチパターンです。
 *      - `.get()` を直呼びするのは Rustの `.unwrap()` と同様に危険。`.orElse()`, `.orElseGet()`, `.map()` を使います。
 */
public class ExceptionAndResource {

    public static void run() {
        System.out.println("=== 1. Checked vs Unchecked Exceptions (Custom Exception) ===");
        demonstrateExceptions();

        System.out.println("\n=== 2. try-with-resources (AutoCloseable) ===");
        demonstrateTryWithResources();

        System.out.println("\n=== 3. Optional<T> Best Practices (Rust Option Equivalent) ===");
        demonstrateOptional();
    }

    // --- 非検査例外 (推奨スタイル: RuntimeExceptionを継承) ---
    public static class DomainException extends RuntimeException {
        public DomainException(String message) {
            super(message);
        }
    }

    public static class InsufficientFundsException extends DomainException {
        private final int currentBalance;
        private final int requiredAmount;

        public InsufficientFundsException(int currentBalance, int requiredAmount) {
            super("Insufficient funds: current=" + currentBalance + ", required=" + requiredAmount);
            this.currentBalance = currentBalance;
            this.requiredAmount = requiredAmount;
        }

        public int getCurrentBalance() { return currentBalance; }
        public int getRequiredAmount() { return requiredAmount; }
    }

    private static void demonstrateExceptions() {
        try {
            withdraw("account-001", 5000, 10000);
        } catch (InsufficientFundsException e) {
            System.err.println("Caught Domain Error: " + e.getMessage());
            System.err.println("Shortage amount: " + (e.getRequiredAmount() - e.getCurrentBalance()));
        } catch (Exception e) {
            System.err.println("Unexpected system error: " + e.getMessage());
        }
    }

    private static void withdraw(String accountId, int balance, int amount) {
        if (balance < amount) {
            throw new InsufficientFundsException(balance, amount);
        }
    }

    // =========================================================================
    // AutoCloseable によるカスタムリソース (DBコネクションやファイルハンドルの模擬)
    // =========================================================================
    public static class DatabaseSession implements AutoCloseable {
        private final String connectionId;

        public DatabaseSession(String connectionId) {
            this.connectionId = connectionId;
            System.out.println("[DB] Session started: " + connectionId);
        }

        public void executeQuery(String sql) {
            System.out.println("[DB] Executing SQL (" + connectionId + "): " + sql);
        }

        @Override
        public void close() {
            // ブロック脱出時に自動で確実に実行される (Goの defer、Rustの Drop)
            System.out.println("[DB] Session closed (Cleaned up): " + connectionId);
        }
    }

    private static void demonstrateTryWithResources() {
        // try (...) の中に宣言されたリソースはスコープ終了時に逆順で自動 close されます。
        try (
            var session = new DatabaseSession("conn-12345");
            var reader = new BufferedReader(new StringReader("Line 1\nLine 2"))
        ) {
            session.executeQuery("SELECT * FROM users");
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("Read: " + line);
            }
        } catch (Exception e) {
            System.err.println("Resource error: " + e.getMessage());
        }
        // ここに到達した時点で session.close() と reader.close() は完了済み
    }

    // =========================================================================
    // Optional の実践的活用
    // =========================================================================
    public record UserProfile(String id, String nickname, String email) {}

    // リポジトリメソッドの戻り値として Optional を返す (Rustの fn find_by_id() -> Option<User> と同等)
    private static Optional<UserProfile> findUserById(String id) {
        if ("u123".equals(id)) {
            return Optional.of(new UserProfile("u123", "Bob", "bob@example.com"));
        } else if ("u456".equals(id)) {
            // メールアドレスが null の可能性
            return Optional.of(new UserProfile("u456", "Charlie", null));
        }
        return Optional.empty(); // 該当なし
    }

    private static void demonstrateOptional() {
        // 1. 存在する場合の変換 (map) & デフォルト値 (orElse)
        String email1 = findUserById("u123")
            .map(UserProfile::email)
            .orElse("no-reply@example.com");
        System.out.println("User u123 Email: " + email1);

        // 2. 存在しない場合のフォールバック
        String email2 = findUserById("unknown")
            .map(UserProfile::email)
            .orElse("default@example.com");
        System.out.println("User unknown Email: " + email2);

        // 3. 存在しない場合に例外をスロー (orElseThrow)
        try {
            UserProfile user = findUserById("unknown")
                .orElseThrow(() -> new DomainException("User not found"));
        } catch (DomainException e) {
            System.out.println("orElseThrow caught: " + e.getMessage());
        }

        // 4. ifPresent / ifPresentOrElse (値があるときだけアクション実行)
        findUserById("u123").ifPresentOrElse(
            u -> System.out.println("User found: " + u.nickname()),
            () -> System.out.println("User not found")
        );
    }
}
