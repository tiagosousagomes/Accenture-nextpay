package acc.br.nextpay.config;

import acc.br.nextpay.ai.AssistenteRegrasNegocio;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Value; // IMPORT CORRIGIDO AQUI
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Bean
    public ChatLanguageModel chatLanguageModel() {
        return GoogleAiGeminiChatModel.builder()
                .apiKey(apiKey)
                .modelName("gemini-2.5-flash")
                .build();
    }

    @Bean
    public AssistenteRegrasNegocio assistenteRegrasNegocio(
            ChatLanguageModel chatLanguageModel,
            ContentRetriever contentRetriever
    ) {
        return AiServices.builder(AssistenteRegrasNegocio.class)
                .chatLanguageModel(chatLanguageModel)
                .contentRetriever(contentRetriever) // Conexão com o RagConfig
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .build();
    }
}