# OpenOlat Architecture Knowledge Base

> Compressed reference for developers working with Claude Code on the OpenOlat codebase.
> For full documentation see `openolat-architecture.html` and `openolat-architecture.md`.
> For a Claude Code skill, copy `openolat-dev-skill/` to `.claude/skills/`.

## 1. Core Concepts

**Server-centric architecture.** All UI state lives on the server. No client-side framework. Browser receives HTML fragments via AJAX. The component tree is a server-side object graph.

**Session-per-request.** Each HTTP request gets its own Hibernate `EntityManager` via `ThreadLocal`. Framework commits after dispatch. Between requests, all entities are detached.

**Synchronized window.** Each browser tab is a `Window` object. All dispatches to a window are synchronized — prevents race conditions from double-click or concurrent requests.

## 2. Request Flow

```
Browser → Servlet → Dispatcher (by URL prefix)
  /auth/   → AuthenticatedDispatcher (main app)
  /dmz/    → DMZDispatcher (login, registration)
  /raw/    → StaticMediaDispatcher (JS, CSS, images, themes)
  /m/      → MapperDispatcher (dynamic per-session resources)
  /g/      → GlobalMapperRegistry (shared dynamic resources)
  /url/    → RESTDispatcher (external deep links)
  /webdav/ → WebDAV access
  /lti/, /bigbluebutton/, /certificate/, /badge/ → special handlers

Within AuthenticatedDispatcher:
  Window.dispatchRequest() → find Component by dispatch ID → component.dispatchRequest()
  → fires Event to controller → controller.event() → business logic → dirty flags
  → RENDER: traverse tree, render dirty components → JSON response with HTML fragments
```

**Critical rule:** During rendering, all business logic must be complete. No DB access during render phase (by convention — the connection is technically open but must not be used).

## 3. Controller Hierarchy

```
DefaultController          — base: dispose lifecycle, DISPOSE_LOCK
  BasicController          — adds VelocityContainer, listenTo(), WindowControl
    FormBasicController    — adds FlexiForm: initForm/validate/formOK/formInnerEvent
```

**Controller communication:**
- Child → Parent: `fireEvent(ureq, Event.DONE_EVENT)` — parent receives in `event(ureq, Controller, Event)`
- Component → Controller: button clicks etc. — received in `event(ureq, Component, Event)`
- Cross-session: `EventBus.fireEventToListenersOf(MultiUserEvent, OLATResourceable)`

**Lifecycle:** `listenTo(childCtrl)` registers for events AND ensures automatic disposal. `removeAsListenerAndDispose(ctrl)` for replacement. Override `doDispose()` for manual cleanup (EventBus, locks, heavy refs).

## 4. Component Model

Every visible UI element is a `Component`. Components render themselves via `ComponentRenderer`. The tree has:
- `VelocityContainer` — renders `.html` Velocity template, children accessed via `$r.render("name")`
- `Panel` — single-child container, swappable content
- `Link` — clickable element (button or link)
- `FormItem` — FlexiForm elements (TextElement, SingleSelection, etc.)

**Dirty flag mechanism:** When a component changes, `setDirty(true)` propagates up the tree. Only dirty subtrees are re-rendered in AJAX responses.

## 5. FlexiForm System

Two-phase: `initForm()` builds the form structure, then `validateFormLogic()` + `formOK()` handle submission.

Key form items: `TextElement`, `TextAreaElement`, `RichTextElement`, `SingleSelection` (dropdown/radio), `MultipleSelectionElement` (checkboxes), `DateChooser`, `FileElement`, `StaticTextElement`, `FormToggle`, `FormLink`.

Layout containers: `FormLayoutContainer.createDefaultFormLayout()` (label-field pairs), `createHorizontalFormLayout()`, `createCustomFormLayout()` (Velocity template), `createButtonLayout()`.

## 6. FlexiTable

Data-driven table component with server-side sorting, filtering, pagination.

Pattern: Define columns enum implementing `FlexiColumnDef`, create `FlexiTableDataModel<Row>` subclass, add `FlexiTableElement` via `uifactory.addTableElement()`.

Features: search, filters, column chooser, batch actions, empty state, classic (HTML table) and custom (Velocity-based card layout) renderers.

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
- Entities in `src/main/resources/META-INF/persistence.xml` (~400+ classes)
- Use `join fetch` to prevent LazyInitializationException
- Bulk ops: `intermediateCommit()` every ~100 items
- Background jobs: must `commitAndCloseSession()` explicitly
- Connection pool: HikariCP. L2 cache: Infinispan.

## 10. VFS (Virtual File System)

All file access through VFS, never direct filesystem. `bcroot/` is the physical storage root.

- `VFSContainer` = directory, `VFSLeaf` = file
- `VFSManager.olatRootContainer(path)` — resolve from bcroot
- Security callbacks control read/write/delete per container
- Metadata in `o_vfs_metadata` table. Versioning, quotas, trash handled transparently.

