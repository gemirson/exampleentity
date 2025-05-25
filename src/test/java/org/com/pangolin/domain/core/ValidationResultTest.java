package org.com.pangolin.domain.core;

import org.com.pangolin.domain.core.validacoes.ErrosValidacao;
import org.com.pangolin.domain.core.validacoes.ResultadoValidacao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.JsonNode;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;

import static org.testcontainers.shaded.org.hamcrest.MatcherAssert.assertThat;

class ValidationResultTest {

    private  ResultadoValidacao resultadoValidacao;
    private Map<String, List<ErrosValidacao>> sampleErrors = new LinkedHashMap<>();;

    @BeforeEach
    void setUp() {
        sampleErrors.put("email", List.of( new ErrosValidacao("EMAIL_INVALID","Invalid email format",false)
                , new ErrosValidacao("EMAIL_TAKEN","Email already in use",false)));

        sampleErrors.put("password", List.of( new ErrosValidacao("PWD_TOO_SHORT","Password must be at least 8 characters",false)
                ));

        resultadoValidacao = new ResultadoValidacao (false,sampleErrors);
    }

    @Test
    void testValid_ShouldReturnValidResult() {


        //--------------------------------- Act ----------------------------//

         ResultadoValidacao resultado =  ResultadoValidacao.validar();

        //--------------------------------- Assert -------------------------//
        assertAll(
                () -> assertTrue(resultado.valido(), "Deveria ser um resultado válido"),
                () -> assertTrue(resultado.erros().isEmpty(), "Deveria ter mapa de erros vazio"),
                () -> assertEquals(0, resultado.erros().size(), "Deveria ter zero erros")
        );
    }




    @Test
    void testValid_ShouldHaveEmptyErrorMap() {
        //-------------------------- Act -----------------------//
        ResultadoValidacao resultado =  ResultadoValidacao.validar();

        //-------------------------- Assert -------------------//
        assertEquals(Collections.emptyMap(), resultado.erros(),
                "O mapa de erros deveria ser vazio");
        assertTrue(resultado.erros().isEmpty(),
                "O mapa de erros deveria estar vazio");
    }

    @Test
    void testValid_ShouldNotContainAnyErrors() {
        //------------------------ Act ---------------------------------//
        ResultadoValidacao resultado =  ResultadoValidacao.validar();

        //------------------------ Assert -----------------------------//
        assertAll(
                 () -> assertEquals(Collections.emptyList(), resultado.errorPorCampo("anyField"),
                        "Deveria retornar lista vazia para qualquer campo")
        );
    }

    @Test
    void testValid_CombinedOperationsShouldRemainValid() {
        //-------------------------- Arrange ------------------------//
        ResultadoValidacao resultado =  ResultadoValidacao.validar();

        //-------------------------- Act ----------------------------//
        ResultadoValidacao combinado = resultado.combinar(ResultadoValidacao.validar());

        //-------------------------- Assert ------------------------//
        assertTrue(combinado.valido(),
                "Combinação com outro resultado válido deveria permanecer válido");
    }

    @Test
    void testValid_ShouldReturnConsistentStringRepresentation() {
        //---------------------------------- Act ------------------//
        ResultadoValidacao resultado =  ResultadoValidacao.validar();

        //-------------------------- Assert ----------------------//
        assertEquals("ResultadoValidacao{valido=true, erros={}}", resultado.toString(),
                "A representação em string deveria ser consistente");
    }



    @Test
    void invalid_ShouldCreateResultWithSingleError() {
        //-------------------------------  Arrange  ---------------------------------//
        String campo = "email";
        ErrosValidacao  erro = new ErrosValidacao("EMAIL_INVALID","Invalid email format",false);
        List<ErrosValidacao>  erros = List.of(erro);
        //-------------------------------  Act -------------------------------------//
        ResultadoValidacao resultado = ResultadoValidacao.invalidar(campo, erros);

        //------------------------------- Assert ----------------------------------//
        assertAll(
                () -> assertFalse(resultado.valido(), "Deveria ser resultado inválido"),
                () -> assertEquals(1, resultado.erros().size(), "Deveria ter exatamente 1 erro"),
                () -> assertTrue(resultado.existeErroPorCampo(campo),
                        "Deveria ter erros para o campo especificado"),
                () -> assertEquals(singletonList(erro), resultado.errorPorCampo(campo),
                        "Deveria conter o erro especificado")
        );
    }



