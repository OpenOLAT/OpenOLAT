# Media Center

**Package:** `org.olat.modules.cemedia`

The media center stores the reusable media of a person: images, videos, audio, files, draw.io diagrams, citations and texts. Pages of the content editor (`org.olat.modules.ceditor`) and of the portfolio reference these media. Each media has an ordered list of versions. Pluggable `MediaHandler` implementations provide the type-specific logic. The package also tracks where a media is used, whom it is shared with, its tags and taxonomy levels, and a change log. An asynchronous service fills in AI image metadata.

Key features:

- One `Media` per item, with an ordered list of `MediaVersion` rows. Position 0 is the current version.
- Pluggable media types through Spring beans that implement `MediaHandler`
- Usage tracking across image parts, quiz parts, gallery parts and image comparison parts
- Sharing with users, business groups, organisations and courses, each with an "editable" flag
- Tags, taxonomy levels and a license per media
- A change log (`o_media_log`) with the actions created, uploaded, versioned, and so on
- Asynchronous AI metadata for images: title, description, alt text, tags and taxonomy levels (`MediaAiMetadataService`)
- Media center as a personal user tool, a chooser for editors, and an admin view for all media

## Package Structure

| Directory | Purpose |
|-----------|---------|
| `cemedia/` (root) | Domain interfaces (`Media`, `MediaVersion`, relations), `MediaHandler`, `MediaService`, `MediaModule`, the user tool extension and the license handler |
| `handler/` | The built-in media handlers and their shared base class `AbstractMediaHandler` |
| `manager/` | `MediaServiceImpl`, the DAOs, `MediaSearchQuery`, `MetadataXStream`, `MediaAiMetadataService` and `MediaAiMetadataGenerationTask` |
| `model/` | JPA entities (`*Impl`), search parameters and read-only views (`MediaUsage`, `MediaShare`, `MediaWithVersion`) |
| `ui/` | Media center controllers, details, usage, relations, log and admin controllers |
| `ui/medias/` | Add, collect, edit and new-version controllers per media type |
| `ui/component/` | Cell renderers and the citation component |
| `ui/event/` | Events of the media center UI |

---

## 1. Class Overview

### 1.1 Root package

| Class | Responsibility |
|-------|---------------|
| `MediaLight` | Light view of a media: key, type, title, description, creation and collection date, business path. `OLATResourceable` with the resource type `CEMedia` (`MEDIA_RESOURCE_TYPE`). |
| `Media` | Full media. Extends `MediaLight` and `DublinCoreMetadata`. Adds alt text, author, UUID, the metadata XML and `getVersions()`. |
| `MediaVersion` | One version of a media. Version name, UUID, checksum, collection date, content, storage (`getStoragePath()`, `getRootFilename()` from `StoredData`), the VFS metadata and the optional `MediaVersionMetadata`. |
| `MediaVersionMetadata` | Technical data of a version: URL, width, height, length and format. Used for video via URL and for image and video dimensions. |
| `MediaHandler` | SPI for one media type. See section 2.1. |
| `MediaHandlerUISettings` | Record: `hasVersion`, `canUploadVersion`, `canCreateVersion`, the icon CSS classes, `viewLogs`, `hasLicense`. |
| `MediaInformations` | Type, title and description to prefill the creation form. |
| `MediaService` | Public service. Search, create versions, usage, sharing, tags, taxonomy levels, logs and quota. |
| `MediaToPagePart` | Link from a media and version to a page part that holds several media (gallery, image comparison). |
| `MediaToGroupRelation` | Share of a media. `MediaToGroupRelationType`: `USER`, `BUSINESS_GROUP`, `ORGANISATION`, `REPOSITORY_ENTRY`. Carries the `editable` flag. |
| `MediaToTaxonomyLevel` | Link from a media to a taxonomy level. |
| `MediaTag` | Link from a media to a tag of `org.olat.core.commons.services.tag`. |
| `MediaLog` | Log row. `Action`: `ASSIGNMENT`, `CREATED`, `RECORDED`, `COLLECTED`, `UPDATE`, `UPLOAD`, `IMPORTED`, `VERSIONED`. |
| `MediaModule` | Configuration. See section 5. |
| `MediaCenterExtension` | User tool extension that opens `MediaCenterPersonalToolController`. |
| `MediaCenterLicenseHandler` | `LicenseHandler` for media licenses. |
| `Citation`, `CitationSourceType` | Citation data of a citation media. |
| `MediaLoggingAction` | Logging actions of the media center. |

