package sample;

/**
 * ============================================================================
 * 15: レコードパターン & 高度なパターンマッチング (Record Patterns & Pattern Matching)
 * ============================================================================
 * 
 * 【他言語経験者（Rust, C#, Go）向け要点】
 * 1. レコードパターン（分解代入: Deconstruction Patterns - Java 21+）:
 *    - Rust や C# のような言語では、構造体やタプルのフィールドをパターンマッチングの段階で
 *      直接変数に展開（分解代入: Destructuring）できます。
 *    - Java 21 で導入された **Record Patterns (JEP 440)** により、
 *      `if (obj instanceof Point(var x, var y))` や `case Point(var x, var y)` のように
 *      getter を呼ぶことなく直接構成要素を展開できるようになりました。
 * 
 * 2. 無名変数 & 無名パターン (`_` - Java 22+ 正式化 JEP 456):
 *    - 使わない変数は Rust や Go と同様に `_`（アンダースコア）で破棄・明示的無視が可能。
 * 
 * 3. 網羅性検査 (Exhaustiveness Check):
 *    - `sealed interface` とレコードパターンを組み合わせることで、
 *      すべての分岐（バリアント）がカバーされているかをコンパイラが厳格に検証。
 *      `default` 句を書かずに全ケースを網羅することで、将来ケースが追加された際に
 *      コンパイルエラーとして検知可能（Rust の `match` と全く同じ堅牢性）。
 */
public class RecordPatternsAndAdvancedMatching {

    // --- ドメインモデル (Sealed Hierarchies & Records) ---
    public record Point(double x, double y) {}

    public sealed interface DomainEvent permits PaymentSuccess, RefundProcessed, AccountBlocked {}

    public record PaymentSuccess(String txId, String userId, long amountCents, Point location) implements DomainEvent {}
    public record RefundProcessed(String txId, String originalTxId, long refundAmountCents, String reason) implements DomainEvent {}
    public record AccountBlocked(String userId, String securityReason) implements DomainEvent {}

    public static void run() {
        System.out.println("=== 1. Basic Record Deconstruction Patterns ===");
        demonstrateBasicRecordPatterns();

        System.out.println("\n=== 2. Nested Record Deconstruction & Deep Matching ===");
        demonstrateNestedRecordPatterns();

        System.out.println("\n=== 3. Unnamed Variables & Patterns (_) (JEP 456) ===");
        demonstrateUnnamedPatterns();

        System.out.println("\n=== 4. Exhaustive Domain Event Processing with Sealed Interface ===");
        demonstrateExhaustiveDomainProcessing();
    }

    /**
     * 基本的なレコードパターンの分解代入
     */
    private static void demonstrateBasicRecordPatterns() {
        Object obj = new Point(12.5, 48.0);

        // 従来のやり方: 型チェックしてキャスト後、getterを呼ぶ
        if (obj instanceof Point p) {
            System.out.printf("  [Old way] x=%.1f, y=%.1f%n", p.x(), p.y());
        }

        // Java 21+ レコードパターン: マッチングと同時にフィールドを変数に直接分解
        if (obj instanceof Point(var x, var y)) {
            System.out.printf("  [Modern Record Pattern] Direct deconstruction: x=%.1f, y=%.1f%n", x, y);
        }
    }

    /**
     * ネストしたレコードの深層分解マッチング
     */
    private static void demonstrateNestedRecordPatterns() {
        DomainEvent event = new PaymentSuccess(
            "TX-9988",
            "usr_alice",
            15000,
            new Point(35.6895, 139.6917) // 東京の座標
        );

        // ネストした Point の x, y もワンステップで深層分解！
        if (event instanceof PaymentSuccess(var txId, var userId, var amount, Point(var lat, var lon))) {
            System.out.println("  Deep nested matching extracted:");
            System.out.printf("    * Transaction: %s, User: %s, Amount: %d JPY%n", txId, userId, amount);
            System.out.printf("    * GPS Coordinates: Latitude=%.4f, Longitude=%.4f%n", lat, lon);
        }
    }

    /**
     * 無名変数と無名パターン (`_`)
     * 不要なフィールドを明示的に無視し、コードの意図をクリアにする
     */
    private static void demonstrateUnnamedPatterns() {
        DomainEvent event = new PaymentSuccess("TX-5544", "usr_bob", 8500, new Point(0, 0));

        // txId や location は不要で、userId と amount だけが必要な場合
        // Rust の `PaymentSuccess { userId, amount, .. }` や Go の `_, amount := ...` と同等
        if (event instanceof PaymentSuccess(_, var userId, var amount, _)) {
            System.out.printf("  Unnamed Pattern: User %s paid %d JPY (txId & location safely ignored)%n", userId, amount);
        }

        // try-catch で例外オブジェクトを使わない場合にも _ を使用可能
        try {
            int parsed = Integer.parseInt("not_a_number");
        } catch (NumberFormatException _) {
            System.out.println("  Unnamed catch variable: Handled parse failure without unused 'e' variable.");
        }
    }

    /**
     * Sealed Hierarchy と レコードパターン switch 式による完全性（網羅性）検証
     */
    private static void demonstrateExhaustiveDomainProcessing() {
        DomainEvent[] events = {
            new PaymentSuccess("TX-001", "usr_1", 12000, new Point(35.0, 139.0)),
            new PaymentSuccess("TX-002", "usr_1", 950000, new Point(35.0, 139.0)), // 高額決済
            new RefundProcessed("REF-001", "TX-001", 12000, "Customer Request"),
            new AccountBlocked("usr_evil", "Suspicious brute-force login attempts")
        };

        for (DomainEvent ev : events) {
            // default 句なし！全パターンが網羅されていることをコンパイラが保証
            String summary = switch (ev) {
                // ガード節 (when) による高額決済の優先処理
                case PaymentSuccess(var id, var user, var amount, _) when amount >= 500000 ->
                    String.format("[ALERT: HIGH VALUE] Large payment %d JPY by %s (TX: %s)", amount, user, id);

                // 通常の決済成功
                case PaymentSuccess(var id, var user, var amount, Point(var lat, var lon)) ->
                    String.format("[PAYMENT] %s paid %d JPY at (%.1f, %.1f) (TX: %s)", user, amount, lat, lon, id);

                // 返金処理
                case RefundProcessed(var refId, var origTx, var amount, var reason) ->
                    String.format("[REFUND] %d JPY refunded for %s (Reason: '%s', Ref: %s)", amount, origTx, reason, refId);

                // アカウント凍結
                case AccountBlocked(var user, var reason) ->
                    String.format("[SECURITY] Account %s BLOCKED! Reason: %s", user, reason);
            };

            System.out.println("  " + summary);
        }
    }
}
