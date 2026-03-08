# Dashboard Framework

**Package:** `org.olat.core.gui.control.generic.dashboard`

Developer documentation for the OpenOlat bento-grid dashboard infrastructure.

## 1. Overview

The dashboard framework provides a reusable, user-configurable widget container
rendered as a CSS Grid bento layout. Widgets can be added by any module.
When editing is enabled, users can reorder, hide, and restore widgets via
drag & drop. Widget configuration is persisted per user in `GuiPreferences`.

Key features:

- Bento grid layout with configurable widget sizes (1x1 up to 4x4)
- Optional edit mode with Dragula.js drag & drop
- Per-user preferences stored via XStream in `GuiPreferences`
- Automatic title detection from controllers implementing `DashboardWidget`
- Abstract `TableWidgetController` base class for table-based widgets

## 2. Class Diagram

<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 870 430" font-family="'SF Mono','Cascadia Code','Fira Code','Consolas',monospace">
  <style>
    .box { stroke-width: 1.2; rx: 4; ry: 4; }
    .box-class    { fill: #f1f5f9; stroke: #475569; }
    .box-iface    { fill: #ecfdf5; stroke: #16a34a; }
    .box-enum     { fill: #fffbeb; stroke: #ca8a04; }
    .box-abstract { fill: #eff6ff; stroke: #2563eb; }
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
  <text class="cls-text" x="18" y="138">- doEdit(ureq)</text>

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

  <!-- DashboardEditController -->
  <rect class="box box-class" x="10" y="200" width="310" height="84"/>
  <text class="cls-title" x="165" y="220" text-anchor="middle">DashboardEditController</text>
  <line class="sep" x1="10" y1="228" x2="320" y2="228"/>
  <text class="cls-text" x="18" y="244">Drag &amp; drop reordering (Dragula.js)</text>
  <text class="cls-text" x="18" y="259">Add / Remove / Reorder widgets</text>
  <text class="cls-text" x="18" y="274">&#8594; CHANGED_EVENT | CANCELLED_EVENT</text>

  <!-- Widget (inner class) -->
  <rect class="box box-class" x="365" y="200" width="195" height="92"/>
  <text class="cls-stereo" x="462" y="215" text-anchor="middle">&#171;inner class&#187;</text>
  <text class="cls-title" x="462" y="231" text-anchor="middle">Widget</text>
  <line class="sep" x1="365" y1="238" x2="560" y2="238"/>
  <text class="cls-text" x="373" y="254">name : String</text>
  <text class="cls-text" x="373" y="269">title : String</text>
  <text class="cls-text" x="373" y="284">css : String</text>

  <!-- DashboardWidget (interface) -->
  <rect class="box box-iface" x="365" y="338" width="195" height="56"/>
  <text class="cls-stereo" x="462" y="354" text-anchor="middle">&#171;interface&#187;</text>
  <text class="cls-title" x="462" y="370" text-anchor="middle">DashboardWidget</text>
  <line class="sep" x1="365" y1="377" x2="560" y2="377"/>
  <text class="cls-text" x="373" y="390">+ getWidgetTitle() : String</text>

  <!-- TableWidgetController (abstract) -->
  <rect class="box box-abstract" x="600" y="310" width="250" height="108"/>
  <text class="cls-stereo" x="725" y="326" text-anchor="middle">&#171;abstract&#187;</text>
  <text class="cls-title" x="725" y="342" text-anchor="middle">TableWidgetController</text>
  <line class="sep" x1="600" y1="349" x2="850" y2="349"/>
  <text class="cls-text" x="608" y="364"># getTitle() : String</text>
  <text class="cls-text" x="608" y="379"># createTable(cont)</text>
  <text class="cls-text" x="608" y="394"># createShowAll(cont)</text>
  <text class="cls-text" x="608" y="409">+ getWidgetTitle() : String</text>

  <!-- Arrows -->
  <line class="arr arr-dash" x1="320" y1="30" x2="575" y2="30" marker-end="url(#ah)"/>
  <text class="arr-lbl" x="448" y="23" text-anchor="middle">reads / writes</text>

  <line class="arr arr-dash" x1="320" y1="112" x2="575" y2="112" marker-end="url(#ah)"/>
  <text class="arr-lbl" x="448" y="105" text-anchor="middle">uses</text>

  <line class="arr arr-dash" x1="100" y1="150" x2="100" y2="200" marker-end="url(#ah)"/>
  <text class="arr-lbl" x="112" y="180">creates</text>

  <polygon points="320,128 312,121 304,128 312,135" fill="#64748b"/>
  <line class="arr" x1="320" y1="128" x2="462" y2="200" marker-end="url(#ah)"/>
  <text class="arr-lbl" x="385" y="152">contains</text>

  <line class="arr" x1="320" y1="250" x2="365" y2="250" marker-end="url(#ah)"/>
  <text class="arr-lbl" x="342" y="243" text-anchor="middle">uses</text>

  <line class="arr arr-dash" x1="600" y1="366" x2="560" y2="366" marker-end="url(#oh)"/>
  <text class="arr-lbl" x="580" y="358" text-anchor="middle">implements</text>
</svg>

### Roles of each class

| Class | Responsibility |
|-------|---------------|
| `DashboardController` | Main controller. Renders the bento grid, manages widget registration, loads user preferences, and opens edit mode. |
| `DashboardEditController` | Edit-mode controller. Drag & drop reordering via Dragula.js, add/remove actions, save/cancel/reset. Fires `CHANGED_EVENT` or `CANCELLED_EVENT`. |
| `Widget` | Static inner class of `DashboardController`. View model holding the widget's `name`, `title`, and `css` class. |
| `DashboardPrefs` | POJO stored in `GuiPreferences` via XStream. Contains the ordered list of enabled widget names. Declares its own `@XStreamAlias` for decoupled serialization. |
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

> **Note:** The `dashboardId` is used as the key for `GuiPreferences`.
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
5. User clicks **Reset** -> preferences are deleted (reverts to defaults) -> `CHANGED_EVENT` is fired
6. User clicks **Cancel** -> `CANCELLED_EVENT` is fired, no changes saved
7. `DashboardController` receives the event, swaps the panel back, and reloads the configuration

### Preference storage

User preferences are stored as `DashboardPrefs` objects in `GuiPreferences`,
serialized via XStream. The `DashboardPrefs` class declares its own alias using the
`@XStreamAlias("DashboardPrefs")` annotation, which is auto-detected by
`PreferencesImpl` (via `xstream.autodetectAnnotations(true)`).
This produces clean XML without package names:

```xml
<DashboardPrefs>
  <enabledWidgets>
    <string>courses</string>
    <string>lectureBlocks</string>
  </enabledWidgets>
</DashboardPrefs>
```

### Default behavior

- When no preferences are stored, **all widgets are shown** in registration order (the default).
- Default settings are **never persisted** -- only explicit user changes are saved.
- Resetting **deletes** the stored preferences (stores `null`), reverting to defaults.

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
| `dashboard_edit.html` | Edit view. Dragula-enabled container for active widgets, disabled widgets with "Add" buttons. Uses `$r.openJavaScriptCommand()` for AJAX events. |
| `widget_table.html` | Standard layout for `TableWidgetController` subclasses. Renders title, indicators, table, empty state, and footer. |

## 9. Complete Example

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

## 10. i18n Keys

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
