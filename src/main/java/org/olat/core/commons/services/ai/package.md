# AI Service

**Package:** `org.olat.core.commons.services.ai`

Pluggable framework for integrating generative AI into OpenOlat. Uses the
Service Provider Interface (SPI) pattern: the core defines interfaces and a
feature registry, concrete providers implement the API calls. Multiple
providers can be active simultaneously. Each AI feature is independently
mapped to a provider and model via the admin UI.

Key features:

- Multi-provider support (OpenAI, Anthropic Claude, generic OpenAI-compatible servers)
- Per-feature provider + model configuration
- AI-powered MC question generation from text input
- AI-powered image description generation (title, alt text, tags, keywords) with vision models
- Generic providers for self-hosted servers (vLLM, Ollama, LiteLLM, etc.) — added dynamically, multiple instances
- Shared prompt construction and response parsing via `AiPromptHelper`
- Image preprocessing (scaling, base64 encoding) via `AiImageHelper`
- LangChain4j for chat model abstraction and model catalog APIs

## 1. Class Diagram

<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 870 640" font-family="'SF Mono','Cascadia Code','Fira Code','Consolas',monospace">
  <style>
    .box { stroke-width: 1.2; rx: 4; ry: 4; }
    .box-iface    { fill: #ecfdf5; stroke: #16a34a; }
    .box-class    { fill: #f1f5f9; stroke: #475569; }
    .box-service  { fill: #fef3c7; stroke: #d97706; }
    .box-model    { fill: #fdf4ff; stroke: #a855f7; }
    .cls-title  { font-size: 13px; font-weight: 700; fill: #1e293b; }
    .cls-stereo { font-size: 10px; font-style: italic; fill: #64748b; }
    .cls-text   { font-size: 10.5px; fill: #334155; }
    .sep  { stroke: #cbd5e1; stroke-width: .8; }
    .arr  { stroke: #64748b; stroke-width: 1.2; fill: none; }
    .arr-dash { stroke-dasharray: 6 3; }
    .arr-lbl  { font-size: 10px; fill: #64748b; font-style: italic; font-family: -apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif; }
  </style>
  <defs>
    <marker id="oh" viewBox="0 0 10 7" refX="9" refY="3.5" markerWidth="8" markerHeight="7" orient="auto-start-reverse">
      <polygon points="0 0,10 3.5,0 7" fill="none" stroke="#64748b" stroke-width="1.2"/>
    </marker>
    <marker id="ah" viewBox="0 0 10 7" refX="9" refY="3.5" markerWidth="8" markerHeight="7" orient="auto-start-reverse">
      <polygon points="0 0,10 3.5,0 7" fill="#64748b"/>
    </marker>
  </defs>

  <!-- ── Interfaces (row 1) ── -->
  <rect class="box box-iface" x="10" y="8" width="200" height="100"/>
  <text class="cls-stereo" x="110" y="24" text-anchor="middle">&#171;interface&#187;</text>
  <text class="cls-title" x="110" y="40" text-anchor="middle">AiSPI</text>
  <line class="sep" x1="10" y1="48" x2="210" y2="48"/>
  <text class="cls-text" x="18" y="64">+ getId() : String</text>
  <text class="cls-text" x="18" y="78">+ getName() : String</text>
  <text class="cls-text" x="18" y="92">+ isEnabled() / setEnabled()</text>
  <text class="cls-text" x="18" y="104">+ createAdminController()</text>

  <rect class="box box-iface" x="240" y="8" width="280" height="100"/>
  <text class="cls-stereo" x="380" y="24" text-anchor="middle">&#171;interface&#187;</text>
  <text class="cls-title" x="380" y="40" text-anchor="middle">AiMCQuestionGeneratorSPI</text>
  <line class="sep" x1="240" y1="48" x2="520" y2="48"/>
  <text class="cls-text" x="248" y="64">+ generateMCQuestionsResponse()</text>
  <text class="cls-text" x="248" y="78">+ setMCGeneratorModel(String)</text>
  <text class="cls-text" x="248" y="92">+ getAvailableMCGeneratorModels()</text>

  <rect class="box box-iface" x="550" y="8" width="220" height="86"/>
  <text class="cls-stereo" x="660" y="24" text-anchor="middle">&#171;interface&#187;</text>
  <text class="cls-title" x="660" y="40" text-anchor="middle">AiApiKeySPI</text>
  <line class="sep" x1="550" y1="48" x2="770" y2="48"/>
  <text class="cls-text" x="558" y="64">+ getApiKey() / setApiKey()</text>
  <text class="cls-text" x="558" y="78">+ verifyApiKey(String) : List</text>
  <text class="cls-text" x="558" y="90">+ getAdminTitleI18nKey()</text>

  <!-- ── Feature interface (row 1b) ── -->
  <rect class="box box-iface" x="240" y="118" width="280" height="100"/>
  <text class="cls-stereo" x="380" y="134" text-anchor="middle">&#171;interface&#187;</text>
  <text class="cls-title" x="380" y="150" text-anchor="middle">AiImageDescriptionSPI</text>
  <line class="sep" x1="240" y1="158" x2="520" y2="158"/>
  <text class="cls-text" x="248" y="174">+ generateImageDescription()</text>
  <text class="cls-text" x="248" y="188">+ setImageDescriptionModel(String)</text>
  <text class="cls-text" x="248" y="202">+ getAvailableImageDescriptionModels()</text>

  <!-- ── Implementations (row 2) ── -->
  <rect class="box box-class" x="10" y="248" width="240" height="72"/>
  <text class="cls-title" x="130" y="268" text-anchor="middle">OpenAiSPI</text>
  <line class="sep" x1="10" y1="276" x2="250" y2="276"/>
  <text class="cls-text" x="18" y="292">@Service, AbstractSpringModule</text>
  <text class="cls-text" x="18" y="306">OpenAiChatModel + OpenAiModelCatalog</text>

  <rect class="box box-class" x="280" y="248" width="240" height="72"/>
  <text class="cls-title" x="400" y="268" text-anchor="middle">AnthropicAiSPI</text>
  <line class="sep" x1="280" y1="276" x2="520" y2="276"/>
  <text class="cls-text" x="288" y="292">@Service, AbstractSpringModule</text>
  <text class="cls-text" x="288" y="306">AnthropicChatModel + ModelCatalog</text>

  <rect class="box box-class" x="550" y="248" width="240" height="72"/>
  <text class="cls-title" x="670" y="268" text-anchor="middle">GenericAiSPI</text>
  <line class="sep" x1="550" y1="276" x2="790" y2="276"/>
  <text class="cls-text" x="558" y="292">@Service, factory/registry</text>
  <text class="cls-text" x="558" y="306">+ createInstance() / deleteInstance()</text>

  <rect class="box box-class" x="550" y="340" width="240" height="72"/>
  <text class="cls-title" x="670" y="360" text-anchor="middle">GenericAiSpiInstance</text>
  <line class="sep" x1="550" y1="368" x2="790" y2="368"/>
  <text class="cls-text" x="558" y="384">AiSPI+ApiKey+MCGen+ImgDesc</text>
  <text class="cls-text" x="558" y="398">OpenAiChatModel with custom baseUrl</text>

  <!-- ── Core services (row 3) ── -->
  <rect class="box box-service" x="10" y="440" width="340" height="100"/>
  <text class="cls-stereo" x="180" y="456" text-anchor="middle">&#171;service&#187;</text>
  <text class="cls-title" x="180" y="472" text-anchor="middle">AiModule</text>
  <line class="sep" x1="10" y1="480" x2="350" y2="480"/>
  <text class="cls-text" x="18" y="496">+ getAiProviders() : List&lt;AiSPI&gt;</text>
  <text class="cls-text" x="18" y="510">+ getMCQuestionGenerator()</text>
  <text class="cls-text" x="18" y="524">+ getImageDescriptionGenerator()</text>
  <text class="cls-text" x="18" y="538">+ isAiEnabled()</text>

  <rect class="box box-service" x="380" y="440" width="300" height="100"/>
  <text class="cls-stereo" x="530" y="456" text-anchor="middle">&#171;service&#187;</text>
  <text class="cls-title" x="530" y="472" text-anchor="middle">AiPromptHelper</text>
  <line class="sep" x1="380" y1="480" x2="680" y2="480"/>
  <text class="cls-text" x="388" y="496">+ createQuestionSystemMessage()</text>
  <text class="cls-text" x="388" y="510">+ createImageDescriptionSystemMessage()</text>
  <text class="cls-text" x="388" y="524">+ parseQuestionResult(String)</text>
  <text class="cls-text" x="388" y="538">+ parseImageDescriptionResult(String)</text>

  <rect class="box box-service" x="710" y="440" width="150" height="72"/>
  <text class="cls-stereo" x="785" y="456" text-anchor="middle">&#171;service&#187;</text>
  <text class="cls-title" x="785" y="472" text-anchor="middle">AiImageHelper</text>
  <line class="sep" x1="710" y1="480" x2="860" y2="480"/>
  <text class="cls-text" x="718" y="496">+ prepareImageBase64()</text>
  <text class="cls-text" x="718" y="510">+ getMimeType(String)</text>

  <!-- ── Model classes (row 4) ── -->
  <rect class="box box-model" x="10" y="568" width="160" height="52"/>
  <text class="cls-title" x="90" y="588" text-anchor="middle">AiResponse</text>
  <line class="sep" x1="10" y1="596" x2="170" y2="596"/>
  <text class="cls-text" x="18" y="612">+ isSuccess() / getError()</text>

  <rect class="box box-model" x="200" y="568" width="220" height="52"/>
  <text class="cls-title" x="310" y="588" text-anchor="middle">AiMCQuestionsResponse</text>
  <line class="sep" x1="200" y1="596" x2="420" y2="596"/>
  <text class="cls-text" x="208" y="612">+ getQuestions() : List</text>

  <rect class="box box-model" x="450" y="568" width="230" height="52"/>
  <text class="cls-title" x="565" y="588" text-anchor="middle">AiImageDescriptionResponse</text>
  <line class="sep" x1="450" y1="596" x2="680" y2="596"/>
  <text class="cls-text" x="458" y="612">+ getDescription() : Data</text>

  <!-- ── Arrows ── -->
  <!-- implements (dashed, open triangle) -->
  <line class="arr arr-dash" x1="130" y1="248" x2="110" y2="108" marker-end="url(#oh)"/>
  <line class="arr arr-dash" x1="400" y1="248" x2="380" y2="108" marker-end="url(#oh)"/>
  <line class="arr arr-dash" x1="200" y1="258" x2="380" y2="218" marker-end="url(#oh)"/>
  <line class="arr arr-dash" x1="280" y1="268" x2="240" y2="218" marker-end="url(#oh)"/>
  <line class="arr arr-dash" x1="550" y1="350" x2="210" y2="108" marker-end="url(#oh)"/>
  <line class="arr arr-dash" x1="600" y1="340" x2="520" y2="108" marker-end="url(#oh)"/>
  <line class="arr arr-dash" x1="600" y1="360" x2="520" y2="218" marker-end="url(#oh)"/>
  <line class="arr arr-dash" x1="700" y1="340" x2="660" y2="94" marker-end="url(#oh)"/>

  <!-- GenericAiSPI creates GenericAiSpiInstance -->
  <line class="arr" x1="670" y1="320" x2="670" y2="340" marker-end="url(#ah)"/>
  <text class="arr-lbl" x="684" y="334">creates *</text>

  <!-- AiModule uses SPIs -->
  <line class="arr arr-dash" x1="130" y1="440" x2="130" y2="320" marker-end="url(#ah)"/>
  <text class="arr-lbl" x="140" y="385">uses</text>
  <line class="arr arr-dash" x1="350" y1="470" x2="530" y2="470" marker-end="url(#ah)"/>

  <!-- extends / contains -->
  <line class="arr" x1="200" y1="594" x2="170" y2="594" marker-end="url(#oh)"/>
  <text class="arr-lbl" x="180" y="587" text-anchor="middle">extends</text>
  <line class="arr" x1="450" y1="594" x2="170" y2="594" marker-end="url(#oh)"/>
  <text class="arr-lbl" x="300" y="587" text-anchor="middle">extends</text>
</svg>

### Roles of each class

| Class | Responsibility |
|-------|---------------|
| `AiSPI` | Base interface every provider must implement. Identity, enable/disable, admin UI factory. |
| `AiMCQuestionGeneratorSPI` | Feature mixin for MC question generation. Providers implement this alongside `AiSPI`. |
| `AiImageDescriptionSPI` | Feature mixin for image description generation (title, alt text, tags, keywords). Requires vision-capable models. |
| `AiApiKeySPI` | Mixin for API-key-based providers. Enables the reusable `GenericAiApiKeyAdminController`. |
| `OpenAiSPI` | OpenAI provider. `@Service` extending `AbstractSpringModule`. Uses `OpenAiChatModel` and `OpenAiModelCatalog`. |
| `AnthropicAiSPI` | Anthropic Claude provider. Same pattern as `OpenAiSPI`. |
| `GenericAiSPI` | Factory/registry for generic OpenAI-compatible instances. Stores config as numbered properties. Not an `AiSPI` itself. |
| `GenericAiSpiInstance` | Single generic provider. Uses `OpenAiChatModel` with custom `baseUrl`. Created by `GenericAiSPI`. |
| `AiModule` | Central module. Merges Spring providers + generic instances, stores per-feature provider/model config. Entry point for consumers. |
| `AiPromptHelper` | Shared prompt construction (system + user messages) and XML response parsing for both question and image description features. Used by all SPIs. |
| `AiImageHelper` | Image preprocessing service. Scales images to max 1024px and base64-encodes them for vision API calls. |
| `AiResponse` / `AiMCQuestionsResponse` / `AiMCQuestionData` | Response model for MC questions. `AiResponse` holds error state, `AiMCQuestionsResponse` wraps a list of parsed `AiMCQuestionData`. |
| `AiImageDescriptionResponse` / `AiImageDescriptionData` | Response model for image descriptions. Wraps parsed metadata: title, description, alt text, color tags, category tags, keywords. |

## 2. Using the MC Question Generator

```java
@Autowired
private AiModule aiModule;

// Check availability (no side effects — safe for conditional UI)
if (aiModule.isMCQuestionGeneratorEnabled()) {
    showAiButton();
}

// Get the configured generator (sets model, returns null if not configured)
AiMCQuestionGeneratorSPI generator = aiModule.getMCQuestionGenerator();
if (generator != null) {
    AiMCQuestionsResponse response = generator.generateMCQuestionsResponse(inputText, 5);
    if (response.isSuccess()) {
        for (AiMCQuestionData q : response.getQuestions()) {
            // q.getTitle(), q.getQuestion(), q.getCorrectAnswers(), q.getWrongAnswers()
        }
    } else {
        showError(response.getError());
    }
}
```

## 3. Using the Image Description Generator

```java
@Autowired
private AiModule aiModule;
@Autowired
private AiImageHelper aiImageHelper;

// Check availability
if (aiModule.isImageDescriptionGeneratorEnabled()) {
    showAiButton();
}

// Generate description from an image file
AiImageDescriptionSPI generator = aiModule.getImageDescriptionGenerator();
if (generator != null) {
    String base64 = aiImageHelper.prepareImageBase64(imageFile, "jpg");
    String mimeType = aiImageHelper.getMimeType("jpg");
    if (base64 != null && mimeType != null) {
        AiImageDescriptionResponse response = generator.generateImageDescription(base64, mimeType, locale);
        if (response.isSuccess()) {
            AiImageDescriptionData data = response.getDescription();
            // data.getTitle(), data.getDescription(), data.getAltText()
            // data.getColorTags(), data.getCategoryTags(), data.getKeywords()
        } else {
            showError(response.getError());
        }
    }
}
```

`AiImageHelper` handles scaling (max 1024px) and base64 encoding. Supports JPEG, PNG, GIF, and WebP.

## 4. Writing a Custom SPI

Extend `AbstractSpringModule`, implement `AiSPI` plus feature interfaces.
If your provider uses an API key, also implement `AiApiKeySPI` to get the
reusable `GenericAiApiKeyAdminController` for free. Implement
`AiImageDescriptionSPI` for image description support (requires vision-capable models).

```java
@Service
public class MyAiSPI extends AbstractSpringModule
        implements AiSPI, AiApiKeySPI, AiMCQuestionGeneratorSPI, AiImageDescriptionSPI {

    private static final String MY_API_KEY = "myai.api.key";
    private static final String MY_ENABLED = "myai.enabled";
    private String apiKey;
    private boolean enabled;
    private String mcGeneratorModel;

    @Autowired private AiPromptHelper aiPromptHelper;

    @Autowired
    public MyAiSPI(CoordinatorManager coordinatorManager) {
        super(coordinatorManager);
    }

    @Override public void init() {
        apiKey = getStringPropertyValue(MY_API_KEY, apiKey);
        enabled = getBooleanPropertyValue(MY_ENABLED);
    }

    @Override protected void initFromChangedProperties() { init(); }

    // AiSPI
    @Override public String getId() { return "MyAI"; }
    @Override public String getName() { return "My AI Provider"; }
    @Override public boolean isEnabled() { return enabled; }
    @Override public void setEnabled(boolean e) { enabled = e; setBooleanProperty(MY_ENABLED, e, true); }
    @Override public Controller createAdminController(UserRequest ureq, WindowControl wc) {
        return new GenericAiApiKeyAdminController(ureq, wc, this);
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

    // AiMCQuestionGeneratorSPI
    @Override public void setMCGeneratorModel(String m) { mcGeneratorModel = m; }
    @Override public String getMCGeneratorModel() { return mcGeneratorModel; }
    @Override public List<String> getAvailableMCGeneratorModels() {
        return List.of("my-model-v1", "my-model-v2");
    }

    @Override
    public AiMCQuestionsResponse generateMCQuestionsResponse(String input, int number) {
        AiMCQuestionsResponse response = new AiMCQuestionsResponse();
        try {
            Locale locale = aiPromptHelper.detectSupportedLocale(input);
            if (locale == null) { response.setError("Language not supported."); return response; }
            SystemMessage sys = aiPromptHelper.createQuestionSystemMessage(locale);
            UserMessage usr = aiPromptHelper.createChoiceQuestionUserMessage(input, number, 2, 3, locale);
            String result = callMyApi(sys, usr); // your API call
            response = aiPromptHelper.parseQuestionResult(result);
        } catch (Exception e) { response.setError(e.getMessage()); }
        return response;
    }
}
```

## 5. Adding a New Feature

1. Define a feature interface in the root package (e.g. `AiTextSummarizerSPI`)
2. Add feature config properties and methods to `AiModule` (follow the MC generator / image description pattern)
3. Implement in existing SPIs (`OpenAiSPI`, `AnthropicAiSPI`, `GenericAiSpiInstance`)
4. Add prompt construction and response parsing to `AiPromptHelper`
5. Add UI config to `AiFeaturesAdminController`

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
3. **Features form** (`AiFeaturesAdminController`) — Per-feature provider + model selection. Models loaded live from provider API; free-text fallback when API unreachable.

| Controller | Purpose |
|------------|---------|
| `AiAdminController` | Main page, orchestrates all sections |
| `AddProviderFormController` | Inner form with dropdown menu |
| `GenericAiApiKeyAdminController` | Reusable form for API-key providers (OpenAI, Anthropic) |
| `GenericAiSpiAdminController` | Form for generic instances (base URL, models, optional API key) |
| `AiFeaturesAdminController` | Per-feature SPI + model config |

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
