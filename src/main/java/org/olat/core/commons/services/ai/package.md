# AI Service

**Package:** `org.olat.core.commons.services.ai`

Pluggable framework for integrating generative AI into OpenOlat. Uses the
Service Provider Interface (SPI) pattern: the core defines interfaces and a
feature registry, concrete providers implement the chat model factory. Multiple
providers can be active simultaneously. Each AI feature is independently
mapped to a provider and model via the admin UI.

Key features:

- Multi-provider support (OpenAI, Anthropic Claude, generic OpenAI-compatible servers)
- Per-feature provider + model configuration
- AI-powered MC question generation from text input
- AI-powered image description generation (title, alt text, tags, keywords) with vision models
- Generic providers for self-hosted servers (vLLM, Ollama, LiteLLM, etc.) — added dynamically, multiple instances
- Image preprocessing (scaling, base64 encoding) via `AiImageHelper`
- LangChain4j for chat model abstraction, structured output extraction, and model catalog APIs
- Singleton LangChain4j AiServices instances via `CachedChatModel`, rebuilt automatically when provider config changes

## 1. Class Overview

| Class | Responsibility |
|-------|---------------|
| `AiSPI` | Base interface every provider must implement. Identity, enable/disable, admin UI factory, chat model factory (`buildChatModel`), available models list. |
| `AiApiKeySPI` | Mixin for API-key-based providers. Enables the reusable `GenericAiApiKeyAdminController`. |
| `OpenAiSPI` | OpenAI provider. `@Service` extending `AbstractSpringModule`. Uses `OpenAiChatModel` and `OpenAiModelCatalog`. |
| `AnthropicAiSPI` | Anthropic Claude provider. Same pattern as `OpenAiSPI`. |
| `GenericAiSPI` | Factory/registry for generic OpenAI-compatible instances. Stores config as numbered properties. Not an `AiSPI` itself. |
| `GenericAiSpiInstance` | Single generic provider instance. Uses `OpenAiChatModel` with custom `baseUrl`. Created by `GenericAiSPI`. |
| `AiModule` | Central module. Merges Spring providers + generic instances. Stores per-feature provider/model config. Provides `resolveProvider()` for service implementations. |
| `AiMCQuestionService` | Spring service interface for MC question generation. |
| `AiMCQuestionServiceImpl` | Implementation using `MCQuestionAiService` (LangChain4j) via `CachedChatModel`. |
| `AiImageDescriptionService` | Spring service interface for image description generation. |
| `AiImageDescriptionServiceImpl` | Implementation using `ImageDescriptionAiService` (LangChain4j) via `CachedChatModel`. |
| `MCQuestionAiService` | LangChain4j `AiServices` interface. Defines the MC question prompt via `@SystemMessage`/`@UserMessage`. Returns `List<MCQuestionData>`. |
| `ImageDescriptionAiService` | LangChain4j `AiServices` interface. Defines the image description prompt via `@SystemMessage`. Returns `ImageDescriptionData`. |
| `CachedChatModel` | Immutable record caching a LangChain4j `AiServices` proxy keyed by (spiId, modelName). Rebuilt on config change. |
| `MCQuestionData` | Structured output model for one MC question. LangChain4j `@Description` annotations guide the AI response extraction. |
| `ImageDescriptionData` | Structured output model for image metadata. Same pattern as `MCQuestionData`. |
| `AiMCQuestionsResponse` | Response wrapper for MC question generation results. Extends `AiResponse`. |
| `AiImageDescriptionResponse` | Response wrapper for image description results. Extends `AiResponse`. |
| `AiResponse` | Base response class. Holds an error string; `isSuccess()` returns true when error is null. |
| `AiImageHelper` | `@Service` that scales images to max 1024px and base64-encodes them for vision API calls. |
| `LangChain4jHttpClientBuilder` | Adapts OpenOlat's `HttpClientService` to LangChain4j's `HttpClientBuilder`. |

## 2. Using the MC Question Generator

```java
@Autowired
private AiMCQuestionService aiMCQuestionService;

if (aiMCQuestionService.isEnabled()) {
    showAiButton();
}

AiMCQuestionsResponse response = aiMCQuestionService.generateMCQuestionsResponse(inputText, 5);
if (response.isSuccess()) {
    for (MCQuestionData q : response.getQuestions()) {
        // q.getTitle(), q.getQuestion(), q.getCorrectAnswers(), q.getWrongAnswers()
    }
} else {
    showError(response.getError());
}
```

## 3. Using the Image Description Generator

```java
@Autowired
private AiImageDescriptionService aiImageDescriptionService;
@Autowired
private AiImageHelper aiImageHelper;

if (aiImageDescriptionService.isEnabled()) {
    showAiButton();
}

String base64 = aiImageHelper.prepareImageBase64(imageFile, "jpg");
String mimeType = aiImageHelper.getMimeType("jpg");
if (base64 != null && mimeType != null) {
    AiImageDescriptionResponse response = aiImageDescriptionService.generateImageDescription(base64, mimeType, locale);
    if (response.isSuccess()) {
        ImageDescriptionData data = response.getDescription();
        // data.getTitle(), data.getDescription(), data.getAltText()
        // data.getColorTags(), data.getCategoryTags(), data.getKeywords()
    } else {
        showError(response.getError());
    }
}
```

`AiImageHelper` handles scaling (max 1024px) and base64 encoding. Supports JPEG, PNG, GIF, and WebP.

## 4. Writing a Custom SPI

Extend `AbstractSpringModule` and implement `AiSPI`. If your provider uses an
API key, also implement `AiApiKeySPI` to get the reusable
`GenericAiApiKeyAdminController` for free. The Spring service layer handles
all feature-specific prompt construction and response parsing — the SPI only
needs to supply a `ChatModel` and the list of available model names.

