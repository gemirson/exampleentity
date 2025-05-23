package org.com.pangolin.domain.core;

public interface WalletCommandExecutorFactory {
    <O, I> WalletCommandExecutor<O, I> create(Class<?> commandType);
}
