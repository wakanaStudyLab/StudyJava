# Modern Java Crash Course (For Rust, C#, Go Developers) - Java 21+ / 26 Edition

Rust, C#, Go などの静的型付け言語を習得済みのエンジニアが、**最短でモダンJava（Java 21〜26 LTS世代）をマスターするための体系的リファレンス & サンプルコード集**です。

基本構文・型システム・ラムダから、PECSジェネリクス、構造化並行性（Project Loom）、NIO.2/HttpClient、リフレクション/動的プロキシ（Spring内部機構）、JMM（メモリモデル）、デザインパターンまで全12モジュールで完全網羅しています。

---

## 📂 プロジェクト全体構成 (Directory Structure)

```text
.\
├── build_and_run.ps1                 # 一括コンパイル & 実行スクリプト (プレビュー有効化)
├── LAMBDA.md                         # 📘 Modern Java ラムダ式・関数型プログラミング完全理解ガイド
├── README.md                         # 本ファイル (総合リファレンス & 言語対比表)
│
├── bin\                              # コンパイル済みバイトコード出力ディレクトリ (.class)
│
└── src\sample\                       # ソースコード (全12モジュール)
    ├── BasicsAndTypes.java           # 01: 基本型・record・sealed interface・switch パターンマッチング
    ├── CollectionsAndStreams.java    # 02: 不変/可変コレクション・Stream API パイプライン・LINQ対比
    ├── ExceptionAndResource.java     # 03: 検査/非検査例外・try-with-resources・Optional ベストプラクティス
    ├── ConcurrencyAndVirtualThreads.java # 04: Virtual Threads (1万並行)・CompletableFuture・並行マップ
    ├── LambdaDeepDive.java           # 05: ラムダ構文・標準SAM・メソッド参照4形態・例外ラップ
    ├── GenericsDeepDive.java         # 06: 型消去 (Type Erasure)・PECS原則・再帰的境界ジェネリクス
    ├── StructuredConcurrencyAndScopedValues.java # 07: 構造化並行性 (StructuredTaskScope)・ScopedValue
    ├── ModernIOAndHttpClient.java    # 08: NIO.2 (Files/Path)・標準 HttpClient (HTTP/2 & 非同期)
    ├── ReflectionAndProxies.java     # 09: カスタムアノテーション・リフレクション・動的プロキシ (Spring AOP)
    ├── JvmMemoryAndPerformance.java  # 10: JVMメモリ解剖・JMM (volatile / happens-before)・G1GC/ZGC
    ├── SequencedCollectionsAndSafety.java # 11: Java 21 Sequenced Collections・防衛的コピー・record検証
    ├── ModernDesignPatterns.java     # 12: Modern Strategy・Sealed State パターン・Flow API (Reactive)
    └── Main.java                     # 全12モジュール統合エントリーポイント
```

---

## 🛠️ 動作要件 & 開発環境 (Prerequisites)

| 項目 | 推奨バージョン | 備考 |
| :--- | :--- | :--- |
| **JDK (Java Development Kit)** | **Java 21 LTS 以上** (本環境: **Java 26.0.1**) | `javac`, `java` コマンドが PATH に通っていること |
| **実行フラグ** | `--enable-preview --release 26` | Loom の最新機能（StructuredTaskScope / ScopedValue）に必要 |
| **VS Code 拡張機能** | **Extension Pack for Java** (`vscjava.vscode-java-pack`) | コード補完、デバッグ (`F5`)、テスト実行 |

---

## 🚀 クイックスタート (ビルド & 実行方法)

### 1. PowerShell スクリプトで一括実行 (最も簡単)
ディレクトリ直下のスクリプトを実行するだけで、自動で `bin` フォルダを作成し全12モジュールのデモを順次実行します：

```powershell
.\build_and_run.ps1
```

### 2. コマンドラインでの手動コンパイル & 実行
```powershell
# UTF-8 でプレビュー機能を有効化してコンパイル
javac -encoding UTF-8 --enable-preview --release 26 -d bin src/sample/*.java

# 実行
java --enable-preview -cp bin sample.Main
```

### 3. VS Code での F5 デバッグ
- VS Codeでフォルダを開きます。
- `src/sample/Main.java` を開き、**`F5`** キー（またはエディタ右上の `Run` ボタン）を押すだけでデバッグ実行できます。

---

## 🗺️ 言語対比マッピング早見表 (Java vs C# vs Rust vs Go)

