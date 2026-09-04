package sample;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.util.Arrays;

/**
 * ============================================================================
 * 13: 外部関数 & メモリ API (Foreign Function & Memory API - Project Panama)
 * ============================================================================
 * 
 * 【他言語経験者（Rust, C#, Go）向け要点】
 * 1. JNI (Java Native Interface) の終焉と FFM API (Java 22+ 正式標準化):
 *    - 従来、JavaからC/C++ライブラリを呼ぶには煩雑な C++ ラッパーコードと `javah` ヘッダー生成、
 *      動的ライブラリコンパイルが必要で、極めて脆弱かつ低速でした。
 *    - FFM API (`java.lang.foreign.*`) により、**純粋なJavaコードだけで、Cライブラリや
 *      オフヒープメモリ（Direct Memory）へ安全かつゼロコピーでアクセス可能**になりました。
 * 
 * 2. 他言語との対比表:
 *    | 概念 | Modern Java (FFM API) | C# | Rust | Go |
 *    | :--- | :--- | :--- | :--- | :--- |
 *    | オフヒープメモリ | `Arena` / `MemorySegment` | `Marshal.AllocHGlobal` / `Span<T>` | `Box::into_raw` / `std::alloc` | `C.malloc` |
 *    | メモリ解放保証 | `Arena` (try-with-resources / RAII) | `IDisposable` / `using` | `Drop` トレイト (RAII) | `defer C.free(...)` |
 *    | ネイティブ関数呼出 | `Linker` + `downcallHandle` | `[DllImport]` / P/Invoke | `extern "C"` (FFI) | `cgo` |
 *    | 構造体レイアウト | `MemoryLayout.structLayout(...)` | `[StructLayout]` | `#[repr(C)] struct` | `C.struct_...` |
 * 
 * 3. Arena (アリーナ) の3大ライフサイクル管理:
 *    - `Arena.ofConfined()`: 単一スレッド専用。最速。`try-with-resources` を抜けると即座にメモリ解放。
 *    - `Arena.ofShared()`: 複数スレッド間で共有可能。スレッドセーフだがアトミックな管理コストがある。
 *    - `Arena.ofAuto()`: JVM の ガベージコレクタ (GC) が参照不能になった時点で自動解放。
 */
public class ForeignFunctionAndMemoryAPI {

    public static void run() {
        System.out.println("=== 1. Safe Off-Heap Memory Allocation with Arena & MemorySegment ===");
        demonstrateOffHeapMemory();

        System.out.println("\n=== 2. Struct MemoryLayout (C-Style Struct in Java) ===");
        demonstrateStructLayout();

        System.out.println("\n=== 3. Downcall: Invoking C Standard Library (strlen) ===");
        demonstrateNativeFunctionCall();
    }

    /**
     * オフヒープメモリの安全な割り当てとスライス操作
     */
    private static void demonstrateOffHeapMemory() {
        // Arena.ofConfined(): try-with-resources を抜けると、管理下の全オフヒープメモリが即座にOSへ返還される
        // GCに頼らないため、巨大データ処理でもヒープ圧迫・GCポーズがゼロ！
        try (Arena arena = Arena.ofConfined()) {
            // 1. オフヒープに 100 個の 32-bit 整数 (400 bytes) を割り当て
            long elementCount = 5;
            MemorySegment intSegment = arena.allocate(ValueLayout.JAVA_INT, elementCount);

            // 2. 配列のようにインデックス指定で値を直接書き込み
            for (long i = 0; i < elementCount; i++) {
                intSegment.setAtIndex(ValueLayout.JAVA_INT, i, (int) (i + 1) * 10);
            }

            // 3. 値の読み出し
            int[] values = new int[(int) elementCount];
            for (long i = 0; i < elementCount; i++) {
                values[(int) i] = intSegment.getAtIndex(ValueLayout.JAVA_INT, i);
            }
            System.out.println("  Allocated int elements in off-heap: " + Arrays.toString(values));
            System.out.println("  Total segment byte size: " + intSegment.byteSize() + " bytes");

            // 4. ネイティブ UTF-8 文字列のゼロコピー割り当て
            MemorySegment strSegment = arena.allocateFrom("Hello Project Panama from Off-Heap!");
            System.out.println("  Off-Heap String: " + strSegment.getString(0));

            // 5. メモリスライス（C#の Span<T> や Rust のスライス &[T] と同等）
            MemorySegment sliced = intSegment.asSlice(ValueLayout.JAVA_INT.byteSize() * 2, ValueLayout.JAVA_INT.byteSize() * 2);
            System.out.println("  Sliced values (index 2 & 3): [" 
                + sliced.getAtIndex(ValueLayout.JAVA_INT, 0) + ", " 
                + sliced.getAtIndex(ValueLayout.JAVA_INT, 1) + "]");

        } // <-- ここで arena がクローズされ、OSメモリが即座に解放される (Use-After-Free は例外で防止される)
        System.out.println("  Arena closed: Off-heap memory safely reclaimed by OS.");
    }

