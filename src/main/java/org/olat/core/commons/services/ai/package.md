# AI Service

**Package:** `org.olat.core.commons.services.ai`

Pluggable multi-provider AI framework for OpenOlat. Uses a two-level abstraction: a Spring **service layer** that owns all feature-specific prompt construction and result handling, and an **SPI layer** that supplies only a configured `ChatModel` and a list of model names. Multiple providers (OpenAI, Anthropic, generic OpenAI-compatible endpoints) can be active simultaneously; each AI feature is independently mapped to a provider and model via the admin UI.

Key features:

- Multi-provider support (OpenAI, Anthropic Claude, generic OpenAI-compatible servers — vLLM, Ollama, LiteLLM, etc.)
- Per-feature provider + model configuration via `AiModule`
- AI-powered MC question generation from text input
- AI-powered image description generation (title, alt text, tags, keywords) with vision models
- AI-powered essay question generation from Markdown source (page editor, question pool, legacy drawer)
- AI-powered formative essay grading with structured feedback and XSS-sanitised student output
- Per-user, per-feature rate limiting enforced at the submit boundary
- Image preprocessing (scaling, base64 encoding) via `AiImageHelper`
- LangChain4j for chat model abstraction, structured output extraction, and model catalog APIs
- Singleton LangChain4j AiServices instances via `CachedChatModel`, rebuilt automatically when provider config changes
- AI usage logging to `o_ai_usage_log` with token counts, timing and error (generic, no feature-specific columns)
- Async persistent job execution via `TaskExecutorManager` + `o_ex_task` (cluster-aware crash recovery)

## Package Structure

| Directory | Purpose |
|-----------|---------|
| `ai/` (root) | Service interfaces, `AiModule`, `AiSPI`, `AiFeature`, usage-log interface and enums |
| `spi/` | Provider implementations: `OpenAiSPI`, `AnthropicAiSPI`, `GenericAiSPI` / `GenericAiSpiInstance` |
| `manager/` | Service implementations, `AiLoggingChatModel`, `CachedChatModel`, `AiUsageLogDAO` |
| `service/` | LangChain4j `@AiServices` declarative interfaces (prompt templates + structured output) |
| `model/` | Structured output POJOs, response wrappers, JPA entity, usage context value object |
| `essay/` | Essay grading + generation domain: services, jobs, sinks, file stores, POJOs, filters, exceptions |
| `content/` | `AiContentChunker`, `AiContentHardener` |
| `event/` | OLATResourceable events for config-change notification |
| `ui/` | Admin controllers for provider config, feature config, and usage log |

---

## 1. Class Overview

### 1.1 Root package

| Class | Responsibility |
|-------|---------------|
| `AiSPI` | Base interface every provider must implement. Identity, enable/disable, admin UI factory, chat model factory (`buildChatModel`), available models list. |
| `AiApiKeySPI` | Mixin for API-key-based providers. Enables the reusable `GenericAiApiKeyAdminController`. |
| `AiModule` | Central module. Merges Spring providers + generic instances. Stores per-feature provider/model config. Provides `resolveProvider()` for service implementations. Holds per-user rate limit thresholds (`getEssayGradingMaxCallsPerMinutePerUser`, `getEssayGenerationMaxCallsPerMinutePerUser`). |
| `AiFeature` | Enum of all AI features: `MCQuestionGenerator`, `ImageDescriptionGenerator`, `EssayGeneration`, `EssayGrading`. Used as type discriminator on usage-log rows. |
| `AiMCQuestionService` | Spring service interface for MC question generation. |
| `AiImageDescriptionService` | Spring service interface for image description generation. |
| `AiEssayGradingService` | Spring service interface for essay grading. Returns `GradingRun` (suggestion + usage-log key). |
| `AiEssayGenerationService` | Spring service interface for essay question generation. Returns `List<EssayItemDraft>`. |
| `AiUsageLog` | JPA interface for the `o_ai_usage_log` table. Holds context type/id, resource type/id, token counts, timing, status and error. Generic ledger, no feature-specific columns. |
| `AiUsageLogSearchParams` | Search parameter object for usage-log queries. |
| `AiUsageLogStatus` | Enum: `SUCCESS`, `ERROR`. |
| `AiImageHelper` | `@Service` that scales images to max 1024px and base64-encodes them for vision API calls. |