### 1.2 `handler/` and further handlers

Spring collects every `MediaHandler` bean into `MediaServiceImpl.mediaHandlers`. `MediaService.getMediaHandler(type)` returns the handler for a type string.

| Handler | Type string | Package |
|---------|-------------|---------|
| `ImageHandler` | `image` | `cemedia.handler` |
| `VideoHandler` | `video` | `cemedia.handler` |
| `AudioHandler` | `audio` | `cemedia.handler` |
| `FileHandler` | `bc` | `cemedia.handler` |
| `DrawioHandler` | `drawio` | `cemedia.handler` |
| `CitationHandler` | `citation` | `cemedia.handler` |
| `TextHandler` | `text` | `modules.portfolio.handler` |
| `ForumMediaHandler` | forum type name | `modules.fo.portfolio` |
| `WikiMediaHandler` | `WikiResource.TYPE_NAME` | `modules.wiki.portfolio` |
| `BlogEntryMediaHandler` | `BlogFileResource.TYPE_NAME` | `modules.webFeed.portfolio` |
| `EfficiencyStatementMediaHandler` | `EfficiencyStatement` | `course.assessment.portfolio` |

`AbstractMediaHandler` is the base class of all of them. `VideoViaUrlHandlerDelegate` holds the logic for videos from a URL (YouTube, Vimeo, nanoo.tv) that `VideoHandler` uses.

### 1.3 `manager/`

| Class | Responsibility |
|-------|---------------|
| `MediaServiceImpl` | Implements `MediaService`. Listens on `DocEditorService.DOCUMENT_SAVED_EVENT_CHANNEL`, so a file saved in the document editor creates a new version. |
| `MediaDAO` | Media and version persistence, usage queries (`getUsages`, `countUsages`, `isUsed`), file usage and the version list. |
| `MediaRelationDAO` | Shares (`o_media_to_group`). |
| `MediaTagDAO` | Tags (`o_media_tag`). |
| `MediaToTaxonomyLevelDAO` | Taxonomy links (`o_media_to_tax_level`). |
| `MediaToPagePartDAO` | Media relations of gallery and image comparison parts (`o_media_to_page_part`). |
| `MediaLogDAO` | Log rows (`o_media_log`). |
| `MediaSearchQuery` | Search behind `MediaService.searchMedias(SearchMediaParameters)`. |
| `MetadataXStream` | XStream for the metadata XML of a media. |
| `MediaAiMetadataService` | AI metadata for images. See section 3. |
| `MediaAiMetadataGenerationTask` | `LongRunnable` for one AI metadata run. See section 3. |

### 1.4 `ui/`

| Controller | Purpose |
|------------|---------|
| `MediaCenterPersonalToolController` | Entry of the user tool. It opens `MediaCentersController` for an administrator or a learn resource manager, and `MediaCenterController` for everybody else. |
| `MediaCentersController` | Segments "My media center" (`MediaCenterConfig.valueOfMy()`) and the admin media center (`MediaCenterConfig.managementConfig()`). |
| `MediaCenterController` | Media table with tabs, filters, upload and add actions. Behaviour comes from the `MediaCenterConfig` record. |
| `MediaCenterChooserController` | Media selection for the content editor (`MediaCenterConfig.valueOfChooser(...)` and `valueOfUploader(...)`). |
| `MediaUploadController` | Upload of a new media from a file. |
| `MediaDetailsController` | Details of one media: metadata, versions, usage, relations and log. |
| `MediaOverviewController`, `MediaMetadataController` | Preview and metadata of a media. |
| `MediaUsageController` | Where the media is used. |
| `MediaRelationsController` | Shares of the media. |
| `MediaLogController` | Change log. |
| `MediaAdminController` | Admin form: taxonomies, share rights per role, forced license check. Registered in `modules/_spring/modulesContext.xml` under the navigation key `mediacenter`. |

