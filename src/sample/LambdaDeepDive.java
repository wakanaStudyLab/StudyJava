package sample;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.*;

/**
 * ============================================================================
 * モジュール 05: Java ラムダ式 & 関数型プログラミング 完全理解コード
 * ============================================================================
 * 
 * 本クラスは LAMBDA.md に対応した実行可能な実践サンプル集です。
 * ラムダ式の構文、標準関数型インターフェース、メソッド参照、変数キャプチャ、
 * 例外処理、デザインパターンの関数型置換を網羅しています。
 */
public class LambdaDeepDive {

    public static void run() {
        demonstrateBasicSyntax();
        demonstrateStandardFunctionalInterfaces();
        demonstrateMethodReferences();
        demonstrateVariableCapture();
        demonstrateExceptionHandling();
        demonstratePracticalPatterns();
    }

    // =========================================================================
    // 1. 基本構文と省略記法
    // =========================================================================
    private static void demonstrateBasicSyntax() {
        System.out.println("=== 1. Basic Syntax and Concise Notations ===");

        // 完全な記法 (Full syntax with types, block, return)
        BinaryOperator<Integer> addFull = (Integer a, Integer b) -> {
            return a + b;
        };
        System.out.println("Addition result (full): " + addFull.apply(10, 20));

        // 型推論 + 単一式による省略記法 (Type inference + expression)
        BinaryOperator<Integer> addShort = (a, b) -> a + b;
        System.out.println("Addition result (short): " + addShort.apply(10, 20));

        // 引数1つの場合: カッコ () の省略 (Single argument omitting parentheses)
        Function<String, Integer> lengthFunc = s -> s.length();
        System.out.println("String length: " + lengthFunc.apply("Modern Java"));

        // 引数なしの場合: () は必須 (No argument requires empty parentheses)
        Supplier<String> greeting = () -> "Hello, Lambda!";
        System.out.println("Supplier result: " + greeting.get());
    }

    // =========================================================================
    // 2. 標準関数型インターフェース (java.util.function) & 合成
    // =========================================================================
    private static void demonstrateStandardFunctionalInterfaces() {
        System.out.println("\n=== 2. Standard Functional Interfaces (java.util.function) ===");

        // 1. Predicate<T>: 条件判定 (T -> boolean)
        Predicate<String> isLongerThan3 = s -> s.length() > 3;
        Predicate<String> startsWithJ = s -> s.startsWith("J");
        // 関数の合成 (and, or, negate)
        Predicate<String> javaAndLong = startsWithJ.and(isLongerThan3);
        System.out.println("Is 'Java' startsWith 'J' AND length > 3? " + javaAndLong.test("Java")); // true
        System.out.println("Is 'JS' startsWith 'J' AND length > 3?   " + javaAndLong.test("JS"));   // false

        // 2. Function<T, R>: 変換 (T -> R)
        Function<String, Integer> parse = Integer::parseInt;
        Function<Integer, Integer> square = n -> n * n;
        // andThen によるパイプライン合成: parse してから square
        Function<String, Integer> parseAndSquare = parse.andThen(square);
        System.out.println("Parse '12' then square: " + parseAndSquare.apply("12")); // 144

        // 3. Consumer<T>: 消費 (T -> void)
        Consumer<String> printer = s -> System.out.println("[PRINT] " + s);
        printer.accept("Consumer output processed");

        // 4. Supplier<T>: 遅延評価・生成 (() -> T)
        Supplier<Double> randomSupplier = Math::random;
        System.out.println("Lazy random value generated: " + randomSupplier.get());

        // 5. プリミティブ特化型 (Auto-boxing overhead: ZERO)
        IntPredicate isEven = n -> n % 2 == 0;
        System.out.println("Is 42 even? " + isEven.test(42));
    }

    // =========================================================================
    // 3. メソッド参照 (::) の4大パターン
    // =========================================================================
    private static void demonstrateMethodReferences() {
        System.out.println("\n=== 3. Method References (::) - 4 Patterns ===");

        // パターン1: 静的メソッド参照 (ClassName::staticMethod)
        // 等価: (s) -> Integer.parseInt(s)
        Function<String, Integer> p1 = Integer::parseInt;
        System.out.println("Pattern 1 (Static): " + p1.apply("42"));

        // パターン2: 特定インスタンスのメソッド参照 (instance::method)
        // 等価: (s) -> System.out.println(s)
        Consumer<String> p2 = System.out::println;
        p2.accept("Pattern 2 (Bound Instance): System.out::println");

        // パターン3: 任意インスタンスのメソッド参照 (ClassName::instanceMethod) ★第1引数がレシーバー★
        // 等価: (s) -> s.toUpperCase()
        Function<String, String> p3 = String::toUpperCase;
        System.out.println("Pattern 3 (Unbound Instance): " + p3.apply("lambda"));

        // パターン4: コンストラクタ参照 (ClassName::new)
        // 等価: () -> new ArrayList<>()
        Supplier<List<String>> p4 = ArrayList::new;
        List<String> list = p4.get();
        list.add("Item1");
        System.out.println("Pattern 4 (Constructor): " + list);
    }

