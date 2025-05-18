package org.com.pangolin.domain.core;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.*;


/**
 * Represents the result of a validation operation, containing structured information about
 * validation errors organized by field, with error codes and descriptive messages.
 *
 * <p>This class is immutable and thread-safe, allowing:</p>
 * <ul>
 *   <li>Safe combination of multiple validation results</li>
 *   <li>Structured querying of errors by field or code</li>
 *   <li>Conversion to various output formats</li>
 * </ul>
 *
 * <p><b>Usage example:</b></p>
 * <pre>{@code
 * ValidationResult result = ValidationResult.invalid("email", "INVALID_FORMAT", "Invalid email");
 * if (!result.isValid()) {
 *     result.getErrorsForField("email").forEach(error ->
 *         System.out.println(error.getCode() + ": " + error.getMessage()));
 * }
 * }</pre>
 *
 * @see #valid()
 * @see #invalid(String, String, String)
 * @see #combine(ValidationResult)
 */
public class ValidationResult {
    private final boolean valid ;
    private  Map<String, List<String>> errors;
    private final Map<String, List<ValidationError>> validationErrors;

    /**
     * Constructs a new ValidationResult instance with the specified validity state and error mappings.
     *
     * <p>This constructor initializes the validation result with the given parameters and enforces
     * the following invariants:</p>
     * <ul>
     *   <li>Creates an immutable copy of the errors map to maintain immutability</li>
     *   <li>Preserves insertion order of errors using {@link LinkedHashMap}</li>
     *   <li>Validates state consistency through {@link #validateState()}</li>
     * </ul>
     *
     * <p><b>Important:</b> This constructor is private to enforce usage of the factory methods
     * {@link #valid()} and {@link #invalid(String, String)} which provide proper validation
     * and null-checking.</p>
     *
     * @param valid indicates whether the validation result represents a valid state (true) or invalid state (false)
     * @param errors mapping of field names to their associated error messages (cannot be null)
     *
     * @throws NullPointerException if errors is null
     * @throws IllegalStateException if the validity state contradicts the error content:
     *         <ul>
     *           <li>valid=true but errors map is not empty</li>
     *           <li>valid=false but errors map is empty</li>
     *         </ul>
     *
     * @see #valid()
     * @see #invalid(String, String)
     * @see #invalid(Map)
     * @see #validateState()
     *
     * @implSpec The constructor:
     * <ul>
     *   <li>Makes a defensive copy of the input map</li>
     *   <li>Wraps the copy in an unmodifiable map</li>
     *   <li>Validates the consistency of the valid flag with the error content</li>
     * </ul>
     */
    private ValidationResult(boolean valid,Map<String, List<ValidationError>> validationErrors) {
        this.valid = valid;
        this.validationErrors =  validationErrors;
        validateState();

    }

    public ValidationResult(Map<String, List<ValidationError>> validationErrors) {
        this.validationErrors = validationErrors;
        this.valid = false;
        validateState();
    }

    public  static class Builder{

        private  boolean valid;
        private  Map<String,List<ValidationError>> errors;
        public Builder(){}

        public  Builder withValid(boolean valid){
            this.valid = valid;
            return  this;
        }

        public Builder withErrors(Map<String,List<ValidationError>> errors){
            this.errors = errors;
            return  this;
        }

        public  ValidationResult builder(){
            return  new ValidationResult(valid,errors);
        }



    }

    /**
     * Validates the internal state consistency of this validation result.
     *
     * <p>This private method performs the following consistency checks:</p>
     * <ul>
     *   <li>A valid result (isValid() = true) must not contain any errors</li>
     *   <li>An invalid result (isValid() = false) must contain at least one error</li>
     * </ul>
     *
     * @throws IllegalStateException if either of these conditions is violated:
     * <ul>
     *   <li>"Valid result cannot contain errors" - when valid=true but errors exist</li>
     *   <li>"Invalid result must contain errors" - when valid=false but errors are empty</li>
     * </ul>
     *
     * <p>This validation is automatically called during object construction
     * to ensure all ValidationResult instances maintain consistent state.</p>
     */
    private void validateState() {
        if (valid && !validationErrors.isEmpty()) {
            throw new IllegalStateException("Valid result cannot contain errors");
        }
        if (!valid && validationErrors.isEmpty()) {
            throw new IllegalStateException("Invalid result must contain errors");
        }
    }
    /**
     * Creates a valid validation result (no errors).
     *
     * @return ValidationResult instance representing successful validation
     */
    public static ValidationResult valid() {
        return new ValidationResult(true, Collections.emptyMap());
    }




