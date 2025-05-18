package org.com.pangolin.domain.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.function.Function;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;

class ValidationResultTest {

    private  ValidationResult validationResult;
    private Map<String, List<ValidationResult.ValidationError>> sampleErrors = new LinkedHashMap<>();;

    @BeforeEach
    void setUp() {
        sampleErrors.put("email", List.of(
                new ValidationResult.ValidationError.Builder("EMAIL_INVALID")
                        .withMessageKey("Invalid email format").build(),
                new ValidationResult.ValidationError.Builder("EMAIL_TAKEN")
                        .withMessageKey("Email already in use").build()
        ));
        sampleErrors.put("password", List.of(
                new ValidationResult.ValidationError.Builder("PWD_TOO_SHORT")
                        .withMessageKey("Password must be at least 8 characters").build()
        ));
        validationResult = new ValidationResult(sampleErrors);
    }

    @Test
    void testValid_ShouldReturnValidResult() {


        //--------------------------------- Act ----------------------------//

        ValidationResult result = ValidationResult.valid();

        //--------------------------------- Assert -------------------------//
        assertAll(
                () -> assertTrue(result.isValid(), "Deveria ser um resultado válido"),
                () -> assertTrue(result.getErrors().isEmpty(), "Deveria ter mapa de erros vazio"),
                () -> assertEquals(0, result.getErrors().size(), "Deveria ter zero erros")
        );
    }




    @Test
    void testValid_ShouldHaveEmptyErrorMap() {
        //-------------------------- Act -----------------------//
        ValidationResult result = ValidationResult.valid();

        //-------------------------- Assert -------------------//
        assertEquals(Collections.emptyMap(), result.getErrors(),
                "O mapa de erros deveria ser vazio");
        assertTrue(result.getErrors().isEmpty(),
                "O mapa de erros deveria estar vazio");
    }

    @Test
    void testValid_ShouldNotContainAnyErrors() {
        //------------------------ Act ---------------------------------//
        ValidationResult result = ValidationResult.valid();

        //------------------------ Assert -----------------------------//
        assertAll(
                 () -> assertEquals(Collections.emptyList(), result.getErrorsForField("anyField"),
                        "Deveria retornar lista vazia para qualquer campo")
        );
    }

    @Test
    void testValid_CombinedOperationsShouldRemainValid() {
        //-------------------------- Arrange ------------------------//
        ValidationResult validResult = ValidationResult.valid();

        //-------------------------- Act ----------------------------//
        ValidationResult combined = validResult.combine(ValidationResult.valid());

        //-------------------------- Assert ------------------------//
        assertTrue(combined.isValid(),
                "Combinação com outro resultado válido deveria permanecer válido");
    }

    @Test
    void testValid_ShouldReturnConsistentStringRepresentation() {
        //---------------------------------- Act ------------------//
        ValidationResult result = ValidationResult.valid();

        //-------------------------- Assert ----------------------//
        assertEquals("ValidationResult{valid=true, errors={}}", result.toString(),
                "A representação em string deveria ser consistente");
    }

    @Test
    void testValid_ShouldSerializeCorrectly() throws JsonProcessingException {
        //--------------------------- Arrange -----------------------//
        ObjectMapper mapper = new ObjectMapper();
        ValidationResult result = ValidationResult.valid();

        //--------------------------- Act --------------------------//
        String json = mapper.writeValueAsString(result);

        //-------------------------- Assert -----------------------//
        assertTrue(json.contains("\"valid\":true"), "JSON deveria indicar válido");
        assertTrue(json.contains("\"errors\":{}"), "JSON deveria mostrar erros vazios");
    }

    @Test
    void invalid_ShouldCreateResultWithSingleError() {
        //-------------------------------  Arrange  ---------------------------------//
        String fieldName = "email";
        ValidationResult.ValidationError error = new ValidationResult.ValidationError.Builder("EMAIL_INVALID")
                .withMessageKey("Invalid email format").build();

        //-------------------------------  Act -------------------------------------//
        ValidationResult result = ValidationResult.invalid(fieldName, error);

        //------------------------------- Assert ----------------------------------//
        assertAll(
                () -> assertFalse(result.isValid(), "Deveria ser resultado inválido"),
                () -> assertEquals(1, result.getErrors().size(), "Deveria ter exatamente 1 erro"),
                () -> assertTrue(result.hasErrorsForField(fieldName),
                        "Deveria ter erros para o campo especificado"),
                () -> assertEquals(singletonList(error), result.getErrorsForField(fieldName),
                        "Deveria conter o erro especificado")
        );
    }

