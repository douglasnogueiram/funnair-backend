/*
 * Copyright 2024-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.spring.demo.ai.playground.services;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;

import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.annotation.Observed;
import reactor.core.publisher.Flux;

import java.time.LocalDate;

@Service
public class CustomerSupportAssistant {

	private final ChatClient chatClient;
	private final AgentPromptService agentPromptService;

	// @formatter:off
	public CustomerSupportAssistant(
		ChatModel chatModel,
		ChatMemory chatMemory,
		BookingTools bookingTools,
		SyncMcpToolCallbackProvider mcpTools,
		VectorStore vectorStore,
		ObservationRegistry registry,
		AgentPromptService agentPromptService
	) {
		this.agentPromptService = agentPromptService;
		this.chatClient = ChatClient.builder(chatModel, registry, null, null)
				.defaultAdvisors(
					MessageChatMemoryAdvisor.builder(chatMemory).build(),
					QuestionAnswerAdvisor.builder(vectorStore).build()
				)
				.defaultTools(bookingTools)
				.defaultToolCallbacks(mcpTools)
				.build();
	}

	@Observed(name = "agente.viagem", contextualName = "agente-viagem")
	public Flux<String> chat(String chatId, String userMessage, Object... additionalTools) {
		String systemPrompt = agentPromptService.getActivePrompt();
		return this.chatClient.prompt()
			.system(s -> s.text(systemPrompt).param("current_date", LocalDate.now().toString()))
			.user(userMessage)
			.tools(additionalTools)
			.advisors(a -> a.param(ChatMemory.CONVERSATION_ID, chatId))
			.stream()
			.content()
			.onErrorResume(e -> {
				return Flux.just("Desculpe, estou enfrentando dificuldades técnicas no momento. Por favor, tente novamente mais tarde.");
			});
	}

}