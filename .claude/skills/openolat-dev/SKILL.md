---
name: openolat-dev
description: Use this skill when developing OpenOlat features, fixing bugs, or writing new controllers, services, forms, or templates. Provides architecture knowledge, patterns, and conventions for the OpenOlat LMS codebase.
allowed-tools: Read, Grep, Glob, Bash(mvn *)
---

# OpenOlat Developer Assistant

You are an expert OpenOlat developer. Use the architecture knowledge below and the reference files to help developers write correct, idiomatic OpenOlat code.

For compressed architecture knowledge (condensed, covers 21.1.0, same version markers), read `.claude/openolat-architecture-knowledge.md`

For detailed architecture documentation, read: `doc/openolat-architecture.md` (covers release 21.1.0; section numbers below refer to it). Package-level details: `package.md` next to the code, e.g. `src/main/java/org/olat/core/commons/services/ai/package.md`.

Version markers in this skill: `**New in 21.1.0:**` marks a pattern that exists only on the 21.1 code line (master), not on the 21.0 branch.


## Project Basics

- **Build:** `mvn compile -pl :openolat-lms -q`
- **Java 17+**, Jakarta EE, Spring 7, Hibernate 7, Apache Velocity
- **Naming:** Always write "OpenOlat" in prose (not "OpenOLAT"). Java classes keep original casing.
- **License:** Apache 2.0. Developed by frentix GmbH, Zurich, Switzerland.

## Architecture Overview

OpenOlat is a **server-centric** web framework. All UI state lives on the server. The browser receives HTML fragments via AJAX — there is no client-side framework (no React/Angular/Vue).

### Key Layers (top to bottom)
1. **Presentation** — Controllers, Components, Velocity templates
2. **Modules** — Feature modules (course, question pool, portfolio, groups, etc.)
3. **Services** — Business logic (`*Manager` interfaces, Spring beans)
4. **Persistence** — Hibernate/JPA DAOs, `DB` facade, VFS

### Request Lifecycle
```
HTTP Request → Servlet → Dispatcher → Window (synchronized)
→ Find Component by ID → Fire event to Controller
→ Controller executes business logic (DB, services)
→ Controller may fire events to parent controllers
→ Controller may fire MultiUserEvents to EventBus
→ Controller updates component tree (dirty flags)
→ RENDER PHASE (no more business logic, no DB by convention)
→ Dirty components rendered → JSON/HTML response
```

## Controller Patterns

### Controller Hierarchy
```
Controller (interface)
  └─ DefaultController (event dispatch, dispose lifecycle)
       └─ BasicController (listenTo, Velocity, mappers)
            └─ FormBasicController (FlexiForm support)
```
`FormLayoutContainer` is not a controller. It is a `FormItem` container for layout (section 8 of the architecture doc).
Controllers are autowired by the framework: `@Autowired` fields work in controllers too.

### Creating a BasicController
```java
public class MyController extends BasicController {
    @Autowired private MyService myService;

    public MyController(UserRequest ureq, WindowControl wControl) {
        super(ureq, wControl);
        VelocityContainer vc = createVelocityContainer("my_template");
        Link btn = LinkFactory.createButton("save", vc, this);
        putInitialPanel(vc);
    }

    @Override
    protected void event(UserRequest ureq, Component source, Event event) {
        if (source instanceof Link link && "save".equals(link.getCommand())) {
            doSave(ureq);
        }
    }
}
```