    @Test
    void invalid_ShouldReturnCorrectErrorStructure() {
        //--------------------------------------- Arrange ------------------------------------//
        String fieldName = "password";
        ValidationResult.ValidationError error = new ValidationResult.ValidationError.Builder("PWD_TOO_WEAK")
                .withMessageKey("Password is too weak").build();

        //--------------------------------------- Act ---------------------------------------//
        ValidationResult result = ValidationResult.invalid(fieldName, error);
        Map<String, List<ValidationResult.ValidationError>> errors = result.getErrors();

        //--------------------------------------- Assert -----------------------------------//
        assertAll(
                () -> assertEquals(1, errors.size(), "Deveria ter exatamente 1 entrada no mapa"),
                () -> assertTrue(errors.containsKey(fieldName),
                        "Deveria conter o campo especificado"),
                () -> assertEquals(1, errors.get(fieldName).size(),
                        "Deveria ter exatamente 1 erro para o campo"),
                () -> assertEquals(error, errors.get(fieldName).getFirst(),
                        "Deveria conter o erro especificado")
        );
    }
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  "})
    void invalid_ShouldThrowWhenFieldIsBlank(String invalidField) {
        //--------------------------- Arrange -------------------------------------//
        ValidationResult.ValidationError error = new ValidationResult.ValidationError.Builder("ERROR_CODE")
                .withMessageKey("Error message").build();

        //------------------------------- Act & Assert ----------------------------//
        assertThrows(IllegalArgumentException.class,
                () -> ValidationResult.invalid(invalidField, error),
                "Deveria lançar exceção para campo nulo ou vazio");
    }

    @Test
    void invalid_ShouldThrowWhenErrorIsNull() {
        //----------------------------  Arrange ----------------------//
         String fieldName = "username";

        //----------------------------- Act & Assert -----------------//
        assertThrows(NullPointerException.class,
                () -> ValidationResult.invalid(fieldName, (List<ValidationResult.ValidationError>) null),
                "Deveria lançar exceção para erro nulo");
    }

    @Test
    void invalid_ShouldCombineCorrectlyWithOtherResults() {
        //------------------------------- Arrange ---------------------------//
        ValidationResult.ValidationError emailError = new ValidationResult.ValidationError.Builder("EMAIL_INVALID")
                .withMessageKey("Invalid email").build();
        ValidationResult result1 = ValidationResult.invalid("email", emailError);

        ValidationResult.ValidationError passwordError = new ValidationResult.ValidationError.Builder("PWD_WEAK")
                .withMessageKey("Weak password").build();

        ValidationResult result2 = ValidationResult.invalid("password", passwordError);

        //-------------------------------- Act -----------------------------//
        ValidationResult combined = result1.combine(result2);

        //------------------------------ Assert ----------------------------//
        assertAll(
                () -> assertFalse(combined.isValid(), "Deveria ser inválido após combinação"),
                () -> assertEquals(2, combined.getErrors().size(), "Deveria ter 2 erros"),
                () -> assertTrue(combined.hasErrorsForField("email"),
                        "Deveria manter erro de email"),
                () -> assertTrue(combined.hasErrorsForField("password"),
                        "Deveria manter erro de password")
        );
    }

    @Test
    void invalidMulti_ShouldCreateResultWithMultipleErrors() {
        //-------------------------- Arrange ---------------------------//
        Map<String, String> fieldErrors = new HashMap<>();
        fieldErrors.put("email", "Invalid email format");
        fieldErrors.put("password", "Password too weak");
        fieldErrors.put("username", "Username already taken");

        //-----------------------------  Act -------------------------------//
        ValidationResult result = ValidationResult.invalidMulti(fieldErrors);

        //---------------------------- Assert ------------------------------//
        assertAll(
                () -> assertFalse(result.isValid(), "Deveria ser resultado inválido"),
                () -> assertEquals(3, result.getErrors().size(), "Deveria ter 3 erros no total"),
                () -> assertEquals(1, result.getErrorsForField("email").size(),
                        "Deveria ter 1 erro para email"),
                () -> assertEquals(1, result.getErrorsForField("password").size(),
                        "Deveria ter 1 erro para password"),
                () -> assertEquals(1, result.getErrorsForField("username").size(),
                        "Deveria ter 1 erro para username"),
                () -> assertFalse(result.getErrors().isEmpty(), "Deveria indicar que não tem erros")
        );
    }
    @Test
    void invalidMulti_ShouldHandleEmptyMap() {

        //------------------------------------ Act and Assert ----------------------------//
        assertThrows(IllegalStateException.class, ()->ValidationResult.invalidMulti(Collections.emptyMap()),"Deveria indicar uma exception");

    }

