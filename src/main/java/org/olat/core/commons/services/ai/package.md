# AI Service

**Package:** `org.olat.core.commons.services.ai`

Pluggable multi-provider AI framework for OpenOlat. Uses a two-level abstraction: a Spring **service layer** that owns all feature-specific prompt construction and result handling, and an **SPI layer** that supplies only a configured `ChatModel` and a list of model names. Multiple providers (OpenAI, Anthropic, generic OpenAI-compatible endpoints) can be active simultaneously; each AI feature is independently mapped to a provider and model via the admin UI.

Key features:

- Multi-provider support (OpenAI, Anthropic Claude, generic OpenAI-compatible servers — vLLM, Ollama, LiteLLM, etc.)
- Embedding models for taxonomy matching through `AiEmbeddingSPI`: OpenAI, generic instances, and the in-process `LocalOnnxSPI` (uploaded ONNX model)
- Per-feature provider + model configuration via `AiModule`
- AI-powered MC question generation from text input
- AI-powered image description generation (title, alt text, tags, keywords) with vision models; media metadata enrichment runs asynchronously via `MediaAiMetadataService` (in `org.olat.modules.cemedia.manager`), one persisted task per image
- AI-powered essay question generation from Markdown source (page editor, question pool, legacy drawer)
- AI-powered formative essay grading with structured feedback and XSS-sanitised student output
- Per-user, per-feature rate limiting enforced at the submit boundary
- Per-feature output token cap, provider timeout and input size limit, configurable in the AI features form
- Two dedicated task pools (`aiInteractive`, `aiBatch`) with configurable size; an overloaded interactive pool refuses new essay corrections
- **New in 21.1.0:** User control of the automatic features: a system default per feature plus a personal choice in the user settings, enforced at three gates (see section 2.10)
- **New in 21.1.0:** Read-only AI administration for a person without the administrator or system administrator role (see section 12)
- **New in 21.1.0:** Provider calls use the `CONFIGURED` HTTP protection profile, so the SSRF filter does not block an administrator-configured provider URL (see section 2.11)
- Image preprocessing (scaling, base64 encoding) via `AiImageHelper`
- LangChain4j for chat model abstraction, structured output extraction, and model catalog APIs
- Singleton LangChain4j AiServices instances via `CachedChatModel`, rebuilt automatically when provider config changes
- AI usage logging to `o_ai_usage_log` with token counts, timing and error (generic, no feature-specific columns)
- Async persistent job execution via `TaskExecutorManager` + `o_ex_task` (cluster-aware crash recovery)

## Package Structure

| Directory | Purpose |
|-----------|---------|
| `ai/` (root) | Service interfaces, `AiModule`, `AiSPI`, `AiEmbeddingSPI`, `AiFeature`, usage-log interface and enums |
| `spi/` | Provider implementations in sub-packages: `openAI/OpenAiSPI`, `anthropic/AnthropicAiSPI`, `generic/GenericAiSPI` + `GenericAiSpiInstance` + `GenericAiSpiAdminController`, `localonnx/LocalOnnxSPI` + `QwenOnnxEmbeddingModel` + `ui/LocalOnnxModelAdminController` |
| `manager/` | Service implementations, `AiLoggingChatModel`, `AiLoggingEmbeddingModel`, `CachedChatModel`, `AiUsageLogDAO`, `AiTaskExecutorService`, HTTP client adapters |
| `service/` | LangChain4j `@AiServices` declarative interfaces (prompt templates + structured output) |
| `model/` | Structured output POJOs, response wrappers, JPA entity, usage context value object |
| `essay/` | Essay grading + generation domain: services, jobs, sinks, file stores, POJOs, filters, exceptions |
| `content/` | `AiContentChunker`, `AiContentHardener` |
| `event/` | `AiQuestionItemsCreatedEvent`, `AiServiceFailedEvent`: event classes from the first MC generator version. No code outside the package uses them. |
| `ui/` | Admin controllers for providers, features, task pools and usage log, plus the two learner-facing controllers `AiUserSettingsController` and `AiCorrectionConsentController` (**New in 21.1.0**) |

---

## 1. Class Overview

### 1.1 Root package