### Creating a FormBasicController
```java
public class MyFormController extends FormBasicController {
    private TextElement nameEl;

    public MyFormController(UserRequest ureq, WindowControl wControl) {
        super(ureq, wControl);
        initForm(ureq);
    }

    @Override
    protected void initForm(FormItemContainer layout, Controller listener, UserRequest ureq) {
        nameEl = uifactory.addTextElement("name", "form.name", 255, "", layout);
        nameEl.setMandatory(true);
        uifactory.addFormSubmitButton("save", layout);
    }

    @Override
    protected boolean validateFormLogic(UserRequest ureq) {
        boolean ok = super.validateFormLogic(ureq);
        nameEl.clearError();
        if (!StringHelper.containsNonWhitespace(nameEl.getValue())) {
            nameEl.setErrorKey("form.mandatory");
            ok = false;
        }
        return ok;
    }

    @Override
    protected void formOK(UserRequest ureq) {
        String name = nameEl.getValue();
        // save logic
        fireEvent(ureq, Event.DONE_EVENT);
    }
}
```

### Child Controller Lifecycle
```java
// ALWAYS use listenTo() — ensures automatic disposal
detailCtrl = new DetailController(ureq, getWindowControl(), item);
listenTo(detailCtrl);

// To replace: remove first, then create new
removeAsListenerAndDispose(detailCtrl);
detailCtrl = new DetailController(ureq, getWindowControl(), newItem);
listenTo(detailCtrl);
```

### Handling Child Controller Events
```java
@Override
protected void event(UserRequest ureq, Controller source, Event event) {
    if (source == detailCtrl) {
        if (event == Event.DONE_EVENT) {
            // handle success
        } else if (event == Event.CANCELLED_EVENT) {
            // handle cancel
        }
    }
}
```

## Event System

| Type | Scope | Example |
|------|-------|---------|
| Component → Controller | Same controller | Button click, link click |
| Controller → Parent | Parent via `listenTo()` | `fireEvent(ureq, Event.DONE_EVENT)` |
| Form events | FormBasicController | `formOK()`, `formCancelled()`, `formInnerEvent()` |
| Multi-user (EventBus) | Cross-session, cluster-wide | `coordinatorManager.getCoordinator().getEventBus()` |

**EventBus pattern:**
```java
// Register
OLATResourceable ores = OresHelper.createOLATResourceableInstance("CourseModule", courseId);
coordinatorManager.getCoordinator().getEventBus().registerFor(this, ureq.getIdentity(), ores);

// Fire
coordinatorManager.getCoordinator().getEventBus().fireEventToListenersOf(new MultiUserEvent("changed"), ores);

// MUST deregister in doDispose()!
coordinatorManager.getCoordinator().getEventBus().deregisterFor(this, ores);
```

## Velocity Templates

Templates in `_content/` directory, colocated with the controller's package.

```velocity
## Render a child component
$r.render("myComponent")

## Translate
$r.translate("my.key")
$r.translate("greeting", $userName)

## Escape user content (CRITICAL for XSS prevention)
$r.escapeHtml($userText)

## Conditional rendering
#if($showDetails)
  <div>$r.render("detailPanel")</div>
#end

## Loop
#foreach($item in $items)
  <div>$r.escapeHtml($item.name)</div>
#end
```

**Important:** `$r.render()` is safe (components handle escaping), but `$myVar` from `contextPut()` is **NOT auto-escaped** — always use `$r.escapeHtml($myVar)` for user-provided text.

## FlexiTable

```java
// In initForm():
FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.name));
columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.date,
    new DateFlexiCellRenderer(getLocale())));
columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("edit",
    translate("action.edit"), "edit"));  // action column

tableModel = new MyTableModel(columnsModel);
tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, getTranslator(), layout);
tableEl.setSearchEnabled(true);             // or setSearchEnabled(listProvider, ureq.getUserSession()) for typeahead
tableEl.setSelectAllEnable(true);
tableEl.setEmptyStateConfig(EmptyStateConfig.builder()
        .withIconCss("o_icon_empty").withMessageI18nKey("table.empty").build());
tableEl.setAndLoadPersistedPreferences(ureq, "my-table-v1");

// Default sort
FlexiTableSortOptions options = new FlexiTableSortOptions();
options.setDefaultOrderBy(new SortKey(Cols.name.name(), true));
tableEl.setSortSettings(options);
```

## New Form Elements

