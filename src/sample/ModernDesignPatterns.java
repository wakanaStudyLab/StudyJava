package sample;

import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;
import java.util.function.UnaryOperator;

/**
 * ============================================================================
 * 12: Modern Java デザインパターン (Modern Design Patterns in Java 21+)
 * ============================================================================
 * 
 * 【他言語経験者（Rust, C#, Go）向け要点】
 * 1. Strategy パターンの関数型簡略化:
 *    - かつての GoF はクラスを大量に作る必要がありましたが、
 *      現代Javaでは関数型インターフェース (UnaryOperator や Function) の代入だけで完了。
 * 
 * 2. State パターン (Sealed Interface + switch 式):
 *    - 状態遷移を `sealed interface` と `record` で表現し、
 *      switch 式で網羅性チェック（exhaustive check）を行いながら遷移させます。
 *    - Rust の `enum State` + `match` と全く同じ堅牢な設計が可能です。
 * 
 * 3. Observer / Reactive Streams (java.util.concurrent.Flow):
 *    - Java 9 で標準化された Reactive Streams 仕様。
 *    - 背圧（Backpressure）制御を備えた非同期メッセージ配信。
 */
public class ModernDesignPatterns {

    // --- 1. Modern Strategy パターン ---
    public static class PriceCalculator {
        // 戦略（Strategy）は単なる関数
        public static double calculate(double basePrice, UnaryOperator<Double> discountStrategy) {
            return discountStrategy.apply(basePrice);
        }
    }

    // --- 2. Modern State パターン (Sealed Interface + ADT) ---
    public sealed interface OrderState permits PendingState, PaidState, ShippedState, CancelledState {}

    public record PendingState(String orderId) implements OrderState {}
    public record PaidState(String orderId, long paidAmount) implements OrderState {}
    public record ShippedState(String orderId, String trackingNumber) implements OrderState {}
    public record CancelledState(String orderId, String reason) implements OrderState {}

    public static OrderState transition(OrderState current) {
        // switch 式のパターンマッチングによる状態遷移 (網羅性がコンパイル時に検証される)
        return switch (current) {
            case PendingState p   -> new PaidState(p.orderId(), 5000);
            case PaidState paid   -> new ShippedState(paid.orderId(), "TRACK-998877");
            case ShippedState s   -> s; // 最終状態
            case CancelledState c -> c; // 最終状態
        };
    }

    public static void run() {
        System.out.println("=== 1. Modern Strategy Pattern (Zero-Boilerplate Lambdas) ===");
        demonstrateStrategy();

        System.out.println("\n=== 2. Modern State Pattern (Sealed Interface & Exhaustive Switch) ===");
        demonstrateStatePattern();

        System.out.println("\n=== 3. Modern Observer Pattern (java.util.concurrent.Flow) ===");
        demonstrateFlowApi();
    }

    private static void demonstrateStrategy() {
        double originalPrice = 10000.0;

        // 戦略をラムダ式で直接切り替え
        double regularPrice = PriceCalculator.calculate(originalPrice, p -> p);
        double blackFriday = PriceCalculator.calculate(originalPrice, p -> p * 0.7); // 30% OFF
        double vipCoupon = PriceCalculator.calculate(originalPrice, p -> p - 2000.0); // 2000 JPY OFF

        System.out.println("  Original:     " + originalPrice + " JPY");
        System.out.println("  Regular:      " + regularPrice + " JPY");
        System.out.println("  Black Friday: " + blackFriday + " JPY");
        System.out.println("  VIP Coupon:   " + vipCoupon + " JPY");
    }

    private static void demonstrateStatePattern() {
        OrderState state = new PendingState("ORD-001");
        System.out.println("  Initial state: " + state);

        state = transition(state);
        System.out.println("  After 1st transition: " + state);

        state = transition(state);
        System.out.println("  After 2nd transition: " + state);
    }

    private static void demonstrateFlowApi() {
        // 標準の SubmissionPublisher (Publisher 実装)
        try (SubmissionPublisher<String> publisher = new SubmissionPublisher<>()) {

            // Subscriber の登録
            publisher.subscribe(new Flow.Subscriber<>() {
                private Flow.Subscription subscription;

                @Override
                public void onSubscribe(Flow.Subscription subscription) {
                    this.subscription = subscription;
                    this.subscription.request(2); // 2件リクエスト (背圧: Backpressure)
                }

                @Override
                public void onNext(String item) {
                    System.out.println("    [Flow Subscriber] Received event: " + item);
                    this.subscription.request(1); // 次の1件を要求
                }

                @Override
                public void onError(Throwable throwable) {
                    System.err.println("    [Flow Subscriber] Error: " + throwable.getMessage());
                }

                @Override
                public void onComplete() {
                    System.out.println("    [Flow Subscriber] All events consumed successfully!");
                }
            });

            // メッセージの発行
            publisher.submit("EVENT_USER_SIGNUP");
            publisher.submit("EVENT_EMAIL_VERIFIED");
            publisher.submit("EVENT_FIRST_PURCHASE");

            // 非同期配信のため少し待機
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