| Class | Responsibility |
|-------|---------------|
| `AiSPI` | Base interface every provider must implement. Identity, enable/disable, admin UI factory (`createAdminController`), chat model factory (`buildChatModel`, with an optional per-call timeout), available models list. **Changed in 21.1.0:** `createAdminController(UserRequest, WindowControl, boolean readOnly)` takes a read-only flag. |
| `AiEmbeddingSPI` | Extends `AiSPI` for providers that can build a LangChain4j `EmbeddingModel`: `isEmbeddingEnabled()`, `buildEmbeddingModel(modelName)`, `getAvailableEmbeddingModels()`, `getEmbeddingDimension(modelName)`. Implemented by `OpenAiSPI`, `GenericAiSpiInstance` and `LocalOnnxSPI`. Used by taxonomy matching (`TaxonomyMatchingServiceImpl` in `org.olat.modules.taxonomy.matching.manager`). |
| `AiApiKeySPI` | Mixin for API-key-based providers. Enables the reusable `GenericAiApiKeyAdminController`. |
| `AiModule` | Central module. Merges Spring providers + generic instances. Stores per-feature provider/model config. Provides `resolveProvider()` for service implementations. Holds per-feature limits (output tokens, timeout, input size), the task pool sizes, the per-user rate limit thresholds (`getEssayGradingMaxCallsPerMinutePerUser`, `getEssayGenerationMaxCallsPerMinutePerUser`) and the system default of each user-controlled feature (`isUserDefaultOn(AiFeature)`, `setUserDefaultOn(AiFeature, boolean)`). **New in 21.1.0:** the user default methods and the constant `PROTECTION_PROFILE`. |
| `AiFeature` | Enum of all AI features: `MCQuestionGenerator`, `ImageDescriptionGenerator`, `EssayGeneration`, `EssayGrading`, `TaxonomyMatching`. Used as type discriminator on usage-log rows. **New in 21.1.0:** each constant carries a `userControlled` flag; `isUserControlled()` returns true for `EssayGrading` and `ImageDescriptionGenerator`. `getI18nKey()` names the tool in the administration, `getI18nUserNameKey()` and `getI18nDescriptionKey()` name and explain the feature for the person. |
| `AiUserPreference` | **New in 21.1.0.** Enum `DEFAULT / ON / OFF`. The choice of a person for one user-controlled feature. `DEFAULT` means "no choice", so the system default of `AiModule` decides. |
| `AiUserPreferenceService` | **New in 21.1.0.** `@Service` that stores and resolves that choice. See section 2.10. |
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
| `OpenAiSPI` | OpenAI provider. `@Service` extending `AbstractSpringModule`, implements `AiEmbeddingSPI` and `AiApiKeySPI`. Uses `OpenAiChatModel` and `OpenAiModelCatalog`. |
| `AnthropicAiSPI` | Anthropic Claude provider. Same pattern as `OpenAiSPI`, but chat only: it does not implement `AiEmbeddingSPI`. |
| `GenericAiSPI` | Factory/registry for generic OpenAI-compatible instances. Stores config as numbered properties. Not an `AiSPI` itself. |
| `GenericAiSpiInstance` | Single generic provider instance. Uses `OpenAiChatModel` with custom `baseUrl`. Created by `GenericAiSPI`. Implements `AiEmbeddingSPI`. |
| `GenericAiSpiAdminController` | Admin form of one generic instance (base URL, models, optional API key). |
| `LocalOnnxSPI` | `@Service("localOnnxSPI")`, id `LocalOnnx`. Embedding-only SPI that runs an ONNX embedding model in-process. The administrator uploads `model.onnx` and `tokenizer.json`; the model is not bundled in the WAR. Chat methods are not supported. |
| `QwenOnnxEmbeddingModel` | LangChain4j `EmbeddingModel` for the uploaded Qwen ONNX model. |
| `LocalOnnxModelAdminController` | Admin form for the upload of the local ONNX model. |

### 1.3 `manager/` — service implementations

