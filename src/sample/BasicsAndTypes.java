package sample;

import java.util.Objects;

/**
 * ============================================================================
 * 01: 基本型・モダン構文・型システム (Basics & Type System)
 * ============================================================================
 * 
 * 【他言語経験者（Rust, C#, Go）向け要点】
 * 1. プリミティブ型 vs 参照型:
 *    - int, double, boolean 等は値そのもの（スタック/インライン配置）。
 *    - Integer, Double, Boolean 等はラッパークラス（ヒープ参照、nullを取り得る）。
 *    - Javaのジェネリクス (List<T>) は参照型しか取れないため、int を入れると Integer に
 *      自動変換（Auto-Boxing）され、オーバーヘッドとnullのリスクが生じます。
 * 
 * 2. 同一性 (==) vs 同値性 (.equals()):
 *    - C#と決定的に異なる点: Javaの `==` は演算子オーバーロードがなく、参照の一致を比較します。
 *    - 文字列やオブジェクトの中身比較は必ず `equals()` または `Objects.equals(a, b)` を使います。
 * 
 * 3. record (Java 16+):
 *    - C#の `record`、Rust/Goの `struct` に相当する不変データキャリア。
 *    - 全フィールドの private final、getter (名前は getX() ではなく x())、equals/hashCode/toString が自動生成。
 * 
 * 4. sealed class / interface (Java 17+):
 *    - 継承・実装できる型を制限（permits）する機能。
 *    - Rustの `enum` (代数的データ型: ADT) や C#の判別共用体パターンを表現できます。
 * 
 * 5. switch 式 & パターンマッチング (Java 21+):
 *    - 式（値を返す）としての switch、instanceof パターンマッチング、when ガード節。
 */
public class BasicsAndTypes {

    public static void run() {
        System.out.println("=== 1. Primitive vs Reference Types & Equality ===");
        demonstrateTypesAndEquality();

        System.out.println("\n=== 2. Modern Syntax: var and Text Blocks ===");
        demonstrateModernSyntax();

        System.out.println("\n=== 3. Record (Immutable Data Carrier) ===");
        demonstrateRecords();

        System.out.println("\n=== 4. Sealed Class & Pattern Matching (Rust Enum/ADT) ===");
        demonstrateSealedAndPatternMatching();
    }

    private static void demonstrateTypesAndEquality() {
        // --- プリミティブ vs ラッパー ---
        int pInt = 42;             // スタック上の値 (Goの int, Rustの i32, C#の int)
        Integer rInt = 42;          // ヒープ上のオブジェクト (Auto-Boxing)

        // --- 同一性 (==) の罠 ---
        // Javaは -128 〜 127 のIntegerはキャッシュされますが、それ以外は別インスタンスになります。
        Integer a = 1000;
        Integer b = 1000;
        System.out.println("a == b (Reference Comparison): " + (a == b));             // false! (C#プログラマが最も引っかかる罠)
        System.out.println("a.equals(b) (Value Comparison): " + a.equals(b));       // true
        System.out.println("Objects.equals(a, b) (Null-safe): " + Objects.equals(a, b)); // null安全な推奨比較

        // 文字列の比較
        String s1 = "hello";
        String s2 = new String("hello");
        System.out.println("s1 == s2 (Reference): " + (s1 == s2));         // false (別オブジェクト)
        System.out.println("s1.equals(s2) (Value): " + s1.equals(s2)); // true (値の一致)
    }

    private static void demonstrateModernSyntax() {
        // --- 型推論 var (Java 10+) ---
        // C#の var, Goの :=, Rustの let と同等。型はコンパイル時に静的に決定されます。
        // ローカル変数のみで使用可能（フィールドやメソッド引数・戻り値には不可）。
        var message = "Hello Modern Java!"; // String と推論
        var count = 10;                     // int と推論

        // --- Text Blocks (Java 15+) ---
        // 3つのダブルクォートで複数行文字列。インデントは自動でストリップ（正規化）されます。
        // C#の raw string literals ($"""...""") や Rustの r#"..."# に相当。
        String json = """
                {
                    "name": "Antigravity",
                    "version": 21,
                    "features": ["Virtual Threads", "Pattern Matching", "Records"]
                }
                """;
        System.out.println("Text Block JSON:\n" + json.trim());
    }