- **New in 21.1.0:** `FormSection` structures a long form (section 8 "Form Sections"):
  ```java
  FormSection sec = uifactory.addFormSection("advanced", translate("section.advanced"), formLayout, FormSection.Level.SUB_TITLE);
  sec.setCollapsible(true);
  sec.setCollapsed(true);
  sec.setPersistedStatusId(ureq, "my.form.advanced"); // after setCollapsed()
  uifactory.addTextElement("name", "field.name", 200, "", sec);
  ```
- **New in 21.1.0:** `SearchElement` is the one search field (section 8 "Search Element"). `uifactory.addSearchElement(name, SearchVariant.DEFAULT|LARGE|TYPEAHEAD, formLayout)` or `addSearchElement(name, listProvider, usess, formLayout)`. React to `SearchFormEvent` (`SEARCH`, `RESET`) in `formInnerEvent()`. It does not mark the form dirty. Do not build a search field from `TextElement` + button.
- **New in 21.1.0:** `formItem.setLabelIconCss("o_icon o_icon_locked")` shows an icon next to the label (visual cue only).
- `addRelativeDateElement(name, label, layout, wControl, relativeDateContext)`, `addObjectSelectionElement(...)` with an `ObjectSelectionSource` (`IdentitySelectionSource` for users), `addTwoColumnsFormLayout(...)`.
- **New in 21.1.0:** Components `FactSheet` (`FactSheetFactory`), `Sections` (`SectionsFactory`), `ComponentList`; `$r.sectionHeader(...)` in templates (section 5, 7).

## Database Access

```java
// Service method pattern — EntityManager via ThreadLocal
public MyEntity loadByKey(Long key) {
    return dbInstance.getCurrentEntityManager().find(MyEntity.class, key);
}

// JPQL query
public List<MyEntity> findByName(String name) {
    return dbInstance.getCurrentEntityManager()
        .createQuery("select e from MyEntity e where e.name = :name", MyEntity.class)
        .setParameter("name", name)
        .getResultList();
}

// Persist
public void save(MyEntity entity) {
    dbInstance.getCurrentEntityManager().persist(entity);
}
```

- Session-per-request: framework commits after dispatch
- Aggregate projections: `PersistenceHelper.extractBigDecimal(row, pos, default)` (**New in 21.1.0**)
- Background jobs: must call `dbInstance.commitAndCloseSession()` explicitly
- Bulk ops: use `dbInstance.intermediateCommit()` every ~100 items
- All entities registered in `src/main/resources/META-INF/persistence.xml`

## Spring & Services

```java
// In Spring-managed beans: use @Autowired
@Service
public class MyManagerImpl implements MyManager {
    @Autowired private DB dbInstance;
}

// In controllers (not Spring-managed): use CoreSpringFactory
MyManager mgr = CoreSpringFactory.getImpl(MyManager.class);
```

- Config defaults: `src/main/resources/serviceconfig/olat.properties`
- Local overrides: `olat.local.properties`
- Module pattern: extend `AbstractSpringModule` for feature toggles (section 20): read values in `init()` (persisted value first, `@Value` default second), re-read in `initFromChangedProperties()`, write with `setStringProperty(key, value, true)`. Values live in `{userdata}/system/configuration/{FQCN}.properties` (not `o_property`); a `PersistedPropertiesChangedEvent` reloads them on all nodes. Secrets: `setSecretStringProperty()`.
- Cluster-singleton Quartz job: trigger references `myJob.${cluster.singleton.services}`, beans `myJob.enabled` (job class) and `myJob.disabled` (`DummyJob`) (section 21)
- Async work: a `TaskRunnable`/`LongRunnable` on `TaskExecutorManager`, select the pool with `TaskRunnable.Queue` (`standard`, `sequential`, `lowPriority`, `external`, `aiInteractive`, `aiBatch`)
- Work that must survive a restart: implement `LongRunnable` (section 22). Keep only keys and simple values (XStream-serialized into `o_ex_task`), look up services with `CoreSpringFactory.getImpl()` in `run()`, make `run()` safe to run twice, return `isDelayed() == false` to start at once. Submit with `taskExecutorManager.execute(task, identity, resource, resSubPath, null)`.
- Cluster (section 28): caches, counters and pools are per node; EventBus events go through JMS to every node (asynchronous, `Serializable`, small). Cross-node exclusivity: `Locker` (`oc_lock`) for user locks, `Syncer.doInSync()` (`o_plock` row lock) for short critical sections, `${cluster.singleton.services}` for single-node jobs.
- Caches: `coordinatorManager.getCoordinator().getCacher().getCache("MyService", "things")` → cache `MyService-things`; declare size/expiry in `infinispan-config.xml` (default 10,000 entries, 15 min idle). `put(key, value, lifespan, maxIdle)` replaced `put(key, value, expiration)`.

