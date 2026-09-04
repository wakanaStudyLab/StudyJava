package sample;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * ============================================================================
 * 09: リフレクション・アノテーション・動的プロキシ (Reflection & Dynamic Proxies)
 * ============================================================================
 * 
 * 【他言語経験者（Rust, C#, Go）向け要点】
 * 1. Java フレームワークの魔術（Spring, Hibernate, Quarkus 等）の正体:
 *    - Java エンタープライズ開発で日常的に使われる `@Autowired`, `@Transactional` 等の正体は
 *      「リフレクション (Reflection) + カスタムアノテーション + 動的プロキシ (Dynamic Proxy)」です。
 * 
 * 2. カスタムアノテーション:
 *    - `@Retention(RetentionPolicy.RUNTIME)` を指定することで、
 *      コンパイル後もクラスファイルにメタデータが残り、実行時にリフレクションで読み取れます。
 * 
 * 3. java.lang.reflect.Proxy:
 *    - インターフェースのメソッド呼び出しを横取り (Intercept) し、
 *      前後に共通処理（ログ出力、トランザクション開始・コミット、メトリクス計測）を挟み込む仕組み。
 *    - AOP (アスペクト指向プログラミング) の中核技術です。
 */
public class ReflectionAndProxies {

    // --- 1. カスタムアノテーションの定義 ---
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface AuditLog {
        String action() default "DEFAULT_ACTION";
    }

    // --- 2. ビジネスロジックのインターフェースと実装 ---
    public interface PaymentService {
        void pay(String accountId, int amount);
        String queryBalance(String accountId);
    }

    public static class RealPaymentService implements PaymentService {
        @Override
        @AuditLog(action = "ACCOUNT_PAYMENT")
        public void pay(String accountId, int amount) {
            System.out.println("    [RealPaymentService] Executed payment: " + amount + " JPY for " + accountId);
        }

        @Override
        public String queryBalance(String accountId) {
            return "Balance for " + accountId + ": 500,000 JPY";
        }
    }

    // --- 3. 動的プロキシ (InvocationHandler: AOP インターセプター) ---
    static class AuditInvocationHandler implements InvocationHandler {
        private final Object target;

        public AuditInvocationHandler(Object target) {
            this.target = target;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            // 対象メソッドに @AuditLog アノテーションが付いているか検査
            // (インターフェースのメソッドではなく、実装クラスの対応メソッドから取得)
            Method targetMethod = target.getClass().getMethod(method.getName(), method.getParameterTypes());
            AuditLog audit = targetMethod.getAnnotation(AuditLog.class);

            if (audit != null) {
                System.out.println("  [AOP Interceptor >>> BEFORE] Auditing action: '" + audit.action() + "' on method: " + method.getName());
            }

            long start = System.nanoTime();
            // 本物のオブジェクトのメソッドを実行 (Reflection invoke)
            Object result = method.invoke(target, args);
            long elapsed = System.nanoTime() - start;

            if (audit != null) {
                System.out.println("  [AOP Interceptor <<< AFTER] Action completed successfully in " + elapsed + " ns.");
            }

            return result;
        }
    }

    public static void run() {
        System.out.println("=== 1. Inspecting Class & Annotations via Reflection ===");
        inspectService();

        System.out.println("\n=== 2. Dynamic Proxy in Action (Spring AOP Simulation) ===");
        demonstrateDynamicProxy();
    }

    private static void inspectService() {
        Class<?> clazz = RealPaymentService.class;
        System.out.println("  Inspecting class: " + clazz.getSimpleName());

        for (Method m : clazz.getDeclaredMethods()) {
            System.out.print("  - Method: " + m.getName());
            if (m.isAnnotationPresent(AuditLog.class)) {
                AuditLog log = m.getAnnotation(AuditLog.class);
                System.out.print(" [@AuditLog(action='" + log.action() + "')]");
            }
            System.out.println();
        }
    }

    private static void demonstrateDynamicProxy() {
        // 1. 本物のサービスインスタンス
        PaymentService realService = new RealPaymentService();

        // 2. 動的プロキシの生成 (JDK 標準の Proxy クラス)
        PaymentService proxyService = (PaymentService) Proxy.newProxyInstance(
            PaymentService.class.getClassLoader(),
            new Class<?>[]{PaymentService.class},
            new AuditInvocationHandler(realService)
        );

        // 3. クライアントコードは通常のインターフェースとして呼び出すだけ！
        System.out.println("  Calling proxy.pay() (Has @AuditLog):");
        proxyService.pay("ACC-7788", 12000);

        System.out.println("\n  Calling proxy.queryBalance() (No @AuditLog):");
        String balance = proxyService.queryBalance("ACC-7788");
        System.out.println("    Result: " + balance);
    }
}
