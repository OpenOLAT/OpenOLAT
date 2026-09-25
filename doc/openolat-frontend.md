# OpenOlat Frontend Architecture Reference

**Release:** OpenOlat 21.1.0 | **Last updated:** 2026-09-25 | **Companion pages:** `doc/openolat-frontend.html` (visual version with diagrams), `doc/component-library/index.html` (live component examples)

This Markdown file and the HTML version have the same 37 chapters with the same numbering and headings. The HTML version adds the SVG diagrams; the Markdown names them with their caption.

Markers: `**New in 21.1.0**` under a heading and `**New in 21.1.0:**` / `**Changed in 21.1.0:**` at the start of a paragraph flag elements and behaviour that came with release 21.1.0. Changes that shipped in 21.0.x have no marker. Chapter 37 lists all changes of this update.

## 1. Architecture Overview

OpenOlat uses a **server-centric** UI architecture where all state lives on the server. The browser receives pre-rendered HTML fragments via AJAX. There is no client-side framework.

*Figure 1: OpenOlat frontend technology stack showing browser layer (CSS/JS) and server layer (rendering + controllers) (diagram in the HTML version)*

> **Key principle:** No business logic during the render phase. All DB access and service calls must complete before rendering begins. Controllers update state and mark components dirty; renderers only generate HTML.

## 2. Rendering Pipeline

*Figure 2: AJAX request-response cycle showing the full rendering pipeline from user action to DOM update (diagram in the HTML version)*

Every renderable component gets a unique DOM ID: `o_c{dispatchID}`. The server returns JSON commands instructing the browser which DOM elements to replace. Only **dirty** (changed) components are re-rendered, minimizing bandwidth.

## 3. Theme Structure

The theme system is built on **Bootstrap 3.4.1** (SASS version) with ~100 custom OpenOlat SCSS module files (99 imports in `_modules.scss`). The `light` theme is the base; all custom themes derive from it.

*Figure 3: Theme inheritance model — Bootstrap feeds into the Light base theme, which is extended by the OpenOlat default theme and many client themes (diagram in the HTML version)*

Directory layout:

```
src/main/webapp/static/
├── bootstrap/                       # Bootstrap 3.4.1 (SASS version)
│   ├── stylesheets/bootstrap/       # Bootstrap SCSS partials (_variables.scss, _mixins.scss, ...)
│   └── OO_bootstrap_version         # "3.4.1"
├── themes/
│   ├── themes.README                # Theming guide
│   ├── compiletheme.sh              # Dart Sass compile script
│   ├── light/                       # BASE THEME (do not modify)
│   │   ├── theme.scss               # Main entry point → theme.css
│   │   ├── content.scss             # iFrame content → content.css
│   │   ├── email.scss               # Email styles → email.css
│   │   ├── oo-docs.scss             # Icon bundle for the user manual → oo-docs.css
│   │   ├── _config.scss             # ALL variables (~2000 lines)
│   │   ├── _functions.scss          # a11y functions: luminance, contrast
│   │   ├── _modules.scss            # Import manifest (99 imports)
│   │   ├── _patches.scss            # Post-import fixes
│   │   ├── modules/                 # ~100 SCSS partials (_form.scss, _table.scss, _search.scss, _card.scss, ...)
│   │   ├── styles/                  # Optional variants: _square_config.scss, _square_theme.scss, _realistic.scss
│   │   ├── images/  fonts/  meta/   # Images, icon fonts, favicon and app icons
│   └── openolat/                    # DEFAULT DERIVED THEME
│       ├── theme.scss               # fonts → openolat_config → light/theme → openolat_theme
│       ├── _openolat_config.scss    # Variable overrides ($brand-primary, colors, fonts)
│       ├── _openolat_theme.scss     # CSS rule overrides (logo, login page)
│       ├── _openolat_content.scss   # Content CSS overrides
│       ├── _openolat_email.scss     # Email CSS overrides
│       ├── fonts/  images/          # Roboto, logos, backgrounds
│       └── theme.js                 # Theme-specific JavaScript
```

### 3.1 Three CSS Output Files

| File | Purpose | Included Where |
|---|---|---|
| `theme.css` | Main application styles | Every OpenOlat page |
| `content.css` | Learning content in iFrames | Single pages, SCORM, CP iframes |
| `email.css` | Email notification styles | Inlined in email HTML |
| `oo-docs.css` | Icon classes and icon fonts only (`light/oo-docs.scss`) | Copied into the user manual (OpenOLAT-docs), so the manual shows the same icons as the application |

`compiletheme.sh` compiles every non-partial `.scss` file in a theme folder, so `oo-docs.css` is built together with `theme.css` for `light`. `oo-docs.scss` imports only `functions`, `config`, `modules/mixins` and `modules/icons`, and loads the fonts from a sibling `fonts/` folder.

## 4. Compilation Pipeline

Custom themes import their config **before** `light/theme`, so variables with `!default` in `_config.scss` only apply when not already defined:

```
theme.scss (entry point for a custom theme)
  @import "mytheme_config"       // Override variables FIRST
  @import "light/theme"          // Base theme (uses !default)
    @import "functions"          // luminance(), contrast(), o-a11y-color()
    @import "config"             // ~2000 lines of vars with !default
      @import "bootstrap/variables"
    @import "modules"            // 100 SCSS module files
      @import "modules/bootstrap"
      @import "modules/mixins"
      @import "modules/layout"
      @import "modules/main"
      @import "modules/navbar"
      ... (90+ more)
    @import "patches"
  @import "mytheme_theme"        // CSS rule overrides LAST
```

Compile: `./compiletheme.sh mytheme` (requires Dart Sass ≥ 1.33)

## 5. Variables & Config

Variables with `!default` in `_config.scss` are only set if not already defined. Custom themes import their config before `light/theme`, so their values take precedence.

```
// Bootstrap variables (no prefix)
$brand-primary           : #2E6BBF;
$font-size-base          : 14px;
$btn-default-bg          : #fff;

// OpenOlat variables ($o- prefix)
$o-navbar-height         : $navbar-height;
$o-toolbar-bg-color      : $navbar-default-bg;
$o-tree-link-color       : $navbar-default-link-color;
```

All OpenOlat-specific variables use the `$o-` prefix. Bootstrap variables are unprefixed. The `_config.scss` file spans about 2000 lines organized by component (line numbers as of 21.1.0; they move with every change):

| Category | Lines | Example Variables |
|---|---|---|
| Bootstrap overrides | 34–80 | `$btn-default-bg`, `$input-border`, `$text-color` |
| Color system | 82–142 | `$o-labeled-{color}-{variant}-{property}` (8 colors × 3 variants) |
| Brand colors | 150–175 | `$brand-info`, `$brand-warning`, `$brand-danger` |
| Page layout | 273–286 | `$o-page-width-max`, `$o-main-*-bg` |
| Navbar | 289–346 | `$o-navbar-height`, `$o-navbar-bg`, `$o-navbar-link-*` |
| Toolbar | 380–424 | `$o-toolbar-bg-color`, `$o-toolbar-breadcrumb-*` |
| Forms | 427–445 | `$o-radio-card-*` |
| Tree menu | 448–475 | `$o-tree-link-*`, `$o-tree-padding-*` |
| Course | 677–760 | `$o-course-state-*`, `$o-lp-*` (learning path) |
| QTI Assessment | 1021–1115 | `$o-qti-status-*`, `$o-qti-menu-*` |
| Content Editor | 963–1017 | `$o-editor-*`, `$o-ce-padding-*` |
| Email | 1488–1503 | `$o-email-brand`, `$o-email-btn-*` |
| Fact sheet **(New in 21.1.0)** | 1873–1885 | `$o-fact-sheet-*`, `$o-fact-*` (padding 20px, title 18px, icon 32px, `$o-fact-min-width: 320px`) |

Button and field borders use two tokens: `$btn-default-border` (`#6c757d`) and `$btn-default-hover-border` (`#667085`) for buttons and dropdown toggles, `$input-border` (`#d0d5dd`) for text fields, radio cards, tag selection and the toolbar nav. Ghost buttons use `$o-button-ghost-color: $btn-primary-bg` with a transparent hover background. `$o-font-size-section-title` (about 18px) sets the size of section headings.

## 6. Accessibility Functions

`_functions.scss` provides built-in WCAG contrast checking used throughout the config:

```
// Calculate relative luminance (0=black, 1=white)
@function luminance($color) { ... }

// Calculate contrast ratio (returns float, 4.5+ = WCAG AA)
@function contrast($color1, $color2) { ... }

// Auto-adjust color for WCAG 4.5:1 contrast
@function o-a11y-color($color, $background, $targetRatio: 4.5) { ... }

// Usage in config:
$btn-primary-color: o-a11y-color(#fff, $btn-primary-bg);

// Dynamic contrast check:
@if contrast($brand-primary, #FFF) < 4.5 {
  $o-brand-box-border: darken($brand-primary, 20%);
}
```

## 7. Theme Customization

To create a custom theme, copy `openolat/`, rename files, and override variables + CSS rules:

1. Copy `openolat/` → `mytheme/`
2. Rename `_openolat_*` files to `_mytheme_*`
3. Update imports in `theme.scss`, `content.scss`, `email.scss`
4. Override variables in `_mytheme_config.scss` (typically 20–50 vars):

- `$brand-primary` — main brand color
- `$text-color`, `$link-color`, `$headings-color`
- `$font-family-base`
- `$o-navbar-*`, `$o-footer-*`
- `$o-login-form-bg-img`, `$o-login-form-align`
5. Override CSS rules in `_mytheme_theme.scss` (typically 50–150 lines):

- `.o_navbar-brand` (logo image)
- `.o_login_logo` (login page logo)
- `#o_body.o_dmz` (login page layout)
6. Compile: `./compiletheme.sh mytheme`

## 8. Page DOM Structure

*Figure 4: Page DOM skeleton — the TooledStackedPanel renders `#o_main_toolbar` as a sibling above `#o_main`, which uses CSS Grid for its 3-column layout (diagram in the HTML version)*

Skeleton as HTML:

```
<body id="o_body" class="[o_dmz]">
  <a href="#o_main_container" class="sr-only">Skip to main content</a>
  <div id="o_navbar_wrapper" class="o_navbar" role="navigation" aria-label="Main navigation">
    <div id="o_navbar_container">
      <a class="o_navbar-brand" href="..."></a>                   <!-- logo -->
      <ul class="o_navbar-nav o_navbar_tabs">...site tabs...</ul>
      <ul id="o_navbar_more" class="nav o_navbar-nav o_dropdown_toggle">...</ul>
      <ul id="o_navbar_tools_permanent" class="nav o_navbar-nav o_navbar-right">
        <li id="o_navbar_imclient" class="o_navbar_tool">...</li>
        <li id="o_navbar_search_opener" class="o_navbar_tool dropdown">...#o_navbar_search...</li>
        <li id="o_navbar_my_menu" class="o_portrait">...</li>       <!-- personal menu, logout inside -->
      </ul>
    </div>
  </div>
  <div id="o_messages">...</div>                                  <!-- info boxes -->
  <div id="o_main_wrapper">
    <div id="o_main_container">
      <!-- TooledStackedPanel: #o_main_toolbar.o_toolbar is a sibling of #o_main -->
      <div id="o_main" class="[o_hide_main_left] [o_hide_main_right]">
        <div id="o_main_left"><div id="o_main_left_content">...tree...</div></div>
        <div id="o_main_center"><div id="o_main_center_content"><div id="o_main_center_content_inner">...</div></div></div>
        <div id="o_main_right"><div id="o_main_right_content">...</div></div>
      </div>
    </div>
  </div>
  <div id="o_footer_wrapper"><div id="o_footer_container">...</div></div>
</body>
```