`ui/medias/` holds one set of controllers per type: `Add*Controller` (new media), `Collect*MediaController` (metadata form while collecting), `*MediaController` (view), `NewFileMediaVersionController`, `UpdateTextVersionController` and `StandardEditMediaController`.

---

## 2. Important Patterns

### 2.1 Media Handler SPI

A `MediaHandler` provides everything that depends on the type:

- `getType()`, `getSubType(MediaVersion)`, `acceptMimeType(String)`, `getIconCssClass(MediaVersion)`
- `hasMediaThumbnail(MediaVersion)`, `getThumbnail(MediaVersion, Size)`
- `getUISettings(MediaVersion)`: versioning, upload and license options of the details view
- `getInformations(Object)` and `createMedia(title, description, altText, mediaObject, businessPath, author, action)`
- `getMediaController(...)`, `getCollectMetadataController(...)`, `getEditMetadataController(...)`, `getNewVersionController(..., CreateVersion)`
- `export(Media, ManifestBuilder, File, Locale)` and `hasDownload(MediaVersion)`

`CreateVersion` is `CREATE` or `UPLOAD`.

### 2.2 Versions and Storage

A media keeps its versions in `o_media_version`, ordered by `pos`. Position 0 is the current version. `MediaService` offers `addVersion(...)` for content, for a file and for version metadata, plus `setVersion(...)` and `restoreVersion(...)`. `ContentEditorFileStorage` (in `org.olat.modules.ceditor.manager`) stores the files below `bcroot/portfolio/artefacts/`. `getMediaDirectory(StoredData)` resolves the directory of a version, and `generateMediaSubDirectory(Media)` creates one for a new media.

### 2.3 Usage Tracking

`MediaDAO.getUsages(media)` combines three queries:

1. `getMediaPartUses`: image parts (`cemediapart`) of pages
2. `getQuizPartUses`: quiz parts that reference the media
3. `getPagePartRelationUses`: `MediaToPagePart` rows of gallery and image comparison parts

The result is a list of `MediaUsage` records with page, binder and status. `MediaService.isUsed(media)` blocks the deletion of a used media.

### 2.4 Sharing

`MediaService.addRelation(media, editable, target)` shares a media with an identity, an organisation, a business group or a repository entry. `MediaModule` defines which roles may share with which target. `isMediaEditable(...)`, `isMediaShared(...)` and `isAdminOf(...)` answer the access questions.

**Changed in 21.1.0:** `MediaDetailsController` shows the delete link only when the person is the author of the media (OO-9774). The admin media center (`MediaCenterConfig.managementConfig()`) no longer offers the multi-select delete.

### 2.5 Tags and Taxonomy

`updateTags(identity, media, tags)` and `getTagInfos(...)` manage the tags. `updateTaxonomyLevels(media, levels)` and `getTaxonomyLevels(media)` manage the taxonomy levels. `MediaModule.getTaxonomyRefs()` returns the taxonomies of the media center. Without an own setting it falls back to the taxonomies of the repository module.

---

## 3. AI Image Metadata

`MediaAiMetadataService` generates title, description, alt text, tags and taxonomy levels for an image media. It uses `AiImageDescriptionService` of `org.olat.core.commons.services.ai`.

| Method | Purpose |
|--------|---------|
| `isEnabled()` | True when the image description feature is enabled. |
| `isSupportedImage(filename)` | True for JPEG, PNG, GIF and WebP. |
| `generateNow(imageFile, filename, requester, locale, usageContextType)` | Synchronous call for the "Generate metadata with AI" button. The form shows the result, nothing is saved. |
| `submit(media, requester, locale, usageContextType, resourceType, resourceId, resourceSubId, overwrite)` | Schedules one `MediaAiMetadataGenerationTask`. Returns `false` when the feature is off or the current version is not a supported image. |
| `runTask(task)` | Body of the task. Loads the image, calls the vision model and applies the result. |

