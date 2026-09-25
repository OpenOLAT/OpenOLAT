# ceditor Package — Quick Reference

## Architecture Summary

The `ceditor` package (`src/main/java/org/olat/modules/ceditor/`) implements a server-centric, component-based content editor for OpenOlat. Pages consist of ordered `PagePart` elements stored via single-table JPA inheritance. The system uses a handler pattern for extensibility — each element type has a `PageElementHandler` that provides view, editor, and inspector controllers. Two known integration points: Portfolio pages (`PageRunController`) and Evaluation forms (`EvaluationFormEditorController`).

## Key Interfaces

- `PageElement`: `getId()`, `getType()` — base for all elements
- `PagePart extends PageElement`: `getKey()`, `getContent()`, `setContent()`, `getLayoutOptions()`, `setLayoutOptions()`, `getStoragePath()`, `getPartFlow()`, `copy()`, `afterCopy()`, `beforeDelete()`
- `PageElementHandler`: `getType()`, `getIconCssClass()`, `getCategory()`, `getSortOrder()`, `getContent(ureq,wc,el,hints)` → `PageRunElement`, `getEditor(ureq,wc,el)` → `Controller`, `getInspector(ureq,wc,el)` → `PageElementInspectorController`
- `SimpleAddPageElementHandler`: `createPageElement(Locale)` — synchronous factory
- `InteractiveAddPageElementHandler`: `getAddPageElementController(ureq,wc,AddSettings)` — wizard-based
- `CloneElementHandler`: `clonePageElement(PageElement)`
- `PageElementStore<U>`: `savePageElement(U)` — persistence via PageService
- `PageProvider`: `getElements()`, `getAvailableHandlers()`
- `PageEditorProvider extends PageProvider`: CRUD ops (`appendPageElement`, `removePageElement`, `movePageElement`, etc.)
- `PageEditorSecurityCallback`: `canCloneElement()`, `canDeleteElement()`, `canMoveUpAndDown()`
- `RenderingHints`: `isToPdf()`, `isOnePage()`, `isExtendedMetadata()`, `isEditable()`
- `PageElementCategory` enum: text, questionType, organisational, media, knowledge, other, content, layout

## JPA Tables

```
o_ce_page (PageImpl) ──1:1──> o_ce_page_body (PageBodyImpl) ──1:N ordered──> o_ce_page_part (AbstractPart, single-table inheritance)
```

- `AbstractPart`: `@Entity(name="cepagepart")`, `@Table(name="o_ce_page_part")`, `@DiscriminatorColumn`
- Fields: `id`, `creationdate`, `lastmodified`, `pos` (read-only order), `p_content`, `p_flow`, `p_layout_options`, `p_storage_path`, `fk_page_body_id`
- `PageBodyImpl`: `@OneToMany(targetEntity=AbstractPart.class)` + `@OrderColumn(name="pos")` + `orphanRemoval=true, cascade=REMOVE`

## Element Types

| Type String | Handler | JPA Entity | Settings Class | Category |
|-------------|---------|------------|---------------|----------|
| htitle | TitlePageElementHandler | TitlePart (cetitlepart) | TitleSettings | text |
| htmlparagraph | ParagraphPageElementHandler (reuses HTMLRawEditorController / HTMLRawInspectorController) | ParagraphPart (ceparagraphpart) | TextSettings | text |
| htmlraw | HTMLRawPageElementHandler | HTMLPart (cehtmlpart) | TextSettings | text |
| hr | SpacerElementHandler (no inspector, `getInspector()` returns null) | SpacerPart (ceseparatorpart) | — | layout |
| container | ContainerHandler | ContainerPart (cecontainerpart) | ContainerSettings | layout |
| table | TablePageElementHandler | TablePart (cetablepart) | TableSettings (+TableContent) | text |
| image | ImageHandler (cemedia) | MediaPart (cemediapart) | ImageSettings | media |
| gallery | GalleryElementHandler | GalleryPart (cegallerypart) | GallerySettings | media |
| imagecomparison | ImageComparisonElementHandler | ImageComparisonPart | ImageComparisonSettings | media |
| code | CodeElementHandler | CodePart (cecodepart) | CodeSettings | text |
| math | MathPageElementHandler (MathLiveEditorController / MathLiveInspectorController / MathLiveRunComponent) | MathPart (cemathpart) | MathSettings | text |
| quiz | QuizElementHandler | QuizPart (cequizpart) | QuizSettings | knowledge |
| evaluationform | EvaluationFormHandler | EvaluationFormPart (ceformpart) | — | organisational |
| toc | TocElementHandler | TocPart (cetocpart) | TocSettings (`title`, `visibleLevels`) | other |

## Settings Serialization

- `ContentEditorXStream.toXml(obj)` / `ContentEditorXStream.fromXml(xml, Class)`
- Dual storage: `content` column for HTML/data, `layoutOptions` for XStream XML settings
- All settings registered with explicit type permissions in ContentEditorXStream

