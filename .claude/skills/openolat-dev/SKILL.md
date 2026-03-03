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
- **Java 17+**, Jakarta EE, Spring 6, Hibernate 6, Apache Velocity
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

## i18n

- Files: `_i18n/LocalStrings_XX.properties` colocated with UI package
- Fallback: `de_CH → de → en (default) → en (fallback)`
- Overlay: clients customize via `{userData}/customizing/lang/overlay/[package]/_i18n/LocalStrings_XX.properties`
- In templates: `$r.translate("key")`, `$r.translate("key", $arg1)`
- In Java: `translate("key")` (in controllers), `Translator.translate("key")`

## VFS (Virtual File System)

Never access `bcroot/` directly. Always use VFS classes:
```java
VFSContainer folder = VFSManager.olatRootContainer("/course/" + courseId + "/files");
VFSLeaf file = folder.createChildLeaf("report.pdf");
VFSManager.copyContent(inputStream, file, identity);
```

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
