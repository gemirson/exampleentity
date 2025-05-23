package org.com.pangolin.domain.core.constantes;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class AppConstants {
    // Construtor privado para evitar instanciação
    private AppConstants() {
        throw new AssertionError("Cannot instantiate constant class");
    }

    // Interface base para todas as constantes
    public interface Constant {
        String code();
        String description();
    }

    // Classe base abstrata para implementação de constantes
    public abstract static class AbstractConstant implements Constant {
        private final String code;
        private final String description;

        protected AbstractConstant(String code, String description) {
            this.code = code;
            this.description = description;
        }

        @Override
        public String code() {
            return code;
        }

        @Override
        public String description() {
            return description;
        }

        @Override
        public final boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof AbstractConstant)) return false;
            AbstractConstant other = (AbstractConstant) obj;
            return code.equals(other.code) && getClass().equals(other.getClass());
        }

        @Override
        public final int hashCode() {
            return code.hashCode();
        }

        @Override
        public String toString() {
            return code + " - " + description;
        }
    }

    // ========== Categorias de Constantes ==========

    // Categoria: Status de Pedido
    public static final class OrderStatus extends AbstractConstant {
        private static final Map<String, OrderStatus> CACHE = new HashMap<>();

        // Constantes públicas
        public static final OrderStatus PENDING = new OrderStatus("PEND", "Pedido pendente");
        public static final OrderStatus PROCESSING = new OrderStatus("PROC", "Pedido em processamento");
        public static final OrderStatus SHIPPED = new OrderStatus("SHIP", "Pedido enviado");
        public static final OrderStatus DELIVERED = new OrderStatus("DELV", "Pedido entregue");
        public static final OrderStatus CANCELLED = new OrderStatus("CANC", "Pedido cancelado");

        private OrderStatus(String code, String description) {
            super(code, description);
            CACHE.put(code, this);
        }

        public static OrderStatus getByCode(String code) {
            return CACHE.get(code);
        }

        public static Map<String, OrderStatus> getAll() {
            return Collections.unmodifiableMap(CACHE);
        }
    }

    // Categoria: Tipos de Pagamento
    public static final class PaymentType extends AbstractConstant {
        private static final Map<String, PaymentType> CACHE = new HashMap<>();

        // Constantes públicas
        public static final PaymentType CREDIT_CARD = new PaymentType("CC", "Cartão de Crédito");
        public static final PaymentType DEBIT_CARD = new PaymentType("DC", "Cartão de Débito");
        public static final PaymentType PIX = new PaymentType("PIX", "Pagamento via PIX");
        public static final PaymentType BOLETO = new PaymentType("BOL", "Boleto Bancário");

        private PaymentType(String code, String description) {
            super(code, description);
            CACHE.put(code, this);
        }

        public static PaymentType getByCode(String code) {
            return CACHE.get(code);
        }

        public static Map<String, PaymentType> getAll() {
            return Collections.unmodifiableMap(CACHE);
        }
    }

    // Categoria: Estados Brasileiros
    public static final class BrazilianState extends AbstractConstant {
        private static final Map<String, BrazilianState> CACHE = new HashMap<>();

        // Constantes públicas
        public static final BrazilianState SP = new BrazilianState("SP", "São Paulo");
        public static final BrazilianState RJ = new BrazilianState("RJ", "Rio de Janeiro");
        public static final BrazilianState MG = new BrazilianState("MG", "Minas Gerais");
        public static final BrazilianState RS = new BrazilianState("RS", "Rio Grande do Sul");

        private BrazilianState(String code, String description) {
            super(code, description);
            CACHE.put(code, this);
        }

        public static BrazilianState getByCode(String code) {
            return CACHE.get(code);
        }

        public static Map<String, BrazilianState> getAll() {
            return Collections.unmodifiableMap(CACHE);
        }
    }

    // Método utilitário genérico para busca por código
    public static <T extends Constant> T getByCode(Class<T> constantClass, String code) {
        if (OrderStatus.class.equals(constantClass)) {
            return constantClass.cast(OrderStatus.getByCode(code));
        }
        if (PaymentType.class.equals(constantClass)) {
            return constantClass.cast(PaymentType.getByCode(code));
        }
        if (BrazilianState.class.equals(constantClass)) {
            return constantClass.cast(BrazilianState.getByCode(code));
        }
        throw new IllegalArgumentException("Unsupported constant class: " + constantClass);
    }
}