## 9. CSS Grid Layout

`#o_main` uses CSS Grid for the 3-column layout. The center column fills all available space via `minmax(0, 1fr)`:

```
#o_main {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto;
  column-gap: 15px;

  &.o_hide_main_left       { grid-template-columns: minmax(0, 1fr) auto; }
  &.o_hide_main_right      { grid-template-columns: auto minmax(0, 1fr); }
  &.o_hide_main_left.o_hide_main_right { grid-template-columns: minmax(0, 1fr); }
}
```

## 10. Toolbar & Breadcrumb

The toolbar (`#o_main_toolbar .o_toolbar`) is rendered by `TooledStackedPanel` as a **sibling above** `#o_main`. Both are children of the panel wrapper (`.o_with_toolbar`). The toolbar contains the breadcrumb trail and action tools organized by alignment:

| Tool Align | CSS Class | Position |
|---|---|---|
| left | `o_tools_left` | Left side of toolbar |
| center | `o_tools_center` | Center of toolbar |
| right | `o_tools_right` | Right side of toolbar |
| rightEdge | `o_tools_right_edge` | Far right (settings, close) |
| segment | `o_tools_segments` | Below toolbar as tab-like buttons |

Toolbar markup:

```
<div class="o_toolbar [o_toolbar_with_segments]">
  <div class="o_breadcrumb">
    <ol class="breadcrumb">
      <li class="o_breadcrumb_back"><a>Back</a></li>
      <li class="o_breadcrumb_root"><a>Home</a></li>
      <li class="o_breadcrumb_crumb"><a>Level 1</a></li>
      <li class="o_breadcrumb_crumb o_last_crumb" aria-current="true">Current</li>
      <li class="o_breadcrumb_close"><a>Close</a></li>
    </ol>
  </div>
  <div class="o_tools_container">
    <ul class="o_tools o_tools_left list-inline"><li class="o_tool"><a class="btn btn-sm">Action</a></li></ul>
    <ul class="o_tools o_tools_center list-inline">...</ul>
    <ul class="o_tools o_tools_right list-inline">...</ul>
    <ul class="o_tools o_tools_right_edge list-inline">...</ul>
  </div>
</div>
```

Segments are rendered by `SegmentViewComponent` as a justified button group. The selected segment is a primary button:

```
<ul class="o_tools o_tools_segments [o_tools_segments_alone] list-inline">
  <li>
    <div class="o_segments btn-group btn-group-justified" role="navigation">
      <a class="btn btn-primary">Tab 1</a>   <!-- selected -->
      <a class="btn btn-default">Tab 2</a>
    </div>
  </li>
</ul>
```

The personal tools in the navbar are in `ul#o_navbar_tools_permanent`: `li#o_navbar_imclient` (chat), `li#o_navbar_search_opener` (search dropdown with `#o_navbar_search`) and `li#o_navbar_my_menu` (portrait and personal menu, logout inside).

## 11. Navigation System

Navigation in OpenOlat is based on a **breadcrumb controller stack**. Each level represents a pushed controller:

- **BreadcrumbedStackedPanel** — manages the stack of breadcrumb links + content panel
- **TooledStackedPanel** — extends with toolbar, tools, segments, messages
- **pushController()** — adds a new breadcrumb level
- **popController()** — navigates back (removes level)

TabbedPane markup:

```
<div class="o_tabbed_pane">
  <ul role="tablist" class="nav nav-tabs">
    <li class="active"><a role="tab" aria-selected="true">Tab 1</a></li>
    <li><a role="tab" aria-selected="false">Tab 2</a></li>
  </ul>
  <div role="tabpanel" class="o_tabbed_pane_content">...active tab content...</div>
</div>
```

> **Tab navigation:** `TabbedPane` renders `<ul class="nav nav-tabs">` for internal tab switching. `SegmentViewComponent` renders segment buttons in the toolbar for view switching (list/card/etc.).

## 12. Component Renderers

```
User Action → AJAX POST → Server Dispatch
  → Controller.event() → Business Logic → Mark dirty
  → RENDER PHASE: Window → Component Tree traversal
      → ComponentRenderer.render(StringOutput, Component, args) → HTML string
  → JSON response: {action: "updcmp", id: "o_c12345", content: "..."}
  → Client: jQuery replaces the DOM element by ID
```

Every rendered component gets a unique DOM ID `o_c{dispatchID}`, for example `<div id="o_c12345">`. `VelocityContainerRenderer` processes the `.html` templates; templates include child components with `$r.render("name")`. `LayeredPanelRenderer` adds `o_layer_0`, `o_layer_1` for the modal stacking.

LinkRenderer output:

```
<a id="o_c{id}" href="javascript:;" onclick="o_XHREvent(...);" class="[btn btn-default] [o_button_ghost]">
  <i class="o_icon o_icon_{name}" aria-hidden="true"></i> <span>Link Text</span>
</a>
<!-- disabled -->
<a class="o_disabled disabled btn btn-default">...</a>
```

OpenOlat has **~540 ComponentRenderer implementations**. Each generates HTML for a specific component type. Key renderers:

| Renderer | Component | Key CSS Classes |
|---|---|---|
| VelocityContainerRenderer | Template-based views | varies (template-defined) |
| PanelRenderer | Stacked content panels | `o_layer_0`, `o_layer_1` |
| LinkRenderer | Links and buttons | `btn`, `btn-default`, `o_disabled` |
| FormLayoutRenderer | Form containers | `form-horizontal`, `form-group` |
| FlexiTableClassicRenderer | Data tables (classic) | `o_table_wrapper`, `o_rendertype_classic`, `table` |
| FlexiTableCustomRenderer | Data tables (card/list) | `o_table_wrapper`, `o_rendertype_custom`, `o_table_row` |
| MenuTreeRenderer | Tree navigation | `o_tree`, `o_tree_l{n}` |
| TabbedPaneRenderer | Tab navigation | `nav nav-tabs`, `o_tabbed_pane` |
| DropdownRenderer | Dropdown menus | `dropdown`, `dropdown-menu` |
| TooledStackedPanelRenderer | Toolbar + breadcrumb | `o_toolbar`, `o_breadcrumb`, `o_tools_*` |
| CloseableModalController | Modal dialogs | `dialog.modal`, `modal-dialog`, `modal-content` |
| SearchElementRenderer **(New in 21.1.0)** | Search fields | `o_search`, `o_search_default`, `o_search_large`, `o_search_typeahead` |
| FactSheetRenderer **(New in 21.1.0)** | Fact sheets | `o_fact_sheet`, `o_facts`, `o_fact` |
| SectionsRenderer / SectionHeaderRenderer **(New in 21.1.0)** | Collapsible sections | `o_sections`, `o_section`, `o_section_toggle` |
| ComponentListRenderer **(New in 21.1.0)** | Row of components | `o_component_list` |
| TranslateCellRenderer **(New in 21.1.0)** | Translatable term in a table cell | `o_icon_language` |

## 13. Form Elements

| Element | Renderer | DOM Output |
|---|---|---|
| Text input | TextElementRenderer | `<input class="form-control">` |
| Textarea | TextAreaElementRenderer | `<textarea class="form-control">` |
| Select | SelectboxRenderer | `<select class="form-control">` |
| Radio buttons | RadioElementRenderer | `<div class="radio"><label><input type="radio">` |
| Checkboxes | CheckboxElementRenderer | `<div class="checkbox"><label><input type="checkbox">` |
| Date picker | JSDateChooserRenderer | `<div class="o_date"><div class="input-group o_date_picker"><input class="form-control o_date_day datepicker-input">` + Vanilla JS Datepicker popup |
| Rich text | RichTextElementRenderer | TinyMCE wrapper |
| File upload | FileElementRenderer | `<input type="file">` + upload zone |
| Toggle switch | FormToggleRenderer | `<button type="button" role="switch" aria-checked="true" class="o_button_toggle o_toggle_on">` |
| Submit button | FormSubmitRenderer | `<button class="btn btn-primary">` |
| Static text | StaticTextElementRenderer | `<p class="form-control-static">` (wrapper element configurable) |
| Spacer | SpacerElementRenderer | `<hr class="o_spacer">`, optionally in `div.o_spacer_wrapper` |
| Form link | FormLinkRenderer | `<a class="btn btn-default">` |
| Object selection | ObjectSelectionComponentRenderer | `<button class="o_expand_button btn btn-default o_selection_display" aria-haspopup="dialog">` + optional `button.o_selection_browse_button` |
| Relative date | RelativeDateComponentRenderer | `<div class="o_relative_date input-group">` with read-only input |
| Search **(New in 21.1.0)** | SearchElementRenderer | `<div class="o_search o_search_default" role="search">` |
| Form section **(New in 21.1.0)** | `form_section.html` | `<fieldset class="o_form_section">` |

All form elements are wrapped in `<div class="form-group">` with a `<label class="control-label">`. Validation errors appear as `<div class="o_error">`.

**New in 21.1.0:** `FormItem.setLabelIconCss(String)` renders an icon before the label text: `<label class="control-label"><i class="o_icon o_icon-fw o_icon_locked" aria-hidden="true"></i> Label</label>`. The mandatory marker `o_icon_mandatory` comes first. The course editor uses `o_icon o_icon-fw o_icon_locked text-primary` for fields that stay read-only until the edit mode is on.

Object selection: a disabled element renders `disabled o_disabled` and the `disabled` attribute on the button. The browse button stays visible and is disabled too. User selections list the current user first, with the suffix "(me)".

### 13.1 Form Section

**New in 21.1.0**

A form section groups the fields of one form under a heading. Java: `FormSection` (`uifactory.addFormSection(name, title, container, Level)`). The template `form_section.html` renders one `fieldset.o_form_section`. The items inside use the normal 3/9 horizontal layout. Styles: `.o_form_section` in `_form.scss`.

*Figure 6: Settings page blueprint from the GUI demo "Example settings": a module toggle and two form sections with the level SUB_TITLE (diagram in the HTML version)*

```
<!-- Level.SUB_TITLE -->
<fieldset id="o_c1000001513" class="o_form_section">
  <div class="o_section_sub_title"><span class="o_section_title_text">Configuration</span></div>
  <div id="o_c1000001513_content">...form-groups...</div>
</fieldset>

<!-- Level.TITLE + setCollapsible(true) -->
<fieldset id="o_c42" class="o_form_section">
  <legend>
    <h4 class="o_section_title o_section_toggle o_link_plain [collapsed]" id="o_c42_content_toggle"
        role="button" tabindex="0" data-target="o_c42_content"
        aria-controls="o_c42_content" aria-expanded="true">
      <span class="o_section_title_text">
        <i id="o_c42_content_toggler" aria-hidden="true" class="o_icon o_icon-fw o_icon_close_togglebox"> </i>Title
        <span id="o_c42_content_togglerLabel" class="sr-only">collapse</span>
      </span>
    </h4>
  </legend>
  <div id="o_c42_content" class="collapse o_section_content in">...</div>
</fieldset>
```