    @Test
    void testInvalidResult_MustContainExactlyOneErrorForField() {
        final String field = "password";
        final ErrosValidacao error = new ErrosValidacao("PWD_TOO_WEAK", "Password is too weak", false);

        // Execute
        ResultadoValidacao result = ResultadoValidacao.invalidar(field, List.of(error));

        // Verify
        final Map<String, List<ErrosValidacao>> errors = result.erros();

        assertEquals(1, errors.size(), "Must contain exactly one field entry");
        assertTrue(errors.containsKey(field), "Must contain the specified field");
        assertEquals(List.of(error), errors.get(field), "Must contain the exact error provided");
    }
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  "})
    void invalid_ShouldThrowWhenFieldIsBlank(String invalidField) {
        //--------------------------- Arrange -------------------------------------//
        ErrosValidacao  erro = new ErrosValidacao("PWD_TOO_WEAK","Password is too weak",false);
        List<ErrosValidacao>  erros = List.of(erro);

        //------------------------------- Act & Assert ----------------------------//
        assertThrows(IllegalArgumentException.class,
                () -> ResultadoValidacao.invalidar(invalidField, erros),
                "Deveria lançar exceção para campo nulo ou vazio");
    }

    @Test
    void invalid_ShouldThrowWhenErrorIsNull() {
        //----------------------------  Arrange ----------------------//
         String fieldName = "username";

        //----------------------------- Act & Assert -----------------//
        assertThrows(IllegalArgumentException.class,
                () -> ResultadoValidacao.invalidar(fieldName, (List<ErrosValidacao>) null),
                "Deveria lançar exceção para erro nulo");
    }

    @Test
    void invalid_ShouldCombineCorrectlyWithOtherResults() {
        //------------------------------- Arrange ---------------------------//
        ErrosValidacao  passwordErro = new ErrosValidacao("PWD_TOO_WEAK","Password is too weak",false);
        ErrosValidacao  emailErro = new ErrosValidacao("EMAIL_INVALID","Invalid email format",false);

        ResultadoValidacao resultado_email = ResultadoValidacao.invalidar("email", List.of(emailErro));
        ResultadoValidacao resultado_password = ResultadoValidacao.invalidar("password",List.of(passwordErro));

        //-------------------------------- Act -----------------------------//
        ResultadoValidacao combinado = resultado_email.combinar(resultado_password);

        //------------------------------ Assert ----------------------------//
        assertAll(
                () -> assertFalse(combinado.valido(), "Deveria ser inválido após combinação"),
                () -> assertEquals(2, combinado.erros().size(), "Deveria ter 2 erros"),
                () -> assertTrue(combinado.existeErroPorCampo("email"),
                        "Deveria manter erro de email"),
                () -> assertTrue(combinado.existeErroPorCampo("password"),
                        "Deveria manter erro de password")
        );
    }









    @Test
    void getFormattedMessages_ShouldApplyFormatterToAllErrors() {
        //-------------------------------  Arrange  -----------------------------//
        Function<ErrosValidacao, String> formatter = erro ->
                String.format("[%s] %s", erro.codigo(), erro.menssagem());

        //-------------------------------- Act ---------------------------------//
        List<String> messages = resultadoValidacao.mensagemFormatadas(formatter);

        //------------------------------ Assert  -------------------------------//
        assertEquals(List.of(
                "[EMAIL_INVALID] Invalid email format",
                "[EMAIL_TAKEN] Email already in use",
                "[PWD_TOO_SHORT] Password must be at least 8 characters"
        ), messages, "Formatter must be applied consistently to all errors");
    }
    @Test
    void getFormattedMessages_ShouldReturnEmptyListForValidResult() {
        // Arrange
        ResultadoValidacao resultado = ResultadoValidacao.validar();

        Function<ErrosValidacao, String> formatter = error -> "Error: " + error.menssagem();

        // Act
        List<String> messages = resultado.mensagemFormatadas(formatter);

        // Assert
        assertTrue(messages.isEmpty(), "Deveria retornar lista vazia para resultado válido");
    }

