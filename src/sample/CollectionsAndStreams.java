package sample;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * ============================================================================
 * 02: コレクション & Stream API (Collections & Streams)
 * ============================================================================
 * 
 * 【他言語経験者（Rust, C#, Go）向け要点】
 * 1. コレクションの可変性 (Mutability):
 *    - `List.of(...)`, `Set.of(...)`, `Map.of(...)`: Java 9+ で追加された「不変（Immutable）」コレクション。
 *      要素の追加・削除を呼ぶと `UnsupportedOperationException` をスローします。
 *    - `new ArrayList<>()`, `new HashMap<>()`: 従来の「可変（Mutable）」コレクション。
 * 
 * 2. C# LINQ / Rust Iterator との対応表:
 *    | 操作           | Java Stream API     | C# LINQ         | Rust Iterator       |
 *    |----------------|---------------------|-----------------|---------------------|
 *    | フィルタ       | .filter(x -> ...)   | .Where(x => ...) | .filter(|x| ...)    |
 *    | 変換・射影     | .map(x -> ...)      | .Select(x => ...) | .map(|x| ...)       |
 *    | 平坦化         | .flatMap(x -> ...)  | .SelectMany(...) | .flat_map(|x| ...)  |
 *    | リスト化       | .toList()           | .ToList()       | .collect::<Vec<_>>()|
 *    | 集計・畳み込み | .reduce(...)        | .Aggregate(...) | .fold(...) / reduce |
 *    | グループ化     | .collect(groupingBy)| .GroupBy(...)   | (外部クレート等)    |
 * 
 * 3. 遅延評価 (Lazy Evaluation):
 *    - 中間操作 (filter, map等) は終端操作 (toList, count, findFirst等) が呼ばれるまで一切実行されません。
 * 
 * 4. プリミティブ特化 Stream (IntStream, LongStream, DoubleStream):
 *    - プリミティブのボクシング (int -> Integer) によるメモリ確保・GC負荷を回避するために必須です。
 */
public class CollectionsAndStreams {

    public static void run() {
        System.out.println("=== 1. Creating Collections (Immutable vs Mutable) ===");
        demonstrateCollections();

        System.out.println("\n=== 2. Stream API Pipeline (LINQ / Rust Iterator) ===");
        demonstrateStreamPipeline();

        System.out.println("\n=== 3. Advanced Aggregation (Collectors.groupingBy & Stats) ===");
        demonstrateAdvancedCollectors();

        System.out.println("\n=== 4. Primitive Specialized Streams (IntStream) ===");
        demonstratePrimitiveStreams();
    }

    private static void demonstrateCollections() {
        // --- 不変リスト (List.of) ---
        // 要素数固定、要素の変更不可、null不可
        List<String> immutableList = List.of("Apple", "Banana", "Cherry");
        System.out.println("Immutable List: " + immutableList);

        // --- 可変リスト (ArrayList) ---
        List<String> mutableList = new ArrayList<>(immutableList);
        mutableList.add("Date"); // OK
        System.out.println("Mutable List: " + mutableList);

        // --- 不変マップ (Map.of / Map.ofEntries) ---
        Map<String, Integer> scoreMap = Map.of(
            "Alice", 95,
            "Bob", 80,
            "Charlie", 88
        );
        System.out.println("Map.of: " + scoreMap);
        System.out.println("Alice's score: " + scoreMap.get("Alice"));
    }

    public record Product(String id, String name, String category, int price, int stock) {}

    private static void demonstrateStreamPipeline() {
        List<Product> products = List.of(
            new Product("p1", "MacBook Pro", "Electronics", 250000, 5),
            new Product("p2", "Mechanical Keyboard", "Electronics", 18000, 0), // 在庫なし
            new Product("p3", "Clean Code Book", "Books", 3800, 12),
            new Product("p4", "Rust in Action", "Books", 4200, 8),
            new Product("p5", "Coffee Mug", "Misc", 1500, 20)
        );

        // クエリ: 在庫がある Electronics の商品名を大文字にして価格順にソートし、Listとして取得
        List<String> availableElectronicsNames = products.stream()
            .filter(p -> p.category().equals("Electronics")) // C#: .Where
            .filter(p -> p.stock() > 0)
            .sorted(Comparator.comparingInt(Product::price))  // C#: .OrderBy(p => p.price) / メソッド参照構文 (Class::method)
            .map(Product::name)                              // C#: .Select(p => p.name)
            .map(String::toUpperCase)
            .toList();                                       // Java 16+ の簡潔な終端操作 (従来の .collect(Collectors.toList()) の短縮)

        System.out.println("Available Electronics: " + availableElectronicsNames);

        // 集計操作: 全在庫商品の合計価格 (reduce / mapToInt)
        int totalInventoryValue = products.stream()
            .mapToInt(p -> p.price() * p.stock())
            .sum();
        System.out.println("Total Inventory Value: JPY " + String.format("%,d", totalInventoryValue));
    }

    private static void demonstrateAdvancedCollectors() {
        List<Product> products = List.of(
            new Product("p1", "MacBook Pro", "Electronics", 250000, 5),
            new Product("p2", "Mechanical Keyboard", "Electronics", 18000, 10),
            new Product("p3", "Clean Code Book", "Books", 3800, 12),
            new Product("p4", "Rust in Action", "Books", 4200, 8),
            new Product("p5", "Coffee Mug", "Misc", 1500, 20)
        );

        // 1. カテゴリ別にグループ化 (C#の GroupBy) -> Map<Category, List<Product>>
        Map<String, List<Product>> byCategory = products.stream()
            .collect(Collectors.groupingBy(Product::category));
        System.out.println("Categories: " + byCategory.keySet());

        // 2. カテゴリ別の平均価格を計算 (下流コレクター: Downstream Collector)
        Map<String, Double> averagePriceByCategory = products.stream()
            .collect(Collectors.groupingBy(
                Product::category,
                Collectors.averagingInt(Product::price)
            ));
        System.out.println("Avg price by category: " + averagePriceByCategory);

        // 3. 文字列の結合 (Collectors.joining)
        String namesJoined = products.stream()
            .map(Product::name)
            .collect(Collectors.joining(", ", "[Products: ", "]"));
        System.out.println("Joined Names: " + namesJoined);
    }

    private static void demonstratePrimitiveStreams() {
        // --- IntStream による高速ループ・範囲生成 (Rustの 0..10 / Goのループ相当) ---
        int sumOfEvens = IntStream.rangeClosed(1, 100) // 1 から 100 含む
            .filter(n -> n % 2 == 0)
            .sum();
        System.out.println("Sum of evens (1..100): " + sumOfEvens);

        // 統計サマリー (IntSummaryStatistics: min, max, average, sum, count を1パスで取得)
        IntSummaryStatistics stats = IntStream.of(10, 25, 45, 90, 32, 78)
            .summaryStatistics();
        System.out.println("Stats -> Count: " + stats.getCount() 
            + ", Min: " + stats.getMin() 
            + ", Max: " + stats.getMax() 
            + ", Avg: " + stats.getAverage());
    }
}