## AI Services (`org.olat.core.commons.services.ai`)

Use the feature services (`AiMCQuestionService`, `AiImageDescriptionService`, `AiEssayGradingService`, `AiEssayGenerationService`), never an SPI or a LangChain4j model directly. Check `isEnabled()` first. Pass an `AiUsageContext` (`AiUsageContext.builder()...build()`) so the call is logged in `o_ai_usage_log`. Long work runs as `LongRunnable` on the `aiInteractive`/`aiBatch` queues. New feature: follow "Adding a New AI Feature" in the AI `package.md`.

**New in 21.1.0:** An AI call that runs automatically (without an explicit click) must respect the person's choice (AI Act, OO-9784):
```java
if (aiUserPreferenceService.isActive(ureq.getUserSession().getGuiPreferences(), AiFeature.ImageDescriptionGenerator)) {
    // start the automatic AI work
}
```
Mark a new automatic feature `userControlled` in `AiFeature` and gate it where the call starts. Explicit "Generate with AI" buttons are not gated. Details: architecture doc section 31.
## Wizards (section 10)

```java
wizardCtrl = new StepsMainRunController(ureq, getWindowControl(), new MyStep00(ureq), finishCallback, null,
        translate("wizard.title"), "o_sel_my_wizard");
listenTo(wizardCtrl);
getWindowControl().pushAsModalDialog(wizardCtrl.getInitialComponent());
```
- Steps extend `BasicStep` (`setNextStep()`, `getInitialPrevNextFinishConfig()`, `getStepController()`); step controllers extend `StepFormBasicController` and use the shared root form.
- Pass data with `addToRunContext()` / `getFromRunContext()`; advance with `fireEvent(ureq, StepsEvent.ACTIVATE_NEXT)`.
- Commit only in the finish `StepRunnerCallback`; return `StepsMainRunController.DONE_MODIFIED` or `DONE_UNCHANGED`. The parent gets `CHANGED_EVENT` / `DONE_EVENT` / `CANCELLED_EVENT`.
- **New in 21.1.0:** keep an expensive sub-form alive across navigation with `CachedRunContextController.of(runContext, key, rootForm, factory, this)`; call `release(this)` in `doDispose()`.

## i18n (Internationalization)

- **Files:** `_i18n/LocalStrings_XX.properties` colocated with UI package (Java `.properties` format)
- **Fallback chain:** `de_CH__customizing → de_CH → de__customizing → de → en__customizing → en (default) → en (fallback)`
- **Overlay:** clients customize via `{userData}/customizing/lang/overlay/{package}/_i18n/LocalStrings_XX__customizing.properties`
- **In templates:** `$r.translate("key")`, `$r.translate("key", $arg1)`
- **In Java:** `translate("key")` (in controllers), `translate("key", new String[]{arg})`
- **Cross-referencing translations:**
  - Same-package: `$\:other.key` — references another key in the same `.properties` file
  - Cross-package: `$org.olat.other.package:other.key` or `${org.olat.other.package:other.key}`
  - Recursive resolution up to 10 levels deep
