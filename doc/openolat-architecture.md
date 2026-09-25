# OpenOlat Architecture Reference

A comprehensive guide to the architecture, design patterns, and internal workings of the OpenOlat Learning Management System.

**Covers release:** OpenOlat 21.1.0 · **Document version:** 2.0 · **Last updated:** 2026-09-25

> **Version markers:** `**New in 21.1.0**` under a heading marks a new mechanism. `**New in 21.1.0:**` or `**Changed in 21.1.0:**` at the start of a paragraph marks a change inside a section. Mechanisms that shipped with 21.0.x carry no marker, except where the reader needs the version. See section 39 for the list of changes in this revision.

## 1. Overview & Project Structure

OpenOlat is a full-featured Learning Management System built as a monolithic Java web application. It follows a **server-centric architecture** — an established approach also used by frameworks like Vaadin, JSF, and Wicket — where the server maintains the complete UI state for each user session, renders HTML via Apache Velocity templates, and delivers incremental DOM updates over AJAX. The browser acts as a thin rendering layer, while all UI logic, state management, and security enforcement happen on the server.

This design has significant implications. Every user interaction — a button click, a dropdown change, a table sort — triggers a round-trip to the server, where the framework locates the target UI component, dispatches the event to the responsible controller, and re-renders only the parts of the page that changed. The server holds the entire component tree in memory, synchronized per browser window, ensuring that the UI state is always consistent and authoritative.

### Technology Stack

- **Build**: Maven (`mvn compile -pl :openolat-lms -q`), Java 17 target (`targetJdk` in `pom.xml`)
- **Dependency Injection**: Spring Framework 7 (XML + annotation-based)
- **ORM**: Hibernate 7 / JPA via Jakarta Persistence
- **Templating**: Apache Velocity (server-side rendering)
- **Frontend**: Server-rendered HTML + custom AJAX partial update protocol
- **CSS**: SASS-based theming with Bootstrap integration
- **Databases**: MySQL, PostgreSQL, Oracle
- **AI**: LangChain4j (chat and embedding models, see section 19)

Key library versions on the 21.1.0 code line (from `pom.xml` properties):

| Library | Version | Property |
|---------|---------|----------|
| Spring Framework | 7.0.9 | `org.springframework.version` |
| Hibernate ORM | 7.4.10.Final | `org.hibernate.version` |
| Hibernate Validator | 9.1.4.Final | `org.hibernate.validator.version` |
| Infinispan | 16.2.3 | `org.infinispan.version` |
| Apache CXF (REST) | 4.2.3 | `apache.cxf` |
| Jackson | 2.22.3 | `jackson.version` |
| Undertow core / Undertow EE servlet (test scope) | 2.4.3.Final / 2.0.2.Final | `io.undertow` / `io.undertow.ee` |
| LangChain4j | 1.20.0 | `dev.langchain4j` |
| Log4j2 / SLF4J | 2.26.1 / 2.0.20 | `apache.log4j` / `org.slf4j` |
| Velocity / Quartz / XStream | 2.4.1 / 2.5.2 / 1.4.21 | inline versions |
| PostgreSQL / MySQL JDBC | 42.7.13 / 26.7.0 | `org.postgresql.version` / `com.mysql.version` |

These versions come from the regular dependency updates (OO-9367, OO-9656). The 21.0 branch carries the same versions. The Maxima CAS integration and the Nashorn engine are removed (OO-9587, OO-9637).

### Package Layout

The codebase follows a layered package structure. The `org.olat.core` package contains the framework itself — the MVC engine, persistence layer, VFS, i18n, and security infrastructure. The `org.olat.modules` package contains self-contained feature modules (question pools, taxonomy, lectures, etc.), each with their own Spring context, controllers, and persistence. The `org.olat.course` package is the course engine — one of the largest subsystems — handling course structure, node types, conditions, and assessment.

```
org.olat.core.*              Core framework
  core.gui.*                 UI framework (components, controllers, renderers)
  core.commons.persistence   DB layer (DB, DBImpl, JPA helpers)
  core.commons.services.*    Shared services (AI, notifications, comments, etc.)
  core.util.vfs              Virtual file system abstraction
  core.dispatcher            HTTP dispatchers (DMZ, Auth, Mapper)

org.olat.modules.*           Feature modules (qpool, taxonomy, lecture, etc.)
org.olat.course.*            Course engine (nodes, conditions, assessment)
org.olat.basesecurity        Authentication, authorization, identity management
org.olat.repository          Repository entries (courses, resources)
org.olat.restapi             REST API (JAX-RS via CXF)
```

### Convention-Based Resource Colocation

A distinctive feature of OpenOlat's codebase is that resources are **colocated with the Java source**. Each package that contains UI controllers also contains subdirectories for its Velocity templates, translation files, and Spring configuration. This keeps related files close together and makes it easy to find all resources belonging to a feature.

```
src/main/java/org/olat/modules/myfeature/
  ui/
    MyController.java             ← Controller
    _content/
      mypage.html                 ← Velocity template
    _i18n/
      LocalStrings_en.properties  ← English translations
      LocalStrings_de.properties  ← German translations
  manager/
    MyServiceImpl.java            ← Business logic
  model/
    MyEntity.java                 ← JPA entity
  _spring/
    myfeatureContext.xml           ← Spring wiring
```

---

## 2. Architectural Layers

OpenOlat's architecture can be understood as a stack of layers, each building upon the one below. The foundation layer provides cross-cutting infrastructure. The persistence layer handles data storage and retrieval. The service layer contains business logic organized into modules. The UI layer presents the user interface through the custom MVC framework. And the API layer exposes functionality to external systems.

```
┌─────────────────────────────────────────────────────────────────────┐
│  API LAYER     REST API (CXF/JAX-RS) · WebDAV · LTI 1.3 · SCORM   │
├─────────────────────────────────────────────────────────────────────┤
│  UI LAYER      Controllers · Components · Velocity · FlexiForm     │
│                FlexiTable · AJAX Engine · Themes/SASS              │
├─────────────────────────────────────────────────────────────────────┤
│  MODULES       Course Engine · Assessment · Repository · Curriculum  │
│                Groups · Portfolio · QPool · Lectures · Taxonomy      │
├─────────────────────────────────────────────────────────────────────┤
│  SERVICES      RepositoryService · UserManager · GroupManager       │
│                MailManager · CalendarManager · AI · Search (Lucene) │
├─────────────────────────────────────────────────────────────────────┤
│  PERSISTENCE   Hibernate/JPA · DB/DBImpl Facade · VFS (Files)      │
│                HikariCP Pool · Infinispan L2 Cache                  │
├─────────────────────────────────────────────────────────────────────┤
│  FOUNDATION    Spring DI · Servlet Container · Dispatcher Routing   │
│                i18n · Security/Auth · Configuration · Logging       │
└─────────────────────────────────────────────────────────────────────┘
```

Understanding these layers helps orient developers within the codebase:

- **Foundation** provides the runtime infrastructure: the servlet container receives HTTP requests, the `DispatcherModule` routes them to the correct handler, Spring manages dependency injection across all modules, and the security layer enforces authentication and authorization. Configuration flows from `olat.properties` through Spring property placeholders into module beans.

- **Persistence** abstracts all data storage. The `DB` facade wraps Hibernate's `EntityManager` in a thread-local pattern, so any code can call `DBFactory.getInstance().getCurrentEntityManager()` to get the current transaction's entity manager. The VFS (Virtual File System) provides a uniform interface for file storage, whether files live on the local filesystem or are backed by metadata in the database.

- **Services** are stateless Spring beans that encapsulate business operations. They are the primary integration point — controllers call services, services call the persistence layer. Services are accessed via `CoreSpringFactory.getImpl(ServiceInterface.class)` or via `@Autowired` injection.

- **Modules** implement the LMS domain logic: courses with their node types and conditions, the assessment pipeline, the repository for learning resources, curriculum management, groups, portfolio, question pools, lectures with attendance tracking, and more. Each module is largely self-contained with its own Spring context, entities, services, and UI controllers. Modules live primarily in `org.olat.modules.*` and `org.olat.course.*`.

- **UI Layer** is the custom MVC framework. Controllers manage user interaction, components model the UI widget tree, Velocity templates render HTML, and the AJAX engine delivers incremental updates. This layer is unique to OpenOlat and is documented in detail in the following sections.

- **API Layer** exposes OpenOlat's functionality to external systems. The REST API (built on Apache CXF / JAX-RS) provides CRUD operations for most domain objects. WebDAV allows file-based access. LTI 1.3 enables tool integration with other LMS platforms. SCORM and IMS QTI 2.1 support standardized e-learning content and assessments.

> **Key principle:** Dependencies flow downward. UI controllers depend on services, services depend on persistence, but never the reverse. This layering ensures that business logic is independent of the UI and can be accessed equally from REST endpoints, background jobs, or the web interface.

---

## 3. HTTP Request Lifecycle

Every user interaction in OpenOlat — whether clicking a button, submitting a form, or navigating to a new page — follows the same fundamental request lifecycle. Understanding this flow is essential for working with the framework, as it explains how a browser click travels through the system to update the UI.

### The Big Picture

The request lifecycle has three main phases: **routing** (finding the right handler), **dispatch** (executing the business logic), and **rendering** (producing the response). The entire cycle is designed around server-side state: the server maintains the complete component tree for each browser window, and each request targets a specific component within that tree by its unique ID.

The dispatch phase is where the real work happens. The target component translates the HTTP request into a semantic event and fires it to its listening controller. The controller then executes business logic — calling services, updating data, loading models. Crucially, the controller may also **fire events to parent controllers** (e.g., `fireEvent(ureq, Event.DONE_EVENT)`), causing a chain of reactions up the controller hierarchy. It may also fire **multi-user events** via the EventBus to notify other user sessions. Only when this entire event chain has completed — all business logic executed, all models loaded, all component state updated — does the render phase begin. During rendering, the framework simply reads the already-prepared component state and produces HTML. **No business logic, no service calls, and no database access should occur during rendering.**

```
┌─ ROUTING ──────────┐  ┌─ DISPATCH (EVENT CHAIN) ──┐  ┌─ RENDER ──────────┐
│                     │  │                            │  │                    │
│  HTTP Request       │  │  Find Component & Validate │  │  Collect Dirty     │
│       ↓             │  │       ↓                    │  │  Components        │
│  OpenOLATServlet    │  │  Component.dispatch()      │  │       ↓            │
│       ↓             │  │  fires event to controller │  │  Render to HTML    │
│  DispatcherModule   │  │       ↓                    │  │       ↓            │
│  (path routing)     │  │  Controller.event()        │  │  Build JSON        │
│       ↓             │  │  (business logic, DB, svc) │  │  Response          │
│  Authenticated-     │──│       ↓                    │──│       ↓            │
│  Dispatcher         │  │  fireEvent() to parent     │  │  Send Commands     │
│       ↓             │  │  (event chain up hierarchy)│  │       ↓            │
│  UserRequestImpl    │  │       ↓                    │  │  Client JS:        │
│  (parse URL)        │  │  EventBus (optional)       │  │  DOM Replacement   │
│       ↓             │  │  (notify other sessions)   │  │                    │
│  Window.dispatch()  │  │       ↓                    │  │  ⚠ No DB access!  │
│                     │  │  Update component state    │  │  Model must be     │
│                     │  │  (set dirty flags)         │  │  fully loaded.     │
└─────────────────────┘  └────────────────────────────┘  └────────────────────┘
```

### Phase 1: Routing

Every request enters through `OpenOLATServlet` (mapped to `/*`), which extracts the first path segment and looks up the corresponding `Dispatcher` implementation. The `DispatcherModule` maintains a map of path prefixes to dispatchers, populated at startup from the Spring configuration:

| Path | Dispatcher | Purpose |
|------|-----------|---------|
| `/auth/` | `AuthenticatedDispatcher` | All authenticated user requests (main UI) |
| `/dmz/` | `DMZDispatcher` | Login, registration, password reset (unauthenticated) |
| `/raw/` | `StaticServlet` | Static resources (JS, CSS, images, themes). Versioned URLs for cache-busting, built by `StaticMediaDispatcher.getStaticURI()`. |
| `/m/` | `MapperDispatcher` | Session-scoped dynamic resource handlers |
| `/g/` | `GlobalMapperRegistry` | Global resource mappers (not session-bound) |
| `/url/` | `RESTDispatcher` | External deep links to business paths |
| `/webdav/` | `WebDAVDispatcher` | WebDAV file access |
| `/restapi/` | `CXFServlet` (JAX-RS) | REST API, behind the servlet filter `RestApiLoginFilter` |
| `/lti/` | `LTI13Dispatcher` | LTI 1.3 tool integration |

Additional routes: `/robots.txt`, `/sitemap.xml`, `/tiny/`, `/bigbluebutton/`, `/teams/`, `/certificate/`, `/badge/`, `/catalog/`, `/survey/`, `/csp/`, `/pdfd/`, `/edusharing/`, `/transcoding/`.

The registration has three places:

- `dispatcherContext.xml` maps the path prefixes of the `DispatcherModule` bean (for example `/auth/`, `/dmz/`, `/url/`, `/lti/`).
- `OpenOLATServlet.init()` adds `/m/`, `/g/` and `/webdav/` in code.
- `web.xml` (for example `src/main/webapp-tomcat/WEB-INF/web.xml`) maps the servlets `StaticServlet` (`/raw/*`), `CXFServlet` (`/restapi/*`), `PersonalRSSServlet` (`/rss/*`) and `ICalServlet` (`/ical/*`), and the filters `SameSiteCookieFilter`, `RestApiLoginFilter` and `HeadersFilter`.

For the main UI flow (`/auth/`), the `AuthenticatedDispatcher` creates a `UserRequestImpl` by parsing the URL. The framework encodes its state-management parameters directly into the URL path, using colons as delimiters:

```
/olat/auth/321:2:1234:1567:1:0/course/run
           ^^^  ^  ^^^^  ^^^^  ^  ^
           |    |  |     |     |  extra params
           |    |  |     |     mode (1=AJAX)
           |    |  |     componentTimestamp
           |    |  componentID
           |    timestampID
           windowID
```

The **windowID** identifies which browser window/tab the request belongs to (OpenOlat supports multiple windows per session). The **componentID** identifies which UI widget was interacted with. The **timestamps** are version counters that prevent processing stale requests — if a user hits the back button and re-clicks a link, the timestamps will not match the current component tree, and the framework will reject the dispatch and re-render instead.

### Phase 2: Dispatch (Event Chain)

Inside `Window.dispatchRequest()`, the framework finds the target component, validates timestamps, checks CSRF, and calls `target.dispatchRequest(ureq)`. What follows is an event chain:

1. **Component → Controller:** The component fires an event. The controller receives it in `event(ureq, Component, Event)`.
2. **Business logic:** The controller calls services, loads data, updates the database.
3. **Controller → Parent Controller:** The controller may call `fireEvent(ureq, Event.DONE_EVENT)` to notify its parent, which reacts (closing dialogs, refreshing tables, navigating).
4. **Multi-User Events:** The controller may fire events to the EventBus to notify other sessions.
5. **Component tree updates:** Controllers modify component state (dirty flags set automatically).

Only when the entire event chain completes does the render phase begin.

> **Synchronization:** The entire dispatch-render cycle runs `synchronized(window)`. Concurrent AJAX requests to the same window are serialized. Different windows can process concurrently.

### Phase 3: Rendering

After the dispatch, the framework traverses the component tree to find dirty components. Each is re-rendered by its `ComponentRenderer` singleton. The fragments are packaged into JSON:

