package org.com.pangolin.carteira.core.comandos;


public interface CarteiraComandoExecutorFactory {
    <O, I> CarteiraComandoExecutor<O, I> criar(Class<?> commandType);
}
