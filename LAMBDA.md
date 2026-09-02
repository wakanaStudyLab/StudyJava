# Java ラムダ式 完全理解ガイド (Complete Guide to Java Lambdas)

Java 8 で導入され、現代の Java（Java 17 / 21 LTS 以降）において**避けては通れない最重要機能「ラムダ式（Lambda Expression）」**の完全解説書です。

「書き方はなんとなく知っているけれど、内部で何が起きているのか分からない」「メソッド参照 `::` のパターンで混乱する」「実質的 final や例外処理でつまずく」といった疑問を根本から解消します。

---

## 📑 目次

1. [ラムダ式の誕生背景と本質（なぜ導入されたのか？）](#1-ラムダ式の誕生背景と本質なぜ導入されたのか)
2. [ラムダ式の基本構文と省略ルール完全マスター](#2-ラムダ式の基本構文と省略ルール完全マスター)
3. [関数型インターフェース（SAM）の正体](#3-関数型インターフェースsamの正体)
4. [標準組み込み関数型インターフェース完全攻略 (`java.util.function`)](#4-標準組み込み関数型インターフェース完全攻略-javautilfunction)
5. [メソッド参照 (`::`) の4大パターン完全攻略](#5-メソッド参照--の4大パターン完全攻略)
6. [変数キャプチャとスコープ（実質的 final の壁）](#6-変数キャプチャとスコープ実質的-final-の壁)
7. [ラムダ式とチェック例外の壁・対処法](#7-ラムダ式とチェック例外の壁対処法)
8. [実務頻出の実践パターン & デザインパターン再構築](#8-実務頻出の実践パターン--デザインパターン再構築)
9. [アンダー・ザ・フード：内部実装（`invokedynamic` と性能）](#9-アンダーザフード内部実装invokedynamic-と性能)
10. [他言語エンジニア向け対応表 (Java vs C# vs Rust vs Go vs TS)](#10-他言語エンジニア向け対応表-java-vs-c-vs-rust-vs-go-vs-ts)
11. [理解度チェッククイズ & よくあるエラー虎の巻](#11-理解度チェッククイズ--よくあるエラー虎の巻)

---

## 1. ラムダ式の誕生背景と本質（なぜ導入されたのか？）

### 1-1. Java 7 以前の「無名内部クラス」地獄
ラムダ式が登場する前、Java で「何らかの処理（ロジック）」をメソッドに渡すには、**無名内部クラス（Anonymous Inner Class）** をインスタンス化するしかありませんでした。

```java
// ❌ Java 7 以前: たった1行の処理を渡すのに 5行以上の儀式コードが必要
Collections.sort(names, new Comparator<String>() {
    @Override
    public int compare(String a, String b) {
        return a.compareToIgnoreCase(b);
    }
});

// スレッドの起動も同様
new Thread(new Runnable() {
    @Override
    public void run() {
        System.out.println("Hello from thread!");
    }
}).start();
```

これには以下のような致命的な問題がありました：
1. **圧倒的なボイラープレート（儀式コード）**: 本質的なロジックは `a.compareToIgnoreCase(b)` だけなのに、型名やメソッド宣言のノイズで埋もれる。
2. **意図が読み取りにくい**: 「何をしたいか」が直感的に伝わらない。
3. **無駄なクラスファイルの生成**: コンパイル時に `MyClass$1.class` のような無名クラスファイルが作られ、クラスローダやメモリに負荷がかかる。

### 1-2. ラムダ式による解決
ラムダ式は、**「コード（振る舞い・関数）」をデータ（値）と同じように変数に代入したり、メソッドの引数に渡したり、戻り値として返したりできるようにする仕組み**（第一級関数：First-Class Functions の概念）です。

```java
// ⭕ モダンJava（ラムダ式）: 本質的なロジックのみを記述
names.sort((a, b) -> a.compareToIgnoreCase(b));

// メソッド参照ならさらに簡潔
names.sort(String::compareToIgnoreCase);

new Thread(() -> System.out.println("Hello from thread!")).start();
```

---

## 2. ラムダ式の基本構文と省略ルール完全マスター

ラムダ式の基本形は **`(引数リスト) -> { 処理本文 }`** です。
矢印記号 `->` は「引数を受け取って、右側の処理を実行する」という意味を持ちます。

### 2-1. 構文の省略ルール一覧

Javaのコンパイラは強力な型推論を行うため、多くの記述を省略できます。

| パターン | 完全な記法 (Full) | 省略記法 (Idiomatic) | 省略可能な条件 |
| :--- | :--- | :--- | :--- |
| **引数型** | `(Integer x, Integer y) -> { return x + y; }` | `(x, y) -> x + y` | コンパイラが文脈から引数の型を推論できる場合 |
| **引数カッコ `()`** | `(s) -> { return s.length(); }` | `s -> s.length()` | **引数が1つ**のときのみカッコを省略可能 |
| **引数なし** | `() -> { return 42; }` | `() -> 42` | **引数がない場合は `()` の省略不可** |
| **ブロック `{}`** | `x -> { return x * 2; }` | `x -> x * 2` | 処理が**単一の式（Expression）**の場合、`{}` と `return` と `;` をセットで省略 |
| **戻り値 void** | `s -> { System.out.println(s); }` | `s -> System.out.println(s)` | 処理が1文なら `{}` と末尾の `;` を省略可能 |

### 2-2. よくある間違い・注意点

```java
// ❌ コンパイルエラー例1: 片方だけ型を書くことはできない
(int x, y) -> x + y

// ❌ コンパイルエラー例2: 引数がないのにカッコを省く
-> System.out.println("hello") // 正解: () -> System.out.println("hello")

// ❌ コンパイルエラー例3: {} をつけたのに return を書かない（戻り値が必要な場合）
(a, b) -> { a + b; } // 正解: (a, b) -> a + b または (a, b) -> { return a + b; }

// ❌ コンパイルエラー例4: 式のまま return を書く
(a, b) -> return a + b // 正解: (a, b) -> a + b
```

### 2-3. 引数での `var` の利用 (Java 11+)
Java 11 から、ラムダ式の引数に `var` を使えるようになりました。
型名は省略したいが、**引数にアノテーション（`@NonNull` や `@Nullable` 等）を付与したい場合**に重宝します。

```java
// 型を明示せずにアノテーションを付与できる
(@NotNull var a, @NotNull var b) -> a.compareTo(b)
```

---

## 3. 関数型インターフェース（SAM）の正体

### 3-1. ラムダ式の「型」は何なのか？
Java は C# の `Func<T, R>` や TypeScript の `(x: number) => number` のような**独立した「関数型（Function Type）」を持っていません**。

その代わり、Java は既存のオブジェクト指向システムと後方互換性を保つため、
**「抽象メソッドが1つだけ定義されたインターフェース（SAM: Single Abstract Method）」** を関数の型として扱います。これを **関数型インターフェース (Functional Interface)** と呼びます。

```java
// 抽象メソッドが 1 つだけ = 関数型インターフェース
@FunctionalInterface
public interface StringTransformer {
    String transform(String input); // 唯一の抽象メソッド
}
```

コンパイラは、ラムダ式が代入される場所の「期待される型」を見て、自動的にそのインターフェースの実装としてバインドします。これを **ターゲット型推論 (Target Typing)** と呼びます。

```java
// StringTransformer 型として推論される
StringTransformer upper = s -> s.toUpperCase();
String result = upper.transform("hello"); // "HELLO"
```

### 3-2. `@FunctionalInterface` アノテーション
- 必須ではありませんが、宣言につけることで「これが関数型インターフェースであること」をコンパイラに明示します。
- メソッドを誤って2つ以上追加した場合、**コンパイルエラーとして検知**してくれます。

```java
@FunctionalInterface
public interface Calculator {
    int calculate(int a, int b);
    
    // ⭕ default メソッドがあっても SAM 条件を満たす
    default void printInfo() {
        System.out.println("Calculator interface");
    }

    // ⭕ static メソッドがあっても SAM 条件を満たす
    static int add(int a, int b) {
        return a + b;
    }

    // ⭕ java.lang.Object の public メソッドのオーバーライド宣言があってもノーカウント
    @Override
    boolean equals(Object obj);
}
```

---

## 4. 標準組み込み関数型インターフェース完全攻略 (`java.util.function`)

自分で `@FunctionalInterface` を定義しなくても、Java 標準ライブラリが用意している `java.util.function` パッケージのインターフェースを使えば、実務の99%をカバーできます。

### 4-1. 最重要 4大インターフェース + 2大派生

```
           引数あり                     引数なし
        ┌──────────────┐             ┌──────────────┐
戻り値  │ Function<T,R>│             │  Supplier<T> │
あり    │ (T -> R 変換) │             │ ( () -> T )  │
        └──────────────┘             └──────────────┘
        ┌──────────────┐             ┌──────────────┐
戻り値  │  Consumer<T> │             │   Runnable   │
なし    │ (T -> void)  │             │ ( () -> void)│
        └──────────────┘             └──────────────┘
        ┌──────────────┐
真偽値  │ Predicate<T> │
        │(T -> boolean)│
        └──────────────┘
```

| インターフェース | メソッド宣言 | 引数 | 戻り値 | 主な用途・Streamでの対応メソッド |
| :--- | :--- | :--- | :--- | :--- |
| **`Predicate<T>`** | `boolean test(T t)` | `T` | `boolean` | 条件判定・フィルター (`stream.filter(...)`) |
| **`Consumer<T>`** | `void accept(T t)` | `T` | `void` | 値の消費・副作用 (`stream.forEach(...)`) |
| **`Function<T, R>`** | `R apply(T t)` | `T` | `R` | 値の変換・マッピング (`stream.map(...)`) |
| **`Supplier<T>`** | `T get()` | なし | `T` | 値の生成・遅延評価 (`optional.orElseGet(...)`) |
| **`UnaryOperator<T>`** | `T apply(T t)` | `T` | `T` | 引数と戻り値が同型の変換 (`Function<T, T>` の特化) |
| **`BinaryOperator<T>`** | `T apply(T t1, T t2)`| `T, T` | `T` | 同型2引数の集約 (`stream.reduce(...)`) |

### 4-2. 具体的な使用コード例

```java
// 1. Predicate: 文字列が空かどうか判定
Predicate<String> isEmpty = s -> s.isEmpty();
System.out.println(isEmpty.test("")); // true

// 2. Consumer: 値を受け取ってログ出力
Consumer<String> logger = msg -> System.out.println("[LOG] " + msg);
logger.accept("処理が完了しました");

// 3. Function: 文字列を文字数に変換
Function<String, Integer> lengthFunc = s -> s.length();
System.out.println(lengthFunc.apply("Java")); // 4

// 4. Supplier: 呼び出されたタイミングで新しいUUIDを生成
Supplier<String> idGenerator = () -> UUID.randomUUID().toString();
System.out.println(idGenerator.get()); // "d3b07384-..."

// 5. UnaryOperator: 文字列の前後にブラケットを付加
UnaryOperator<String> bracket = s -> "[" + s + "]";
System.out.println(bracket.apply("OK")); // "[OK]"

// 6. BinaryOperator: 2つの数値を足し算
BinaryOperator<Integer> adder = (a, b) -> a + b;
System.out.println(adder.apply(10, 20)); // 30
```

### 4-3. 2引数版 (Bi-系)
引数を2つ取るバージョンも標準で用意されています。
- `BiPredicate<T, U>` : `(T, U) -> boolean`
- `BiConsumer<T, U>` : `(T, U) -> void` (例: `Map.forEach((k, v) -> ...)`)
- `BiFunction<T, U, R>` : `(T, U) -> R`

### 4-4. プリミティブ特化型（Auto-Boxingのオーバーヘッド回避）
Javaのジェネリクス `Function<Integer, Integer>` は参照型しか扱えないため、大量の数値データを処理すると `int` ↔ `Integer` の間で **Auto-Boxing / Unboxing によるヒープ確保とGC負荷** が発生します。

これを回避するために、プリミティブ専用の型が提供されています：
- **`IntPredicate`**, **`LongPredicate`**, **`DoublePredicate`**
- **`IntConsumer`**, **`LongConsumer`**, **`DoubleConsumer`**
- **`IntFunction<R>`**, **`ToIntFunction<T>`**, **`IntToLongFunction`** 等
- **`IntUnaryOperator`**, **`IntBinaryOperator`**

> **💡 ベストプラクティス**: 数万〜数百万回の計算ループや数値 Stream では、必ず `IntStream` とプリミティブ特化関数型インターフェース（`IntPredicate` 等）を使用してください。

### 4-5. 関数の合成 (Composition)
関数型インターフェースには、関数同士を合成する `default` メソッドが備わっています。

```java
// Predicate の合成 (and, or, negate)
Predicate<String> nonNull = Objects::nonNull;
Predicate<String> nonEmpty = s -> !s.isBlank();
Predicate<String> isValid = nonNull.and(nonEmpty); // null でなく、かつ空白でもない

// Function の合成 (andThen vs compose)
Function<Integer, Integer> multiply2 = x -> x * 2;
Function<Integer, Integer> add10 = x -> x + 10;

// andThen: multiply2 を実行した後に add10 を実行 ( (5 * 2) + 10 = 20 )
Function<Integer, Integer> mulThenAdd = multiply2.andThen(add10);
System.out.println(mulThenAdd.apply(5)); // 20

// compose: add10 を実行した後に multiply2 を実行 ( (5 + 10) * 2 = 30 )
Function<Integer, Integer> addThenMul = multiply2.compose(add10);
System.out.println(addThenMul.apply(5)); // 30
```

---

## 5. メソッド参照 (`::`) の4大パターン完全攻略

「ラムダ式が単に既存のメソッドを呼び出しているだけ」の場合、**メソッド参照 `::`（コロン2つ）** を使うことでさらに簡潔に記述できます。

初心者が最もつまずくのは、**「いつクラス名を書くのか？」「いつ変数名を書くのか？」** です。以下の4つの分類を覚えれば完璧に区別できます。

```
                    ┌─────────────────────────┐
                    │  メソッド参照の4パターン  │
                    └────────────┬────────────┘
     ┌───────────────────┬───────┴───────────┬───────────────────┐
     ▼                   ▼                   ▼                   ▼
1. 静的メソッド       2. 特定インスタンス     3. 任意インスタンス     4. コンストラクタ
ClassName::static   instance::method    ClassName::method   ClassName::new
```

### パターン1: 静的（static）メソッド参照
- **形式**: `ClassName::staticMethod`
- **等価なラムダ**: `(args) -> ClassName.staticMethod(args)`

```java
// ラムダ式
Function<String, Integer> f1 = s -> Integer.parseInt(s);
// メソッド参照
Function<String, Integer> f2 = Integer::parseInt;

BinaryOperator<Double> max1 = (a, b) -> Math.max(a, b);
BinaryOperator<Double> max2 = Math::max;
```

### パターン2: 特定オブジェクト（インスタンス）のメソッド参照
- **形式**: `instanceVariable::instanceMethod`
- **等価なラムダ**: `(args) -> instanceVariable.instanceMethod(args)`
- 外側ですでに存在する変数（レシーバー）のメソッドを呼び出す。

```java
String prefix = "DEBUG: ";
// prefix という「特定のインスタンス」のメソッドを呼ぶ
Consumer<String> printer1 = s -> System.out.println(s);
Consumer<String> printer2 = System.out::println; // System.out が特定インスタンス
```

### パターン3: 任意オブジェクト（第1引数がレシーバーになる）メソッド参照 ★最重要★
- **形式**: `ClassName::instanceMethod`
- **等価なラムダ**: `(obj, args) -> obj.instanceMethod(args)`
- **見分け方**: クラス名を書いているのに、呼び出すのは `static` ではなく**インスタンスメソッド**であるパターン。ラムダ式の**第1引数のオブジェクトに対してメソッドが呼び出されます**。

```java
// ラムダ式: 第1引数の s に対して s.toUpperCase() を呼んでいる
Function<String, String> upper1 = s -> s.toUpperCase();
// メソッド参照: クラス名::インスタンスメソッド名
Function<String, String> upper2 = String::toUpperCase;

// 2引数の場合: 第1引数 a に対して a.compareToIgnoreCase(b) を呼ぶ
Comparator<String> comp1 = (a, b) -> a.compareToIgnoreCase(b);
Comparator<String> comp2 = String::compareToIgnoreCase;

// Getter の参照（実務で一番使う！）
Function<User, String> getName1 = user -> user.getName();
Function<User, String> getName2 = User::getName;
```

### パターン4: コンストラクタ参照
- **形式**: `ClassName::new` または `Type[]::new`
- **等価なラムダ**: `(args) -> new ClassName(args)`

```java
// 引数なしコンストラクタ (Supplier)
Supplier<List<String>> listSupplier1 = () -> new ArrayList<>();
Supplier<List<String>> listSupplier2 = ArrayList::new;

// 引数ありコンストラクタ (Function)
Function<String, User> userFactory1 = name -> new User(name);
Function<String, User> userFactory2 = User::new;

// 配列コンストラクタ (IntFunction)
IntFunction<String[]> arrayFactory1 = size -> new String[size];
IntFunction<String[]> arrayFactory2 = String[]::new;
```

---

## 6. 変数キャプチャとスコープ（実質的 final の壁）

### 6-1. Variable Capture（変数のキャプチャ）とは？
ラムダ式の中から、ラムダ式の外側で宣言されたローカル変数を参照（キャプチャ）することができます。

```java
String prefix = "[INFO] ";
Consumer<String> logger = msg -> System.out.println(prefix + msg); // prefix をキャプチャ
```

### 6-2. 「実質的 final (Effectively Final)」の制約
キャプチャするローカル変数は、**`final` 修飾されているか、または「再代入されていない（実質的 final）」** でなければなりません。

```java
int count = 0;
// ❌ コンパイルエラー: 
// Local variable count defined in an enclosing scope must be final or effectively final
Runnable r = () -> {
    count++; // 再代入しようとするとエラー！
};

int port = 8080;
port = 8081; // ここで再代入しているため、port は実質的 final ではなくなる
Runnable r2 = () -> System.out.println(port); // ❌ コンパイルエラー！
```

### 6-3. なぜ Java は final を強制するのか？（メモリ構造の理由）
Rust や C#、JavaScript のクロージャを知っている人は「なぜ Java では外側の変数を書き換えられないのか？」と疑問に思うはずです。

その理由は、Java の**スタックメモリとヒープメモリのライフサイクルの違い**にあります：

```
[スタック領域 (メソッド実行フレーム)]        [ヒープ領域 (GC管理)]
┌────────────────────────────────┐         ┌───────────────────────────────┐
│ メソッド実行中...               │         │ ラムダ式インスタンス            │
│ int count = 10;                │ ──コピー─>│ (int count のコピーを保持)     │
│ メソッド終了時に即座に破棄！     │         │ 別スレッドで生き続ける可能性  │
└────────────────────────────────┘         └───────────────────────────────┘
```

1. ローカル変数は**スレッドのコールスタック**に確保され、メソッドを抜けると消滅します。
2. 一方、ラムダ式は**ヒープ領域**に生成されるオブジェクトであり、メソッド終了後も別のスレッド等で生存し続ける可能性があります。
3. そのため、Java はローカル変数の「実体（メモリ番地）」ではなく、**「値のコピー」をラムダ式内に保持（キャプチャ）** しています。
4. もし外側の変数やラムダ内の変数を変更できてしまうと、「スタック上の値」と「ヒープ上にコピーされた値」の間で不整合が発生し、さらにマルチスレッド並行アクセス時に致命的な競合状態（Race Condition）を引き起こすため、Java 言語仕様として「再代入不可（final）」を強制しています。

### 6-4. どうしてもカウントアップしたい時の対処法
実務で「ラムダ内で値を加算したい」場合、**参照（ヒープ上のオブジェクト）** を通して行います。

```java
// 対処法1: AtomicInteger を使う（マルチスレッド安全・推奨）
AtomicInteger counter = new AtomicInteger(0);
List.of("a", "b", "c").forEach(item -> counter.incrementAndGet());
System.out.println(counter.get()); // 3

// 対処法2: 要素数1の配列（単一スレッドなら動作するが並行処理では非推奨）
int[] holder = new int[]{0};
List.of("a", "b", "c").forEach(item -> holder[0]++);

// ⭕ 最善のモダンJavaアプローチ: そもそも副作用（変数の変更）を避け、Streamで集計する
long count = List.of("a", "b", "c").stream().count();
```

### 6-5. スコープと `this` の扱い
無名クラスとラムダ式では、`this` の指す対象が決定的に異なります。

- **無名内部クラスの `this`**: その無名クラス自身のインスタンスを指す。
- **ラムダ式の `this`**: **ラムダ式を囲んでいる外側のクラスのインスタンス（Lexical Scope）** を指す。

```java
public class ScopeTest {
    private String name = "OuterClass";

    public void test() {
        // 無名内部クラスの場合
        Runnable r1 = new Runnable() {
            String name = "AnonymousClass";
            @Override
            public void run() {
                System.out.println(this.name); // "AnonymousClass" を出力
            }
        };

        // ラムダ式の場合
        Runnable r2 = () -> {
            // ラムダ自身は独自の this スコープを持たない
            System.out.println(this.name); // "OuterClass" を出力
        };
    }
}
```

---

## 7. ラムダ式とチェック例外の壁・対処法

### 7-1. なぜチェック例外（Checked Exception）でエラーになるのか？
Java 標準の関数型インターフェース（`Function`, `Consumer` 等）のメソッド定義には `throws Exception` がついていません。

そのため、ラムダ式の中で `IOException` や `SQLException` などのチェック例外を投げる処理を書くと、コンパイルエラーになります。

```java
List<String> paths = List.of("file1.txt", "file2.txt");

// ❌ コンパイルエラー: Unhandled exception type IOException
paths.forEach(path -> Files.readString(Path.of(path)));
```

### 7-2. 実務での3つの解決アプローチ

#### アプローチ1: ラムダ式の中で `try-catch` して非チェック例外にラップ（最も標準的）
```java
paths.forEach(path -> {
    try {
        String content = Files.readString(Path.of(path));
        System.out.println(content);
    } catch (IOException e) {
        throw new UncheckedIOException(e); // または RuntimeException
    }
});
```

#### アプローチ2: 例外をスローできるカスタム関数型インターフェース＆ラッパー関数を作る（美しい設計）
```java
// チェック例外をスローできるインターフェースを定義
@FunctionalInterface
public interface ThrowingConsumer<T, E extends Exception> {
    void accept(T t) throws E;

    // 標準の Consumer に変換するユーティリティ
    static <T> Consumer<T> unchecked(ThrowingConsumer<T, ?> f) {
        return t -> {
            try {
                f.accept(t);
            } catch (Exception e) {
                if (e instanceof RuntimeException re) throw re;
                throw new RuntimeException(e);
            }
        };
    }
}

// 呼び出し側が極めてスッキリ書ける
paths.forEach(ThrowingConsumer.unchecked(path -> {
    String content = Files.readString(Path.of(path));
    System.out.println(content);
}));
```

#### アプローチ3: Sneaky Throws（ジェネリクス型消去を利用した裏技）
コンパイラを騙してチェック例外を `throws` 宣言なしで投げる手法です。Lombok の `@SneakyThrows` もこの仕組みを使っています。

---

## 8. 実務頻出の実践パターン & デザインパターン再構築

ラムダ式と関数型インターフェースを活用することで、かつての古典的デザインパターン（GoF）の多くを極めてシンプルに記述できます。

### 8-1. Strategy（戦略）パターンの大幅簡素化
インターフェースを実装した無数のクラスファイル（`CreditPaymentStrategy.java`, `PaypalPaymentStrategy.java` 等）を作る必要がなくなります。

```java
// 支払い処理の関数型表現
@FunctionalInterface
public interface PaymentStrategy {
    void pay(int amount);
}

public class OrderService {
    public void checkout(int amount, PaymentStrategy strategy) {
        // 事前バリデーションや注文確定処理...
        strategy.pay(amount);
    }
}

// 利用側: クラスを作らずにその場でラムダ式やメソッド参照を渡す
orderService.checkout(5000, amt -> System.out.println("クレジットカードで決済: " + amt));
orderService.checkout(3000, amt -> System.out.println("PayPayで決済: " + amt));
```

### 8-2. 遅延評価（Lazy Evaluation）によるパフォーマンス最適化
「ログレベルが無効なとき、文字列結合や重いクエリを実行したくない」場合に `Supplier<T>` が活躍します。

```java
// ❌ 悪い例: ログが無効（INFOレベル）でも generateHeavyReport() が必ず即座に実行されてしまう
logger.debug("レポート生成結果: " + generateHeavyReport());

// ⭕ 良い例: Supplier を渡すことで、DEBUGレベルが有効な時だけ初めてメソッドが評価される
logger.debug(() -> "レポート生成結果: " + generateHeavyReport());
```

### 8-3. `Optional` との連携パイプライン
`null` チェックの `if` 文の連鎖を排除できます。

```java
// ユーザーのメールアドレスを取得し、大文字にして出力。なければデフォルト値を返す
String email = Optional.ofNullable(user)
    .map(User::getEmail)               // Function<User, String>
    .filter(e -> e.contains("@"))      // Predicate<String>
    .map(String::toUpperCase)          // Function<String, String>
    .orElseGet(() -> "NO_EMAIL@DOMAIN");// Supplier<String> (遅延評価)
```

### 8-4. モダンな `Comparator`（ソート処理の極致）
`Comparator` のファクトリメソッドとメソッド参照を組み合わせることで、直感的に多段ソートが組めます。

```java
record Employee(String department, String name, int salary) {}

List<Employee> employees = ...;

// 「部署昇順」 → 「給与降順」 → 「名前昇順」でソート
employees.sort(
    Comparator.comparing(Employee::department)
              .thenComparing(Comparator.comparingInt(Employee::salary).reversed())
              .thenComparing(Employee::name)
);
```

---

## 9. アンダー・ザ・フード：内部実装（`invokedynamic` と性能）

### 9-1. ラムダ式は「無名クラスの構文糖衣」ではない！
多くの人が「ラムダ式は、裏でコンパイラが無名内部クラスを自動生成しているだけ」と誤解しています。**これは明確に間違いです。**

もし無名クラスだとすると：
- ソースをコンパイルするたびに `Main$$Lambda$1.class` のような無数のクラスファイルがディスクに生成される。
- クラスロード時に JVM の PermGen / Metaspace メモリを浪費する。
- 呼び出しごとに `new` インスタンス化のヒープ割り当てオーバーヘッドが発生する。

### 9-2. `invokedynamic` (indy) と `LambdaMetafactory`
Java 8 以降、Java コンパイラはラムダ式を以下の手順で処理します：

1. **クラスファイル生成**: ラムダ式の本体コードを、そのクラス内の `private static synthetic` メソッドとしてバイトコードに埋め込む。
2. **`invokedynamic` 命令の配置**: ラムダ式を呼び出す箇所に、Java 7 で導入されたバイトコード命令 `invokedynamic` を配置する。
3. **実行時初回呼び出し (Bootstrap)**: 初回実行時、`LambdaMetafactory.metafactory` メソッドが呼ばれ、メモリ上で動的に軽量な関数オブジェクトの呼び出しリンケージを生成する。
4. **インスタンスの再利用**: 外側の変数をキャプチャしない（Stateless な）ラムダ式の場合、JVM は**単一のインスタンスをキャッシュして再利用（シングルトン化）**し、新規のオブジェクト生成（アロケーション）をゼロにします。

> **🚀 結論**: ラムダ式は無名内部クラスよりも**フットプリントが小さく、クラスロードが高速で、JVM JITコンパイラによるインライン化最適化の恩恵を最大限に受けられる**ように設計されています。

---

## 10. 他言語エンジニア向け対応表 (Java vs C# vs Rust vs Go vs TS)

| 概念 | Java (21+) | C# | Rust | Go | TypeScript |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **記法** | `(x) -> x * 2` | `x => x * 2` | `\|x\| x * 2` | `func(x int) int { return x * 2 }` | `(x) => x * 2` |
| **引数なし** | `() -> 42` | `() => 42` | `\|\| 42` | `func() int { return 42 }` | `() => 42` |
| **単一引数** | `x -> x * 2` | `x => x * 2` | `\|x\| x * 2` | `func(x int) int { ... }` | `x => x * 2` |
| **型システム** | SAM インターフェース | デリゲート (`Func`, `Action`) | トレイト (`Fn`, `FnMut`, `FnOnce`)| 関数型 (`func(int) int`) | 関数型 (`(x: number) => number`) |
| **メソッド参照** | `String::toUpperCase` | なし (直接メソッド名渡し `ToUpper`) | なし (`str::to_uppercase`) | なし | なし |
| **外側変数変更** | ❌ 不可 (final限定) | ⭕ 可能 (ヒープ退避) | ⭕ 可能 (`FnMut` / `mut`) | ⭕ 可能 (ポインタ昇格) | ⭕ 可能 (レキシカル環境) |
| **チェック例外** | ⚠️ SAM宣言外はスロー不可 | なし (全て非チェック) | `Result<T, E>` を返す | `error` を返す | なし |

---

## 11. 理解度チェッククイズ & よくあるエラー虎の巻

### Q1. 次のラムダ式のうち、構文として正しいものはどれですか？（複数選択可）
- A. `x, y -> x + y`
- B. `(x) -> { return x * 2; }`
- C. `() -> { System.out.println("Hi"); }`
- D. `s -> return s.length();`

<details>
<summary>▶ 解答と解説</summary>

**正解: B, C**
- A は引数が複数のため、カッコが必要です: `(x, y) -> x + y`
- D は `{}` なしの単一式において `return` キーワードを書いてはいけません: `s -> s.length()` または `s -> { return s.length(); }`
</details>

### Q2. 次のコードがコンパイルエラーになる理由と修正法は？
```java
int total = 0;
List.of(1, 2, 3).forEach(n -> total += n);
```
<details>
<summary>▶ 解答と解説</summary>

**理由**: `total` はローカル変数であり、ラムダ式内から再代入（変更）することはできません（実質的 final 違反）。
**修正法 (Stream API を活用)**:
```java
int total = List.of(1, 2, 3).stream().mapToInt(Integer::intValue).sum();
```
</details>

### Q3. `String::length` はメソッド参照のどの分類に当てはまりますか？
- A. 静的メソッド参照
- B. 特定オブジェクトのインスタンスメソッド参照
- C. 任意オブジェクトのインスタンスメソッド参照
- D. コンストラクタ参照

<details>
<summary>▶ 解答と解説</summary>

**正解: C**
`length()` は `String` クラスのインスタンスメソッドです。`String::length` はラムダ式で表すと `(String s) -> s.length()` となり、第1引数 `s` がレシーバーとなってメソッドが呼び出されます。
</details>

---

## まとめ：ラムダ式を使いこなすための三原則

1. **「型はSAM（単一抽象メソッド）」**: ラムダ式を見たら、代入先の関数型インターフェース（`Predicate`, `Function`, `Consumer` 等）の抽象メソッドのシグネチャを意識する。
2. **「引数を渡すだけならメソッド参照 `::` を検討」**: コードの可読性と意図の伝わりやすさが格段に向上する。
3. **「副作用を起こさない（イミュータブルな思考）」**: 外側の変数を書き換えようとせず、Stream API や戻り値のパイプラインとして組み立てる。