- **Levels:** `TITLE` renders `legend > h4.o_section_title`. `SUB_TITLE` renders `div.o_section_sub_title` with `$o-font-size-section-title` and a `::after` rule. Below `$screen-xs-max` the rule moves under the title.
- **Collapsible:** Bootstrap `collapse` on `.o_section_content`. One delegated handler in `functions.js` toggles on click, Enter and Space, and keeps `aria-expanded`, `collapsed`, the icon and the `sr-only` label in sync.
- **Persisted state:** `setPersistedStatusId(ureq, id)` stores the open/closed state in the GUI preferences.

### 13.2 Search Element

**New in 21.1.0**

One element for all search fields: FlexiTable quick search, navbar search, overview searches, object and tag selection. Java: `uifactory.addSearchElement(name, SearchVariant, container)`, or with a `ListProvider` for suggestions. Styles: `_search.scss`. GUI demo: **Flexi forms > Search**.

```
<div class="o_search o_search_default" role="search">
  <div class="input-group">
    <!-- only without search button: span.input-group-addon with o_icon_search -->
    <div class="o_search_input_wrapper">
      <input type="text" class="form-control o_search_input" role="searchbox" aria-label="Enter search term">
      <span id="{id}_resetWrap" style="display:none">   <!-- visible when the field has text -->
        <a class="btn btn-sm btn-default o_button_ghost o_search_reset" role="button">...o_icon_close...</a>
      </span>
    </div>
    <span class="input-group-btn">
      <a class="btn btn-default o_search_button" role="button">...o_icon_search...</a>
    </span>
  </div>
</div>
```

| Variant | Class | Behaviour |
|---|---|---|
| `SearchVariant.DEFAULT` | `o_search_default` | Search button with icon only, toolbar size |
| `SearchVariant.LARGE` | `o_search_large` | Search button with icon and text, `input-lg` sizing |
| `SearchVariant.TYPEAHEAD` | `o_search_typeahead` | No button, addon icon left, search while typing (300 ms debounce). With a `ListProvider` typeahead.js wraps the input (`.tt-input`, `.tt-menu`) |
| `setSearchButtonVisible(false)` | `o_search_default` | Same markup as typeahead, addon icon left |

Keyboard: Enter starts the search by AJAX, not by form submit. When a suggestion is highlighted (`.tt-cursor`), Enter commits the suggestion. In the typeahead variant Escape resets the field. Widths: `#o_navbar_search .o_search` 320px, `.o_table_search .o_search` max 340px, `.o_table_large_search .o_search` max 450px.

## 14. FlexiTable

FlexiTable is the primary data display component. It supports **4 render types**, selectable per table instance:

| Render Type | CSS Class | Description |
|---|---|---|
| **CLASSIC** | `o_rendertype_classic` | Traditional HTML `<table>` with headers, rows, and cells. Used by member lists, enrollment tables, admin tables, Autorenbereich |
| **CUSTOM** | `o_rendertype_custom` | Flex-based card/list view. No visible table headers. Used by course list, catalog, coaching dashboard |
| **EXTERNAL** | — | Fully custom external renderer |
| **VERTICAL_TIMELINE** | `o_vertical_timeline` | Date-grouped timeline with path connectors. Used by activity logs, course timelines |

Users toggle between CLASSIC and CUSTOM views via `.o_sel_table` / `.o_sel_custom` buttons in the toolbar (class `active` on the selected one).

### 14.1 FlexiTable Component Structure

A full FlexiTable with all features enabled has this overall layout. Each section is rendered only when applicable:

```
<!-- Component DIV (e.g. #o_c...) contains all parts as siblings: -->
<div class="o_table_tabs">                                    <!-- Tab buttons (Favoriten, Meine Kurse, Suche, etc.) -->
  <a class="btn btn-default btn-primary o_sel_author_bookmarks">...</a>
  <a class="btn btn-default o_sel_author_courses">...</a>
</div>
<div class="o_table_filters_wrapper o_expanded">              <!-- FlexiFiltersComponentRenderer -->
  <div class="o_table_filters_row">
    <ul class="nav nav-pills o_table_filters">
      <li role="presentation">
        <a class="btn btn-default o_table_filter o_filter_active"> <!-- active on <a>, NOT <li> -->
          <span>Type: Course</span> <i class="o_icon o_icon-fw o_icon_caret"></i>
        </a>
      </li>
      <li role="presentation">
        <a class="btn btn-default o_table_filter"><span>Status</span> <i class="o_icon o_icon-fw o_icon_caret"></i></a>
      </li>
    </ul>
  </div>
</div>
<div class="o_table_toolbar o_table_batch_hide clearfix">     <!-- Search + tools row -->
  <div class="o_table_search o_noprint">...</div>
  <div class="o_table_tools o_noprint">
    <div class="btn-group"><a class="btn btn-default">Sort</a></div>
    <div class="btn-group"><a class="btn btn-default">Columns</a></div>
    <div class="btn-group"><a class="btn btn-default">Download</a></div>
    <div class="btn-group o_table_settings">...</div>
  </div>
</div>
<div class="o_button_group o_table_batch_buttons o_table_batch_hide"> <!-- Bulk actions -->
  <a class="btn btn-default">E-Mail</a>
  <a class="btn btn-default o_sel_modify_status">Status</a>
</div>
<div class="o_table_wrapper o_table_flexi [o_table_bulk] [o_coursetable] o_rendertype_classic|custom">
  ...                                                          <!-- table or card body -->
</div>
```

### 14.2 Classic Table DOM

```
<div class="o_table_wrapper o_table_flexi o_table_bulk o_rendertype_classic">
  <div class="o_scrollable_wrapper">
    <table class="table table-condensed table-striped table-hover">
      <thead><tr>
        <th class="o_multiselect o_table_checkall o_col_sticky_left">...</th>
        <th class="o_col_favorit"><a>Favorit</a></th>
        <th><a>Typ <i class="o_icon o_icon_sort_asc"></i></a></th>
        <th class="o_col_displayname"><a>Titel</a></th>
        <th class="o_col_sticky_right o_col_action">...</th>
      </tr></thead>
      <tbody>
        <tr>
          <td class="o_multiselect o_col_sticky_left"><input type="checkbox"></td>
          <td class="text-left">...</td>
          <td class="text-left o_col_sticky_right o_col_action">...</td>
        </tr>
      </tbody>
    </table>
  </div>
  <div class="o_table_pagination">...</div>
</div>
```

### 14.3 Custom (Card/List) View DOM

```
<div class="o_table_wrapper o_table_flexi o_coursetable o_rendertype_custom">
  <div class="o_table_body">
    <div class="o_table_row row">
      <div class="o_repo_entry_list_item o_seminar_course">   <!-- type class varies -->
        <div class="o_visual">
          <a><img ...></a>                                    <!-- thumbnail -->
        </div>
        <div class="o_meta">
          <div class="o_go_xs visible-xs">...</div>
          <div class="o_ext_ref">
            <span class="o_technical_type">Lernpfad</span>
          </div>
          <h3 class="o_title"><a>Course Name</a></h3>
          <div class="o_flex_block_one_line_left o_education">
            <span class="o_educational_type">Seminar</span>
          </div>
          <div class="o_flex_block_one_line_left o_flex_block_last">
            <span class="o_performance">...</span>           <!-- progress -->
          </div>
        </div>
        <div class="o_access">
          <div class="o_social_actions">...</div>
          <div class="o_actions">...</div>
        </div>
      </div>
    </div>
  </div>
  <div class="o_table_pagination">...</div>
</div>
```

### 14.4 Course Type Classes on `o_repo_entry_list_item`

The card item gets an additional class based on the educational type / course format:

| Class | Course type |
|---|---|
| `o_standard_course` | Standard course (default) |
| `o_seminar_course` | Seminar format |
| `o_self_study_course` | Self-study course |
| `o_exam_course` | Exam course |

### 14.5 Course Table & Catalog Classes

| Class | Description |
|---|---|
| `o_coursetable` | Course table root container (works with both render types) |
| `o_repo_entry_list_item` | Single entry in custom/card view (gets course-type class) |
| `o_repo_entry_card` | Card variant of a repository entry |
| `o_visual` | Thumbnail/image container |
| `o_visual_not_available` | Placeholder when no image is available |
| `o_meta` | Metadata container (title, ext ref, educational type, performance) |
| `o_title` | Entry title (h3 in card view) |
| `o_technical_type` | Technical course type label (Lernpfad / Kurs) |
| `o_ext_ref` | External reference/ID code container |
| `o_educational_type` | Badge showing educational format (exam, seminar, etc.) |
| `o_education` | Wrapper for educational type with flex layout |
| `o_performance` | Performance indicators (progress bars, scores) |
| `o_teaser` | Short description/teaser text |
| `o_author` | Author name display |
| `o_taxonomy_levels` | Taxonomy tag labels |
| `o_lifecycle` | Start/end date display |
| `o_access` | Access controls and action buttons column |
| `o_social_actions` | Social actions within access (comments, ratings) |
| `o_actions` | Primary action buttons within access |
| `o_start` | Open/start button on course list entries |
| `o_catalog_open` | Book/open button on catalog entries |
| `o_bookmark` | Bookmark/favorite indicator |
| `o_entry_cards` | Grid container for card-mode entries |

### 14.6 Autorenbereich (Author Area) Table

The Autorenbereich uses a CLASSIC FlexiTable with tabs, filters, bulk operations, and extensive column configurations:

- **Tabs:** `o_sel_author_bookmarks`, `o_sel_author_courses`, `o_sel_author_my`, `o_sel_author_search`, `o_sel_author_deleted`
- **Key columns:** `o_col_favorit`, `o_col_displayname`, `o_col_lifecycleStart`, `o_col_lifecycleEnd`, `o_col_author`, `o_col_access` (status), `o_col_ac` (sharing), `o_col_references`
- **Sticky:** Checkbox column (`o_col_sticky_left`), actions column (`o_col_sticky_right o_col_action`)
- **Bulk actions:** E-Mail, Status, Verwendungszweck, Besitzer/innen, Metadaten, Kopieren, Löschen
- **Search tab filters:** `ul.nav.nav-pills.o_table_filters` with filter pills (Id, Favoriten, Technischer Typ, Durchführungsformat, Status, Typ, Mehr...)

### 14.7 Coaching Dashboard

The Coaching site uses a CUSTOM FlexiTable within a dashboard layout. Cards are wrapped in `o_bento` grid:

```
<div class="o_dashboard">
  <div class="o_bento">...</div>                              <!-- dashboard grid -->
</div>
<div class="o_table_wrapper o_table_flexi o_rendertype_custom">
  <div class="o_table_body container-fluid o_dashboard_table_max_height o_scrollable_vertical">
    <div class="o_table_row row">
      <a class="o_row_link o_link_uncolored">
        <div class="o_coaching_widget_course_row">
          <div class="o_row_course">...</div>
          <div class="o_row_completion">...</div>
          <div class="o_row_success_status">...</div>
        </div>
      </a>
    </div>
  </div>
</div>
```

