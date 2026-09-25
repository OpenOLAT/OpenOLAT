# OpenOlat Architecture Knowledge Base

> Compressed reference for developers working with Claude Code on the OpenOlat codebase.
> Covers release 21.1.0 (updated 2026-09-25). For full documentation see `doc/openolat-architecture.md` and `doc/openolat-architecture.html` ("doc §N" refers to their section numbers).
> The `openolat-dev` skill lives in `.claude/skills/openolat-dev/`.
> Markers: `**New in 21.1.0:**` / `**Changed in 21.1.0:**` = only on the 21.1 code line (master). Items without a marker also exist on 21.0.x.

## 1. Core Concepts

**Server-centric architecture.** All UI state lives on the server. No client-side framework. Browser receives HTML fragments via AJAX. The component tree is a server-side object graph.

**Session-per-request.** Each HTTP request gets its own Hibernate `EntityManager` via `ThreadLocal`. Framework commits after dispatch. Between requests, all entities are detached.

**Synchronized window.** Each browser tab is a `Window` object. All dispatches to a window are synchronized — prevents race conditions from double-click or concurrent requests.

**Stack (21.1.0):** Java 17 target, Spring 7.0.9, Hibernate 7.4.10, Infinispan 16.2.3, CXF 4.2.3, Jackson 2.22.3, LangChain4j 1.20.0, Log4j2 2.26.1, Velocity 2.4.1, Quartz 2.5.2. Maxima CAS and Nashorn are removed.

## 2. Request Flow

```
Browser → web.xml (filters SameSiteCookieFilter, RestApiLoginFilter, HeadersFilter)
  /raw/*     → StaticServlet (JS, CSS, images, themes; URLs via StaticMediaDispatcher.getStaticURI())
  /restapi/* → CXFServlet (JAX-RS) behind RestApiLoginFilter
  /rss/*, /ical/* → PersonalRSSServlet, ICalServlet
  /*         → OpenOLATServlet → Dispatcher by URL prefix:
     /auth/   → AuthenticatedDispatcher (main app)
     /dmz/    → DMZDispatcher (login, registration)
     /url/    → RESTDispatcher (external deep links)
     /lti/    → LTI13Dispatcher
     /bigbluebutton/, /teams/, /certificate/, /badge/, /catalog/, ... (dispatcherContext.xml)
     /m/      → MapperDispatcher      ┐ added in code by
     /g/      → GlobalMapperRegistry  │ OpenOLATServlet.init(),
     /webdav/ → WebDAVDispatcher      ┘ not in dispatcherContext.xml

Within AuthenticatedDispatcher:
  Window.dispatchRequest() → find Component by dispatch ID → component.dispatchRequest()
  → fires Event to controller → controller.event() → business logic → dirty flags
  → RENDER: traverse tree, render dirty components → JSON response with HTML fragments
```

**Critical rule:** During rendering, all business logic must be complete. No DB access during render phase (by convention — the connection is technically open but must not be used).

## 3. Controller Hierarchy

```
Controller (interface)
  DefaultController          : base, event dispatch, dispose lifecycle, DISPOSE_LOCK
    BasicController          : adds VelocityContainer, listenTo(), mappers, WindowControl
      FormBasicController    : adds FlexiForm: initForm/validate/formOK/formInnerEvent
```

`FormLayoutContainer` is a `FormItem` container, not a controller. Controllers are autowired by the framework: `@Autowired` fields work in controllers.

**Controller communication:**
- Child → Parent: `fireEvent(ureq, Event.DONE_EVENT)` — parent receives in `event(ureq, Controller, Event)`
- Component → Controller: button clicks etc. — received in `event(ureq, Component, Event)`
- Cross-session: `EventBus.fireEventToListenersOf(MultiUserEvent, OLATResourceable)`

**Lifecycle:** `listenTo(childCtrl)` registers for events AND ensures automatic disposal. `removeAsListenerAndDispose(ctrl)` for replacement. Override `doDispose()` for manual cleanup (EventBus, locks, heavy refs).

## 4. Component Model

