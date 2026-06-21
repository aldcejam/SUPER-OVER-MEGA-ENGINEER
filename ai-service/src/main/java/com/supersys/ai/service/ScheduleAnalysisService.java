package com.supersys.ai.service;

import com.supersys.ai.dto.ScheduleDto;
import com.supersys.ai.dto.ScheduleAnalysisResponseDto;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.stream.Collectors;

@Service
public class ScheduleAnalysisService {

    private final ChatModel chatModel;

    @Autowired
    public ScheduleAnalysisService(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    public ScheduleAnalysisResponseDto analyze(ScheduleDto schedule) {
        var outputConverter = new BeanOutputConverter<>(ScheduleAnalysisResponseDto.class);

        String stepsString = "Nenhum passo definido.";
        if (schedule.steps() != null && !schedule.steps().isEmpty()) {
            stepsString = schedule.steps().stream()
                    .map(step -> String.format("- Passo: %s (Seq: %d, Dias: %d, Concluído: %b) - %s",
                            step.stepName(),
                            step.sequence() != null ? step.sequence() : 0,
                            step.daysRequired() != null ? step.daysRequired() : 0,
                            step.completed() != null ? step.completed() : false,
                            step.description() != null ? step.description() : ""))
                    .collect(Collectors.joining("\n"));
        }

        String promptMessage = String.format(
                "Você é o assistente virtual da arquitetura distribuída SUPER-SYS.\n" +
                "Analise o seguinte cronograma recebido de project-analysis:\n\n" +

                "ID: %s\n" +
                "Título: %s\n" +
                "Data de Início: %s\n" +
                "Data de Fim: %s\n" +
                "Status Atual: %s\n" +
                "Detalhes: %s\n\n" +
                "Etapas/Passos:\n%s\n\n" +
                "Gere uma análise detalhada contendo status, resumo, pontos de risco identificados, sugestões de otimização e a duração estimada em dias.\n" +
                "A sua resposta deve seguir RIGOROSAMENTE o seguinte formato de saída:\n%s\n",
                schedule.id() != null ? schedule.id() : "N/A",
                schedule.title(),
                schedule.startDate() != null ? schedule.startDate() : "N/A",
                schedule.endDate() != null ? schedule.endDate() : "N/A",
                schedule.status() != null ? schedule.status() : "N/A",
                schedule.details() != null ? schedule.details() : "N/A",
                stepsString,
                outputConverter.getFormat()
        );

        ChatResponse chatResponse = this.chatModel.call(new Prompt(promptMessage));
        String responseText = chatResponse.getResult().getOutput().getText();

        return outputConverter.convert(responseText);
    }
}