### 14.8 Curriculum Table Classes

| Class | Description |
|---|---|
| `o_curriculumtable` | Curriculum element table root |
| `o_curriculum_course_cards` | Grid of course cards within curriculum |
| `o_curriculum_course_card` | Individual curriculum course card |
| `o_curriculum_offer_cards` | Offer/booking cards container |
| `o_curriculum_element_inactive` | Inactive curriculum element styling |

## 15. Tree Navigation

The `.o_tree` component renders hierarchical navigation with indentation levels controlled by CSS classes `o_tree_l0` through `o_tree_l11`, each adding `$o-tree-padding-steps-px` (15px) of left padding.

```
<div class="o_tree [o_tree_root_hidden]">
  <ul role="tree" class="o_tree_l0">
    <li role="treeitem" aria-selected="false" data-nodeid="...">
      <div id="dd{nodeid}" class="o_tree_l0">
        <a class="o_tree_oc_l0">...</a>                  <!-- opener, only for nodes with children -->
        <span class="o_tree_link o_tree_l0">
          <a href="javascript:;" title="..."><span class="o_tree_item">Node Title</span></a>
        </span>
      </div>
      <ul class="o_tree_l1">...children...</ul>
    </li>
  </ul>
</div>
```

## 16. Modals & Dialogs

Dialogs use Bootstrap's modal structure wrapped in OpenOlat's `LayeredPanel` for z-index stacking:

```
<div class="o_layered_panel o_layer_0">              <!-- LayeredPanelRenderer -->
  <dialog id="o_closablewapper_{id}" class="dialog modal show" aria-modal="true" aria-labelledby="o_md_{id}">
    <div class="modal-dialog modal-lg">
      <div class="modal-content">
        <div class="modal-header">...<h4 id="o_md_{id}">Title</h4></div>
        <div class="modal-body">...</div>
        <div class="modal-footer">...</div>   <!-- DialogBoxController -->
      </div>
    </div>
  </dialog>
</div>
```

Multiple modals stack with `o_layer_0`, `o_layer_1`, etc. The HTML element is a native `<dialog>` with the classes `dialog modal show`; `.modal-dialog` is the inner Bootstrap wrapper.

### 16.1 Callout Triggers and Focus (a11y)

A link or button that opens a callout uses the ARIA disclosure pattern. `Link.setAriaDialogOpener()` and `FormLink.setAriaDialogOpener()` render `role="button" aria-haspopup="dialog" aria-expanded="false"`. `CloseableCalloutWindowController` switches `aria-expanded` with the `setattribute` command and returns the focus to the trigger when the callout closes.

*Figure 7: ARIA state and focus handling when a callout opens and closes (diagram in the HTML version)*

**Changed in 21.1.0:** The pattern is rolled out to the "more actions" buttons of table action columns (`ActionsColumnModel`), table tools (sort, customize columns), filter buttons, relative date and multi-selection fields, and many feature controllers. Controllers pass the trigger component to `CloseableCalloutWindowController` instead of its DOM ID. When a callout opens, the focus moves to the first *visible* focusable element. The hidden close control is a `<button class="close">` in `div.sr-only` (before: `<a>`).

## 17. CSS Naming Conventions

| Prefix | Origin | Example |
|---|---|---|
| (none) | Bootstrap 3 | `btn`, `form-control`, `table`, `nav-tabs`, `panel` |
| `o_` | OpenOlat core | `o_toolbar`, `o_tree`, `o_table_wrapper`, `o_block` |
| `o_icon_` | Icon classes | `o_icon_delete`, `o_icon_edit`, `o_icon_user` |
| `o_sel_` | Selenium test selectors | `o_sel_course_add_member`, `o_sel_author_courses` |
| `fa-` | Font Awesome 6 | `fa-check`, `fa-times`, `fa-spinner` |
| `#o_` | Page structure IDs | `#o_body`, `#o_main`, `#o_navbar_wrapper` |
| `o_colcat_` | Color category | `o_colcat_dark_blue`, `o_colcat_red_bg` |
| `o_lp_` | Learning path status | `o_lp_done`, `o_lp_in_progress` |
| `o_ce_` | Content editor | `o_ce_add`, `o_ce_edit_wrapper`, `o_ce_layout_normal` |

The `o_sel_` prefix is reserved exclusively for Selenium test selectors. These classes carry no CSS styling and should never be used for visual purposes. They provide stable anchors for automated UI testing.

## 18. Layout & Spacing Classes

### 18.1 Block Spacing

| Class | Effect |
|---|---|
| `o_block` | margin: 1em 0 (standard vertical spacing between sections) |
| `o_block_top` / `o_block_bottom` | margin-top / margin-bottom: 1em only |
| `o_block_small` | margin: 0.5em 0 (tight vertical spacing) |
| `o_block_small_top` / `o_block_small_bottom` | margin-top / margin-bottom: 0.5em only |
| `o_block_large` | margin: 2em 0 (wide vertical spacing) |
| `o_block_large_top` / `o_block_large_bottom` | margin-top / margin-bottom: 2em only |
| `o_block_move_up` | Negative top margin (-1em) to pull element upward |
| `o_block_move_up_small` / `o_block_move_up_large` | Negative top margin (-0.5em / -2em) |
| `o_block_inline` | display: inline-block; vertical-align: top |
| `o_block_inline_left` / `o_block_inline_right` | Inline-block with left/right margin spacing |
| `o_block_inline_both` | Inline-block with spacing on both sides |

### 18.2 Flexbox Utilities

| Class | Effect |
|---|---|
| `o_flex_block_one_line_left` | Flex row, nowrap, width:100%. Combine with `o_flex_item_fix` and `o_flex_item_max` children |
| `o_flex_item_fix` | flex: 0 0 auto (fixed-size child, does not grow/shrink) |
| `o_flex_item_max` | flex: 1 1 auto with overflow:hidden and text-overflow:ellipsis |
| `o_flex_first_grow` | First child takes remaining horizontal space |
| `o_flex_first_grow_vertical` | First child takes remaining vertical space in column layout |

### 18.3 Grid Layouts

| Class | Effect |
|---|---|
| `o_two_col_metadata` | CSS Grid: 2 equal columns (collapses to 1 on small screens) |
| `o_two_span` | Spans full width in a 2-column grid |
| `o_metadata_horizontal` | Flex horizontal metadata layout with 45px column gap |
| `o_block_with_icon_left` | Grid: 32px icon column + auto content column |
| `o_form_two_cols` | 2-column grid for form layouts (from `$screen-sm-max`, 991px), gap 10px |

### 18.4 Centering & Positioning

| Class | Effect |
|---|---|
| `o_block_centered_wrapper` + `o_block_centered_content` | Table-based vertical & horizontal centering |
| `o_block_imagebg` | Full background image with semi-transparent text overlay |
| `o_header_with_buttons` | Heading with right-floated button group, clearfix |
| `o_centered_form` | Center-aligned text with inline fieldset |

### 18.5 Scrolling

| Class | Effect |
|---|---|
| `o_scrollblock` | overflow-x: auto; overflow-y: hidden (horizontal scroll only) |
| `o_scrollable_wrapper` | Scroll container with left/right shadow indicators via pseudo-elements |
| `o_scrollable` | Inner scrollable area (overflow:auto, -webkit-overflow-scrolling:touch) |
| `o_scrollable_left` | Shows left shadow indicator when scrolled right |
| `o_scrollable_right` | Shows right shadow indicator when more content to scroll |

### 18.6 Padding Helpers

| Class | Effect |
|---|---|
| `o_flow_padding_left_large` | 2em left padding |
| `o_flow_padding_left_small` | 0.5em left padding |
| `o_spacer_left` | 2.5em left margin |
| `o_list_unstyled_left` | 20px left padding for unstyled lists |
| `o_list_left_aligned` | Removes left padding from a list while keeping the bullets visible (uses `list-style-position: inside`) |

## 19. Typography & Text Classes

### 19.1 Font Size

| Class | Effect |
|---|---|
| `o_xsmall` | Extra small font (~10px) |
| `o_small` | Small font size |
| `o_large` | Large font size |
| `o_xlarge` | Extra large font size |

### 19.2 Visual State

| Class | Effect |
|---|---|
| `o_disabled` | Muted color, default cursor, no text decoration. Indicates non-interactive element |
| `o_dimmed` | opacity: 0.4 — visually fades the element |
| `o_muted` | color: $text-muted (Bootstrap muted color) |
| `o_selected` | font-weight: bold — highlights selected item |
| `o_deleted` | text-decoration: line-through |
| `o_highlight_on_hover` | Background color change on hover (for row highlighting) |

### 19.3 Text Handling

| Class | Effect |
|---|---|
| `o_nowrap` | white-space: nowrap; overflow: hidden; text-overflow: ellipsis |
| `o_wrap_anywhere` | overflow-wrap: anywhere (breaks on any character) |
| `o_wrap_break_word` | overflow-wrap: break-word |
| `o_with_hyphens` | CSS hyphenation (webkit/moz/ms/standard) |
| `first-letter-capitalize` | Capitalizes first letter via :first-letter pseudo-element |

### 19.4 Interaction

| Class | Effect |
|---|---|
| `o_clickable` | cursor: pointer |
| `o_undecorated` | No underline on hover/focus (for links) |
| `o_dragable` | cursor: grab (changes to grabbing on active) |
| `o_hidden` | visibility: hidden (reserves space but invisible) |

### 19.5 Link Variants

| Class | Effect |
|---|---|
| `o_link_plain` | Remove link color and decoration entirely |
| `o_link_uncolored` | Remove link color, keep other properties |
| `o_button_textstyle` | Minimal text-only button rendered as `[text]` with brackets |

### 19.6 Code & Monospace

| Class | Effect |
|---|---|
| `o_copy_code` | Monospace text with background, horizontal scroll for input/textarea |
| `o_markdown_striped` | Pre-formatted text with white-space: pre-wrap |
| `o_static_textelement_bold` | Bold text for static form display elements |

## 20. Button Variants

### 20.1 Bootstrap Button Types

| Class | Description |
|---|---|
| `btn btn-default` | Standard button (grey border, white bg) |
| `btn btn-primary` | Primary action button ($brand-primary color) |
| `btn btn-success` | Success action button (green) |
| `btn btn-warning` | Warning action button (orange) |
| `btn btn-danger` | Destructive action button (red) |
| `btn btn-link` | Button styled as a text link |
| `btn-xs` / `btn-sm` / `btn-lg` | Button size variants (extra small, small, large) |

### 20.2 OpenOlat Button Extensions

| Class | Description |
|---|---|
| `btn o_button_ghost` | Transparent background, primary-colored text (`$btn-primary-bg`), primary border on hover. Used for low-emphasis actions |
| `btn o_button_ghost btn-danger` **(New in 21.1.0)** | Ghost button for destructive actions: `$brand-danger` text and hover border. Java: `link.setGhost(true)` plus `btn-danger`. Example: "Leave course" on the info page |
| `btn o_button_mega` | Large button with flex layout for icon + title + subtitle. Used in selection screens |
| `btn o_button_primary_light` | Primary color outline variant (lighter emphasis than btn-primary) |
| `btn o_button_call_to_action` | Large, bold CTA button (min-width: 30%, wide padding) |
| `btn o_button_dirty` | Warning-colored button indicating unsaved changes |