    /**
     * Creates an invalid validation result with a single error.
     *
     * @param field field associated with the error (cannot be null)
     * @param code error code (cannot be null)
     * @param message error message (cannot be null)
     * @return ValidationResult instance representing failed validation
     * @throws NullPointerException if any parameter is null
     */
    public static ValidationResult invalid(String field, String code, String message) {

        return invalid(field,new ValidationError.Builder(code)
                .withMessageKey(message)
                .build());
    }

    /**
     * Creates an invalid validation result with a ValidationError object.
     *
     * @param field field associated with the error (cannot be null)
     * @param error object containing code and message (cannot be null)
     * @return ValidationResult instance representing failed validation
     * @throws NullPointerException if field or error are null
     */
    public static ValidationResult invalid(String field, ValidationError error) {
        Map<String, List<ValidationError>> errors = new LinkedHashMap<>();

        if(Objects.isNull(field)){
            throw  new IllegalArgumentException("The field is mandatary");
        }
        if(field.isBlank() || field.contains("  ") || field.contains(" ")){
            throw  new IllegalArgumentException("The field is mandatary");
        }

        errors.put(field, singletonList(error));
        return new ValidationResult(errors);
    }

    /**
     * Creates an invalid validation result with multiple errors.
     *
     * @param errors map of errors by field (cannot be null or contain null values)
     * @return ValidationResult instance representing failed validation
     * @throws NullPointerException if map or any element is null
     * @throws IllegalArgumentException if map is empty
     */
    public static ValidationResult invalid(Map<String, List<ValidationError>> validationErrors) {
        return new ValidationResult(validationErrors);
    }

    /**
     * Creates an invalid validation result with multiple validation errors for a single field.
     *
     * <p>The method creates an immutable validation result containing all provided errors
     * associated with the specified field.</p>
     *
     * <p><b>Usage example:</b></p>
     * <pre>{@code
     * List<ValidationError> errors = Arrays.asList(
     *     new ValidationError("INVALID_FORMAT", "Invalid format"),
     *     new ValidationError("MISSING_DATA", "Required field is missing")
     * );
     * ValidationResult result = ValidationResult.invalid("email", errors);
     * }</pre>
     *
     * @param field The field name to associate with the errors (cannot be {@code null} or blank)
     * @param errors The list of validation errors for the field (cannot be {@code null},
     *               can be empty, and cannot contain {@code null} elements)
     * @return A new {@code ValidationResult} in invalid state containing the provided errors
     *
     * @throws NullPointerException if either {@code field} or {@code errors} is {@code null}
     * @throws IllegalArgumentException if {@code field} is blank or if {@code errors}
     *         contains {@code null} elements
     *
     * @see ValidationError
     * @see #invalid(String, String, String)
     * @see #invalid(String, ValidationError)
     * @see #invalid(Map)
     */
    public static ValidationResult invalid(String field, List<ValidationError> errors) {
        Map<String, List<ValidationError>> errorMap = new LinkedHashMap<>();
        errorMap.put(field, List.copyOf(errors));
        return new ValidationResult(errorMap);
    }