```java
@Service
public class MyAiSPI extends AbstractSpringModule implements AiSPI, AiApiKeySPI {

    private static final String MY_API_KEY = "myai.api.key";
    private static final String MY_ENABLED = "myai.enabled";

    private String apiKey;
    private boolean enabled;

    @Autowired
    public MyAiSPI(CoordinatorManager coordinatorManager) {
        super(coordinatorManager);
    }

    @Override public void init() {
        apiKey = getStringPropertyValue(MY_API_KEY, apiKey);
        enabled = getBooleanPropertyValue(MY_ENABLED);
    }

    @Override protected void initFromChangedProperties() { init(); }

    @Override public String getId() { return "MyAI"; }
    @Override public String getName() { return "My AI Provider"; }
    @Override public boolean isEnabled() { return enabled; }
    @Override public void setEnabled(boolean e) { enabled = e; setBooleanProperty(MY_ENABLED, e, true); }
    @Override public Controller createAdminController(UserRequest ureq, WindowControl wc) {
        return new GenericAiApiKeyAdminController(ureq, wc, this);
    }

    @Override
    public ChatModel buildChatModel(String modelName, int maxTokens) {
        return OpenAiChatModel.builder()
                .httpClientBuilder(new LangChain4jHttpClientBuilder())
                .apiKey(apiKey)
                .modelName(modelName)
                .maxCompletionTokens(maxTokens)
                .supportedCapabilities(Set.of(Capability.RESPONSE_FORMAT_JSON_SCHEMA))
                .build();
    }

    @Override
    public List<String> getAvailableModels() {
        return List.of("my-model-v1", "my-model-v2");
    }

    // AiApiKeySPI
    @Override public String getApiKey() { return apiKey; }
    @Override public void setApiKey(String k) { apiKey = k; setStringProperty(MY_API_KEY, k, true); }
    @Override public List<String> verifyApiKey(String k) throws Exception {
        return List.of("my-model-v1", "my-model-v2");
    }
    @Override public String getAdminTitleI18nKey() { return "ai.myai.title"; }
    @Override public String getAdminDescI18nKey() { return "ai.myai.desc"; }
    @Override public String getAdminApiKeyI18nKey() { return "ai.myai.apikey"; }
}
```

## 5. Adding a New AI Feature

1. Define a LangChain4j AiService interface in `service/` with `@SystemMessage`/`@UserMessage` prompt templates and a structured return type
2. Define a structured output model in `model/` with LangChain4j `@Description` annotations
3. Define a response wrapper in `model/` extending `AiResponse`
4. Define a Spring service interface in the root package (follow `AiMCQuestionService` as a template)
5. Implement the service in `manager/` using `CachedChatModel` for singleton LangChain4j proxy management
6. Add feature config properties and methods to `AiModule` (follow the MC generator pattern)
7. Add feature UI section to `AiFeaturesAdminController`

## 6. Configuration

Each `AbstractSpringModule` persists config to `{userdata}/system/configuration/{FQCN}.properties`.

| Module | Key properties |
|--------|---------------|
| `AiModule` | `ai.feature.mc-question-generator.spi`, `.model`, `ai.feature.image-description-generator.spi`, `.model` |
| `OpenAiSPI` | `openai.api.key`, `openai.enabled` |
| `AnthropicAiSPI` | `anthropic.api.key`, `anthropic.enabled` |
| `GenericAiSPI` | `generic.instances=1,2`, `generic.{id}.name`, `.base.url`, `.api.key`, `.models`, `.enabled` |

Defaults in `olat.properties`: `ai.openai.enabled=false`, `ai.openai.api.key=`, `ai.anthropic.enabled=false`, `ai.anthropic.api.key=`

## 7. Admin UI

`AiAdminController` is a `BasicController` that orchestrates:

1. **Add Provider dropdown** — OpenAI/Anthropic greyed out when already configured. Generic always available, multiple instances.
2. **Per-provider forms** — Enable/disable toggle, config fields (API key, base URL, models), delete with confirmation dialog.
3. **Features form** (`AiFeaturesAdminController`) — Per-feature provider + model selection. Models loaded live from provider API; free-text fallback when API unreachable. Test button for each feature.

| Controller | Purpose |
|------------|---------|
| `AiAdminController` | Main page, orchestrates all sections |
| `AddProviderFormController` | Inner form with dropdown menu |
| `GenericAiApiKeyAdminController` | Reusable form for API-key providers (OpenAI, Anthropic) |
| `GenericAiSpiAdminController` | Form for generic instances (base URL, models, optional API key) |
| `AiFeaturesAdminController` | Per-feature SPI + model config |
| `AiFeaturesTestController` | Runs feature tests against a specific provider/model and shows results |

## 8. i18n Keys

| Key | Usage |
|-----|-------|
| `ai.apikey.check` | "Check API key" button |
| `ai.apikey.not.set` | Warning when API key is missing ({0} = provider name) |
| `ai.apikey.verify.success` | Success message ({0} = provider, {1} = model count) |
| `ai.apikey.verify.error` | Error message ({0} = provider name) |
| `ai.spi.enabled` | Enable/disable toggle label |
| `ai.add.provider` | "Add AI provider" dropdown button |
| `ai.delete.config` | "Delete configuration" link |
| `ai.delete.confirm.title/text` | Delete confirmation dialog |
| `ai.features.title/desc` | Features section heading |
| `ai.feature.spi` / `ai.feature.model` | Provider/model dropdown labels |
| `ai.generic.*` | Generic provider form labels |