> **Important convention:** During rendering, all models must be loaded and all business logic complete. Although the DB connection is technically still open (closed in the servlet's `finally` block), **no DB access, service calls, or business logic should occur during rendering**. Renderers and templates should only read data prepared during dispatch.

```json
{
  "cc": 2,
  "cps": [
    { "id": "o_c1234", "n": "<div>...new HTML...</div>" },
    { "id": "o_c5678", "n": "<span>...updated...</span>" }
  ]
}
```

On the client side, JavaScript receives this JSON and replaces the corresponding DOM elements by their ID. The result is a seamless partial page update without a full reload.

---

## 4. Controller Hierarchy

Controllers are the workhorses of the OpenOlat UI. They receive user events, execute business logic (often by delegating to services), and update the component tree to reflect the new state. The framework provides a layered controller hierarchy, where each level adds progressively more convenience and structure.

```
Controller (interface)
  │  getInitialComponent(), dispose(), addControllerListener()
  │
  └─ DefaultController (abstract)
       │  event(ureq, Component, Event)  — abstract
       │  event(ureq, Controller, Event) — overridable
       │  fireEvent(ureq, Event)         — propagate to parents
       │
       └─ BasicController (abstract)
            │  listenTo(ctrl) / removeAsListenerAndDispose(ctrl)
            │  createVelocityContainer() / showInfo/Warning/Error()
            │
            └─ FormBasicController (abstract)
                 initForm(layout, listener, ureq) — abstract
                 formOK(ureq)                     — abstract
                 formInnerEvent(ureq, FormItem, FormEvent)
                 validateFormLogic(ureq)
```

### Controller (Interface)

The root `Controller` interface defines the contract: every controller has an initial component (the root of its UI subtree), can register listeners for controller-to-controller events, and must be disposable. This interface is intentionally minimal — it defines the communication protocol but leaves the implementation to subclasses.

### DefaultController — Event Dispatch Engine

The first concrete superclass provides the event dispatch machinery. It defines two abstract `event()` methods corresponding to the two event channels (component-to-controller and controller-to-controller), and the `fireEvent()` method that propagates events upward to parent controllers. DefaultController also handles the disposed state: if a controller is disposed while a request is in flight, it gracefully replaces its content with a "stale" message rather than crashing.

```java
// Two event channels every controller can handle:
protected abstract void event(UserRequest ureq, Component source, Event event);
protected void event(UserRequest ureq, Controller source, Event event) { }

// Fire an event upward to any listening parent controllers:
protected void fireEvent(UserRequest ureq, Event event) {
    for (ControllerEventListener listener : listeners) {
        listener.dispatchEvent(ureq, this, event);
    }
}
```

### BasicController — Lifecycle Management

This is the most commonly extended controller class. It introduces the crucial **`listenTo()` / `removeAsListenerAndDispose()`** pattern that manages child controller lifecycles. When you call `listenTo(childCtrl)`, the parent registers as a listener for events from the child AND tracks the child for automatic disposal. When the parent is disposed, all tracked children are disposed too. This prevents memory leaks and ensures clean teardown.

```java
// The fundamental lifecycle pattern:
private EditController editCtrl;

// When creating a child controller, always listenTo():
editCtrl = new EditController(ureq, getWindowControl());
listenTo(editCtrl);

// When replacing a child controller, always dispose the old one first:
removeAsListenerAndDispose(editCtrl);
editCtrl = new EditController(ureq, getWindowControl());
listenTo(editCtrl);
```

### FormBasicController — Structured Form Handling

For controllers that manage data entry forms, `FormBasicController` provides a structured lifecycle. Instead of manually handling component events, you implement `initForm()` to build the form using the `FormUIFactory`, `validateFormLogic()` to check inputs, and `formOK()` to process the submitted data. The form framework handles validation orchestration, error display, dirty tracking, and cancel/reset operations automatically.

### How Controllers Compose

Complex screens are built by composing controllers hierarchically. A main controller creates child controllers, registers them via `listenTo()`, and embeds their components in its Velocity template. When a child needs to communicate with its parent (e.g., "the user saved the form"), it calls `fireEvent(ureq, Event.DONE_EVENT)`, and the parent receives this in its `event(ureq, Controller, Event)` method.

```
┌── ParentController ──────────────────────────────────────────────┐
│                                                                   │
│   VelocityContainer (mainVC)       ┌─ EditFormController ──────┐ │
│   ┌─────────────────────────┐      │  (FormBasicController)    │ │
│   │  $r.render("editForm")  │─────→│                           │ │
│   │                         │      │  fireEvent(DONE_EVENT) ───┼─┼──→ parent.event()
│   │  $r.render("details")   │─┐    └───────────────────────────┘ │
│   └─────────────────────────┘ │    ┌─ DetailController ────────┐ │
│                                └──→│  (BasicController)        │ │
│                                    │  fireEvent(CHANGED_EVENT)─┼─┼──→ parent.event()
│                                    └───────────────────────────┘ │
└───────────────────────────────────────────────────────────────────┘
```

---

## 5. Component Model

If controllers are the brains of the UI, components are the body. The component tree is an in-memory representation of the entire page structure for a given browser window. Each component knows how to render itself to HTML, tracks whether it needs re-rendering (the dirty flag), and can dispatch user interactions back to its listening controller.

The component model follows a composite pattern: containers hold children, and the renderer traverses the tree recursively. The key insight is that **every UI widget on the page has a corresponding server-side component object**, identified by a unique dispatch ID. This ID appears in the generated HTML (as a DOM element ID) and in the AJAX request URLs, allowing the framework to route any user interaction to the exact component that should handle it.

```
Component (interface)
  └─ AbstractComponent
       │  dispatchID, dirty flag, timestamp, listeners
       │
       ├─ Container                → VelocityContainer
       │    Map<String, Component>    template + context map
       │
       ├─ Panel                    → StackedPanel
       │    single child slot         push/pop for modals & layers
       │
       ├─ Link                     → FlexiTableComponent
       │    command, i18n, style      data model + column model
       │
       └─ MenuTree
            TreeModel, selection
```

### The Dirty Flag Mechanism

The dirty flag is the core optimization that makes server-side rendering performant. Instead of re-rendering the entire page on every interaction, the framework only re-renders components whose state has changed. When a controller modifies a component (e.g., calls `contextPut()` on a VelocityContainer, adds/removes children from a Container, or changes a Link's label), the component automatically sets its dirty flag to `true`. During the render phase, the framework traverses the tree, collects all dirty components, and renders only those — producing a compact JSON response with targeted DOM updates.

### ComponentRenderer — Singleton per Type

Each component type has a corresponding `ComponentRenderer` implementation, held as a static singleton. The renderer is stateless and thread-safe — it receives the component, a `StringOutput` buffer, a `URLBuilder` (for generating framework URLs), and a `Translator` (for i18n). The `VelocityContainerRenderer` merges the template with the context, while simpler renderers like `LinkRenderer` emit HTML directly.

```java
public interface ComponentRenderer {
    void render(Renderer renderer, StringOutput sb, Component source,
        URLBuilder ubu, Translator translator, RenderResult renderResult, String[] args);
}

// Each component type returns its singleton renderer:
private static final ComponentRenderer RENDERER = new VelocityContainerRenderer();
public ComponentRenderer getHTMLRendererSingleton() { return RENDERER; }
```

### Component Overview

The `core.gui.components` package contains over 300 component classes. Key components by category:

| Category | Component | Purpose |
|----------|-----------|---------|
| **Containers** | `VelocityContainer` | Renders a Velocity template with a context map of child components and variables |
| | `Panel` | Single-slot wrapper — holds one child, allows swapping content |
| | `StackedPanel` | Push/pop stack of components (modals, layer management) |
| | `BreadcrumbedStackedPanel` | StackedPanel with breadcrumb navigation trail — the standard drill-down container |
| | `TabbedPane` | Tabbed container with lazy-loading support |
| **Navigation** | `Link` | Clickable element (button, link, icon-only) — the primary interaction component |
| | `MenuTree` | Hierarchical tree navigation with TreeModel and selection |
| | `SegmentViewComponent` | Horizontal segment/tab bar for sub-view switching |
| | `Dropdown` | Dropdown menu containing Link children |
| **Display** | `ProgressBar` | Visual progress indicator (percentage or actual/max) |
| | `CountDownComponent` | Client-side countdown timer |
| | `RatingComponent` | Star rating display with optional interactive input |
| | `EmptyState` | Standardized "no data" placeholder with icon and action |
| | `FactSheet` | **New in 21.1.0:** Card with a title, a list of `Fact` rows (icon, label, value or component) and footer links. Create it with `FactSheetFactory` (package `core.gui.components.factsheet`). |
| | `Sections` | **New in 21.1.0:** List of titled, collapsible sections. Create it with `SectionsFactory.createSections()` and `createSection()` / `createTextSection()` / `createLinksSection()` (package `core.gui.components.sections`). |
| | `ComponentList` | **New in 21.1.0:** Renders a list of components in order, without a template (package `core.gui.components.util`). |
| **Charts** | `BarChartComponent` | Bar chart via D3.js |
| | `PieChartComponent` | Pie/donut chart via D3.js |
| | `RadarChartComponent` | Radar/spider chart |
| **Media** | `ImageComponent` | Image display with cropping, lazy loading, mapper integration |
| | `DownloadComponent` | Download link for a VFSLeaf |
| **Widgets** | `TextWidget` / `FigureWidget` / `ComponentWidget` | Dashboard widgets for text, numbers, or arbitrary components |
| **Form** | `FormBaseComponent` | Bridge between FlexiForm elements and the component tree (see section 8) |

> **FlexiForm elements are also components.** Every `FormItem` wraps a `Component` via `getComponent()`. The FlexiForm system is the preferred way to create interactive form elements — see section 8 for the full catalog.

### Window & WindowControl

The `Window` component is the root of each browser window's component tree. It owns the dispatch-render cycle and runs it `synchronized(window)` to prevent concurrent dispatch on double-click or rapid navigation.

Controllers interact with the window through `WindowControl` — a facade providing:

- **Modal dialogs:** `pushAsModalDialog(component)`, `pushAsTopModalDialog(component)`, `pushAsCallout(component, targetId)`
- **User messages:** `setInfo(msg)`, `setWarning(msg)`, `setError(msg)` — displayed as growl-style notifications
- **Navigation:** `pop()`, `makeFlat()`, `pushFullScreen(controller)`
- **Business path:** `getBusinessControl()` for deep linking (see section 13)
- **Back office:** `getWindowBackOffice()` — command queue, JS commands to browser, cycle listeners

The `WindowBackOffice` maintains a **command queue** accumulating during the request. Controllers send commands via `wbo.sendCommandTo(command)`. The client-side JavaScript processes the queue after receiving the AJAX response.

---

## 6. Event System

OpenOlat has four distinct event mechanisms, each serving a different communication scope. Understanding which to use is fundamental.

### 6.1 Component → Controller Events

When a user clicks a link or interacts with a UI component, the component fires an event to its listening controller. The controller receives it in:

```java
@Override
public void event(UserRequest ureq, Component source, Event event) {
    if (source == editLink) {
        // Handle click on editLink
    } else if (source == myTree) {
        TreeEvent te = (TreeEvent) event;
        String nodeId = te.getNodeId();
    }
}
```

Source identification is by **reference comparison** (`source == myLink`), not by string matching. Each component type fires its own event subclass: `TreeEvent` for menus, `TableEvent` for old-style tables, etc.

### 6.2 Controller → Controller Events

When a child controller completes its work, it notifies its parent using `fireEvent()`:

```java
// In child controller (e.g., an edit form):
fireEvent(ureq, Event.DONE_EVENT);

// In parent controller:
@Override
public void event(UserRequest ureq, Controller source, Event event) {
    if (source == editCtrl && event == Event.DONE_EVENT) {
        loadModel();  // Refresh data
        cmc.deactivate();  // Close modal
    }
}
```

Standard event constants: `Event.DONE_EVENT`, `Event.CANCELLED_EVENT`, `Event.CHANGED_EVENT`, `Event.BACK_EVENT`, `Event.CLOSE_EVENT`, `Event.FAILED_EVENT`.

The parent registers for events via `listenTo(childCtrl)`. The `listenTo()` call also transfers disposal responsibility — when the parent is disposed, the child is too.

### 6.3 Form Events

In the FlexiForm system, `FormEvent` extends `Event` with trigger information. Form elements fire events to `formInnerEvent()`:

```java
@Override
protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
    if (source == statusEl && event.wasTriggerdBy(FormEvent.ONCHANGE)) {
        updateVisibility();  // React to dropdown change
    } else if (source == deleteLink) {
        doDelete(ureq);
    }
}
```

Trigger types: `ONCLICK`, `ONCHANGE`, `ONBLUR`. Elements must opt in via `addActionListener(FormEvent.ONCHANGE)`. The form submit flow goes: `validateFormLogic()` → `formOK()` → `fireEvent(DONE)`.

### 6.4 Multi-User Events (EventBus)

The EventBus is a **cross-session, cross-cluster** publish/subscribe mechanism for notifying all interested users about changes — for example, when one user edits a course, all other users viewing that course are notified.

**Channels** are identified by `OLATResourceable` objects — a type/key pair:

```java
// Create a channel identifier
OLATResourceable courseOres = OresHelper.createOLATResourceableInstance("CourseModule", courseKey);

// Register for events
CoordinatorManager.getInstance().getCoordinator().getEventBus()
    .registerFor(this, identity, courseOres);

// Fire an event to all listeners on that channel
MultiUserEvent event = new MultiUserEvent("courseChanged");
CoordinatorManager.getInstance().getCoordinator().getEventBus()
    .fireEventToListenersOf(event, courseOres);
```

Listeners implement `GenericEventListener` with a single `event(Event)` method. Events must extend `MultiUserEvent` (which is `Serializable` for cluster transport). The `Coordinator` facade provides access to the EventBus, plus `Syncer` (synchronization), `Locker` (distributed locking), and `Cacher` (distributed cache).

**Important**: Always deregister in `dispose()` to prevent memory leaks:
```java
CoordinatorManager.getInstance().getCoordinator().getEventBus()
    .deregisterFor(this, courseOres);
```

There is also a per-session EventBus (`ureq.getUserSession().getSingleUserEventCenter()`) for controllers of one session without a parent-child relationship, for example a toolbar button that toggles the glossary overlay.

### Real-World Example: CourseRuntimeController

When a user enters a course, `CourseRuntimeController` registers on the channels of the course and of the repository entry:

```java
// On entry: register for course and repository events
coordinatorManager.getCoordinator().getEventBus()
    .registerFor(this, getIdentity(), getOlatResourceable());
coordinatorManager.getCoordinator().getEventBus()
    .registerFor(this, getIdentity(), getRepositoryEntry());

// On exit: deregister
coordinatorManager.getCoordinator().getEventBus()
    .deregisterFor(this, getOlatResourceable());
```

---

## 7. Velocity Templating

OpenOlat uses Apache Velocity for server-side HTML rendering. Templates define the visual structure, while controllers provide the data and child components. The framework enhances Velocity with the `$r` decorator object — a `VelocityRenderDecorator` that bridges the template and the component framework.

Templates live in `_content/` directories alongside the controller's Java package. When a controller calls `createVelocityContainer("mypage")`, the framework resolves the template path automatically from the controller's package: `org/olat/modules/feature/ui/_content/mypage.html`.

### The `$r` Decorator

The `$r` object is the most important tool in templates:

```velocity
## Render a child component (controller or form element)
$r.render("editForm")
$r.render("dataTable")

## Translate an i18n key
$r.translate("page.title")

## Get the DOM ID of a component (for JavaScript targeting)
$r.getId("myLink")

## Conditional rendering based on component visibility
#if($r.visible("optionalSection"))
    $r.render("optionalSection")
#end

## Access context data pushed by the controller
<h2>$userName</h2>
#foreach($item in $items)
    <div class="o_item">$item.name</div>
#end
```

The separation is clean: the controller pushes data via `contextPut()` and adds child components via `put()`; the template decides layout and presentation. Controllers never emit HTML, and templates never contain business logic.

### Complete `$r` Method Reference

The `$r` decorator (`VelocityRenderDecorator`) exposes rendering, translation, formatting, and link-generation utilities. `Formatter` and `StringHelper` are **not directly exposed** — their functionality is available through `$r` methods:

```velocity
## Rendering
$r.render("childComponent")            ## render a sub-component
#if($r.visible("optionalPart"))
    $r.render("optionalPart")          ## conditional rendering
#end
$r.getId("prefix")                     ## unique DOM ID within component

## Translation
$r.translate("i18n.key")               ## translate with current locale
$r.translate("greeting", $name)        ## with parameter substitution

## Escaping & formatting
$r.escapeHtml($text)                   ## HTML entity encoding
$r.escapeJavaScript($text)             ## JS string escaping
$r.formatDate($date)                   ## locale-aware date
$r.formatDateAndTime($date)            ## date + time
$r.formatBytes($size)                  ## human-readable file size

## Static resources & links
$r.staticLink("js/mylib.js")           ## versioned static resource URL
$r.contextHelpWithWrapper("page")      ## help icon with link
$r.contextPath()                       ## server context path

## Sections (New in 21.1.0)
$r.sectionHeader($id, $title, false, true, true)  ## header row of a collapsible section
```

**New in 21.1.0:** `$r.sectionHeader(id, title, subTitle, collapsible, expanded)` renders the header row of a titled section with `SectionHeaderRenderer`. `FormSection` (section 8) and the `Sections` component use the same header markup.

### Asynchronous & Background Events

A critical Velocity feature is the ability to fire events to the server **without triggering a full re-render**. This is essential when JavaScript performs client-side rendering (drag-and-drop, inline editing, chart interactions) and needs to persist state on the server without the server overwriting the DOM.

| Method | Behavior | Use Case |
|--------|----------|----------|
| `$r.formURIbg("myElement")` | XHR, **no re-render** | JS-driven UI: save state silently |
| `$r.formURI("myElement")` | XHR, **triggers re-render** | Standard form interactions |
| `$r.backgroundCommand("cmd")` | XHR, **no re-render** | Notify server of client-side state change |
| `$r.openJavaScriptCommand("cmd")` | XHR, **no re-render** | Same as background, for component links |

The `bg` variants send the request but mark the component as **not dirty**, so the AJAX cycle skips re-rendering it. The server-side `event()` or `formInnerEvent()` still fires — the controller can persist data or fire multi-user events. Only the DOM update is suppressed.

**Pattern: JavaScript Drag-and-Drop with Server Persistence**

```velocity
## In template: generate background URL for each draggable item
#foreach($item in $items)
  <div class="o_draggable" data-save-url="$r.formURIbg("saveOrder")"
       data-item-id="$item.key">$item.name</div>
#end
```

```java
// Server-side: event fires normally, persist the new order
@Override
protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
    if (source == saveOrderEl) {
        String itemId = ureq.getParameter("item");
        String pos = ureq.getParameter("pos");
        myService.updateSortOrder(itemId, Integer.parseInt(pos));
        // No setDirty(true) — the bg URL already suppresses re-rendering
    }
}
```

This pattern is used in the course editor (node reordering), portfolio (artifact arrangement), and form designer (element ordering).

---

## 8. FlexiForm System

The FlexiForm system provides a structured, declarative way to build data entry forms. Instead of manually creating HTML form elements and parsing request parameters, developers describe their form in `initForm()` using the `FormUIFactory` (available as the `uifactory` field). The framework handles rendering, client-side behavior, validation, error display, and submit/cancel flow.

### Form Lifecycle

```
initForm() → User interacts → formInnerEvent() (element events)
                             → Submit → validateFormLogic() → formOK() → fireEvent(DONE)
                             → Cancel → formCancelled()
```

### Building Forms with FormUIFactory

```java
@Override
protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
    // Text input
    nameEl = uifactory.addTextElement("name", "field.name", 200, "", formLayout);
    nameEl.setMandatory(true);

    // Dropdown selection
    String[] keys = {"draft", "published"};
    String[] values = {translate("status.draft"), translate("status.published")};
    statusEl = uifactory.addDropdownSingleselect("status", "field.status",
        formLayout, keys, values, null);
    statusEl.addActionListener(FormEvent.ONCHANGE); // react immediately

    // Date picker
    dateEl = uifactory.addDateChooser("date", "field.date", formLayout);

    // Rich text editor
    contentEl = uifactory.addRichTextElementForStringDataMinimalistic(
        "content", "field.content", "", 10, 60, formLayout, getWindowControl());

    // Buttons
    FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
    formLayout.add(buttonLayout);
    uifactory.addFormSubmitButton("save", buttonLayout);
    uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
}
```

### Complete Form Elements Catalog

| Category | Element | Description |
|----------|---------|-------------|
| **Text Input** | `TextElement` | Single-line text field with max length, placeholder, autocomplete |
| | `TextAreaElement` | Multi-line text area with rows, character counter, strip-HTML |
| | `RichTextElement` | TinyMCE WYSIWYG editor (full, minimalistic, string-data variants) |
| | `IntegerElement` | Numeric-only field with min/max validation |
| | `MarkdownElement` | Markdown editor with live preview |
| **Selection** | `SingleSelection` | One-of-many: dropdown, radio buttons, card select, button group |
| | `MultipleSelectionElement` | Many-of-many: checkboxes (horizontal/vertical), dropdown with checkboxes |
| | `FormToggle` | On/off toggle switch |
| **Date & Time** | `DateChooser` | Date picker with optional time, date range, min/max constraints |
| | `RelativeDateElement` | Date relative to a reference date (offset, unit, direction). The caller supplies a `RelativeDateContext`. Add it with `addRelativeDateElement()`. |
| **File** | `FileElement` | File upload with MIME validation, max size, drag-and-drop, preview |
| | `DownloadLink` | Download link for an existing file |
| **Specialized** | `ColorPickerElement` | Color chooser with palette and hex input |
| | `SliderElement` | Range slider with min/max/step |
| | `IconSelectorElement` | Icon chooser from predefined CSS classes |
| | `AutoCompleter` | Text field with server-side autocomplete (AJAX) |
| | `SearchElement` | **New in 21.1.0:** Unified search field with the variants `DEFAULT`, `LARGE` and `TYPEAHEAD` (`SearchVariant`). See "Search Element" below. |
| | `ObjectSelectionElement` | Selection of objects from an `ObjectSelectionSource`, with option groups (`ObjectOptionGroup`) and an optional browser controller. `IdentitySelectionSource` selects users. |
| | `MathLiveElement` | Mathematical formula editor (LaTeX) |
| **Display Only** | `StaticTextElement` | Read-only HTML text |
| | `SpacerElement` | Visual separator |
| | `FormLink` | Clickable link/button within a form — wraps a `Link` component |
| | `MemoryElement` | Hidden element preserving a value across form rebuilds |
| **Multi-Value** | `TextBoxListElement` | Tag-style input — multiple values as removable chips |
| | `AutoCompletionMultiSelection` | Autocomplete with multiple selections |
| **Layout** | `FormSection` | **New in 21.1.0:** Titled, optionally collapsible part of a form. See "Form Sections" below. |
| **Buttons** | `FormSubmit` | Triggers validation then `formOK()` |
| | `FormCancel` | Fires `Event.CANCELLED_EVENT` to parent |
| | `FormReset` | Resets all elements to initial values |
| **Table** | `FlexiTableElement` | Full data table (see section 9) |

**New in 21.1.0:** `FormItem.setLabelIconCss(String)` shows an icon next to the label, for example `"o_icon o_icon_locked"` for a read-only field. The icon is only a visual cue. It does not change `setMandatory()` or `setEnabled()`.

### FormItem – Component Relationship

Every `FormItem` wraps an underlying `Component`. When a form is rendered, each element's component is placed into the component tree. This dual nature means form elements participate in both the FlexiForm lifecycle (validation, data binding) and the standard component lifecycle (dirty flags, AJAX rendering).

```java
FormItem nameEl = uifactory.addTextElement(...);
Component comp = nameEl.getComponent();  // → TextElementComponent
// In Velocity: $r.render("name") renders the TextElementComponent
```

### Form Layout System

`FormLayoutContainer` is both a `FormItem` and a container for other form items. It uses Velocity templates for layout. Built-in layout types:

| Layout | Factory Method | Use Case |
|--------|---------------|----------|
| `LAYOUT_DEFAULT` (3:9) | `createDefaultFormLayout()` | Standard label-left, input-right (Bootstrap 3:9 grid) |
| `LAYOUT_DEFAULT_6_6` / `_9_3` / `_2_10` | `createDefaultFormLayout_6_6()` / `_9_3()` / `_2_10()` | Label-left layout with another grid ratio |
| `LAYOUT_HORIZONTAL` | `createHorizontalFormLayout()` | Elements in a row |
| `LAYOUT_VERTICAL` | `createVerticalFormLayout()` | Labels above inputs, stacked |
| `LAYOUT_TWO_COLS` | `createTwoColsFormLayout()`, `uifactory.addTwoColumnsFormLayout()` | Two equal columns from 992 px viewport width, stacked below |
| (custom page) | `createCustomFormLayout(name, translator, page)` | Developer-provided Velocity template. There is no enum constant for it. |
| `LAYOUT_BUTTONGROUP` | `createButtonLayout()` | Button row with spacing |
| `LAYOUT_BAREBONE` | `createBareBoneFormLayout()` | No wrapping markup |
| `LAYOUT_PANEL` | `createPanelFormLayout()` | Panel with border and optional title |
| `LAYOUT_INLINE` | `createInlineFormLayout()` | Inline form elements |
| `LAYOUT_INPUTGROUP` | `createInputGroupLayout()` | Bootstrap input group |
| `LAYOUT_TABLE_CONDENSED` | `createTableCondensedLayout()` | Compact table-like layout |

Layout containers can be nested. Common pattern: default form layout with a button group sub-container.

### Form Sections

**New in 21.1.0**

`FormSection` (package `core.gui.components.form.flexible.impl`) divides a long form into titled parts (OO-9756). It extends `FormLayoutContainer` and renders its items with the default 3:9 layout under a section header.

```java
FormSection generalSection = uifactory.addFormSection("general", translate("section.general"),
        formLayout, FormSection.Level.TITLE);
nameEl = uifactory.addTextElement("name", "field.name", 200, "", generalSection);

FormSection advancedSection = uifactory.addFormSection("advanced", translate("section.advanced"),
        formLayout, FormSection.Level.SUB_TITLE);
advancedSection.setCollapsible(true);
advancedSection.setCollapsed(true);                       // default for a first visit
advancedSection.setPersistedStatusId(ureq, "my.form.advanced"); // remember the toggle
```

- `Level.TITLE` renders a legend-style heading, `Level.SUB_TITLE` a sub-title heading.
- `setCollapsible(true)` makes the header a toggle. The toggle runs in the browser (Bootstrap collapse) without a server round trip.
- With a persisted status id, the browser sends the new state as a background command. `FormSectionVelocityContainer` saves it without a form submit.
- `setPersistedStatusId(ureq, id)` loads the collapsed state from the GUI preferences (attributed class `FormSection`) and saves every toggle. Call it after `setCollapsed()`.
- The title is an already translated string.

The factory hook `FormLayoutContainer.createFormVelocityContainer()` lets a subclass use its own `FormVelocityContainer`. `FormSection` uses it for the toggle handling.

### Search Element

**New in 21.1.0**

`SearchElement` (OO-9627) is the one search field for forms, tables and headers. `FormUIFactory` offers two methods:

```java
// Search with a button, fires SearchFormEvent.SEARCH or SearchFormEvent.RESET
searchEl = uifactory.addSearchElement("search", SearchVariant.DEFAULT, formLayout);
searchEl.setPlaceholderKey("search.placeholder");
searchEl.setAriaControls(resultsDomId);

// Search as you type, suggestions from a ListProvider (always SearchVariant.TYPEAHEAD)
searchEl = uifactory.addSearchElement("search", listProvider, ureq.getUserSession(), formLayout);
searchEl.setAutoCompleteSelectListener((lureq, key) -> doSelect(lureq, key));
```

| Variant | Behaviour |
|---------|-----------|
| `DEFAULT` | Explicit search button, toolbar size |
| `LARGE` | Explicit search button, hero size, button label visible |
| `TYPEAHEAD` | Search as you type, no search button |

The element fires a `SearchFormEvent` (`SEARCH` or `RESET`) to `formInnerEvent()`. By default it does not mark the surrounding form dirty (`setPropagateDirtiness(false)`). A composite that embeds the element calls `evalLocalDispatch(ureq)` and fires its own event. The FlexiTable search uses this (section 9).

---

## 9. FlexiTable

The FlexiTable is OpenOlat's most complex UI component — a data table with sorting, filtering, searching, pagination, multi-select, export, and customizable cell rendering. Since FlexiTable is a form element, table controllers extend `FormBasicController`.

### 9.1 Column Descriptors

Columns are defined by an enum implementing `FlexiSortableColumnDef`:

```java
public enum MyCols implements FlexiSortableColumnDef {
    id("table.id"), name("table.name"), date("table.date"), actions("table.actions");

    private final String i18nKey;
    @Override public String i18nHeaderKey() { return i18nKey; }
    @Override public boolean sortable() { return this != actions; }
    @Override public String sortKey() { return name(); }
}
```

Columns are added to a `FlexiTableColumnModel` using `DefaultFlexiColumnModel`:

```java
FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MyCols.id));
columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MyCols.name,
    new TextFlexiCellRenderer()));  // Custom renderer
columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, MyCols.date));  // visible=false → optional column
```

The `DefaultFlexiColumnModel` constructor's first boolean parameter controls **default visibility**: `false` means the column is hidden initially but can be shown by the user via the column chooser. `true` (or omitted) means visible by default.

### 9.2 Cell Renderers

Cell renderers control how individual cells are displayed. They implement `FlexiCellRenderer`:

```java
public interface FlexiCellRenderer {
    void render(Renderer renderer, StringOutput target, Object cellValue,
                int row, FlexiTableComponent source, URLBuilder ubu,
                Translator translator);
}
```

Built-in renderers (package `core.gui.components.form.flexible.impl.elements.table`): `TextFlexiCellRenderer`, `DateFlexiCellRenderer`, `DateTimeFlexiCellRenderer`, `BooleanCellRenderer`, `YesNoCellRenderer`, `CSSIconFlexiCellRenderer`, `StaticFlexiCellRenderer` (wraps value in a link), `TreeNodeFlexiCellRenderer` (indented tree rows), `ActionsCellRenderer`. `ProgressRadialCellRenderer` lives in `core.gui.components.progressbar`. You assign a renderer when adding the column model.

**New in 21.1.0:** `TranslateCellRenderer` marks a cell value that has a translation (OO-9596).

### 9.3 Data Model

The data model maps rows and columns to values:

```java
public class MyTableModel extends DefaultFlexiTableDataModel<MyRow> {
    public MyTableModel(FlexiTableColumnModel columnsModel) {
        super(columnsModel);
    }
    @Override
    public Object getValueAt(int row, int col) {
        MyRow r = getObject(row);
        return switch (MyCols.values()[col]) {
            case id -> r.getKey();
            case name -> r.getName();
            case date -> r.getCreationDate();
            case actions -> r.getKey();  // used by action renderer
        };
    }
}
```

### 9.4 Sorting

Sorting is column-based. By default, columns sort by the natural order of the value returned by `getValueAt()`. For custom sort behavior, implement `SortableFlexiTableDataModel` and override `sort(SortKey)`. You set the default sort with `FlexiTableSortOptions`:

```java
FlexiTableSortOptions options = new FlexiTableSortOptions();
options.setDefaultOrderBy(new SortKey(MyCols.name.name(), true));  // ascending by name
tableEl.setSortSettings(options);
```

### 9.5 Filters

Filters narrow the displayed rows. Available filter types:

- `FlexiTableTextFilter` — free-text input
- `FlexiTableSingleSelectionFilter` — dropdown with predefined values
- `FlexiTableMultiSelectionFilter` — multi-select checkboxes
- Further types in `table/filter/`: `FlexiTableDateRangeFilter`, `FlexiTablePeriodFilter`, `FlexiTableNumericalRangeFilter`, `FlexiTableOneClickSelectionFilter`

Filters are added to the table element and handled in `formInnerEvent()`:

```java
List<FlexiTableExtendedFilter> filters = new ArrayList<>();
filters.add(new FlexiTableSingleSelectionFilter("Status", STATUS_FILTER,
    statusOptions, true));  // statusOptions: SelectionValues, true = filter visible by default
tableEl.setFilters(true, filters, false, false);

// In formInnerEvent, react to filter changes:
if (source == tableEl && event instanceof FlexiTableFilterEvent) {
    loadModel();  // Reload with filters applied
}
```

### 9.6 Column Visibility & Table Configuration

Users can toggle optional columns via a column chooser (gear icon). Columns with `visible=false` in the constructor are hidden by default. The table also supports:

```java
tableEl.setSearchEnabled(true);       // Free-text search bar
tableEl.setExportEnabled(true);       // CSV/Excel export
tableEl.setMultiSelect(true);         // Checkboxes for batch operations
tableEl.setSelectAllEnable(true);     // Select-all checkbox
tableEl.setPageSize(25);              // Rows per page
tableEl.setCustomizeColumns(true);    // Column chooser UI
tableEl.setEmptyStateConfig(EmptyStateConfig.builder()
        .withIconCss("o_icon_calendar")
        .withMessageI18nKey("table.empty.message")
        .build());
tableEl.setAndLoadPersistedPreferences(ureq, "my-table-v1");  // column and sort preferences
```

Renderer types (`FlexiTableRendererType`): `classic` (HTML table), `custom` (Velocity-based row templates for card layouts or custom rendering), `external` (a renderer set with `setExternalRenderer()`) and `verticalTimeLine` (timeline rows).

**Changed in 21.1.0:** The table search field is a `SearchElement` (section 8). `FlexiTableElementImpl.getSearchEl()` returns it. `setSearchEnabled(true)` uses the `DEFAULT` variant. `setSearchEnabled(ListProvider, UserSession)` uses the `TYPEAHEAD` variant with suggestions. The table intercepts the search with `evalLocalDispatch()` and fires its own `FlexiTableSearchEvent`.

---

## 10. Wizards

A wizard leads the user through a sequence of steps and commits the work at the end. The framework lives in `org.olat.core.gui.control.generic.wizard`. Examples: `UserImportController` (user import), `PwChangeController` (password change), the booking process in `org.olat.resource.accesscontrol.ui.wizard`.

### Wizard Building Blocks

| Class | Role |
|-------|------|
| `StepsMainRunController` | The wizard itself. A `FormBasicController` with one root `Form` for all steps, the step title links and the buttons back, next, finish and cancel. |
| `Step` / `BasicStep` | One step. `getStepController()` creates the controller of the step. `nextStep()` returns the following step or `Step.NOSTEP`. `getInitialPrevNextFinishConfig()` enables the buttons. |
| `StepFormBasicController` | Base class of a step controller. It uses the root form of the wizard and gives access to the run context. |
| `StepsRunContext` (`DefaultStepsRunContext`) | Map that carries the data from step to step: `put()`, `get()`, `containsKey()`, `remove()`. |
| `PrevNextFinishConfig` | Which buttons a step enables: constants `NEXT`, `BACK_NEXT`, `BACK_FINISH`, `NEXT_FINISH`, `BACK_NEXT_FINISH`, `BACK`, `NOOP`. |
| `StepRunnerCallback` | `execute(ureq, wControl, runContext)` for finish and cancel. Returns `StepsMainRunController.DONE_MODIFIED` or `DONE_UNCHANGED`. |
| `StepsEvent` | Events of a step controller: `ACTIVATE_NEXT`, `ACTIVATE_PREVIOUS`, `INFORM_FINISHED`, `STEPS_CHANGED`, `RELOAD`. |

### Wizard Flow

```java
Step start = new ImportStep00(ureq);                 // extends BasicStep
StepRunnerCallback finish = (uureq, wControl, runContext) -> {
    List<?> newUsers = (List<?>) runContext.get("newUsers");
    // commit the work here, and only here
    return StepsMainRunController.DONE_MODIFIED;
};
wizardCtrl = new StepsMainRunController(ureq, getWindowControl(), start, finish, null,
        translate("title"), "o_sel_user_import_wizard");
listenTo(wizardCtrl);
getWindowControl().pushAsModalDialog(wizardCtrl.getInitialComponent());
```

A step controller validates its fields, stores the result in the run context and asks for the next step:

```java
@Override
protected void formOK(UserRequest ureq) {
    addToRunContext("newUsers", newUsers);
    fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
}
```

The wizard fires `Event.CHANGED_EVENT` to its parent after `DONE_MODIFIED` and `Event.DONE_EVENT` after `DONE_UNCHANGED`. Cancel fires `Event.CANCELLED_EVENT`. The parent closes the modal dialog and disposes the wizard.

Rules:

- Change data only in the finish callback. The steps collect data in the run context.
- The wizard disposes the controller of a step when the user leaves the step. It creates a new controller when the user comes back.
- All step controllers share the root form. The validation of every registered sub form runs on next and finish.

### Cached Step Controllers

**New in 21.1.0**

`CachedRunContextController` keeps a sub-controller alive across back and forward navigation (OO-9742). Use it when a step embeds an expensive form, for example an evaluation form, whose input must survive navigation.

```java
detailsCtrl = CachedRunContextController.of(runContext, CACHE_KEY, rootForm,
        () -> new InvoiceSubmitDetailsController(ureq, wControl, bookingContext, rootForm), this);

@Override
public FormItem getStepFormItem() {
    return detailsCtrl.getStepFormItem();
}

@Override
protected void doDispose() {
    detailsCtrl.release(this);   // stop listening and remove the sub form listener
    super.doDispose();
}
```

- `of()` creates the controller on the first visit and stores it in the run context. On a later visit it registers the cached controller again as sub form listener of the root form.
- `release()` removes the sub form listener. The hidden form of an inactive step therefore does not block next or finish with its validation.
- `StepsMainRunController.doDispose()` calls `CachedRunContextController.disposeAll(runContext)`.

**Changed in 21.1.0:** `StepsMainRunController` propagates the dirty state only for its own navigation items (`propagateDirtinessToContainer()`). An event inside a step no longer redraws the whole wizard and keeps the scroll position. `setFinishText(String)` sets a custom label for the finish button. A `LanguageChangedEvent` of a step goes to the parent.

---

## 11. AJAX Update Cycle

OpenOlat's AJAX mechanism is what makes the server-centric architecture feel responsive. Rather than sending full page reloads, the framework sends only the HTML fragments that changed.

```
  Browser                              Server (synchronized per Window)
  ─────────                            ──────────────────────────────────
  1. User clicks button
  2. JS sends AJAX POST ──────────→    3. Find component by ID
                                       4. Dispatch to controller
                                       5. Execute business logic
                                          (components marked dirty)
                                       6a. Traverse tree for dirty
                                       6b. Render HTML fragments
                                       6c. Build JSON + commands
  7. Receive JSON response ←───────
  8. Replace DOM elements by ID
```

The `WindowBackOfficeImpl` maintains a command queue that accumulates during the request. Besides DOM replacement commands, the queue can contain JS/CSS inclusion commands, redirect commands, scroll commands, and more. The client-side JavaScript in `functions.js` processes this queue sequentially.

> **Performance implication:** The dirty-flag mechanism means that even complex pages with hundreds of components can update efficiently. If a user clicks a "save" button that updates a status label and refreshes a counter, only those two small components are re-rendered and sent over the wire — not the entire page.

### The Poller — Server-Push via Periodic Polling

User clicks trigger AJAX requests, but changes from *other* users (chat messages, collaborative edits, notifications) mark components dirty on the server with no user click to trigger fetching. The **poller** solves this with periodic background requests.

The poller is initialized per window by `AjaxController` (via `serverpart.html`), using `jQuery.periodic`:

- **Initial interval:** 5 seconds (`DEFAULT_POLLPERIOD`)
- **Exponential decay:** 1.005× per cycle — interval grows when nothing changes
- **Activity reset:** `mouseover`, `click`, `keypress` reset to base period
- **Inactivity timeout:** Polling stops after 60 minutes

On the server, the poll handler validates the CSRF token, then calls `window.handleDirties()` — the same method used after user-initiated AJAX. If dirty components exist, they are rendered and returned as JSON. If nothing changed, a `304 Not Modified` response is returned (`NothingChangedMediaResource`), triggering client-side decay.

Full cycle for server-push: another user's action fires `MultiUserEvent` via EventBus → listening controller's `event()` runs, calls `setDirty(true)` → next poll finds dirty component via `handleDirties()` → re-rendered HTML sent to browser.

---

## 12. Mapper Infrastructure

Mappers serve dynamic resources (images, files, JSON, generated content) via stable URLs. While Velocity renders HTML fragments, mappers serve **binary or non-HTML content**.

### The Mapper Interface

```java
public interface Mapper {
    MediaResource handle(String relPath, HttpServletRequest request);
}
```

| URL Prefix | Registry | Scope |
|------------|----------|-------|
| `/m/` | `MapperService` | Session-bound (per user) |
| `/g/` | `GlobalMapperRegistry` | Application-wide (shared) |

### Registering Mappers

`BasicController` provides convenience methods that track mappers for automatic cleanup:

```java
// Non-cacheable — random URL, browser always fetches fresh
String url = registerMapper(ureq, (relPath, request) -> new FileMediaResource(myFile));

// Cacheable — stable URL from ID, enables browser caching
String url = registerCacheableMapper(ureq, "gallery-" + galleryId, new GalleryMapper(images));

// With explicit expiration (seconds)
String url = registerCacheableMapper(ureq, "thumb-" + id, mapper, 3600);
```

### Cacheable vs. Non-Cacheable

- **Non-cacheable** (`registerMapper`): URL = MD5(random UUID). Forces fresh fetch.
- **Cacheable** (`registerCacheableMapper`): URL = MD5(stable ID). Enables browser caching.

### Persisted Mappers (Cluster Support)

Serializable mappers are persisted to DB (via `MapperDAO`, `o_mapper` table, serialized with XStream) for cluster support. Any node of the cluster can then serve the content of the mapper.

### Global Mappers

For application-wide shared resources (JS translation maps, static generated content), use the `GlobalMapperRegistry`:

```java
// Register a global mapper. The URL includes the build ID for cache-busting across releases.
String path = GlobalMapperRegistry.getInstance()
    .register(JSTranslatorMapper.class, jsTranslationMapper);
// Results in URL: /g/{buildId}org.olat.core.gui.JSTranslatorMapper/...
```

### Mapper Cleanup

Session-bound mappers are cleaned up in three ways:

1. **Controller dispose:** `BasicController.doPreDispose()` calls `MapperService.cleanUp(mapperKeys)`.
2. **Session end:** `MapperSessionListener` removes all mappers of an HTTP session.
3. **Zombie slayer:** the scheduled `MapperZombieSlayerJob` removes expired persisted mappers.

A request to an unknown mapper ID gets the status 404. The same applies to a sandboxed mapper that cannot be reclaimed (see below).

### Sandboxed Mappers & Content Domain

**New in 21.0.0**

Section 34 gives the overview of all security mechanisms.

OpenOlat can deliver user content (single pages, SCORM packages, iframe content) from a second domain (OO-7612). A script in that content then runs in another origin than the main application. It cannot read the session cookie or call the main UI.

- Configure the domain with `server.content.domainname` in `olat.properties`. The default is `${server.domainname}`, which disables the feature.
- `Settings.isContentDomainNameEnabled()` is true when the content domain differs from the main domain. `Settings.createContentServerURI()` builds its base URL.
- A controller calls `registerSandboxedMapper(ureq, mapper)` in `BasicController`. The method returns a `MapperKey` with the URL and a one-time token.
- The page loads `contentServerURI + mapperKey.getUrl() + "?token=" + mapperKey.getToken()`. See `IFrameDisplayController` (with `IFrameSettings.isUseContentDomain()`) and `ScormWrapperController`.

```java
MapperKey mKey = registerSandboxedMapper(ureq, contentMapper);
String baseUri = Settings.isContentDomainNameEnabled()
        ? Settings.createContentServerURI() : Settings.createServerURI();
String url = baseUri + mKey.getUrl() + "?token=" + mKey.getToken();
```

The browser has no session on the content domain. On the first request, `MapperDispatcher` asks `MapperService.getSandboxMapper()` for the mapper. The service checks the token and copies identity, roles and locale of the parent `UserSession` to the new content session. The token is valid once. A request of another identity gets the status 404.

`ControllerDeliveryMapper` runs a complete controller in a popup window of the content session. `ControllerDeliveryCreator` marks the session as content delivery. The sandbox mappers live in the Infinispan cache `MapperService-sandboxedmapper` (max idle 1 hour).

Framework rules for a content session (`UserSession.isContentDelivery()` is true):

| Mechanism | Rule |
|-----------|------|
| `HeadersFilter` | No `X-Frame-Options`. The CSP gets `frame-ancestors` with the main server URI. |
| `SameSiteCookieFilter` | The session cookie gets `SameSite=None; Partitioned`, because the browser loads it in a third-party iframe. |
| `ContentDeliveryDirectiveProvider` | Adds the content domain to the CSP source lists of the main application. |
| `RestApiLoginFilter` | Rejects REST calls with 403. Exception: the course database endpoint `/restapi/repo/courses/{id}/db/...`. |
| `UserSessionManager` | Sign-off of the parent session invalidates all sandbox sessions (`UserSession.getSandboxSessions()`). |

A sandboxed mapper stays usable for reloads of the same content session. Only the first request needs the token (OO-9728).

---

## 13. Business Path & Deep Linking

A server-centric architecture faces a fundamental tension: the browser's URL bar is document-oriented (each URL identifies a page), but the server's UI state is component-oriented (a tree of controllers managing an interactive session). When a user clicks inside OpenOlat, the interaction is dispatched to a specific component via its internal ID — the resulting URL (`/auth/1:2:3:4/...`) is a **component address**, meaningful only to the current session.

The **business path** system provides a second, **document-style address** for every UI state — a serializable path like `[RepositoryEntry:123][CourseNode:456]` that describes *what* the user is looking at, not *which component* renders it. This enables bookmarking, deep linking, browser back/forward, "open in new tab", and session resumption.

### 13.1 Business Path Format

A business path is a sequence of `ContextEntry` segments, each wrapping an `OLATResourceable`:

```
[RepositoryEntry:8519168][CourseNode:107803373834]
[HomeSite:1234][notifications:0]
[RepositoryEntry:123][CourseNode:456][path=/documents/report.pdf:0]
```

### 13.2 Dual-Address Architecture

**Outbound (state → URL):** After each interaction, the controller updates its `BusinessControl` via `setCurrentContextEntry()`. During the AJAX render cycle, `Window.handleBusinessPath()` serializes the path and sends an AJAX command that updates the browser URL via `history.replaceState()`.

**Inbound (URL → state):** When a user navigates to a business path URL (bookmark, deep link, new tab), `AuthenticatedDispatcher` parses it into `ContextEntry` objects and calls `NewControllerFactory.launch()`. The factory looks up a `ContextEntryControllerCreator` for the resource type, creates the controller, and — if it implements `Activateable2` — calls `activate()` to restore the navigation state recursively.

### 13.3 The Activateable2 Contract

Controllers supporting deep linking implement `Activateable2`:

```java
public interface Activateable2 {
    void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state);
}
```

The `entries` list contains the **remaining** entries. A typical implementation peels off the first, navigates to the sub-view, and passes the rest to the child:

```java
@Override
public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
    if (entries == null || entries.isEmpty()) return;
    ContextEntry entry = entries.get(0);
    String type = entry.getOLATResourceable().getResourceableTypeName();
    if ("CourseNode".equals(type)) {
        Long nodeId = entry.getOLATResourceable().getResourceableId();
        selectCourseNode(ureq, nodeId);
        if (entries.size() > 1 && currentNodeCtrl instanceof Activateable2 a2) {
            a2.activate(ureq, entries.subList(1, entries.size()), null);
        }
    }
}
```

### 13.4 BusinessControl Chain

`BusinessControl` models the path as a linked list mirroring the controller hierarchy. Each `WindowControl` wraps a `StackedBusinessControl` pointing to its parent. Key operations: `getAsString()` (serialize full path), `popLauncherContextEntry()` (consume next entry during activation), `setCurrentContextEntry()` (update current segment).

### 13.5 NewControllerFactory

The central registry mapping resource types to `ContextEntryControllerCreator` instances. Each creator knows how to create a controller, validate access, and decide whether to open as a site (fixed tab) or dynamic tab. Integrates with the **DTabs** tab system — activating an already-open tab calls `activate()` on its existing controller.

### 13.6 Session Resumption

`HistoryManager` persists the user's last `HistoryPoint` (containing the business path). On next login, `ResumeController` offers to restore the previous state by re-launching the saved business path through `NewControllerFactory.launch()`.

> **Key Insight:** The business path is what makes a server-centric application feel like a document-oriented web app. Without it, every URL would be a session-specific component address that breaks on refresh. With it, users get bookmarkable URLs, working back/forward buttons, and "open in new tab" — all while the server maintains full control of the UI state.

---

## 14. Controller Disposal & Housekeeping

Proper cleanup prevents memory leaks. The framework provides hierarchical disposal.

### The Dispose Lifecycle

```java
// DefaultController.dispose()
public synchronized void dispose() {
    synchronized (DISPOSE_LOCK) {
        if (disposed) return;    // prevent double-dispose
        disposed = true;          // suppress all future events
    }
    doPreDispose();   // framework cleanup (mappers, children)
    doDispose();      // custom cleanup (override this)
}
```

### Automatic Cleanup (no developer action needed)

- **Child controllers** registered via `listenTo()` — recursively disposed
- **Mappers** registered via `registerMapper()` — deregistered from MapperService
- **Disposable form items** (e.g., FileElement) — FormBasicController disposes all Disposable items

### Manual Cleanup (override doDispose)

```java
@Override
protected void doDispose() {
    // 1. Deregister EventBus listeners
    coordinatorManager.getCoordinator().getEventBus().deregisterFor(this, courseOres);

    // 2. Release locks
    if (lockEntry != null && lockEntry.isSuccess()) {
        coordinatorManager.getCoordinator().getLocker().releaseLock(lockEntry);
        lockEntry = null;
    }

    // 3. Null heavy references for GC
    currentNodeController = null;
}
```

### Replacing Controllers at Runtime

```java
// CORRECT: removes from child list, deregisters listener, disposes
removeAsListenerAndDispose(detailCtrl);
detailCtrl = new DetailController(ureq, wControl, item);
listenTo(detailCtrl);
```

### Disposal Checklist

| Resource | Cleanup | Automatic? |
|----------|---------|------------|
| Child controllers (`listenTo`) | Recursive dispose | Yes |
| Mappers | `MapperService.cleanUp()` | Yes |
| Disposable form items | `Disposable.dispose()` | Yes |
| EventBus listeners | `eventBus.deregisterFor()` | **No** |
| Locks | `locker.releaseLock()` | **No** |
| Heavy references | Set to `null` | **No** |

---

## 15. Accessibility (a11y)

OpenOlat integrates accessibility support directly into the component framework. Built-in renderers produce accessible HTML by default.

### ARIA in Component Renderers

| Component | ARIA Support |
|-----------|-------------|
| `TextElementRenderer` | `aria-required`, `aria-label`, `aria-controls`, `aria-describedby` for errors |
| `SingleSelectionRenderer` | `role="radiogroup"`, `aria-required` |
| `FormToggleRenderer` | `role="switch"`/`role="checkbox"`, `aria-checked`, `aria-labelledby`, `aria-disabled` |
| `TabbedPaneRenderer` | `role="tablist"`, `role="tab"` with `aria-selected`, `aria-controls` |
| `MenuTreeRenderer` | `role="tree"` |
| `DropdownRenderer` | `aria-label`, `aria-hidden="true"` for icons |
| `ExpandButtonRenderer` | `aria-haspopup`, `aria-controls`, `aria-expanded` |
| `AutoCompleterRenderer` | `aria-required`, `aria-label`, `aria-controls` |
| `Link` | `ariaLabel` and `ariaRole` properties |

### Screen Reader Support

The `$r.screenreaderOnly()` method wraps text in `<span class='sr-only'>` for content visible only to screen readers. Used for icon-only buttons, accordion states, lightbox close buttons.

### Modal & Dialog Accessibility

Dialogs use semantic `<dialog>` element with `aria-labelledby`. Lightbox adds `aria-modal="true"`.

A callout trigger follows the ARIA disclosure pattern (OO-8877):

```java
toolsLink.setAriaDialogOpener();   // role=button, aria-haspopup=dialog, aria-expanded=false
calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
        toolsCtrl.getInitialComponent(), toolsLink, title, true, "", settings);
```

`CloseableCalloutWindowController` sets `aria-expanded` on the trigger when it opens and closes the callout. It sends `FunctionCommand.pushDialogFocus()` on open and `popDialogFocus()` on close, so the focus returns to the trigger.

**Changed in 21.1.0:** The constructors accept the trigger `Link` or `FormLink` in place of a DOM ID (OO-9727). Without a title or an `aria-label` in `CalloutSettings`, the callout takes the title or the `aria-label` of the trigger as its label. The focus goes to the first visible element in the callout, not to the hidden close button. Buttons and FlexiTable action columns use the pattern.

### Form Label Association

FlexiForm layout templates auto-generate `<label>` with `for` attribute. Errors linked via `aria-describedby`. Mandatory fields get `aria-required="true"`.

### Keyboard Navigation

- Accordion: `onkeydown` handlers for Enter/Space
- Tabs and tree: standard keyboard patterns via semantic roles
- Decorative icons: `aria-hidden="true"`

### Accessibility Best Practices

- **Use framework components** — renderers handle ARIA automatically
- **Set `ariaLabel` on icon-only links:** `link.setAriaLabel(translate("action.edit"))`
- **Use `$r.screenreaderOnly()`** for hidden context text
- **Use `setMandatory(true)`** — triggers both visual asterisk and `aria-required`
- **Mark decorative images:** empty `alt=""`, meaningful alt text on content images
- **Use semantic elements:** `<dialog>`, `<nav>`, `<main>`
- **Test with keyboard-only navigation**

---

## 16. Database Access (Hibernate/JPA)

OpenOlat's persistence layer wraps Hibernate/JPA behind a `DB` facade that provides thread-local `EntityManager` access. This design means any code — in a controller, service, or background job — can call `DBFactory.getInstance().getCurrentEntityManager()` to get the current transaction's entity manager without explicit dependency injection.

The framework manages transaction boundaries automatically during request processing: the request begins a transaction, controllers and services do their work, and the framework commits after dispatch. For background tasks, developers must explicitly call `db.commitAndCloseSession()`.

```java
// Typical service method pattern:
public List<MyEntity> findByName(String name) {
    return dbInstance.getCurrentEntityManager()
        .createQuery("select e from MyEntity e where e.name = :name", MyEntity.class)
        .setParameter("name", name)
        .getResultList();
}

public void save(MyEntity entity) {
    dbInstance.getCurrentEntityManager().persist(entity);
}
```

### Entity Mapping

All entities use JPA annotations and are registered in `src/main/resources/META-INF/persistence.xml` (443 `<class>` entries on 21.1.0). Infrastructure: HikariCP connection pooling (`hibernate-hikaricp`), Infinispan L2 cache, MySQL/PostgreSQL/Oracle support.

`PersistenceHelper` has static helpers for JPQL results and query building.

**New in 21.1.0:** `PersistenceHelper.extractBigDecimal(Object[], pos, default)` reads an aggregate column of a projection query as `BigDecimal`.

---

## 17. Hibernate Session & Entity Lifecycle

OpenOlat uses a strict **session-per-request** pattern. Each HTTP request gets its own `EntityManager` via `ThreadLocal` in `DBImpl`. Between requests, **no session exists** — all entities are detached.

### The DB Lifecycle

| Method | Purpose |
|--------|---------|
| `getCurrentEntityManager()` | Get/create thread-bound EntityManager |
| `commitAndCloseSession()` | Commit + close + remove from ThreadLocal (**preferred**) |
| `rollbackAndCloseSession()` | Rollback + close on error |
| `intermediateCommit()` | Commit + close in bulk loops |

### Preventing Stale Objects

```java
// Pattern 1: Reload by key (most common)
RepositoryEntry entry = repositoryEntryDAO.loadByKey(entryKey);

// Pattern 2: Detach stale, reload fresh for pessimistic lock
dbInstance.getCurrentEntityManager().detach(staleEntry);
RepositoryEntry fresh = loadByKey(staleEntry.getKey());
dbInstance.getCurrentEntityManager().lock(fresh, LockModeType.PESSIMISTIC_WRITE);

// Pattern 3: Merge detached entity back
RepositoryEntry managed = dbInstance.getCurrentEntityManager().merge(detachedEntry);
```

### Preventing LazyInitializationException

```java
// Use join fetch in JPQL:
"select v from RepositoryEntry v"
    + " inner join fetch v.olatResource"
    + " left join fetch v.lifecycle"
    + " where v.key = :key"

// Or force initialization while session open:
entry.getOlatResource().getResourceableTypeName();
```

### Avoiding Unintended Dirty Checking

Hibernate detects changes to managed entities and flushes them on commit. To prevent accidental updates:

- Do not modify managed entities unless you intend to persist the change.
- Use DTOs or value objects when the UI layer can modify the data.
- Detach an entity (`em.detach(entity)`) when you need a read-only copy.

### Bulk Operations

Use `intermediateCommit()` to avoid huge transactions. `DBImpl` warns if >500 DB accesses in one transaction.

```java
for (Item item : largeList) {
    process(item);
    if (counter++ % 100 == 0) {
        dbInstance.intermediateCommit();
    }
}
```

---

## 18. Virtual File System (VFS)

OpenOlat does not access the filesystem directly. All file operations go through the **Virtual File System (VFS)** — an abstraction layer that decouples application code from physical storage, while transparently handling security, quotas, metadata, versioning, and trash.

Files are physically stored under `bcroot/` (the "base canonical root"), with metadata in the `o_vfs_metadata` database table.

### 18.1 Core Interfaces

- **`VFSItem`** — base interface. Provides `resolve(path)`, `rename()`, `delete()`, `exists()`, capability checks (`canRename()`, `canMeta()`, `canVersion()`), and `getMetaInfo()` / `getLocalSecurityCallback()`.
- **`VFSContainer`** (extends VFSItem) — directories. Adds `getItems(filter)`, `getDescendants(filter)`, `createChildContainer(name)`, `createChildLeaf(name)`, `copyFrom(source, identity)`.
- **`VFSLeaf`** (extends VFSItem) — files. Adds `getInputStream()`, `getOutputStream(append)`, `getSize()`.

### 18.2 Key Implementations

**`LocalFolderImpl`** / **`LocalFileImpl`** — wrap `java.io.File` objects in `bcroot/`. The workhorses of the VFS.

**`NamedContainerImpl`** — wraps a container with a different display name without changing the filesystem path.

**`MergeSource`** — creates a virtual container that merges children from multiple physical folders:

```java
MergeSource root = new MergeSource(null, "courseFiles");
root.addContainersChildren(courseFolder, true);  // merge children, enableWrite=true → write target
root.addContainer(new NamedContainerImpl("_shared", sharedFolder));  // add as sub-folder
```

The `enableWrite=true` parameter designates the **rootWriteContainer** — uploads to this MergeSource land in that folder.

### 18.3 Security Callbacks

Each VFS item can carry a `VFSSecurityCallback` defining permitted operations:

```java
public interface VFSSecurityCallback {
    boolean canRead(), canWrite(), canCreateFolder(), canDelete(), canList(), canCopy();
    Quota getQuota();
}
```

Callbacks **inherit down the tree**: children without their own callback use their parent's. Common implementations:
- `FullAccessCallback` — all permitted, no quota
- `ReadOnlyCallback` — read/list only
- `FullAccessWithQuotaCallback` — full access with storage limits
- `DefaultVFSSecurityCallback` — everything denied (safe fallback)

### 18.4 Metadata & Versioning

`VFSRepositoryService` manages file metadata in the database. The `VFSMetadata` interface exposes identity tracking (creator, last modifier, deleter), Dublin Core fields (title, publisher, language), download counts, and deletion tracking.

**Versioning** is automatic for files where `canVersion() == YES`. Calling `VFSRepositoryService.itemSaved(leaf, identity)` creates a new revision.

**Trash**: `delete()` moves files to a `._ootrash` folder (soft delete). `deleteSilently()` removes permanently. Cleanup via `VFSRepositoryService.cleanTrash()`.

### 18.5 Quotas

Storage quotas are enforced through security callbacks. A `Quota` object gives `getQuotaKB()` (maximum storage) and `getUlLimitKB()` (per-upload limit). `Quota.UNLIMITED` (-1) disables the check. `LocalFolderImpl` checks the quota automatically during `copyFrom()`:

```java
long quotaLeftKB = VFSManager.getQuotaLeftKB(container);
if (quotaLeftKB != Quota.UNLIMITED && quotaLeftKB < (fileSize / 1024)) {
    // Not enough space
}
```

### 18.6 Utilities

`VFSManager` provides static helpers: `olatRootContainer(relPath)` / `olatRootLeaf(relPath)` for bcroot access, `resolveFile(root, path)` for lookups, `resolveOrCreateContainerFromPath()` / `resolveOrCreateLeafFromPath()` for path creation, `getRealPath()` to unwrap virtual containers, `findInheritedSecurityCallback()` to find effective permissions, `copyContent()` to write streams and leaves. `VFSContainer.isInPath(path)` rejects directory traversal, for example in `ZipUtil`.

### 18.7 Filtering

`VFSItemFilter` (single method `accept(VFSItem)`) controls which items appear in `getItems()`. Built-in: `VFSSystemItemFilter` (excludes hidden files and `__MACOSX`), `VFSLeafFilter` (files only), `VFSContainerFilter` (folders only), `VFSItemSuffixFilter` (by extension), `VFSAndFilter`/`VFSOrFilter` (composites). Set default filter on a container with `setDefaultItemFilter()`.

### 18.8 HTTP File Delivery

`VFSMediaResource` bridges VFS and HTTP. It wraps a `VFSLeaf` for browser delivery with MIME types, `Content-Disposition`, range requests and cache control. HTML and SVG files get restrictive CSP headers.

```java
VFSLeaf file = (VFSLeaf) container.resolve("report.pdf");
VFSMediaResource resource = new VFSMediaResource(file);
resource.setDownloadable(true);  // force download instead of inline display
ureq.getDispatchResult().setResultingMediaResource(resource);
```

### 18.9 Real-World Example: MergedCourseContainer

`MergedCourseContainer` (package `org.olat.course`) merges the file resources of a course into one tree:

```java
// simplified from MergedCourseContainer.init()
VFSContainer courseFolder = persistingCourse.getIsolatedCourseFolder();
addContainersChildren(courseFolder, true);                        // merge, write target
addContainer(new NamedContainerImpl("_sharedfolder", sharedFolder)); // shared folder as sub-folder
// plus a MergedCourseElementDataContainer with the folders of the course elements
```

The user sees one folder tree. The VFS routes every operation to the correct physical folder.

> **Key Rule**: Never access `bcroot/` directly. Always use VFS classes to ensure security, quotas, metadata, and versioning are respected.

---

## 19. Spring & Dependency Injection

OpenOlat uses Spring for dependency injection, with a hybrid approach: newer code uses annotation-based wiring (`@Service`, `@Autowired`), while older code uses XML configuration. The main Spring context at `src/main/java/org/olat/_spring/mainContext.xml` imports 30 top-level contexts (for example `mainCorecontext.xml`, `modulesContext.xml`, `restApiContext.xml`). These import the contexts in the `_spring/` folders of the modules. A `context:component-scan` picks up the annotated beans.

```java
// Service locator (in controllers and non-Spring classes):
UserManager userManager = CoreSpringFactory.getImpl(UserManager.class);

// Standard injection (in Spring-managed beans):
@Service
public class MyServiceImpl implements MyService {
    @Autowired private DB dbInstance;
    @Autowired private UserManager userManager;
}
```

Configuration: `src/main/resources/serviceconfig/olat.properties` (defaults) + `olat.local.properties` (overrides). Beans read the values with `@Value("${key:default}")` or property placeholders. An `AbstractSpringModule` persists values changed in the administration UI to `{userdata}/system/configuration/{FQCN}.properties` and reads them in `initFromChangedProperties()`.

Controllers are not Spring beans, but the framework autowires them: `@Autowired` fields work in every controller. `BasicController` itself uses an `@Autowired MapperService`.

### AI Services

The AI services are Spring beans in `org.olat.core.commons.services.ai`. Inject the feature service (for example `AiMCQuestionService`) and never an SPI. Section 31 describes the architecture, the task pools and the user control of AI features.

---

## 20. Module Configuration (`AbstractSpringModule`)

A module class holds the configuration of a feature: switches, limits, defaults. It extends `AbstractSpringModule` (package `org.olat.core.configuration`) and is a Spring bean. Examples: `AiModule`, `RestModule`, `HttpClientModule`.

### Lifecycle

| Phase | Method | What happens |
|-------|--------|--------------|
| Construction | `AbstractSpringModule(coordinatorManager)` | Creates the `PersistedProperties` of the module. |
| Spring init | `afterPropertiesSet()` | Loads the persisted file, calls `initDefaultProperties()`, then `init()`. |
| Init | `init()` (abstract) | Reads each value: persisted value first, else the `@Value` default from `olat.properties`. |
| Change | `initFromChangedProperties()` (abstract) | Runs after every save, on this node and on all other nodes. |
| Shutdown | `destroy()` | Deregisters the event listener. |

### Reading and Writing Settings

```java
@Service
public class MyModule extends AbstractSpringModule {

    private static final String MY_ENABLED = "my.enabled";

    @Value("${my.enabled:false}")
    private boolean enabled;

    @Autowired
    public MyModule(CoordinatorManager coordinatorManager) {
        super(coordinatorManager);
    }

    @Override
    public void init() {
        String enabledObj = getStringPropertyValue(MY_ENABLED, true);
        if (StringHelper.containsNonWhitespace(enabledObj)) {
            enabled = "true".equals(enabledObj);
        }
    }

    @Override
    protected void initFromChangedProperties() {
        init();
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        setStringProperty(MY_ENABLED, Boolean.toString(enabled), true); // save and fire the event
    }
}
```

- Getters: `getStringPropertyValue()`, `getIntPropertyValue()`, `getBooleanPropertyValue()`, `getLongPropertyValue()`.
- Setters: `setStringProperty()`, `setIntProperty()`, `setBooleanProperty()`, `setLongProperty()`, `removeProperty()`. With `saveConfiguration=true`, the value is saved at once. With `false`, call `savePropertiesAndFireChangedEvent()` later.
- `setSecretStringProperty()` stores a value without writing it to the log. `setStringProperty()` writes every change to the audit log (`Tracing.M_AUDIT`).
- The constructor `AbstractSpringModule(coordinatorManager, true)` encrypts the persisted file (secured module).

### Storage and Cluster Propagation

`PersistedProperties` stores the values in the file `{userdata}/system/configuration/{fully.qualified.ClassName}.properties`. The database table `o_property` is not used for module settings. It belongs to `PropertyManager`, which stores properties of users, groups and resources.

```
admin UI -> module.setXxx() -> PersistedProperties.savePropertiesAndFireChangedEvent()
   -> write {userdata}/system/configuration/{FQCN}.properties
   -> EventBus: PersistedPropertiesChangedEvent on the channel of the module class
   -> every node: AbstractSpringModule.event()
        other node: loadPropertiesFromFile()   (the userdata directory is shared)
        all nodes:  initFromChangedProperties()
```

Rules:

- A saved value wins over `olat.properties` and `olat.local.properties`. A changed default in `olat.properties` has no effect on an instance that saved the value.
- Deleting the file restores the defaults.
- Read a setting from the module each time you need it, if the change must apply at runtime. Copy it at init time only if a restart is acceptable.
- An upgrade runs after `init()`. It must call the setter of the module to change a value (section 23).

---

## 21. Scheduler & Background Tasks

OpenOlat uses **Quartz** for all scheduled tasks (no Spring `@Scheduled`). Configured in `schedulerContext.xml` with a 5-thread pool.

### JobWithDB

`JobWithDB` is the base class for all jobs. It commits the DB session on success and rolls back on error:

```java
public abstract class JobWithDB extends QuartzJobBean {
    protected final void executeInternal(JobExecutionContext ctx) {
        try {
            executeWithDB(ctx);
            DBFactory.getInstance().commitAndCloseSession();
        } catch (Exception e) {
            DBFactory.getInstance().rollbackAndCloseSession();
        }
    }
}
```

### TaskExecutorManager

`TaskExecutorManager` has thread pools for async (non-periodic) execution. A `TaskRunnable` selects its pool with `TaskRunnable.Queue`:

| Pool (Spring bean) | Threads | Queue |
|--------------------|---------|-------|
| `mpTaskSpringExecutor` | 2 to 5 | `standard` (default) |
| `sequentialTaskSpringExecutor` | 1 | `sequential` |
| `lowPriorityTaskSpringExecutor` | 2 | `lowPriority` |
| `externalPriorityTaskSpringExecutor` | 2 to 5 | `external` |
| `aiInteractiveTaskSpringExecutor` | 4 (from `AiModule`) | `aiInteractive`: AI calls a user waits for |
| `aiBatchTaskSpringExecutor` | 2 (from `AiModule`) | `aiBatch`: long AI batch work |

`AiTaskExecutorService` resizes the two AI pools from the `AiModule` configuration at startup and at runtime (OO-9496). A `LongRunnable` is persisted in `o_ex_task` and survives a restart (section 22).

### Key Scheduled Jobs

Notifications, statistics, search indexing, REST token cleanup, reminders, video transcoding, assessment evaluation, lifecycle management, and more. All are Quartz triggers in `schedulerContext.xml` or in a module context.

A job that must run on one cluster node only uses the singleton pattern. The trigger references the bean `myJob.${cluster.singleton.services}`. The bean `myJob.enabled` runs the job class, the bean `myJob.disabled` runs `DummyJob`:

```xml
<bean id="apiAuditRetentionTrigger" class="org.springframework.scheduling.quartz.CronTriggerFactoryBean">
  <property name="jobDetail" ref="apiAuditRetentionJob.${cluster.singleton.services}" />
  <property name="cronExpression" value="0 41 1 * * ?" />
</bean>
<bean id="apiAuditRetentionJob.enabled" class="org.springframework.scheduling.quartz.JobDetailFactoryBean" lazy-init="true">
  <property name="jobClass" value="org.olat.restapi.audit.manager.ApiAuditLogRetentionJob" />
</bean>
<bean id="apiAuditRetentionJob.disabled" class="org.springframework.scheduling.quartz.JobDetailFactoryBean" lazy-init="true">
  <property name="jobClass" value="org.olat.core.commons.services.scheduler.DummyJob" />
</bean>
```

**New in 21.1.0:** `ApiAuditLogRetentionJob` deletes the rows of the REST API audit log that are older than `restapi.auditlog.retention.days` (section 37).

---

## 22. Persisted Long-Running Tasks

A task that takes longer than a user waits, or that must survive a restart, runs as a `LongRunnable` (package `org.olat.core.commons.services.taskexecutor`). `TaskExecutorManager` persists it in the table `o_ex_task` and runs it on a thread pool. Examples: `BulkAssessmentTask`, `DeleteUserDataTask`, `EssayAiCorrectionTask`, `QtiQuestionGenerationTask`.

### Runnable Types

| Type | Persisted | Use for |
|------|-----------|---------|
| `Runnable` / `TaskRunnable` | No | Short async work. Lost on restart. `taskExecutorManager.execute(runnable)` |
| `LongRunnable` (extends `TaskRunnable`, `Serializable`) | Yes, XStream XML in `o_ex_task` | Long or important work that must finish after a restart |
| `TaskAwareRunnable` | (mixin) | Receives its `Task` row with `setTask()`, for example to report progress |

`TaskRunnable.getExecutorsQueue()` selects the pool (`Queue.standard` is the default, section 21). `LongRunnable.isDelayed()` defaults to `true`: the task waits for the next run of the executor job. Return `false` to start it at once.

### Writing a Task

```java
public class MyExportTask implements LongRunnable {

    private static final long serialVersionUID = 1L;

    private final Long exportKey;          // keep the state small: keys, not entities

    public MyExportTask(Long exportKey) {
        this.exportKey = exportKey;
    }

    @Override
    public Queue getExecutorsQueue() {
        return Queue.lowPriority;
    }

    @Override
    public boolean isDelayed() {
        return false;                        // start now, not with the next executor job
    }

    @Override
    public void run() {
        CoreSpringFactory.getImpl(MyExportService.class).runExport(exportKey);
    }
}

// submit
taskExecutorManager.execute(new MyExportTask(export.getKey()), getIdentity(), resource, "my-export", null);
```

- The task is serialized with XStream. Store keys and simple values, never entities or controllers.
- Look up services with `CoreSpringFactory.getImpl()` inside `run()`, because the task is deserialized without Spring.
- `execute(task, creator, resource, resSubPath, scheduledDate)` binds the task to a resource. `getTasks(resource)` and `hasRunningTasks(resources)` find it again, for example to show a status.
- A `TaskCompanionFactory` creates a companion row in the same transaction as the task.
- `updateProgress(task, progress, checkpoint)` and `getProgress(task)` report progress.

### Lifecycle and Recovery

`TaskStatus` values: `newTask`, `inWork`, `edition`, `failed`, `done`, `ignore`, `cancelled`.

1. `execute()` writes the row with status `newTask` and commits.
2. The Quartz job `taskExecutorJob` (`ExecutorJob`, every 5 minutes on every node) or the direct start (`isDelayed() == false`) wraps the key in a `PersistentTaskRunnable`.
3. `PersistentTaskDAO.pickTaskForRun()` locks the row (`select for update`). It sets `inWork`, the node ID and the boot ID of the node. Only one node can pick a task.
4. The task runs. The runner commits the DB session and marks the row as done, or rolls back and marks it `failed`.
5. The EventBus channel `TaskExecutorManager.TASK_EVENTS` receives `TaskEvent.TASK_STARTED` and `TASK_DONE`.

Recovery after a restart: the named query `taskToDos` also returns `inWork` tasks of the same node with another boot ID. A task that was running when its node stopped starts again on that node. Write `run()` so that a second run is safe.

When a pool is full (`RejectedExecutionException`), the task stays `newTask` and the next executor job tries again.

`pickTaskForEdition()` / `returnTaskAfterEdition()` protect a task while a user edits its parameters. `cancel()` stops a waiting task.

---

## 23. Upgrade Infrastructure (`org.olat.upgrade`)

OpenOlat includes a built-in migration framework in the `org.olat.upgrade` package that handles both database schema changes and data transformations when upgrading between versions. The system is designed to run automatically on startup, track progress persistently, and support clustered deployments.

### Two-Phase Upgrade Process

The upgrade system operates in two distinct phases:

1. **Database schema upgrades:** Executed early during Spring context initialization by the `DatabaseUpgradeManager`. These run SQL ALTER scripts (located in `src/main/resources/database/mysql/`, `.../postgresql/` and `.../oracle/`, for example `alter_21_0_x_to_21_1_0.sql`) to bring the database schema up to date. This happens *before* most services and modules are initialized, ensuring the schema is ready for application code.

2. **Post-system-init upgrades** — Executed after the entire application (all Spring beans, modules, and services) has fully initialized. The `UpgradeManager` listens for the `FrameworkStartedEvent` and then runs data migration logic in a background thread via `TaskExecutorManager`. These upgrades can use any service or manager through `@Autowired` injection.

> **Important:** Post-system-init upgrades run *after* all services and modules have been initialized. This means that any upgrade that changes module configurations (e.g., enabling features, modifying default settings in an `AbstractSpringModule` subclass) must be aware that the module has already been initialized with its previous configuration. If the module reads configuration during `init()`, changes made by the upgrader will only take effect on the *next* startup. In such cases, the upgrader may need to trigger a re-initialization of the affected module, or the module itself must be designed to pick up configuration changes dynamically.

### Key Classes

| Class | Role |
|-------|------|
| `OLATUpgrade` | Abstract base class for all upgrades. Provides `getVersion()`, `doPostSystemInitUpgrade()`, and helpers for SQL execution. |
| `DatabaseUpgrade` | Simple wrapper for schema-only upgrades (SQL ALTER scripts, no post-init logic). |
| `UpgradeHistoryData` | Persistent state container tracking which upgrades (and sub-tasks within an upgrade) have completed. |
| `UpgradesDefinitions` | Spring bean holding the ordered list of all registered `OLATUpgrade` instances. |
| `UpgradeManager` | Abstract base managing upgrade execution, history loading/saving, and event coordination. |
| `UpgradeManagerImpl` | Production implementation that executes post-system-init upgrades in a background thread. |
| `UpgradeManagerDummy` | No-op implementation used on non-singleton cluster nodes to prevent duplicate execution. |
| `DatabaseUpgradeManager` | Extends `UpgradeManagerImpl` to also run SQL ALTER scripts during early initialization. |

### Upgrade Registration

Upgrades are registered as ordered Spring bean lists in `org/olat/upgrade/_spring/upgradeContext.xml` (post-init upgrades) and `databaseUpgradeContext.xml` (schema upgrades):

```xml
<!-- upgradeContext.xml -->
<bean id="olatupgrades" class="org.olat.upgrade.UpgradesDefinitions">
  <property name="upgrades">
    <list>
      <bean id="upgrade_16_0_0" class="org.olat.upgrade.OLATUpgrade_16_0_0"/>
      <bean id="upgrade_16_1_0" class="org.olat.upgrade.OLATUpgrade_16_1_0"/>
      <!-- ... upgrades in version order ... -->
    </list>
  </property>
</bean>
```

The ordering of beans in the list determines execution order. Each upgrade class follows the naming convention `OLATUpgrade_XX_Y_Z` matching the target version.

### Execution Lifecycle

```
OpenOLATServlet.init()
├── Spring context starts
│   ├── DatabaseUpgradeManager.init()
│   │   └── Executes SQL ALTER scripts immediately
│   │
│   ├── All module beans initialized (AbstractSpringModule.init(), etc.)
│   │
│   └── UpgradeManager.init()
│       ├── Loads upgrade history from olatdata/system/installed_upgrades.xml
│       └── Registers for FrameworkStartedEvent
│
└── Framework fully loaded → FrameworkStartedEvent fired
    └── UpgradeManager receives event
        └── Submits UpgradesTask to TaskExecutorManager (background thread)
            └── For each upgrade (in order):
                ├── Check if already completed (via UpgradeHistoryData)
                ├── Execute doPostSystemInitUpgrade()
                ├── Commit DB session
                └── Persist state to installed_upgrades.xml
```

### Writing an Upgrade

Each upgrade class follows a standard pattern with idempotent sub-tasks:

```java
public class OLATUpgrade_20_3_0 extends OLATUpgrade {
    private static final String VERSION = "OLAT_20.3.0";
    private static final String MIGRATE_SETTINGS = "MIGRATE SETTINGS";

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
            return false;  // Already done
        }

        boolean allOk = true;
        allOk &= migrateSettings(upgradeManager, uhd);

        uhd.setInstallationComplete(allOk);
        upgradeManager.setUpgradesHistory(uhd, VERSION);
        return allOk;
    }

    private boolean migrateSettings(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
        if (uhd.getBooleanDataValue(MIGRATE_SETTINGS)) {
            return true;  // Sub-task already done
        }
        // ... perform migration using @Autowired services ...
        uhd.setBooleanDataValue(MIGRATE_SETTINGS, true);
        upgradeManager.setUpgradesHistory(uhd, VERSION);
        return true;
    }
}
```

Key patterns:
- **Idempotent sub-tasks** — Each migration step checks a boolean flag in `UpgradeHistoryData` before executing, allowing safe resume after interruption.
- **Service injection** — Upgrades are Spring beans and can `@Autowired` any service.
- **Batch processing** — Large data migrations should process in batches with `intermediateCommit()`.
- **Version constant** — Format is `OLAT_XX.Y.Z` (e.g., `OLAT_20.3.0`).
- **Schema script per dialect:** Add the same change to the MySQL, PostgreSQL and Oracle script. Register a `DatabaseUpgrade` bean with `alterDbStatements` in `databaseUpgradeContext.xml`.
- **Upgrade-private entities:** An upgrade that needs an old table layout maps it with its own entity in `org.olat.upgrade.model` (for example `UpgradeOrganisationUnitMembershipImpl`). Register the class in `persistence.xml`.

**New in 21.1.0:** `OLATUpgrade_21_1_0` shows how to introduce a secure default without a change of behaviour for existing instances. A new instance gets `http.ssrf.protection.enabled=true`. The upgrade switches the protection off on an existing instance, unless the key is set in `olat.local.properties` or as a system property (`isExplicitlyConfigured()`). The same upgrade adds the new user property `customerNumber` to the usage contexts of instances with a customized user property configuration.

### State Tracking

Upgrade progress is persisted as XStream-serialized XML files in the `olatdata/system/` directory:
- `installed_upgrades.xml` — Tracks post-system-init upgrades
- `installed_database_upgrades.xml` — Tracks database schema upgrades

Each upgrade version maps to an `UpgradeHistoryData` object containing an `installationComplete` flag and a map of sub-task completion flags.

### Cluster Awareness

In a clustered deployment, only the singleton node (configured via `cluster.singleton.services=enabled`) runs upgrades. Other nodes use `UpgradeManagerDummy`, which is a no-op implementation. This prevents concurrent upgrade execution across cluster nodes.

---

## 24. GUI Preferences

Per-user preference storage for persisting UI state across sessions — table column visibility, segment selection, view modes.

### Preferences Interface

```java
Preferences prefs = ureq.getUserSession().getGuiPreferences();
String value = (String) prefs.get(MyController.class, "selectedTab");
prefs.putAndSave(MyController.class, "selectedTab", "overview");
```

Key = class (namespace) + string key. Values serialized via XStream, stored in `o_gui_prefs` table.

### GuiPreferenceService

`GuiPreferenceService` (package `core.util.prefs.gui`) is the database-backed storage behind `Preferences`. Controllers use the `Preferences` interface of the user session. Guests get transient `RamPreferences`, because all guests of one language share one identity.

### Common Uses

- **FlexiTable column preferences** — show/hide columns persisted automatically
- **SegmentView selection** — remembers last selected tab
- **View mode** — table vs. card, compact vs. expanded
- **Filter state** — last-used filter settings
- **Form sections:** collapsed state of a `FormSection` with `setPersistedStatusId()` (section 8)
- **AI choices:** `AiUserPreferenceService` stores the choice per AI feature (section 19)

---

## 25. Internationalization (i18n)

### Core Architecture

**Core classes:**
- `I18nModule` (`org.olat.core.util.i18n`) — Spring module, manages language discovery, configuration, overlay/locale setup, gender strategies
- `I18nManager` — Singleton service for key resolution, property loading, caching, recursive reference resolution
- `PackageTranslator` (`org.olat.core.gui.translator`) — Per-controller translator bound to a specific package and locale, with fallback chain to `org.olat.core` and `org.olat` bundles

Translation files follow the colocated resource pattern: each UI package contains an `_i18n/` directory with `LocalStrings_XX.properties` files (Java `.properties` format: `key=value`, `#` comments, `\:` escapes colons).

**Statistics (21.1.0):** 408 i18n bundles with a `LocalStrings_en.properties`, about 33,000 English keys, 31 language codes plus the demo overlay `en__DEMOVENDOR`.

### Locale Resolution Order

The `I18nManager.getLocalizedString()` method resolves translations through this fallback chain (overlay is checked first at every step):

```
a) Overlay of requested locale (e.g. de_CH__customizing)
b) Requested locale (de_CH)
c) Overlay of locale without variant (de__customizing, if locale had variant)
d) Locale without variant (de)
e) Overlay of locale without country (if locale had country)
f) Locale without country
g) Overlay of default locale (en__customizing)
h) Default locale (en, configurable)
i) Overlay of fallback locale
j) Fallback locale (en, hardcoded)
k) Error: NO_TRANSLATION_ERROR_PREFIX
```

### Overlay Mechanism (Client Customization)

The **overlay** system allows clients to customize any translation key without modifying source code. Overlay files go in:

```
{userData}/customizing/lang/overlay/{package}/_i18n/LocalStrings_XX__customizing.properties
```

The overlay is checked **first** at every step of the fallback chain. A client can override any translation — product name, button label, help text — by placing a single properties file in the overlay directory. The overlay name `customizing` is configured in `I18nModule`.

### Recursive Key Resolution (Cross-Referencing)

Translation values can reference other translation keys using two syntaxes:

1. **Same-package reference:** `$\:other.key` — resolves `other.key` from the same `.properties` file
2. **Cross-package reference:** `$org.olat.other.package:other.key` or `${org.olat.other.package:other.key}` — resolves from a different package

Resolution is recursive up to 10 levels (guarded by `recursionLevel` counter). The regex pattern used: `\$\{?([package]):([key])\}?` (see `resolvingKeyPattern` in `I18nManager`).

When caching is enabled, references are resolved at property load time and cached. When caching is disabled (dev mode), resolution happens at access time. A `referencingBundlesIndex` tracks which bundles reference which keys for cache invalidation.

Currently ~851 same-package and ~1,171 cross-package references are in use across the codebase.

### Fallback Bundle Chain

`PackageTranslator` first looks in its primary bundle, then falls back to:
- `org.olat.core` (core fallback bundle) — common framework translations
- `org.olat` (application fallback bundle) — common application translations

### Parameter Substitution

Uses `java.text.MessageFormat`: `{0}`, `{1}` etc. Single quotes have special meaning in MessageFormat and must be escaped as `''`.

```properties
greeting=Hello {0}, welcome to {1}!
```

### Gender Strategy

For languages with gendered nouns (especially German), a second gender word ending is written in curly brackets:

```properties
user.label=Benutzer{in}
```

At runtime, `I18nManager.applyGenderStrategy()` converts this based on the configured `GenderStrategy` per locale:
- `star`: `Benutzer*in` (default)
- `colon`: `Benutzer:in`
- `middleDot`: `Benutzer⸱in`
- `slash`: `Benutzer/in`
- `slashDash`: `Benutzer/-in`
- `camelCase`: `BenutzerIn`

### Caching

Controlled by `localization.cache` property (default: `true`). Cluster-aware: `I18nReInitializeCachesEvent` fired via EventBus to flush caches on all nodes. Dev mode disables caching for live property file reloading.

### Usage

```java
// In controllers (BasicController/FormBasicController):
String text = translate("my.key");
String withParams = translate("greeting", new String[]{ identity.getName() });

// In Velocity templates:
$r.translate("my.key")
$r.translate("greeting", $userName)

// Direct I18nManager access (rare, for services):
I18nManager.getInstance().getLocalizedString(bundleName, key, args, locale, true, true);
```

### Glossary

The end-user glossary of product-specific terms lives in the docs repo at `OpenOLAT-docs/sites/reference_glossary/docs/glossary.md` (EN) and `glossary.de.md` (DE). Canonical i18n term mappings across languages live in `doc/i18n-translation-reference.yaml`; `doc/i18n-translation-reference.md` is its readable view. Both are generated from the OpenOlat concept map (`fxIntelligence/knowledge/openolat/concept-map/`) and are not edited by hand.

---

## 26. Theming & CSS

OpenOlat uses SASS-based theming. Themes live in `src/main/webapp/static/themes/` and are served via `/raw/` with versioned URLs.

### Theme Directory Structure

```
themes/
├── light/                  ← base theme
│   ├── theme.scss          ← main SCSS entry
│   ├── theme.css           ← compiled (committed)
│   ├── _config.scss        ← SCSS variables
│   ├── modules/            ← module-specific SCSS
│   └── meta/               ← favicons, PWA manifest
└── openolat/               ← product theme (extends light/)
    └── _config.scss        ← override variables only
```

### Theme Layering

The `light` theme is the base (includes Bootstrap). Product themes extend it by overriding `_config.scss` variables. The Sass compiler loads the module partials from `light/modules/` for every theme.

### Compilation

Compilation via `compiletheme.sh` (wraps the `sass` CLI, option `-w` for watch mode). Compiled CSS is committed, so there is no build step at deployment.

### Custom Themes

Custom themes can be stored outside the codebase in a configurable `guiCustomThemePath`. The `Theme` class renders the header elements: CSS link, optional custom JS, favicons, Apple touch icons, PWA manifest. In templates: `$r.staticThemeLink("image.png")`. The frontend reference `doc/openolat-frontend.md` documents the theme system in detail.

---

## 27. Caching Infrastructure

OpenOlat uses **Infinispan** for caching, accessed via `Coordinator.getCacher()`.

### Obtaining a Cache

```java
@Autowired
private CoordinatorManager coordinatorManager;

private CacheWrapper<Long, CourseData> courseCache;

@PostConstruct
public void init() {
    courseCache = coordinatorManager.getCoordinator().getCacher()
        .getCache("CourseService", "courses");
}
```

### CacheWrapper API

```java
cache.get(key)                          // retrieve
cache.put(key, value)                   // store (reload from DB)
cache.update(key, value)                // store (propagates invalidation in cluster)
cache.putIfAbsent(key, value)           // atomic conditional store
cache.computeIfAbsent(key, k -> load()) // lazy loading
cache.remove(key)                       // explicit invalidation
```

`put()` = reload from persistent source. `update()` = data changed, trigger cluster invalidation.

`put(key, value, lifespan, maxIdleTime)` stores an entry with its own expiration in seconds. It replaced `put(key, value, expirationTime)` in 21.0.x (OO-9728).

### Cache Configuration

`getCache(type, name)` returns the cache `type-name`. Caches are configured in `src/main/resources/infinispan-config.xml`. A cache without an entry gets the defaults of `InfinispanCacher`: 10,000 entries and 15 minutes max idle.

| Cache | Max Entries | Max Idle | Content |
|-------|-------------|----------|---------|
| `CourseFactory-courses` | 1,500 | 1 hour | Course structures |
| `UserManager-username` | 20,000 | 45 min | Username lookups |
| `Velocity-templates` | 7,700 | Never | Compiled templates |
| `QTIWorks-testSessionControllers` | 50,000 | 2 hours | Test execution state |
| `VFSLockManager-file-locks` | 50,000 | 6 hours | WebDAV locks |
| `MapperService-sandboxedmapper` | default | 1 hour | Sandboxed mappers of the content domain (section 12) |
| `RequestRateLimiter-window` | 50,000 | never, lifespan 61 s | **New in 21.1.0:** Counters of the REST rate limiter (section 37) |

### Cache Invalidation

1. **TTL-based** — Infinispan reaper thread removes idle entries
2. **Event-driven** — `MultiUserEvent` via EventBus triggers `cache.remove()` on all nodes
3. **Manual** — admin UI (`AllCachesController`) can flush individual caches

```java
// Event-driven invalidation (MyDataChangedEvent is an example class):
// 1. After a change, fire the event on the channel
CoordinatorManager.getInstance().getCoordinator().getEventBus()
    .fireEventToListenersOf(new MyDataChangedEvent(dataKey), dataOres);

// 2. The listener on every node removes the entry
@Override
public void event(Event event) {
    if (event instanceof MyDataChangedEvent e) {
        dataCache.remove(e.getDataKey());
    }
}
```

---

## 28. Clustering

OpenOlat runs on one node or on several nodes behind a load balancer with sticky sessions. The nodes share the database and the `userdata` directory. The `Coordinator` (`CoordinatorManager.getInstance().getCoordinator()`) gives access to the cluster services: `getEventBus()`, `getLocker()`, `getSyncer()`, `getCacher()`. The same `ClusterCoordinator` runs in single-node mode and in cluster mode.

### Cluster Configuration

| Property | Default | Purpose |
|----------|---------|---------|
| `cluster.mode` | `SingleVM` | `Cluster` enables the cluster features |
| `node.id` | 1 | Unique ID of the node, 1 to 64 (`ClusterConfig.getNodeId()`) |
| `cluster.singleton.services` | `enabled` | `enabled` on exactly one node, `disabled` on the others |
| `jms.broker.url` | `vm://0?...` | Embedded Artemis broker; a remote broker (`failover:(tcp://...)`) in a cluster |

### Cluster Services

| Service | Implementation | Scope and storage |
|---------|----------------|-------------------|
| EventBus | `ClusterEventBus` | JMS topic `olat/{instance.id}/sysbus` (ActiveMQ Artemis, bean `sysbus.topic`). Every event goes through the broker, also on a single node. |
| Locker | `ClusterLocker` + `ClusterLockManager` | Persistent locks in the table `oc_lock`, cluster wide. Released on logout of the owner. |
| Syncer | `ClusterSyncer` | `doInSync(ores, callback)`: a JVM lock plus a DB row lock in `o_plock` (`select for update`) for the duration of the transaction |
| Cacher | `InfinispanCacher` | Local Infinispan caches on every node (`local-cache` in `infinispan-config.xml`). No replication. |
| Hibernate L2 cache | `InfinispanRegionFactory` | Local configuration (`infinispan-configs-local.xml`) |

### EventBus Transport

`ClusterEventBus.fireEventToListenersOf()` wraps the event in a `JMSWrapper` (node ID, message ID, resource, event) and sends it to the topic. Every node, the sender included, receives the message and delivers it to its listeners. The delivery is asynchronous and runs on the JMS listener thread, not in the request of the sender.

- The event must be `Serializable` (extend `MultiUserEvent`). Keep it small: keys, not entities.
- `MultiUserEvent.isEventOnThisNode()` tells whether the event comes from this node. `AbstractSpringModule` uses it to reload its file only on the other nodes (section 20).
- A JMS failure throws an `OLATRuntimeException`. The bus fails fast, so a broken broker is visible at once.
- `ureq.getUserSession().getSingleUserEventCenter()` is a local bus inside one user session, without JMS.

### Singleton Services

Some services must run on one node only: upgrades (`UpgradeManagerDummy` on the other nodes), statistics, notifications, retention jobs. The pattern uses the property `cluster.singleton.services` in the bean name (section 21):

```xml
<property name="jobDetail" ref="myJob.${cluster.singleton.services}" />
```

The persisted task executor needs no singleton: every node picks tasks, and the row lock prevents a double run (section 22).

### Per-Node State

Caches, counters and in-memory maps are per node. Plan for it:

- Cache invalidation: fire a `MultiUserEvent` after a change. Each node removes its entry (section 27).
- REST rate limit: the counters of `RequestRateLimiterImpl` are per node. The effective limit is the configured value times the number of nodes (section 37).
- AI pools: the effective parallelism against a provider is the pool size times the number of nodes (`AiTaskExecutorService`).
- Mappers: session-bound mappers live on the node of the session. `Serializable` mappers are persisted in `o_mapper`, so any node can serve them (section 12).

---

## 29. Logging Infrastructure

Two distinct layers: **technical logging** (log files) and **user activity logging** (database audit trail).

### Technical Logging (Log4j2)

```java
private static final Logger log = Tracing.createLoggerFor(MyClass.class);

log.info("Course published: {}", courseId);
log.warn("Retry failed for resource: {}", resourceKey);
log.error("Unexpected error in service", exception);
```

`Tracing.createLoggerFor()` returns a Log4j2 `Logger`. MDC entries (user identity, IP, session ID) are set via `ThreadLocalUserActivityLogger`. Configuration in `src/main/resources/log4j2.xml`. Audit marker: `Tracing.M_AUDIT`.

**New in 21.1.0:** The marker `Tracing.M_REST` tags the REST access log. `Tracing.setRequest(method, uri)` and `Tracing.setAuthProvider(provider)` put the keys `method`, `uri` and `authProvider` into the Log4j2 `ThreadContext`. A log pattern can print them.

### User Activity Logging

Structured events persisted to `o_loggingtable` for compliance/auditing:

```java
ThreadLocalUserActivityLogger.log(LearningResourceLoggingAction.LEARNING_RESOURCE_OPEN,
    getClass(), LoggingResourceable.wrap(repositoryEntry));
```

### REST API Access Log

**New in 21.1.0**

`ApiAuditLogServiceImpl` writes one line per REST request on the logger `org.olat.restapi.access` (OO-9778). The line has the format `METHOD path?query status durationms channel= auth= actor= ip= resource= ref=`. The level depends on the result:

| Level | Requests |
|-------|----------|
| `warn` | Denied and failed calls: 401, 403, 429, 5xx |
| `info` | Writes: PUT, POST, PATCH, DELETE |
| `debug` | Reads: GET, HEAD |

The line never contains the request body. Values of secret query parameters are masked. `log4j2.xml` has a commented example of the logger. The table `o_api_audit_log` stores the auditable requests (section 37).

### Best Practices for Logging

- Always use `Tracing.createLoggerFor()`, never instantiate loggers directly
- Use parameterized messages (`log.info("msg: {}", val)`) to avoid string concatenation
- Pass exceptions as last argument: `log.error(msg, exception)`
- Do not log sensitive data (passwords, tokens, personal data)
- Use `ThreadLocalUserActivityLogger` for auditable business actions
- Use `log.isDebugEnabled()` guards only when the construction of the message is expensive

---

## 30. Core Utility Classes

OpenOlat provides widely-used utility classes in `org.olat.core.util`:

### Formatter
Locale-aware formatting via `Formatter.getInstance(locale)`:
- `formatDate()`, `formatDateAndTime()`, `formatDateLong()` — date formatting
- `formatBytes(1536000)` → "1.5 MB"
- `truncate("long text", 20)`, `round(3.14, 2)`
- Static: `Formatter.formatDatetime()` (ISO 8601), `formatDatetimeFilesystemSave()`

### StringHelper
Most-used utility — static methods for validation and escaping:
- `containsNonWhitespace(str)` — null-safe "not blank"
- `escapeHtml()`, `escapeJavaScript()`, `xssScan()` — security escaping
- `isHtml()`, `isLong()` — type detection
- `formatAsCSVString(list)`, `blankIfNull()`

### FileUtils
File I/O and filename handling:
- `copyFileToDir()`, `deleteDirsAndFiles()`, `load()`, `save()`
- `getFileSuffix()`, `validateFilename()`, `normalizeFilename()`

### Encoder
Hashing and legacy encryption with `Encoder.Algorithm`: `md5` variants, `sha1`, `sha256`, `sha512`, `pbkdf2`, `argon2id`, `argon2id_owasp` (password hashing) and `aes` (`Encoder.encrypt()` / `decrypt()`).

**New in 21.1.0:** `AesGcmCipher` (package `core.util.crypto`) encrypts short secrets with AES-256-GCM (OO-9665). Use it for credentials that must be readable later, for example tokens stored in the database. Prefer it over `Encoder.Algorithm.aes`, which uses an unauthenticated mode and a hard-coded passphrase.

```java
SecretKey key = AesGcmCipher.keyFromBase64(configuredBase64Key);  // AesGcmCipher.generateKeyBase64() creates one
String stored = AesGcmCipher.encrypt(key, refreshToken, ownerId);  // ownerId = additional authenticated data
String clear = AesGcmCipher.decrypt(key, stored, ownerId);
```

The stored value starts with the prefix `{aesgcm1}`. A value without the prefix is rejected. The additional authenticated data binds the secret to its owner, so a value copied to another row cannot be decrypted. Reference use: `TeamsCryptoHelper`.

### DocxToMarkdownService
`DocxToMarkdownService` (package `core.util.docxToMarkdown`) converts a Word file (.docx) to Markdown with images, footnotes, numbering, math and SmartArt (OO-9414). The page editor import uses it. `DocxZipExtractor` rejects unsafe archives with a `DocxSecurityException`. Details: `package.md` in the same package.

### XStream
`XStreamHelper` creates configured XStream instances. `EnhancedXStream` registers `HibernateProxyConverter`, which writes a Hibernate proxy as its entity class, so proxy class names never appear in the XML (OO-9671, since 21.0.1).

---

## 31. AI Framework

The package `org.olat.core.commons.services.ai` connects OpenOlat to AI providers through LangChain4j. The full reference with all classes, prompts, file stores and admin controllers is `src/main/java/org/olat/core/commons/services/ai/package.md`. This section describes the architecture and the rules for developers.

### Service and SPI Layers

| Layer | Classes | Responsibility |
|-------|---------|----------------|
| Feature services | `AiMCQuestionService`, `AiImageDescriptionService`, `AiEssayGradingService`, `AiEssayGenerationService`, `EssayGenerationService`, `EssayAiCorrectionService` | Prompts (LangChain4j `@AiServices` interfaces in `service/`), structured output, validation, rate limits |
| Configuration | `AiModule` | Maps each `AiFeature` to a provider (`spiId`) and a model. Holds limits, pool sizes and user defaults. `resolveProvider(spiId)`, `getEnabledProviders()` |
| Chat SPI | `AiSPI`: `buildChatModel(modelName, maxTokens)`, `getAvailableModels()`, `createAdminController(ureq, wControl, readOnly)` | `OpenAiSPI`, `AnthropicAiSPI`, `GenericAiSpiInstance` (OpenAI-compatible endpoints, managed by `GenericAiSPI`) |
| Embedding SPI | `AiEmbeddingSPI`: `buildEmbeddingModel(modelName)`, `getAvailableEmbeddingModels()`, `getEmbeddingDimension(modelName)` | `OpenAiSPI`, `GenericAiSpiInstance`, `LocalOnnxSPI` (uploaded ONNX model, in process). Used by the taxonomy matching. |
| Logging | `AiLoggingChatModel`, `AiLoggingEmbeddingModel`, `AiUsageLogDAO` | One row per provider call in `o_ai_usage_log`: tokens, duration, status, error, usage context |
| HTTP | `LangChain4jHttpClientBuilder` | Routes the LangChain4j HTTP calls through `HttpClientService` with `AiModule.PROTECTION_PROFILE` |

The SPI knows nothing about features. A new provider implements only `AiSPI` (and `AiApiKeySPI` for the generic API key form). A new feature needs no change in any SPI.

### Calling a Feature Service

```java
if (aiMCQuestionService.isEnabled()) {
    AiUsageContext usageContext = AiUsageContext.builder()
            .usageContextType("my-feature")      // shows in the usage log
            .identity(getIdentity())
            .resource(courseEntry)               // OLATResourceable, optional
            .locale(getLocale())
            .build();
    AiMCQuestionsResponse response = aiMCQuestionService.generateMCQuestionsResponse(usageContext, text, 5);
    if (!response.isSuccess()) {
        showError("ai.error");
    }
}
```

- Always check `isEnabled()` (feature switched on and provider configured).
- Always pass an `AiUsageContext`. It links the log row to the person and the resource.
- Never call an SPI or a LangChain4j model from UI code.

### AI Task Pools

Long AI work runs as `LongRunnable` (section 22) on two dedicated queues. `AiModule` holds the sizes (`getAiTaskPoolInteractiveSize()`, `getAiTaskPoolBatchSize()`). `AiTaskExecutorService` applies them to the pools:

| Queue | Default size | Use |
|-------|--------------|-----|
| `aiInteractive` | 4 | A user waits for the result, for example `EssayAiCorrectionTask` |
| `aiBatch` | 2 | Background work, for example `QtiQuestionGenerationTask` and the image metadata of the media center |

A full interactive pool refuses new essay corrections. The pools are per node (section 28).

### User Control and Consent

**New in 21.1.0**

An AI feature that runs automatically, without an explicit request of the person, is user controlled (OO-9784, AI Act). `AiFeature.isUserControlled()` is true for `EssayGrading` and `ImageDescriptionGenerator`.

```
feature switched on in AiModule?  --no-->  never runs
        | yes
person's choice (AiUserPreferenceService, GUI preferences)
        ON  --> runs
        OFF --> never runs
        DEFAULT --> system default AiModule.isUserDefaultOn(feature)
```

- Gate every automatic call with `AiUserPreferenceService.isActive(prefs, feature)` at the point where the call starts.
- `AiCorrectionConsentController` asks the person before the first AI essay correction when no choice exists. `EssayAiCorrectionService.submit()` checks again and returns `null` when the person switched it off.
- Explicit requests ("Generate with AI" buttons) are not gated.
- `AiUserSettingsController` shows the choices in the user settings, segment "AI settings".

### Provider Configuration and Security

- Providers and feature mapping are configured in the administration (`AiAdminController`). Values persist through `AiModule` and the SPI modules (section 20).
- **Changed in 21.1.0:** A person without administrator rights can open the AI administration read-only (`createAdminController(..., readOnly)`).
- Content from users goes through `AiContentHardener` before it reaches a prompt. Output for learners is sanitised with the HTML filter (section 35).
- `AiPromptRules.OUTPUT_STYLE_RULES` is appended to every generative system prompt.

---

## 32. Identity, Security & Organisations

OpenOlat's security model is built around three core concepts: **Identity** (who you are), **Roles** (what you can do), and **Organisation** (where you belong). Defined in `org.olat.basesecurity` and `org.olat.core.id`.

### Identity

`Identity` is the central authentication entity — a unique account used for access control, group membership, and audit trails.

- `getKey()` — unique database ID (Long)
- `getName()` — login username (unique, immutable)
- `getUser()` — link to the `User` profile entity (one-to-one, see section 33)
- `getStatus()`: lifecycle state as `Integer` constants of `Identity`: `STATUS_PERMANENT` (1), `STATUS_ACTIV` (2), `STATUS_LOGIN_DENIED` (101), `STATUS_PENDING` (102), `STATUS_INACTIVE` (103), `STATUS_DELETED` (199). Values from `STATUS_VISIBLE_LIMIT` (100) up are invisible in user searches.
- `getExternalId()` — external system identifier (LDAP DN, Shibboleth ID, etc.)
- `getLastLogin()` — timestamp of last authentication

### Roles

| Enum | Scope | Values |
|------|-------|--------|
| `OrganisationRoles` | System-wide / per org | `sysadmin`, `administrator`, `usermanager`, `rolesmanager`, `learnresourcemanager`, `lecturemanager`, `groupmanager`, `poolmanager`, `curriculummanager`, `qualitymanager`, `projectmanager`, `linemanager`, `educationmanager`, `selectusmanager`, `principal`, `author`, `user`, `invitee`, `guest` |
| `GroupRoles` | Per resource / group / course | `owner`, `coach`, `participant`, `invitee`, `waiting` |
| `CurriculumRoles` | Per curriculum / curriculum element | `curriculummanager`, `curriculumowner`, `curriculumelementowner`, `owner`, `mastercoach`, `coach`, `participant` |
| `ProjectRole` | Per project | `owner`, `leader`, `projectOffice`, `participant`, `supplier`, `client`, `steeringCommitee`, `invitee` |
| `ContentRoles` | Per content editor element (portfolio, form) | `owner`, `coach`, `reviewer`, `invitee`, `readInvitee` |

> **Course roles:** Courses use `GroupRoles` for their membership model. A course has an internal `Group` where `owner` = course owner, `coach` = course coach/tutor, and `participant` = course participant. The same `GroupRoles` enum is also used for business groups, learning resource memberships, and curriculum element memberships.

The `Roles` object is an immutable container in the user session with `RolesByOrganisation` entries:

```java
Roles roles = ureq.getUserSession().getRoles();
boolean isAdmin = roles.isAdministrator();
boolean hasRole = roles.hasRole(org, OrganisationRoles.usermanager);
List<OrganisationRef> orgs = roles.getOrganisationsWithRole(OrganisationRoles.author);
```

### Organisations

Organisations form a **hierarchical tree** (university → faculty → department). Each has:
- `getParent()` — parent organisation (null for root)
- `getMaterializedPathKeys()` — slash-delimited ancestor keys (e.g., `/1/42/103/`) for efficient subtree SQL queries
- An internal `Group` entity holding membership relations

Role assignments are scoped to an organisation. A user can be *administrator* in one org and just *author* in another.

### Group Membership & Inheritance

All memberships stored as `GroupMembership` (Identity + Group + role). Inheritance modes:
- `none` — valid only for this organisation
- `root` — directly assigned, inherits to children
- `inherited` — propagated from a parent

### Grant Mechanism

`Grant` provides fine-grained permissions on specific `OLATResource` instances, linking a group role to a permission string on a resource. `GroupDAO` manages them: `addGrant(group, role, permission, resource)`, `getGrants(...)` and `hasGrant(identity, permission, resource, role)`.

### BaseSecurity Service

Primary interface for identity management:
- `findIdentityByLogin(login)`, `loadIdentityByKey(key)`
- `createAndPersistIdentityAndUser(...)` — create new account
- `getRoles(identity)` — get immutable `Roles` object
- `isIdentityLoginAllowed(identity, provider)`, `isIdentityVisible(identity)`: status checks
- `getAuthentications(identity)` — list auth providers (OLAT, LDAP, OAuth, etc.)

---

## 33. User Properties & Profile

While `Identity` handles authentication, the `User` entity stores profile data: name, email, institutional affiliation.

### Identity vs. User

One-to-one relationship. Identity owns the reference: `identity.getUser()`. The `UserImpl` entity maps 78 profile columns (`u_*`) plus a generic `userProperties` map.

**New in 21.1.0:** The profile field `customerNumber` (column `u_customernumber`, handler `CustomerNumberPropertyHandler`). `OLATUpgrade_21_1_0` adds it to the usage contexts of customized configurations.

### UserPropertyHandler

User properties use a **dynamic handler system**. Each property (first name, email, phone, etc.) is a `UserPropertyHandler` implementation providing:
- `getUserProperty(user, locale)` / `setUserProperty(user, value)` — get/set values
- `addFormItem(locale, user, key, isAdmin, container)` — create a form element for editing
- `updateUserFromFormItem(user, formItem)` — write form value back
- `isValid(user, formItem, properties)` — validate
- `getColumnDescriptor(colIndex, action, locale)` — table column for this property

### Context-Based Visibility

`UserPropertyUsageContext` controls which properties appear in which UI context. Each context defines which properties are visible, mandatory, admin-only, or read-only:

```java
List<UserPropertyHandler> handlers = propConfig.getUserPropertyHandlersFor(
    "org.olat.user.ProfileFormController", isAdmin);
for (UserPropertyHandler handler : handlers) {
    handler.addFormItem(getLocale(), identity.getUser(), usageIdentifier, isAdmin, formLayout);
}
```

### User Properties in Tables

Handlers also create `FlexiColumnModel` instances, so table columns for user properties are dynamically constructed based on context configuration.

### UserManager

Primary service for profile operations with display name caching:
- `getUserDisplayName(identity)` — formatted display name
- `getUserDisplayNamesByKey(identityKeys)` — batch lookup returning `Map<Long, String>`

---

## 34. Security Architecture

This section is the map of the security mechanisms. Each row points to the section with the details.

### Request Path

| Stage | Mechanism | Detail |
|-------|-----------|--------|
| Servlet filters | `SameSiteCookieFilter`: `SameSite` attribute of the session cookie (`base.security.cookie.samesite`, default `Lax`). A content-delivery session gets `SameSite=None; Partitioned`. | section 12 |
| | `HeadersFilter`: HSTS, `X-Content-Type-Options`, `X-Frame-Options`, CSP | section 35 |
| | `RestApiLoginFilter`: REST authentication, rate limiting, audit | section 37 |
| Dispatch | CSRF token of every `Form`; timestamps in the URL reject stale requests | sections 3, 35 |
| Authorization | `Roles` per organisation, security callbacks, `GroupDAO.hasGrant()` | section 32 |
| Rendering | `$r.escapeHtml()`, HTML sanitizer `OWASPAntiSamyXSSFilter` | sections 7, 35 |

### Content Isolation

User content that may run scripts (HTML pages, SCORM, iframe content) is delivered from a second domain (`server.content.domainname`) through sandboxed mappers. The content session has no REST access and no access to the main session cookie. Details: section 12 "Sandboxed Mappers & Content Domain".

### Content Security Policy

`CSPBuilder` builds the policy from `CSPModule` and from all Spring beans that implement `CSPDirectiveProvider`. A module that embeds an external service adds its URLs through such a provider (for example `ContentDeliveryDirectiveProvider` for the content domain). The provider methods cover `script-src`, `style-src`, `img-src`, `font-src`, `connect-src`, `frame-src`, `frame-ancestors`, `media-src`, `form-action`, `object-src` and `worker-src`. `CSPModule` can switch the policy to report-only.

### Outbound Requests

`HttpClientService` is the only HTTP client. The `ProtectionProfile` decides whether the SSRF filter applies: `USER_PROVIDED` for URLs of users, `CONFIGURED` for URLs of administrators. Details: section 36 "Protection Profiles".

### Secrets and Audit

| Topic | Mechanism | Detail |
|-------|-----------|--------|
| Passwords | `Encoder.Algorithm.argon2id` / `pbkdf2` hashes | section 30 |
| Stored tokens | `AesGcmCipher` (**New in 21.1.0**) | section 30 |
| Module secrets | `setSecretStringProperty()`, secured module files | section 20 |
| Admin changes | `setStringProperty()` logs with `Tracing.M_AUDIT` | sections 29, 20 |
| User actions | `ThreadLocalUserActivityLogger`, `o_loggingtable` | section 29 |
| REST calls | `o_api_audit_log`, logger `org.olat.restapi.access`, masked secrets (**New in 21.1.0**) | sections 29, 37 |
| AI calls | `o_ai_usage_log` | section 31 |

---

## 35. XSS & Security Infrastructure

OpenOlat employs multiple layers of defense against XSS and injection attacks. Section 34 is the map of all security mechanisms; this section holds the details of XSS, CSRF and headers.

### OWASP HTML Sanitizer

User-generated HTML (rich-text editors, imported pages) is sanitized through `OWASPAntiSamyXSSFilter` using a whitelist policy (`OpenOLATPolicy.java`). The class keeps its historical name, but the implementation uses the OWASP Java HTML Sanitizer (`org.owasp.html.HtmlPolicyBuilder`), not AntiSamy. It strips `<script>`, event handlers, dangerous URI schemes.

```java
OWASPAntiSamyXSSFilter filter = new OWASPAntiSamyXSSFilter();
String safeHtml = filter.filter(userInput);
// Applied automatically to RichTextElement, imported HTML, REST API content
```

### Output Escaping

- `$r.escapeHtml($text)` / `StringHelper.escapeHtml()` — HTML entity encoding
- `$r.escapeJavaScript($text)` — safe JS string embedding
- `StringHelper.xssScan()` — quick scan for dangerous patterns (validation, not sanitization)

**Important:** `$r.render()` is safe (components handle escaping), but `contextPut()` variables rendered via `$myVar` are **not auto-escaped** — use `$r.escapeHtml($myVar)` for user text.

### CSRF Protection

`Form` generates a unique CSRF token per form instance as a hidden field. `Form.validate()` checks the token before processing.

### Security Headers

`HeadersFilter` adds to every response (each header can be switched in `CSPModule`):
- `X-Frame-Options: SAMEORIGIN` — prevents clickjacking
- `X-Content-Type-Options: nosniff`
- **Content-Security-Policy** (or `Content-Security-Policy-Report-Only`): built by `CSPBuilder` from `CSPModule` and all `CSPDirectiveProvider` beans
- **Strict-Transport-Security** — enforces HTTPS

A response of a content-delivery session gets no `X-Frame-Options`. Its CSP gets `frame-ancestors` with the main server URI instead (section 12). `CSPDirectiveProvider` has default methods for `style-src`, `object-src` and `worker-src` in addition to the older source lists.

### SSRF Protection

**New in 21.1.0**

An outbound request to a URL that a user entered can reach internal hosts (server-side request forgery). `HttpClientService` blocks such requests with the protection profile `ProtectionProfile.USER_PROVIDED` (section 36).

---

## 36. HTTP Client Service

All outbound HTTP requests in OpenOlat **must** go through the centralized `HttpClientService` (`org.olat.core.util.httpclient.HttpClientService`). Do not use the native Java HTTP client (`java.net.http.HttpClient`), any other third-party HTTP client library, or instantiate Apache `HttpClient` directly.

**Changed in 21.1.0:** Every factory method takes a `ProtectionProfile` as last parameter (OO-9310). The methods without a profile are removed. See "Protection Profiles" below and the overview in section 34.

### Why

The `HttpClientService` is the single point of configuration for all outbound HTTP traffic. It ensures that:

- **Proxy settings** are applied consistently (proxy URL, port, credentials, exclusion list) — configured via `http.proxy.*` properties
- **Timeouts** are standardized (connect, request, socket) — configured via `http.connect.*` properties
- **Credentials** for basic authentication are handled uniformly
- **Database connections** are freed before making outbound calls (`dbInstance.commit()` is called internally) to avoid holding a DB connection while waiting for an external service
- **SSRF protection** is applied to URLs that users enter (profile `USER_PROVIDED`)

### Configuration Properties

| Property | Default | Description |
|----------|---------|-------------|
| `http.connect.timeout` | 30000 | Connection timeout in ms |
| `http.connect.request.timeout` | 30000 | Connection request timeout in ms |
| `http.connect.socket.timeout` | 30000 | Socket timeout in ms |
| `http.proxy.url` | — | Proxy host |
| `http.proxy.port` | 8080 | Proxy port |
| `http.proxy.exclusion` | — | Comma-separated list of hosts to bypass proxy |
| `http.proxy.user` / `http.proxy.pwd` | — | Proxy authentication credentials |
| `http.ssrf.protection.enabled` | true | **New in 21.1.0:** SSRF filter for the profile `USER_PROVIDED`. Switchable in the administration (`HttpClientModule`). |
| `http.ssrf.allowed.addresses` | (empty) | **New in 21.1.0:** Comma-separated IP addresses or CIDR blocks that are allowed although not public, e.g. `10.0.0.0/8` |
| `http.ssrf.allowed.hosts` | (empty) | **New in 21.1.0:** Comma-separated host names that are allowed although not public |

### Usage

In Spring-managed beans, inject the service:

```java
@Autowired
private HttpClientService httpClientService;
```

In controllers or non-Spring-managed classes:

```java
HttpClientService httpClientService = CoreSpringFactory.getImpl(HttpClientService.class);
```

Create a client and execute a request:

```java
// URL from the configuration of an administrator
try (CloseableHttpClient httpClient = httpClientService.createHttpClient(ProtectionProfile.CONFIGURED)) {
    HttpGet request = new HttpGet("https://api.example.com/data");
    try (CloseableHttpResponse response = httpClient.execute(request)) {
        // handle response
    }
}

// URL entered by a user (feed, calendar subscription, external page)
try (CloseableHttpClient httpClient = httpClientService.createHttpClient(ProtectionProfile.USER_PROVIDED)) {
    // a request to a loopback, private or link-local address fails with FilteredHostException
}

// With basic authentication
try (CloseableHttpClient httpClient = httpClientService.createThreadSafeHttpClient(
        "api.example.com", 443, "user", "password", true, ProtectionProfile.CONFIGURED)) {
    // use client for multiple requests
}

// Custom builder for additional configuration
HttpClientBuilder builder = httpClientService.createHttpClientBuilder(ProtectionProfile.CONFIGURED);
builder.setDefaultHeaders(List.of(new BasicHeader("Authorization", "Bearer " + token)));
try (CloseableHttpClient httpClient = builder.build()) {
    // use client
}
```

### Available Methods

| Method | Description |
|--------|-------------|
| `createHttpClient(profile)` | Simple client with default config (timeouts + proxy) |
| `createHttpClientBuilder(profile)` | Builder for customization before building |
| `createHttpClientBuilder(host, port, user, password, profile)` | Builder with basic auth credentials |
| `createThreadSafeHttpClient(redirect, profile)` | Pooled client for concurrent use |
| `createThreadSafeHttpClient(host, port, user, password, redirect, profile)` | Pooled client with basic auth |

### Protection Profiles

**New in 21.1.0**

| Profile | Use for | Effect |
|---------|---------|--------|
| `ProtectionProfile.USER_PROVIDED` | A URL that a user entered: feeds (`RomeFeedFetcher`), calendar subscriptions (`CalendarManager`), the course element "External page" (`TUConfigForm`, `IframeTunnelController`), LTI 1.1 (`LTIManager`), video and live stream URLs, images in a Markdown import, Open Badges verification | With `http.ssrf.protection.enabled=true`, only public internet addresses are reachable. The allow lists are exempt. |
| `ProtectionProfile.CONFIGURED` | A URL that an administrator configured: AI providers (`AiModule.PROTECTION_PROFILE`), BigBlueButton, Opencast, OnlyOffice, Gotenberg, OAuth login, Zoom | No address filter |

The filter has two stages (package `core.util.httpclient.filter`):

1. `FilteringDnsResolver` resolves the host and rejects loopback, link-local, private and special-purpose addresses (`InetAddressFilter.externalAddresses()`). The configured proxy host is exempt.
2. `FilteringHostRequestInterceptor` checks literal IP addresses in the request. It covers the cases the DNS resolver does not see, for example a request through a forward proxy.

A rejected request throws `FilteredHostException`. The DNS filter works only as long as no connection manager is set on the builder. Do not call `setConnectionManager()` on a builder with the profile `USER_PROVIDED`.

Choose the profile by the origin of the URL, not by the feature. When in doubt, use `USER_PROVIDED`. On an instance upgraded to 21.1.0, `OLATUpgrade_21_1_0` switches the protection off, unless the key is set explicitly (section 23). An administrator switches it on in the administration of the HTTP client.

---

## 37. Common Patterns & Best Practices

### Choosing the Right Controller Type

| Scenario | Controller | Why |
|----------|------------|-----|
| Navigation page with links and child views | `BasicController` | No form elements needed |
| Data entry form with validation | `FormBasicController` | Structured form lifecycle |
| Settings page with save/cancel | `FormBasicController` | Built-in submit/cancel |
| Data table with search and filters | `FormBasicController` | FlexiTable is a form element |
| Dashboard composing multiple widgets | `BasicController` | Orchestrates children |

### Creating a New View (Checklist)

1. **Create the controller** — extend `BasicController` or `FormBasicController`
2. **Create the Velocity template** — in `_content/` next to the controller package
3. **Add i18n keys** — in `_i18n/LocalStrings_en.properties` (and other locales)
4. **Wire it up** — create the child in the parent, call `listenTo()`, embed via `$r.render()`

### Key Lifecycle Rules

- **Always `listenTo()`** child controllers — ensures automatic disposal and event routing
- **Always `removeAsListenerAndDispose()`** before replacing a controller reference
- **Never store `UserRequest`** — it is valid only for the current request cycle
- **Use `DBFactory.getInstance().commitAndCloseSession()`** in background tasks
- **Use `CoreSpringFactory.getImpl()`** for service access in non-Spring-managed classes
- **Use `Tracing.createLoggerFor()`** for all logging — never instantiate loggers directly. Use parameterized messages. See section 29
- **Use `ThreadLocalUserActivityLogger.log()`** for business-relevant actions that need an audit trail

### WindowControl — Modals & Overlays

```java
// Push a modal dialog
getWindowControl().pushAsModalDialog(editCtrl.getInitialComponent());

// Remove when done
getWindowControl().removeModalDialog(editCtrl.getInitialComponent());

// Show a callout popup near an element
getWindowControl().pushAsCallout(helpCtrl.getInitialComponent(), targetDomId, settings);

// Full-screen takeover
getWindowControl().pushFullScreen(editorCtrl, "o_editor_body");
```

### Module Package Structure

Well-structured modules (e.g., `certificationprogram`, `grading`, `topicbroker`, `curriculum`) follow a consistent layout:

```
org.olat.modules.mymodule/
├── MyModule.java              // AbstractSpringModule — config properties
├── MyService.java             // public service interface
├── manager/
│   └── MyServiceImpl.java     // @Service implementation with DAO injection
│   └── MyDAO.java             // DB access, package-private
├── model/
│   └── MyEntity.java          // JPA @Entity
├── ui/
│   ├── MyListController.java  // FlexiTable-based list
│   ├── MyEditController.java  // FormBasicController for editing
│   ├── _content/              // Velocity templates
│   └── _i18n/                 // LocalStrings_XX.properties
├── restapi/
│   └── MyWebService.java      // JAX-RS endpoints
└── _spring/
    └── mymoduleContext.xml     // optional Spring XML config
```

Key conventions from best-practice modules:
- **Service interface + impl separation** — public interface in package root, `@Service` impl in `manager/`. DAOs are package-private.
- **Module class for feature toggles** — `AbstractSpringModule` subclass manages `isEnabled()` and persisted config.
- **FlexiTable for all lists** — data source pattern with server-side sorting/filtering.
- **Breadcrumb navigation** — `BreadcrumbedStackedPanel` for multi-level drill-down views.
- **Toolbar actions** — `toolbarPanel.addTool()` for create/export/import at top of lists.
- **Security callbacks** — pass permission objects to controllers rather than checking roles inline.

### REST API Conventions

Module REST resources live in the `restapi/` sub-package of their module. Each resource class carries both `@Component` (Spring bean) and `@Path` (JAX-RS mount point), and its package must be listed in `src/main/java/org/olat/restapi/_spring/restApiContext.xml` for OpenAPI scanning. The `@Tag(name = "...")` annotation determines the section name in the Swagger UI. Reference pattern: `org.olat.modules.bigbluebutton.restapi`.

Auth convention: call `getRoles(httpRequest)` early and delegate to a local `isAuthorised(Roles)` helper before any business logic. For non-admin roles (`author`, `lecturemanager`, `learnresourcemanager`) results are filtered at the DAO layer via a `SearchParameters.identity` field — the REST resource passes the caller's identity into the search parameters and never re-implements the predicate itself. Fields that must be hidden from non-admins (e.g. `externalId`, internal notes) are masked inside the VO's `valueOf(Entity, Roles)` factory rather than in the endpoint method.

Notable module REST surfaces:

| Package | Root path(s) | Purpose |
|---------|-------------|---------|
| `org.olat.modules.roommanagement.restapi` | `GET /rm/buildings`, `GET /rm/rooms` | Read-only search and detail for buildings and rooms; results org-scoped for non-admins. Room booking sub-resource added to `LectureBlockWebService`: `GET / PUT / DELETE .../lectureblocks/{k}/room`. |
| `org.olat.modules.certificationprogram.restapi` | certification programs | CRUD for certification programs (OO-9508). Listed in `restApiContext.xml`. |

A request of a content-delivery session (section 12) gets the status 403. The only exception is the course database endpoint `/restapi/repo/courses/{id}/db/...`.

Every REST request passes the same chain:

```
HTTP request
  -> RestApiLoginFilter (servlet filter, web.xml)
       authentication (API key, password, token, session, IP)
       rate limit: RequestRateLimiter.acquire() + check()  -> 429 + Retry-After
       write call: CachedBodyHttpServletRequest keeps the body
  -> CXFServlet -> JAX-RS resource (@Path)
  -> ApiAuditResponseFilter (JAX-RS ContainerResponseFilter)
       ApiAuditEntry -> ApiAuditLogService.log()   (same transaction)
       status >= 500 or DB error -> entry pending
  <- RestApiLoginFilter finally block
       pending entry -> logInNewTransaction()      (after the rollback)
```

### REST API Audit Log

**New in 21.1.0**

The REST API writes an audit trail to the table `o_api_audit_log` (OO-9778, package `org.olat.restapi.audit`). The resource classes need no code for it.

| Class | Role |
|-------|------|
| `ApiAuditResponseFilter` | JAX-RS response filter. Builds an `ApiAuditEntry` with resource class, method, path parameters and status. |
| `ApiAuditLogService` / `ApiAuditLogServiceImpl` | Decides what to store (`isAuditable(method, status)`), writes the row and the access log line (section 29). |
| `ApiAuditMasking` | Masks the values of the keys `password`, `credential`, `secret`, `clientSecret`, `token` and `sharedSecret` in JSON bodies and query strings with `***`. |
| `CachedBodyHttpServletRequest` | Keeps a copy of the request body of a write call, up to `restapi.auditlog.body.maxsize` bytes. |
| `ApiAuditLogRetentionJob` | Nightly job on the cluster singleton node. Deletes rows older than the retention. |
| `ApiAuditLogAdminController` | Table of the audit log in the REST API administration (package `org.olat.admin.restapi`). |

`ApiAuditChannel` names the source of a row: `rest` today, `mcp` for a future MCP server.

A row is stored for every PUT, POST, PATCH and DELETE, and for every request with the status 401, 403, 429 or 5xx. GET and HEAD requests are stored only with `restapi.auditlog.reads=true`.

| Property | Default | Purpose |
|----------|---------|---------|
| `restapi.auditlog.enabled` | true | Audit log on or off |
| `restapi.auditlog.reads` | false | Also store successful reads (high volume) |
| `restapi.auditlog.body` | true | Store the masked JSON body of writes |
| `restapi.auditlog.body.maxsize` | 16384 | Maximum stored body size in bytes |
| `restapi.auditlog.retention.days` | 365 | Retention; 0 keeps all rows |

A failed request loses its transaction. The response filter then marks the entry as pending. The servlet filter writes it with `logInNewTransaction()` after the rollback.

### REST API Rate Limiting

**New in 21.1.0**

`RestApiLoginFilter` limits the requests per subject (OO-9779). A request above a limit gets the status 429 at once. It is never queued.

- The subject is `rest:id:<identity key>` for an authenticated request and `rest:ip:<address>` for an unauthenticated one (open URLs and the login URL `/restapi/auth`).
- Two guards apply: parallel requests (`RequestRateLimiter.acquire()` / `release()`) and requests per fixed window of 60 seconds (`check()`).
- Every allowed request gets the headers `X-RateLimit-Limit`, `X-RateLimit-Remaining` and `X-RateLimit-Reset` (epoch seconds).
- A rejected request gets `Retry-After` (seconds) and the JSON body `{"code":429,"message":"Too many requests, retry after N seconds"}`.
- Only the first rejection per subject, guard and window goes to the audit log and the access log. A client that ignores the 429 cannot flood the logs.
- Exempt paths: `/restapi/ping`, `/restapi/i18n`, `/restapi/openmeetings`, `/restapi/drawio`, `/restapi/onlyoffice`, `/restapi/office365`, and `/restapi/system` for the IPs in `restapi.ips.system`.

| Property | Default | Purpose |
|----------|---------|---------|
| `restapi.ratelimit.enabled` | false | Rate limit on or off |
| `restapi.ratelimit.requests.per.minute` | 600 | Requests per minute of an authenticated user |
| `restapi.ratelimit.max.parallel` | 4 | Parallel requests of an authenticated user |
| `restapi.ratelimit.anonymous.requests.per.minute` | 60 | Requests per minute of a client IP without authentication |

The administration of the REST API can change the values at runtime (`RestModule`). The counters are per node: on a cluster, the effective limit is the configured value multiplied by the number of nodes.

`RequestRateLimiter` (package `core.util.ratelimit`) is a generic service. `RequestRateLimiterImpl` keeps the window counters in the Infinispan cache `RequestRateLimiter-window` and the parallel slots in a local map. `check()` returns a `RateLimitDecision` record. A caller that takes a slot with `acquire()` must call `release()` in a `finally` block.

```java
@Autowired
private RequestRateLimiter requestRateLimiter;

String key = "myfeature:id:" + identity.getKey();
if (requestRateLimiter.acquire(key, maxParallel)) {
    try {
        RateLimitDecision decision = requestRateLimiter.check(key, limitPerMinute);
        if (!decision.allowed()) {
            // reject, retry after decision.retryAfterSeconds()
        }
    } finally {
        requestRateLimiter.release(key);
    }
}
```

---

## 38. Testing Infrastructure

Multi-layered testing: unit tests, Spring integration tests, REST API tests, and Selenium browser tests.

### Unit & Integration Tests (JUnit)

| Base Class | Purpose | Infrastructure |
|------------|---------|----------------|
| Plain JUnit | Pure logic | No Spring, no DB |
| `OlatTestCase` | Spring integration | Full Spring context, DB with rollback |
| `OlatRestTestCase` | REST API | Embedded Undertow, real HTTP |

```java
// Spring integration test:
public class MyServiceTest extends OlatTestCase {
    @Autowired private MyService myService;
    @Autowired private DB dbInstance;

    @Test
    public void testCreateItem() {
        Identity author = JunitTestHelper.createAndPersistIdentityAsAuthor("test");
        MyItem item = myService.createItem(author, "Test");
        dbInstance.commitAndCloseSession();
        assertNotNull(item.getKey());
    }
}
```

### Selenium / UI Tests (Arquillian)

Browser tests use Arquillian + Selenium WebDriver with a page object pattern:

```java
public class CourseEditorPage {
    private WebDriver browser;

    public CourseEditorPage addCourseElement(CourseElementType type) {
        browser.findElement(By.cssSelector(".o_sel_course_editor_add")).click();
        return this;
    }

    public CourseEditorPage publish() {
        browser.findElement(By.cssSelector(".o_sel_publish")).click();
        OOGraphene.waitBusy(browser);  // wait for AJAX
        return this;
    }
}
```

Key conventions:
- `OOGraphene` — wait helpers: `waitBusy()`, `waitElement()`, `waitAndCloseBlueMessageWindow()`
- `JunitTestHelper` — creates test users, courses, fixtures
- CSS selectors prefixed with `o_sel_` for test automation
- Tests mirror main source tree in `src/test/java/org/olat/`

---

## 39. About

### About This Document

| Item | Value |
|------|-------|
| Document version | 2.0 |
| Covered release | OpenOlat 21.1.0 (master, `21.1-SNAPSHOT`) |
| Last updated | 2026-09-25 |
| Previous content update | 2026-05-19 (commit `519afaf02c`) |
| HTML view | `doc/openolat-architecture.html` (same sections, with diagrams) |

Changes in version 2.0:

- **New mechanisms of 21.1.0:** REST API audit log and rate limiting (section 37), REST access log (section 29), SSRF protection profiles of `HttpClientService` (sections 35, 36), user control of AI features (section 31), cached wizard step controllers (section 10), `FormSection` and `SearchElement` (section 8), `FactSheet`, `Sections`, `ComponentList` (section 5), callout accessibility rollout (section 15), `AesGcmCipher` (section 30), label icon of `FormItem` (section 8).
- **Documented 21.0.x mechanisms:** sandboxed mappers and the content domain (section 12), AI services and AI task pools (sections 21, 31), `RelativeDateElement`, `ObjectSelectionElement`, `LAYOUT_TWO_COLS` (section 8), `DocxToMarkdownService`, `HibernateProxyConverter` (section 30), the `CacheWrapper.put()` signature (section 27).
- **Corrections:** dispatcher registration (section 3), component and cell renderer names (sections 5, 9), FlexiTable sort and empty state API (section 9), form layout types (section 8), VFS helpers (section 18), Spring context count (section 19), identity status values and organisation roles (section 32), HTML sanitizer library (section 35), logger type (section 29), statistics (sections 16, 25, 33).
- **Aligned with the HTML view:** subsections that existed only in the HTML are now part of this file.
- **New sections for existing mechanisms:** Wizards (10), Module Configuration (20), Persisted Long-Running Tasks (22), Clustering (28), AI Framework (31), Security Architecture (34). Sections after 9 are renumbered; the old sections 10 to 33 are now 11 to 39.

### OpenOlat

[OpenOlat](https://www.openolat.com) is a web-based learning management system (LMS) for teaching, learning, assessment and communication. Its tagline is *"Infinite learning"*. OpenOlat provides a sophisticated modular toolkit with a wide range of didactic possibilities including course design, learning paths, eTesting and assessment, web conferencing, quality management, and extensive integration capabilities. Each installation can be individually extended, adapted to organizational needs, and integrated into existing IT infrastructures. The architecture is designed for minimal resource consumption, scalability and security.

### Developed by frentix

OpenOlat is developed and maintained by [frentix GmbH](https://frentix.com), a Swiss software company based in Zurich, Switzerland. Founded in 2006, frentix specializes in e-learning, software development, multimedia and media production. The company helps clients implement e-learning strategies effectively and offers hosting on Swiss servers, multimedia content creation, and system integration (LDAP, Shibboleth, OAuth2). frentix has been recognized multiple times as the best LMS provider in the German-speaking regions (eLearning CHECK awards).

### History

The original codebase (olat.org) was developed between 1999 and 2011 at the Multimedia & E-Learning Services (MELS) of the University of Zurich, with over 50 contributors. In 2011, the openolat.org community was initiated by frentix GmbH to continue the development as an open-source project. Today, contributors include frentix GmbH, BPS Bildungsportal Sachsen GmbH, VCRP, and the University of Innsbruck, among others.

### License

OpenOlat is licensed under the **Apache License, Version 2.0**. You may obtain a copy of the license at [apache.org/licenses/LICENSE-2.0](https://www.apache.org/licenses/LICENSE-2.0).

The OpenOlat logo is licensed under the **Creative Commons Attribution-NoDerivs 2.5** license.

OpenOlat includes numerous third-party libraries under various open-source licenses (Apache 2.0, MIT, BSD, LGPL). See the `NOTICE.TXT` file in the project root for a full list of dependencies and their licenses.