    @Test
    void getFormattedMessages_ShouldHandleNullFormatter() {
        //---------------------------------- Act & Assert ---------------------------//
        assertThrows(NullPointerException.class,
                () -> resultadoValidacao.mensagemFormatadas(null),
                "Deveria lançar exceção para formatter nulo");
    }

    @Test
    void testGetFormattedMessages_MustMaintainInsertionOrder() {
        //------------------------------ Arrange -------------------------------//
        Function<ErrosValidacao, String> formatter = ErrosValidacao::codigo;

        //-------------------------------- Act --------------------------------//
        List<String> messages = resultadoValidacao.mensagemFormatadas(formatter);

        //--------------------------------- Assert ----------------------------//
       // assertAll(
               // () -> assertEquals(3, messages.size(), "Deveria manter todos os códigos de erro")
              //  () -> assertEquals("EMAIL_INVALID", messages.get(2), "Primeiro erro deveria ser o primeiro"),
             //   () -> assertEquals("EMAIL_TAKEN", messages.get(1), "Segundo erro deveria ser o segundo"),
             //   () -> assertEquals("PWD_TOO_SHORT", messages.getFirst(), "Terceiro erro deveria ser o terceiro")
       // );

        assertIterableEquals(
                List.of("EMAIL_INVALID", "EMAIL_TAKEN", "PWD_TOO_SHORT"),
                messages,
                "Deveria manter todos os códigos de errona ordem"
        );
    }
    @Test
    void getFormattedMessages_ShouldHandleEmptyErrorLists() {
        //----------------------------------- Arrange -------------------------------------
        ErrosValidacao  emailErro = new ErrosValidacao("EMAIL_INVALID","Invalid email",false);
        Map<String, List<ErrosValidacao>> errorsWithEmptyList = Map.of(
                "empty", List.of(),
                "email", List.of(emailErro)
        );
        ResultadoValidacao resultado = new ResultadoValidacao(false,errorsWithEmptyList);
        Function<ErrosValidacao, String> formatter = ErrosValidacao::menssagem;

        //------------------------------------ Act ---------------------------------------
        List<String> messages = resultado.mensagemFormatadas(formatter);

        //----------------------------------- Assert ---------------------------------------
        assertEquals(1, messages.size(), "Deveria ignorar listas de erro vazias");
        assertEquals("Invalid email", messages.getFirst());
    }


    @Test
    void testErroPorCodigo_MustGroupErrorsByCodeMaintainingOrder() {
        // Setup - create test data with KNOWN ordering
        final ErrosValidacao error1 = new ErrosValidacao("CODE_A", "Message 1", false);
        final ErrosValidacao error2 = new ErrosValidacao("CODE_B", "Message 2", false);
        final ErrosValidacao error3 = new ErrosValidacao("CODE_A", "Message 3", false);

        ResultadoValidacao result = ResultadoValidacao.invalidar( Map.of("field",List.of(error1, error2, error3)));


        // Execute
        Map<String, List<ErrosValidacao>> grouped = result.erroPorCodigo();

        assert grouped.size() == 2 : "Tamanho errado do mapa";
        assert grouped.containsKey("CODE_A") : "Faltando CODE_A";
        assert grouped.containsKey("CODE_B") : "Faltando CODE_B";
    }

    @Test
    void testToString_WhenValid_ShouldShowEmptyErrors() {
        ResultadoValidacao resultado = ResultadoValidacao.validar();

        assertTrue(resultado.toString().contains("valido=true"));
        assertTrue(resultado.toString().contains("erros={}"));
    }