    /**
     * C言語の構造体 (struct) を Java 側でメモリレイアウトとして定義し操作
     * 
     * struct Point3D {
     *     int id;       // 4 bytes
     *     // padding    // 4 bytes (8バイトアライメント)
     *     double x;     // 8 bytes
     *     double y;     // 8 bytes
     *     double z;     // 8 bytes
     * };
     */
    private static void demonstrateStructLayout() {
        // C言語の struct Point3D と完全に互換性のあるメモリレイアウトを定義
        StructLayout point3DLayout = MemoryLayout.structLayout(
            ValueLayout.JAVA_INT.withName("id"),
            MemoryLayout.paddingLayout(4), // 8バイトアライメント用パディング
            ValueLayout.JAVA_DOUBLE.withName("x"),
            ValueLayout.JAVA_DOUBLE.withName("y"),
            ValueLayout.JAVA_DOUBLE.withName("z")
        );

        // 各フィールドへのオフセットハンドル（VarHandle）を型安全に取得
        var idHandle = point3DLayout.varHandle(MemoryLayout.PathElement.groupElement("id"));
        var xHandle  = point3DLayout.varHandle(MemoryLayout.PathElement.groupElement("x"));
        var yHandle  = point3DLayout.varHandle(MemoryLayout.PathElement.groupElement("y"));
        var zHandle  = point3DLayout.varHandle(MemoryLayout.PathElement.groupElement("z"));

        try (Arena arena = Arena.ofConfined()) {
            MemorySegment pointSegment = arena.allocate(point3DLayout);

            // オフヒープ構造体へフィールド値をセット
            idHandle.set(pointSegment, 0L, 101);
            xHandle.set(pointSegment, 0L, 12.5);
            yHandle.set(pointSegment, 0L, 45.0);
            zHandle.set(pointSegment, 0L, 99.8);

            // 読み出し
            int id = (int) idHandle.get(pointSegment, 0L);
            double x = (double) xHandle.get(pointSegment, 0L);
            double y = (double) yHandle.get(pointSegment, 0L);
            double z = (double) zHandle.get(pointSegment, 0L);

            System.out.printf("  Point3D Struct in Off-Heap: ID=%d, X=%.1f, Y=%.1f, Z=%.1f (Total: %d bytes)%n",
                id, x, y, z, point3DLayout.byteSize());
        }
    }

    /**
     * C標準ライブラリ（libc / msvcrt）の native 関数を Java から直接ダウンコール
     */
    private static void demonstrateNativeFunctionCall() {
        Linker linker = Linker.nativeLinker();
        SymbolLookup stdlibLookup = linker.defaultLookup();

        // C言語の 'size_t strlen(const char *s);' を検索
        var strlenSymbol = stdlibLookup.find("strlen");

        if (strlenSymbol.isEmpty()) {
            System.out.println("  [Notice] 'strlen' function symbol not found in default platform lookup.");
            return;
        }

        // Cの関数シグネチャ（記述子）を定義:
        // 戻り値: size_t (64-bit 環境では JAVA_LONG)
        // 引数: const char* (ValueLayout.ADDRESS)
        FunctionDescriptor strlenDescriptor = FunctionDescriptor.of(
            ValueLayout.JAVA_LONG,
            ValueLayout.ADDRESS
        );

        // 高速なダウンコールハンドルを生成
        MethodHandle strlenHandle = linker.downcallHandle(strlenSymbol.get(), strlenDescriptor);

        try (Arena arena = Arena.ofConfined()) {
            String testString = "Project Panama makes Java blazing fast!";
            // C互換のヌル終端 UTF-8 文字列をオフヒープに作成
            MemorySegment nativeString = arena.allocateFrom(testString);

            // Cネイティブ関数を実行
            long nativeCalculatedLength = (long) strlenHandle.invokeExact(nativeString);

            System.out.println("  Java String:      \"" + testString + "\"");
            System.out.println("  Java length():    " + testString.length());
            System.out.println("  C strlen() Call:  " + nativeCalculatedLength);
            System.out.println("  Verification:     " + (testString.length() == nativeCalculatedLength ? "MATCH! (Zero overhead FFI)" : "FAIL"));
        } catch (Throwable t) {
            System.err.println("  Error during native downcall: " + t.getMessage());
        }
    }
}