### 1.2 `spi/` — provider implementations

| Class | Responsibility |
|-------|---------------|
| `OpenAiSPI` | OpenAI provider. `@Service` extending `AbstractSpringModule`. Uses `OpenAiChatModel` and `OpenAiModelCatalog`. |
| `AnthropicAiSPI` | Anthropic Claude provider. Same pattern as `OpenAiSPI`. |
| `GenericAiSPI` | Factory/registry for generic OpenAI-compatible instances. Stores config as numbered properties. Not an `AiSPI` itself. |
| `GenericAiSpiInstance` | Single generic provider instance. Uses `OpenAiChatModel` with custom `baseUrl`. Created by `GenericAiSPI`. |

### 1.3 `manager/` — service implementations

| Class | Responsibility |
|-------|---------------|
| `AiMCQuestionServiceImpl` | MC question generation. Uses `MCQuestionAiService` (LangChain4j) via `CachedChatModel`. |
| `AiImageDescriptionServiceImpl` | Image description generation. Uses `ImageDescriptionAiService` (LangChain4j) via `CachedChatModel`. |
| `AiEssayGradingServiceImpl` | Essay grading. Resolves provider via `AiModule`, wraps the `ChatModel` in `AiLoggingChatModel`, invokes the `EssayGradingAiService` LangChain4j proxy. |
| `AiEssayGenerationServiceImpl` | Essay question generation. Same pattern; uses `EssayGenerationAiService`. |
| `AiUsageLogDAO` | DAO for `o_ai_usage_log`. Writes and queries usage rows; `createGuardLog` records non-LLM refusals. Exposes `countByIdentityFeatureSince` for rate-limit checks. Grading provenance lives on `o_ai_essay_correction`, not the log. |
| `AiLoggingChatModel` | Decorator that wraps any `ChatModel` and writes an `AiUsageLog` row on each call. |
| `CachedChatModel` | Immutable record caching a LangChain4j `AiServices` proxy keyed by (spiId, modelName). Rebuilt on config change. |
| `LangChain4jHttpClientBuilder` | Adapts OpenOlat's `HttpClientService` to LangChain4j's `HttpClientBuilder`. |

### 1.4 `service/` — LangChain4j AiService interfaces

| Class | Responsibility |
|-------|---------------|
| `MCQuestionAiService` | LangChain4j `AiServices` interface. Defines the MC question prompt via `@SystemMessage`/`@UserMessage`. Returns `List<MCQuestionData>`. |
| `ImageDescriptionAiService` | LangChain4j `AiServices` interface. Defines the image description prompt via `@SystemMessage`. Returns `ImageDescriptionData`. |
| `EssayGradingAiService` | LangChain4j `AiServices` interface for essay grading. Returns a structured `GradingSuggestion`. |
| `EssayGenerationAiService` | LangChain4j `AiServices` interface for essay question generation. Returns `List<EssayItemDraft>`. |

### 1.5 `model/` — structured output and response models

| Class | Responsibility |
|-------|---------------|
| `MCQuestionData` | Structured output for one MC question. LangChain4j `@Description` annotations guide extraction. |
| `ImageDescriptionData` | Structured output for image metadata. Same pattern as `MCQuestionData`. |
| `AiMCQuestionsResponse` | Response wrapper for MC question generation. Extends `AiResponse`. |
| `AiImageDescriptionResponse` | Response wrapper for image description. Extends `AiResponse`. |
| `AiResponse` | Base response class. Holds error string; `isSuccess()` returns true when error is null. |
| `AiUsageContext` | Value object carrying usage context type, identity, locale, resource type/id, and resource sub-id. Built via `AiUsageContext.builder()`. |
| `AiUsageLogImpl` | JPA entity implementing `AiUsageLog`. Maps to `o_ai_usage_log`. |
| `AiUsageLogStats` | Read-only projection for aggregated usage statistics. |