The task runs on the `Queue.aiBatch` queue and is persisted in `o_ex_task`. A cluster restart picks it up again.

**Apply rules.** The person may edit the media before the task runs, so the task applies the result defensively:

- Title only when empty or filename-like. Truncated to 255 characters.
- Description and alt text only when empty. The alt text is truncated to 1000 characters.
- Tags only when the media has no tag yet. The tags come from orientation, colour tags, category tags, keywords and subject.
- Taxonomy levels only when the media has none. `TaxonomyMatchingHelper.matchTaxonomyLevels(...)` maps the AI subject to a level of the media center taxonomies (`org.olat.modules.taxonomy.matching`).

With `overwrite = true` the task replaces existing values. The Markdown and Word import of the content editor passes `true`, because the imported titles and alt texts are machine generated. The upload and collect forms pass `false`.

**Callers.**

| Caller | Usage context type | Gate |
|--------|--------------------|------|
| `MediaUploadController` | `mc-upload-image` | After the save, when no AI button was used |
| `CollectImageMediaController` | `mc-collect-image` | After the save, when no AI button was used |
| `MarkdownPagePartVisitor` (ceditor) | `page-markdown-import` | Import toggle of the person |

**Changed in 21.1.0:** The upload and collect forms schedule the task only when `AiUserPreferenceService.isActive(prefs, AiFeature.ImageDescriptionGenerator)` returns true (OO-9784). A person who switched the AI image descriptions off gets no background task. The explicit AI button stays available. See section 2.10 of the AI package documentation.

---

## 4. JPA Tables

| Table | Entity | Content |
|-------|--------|---------|
| `o_media` | `MediaImpl` (`mmedia`) | Media: type, title, description, alt text, Dublin Core fields, UUID, metadata XML, author |
| `o_media_version` | `MediaVersionImpl` (`mediaversion`) | Versions: `pos`, version name, UUID, checksum, storage path, root filename, content, `fk_media`, `fk_metadata`, `fk_version_metadata` |
| `o_media_version_metadata` | `MediaVersionMetadataImpl` | URL, width, height, length, format |
| `o_media_to_group` | `MediaToGroupRelationImpl` (`mediatogroup`) | Shares: type, editable, `fk_group`, `fk_repositoryentry`, `fk_media` |
| `o_media_to_page_part` | `MediaToPagePartImpl` (`mediatopagepart`) | Media of gallery and image comparison parts |
| `o_media_to_tax_level` | `MediaToTaxonomyLevelImpl` | Taxonomy links |
| `o_media_tag` | `MediaTagImpl` | Tags |
| `o_media_log` | `MediaLogImpl` (`medialog`) | Change log |

---

## 5. Configuration

`MediaModule` persists its settings in `{userdata}/system/configuration/org.olat.modules.cemedia.MediaModule.properties`.

| Key | Default (`olat.properties`) | Meaning |
|-----|-----------------------------|---------|
| `media.center.share.with.user` | `author,learnresourcemanager,administrator` | Roles that may share with a user |
| `media.center.share.with.group` | `user,author,learnresourcemanager,administrator` | Roles that may share with a business group |
| `media.center.share.with.course` | `author,learnresourcemanager,administrator` | Roles that may share with a course |
| `media.center.share.with.organisation` | `administrator` | Roles that may share with an organisation |
| `media.force.license.check` | `false` | A license is mandatory for a new media |
| `taxonomy.tree.key` | none | Taxonomies of the media center (comma-separated keys); empty means the repository taxonomies |

The video via URL platforms use `media.server.*` keys of `MediaServerModule` (`media.server.mode`, `media.server.youtube`, `media.server.vimeo`, `media.server.nanootv`).

## 6. Related Packages

- `org.olat.modules.ceditor`: pages and page parts that display media
- `org.olat.modules.portfolio`: portfolio pages and the `TextHandler`
- `org.olat.core.commons.services.ai`: image description service and user preferences
- `org.olat.modules.taxonomy.matching`: matching of an AI subject to taxonomy levels ([package doc](../taxonomy/matching/package.md))