Every visible UI element is a `Component`. Components render themselves via `ComponentRenderer`. The tree has:
- `VelocityContainer` — renders `.html` Velocity template, children accessed via `$r.render("name")`
- `Panel` / `StackedPanel` / `BreadcrumbedStackedPanel`: swappable content, stacks, drill-down
- `Link`: clickable element (button or link); `MenuTree` for trees; `RatingComponent`, `EmptyState`
- `FormItem` — FlexiForm elements (TextElement, SingleSelection, etc.)

**New in 21.1.0:** `FactSheet` (`FactSheetFactory`: title, `Fact` rows, footer links), `Sections` (`SectionsFactory`: titled collapsible sections), `ComponentList` (components in order, no template).

**Dirty flag mechanism:** When a component changes, `setDirty(true)` propagates up the tree. Only dirty subtrees are re-rendered in AJAX responses.

## 5. FlexiForm System

Two-phase: `initForm()` builds the form structure, then `validateFormLogic()` + `formOK()` handle submission.

Key form items: `TextElement`, `TextAreaElement`, `RichTextElement`, `SingleSelection` (dropdown/radio), `MultipleSelectionElement` (checkboxes), `DateChooser`, `RelativeDateElement` (`addRelativeDateElement(..., RelativeDateContext)`), `ObjectSelectionElement` (`ObjectSelectionSource` with `ObjectOptionGroup`; `IdentitySelectionSource` for users), `FileElement`, `StaticTextElement`, `FormToggle`, `FormLink`.

Layout containers (`FormLayoutContainer.FormLayout`, 13 types): `createDefaultFormLayout()` (3:9, also `_6_6`, `_9_3`, `_2_10`), `createHorizontalFormLayout()`, `createVerticalFormLayout()`, `createTwoColsFormLayout()` / `uifactory.addTwoColumnsFormLayout()`, `createPanelFormLayout()`, `createInlineFormLayout()`, `createInputGroupLayout()`, `createTableCondensedLayout()`, `createButtonLayout()`, `createBareBoneFormLayout()`, `createCustomFormLayout(name, trans, page)` (no enum constant).

**New in 21.1.0:**
- `FormSection` (§8): `uifactory.addFormSection(name, title, layout, FormSection.Level.TITLE|SUB_TITLE)`, `setCollapsible()`, `setCollapsed()`, then `setPersistedStatusId(ureq, id)` (state in GUI prefs). Add items into the section.
- `SearchElement` (§8): `uifactory.addSearchElement(name, SearchVariant.DEFAULT|LARGE|TYPEAHEAD, layout)` or `addSearchElement(name, listProvider, usess, layout)`. Fires `SearchFormEvent` (`SEARCH`, `RESET`); does not mark the form dirty.
- `FormItem.setLabelIconCss("o_icon o_icon_locked")`: icon next to the label, visual cue only.
- `$r.sectionHeader(id, title, subTitle, collapsible, expanded)` in templates.

## 6. FlexiTable

Data-driven table component with server-side sorting, filtering, pagination.

Pattern: Define columns enum implementing `FlexiSortableColumnDef`, create `DefaultFlexiTableDataModel<Row>` subclass, add `FlexiTableElement` via `uifactory.addTableElement()`.

- Default sort: `FlexiTableSortOptions.setDefaultOrderBy(new SortKey(...))` + `tableEl.setSortSettings(options)`
- Empty state: `tableEl.setEmptyStateConfig(EmptyStateConfig.builder()...build())`
- Filters: `setFilters(true, List<FlexiTableExtendedFilter>, customPresets, alwaysExpanded)`; filter tabs `setFilterTabs()`
- Preferences: `setAndLoadPersistedPreferences(ureq, id)`
- Cell renderers: `TextFlexiCellRenderer`, `DateFlexiCellRenderer`, `StaticFlexiCellRenderer`, `BooleanCellRenderer`, `CSSIconFlexiCellRenderer`, `TreeNodeFlexiCellRenderer`, `ActionsCellRenderer`; **New in 21.1.0:** `TranslateCellRenderer`
- Renderer types: `classic`, `custom` (Velocity card layout), `external` (`setExternalRenderer()`), `verticalTimeLine`

**Changed in 21.1.0:** The table search field is a `SearchElement` (`getSearchEl()`). `setSearchEnabled(ListProvider, UserSession)` uses the typeahead variant.

