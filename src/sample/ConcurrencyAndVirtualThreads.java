package sample;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

/**
 * ============================================================================
 * 04: 並行処理・非同期・Virtual Threads (Concurrency & Virtual Threads)
 * ============================================================================
 * 
 * 【他言語経験者（Rust, C#, Go）向け要点】
 * 1. Virtual Threads (仮想スレッド - Java 21+):
 *    - 【Goの goroutine とほぼ同等】のM:Nグリーンスレッド機構 (Project Loom)。
 *    - 従来のOSスレッド (Platform Thread) はスタックに約1MBを消費し、数千個が限界でした。
 *    - Virtual Threads はヒープ上に数十バイト〜数KBで生成され、数十万〜数百万個を同時に起動可能。
 *    - ブロッキングI/O（ネットワーク、ファイル、Thread.sleep等）が発生すると自動でキャリアスレッドを解放。
 *    - reactive/async-await (C# Task, Rust async) を使わなくても「素の同期コード」で最高スループットを達成できます。
 * 
 * 2. CompletableFuture<T> (Java 8+):
 *    - C#の `Task<T>`、JavaScriptの `Promise`、Rustの `Future` に相当する合成可能な非同期パイプライン。
 * 
 * 3. スレッドセーフなデータ構造 & アトミック操作:
 *    - `ConcurrentHashMap`: 高速な並行マップ (Goの sync.Map、C#の ConcurrentDictionary 相当)。
 *    - `AtomicInteger` / `LongAdder`: CAS (Compare-And-Swap) によるロックフリーなカウンター。
 */
public class ConcurrencyAndVirtualThreads {

    public static void run() {
        System.out.println("=== 1. Virtual Threads (Java 21+ / Go goroutines equivalent) ===");
        demonstrateVirtualThreads();

        System.out.println("\n=== 2. CompletableFuture (C# Task / JS Promise) ===");
        demonstrateCompletableFuture();

        System.out.println("\n=== 3. Concurrent Collections & Atomic Primitives ===");
        demonstrateConcurrentPrimitives();
    }

    private static void demonstrateVirtualThreads() {
        int taskCount = 10_000;
        System.out.println("Spawning " + taskCount + " Virtual Threads concurrently to simulate I/O wait...");

        Instant start = Instant.now();

        // Java 21 の推奨エグゼキューター: タスクごとに仮想スレッドを新規生成する Executor
        // try-with-resources を使うと、全タスクの完了を待機 (auto close & join) します (Structured Concurrency 的な動作)
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            IntStream.range(0, taskCount).forEach(i -> {
                executor.submit(() -> {
                    // ブロッキングI/O（スリープ）を実行
                    // Virtual Thread はOSスレッドをブロックせず、キャリアスレッドを一時的に明け渡します
                    try {
                        Thread.sleep(Duration.ofMillis(100));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    return i;
                });
            });
        } // ここで10,000タスクすべての完了が保証される

        Instant end = Instant.now();
        System.out.println("10,000 tasks finished in: " + Duration.between(start, end).toMillis() + " ms");
        System.out.println("(Creating 10,000 Platform/OS threads would cause OutOfMemoryError or huge overhead)");
    }

    private static void demonstrateCompletableFuture() {
        // C# の Task.Run(() => ...) に相当
        CompletableFuture<String> fetchUserTask = CompletableFuture.supplyAsync(() -> {
            simulateLatency(150);
            return "User: Alice";
        });

        CompletableFuture<String> fetchOrderTask = CompletableFuture.supplyAsync(() -> {
            simulateLatency(200);
            return "Order: #98765 (Item: Mechanical Keyboard)";
        });

        // 2つの非同期タスクを並行実行し、両方完了したら結果を結合 (C#の Task.WhenAll)
        CompletableFuture<String> combinedTask = fetchUserTask.thenCombine(
            fetchOrderTask,
            (user, order) -> user + " -> " + order
        );

        // 結果をブロックして取得 (join または get)
        String summary = combinedTask.join();
        System.out.println("CompletableFuture Result: " + summary);

        // 例外ハンドリングパイプライン (exceptionally / handle)
        CompletableFuture<String> safeTask = CompletableFuture.supplyAsync(() -> {
            boolean shouldFail = true;
            if (shouldFail) {
                throw new RuntimeException("API Connection Failed");
            }
            return "Success";
        }).exceptionally(ex -> "Fallback Data (Error: " + ex.getMessage() + ")");

        System.out.println("Exception Fallback Result: " + safeTask.join());
    }

    private static void demonstrateConcurrentPrimitives() {
        // 1. ConcurrentHashMap (複数スレッドからの安全な読み書き)
        ConcurrentMap<String, Integer> wordCounts = new ConcurrentHashMap<>();
        
        // computeIfAbsent / merge などを使ったアトミック更新
        wordCounts.merge("java", 1, Integer::sum);
        wordCounts.merge("java", 1, Integer::sum);
        wordCounts.merge("rust", 1, Integer::sum);
        System.out.println("Word Counts: " + wordCounts);

        // 2. AtomicInteger (ロックフリーなカウンター / CAS命令)
        AtomicInteger atomicCounter = new AtomicInteger(0);
        
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            IntStream.range(0, 1000).forEach(i -> {
                executor.submit(atomicCounter::incrementAndGet);
            });
        }

        System.out.println("AtomicCounter (Expected 1000): " + atomicCounter.get());
    }

    private static void simulateLatency(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