| Class | Responsibility |
|-------|---------------|
| `AiMCQuestionServiceImpl` | MC question generation. Uses `MCQuestionAiService` (LangChain4j) via `CachedChatModel`. |
| `AiImageDescriptionServiceImpl` | Image description generation. Uses `ImageDescriptionAiService` (LangChain4j) via `CachedChatModel`. |
| `AiEssayGradingServiceImpl` | Essay grading. Resolves provider via `AiModule`, wraps the `ChatModel` in `AiLoggingChatModel`, invokes the `EssayGradingAiService` LangChain4j proxy. |
| `AiEssayGenerationServiceImpl` | Essay question generation. Same pattern; uses `EssayGenerationAiService`. |
| `AiUsageLogDAO` | DAO for `o_ai_usage_log`. Writes and queries usage rows; `createGuardLog` records non-LLM refusals. Exposes `countByIdentityFeatureSince` for rate-limit checks. Grading provenance lives on `o_ai_essay_correction`, not the log. |
| `AiLoggingChatModel` | Decorator that wraps any `ChatModel` and writes an `AiUsageLog` row on each call. |
| `AiLoggingEmbeddingModel` | Same decorator for an `EmbeddingModel` (taxonomy matching). |
| `AiUsageLoggedException` | Wraps an exception that was already written to the usage log. |
| `AiJsonParseFailure` | Package-private helper. Walks a cause chain and detects a truncated provider response. |
| `AiTaskExecutorService` | Owns the two AI thread pools (`aiInteractive`, `aiBatch` queues of `TaskExecutorManager`), resizes them from `AiModule` and reports running and waiting jobs (`getInteractiveStats()`, `getBatchStats()`). |
| `CachedChatModel` | Immutable record caching a LangChain4j `AiServices` proxy keyed by (spiId, modelName). Rebuilt on config change. |
| `LangChain4jHttpClientBuilder` | Adapts OpenOlat's `HttpClientService` to LangChain4j's `HttpClientBuilder`. |
| `LangChain4jHttpClient` | LangChain4j `HttpClient` built by `LangChain4jHttpClientBuilder`. **Changed in 21.1.0:** it creates the Apache client with `HttpClientService.createHttpClient(AiModule.PROTECTION_PROFILE)`. |

### 1.4 `service/` — LangChain4j AiService interfaces

| Class | Responsibility |
|-------|---------------|
| `AiPromptRules` | Shared compile-time prompt fragments. `OUTPUT_STYLE_RULES` (no em/en dashes; Swiss German: ss instead of ß, real umlauts instead of ae/oe/ue) is appended to every generative `@SystemMessage`. |
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
| `EssayFormativeFeedbackService` | Synchronous grading step of the formative-feedback flow. Takes the loaded `EssayAiGrading`, verifies the content hash, runs pre-filters, invokes the grader with the provider timeout of the feature (`ai.essay.grading.timeout.seconds`), sanitises output via OpenOlat's XSS filter, and records the grading-run provenance on the `o_ai_essay_correction` row. |
| `EssayGenerationService` | Public entry point for AI question generation. `submit(GenerationRequest)` schedules one `QtiQuestionGenerationTask`; `runTask(QtiQuestionGenerationTask)` runs it and dispatches accepted drafts to the configured sink. |
| `EssayGenerationService.GenerationRequest` | Record encapsulating a generation request. Factory methods: `forQuizPart` (page editor), `forPool` (question pool import), `of` (legacy drawer). |
| `EssayGenerationService.GenerationDestination` | Enum `DRAWER / QUIZ_PART / POOL`. Drives sink selection and usage-context labelling. |
| `QtiQuestionGenerationTask` | `LongRunnable` for one question-generation run, persisted in `o_ex_task`. Question-type agnostic: it generates essay and MC items in one task. Self-contained, it carries the complete `GenerationRequest`. |
| `EssayAiCorrectionService` | Public entry point for the AI correction of one essay answer. `submit(storagePath, questionId, studentAnswer, assessmentItemSessionKey, identity)` checks the queue load and the per-user rate limit, gates on the choice of the person (**New in 21.1.0**), writes an `EssayAiCorrection` row and schedules an `EssayAiCorrectionTask`. Returns the correction key, or `null` when the feature is unavailable or the person switched it off. |
| `EssayAiCorrection` | JPA entity (`o_ai_essay_correction`). One correction run: status, result JSON, and the grading-run provenance (content hash at call, prompt template version, tier). It is also the usage context of every `o_ai_usage_log` row of that run. |
| `EssayAiCorrectionDao` | DAO for `EssayAiCorrection`. Creates the row at submit time and updates it as the task progresses. |
| `EssayAiCorrectionTask` | `LongRunnable` for one correction run, persisted in `o_ex_task`. Thin: it carries the correction key and calls back into `EssayAiCorrectionService`. |
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
| `AiRateLimitExceededException` | Thrown by `EssayAiCorrectionService.submit(...)` and `EssayGenerationService.submit()` when the per-user, per-minute quota is exceeded. |
| `AiOverloadedException` | Thrown by `EssayAiCorrectionService.submit(...)` when more than two full pool rounds wait in the interactive queue. The refusal writes no guard row, so it does not count against the rate limit of the person. |
| `AiEssayResponseTruncatedException`, `AiEssayGenerationResponseTruncatedException` | Thrown when the provider reply was cut off (typically at the output token cap) and cannot be parsed. Grading turns it into a rejection card with `ai.essay.error.response.truncated`. |
| `AnnotatedParagraph`, `AnnotatedSpan`, `MarkKind` | Structured output of the inline annotation view of the student answer. `MarkKind` is `CORRECT`, `AMBIGUOUS`, `WRONG` or `NEUTRAL`. |
| `AiEssayGradingException` | Unchecked exception for grading service failures. |
| `AiEssayGenerationException` | Unchecked exception for generation service failures. |
| `EssayGradingTimeoutException` | Thrown when the provider HTTP timeout of essay grading expires. |
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