## 7. Event System

| Scope | Mechanism | Example |
|-------|-----------|---------|
| Component → owning Controller | `event(ureq, Component, Event)` | Button click |
| Child Controller → Parent | `fireEvent()` / `event(ureq, Controller, Event)` | Dialog result |
| Form lifecycle | `formOK()`, `formCancelled()`, `formInnerEvent()` | Form submission |
| Cross-session (cluster) | `EventBus` + `MultiUserEvent` | Course change notification |

EventBus: register with `OLATResourceable` key, fire to all listeners of that resource, works across cluster nodes. **Must deregister in `doDispose()`.**

## 8. Velocity Templating

Templates in `_content/` directory, colocated with controller package. Access via `$r` helper object:
- `$r.render("componentName")` — render child component (safe)
- `$r.translate("key")` — i18n translation
- `$r.escapeHtml($var)` — **MUST use for user text** (XSS prevention)
- `$r.contextHelpWithWrapper("page")` — help link
- `$r.getId("elementId")` — scoped HTML ID
- `$r.staticLink("path")` — versioned static resource URL
- `$r.screenreaderOnly("text")` — `<span class="sr-only">`

**XSS rule:** `$r.render()` is safe. `$myVar` from `contextPut()` is NOT auto-escaped.

## 9. Database & Persistence

- `DBFactory.getInstance()` / `@Autowired DB dbInstance` → `getCurrentEntityManager()`
- All queries use JPQL with named parameters (never string concatenation)
- Entities in `src/main/resources/META-INF/persistence.xml` (443 classes)
- Use `join fetch` to prevent LazyInitializationException
- Bulk ops: `intermediateCommit()` every ~100 items (`DBImpl` warns above 500 accesses)
- Background jobs: must `commitAndCloseSession()` explicitly
- Connection pool: HikariCP. L2 cache: Infinispan.
- **New in 21.1.0:** `PersistenceHelper.extractBigDecimal(row, pos, default)` for aggregate columns

## 10. VFS (Virtual File System)

All file access through VFS, never direct filesystem. `bcroot/` is the physical storage root.

- `VFSContainer` = directory, `VFSLeaf` = file
- `VFSManager.olatRootContainer(path)`: resolve from bcroot; `resolveFile()`, `copyContent()`, `getRealPath()`, `findInheritedSecurityCallback()`
- `VFSContainer.isInPath(path)` rejects directory traversal
- Security callbacks control read/write/delete per container
- Metadata in `o_vfs_metadata` table. Versioning, quotas, trash handled transparently.
- HTTP delivery: `VFSMediaResource`

## 11. Spring & Configuration

- Hybrid: annotations (`@Service`, `@Autowired`) for new code, XML for legacy
- Main context: `src/main/java/org/olat/_spring/mainContext.xml` (imports 30 top-level contexts, which import the module `_spring/` contexts, plus a component scan)
- In controllers: `@Autowired` works (framework autowiring); `CoreSpringFactory.getImpl(MyManager.class)` in other non-Spring classes
- Config: `olat.properties` (defaults) + `olat.local.properties` (overrides); admin changes persisted by `AbstractSpringModule` in `{userdata}/system/configuration/{FQCN}.properties`
- Feature toggles: extend `AbstractSpringModule`

## 12. i18n (Internationalization)

**Core classes:** `I18nModule` (config, language discovery), `I18nManager` (resolution, caching, property loading), `PackageTranslator` (per-controller translator).

**Files:** `_i18n/LocalStrings_XX.properties` colocated with UI package. Java `.properties` format (key=value, `#` comments, `\:` escapes colons).

**Resolution order** (per locale, with overlay checked first at each step):
1. Overlay of requested locale (e.g. `de__customizing`)
2. Requested locale (`de_CH`)
3. Fallback to language-only variant (`de_CH` → `de`)
4. Fallback to country-only variant
5. Overlay of default locale
6. Default locale (configurable, typically `en`)
7. Overlay of fallback locale
8. Fallback locale (hardcoded `en`)
9. Error / `NO_TRANSLATION_ERROR_PREFIX`

