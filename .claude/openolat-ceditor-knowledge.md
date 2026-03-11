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
| htmlparagraph | ParagraphPageElementHandler | ParagraphPart (ceparagraphpart) | TextSettings | text |
| htmlraw | HTMLRawPageElementHandler | HTMLPart (cehtmlpart) | TextSettings | text |
| hr | SpacerElementHandler | SpacerPart (ceseparatorpart) | — | layout |
| container | ContainerHandler | ContainerPart (cecontainerpart) | ContainerSettings | layout |
| table | TablePageElementHandler | TablePart (cetablepart) | TableSettings (+TableContent) | text |
| image | ImageHandler (cemedia) | MediaPart (cemediapart) | ImageSettings | media |
| gallery | GalleryElementHandler | GalleryPart (cegallerypart) | GallerySettings | media |
| imagecomparison | ImageComparisonElementHandler | ImageComparisonPart | ImageComparisonSettings | media |
| code | CodeElementHandler | CodePart (cecodepart) | CodeSettings | text |
| math | MathPageElementHandler | MathPart (cemathpart) | MathSettings | text |
| quiz | QuizElementHandler | QuizPart (cequizpart) | QuizSettings | knowledge |
| evaluationform | EvaluationFormHandler | EvaluationFormPart (ceformpart) | — | organisational |

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

block_1col(1), block_2cols(2), block_3cols(3), block_4cols(4,deprecated), block_5cols(5,deprecated), block_6cols(6), block_3rows(3), block_2_1rows(3), block_1_3rows(4), block_1_1lcols(2), block_1_2rows(3), block_1_2cols(3)

## Event System (27 types)

- **Lifecycle**: AddElementEvent, ChangePartEvent, ChangeVersionPartEvent, CloneElementEvent, DeleteElementEvent, SaveElementEvent, ImportEvent
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

## Key File Paths

- Interfaces: `ceditor/PageElement.java`, `PagePart.java`, `PageElementHandler.java`, `PageService.java`, `PageEditorProvider.java`
- Base entity: `ceditor/model/jpa/AbstractPart.java`
- Main controller: `ceditor/ui/PageEditorV2Controller.java`
- Handlers: `ceditor/handler/TitlePageElementHandler.java` (canonical), all in `ceditor/handler/`
- Models: `ceditor/model/TitleSettings.java`, `TableContent.java`, `ContainerSettings.java`, `CodeSettings.java`, `CodeLanguage.java`
- Serialization: `ceditor/ContentEditorXStream.java`
- Media: `cemedia/handler/ImageHandler.java`

## Adding a New Element Type — Checklist

1. Create model interface in `ceditor/model/` (extends PageElement or suitable sub-interface)
2. Create JPA entity in `ceditor/model/jpa/` (extends AbstractPart, override `getType()`, implement `copy()`)
3. Register entity in `META-INF/persistence.xml`
4. Create settings class in `ceditor/model/` if needed; register in `ContentEditorXStream`
5. Create handler in `ceditor/handler/` (impl PageElementHandler + mixins)
6. Create editor + inspector + run controllers in `ceditor/ui/`
7. Add i18n keys in `ceditor/ui/_i18n/`
8. Register handler in provider implementations (PageRunController, EvaluationFormEditorController)
