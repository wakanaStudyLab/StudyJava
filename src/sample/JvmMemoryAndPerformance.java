package sample;

/**
 * ============================================================================
 * 10: JVM メモリモデル・GC・パフォーマンス (JVM Internals & JMM)
 * ============================================================================
 * 
 * 【他言語経験者（Rust, C#, Go）向け要点】
 * 1. JVM メモリ領域の構成:
 *    - スタック (Stack): スレッドごとに独立。プリミティブ値と参照ポインタを保持。
 *    - ヒープ (Heap): 全オブジェクトの実体が配置される共有領域。GC が管理。
 *    - メタスペース (Metaspace): クラスのメタデータやバイトコードを格納（ネイティブメモリ上）。
 * 
 * 2. JMM (Java Memory Model) と volatile:
 *    - 各スレッドは CPU キャッシュ（L1/L2）を持ち、別スレッドの変数変更が即座に見えない（可視性の問題）。
 *    - またコンパイラや CPU は命令を自由に並べ替えます（Reordering）。
 *    - `volatile` は「CPUキャッシュの直接同期」と「メモリバリア挿入（happens-before関係）」を保証します。
 * 
 * 3. 現代の GC (Garbage Collection):
 *    - Java 21+ では G1GC (デフォルト) に加え、**ZGC (Generational ZGC)** が実用段階に。
 *    - テラバイト級のヒープでも最大停止時間 (STW: Stop The World) が 1ミリ秒未満！
 * 
 * 4. オブジェクトの物理オーバーヘッド:
 *    - Java のオブジェクトは必ず「オブジェクトヘッダー (12〜16バイト)」を持つため、
 *      小さなオブジェクトを数百万個作るとメモリを圧迫します（Rust/C++のゼロコスト構造体との最大の違い）。
 */
public class JvmMemoryAndPerformance {

    // volatile なフラグ (happens-before の保証)
    private static volatile boolean running = true;
    private static int data = 0;

    public static void run() {
        System.out.println("=== 1. JVM Runtime Memory Inspection ===");
        inspectJvmMemory();

        System.out.println("\n=== 2. Java Memory Model (JMM) & volatile ===");
        demonstrateJmmVolatile();

        System.out.println("\n=== 3. Modern GC & Object Overhead Considerations ===");
        explainGcAndMemoryOverhead();
    }

    private static void inspectJvmMemory() {
        Runtime runtime = Runtime.getRuntime();

        long maxMemoryMB = runtime.maxMemory() / (1024 * 1024);
        long totalMemoryMB = runtime.totalMemory() / (1024 * 1024);
        long freeMemoryMB = runtime.freeMemory() / (1024 * 1024);
        long usedMemoryMB = totalMemoryMB - freeMemoryMB;

        System.out.println("  Available Processors (Cores): " + runtime.availableProcessors());
        System.out.println("  JVM Max Memory (-Xmx):        " + maxMemoryMB + " MB");
        System.out.println("  JVM Total Allocated Memory:   " + totalMemoryMB + " MB");
        System.out.println("  JVM Used Heap Memory:         " + usedMemoryMB + " MB");
        System.out.println("  JVM Free Allocated Memory:    " + freeMemoryMB + " MB");
    }

    private static void demonstrateJmmVolatile() {
        // バックグラウンドスレッドでフラグを監視
        Thread worker = Thread.ofVirtual().start(() -> {
            while (running) {
                // volatile により、別スレッドによる running = false が確実に検知される
                Thread.onSpinWait(); // CPU にスピン待機を通知するヒント命令
            }
            System.out.println("    [Virtual Worker] Detected running=false! Read synchronized data: " + data);
        });

        try {
            Thread.sleep(20);
            data = 42;          // volatile 変数への書き込み前に書き込まれた値は
            running = false;    // happens-before 規則により、worker 側でも確実に可視化される！
            worker.join();
            System.out.println("  JMM happens-before demo completed successfully.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void explainGcAndMemoryOverhead() {
        System.out.println("  - Modern GC Landscape in Java 21+:");
        System.out.println("    * G1GC (Default): Balanced throughput and low latency (<200ms STW).");
        System.out.println("    * Generational ZGC (-XX:+UseZGC): Ultra-low latency (<1ms STW) even on 1TB heaps.");
        System.out.println("  - Memory Overhead Anatomy (per Object):");
        System.out.println("    * Mark Word (8 bytes): HashCode, GC Age, Lock States.");
        System.out.println("    * Klass Word (4 or 8 bytes with Compressed OOPs): Pointer to Class metadata.");
        System.out.println("    * Padding: Aligned to 8-byte boundaries.");
        System.out.println("    * Best Practice: Use primitive arrays (int[]) or primitive streams for heavy math!");
    }
}
