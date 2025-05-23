package org.com.pangolin.domain.core;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class DefaultWalletCommandExecutorFactory implements WalletCommandExecutorFactory {
    private final Map<Class<?>, Supplier<?>> registry = new HashMap<>();


    public <O, I> void register(Class<I> commandType,
                                Supplier<WalletCommandExecutor<O, I>> supplier) {
        registry.put(commandType, supplier);
    }

     /**
     * @param commandType 
     * @param <O>
     * @param <I>
     * @return
     */
    @Override
    @SuppressWarnings("unchecked")
    public <O, I> WalletCommandExecutor<O, I> create(Class<?> commandType) {
        Supplier<WalletCommandExecutor<O, I>> supplier =
                (Supplier<WalletCommandExecutor<O, I>>) registry.get(commandType);

        if (supplier == null) {
            throw new IllegalArgumentException(
                    "No executor registered for command type: " + commandType.getName());
        }

        return supplier.get();
    }
}
