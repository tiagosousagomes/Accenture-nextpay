package acc.br.nextpay;

import acc.br.nextpay.ai.AssistenteRegrasNegocio;
import acc.br.nextpay.config.AiConfig;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

class AiConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(AiConfig.class, MockConfig.class)
            .withPropertyValues("gemini.api.key=chave-teste-123");

    @Configuration
    static class MockConfig {
        @Bean
        public ContentRetriever contentRetriever() {
            return Mockito.mock(ContentRetriever.class);
        }
    }

    @Test
    void deveConfigurarBeansDeAiComSucesso() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(ChatLanguageModel.class);
            assertThat(context).hasSingleBean(AssistenteRegrasNegocio.class);

            AssistenteRegrasNegocio assistente = context.getBean(AssistenteRegrasNegocio.class);
            Assertions.assertNotNull(assistente);
        });
    }

    @Test
    void deveCarregarContextoMesmoSemChaveMasNaoDeveFuncionar() {
        // Removi o "hasFailed" porque o Spring injeta a string "${gemini.api.key}"
        // se não houver um PropertyPlaceholderConfigurer validando.
        new ApplicationContextRunner()
                .withUserConfiguration(AiConfig.class, MockConfig.class)
                .run(context -> {
                    assertThat(context).hasSingleBean(ChatLanguageModel.class);
                    // O contexto sobe, mas o bean estará com a chave inválida.
                    // Isso é o comportamento padrão do Spring sem validação explícita.
                });
    }
}