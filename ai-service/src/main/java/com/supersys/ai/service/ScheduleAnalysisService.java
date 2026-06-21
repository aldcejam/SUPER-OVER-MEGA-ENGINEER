package com.supersys.ai.service;

import com.supersys.ai.dto.ScheduleDto;
import com.supersys.ai.dto.ScheduleAnalysisResponseDto;
import com.supersys.ai.dto.ProjectDto;
import com.supersys.ai.dto.ProjectAnalysisResponseDto;
import com.supersys.ai.dto.ResourceAnalysisResponseDto;
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

    public ProjectAnalysisResponseDto analyzeProject(ProjectDto project) {
        var outputConverter = new BeanOutputConverter<>(ProjectAnalysisResponseDto.class);

        String scheduleInfo = "Nenhum cronograma definido.";
        if (project.schedule() != null) {
            String stepsString = "Nenhum passo definido.";
            if (project.schedule().steps() != null && !project.schedule().steps().isEmpty()) {
                stepsString = project.schedule().steps().stream()
                        .map(step -> String.format("- %s (Seq: %d, Dias: %d, Concluído: %b)",
                                step.stepName(), step.sequence(), step.daysRequired(), step.completed()))
                        .collect(Collectors.joining("\n"));
            }
            scheduleInfo = String.format("Título: %s\nDetalhes: %s\nPassos:\n%s", 
                    project.schedule().title(), project.schedule().details(), stepsString);
        }

        String promptMessage = String.format(
                "Você é o assistente virtual da arquitetura distribuída SUPER-SYS.\n" +
                "Analise a viabilidade e escopo do seguinte projeto:\n\n" +
                "ID: %s\n" +
                "Nome do Projeto: %s\n" +
                "Descrição: %s\n" +
                "Orçamento: R$ %.2f\n" +
                "Status: %s\n\n" +
                "Cronograma Relacionado:\n%s\n\n" +
                "Gere uma análise detalhada contendo nota de viabilidade geral (Alta/Média/Baixa), riscos de orçamento/estouro de custos, resumo da viabilidade e recomendações estratégicas.\n" +
                "A sua resposta deve seguir RIGOROSAMENTE o seguinte formato de saída:\n%s\n",
                project.id() != null ? project.id() : "N/A",
                project.name(),
                project.description() != null ? project.description() : "N/A",
                project.budget() != null ? project.budget() : 0.0,
                project.status() != null ? project.status() : "N/A",
                scheduleInfo,
                outputConverter.getFormat()
        );

        ChatResponse chatResponse = this.chatModel.call(new Prompt(promptMessage));
        String responseText = chatResponse.getResult().getOutput().getText();

        return outputConverter.convert(responseText);
    }

    public ResourceAnalysisResponseDto analyzeResources(ProjectDto project) {
        var outputConverter = new BeanOutputConverter<>(ResourceAnalysisResponseDto.class);

        String allocationsString = "Nenhum recurso alocado.";
        if (project.allocations() != null && !project.allocations().isEmpty()) {
            allocationsString = project.allocations().stream()
                    .map(alloc -> String.format("- Recurso: %s | Cargo/Função: %s | Horas/Semana: %d | Custo/Hora: R$ %.2f | Qtd: %d",
                            alloc.resourceName(), alloc.role(), alloc.hoursPerWeek(), alloc.costPerHour(), alloc.quantity()))
                    .collect(Collectors.joining("\n"));
        }

        String promptMessage = String.format(
                "Você é o assistente virtual da arquitetura distribuída SUPER-SYS.\n" +
                "Analise as alocações de equipe e identifique possíveis gargalos e sobrecargas no seguinte projeto:\n\n" +
                "ID: %s\n" +
                "Nome do Projeto: %s\n" +
                "Orçamento: R$ %.2f\n\n" +
                "Recursos Alocados:\n%s\n\n" +
                "Gere uma análise de alocação de recursos contendo taxa de utilização geral da equipe, gargalos/cargos sobrecarregados detectados, eficiência de custos e sugestões para otimizar a distribuição do trabalho.\n" +
                "A sua resposta deve seguir RIGOROSAMENTE o seguinte formato de saída:\n%s\n",
                project.id() != null ? project.id() : "N/A",
                project.name(),
                project.budget() != null ? project.budget() : 0.0,
                allocationsString,
                outputConverter.getFormat()
        );

        ChatResponse chatResponse = this.chatModel.call(new Prompt(promptMessage));
        String responseText = chatResponse.getResult().getOutput().getText();

        return outputConverter.convert(responseText);
    }
}