    @Test
    void erroStream_emptyMap_returnsEmptyStream() {
        ResultadoValidacao resultado = new ResultadoValidacao(false, Collections.emptyMap());
        assertEquals(0, resultado.erroStream().count());
    }

    @Test
    void erroStream_withErrors_returnsEntries() {
        Map<String, List<ErrosValidacao>> erros = new HashMap<>();
        erros.put("campo", List.of(new ErrosValidacao("codigo", "mensagem", false)));
        ResultadoValidacao resultado = new ResultadoValidacao(false, erros);

        Stream<Map.Entry<String, ErrosValidacao>> stream = resultado.erroStream();
        List<Map.Entry<String, ErrosValidacao>> entries = stream.toList();

        assertEquals(1, entries.size());
        assertEquals("campo", entries.get(0).getKey());
        assertEquals("codigo", entries.get(0).getValue().codigo());
        assertEquals("mensagem", entries.get(0).getValue().menssagem());
    }

    @Test
    void filtroPorCodigoDeErro_shouldFilterByGivenCodes() {
        List<ErrosValidacao> erros = List.of(
                new ErrosValidacao("A", "msgA", false),
                new ErrosValidacao("B", "msgB", false),
                new ErrosValidacao("C", "msgC", false)
        );
        Map<String, List<ErrosValidacao>> map = new HashMap<>();
        map.put("campo1", erros);
        ResultadoValidacao rv = ResultadoValidacao.invalidar(map);

        ResultadoValidacao filtered = rv.filtroPorCodigoDeErro("A", "C");
        assertFalse(filtered.valido());
        assertEquals(2, filtered.erros().get("campo1").size());
        assertTrue(filtered.erros().get("campo1").stream().anyMatch(e -> e.codigo().equals("A")));
        assertTrue(filtered.erros().get("campo1").stream().anyMatch(e -> e.codigo().equals("C")));
        assertFalse(filtered.erros().get("campo1").stream().anyMatch(e -> e.codigo().equals("B")));
    }

    @Test
    void filtroPorCodigoDeErro_shouldReturnValidIfNoMatch() {
        List<ErrosValidacao> erros = List.of(
                new ErrosValidacao("A", "msgA", false)
        );
        Map<String, List<ErrosValidacao>> map = new HashMap<>();
        map.put("campo1", erros);
        ResultadoValidacao rv = ResultadoValidacao.invalidar(map);

        ResultadoValidacao filtered = rv.filtroPorCodigoDeErro("X");
        assertTrue(filtered.valido());
        assertTrue(filtered.erros().isEmpty());
    }

    @Test
    void filtroPorCodigoDeErro_shouldHandleEmptyErrors() {
        ResultadoValidacao rv = ResultadoValidacao.validar();
        ResultadoValidacao filtered = rv.filtroPorCodigoDeErro("A");
        assertTrue(filtered.valido());
        assertTrue(filtered.erros().isEmpty());
    }
    @Test
    void toSimpleErrorMap_returnsExpectedMap() {
        ErrosValidacao erro1 = new ErrosValidacao("CODE1", "Mensagem 1", false);
        ErrosValidacao erro2 = new ErrosValidacao("CODE2", "Mensagem 2", false);
        Map<String, List<ErrosValidacao>> erros = Map.of(
                "campo1", List.of(erro1, erro2)
        );
        ResultadoValidacao resultado = new ResultadoValidacao(false, erros);

        Map<String, String> simpleMap = resultado.toSimpleErrorMap();

        assertEquals(1, simpleMap.size());
        assertTrue(simpleMap.containsKey("campo1"));
        assertEquals("Mensagem 1, Mensagem 2", simpleMap.get("campo1"));
    }

    @Test
    void toSimpleErrorMap_emptyErrors_returnsEmptyMap() {
        ResultadoValidacao resultado = new ResultadoValidacao(false, Map.of());
        Map<String, String> simpleMap = resultado.toSimpleErrorMap();
        assertTrue(simpleMap.isEmpty());
    }

