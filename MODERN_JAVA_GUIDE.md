# モダンJava完全攻略マスターハンドブック (Modern Java 21〜26 Complete Guide)

> **対象読者**: Rust, C#, Go などの静的型付け言語を習得済みのエンジニア、および「昔のJava（Java 7/8時代）の知識で止まっている」エンジニア  
> **対応バージョン**: **Java 21 LTS 〜 Java 26 最新世代 (Project Loom, Project Panama, Stream Gatherers)**  
> **対象プロジェクト**: 本リポジトリ (`./` 直下に全16モジュール収録)

---

## 📑 目次

1. [はじめに：現代のJavaが遂げた進化の全貌](#1-はじめに現代のjavaが遂げた進化の全貌)
2. [言語対比マッピング完全早見表 (Java vs C# vs Rust vs Go)](#2-言語対比マッピング完全早見表-java-vs-c-vs-rust-vs-go)
3. [第1部: 言語構文・型システム・不変性 (モジュール 01, 11, 15, 16)](#3-第1部-言語構文型システム不変性)
   - [01: プリミティブ vs 参照型、同一性と同値性](#3-1-プリミティブ-vs-参照型同一性と同値性)
   - [01 & 11: `record` による不変データキャリアとバリデーション](#3-2-record-による不変データキャリアとバリデーション)
   - [01: `sealed interface` による代数的データ型 (ADT)](#3-3-sealed-interface-による代数的データ型-adt)
   - [15: レコードパターン（構造分解: Deconstruction）](#3-4-レコードパターン構造分解-deconstruction)
   - [15: 無名変数・無名パターン (`_`)](#3-5-無名変数無名パターン-_)
   - [16: Flexible Constructor Bodies (`super(...)` 前の文実行)](#3-6-flexible-constructor-bodies-super-前の文実行)
   - [11: Sequenced Collections と防衛的コピーの罠](#3-7-sequenced-collections-と防衛的コピーの罠)
4. [第2部: 関数型プログラミング・コレクション・Stream (モジュール 02, 05, 14)](#4-第2部-関数型プログラミングコレクションstream)
   - [05: ラムダ式とSAMインターフェースの本質](#4-1-ラムダ式とsamインターフェースの本質)
   - [05: メソッド参照 (`::`) 4大パターン](#4-2-メソッド参照--4大パターン)
   - [02: 不変コレクション (`List.of`) と Stream API パイプライン](#4-3-不変コレクション-listof-と-stream-api-パイプライン)
   - [14: Stream Gatherers による中間操作の革新 (JEP 461/473/485)](#4-4-stream-gatherers-による中間操作の革新)
5. [第3部: エラーハンドリングとリソース安全 (モジュール 03)](#5-第3部-エラーハンドリングとリソース安全)
   - [03: 検査例外 (Checked) vs 非検査例外 (Unchecked) の現代的作法](#5-1-検査例外-checked-vs-非検査例外-unchecked-の現代的作法)
   - [03: `try-with-resources` と `AutoCloseable` (RAII)](#5-2-try-with-resources-と-autocloseable-raii)
   - [03: `Optional<T>` の正しい境界づけとアンチパターン](#5-3-optionalt-の正しい境界づけとアンチパターン)
6. [第4部: 並行処理・非同期・Project Loom (モジュール 04, 07)](#6-第4部-並行処理非同期project-loom)
   - [04: 仮想スレッド (Virtual Threads) の衝撃と M:N スケジューリング](#6-1-仮想スレッド-virtual-threads-の衝撃と-mn-スケジューリング)
   - [04: `CompletableFuture` による非同期パイプライン](#6-2-completablefuture-による非同期パイプライン)
   - [07: 構造化並行性 (`StructuredTaskScope`) によるスレッドリーク根絶](#6-3-構造化並行性-structuredtaskscope-によるスレッドリーク根絶)
   - [07: スコープ付き値 (`ScopedValue`) による軽量コンテキスト](#6-4-スコープ付き値-scopedvalue-による軽量コンテキスト)
7. [第5部: 低レイヤ・ネイティブ連携・JVM解剖 (モジュール 08, 09, 10, 13)](#7-第5部-低レイヤネイティブ連携jvm解剖)
   - [08: NIO.2 (`Files` & `Path`) と現代的標準 `HttpClient`](#7-1-nio2-files--path-と現代的標準-httpclient)
   - [09: アノテーション・リフレクション・動的プロキシ (Spring AOPの正体)](#7-2-アノテーションリフレクション動的プロキシ-spring-aopの正体)
   - [10: JVMメモリ構造・JMM (`volatile` & happens-before)・超低遅延GC](#7-3-jvmメモリ構造jmm-volatile--happens-before超低遅延gc)
   - [13: Project Panama: Foreign Function & Memory API (FFM API)](#7-4-project-panama-foreign-function--memory-api-ffm-api)
8. [第6部: デザインパターン & アーキテクチャのモダナイズ (モジュール 12, 16)](#8-第6部-デザインパターン--アーキテクチャのモダナイズ)
   - [12: Modern Strategy / Modern State (Sealed Interface + switch)](#8-1-modern-strategy--modern-state)
   - [12: Reactive Streams (`java.util.concurrent.Flow`)](#8-2-reactive-streams-flow-api)
   - [16: 現代的 Result 型パターンによるドメイン駆動設計](#8-3-現代的-result-型パターンによるドメイン駆動設計)
9. [全16モジュール対照カタログ & クイックリファレンス](#9-全16モジュール対照カタログ--クイックリファレンス)
10. [実務におけるビルド & エコシステム指針](#10-実務におけるビルド--エコシステム指針)

---

## 1. はじめに：現代のJavaが遂げた進化の全貌

かつて「Java」といえば、以下のようなイメージを持たれがちでした：
- Getter/Setter/hashCode/equals の儀式コード（ボイラープレート）で埋め尽くされている。
- C# や Rust に比べて構文が古臭く、型推論やパターンマッチングがない。
- 並行処理はスレッドプールのチューニングが複雑で、非同期プログラミング（Reactive/async-await）は学習コストが高い。
- C/C++ との連携（JNI）は煩雑で危険。

**しかし、Java 21 LTS、そして Java 22〜26 の進化によって、Java は完全に生まれ変わりました。**

1. **ボイラープレートの消滅**: `record`（不変データキャリア）、`var`（型推論）、Text Blocks（複数行文字列）、Flexible Constructor Bodies により、コード量は C# や Go と完全に同等になりました。
2. **表現力豊かな型システム**: `sealed interface` と **Record Patterns（構造分解代入）** により、Rust の `enum` や `match` と同等以上の型安全な代数的データ型 (ADT) を実現。
3. **並行性の革命 (Project Loom)**: 仮想スレッド (Virtual Threads) により、Go の goroutine と同等の M:N 軽量スレッドが標準搭載。複雑な async/await や Reactive Streams を書かなくても、**「素直な同期コード」のまま数十万並行の I/O 処理** が可能になりました。
4. **低レイヤと安全性の両立 (Project Panama)**: JNI を葬り去る **FFM API** により、オフヒープメモリへの安全なアクセスと C ライブラリの直接呼び出しがゼロコピーで実現。
5. **Stream API のミッシングリンク解消**: **Stream Gatherers** により、チャンク分割やスライディングウィンドウなどのカスタム中間操作を自在に合成可能。

本リポジトリ (`sample`) は、これらの最先端モダンJavaのすべてを、実際に動かして学べる 16 のモジュールとして構成しています。

---

## 2. 言語対比マッピング完全早見表 (Java vs C# vs Rust vs Go)

Rust, C#, Go から Java に参入したエンジニアが、迷わずマッピングできる対照表です。

| 機能・概念 | Modern Java (21〜26) | C# (.NET 8/9) | Rust (2021/2024) | Go (1.21+) |
| :--- | :--- | :--- | :--- | :--- |
| **ローカル型推論** | `var x = 10;` | `var x = 10;` | `let x = 10;` | `x := 10` |
| **不変データ構造** | **`record User(...)`** | `record User(...)` | `struct User { ... }` | `type User struct` |
| **値の等価比較** | **`a.equals(b)`** / `Objects.equals` | `a == b` (オーバーロード) | `a == b` (`PartialEq`) | `a == b` / `reflect.DeepEqual` |
| **参照の一致比較** | **`a == b`** | `object.ReferenceEquals(a, b)` | `std::ptr::eq(a, b)` | ポインタ比較 `p1 == p2` |
| **代数的データ型 (ADT)** | **`sealed interface` + `permits`** | `abstract record` | **`enum Name { A, B }`** | interface + type switch |
| **パターン分解** | **`case Point(var x, var y)`** | `case Point(var x, var y)` | `Point { x, y } => ...` | なし (手動フィールドアクセス) |
| **無視パターン** | **`_` (JEP 456)** | `_` | `_` | `_` |
| **複数行文字列** | `""" ... """` (Text Blocks) | `$""" ... """` | `r#" ... "#` | `` ` ... ` `` |
| **コレクション処理** | `stream().filter().map().toList()` | `.Where().Select().ToList()` | `.iter().filter().map().collect()` | ループ / `slices` |
| **拡張中間操作** | **`stream.gather(Gatherers.xxx)`** | LINQ 拡張メソッド | Iterator トレイト拡張 | なし |
| **Null 安全表現** | `Optional<T>` (戻り値専用) | `Nullable<T>` / `T?` | **`Option<T>`** | `(T, bool)` / `nil` |
| **リソース確実解放** | **`try (var r = ...) { ... }`** | `using (var r = ...)` | **`Drop` トレイト (RAII)** | `defer r.Close()` |
| **ジェネリクス境界** | **PECS** (`? extends`, `? super`) | `in T`, `out T` (変位指定) | トレイト境界 (`T: Trait`) | 型パラメータ制約 |
| **型情報の実行時保持** | 型消去 (Type Erasure) | 具象化 (Reified Generics) | モノモーフィズム (単一化) | なし (`interface{}`) |
| **軽量スレッド** | **Virtual Threads** (Loom) | ❌ (OSスレッド + async) | ❌ (OSスレッド + Tokio) | **goroutine** (`go func()`) |
| **構造化並行性** | **`StructuredTaskScope`** | Task.WhenAll (手動キャンセル) | JoinSet / select! | `errgroup.Group` |
| **スコープ値伝搬** | **`ScopedValue`** | `AsyncLocal<T>` | 引数引き回し | `context.Context` |
| **統一シーケンス** | **`SequencedCollection`** | `IList<T>` (`^1` 添字) | `VecDeque` | スライス (`s[0]`, `s[len-1]`) |
| **オフヒープ・FFI** | **FFM API** (`Arena`/`Segment`) | `P/Invoke` / `Span<T>` | `extern "C"` / `std::alloc` | `cgo` / `unsafe` |

---

## 3. 第1部: 言語構文・型システム・不変性

### 3-1. プリミティブ vs 参照型、同一性と同値性
*(対応モジュール: `01: BasicsAndTypes.java`)*

#### メモリ構造の違い
- **プリミティブ型 (`int`, `long`, `boolean`, `double` 等)**:
  - スタック領域またはオブジェクト内部に直接インライン配置（メモリオーバーヘッドなし）。
- **参照型・ラッパークラス (`Integer`, `Long`, `Boolean` 等)**:
  - ヒープ領域にオブジェクト（マークワード 8B + クラスポインタ 4/8B + データ本体）として確保され、スタックからはポインタで参照。
  - Javaのジェネリクス (`List<T>`) は歴史的経緯（型消去）により参照型しか受け取れないため、`List<int>` は作れず `List<Integer>` になります。
  - 数値をコレクションに入れると自動的に `Integer.valueOf(x)`（**Auto-Boxing**）が走り、取り出すと `.intValue()`（**Auto-Unboxing**）が走ります。大量データを扱う際は GC 圧迫の原因になります。

#### ⚠️ `==` 比較の決定的な罠
C# や Rust と異なり、**Java は演算子オーバーロードをサポートしていません**。
したがって、参照型に対する `==` は**「メモリ上のアドレス（参照）が一致しているか」**のみを判定します。

```java
// ❌ 危険: 文字列やIntegerの == 比較
Integer a = 1000;
Integer b = 1000;
System.out.println(a == b); // false! (別々のヒープ領域)

String s1 = new String("hello");
String s2 = new String("hello");
System.out.println(s1 == s2); // false! (別インスタンス)

// ⭕ 正解: equals() または Objects.equals() を使う
System.out.println(a.equals(b));              // true (値の比較)
System.out.println(Objects.equals(s1, s2));    // true (null安全な比較)
```

---

### 3-2. `record` による不変データキャリアとバリデーション
*(対応モジュール: `01: BasicsAndTypes.java`, `11: SequencedCollectionsAndSafety.java`)*

Java 16 で正式化された `record` は、従来の Java Bean（Getter, Setter, toString, equals, hashCode）のボイラープレートを 100% 根絶します。

```java
// 1行で定義完了！
// 全フィールドは暗黙的に private final になり、アクセサメソッド id(), name(), age() が自動生成
public record User(String id, String name, int age) {

    // コンパクトコンストラクタ (Compact Constructor):
    // 引数リストの記述を省略し、バリデーションと正規化だけに集中可能
    public User {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(name, "name must not be null");
        if (age < 0) {
            throw new IllegalArgumentException("age cannot be negative: " + age);
        }
        // 正規化（トリム）して代入
        name = name.trim();
    }
}
```

> **C# / Rust との違い**:
> アクセサの名前は `getId()` ではなく、プロパティ名そのままの `user.id()` になります。また、C# の `with { ... }`（非破壊的変更）は Java では現在策定中（Withers）のため、コピー変更時は新しい record を生成します。

---

### 3-3. `sealed interface` による代数的データ型 (ADT)
*(対応モジュール: `01: BasicsAndTypes.java`, `12: ModernDesignPatterns.java`)*

Rust の `enum Shape { Circle(f64), Rectangle(f64, f64) }` や C# の判別共用体を Java で表現するのが **`sealed`（封印）階層** です。

```java
// permits で継承・実装を許可する子クラスを明示的に指定（同一ファイル内なら permits 省略可）
public sealed interface Shape permits Circle, Rectangle, Triangle {}

public record Circle(double radius) implements Shape {}
public record Rectangle(double width, double height) implements Shape {}
public record Triangle(double base, double height) implements Shape {}
```

#### パターンマッチング switch 式との融合
全ケースが網羅されていることをコンパイラが検証するため、**`default` 節が不要** になります。

```java
double area = switch (shape) {
    case Circle c    -> Math.PI * c.radius() * c.radius();
    case Rectangle r -> r.width() * r.height();
    case Triangle t  -> 0.5 * t.base() * t.height();
    // 将来 Shape に Polygon が追加されたら、ここで即座にコンパイルエラーになり修正漏れを防止！
};
```

---

### 3-4. レコードパターン（構造分解: Deconstruction）
*(対応モジュール: `15: RecordPatternsAndAdvancedMatching.java`)*

Java 21 で導入された **Record Patterns (JEP 440)** により、レコードの構成要素をパターンマッチングの中で直接ローカル変数に分解代入できるようになりました。

```java
public record Point(double x, double y) {}
public record Window(Point topLeft, Point bottomRight, String title) {}

// ネストしたレコードもワンステップで再帰的に分解！
if (obj instanceof Window(Point(var x1, var y1), Point(var x2, var y2), var title)) {
    System.out.printf("Window '%s' from (%.1f, %.1f) to (%.1f, %.1f)%n", title, x1, y1, x2, y2);
}
```

---

### 3-5. 無名変数・無名パターン (`_`)
*(対応モジュール: `15: RecordPatternsAndAdvancedMatching.java`)*

Java 22 で正式化された **Unnamed Variables & Patterns (JEP 456)** により、Rust や Go と同様に、使わない変数を `_` で明示的に無視できます。

```java
// 1. レコードパターンで不要なフィールドを無視
if (event instanceof PaymentSuccess(_, var userId, var amount, _)) {
    System.out.println("User: " + userId + ", Amount: " + amount);
}

// 2. try-catch で例外オブジェクト変数を使わない場合
try {
    Integer.parseInt(input);
} catch (NumberFormatException _) {
    // 未使用の 'e' 変数を宣言せず安全に握りつぶし/フォールバック
}
```

---

### 3-6. Flexible Constructor Bodies (`super(...)` 前の文実行)
*(対応モジュール: `16: FlexibleConstructorAndModernLanguage.java`)*

Java 22+ (JEP 482) で、25年間続いた「`super(...)` はコンストラクタの最初の1行目に書かねばならない」という厳しい構文制限が撤廃されました。

```java
public class SecureUserAccount extends Entity {
    private final String email;

    public SecureUserAccount(String rawEmail) {
        // ⭕ super() の前に引数の検証や事前計算を自由に実行可能！
        if (rawEmail == null || !rawEmail.contains("@")) {
            throw new IllegalArgumentException("Invalid email: " + rawEmail);
        }
        String normalized = rawEmail.trim().toLowerCase();
        String generatedId = "usr_" + UUID.nameUUIDFromBytes(normalized.getBytes());

        // 前処理済みの安全な値で親コンストラクタを呼び出す
        super(generatedId, Instant.now());

        this.email = normalized;
    }
}
```

---

### 3-7. `SequencedCollection` と防衛的コピーの罠
*(対応モジュール: `11: SequencedCollectionsAndSafety.java`)*

#### Java 21 `SequencedCollection`
先頭・末尾へのアクセスや逆順ビューの取得が統一されました：
- `collection.getFirst()`, `collection.getLast()`
- `collection.addFirst(e)`, `collection.addLast(e)`
- `collection.reversed()`: コピーではなく、逆順で走査できる O(1) のビューを返す

#### ⚠️ `Collections.unmodifiableList` の偽りの不変性
```java
List<String> original = new ArrayList<>(List.of("A", "B"));
List<String> unmod = Collections.unmodifiableList(original); // ❌ 単なるラッパービュー！

original.add("C (Injected!)"); 
System.out.println(unmod); // ["A", "B", "C (Injected!)"] <-- 中身が変わってしまう！

// ⭕ 完全な不変スナップショットを作るには Java 10+ の List.copyOf() を使う
List<String> trulyImmutable = List.copyOf(original);
```

---

## 4. 第2部: 関数型プログラミング・コレクション・Stream

### 4-1. ラムダ式とSAMインターフェースの本質
*(対応モジュール: `05: LambdaDeepDive.java`, `LAMBDA.md`)*

Java には言語組み込みの「関数型」は存在しません。代わりに**単一の抽象メソッドを持つインターフェース（SAM: Single Abstract Method / `@FunctionalInterface`）**を関数の型として扱います。

```java
@FunctionalInterface
public interface Calculator {
    int calculate(int a, int b);
}

// ラムダ式による実装
Calculator add = (a, b) -> a + b;
```

#### 標準組み込み関数型インターフェース (`java.util.function`)
| インターフェース | メソッドシグネチャ | 用途 |
| :--- | :--- | :--- |
| **`Predicate<T>`** | `boolean test(T t)` | 条件判定・フィルタリング |
| **`Function<T, R>`** | `R apply(T t)` | 変換・射影 |
| **`Consumer<T>`** | `void accept(T t)` | 副作用・出力処理 |
| **`Supplier<T>`** | `T get()` | 遅延生成・ファクトリ |
| **`UnaryOperator<T>`** | `T apply(T t)` | 同一型の変換 |
| **`BiFunction<T, U, R>`** | `R apply(T t, U u)` | 2引数の計算・合成 |

---

### 4-2. メソッド参照 (`::`) 4大パターン
*(対応モジュール: `05: LambdaDeepDive.java`)*

| 分類 | 構文 | ラムダ等価式 | 例 |
| :--- | :--- | :--- | :--- |
| **1. 静的メソッド参照** | `ClassName::staticMethod` | `(x) -> ClassName.staticMethod(x)` | `Integer::parseInt` |
| **2. バインドされたインスタンス** | `instance::instanceMethod` | `(x) -> instance.instanceMethod(x)` | `System.out::println` |
| **3. バインドされないインスタンス** | `ClassName::instanceMethod` | `(x) -> x.instanceMethod()` | `String::toUpperCase` |
| **4. コンストラクタ参照** | `ClassName::new` | `(x) -> new ClassName(x)` | `ArrayList::new` |

---

### 4-3. 不変コレクション (`List.of`) と Stream API パイプライン
*(対応モジュール: `02: CollectionsAndStreams.java`)*

```java
// 不変リスト（要素変更不可、null不可）
List<String> items = List.of("Apple", "Banana", "Cherry", "Avocado");

// Stream パイプライン
List<String> result = items.stream()
    .filter(s -> s.startsWith("A"))       // 中間操作（遅延評価）
    .map(String::toUpperCase)             // 中間操作
    .sorted()                             // 中間操作
    .toList();                            // 終端操作 (Java 16+ 不変リスト生成)
```

#### 高度な集計 (`Collectors.groupingBy`)
```java
Map<String, List<Product>> byCategory = products.stream()
    .collect(Collectors.groupingBy(Product::category));
```

---

### 4-4. Stream Gatherers による中間操作の革新 (JEP 461/473/485)
*(対応モジュール: `14: StreamGatherersAndPipelines.java`)*

Java 8 の誕生以来、Stream API の中間操作は `filter`, `map`, `flatMap` などの固定メソッドに限られていました。**Stream Gatherers** は、ストリームの中間処理を完全に独自拡張可能にする革新的機能です。

```java
// 1. windowFixed: 固定サイズでチャンク化（バッチ処理）
List<List<Integer>> batches = Stream.of(1, 2, 3, 4, 5, 6, 7)
    .gather(Gatherers.windowFixed(3))
    .toList(); // [[1, 2, 3], [4, 5, 6], [7]]

// 2. windowSliding: スライディングウィンドウ（移動平均・トレンド分析）
List<List<Integer>> windows = Stream.of(1, 2, 3, 4, 5)
    .gather(Gatherers.windowSliding(3))
    .toList(); // [[1, 2, 3], [2, 3, 4], [3, 4, 5]]

// 3. scan: 累積和・累積状態のストリーム生成
List<Integer> runningTotals = Stream.of(10, 20, 30)
    .gather(Gatherers.scan(() -> 0, Integer::sum))
    .toList(); // [10, 30, 60]

// 4. mapConcurrent: 仮想スレッドによる超高並列マッピング
List<String> responses = urls.stream()
    .gather(Gatherers.mapConcurrent(50, this::fetchHttp))
    .toList();
```

---

## 5. 第3部: エラーハンドリングとリソース安全

### 5-1. 検査例外 (Checked) vs 非検査例外 (Unchecked) の現代的作法
*(対応モジュール: `03: ExceptionAndResource.java`)*

- **検査例外 (`Exception` 継承)**: メソッドシグネチャに `throws` を強制する Java 特有の機構。
  - **現代の実務評価**: コードが極めて冗長になり、ラムダ式や Stream API との相性が最悪なため、**Spring Boot やモダンなライブラリではアンチパターン** と見なされています。
- **非検査例外 (`RuntimeException` 継承)**:
  - **モダンJavaの標準**: 独自の業務例外はすべて `RuntimeException` を継承して作成します。

---

### 5-2. `try-with-resources` と `AutoCloseable` (RAII)
*(対応モジュール: `03: ExceptionAndResource.java`)*

C# の `using`、Rust の `Drop`、Go の `defer` に相当する安全なリソース解放機構です。

```java
// AutoCloseable を実装したリソースは、ブロックを抜ける際に確実に close() が呼ばれる
try (var in = Files.newInputStream(path);
     var out = Files.newOutputStream(targetPath)) {
    in.transferTo(out);
} // 例外が発生しても、確実に out -> in の順で自動クローズされる
```

---

### 5-3. `Optional<T>` の正しい境界づけとアンチパターン
*(対応モジュール: `03: ExceptionAndResource.java`)*

Rust の `Option<T>` に似ていますが、**Java の Optional は用途が厳格に限定されています**。

| やって良いこと (DO) | やってはいけないこと (DON'T) |
| :--- | :--- |
| **メソッドの戻り値** として「結果が存在しない可能性」を表現する | クラスの**フィールド**に `Optional` を保持する（メモリの無駄・シリアライズ不可） |
| `.map()`, `.filter()`, `.orElse()`, `.orElseGet()` で関数的に処理する | メソッドの**引数**に `Optional` を渡す（冗長） |
| `.orElseThrow(...)` で明示的に業務例外へ変換する | **`.get()` を直呼びする**（値がないと `NoSuchElementException` でクラッシュ） |

---

## 6. 第4部: 並行処理・非同期・Project Loom

### 6-1. 仮想スレッド (Virtual Threads) の衝撃と M:N スケジューリング
*(対応モジュール: `04: ConcurrencyAndVirtualThreads.java`)*

Java 21 の最大の目玉機能が **Virtual Threads（Project Loom）** です。

```
[従来のOSスレッド (1:1)]
Java Thread  -------> OS Kernel Thread (約 1MB スタック消費, 数千個が限界)

[仮想スレッド (M:N)]
Virtual Thread 1 \
Virtual Thread 2  -----> キャリアスレッド (OSスレッド / CPUコア数分のみ常駐)
Virtual Thread N /       (仮想スレッドはヒープ上にわずか数百バイト)
```

```java
// 仮想スレッドを1万個起動しても、メモリは数MBしか消費せず瞬時に完了！
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    for (int i = 0; i < 10_000; i++) {
        executor.submit(() -> {
            Thread.sleep(Duration.ofMillis(100)); // ブロッキングI/O
            return "OK";
        });
    }
} // 全タスク完了まで待機
```

> **革命的な理由**:  
> Go の goroutine と同様、スレッドがブロッキングI/O（ネットワーク待ちやDB待ち）に入ると、自動的にキャリアスレッドからアンマウント（退避）され、CPUを他の仮想スレッドに譲ります。  
> したがって、**コードを一切非同期（Reactive / async-await）に書き直すことなく、プレーンな同期コードのまま最高のスループットを達成できます。**

---

### 6-2. `CompletableFuture` による非同期パイプライン
*(対応モジュール: `04: ConcurrencyAndVirtualThreads.java`)*

JavaScript の `Promise` や C# の `Task` に相当する非同期計算の合成機構です。

```java
CompletableFuture.supplyAsync(() -> fetchPrice())
    .thenApply(price -> price * 1.1) // 消費税計算
    .thenAccept(total -> System.out.println("Total: " + total))
    .exceptionally(ex -> {
        System.err.println("Error: " + ex.getMessage());
        return null;
    });
```

---

### 6-3. 構造化並行性 (`StructuredTaskScope`) によるスレッドリーク根絶
*(対応モジュール: `07: StructuredConcurrencyAndScopedValues.java`)*

従来の非同期コードは、親タスクが失敗しても子タスクが裏で動き続ける「スレッドリーク」のリスクを抱えていました。**構造化並行性** は、並行サブタスクのライフサイクルを構文ブロックに閉じ込めます。

```java
try (var scope = StructuredTaskScope.open()) {
    Subtask<User> userTask   = scope.fork(() -> fetchUser());
    Subtask<Order> orderTask = scope.fork(() -> fetchOrders());

    // 両方のサブタスクの完了を待機
    scope.join();

    // 結果を取得（いずれかが失敗した場合はキャンセル伝播）
    User user = userTask.get();
    Order order = orderTask.get();
}
```

---

### 6-4. スコープ付き値 (`ScopedValue`) による軽量コンテキスト
*(対応モジュール: `07: StructuredConcurrencyAndScopedValues.java`)*

従来の `ThreadLocal` は可変であり、仮想スレッドが数十万個起動した際に莫大なメモリを消費する欠点がありました。`ScopedValue` は **不変で高速、スコープを抜けると自動破棄される軽量なコンテキスト受け渡し機構** です。

```java
private static final ScopedValue<String> TRACE_ID = ScopedValue.newInstance();

// スコープをバインドして実行
ScopedValue.where(TRACE_ID, "req-xyz-999").run(() -> {
    // 呼び出し先メソッドのどこからでも引数引き回し不要で取得可能
    System.out.println("Trace: " + TRACE_ID.get());
});
```

---

## 7. 第5部: 低レイヤ・ネイティブ連携・JVM解剖

### 7-1. NIO.2 (`Files` & `Path`) と現代的標準 `HttpClient`
*(対応モジュール: `08: ModernIOAndHttpClient.java`)*

- **NIO.2 (`java.nio.file.Files`)**: 行のストリーム読み込み (`Files.lines`)、ワンライナーでの読み書き (`Files.readString`, `Files.writeString`)。
- **標準 `HttpClient` (Java 11+)**: HTTP/2、WebSocket、非同期 (`sendAsync`) をネイティブサポート。外部ライブラリ（Apache HttpClient 等）は不要です。

---

### 7-2. アノテーション・リフレクション・動的プロキシ (Spring AOPの正体)
*(対応モジュール: `09: ReflectionAndProxies.java`)*

Spring Boot の `@Transactional` や `@AuditLog` がどのようにして裏で動いているのか？その正体は **`java.lang.reflect.Proxy`（動的プロキシ）** によるメソッド呼び出しのインターセプトです。

```java
// インターフェースのメソッド呼び出しを動的に横取りするプロキシ
PaymentService proxy = (PaymentService) Proxy.newProxyInstance(
    PaymentService.class.getClassLoader(),
    new Class<?>[]{ PaymentService.class },
    (p, method, args) -> {
        System.out.println("[BEFORE] Transaction started");
        Object result = method.invoke(targetInstance, args); // 本体の実行
        System.out.println("[AFTER] Transaction committed");
        return result;
    }
);
```

---

### 7-3. JVMメモリ構造・JMM (`volatile` & happens-before)・超低遅延GC
*(対応モジュール: `10: JvmMemoryAndPerformance.java`)*

#### JVM メモリ領域
1. **ヒープ (Heap)**: インスタンスや配列が配置される領域。GCの管理対象。
2. **スタック (Stack)**: スレッドごとに確保されるローカル変数とコールフレーム。
3. **メタスペース (Metaspace)**: クラスのメタデータが配置されるネイティブメモリ領域。

#### JMM (Java Memory Model) と `volatile`
マルチコアCPU環境では、各コアがL1/L2キャッシュを持つため、あるスレッドが変数を書き換えても他のスレッドから見えない問題（可視性問題）が発生します。
- **`volatile` 修飾子**:
  - メモリバリア（CPUフェンス）を挿入し、キャッシュではなくメインメモリ経由の読み書きを保証。
  - 命令の並べ替え（リオーダリング）を防止し、**happens-before 関係** を確立。

#### 現代の GC 事情 (G1GC vs ZGC)
- **G1GC (デフォルト)**: スループットと停止時間のバランス型（停止時間目安: 数十ms〜数百ms）。
- **Generational ZGC (`-XX:+UseZGC`)**: **TB級の超巨大ヒープでも停止時間（STW）が 1ms 未満** の超低遅延ガベージコレクタ。

---

### 7-4. Project Panama: Foreign Function & Memory API (FFM API)
*(対応モジュール: `13: ForeignFunctionAndMemoryAPI.java`)*

Java 22 で正式標準化された FFM API は、長年親しまれた JNI を完全に過去のものにしました。

```java
// 1. オフヒープメモリの安全な割り当て（try-with-resources で即座にOSへ返還）
try (Arena arena = Arena.ofConfined()) {
    MemorySegment segment = arena.allocate(ValueLayout.JAVA_INT, 100);
    segment.setAtIndex(ValueLayout.JAVA_INT, 0, 42);

    // 2. C言語の標準関数 (strlen) を純粋なJavaコードから直接呼び出し
    Linker linker = Linker.nativeLinker();
    MethodHandle strlen = linker.downcallHandle(
        linker.defaultLookup().find("strlen").orElseThrow(),
        FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS)
    );

    MemorySegment cStr = arena.allocateFrom("Hello Panama!");
    long len = (long) strlen.invokeExact(cStr);
    System.out.println("C strlen returned: " + len); // 13
}
```

---

## 8. 第6部: デザインパターン & アーキテクチャのモダナイズ

### 8-1. Modern Strategy / Modern State
*(対応モジュール: `12: ModernDesignPatterns.java`)*

- **Modern Strategy**: 従来のインターフェース実装クラス群を廃止し、`Function<T, R>` やラムダ式をマップや変数に代入するだけで実現。
- **Modern State**: 複雑な状態遷移ロジックを、`sealed interface` と `switch` 式に集約することで、遷移漏れをコンパイル時に 100% 防止。

---

### 8-2. Reactive Streams (`Flow` API)
*(対応モジュール: `12: ModernDesignPatterns.java`)*

`java.util.concurrent.Flow`（`Publisher`, `Subscriber`, `Subscription`）により、非同期・ノンブロッキングな背圧（Backpressure）制御を備えたイベント配信モデルを構築できます。

---

### 8-3. 現代的 `Result` 型パターンによるドメイン駆動設計
*(対応モジュール: `16: FlexibleConstructorAndModernLanguage.java`)*

Rust の `Result<T, E>` や Swift の `Result` と同等のイディオムを `sealed interface` とレコードでエレガントに構築できます。

```java
public sealed interface Result<T, E> {
    record Success<T, E>(T value) implements Result<T, E> {}
    record Failure<T, E>(E error) implements Result<T, E> {}
}

// 呼び出し側のパターンマッチング
String message = switch (result) {
    case Result.Success(var val) -> "Processed: " + val;
    case Result.Failure(var err) -> "Failed with: " + err;
};
```

---

## 9. 全16モジュール対照カタログ & クイックリファレンス

すべてのソースコードは `src/sample/` に配置されています。

| # | ソースファイル | 主要トピック |
| :-: | :--- | :--- |
| **01** | [`BasicsAndTypes.java`](./src/sample/BasicsAndTypes.java) | プリミティブ vs 参照型、`==` vs `equals`、`var`、Text Blocks、`record`、`sealed interface` |
| **02** | [`CollectionsAndStreams.java`](./src/sample/CollectionsAndStreams.java) | 不変リスト (`List.of`)、Stream パイプライン、`groupingBy`、`IntStream` |
| **03** | [`ExceptionAndResource.java`](./src/sample/ExceptionAndResource.java) | 検査例外 vs 非検査例外、`try-with-resources`、`Optional` のベストプラクティス |
| **04** | [`ConcurrencyAndVirtualThreads.java`](./src/sample/ConcurrencyAndVirtualThreads.java) | **Virtual Threads** (1万並行)、`CompletableFuture`、`ConcurrentHashMap` |
| **05** | [`LambdaDeepDive.java`](./src/sample/LambdaDeepDive.java) | ラムダ構文、標準SAM (`java.util.function`)、メソッド参照 4形態、例外ラップ |
| **06** | [`GenericsDeepDive.java`](./src/sample/GenericsDeepDive.java) | 型消去 (Type Erasure)、PECS 原則 (`? extends`, `? super`)、再帰的境界ジェネリクス |
| **07** | [`StructuredConcurrencyAndScopedValues.java`](./src/sample/StructuredConcurrencyAndScopedValues.java) | **`StructuredTaskScope`** (構造化並行性)、**`ScopedValue`** (不変コンテキスト) |
| **08** | [`ModernIOAndHttpClient.java`](./src/sample/ModernIOAndHttpClient.java) | **NIO.2 (`Files`/`Path`)** 行走査、標準 **`HttpClient`** (HTTP/2 & 非同期通信) |
| **09** | [`ReflectionAndProxies.java`](./src/sample/ReflectionAndProxies.java) | カスタムアノテーション、リフレクション、**動的プロキシ (`Proxy`)** による AOP |
| **10** | [`JvmMemoryAndPerformance.java`](./src/sample/JvmMemoryAndPerformance.java) | JVM メモリ解剖、**JMM (`volatile` & happens-before)**、G1GC vs ZGC (世代別ZGC) |
| **11** | [`SequencedCollectionsAndSafety.java`](./src/sample/SequencedCollectionsAndSafety.java) | **Java 21 `SequencedCollection`**、防衛的コピー (`List.copyOf`)、record検証 |
| **12** | [`ModernDesignPatterns.java`](./src/sample/ModernDesignPatterns.java) | Modern Strategy、Modern State (`sealed` + `switch`)、Reactive Streams (`Flow`) |
| **13** | [`ForeignFunctionAndMemoryAPI.java`](./src/sample/ForeignFunctionAndMemoryAPI.java) | **Project Panama FFM API** (`Arena`, `MemorySegment`, Cライブラリダウンコール) |
| **14** | [`StreamGatherersAndPipelines.java`](./src/sample/StreamGatherersAndPipelines.java) | **Stream Gatherers** (`windowFixed`, `windowSliding`, `scan`, `mapConcurrent`, 自作Gatherer) |
| **15** | [`RecordPatternsAndAdvancedMatching.java`](./src/sample/RecordPatternsAndAdvancedMatching.java) | **レコードパターン分解**、ネスト深層マッチング、無名変数 (`_`)、網羅的 switch |
| **16** | [`FlexibleConstructorAndModernLanguage.java`](./src/sample/FlexibleConstructorAndModernLanguage.java) | **Statements before `super(...)`**、Result型パターン、ゼロボイラープレート設計 |

- [`Main.java`](./src/sample/Main.java): 上記全16モジュールを順次実行する統合エントリーポイント

---

## 10. 実務におけるビルド & エコシステム指針

実務のエンタープライズ開発で推奨されるスタックです：

1. **ビルドツール**:
   - **Gradle (Kotlin DSL / `build.gradle.kts`)**: 高速なインクリメンタルビルドと洗練された依存関係管理。
   - **Maven (`pom.xml`)**: 枯れた安定性と規約重視のプロジェクト向け。
2. **フレームワーク**:
   - **Spring Boot 3.x**: Java 21+ と Virtual Threads をネイティブサポートする業界標準。
   - **Quarkus**: GraalVM Native Image との相性が抜群で、コンテナ起動速度ミリ秒台を誇るクラウドネイティブ向け。
3. **テスティング**:
   - **JUnit 5**: `@ParameterizedTest` などの強力なテスト機構。
   - **AssertJ**: 流暢なアサーション構文（`assertThat(result).isEqualTo(...)`）。
   - **Testcontainers**: Docker コンテナ（PostgreSQL, Redis 等）をテストコードから自動起動して行う本物の統合テスト。

---

> 💡 **実行方法**:  
> プロジェクト直下で `.\build_and_run.ps1` を実行するだけで、全16モジュールの動作検証結果を確認できます。