    // =========================================================================
    // 4. 変数キャプチャと実質的 final (Effectively Final)
    // =========================================================================
    private static void demonstrateVariableCapture() {
        System.out.println("\n=== 4. Variable Capture & Lexical Scope ===");

        // 実質的 final な変数のキャプチャ
        String prefix = "User: "; // 再代入されていないためキャプチャ可能
        Consumer<String> greet = name -> System.out.println("  " + prefix + name);
        greet.accept("Alice");

        // ラムダ式内で状態をカウントアップしたい場合の正しいアプローチ (AtomicInteger)
        AtomicInteger counter = new AtomicInteger(0);
        List<String> items = List.of("Apple", "Banana", "Cherry");
        items.forEach(item -> {
            int current = counter.incrementAndGet();
            System.out.println("  Item " + current + ": " + item);
        });
        System.out.println("Total count: " + counter.get());
    }

    // =========================================================================
    // 5. チェック例外のハンドリング
    // =========================================================================
    @FunctionalInterface
    public interface ThrowingFunction<T, R, E extends Exception> {
        R apply(T t) throws E;

        // 非チェック例外 (RuntimeException) にラップする汎用コンバータ
        static <T, R> Function<T, R> unchecked(ThrowingFunction<T, R, ?> f) {
            return t -> {
                try {
                    return f.apply(t);
                } catch (Exception e) {
                    if (e instanceof RuntimeException re) throw re;
                    throw new RuntimeException(e);
                }
            };
        }
    }

    private static void demonstrateExceptionHandling() {
        System.out.println("\n=== 5. Handling Checked Exceptions in Streams ===");

        List<String> fakeFilePaths = List.of("sample.txt", "data.json");

        // 擬似的にチェック例外を投げる関数
        ThrowingFunction<String, String, IOException> readFile = path -> {
            if (path.endsWith(".json")) {
                return "{ \"status\": \"ok\" }";
            }
            return "Plain text content";
        };

        // ThrowingFunction.unchecked() を介すことで Stream の中で安全・クリーンに呼び出し可能
        List<String> contents = fakeFilePaths.stream()
                .map(ThrowingFunction.unchecked(readFile))
                .toList();

        System.out.println("Stream parsed contents: " + contents);
    }

    // =========================================================================
    // 6. 実務頻出の実践パターン (Strategy, 遅延評価, 現代的Comparator)
    // =========================================================================
    record Product(String category, String name, int price) {}

    // Strategy インターフェース
    @FunctionalInterface
    interface DiscountStrategy {
        int applyDiscount(int price);
    }

    private static void demonstratePracticalPatterns() {
        System.out.println("\n=== 6. Practical Design Patterns with Lambdas ===");

        // 1. Strategy パターン: クラスを作らずにラムダで戦略を切り替える
        DiscountStrategy regular = price -> price;
        DiscountStrategy holidaySale = price -> (int) (price * 0.8); // 20% OFF
        DiscountStrategy couponSale = price -> Math.max(0, price - 500); // 500円引き

        int originalPrice = 2000;
        System.out.println("Regular price: " + regular.applyDiscount(originalPrice));
        System.out.println("Holiday sale:  " + holidaySale.applyDiscount(originalPrice));
        System.out.println("Coupon sale:   " + couponSale.applyDiscount(originalPrice));

        // 2. 遅延評価 (Lazy Evaluation)
        computeIfDebugEnabled(() -> {
            // 重い計算シミュレーション
            return "Expensive report string generated at: " + System.currentTimeMillis();
        });

        // 3. 現代的 Comparator による多段ソート
        List<Product> products = new ArrayList<>(List.of(
                new Product("Book", "Java Master", 3500),
                new Product("Book", "Rust in Action", 4200),
                new Product("Device", "Mouse", 2500),
                new Product("Device", "Keyboard", 9800)
        ));

        // カテゴリ昇順 → 価格降順
        products.sort(
                Comparator.comparing(Product::category)
                        .thenComparing(Comparator.comparingInt(Product::price).reversed())
        );

        System.out.println("Multi-level sorted products:");
        products.forEach(p -> System.out.println("  " + p));
    }

    private static void computeIfDebugEnabled(Supplier<String> messageSupplier) {
        boolean isDebug = true; // デバッグモードフラグ
        if (isDebug) {
            // 必要な時だけ Supplier.get() を呼び出す
            System.out.println("[DEBUG] " + messageSupplier.get());
        }
    }
}