Before any provider call, `EssayFormativeFeedbackService.grade(...)` runs three sequential filters. Order matters:

1. `LengthPreFilter`: rejects answers below or above the word-count bounds. The upper bound is `ai.essay.grading.max.input.words` (default 400). Cheapest check, runs first.
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

Both correction and generation use the OpenOlat persistent task infrastructure. `TaskExecutorManager` persists each `LongRunnable` in `o_ex_task` and runs it on one of the two AI queues: `aiInteractive` for the essay correction a learner waits for, `aiBatch` for generation and image metadata. `AiTaskExecutorService` sizes the two pools from `ai.task.pool.interactive.size` (default 8) and `ai.task.pool.batch.size` (default 2) per node. On cluster restart, unfinished tasks are picked up automatically. The correction writes its `EssayAiCorrection` row first and schedules `EssayAiCorrectionTask` with the key; generation carries the whole `GenerationRequest` in `QtiQuestionGenerationTask` and keeps no job row of its own.

### 2.6 Owner-Checked Status Access

`EssayAiCorrectionService.getStatus(correctionKey, Identity)` returns "not found" when the requesting identity does not match the owner of the correction, the same response as for an unknown key. This prevents IDOR: an attacker who guesses a correction key cannot determine whether it belongs to another user.

### 2.7 Per-User Rate Limiting

Before accepting a submit, both `EssayAiCorrectionService` and `EssayGenerationService` call `AiUsageLogDAO.countByIdentityFeatureSince(identityKey, feature, since)` to count calls within the last 60 seconds. The thresholds are constants in `AiModule` (30 per minute for grading, 10 for generation). A refusal writes a guard row with `createGuardLog`, so it counts toward the limit too. Exceeding the limit throws `AiRateLimitExceededException`, which the controller translates to a localised error message.

### 2.8 Sanitisation Pipeline for Student-Facing Output

The AI provider response is passed through OpenOlat's built-in XSS filter with a restricted tag whitelist before storage or display. Only safe formatting tags are allowed; all script, iframe, and event-handler attributes are stripped.

### 2.9 Usage Logging with Destination-Aware Context

Every provider call writes a row to `o_ai_usage_log`. The `usageContextType` field encodes both the feature and the calling context (e.g. `ai-essay-correction`, `qpool-generate-questions`, `ceditor-quizpart-generate-questions`), and `usageContextId` points at the context entity that the call is about. For essay correction that entity is the `o_ai_essay_correction` row: `usageContextType = ai-essay-correction`, `usageContextId = <correction key>`, so all log rows of one correction (guard, grading, retry) share the same context id and the log stays a generic ledger with no feature-specific columns. The `resourceType` / `resourceId` pair identifies the resource: `RepositoryEntry` for course/page operations, `PoolQPool` for pool operations. The grading-run provenance (content hash, prompt template version, tier) lives on the `o_ai_essay_correction` row, not on the log; deleting a correction therefore drops its provenance while the cost ledger row survives (it keeps the now-stale context id, like any other soft reference).

### 2.10 User Control of an Automatic Feature

**New in 21.1.0**

A feature that runs without being asked for is user controlled. `AiFeature.isUserControlled()` marks these features; today they are `EssayGrading` and `ImageDescriptionGenerator`. Three values decide whether such a feature runs for one person:

1. The administrator switches the feature on or off in the AI module. An off feature never runs, whatever the person chose.
2. The administrator sets the system default per feature: `ai.feature.<type>.user.default` in `olat.properties`, editable in the AI features form. `AiModule.isUserDefaultOn(AiFeature)` reads it, `setUserDefaultOn(AiFeature, boolean)` writes it and fires the cluster-wide change event. Both features preset to `true`.
3. The person overrides the default in the user settings, segment "AI settings" (`AiUserSettingsController`). `AiUserPreferenceService` stores the choice in the GUI preferences, attributed class `AiUserPreferenceService`, key `ai.optout.<feature type>`, value `default`, `on` or `off`.

