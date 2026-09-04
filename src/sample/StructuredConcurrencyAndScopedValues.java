package sample;

import java.util.concurrent.StructuredTaskScope;
import java.lang.ScopedValue;
import java.time.Duration;

/**
 * ============================================================================
 * 07: 構造化並行性 & スコープ付き値 (Structured Concurrency & Scoped Values)
 * ============================================================================
 * 
 * 【他言語経験者（Rust, C#, Go）向け要点】
 * 1. 構造化並行性 (Structured Concurrency - Project Loom):
 *    - Go の goroutine や従来の Java スレッドは「非構造化（投げっぱなし）」になりがちで、
 *      親スレッドが終了しても子スレッドが動き続けたり（スレッドリーク）、
 *      エラーが起きたときのキャンセル処理が極めて困難でした。
 *    - StructuredTaskScope は、並行タスク群をコードブロック（try-with-resources）の
 *      スコープ内に厳格に閉じ込めます（Go の errgroup や Kotlin の coroutineScope 相当）。
 * 
 * 2. スコープ付き値 (ScopedValue - Java 21+):
 *    - 従来の `ThreadLocal` は「可変」「メモリリーク多発」「仮想スレッドが数万個起動した際に巨大なメモリ消費」
 *      という致命的な問題を抱えていました。
 *    - `ScopedValue` は「不変」「限定されたスコープ内のみ有効」「仮想スレッド間でゼロコピー共有」
 *      を実現する最新のコンテキスト受け渡し機構です（認証トークンやリクエストIDの伝搬に最適）。
 */
public class StructuredConcurrencyAndScopedValues {

    // 不変コンテキストキーの定義
    private static final ScopedValue<String> CURRENT_USER_ID = ScopedValue.newInstance();
    private static final ScopedValue<String> TRACE_ID = ScopedValue.newInstance();

    public static void run() {
        System.out.println("=== 1. StructuredTaskScope (Structured Concurrency) ===");
        demonstrateStructuredConcurrency();

        System.out.println("\n=== 2. ScopedValue (Modern Replacement for ThreadLocal) ===");
        demonstrateScopedValue();
    }

    // 擬似的なマイクロサービス通信
    private static String fetchUserProfile() throws InterruptedException {
        Thread.sleep(Duration.ofMillis(50));
        return "User[Alice, Tier=Premium]";
    }

    private static int fetchUserOrdersCount() throws InterruptedException {
        Thread.sleep(Duration.ofMillis(30));
        return 42;
    }

    private static void demonstrateStructuredConcurrency() {
        System.out.println("  Launching concurrent tasks inside StructuredTaskScope...");
        long start = System.currentTimeMillis();

        try (var scope = StructuredTaskScope.open()) {
            // 親スレッドのコンテキスト下で、仮想スレッドとして並行サブタスクをフォーク
            var profileTask = scope.fork(() -> fetchUserProfile());
            var ordersTask = scope.fork(() -> fetchUserOrdersCount());

            // スコープの終端で全タスクの完了を待機 (join)
            scope.join();

            // 結果の安全な取得
            String profile = profileTask.get();
            int orders = ordersTask.get();

            long elapsed = System.currentTimeMillis() - start;
            System.out.println("  - Fetched Profile: " + profile);
            System.out.println("  - Fetched Orders:  " + orders);
            System.out.println("  - Both subtasks finished concurrently in " + elapsed + " ms (not sequential 80ms)!");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Task was interrupted: " + e.getMessage());
        }
    }

    // 内部の下請けサービスクラス (引数に userId を引き回さない)
    static class OrderService {
        public void processOrder() {
            // ScopedValue から現在のコンテキストを直接取得
            String user = CURRENT_USER_ID.orElse("Anonymous");
            String trace = TRACE_ID.orElse("NO-TRACE");
            System.out.println("    [OrderService] Processing order for User: '" + user + "' (Trace: " + trace + ")");
            dispatchNotification();
        }

        private void dispatchNotification() {
            String user = CURRENT_USER_ID.orElse("Anonymous");
            System.out.println("    [NotificationService] Sending confirmation to: " + user);
        }
    }

    private static void demonstrateScopedValue() {
        OrderService orderService = new OrderService();

        // 1. スコープ外では値は存在しない
        System.out.println("  Outside ScopedValue: " + CURRENT_USER_ID.orElse("Empty"));

        // 2. ScopedValue.where でバインドされたブロック内でのみ有効
        System.out.println("  Entering ScopedValue execution block...");
        ScopedValue.where(CURRENT_USER_ID, "usr_98765")
                   .where(TRACE_ID, "req-trace-abc-123")
                   .run(() -> {
                       orderService.processOrder();
                   });

        // 3. ブロックを抜けると自動的に値は解放され、メモリリークのリスクゼロ
        System.out.println("  Exited ScopedValue block. Is bound now? " + CURRENT_USER_ID.isBound());
    }
}
