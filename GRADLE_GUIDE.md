# 🐘 Gradle 完全攻略マスターガイド (Gradle Guide)

Java プロジェクトにおける標準ビルドツール **Gradle** の基礎概念、`build.gradle` の読み方・書き方、日常的なユースケース別チートシートをまとめた実践的リファレンスです。

---

## 📑 目次
1. [Gradle の全体像と構成要素](#1-gradle-の全体像と構成要素)
2. [`build.gradle` の基本構造（解剖図）](#2-buildgradle-の基本構造解剖図)
3. [依存関係（dependencies）の書き方とスコープ](#3-依存関係dependenciesの書き方とスコープ)
4. [日常的によく書く実用パターン集](#4-日常的によく書く実用パターン集)
5. [よく使う Gradle コマンド集（CLI 逆引き）](#5-よく使う-gradle-コマンド集cli-逆引き)
6. [トラブルシューティング & Tips](#6-トラブルシューティング--tips)

---

## 1. Gradle の全体像と構成要素

Gradle プロジェクトは主に以下の 3 つの要素で構成されています。

```text
.\
├── gradlew / gradlew.bat         # ① Gradle Wrapper (実行スクリプト)
├── gradle/wrapper/               # ① Wrapper の本体 (jar / properties)
├── settings.gradle               # ② プロジェクト全体の定義 (プロジェクト名やマルチモジュール設定)
└── build.gradle                  # ③ ビルドスクリプト本体 (ライブラリ・タスク設定)
```

| 要素 | 人間が自分で書く？ | 役割 |
| :--- | :---: | :--- |
| **`gradlew` (ラッパー)** | **書かない (0%)** | `gradle wrapper` で自動生成。PC に Gradle がなくても自動取得してビルドを保証するスクリプト。 |
| **`settings.gradle`** | **たまに書く (5%)** | プロジェクト名（`rootProject.name`）や、サブプロジェクトの包含を定義。 |
| **`build.gradle`** | **日常的に書く (95%)** | ライブラリの追加、Javaバージョン、コンパイラ設定、実行方法などを記述。 |

> [!NOTE]
> **Groovy DSL vs Kotlin DSL**  
> - `build.gradle` (Groovy): 従来の標準。記述が柔軟でシンプル。現在も広く普及。  
> - `build.gradle.kts` (Kotlin): 近年の推奨。型安全性と IDE（IntelliJ/VS Code）の入力補完が強力。  
> ※ 本ガイドでは本プロジェクトに合わせて **Groovy DSL (`build.gradle`)** をベースに解説します。

---

## 2. `build.gradle` の基本構造（解剖図）

一般的な `build.gradle` の全体像です。必要なブロックを組み合わせて構築します。

```groovy
// 1. プラグインの適用 (プロジェクトに必要な機能を追加)
plugins {
    id 'java'           // Java の標準コンパイル・テスト・JAR作成を有効化
    id 'application'    // main メソッドを持つ実行可能アプリ化
}

// 2. プロジェクトのメタ情報
group = 'com.example'
version = '1.0.0'

// 3. Java バージョン (Java Toolchain)
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21) // 使用する JDK バージョン
    }
}

// 4. ソースコードの配置場所 (デフォルト src/main/java 以外を使う場合のみ記述)
sourceSets {
    main {
        java {
            srcDirs = ['src']
        }
    }
}

// 5. 外部ライブラリの取得元リポジトリ
repositories {
    mavenCentral() // 世界標準の Maven Central リポジトリからダウンロード
}

// 6. 外部ライブラリ (依存関係)
dependencies {
    implementation 'com.google.guava:guava:33.1.0-jre'
    testImplementation 'org.junit.jupiter:junit-jupiter:5.10.2'
}

// 7. コンパイル設定
tasks.withType(JavaCompile).configureEach {
    options.encoding = 'UTF-8'
    options.compilerArgs += ['--enable-preview'] // プレビュー機能有効化
}

// 8. アプリケーション実行設定 (application プラグイン使用時)
application {
    mainClass = 'com.example.Main'
}
```

---

## 3. 依存関係（dependencies）の書き方とスコープ

外部ライブラリを追加する際は、**「どのフェーズで必要になるか（スコープ）」** に応じて書き分けます。

```groovy
dependencies {
    // ① アプリの本番実行とコンパイルの両方で必要 (最も一般的)
    implementation 'org.slf4j:slf4j-api:2.0.12'

    // ② テストコードのコンパイル・実行のみで必要
    testImplementation 'org.junit.jupiter:junit-jupiter:5.10.2'

    // ③ コンパイル時のみ必要 (Lombok や アノテーションプロセッサ等)
    compileOnly 'org.projectlombok:lombok:1.18.32'
    annotationProcessor 'org.projectlombok:lombok:1.18.32'

    // ④ 実行時のみ必要 (ログの実装ライブラリや JDBC ドライバ等)
    runtimeOnly 'ch.qos.logback:logback-classic:1.5.3'
}
```

### 依存関係の記法フォーマット
ライブラリの指定は通常 **`'グループID:アーティファクトID:バージョン'`** の形式（コロン区切り）で指定します。
- [Maven Central (mvnrepository.com)](https://mvnrepository.com/) でライブラリ名を検索すると、コロン区切りのコードがワンクリックでコピーできます。

---

## 4. 日常的によく書く実用パターン集

### パターン①: ライブラリを追加したい（JSON、HTTP等）
```groovy
repositories {
    mavenCentral()
}

dependencies {
    // Jackson (JSON パース用)
    implementation 'com.fasterxml.jackson.core:jackson-databind:2.17.0'
}
```

### パターン②: 最新機能・プレビュー機能を有効にしたい
Java 21/26 などの最新プレビュー機能（Virtual Threads, Panama FFM, Stream Gatherers 等）をフル活用する場合の設定：

```groovy
// コンパイル時のプレビュー有効化
tasks.withType(JavaCompile).configureEach {
    options.encoding = 'UTF-8'
    options.compilerArgs += ['--enable-preview']
}

// テスト実行時のプレビュー有効化
tasks.withType(Test).configureEach {
    jvmArgs += ['--enable-preview']
}

// アプリ実行時 (./gradlew run) のプレビュー & ネイティブアクセス有効化
application {
    mainClass = 'sample.Main'
    applicationDefaultJvmArgs = [
        '--enable-preview',
        '--enable-native-access=ALL-UNNAMED'
    ]
}
```

### パターン③: 全ライブラリ同梱の単一 JAR (Fat JAR / Uber JAR) を作りたい
ライブラリをひとまとめにした「どこでも動く単一JAR」を作りたい場合：

```groovy
// jar タスクをカスタマイズ
tasks.named('jar') {
    manifest {
        attributes 'Main-Class': 'sample.Main'
    }
    // 依存ライブラリのクラスファイルを JAR 内に展開して同梱
    from {
        configurations.runtimeClasspath.collect { it.isDirectory() ? it : zipTree(it) }
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
```

### パターン④: 独自の自作タスク（Custom Task）を追加したい
ビルドの前後に任意のファイルコピーやバッチ処理を行いたい場合：

```groovy
// 例: ビルド成果物の JAR を dist フォルダにコピーするタスク
tasks.register('copyArtifacts', Copy) {
    dependsOn tasks.named('jar')           // jar タスクの完了後に実行
    from layout.buildDirectory.dir('libs') // コピー元
    into layout.projectDirectory.dir('dist') // コピー先
}
```

---

## 5. よく使う Gradle コマンド集（CLI 逆引き）

Windows では `.\gradlew`、Mac/Linux では `./gradlew` を使用します。

| やりたいこと | コマンド | 解説 |
| :--- | :--- | :--- |
| **コンパイル & JAR 作成** | `.\gradlew build` | テストも同時に実行し、`build/libs/` に JAR を出力 |
| **テストを飛ばして高速ビルド**| `.\gradlew build -x test` | テスト失敗でビルドが止まるのを一旦スルーしたい時 |
| **そのままアプリを実行** | `.\gradlew run` | `application` プラグインで定義した `mainClass` を起動 |
| **完全に作り直す (クリーン)** | `.\gradlew clean build` | キャッシュや前回の `build/` を全削除してゼロからビルド |
| **ライブラリ依存関係の確認** | `.\gradlew dependencies` | どのライブラリがどのバージョンで読み込まれているかツリー表示 |
| **利用可能なタスクの一覧** | `.\gradlew tasks` | プロジェクトで実行可能な全コマンド（タスク）を確認 |
| **詳細なエラーログを見る** | `.\gradlew build --stacktrace` | エラー箇所の完全なスタックトレースを表示 |
| **新しいラッパーを生成/更新**| `gradle wrapper` | Gradle のバージョンアップ時やラッパー再生成時 |

---

## 6. トラブルシューティング & Tips

### Q1. ライブラリを追加したのに VS Code / IntelliJ で補完が効かない
- **原因**: IDE がまだ Gradle の変更をリロードしていません。
- **対処**:
  - **VS Code**: `Ctrl + Shift + P` → `Java: Clean Java Language Server Workspace` を実行して再起動。
  - **IntelliJ**: 右側の Gradle パネルから「Reload All Gradle Projects (くるくる矢印)」をクリック。

### Q2. `java.lang.UnsupportedClassVersionError` が出る
- **原因**: ビルドで使用した Java のバージョンと、実行時に使っている `java` コマンドのバージョンが一致していません。
- **対処**: `java -version` を確認するか、`build.gradle` の `java.toolchain` を手元の JDK に合わせます。

### Q3. ビルドがキャッシュのせいで意図通りに動かない
- **対処**: `--no-cache` を付けるか、クリーンビルドを行います。
  ```powershell
  .\gradlew clean build --no-cache
  ```
