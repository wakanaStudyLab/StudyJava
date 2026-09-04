package sample;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * ============================================================================
 * 08: 現代的ファイルI/O (NIO.2) & 標準 HttpClient (Modern I/O & HTTP)
 * ============================================================================
 * 
 * 【他言語経験者（Rust, C#, Go）向け要点】
 * 1. NIO.2 (java.nio.file.Files & Path):
 *    - 古い `java.io.File` は非推奨（例外を投げず boolean を返すため失敗原因が不明）。
 *    - 現代Javaでは `Path` と `Files` を使用します（Go の os/filepath、Rust の std::path 相当）。
 *    - `Files.readString()` / `Files.writeString()` で極めて簡潔にファイル操作が可能。
 *    - `Files.lines()` によるストリーム走査で、巨大ファイルも省メモリに読み取れます。
 * 
 * 2. 標準 HttpClient (java.net.http):
 *    - Java 11 で導入された近代的な HTTP クライアント（Apache HttpClient などの外部依存が不要に）。
 *    - HTTP/1.1 および HTTP/2、WebSocket をネイティブサポート。
 *    - `sendAsync` と `CompletableFuture` によるノンブロッキング非同期通信。
 */
public class ModernIOAndHttpClient {

    public static void run() {
        System.out.println("=== 1. Modern File I/O with NIO.2 (Files & Path) ===");
        demonstrateNioFiles();

        System.out.println("\n=== 2. Modern Java Standard HttpClient (HTTP/2 & Async) ===");
        demonstrateHttpClient();
    }

    private static void demonstrateNioFiles() {
        try {
            // 1. 一時ディレクトリとパスの結合 (resolve)
            Path tempDir = Files.createTempDirectory("java_sample_sandbox");
            Path targetFile = tempDir.resolve("system_metrics.log");
            System.out.println("  Temporary Sandbox Path: " + tempDir);

            // 2. 文字列の書き込み (Files.writeString)
            String logContent = """
                [2026-09-04 10:00:00] SYSTEM_BOOT: OK
                [2026-09-04 10:00:01] CPU_USAGE: 12.5%
                [2026-09-04 10:00:02] MEMORY_FREE: 4096MB
                [2026-09-04 10:00:03] NETWORK_LINK: UP (10Gbps)
                """;
            Files.writeString(targetFile, logContent, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            System.out.println("  Successfully wrote log lines to: " + targetFile.getFileName());

            // 3. ファイルサイズの取得
            long fileSize = Files.size(targetFile);
            System.out.println("  File size: " + fileSize + " bytes");

            // 4. Stream によるメモリ効率の良い行フィルタリング (Files.lines)
            System.out.println("  Filtered stream lines (containing 'USAGE' or 'FREE'):");
            try (var lines = Files.lines(targetFile)) {
                lines.filter(line -> line.contains("USAGE") || line.contains("FREE"))
                     .forEach(line -> System.out.println("    * " + line));
            }

            // 5. ディレクトリ走査
            System.out.println("  Listing entries in directory:");
            try (var stream = Files.list(tempDir)) {
                stream.forEach(p -> System.out.println("    - " + p.getFileName()));
            }

            // クリーンアップ
            Files.deleteIfExists(targetFile);
            Files.deleteIfExists(tempDir);
            System.out.println("  Cleaned up temporary sandbox successfully.");

        } catch (IOException e) {
            System.err.println("NIO Operation failed: " + e.getMessage());
        }
    }

    private static void demonstrateHttpClient() {
        // HttpClient インスタンスの生成 (HTTP/2, リダイレクト追従, タイムアウト設定)
        HttpClient client = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(2))
            .build();

        // 1. HttpRequest の構築 (Builder パターン)
        // 外部通信が失敗しても安全にフォールバックできるよう設計
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://httpbin.org/get"))
            .timeout(Duration.ofSeconds(2))
            .header("User-Agent", "ModernJavaCrashCourse/26.0")
            .GET()
            .build();

        System.out.println("  Dispatching Async HTTP request to: " + request.uri());

        // 2. sendAsync による非同期リクエスト (CompletableFuture)
        CompletableFuture<String> futureResponse = client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(response -> {
                return "HTTP Status: " + response.statusCode() + " | Body Length: " + response.body().length();
            })
            .exceptionally(ex -> {
                // オフライン環境やタイムアウト時の安全なフォールバック
                return "[Offline/Mock Fallback] Simulated HTTP 200 OK (Connection timed out or offline)";
            });

        // 非同期処理の完了を待機
        String result = futureResponse.join();
        System.out.println("  Async Response Result: " + result);
    }
}
