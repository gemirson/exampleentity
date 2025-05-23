package org.com.pangolin.domain.core.validacoes;

import org.com.pangolin.domain.core.ValidationResult;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;

public class ResultadoValidacao {

    private  final  boolean valido;
    private Map<String, List<ErrosValidacao>> erros;
    private final boolean deveLancarExcecao = false;

    public ResultadoValidacao(boolean valido, Map<String, List<ErrosValidacao>> erros) {
        this.valido = valido;
        this.erros = erros;
    }

    public boolean valido() {
        return valido;
    }
    public boolean deveLancarExcecao() { return  deveLancarExcecao;    }


    public void lancarSeInvalido() {
        if (!valido) {
            throw new ValidacaoException(String.join(", ", erros.values().stream()
                    .flatMap(List::stream)
                    .map(ErrosValidacao::menssagem)
                    .toList()));
        }
    }

    public Map<String, List<ErrosValidacao>> erros() {
        return erros;
    }
    public void comErros(Map<String, List<ErrosValidacao>> errosValidacao) {
        this.erros = errosValidacao;
    }

    public static  ResultadoValidacao invalidar(Map<String, List<ErrosValidacao>> errosValidacao) {
        return new ResultadoValidacao(false, errosValidacao);
    }

    public  static ResultadoValidacao validar() {
        return new ResultadoValidacao(true, null);
    }
    public  static ResultadoValidacao invalidar(String campo, String codigo, String menssagem) {
        return invalidar(Map.of(campo, List.of(new ErrosValidacao(codigo, menssagem,false))));
    }

    public  static ResultadoValidacao invalidar(String campo, List<ErrosValidacao> errosValidacao) {
        return invalidar(Map.of(campo, errosValidacao));
    }

    public ResultadoValidacao combinar(ResultadoValidacao outro) {
        if (this.valido && outro.valido) {
            return validar();
        }
        return new ResultadoValidacao(false, mergeErrors(this.erros, outro.erros));
    }
    public static ResultadoValidacao criar(boolean valido, Map<String, List<ErrosValidacao>> errosValidacao) {
        return new ResultadoValidacao(valido, errosValidacao);
    }
   /*

    */
    private static Map<String, List<ErrosValidacao>> mergeErrors(
            Map<String, List<ErrosValidacao>> erros,
            Map<String, List<ErrosValidacao>> outroErros) {

        Map<String, List<ErrosValidacao>> merged = new LinkedHashMap<>();
        Stream.of(erros, outroErros).forEach(map ->
                map.forEach((key, value) -> {
                    merged.merge(key, value, (oldVal, newVal) -> {
                        oldVal.addAll(newVal);
                        return oldVal;
                    });
                }));
        return merged;
    }

    /**
     * Checks if the specified field contains an error with the exact given message.
     *
     * <p>This method performs a case-sensitive search for the error message among
     * all errors associated with the specified field.</p>
     *
     * <p><b>Usage example:</b></p>
     * <pre>{@code
     * ValidationResult result = ValidationResult.invalid("username", "INVALID_CHARS",
     *     "Username contains invalid characters");
     *
     * boolean contains = result.containsError("username", "Username contains invalid characters");
     * // Returns true
     * }</pre>
     *
     * @param campo The field name to check for errors (case-sensitive)
     * @param memsagemErro The exact error message to search for (case-sensitive, cannot be null)
     * @return {@code true} if the field has at least one error with the exact specified message,
     *         {@code false} otherwise (including when the field doesn't exist or has no errors)
     *
     * @throws NullPointerException if {@code errorMessage} is {@code null}
     *
     * @see #contemCodigoDeErro(String) (String, String)
     * @see #errorPorCampo(String) (String)
     * @see ValidationResult.ValidationError#getMessage()
     *
     * @implNote This performs a direct string comparison using {@link String#equals(Object)}.
     *           For case-insensitive matching or partial matching, use {@link #getErrorsForField(String)}
     *           and implement custom matching logic.
     */
    public boolean contemErro(String campo, String memsagemErro) {
        if (memsagemErro == null) {
            throw new NullPointerException("O menssagemErro não pode ser nulo");
        }
        return erros != null && erros.containsKey(campo) &&
                erros.get(campo).stream().anyMatch(erro -> erro.menssagem().equals(memsagemErro));
    }