## Programmatic Element Creation

```java
TitlePart title = new TitlePart();
title.setContent("Heading Text");
TitleSettings ts = new TitleSettings();
ts.setSize(2); // h2
ts.setLayoutSettings(BlockLayoutSettings.getPredefined());
title.setTitleSettings(ts);
pageService.appendNewPagePart(page, title);
```

For images: `ImageHandler.createMedia(title, desc, altText, file, filename, bpath, author, action)` → `MediaPart.valueOf(author, media)` → `pageService.appendNewPagePart(page, mediaPart)`

For tables: `TableContent tc = new TableContent(rows, cols)` → `tc.addContent(row, col, text)` → `tablePart.setContent(ContentEditorXStream.toXml(tc))`

## ContainerLayout Enum

block_1col(1), block_2cols(2), block_3cols(3), block_4cols(4,deprecated), block_5cols(5,deprecated), block_6cols(6,deprecated), block_3rows(3), block_2_1rows(3), block_1_3rows(4), block_1_1lcols(2), block_1_2rows(3), block_1_2cols(3)

## Event System (29 types)

- **Lifecycle**: AddElementEvent, ChangePartEvent, ChangeVersionPartEvent, CloneElementEvent, DeleteElementEvent, SaveElementEvent, ImportEvent, ImportMarkdownEvent, MarkdownImportDoneEvent, PageStructureChangedEvent
- **Edit**: EditElementEvent, EditFragmentEvent, EditionEvent, EditPageElementEvent, CloseElementsEvent, ClosePartEvent
- **Inspector**: CloseInspectorEvent, CloseInspectorsEvent, OpenRulesEvent
- **Position**: MoveUpElementEvent, MoveDownElementEvent
- **Add**: OpenAddElementEvent, OpenAddLayoutEvent
- **Drag-Drop**: DropFragmentEvent, DropToEditorEvent, DropToPageElementEvent, DropCanceledEvent
- **Container**: ContainerColumnEvent, ContainerRuleLinkEvent

## UI Components

- `PageEditorV2Controller` — central editor orchestrator
- `ContentEditorComponent` — root component (manages fragments, Dragula drag-drop)
- `ContentEditorFragment` (interface) → `ContentEditorFragmentComponent` (leaf) / `ContentEditorContainerComponent` (container)
- Renderers: ContentEditorComponentRenderer, ContentEditorFragmentComponentRenderer, ContentEditorContainerComponentRenderer

## Provider Implementations

1. `PageRunController.PortfolioPageEditorProvider` (portfolio/ui/) — portfolio pages
2. `EvaluationFormEditorController.FormPageEditorProvider` (forms/ui/) — evaluation forms

## Service Layer

- `PageService` (interface): CRUD for pages and parts
- `PageServiceImpl` (@Service): delegates to PageDAO
- `PageDAO`: JPA operations
- `ContentEditorFileStorage`: bcroot/portfolio/ file management
- `PageImportExportHelper`: ZIP-based import/export
- `ContentEditorXStream`: settings serialization

### Markdown Import

- `MarkdownImportService` (@Service): converts markdown → PageParts and persists them wrapped in a `ContainerPart` (`block_1col`). Reuses last empty container if available. Result `MarkdownImportResult(warnings, container, column, insertIndex, aiMetadataJobs)`; `column`/`insertIndex` place the AI quiz placeholder, also in multi-column layouts.
- **New in 21.1.0:** `convertAndPersist(...)` takes `MarkdownImportOptions options` as last parameter (record, flag `generateImageMetadata`; `null` or `NONE` = no AI). The visitor calls `MediaAiMetadataService.submit(..., overwrite=true)` per image only when the flag is on. Before 21.1.0 every image got a task when the service was enabled.
- Unclosed YAML front matter: the front matter extension is dropped, the content is imported, warning `import.markdown.warn.frontmatter.unclosed`.
- `MarkdownPagePartVisitor`: CommonMark `AbstractVisitor` producing PageParts. Headings→TitlePart, paragraphs→ParagraphPart (consecutive merged), code→CodePart, tables→TablePart, blockquotes→ParagraphPart+AlertBoxSettings, images→MediaPart, math→MathPart. Inline HTML entity-escaped; HTML blocks skipped. URL sanitization (http/https/mailto only). Remote image SSRF protection via `MediaServerModule.isRestrictedDomain()`. **New in 21.1.0:** downloads use `HttpClientService.createThreadSafeHttpClient(true, ProtectionProfile.USER_PROVIDED)` (OO-9310). Alt text truncated to 1000 chars, media title to 255. AI image metadata runs async (`MediaAiMetadataGenerationTask`, `Queue.aiBatch`).
- `MarkdownMathPreprocessor`: replaces $$...$$ blocks with placeholders before CommonMark parsing.
- `MarkdownCodeLanguageMapping`: maps fenced code info strings to `CodeLanguage` enum.
- `MarkdownImportController`: `FormBasicController` with file upload (.md/.markdown/.txt/.zip/.docx) or text paste. ZIP support extracts single .md file with relative image assets; DOCX via `DocxToMarkdownService`. Text longer than the AI input limit (`resolveMaxInputChars()`, from `AiModule`) fails with `form.error.toolong`.
  - AI quiz section: shown when `allowAiQuestionGeneration` (from `PageRunController`: `PageSettings.isCanCreateQuiz()`), a repository entry key and `AiEssayGenerationService.isEnabled()`. Fields: essay/MC counts 0 to 5, Bloom levels (`ai.bloom`), difficulty (`ai.difficulty`), objectives (`ai.objectives`); sent via `GenerationRequest.forQuizPart(...)` → `QtiQuestionGenerationTask` on `Queue.aiBatch`.
  - **New in 21.1.0:** toggle `import.ai.imagemeta`, own gate `MediaAiMetadataService.isEnabled()` (also in the e-portfolio). Preset from `AiUserPreferenceService.isActive(prefs, AiFeature.ImageDescriptionGenerator)`; valid for this import only, not stored.