| 概念・機能 | Java (Modern 21+) | C# | Rust | Go |
| :--- | :--- | :--- | :--- | :--- |
| **ローカル型推論** | `var x = 10;` | `var x = 10;` | `let x = 10;` | `x := 10` |
| **不変データキャリア** | **`record User(...)`** | `record User(...)` | `struct User { ... }` | `type User struct` |
| **値の同値性比較** | `a.equals(b)` / `Objects.equals` | `a == b` (オーバーロード) | `a == b` (`PartialEq`) | `a == b` / `reflect.DeepEqual` |
| **参照の一致比較** | `a == b` | `object.ReferenceEquals(a, b)` | `std::ptr::eq(a, b)` | ポインタの `p1 == p2` |
| **代数的データ型 (ADT)** | **`sealed interface` + `permits`** | `abstract record` | `enum Name { A, B }` | interface + type switch |
| **パターンマッチング** | **`switch (val) { case ... }`** | `val switch { ... }` | `match val { ... }` | `switch val.(type)` |
| **複数行文字列** | `""" ... """` (Text Block) | `$""" ... """` | `r#" ... "#` | `` ` ... ` `` |
| **コレクション操作** | `stream().filter().map().toList()` | `.Where().Select().ToList()` | `.iter().filter().map().collect()` | ループ / `slices` パッケージ |
| **Null 安全性** | `Optional<T>` (戻り値専用) | `Nullable<T>` / `T?` | `Option<T>` | `(T, bool)` / `nil` |
| **リソース自動解放** | **`try (var r = ...) { ... }`** | `using (var r = ...)` | `Drop` トレイト (スコープ脱出) | `defer r.Close()` |
| **ジェネリクス境界** | **PECS** (`? extends` / `? super`) | `in T` / `out T` (変位指定) | トレイト境界 (`T: Trait`) | `[T any]` / `[T constraints]` |
| **型情報の保持** | 型消去 (Type Erasure) | 具象化 (Reified Generics) | モノモーフィズム (単一化) | なし (interface{}) |
| **軽量スレッド** | **Virtual Threads** (Java 21+) | ❌ (OSスレッド + async/await) | ❌ (OSスレッド + Tokio async) | **goroutine** (`go func()`) |
| **構造化並行性** | **`StructuredTaskScope`** (Loom) | Task.WhenAll (手動キャンセル) | JoinSet / select! | `errgroup.Group` |
| **スコープコンテキスト**| **`ScopedValue`** (Loom) | `AsyncLocal<T>` | タスク引数引き回し | `context.Context` |
| **非同期 Promise** | `CompletableFuture<T>` | `Task<T>` / `async-await` | `Future<Output=T>` | チャネル + goroutine |
| **並行マップ** | `ConcurrentHashMap` | `ConcurrentDictionary` | `DashMap` (クレート) | `sync.Map` |
| **現代的ファイルI/O** | `java.nio.file.Files` (NIO.2) | `System.IO.File` | `std::fs` | `os` / `io` |
| **標準 HTTP Client** | `java.net.http.HttpClient` | `HttpClient` | `reqwest` (外部) | `net/http` |
| **AOP / 動的拡張** | `java.lang.reflect.Proxy` | `DispatchProxy` / Castle | マクロ / トレイトオブジェクト | interface ラッパー |
| **メモリ可視性保証** | `volatile` (happens-before) | `volatile` | `std::sync::atomic` | `sync/atomic` |
| **統一シーケンス** | **`SequencedCollection`** (Java 21) | `IList<T>` (`^1` 添字) | `VecDeque` | スライス (`s[0]`, `s[len-1]`) |

---

## ⚠️ 他言語経験者が最もハマる Java の「罠」と実践的作法

### 1. `==` は演算子オーバーロードできない（参照比較になる）
- **C# / Rust / Go**: `a == b` で文字列や構造体の値比較ができる。
- **Java**: `==` は常に **メモリ上の参照 (ポインタ) が同一か** を判定します。
  ```java
  String s1 = new String("hello");
  String s2 = new String("hello");
  boolean wrong = (s1 == s2);            // false! (最も引っかかる罠)
  boolean right = s1.equals(s2);         // true (値の比較)
  boolean nullSafe = Objects.equals(s1, s2); // null安全な推奨比較
  ```

### 2. プリミティブ型 vs 参照型（Auto-Boxing のコスト）
- `int`, `long`, `boolean` はスタック上の生データ。
- `Integer`, `Long`, `Boolean` はヒープ上に確保されるオブジェクト。
- `List<int>` は作れず `List<Integer>` になるため、大量データを扱う際は **Auto-Boxing によるメモリ・GC負荷** が発生します。
- **対策**: 数値計算のループや大量集計には `IntStream`, `LongStream`, `int[]` を使う。

