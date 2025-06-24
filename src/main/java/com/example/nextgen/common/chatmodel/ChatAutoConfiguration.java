package com.example.nextgen.common.chatmodel;

import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.observation.ChatModelObservationConvention;
import org.springframework.ai.model.SimpleApiKey;
import org.springframework.ai.model.openai.autoconfigure.OpenAIAutoConfigurationUtil;
import org.springframework.ai.model.openai.autoconfigure.OpenAiChatProperties;
import org.springframework.ai.model.openai.autoconfigure.OpenAiConnectionProperties;
import org.springframework.ai.model.tool.DefaultToolExecutionEligibilityPredicate;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionEligibilityPredicate;
import org.springframework.ai.model.tool.autoconfigure.ToolCallingAutoConfiguration;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.retry.autoconfigure.SpringAiRetryAutoConfiguration;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Objects;

@AutoConfiguration(
        after = {RestClientAutoConfiguration.class, WebClientAutoConfiguration.class, SpringAiRetryAutoConfiguration.class, ToolCallingAutoConfiguration.class}
)
@ConditionalOnClass({OpenAiApi.class})
@EnableConfigurationProperties({OpenAiConnectionProperties.class, OpenAiChatProperties.class})
@ConditionalOnProperty(
        name = {"spring.ai.model.chat"},
        havingValue = "openai",
        matchIfMissing = true
)
@ImportAutoConfiguration(
        classes = {SpringAiRetryAutoConfiguration.class, RestClientAutoConfiguration.class, WebClientAutoConfiguration.class, ToolCallingAutoConfiguration.class}
)
public class ChatAutoConfiguration {
    @Bean
    @Primary
    public ChatModel qwenPlusChatModel(OpenAiConnectionProperties commonProperties, OpenAiChatProperties chatProperties, ObjectProvider<RestClient.Builder> restClientBuilderProvider, ObjectProvider<WebClient.Builder> webClientBuilderProvider, ToolCallingManager toolCallingManager, RetryTemplate retryTemplate, ResponseErrorHandler responseErrorHandler, ObjectProvider<ObservationRegistry> observationRegistry, ObjectProvider<ChatModelObservationConvention> observationConvention, ObjectProvider<ToolExecutionEligibilityPredicate> openAiToolExecutionEligibilityPredicate) {
        OpenAiChatOptions copy = chatProperties.getOptions().copy();
        copy.setModel("qwen-plus");
        OpenAiApi openAiApi = this.openAiApi(chatProperties, commonProperties, restClientBuilderProvider.getIfAvailable(RestClient::builder), webClientBuilderProvider.getIfAvailable(WebClient::builder), responseErrorHandler, "chat");
        OpenAiChatModel chatModel = OpenAiChatModel.builder().openAiApi(openAiApi).defaultOptions(chatProperties.getOptions()).toolCallingManager(toolCallingManager).toolExecutionEligibilityPredicate(openAiToolExecutionEligibilityPredicate.getIfUnique(DefaultToolExecutionEligibilityPredicate::new)).retryTemplate(retryTemplate).observationRegistry((ObservationRegistry)observationRegistry.getIfUnique(() -> ObservationRegistry.NOOP)).build();
        Objects.requireNonNull(chatModel);
        observationConvention.ifAvailable(chatModel::setObservationConvention);
        return chatModel;
    }

    @Bean
    public ChatModel qwenMaxChatModel(OpenAiConnectionProperties commonProperties, OpenAiChatProperties chatProperties, ObjectProvider<RestClient.Builder> restClientBuilderProvider, ObjectProvider<WebClient.Builder> webClientBuilderProvider, ToolCallingManager toolCallingManager, RetryTemplate retryTemplate, ResponseErrorHandler responseErrorHandler, ObjectProvider<ObservationRegistry> observationRegistry, ObjectProvider<ChatModelObservationConvention> observationConvention, ObjectProvider<ToolExecutionEligibilityPredicate> openAiToolExecutionEligibilityPredicate) {
        OpenAiChatOptions copy = chatProperties.getOptions().copy();
        copy.setModel("qwen-max");
        OpenAiApi openAiApi = this.openAiApi(chatProperties, commonProperties, restClientBuilderProvider.getIfAvailable(RestClient::builder), webClientBuilderProvider.getIfAvailable(WebClient::builder), responseErrorHandler, "chat");
        OpenAiChatModel chatModel = OpenAiChatModel.builder().openAiApi(openAiApi).defaultOptions(copy).toolCallingManager(toolCallingManager).toolExecutionEligibilityPredicate(openAiToolExecutionEligibilityPredicate.getIfUnique(DefaultToolExecutionEligibilityPredicate::new)).retryTemplate(retryTemplate).observationRegistry((ObservationRegistry)observationRegistry.getIfUnique(() -> ObservationRegistry.NOOP)).build();
        Objects.requireNonNull(chatModel);
        observationConvention.ifAvailable(chatModel::setObservationConvention);
        return chatModel;
    }

    private OpenAiApi openAiApi(OpenAiChatProperties chatProperties, OpenAiConnectionProperties commonProperties, RestClient.Builder restClientBuilder, WebClient.Builder webClientBuilder, ResponseErrorHandler responseErrorHandler, String modelType) {
        OpenAIAutoConfigurationUtil.ResolvedConnectionProperties resolved = OpenAIAutoConfigurationUtil.resolveConnectionProperties(commonProperties, chatProperties, modelType);
        return OpenAiApi.builder().baseUrl(resolved.baseUrl()).apiKey(new SimpleApiKey(resolved.apiKey())).headers(resolved.headers()).completionsPath(chatProperties.getCompletionsPath()).embeddingsPath("/v1/embeddings").restClientBuilder(restClientBuilder).webClientBuilder(webClientBuilder).responseErrorHandler(responseErrorHandler).build();
    }
}