`AiUserPreferenceService` public methods:

| Method | Purpose |
|--------|---------|
| `get(Preferences, AiFeature)` | The stored choice. `DEFAULT` for an absent row and for an unreadable value. |
| `get(Identity, AiFeature)` | The same, for a caller without a user session. It resolves the persistent `Preferences` through `PreferencesFactory`. A controller uses the `Preferences` variant, so the session copy stays in sync after a write. |
| `set(Preferences, AiFeature, AiUserPreference)` | Stores the choice. Never writes for a guest: all guests of one language share one identity, so a guest gets transient `RamPreferences` and the call returns without writing. |
| `hasPreference(Preferences, AiFeature)` | True when the person made a choice, that is a stored `ON` or `OFF`. |
| `isActive(Preferences, AiFeature)` | The effective answer: feature available, and `ON`, or `DEFAULT` plus a system default of on. Fails closed on a null `Preferences`. |
| `isFeatureAvailable(AiFeature)` | True when the administrator switched the feature on and a provider is configured. False for a feature that is not user controlled. |

**The three gates.** Each automatic trigger asks once, at the point where the AI call would start.

| Gate | Where | Behaviour |
|------|-------|-----------|
| Quiz start dialog | `QuizRunController` with `AiCorrectionConsentController` | Before the first essay answer is corrected. A stored `ON` or `OFF` decides silently. On `DEFAULT` the modal asks: "Allow once" runs this quiz only, "Always allow" writes `ON`, "Not now" and Escape refuse for this run. A refusal skips the correction: no correction row, no overlay, no error card. `EssayAiCorrectionService.submit(...)` repeats the check as a defensive guard for every other caller and returns `null` on an unavailable feature or a stored `OFF`. A run rebuilt by a cluster event (`SyntheticUserRequest`) never opens the modal and fails closed for that run; the next start or retry asks again. |
| Page editor import toggle | `MarkdownImportController` | The import dialog of a Markdown or Word file shows a toggle, preset with `isActive(...)`. The value travels as `MarkdownImportOptions` into `MarkdownPagePartVisitor`, which submits the AI metadata of the imported images only when it is on. The toggle is valid for this one import and is not stored. |
| Upload background submit | `MediaUploadController`, `CollectImageMediaController` | The background metadata generation after an image upload runs only when `isActive(...)` is true. The explicit "Generate metadata with AI" buttons are not gated: the person asks for the result. |

### 2.11 HTTP Protection Profile

**New in 21.1.0**

All provider calls go through `LangChain4jHttpClient`, which asks `HttpClientService.createHttpClient(AiModule.PROTECTION_PROFILE)` for the Apache client. `AiModule.PROTECTION_PROFILE` is `ProtectionProfile.CONFIGURED`: the administrator configured the provider URL, so the SSRF filter of `HttpClientService` does not apply. The filter applies only to the profile `USER_PROVIDED` and only when `http.ssrf.protection.enabled=true`. A generic instance on an internal address, for example a local vLLM server, therefore stays reachable.

---

## 3. Using the MC Question Generator

```java
@Autowired
private AiMCQuestionService aiMCQuestionService;

if (aiMCQuestionService.isEnabled()) {
    showAiButton();
}

AiUsageContext usageContext = AiUsageContext.builder()
        .usageContextType("my-usage-context-type")
        .identity(identity)
        .locale(locale)
        .build();
AiMCQuestionsResponse response = aiMCQuestionService.generateMCQuestionsResponse(usageContext, inputText, 5);
if (response.isSuccess()) {
    for (MCQuestionData q : response.getQuestions()) {
        // q.getTitle(), q.getQuestion(), q.getCorrectAnswers(), q.getWrongAnswers() (List<McAnswerOption>)
    }
} else {
    showError(response.getError());
}
```

Two further overloads exist: `generateMCQuestionsResponse(usageContext, input, number, bloomLevels, targetDifficulty, learningObjectives)` for the essay generation pipeline, and `generateMCQuestionsResponse(usageContext, input, number, spiId, modelName)` for the admin feature test.

## 4. Using the Image Description Generator