### 20.3 Button Groups & Layout

| Class | Description |
|---|---|
| `o_button_group` | Center-aligned button container with standard margins |
| `o_button_group_left` | Left-aligned button group |
| `o_button_group_right` | Right-aligned button group |
| `o_button_group_top` | Remove top margin from button group |
| `o_button_group_bottom` | Remove bottom margin from button group |
| `o_mega_buttons` | CSS Grid container for mega buttons (`repeat(auto-fill, minmax(207px, 1fr))`, gap 8px) |
| `o_button_group compact` **(New in 21.1.0)** | Button group with small vertical margin, for example the "enable edit mode" button inside an info box |
| `o_btn_group_nowrap` | With `btn-group`: `display: flex`, split buttons do not wrap |

Disabled buttons: `LinkRenderer` writes `<a class="o_disabled disabled btn btn-default">`. Button borders use `$btn-default-border` (`#6c757d`), text fields the lighter `$input-border` (`#d0d5dd`). A `FormLink` with the class `input-group-addon` in an input group gets the button border and background (`_bootstrap.scss`); the text field keeps `$input-bg`, also when it is disabled.

## 21. Messages & Alerts

### 21.1 Message Boxes

Full-width message boxes with icon and colored styling. Available in two forms: basic (text only) and `_with_icon` (icon + content layout).

| Class | Color | Icon | Usage |
|---|---|---|---|
| `o_info` / `o_info_with_icon` | Blue | info-circle | Informational messages |
| `o_note` / `o_note_with_icon` | Light | lightbulb | Notes and tips |
| `o_tip` / `o_tip_with_icon` | Light | hand-pointer | Usage tips and hints |
| `o_important` / `o_important_with_icon` | Amber | exclamation | Important notices |
| `o_success` / `o_success_with_icon` | Green | check-circle | Success confirmations |
| `o_warning` / `o_warning_with_icon` | Orange | triangle-excl. | Warning messages |
| `o_error` / `o_error_with_icon` | Red | circle-xmark | Error messages |

### 21.2 Inline Variants

| Class | Description |
|---|---|
| `o_warning_line` | Compact inline warning (left border only, no full box) |
| `o_error_line` | Compact inline error (left border only, no full box) |
| `o_hint` | Simple hint text without box styling |
| `o_instruction` | Warning-style message with top padding (for instructions) |

### 21.3 System Alerts

| Class | Description |
|---|---|
| `#o_msg_sticky` | Danger-colored sticky bar at top of page (system announcements) |
| `.o_alert_info` | Auto-disappearing notification fixed at top (z-index 2000) |
| `.o_msg_sticky_fullscreen` | Sticky message without top margin (for fullscreen mode) |

## 22. Form Classes

### 22.1 Form Containers