**Overlay:** `{userData}/customizing/lang/overlay/{package}/_i18n/LocalStrings_XX__customizing.properties` — customer-specific overrides, checked first at every resolution step.

**Cross-referencing translations (key → key):**
- **Same-package:** `$\:other.key` — resolves `other.key` from the same `.properties` file
- **Cross-package:** `$org.olat.other.package:other.key` or `${org.olat.other.package:other.key}` — resolves from a different package
- Resolution is recursive (up to 10 levels), handled by `resolveValuesInternalKeys()` using regex pattern `\$\{?([package]):([key])\}?`
- Resolved at load time when caching is enabled; at access time otherwise

**Fallback bundles:** `org.olat.core` (core fallback), `org.olat` (application fallback) — checked by `PackageTranslator` when key not found in primary bundle.

**Parameter substitution:** `{0}`, `{1}` etc. — processed by `java.text.MessageFormat`. Single quotes must be escaped as `''`.

**Gender strategy:** Written as `Benutzer{in}` in `.properties` files, converted at runtime to configured style per locale (star `*`, colon `:`, middleDot, slash, etc.). Configured via `I18nModule.GenderStrategy`.

**In Java:** `translate("key")` (in controllers), `translate("key", new String[]{arg})` with args.
**In templates:** `$r.translate("key")`, `$r.translate("key", $arg)`.

**Caching:** Controlled by `localization.cache` property. Production: on. Dev: off for live reloading. Cluster-aware via EventBus (`I18nReInitializeCachesEvent`).

**Stats (21.1.0):** 408 bundles with `LocalStrings_en.properties`, about 33,000 English keys, 31 language codes. About 2,000 cross-references.

**Terms:** `doc/i18n-translation-reference.yaml` (generated from the concept map in fxIntelligence, never edit by hand).

## 13. Theming

SASS-based. Themes in `src/main/webapp/static/themes/`. `light/` is the base theme (includes Bootstrap). Product themes override `_config.scss` variables only. Compiled via `compiletheme.sh`. Custom themes via `guiCustomThemePath`. Details: `doc/openolat-frontend.md`.

## 14. Scheduler & Background Tasks

Quartz for periodic tasks (no `@Scheduled`). Config: `schedulerContext.xml`, 5-thread pool. Base class: `JobWithDB` (auto commit/rollback). Cluster-singleton job: trigger references `myJob.${cluster.singleton.services}`, beans `myJob.enabled` (job class) and `myJob.disabled` (`DummyJob`).

Async execution: `TaskExecutorManager`, pool chosen by `TaskRunnable.Queue`: `standard` (default, 2 to 5 threads), `sequential` (1), `lowPriority` (2), `external` (2 to 5), `aiInteractive` (4), `aiBatch` (2). `AiTaskExecutorService` sizes the AI pools from `AiModule`. `LongRunnable` tasks persist in `o_ex_task` and survive a restart (section 26 below).

## 15. Caching

Infinispan via `Coordinator.getCacher().getCache(type, name)` → cache `type-name`. Configure it in `infinispan-config.xml`; an undeclared cache gets 10,000 entries and 15 min max idle. `put(key, value)` = reload, `update(key, value)` = changed data with cluster invalidation, `put(key, value, lifespan, maxIdleTime)` replaced the old `put(key, value, expirationTime)`. Invalidation: TTL, EventBus-driven, or manual via admin UI (`AllCachesController`).

## 16. Identity & Security Model