The synchronous API is `AiImageDescriptionService.generateImageDescription(...)`. It is used directly only where the caller genuinely waits for the result: the admin feature test and the explicit "Generate metadata with AI" buttons in `CollectImageMediaController` and `MediaUploadController` (via `MediaAiMetadataService.generateNow(...)`), which overwrite the open form so the user can review before saving. All automatic enrichment flows (Markdown/Word import in the content editor, media center upload, collect-image save) go through the **asynchronous** `MediaAiMetadataService` in `org.olat.modules.cemedia.manager` instead — the user never waits on a provider round-trip:

```java
@Autowired
private MediaAiMetadataService mediaAiMetadataService;

// After the media has been created and committed:
// overwrite = true replaces machine-generated values (import flows),
// overwrite = false fills only empty fields (form flows)
boolean scheduled = mediaAiMetadataService.submit(media, requester, locale,
        "my-usage-context-type", resourceType, resourceId, resourceSubId, false);
if (scheduled) {
    showInfo("ai.metadata.background");
}
```

`submit()` is a no-op returning `false` when the feature is disabled or the media is not a supported raster image. It schedules one `MediaAiMetadataGenerationTask` (`LongRunnable`, `Queue.aiBatch`, persisted in `o_ex_task`, cluster-aware crash recovery) per media. The task re-resolves the image from the media storage, calls the vision model plus the optional taxonomy matching, and applies the result defensively: title only when empty or filename-like, description/alt text only when empty, tags and taxonomy only when none are assigned — a user editing the media before the task runs is never overwritten. The last argument `overwrite` relaxes this rule: with `true` the AI values replace existing machine-generated title and alt text, for example Word auto alt texts. The Markdown/Word import passes `true`, the form flows pass `false`.

Direct synchronous use for testing:

```java
@Autowired
private AiImageDescriptionService aiImageDescriptionService;
@Autowired
private AiImageHelper aiImageHelper;

String base64 = aiImageHelper.prepareImageBase64(imageFile, "jpg");
String mimeType = aiImageHelper.getMimeType("jpg");
if (base64 != null && mimeType != null) {
    AiImageDescriptionResponse response = aiImageDescriptionService.generateImageDescription(usageContext, base64, mimeType, locale);
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

The synchronous path is `EssayFormativeFeedbackService`. In practice the caller is `EssayAiCorrectionService.runCorrection(...)` inside an `EssayAiCorrectionTask`, but the direct API is also usable for testing. A UI opens the flow with `EssayAiCorrectionService.submit(...)` and polls `getStatus(correctionKey, caller)`. The rate limit (`AiRateLimitExceededException`) and the queue overload (`AiOverloadedException`) are checked in `submit(...)`, not in `grade(...)`. `QuizRunController` shows `ai.essay.feedback.error.ratelimit`, `ai.essay.correction.overloaded` and `ai.essay.correction.timeout` for these cases.

```java
@Autowired
private EssayFormativeFeedbackService feedbackService;
@Autowired
private EssayAiGradingFileStore gradingStore;

// questionDir is the directory (java.io.File) that contains the QTI essay item XML
EssayAiGrading grading = gradingStore.load(questionDir);
if (grading != null) {
    try {
        // correctionKey may be null for a preview or test call without a correction row
        FormativeFeedback feedback = feedbackService.grade(
                correctionKey, grading, studentAnswer, itemSession, student, locale);
        // feedback.tier(), feedback.suggestion(), feedback.warnings()
    } catch (EssayGradingTimeoutException e) {
        // provider HTTP timeout (ai.essay.grading.timeout.seconds)
    } catch (EssayGradingIntegrityException e) {
        // content hash of ai-grading.json does not match
    }
}
```

## 6. Using the Essay Generation Service

The async path is `EssayGenerationService`. The caller builds a `GenerationRequest` using one of the factory methods and calls `submit()`, which returns at once. The service schedules one `QtiQuestionGenerationTask` and keeps no job row of its own, so there is no status to poll: the UI waits for the sink to attach the result.

```java
@Autowired
private EssayGenerationService essayGenerationService;

// Page editor (QuizPart) flow
GenerationRequest request = GenerationRequest.forQuizPart(
        pageMarkdown, repositoryEntryKey, locale, currentIdentity,
        pageKey, quizPartKey, 2 /* essay */, 2 /* mc */);
essayGenerationService.submit(request);

// Question pool flow
GenerationRequest poolRequest = GenerationRequest.forPool(
        sourceText, null, locale, currentIdentity, 3, 3, taxonomyLevelKey);