| Class | Description |
|---|---|
| `.form-group` | Bootstrap standard form group wrapper (label + input + help) |
| `.form-control` | Bootstrap standard form input styling |
| `o_form_two_cols` | 2-column grid for side-by-side form fields (from 991px). Template `form_two_cols.html` renders a container with this class |
| `o_form_section` **(New in 21.1.0)** | Form section fieldset, see [Form Section](#131-form-section) |
| `o_form_button_right` | Grid: form area (100fr) + button (1fr) side-by-side |
| `o_inline_cont.form-inline` | Inline form where children display as inline-block |
| `o_form_label` | Inline-block, bold, 5px bottom margin |
| `o_muted_labels` | Muted color for all labels within this container |

### 22.2 Form Feedback

| Class | Description |
|---|---|
| `.o_form .o_desc` | Info-style description below a field |
| `.o_form .o_error` | Red error message below a field |
| `.o_form .o_warning` | Warning message below a field |
| `.o_form .o_hint` | Hint text below a field |
| `.o_form .o_form_example` | Example text (90% font-size, muted) |
| `.o_form .o_form_explanation` | Warning-colored explanation text |
| `.o_form .o_form_chelp` | Context help icon positioned near label |

**Form field errors** are rendered by `SimpleFormErrorText` as a block `<div class="o_error">` (not a `<span>`) positioned below the input within the `.form-group`. This is distinct from message boxes — form errors are inline field-level feedback.

### 22.3 Date Picker

| Class | Description |
|---|---|
| `o_date` | Flex container with 20px column-gap, 15px row-gap (wrapping) |
| `o_date_picker` | 11em width input-group for date selection |
| `o_date_ms` | 3em width input for milliseconds |
| `o_first_date` / `o_second_ms` | Padding for date range (from/to) layout |
| `o_table_filter_date_range` | Date range picker within table filter |

### 22.4 Radio Cards

Rich selection controls that render as visual cards instead of plain radio buttons:

| Class | Description |
|---|---|
| `o_radio_cards_wrapper .o_radio_cards` | Flex container (column or row wrap) for radio card layout |
| `o_radio_card` | Individual card: flex row, center aligned, with padding and border |
| `o_radio_label` | Bold label text within card |
| `o_radio_desc` | Description text (90% font-size) below label |
| `o_radio_icon` | Icon area within card (fixed size, no shrink) |
| `o_radio_cards_sm` / `o_radio_cards_lg` | Small / large size variants for cards |
| `o_radio_cards_vcenter` | Vertically center items within cards |
| `o_radio_card_top_to_bottom` | Grid layout with icon above label (vertical card) |

### 22.5 Toggle & Switch Controls

| Class | Description |
|---|---|
| `o_button_toggle` | On/off toggle switch (border, rounded, inline-block) |
| `o_toggle_block` | Display: block modifier for toggle |
| `o_toggle_disabled` | 50% opacity disabled state |
| `o_toggle_check` | Circular toggle checkbox (18px, 1.5em border-radius) |
| `o_toggle_button` | Button-style toggle with text and icon |
| `o_toggle_link` | Link-colored background variant of toggle |

### 22.6 File Upload

| Class | Description |
|---|---|
| `o_fileinput` | File input wrapper (layers real input over styled fake) |
| `o_fakechooser` | Visible styled button (z-index 1) |
| `o_realchooser` | Transparent real file input overlay (z-index 2) |
| `o_fileinput.o_area` | Drag-drop area (dashed border, 5px 20px padding) |
| `o_dnd_over` | Active state when file is dragged over drop zone |
| `o_filepreview` | Flex wrap preview of uploaded file thumbnails |

### 22.7 Color & Icon Pickers

| Class | Description |
|---|---|
| `o_color_picker_button` | Full-width button with color swatch |
| `o_color_picker_colored_area` | 20×20px colored square with border and radius |
| `o_icon_selector_button` | Full-width button showing selected icon |
| `o_compact` | Compact grid of color/icon swatches (32px cells) |

## 23. Table Classes

### 23.1 FlexiTable Structure

| Class | Description |
|---|---|
| `o_table_wrapper.o_table_flexi` | Main FlexiTable container |
| `o_table_body` | Table body area (margin-top: line-height) |
| `o_table_toolbar` | Toolbar above table (flex align-center, space-between, 100% width) |
| `o_table_footer` | Footer with pagination and info |

### 23.2 Table Search & Filters

| Class | Description |
|---|---|
| `o_table_search` | Search area in the toolbar. **(Changed in 21.1.0)** Contains the `SearchElement` (`.o_search.o_search_default`, max 340px, 170px below 540px) and the row count |
| `o_table_large_search` | Large search above the table. **(Changed in 21.1.0)** Contains `.o_search.o_search_large` (max 450px) and `.o_table_rowcount` |
| `o_reset_quick_search` | Reset button within search field |
| `o_table_tabs` | Tab bar above table (flex, space-between, border-bottom) |
| `o_table_filters_wrapper` | Filter area (z-index 3, relative, centered) |
| `o_table_filter` | Individual filter button |
| `o_filter_active` | Active filter state (colored) |
| `o_table_add_filter` | Button to add new filter (200px width) |
| `o_table_filter_callout` | Filter popup/callout container |

### 23.3 Table Rows & Columns

| Class | Description |
|---|---|
| `o_row_selected` | Selected row background color |
| `o_table_row_expanded` | Expanded detail row background |
| `o_table_row_details` | Detail content row with padding and no side borders |
| `o_marked` | Bold text for marked/important rows |
| `o_marked_deleted` | Italic, muted, strikethrough text for deleted items |
| `o_col_action` | 35px min-width column for action icons |
| `o_cell_stretch` | width: 100% to fill available column space |
| `o_multiselect` | 20px checkbox column with centered text |
| `o_singleselect` | 32px radio column with centered text |

**New in 21.1.0:** `TranslateCellRenderer` marks a translatable term in a cell: a link with `o_icon_language`, the title and `sr-only` text "Translate "{term}"" (core key `translate.name`), then the term. The link fires the table action `translate`. First use: grade scales.

### 23.4 Sticky Columns

| Class | Description |
|---|---|
| `o_col_sticky_left` | position:sticky; left:0 (freezes column on horizontal scroll) |
| `o_col_sticky_right` | position:sticky; right:0 (freezes column on horizontal scroll) |

When the table scrolls, shadow indicators appear on sticky columns via the `o_scrollable_left` / `o_scrollable_right` classes on the wrapper.

### 23.5 Batch Operations

| Class | Description |
|---|---|
| `o_table_batch_buttons` | Container for batch action buttons (appears when rows selected) |
| `o_table_batch_hide` | display: none when no rows selected |
| `o_table_batch_label` | Label text for batch area (80% font-size) |

### 23.6 Table Variants

| Class | Description |
|---|---|
| `o_table_reduced` | Compact table: hidden header/footer, flex rows, inline-block cells |
| `o_table_edit` | Edit mode with distinct border-top and background |
| `o_table_no_margin` | Remove all margins from table |
| `o_thead_sr_only` | Screen-reader only table header (visually hidden) |

### 23.7 Vertical Timeline

| Class | Description |
|---|---|
| `o_table_flexi.o_vertical_timeline` | Timeline layout variant of FlexiTable |
| `o_vertical_timeline_year` | Year header with border and padding |
| `o_vertical_timeline_row` | Grid row: 90px date column + 1fr content |
| `o_vertical_timeline_item` | Timeline item with path connector and content |

### 23.8 Sort Indicators

| Class | Description |
|---|---|
| `a.o_orderby` | Sortable column header link (nowrap, no underline) |
| `o_orderby_asc` | Ascending sort indicator (caret-up icon via :after) |
| `o_orderby_desc` | Descending sort indicator (caret-down icon via :after) |

## 24. Cards & Widgets

### 24.1 Card Layout

| Class | Description |
|---|---|
| `o_card` | Standard card with border and border-radius |
| `o_card_title a` | Link styling within card title |
| `o_cards` | Flex wrap container with 10px gap |
| `o_cards.o_cards_4` | 4-column responsive grid (3 on md, 2 on sm, 1 on xs) |
| `o_cards.o_cards_5` | 5-column responsive grid |
| `o_card_radio_select` | Card with selectable radio input (grid, center-aligned) |

### 24.2 Widgets

| Class | Description |
|---|---|
| `o_widget` | Compact widget card (min-width, fixed height, flex column, border) |
| `o_widget_header` | Widget header with title + right-aligned icon |
| `o_widget_content` | Widget main content area |
| `o_widget_main_text` | Text-based widget content |
| `o_widget_main_figure` | Large number/figure widget content |
| `o_widget_main_download` | Download document widget content |
| `o_widget_text_success` / `o_widget_text_danger` | Success (green) / danger (red) color variants |
| `o_widgets` | CSS Grid auto-fill container (minmax 260px) |

#### Widget DOM Structure

Widgets are rendered in a responsive CSS Grid. Each widget is a card with a header and content area. Widget types include figures, text, tables, and downloads.

```
<div class="o_widgets">                         <!-- WidgetGroupRenderer, CSS Grid: auto-fill minmax(260px, 1fr) -->
  <div class="o_widget">                         <!-- WidgetRenderer -->
    <div class="o_widget_header">
      <div class="o_widget_title o_flex_item_max">Widget Title</div>
      <div class="o_widget_icon"><i class="o_icon ..."></i></div>
    </div>
    <div class="o_widget_content">
      <div class="o_widget_main o_widget_main_figure">   <!-- FigureWidgetRenderer -->
        <div class="o_widget_figure o_flex_item_max">...</div>
      </div>
      <!-- or: div.o_widget_main.o_widget_main_text (TextWidgetRenderer)
           or: div.o_widget_main + div.o_widget_link (ComponentWidgetRenderer, e.g. a mini table) -->
    </div>
    <div class="o_widget_additional">...</div>   <!-- optional -->
  </div>
</div>
```

### 24.3 Fact Sheet

**New in 21.1.0**

`FactSheet` (`org.olat.core.gui.components.factsheet`, `FactSheetFactory`) shows key facts with icons in a responsive grid. The info page of courses and implementations uses it. GUI demo: **Fact Sheets**. Styles: `_card.scss`, variables `$o-fact-sheet-*` and `$o-fact-*`.

```
<div class="o_fact_sheet">
  <h3 class="o_fact_sheet_title">Fact sheet</h3>
  <div class="o_facts">                        <!-- grid: repeat(auto-fill, minmax($o-fact-min-width, 1fr)) -->
    <div class="o_fact">
      <div class="o_fact_icon"><i class="o_icon o_icon_location"> </i></div>
      <div class="o_fact_body">
        <div class="o_fact_label text-muted">Location</div>
        <div class="o_fact_value">Lviv</div>    <!-- text or a component -->
        <div class="o_fact_sub_value">Campus Pidzamche, room B3.12</div>
      </div>
    </div>
  </div>
  <div class="o_fact_sheet_footer"><div class="o_fact_sheet_footer_buttons">...</div></div>
</div>
```

| Class | Description |
|---|---|
| `o_fact_sheet` | Box with border (`$o-fact-sheet-border`), radius and 20px padding |
| `o_fact_sheet_title` | `h3`, 18px, bold, separator line below |
| `o_facts` | CSS Grid, columns of at least 320px (`$o-fact-min-width`), gap 20px |
| `o_fact_icon` | 32px circle, icon in `$brand-primary` on 10% tint |
| `o_fact_label` / `o_fact_value` / `o_fact_sub_value` | Muted label, value (16px), optional smaller sub-value |
| `o_fact_sheet_footer_buttons` | Right-aligned footer buttons; `o_fact_sheet_footer_full` on a link makes it full width |
| `o_component_list` | `ComponentList` output: wrapping flex row, gap 10px (e.g. group links in one fact value) |

### 24.4 Sections

**New in 21.1.0**

`Sections` (`org.olat.core.gui.components.sections`, `SectionsFactory`) renders titled, collapsible blocks outside of forms, for example on the redesigned info page. `SectionHeaderRenderer` writes the same header as the collapsible [Form Section](#131-form-section), and the same `functions.js` handler toggles it.

```
<div id="o_sections_{id}" class="o_sections">
  <div class="o_section">
    <fieldset>
      <legend><h4 class="o_section_title o_section_toggle o_link_plain" role="button" tabindex="0"
                  aria-controls="o_sections_{id}_{sectionId}" aria-expanded="true">...</h4></legend>
      <div id="o_sections_{id}_{sectionId}" class="collapse o_section_content in">...</div>
    </fieldset>
  </div>
</div>
```

### 24.5 Dashboard

| Class | Description |
|---|---|
| `o_dashboard_widget` | Dashboard widget with border, radius, bg-color, flex column |
| `o_dashboard_widget_header` | Widget header with h3 |
| `o_dashboard_widget_footer` | Footer with right-aligned buttons |
| `o_dashboard_table_max_height` | Max-height with scroll gradient overlay |
| `o_dashboard_edit` | Dashboard edit mode container |
| `o_dashboard_edit_item` | Draggable widget in edit mode (transition, grab cursor) |
| `o_empty_state` | Empty state placeholder styling |

### 24.6 Panels

| Class | Description |
|---|---|
| `panel-imagebg` | Panel with background image |
| `panel-placeholder` | Dashed border placeholder panel ("add content here") |

## 25. State & Visibility

### 25.1 Drag & Drop

| Class | Description |
|---|---|
| `o_dragable` | cursor: grab; on :active changes to cursor: grabbing |
| `o_draging` | cursor: grabbing state during drag |
| `o_dnd_item` | Draggable item (cursor: move, z-index 100) |
| `o_dnd_proxy` | 40% opacity drag placeholder |
| `o_dnd_over` | Yellow highlight when dragging over a drop target |
| `o_dnd_sibling` | 7px height drop zone between siblings |
| `gu-mirror` | Dragula.js mirror element (box-shadow during drag) |
| `gu-transit` | Dragula.js transit element (dashed border) |

### 25.2 Activity Indicators

| Class | Description |
|---|---|
| `o_activity_add` | Green border circle with plus icon (add activity) |
| `o_activity_modify` | Yellow border circle with pencil icon (modify activity) |
| `o_activity_remove` | Red border circle with trash icon (remove activity) |

### 25.3 Skeleton Loading

| Class | Description |
|---|---|
| `o_skeleton` | Flex column with gap 10px and top border |
| `o_skeleton_bar` | 12px tall grey bar with border-radius 6px |
| `o_skeleton_bar_full` | 100% width skeleton bar |
| `o_skeleton_bar_long` | 80% width skeleton bar |
| `o_skeleton_bar_medium` | 55% width skeleton bar |
| `o_skeleton_bar_short` | 35% width skeleton bar |

### 25.4 Assessment Status

| Class | Description |
|---|---|
| `o_passed` | Green text color for passed status |
| `o_failed` | Red text color for failed status |
| `o_unknown` | Yellow text color for unknown status |
| `o_noinfo` | Muted text color for no-info status |

### 25.5 Learning Path Status

| Class | Description |
|---|---|
| `o_lp_not_accessible` | Not accessible (ban icon, dotted connector path) |
| `o_lp_ready` | Ready to start (circle icon, grey connector) |
| `o_lp_in_progress` | In progress (play icon, blue connector) |
| `o_lp_in_review` | In review (highlighter icon) |
| `o_lp_done` | Completed (check icon, green connector) |

### 25.6 Labeled Badges

| Class | Description |
|---|---|
| `o_labeled.o_results_visible` | Results visible badge |
| `o_labeled.o_results_hidden` | Results hidden badge |
| `o_labeled.o_evaluation_in_progress` | Evaluation in progress badge |
| `o_labeled.o_evaluation_done` | Evaluation done badge |

### 25.7 Repository Entry Status

| Class | Description |
|---|---|
| `o_repo_status_preparation` | Entry in preparation (draft) |
| `o_repo_status_review` | Entry under review |
| `o_repo_status_published` | Published and available |
| `o_repo_status_coachpublished` | Published for coaches only |
| `o_repo_status_closed` | Closed / archived |
| `o_repo_status_trash` | In trash / deleted |

### 25.8 Educational Course Types

| Class | Description |
|---|---|
| `o_educational_type` | Base class: inline-block with border and color |
| `o_standard_course` | Standard course type |
| `o_exam_course` | Exam course type |
| `o_certification_course` | Certification course type |
| `o_date_course` / `o_seminar_course` | Date-based / seminar course types |
| `o_blendedlearning_course` / `o_selfstudy_course` | Blended learning / self-study types |

### 25.9 Course Color Categories

| Class | Description |
|---|---|
| `o_colcat_dark_blue` / `o_colcat_light_blue` | Blue color categories |
| `o_colcat_purple` / `o_colcat_red` | Purple and red categories |
| `o_colcat_orange` / `o_colcat_yellow` | Warm color categories |
| `o_colcat_light_green` / `o_colcat_dark_green` | Green color categories |
| `o_colcat_*_bg` | Background variant of color category |
| `o_colcat_*_left` / `o_colcat_*_top` | Left / top border color variants |
| `o_colcat_nocolor` | Diagonal stripe pattern fallback (no color assigned) |

## 26. Color System

OpenOlat defines a semantic color system with 8 base colors, each in 3 variants (solid, light/outline, mega/tinted):

*Figure 5: The labeled color system with 8 colors × 3 variants (solid, light/outline, mega/tinted background) (diagram in the HTML version)*

| Color | Base | Solid (labeled) | Light (outline) | Mega (tinted bg) |
|---|---|---|---|---|
| Blue | `#105CAD` | White on `#146DCC` | Blue on white, blue border | Blue on `#cde3f9` |
| Green | `#268000` | White on `#37AD00` | Green on white, green border | Green on `#C4E6B8` |
| Yellow |  | `#574000` on `#FBD774` | `#574000` on white | `#805e00` on `#fbe6a7` |
| Orange | `#D17A00` | `#663B00` on `#F4AC47` | Orange on white | `#663B00` on `#FFDAA6` |
| Red |  | White on `#b30018` | Red on white | Red on `#ffc9bd` |
| Brown |  | White on `#804A33` | Brown on white | Brown on `#E7D2BC` |
| Grey |  | White on `#595959` | Grey on white | `#342c24` on `#F6F6F6` |

Variables: `$o-labeled-{color}-{variant}-{property}` where variant = (none)/light/mega and property = color/bg-color/border-color.

## 27. SCSS Module Index

### 27.1 Core Modules

| Module | Purpose |
|---|---|
| `_bootstrap.scss` | Bootstrap 3.4.1 imports + overrides |
| `_mixins.scss` | OpenOlat SASS mixins (message boxes, icons, buttons) |
| `_icons.scss` | Font Awesome 6 icon definitions |
| `_helpers.scss` | Spacing, flex, scrolling, buttons, typography utilities |
| `_fonts.scss` | Font imports and declarations |
| `_colors.scss` | Labeled color classes |
| `_type.scss` | Typography and heading styles |

### 27.2 Layout Modules

| Module | Purpose |
|---|---|
| `_layout.scss` | html/body base, footer margin |
| `_main.scss` | CSS Grid 3-column layout (#o_main) |
| `_navbar.scss` | Top navigation bar, tabs, brand, tools |
| `_toolbar.scss` | Breadcrumb + tools container |
| `_footer.scss` | Page footer |
| `_offcanvas.scss` | Mobile off-canvas menu |

### 27.3 Component Modules

| Module | Purpose |
|---|---|
| `_form.scss` | Forms, date pickers, radio cards, toggles |
| `_table.scss` | FlexiTable, filters, pagination, sticky columns |
| `_tree.scss` | Menu tree navigation, DnD, badges |
| `_dialog.scss` | Modal dialogs, floating windows |
| `_breadcrumb.scss` | Breadcrumb trail styling |
| `_wizard.scss` | Multi-step wizard progress |
| `_card.scss` | Card layouts, dashboard widgets; **(New in 21.1.0)** fact sheet |
| `_search.scss` | **Changed in 21.1.0:** Unified `SearchElement` styles (variants, reset button, navbar and table widths) |
| `_bento.scss` | Bento grid layout |
| `_autocomplete.scss` | Autocompletion (typeahead.js `tt-*` classes) |

### 27.4 Feature Modules (selection of ~60)

| Module | Feature |
|---|---|
| `_contenteditor.scss` | Page/content editor |
| `_course.scss` | Course node styles, learning path |
| `_qti21.scss` | Assessment interactions |
| `_cal.scss` | Calendar (FullCalendar) |
| `_forum.scss` | Forum messages, quotes |
| `_portfolio_v2.scss` | Portfolio entries, binders |
| `_repository.scss` | Course catalog |
| `_dmz.scss` | Login page |
| `_curriculum.scss` | Curriculum browser |
| `_video.scss` | Video player |
| `_projects.scss` | Project management |
| `_todo.scss` | To-do lists |
| `_print.scss` | Print styles |
| `_room_management.scss` | Buildings, rooms, room scheduling |
| `_ai_pulse.scss` | Animated SVG indicator for AI activity (`o_ai_pulse`, `o_ai_pulse_active`, sizes `o_ai_pulse_xs` to `o_ai_pulse_xl`, reduced-motion fallback) |
| `_ai_feedback.scss` | AI essay correction marks (`o_ai_mark_correct`, `o_ai_mark_ambiguous`, `o_ai_mark_wrong`) |
| `_selectus.scss` | Selectus application and review module |

## 28. AJAX Mechanism

OpenOlat uses a custom AJAX framework (not REST). User actions trigger `o_XHREvent()` which POSTs to the server. The response contains JSON commands that update specific DOM elements. Background polling (`jQuery.periodic`) provides server-push capability.

1. **User action:** JavaScript captures the event (click, submit, change).
2. **XHR POST:** `o_XHREvent()` or a form event (`o_ffXHREvent()`) sends the request.
3. **Server dispatch:** the window finds the target component by ID.
4. **Controller event:** business logic, state updates, `setDirty(true)`.
5. **Render phase:** only dirty components are re-rendered.
6. **JSON response:** array of commands, for example `[{action: "updcmp", id: "o_c12345", content: "..."}]`.
7. **DOM update:** `jQuery('#o_c12345').replaceWith(newHTML)`.

Form events send the request data as an array of `{name, value}` pairs, not as an object, so a checkbox group with several checked boxes sends all values. Explicit parameters override form fields with the same name; `dispatchuri`, `dispatchevent` and `_csrf` are always added.

Background polling for server-push updates:

```
jQuery.periodic({period: 5000, decay: 1.005}, function() {
  jQuery.ajax({
    method: 'POST', url: '/olat/auth/ajax',
    data: {_csrf: token, oow: window.name},
    success: function(commands) { o_ainvoke(commands); }
  });
});
```

### 28.1 Key JavaScript Functions

| Function | Purpose |
|---|---|
| `o_XHREvent()` | Send AJAX request for link/button clicks |
| `o_ffXHREvent()` / `o_ffXHRNFEvent()` | Send AJAX request for form element events. Data is sent as an array of `{name, value}` pairs, so all values of a checkbox group arrive |
| `o_ainvoke()` | Execute JSON command array from server. Handles `pushdialogfocus`, `popdialogfocus` and `setattribute` for callout focus and ARIA state |
| `setFlexiFormDirty()` | Track unsaved form changes |
| `o_waitForVisibleThenFocusDialog()` | Focus the first focusable element of a new dialog. **(Changed in 21.1.0)** Skips clipped elements |
| `o_scrollToElementIfInvisible()` | Scroll only when the element is outside the viewport or the modal content |
| `o_shareCopyLink()` **(New in 21.1.0)** | Copy a share link to the clipboard and show a confirmation (new share component `o_share_links`) |
| `o_table_toggleCheck()` | Toggle table row selection |
| `o_initScrollableOverflowIndicator()` | Show scroll shadow indicators |

## 29. Dirty Marking

Only components marked **dirty** are re-rendered. This minimizes the HTML payload in AJAX responses:

```
// In controller event handler
if (itemDeleted) {
  tableComponent.setDirty(true);   // Re-render table
  messagePanel.setDirty(true);     // Show success message
  // Navbar, toolbar stay clean → not re-rendered
}
```

## 30. Responsive Design

Bootstrap 3 breakpoints:

| Breakpoint | Variable | Width |
|---|---|---|
| xs (phone) | `$screen-xs-max` | < 768px |
| sm (tablet) | `$screen-sm-max` | < 992px |
| md (desktop) | `$screen-md-max` | < 1200px |
| lg (large) | — | ≥ 1200px |

Key responsive behaviors: navbar collapses to hamburger on xs/sm; left column becomes offcanvas drawer on xs; tables get horizontal scroll with shadow indicators; toolbar tools shrink/hide text on smaller screens.

- `o_form_two_cols` is a 2-column grid only from `$screen-sm-max` (991px) upwards.
- **New in 21.1.0:** Below `$screen-xs-max` the rule of `.o_section_sub_title` moves under the title.
- **New in 21.1.0:** `.o_facts` uses `auto-fill` columns of at least 320px, so the fact sheet shows 1 to n columns without media queries.
- **New in 21.1.0:** Below 540px the table quick search shrinks to 170px.

## 31. Third-Party Libraries

| Library | Version | Purpose | CSS Impact |
|---|---|---|---|
| Bootstrap | 3.4.1 (SASS, JS partly removed) | CSS framework | Grid, buttons, forms, navs, modals |
| Font Awesome | 6.7.2 | Icon set | `fa-*` / `o_icon_*` classes |
| jQuery | 3.7.1 | DOM manipulation | (no CSS) |
| TinyMCE | 6.8.6 (folder `js/tinymce4/`) | Rich text editor | Themed via `_tinymce.scss` |
| FullCalendar | 6.1.15 | Calendar widget | `fc-*` classes, `_cal.scss` |
| Vanilla JS Datepicker | 1.3.4 | Date picker of `JSDateChooserRenderer` | `datepicker-*` classes |
| jQuery UI | 1.13.2 | Dialog, drag and drop, resize, slider | `ui-*` classes, `_thirdparty.scss` |
| typeahead.js | bundle in `js/jquery/typeahead/` | Autocompletion, `SearchElement` with `ListProvider` | `tt-*` classes, `_autocomplete.scss` |
| MathLive | — | Math input | `_mathlive.scss` |
| D3.js | 7.9.0 | Charts (pie, bar, radar, statistics) via the jQuery plugins in `js/jquery/openolat/` | SVG, minimal CSS |
| Leaflet | 1.9.4 | Maps in room management (building address) | `leaflet.css` |

Other OpenOlat scripts: `js/openolat/resize.js` holds `o_adjustContentHeightForAbsoluteElement()`, and `js/edusharing/edusharing.js` holds the edu-sharing integration. Both were moved out of `functions.js`.

## 32. Print Styles

`_print.scss` defines `@media print` rules:

- Hides navbar, footer, toolbar tools, tree menu
- Shows breadcrumb as text trail
- Forces white backgrounds
- Avoids page breaks in tables and forms; helpers `o_print_break_avoid`, `o_print_break_before`, `o_print_break_after`
- Adds print-specific header with logo via `#o_print_brand`

**New in 21.1.0:** The info page of courses and implementations (`.o_info_page`, `_repository.scss`) has its own `@media print` block. It forces the one-column order of the mobile layout, hides interactive parts, and shows the QR code (`.o_info_page_qrcode`) next to the header instead of the teaser image.

## 33. Key CSS IDs

| ID | Element | Purpose |
|---|---|---|
| `#o_body` | `<body>` | Root body, gets `.o_dmz` on login page |
| `#o_navbar_wrapper` / `#o_navbar_container` | `<div>` | Top navigation bar |
| `#o_navbar_tools_permanent` | `<ul>` | Personal tools: `#o_navbar_imclient`, `#o_navbar_search_opener`, `#o_navbar_my_menu` |
| `#o_main_wrapper` / `#o_main_container` | `<div>` | Main content wrappers |
| `#o_main` | `<div>` | CSS Grid 3-column layout |
| `#o_main_left` / `#o_main_center` / `#o_main_right` | `<div>` | Tree menu / content / sidebar |
| `#o_main_center_content_inner` | `<div>` | Content area inner wrapper |
| `#o_footer_wrapper` / `#o_footer_container` | `<div>` | Footer |
| `#o_toplink` | `<a>` | Scroll-to-top link |
| `#o_bg` | `<div>` | Background image (login page) |

## 34. Renderer Statistics

| Category | Count | Key Renderers |
|---|---|---|
| Form elements | 24 | TextElementRenderer, SelectboxRenderer, CheckboxElementRenderer, SearchElementRenderer |
| FlexiTable | 9 | FlexiTableClassicRenderer, FlexiFilterTabsComponentRenderer |
| Panels/Containers | 13 | PanelRenderer, IconPanelRenderer, VelocityContainerRenderer, SectionsRenderer |
| Navigation | 10 | TabbedPaneRenderer, DropdownRenderer, MenuTreeRenderer |
| Widgets | 4 | WidgetRenderer, TextWidgetRenderer, FigureWidgetRenderer |
| Media/Files | 4 | ImageRenderer, DownloadComponentRenderer |
| Charts | 7 | BarChartComponentRenderer, ProgressBarRenderer |
| Assessment/QTI | 10 | AssessmentTestComponentRenderer |
| Content Editor | 6 | ContentEditorComponentRenderer |
| Specialized Cell | 250+ | PassedCellRenderer, CompletionRenderer, GradeCellRenderer, TranslateCellRenderer |
| Other | 200+ | Curriculum, Portfolio, Badge, Lecture, FactSheet renderers |
| **Total** | **~540** |  |

## 35. Page Pattern: Info Page

**New in 21.1.0**

The info page of courses and implementations was redesigned in 21.1.0 (OO-8728). Template: `repository/ui/list/_content/info_page.html`. Styles: `.o_info_page` in `_repository.scss`. Library entry: [Layout & Containers, Info Page](component-library/layout.html#info-page).

```
<div class="o_info_page [o_{educationalType}_course] [o_info_page_member]">
  <div class="o_info_page_column_aside">
    <div class="o_info_page_item o_info_page_thumbnail">...</div>
    <div class="o_info_page_item o_info_page_get_started">...offers, start button...</div>
    <div class="o_info_page_item o_info_page_my_course">...FactSheet "My course"...</div>
    <div class="o_info_page_item o_info_page_events">...dates...</div>
  </div>
  <div class="o_info_page_column_main">
    <div class="o_info_page_item o_info_page_header">
      <div class="o_header">...o_meta, h2 title, o_header_actions, o_header_offers...
        <div class="o_info_page_qrcode">...</div>          <!-- print only -->
      </div>
    </div>
    <div class="o_info_page_item o_info_page_facts">...FactSheet...</div>
    <div class="o_info_page_item o_info_page_sections">...Sections...</div>
    <div class="o_info_page_item o_info_page_licence">...</div>
    <fieldset class="o_info_page_item o_info_page_rating"><legend>Rating</legend>...</fieldset>
  </div>
</div>
```

*Figure 8: The info page items sorted by `order` in the one-column layout (left) and placed in two columns on desktop (right). Orange items belong to the aside column. (diagram in the HTML version)*

- **Mobile (below `$screen-sm-min`):** one grid column, gap 20px. The two column wrappers get `display: contents`, so the items become grid children and `order` 1 to 10 sorts them.
- **Tablet (sm):** `grid-template-columns: 1fr auto`; the thumbnail (max 285px) sits next to the header.
- **Desktop (from `$screen-md-min`):** `grid-template-columns: 2fr 1fr`; each column wrapper is a flex column with gap 20px.
- **Print:** back to the one-column order, interactive parts hidden, `.o_info_page_qrcode` shown next to the header.
- **Building blocks:** the facts and "My course" are [Fact Sheets](#243-fact-sheet), the description is [Sections](#244-sections), "Leave course" is a ghost danger button with `o_fact_sheet_footer_full`, share is a dialog opener.

## 36. Accessibility

This chapter collects the accessibility mechanisms of the frontend layer. Markers show what changed in 21.1.0; the rest is documented here for the first time.

### 36.1 Focus Management

| Mechanism | Behaviour |
|---|---|
| Focus after AJAX | The server sends focus commands; `o_ffSetFocusArray()` tries each target with `o_ffSetFocus()`. Only inputs, selects, textareas, and buttons or `.btn` links with `o_can_have_focus` receive focus. A text input gets its content selected. A second attempt runs after the next tick because typeahead.js can steal the focus. Nothing happens while the focus is inside an open `<dialog>`. |
| `o_info.lastFormFocusEl` | Form elements record the last focused element in `onfocus`, so a re-rendered form can restore it. |
| Scroll and focus | `o_scrollTopAndFocus()` scrolls to the top first, then sets the focus, so the page does not jump twice. |
| Dialogs and callouts | `o_waitForVisibleThenFocusDialog()` waits until the new dialog is visible and focuses its first focusable element. **(Changed in 21.1.0)** Clipped (hidden) elements are skipped, so the focus no longer lands on the `sr-only` close button. |
| Focus return | `pushdialogfocus` / `popdialogfocus` keep a stack of trigger IDs (`o_info.focusReturnStack`). After the close, `o_ainvoke()` focuses `o_info.pendingFocusReturn` and scrolls it into view only when needed (`o_scrollToElementIfInvisible()`). See [Callout Triggers and Focus](#161-callout-triggers-and-focus-a11y). |
| Skip link | The first element of the page is `<a href="#o_main_container" class="sr-only">` "Skip to main content" (`fullwebapplayout.html`). |

### 36.2 ARIA Patterns

| Pattern | Markup | Where |
|---|---|---|
| Modal dialog | `<dialog class="dialog modal" aria-modal="true" aria-labelledby="o_md_{id}">` | `CloseableModalController`, `DialogBoxController`, dirty form dialog |
| Dialog opener (callout trigger) | `role="button" aria-haspopup="dialog" aria-expanded` (+ `aria-controls` on object selection) | `Link`/`FormLink.setAriaDialogOpener()`, **(Changed in 21.1.0)** action columns, table tools, filters |
| Disclosure | toggle with `role="button"`, `aria-expanded`, `aria-controls`; body with Bootstrap `collapse` | [Form Section](#131-form-section) and [Sections](#244-sections) (**New in 21.1.0**), `InfoPanel`, assistance accordion, `FormExpandButton` |
| Switch | `button role="switch" aria-checked` | `FormToggle` (`o_button_toggle`) |
| Toggle button | `button role="checkbox" aria-checked`; the hidden text has `aria-hidden="true"` | scopes (`o_toggle_button o_scope_toggle`) |
| Tabs | `role="tablist"`, `role="tab" aria-selected`, `role="tabpanel"` | `TabbedPane` |
| Tree | `ul role="tree"`, `li role="treeitem" aria-selected` | `MenuTree` |
| Search | `div role="search"`, `input role="searchbox" aria-label` | [Search Element](#132-search-element) (**New in 21.1.0**) |
| Landmarks | `role="navigation" aria-label` on `#o_navbar_wrapper` and `#o_offcanvas_right` | page layout |
| Images | user initials `role="img" aria-label="{name}"`; AI pulse SVG `role="img" aria-label="OpenOlat AI"`; decorative icons `aria-hidden="true"` | portraits, AI UI, all `o_icon` in links |

### 36.3 Live Regions

- **Status:** AI job overlays and messages use `role="status"` (question generation overlay `o_ai_questions_overlay` and essay correction overlay also `aria-live="polite"`).
- **Announcements:** the dashboard edit mode writes into `#o_dashboard_edit_live` (`sr-only`, `aria-live="polite" aria-atomic="true"`).
- **Errors:** the AI correction error box uses `role="alert"`.
- **Progress:** the AJAX busy dialog `#o_ajax_busy` contains a `role="progressbar"` with `aria-valuenow`, updated by `functions.js`.
- **Gap:** the info messages of `showInfoBox()` are prepended to `#o_messages`, which is not a live region. Screen readers do not announce them.

### 36.4 Keyboard Patterns

| Key | Behaviour |
|---|---|
| Enter / Space on `role="button"` links | `triggerClick(event, enter, space)` prevents the default action and clicks the element. `LinkRenderer` adds it for form links and links with the button role; Enter on a link inside a form never submits the form. |
| Escape | `o_doEscapeDispatch()` closes the top layer: lightbox, then the last layered panel, callout or modal (its close button). Ignored in selects and in the date picker. |
| Enter / Escape in search fields | Enter searches by AJAX instead of submitting the form; in the typeahead variant Escape resets (**New in 21.1.0**). |
| Enter / Space on section headers | Toggles the section (**New in 21.1.0**). |
| Space / Enter in the colour picker | Selects the colour and closes the dropdown. |

### 36.5 A11y Helpers in the JS Layer

| Function / command | Purpose |
|---|---|
| `o_ffSetFocus()`, `o_ffSetFocusArray()` | Programmatic focus after AJAX updates |
| `o_waitForVisibleThenFocusDialog()` | Initial focus in dialogs and callouts |
| `pushdialogfocus`, `popdialogfocus` | Focus return to the trigger |
| `setattribute` | Change one attribute (e.g. `aria-expanded`) without re-render |
| `o_scrollToElementIfInvisible()` | Scroll only when needed, also inside modal content |
| `triggerClick()` | Keyboard activation for link buttons |
| `o_doEscapeDispatch()` | Escape closes the top layer |
| section handler (`click.oSection keydown.oSection`) | Disclosure state for `.o_section_toggle` (**New in 21.1.0**) |

### 36.6 A11y Rules of the Theme

- **Contrast:** `o-a11y-color()` in `_functions.scss` adjusts text colours to 4.5:1 against their background (see [Accessibility Functions](#6-accessibility-functions)).
- **Screen-reader text:** `.sr-only` is forced to black on white (`_bootstrap.scss`) so contrast checkers do not report it; templates use `$r.screenreaderOnly("text")`.
- **Visible focus:** `.dropdown-toggle:focus-visible` uses the Bootstrap `tab-focus` mixin.
- **Reduced motion:** `prefers-reduced-motion` rules in 10 modules, for example ghost button transitions (`_helpers.scss`), the AI pulse (all animation off) and the wavy AI marks (solid).
- **Icons:** icon fonts are decorative; links render them with `aria-hidden="true"` and put the text in a `span` or `sr-only` span.

### 36.7 Known Gaps (verified 2026-09-25)

- Object selection: the button `aria-label` contains raw HTML from `IdentitySelectionSource`; `label for` points to a non-existent id; after the callout closes the focus lands on `body`.
- Colour picker: `ColorPickerRenderer` writes `aria-expanded='true'` also when the dropdown is closed, and omits the space before `onfocus`.
- `#o_messages` info boxes are not announced (no live region).
- AI essay marks carry the meaning by colour and `title` only.
- Question pool AI overlay: the classes `o_ai_questions_overlay` and `o_ai_questions_generating` have no rule in the light theme.

## 37. Changelog

### 37.1 21.1.0 (2026-09-25)

New components and mechanisms (commits on `master` that are not on `OpenOLAT_21.0`):

| Issue | Change | Section |
|---|---|---|
| OO-9756 | `FormSection` (`o_form_section`), GUI demo "Example settings" as settings page blueprint | [Form Section](#131-form-section) |
| OO-9627 | `SearchElement` (`o_search` with variants) in tables, navbar, overviews, object and tag selection | [Search Element](#132-search-element) |
| OO-9688 | `FactSheet` (`o_fact_sheet`), `ComponentList` (`o_component_list`) | [Fact Sheet](#243-fact-sheet) |
| OO-8728 | `Sections` (`o_sections`), info page redesign, ghost danger button | [Sections](#244-sections), [Buttons](#20-button-variants) |
| OO-9647 | Label icon (`FormItem.setLabelIconCss`), `.o_button_group.compact` | [Form Elements](#13-form-elements) |
| OO-9727 | Callout trigger ARIA pattern on buttons and action columns, focus on first visible element | [Callout Triggers](#161-callout-triggers-and-focus-a11y) |
| OO-9596 | `TranslateCellRenderer` | [Table Classes](#23-table-classes) |
| OO-9693 | Share component with `o_shareCopyLink()` | [AJAX Mechanism](#28-ajax-mechanism) |
| OO-9299 | Print layout of the info page | [Print Styles](#32-print-styles), [Info Page](#35-page-pattern-info-page) |

Documented for the first time (shipped in 21.0.x, no marker): button and input border tokens (OO-9547), callout ARIA disclosure pattern and focus return (OO-8877), object selection disabled state and "(me)" user (OO-9620, OO-9526, OO-9531), `RelativeDateElement` (OO-9304), AJAX form data as name/value pairs (OO-9795), `oo-docs.css` icon bundle, SCSS modules `_room_management`, `_ai_pulse`, `_ai_feedback`, `_selectus`, Leaflet.

Also new in this update: chapter 35 (info page pattern) and chapter 36 (accessibility); the Markdown version follows the same 37-chapter structure.

Corrections of earlier content: toolbar segment markup, modal markup (`o_layered_panel` + `<dialog class="dialog modal">`), FormToggle and date picker markup, widget type classes, JavaScript function names (`o_XHRFnCall`, `o_ffSetFormDirty` do not exist), `o_ce_` and `o_sel_` examples, `o_form_two_cols` breakpoint, `_config.scss` size and line numbers. Sections 31 to 34 bring content from the Markdown version that was missing here.