    @Test
    void testTodosCodigoDeErroComErros() {
        ErrosValidacao erro1 = new ErrosValidacao("COD1", "Mensagem 1", false);
        ErrosValidacao erro2 = new ErrosValidacao("COD2", "Mensagem 2", false);
        ErrosValidacao erro3 = new ErrosValidacao("COD1", "Mensagem 3", false);

        Map<String, List<ErrosValidacao>> erros = Map.of(
                "campo1", List.of(erro1, erro2),
                "campo2", List.of(erro3)
        );
        ResultadoValidacao resultado = new ResultadoValidacao(false, erros);

        List<String> codigos = resultado.todosCodigoDeErro();
        assertEquals(2, codigos.size());
        assertTrue(codigos.contains("COD1"));
        assertTrue(codigos.contains("COD2"));
    }

    @Test
    void testTodosCodigoDeErroSemErros() {
        ResultadoValidacao resultado = ResultadoValidacao.validar();
        List<String> codigos = resultado.todosCodigoDeErro();
        assertNotNull(codigos);
        assertTrue(codigos.isEmpty());
    }

    @Test
    void todasMensagemErro_returnsAllMessages() {
        ErrosValidacao erro1 = new ErrosValidacao("CODE1", "Mensagem 1", false);
        ErrosValidacao erro2 = new ErrosValidacao("CODE2", "Mensagem 2", false);
        Map<String, List<ErrosValidacao>> erros = Map.of(
                "campo1", List.of(erro1),
                "campo2", List.of(erro2)
        );
        ResultadoValidacao resultado = new ResultadoValidacao(false, erros);

        List<String> mensagens = resultado.todasMensagemErro();

        assertEquals(2, mensagens.size());
        assertTrue(mensagens.contains("Mensagem 1"));
        assertTrue(mensagens.contains("Mensagem 2"));
    }

    @Test
    void todasMensagemErro_returnsEmptyListWhenNoErrors() {
        ResultadoValidacao resultado = new ResultadoValidacao(true, Map.of());
        List<String> mensagens = resultado.todasMensagemErro();
        assertNotNull(mensagens);
        assertTrue(mensagens.isEmpty());
    }


    @Test
    void errorPorCampoReturnsListWhenFieldExists() {
        ErrosValidacao erro = new ErrosValidacao("CODE", "Mensagem", false);
        ResultadoValidacao resultado = new ResultadoValidacao(false, Map.of("campo", List.of(erro)));
        List<ErrosValidacao> erros = resultado.errorPorCampo("campo");
        assertEquals(1, erros.size());
        assertEquals(erro, erros.get(0));
    }

    @Test
    void errorPorCampoReturnsEmptyListWhenFieldDoesNotExist() {
        ResultadoValidacao resultado = new ResultadoValidacao(false, Map.of());
        List<ErrosValidacao> erros = resultado.errorPorCampo("inexistente");
        assertNotNull(erros);
        assertTrue(erros.isEmpty());
    }


    @Test
    void testExisteErroPorCampo() {
        // Campo existe e tem erros
        ResultadoValidacao r1 = new ResultadoValidacao(false, Map.of(
                "campo1", List.of(new ErrosValidacao("C1", "msg", false))
        ));
        assertTrue(r1.existeErroPorCampo("campo1"));

        // Campo existe e não tem erros
        ResultadoValidacao r2 = new ResultadoValidacao(false, Map.of(
                "campo2", List.of()
        ));
        assertFalse(r2.existeErroPorCampo("campo2"));

        // Campo não existe
        ResultadoValidacao r3 = new ResultadoValidacao(false, Map.of());
        assertFalse(r3.existeErroPorCampo("inexistente"));
    }


    @Test
    void testcontemCodigoDeErro_returnsTrueIfCodeExists() {
        ErrosValidacao erro = new ErrosValidacao("CODE1", "Mensagem", false);
        ResultadoValidacao resultado = new ResultadoValidacao(false, Map.of("campo", List.of(erro)));
        assertTrue(resultado.contemCodigoDeErro("CODE1"));
    }

