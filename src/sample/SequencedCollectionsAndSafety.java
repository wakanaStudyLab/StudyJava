package sample;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.SequencedCollection;
import java.util.SequencedMap;
import java.util.SequencedSet;

/**
 * ============================================================================
 * 11: Sequenced Collections & 不変・防衛的プログラミング (Java 21+)
 * ============================================================================
 * 
 * 【他言語経験者（Rust, C#, Go）向け要点】
 * 1. Java 21 新機能 Sequenced Collections:
 *    - 従来の Java は「先頭や末尾の要素を取得・操作する方法」が
 *      List (`list.get(0)`), Deque (`deque.getFirst()`), SortedSet (`set.first()`)
 *      とバラバラで、統一されたインターフェースが 25 年間存在しませんでした。
 *    - Java 21 で `SequencedCollection`, `SequencedSet`, `SequencedMap` が導入され、
 *      `getFirst()`, `getLast()`, `reversed()` で完全に統一されました。
 * 
 * 2. 防衛的コピー (Defensive Copy) の罠:
 *    - `Collections.unmodifiableList(list)` は「変更不可ビュー」に過ぎず、
 *      元の `list` が変更されるとビュー側も勝手に変わってしまう重大なバグの温床でした。
 *    - Java 10+ の `List.copyOf(list)` は完全なディープ不変スナップショットを作ります。
 * 
 * 3. Record コンパクトコンストラクタ:
 *    - データを不変に保持するための引数検証（nullチェック、範囲チェック）の標準イディオム。
 */
public class SequencedCollectionsAndSafety {

    // 不変かつ検証済みのドメインモデル
    record BankAccount(String accountId, long balance) {
        // コンパクトコンストラクタ (引数リストなしで事前検証を行う)
        public BankAccount {
            if (accountId == null || accountId.isBlank()) {
                throw new IllegalArgumentException("Account ID must not be blank");
            }
            if (balance < 0) {
                throw new IllegalArgumentException("Balance cannot be negative");
            }
        }
    }

    public static void run() {
        System.out.println("=== 1. Java 21 Sequenced Collections (Unified First/Last/Reversed) ===");
        demonstrateSequencedCollections();

        System.out.println("\n=== 2. Defensive Copying: unmodifiableList vs List.copyOf ===");
        demonstrateDefensiveCopy();

        System.out.println("\n=== 3. Record Compact Constructor Validation ===");
        demonstrateRecordValidation();
    }

    private static void demonstrateSequencedCollections() {
        // 1. SequencedCollection (List)
        SequencedCollection<String> seqList = new ArrayList<>(List.of("First", "Second", "Third"));
        System.out.println("  Original List:       " + seqList);
        System.out.println("  getFirst():          " + seqList.getFirst());
        System.out.println("  getLast():           " + seqList.getLast());

        seqList.addFirst("Zero");
        seqList.addLast("Fourth");
        System.out.println("  After addFirst/Last: " + seqList);

        // reversed() は新しいリストを作らず、O(1) の逆順ビューを提供する
        SequencedCollection<String> reversedView = seqList.reversed();
        System.out.println("  Reversed View:       " + reversedView);

        // 2. SequencedSet (LinkedHashSet)
        SequencedSet<Integer> seqSet = new LinkedHashSet<>(List.of(10, 20, 30));
        System.out.println("  SequencedSet first: " + seqSet.getFirst() + ", last: " + seqSet.getLast());

        // 3. SequencedMap (LinkedHashMap)
        SequencedMap<String, String> seqMap = new LinkedHashMap<>();
        seqMap.put("k1", "val1");
        seqMap.put("k2", "val2");
        System.out.println("  SequencedMap firstEntry: " + seqMap.firstEntry());
        System.out.println("  SequencedMap lastEntry:  " + seqMap.lastEntry());
    }

    private static void demonstrateDefensiveCopy() {
        List<String> mutableSource = new ArrayList<>(List.of("Alpha", "Beta"));

        // パターンA: Collections.unmodifiableList (ビューに過ぎない)
        List<String> unmodView = Collections.unmodifiableList(mutableSource);

        // パターンB: List.copyOf (安全な完全不変スナップショット)
        List<String> safeSnapshot = List.copyOf(mutableSource);

        // 元のリストを変更
        mutableSource.add("Gamma (Injected!)");

        System.out.println("  Mutable source was modified: " + mutableSource);
        System.out.println("  [Danger!] unmodifiableList changed too: " + unmodView);
        System.out.println("  [Safe!]   List.copyOf remained unchanged: " + safeSnapshot);
    }

    private static void demonstrateRecordValidation() {
        // 正常ケース
        BankAccount valid = new BankAccount("ACC-1234", 50000);
        System.out.println("  Valid Account created: " + valid);

        // 不正ケースの検証
        try {
            new BankAccount("", -100);
        } catch (IllegalArgumentException e) {
            System.out.println("  Caught expected validation error: " + e.getMessage());
        }
    }
}