    @ParameterizedTest
    @NullSource
    void invalidMulti_ShouldThrowWhenMapIsNull(Map<String, String> nullMap) {
        // Act & Assert
        assertThrows(NullPointerException.class,
                () -> ValidationResult.invalidMulti(nullMap),
                "Deveria lançar exceção para mapa nulo");
    }

    @Test
    void invalidMulti_ShouldHandleNullMessages() {
        // Arrange
        Map<String, String> fieldErrors = new HashMap<>();
        fieldErrors.put("email", null);
        fieldErrors.put("password", "");
        fieldErrors.put("username", "  ");


        //-----------------------------------  Act and Assert ----------------------------//
        assertThrows(IllegalStateException.class, ()->ValidationResult.invalidMulti(fieldErrors),
                        "Deveria criar erro com mensagem em branco");
    }

    @Test
    void invalidMulti_ShouldCombineCorrectlyWithOtherResults() {
        // Arrange
        Map<String, String> fieldErrors1 = Map.of(
                "email", "Invalid email",
                "password", "Too short"
        );

        Map<String, String> fieldErrors2 = Map.of(
                "username", "Already taken",
                "email", "Already registered" // Erro duplicado para email
        );

        ValidationResult result1 = ValidationResult.invalidMulti(fieldErrors1);
        ValidationResult result2 = ValidationResult.invalidMulti(fieldErrors2);

        // Act
        ValidationResult combined = result1.combine(result2);

        // Assert
        assertAll(
                () -> assertEquals(3, combined.getErrors().size(),
                        "Deveria ter erros para 3 campos distintos"),
                () -> assertEquals(2, combined.getErrorsForField("email").size(),
                        "Deveria combinar erros duplicados para o mesmo campo")
        );
    }

    @Test
    void getFormattedMessages_ShouldApplyFormatterToAllErrors() {
        //-------------------------------  Arrange  -----------------------------//
        Function<ValidationResult.ValidationError, String> formatter = error ->
                String.format("[%s] %s", error.getCode(), error.getMessage());

        //-------------------------------- Act ---------------------------------//
        List<String> messages = validationResult.getFormattedMessages(formatter);

        //------------------------------ Assert  -------------------------------//
        assertAll(
                () -> assertEquals(3, messages.size(), "Deveria formatar todos os erros"),
                () -> assertTrue(messages.contains("[EMAIL_INVALID] Invalid email format")),
                () -> assertTrue(messages.contains("[EMAIL_TAKEN] Email already in use")),
                () -> assertTrue(messages.contains("[PWD_TOO_SHORT] Password must be at least 8 characters"))
        );
    }
    @Test
    void getFormattedMessages_ShouldReturnEmptyListForValidResult() {
        // Arrange
        ValidationResult validResult = ValidationResult.valid();
        Function<ValidationResult.ValidationError, String> formatter = error -> "Error: " + error.getMessage();

        // Act
        List<String> messages = validResult.getFormattedMessages(formatter);

        // Assert
        assertTrue(messages.isEmpty(), "Deveria retornar lista vazia para resultado válido");
    }

    @Test
    void getFormattedMessages_ShouldHandleNullFormatter() {
        //---------------------------------- Act & Assert ---------------------------//
        assertThrows(NullPointerException.class,
                () -> validationResult.getFormattedMessages(null),
                "Deveria lançar exceção para formatter nulo");
    }

    @Test
    void getFormattedMessages_ShouldMaintainErrorOrder() {
        //------------------------------ Arrange -------------------------------//
        Function<ValidationResult.ValidationError, String> formatter = ValidationResult.ValidationError::getCode;

        //-------------------------------- Act --------------------------------//
        List<String> messages = validationResult.getFormattedMessages(formatter);

        //--------------------------------- Assert ----------------------------//
        assertAll(
                () -> assertEquals(3, messages.size(), "Deveria manter todos os códigos de erro"),
                () -> assertEquals("EMAIL_INVALID", messages.get(2), "Primeiro erro deveria ser o primeiro"),
                () -> assertEquals("EMAIL_TAKEN", messages.get(1), "Segundo erro deveria ser o segundo"),
                () -> assertEquals("PWD_TOO_SHORT", messages.getFirst(), "Terceiro erro deveria ser o terceiro")
        );
    }
    @Test
    void getFormattedMessages_ShouldHandleEmptyErrorLists() {
        // Arrange
        Map<String, List<ValidationResult.ValidationError>> errorsWithEmptyList = Map.of(
                "empty", List.of(),
                "email", List.of(new ValidationResult.ValidationError.Builder("EMAIL_INVALID")
                        .withMessageKey("Invalid email").build())
        );
        ValidationResult result = new ValidationResult(errorsWithEmptyList);
        Function<ValidationResult.ValidationError, String> formatter = ValidationResult.ValidationError::getMessage;

        // Act
        List<String> messages = result.getFormattedMessages(formatter);

        // Assert
        assertEquals(1, messages.size(), "Deveria ignorar listas de erro vazias");
        assertEquals("Invalid email", messages.getFirst());
    }