    @Test
    void testcontemCodigoDeErro_returnsFalseIfCodeDoesNotExist() {
        ErrosValidacao erro = new ErrosValidacao("CODE1", "Mensagem", false);
        ResultadoValidacao resultado = new ResultadoValidacao(false, Map.of("campo", List.of(erro)));
        assertFalse(resultado.contemCodigoDeErro("CODE2"));
    }

    @Test
    void testcontemCodigoDeErro_returnsFalseIfNoErrors() {
        ResultadoValidacao resultado = new ResultadoValidacao(false, Map.of());
        assertFalse(resultado.contemCodigoDeErro("ANY_CODE"));
    }


    @Test
    void testcontemErro_returnsTrueWhenErrorExists() {
        ResultadoValidacao r = ResultadoValidacao.invalidar(
                "campo", "COD1", "mensagem de erro"
        );
        assertTrue(r.contemErro("campo", "mensagem de erro"));
    }

    @Test
    void testcontemErro_returnsFalseWhenErrorDoesNotExist() {
        ResultadoValidacao r = ResultadoValidacao.invalidar(
                "campo", "COD1", "mensagem de erro"
        );
        assertFalse(r.contemErro("campo", "outra mensagem"));
    }

    @Test
    void testcontemErro_returnsFalseWhenFieldDoesNotExist() {
        ResultadoValidacao r = ResultadoValidacao.invalidar(
                "campo", "COD1", "mensagem de erro"
        );
        assertFalse(r.contemErro("outroCampo", "mensagem de erro"));
    }

    @Test
    void testcontemErro_throwsExceptionWhenMessageIsNull() {
        ResultadoValidacao r = ResultadoValidacao.invalidar(
                "campo", "COD1", "mensagem de erro"
        );
        assertThrows(NullPointerException.class, () -> r.contemErro("campo", null));
    }




    @Test
    void testecriarDeveRetornarInstanciaComValoresCorretos() {
        ErrosValidacao erro = new ErrosValidacao("CODIGO", "mensagem", false);
        Map<String, List<ErrosValidacao>> erros = Map.of("campo", List.of(erro));
        ResultadoValidacao resultado = ResultadoValidacao.criar(false, erros);

        assertFalse(resultado.valido());
        assertEquals(erros, resultado.erros());
    }


    @Test
    void testcombinar_deveRetornarValidar_quandoAmbosValidos() {
        ResultadoValidacao r1 = ResultadoValidacao.validar();
        ResultadoValidacao r2 = ResultadoValidacao.validar();

        ResultadoValidacao combinado = r1.combinar(r2);

        assertTrue(combinado.valido());
        assertTrue(combinado.erros().isEmpty());
    }

    @Test
    void testcombinar_deveRetornarInvalido_quandoUmInvalido() {
        ResultadoValidacao r1 = ResultadoValidacao.invalidar("campo", "COD", "msg");
        ResultadoValidacao r2 = ResultadoValidacao.validar();

        ResultadoValidacao combinado = r1.combinar(r2);

        assertFalse(combinado.valido());
        assertFalse(combinado.erros().isEmpty());
        assertTrue(combinado.erros().containsKey("campo"));
    }

    @Test
    void testcombinar_deveMesclarErros_quandoAmbosInvalidos() {
        ResultadoValidacao r1 = ResultadoValidacao.invalidar("campo1", "COD1", "msg1");
        ResultadoValidacao r2 = ResultadoValidacao.invalidar("campo2", "COD2", "msg2");

        ResultadoValidacao combinado = r1.combinar(r2);

        assertFalse(combinado.valido());
        assertTrue(combinado.erros().containsKey("campo1"));
        assertTrue(combinado.erros().containsKey("campo2"));
    }

    @Test
    void testcombinarDoisValidosRetornaValido() {
        ResultadoValidacao r1 = ResultadoValidacao.validar();
        ResultadoValidacao r2 = ResultadoValidacao.validar();

        ResultadoValidacao combinado = r1.combinar(r2);

        assertTrue(combinado.valido());
        assertTrue(combinado.erros().isEmpty());
    }


