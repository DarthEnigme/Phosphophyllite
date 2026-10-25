package net.roguelogix.phosphophyllite.util;

/**
 * Replacement for cpw.mods.modlauncher.api.LamdbaExceptionUtils, which was removed from modlauncher.
 * Name kept identical so existing usages only need an import change.
 */
public final class LamdbaExceptionUtils {
    private LamdbaExceptionUtils() {
    }
    
    @FunctionalInterface
    public interface BiConsumer_WithExceptions<T, U, E extends Exception> {
        void accept(T t, U u) throws E;
    }
    
    @FunctionalInterface
    public interface Supplier_WithExceptions<T, E extends Exception> {
        T get() throws E;
    }
}
