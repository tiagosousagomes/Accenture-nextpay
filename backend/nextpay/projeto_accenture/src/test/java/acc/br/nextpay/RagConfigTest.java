package acc.br.nextpay;

import acc.br.nextpay.config.RagConfig;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class RagConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(RagConfig.class);

    @BeforeEach
    void setUp() throws IOException {
        // Garante que o diretório e o arquivo existam para o teste não quebrar no loadDocument
        Path path = Path.of("src/main/resources/business_rules.txt");
        if (!Files.exists(path)) {
            Files.createDirectories(path.getParent());
            Files.writeString(path, "Regra de teste: O sistema deve aceitar pagamentos.");
        }
    }

    @Test
    void deveCarregarBeansDoRagComSucesso() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(EmbeddingModel.class);
            assertThat(context).hasSingleBean(EmbeddingStore.class);
            assertThat(context).hasSingleBean(ContentRetriever.class);

            EmbeddingModel model = context.getBean(EmbeddingModel.class);
            Assertions.assertNotNull(model, "O modelo de embedding deve ser inicializado");
        });
    }

    @Test
    void deveGerarEmbeddingsCorretamente() {
        RagConfig config = new RagConfig();
        EmbeddingModel model = config.embeddingModel();

        var response = model.embed("Teste de NextPay");

        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.content());
        // O AllMiniLmL6V2 gera vetores de 384 dimensões
        Assertions.assertEquals(384, response.content().dimension());
    }
}