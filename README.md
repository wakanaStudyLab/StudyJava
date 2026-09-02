# Modern Java Crash Course (For Rust, C#, Go Developers)

Rust, C#, Go などの静的型付け言語を習得済みのエンジニアが、**最短でモダンJava（Java 21+ LTS）をマスターするための実践リファレンス**です。

---

## クイックスタート (実行方法)

```powershell
# sample ディレクトリに移動
cd C:\Users\harun\programming\Java\sample

# UTF-8 でコンパイル (bin ディレクトリに出力)
javac -encoding UTF-8 -d bin src/sample/*.java

# 実行
java -cp bin sample.Main
```

> **VS Code をお使いの場合**:  
> `sample` フォルダを VS Code で開いて `src/sample/Main.java` の `Run` ボタン（または `F5`）を押すだけで実行できます。


---

## 🗺️ 言語対比マッピング早見表 (Java vs C# vs Rust vs Go)

| 概念・機能 | Java (Modern 21+) | C# | Rust | Go |
| :--- | :--- | :--- | :--- | :--- |
| **ローカル型推論** | `var x = 10;` | `var x = 10;` | `let x = 10;` | `x := 10` |
| **不変データ構造** | `record User(...)` | `record User(...)` | `struct User { ... }` | `type User struct` |
| **値の同値性比較** | `a.equals(b)` / `Objects.equals` | `a == b` (オーバーロード) | `a == b` (`PartialEq`) | `a == b` / `reflect.DeepEqual` |
| **参照の一致比較** | `a == b` | `object.ReferenceEquals(a, b)` | `std::ptr::eq(a, b)` | ポインタの `p1 == p2` |
| **代数的データ型 (ADT)** | `sealed interface` + `permits` | `abstract record` | `enum` | interface + type switch |
| **パターンマッチング** | `switch (val) { case ... }` | `val switch { ... }` | `match val { ... }` | `switch val.(type)` |
| **複数行文字列** | `""" ... """` (Text Block) | `$""" ... """` | `r#" ... "#` | `` ` ... ` `` |
| **コレクション操作** | `stream().filter().map().toList()` | `.Where().Select().ToList()` | `.iter().filter().map().collect()` | ループ / `slices` パッケージ |
| **Null 安全性** | `Optional<T>` (戻り値専用) | `Nullable<T>` / `T?` | `Option<T>` | `(T, bool)` / `nil` |
| **リソース自動解放** | `try (var r = ...) { ... }` | `using (var r = ...)` | `Drop` トレイト (スコープ脱出) | `defer r.Close()` |
| **軽量スレッド** | **Virtual Threads** (Java 21+) | ❌ (OSスレッド + async/await) | ❌ (OSスレッド + Tokio async) | **goroutine** (`go func()`) |
| **非同期 Promise** | `CompletableFuture<T>` | `Task<T>` / `async-await` | `Future<Output=T>` | チャネル + goroutine |
| **並行マップ** | `ConcurrentHashMap` | `ConcurrentDictionary` | `DashMap` (クレート) | `sync.Map` |

---

## 他言語経験者が最もハマる Java の「罠」と作法

### 1. `==` は演算子オーバーロードできない（参照比較になる）
- **C# / Rust / Go**: `a == b` で文字列や構造体の値比較ができる。
- **Java**: `==` は常に **メモリ上の参照 (ポインタ) が同一か** を判定します。
  ```java
  String s1 = new String("hello");
  String s2 = new String("hello");
  boolean wrong = (s1 == s2); // false
  boolean right = s1.equals(s2); // true
  boolean nullSafe = Objects.equals(s1, s2); // nullでも安全
  ```

### 2. プリミティブ型 vs 参照型（Auto-Boxing のコスト）
- `int`, `long`, `boolean` はスタック上の生データ。
- `Integer`, `Long`, `Boolean` はヒープ上に確保されるオブジェクト。
- `List<int>` は作れず、`List<Integer>` になるため、大量データを扱う際は **Auto-Boxing によるメモリ・GC負荷** が発生します。
- **対策**: 数値計算のループや集計には `IntStream`, `LongStream`, `int[]` を使う。