### 1.6 `essay/` — essay grading and generation domain

| Class | Responsibility |
|-------|---------------|
| `EssayFormativeFeedbackService` | Synchronous entry point for the formative-feedback flow. Loads `EssayAiGrading` from `ai-grading.json`, runs pre-filters, invokes the grader under a 30 s hard timeout, sanitises output via OpenOlat's XSS filter, persists essay provenance on the `AiUsageLog` row. |
| `EssayGenerationService` | Public entry point for AI question generation. Persists an `EssayGenerationJob`, schedules `EssayGenerationLongRunnable` on `TaskExecutorManager`, and dispatches accepted drafts to the configured sink. |
| `EssayGenerationService.GenerationRequest` | Record encapsulating a generation request. Factory methods: `forQuizPart` (page editor), `forPool` (question pool import), `of` (legacy drawer). |
| `EssayGenerationService.GenerationDestination` | Enum `DRAWER / QUIZ_PART / POOL`. Drives sink selection and usage-context labelling. |
| `EssayGenerationService.JobStatusView` | Read-only status record returned to the polling drawer UI. |
| `EssayFeedbackJobService` | Service coordinating async formative-feedback job lifecycle: submit, status, cancellation. |
| `EssayGenerationJob` | JPA entity (`o_essay_generation_job`). Tracks state (`PENDING / RUNNING / DONE / FAILED / CANCELLED`), progress JSON, and error JSON. |
| `EssayFeedbackJob` | JPA entity (`o_essay_feedback_job`) tracking async formative-feedback job state. |
| `EssayGenerationJobDao` | DAO for `EssayGenerationJob`. |
| `EssayFeedbackJobDao` | DAO for `EssayFeedbackJob`. |
| `EssayGenerationJobPayloadStore` | Stores and retrieves the serialised `GenerationRequest` payload for a job. |
| `EssayGenerationLongRunnable` | `PersistentTaskRunnable` scheduled on `TaskExecutorManager`. Delegates to `EssayGenerationService.runJob()`. Cluster-aware: commits on success, rolls back on failure. |
| `EssayFeedbackLongRunnable` | `PersistentTaskRunnable` for async formative-feedback jobs. |
| `EssayGenerationQuizPartSink` | Completion-hook sink for the `QUIZ_PART` destination. Attaches accepted drafts as QTI essay items to the page editor QuizPart; interleaves MC and essay questions. |
| `EssayGenerationPoolSink` | Completion-hook sink for the `POOL` destination. Persists accepted drafts and MC questions as standalone question-pool items owned by the requester. |
| `EssayAiGrading` | POJO holding grading metadata (reference excerpt, model answer, key points, rubric criteria, content hash, Bloom level). Written to / read from `ai-grading.json`. |
| `EssayAiGradingFileStore` | `@Service`. Reads and writes `ai-grading.json` next to the QTI essay item XML. Stable canonical JSON (sorted keys, ISO-8601 dates). |
| `AiSourceCompanion` | POJO for the AI-source companion file (`ai-source.json`). Holds source provenance for MC and source-only items. |
| `AiSourceCompanionFileStore` | `@Service`. Reads and writes `ai-source.json` next to the QTI item XML. |
| `EssayItemDraft` | Structured output record for one AI-generated essay question (question text, model answer, key points, rubric criteria, Bloom level, learning objective, token estimate). |
| `GradingSuggestion` | Structured output record for one grading result (content signals, language signals, off-topic flag, confidence, student feedback, coach feedback, overall assessment, estimated score percent). |
| `FormativeFeedback` | Result returned by `EssayFormativeFeedbackService.grade()`. Wraps tier, `GradingSuggestion`, warnings, and the usage-log key. |
| `AiGradingTier` | Enum `SHORT / MEDIUM / LONG`. Classifies student answers by word count (CJK-aware). Routes to different `max_tokens` caps and prompt template versions. |
| `AiBloomLevel` | Enum of Bloom taxonomy levels used when requesting essay generation. |
| `AiContentChunk` | Single content chunk produced by `AiContentChunker`. |
| `LengthPreFilter` | Rejects answers that are too short or too long before a provider call is made. |
| `GibberishPreFilter` | Blocks low-information text (repeated characters, random noise) before a provider call. |
| `LanguagePreFilter` | Warns (non-fatal) when the student answer language does not match the expected language. |
| `GeneratedItemValidator` | Validates an `EssayItemDraft` for required fields before the draft is accepted into a job's result set. |
| `AiRateLimitExceededException` | Thrown by `EssayFormativeFeedbackService.grade()` and `EssayGenerationService.submit()` when the per-user, per-minute quota is exceeded. |
| `AiEssayGradingException` | Unchecked exception for grading service failures. |
| `AiEssayGenerationException` | Unchecked exception for generation service failures. |
| `EssayGradingTimeoutException` | Thrown when the 30 s hard timeout expires. |
| `EssayGradingIntegrityException` | Thrown when the content hash on `EssayAiGrading` does not match a freshly computed hash. |
| `EssayGradingPreFilterException` | Thrown when a pre-filter rejects a student answer. |
| `RejectionReason` | Value record returned by pre-filters. Carries an i18n message key and a developer-facing detail string. |

