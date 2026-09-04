package sample;

import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================================
 * 06: ジェネリクスの深層 (Generics Deep Dive, PECS, Type Erasure)
 * ============================================================================
 * 
 * 【他言語経験者（Rust, C#, Go）向け要点】
 * 1. 型消去 (Type Erasure):
 *    - C#のジェネリクスは実行時にも型情報を保持する「具象化 (Reification)」ですが、
 *      Javaのジェネリクスは後方互換性のためコンパイル時にのみ検査され、
 *      バイトコード上では Object (または境界型) に置き換えられ型情報が「消去」されます。
 *    - そのため `new T()` や `new T[10]`、`instanceof List<String>` はコンパイル不可です。
 * 
 * 2. ジェネリクスの不変性 (Invariance):
 *    - String は Object のサブタイプですが、`List<String>` は `List<Object>` のサブタイプではありません。
 *    - もし代入を許すと `listObject.add(123)` で String リストに整数が混入してしまうためです。
 * 
 * 3. 境界ワイルドカードと PECS 原則 (Producer extends, Consumer super):
 *    - 他言語から移行した開発者が最も混乱する概念。
 *    - Producer-extends: データを「取り出す (Produce)」コレクションには `? extends T` (共変)
 *    - Consumer-super: データを「受け取る (Consume)」コレクションには `? super T` (反変)
 * 
 * 4. 再帰的境界ジェネリクス (Recursive Type Bound):
 *    - `<T extends Comparable<T>>`: 自分自身と比較可能な型のみに制約する（Rust の `T: Ord` 相当）。
 */
public class GenericsDeepDive {

    public static void run() {
        System.out.println("=== 1. Type Erasure & Invariance ===");
        demonstrateTypeErasureAndInvariance();

        System.out.println("\n=== 2. PECS Principle (Producer extends, Consumer super) ===");
        demonstratePecsPrinciple();

        System.out.println("\n=== 3. Recursive Type Bound (Self-Comparable Generics) ===");
        demonstrateRecursiveBound();
    }

    private static void demonstrateTypeErasureAndInvariance() {
        List<String> stringList = new ArrayList<>();
        List<Integer> intList = new ArrayList<>();

        // 型消去の実証: 実行時 (Runtime) にはどちらも同じ java.util.ArrayList
        System.out.println("stringList.getClass() == intList.getClass(): " 
            + (stringList.getClass() == intList.getClass()));
        System.out.println("Runtime Class Name: " + stringList.getClass().getName());

        // 不変性 (Invariance):
        // List<Object> objList = stringList; // コンパイルエラー!
        // 理由: もしこれが許されると、objList.add(100) で stringList に数値が入ってしまう。
        System.out.println("Invariance: List<String> cannot be assigned to List<Object>");
    }

    // --- PECS 原則の実演 ---
    // 基底クラスと派生クラスの定義
    static class Animal {
        private final String name;
        Animal(String name) { this.name = name; }
        public String getName() { return name; }
        @Override
        public String toString() { return "Animal[" + name + "]"; }
    }

    static class Dog extends Animal {
        Dog(String name) { super(name); }
    }

    static class Labrador extends Dog {
        Labrador(String name) { super(name); }
    }

    // Producer extends: リストから Animal を読み取る（取り出す = Produce）
    // List<Dog> や List<Labrador> も渡せる（共変: Covariance）
    private static void inspectAnimals(List<? extends Animal> animals) {
        System.out.print("  [Producer extends] Inspected: ");
        for (Animal a : animals) {
            System.out.print(a.getName() + " ");
            // animals.add(new Dog("Buddy")); // コンパイルエラー! 読み取り専用
        }
        System.out.println();
    }

    // Consumer super: リストへ Dog を書き込む（受け取る = Consume）
    // List<Dog> だけでなく List<Animal> や List<Object> も渡せる（反変: Contravariance）
    private static void addDogs(List<? super Dog> dogConsumer) {
        dogConsumer.add(new Dog("Rex"));
        dogConsumer.add(new Labrador("Max"));
        System.out.println("  [Consumer super] Successfully added Dog and Labrador into consumer list.");
        // Dog d = dogConsumer.get(0); // コンパイルエラー! 読み取れるのは Object のみ
    }

    // 完璧な PECS の実例: コピー関数 (Collections.copy と同一構造)
    // src はデータを供給する Producer (? extends T)
    // dest はデータを受け入れる Consumer (? super T)
    private static <T> void copy(List<? super T> dest, List<? extends T> src) {
        for (T item : src) {
            dest.add(item);
        }
    }

    private static void demonstratePecsPrinciple() {
        List<Dog> dogs = new ArrayList<>(List.of(new Dog("Hachi"), new Dog("Pochi")));
        
        // 1. inspectAnimals は List<Dog> を受け取れる (? extends Animal)
        inspectAnimals(dogs);

        // 2. addDogs は List<Animal> を受け取れる (? super Dog)
        List<Animal> animalSanctuary = new ArrayList<>();
        addDogs(animalSanctuary);
        System.out.println("  Sanctuary count: " + animalSanctuary.size());

        // 3. copy メソッドによる転送 (List<Dog> -> List<Animal>)
        copy(animalSanctuary, dogs);
        System.out.println("  Sanctuary after copy: " + animalSanctuary);
    }

    // --- 再帰的境界ジェネリクス ---
    // 自分自身と比較可能（Comparable<T> を実装している）な型のみを受け取る
    private static <T extends Comparable<T>> T findMax(List<T> list) {
        if (list.isEmpty()) {
            throw new IllegalArgumentException("List must not be empty");
        }
        T max = list.get(0);
        for (T item : list) {
            if (item.compareTo(max) > 0) {
                max = item;
            }
        }
        return max;
    }

    private static void demonstrateRecursiveBound() {
        List<Integer> numbers = List.of(10, 42, 5, 99, 23);
        Integer maxNum = findMax(numbers);
        System.out.println("  findMax([10, 42, 5, 99, 23]) -> " + maxNum);

        List<String> words = List.of("Apple", "Zebra", "Banana");
        String maxWord = findMax(words);
        System.out.println("  findMax([Apple, Zebra, Banana]) -> " + maxWord);
    }
}