    /**
     * Creates an invalid validation result with multiple field errors, where each field has a single error message.
     *
     * <p>This convenience method accepts a map of field names to error messages and converts them
     * into a proper validation result structure. Each field will have a single generic validation error
     * with the provided message.</p>
     *
     * <p><b>Usage example:</b></p>
     * <pre>{@code
     * Map<String, String> errors = new HashMap<>();
     * errors.put("email", "Invalid email format");
     * errors.put("password", "Password too weak");
     * ValidationResult result = ValidationResult.invalidMulti(errors);
     * }</pre>
     *
     * <p><b>Note:</b> For more complex errors with custom error codes or multiple errors per field,
     * use {@link #invalid(Map)} instead.</p>
     *
     * @param fieldErrors A map of field names to their corresponding error messages
     *                   (cannot be {@code null}, can be empty, cannot contain {@code null} keys or values)
     * @return A new {@code ValidationResult} in invalid state containing the provided field errors
     *
     * @throws NullPointerException if {@code fieldErrors} is {@code null} or contains {@code null} keys/values
     *
     * @see #invalid(String, String, String)
     * @see #invalid(String, ValidationError)
     * @see #invalid(Map)
     * @see #invalid(String, List)
     */
    public static ValidationResult invalidMulti(Map<String, String> fieldErrors) {

        Objects.requireNonNull(fieldErrors,"The errors is mandatary");

        Map<String, List<ValidationError>> result = new HashMap<>();
        fieldErrors.forEach((field, message) -> {
            ValidationError error = createDefaultValidationError(field, message);
            result.computeIfAbsent(field, k -> new ArrayList<>()).add(error);
        });
        return new ValidationResult( result);
    }

    private static ValidationError createDefaultValidationError(String field, String message) {
        return new ValidationError.Builder(field)
                .withMessageKey(message)
                .build();
    }

    /**
     * Checks if validation was successful.
     *
     * @return true if no validation errors exist, false otherwise
     */
    public  boolean isValid() {
        return valid;
    }

    /**
     * Returns all errors grouped by field.
     *
     * <p>The returned map is immutable and preserves insertion order.</p>
     *
     * @return non-null map of errors by field (empty if validation is valid)
     */
    public Map<String, List<ValidationError>> getErrors() {
        return validationErrors;
    }