### 1.7 `content/` — content preparation helpers

| Class | Responsibility |
|-------|---------------|
| `AiContentChunker` | CommonMark-aware Markdown chunker. Splits source text into semantically coherent `AiContentChunk` list for the generation pipeline. |
| `AiContentHardener` | Scrubs Markdown source for prompt-injection imperatives (`ignore previous instructions`, etc.), chat-template tokens (`<|im_start|>`, `[INST]`, etc.), and dangerous inline HTML (`javascript:` URLs, `<script>`, `<iframe>`) before the text is fed to the essay generator. |

---

## 2. Important Patterns

### 2.1 Provider SPI Abstraction

The SPI contract is deliberately thin: `AiSPI.buildChatModel(modelName, maxTokens)` returns a LangChain4j `ChatModel`, and `getAvailableModels()` returns the model name list. All feature-specific prompt construction, structured-output schema definition (via LangChain4j `@Description` annotations + `RESPONSE_FORMAT_JSON_SCHEMA` capability), and JSON validation live in the service and `service/` layers, never in the SPI. Adding a new provider requires no knowledge of any AI feature.

The `CachedChatModel` record holds a pre-built LangChain4j `AiServices` proxy keyed by (spiId, modelName). The proxy is rebuilt automatically when `AiModule` fires a config-change event.

### 2.2 Pre-filter Chain Pattern

Before any provider call, `EssayFormativeFeedbackService.grade()` runs three sequential filters. Order matters:

1. `LengthPreFilter` — rejects answers below or above the configured word-count bounds. Cheapest check, runs first.
2. `GibberishPreFilter` — blocks low-information text (repeated characters, random noise). Catches keyboard-spam that slips through the length gate.
3. `LanguagePreFilter` — non-fatal; warns when the detected language does not match the item's expected language. The warning surfaces in `FormativeFeedback.warnings()`.

Rejecting filters throw `EssayGradingPreFilterException` carrying a `RejectionReason` (i18n key + developer detail). The controller catches this and shows a localised message to the student without logging a usage row.

### 2.3 Tier-Based Prompt Routing

`AiGradingTier.classify(wordCount)` maps the student answer length (CJK-aware word count) to `SHORT`, `MEDIUM`, or `LONG`. Each tier routes to a different `max_tokens` cap and a different prompt template version string. The selected tier and prompt template version are stamped on the `o_ai_essay_correction` row (via `EssayFormativeFeedbackService.recordProvenance`), enabling retrospective analysis of grading quality per tier.