essayGenerationService.submit(poolRequest);
```

When the task completes, `EssayGenerationQuizPartSink` (for `QUIZ_PART`) or `EssayGenerationPoolSink` (for `POOL`) is called automatically. For the legacy `DRAWER` destination, the caller handles the result directly.

## 7. Writing a Custom SPI

Extend `AbstractSpringModule` and implement `AiSPI`. If your provider uses an API key, also implement `AiApiKeySPI` to get the reusable `GenericAiApiKeyAdminController` for free. If your provider can build embedding models, implement `AiEmbeddingSPI` instead of `AiSPI`. The Spring service layer handles all feature-specific prompt construction and response parsing — the SPI only needs to supply a `ChatModel` and the list of available model names.

**Changed in 21.1.0:** `createAdminController` receives a `readOnly` flag. The admin form must disable every input and hide every save, add and delete action when the flag is true. `GenericAiApiKeyAdminController` takes the flag as its fourth constructor argument.

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
    @Override public Controller createAdminController(UserRequest ureq, WindowControl wc, boolean readOnly) {
        return new GenericAiApiKeyAdminController(ureq, wc, this, readOnly);
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

1. Define a LangChain4j AiService interface in `service/` with `@SystemMessage`/`@UserMessage` prompt templates and a structured return type; append `AiPromptRules.OUTPUT_STYLE_RULES` to the `@SystemMessage` (house typography rules: no em/en dashes, Swiss German spelling)
2. Define a structured output model in `model/` with LangChain4j `@Description` annotations
3. Define a response wrapper in `model/` extending `AiResponse` (synchronous features) or a `record` return type (async/streaming features)
4. Define a Spring service interface in the root package (follow `AiMCQuestionService` or `AiEssayGradingService` as a template)
5. Implement the service in `manager/` using `AiLoggingChatModel` wrapping `AiSPI.buildChatModel` and `AiServices.builder`
6. Add feature config properties and methods to `AiModule` (follow the essay pattern); add rate limit threshold properties if needed
7. Add a variant to the `AiFeature` enum. **New in 21.1.0:** Set `userControlled` to true when the feature runs automatically without being asked for; then add the `.user` and `.desc` i18n keys, a case in `AiUserPreferenceService.isFeatureAvailable(...)`, a preset in `AiModule.userDefaultPreset(...)` plus `ai.feature.<type>.user.default` in `olat.properties`, one gate at the point where the AI call starts (section 2.10), and the constant to `AiUserSettingsController.CONTROLLED_FEATURES`, which drives both the rows of the "AI settings" segment and the visibility of the segment link
8. Add feature UI section to `AiFeaturesAdminController`; respect its `readOnly` flag (**New in 21.1.0**)

## 9. Configuration

Each `AbstractSpringModule` persists config to `{userdata}/system/configuration/{FQCN}.properties`.

| Module | Key properties |
|--------|---------------|
| `AiModule` | Per feature `ai.feature.<type>.enabled` (default `false`), `.spi`, `.model` for `mc-question-generator`, `image-description-generator`, `essay-generation`, `essay-grading`. Output token caps `ai.mc.generator.max.output.tokens` (16384), `ai.img.desc.max.output.tokens` (8192), `ai.essay.generation.max.output.tokens` (16384), `ai.essay.grading.max.output.tokens` (16384). Provider timeouts `ai.mc.generator.timeout.seconds` (180), `ai.img.desc.timeout.seconds` (180), `ai.essay.generation.timeout.seconds` (180), `ai.essay.grading.timeout.seconds` (600). Input limits `ai.mc.generator.max.input.chars` (60000), `ai.essay.generation.max.input.chars` (60000), `ai.essay.grading.max.input.words` (400). Task pools `ai.task.pool.interactive.size` (8), `ai.task.pool.batch.size` (2). **New in 21.1.0:** `ai.feature.essay-grading.user.default` and `ai.feature.image-description-generator.user.default` (both default `true`) |
| `OpenAiSPI` | `openai.api.key`, `openai.enabled` |
| `AnthropicAiSPI` | `anthropic.api.key`, `anthropic.enabled` |
| `GenericAiSPI` | `generic.instances=1,2`, `generic.{id}.name`, `.base.url`, `.api.key`, `.models`, `.enabled` |
| `TaxonomyMatchingModule` (`org.olat.modules.taxonomy.matching`) | `taxonomy.matching.enabled`, `.spi`, `.model`, `.min.score`, `.local.model.dir` and further embedding settings of the `TaxonomyMatching` feature |

Defaults in `olat.properties`: `ai.openai.enabled=false`, `ai.openai.api.key=`, `ai.anthropic.enabled=false`, `ai.anthropic.api.key=`, plus a preset generic instance `ai.generic.preset.enabled`, `.name`, `.base.url`, `.api.key`, `.models` (persisted under `generic.0.*`).

The per-user rate limits are constants in `AiModule` (`DEFAULT_ESSAY_GRADING_MAX_CALLS_PER_MINUTE_PER_USER = 30`, `DEFAULT_ESSAY_GENERATION_MAX_CALLS_PER_MINUTE_PER_USER = 10`). No property and no admin UI changes them.

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

`AiAdminController` is a segmented view with four segments:

1. **Providers** (`AiProvidersAdminController`): "Add AI provider" dropdown: OpenAI/Anthropic greyed out when already configured, generic always available with multiple instances. Below it the per-provider forms: enable/disable toggle, config fields (API key, base URL, models), delete with confirmation dialog.
2. **Features** (`AiFeaturesAdminController`): per-feature provider + model selection, output token cap, timeout and input limit. Models loaded live from provider API; free-text fallback when API unreachable. Test button for each feature.
3. **Processing pools** (`AiTaskPoolAdminController`): size of the `aiInteractive` and `aiBatch` pools per node, with a live snapshot of running and waiting jobs.
4. **Usage log** (`AiUsageLogAdminController`): tabular view of `o_ai_usage_log` rows.

**New in 21.1.0:** `AiAdminController` opens the configuration read-only when the person has neither the administrator nor the system administrator role. It passes the `readOnly` flag to the providers, features and pools segments and to every `AiSPI.createAdminController(...)`. In read-only mode the "Add AI provider" dropdown is hidden and all inputs are disabled; the providers form shows the description key `ai.providers.fx` with the support mail address instead of `ai.providers.desc`. The usage log stays available.

| Controller | Purpose |
|------------|---------|
| `AiAdminController` | Main page, orchestrates all sections |
| `AiProvidersAdminController` | Providers segment: add dropdown and the admin form of each provider. **Changed in 21.1.0:** replaces `AiConfigurationAdminController`, which was removed. |
| `AiTaskPoolAdminController` | Processing pools segment: pool sizes and live queue statistics |
| `GenericAiApiKeyAdminController` | Reusable form for API-key providers (OpenAI, Anthropic) |
| `GenericAiSpiAdminController` | Form for generic instances (base URL, models, optional API key) |
| `LocalOnnxModelAdminController` | Upload form of the local ONNX embedding model |
| `AiFeaturesAdminController` | Per-feature SPI + model config; enables/disables each AI use case; holds the "Default for users" toggle of a user-controlled feature |
| `AiFeaturesTestController` | Runs feature tests against a specific provider/model and shows results |
| `AiEssayGradingTestController` | Live test of essay grading structured output via admin UI |
| `AiEssayGenerationTestController` | Live test of essay generation structured output via admin UI |
| `AiUsageLogAdminController` | Usage log table with filters (`AiUsageLogDataSource`, `AiUsageLogTableModel`, `AiFeatureCellRenderer`, `AiStatusCellRenderer`) |

### 12.1 User UI

**New in 21.1.0**

| Controller | Purpose |
|------------|---------|
| `AiUserSettingsController` | Segment "AI settings" in the user settings (`UserSettingsController`). One row per user-controlled and available feature, with the choice `Default (on)` / `Default (off)`, `On`, `Off`. Hidden for a guest. |
| `AiCorrectionConsentController` | Modal shown by `QuizRunController` before the first AI correction when the person has no stored choice. Three buttons: "Allow once", "Always allow", "Not now". |

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
| `ai.essay.correction.timeout` | Grading timeout exceeded (ceditor bundle) |
| `ai.essay.correction.overloaded` | Interactive AI queue overloaded (ceditor bundle) |
| `ai.essay.error.response.truncated` | Provider reply cut off (ceditor bundle) |
| `ai.providers.fx` | **New in 21.1.0.** Description of the read-only providers form ({0} = support mail address) |
| `ai.feature.user.default` / `.help` | **New in 21.1.0.** "Default for users" toggle in the features form |
| `ai.feature.<type>.user` / `.desc` | **New in 21.1.0.** Learner-facing name and one-sentence description of a user-controlled feature |
| `ai.optout.*` | **New in 21.1.0.** Segment "AI settings" in the user settings: title, description, choices, save message |
| `ai.consent.*` | **New in 21.1.0.** Consent modal of the AI correction: title, text, system default note, three buttons, hint |