- **Fallback bundles:** `org.olat.core` (core), `org.olat` (application) — checked when key not found in primary bundle
- **Parameter substitution:** `{0}`, `{1}` etc. via `MessageFormat`. Escape single quotes as `''`
- **Gender strategy:** `Benutzer{in}` → converted per locale config (star `*`, colon `:`, etc.)
- **Core classes:** `I18nModule` (config), `I18nManager` (resolution/caching), `PackageTranslator` (per-controller)
- **Glossary:** Product-specific term definitions live in the docs repo at `OpenOLAT-docs/sites/reference_glossary/docs/glossary.md` (EN) and `glossary.de.md` (DE). Translator-facing term mappings across all languages live in `doc/i18n-translation-reference.yaml` in this repo (`doc/i18n-translation-reference.md` is its readable view). Both are generated from the OpenOlat concept map in `fxIntelligence/knowledge/openolat/concept-map/`; never edit them by hand.
- **Alphabetical key order (mandatory):** Keys in `LocalStrings_XX.properties` files **must** be sorted alphabetically. When adding new keys, insert them at the correct alphabetical position. When modifying existing keys, keep them in place. Do **not** reorder existing keys unless explicitly told to do so.
- **Wording (mandatory):** Always use the same terms as the OpenOlat application UI. The running product is the source of truth. If multiple terms exist for the same concept, ask the user.
- **Translation-reference-driven translation (mandatory):** When translating i18n strings between languages, always read `doc/i18n-translation-reference.yaml` first. Every term listed there **must** be translated exactly as defined; `keep_apart` and `never` on an entry name the traps. If existing translations use different words, flag the inconsistency to the user and offer to fix it.
- **DE is the base language.** When new terms appear that are not in the translation reference, ask the user to add the term and provide the base translations before proceeding.
- **Glossary sync:** When a term changes, change it in the concept map and rerun its generators; the manual glossary and `doc/i18n-translation-reference.yaml` follow from there.

## VFS (Virtual File System)

Never access `bcroot/` directly. Always use VFS classes:
```java
VFSContainer folder = VFSManager.olatRootContainer("/course/" + courseId + "/files");
VFSLeaf file = folder.createChildLeaf("report.pdf");
VFSManager.copyContent(inputStream, file, identity);
```

## Mappers

- `registerMapper(ureq, mapper)` / `registerCacheableMapper(ureq, id, mapper[, seconds])` in `BasicController`; cleanup is automatic.
- User content that may run scripts (HTML pages, SCORM): deliver it with `registerSandboxedMapper(ureq, mapper)` from the content domain when `Settings.isContentDomainNameEnabled()`; append `"?token=" + mapperKey.getToken()` to the URL. The content session gets no REST access and relaxed framing headers. See section 12 "Sandboxed Mappers & Content Domain" and `IFrameDisplayController`.

## New File Header (mandatory)

Every new Java file must include the standard license header and a class-level Javadoc with the initial date and `@author` tag. The username is mandatory; the email is optional. The author for AI-generated code is `AI, ai@frentix.com, https://www.frentix.com`.

```java
/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.example;

import ...;

/**
 * Initial date: 9 Apr 2026<br>
 * @author AI, ai@frentix.com, https://www.frentix.com
 */
public class ExampleController extends BasicController {
```

## Code Style Rules

- **No pure whitespace changes:** Never change invisible characters (spaces, tabs) on lines where the only modification is the whitespace itself. Whitespace may be changed on lines where actual code is also being modified. This keeps diffs clean and avoids unnecessary merge conflicts.
- **No inline fully-qualified class names:** Always import classes and use the simple name. Never write `java.io.File`, `java.util.HashMap<>()`, `org.olat.core.util.vfs.VFSLeaf` etc. inline in method bodies. Add the import statement and use the class name only. The only exception is when two classes from different packages have the same name — in that case, use the FQN for the less-frequently-used one.