    @Test
    void testCriarComValidoTrue() {
        ResultadoValidacao resultado = ResultadoValidacao.criar(true, Map.of());
        assertTrue(resultado.valido());
        assertTrue(resultado.erros().isEmpty());
    }

    @Test
    void testCriarComValidoFalse() {
        ErrosValidacao erro = new ErrosValidacao("CODIGO", "mensagem", false);
        ResultadoValidacao resultado = ResultadoValidacao.criar(false, Map.of("campo", List.of(erro)));
        assertFalse(resultado.valido());
        assertEquals(1, resultado.erros().size());
        assertEquals(erro, resultado.erros().get("campo").get(0));
    }

    @Test
    void testComErrosSubstituiMapaDeErros() {
        ResultadoValidacao resultado = ResultadoValidacao.validar();
        Map<String, List<ErrosValidacao>> novoMapa = Map.of(
                "campo", List.of(new ErrosValidacao("codigo", "mensagem", false))
        );
        resultado.comErros(novoMapa);
        assertEquals(novoMapa, resultado.erros());
    }

    @Test
    void testComErrosNuloLancaExcecao() {
        ResultadoValidacao resultado = ResultadoValidacao.validar();
        assertThrows(NullPointerException.class, () -> resultado.comErros(null));
    }


    @Test
    void testValidoTrue() {
        ResultadoValidacao resultado = new ResultadoValidacao(true, Map.of());
        assertTrue(resultado.valido());
    }

    @Test
    void testValidoFalse() {
        ResultadoValidacao resultado = new ResultadoValidacao(false, Map.of());
        assertFalse(resultado.valido());
    }

    @Test
    void testValidar() {
        ResultadoValidacao r = ResultadoValidacao.validar();
        assertTrue(r.valido());
        assertTrue(r.erros().isEmpty());
        assertFalse(r.existeErroPorCampo("campo"));
        assertEquals(Collections.emptyList(), r.errorPorCampo("campo"));
        assertEquals(Collections.emptyList(), r.todasMensagemErro());
        assertEquals(Collections.emptyList(), r.todosCodigoDeErro());
        assertEquals(Collections.emptyMap(), r.toSimpleErrorMap());
        assertEquals(Collections.emptyList(), r.mensagemFormatadas(ErrosValidacao::menssagem));
        assertEquals("ResultadoValidacao{valido=true, erros={}}", r.toString());
        assertDoesNotThrow(r::lancarSeInvalido);
        assertEquals(Collections.emptyMap(), r.erroPorCodigo());
    }

    @Test
    void testInvalidarSingle() {
        ResultadoValidacao r = ResultadoValidacao.invalidar("campo", "COD", "msg");
        assertFalse(r.valido());
        assertTrue(r.existeErroPorCampo("campo"));
        assertEquals(1, r.errorPorCampo("campo").size());
        assertTrue(r.contemErro("campo", "msg"));
        assertTrue(r.contemCodigoDeErro("COD"));
        assertEquals(List.of("msg"), r.todasMensagemErro());
        assertEquals(List.of("COD"), r.todosCodigoDeErro());
        assertEquals(Map.of("campo", "msg"), r.toSimpleErrorMap());
        assertEquals(List.of("msg"), r.mensagemFormatadas(ErrosValidacao::menssagem));
        assertThrows(ResultadoValidacao.ValidacaoException.class, r::lancarSeInvalido);
        assertEquals("ResultadoValidacao{valido=false, erros={campo=[ErrosValidacao{codigo='COD', mensagem='msg'}]}}", r.toString());
        assertEquals(1, r.erroPorCodigo().size());
    }

    @Test
    void testErrosRetornaMapaCorreto() {
        ErrosValidacao erro = new ErrosValidacao("CODIGO", "mensagem", false);
        Map<String, List<ErrosValidacao>> errosMap = Map.of("campo", List.of(erro));
        ResultadoValidacao resultado = new ResultadoValidacao(false, errosMap);

        assertEquals(errosMap, resultado.erros());
    }
}



