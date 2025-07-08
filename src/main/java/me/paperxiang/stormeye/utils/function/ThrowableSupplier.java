package me.paperxiang.stormeye.utils.function;
import java.util.function.Consumer;
public interface ThrowableSupplier<T, E extends Throwable> {
    T get() throws E;
    default T orElse(T fallback) {
        try {
            return get();
        } catch (Throwable e) {
            return fallback;
        }
    }
    default void ifPresent(Consumer<T> action) {
        try {
            action.accept(get());
        } catch (Throwable ignored) {}
    }
    default void ifPresent(T fallback, Consumer<T> action) {
        T value;
        try {
            value = get();
        } catch (Throwable e) {
            value = fallback;
        }
        action.accept(value);
    }
    default void ifPresentThrowable(ThrowableConsumer<T, ?> action) {
        try {
            action.tryAccept(get());
        } catch (Throwable ignored) {}
    }
    default void ifPresentThrowable(T fallback, ThrowableConsumer<T, ?> action) {
        T value;
        try {
            value = get();
        } catch (Throwable e) {
            value = fallback;
        }
        action.tryAccept(value);
    }
    static <T> T supplyOrNull(ThrowableSupplier<T, ?> supplier) {
        return supplier.orElse(null);
    }
    static <T> T supplyOrElse(ThrowableSupplier<T, ?> supplier, T fallback) {
        return supplier.orElse(fallback);
    }
    static <T> void supplyAndRun(ThrowableSupplier<T, ?> supplier, Consumer<T> action) {
        supplier.ifPresent(action);
    }
    static <T> void supplyAndRun(ThrowableSupplier<T, ?> supplier, T fallback, Consumer<T> action) {
        supplier.ifPresent(fallback, action);
    }
    static <T> void supplyAndRunThrowable(ThrowableSupplier<T, ?> supplier, ThrowableConsumer<T, ?> action) {
        supplier.ifPresentThrowable(action);
    }
    static <T> void supplyAndRunThrowable(ThrowableSupplier<T, ?> supplier, T fallback, ThrowableConsumer<T, ?> action) {
        supplier.ifPresentThrowable(fallback, action);
    }
}