## 11. Spring & Configuration

- Hybrid: annotations (`@Service`, `@Autowired`) for new code, XML for legacy
- Main context: `src/main/java/org/olat/_spring/mainContext.xml` (imports 70+ module contexts)
- In controllers: `CoreSpringFactory.getImpl(MyManager.class)` (not Spring-managed)
- Config: `olat.properties` (defaults) + `olat.local.properties` (overrides)
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

**Stats:** ~382 i18n files, ~29,000 English keys, 36 language variants. ~2,000 existing cross-references (851 same-package, 1,171 cross-package).

## 13. Theming

SASS-based. Themes in `src/main/webapp/static/themes/`. `light/` is the base theme (includes Bootstrap). Product themes override `_config.scss` variables only. Compiled via `compiletheme.sh`. Custom themes via `guiCustomThemePath`.

## 14. Scheduler & Background Tasks

Quartz for periodic tasks (no `@Scheduled`). Config: `schedulerContext.xml`, 5-thread pool. Base class: `JobWithDB` (auto commit/rollback). Async execution: `TaskExecutorManager` with multiple thread pools.

## 15. Caching

Infinispan via `Coordinator.getCacher()`. Per-type caches configured in `infinispan-config.xml`. Invalidation: TTL, EventBus-driven, or manual via admin UI.

## 16. Identity & Security Model

- `Identity` = authentication entity (key, login, status, user profile link)
- `Roles` = immutable session object with `RolesByOrganisation`
- `Organisation` = hierarchical tree, role assignments scoped per org
- `GroupRoles` used for course memberships (owner/coach/participant)
- `BaseSecurity` = primary service for identity management
- XSS: AntiSamy for HTML sanitization, `$r.escapeHtml()` for output, CSRF tokens on forms
- Security headers: X-Frame-Options, CSP, HSTS via `HeadersFilter`

## 17. AJAX & Poller

User interactions → AJAX POST → dispatch → render dirty components → JSON response with DOM replacements. Server-push via periodic polling (`AjaxController`): 5s initial interval, exponential decay, activity reset, 60min timeout. Dirty components from other users' actions (via EventBus) are picked up by the next poll.

## 18. Business Path & Deep Linking

Serializable path like `[RepositoryEntry:123][CourseNode:456]` for bookmarks, deep links, session resumption. Controllers implement `Activateable2.activate(ureq, entries, state)` to restore navigation state. `NewControllerFactory` maps resource types to controller creators.

## 19. Mapper Infrastructure

`Mapper` interface serves dynamic resources (images, files, JSON) via stable URLs.
- `/m/` = session-bound (MapperService), `/g/` = global (GlobalMapperRegistry)
- `registerMapper()` (non-cacheable), `registerCacheableMapper()` (stable URL, browser-cacheable)
- Auto-cleanup on controller dispose and session end

## 20. Upgrade Infrastructure (`org.olat.upgrade`)

Two-phase migration system for version upgrades:

1. **Database schema upgrades** — `DatabaseUpgradeManager` runs SQL ALTER scripts (`/database/{dialect}/alter_*.sql`) early during Spring init, before modules start.
2. **Post-system-init upgrades** — `UpgradeManager` listens for `FrameworkStartedEvent`, then runs data migrations in a background thread via `TaskExecutorManager`. Can `@Autowired` any service.

Key classes: `OLATUpgrade` (base class), `UpgradeHistoryData` (persistent state), `UpgradesDefinitions` (ordered list in Spring XML). Registration in `org/olat/upgrade/_spring/upgradeContext.xml`. Naming: `OLATUpgrade_XX_Y_Z`, version constant `OLAT_XX.Y.Z`.

**Important:** Post-init upgrades run *after* all modules are initialized. Changes to `AbstractSpringModule` configs may need module re-initialization — the module's `init()` has already run with the old config.

Pattern: idempotent sub-tasks tracked via `UpgradeHistoryData.setBooleanDataValue()`. Cluster-safe: only singleton node runs upgrades (`UpgradeManagerDummy` on other nodes).

State persisted in `olatdata/system/installed_upgrades.xml` and `installed_database_upgrades.xml`.

## 21. Key Conventions

- Always use `listenTo()` for child controllers
- Always `removeAsListenerAndDispose()` before replacing controllers
- Always escape user text in templates with `$r.escapeHtml()`
- Always use JPQL parameters for queries
- Never access `bcroot/` directly — use VFS
- Never do business logic during rendering
- Module classes extend `AbstractSpringModule` for feature toggles
- Test CSS selectors prefixed with `o_sel_`
- Use `Tracing.createLoggerFor()` for logging, never instantiate loggers directly
- Use parameterized log messages: `log.info("msg: {}", val)`