### 2.4 File-Based Companion Pattern

AI grading configuration and source provenance are stored as JSON files next to the QTI assessment item XML. `EssayAiGradingFileStore` writes `ai-grading.json`; `AiSourceCompanionFileStore` writes `ai-source.json`. Serialisation uses sorted map keys and ISO-8601 dates for stable, diff-friendly output.

Integrity is enforced by a SHA-256 prefix hash over the grading-relevant fields of `EssayAiGrading`, computed by `EssayFormativeFeedbackService.computeContentHash()`. A mismatch at grading time raises `EssayGradingIntegrityException` and refuses to grade, preventing grading against a tampered rubric.

`AssessmentItemAiGradingMarker` (in `org.olat.ims.qti21.model.xml`) injects a `<ooExt:aiGrading>` element into the QTI XML carrying the content hash, a stable `kitId`, generation timestamp, and schema version. The marker survives QTI export/import; on pool import the hash is re-verified before the marker is removed.

### 2.5 Persistent Job + LongRunnable Pattern

Both grading and generation use the OpenOlat persistent task infrastructure: a DB row is written first, then the job is registered on `TaskExecutorManager`, which persists it in `o_ex_task`. On cluster restart, unfinished tasks are picked up automatically. The `PersistentTaskRunnable` subclass manages the DB session lifecycle: commits on success, rolls back on failure, leaving the job in `FAILED` state with an error JSON payload.

### 2.6 Owner-Checked Status Access

`EssayGenerationService.getStatus(jobKey, Identity)` returns "not found" when the requesting identity does not match the job owner — the same response as for an unknown key. This prevents IDOR: an attacker who guesses a job key cannot determine whether it belongs to another user.

### 2.7 Per-User Rate Limiting

Before accepting a submit, both `EssayFormativeFeedbackService` and `EssayGenerationService` call `AiUsageLogDAO.countByIdentityFeatureSince(identity, feature, since)` to count calls within the rolling time window. Thresholds are stored in `AiModule`. Exceeding the limit throws `AiRateLimitExceededException`, which the controller translates to a localised error message.

### 2.8 Sanitisation Pipeline for Student-Facing Output

The AI provider response is passed through OpenOlat's built-in XSS filter with a restricted tag whitelist before storage or display. Only safe formatting tags are allowed; all script, iframe, and event-handler attributes are stripped.

### 2.9 Usage Logging with Destination-Aware Context

Every provider call writes a row to `o_ai_usage_log`. The `usageContextType` field encodes both the feature and the calling context (e.g. `ai-essay-correction`, `qpool-generate-questions`, `ceditor-quizpart-generate-questions`), and `usageContextId` points at the context entity that the call is about. For essay correction that entity is the `o_ai_essay_correction` row: `usageContextType = ai-essay-correction`, `usageContextId = <correction key>`, so all log rows of one correction (guard, grading, retry) share the same context id and the log stays a generic ledger with no feature-specific columns. The `resourceType` / `resourceId` pair identifies the resource: `RepositoryEntry` for course/page operations, `PoolQPool` for pool operations. The grading-run provenance (content hash, prompt template version, tier) lives on the `o_ai_essay_correction` row, not on the log; deleting a correction therefore drops its provenance while the cost ledger row survives (it keeps the now-stale context id, like any other soft reference).

---

## 3. Using the MC Question Generator

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

## 4. Using the Image Description Generator

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

## 5. Using the Essay Grading Service

The synchronous path is `EssayFormativeFeedbackService`. In practice the caller is `EssayFeedbackJobService` inside a `PersistentTaskRunnable`, but the direct API is also usable for testing.