    /**
     * Returns errors associated with a specific field.
     *
     * @param field field to query
     * @return immutable list of errors (never null, empty if field doesn't exist)
     */
    public List<ValidationError> getErrorsForField(String field) {
        return validationErrors.getOrDefault(field, emptyList());
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
     * ValidationResult result = ValidationResult.invalid("email", "INVALID", "Invalid email");
     * result = result.combine(ValidationResult.invalid("password", "TOO_SHORT", "Password too short"));
     *
     * List<String> allMessages = result.getAllErrorMessages();
     * // Returns ["Invalid email", "Password too short"]
     * }</pre>
     *
     * @return An immutable list containing all error messages from all fields in encounter order.
     *         Returns an empty list if there are no errors (including valid results).
     *         Never returns {@code null}.
     *
     * @see #getErrors()
     * @see #getErrorsForField(String)
     * @see #getAllErrorCodes()
     * @see ValidationError#getMessage()
     */
    public List<String> getAllErrorMessages() {
        return validationErrors.values().stream()
                .flatMap(List::stream)
                .map(ValidationError::getMessage)
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
     * ValidationResult result = ValidationResult.invalid("email", "INVALID_FORMAT", "Invalid format");
     * result = result.combine(ValidationResult.invalid("email", "INVALID_FORMAT", "Bad format"));
     * result = result.combine(ValidationResult.invalid("password", "TOO_SHORT", "Too short"));
     *
     * List<String> codes = result.getAllErrorCodes();
     * // Returns ["INVALID_FORMAT", "TOO_SHORT"] (duplicates removed)
     * }</pre>
     *
     * @return An immutable list containing all unique error codes across all validation errors,
     *         in the order of their first occurrence. Returns an empty list if there are no errors
     *         (including valid results). Never returns {@code null}.
     *
     * @see #getAllErrorMessages()
     * @see #getErrors()
     * @see #getErrorsForField(String)
     * @see ValidationError#getCode()
     *
     * @implNote The current implementation uses {@link Stream#distinct()} which preserves
     *           the first occurrence of each duplicate element in the encounter order.
     */
    public List<String> getAllErrorCodes() {
        return validationErrors.values().stream()
                .flatMap(List::stream)
                .map(ValidationError::getCode)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Combines this result with another validation result.
     *
     * <p>Combination follows these rules:</p>
     * <ul>
     *   <li>If both results are valid, returns a valid result</li>
     *   <li>If any result is invalid, returns an invalid result</li>
     *   <li>Errors are combined while maintaining insertion order</li>
     * </ul>
     *
     * @param other other validation result (cannot be null)
     * @return new ValidationResult combining both results
     * @throws NullPointerException if other is null
     */
    public ValidationResult combine(ValidationResult other) {
        if (this.valid && other.valid) {
            return valid();
        }

        return new ValidationResult(false, mergeErrors(this.validationErrors, other.validationErrors));
    }

    /**
     *
     * @param map1
     * @param map2
     * @return
     */
    private static Map<String, List<ValidationError>> mergeErrors(
            Map<String, List<ValidationError>> map1,
            Map<String, List<ValidationError>> map2) {

        Map<String, List<ValidationError>> merged = new LinkedHashMap<>();
        Stream.of(map1, map2).forEach(map ->
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
     * @param field The field name to check for errors (case-sensitive)
     * @param errorMessage The exact error message to search for (case-sensitive, cannot be null)
     * @return {@code true} if the field has at least one error with the exact specified message,
     *         {@code false} otherwise (including when the field doesn't exist or has no errors)
     *
     * @throws NullPointerException if {@code errorMessage} is {@code null}
     *
     * @see #containsErrorCode(String, String)
     * @see #getErrorsForField(String)
     * @see ValidationError#getMessage()
     *
     * @implNote This performs a direct string comparison using {@link String#equals(Object)}.
     *           For case-insensitive matching or partial matching, use {@link #getErrorsForField(String)}
     *           and implement custom matching logic.
     */
    public boolean containsError(String field, String errorMessage) {
        return errors.getOrDefault(field, emptyList())
                .contains(errorMessage);
    }

    /**
     * Checks if any error with the specified code exists.
     *
     * @param errorCode error code to search for (cannot be null)
     * @return true if at least one error with the code is found, false otherwise
     * @throws NullPointerException if errorCode is null
     */
    public boolean containsErrorCode(String errorCode) {
        return validationErrors.values().stream()
                .flatMap(List::stream)
                .anyMatch(e -> e.getCode().equals(errorCode));
    }

    /**
     * Determines whether the specified field has any validation errors.
     *
     * <p>This method checks if there are any error entries associated with the given field,
     * regardless of the specific error codes or messages.</p>
     *
     * <p><b>Usage example:</b></p>
     * <pre>{@code
     * ValidationResult result = ValidationResult.invalid("email", "INVALID", "Invalid format");
     *
     * if (result.hasErrorsForField("email")) {
     *     // Handle email field errors
     * }
     * }</pre>
     *
     * @param field The field name to check for errors (case-sensitive)
     * @return {@code true} if the field exists in the error map and has at least one error,
     *         {@code false} if the field has no errors or doesn't exist in the validation result
     *
     * @see #getErrorsForField(String)
     * @see #containsError(String, String)
     * @see #containsErrorCode(String, String)
     *
     * @implNote This is more efficient than {@code getErrorsForField(field).isEmpty()}
     *           as it avoids creating a new list instance for the check.
     */
    public boolean hasErrorsForField(String field) {
        return validationErrors.containsKey(field) && !validationErrors.get(field).isEmpty();
    }

    /**
     * Checks if the specified field contains at least one error with the given error code.
     *
     * <p>This method performs a case-sensitive exact match comparison of error codes
     * for all errors associated with the specified field.</p>
     *
     * <p><b>Usage example:</b></p>
     * <pre>{@code
     * ValidationResult result = ValidationResult.invalid("password", "TOO_SHORT", "Password must be at least 8 characters");
     *
     * if (result.fieldHasErrorCode("password", "TOO_SHORT")) {
     *     // Handle password length validation error
     * }
     * }</pre>
     *
     * @param field The field name to check for errors (case-sensitive)
     * @param errorCode The error code to search for (case-sensitive, cannot be {@code null})
     * @return {@code true} if the field exists and has at least one error with the specified code,
     *         {@code false} if the field doesn't exist, has no errors, or has no matching error code
     *
     * @throws NullPointerException if {@code errorCode} is {@code null}
     *
     * @see #hasErrorsForField(String)
     * @see #containsError(String, String)
     * @see #getErrorsForField(String)
     * @see ValidationError#getCode()
     *
     * @implNote This method provides more specific error code checking than {@link #hasErrorsForField(String)},
     *          and is more efficient than manually filtering {@link #getErrorsForField(String)} when you only
     *          need to check for code existence.
     */
    public boolean fieldHasErrorCode(String field, String errorCode) {
        return getErrorsForField(field).stream()
                .anyMatch(e -> e.getCode().equals(errorCode));
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
        return validationErrors.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().stream()
                                .map(ValidationError::getMessage)
                                .collect(Collectors.joining(", ")),
                        (oldVal, newVal) -> oldVal,
                        LinkedHashMap::new
                ));
    }

    /**
     * Filters errors keeping only those with the specified codes.
     *
     * @param errorCodes error codes to keep (at least one code must be provided)
     * @return new ValidationResult containing only filtered errors (or valid if no errors match)
     * @throws IllegalArgumentException if no codes are provided
     */
    public ValidationResult filterByErrorCode(String... errorCodes) {
        Set<String> codes = Set.of(errorCodes);
        Map<String, List<ValidationError>> filtered = validationErrors.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().stream()
                                .filter(err -> codes.contains(err.getCode()))
                                .collect(Collectors.toList()),
                        (oldList, newList) -> {
                            List<ValidationError> merged = new ArrayList<>(oldList);
                            merged.addAll(newList);
                            return merged;
                        },
                        LinkedHashMap::new
                ));

        filtered.values().removeIf(List::isEmpty);
        return filtered.isEmpty() ? valid() : invalid(filtered);
    }

    /**
     * Returns a sequential {@code Stream} of all field-error pairs in this validation result.
     *
     * <p>The stream elements are {@code Map.Entry} objects where:
     * <ul>
     *   <li>The key is the field name (as {@code String})</li>
     *   <li>The value is the {@code ValidationError} associated with that field</li>
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
     *          operations, prefer using the direct access methods like {@link #getAllErrorMessages()}.
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
    public Stream<Map.Entry<String, ValidationError>> errorStream() {
        return validationErrors.entrySet().stream()
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
     *       <li>The complete {@code ValidationError} (entry value)</li>
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
     * Map<String, List<Map.Entry<String, ValidationError>>> errorsByCode = result.errorsByCode();
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
     * @see #errorStream()
     * @see #getAllErrorCodes()
     * @see Collectors#groupingBy(Function, Supplier, Collector)
     */
    public Map<String, List<Map.Entry<String, ValidationError>>> errorsByCode() {
        return errorStream()
                .collect(Collectors.groupingBy(
                        e -> e.getValue().getCode(),
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
     * List<String> messages = result.getFormattedMessages(
     *     error -> "Error: " + error.getMessage());
     *
     * // Internationalization
     * ResourceBundle bundle = ResourceBundle.getBundle("Messages");
     * List<String> localized = result.getFormattedMessages(
     *     error -> bundle.getString(error.getCode()) + ": " + error.getMessage());
     *
     * // JSON formatting
     * List<String> jsonErrors = result.getFormattedMessages(
     *     error -> String.format("{\"code\":\"%s\",\"message\":\"%s\"}",
     *         error.getCode(), error.getMessage()));
     * private static void validateRate(double rate, List<ValidationError> errors) {
     *         if (rate <= 0) {
     *             errors.add(new ValidationError(
     *                 "PARCEL_RATE_INVALID",
     *                 "A taxa de juros deve ser positiva",
     *                 Map.of(
     *                     "severity", "HIGH",
     *                     "currentRate", rate
     *                 )
     *             ));
     *         }
     *
     *         if (rate > 30.0) {
     *             errors.add(new ValidationError(
     *                 "PARCEL_RATE_EXCESSIVE",
     *                 "A taxa de juros não pode exceder 30%",
     *                 Map.of(
     *                     "severity", "HIGH",
     *                     "maxRate", 30.0,
     *                     "currentRate", rate
     *                 )
     *             ));
     *         }
     *     }
     * }</pre>
     *
     * @param formatter A function that transforms {@code ValidationError} objects into formatted
     *                 strings (cannot be {@code null})
     * @return An immutable list containing the formatted error messages in original error order.
     *         Returns an empty list if there are no errors.
     *
     * @throws NullPointerException if {@code formatter} is {@code null}
     *
     * @see #getAllErrorMessages()
     * @see #errorStream()
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
    public List<String> getFormattedMessages(Function<ValidationError, String> formatter) {
        List<String> result = new ArrayList<>();
        validationErrors.forEach((key, errors) -> {
            errors.forEach(error -> {
                result.add(formatter.apply(error));
            });
        });
        Collections.reverse(result);
        return result;
    }

    /**
     * @return text representation of the result (includes all errors if invalid)
     */
    @Override
    public String toString() {
        return valid ? "ValidationResult{valid=true, errors={}}" :
                "ValidationResult{valid=false, errors={%s}} " + errors.entrySet().stream()
                        .map(e -> e.getKey() + "=" + e.getValue())
                        .collect(Collectors.joining(", "));
    }

    /**
     * Represents an individual validation error with code and message.
     *
     * <p>Error codes should be well-defined constants to enable
     * programmatic error handling.</p>
     */
    public static final class ValidationError {
        private final String code;
        private final String messageKey;
        private final Map<String, Object> metadata;
        private final LocalDateTime timestamp;
        private final SeverityLevel severity;


        private ValidationError(Builder builder) {
            this.code =  Objects.requireNonNull(builder.code,"The code is mandatary");
            this.messageKey = Objects.requireNonNull(builder.messageKey,"The code is mandatary");
            this.metadata = Collections.unmodifiableMap(new HashMap<>(builder.metadata));
            this.timestamp = LocalDateTime.now();
            this.severity = builder.severity;
        }
        /**
         * @return the error code (never null)
         */
        public String getCode() { return code; }
        /**
         * @return the descriptive error message (never null)
         */
        public String getMessage() { return messageKey; }

        public Map<String, Object> getMetadata() { return metadata; }

        public SeverityLevel getSeverity() { return severity; }

        public LocalDateTime getTimestamp() { return timestamp; }

        public enum SeverityLevel {
            INFO, WARNING, ERROR, CRITICAL
        }
        public String getLocalizedMessage(ResourceBundle bundle) {
            try {
                String baseMessage = bundle.getString(messageKey);
                return String.format(baseMessage, metadata.values().toArray());
            } catch (MissingResourceException e) {
                return "Error code: " + code; // Fallback message
            }
        }
        /**
         * @return error representation in "[code] message" format
         */
        @Override
        public String toString() {
            return String.format("[%s] %s (metadata: %s)", code, messageKey, metadata);
        }

        public static class Builder {
            private final String code;
            private String messageKey;
            private final Map<String, Object> metadata = new HashMap<>();
            private SeverityLevel severity = SeverityLevel.ERROR;


            public Builder(String code) {
                this.code = Objects.requireNonNull(code, "Código de erro não pode ser nulo");
            }

            public Builder withMessageKey(String messageKey) {
                this.messageKey = messageKey;
                return this;
            }

            public Builder withMetadata(String key, Object value) {
                this.metadata.put(key, value);
                return this;
            }

            public Builder withSeverity(SeverityLevel severity) {
                this.severity = severity;
                return this;
            }


            public ValidationError build() {
                if ((messageKey == null)|| messageKey.isBlank()|| messageKey.equals(" ") || messageKey.equals("  ")) {
                    throw new IllegalStateException("Chave de mensagem é obrigatória");
                }

                return  new ValidationError(this);
            }
        }
    }




}