- `Identity` = authentication entity (key, login, status, user profile link). Status is an `Integer` constant: `STATUS_PERMANENT` 1, `STATUS_ACTIV` 2, `STATUS_LOGIN_DENIED` 101, `STATUS_PENDING` 102, `STATUS_INACTIVE` 103, `STATUS_DELETED` 199 (from 100 up invisible in searches)
- `Roles` = immutable session object with `RolesByOrganisation`; `OrganisationRoles` include `projectmanager`, `educationmanager`, `selectusmanager`
- `Organisation` = hierarchical tree, role assignments scoped per org
- `GroupRoles` used for course memberships (owner/coach/participant)
- `BaseSecurity` = primary service for identity management; resource grants via `GroupDAO.hasGrant(identity, permission, resource, role)`
- `UserImpl`: 78 `u_*` profile columns. **New in 21.1.0:** property `customerNumber` (`CustomerNumberPropertyHandler`)
- XSS: `OWASPAntiSamyXSSFilter` (implemented with the OWASP Java HTML Sanitizer, not AntiSamy) for HTML sanitization, `$r.escapeHtml()` for output, CSRF tokens on forms
- Security headers via `HeadersFilter` (switches in `CSPModule`): X-Frame-Options, X-Content-Type-Options, CSP (`CSPBuilder` + `CSPDirectiveProvider` beans), HSTS
- **New in 21.1.0:** Encrypt stored secrets with `AesGcmCipher.encrypt(key, value, aad)` (prefix `{aesgcm1}`), not `Encoder.Algorithm.aes`

## 17. AJAX & Poller

User interactions → AJAX POST → dispatch → render dirty components → JSON response with DOM replacements. Server-push via periodic polling (`AjaxController`): 5s initial interval, exponential decay, activity reset, 60min timeout. Dirty components from other users' actions (via EventBus) are picked up by the next poll.

**Callouts (a11y):** trigger `link.setAriaDialogOpener()`; `CloseableCalloutWindowController` sets `aria-expanded` and returns the focus to the trigger. **Changed in 21.1.0:** pass the trigger `Link`/`FormLink` (not a DOM id); the aria label falls back to the trigger title; the focus goes to the first visible element.

## 18. Business Path & Deep Linking

Serializable path like `[RepositoryEntry:123][CourseNode:456]` for bookmarks, deep links, session resumption. Controllers implement `Activateable2.activate(ureq, entries, state)` to restore navigation state. `NewControllerFactory` maps resource types to controller creators.

## 19. Mapper Infrastructure

`Mapper` interface serves dynamic resources (images, files, JSON) via stable URLs.
- `/m/` = session-bound (MapperService), `/g/` = global (GlobalMapperRegistry)
- `registerMapper()` (non-cacheable), `registerCacheableMapper()` (stable URL, browser-cacheable)
- Auto-cleanup on controller dispose and session end; unknown mapper → 404

**Content domain (21.0):** `server.content.domainname` (differs from `server.domainname` → `Settings.isContentDomainNameEnabled()`). `registerSandboxedMapper(ureq, mapper)` returns a `MapperKey`; load `Settings.createContentServerURI() + key.getUrl() + "?token=" + key.getToken()` in an iframe. The one-time token copies identity, roles and locale into a content session (`UserSession.isContentDelivery()`): no X-Frame-Options, CSP `frame-ancestors` = main URI, cookie `SameSite=None; Partitioned`, REST calls 403 except the course DB. Parent sign-off invalidates the sandbox sessions. Reference: `IFrameDisplayController`, `ScormWrapperController`.

## 20. Upgrade Infrastructure (`org.olat.upgrade`)

Two-phase migration system for version upgrades:

1. **Database schema upgrades:** `DatabaseUpgradeManager` runs SQL ALTER scripts from `src/main/resources/database/{mysql,postgresql,oracle}/alter_*.sql` (e.g. `alter_21_0_x_to_21_1_0.sql`, registered as `DatabaseUpgrade` in `databaseUpgradeContext.xml`) early during Spring init, before modules start.
2. **Post-system-init upgrades** — `UpgradeManager` listens for `FrameworkStartedEvent`, then runs data migrations in a background thread via `TaskExecutorManager`. Can `@Autowired` any service.

Key classes: `OLATUpgrade` (base class), `UpgradeHistoryData` (persistent state), `UpgradesDefinitions` (ordered list in Spring XML). Registration in `org/olat/upgrade/_spring/upgradeContext.xml`. Naming: `OLATUpgrade_XX_Y_Z`, version constant `OLAT_XX.Y.Z`. Old table layouts: upgrade-private entities in `org.olat.upgrade.model`, registered in `persistence.xml`.

**Important:** Post-init upgrades run *after* all modules are initialized. Changes to `AbstractSpringModule` configs may need module re-initialization — the module's `init()` has already run with the old config.