```java
@Autowired
private EssayFormativeFeedbackService feedbackService;
@Autowired
private EssayAiGradingFileStore gradingStore;

// gradingDir is the VFS directory containing the QTI essay item XML
EssayAiGrading grading = gradingStore.load(gradingDir);
if (grading != null) {
    try {
        FormativeFeedback feedback = feedbackService.grade(
                grading, studentAnswer, itemSession, student, locale);
        // feedback.tier(), feedback.suggestion(), feedback.warnings()
    } catch (AiRateLimitExceededException e) {
        showError("ai.essay.feedback.error.ratelimit");
    } catch (EssayGradingTimeoutException e) {
        showError("ai.essay.feedback.error.timeout");
    } catch (EssayGradingIntegrityException e) {
        showError("ai.essay.feedback.error.integrity");
    }
}
```

## 6. Using the Essay Generation Service

The async path is `EssayGenerationService`. The caller builds a `GenerationRequest` using one of the factory methods and calls `submit()`, which returns a job key immediately. The caller polls `getStatus(jobKey, caller)` — the owner check is mandatory; a mismatching caller receives the same "not found" response as an unknown key.

```java
@Autowired
private EssayGenerationService essayGenerationService;

// Page editor (QuizPart) flow
GenerationRequest request = GenerationRequest.forQuizPart(
        pageMarkdown, repositoryEntryKey, locale, currentIdentity,
        pageKey, quizPartKey, 2 /* essay */, 2 /* mc */);
Long jobKey = essayGenerationService.submit(request);

// Poll for completion
EssayGenerationService.JobStatusView status =
        essayGenerationService.getStatus(jobKey, currentIdentity);
// status.state() == EssayGenerationJob.State.DONE / FAILED / RUNNING / PENDING

// Question pool flow
GenerationRequest poolRequest = GenerationRequest.forPool(
        sourceText, null, locale, currentIdentity, 3, 3, taxonomyLevelKey);
Long poolJobKey = essayGenerationService.submit(poolRequest);
```

When the job completes, `EssayGenerationQuizPartSink` (for `QUIZ_PART`) or `EssayGenerationPoolSink` (for `POOL`) is called automatically. For the legacy `DRAWER` destination, the caller handles the result directly.

## 7. Writing a Custom SPI

Extend `AbstractSpringModule` and implement `AiSPI`. If your provider uses an API key, also implement `AiApiKeySPI` to get the reusable `GenericAiApiKeyAdminController` for free. The Spring service layer handles all feature-specific prompt construction and response parsing — the SPI only needs to supply a `ChatModel` and the list of available model names.

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

## 8. Adding a New AI Feature

1. Define a LangChain4j AiService interface in `service/` with `@SystemMessage`/`@UserMessage` prompt templates and a structured return type
2. Define a structured output model in `model/` with LangChain4j `@Description` annotations
3. Define a response wrapper in `model/` extending `AiResponse` (synchronous features) or a `record` return type (async/streaming features)
4. Define a Spring service interface in the root package (follow `AiMCQuestionService` or `AiEssayGradingService` as a template)
5. Implement the service in `manager/` using `AiLoggingChatModel` wrapping `AiSPI.buildChatModel` and `AiServices.builder`
6. Add feature config properties and methods to `AiModule` (follow the essay pattern); add rate limit threshold properties if needed
7. Add a variant to the `AiFeature` enum
8. Add feature UI section to `AiFeaturesAdminController`

## 9. Configuration

Each `AbstractSpringModule` persists config to `{userdata}/system/configuration/{FQCN}.properties`.

| Module | Key properties |
|--------|---------------|
| `AiModule` | `ai.feature.mc-question-generator.spi`, `.model`, `ai.feature.image-description-generator.spi`, `.model`, `ai.feature.essay-generation.spi`, `.model`, `ai.feature.essay-grading.spi`, `.model`, `ai.essay.grading.rate.limit.per.user` (default 30/min), `ai.essay.generation.rate.limit.per.user` (default 10/min) |
| `OpenAiSPI` | `openai.api.key`, `openai.enabled` |
| `AnthropicAiSPI` | `anthropic.api.key`, `anthropic.enabled` |
| `GenericAiSPI` | `generic.instances=1,2`, `generic.{id}.name`, `.base.url`, `.api.key`, `.models`, `.enabled` |

