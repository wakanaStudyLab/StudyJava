package sample;

/**
 * ============================================================================
 * Java 最速習得サンプル - メインエントリーポイント
 * ============================================================================
 * 
 * 実行方法:
 *   1. プロジェクトルートディレクトリ (sample ディレクトリ) で:
 *      javac -encoding UTF-8 -d bin src/sample/*.java
 *      java -cp bin sample.Main
 * 
 *   2. または VS Code の「Run (F5)」ボタンで直接実行可能
 */
public class Main {

    public static void main(String[] args) {
        printBanner("MODERN JAVA CRASH COURSE (For Rust / C# / Go Developers)");

        // =====================================================================
        // 【解説: () -> ConcurrencyAndVirtualThreads.run() とは？】
        // 
        // 結論: Javaの「ラムダ式（無名関数）」です。
        //
        // 他言語での書き方との完全一致:
        //   - C#:    () => ConcurrencyAndVirtualThreads.Run()   (ラムダ式 / Action デリゲート)
        //   - Rust:  || ConcurrencyAndVirtualThreads::run()     (クロージャ / impl Fn())
        //   - Go:    func() { ConcurrencyAndVirtualThreads.Run() } (無名関数)
        //
        // 仕組み (関数型インターフェース / SAM):
        //   Javaには言語組み込みの「関数型」が存在しません。
        //   代わりに「抽象メソッドが1つだけ定義されたインターフェース（SAM: Single Abstract Method）」を
        //   関数の型として扱います。
        //   下の section メソッドが第2引数で受け取っている `Runnable` は
        //   `public void run();` を1つだけ持つ標準の関数型インターフェースです。
        //   コンパイラが `() -> ...` を自動的に `Runnable` の実装オブジェクトとして解釈します。
        //
        // ※ さらに簡潔な「メソッド参照（::）」構文:
        //   引数をそのまま渡すだけの処理は `クラス名::メソッド名` と書くこともできます。
        //   例: `BasicsAndTypes::run` (Rustの関数ポインタ渡しやC#のデリゲート代入と同等)
        // =====================================================================

        // ラムダ式による呼び出し例:
        section("01: Primitive vs Reference Types, Records, Pattern Matching", () -> BasicsAndTypes.run());
        section("02: Collections & Stream API (LINQ / Rust Iterator Equivalent)", () -> CollectionsAndStreams.run());
        
        // メソッド参照 (::) による呼び出し例（上記と全く同じ意味になります）:
        section("03: Exception Handling, try-with-resources, Optional", ExceptionAndResource::run);
        section("04: Concurrency, Virtual Threads (Go goroutines equivalent), CompletableFuture", ConcurrencyAndVirtualThreads::run);
        section("05: Lambda Expressions & Functional Programming Deep Dive", LambdaDeepDive::run);

        // 新設モジュール (06〜12):
        section("06: Generics Deep Dive, PECS Principle, Type Erasure", GenericsDeepDive::run);
        section("07: Structured Concurrency & Scoped Values (Project Loom Phase 2)", StructuredConcurrencyAndScopedValues::run);
        section("08: Modern File I/O (NIO.2 Files/Path) & Standard HttpClient", ModernIOAndHttpClient::run);
        section("09: Reflection, Custom Annotations & Dynamic Proxies (Spring AOP Inside)", ReflectionAndProxies::run);
        section("10: JVM Internals, Memory Model (JMM), volatile & Modern GC", JvmMemoryAndPerformance::run);
        section("11: Sequenced Collections & Defensive Copying (Java 21+)", SequencedCollectionsAndSafety::run);
        section("12: Modern Design Patterns in Java 21+ (Strategy, Sealed State, Flow API)", ModernDesignPatterns::run);

        // 最新鋭モジュール (13〜16: Java 21〜26 最新世代):
        section("13: Foreign Function & Memory API (Project Panama, Off-Heap & Native C)", ForeignFunctionAndMemoryAPI::run);
        section("14: Stream Gatherers & Custom Pipelines (JEP 461/473/485)", StreamGatherersAndPipelines::run);
        section("15: Record Patterns, Deconstruction & Unnamed Variables (_) (JEP 440/456)", RecordPatternsAndAdvancedMatching::run);
        section("16: Flexible Constructor Bodies & Modern Clean Architecture (JEP 482)", FlexibleConstructorAndModernLanguage::run);

        printBanner("ALL 16 TUTORIAL MODULES COMPLETED SUCCESSFULLY!");
    }

    /**
     * @param title  セクション見出し
     * @param action 実行したい処理（引数なし・戻り値なしの関数型インターフェース Runnable）
     */
    private static void section(String title, Runnable action) {
        System.out.println("\n################################################################");
        System.out.println("# " + title);
        System.out.println("################################################################\n");
        // 引数として渡されたラムダ式 / メソッド参照を実行する
        action.run();
    }

    private static void printBanner(String message) {
        System.out.println("\n" + "=".repeat(64));
        System.out.println("  " + message);
        System.out.println("=".repeat(64) + "\n");
    }
}