**New in 21.1.0:** `OLATUpgrade_21_1_0` pattern for a new secure default: new instances get SSRF protection on; the upgrade switches it off on existing instances unless the key is set explicitly. It also adds `customerNumber` to customized user property contexts.

Pattern: idempotent sub-tasks tracked via `UpgradeHistoryData.setBooleanDataValue()`. Cluster-safe: only singleton node runs upgrades (`UpgradeManagerDummy` on other nodes).

State persisted in `olatdata/system/installed_upgrades.xml` and `installed_database_upgrades.xml`.

## 21. HTTP Client Service

All outbound HTTP requests **must** use `HttpClientService` (`org.olat.core.util.httpclient`). Never use `java.net.http.HttpClient`, other HTTP libraries, or instantiate Apache `HttpClient` directly.

**Changed in 21.1.0:** Every method takes a `ProtectionProfile` (OO-9310):
- `createHttpClient(profile)`, `createHttpClientBuilder(profile)`, `createThreadSafeHttpClient(redirect, profile)`, variants with `(host, port, user, password, ..., profile)` for basic auth
- `USER_PROVIDED` for URLs a user entered (feeds, calendars, external pages, LTI, video, Markdown images): only public addresses, else `FilteredHostException`. Allow lists `http.ssrf.allowed.addresses` / `http.ssrf.allowed.hosts`, switch `http.ssrf.protection.enabled`
- `CONFIGURED` for admin-configured URLs (AI providers, BigBlueButton, Opencast, OnlyOffice)
- Do not set a connection manager on a `USER_PROVIDED` builder (bypasses the DNS filter)

- `@Autowired HttpClientService` in Spring beans and controllers
- Internally commits DB transaction before outbound call to free connection
- Config: `http.connect.timeout`, `http.connect.request.timeout`, `http.connect.socket.timeout` (all 30s default), `http.proxy.*`

## 22. REST API

- Resource: `restapi/` sub-package, `@Component` + `@Path` + `@Tag`, package listed in `restApiContext.xml`; check `getRoles(httpRequest)` first
- **New in 21.1.0:** Audit log `o_api_audit_log` (`ApiAuditResponseFilter`, `ApiAuditLogService`): writes, 401/403/429/5xx; secrets masked by `ApiAuditMasking`; retention job; properties `restapi.auditlog.*`. Access log `org.olat.restapi.access` with marker `Tracing.M_REST`
- **New in 21.1.0:** Rate limiting in `RestApiLoginFilter` (`restapi.ratelimit.*`, off by default): per identity or IP, 60 s window + parallel slots, `429` + `Retry-After`, headers `X-RateLimit-Limit/Remaining/Reset`. Reusable service `RequestRateLimiter` (`acquire()`/`release()` in finally, `check()`); counters per node

## 23. AI Services (`org.olat.core.commons.services.ai`, doc §31)

Feature services (`AiMCQuestionService`, `AiImageDescriptionService`, `AiEssayGradingService`, `AiEssayGenerationService`) own the prompts; SPIs (`OpenAiSPI`, `AnthropicAiSPI`, `GenericAiSpiInstance`, `LocalOnnxSPI`) only build LangChain4j models. `AiModule` maps each `AiFeature` to provider and model. Pass an `AiUsageContext`; every call is logged in `o_ai_usage_log`. Details: `ai/package.md`.

**New in 21.1.0:** AI Act opt-out. Automatic features (`AiFeature.isUserControlled()`: essay grading, image description) run only if `AiUserPreferenceService.isActive(prefs, feature)`: feature on, and user choice `ON` or `DEFAULT` with `ai.feature.<type>.user.default=true`. Explicit AI buttons are not gated. `AiModule.PROTECTION_PROFILE` is `CONFIGURED`.

## 24. Wizards (doc §10)

`StepsMainRunController` (one root `Form`, buttons back/next/finish/cancel) runs a chain of `Step`s (`BasicStep`: `setNextStep()`, `getInitialPrevNextFinishConfig()` with `PrevNextFinishConfig.NEXT`, `BACK_NEXT`, `BACK_FINISH`...). Step controllers extend `StepFormBasicController`, share the root form, pass data via `StepsRunContext` (`addToRunContext()`), advance with `fireEvent(ureq, StepsEvent.ACTIVATE_NEXT)`. Only the finish `StepRunnerCallback` commits and returns `DONE_MODIFIED` / `DONE_UNCHANGED` (parent gets `CHANGED_EVENT` / `DONE_EVENT`). A step controller is recreated on every visit.

