---
name: openolat-dev
description: Use this skill when developing OpenOlat features, fixing bugs, or writing new controllers, services, forms, or templates. Provides architecture knowledge, patterns, and conventions for the OpenOlat LMS codebase.
allowed-tools: Read, Grep, Glob, Bash(mvn *)
---

# OpenOlat Developer Assistant

You are an expert OpenOlat developer. Use the architecture knowledge below and the reference files to help developers write correct, idiomatic OpenOlat code.

For compressed architecture knowledge, read `.claude/openolat-architecture-knowledge.md`

For detailed architecture documentation, read: `doc/openolat-architecture.md`


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
DefaultController (base, dispose lifecycle)
  └─ BasicController (UI controller, Velocity rendering)
       └─ FormBasicController (FlexiForm support)
            └─ FormLayoutContainer (pure layout)
```

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
tableEl.setSearchEnabled(true);
tableEl.setSelectAllEnable(true);
tableEl.setEmptyTableSettings("icon", "empty.message", null, "create.button");
```

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
- Module pattern: extend `AbstractSpringModule` for feature toggles

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
- **Glossary:** See `doc/openolat-glossary.md` for product-specific term definitions and `doc/openolat-glossary-translations.md` for canonical translations
- **Alphabetical key order (mandatory):** Keys in `LocalStrings_XX.properties` files **must** be sorted alphabetically. When adding new keys, insert them at the correct alphabetical position. When modifying existing keys, keep them in place. Do **not** reorder existing keys unless explicitly told to do so.
- **Wording (mandatory):** Always use the same terms as the OpenOlat application UI. The running product is the source of truth. If multiple terms exist for the same concept, ask the user.
- **Glossary-driven translation (mandatory):** When translating i18n strings between languages, always read `doc/openolat-glossary-translations.md` first. Every glossary term **must** be translated exactly as defined there. If existing translations use different words, flag the inconsistency to the user and offer to fix it.
- **DE is the base language.** When new terms appear that are not in the glossary, ask the user to add the term and provide the base translations before proceeding.
- **Glossary sync:** When `doc/openolat-glossary.md` is updated, the glossary in the OpenOLAT-docs project (`sites/manual_user/docs/general`) must also be updated with the same information.

## VFS (Virtual File System)

Never access `bcroot/` directly. Always use VFS classes:
```java
VFSContainer folder = VFSManager.olatRootContainer("/course/" + courseId + "/files");
VFSLeaf file = folder.createChildLeaf("report.pdf");
VFSManager.copyContent(inputStream, file, identity);
```

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

All outbound HTTP requests **must** use `HttpClientService` (`org.olat.core.util.httpclient.HttpClientService`). Never use `java.net.http.HttpClient`, other HTTP client libraries, or instantiate Apache `HttpClient` directly. The service provides centralized proxy configuration, standardized timeouts, and frees the DB connection before outbound calls.

```java
// In Spring-managed beans
@Autowired
private HttpClientService httpClientService;

// In controllers
HttpClientService httpClientService = CoreSpringFactory.getImpl(HttpClientService.class);

// Simple request
try (CloseableHttpClient httpClient = httpClientService.createHttpClient()) {
    HttpGet request = new HttpGet("https://api.example.com/data");
    try (CloseableHttpResponse response = httpClient.execute(request)) {
        // handle response
    }
}

// Thread-safe pooled client for concurrent use
try (CloseableHttpClient httpClient = httpClientService.createThreadSafeHttpClient(true)) {
    // use for multiple concurrent requests
}
```

**Methods:** `createHttpClient()`, `createHttpClientBuilder()`, `createThreadSafeHttpClient(redirect)`, plus variants with `(host, port, user, password)` for basic auth.

## Security Checklist

- **XSS:** Use `$r.escapeHtml()` for all user text in templates. `$r.render()` is safe.
- **CSRF:** `Form` generates tokens automatically. Always use `FormBasicController` for forms.
- **SQL injection:** Always use JPQL parameters (`:paramName`), never string concatenation.
- **Sanitize HTML:** Use `OWASPAntiSamyXSSFilter` for rich-text content.
- **Velocity SSTI:** Never pass user input as Velocity template content.

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
- **Callouts:** `wControl.pushAsCallout(component)`
- **Info/error messages:** `showInfo("key")`, `showError("key")`
- **Module toggles:** `AbstractSpringModule` with `isEnabled()` and persisted config
- **Toolbar actions:** `toolbarPanel.addTool(link)` for create/export/import buttons
- **Security callbacks:** Pass permission objects to controllers, don't check roles inline

## Writing Upgrades (`org.olat.upgrade`)

For data migrations between versions, create an upgrade class:

```java
public class OLATUpgrade_20_4_0 extends OLATUpgrade {
    private static final String VERSION = "OLAT_20.4.0";
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

Register in `org/olat/upgrade/_spring/upgradeContext.xml` (append to the list). For SQL schema changes, add ALTER scripts to `/database/mysql/` and `/database/postgresql/` and register in `databaseUpgradeContext.xml`.

**Important:** Upgrades run *after* all modules are initialized. Changes to `AbstractSpringModule` configs may need module re-initialization since the module's `init()` has already executed.

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
