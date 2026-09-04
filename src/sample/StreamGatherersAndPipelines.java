package sample;

import java.util.List;
import java.util.Objects;
import java.util.stream.Gatherer;
import java.util.stream.Gatherers;

/**
 * ============================================================================
 * 14: Stream Gatherers と高度な中間操作 (Stream Gatherers - JEP 461 / 473 / 485)
 * ============================================================================
 * 
 * 【他言語経験者（Rust, C#, Go）向け要点】
 * 1. Java 8 以来 10年ぶりの Stream API 最大進化:
 *    - 従来の Java Stream API は、終端操作（`stream.collect(...)`）しかカスタム拡張できず、
 *      「ストリームの中間操作でチャンク分割したい」「スライディングウィンドウを適用したい」
 *      「累積和を取りたい」といった処理が極めて困難でした（外部ライブラリや非効率なインデックス参照が必要）。
 *    - **Stream Gatherers** により、中間パイプライン操作（`stream.gather(...)`）を
 *      100% 自由に定義・合成できるようになりました。
 * 
 * 2. 他言語との対比表:
 *    | 操作 | Java Stream Gatherers | C# LINQ | Rust Iterator |
 *    | :--- | :--- | :--- | :--- |
 *    | 固定チャンク分割 | `Gatherers.windowFixed(n)` | `.Chunk(n)` | `.chunks(n)` (itertools) |
 *    | 移動ウィンドウ | `Gatherers.windowSliding(n)` | (外部 / Zip) | `.windows(n)` |
 *    | 累積状態生成 | `Gatherers.scan(...)` | `.Scan(...)` (MoreLINQ) | `.scan(...)` |
 *    | 並行中間マッピング | `Gatherers.mapConcurrent(n, fn)` | `AsParallel().Select(...)` | `rayon::par_iter` |
 *    | 自作中間操作 | `Gatherer.ofSequential(...)` | `yield return` | `impl Iterator` |
 */
public class StreamGatherersAndPipelines {

    public static void run() {
        System.out.println("=== 1. Batching & Chunking: Gatherers.windowFixed ===");
        demonstrateWindowFixed();

        System.out.println("\n=== 2. Time-Series Analysis: Gatherers.windowSliding (Moving Average) ===");
        demonstrateWindowSliding();

        System.out.println("\n=== 3. Stateful Accumulation: Gatherers.scan (Running Total) ===");
        demonstrateScan();

        System.out.println("\n=== 4. Virtual Thread Parallelism: Gatherers.mapConcurrent ===");
        demonstrateMapConcurrent();

        System.out.println("\n=== 5. Custom Gatherer: Consecutive Deduplication (distinctUntilChanged) ===");
        demonstrateCustomGatherer();
    }

    /**
     * 1. 固定サイズチャンク化: 大量データのバッチインサートやAPIバルク送信に最適
     */
    private static void demonstrateWindowFixed() {
        List<String> userIds = List.of(
            "usr_01", "usr_02", "usr_03", "usr_04", "usr_05",
            "usr_06", "usr_07", "usr_08"
        );

        // 3件ずつのバッチに自動分割（余りは最後のリストに格納）
        List<List<String>> batches = userIds.stream()
            .gather(Gatherers.windowFixed(3))
            .toList();

        System.out.println("  Original Items: " + userIds.size());
        for (int i = 0; i < batches.size(); i++) {
            System.out.printf("  Batch #%d (size %d): %s%n", i + 1, batches.get(i).size(), batches.get(i));
        }
    }

    /**
     * 2. スライディングウィンドウ: 移動平均（Moving Average）や時系列トレンド分析
     */
    private static void demonstrateWindowSliding() {
        // 日ごとの株価やセンサートラフィック
        List<Double> dailyTemperatures = List.of(20.5, 21.0, 23.5, 26.0, 25.0, 22.0, 19.5);

        // 3日間のスライディングウィンドウで移動平均を算出
        List<Double> movingAverages = dailyTemperatures.stream()
            .gather(Gatherers.windowSliding(3))
            .map(window -> {
                double avg = window.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                return Math.round(avg * 100.0) / 100.0;
            })
            .toList();

        System.out.println("  Temperatures:    " + dailyTemperatures);
        System.out.println("  3-Day Sliding Window Moving Averages: " + movingAverages);
    }

    /**
     * 3. 累積状態生成 (Running Total / Cumulative Sum)
     */
    private static void demonstrateScan() {
        List<Integer> transactions = List.of(1000, -200, 500, -100, 3000);

        // 初期残高 5000 円から、各取引後の累積残高ストリームを生成
        List<Integer> accountBalances = transactions.stream()
            .gather(Gatherers.scan(() -> 5000, (currentBalance, tx) -> currentBalance + tx))
            .toList();

        System.out.println("  Transactions:     " + transactions);
        System.out.println("  Running Balances: " + accountBalances);
    }

    /**
     * 4. 仮想スレッドによる並行マッピング (mapConcurrent)
     * 指定した並行度 (maxConcurrency) で仮想スレッドを自動起動し、順序を維持して結果を収集
     */
    private static void demonstrateMapConcurrent() {
        List<String> domains = List.of("google.com", "github.com", "openjdk.org", "rust-lang.org");

        long start = System.currentTimeMillis();

        List<String> pingResults = domains.stream()
            .gather(Gatherers.mapConcurrent(4, domain -> {
                // 擬似的なネットワークI/O待ち（仮想スレッド上で非同期に待機）
                try {
                    Thread.sleep(60);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                boolean isVirtual = Thread.currentThread().isVirtual();
                return domain + " -> 200 OK (VirtualThread=" + isVirtual + ")";
            }))
            .toList();

        long elapsed = System.currentTimeMillis() - start;

        System.out.println("  MapConcurrent Results:");
        pingResults.forEach(res -> System.out.println("    * " + res));
        System.out.println("  Total elapsed time: " + elapsed + " ms (4 tasks executed concurrently, not 240ms!)");
    }

    /**
     * 5. カスタム Gatherer の自作:
     * 連続する重複要素のみをフィルタリングする独自の中間操作 (distinctUntilChanged)
     * 例: [A, A, B, B, A, C, C] -> [A, B, A, C] (Rx / Reactive Streams で超頻出のオペレータ)
     */
    private static void demonstrateCustomGatherer() {
        List<String> statusEvents = List.of("IDLE", "IDLE", "RUNNING", "RUNNING", "IDLE", "DONE", "DONE");

        // 独自 Gatherer を定義
        // Gatherer.ofSequential(initializer, integrator)
        Gatherer<String, ?, String> distinctUntilChanged = Gatherer.ofSequential(
            // 状態保持ホルダー: 前回の要素を保持
            () -> new Object() {
                String previous = null;
                boolean hasPrevious = false;
            },
            // 積分器 (Integrator): 新しい要素が来た時の処理
            Gatherer.Integrator.ofGreedy((state, element, downstream) -> {
                if (!state.hasPrevious || !Objects.equals(state.previous, element)) {
                    state.previous = element;
                    state.hasPrevious = true;
                    return downstream.push(element); // 次のパイプラインへ送出
                }
                return true; // 重複時はスキップして継続
            })
        );

        List<String> filteredEvents = statusEvents.stream()
            .gather(distinctUntilChanged)
            .toList();

        System.out.println("  Input stream with duplicates:   " + statusEvents);
        System.out.println("  distinctUntilChanged (Custom): " + filteredEvents);
    }
}
