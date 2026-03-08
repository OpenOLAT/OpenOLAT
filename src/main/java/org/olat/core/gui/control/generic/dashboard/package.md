# Dashboard Framework

**Package:** `org.olat.core.gui.control.generic.dashboard`

Developer documentation for the OpenOlat bento-grid dashboard infrastructure.

## 1. Overview

The dashboard framework provides a reusable, user-configurable widget container
rendered as a CSS Grid bento layout. Widgets can be added by any module.
When editing is enabled, users can reorder, hide, and restore widgets via
drag & drop. Widget configuration follows a three-tier cascade:
personal preferences > system defaults > all widgets.

Key features:

- Bento grid layout with configurable widget sizes (1x1 up to 4x4)
- Optional edit mode with Dragula.js drag & drop
- Three-tier configuration cascade: personal > system default > all widgets
- Per-user preferences stored via XStream in `GuiPreferences`
- System default configuration stored in the `o_property` table via `PropertyManager`
- Admin tools for managing system default configurations (system administrators only)
- Automatic title detection from controllers implementing `DashboardWidget`
- Abstract `TableWidgetController` base class for table-based widgets

## 2. Class Diagram

<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 870 440" font-family="'SF Mono','Cascadia Code','Fira Code','Consolas',monospace">
  <style>
    .box { stroke-width: 1.2; rx: 4; ry: 4; }
    .box-class    { fill: #f1f5f9; stroke: #475569; }
    .box-iface    { fill: #ecfdf5; stroke: #16a34a; }
    .box-enum     { fill: #fffbeb; stroke: #ca8a04; }
    .box-abstract { fill: #eff6ff; stroke: #2563eb; }
    .box-service  { fill: #fef3c7; stroke: #d97706; }
    .cls-title  { font-size: 13px; font-weight: 700; fill: #1e293b; }
    .cls-stereo { font-size: 10px; font-style: italic; fill: #64748b; }
    .cls-text   { font-size: 10.5px; fill: #334155; }
    .sep  { stroke: #cbd5e1; stroke-width: .8; }
    .arr  { stroke: #64748b; stroke-width: 1.2; fill: none; }
    .arr-dash { stroke-dasharray: 6 3; }
    .arr-lbl  { font-size: 10px; fill: #64748b; font-style: italic; font-family: -apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif; }
  </style>
  <defs>
    <marker id="ah" viewBox="0 0 10 7" refX="9" refY="3.5" markerWidth="8" markerHeight="7" orient="auto-start-reverse">
      <polygon points="0 0,10 3.5,0 7" fill="#64748b"/>
    </marker>
    <marker id="oh" viewBox="0 0 10 7" refX="9" refY="3.5" markerWidth="8" markerHeight="7" orient="auto-start-reverse">
      <polygon points="0 0,10 3.5,0 7" fill="none" stroke="#64748b" stroke-width="1.2"/>
    </marker>
  </defs>

  <!-- DashboardController -->
  <rect class="box box-class" x="10" y="8" width="310" height="142"/>
  <text class="cls-title" x="165" y="28" text-anchor="middle">DashboardController</text>
  <line class="sep" x1="10" y1="36" x2="320" y2="36"/>
  <text class="cls-text" x="18" y="52">- dashboardId : String</text>
  <text class="cls-text" x="18" y="67">- allWidgets : List&lt;Widget&gt;</text>
  <text class="cls-text" x="18" y="82">- enabledWidgets : List&lt;Widget&gt;</text>
  <line class="sep" x1="10" y1="92" x2="320" y2="92"/>
  <text class="cls-text" x="18" y="108">+ addWidget(name, title, ctrl, size)</text>
  <text class="cls-text" x="18" y="123">+ setDashboardCss(css)</text>
  <text class="cls-text" x="18" y="138">- applyConfiguration()</text>

  <!-- DashboardPrefs -->
  <rect class="box box-class" x="575" y="8" width="255" height="52"/>
  <text class="cls-title" x="702" y="28" text-anchor="middle">DashboardPrefs</text>
  <line class="sep" x1="575" y1="36" x2="830" y2="36"/>
  <text class="cls-text" x="583" y="52">enabledWidgets : List&lt;String&gt;</text>

  <!-- BentoBoxSize -->
  <rect class="box box-enum" x="575" y="80" width="255" height="64"/>
  <text class="cls-stereo" x="702" y="96" text-anchor="middle">&#171;enum&#187;</text>
  <text class="cls-title" x="702" y="112" text-anchor="middle">BentoBoxSize</text>
  <line class="sep" x1="575" y1="119" x2="830" y2="119"/>
  <text class="cls-text" x="583" y="135">box_{cols}_{rows} &#8594; getCss()</text>

  <!-- DashboardSystemDefaultsManager -->
  <rect class="box box-service" x="600" y="168" width="250" height="92"/>
  <text class="cls-stereo" x="725" y="184" text-anchor="middle">&#171;service&#187;</text>
  <text class="cls-title" x="725" y="200" text-anchor="middle" style="font-size:10.5px">DashboardSystemDefaultsManager</text>
  <line class="sep" x1="600" y1="207" x2="850" y2="207"/>
  <text class="cls-text" x="608" y="222">+ loadSystemDefault(id)</text>
  <text class="cls-text" x="608" y="237">+ saveSystemDefault(id, prefs)</text>
  <text class="cls-text" x="608" y="252">+ deleteSystemDefault(id)</text>

  <!-- DashboardEditController -->
  <rect class="box box-class" x="10" y="200" width="310" height="100"/>
  <text class="cls-title" x="165" y="220" text-anchor="middle">DashboardEditController</text>
  <line class="sep" x1="10" y1="228" x2="320" y2="228"/>
  <text class="cls-text" x="18" y="244">Drag &amp; drop reordering (Dragula.js)</text>
  <text class="cls-text" x="18" y="259">Add / Remove / Reorder widgets</text>
  <text class="cls-text" x="18" y="274">Admin: save / reset system defaults</text>
  <text class="cls-text" x="18" y="289">&#8594; CHANGED_EVENT | CANCELLED_EVENT</text>

  <!-- Widget (inner class) -->
  <rect class="box box-class" x="365" y="210" width="195" height="92"/>
  <text class="cls-stereo" x="462" y="225" text-anchor="middle">&#171;inner class&#187;</text>
  <text class="cls-title" x="462" y="241" text-anchor="middle">Widget</text>
  <line class="sep" x1="365" y1="248" x2="560" y2="248"/>
  <text class="cls-text" x="373" y="264">name : String</text>
  <text class="cls-text" x="373" y="279">title : String</text>
  <text class="cls-text" x="373" y="294">css : String</text>

  <!-- DashboardWidget (interface) -->
  <rect class="box box-iface" x="365" y="348" width="195" height="56"/>
  <text class="cls-stereo" x="462" y="364" text-anchor="middle">&#171;interface&#187;</text>
  <text class="cls-title" x="462" y="380" text-anchor="middle">DashboardWidget</text>
  <line class="sep" x1="365" y1="387" x2="560" y2="387"/>
  <text class="cls-text" x="373" y="400">+ getWidgetTitle() : String</text>

  <!-- TableWidgetController (abstract) -->
  <rect class="box box-abstract" x="600" y="320" width="250" height="108"/>
  <text class="cls-stereo" x="725" y="336" text-anchor="middle">&#171;abstract&#187;</text>
  <text class="cls-title" x="725" y="352" text-anchor="middle">TableWidgetController</text>
  <line class="sep" x1="600" y1="359" x2="850" y2="359"/>
  <text class="cls-text" x="608" y="374"># getTitle() : String</text>
  <text class="cls-text" x="608" y="389"># createTable(cont)</text>
  <text class="cls-text" x="608" y="404"># createShowAll(cont)</text>
  <text class="cls-text" x="608" y="419">+ getWidgetTitle() : String</text>

  <!-- Arrows -->
  <line class="arr arr-dash" x1="320" y1="30" x2="575" y2="30" marker-end="url(#ah)"/>
  <text class="arr-lbl" x="448" y="23" text-anchor="middle">reads / writes</text>

  <line class="arr arr-dash" x1="320" y1="112" x2="575" y2="112" marker-end="url(#ah)"/>
  <text class="arr-lbl" x="448" y="105" text-anchor="middle">uses</text>

  <line class="arr arr-dash" x1="320" y1="140" x2="600" y2="210" marker-end="url(#ah)"/>
  <text class="arr-lbl" x="440" y="162">autowires</text>

  <line class="arr arr-dash" x1="100" y1="150" x2="100" y2="200" marker-end="url(#ah)"/>
  <text class="arr-lbl" x="112" y="180">creates</text>

  <polygon points="320,128 312,121 304,128 312,135" fill="#64748b"/>
  <line class="arr" x1="320" y1="128" x2="462" y2="210" marker-end="url(#ah)"/>
  <text class="arr-lbl" x="385" y="158">contains</text>

  <line class="arr" x1="320" y1="260" x2="365" y2="260" marker-end="url(#ah)"/>
  <text class="arr-lbl" x="342" y="253" text-anchor="middle">uses</text>

  <path class="arr arr-dash" d="M 250,300 L 250,310 L 725,310 L 725,260" marker-end="url(#ah)"/>
  <text class="arr-lbl" x="480" y="306">autowires</text>

  <line class="arr arr-dash" x1="725" y1="168" x2="710" y2="60" marker-end="url(#ah)"/>
  <text class="arr-lbl" x="733" y="118">reads / writes</text>

  <line class="arr arr-dash" x1="600" y1="376" x2="560" y2="376" marker-end="url(#oh)"/>
  <text class="arr-lbl" x="580" y="368" text-anchor="middle">implements</text>
</svg>

### Roles of each class

| Class | Responsibility |
|-------|---------------|
| `DashboardController` | Main controller. Renders the bento grid, manages widget registration, loads both personal and system default preferences, applies the configuration cascade, and opens edit mode. |
| `DashboardEditController` | Edit-mode controller. Drag & drop reordering via Dragula.js, add/remove actions, save/cancel/reset. For system admins, also shows an ellipsis menu with "Save as system default" and "Reset system default". Fires `CHANGED_EVENT` or `CANCELLED_EVENT`. |
| `DashboardSystemDefaultsManager` | Spring `@Service` managing system-wide default configurations. Stores `DashboardPrefs` as XStream XML in the `o_property` table via `PropertyManager` (null identity/group/resource for system-level storage). |
| `Widget` | Static inner class of `DashboardController`. View model holding the widget's `name`, `title`, and `css` class. |
| `DashboardPrefs` | POJO used for both personal preferences (`GuiPreferences`) and system defaults (`o_property`). Contains the ordered list of enabled widget names. Serialized via XStream using the fully qualified class name. |
| `BentoBoxSize` | Enum defining widget sizes as CSS classes (e.g. `box_4_1` = 4 columns, 1 row). |
| `DashboardWidget` | Optional interface. Controllers implementing it provide a title via `getWidgetTitle()`. |
| `TableWidgetController` | Abstract base class for widgets displaying a FlexiTable with indicators and "Show all" link. Implements `DashboardWidget`. |
| `DashboardUIFactory` | Static factory for common widget UI elements ("Show all" and "Details" links). |

## 3. Creating a Dashboard

### Without edit support

Use the no-arg constructor. All widgets are always shown in registration order.
No edit button is rendered.

```java
DashboardController dashboard = new DashboardController(ureq, wControl);
listenTo(dashboard);
putInitialPanel(dashboard.getInitialComponent());
```

### With edit support

Pass a stable, unique `dashboardId` string (typically the calling controller's
fully qualified class name). An "Edit dashboard" button is shown for non-guest users.

```java
DashboardController dashboard = new DashboardController(ureq, wControl, getClass().getName());
listenTo(dashboard);
putInitialPanel(dashboard.getInitialComponent());
```

> **Note:** The `dashboardId` is used as the key for both `GuiPreferences` (personal)
> and `o_property` (system default).
> Use a stable string that does not change across versions. Guest users never see the edit button.

## 4. Adding Widgets

After creating the dashboard, add widgets using `addWidget()`.
Each call registers the widget and immediately updates the rendered view.

```java
CourseWidgetController courseCtrl = new CourseWidgetController(ureq, wControl);
listenTo(courseCtrl);
dashboard.addWidget("courses", translate("widget.courses"), courseCtrl, BentoBoxSize.box_4_1);
```

### Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `name` | `String` | Unique identifier within this dashboard. Used as the component name in Velocity and stored in user preferences. Must be stable across versions. |
| `title` | `String` | Human-readable title shown in edit mode. Pass `null` to auto-detect from a `DashboardWidget` controller. |
| `ctrl` | `Controller` | The widget controller. The caller must call `listenTo(ctrl)` before adding. |
| `size` | `BentoBoxSize` | Bento grid size (columns x rows). See the reference table below. |

> **Important:** Widget names must be stable. Renaming a widget name will cause existing
> user preferences to lose track of that widget.

## 5. Writing a New Widget

Any `Controller` can serve as a widget. The dashboard framework has no
requirements on the widget controller beyond having an initial component.

### 5a. Simple widget (any Controller)

Create a standard `BasicController` or `FormBasicController`.
The dashboard renders whatever `getInitialComponent()` returns.

```java
public class MyStatsWidgetController extends BasicController {

    public MyStatsWidgetController(UserRequest ureq, WindowControl wControl) {
        super(ureq, wControl);
        VelocityContainer mainVC = createVelocityContainer("my_stats");
        // ... populate the template ...
        putInitialPanel(mainVC);
    }

    @Override
    protected void event(UserRequest ureq, Component source, Event event) {
        // handle events
    }
}
```

Register it with the dashboard:

```java
MyStatsWidgetController statsCtrl = new MyStatsWidgetController(ureq, wControl);
listenTo(statsCtrl);
dashboard.addWidget("myStats", translate("widget.stats"), statsCtrl, BentoBoxSize.box_2_2);
```

### 5b. Implementing DashboardWidget for automatic title detection

If the widget controller implements `DashboardWidget`, you can pass
`null` as the title and it will be auto-detected via `getWidgetTitle()`.

```java
public class MyStatsWidgetController extends BasicController implements DashboardWidget {

    @Override
    public String getWidgetTitle() {
        return getTranslator().translate("widget.stats");
    }

    // ... rest of the controller ...
}
```

```java
// title is auto-detected from getWidgetTitle()
dashboard.addWidget("myStats", null, statsCtrl, BentoBoxSize.box_2_2);
```

### 5c. Table widget (extending TableWidgetController)

For widgets that display a FlexiTable with key-figure indicators and a "Show all" link,
extend `TableWidgetController`. This abstract base class provides a standard
layout and already implements `DashboardWidget`.

```java
public class CourseWidgetController extends TableWidgetController {

    public CourseWidgetController(UserRequest ureq, WindowControl wControl) {
        super(ureq, wControl);
        initForm(ureq);
    }

    @Override
    protected String getTitle() {
        return translate("widget.courses");
    }

    @Override
    protected String createIndicators(FormLayoutContainer widgetCont) {
        // Create and return indicator components (key figures)
        // Return the component name, or null if no indicators
        return null;
    }

    @Override
    protected String createTable(FormLayoutContainer widgetCont) {
        // Create a FlexiTableElement, add it to widgetCont, return its name
        FlexiTableElement tableEl = uifactory.addTableElement(getWindowControl(),
                "courses", dataModel, getTranslator(), widgetCont);
        tableEl.setCssDelegate(MaxHeightScrollableDelegate.DELEGATE);
        return tableEl.getName();
    }

    @Override
    protected String createShowAll(FormLayoutContainer widgetCont) {
        FormLink showAll = DashboardUIFactory.createShowAllLink(widgetCont);
        return showAll.getName();
    }

    @Override
    protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
        // Handle row clicks (CMD_ROW_CLICKED), "show all" link, etc.
        super.formInnerEvent(ureq, source, event);
    }

    public void reload() {
        // Refresh the table data model
    }
}
```

### TableWidgetController features

- Standard layout with title, indicators, table, empty state, and "Show all" footer
- Built-in per-widget preferences (key figures, visible columns, row count) via `TableWidgetConfigProvider`
- `wrapCellLink(renderer)` helper to make entire table rows clickable
- `MaxHeightScrollableDelegate` for scrollable table with max height

## 6. Edit Mode and Preferences

### How edit mode works

1. User clicks "Edit dashboard" -> `DashboardController.doEdit()`
2. The main panel swaps from the dashboard view to the `DashboardEditController` component
3. Edit view shows active widgets (reorderable via drag & drop) and available widgets (with "Add" buttons)
4. User clicks **Save** -> preferences are written to `GuiPreferences` -> `CHANGED_EVENT` is fired
5. User clicks **Reset** -> personal preferences are deleted (reverts to system default or all widgets) -> `CHANGED_EVENT` is fired
6. User clicks **Cancel** -> `CANCELLED_EVENT` is fired, no changes saved
7. `DashboardController` receives the event, swaps the panel back, and reloads the configuration

### Configuration cascade

The dashboard uses a three-tier configuration cascade to determine which widgets are shown and in what order:

1. **Personal preferences** -- Per-user configuration stored in `GuiPreferences`. If present, this takes priority.
2. **System default** -- System-wide default configuration stored in the `o_property` table via `DashboardSystemDefaultsManager`. Used when no personal preferences exist.
3. **All widgets** -- Fallback: all registered widgets are shown in registration order. Used when neither personal nor system default configuration exists.

The cascade is applied in `DashboardController.applyConfiguration()` and `DashboardController.doEdit()`. Both methods check personal preferences first, fall back to system defaults, and finally show all widgets if neither is configured.

### Personal preference storage

User preferences are stored as `DashboardPrefs` objects in `GuiPreferences`,
serialized via XStream using the fully qualified class name. Example XML:

```xml
<org.olat.core.gui.control.generic.dashboard.DashboardPrefs>
  <enabledWidgets>
    <string>courses</string>
    <string>lectureBlocks</string>
  </enabledWidgets>
</org.olat.core.gui.control.generic.dashboard.DashboardPrefs>
```

### System default storage

System defaults are managed by `DashboardSystemDefaultsManager`, a Spring `@Service`.
It stores `DashboardPrefs` as XStream-serialized XML in the `o_property` table using
`PropertyManager` with `null` identity, group, and resource (system-level storage).
The property category is `dashboard.system.default` and the property name is the `dashboardId`.

Both `PreferencesImpl` (for personal preferences) and `DashboardSystemDefaultsManager`
(for system defaults) use their own `XStream` instances with `XStreamHelper.allowDefaultPackage()`.
No alias is registered — both serialize using the fully qualified class name, keeping
serialization consistent across the two storage backends.

### Admin tools in edit mode

When a system administrator opens the edit mode, an ellipsis menu (...) is rendered
next to the Cancel button. This menu provides two additional actions:

- **Save as system default** -- Saves the current widget configuration (as shown in the
  edit view) as the system-wide default. Other users without personal preferences will
  see this configuration.
- **Reset system default** -- Deletes the system default configuration. Users without
  personal preferences will fall back to seeing all widgets in registration order.

The admin menu is only rendered when `ureq.getUserSession().getRoles().isSystemAdmin()`
returns `true`. It uses `DropdownUIFactory.createMoreDropdown()` for the ellipsis button
and `LinkFactory.createToolLink()` for the menu items.

### Default behavior summary

- When no personal preferences are stored and a **system default** exists, the system default configuration is used.
- When neither personal preferences nor system defaults exist, **all widgets are shown** in registration order.
- Personal preferences are **never auto-created** -- only explicit user saves are persisted.
- Resetting personal preferences **deletes** the stored preferences (stores `null`), falling back to the system default or all widgets.

## 7. BentoBoxSize Reference

Each enum value maps to a CSS class that defines the widget's grid span.
The naming convention is `box_{columns}_{rows}`.

| Enum value | CSS class | Grid span |
|-----------|-----------|-----------|
| `box_1_1` | `o_bento_box_1_1` | 1 col x 1 row |
| `box_1_2` | `o_bento_box_1_2` | 1 col x 2 rows |
| `box_1_4` | `o_bento_box_1_4` | 1 col x 4 rows |
| `box_2_1` | `o_bento_box_2_1` | 2 cols x 1 row |
| `box_2_2` | `o_bento_box_2_2` | 2 cols x 2 rows |
| `box_2_4` | `o_bento_box_2_4` | 2 cols x 4 rows |
| `box_4_1` | `o_bento_box_4_1` | 4 cols x 1 row |
| `box_4_2` | `o_bento_box_4_2` | 4 cols x 2 rows |
| `box_4_4` | `o_bento_box_4_4` | 4 cols x 4 rows |

The bento grid has 4 columns. A `box_4_1` widget spans the full width with
minimal height -- the most common choice for table widgets. A `box_2_2`
spans half the width and 2 rows -- suitable for chart or summary widgets.

## 8. Velocity Templates

| Template | Purpose |
|----------|---------|
| `dashboard.html` | Main view. Iterates over `$enabledWidgets` and renders each widget in a bento box. Edit button conditionally shown. |
| `dashboard_edit.html` | Edit view. Dragula-enabled container for active widgets, disabled widgets with "Add" buttons. Uses `$r.openJavaScriptCommand()` for AJAX events. For system admins, renders the admin ellipsis menu (`adminMenu`) next to the action buttons. |
| `widget_table.html` | Standard layout for `TableWidgetController` subclasses. Renders title, indicators, table, empty state, and footer. |

## 9. Spring Configuration

The `DashboardSystemDefaultsManager` is a Spring `@Service`. The package
`org.olat.core.gui.control.generic.dashboard` is included in the component scan
configured in `mainCorecontext.xml`:

```xml
<!-- src/main/java/org/olat/core/_spring/mainCorecontext.xml -->
<context:component-scan base-package="...org.olat.core.gui.control.generic.dashboard,..." />
```

Both `DashboardController` and `DashboardEditController` inject the service via
`@Autowired`. This works because OpenOlat's `DefaultController` calls
`CoreSpringFactory.autowireObject(this)` during construction, enabling dependency
injection in controller classes.

## 10. Complete Example

```java
public class CoachDashboardController extends BasicController {

    private DashboardController dashboardCtrl;
    private CourseWidgetController courseCoachCtrl;
    private CoachLectureBlocksWidgetController lectureBlocksCtrl;

    public CoachDashboardController(UserRequest ureq, WindowControl wControl,
            CoachingSecurity coachingSec) {
        super(ureq, wControl);
        setTranslator(Util.createPackageTranslator(
                CoachMainController.class, getLocale(), getTranslator()));

        // Create dashboard with edit support
        dashboardCtrl = new DashboardController(ureq, wControl, getClass().getName());
        listenTo(dashboardCtrl);
        putInitialPanel(dashboardCtrl.getInitialComponent());

        if (coachingSec.coach()) {
            // Add course widget
            courseCoachCtrl = new CourseWidgetController(ureq, wControl);
            listenTo(courseCoachCtrl);
            dashboardCtrl.addWidget("courseCoach",
                    translate("course.as.coach"), courseCoachCtrl, BentoBoxSize.box_4_1);

            // Add lecture blocks widget
            lectureBlocksCtrl = new CoachLectureBlocksWidgetController(ureq, wControl);
            listenTo(lectureBlocksCtrl);
            lectureBlocksCtrl.reload();
            dashboardCtrl.addWidget("lectureBlocks",
                    translate("lectures.title"), lectureBlocksCtrl, BentoBoxSize.box_4_1);
        }
    }

    @Override
    protected void event(UserRequest ureq, Component source, Event event) {
        //
    }

    public void reload() {
        if (courseCoachCtrl != null) courseCoachCtrl.reload();
        if (lectureBlocksCtrl != null) lectureBlocksCtrl.reload();
    }
}
```

## 11. i18n Keys

The dashboard framework uses the following i18n keys in its own
`_i18n/LocalStrings_*.properties`:

| Key | Usage |
|-----|-------|
| `dashboard.edit` | "Edit dashboard" button |
| `dashboard.save` | Save button in edit mode |
| `dashboard.cancel` | Cancel button in edit mode |
| `dashboard.reset` | Reset button in edit mode |
| `dashboard.enabled.widgets` | Section title for active widgets |
| `dashboard.disabled.widgets` | Section title for available widgets |
| `dashboard.add` | "Add" button on disabled widgets |
| `dashboard.remove` | Remove tooltip on enabled widgets |
| `dashboard.drag` | Drag handle label |
| `dashboard.system.default.save` | "Save as system default" admin action |
| `dashboard.system.default.reset` | "Reset system default" admin action |
| `dashboard.system.default.saved` | Info message after saving system default |
| `dashboard.system.default.deleted` | Info message after resetting system default |