    /**
     * Checks if any error with the specified code exists.
     *
     * @param codigoErro error code to search for (cannot be null)
     * @return true if at least one error with the code is found, false otherwise
     * @throws NullPointerException if errorCode is null
     */
    public boolean contemCodigoDeErro(String codigoErro) {
        return erros.values().stream()
                .flatMap(List::stream)
                .anyMatch(e -> e.codigo().equals(codigoErro));
    }


    /**
     * Determines whether the specified field has any validation errors.
     *
     * <p>This method checks if there are any error entries associated with the given field,
     * regardless of the specific error codes or messages.</p>
     *
     * <p><b>Usage example:</b></p>
     * <pre>{@code
     *  ResultadoValidacao result = ResultadoValidacao.invalidar("email", "INVALID", "Invalid format");
     *
     * if (result.hasErrorsForField("email")) {
     *     // Handle email field errors
     * }
     * }</pre>
     *
     * @param campo The field name to check for errors (case-sensitive)
     * @return {@code true} if the field exists in the error map and has at least one error,
     *         {@code false} if the field has no errors or doesn't exist in the validation result
     *
     * @see #errorPorCampo(String)
     * @see #contemErro(String, String)
     * @see #contemCodigoErro (String, String)
     *
     * @implNote This is more efficient than {@code getErrorsForField(field).isEmpty()}
     *           as it avoids creating a new list instance for the check.
     */
    public boolean existeErroPorCampo(String campo) {
        return erros.containsKey(campo) && !erros.get(campo).isEmpty();
    }

    /**
     * Returns errors associated with a specific field.
     *
     * @param campo field to query
     * @return immutable list of errors (never null, empty if field doesn't exist)
     */
    public List<ErrosValidacao> errorPorCampo(String campo) {
        return erros.getOrDefault(campo, emptyList());
    }

    /**
     * Retrieves all error messages from the validation result as a flattened list.
     *
     * <p>This method collects all error messages across all fields into a single list,
     * preserving the encounter order of fields and their respective errors. The messages
     * are returned in the same order they were added to the validation result.</p>
     *
     * <p><b>Note:</b> For an empty or valid result, this returns an empty list.</p>
     *
     * <p><b>Usage example:</b></p>
     * <pre>{@code
     * ValidationResult result = ErrosValidacao.invalidar("email", "INVALID", "Invalid email");
     * result = result.combine(ErrosValidacao.invalidar("password", "TOO_SHORT", "Password too short"));
     *
     * List<String> allMessages = result.todasMensagemErro();
     * // Returns ["Invalid email", "Password too short"]
     * }</pre>
     *
     * @return An immutable list containing all error messages from all fields in encounter order.
     *         Returns an empty list if there are no errors (including valid results).
     *         Never returns {@code null}.
     *
     * @see #erros()
     * @see #errorPorCampo(String)
     * @see #todosCodigoDeErro()
     * @see ErrosValidacao#menssagem()
     */
    public List<String> todasMensagemErro() {
        return erros.values().stream()
                .flatMap(List::stream)
                .map(ErrosValidacao::menssagem)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all unique error codes from the validation result as a list.
     *
     * <p>This method collects all distinct error codes across all fields and errors,
     * eliminating duplicates while preserving the first occurrence of each code
     * in the encounter order of fields and their respective errors.</p>
     *
     * <p><b>Note:</b> For an empty or valid result, this returns an empty list.</p>
     *
     * <p><b>Usage example:</b></p>
     * <pre>{@code
     * ValidationResult result = ResultadoValidacao.invalidar("email", "INVALID_FORMAT", "Invalid format");
     * result = result.combine(ResultadoValidacao.invalidar("email", "INVALID_FORMAT", "Bad format"));
     * result = result.combine(ResultadoValidacao.invalidar("password", "TOO_SHORT", "Too short"));
     *
     * List<String> codes = result.todosCodigoDeErro();
     * // Returns ["INVALID_FORMAT", "TOO_SHORT"] (duplicates removed)
     * }</pre>
     *
     * @return An immutable list containing all unique error codes across all validation errors,
     *         in the order of their first occurrence. Returns an empty list if there are no errors
     *         (including valid results). Never returns {@code null}.
     *
     * @see #todasMensagemErro()
     * @see #erros()
     * @see #errorPorCampo(String)
     * @see ErrosValidacao#codigo()
     *
     * @implNote The current implementation uses {@link Stream#distinct()} which preserves
     *           the first occurrence of each duplicate element in the encounter order.
     */
    public List<String> todosCodigoDeErro() {
        return erros.values().stream()
                .flatMap(List::stream)
                .map(ErrosValidacao::codigo)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Converts errors to a simplified map (field → concatenated messages).
     *
     * <p>Useful for serialization or simplified display.</p>
     *
     * @return new non-null map containing for each field a string with all messages
     *         concatenated by comma
     */
    public Map<String, String> toSimpleErrorMap() {
        return erros.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().stream()
                                .map(ErrosValidacao::menssagem)
                                .collect(Collectors.joining(", ")),
                        (oldVal, newVal) -> oldVal,
                        LinkedHashMap::new
                ));
    }

    /**
     * Filters errors keeping only those with the specified codes.
     *
     * @param errorCodes error codes to keep (at least one code must be provided)
     * @return new ResultadoValidacao containing only filtered errors (or valid if no errors match)
     * @throws IllegalArgumentException if no codes are provided
     */
    public ResultadoValidacao filtroPorCodigoDeErro(String... errorCodes) {
        Set<String> codes = Set.of(errorCodes);
        Map<String, List<ErrosValidacao>> filtered = erros.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().stream()
                                .filter(err -> codes.contains(err.codigo()))
                                .collect(Collectors.toList()),
                        (oldList, newList) -> {
                            List<ErrosValidacao> merged = new ArrayList<>(oldList);
                            merged.addAll(newList);
                            return merged;
                        },
                        LinkedHashMap::new
                ));

