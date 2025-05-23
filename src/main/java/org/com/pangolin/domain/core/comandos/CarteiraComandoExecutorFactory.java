package org.com.pangolin.domain.core.comandos;


public interface CarteiraComandoExecutorFactory {
    <O, I> CarteiraComandoExecutor<O, I> criar(Class<?> commandType);
}
