package org.com.pangolin.domain.core;

import java.util.List;

public interface RecordValidado {

    static <T> void validar(T value, Validator<T> validator) {
        try {
            validator.validar(value);
        } catch (Validator.ValidacaoException e) {
            throw new IllegalArgumentException(
                    "Falha na validação do record: " + e.getMessage(), e);
        }
    }

    static <T> List<T> validarLista(List<T> lista, Validator<List<T>> validator) {
        validar(lista, validator);
        return List.copyOf(lista); // Retorna lista imutável
    }

    // Método especial para composição String
    static void validarString(String value,
                                        Validator<? super String>... validators) {
        Validator<String> combined = Validator.of(v -> true, "");
        for (Validator<? super String> v : validators) {
            combined = combined.and(v);
        }
        validar(value, combined);
    }
}