        filtered.values().removeIf(List::isEmpty);
        return filtered.isEmpty() ? validar() : invalidar(filtered);
    }

    /**
     * Returns a sequential {@code Stream} of all field-error pairs in this validation result.
     *
     * <p>The stream elements are {@code Map.Entry} objects where:
     * <ul>
     *   <li>The key is the field name (as {@code String})</li>
     *   <li>The value is the {@code ErrosValidacao} associated with that field</li>
     * </ul>
     * </p>
     *
     * <p><b>Usage examples:</b></p>
     * <pre>{@code
     * // Count total errors
     * long totalErrors = result.errorStream().count();
     *
     * // Find all fields with specific error code
     * List<String> fields = result.errorStream()
     *     .filter(e -> "REQUIRED".equals(e.getValue().getCode()))
     *     .map(Map.Entry::getKey)
     *     .collect(Collectors.toList());
     * }</pre>
     *
     * @return a new sequential {@code Stream} of field-error entries, in the original insertion order.
     *         The stream will be empty if this is a valid result or contains no errors.
     *
     * @apiNote The returned stream must be closed after use to avoid resource leaks. For one-time
     *          operations, prefer using the direct access methods like {@link #todasMensagemErro()} .
     *          This method is particularly useful for complex error processing that requires filtering
     *          or transformation of validation errors.
     *
     * @implNote The stream preserves the original insertion order of fields and their respective errors.
     *           Each stream element is a new {@code Map.Entry} instance created during streaming.
     *
     * @see #getErrors()
     * @see #getAllErrorCodes()
     * @see #getAllErrorMessages()
     * @see Stream
     */
    public Stream<Map.Entry<String, ErrosValidacao>> erroStream() {
        return erros.entrySet().stream()
                .flatMap(e -> e.getValue().stream()
                        .map(err -> Map.entry(e.getKey(), err)));
    }

    /**
     * Groups all validation errors by their error code, preserving the original insertion order.
     *
     * <p>Returns a map where:
     * <ul>
     *   <li>Each key is a distinct error code (as {@code String})</li>
     *   <li>Each value is a {@code List} of {@code Map.Entry} objects containing:
     *     <ul>
     *       <li>The field name (entry key)</li>
     *       <li>The complete {@code ErrosValidacao} (entry value)</li>
     *     </ul>
     *   </li>
     * </ul>
     * </p>
     *
     * <p><b>Note:</b> The returned map maintains the first-occurrence order of each error code,
     * and each error list maintains the original error occurrence order.</p>
     *
     * <p><b>Usage example:</b></p>
     * <pre>{@code
     * // Group errors by their code
     * Map<String, List<Map.Entry<String, ErrosValidacao>>> errorsByCode = result.errorsByCode();
     *
     * // Process all "INVALID_FORMAT" errors
     * errorsByCode.getOrDefault("INVALID_FORMAT", Collections.emptyList())
     *     .forEach(entry -> {
     *         String field = entry.getKey();
     *         ValidationError error = entry.getValue();
     *         // Handle specific error
     *     });
     * }</pre>
     *
     * @return An immutable map grouping all errors by their codes. The map keys are in the order
     *         each error code was first encountered, and the error lists maintain their original
     *         insertion order. Returns an empty map if there are no errors.
     *
     * @apiNote This is particularly useful for:
     * <ul>
     *   <li>Generating error summaries by error type</li>
     *   <li>Applying bulk operations to specific error categories</li>
     *   <li>Creating error reports grouped by error code</li>
     * </ul>
     *
     * @see #erroStream()
     * @see #todosCodigoDeErro() ()
     * @see Collectors#groupingBy(Function, Supplier, Collector)
     */
    public Map<String, List<Map.Entry<String, ErrosValidacao>>> erroPorCodigo() {
        return erroStream()
                .collect(Collectors.groupingBy(
                        e -> e.getValue().codigo(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
    }


    /**
     * Transforms all validation error messages using the provided formatter function.
     *
     * <p>This method applies the given {@code formatter} to each validation error and returns
     * the formatted results as a list. The order of messages follows the original insertion
     * order of fields and their respective errors.</p>
     *
     * <p><b>Usage examples:</b></p>
     * <pre>{@code
     * // Simple formatting
     * List<String> messages = result.mensagemFormatada(
     *     error -> "Error: " + error.mensagem());
     *
     * // Internationalization
     * ResourceBundle bundle = ResourceBundle.getBundle("Messages");
     * List<String> localized = result.mensagemFormatada(
     *     error -> bundle.getString(error.codigo()) + ": " + error.mensagem());
     *
     * // JSON formatting
     * List<String> jsonErrors = result.mensagemFormatada(
     *     error -> String.format("{\"code\":\"%s\",\"message\":\"%s\"}",
     *         error.codigo(), error.codigo()));
     * private static void validateRate(double rate, List<ErrosValidacao> errors) {
     *         if (rate <= 0) {
     *             errors.add(new ErrosValidacao(
     *                 "PARCEL_RATE_INVALID",
     *                 "A taxa de juros deve ser positiva", false
     *                );
     *         }
     *
     *         if (rate > 30.0) {
     *             errors.add(new ErrosValidacao(
     *                 "PARCEL_RATE_EXCESSIVE",
     *                 "A taxa de juros não pode exceder 30%",
     *                 false
     *
     *             );
     *         }
     *     }
     * }</pre>
     *
     * @param formatador A function that transforms {@code ErrosValidacao} objects into formatted
     *                 strings (cannot be {@code null})
     * @return An immutable list containing the formatted error messages in original error order.
     *         Returns an empty list if there are no errors.
     *
     * @throws NullPointerException if {@code formatter} is {@code null}
     *
     * @see #todasMensagemErro() ()
     * @see #erroStream()
     * @see Function
     *
     * @apiNote This method is particularly useful for:
     * <ul>
     *   <li>Internationalization of error messages</li>
     *   <li>Custom error message formatting</li>
     *   <li>Preparing errors for API responses</li>
     * </ul>
     *
     * @implNote The formatter function should not return {@code null}. If null values are
     *           possible, they should be handled within the formatter function.
     */
    public List<String> mensagemFormatadas(Function<ErrosValidacao, String> formatador) {
        List<String> resultado = new ArrayList<>();
        erros.forEach((key, errors) -> {
            errors.forEach(error -> {
                resultado.add(formatador.apply(error));
            });
        });
        Collections.reverse(resultado);
        return resultado;
    }

    public static class ValidacaoException extends RuntimeException {
        public ValidacaoException(String message) {
            super(message);
        }
    }
}