## HTTP Client Service

All outbound HTTP requests **must** use `HttpClientService` (`org.olat.core.util.httpclient.HttpClientService`). Never use `java.net.http.HttpClient`, other HTTP client libraries, or instantiate Apache `HttpClient` directly. The service provides centralized proxy configuration, standardized timeouts, SSRF protection, and frees the DB connection before outbound calls.

**New in 21.1.0:** Every factory method takes a `ProtectionProfile` (OO-9310):
- `ProtectionProfile.USER_PROVIDED` for any URL a user entered (feeds, calendars, external pages, images, LTI, video). Private, loopback and link-local addresses are blocked (`FilteredHostException`), except the allow lists `http.ssrf.allowed.addresses` / `.hosts`.
- `ProtectionProfile.CONFIGURED` for URLs an administrator configured (AI providers, BigBlueButton, Opencast, OnlyOffice...).
- When in doubt use `USER_PROVIDED`. Do not set a connection manager on a `USER_PROVIDED` builder (it bypasses the DNS filter).

```java
// In Spring-managed beans
@Autowired
private HttpClientService httpClientService;

// In controllers
HttpClientService httpClientService = CoreSpringFactory.getImpl(HttpClientService.class);

// Simple request
try (CloseableHttpClient httpClient = httpClientService.createHttpClient(ProtectionProfile.CONFIGURED)) {
    HttpGet request = new HttpGet("https://api.example.com/data");
    try (CloseableHttpResponse response = httpClient.execute(request)) {
        // handle response
    }
}

// Thread-safe pooled client for concurrent use
try (CloseableHttpClient httpClient = httpClientService.createThreadSafeHttpClient(true, ProtectionProfile.USER_PROVIDED)) {
    // use for multiple concurrent requests
}
```

**Methods:** `createHttpClient(profile)`, `createHttpClientBuilder(profile)`, `createThreadSafeHttpClient(redirect, profile)`, plus variants with `(host, port, user, password, ..., profile)` for basic auth.

## Security Checklist

- **XSS:** Use `$r.escapeHtml()` for all user text in templates. `$r.render()` is safe.
- **CSRF:** `Form` generates tokens automatically. Always use `FormBasicController` for forms.
- **SQL injection:** Always use JPQL parameters (`:paramName`), never string concatenation.
- **Sanitize HTML:** Use `OWASPAntiSamyXSSFilter` for rich-text content (OWASP Java HTML Sanitizer despite the name).
- **Velocity SSTI:** Never pass user input as Velocity template content.
- **SSRF:** Use `ProtectionProfile.USER_PROVIDED` for user-entered URLs (see HTTP Client Service).
- **Secrets at rest:** Encrypt tokens that must be readable later with `AesGcmCipher.encrypt(key, value, ownerId)` (**New in 21.1.0**), not `Encoder.Algorithm.aes`. Never log secrets.
- **Untrusted content:** Scripted user content goes through a sandboxed mapper on the content domain (see Mappers).

## Disposal Checklist

Override `doDispose()` when your controller:
- Registers EventBus listeners → `eventBus.deregisterFor(this, ores)`
- Acquires locks → `locker.releaseLock(lockEntry)`
- Holds heavy references → set to `null`

You do NOT need to manually dispose:
- Child controllers registered via `listenTo()` (automatic)
- Mappers registered via `registerMapper()` (automatic)
- Disposable form items (automatic)

## Common Patterns

- **Breadcrumb navigation:** `BreadcrumbedStackedPanel` for drill-down views
- **Modal dialogs:** `wControl.pushAsModalDialog(component)`
- **Callouts:** `trigger.setAriaDialogOpener()` + `new CloseableCalloutWindowController(ureq, wControl, content, triggerLink, title, true, "", settings)`, then `listenTo()` and `activate()`. Pass the `Link`/`FormLink` (**New in 21.1.0**), not a DOM id: the controller handles `aria-expanded`, focus and the aria label (section 15)
- **Info/error messages:** `showInfo("key")`, `showError("key")`
- **Module toggles:** `AbstractSpringModule` with `isEnabled()` and persisted config
- **Toolbar actions:** `toolbarPanel.addTool(link)` for create/export/import buttons
- **Security callbacks:** Pass permission objects to controllers, don't check roles inline

