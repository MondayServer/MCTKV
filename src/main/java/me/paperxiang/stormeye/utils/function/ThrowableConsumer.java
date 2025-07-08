package me.paperxiang.stormeye.utils.function;
import java.util.Objects;
public interface ThrowableConsumer<T, E extends Throwable> {
    void accept(T t) throws E;
    default void tryAccept(T t) {
        try {
            accept(t);
        } catch (Throwable ignored) {}
    }
    default <F extends Throwable> ThrowableConsumer<T, F> andThen(ThrowableConsumer<? super T, F> after) {
        Objects.requireNonNull(after);
        return (T t) -> {
            try {
                accept(t);
                after.accept(t);
            } catch (Throwable ignored) {}
        };
    }
    static <T> void accept(T value, ThrowableConsumer<T, ?> consumer) {
        consumer.tryAccept(value);
    }
}
