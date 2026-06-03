package sarisync.services;

import sarisync.enums.PaymentMethod;
import sarisync.interfaces.IPaymentProcessor;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

/**
 * POLYMORPHISM 
 */
public final class PaymentProcessorFactory {

    private static final Map<PaymentMethod, IPaymentProcessor> REGISTRY = new EnumMap<>(PaymentMethod.class);

    static {
        register(new CashPaymentProcessor());
        register(new CardPaymentProcessor());
        register(new GCashPaymentProcessor());
        register(new PayMayaPaymentProcessor());
    }

    private static void register(IPaymentProcessor processor) {
        REGISTRY.put(processor.getPaymentMethod(), processor);
    }

    /**
     * @throws IllegalArgumentException if the method is not registered.
     */
    public static IPaymentProcessor get(PaymentMethod method) {
        return Optional.ofNullable(REGISTRY.get(method))
            .orElseThrow(() -> new IllegalArgumentException(
                "No payment processor registered for: " + method
            ));
    }

    /** Utility class — no instantiation. */
    private PaymentProcessorFactory() {}
}