## Writing Upgrades (`org.olat.upgrade`)

For data migrations between versions, create an upgrade class:

```java
public class OLATUpgrade_21_2_0 extends OLATUpgrade {
    private static final String VERSION = "OLAT_21.2.0";
    private static final String MIGRATE_DATA = "MIGRATE DATA";

    @Autowired
    private MyService myService;

    @Override
    public String getVersion() { return VERSION; }

    @Override
    public boolean doPostSystemInitUpgrade(UpgradeManager upgradeManager) {
        UpgradeHistoryData uhd = upgradeManager.getUpgradesHistory(VERSION);
        if (uhd == null) {
            uhd = new UpgradeHistoryData();
        } else if (uhd.isInstallationComplete()) {
            return false;
        }

        boolean allOk = true;
        allOk &= migrateData(upgradeManager, uhd);

        uhd.setInstallationComplete(allOk);
        upgradeManager.setUpgradesHistory(uhd, VERSION);
        return allOk;
    }

    private boolean migrateData(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
        if (uhd.getBooleanDataValue(MIGRATE_DATA)) {
            return true;  // Already done
        }
        // ... migration logic, use @Autowired services ...
        uhd.setBooleanDataValue(MIGRATE_DATA, true);
        upgradeManager.setUpgradesHistory(uhd, VERSION);
        return true;
    }
}
```

Register in `org/olat/upgrade/_spring/upgradeContext.xml` (append to the list). For SQL schema changes, add the ALTER statements to `src/main/resources/database/mysql/`, `.../postgresql/` and `.../oracle/` (e.g. `alter_21_0_x_to_21_1_0.sql`) and register a `DatabaseUpgrade` bean in `databaseUpgradeContext.xml`. New entities go into `persistence.xml`.

A new secure default that would change the behaviour of existing instances: enable it for new installs, and switch it off in the upgrade unless it is set explicitly (pattern: `OLATUpgrade_21_1_0.disableSSRFOnExistingInstances`).

**Important:** Upgrades run *after* all modules are initialized. Changes to `AbstractSpringModule` configs may need module re-initialization since the module's `init()` has already executed.

## REST API

- Resource class in the `restapi/` sub-package with `@Component` + `@Path` + `@Tag`; list the package in `org/olat/restapi/_spring/restApiContext.xml`. Check `getRoles(httpRequest)` first (section 37 "REST API Conventions").
- **New in 21.1.0:** Audit log and rate limiting are automatic (`RestApiLoginFilter`, `ApiAuditResponseFilter`, table `o_api_audit_log`, logger `org.olat.restapi.access`). Resource code needs nothing. Secret JSON keys (`password`, `token`, `secret`, ...) are masked; do not invent new secret field names without adding them to `ApiAuditMasking`.
- **New in 21.1.0:** Clients must handle `429` with `Retry-After`. For your own throttling reuse `RequestRateLimiter` (`acquire()`/`release()` in `finally`, `check()` per 60 s window).

## Testing

| Level | Base Class | What it tests |
|-------|-----------|---------------|
| Unit | Plain JUnit | Pure logic, no Spring |
| Integration | `OlatTestCase` | Spring context + DB with rollback |
| REST API | `OlatRestTestCase` | Full HTTP stack |
| Selenium | `@RunWith(Arquillian.class)` | Browser UI tests |

- Test CSS selectors: prefixed with `o_sel_` for automation
- Test helper: `JunitTestHelper` for creating users, courses, fixtures
- Selenium helper: `OOGraphene` for wait helpers (`waitBusy()`, `waitElement()`)
