package org.com.pangolin.domain.core.comandos;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class DefaultCarteiraComandoExecutorrFactory implements CarteiraComandoExecutorFactory {

    private final Map<Class<?>, Supplier<?>> registrar = new HashMap<>();
    public <O, I> void registrar(Class<I> commandType,
                                Supplier<CarteiraComandoExecutor<O, I>> supplier) {
        registrar.put(commandType, supplier);
    }

     /**
     * @param commandType 
     * @param <O>
     * @param <I>
     * @return
     */
    @Override
    @SuppressWarnings("unchecked")
    public <O, I> CarteiraComandoExecutor<O, I> criar(Class<?> commandType) {
        Supplier<CarteiraComandoExecutor<O, I>> supplier =
                (Supplier<CarteiraComandoExecutor<O, I>>) registrar.get(commandType);
        if (supplier == null) {
            throw new IllegalArgumentException(
                    "No executor registered for command type: " + commandType.getName());
        }
        return supplier.get();
    }
}
