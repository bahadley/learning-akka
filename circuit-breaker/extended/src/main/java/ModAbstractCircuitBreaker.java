package basic;

import akka.util.Unsafe;

class ModAbstractCircuitBreaker {
    protected final static long stateOffset;
    protected final static long resetTimeoutOffset;

    static {
        try {
            stateOffset = Unsafe.instance.objectFieldOffset(ModCircuitBreaker.class.getDeclaredField("_currentStateDoNotCallMeDirectly"));
            resetTimeoutOffset = Unsafe.instance.objectFieldOffset(ModCircuitBreaker.class.getDeclaredField("_currentResetTimeoutDoNotCallMeDirectly"));
        } catch(Throwable t){
            throw new ExceptionInInitializerError(t);
        }
    }
}