### 3. Optional の作法（RustのOptionとの違い）
- **JavaのOptionalはフィールドやメソッド引数に使ってはいけない**（シリアライズ不能・ボイラープレート化するため）。
- **用途**: 「メソッドの戻り値として、結果が存在しない可能性を明示する」ことだけに使う。
- `.get()` を直呼びしない（Rustの `.unwrap()` と同様に危険）。`.orElse()`, `.orElseGet()`, `.map()` を使う。

### 4. 検査例外 (Checked) vs 非検査例外 (Unchecked)
- `Exception` を直接継承すると「検査例外」になり、呼び出し元すべてに `try-catch` か `throws` が強制される。
- **モダンJava（Spring等）の標準**: 独自の業務例外はすべて `RuntimeException`（非検査例外）を継承して作成する。

### 5. Virtual Threads (Project Loom) の衝撃
- Java 21 から導入された仮想スレッドは、**Goの goroutine と同等のM:Nスケジューラ**です。
- ブロッキングI/Oを行ってもOSスレッドはブロックされず、ヒープメモリも数KBしか消費しません。
- したがって、複雑な Reactive Programming (WebFlux等) や async/await 構文を使わなくても、**「プレーンな同期コード」のまま数十万並行の超高スループット**が実現できます。

---

## 提供サンプルコードの解説

| ファイル | テーマ | 主な学習内容 |
| :--- | :--- | :--- |
| [`BasicsAndTypes.java`](file:///C:/Users/harun/programming/Java/sample/src/sample/BasicsAndTypes.java) | 基本型・モダン構文 | `var`, Text Blocks, `record`, `sealed interface`, `switch` パターンマッチング, `==` vs `equals` |
| [`CollectionsAndStreams.java`](file:///C:/Users/harun/programming/Java/sample/src/sample/CollectionsAndStreams.java) | コレクション & Stream | `List.of` (不変), `filter`/`map`/`sorted`/`toList`, `groupingBy`, `IntStream` (LINQ/Rust Iterator対比) |
| [`ExceptionAndResource.java`](file:///C:/Users/harun/programming/Java/sample/src/sample/ExceptionAndResource.java) | 例外 & リソース管理 | `RuntimeException` カスタム例外, `try-with-resources` (`AutoCloseable`), `Optional` ベストプラクティス |
| [`ConcurrencyAndVirtualThreads.java`](file:///C:/Users/harun/programming/Java/sample/src/sample/ConcurrencyAndVirtualThreads.java) | 並行・非同期・Virtual Threads | `newVirtualThreadPerTaskExecutor` (1万並行待機), `CompletableFuture`, `ConcurrentHashMap`, `AtomicInteger` |
| [`LambdaDeepDive.java`](file:///C:/Users/harun/programming/Java/sample/src/sample/LambdaDeepDive.java) | ラムダ式 & 関数型徹底攻略 | 基本構文、`java.util.function`、メソッド参照4形態、実質的final、例外処理、Strategyパターン |
| [`Main.java`](file:///C:/Users/harun/programming/Java/sample/src/sample/Main.java) | エントリーポイント | 上記全モジュールを一括実行するランナー |

> 📖 **ラムダ式の理論と深層理解**:  
> ラムダ式の完全な解説ドキュメントは [**`LAMBDA.md`**](file:///C:/Users/harun/programming/Java/sample/LAMBDA.md) を参照してください。構文の省略規則から内部バイトコード実装（`invokedynamic`）、他言語比較まで完全網羅しています。

---

## Java エコシステム・ビルドツール解説

実務では `javac` を直接叩くのではなく、以下のいずれかのビルドツールを使用します。

### 1. **Gradle** (推奨 / モダンな標準)
- Rustの `cargo` や Goの `go build` に近い、柔軟で高速なビルドツール（Kotlin DSL / Groovy DSL）。
- 設定ファイル: `build.gradle.kts`

### 2. **Maven** (エンタープライズの定番)
- XMLベースの宣言的ビルドツール。
- 設定ファイル: `pom.xml`

### 3. デファクトフレームワーク
- **Web / API / マイクロサービス**: **Spring Boot 3.x** (デファクトスタンダード) または **Quarkus** / **Micronaut** (超軽量・Native最適化)
- **テスト**: **JUnit 5**, **AssertJ**, **Mockito**, **Testcontainers**