Defaults in `olat.properties`: `ai.openai.enabled=false`, `ai.openai.api.key=`, `ai.anthropic.enabled=false`, `ai.anthropic.api.key=`

Rate limit defaults are hard-coded in `AiModule`; no admin UI exposure yet (TODO).

## 10. File Storage

Two JSON companion files live next to the QTI item XML in the assessment item directory:

| File | Store class | Contents |
|------|-------------|----------|
| `ai-grading.json` | `EssayAiGradingFileStore` | `EssayAiGrading` POJO: reference excerpt, model answer, key points, rubric criteria, content hash, Bloom level, generator provenance. |
| `ai-source.json` | `AiSourceCompanionFileStore` | `AiSourceCompanion` POJO: source provenance for MC and source-only items. |

The `EssayAiGrading` content hash is a SHA-256 prefix over the grading-relevant fields, computed by `EssayFormativeFeedbackService.computeContentHash()`. A mismatch at grading time raises `EssayGradingIntegrityException` and refuses to grade.

### 10.1 QTI Marker

`AssessmentItemAiGradingMarker` (in `org.olat.ims.qti21.model.xml`) injects a `<ooExt:aiGrading>` element into the QTI assessment item XML. The marker carries the content hash, a stable `kitId`, the generation timestamp, and the schema version.

- `inject(Document, Marker)` — called by the export pipeline after regenerating the marker.
- `extract(Document)` — called by the import pipeline to retrieve the marker.
- `remove(Document)` — strips the marker after it has been verified against the companion file.

The marker survives QTI export/import. On pool import the hash is re-verified before the marker is removed.

## 11. AI Usage Logging

Every provider call writes a row to `o_ai_usage_log` via `AiUsageLogDAO`. The row captures:

- `usageContextType` — e.g. `essay-grading`, `qpool-generate-questions`, `ceditor-quizpart-generate-questions`
- `resourceType` / `resourceId` — `RepositoryEntry` (course/page) or `PoolQPool` (pool)
- Token counts, duration, status, error
- The log carries no feature-specific columns; grading-run provenance (content hash, prompt template version, tier) lives on `o_ai_essay_correction`, linked from the log via `usageContextType=ai-essay-correction` + `usageContextId=<correction key>`

## 12. Admin UI

`AiAdminController` orchestrates:

1. **Add Provider dropdown** — OpenAI/Anthropic greyed out when already configured. Generic always available, multiple instances.
2. **Per-provider forms** — enable/disable toggle, config fields (API key, base URL, models), delete with confirmation dialog.
3. **Features form** (`AiFeaturesAdminController`) — per-feature provider + model selection. Models loaded live from provider API; free-text fallback when API unreachable. Test button for each feature.
4. **Usage log** (`AiUsageLogAdminController`) — tabular view of `o_ai_usage_log` rows.

| Controller | Purpose |
|------------|---------|
| `AiAdminController` | Main page, orchestrates all sections |
| `AiConfigurationAdminController` | Provider configuration panel |
| `GenericAiApiKeyAdminController` | Reusable form for API-key providers (OpenAI, Anthropic) |
| `GenericAiSpiAdminController` | Form for generic instances (base URL, models, optional API key) |
| `AiFeaturesAdminController` | Per-feature SPI + model config; enables/disables each AI use case |
| `AiFeaturesTestController` | Runs feature tests against a specific provider/model and shows results |
| `AiEssayGradingTestController` | Live test of essay grading structured output via admin UI |
| `AiEssayGenerationTestController` | Live test of essay generation structured output via admin UI |
| `AiUsageLogAdminController` | Usage log table with filters |

## 13. i18n Keys

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
| `ai.essay.feedback.error.ratelimit` | Rate limit exceeded error for essay grading |
| `ai.questions.error.ratelimit` | Rate limit exceeded error for question generation |
| `ai.essay.feedback.error.timeout` | Grading hard-timeout exceeded |
| `ai.essay.feedback.error.integrity` | Content hash mismatch |