### 3. Optional の作法（RustのOptionとの決定的な違い）
- **JavaのOptionalはフィールドやメソッド引数に使ってはいけない**（シリアライズ不能・ボイラープレート化するため）。
- **用途**: 「メソッドの戻り値として、結果が存在しない可能性を明示する」ことだけに限定する。
- `.get()` を直呼びしない（Rustの `.unwrap()` と同様に危険）。`.orElse()`, `.orElseGet()`, `.map()` を使う。

### 4. 検査例外 (Checked) vs 非検査例外 (Unchecked)
- `Exception` を直接継承すると「検査例外」になり、呼び出し元すべてに `try-catch` か `throws` が強制されます（Stream API とも相性が最悪）。
- **モダンJava（Spring Boot / Quarkus 等）の標準**: 独自の業務例外はすべて `RuntimeException`（非検査例外）を継承して作成する。

### 5. Virtual Threads (Project Loom) の設計思想
- Java 21 から導入された仮想スレッドは、**Goの goroutine と同等のM:Nスケジューラ**です。
- ブロッキングI/Oを行ってもOSスレッドはブロックされず、ヒープメモリも数KBしか消費しません。
- したがって、複雑な Reactive Programming (WebFlux等) や async/await 構文を使わなくても、**「プレーンな同期コード」のまま数十万並行の超高スループット**が実現できます。

### 6. 型消去 (Type Erasure) と PECS 原則
- Java のジェネリクスは実行時には型情報が消去され `Object` になります（`List<String>` も `List<Integer>` も実行時は同じクラス）。
- `List<Dog>` を `List<Animal>` に代入することはできません（不変性: Invariance）。
- コレクションから取り出すなら **`? extends T`** (Producer)、コレクションへ書き込むなら **`? super T`** (Consumer) を指定します（**PECS原則**）。

### 7. 防衛的コピー (Defensive Copy) の罠
- `Collections.unmodifiableList(list)` は単なる「ビュー」であり、元のリストが変更されるとビュー側も勝手に書き換わってしまいます。
- 完全な不変スナップショットを作るには Java 10+ の **`List.copyOf(list)`** を使用します。

---

## 📁 全12モジュール構成一覧

| # | クラス名 | 主な学習トピック |
| :-: | :--- | :--- |
| **01** | [`BasicsAndTypes.java`](./src/sample/BasicsAndTypes.java) | プリミティブ vs 参照型、Auto-Boxing の罠、`==` vs `equals`、`var`、Text Blocks、**`record`**、**`sealed interface`**、`switch` パターンマッチング |
| **02** | [`CollectionsAndStreams.java`](./src/sample/CollectionsAndStreams.java) | 不変リスト (`List.of`) vs 可変リスト、Stream パイプライン (`filter`/`map`/`sorted`/`toList`)、`Collectors.groupingBy`、`IntStream` (LINQ/Rust Iterator対比) |
| **03** | [`ExceptionAndResource.java`](./src/sample/ExceptionAndResource.java) | 検査例外 (Checked) vs 非検査例外 (Unchecked)、`try-with-resources` (`AutoCloseable`) による確実なリソース解放、**`Optional<T>`** の作法とアンチパターン |
| **04** | [`ConcurrencyAndVirtualThreads.java`](./src/sample/ConcurrencyAndVirtualThreads.java) | **Virtual Threads** (1万並行の超高速起動)、`CompletableFuture` (非同期合成・エラーフォールバック)、`ConcurrentHashMap`、`AtomicInteger` |
| **05** | [`LambdaDeepDive.java`](./src/sample/LambdaDeepDive.java) | ラムダ式構文、標準関数型インターフェース (`Predicate`, `Function`, `Consumer`, `Supplier`)、メソッド参照4形態 (`::`)、実質的final、例外ラップイディオム |
| **06** | [`GenericsDeepDive.java`](./src/sample/GenericsDeepDive.java) | **型消去 (Type Erasure)** の物理的実態、ジェネリクスの不変性 (Invariance)、**PECS原則** (Producer `extends`, Consumer `super`)、再帰的境界ジェネリクス (`Comparable<T>`) |
| **07** | [`StructuredConcurrencyAndScopedValues.java`](./src/sample/StructuredConcurrencyAndScopedValues.java) | **`StructuredTaskScope`** (子スレッドのライフサイクル統合・キャンセル伝播)、**`ScopedValue`** (`ThreadLocal` を置き換える不変・軽量コンテキスト受け渡し) |
| **08** | [`ModernIOAndHttpClient.java`](./src/sample/ModernIOAndHttpClient.java) | **NIO.2 (`Files` & `Path`)** による行走査・ストリーム処理、標準 **`HttpClient`** による HTTP/2 & 非同期通信 (`sendAsync`) |
| **09** | [`ReflectionAndProxies.java`](./src/sample/ReflectionAndProxies.java) | カスタムアノテーション (`@Retention(RUNTIME)`)、リフレクション検査、**`Proxy` (動的プロキシ)** による AOP / メソッドインターセプト (Spring 内部機構の解剖) |
| **10** | [`JvmMemoryAndPerformance.java`](./src/sample/JvmMemoryAndPerformance.java) | JVM メモリ解剖 (Heap / Stack / Metaspace)、**JMM (Java Memory Model)**、**`volatile`** と happens-before 関係、G1GC vs ZGC (世代別ZGC)、オブジェクトヘッダー構造 |
| **11** | [`SequencedCollectionsAndSafety.java`](./src/sample/SequencedCollectionsAndSafety.java) | **Java 21 `SequencedCollection`** (`getFirst()`, `getLast()`, `reversed()`)、防衛的コピーの罠 (`unmodifiableList` vs `List.copyOf`)、`record` コンパクトコンストラクタ検証 |
| **12** | [`ModernDesignPatterns.java`](./src/sample/ModernDesignPatterns.java) | Modern Strategy パターン (ラムダ関数化)、Modern State パターン (**Sealed Interface + switch 式**)、Reactive Streams (**`java.util.concurrent.Flow` API**) |