**New in 21.1.0:** `CachedRunContextController.of(runContext, key, rootForm, factory, listener)` keeps a sub-form alive across navigation; `release()` in `doDispose()` removes it as sub form listener so a hidden form does not block validation. The wizard propagates dirtiness only for its own navigation items (no full redraw per step event); `setFinishText()`.

## 25. Module Configuration (doc §20)

`AbstractSpringModule`: `afterPropertiesSet()` → load persisted file → `initDefaultProperties()` → `init()`. Saved values (`setXxxProperty(key, value, true)`) go to `{userdata}/system/configuration/{FQCN}.properties` (not `o_property`, which is `PropertyManager` for user/resource properties) and win over `olat.properties`. Save fires `PersistedPropertiesChangedEvent`; other nodes `loadPropertiesFromFile()`, all nodes call `initFromChangedProperties()`. `setSecretStringProperty()` skips the audit log; the `secured` constructor encrypts the file. Upgrades run after `init()` and must use the module setters.

## 26. Persisted Long-Running Tasks (doc §22)

`LongRunnable` (Serializable `TaskRunnable`) → `TaskExecutorManager.execute(task, creator, resource, resSubPath, scheduledDate)` → row in `o_ex_task` (XStream). Status `newTask` → `inWork` (`PersistentTaskDAO.pickTaskForRun()`, row lock, node ID + boot ID) → `done` / `failed`. Started by `ExecutorJob` every 5 min on every node, or at once with `isDelayed() == false`. Restart recovery: `inWork` rows of the same node with an old boot ID run again, so `run()` must be idempotent. Store keys only; get services via `CoreSpringFactory.getImpl()` in `run()`. Events `TaskEvent` on `TaskExecutorManager.TASK_EVENTS`. Full pool → task stays `newTask`.

## 27. Clustering (doc §28)

Config: `cluster.mode` (`SingleVM`/`Cluster`), `node.id`, `cluster.singleton.services`, `jms.broker.url` (embedded Artemis by default). Services via `Coordinator`: `ClusterEventBus` (JMS topic `olat/{instance.id}/sysbus`; every event, also local ones, goes through the broker and is delivered asynchronously on every node; `MultiUserEvent.isEventOnThisNode()`), `ClusterLocker` (`oc_lock`, released on logout), `ClusterSyncer` (`doInSync()`, row lock in `o_plock`), `InfinispanCacher` (local caches only, no replication; L2 cache local too). Per node: caches, REST rate-limit counters, AI pools, session mappers.

## 28. Security Architecture (doc §34)

Map: filters (`SameSiteCookieFilter`, `HeadersFilter`, `RestApiLoginFilter`) → dispatch (CSRF token, URL timestamps, roles) → render (`$r.escapeHtml()`, sanitizer) → content domain for scripted content → outbound `HttpClientService` + `ProtectionProfile`. CSP = `CSPModule` + all `CSPDirectiveProvider` beans. Secrets: `AesGcmCipher`, `setSecretStringProperty()`. Audit: `Tracing.M_AUDIT`, `o_loggingtable`, `o_api_audit_log`, `o_ai_usage_log`.

## 29. Key Conventions

- Always use `listenTo()` for child controllers
- Always `removeAsListenerAndDispose()` before replacing controllers
- Always escape user text in templates with `$r.escapeHtml()`
- Always use JPQL parameters for queries
- Never access `bcroot/` directly — use VFS
- Never do business logic during rendering
- Module classes extend `AbstractSpringModule` for feature toggles
- Test CSS selectors prefixed with `o_sel_`
- Use `Tracing.createLoggerFor()` for logging (returns a Log4j2 `Logger`), never instantiate loggers directly
- Use parameterized log messages: `log.info("msg: {}", val)`
- Choose the `ProtectionProfile` by the origin of the URL; when in doubt `USER_PROVIDED`
