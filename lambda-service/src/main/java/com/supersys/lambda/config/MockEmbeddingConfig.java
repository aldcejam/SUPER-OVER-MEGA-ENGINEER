package com.supersys.lambda.config;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class MockEmbeddingConfig {

    @Bean
    public EmbeddingModel embeddingModel() {
        return new EmbeddingModel() {
            @Override
            public float[] embed(Document document) {
                return embed(document.getText());
            }

            @Override
            public float[] embed(String text) {
                return new float[384]; // Retorna array de float inicializado com zeros
            }

            @Override
            public EmbeddingResponse call(EmbeddingRequest request) {
                List<Embedding> embeddings = new ArrayList<>();
                List<String> instructions = request.getInstructions();
                for (int i = 0; i < instructions.size(); i++) {
                    float[] vector = embed(instructions.get(i));
                    embeddings.add(new Embedding(vector, i));
                }
                return new EmbeddingResponse(embeddings);
            }
        };
    }
}