    // =========================================================================
    // Record: ボイラープレートを排除した不変データクラス (Java 16+)
    // =========================================================================
    // 従来のJava Bean（Getter/Setter/toString/equals/hashCode）を1行で定義可能。
    // フィールドは暗黙的に `private final` になります。
    public record User(String id, String name, int age) {
        
        // コンパクトコンストラクタ: バリデーションや正規化に利用
        public User {
            Objects.requireNonNull(id, "id must not be null");
            Objects.requireNonNull(name, "name must not be null");
            if (age < 0) {
                throw new IllegalArgumentException("age cannot be negative: " + age);
            }
            name = name.strip(); // 正規化
        }

        // カスタムメソッドの追加も可能
        public boolean isAdult() {
            return age >= 18;
        }
    }

    private static void demonstrateRecords() {
        User user1 = new User("u001", "  Alice  ", 25);
        User user2 = new User("u001", "Alice", 25);

        // getterは getX() ではなく x() で呼ぶ
        System.out.println("User: " + user1.name() + ", Age: " + user1.age());
        System.out.println("Auto-generated toString(): " + user1);
        System.out.println("Auto-generated equals():   " + user1.equals(user2)); // true (値同値性)
        System.out.println("isAdult(): " + user1.isAdult());
    }

    // =========================================================================
    // Sealed Interface & Classes: 型安全な代数的データ型 (Java 17+)
    // =========================================================================
    // permits で明示されたクラス/レコード以外はこのインターフェースを実装できない。
    // これにより、コンパイラは「網羅性チェック (Exhaustiveness)」が可能になります。
    public sealed interface PaymentMethod permits CreditCard, BankTransfer, CryptoCurrency {}

    public record CreditCard(String cardNumber, String holderName) implements PaymentMethod {}
    public record BankTransfer(String accountNumber, String bankCode) implements PaymentMethod {}
    public record CryptoCurrency(String walletAddress, String chain) implements PaymentMethod {}

    private static void demonstrateSealedAndPatternMatching() {
        PaymentMethod payment = new CryptoCurrency("0x1234abcd", "Ethereum");

        // --- Java 21+ switch パターンマッチング ---
        // 1. sealed interface のおかげで全ケースが網羅されていると default 句が不要！ (Rustの match と同様)
        // 2. レコードパターンによるフィールドの即時分解 (Destructuring)
        // 3. when ガード節による追加条件フィルタ
        String result = switch (payment) {
            case CreditCard(var num, var holder) -> 
                "Credit Card [" + holder + " / " + maskCard(num) + "]";
                
            case BankTransfer(var acc, var code) -> 
                "Bank Transfer [Bank: " + code + ", Acc: " + acc + "]";
                
            case CryptoCurrency(var wallet, var chain) when chain.equalsIgnoreCase("Ethereum") -> 
                "Ethereum Crypto Transfer to: " + wallet;
                
            case CryptoCurrency(var wallet, var chain) -> 
                "Other Crypto (" + chain + ") to: " + wallet;
        };

        System.out.println("Payment Result: " + result);

        // --- instanceof パターンマッチング (Java 16+) ---
        // 従来の「instanceof で確認してからキャスト」が1行で可能 (C#の is 演算子と同等)
        Object obj = "Hello Pattern Matching";
        if (obj instanceof String str && str.length() > 5) {
            System.out.println("String length > 5: " + str.toUpperCase());
        }
    }

    private static String maskCard(String num) {
        return "****-****-****-" + (num.length() >= 4 ? num.substring(num.length() - 4) : "0000");
    }
}
