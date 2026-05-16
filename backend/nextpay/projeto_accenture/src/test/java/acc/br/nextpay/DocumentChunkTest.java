package acc.br.nextpay;

import acc.br.nextpay.ai.DocumentChunk;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import java.util.List;

class DocumentChunkTest {

    @Test
    void deveGarantirAIntegridadeDoRecord() {
        // GIVEN
        String conteudoEsperado = "Este é um fragmento de documento de teste para a IA.";
        List<Float> embeddingEsperado = List.of(0.1f, -0.5f, 0.99f);

        // WHEN (Testa o construtor gerado automaticamente)
        DocumentChunk chunk = new DocumentChunk(conteudoEsperado, embeddingEsperado);

        // THEN (Testa os métodos de leitura implícitos)
        Assertions.assertNotNull(chunk);
        Assertions.assertEquals(conteudoEsperado, chunk.content());
        Assertions.assertEquals(embeddingEsperado, chunk.embedding());
    }

    @Test
    void deveCobrirMetodosEstruturaisDoRecord() {
        // Cria instâncias para testar os métodos equals, hashCode e toString gerados pelo compilador
        List<Float> embedding = List.of(1.0f, 2.0f);
        DocumentChunk chunk1 = new DocumentChunk("texto", embedding);
        DocumentChunk chunk2 = new DocumentChunk("texto", embedding);
        DocumentChunk chunkDiferente = new DocumentChunk("outro texto", embedding);

        // Testando Equals e HashCode
        Assertions.assertEquals(chunk1, chunk2);
        Assertions.assertNotEquals(chunk1, chunkDiferente);
        Assertions.assertEquals(chunk1.hashCode(), chunk2.hashCode());

        // Testando ToString (Garante cobertura total das ramificações de strings do record)
        String toStringResult = chunk1.toString();
        Assertions.assertTrue(toStringResult.contains("texto"));
    }
}