    @Test
    void shouldReturnTrueWhenFieldContainsErrorCode() {
        // Caso positivo - código existe no campo
        assertTrue(validationResult.fieldHasErrorCode("email", "EMAIL_INVALID"));
        assertTrue(validationResult.fieldHasErrorCode("email", "EMAIL_TAKEN"));
        assertTrue(validationResult.fieldHasErrorCode("password", "PWD_TOO_SHORT"));
    }

    @Test
    void shouldReturnFalseWhenFieldDoesNotContainErrorCode() {
        // Caso negativo - código não existe no campo
        assertFalse(validationResult.fieldHasErrorCode("email", "NON_EXISTENT_CODE"));
        assertFalse(validationResult.fieldHasErrorCode("password", "EMAIL_INVALID"));
    }

    @Test
    void shouldReturnFalseForNonExistentField() {
        // Campo que não existe
        assertFalse(validationResult.fieldHasErrorCode("username", "ANY_CODE"));
    }

    @Test
    void shouldBeCaseSensitive() {
        // Verifica sensibilidade a maiúsculas/minúsculas
        assertFalse(validationResult.fieldHasErrorCode("email", "email_invalid"));
        assertFalse(validationResult.fieldHasErrorCode("email", "EMAIL_invalid"));
    }

    @Test
    void shouldHandleNullFieldName() {
        // Teste com field null
        assertFalse(validationResult.fieldHasErrorCode(null, "EMAIL_INVALID"));
    }

    @Test
    void shouldHandleNullErrorCode() {
        // Teste com errorCode null
        assertFalse(validationResult.fieldHasErrorCode("email", null));
    }

    @Test
    void shouldReturnFalseForEmptyFieldErrors() {
        // Campo sem erros
        sampleErrors.put("emptyField", Collections.emptyList());
        validationResult = new ValidationResult(sampleErrors);
        assertFalse(validationResult.fieldHasErrorCode("emptyField", "ANY_CODE"));
    }

    @Test
    void shouldCreateValidResultWhenNoErrors() {
        Map<String, List<ValidationResult.ValidationError>> emptyErrors = new HashMap<>();
        ValidationResult result = new  ValidationResult
                                            .Builder()
                                                 .withValid(true)
                                                 .withErrors(emptyErrors)
                                            .builder();

        assertTrue(result.isValid());
        assertTrue(result.getErrors().isEmpty());
    }

    @Test
    void shouldCreateInvalidResultWithErrors() {
        Map<String, List<ValidationResult.ValidationError>> errors = Map.of(
                "email", List.of(
                        new ValidationResult.ValidationError.Builder("EMAIL_INVALID").withMessageKey("EMAIL_INVALID TESTE").build()
                )
        );

        ValidationResult result = new  ValidationResult
                .Builder()
                .withValid(false)
                .withErrors(errors)
                .builder();

        assertFalse(result.isValid());
        assertEquals(1, result.getErrors().size());
        assertTrue(result.hasErrorsForField("email"));
    }

    @Test
    void shouldThrowExceptionWhenValidWithErrors() {
        Map<String, List<ValidationResult.ValidationError>> errors = Map.of(
                "email", List.of(
                        new ValidationResult.ValidationError.Builder("EMAIL_INVALID")
                                .withMessageKey("EMAIL_INVALID_TESTE").build()
                )
        );

        assertThrows(IllegalStateException.class,
                () -> new ValidationResult.Builder()
                         .withValid(true).withErrors( errors).builder());
    }
    @Test
    void shouldThrowExceptionWhenInvalidWithoutErrors() {
        assertThrows(IllegalStateException.class,
                () -> new ValidationResult.Builder()
                        .withValid(false).withErrors(Collections.emptyMap()).builder());
    }

    @Test
    void shouldThrowExceptionWhenInvalidWithErrors() {

        Map<String, List<ValidationResult.ValidationError>> errors = Map.of(
                "email", List.of(
                        new ValidationResult.ValidationError.Builder("EMAIL_INVALID")
                                .withMessageKey("EMAIL_INVALID_TESTE").build()
                )
        );
        assertThrows(IllegalStateException.class,
                () -> new ValidationResult.Builder()
                        .withValid(true).withErrors(errors).builder());
    }


}