- [`Main.java`](./src/sample/Main.java): 全12モジュールを順序よく実演する統合エントリーポイント

> 📖 **詳細理論ドキュメント**:  
> ラムダ式の完全な解説ドキュメントは [**`LAMBDA.md`**](./LAMBDA.md) を参照してください。構文の省略規則から内部バイトコード実装（`invokedynamic`）、他言語比較まで完全網羅しています。

---

## 📚 推薦学習ロードマップ

1. **第1部: 言語基盤と関数型プログラミング (モジュール 01〜03, 05)**
   - プリミティブ vs 参照型、`record`、`sealed interface`、パターンマッチング switch 式
   - コレクションと Stream API パイプライン
   - 例外設計（非検査例外中心）と `try-with-resources`、`Optional` の正しい使い方
   - ラムダ式の構文解剖と関数型インターフェース
2. **第2部: 型システムとフレームワークの裏側 (モジュール 06, 09)**
   - Java の型消去と PECS 原則（境界ワイルドカード `? extends` / `? super`）
   - カスタムアノテーション、リフレクション、動的プロキシ（Spring AOP や DI の仕組み）
3. **第3部: 並行処理・非同期・Project Loom (モジュール 04, 07)**
   - 仮想スレッド (Virtual Threads) による高多重度 I/O 処理
   - `CompletableFuture` による非同期合成
   - 構造化並行性 (`StructuredTaskScope`) によるスレッドリーク根絶
   - スコープ付き値 (`ScopedValue`) による軽量コンテキスト受け渡し
4. **第4部: I/O・低レイヤ・アーキテクチャ (モジュール 08, 10, 11, 12)**
   - NIO.2 (`Files` / `Path`) と最新標準 `HttpClient`
   - JVM メモリ構造、JMM (`volatile` と happens-before)、現代 GC (G1 / ZGC)
   - Java 21 `SequencedCollection` と不変プログラミング
   - Modern Java によるデザインパターン（Strategy, Sealed State, Flow API）

---

## 🏢 Java エコシステム・ビルドツール解説

実務開発では `javac` を直接叩くのではなく、以下のビルドツールとフレームワークを使用します。

### 1. **Gradle** (推奨 / モダンな標準)
- Rustの `cargo` や Goの `go build` に近い、柔軟で高速なビルドツール（Kotlin DSL / Groovy DSL）。
- 設定ファイル: `build.gradle.kts`

### 2. **Maven** (エンタープライズの定番)
- XMLベースの宣言的・安定したビルドツール。
- 設定ファイル: `pom.xml`

### 3. デファクトフレームワーク & ライブラリ
- **Web / API / マイクロサービス**: **Spring Boot 3.x** (デファクトスタンダード) または **Quarkus** / **Micronaut** (超軽量・GraalVM Native最適化)
- **テスト**: **JUnit 5**, **AssertJ**, **Mockito**, **Testcontainers** (Docker連携統合テスト)