- `PageEditorProvider.isImportMarkdownEnabled()`: default method (returns false). Override to true to show the import button. Enabled in `PortfolioPageEditorProvider`.

### Quiz and AI

- Essay items in the quiz (pool import filter in `QuizEditorController`, and the AI correction) require `AiEssayGradingService.isEnabled()`.
- Saving an AI-generated question in `EditQuestionController` writes `supervisedBy` into `ai-source.json` and clears `unsupervisedGenerated`.
- Runtime correction: `QuizRunController` → `EssayAiCorrectionService.submit()` (row `o_ai_essay_correction`, `EssayAiCorrectionTask` on `Queue.aiInteractive`) → poll `getStatus()` every 2000 ms, max polls from `AiModule.getEssayGradingTimeoutSeconds()` → `parseFeedback()` → `FormativeFeedback`. Errors: `ai.essay.correction.timeout`, `ai.essay.correction.overloaded`, `ai.essay.feedback.error.ratelimit`.
- **New in 21.1.0:** consent (OO-9784). At quiz start `doAskAiCorrectionConsent(...)` opens `AiCorrectionConsentController` (Allow once / Always allow / Not now) when EssayGrading is available, the person has no stored choice and an essay has `ai-grading.json`. "Always allow" stores `AiUserPreference.ON`. Decision kept in `aiCorrectionAllowedForRun` for this run only, fails closed (dialog open, `SyntheticUserRequest`). Without a run decision `AiUserPreferenceService.isActive(...)` decides. `EssayAiCorrectionService.submit()` returns null for an opted-out person.

### ContentEditorModule (olat.properties)

`ceditor.image.styles`, `ceditor.image.title.styles`, `ceditor.table.styles` (style lists), `ceditor.import.limit.md` (51200 KB), `ceditor.import.limit.docx` (204800 KB).

## Key File Paths

- Interfaces: `ceditor/PageElement.java`, `PagePart.java`, `PageElementHandler.java`, `PageService.java`, `PageEditorProvider.java`
- Base entity: `ceditor/model/jpa/AbstractPart.java`
- Main controller: `ceditor/ui/PageEditorV2Controller.java`
- Handlers: `ceditor/handler/TitlePageElementHandler.java` (canonical), all in `ceditor/handler/`
- Models: `ceditor/model/TitleSettings.java`, `TableContent.java`, `ContainerSettings.java`, `CodeSettings.java`, `CodeLanguage.java`
- Serialization: `ceditor/ContentEditorXStream.java`
- Markdown import: `ceditor/manager/MarkdownImportService.java`, `MarkdownImportOptions.java`, `MarkdownPagePartVisitor.java`, `MarkdownMathPreprocessor.java`, `MarkdownCodeLanguageMapping.java`
- Markdown UI: `ceditor/ui/MarkdownImportController.java`, `ceditor/ui/event/ImportMarkdownEvent.java`, `ceditor/ui/event/MarkdownImportDoneEvent.java`
- Media: `cemedia/handler/ImageHandler.java`

## Adding a New Element Type — Checklist

1. Create model interface in `ceditor/model/` (extends PageElement or suitable sub-interface)
2. Create JPA entity in `ceditor/model/jpa/` (extends AbstractPart, override `getType()`, implement `copy()`)
3. Register entity in `META-INF/persistence.xml`
4. Create settings class in `ceditor/model/` if needed; register in `ContentEditorXStream`
4b. Register the part entity in `manager/PageXStream.java` (`types` array + `aliasType`), else page export/import breaks (OO-9788)
5. Create handler in `ceditor/handler/` (impl PageElementHandler + mixins)
6. Create editor + inspector + run controllers in `ceditor/ui/`
7. Add i18n keys in `ceditor/ui/_i18n/`
8. Register handler in provider implementations (PageRunController, EvaluationFormEditorController)
