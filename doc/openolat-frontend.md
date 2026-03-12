# OpenOlat Frontend Architecture Reference

## 1. Overview

OpenOlat uses a **server-centric** UI architecture. All state lives on the server; the browser receives HTML fragments via AJAX. There is no client-side framework (no React, Vue, or Angular). The frontend layer consists of:

- **Apache Velocity** templates (`.html` files in `_content/` directories) for HTML generation
- **Java ComponentRenderers** that generate HTML/DOM programmatically
- **Bootstrap 3.4.1** (SASS) as the CSS framework foundation
- **Custom SASS modules** (~100 files) in the `light` theme extending Bootstrap
- **Font Awesome 6** for icons
- **jQuery** for DOM manipulation and AJAX
- **Custom JavaScript** (`functions.js`) for the AJAX rendering pipeline

---

## 2. Theme Architecture

### 2.1 Directory Structure

```
src/main/webapp/static/
├── bootstrap/                       # Bootstrap 3.4.1 (SASS version)
│   ├── stylesheets/bootstrap/       # Bootstrap SCSS partials
│   │   ├── _variables.scss          # Bootstrap defaults
│   │   ├── _mixins.scss
│   │   ├── _grid.scss
│   │   └── ... (40+ partials)
│   └── OO_bootstrap_version         # "3.4.1"
│
├── themes/
│   ├── themes.README                # Theming guide
│   ├── compiletheme.sh              # Dart Sass compile script
│   │
│   ├── light/                       # BASE THEME (do not modify)
│   │   ├── theme.scss               # Main entry point → theme.css
│   │   ├── content.scss             # iFrame content → content.css
│   │   ├── email.scss               # Email styles → email.css
│   │   ├── _config.scss             # ALL variables (1500+ lines)
│   │   ├── _functions.scss          # a11y functions: luminance, contrast
│   │   ├── _modules.scss            # Import manifest (100 modules)
│   │   ├── _patches.scss            # Post-import fixes
│   │   ├── modules/                 # ~100 SCSS partial files
│   │   │   ├── _bootstrap.scss      # Bootstrap imports + overrides
│   │   │   ├── _mixins.scss         # OpenOlat SASS mixins
│   │   │   ├── _helpers.scss        # Utility classes (o_block, o_nowrap, etc.)
│   │   │   ├── _layout.scss         # html/body base
│   │   │   ├── _main.scss           # #o_main grid layout
│   │   │   ├── _navbar.scss         # Top navigation bar
│   │   │   ├── _toolbar.scss        # Breadcrumb + tools bar
│   │   │   ├── _tree.scss           # Menu tree navigation
│   │   │   ├── _form.scss           # Form elements
│   │   │   ├── _table.scss          # FlexiTable styles
│   │   │   ├── _dialog.scss         # Modal dialogs
│   │   │   ├── _icons.scss          # Icon definitions
│   │   │   ├── _fonts.scss          # Font imports
│   │   │   ├── _contenteditor.scss  # Page/Content editor
│   │   │   └── ... (85+ more)
│   │   ├── styles/                  # Optional style variants
│   │   │   ├── _square_config.scss  # No border-radius variant
│   │   │   ├── _square_theme.scss
│   │   │   └── _realistic.scss      # Gradient buttons variant
│   │   ├── images/
│   │   ├── fonts/
│   │   └── meta/                    # Favicon, app icons, manifest
│   │
│   └── openolat/                    # DEFAULT DERIVED THEME
│       ├── theme.scss               # Imports: fonts → config → light/theme → overrides
│       ├── _openolat_config.scss    # Variable overrides ($brand-primary, colors, fonts)
│       ├── _openolat_theme.scss     # CSS rule overrides (logo, login page)
│       ├── _openolat_content.scss   # Content CSS overrides
│       ├── _openolat_email.scss     # Email CSS overrides
│       ├── fonts/                   # Custom fonts (Roboto)
│       ├── images/                  # Logos, backgrounds
│       └── theme.js                 # Theme-specific JavaScript
```

### 2.2 Compilation Pipeline

```
theme.scss (entry point)
  ├── @import "openolat_config"     ← Variable overrides (BEFORE light)
  ├── @import "light/theme"          ← Base theme
  │     ├── @import "functions"      ← luminance(), contrast(), o-a11y-color()
  │     ├── @import "config"         ← 1500+ variables with !default
  │     │     └── @import "bootstrap/variables"  ← Bootstrap defaults
  │     ├── @import "modules"        ← 100 module files
  │     │     ├── @import "modules/bootstrap"  ← Bootstrap core CSS
  │     │     ├── @import "modules/mixins"
  │     │     ├── @import "modules/icons"
  │     │     ├── @import "modules/helpers"
  │     │     ├── @import "modules/layout"
  │     │     ├── @import "modules/main"
  │     │     ├── @import "modules/navbar"
  │     │     ├── @import "modules/toolbar"
  │     │     ├── @import "modules/form"
  │     │     ├── @import "modules/table"
  │     │     ├── @import "modules/tree"
  │     │     └── ... (90+ more modules)
  │     └── @import "patches"
  └── @import "openolat_theme"      ← CSS rule overrides (AFTER light)

Compile: sass --style compressed theme.scss → theme.css
```

**Key insight**: Variables with `!default` in `_config.scss` are only set if not already defined. Custom themes import their config BEFORE `light/theme`, so their values take precedence.

### 2.3 Three CSS Output Files

| File | Purpose | Included Where |
|------|---------|---------------|
| `theme.css` | Main application styles | Every OpenOlat page |
| `content.css` | Learning content in iFrames | Single pages, SCORM, CP iframes |
| `email.css` | Email notification styles | Inlined in email HTML |

### 2.4 Variable Naming Convention

All OpenOlat-specific variables use the `$o-` prefix:

```scss
// Bootstrap variables (no prefix)
$brand-primary           : #337ab7;
$font-size-base          : 14px;
$btn-default-bg          : #f8f8f8;

// OpenOlat variables ($o- prefix)
$o-navbar-height         : $navbar-height;
$o-toolbar-bg-color      : $navbar-default-bg;
$o-tree-link-color       : $navbar-default-link-color;
$o-table-row-selected-color : lighten($state-info-bg, 5%);
```

Variable categories in `_config.scss`:
- **Bootstrap overrides** (lines 34-65): btn, input, pagination, a11y
- **Color system** (lines 67-133): `$o-labeled-{color}-{variant}` for 8 colors × 3 variants
- **Brand colors** (lines 135-155): info, warning, danger, success + custom
- **Layout** (lines 252-266): page width, main area backgrounds
- **Navbar** (lines 268-326): height, colors, links, toggle
- **Toolbar** (lines 359-403): breadcrumb, tools, navigation
- **Form** (lines 405-424): radio cards, date pickers
- **Tree** (lines 426-454): menu tree, indentation, DnD
- **Module-specific** (lines 456-1500): course, QTI, portfolio, calendar, etc.

### 2.5 Accessibility Functions

`_functions.scss` provides built-in WCAG contrast checking:

```scss
// Calculate relative luminance (0=black, 1=white)
@function luminance($color) { ... }

// Calculate contrast ratio between two colors
@function contrast($color1, $color2) { ... }

// Auto-adjust color for WCAG 4.5:1 contrast ratio
@function o-a11y-color($color, $background, $targetRatio: 4.5) { ... }
```

Used throughout `_config.scss`:
```scss
$btn-primary-color: o-a11y-color(#fff, $btn-primary-bg);
$o-toolbar-breadcrumb-link-color: o-a11y-color($link-color, $o-toolbar-breadcrumb-bg-color);

// Dynamic contrast adjustment
@if contrast($brand-primary, #FFF) < 4.5 {
  $o-brand-box-border: darken($brand-primary, 20%);
}
```

### 2.6 Custom Theme Creation Pattern

From `themes.README` and observed client themes:

```
1. Copy openolat/ → mytheme/
2. Rename files: _openolat_* → _mytheme_*
3. Update imports in theme.scss, content.scss, email.scss
4. Override variables in _mytheme_config.scss:
   - $brand-primary          (main brand color)
   - $text-color, $link-color
   - $headings-color
   - $font-family-base
   - $o-navbar-* variables
   - $o-footer-* variables
   - $o-login-form-bg-img
   - $o-coursesite-start-bg
5. Override CSS rules in _mytheme_theme.scss:
   - .o_navbar-brand (logo)
   - .o_login_logo
   - #o_body.o_dmz (login page layout)
6. Compile: ./compiletheme.sh mytheme
```

Typical client theme overrides **20-50 variables** and **50-150 lines of CSS rules** (mostly logo/branding).

---

## 3. Page DOM Structure

### 3.1 Overall Page Skeleton

```html
<html>
<head>
  <link rel="stylesheet" href="/static/themes/{theme}/theme.css">
  <script src="/static/js/functions.js"></script>
</head>
<body id="o_body" class="[o_dmz]">

  <!-- Navbar Wrapper -->
  <div id="o_navbar_wrapper">
    <div id="o_navbar_container">
      <div class="o_navbar">
        <!-- Brand logo -->
        <a class="o_navbar-brand" href="..."></a>
        <!-- Site tabs (Home, Courses, Groups, etc.) -->
        <ul class="o_navbar-nav o_navbar_tabs">
          <li><a href="..."><span>Home</span></a></li>
          <li class="active"><a href="..."><span>Courses</span></a></li>
        </ul>
        <!-- Right-side tools (search, profile, logout) -->
        <ul class="o_navbar-nav o_navbar_tools">
          <li id="o_navbar_search">...</li>
          <li id="o_navbar_my_menu">...</li>
          <li id="o_navbar_logout">...</li>
        </ul>
      </div>
    </div>
  </div>

  <!-- Main Content Area -->
  <div id="o_main_wrapper">
    <div id="o_main_container">
      <div id="o_main" class="[o_hide_main_left] [o_hide_main_right]">
        <!-- 3-column CSS Grid layout -->
        <div id="o_main_left">
          <div id="o_main_left_content">
            <!-- Tree menu / navigation -->
          </div>
        </div>
        <div id="o_main_center">
          <div id="o_main_center_content">
            <div id="o_main_center_content_inner">
              <!-- Toolbar (breadcrumb + tools) -->
              <!-- Active controller content -->
            </div>
          </div>
        </div>
        <div id="o_main_right">
          <div id="o_main_right_content">
            <!-- Optional right sidebar -->
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- Footer -->
  <div id="o_footer_wrapper">
    <div id="o_footer_container">
      <!-- Copyright, version, links -->
    </div>
  </div>

</body>
</html>
```

### 3.2 CSS Grid Layout

The `#o_main` uses CSS Grid for the 3-column layout:

```scss
#o_main {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto;
  column-gap: floor($grid-gutter-width * 0.5);

  &.o_hide_main_left  { grid-template-columns: minmax(0, 1fr) auto; }
  &.o_hide_main_right { grid-template-columns: auto minmax(0, 1fr); }
  &.o_hide_main_left.o_hide_main_right { grid-template-columns: minmax(0, 1fr); }
}
```

### 3.3 Toolbar DOM Structure

```html
<div class="o_toolbar [o_toolbar_with_segments]">
  <!-- Breadcrumb -->
  <div class="o_breadcrumb">
    <ol class="breadcrumb">
      <li class="o_breadcrumb_back"><a>← Back</a></li>
      <li class="o_breadcrumb_root"><a>◆</a></li>
      <li class="o_breadcrumb_crumb"><a>Level 1</a></li>
      <li class="o_breadcrumb_crumb o_last_crumb" aria-current="true">Current</li>
      <li class="o_breadcrumb_close"><a>✕</a></li>
    </ol>
  </div>

  <!-- Tools Container -->
  <div class="o_tools_container">
    <ul class="o_tools o_tools_left list-inline">
      <li class="o_tool"><a class="btn btn-sm">Action 1</a></li>
    </ul>
    <ul class="o_tools o_tools_center list-inline">...</ul>
    <ul class="o_tools o_tools_right list-inline">...</ul>
    <ul class="o_tools o_tools_right_edge list-inline">...</ul>

    <!-- Segment buttons (tab-like) -->
    <ul class="o_tools o_tools_segments list-inline">
      <li><a class="o_segment_button active">Tab 1</a></li>
      <li><a class="o_segment_button">Tab 2</a></li>
    </ul>
  </div>
</div>
```

---

## 4. Component Rendering System

### 4.1 Rendering Pipeline

```
User Action → AJAX POST → Server Dispatch
  → Controller.event() → Business Logic → Mark dirty
  → RENDER PHASE:
    Window → Component Tree traversal
      → ComponentRenderer.render(StringOutput, Component, args)
        → HTML string appended to StringOutput
    → JSON response: {action: "updcmp", id: "o_c12345", content: "..."}
  → Client: jQuery replaces DOM element by ID
```

### 4.2 Component ID Convention

Every rendered component gets a unique DOM ID: `o_c{dispatchID}`

```html
<div id="o_c12345">...</div>  <!-- Window component -->
<a id="o_c12346">...</a>      <!-- Link component -->
<form id="o_c12347">...</form> <!-- Form component -->
```

### 4.3 Key Renderers and Their DOM Output

#### VelocityContainerRenderer
Processes `.html` Velocity templates. Templates use `$r.render("componentName")` to include child components.

#### PanelRenderer / LayeredPanelRenderer
Renders stacked/layered content. LayeredPanel adds `o_layer_0`, `o_layer_1` classes for modal z-indexing.

#### LinkRenderer
```html
<a id="o_c{id}" href="javascript:;" onclick="o_XHREvent(...);" class="[btn btn-default] [o_disabled]">
  <i class="o_icon o_icon_{name}"></i> <span>Link Text</span>
</a>
```

#### Form Element Renderers

| Element | Renderer | DOM Structure |
|---------|----------|---------------|
| TextElement | TextElementRenderer | `<input type="text" class="form-control">` in `<div class="form-group">` |
| TextAreaElement | TextAreaElementRenderer | `<textarea class="form-control">` |
| SelectboxElement | SelectboxRenderer | `<select class="form-control">` |
| SingleSelection (radio) | RadioElementRenderer | `<div class="radio"><label><input type="radio">...</label></div>` |
| MultipleSelection (checkbox) | CheckboxElementRenderer | `<div class="checkbox"><label><input type="checkbox">...</label></div>` |
| DateChooser | DateChooserRenderer | `<div class="o_date"><input class="form-control o_date_picker">` + calendar popup |
| RichTextElement | RichTextElementRenderer | TinyMCE `<textarea>` wrapper |
| FileElement | FileElementRenderer | `<input type="file">` with upload zone |
| FormToggle | FormToggleRenderer | `<div class="o_toggle">` slider switch |
| StaticTextElement | StaticTextElementRenderer | `<span class="form-control-static">` |
| SpacerElement | SpacerElementRenderer | `<hr class="o_spacer">` |
| FormLink | FormLinkRenderer | `<a class="btn btn-default">` |
| FormSubmit | FormSubmitRenderer | `<button type="submit" class="btn btn-primary">` |

#### FlexiTable Renderers

```html
<div class="o_table_wrapper o_table_flexi">
  <!-- Search/filter bar -->
  <div class="o_table_toolbar">
    <div class="o_table_search form-inline">...</div>
    <!-- Filter tabs -->
    <ul class="o_table_tabs nav nav-tabs">...</ul>
  </div>

  <!-- Scrollable table wrapper -->
  <div class="o_scrollable_wrapper">
    <div class="o_scrollable">
      <table class="table [table-striped] [table-condensed] [table-bordered]">
        <thead>
          <tr>
            <th class="o_col_sticky_left o_multiselect"><!-- select-all --></th>
            <th><a>Column Name <i class="o_icon o_icon_sort_asc"></i></a></th>
          </tr>
        </thead>
        <tbody>
          <tr class="o_table_row_selector [o_row_selected]">
            <td class="o_multiselect"><input type="checkbox"></td>
            <td>Cell content</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>

  <!-- Footer: pagination + row count -->
  <div class="o_table_footer">
    <div class="o_table_rows_infos">Showing 1-25 of 100</div>
    <div class="o_table_pagination">
      <ul class="pagination">...</ul>
    </div>
  </div>
</div>
```

#### Tree (Menu Navigation)
```html
<div class="o_tree">
  <ul>
    <li>
      <div>
        <a class="o_tree_oc_l0">▶</a>           <!-- opener -->
        <span class="o_tree_l0 o_tree_link">     <!-- tree node -->
          <a href="...">
            <i class="o_icon o_icon_node"></i>
            <span>Node Title</span>
          </a>
        </span>
      </div>
      <ul><!-- children, indented by level class o_tree_l1, o_tree_l2, etc. --></ul>
    </li>
  </ul>
</div>
```

#### TabbedPane
```html
<div class="o_tabbed_pane">
  <ul role="tablist" class="nav nav-tabs">
    <li class="active"><a role="tab" aria-selected="true">Tab 1</a></li>
    <li><a role="tab" aria-selected="false">Tab 2</a></li>
  </ul>
  <div role="tabpanel" class="o_tabbed_pane_content">
    <!-- Active tab content -->
  </div>
</div>
```

#### Modal Dialog (via LayeredPanel)
```html
<div class="o_modal_wrapper o_layer_0">
  <dialog class="modal-dialog [modal-lg]">
    <div class="modal-content">
      <div class="modal-header">
        <button class="close">×</button>
        <h4>Dialog Title</h4>
      </div>
      <div class="modal-body [alert]">
        <!-- Dialog content -->
      </div>
      <div class="modal-footer">
        <button class="btn btn-default">Cancel</button>
        <button class="btn btn-primary">OK</button>
      </div>
    </div>
  </dialog>
</div>
```

---

## 5. CSS Class Reference

### 5.1 CSS Class Naming Convention

| Prefix | Origin | Example |
|--------|--------|---------|
| (none) | Bootstrap 3 | `btn`, `form-control`, `table`, `nav-tabs` |
| `o_` | OpenOlat core | `o_toolbar`, `o_tree`, `o_table_wrapper` |
| `o_icon_` | Icon classes | `o_icon_delete`, `o_icon_edit` |
| `fa-` | Font Awesome | `fa-check`, `fa-times` |

### 5.2 Layout & Spacing Classes

```scss
// Block spacing
.o_block          { margin: 1em 0; }
.o_block_small    { margin: 0.5em 0; }
.o_block_large    { margin: 2em 0; }
.o_block_top      { margin-top: 1em; }
.o_block_bottom   { margin-bottom: 1em; }

// Inline blocks
.o_block_inline       { display: inline-block; vertical-align: top; }
.o_block_inline_left  { + margin-left: 0.5em; }
.o_block_inline_right { + margin-right: 0.5em; }

// Flex helpers
.o_flex_block_one_line_left  { display: flex; flex-wrap: nowrap; width: 100%; }
.o_flex_item_fix  { flex-grow: 0; flex-shrink: 0; }
.o_flex_item_max  { flex-grow: 1; overflow: hidden; text-overflow: ellipsis; }
.o_flex_first_grow { display: flex; > div:first-child { margin-right: auto; } }

// Scrollable
.o_scrollblock     { overflow-x: auto; overflow-y: hidden; }
.o_scrollable      { width: 100%; overflow-x: auto; overflow-y: hidden; }
.o_scrollable_wrapper { overflow: hidden; position: relative; } // with shadow indicators
```

### 5.3 Typography Classes

```scss
.o_xsmall  { font-size: ceil($font-size-small * .9); }
.o_small   { font-size: $font-size-small; }
.o_large   { font-size: $font-size-large; }
.o_xlarge  { font-size: ceil($font-size-large * 1.1); }

.o_disabled    { color: $text-muted; cursor: default; text-decoration: none; }
.o_dimmed      { opacity: 0.4; }
.o_muted       { color: $text-muted; }
.o_selected    { font-weight: bold; }
.o_deleted     { text-decoration: line-through; }
.o_nowrap      { white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.o_undecorated { &:hover,&:focus { text-decoration: none; } }
.o_clickable   { cursor: pointer; }
.o_hidden      { visibility: hidden; }
```

### 5.4 Button Variants

```scss
// Standard Bootstrap
.btn.btn-default   // Default button
.btn.btn-primary   // Primary action
.btn.btn-danger    // Destructive action
.btn.btn-xs/sm/lg  // Size variants

// OpenOlat custom buttons
.btn.o_button_ghost         // Transparent background, link-colored
.btn.o_button_mega          // Large button with icon + title + subtitle
.btn.o_button_primary_light // Primary outline variant
.btn.o_button_call_to_action // Large, wide CTA button
.btn.o_button_dirty         // Warning-colored (unsaved changes)

// Button groups
.o_button_group       { text-align: center; }
.o_button_group_left  { text-align: left; }
.o_button_group_right { text-align: right; }

// Button with adjacent header
.o_header_with_buttons { h2 { display: inline-block; } .o_button_group { float: right; } }
```

### 5.5 Message Boxes

```scss
// Full message boxes (via @mixin o-make-message-box)
.o_info      { border: 1px solid $o-color-info;    background: $o-color-info-text-bg; }
.o_note      { border: 1px solid $o-color-note;    background: $o-color-note-text-bg; }
.o_tip       { border: 1px solid $o-color-tip;     background: $o-color-tip-text-bg; }
.o_important { border: 1px solid $o-color-important; background: $o-color-important-text-bg; }
.o_success   { border: 1px solid $o-color-success; background: $o-color-success-text-bg; }
.o_warning   { border: 1px solid $o-color-warning; background: $o-color-warning-text-bg; }
.o_error     { border: 1px solid $o-color-error;   background: $o-color-error-text-bg; }

// Inline message lines
.o_warning_line  { background: $o-labeled-orange-mega-bg-color; color: $o-labeled-orange-mega-color; }
.o_error_line    { background: $o-labeled-red-mega-bg-color; color: $o-labeled-red-mega-color; }
```

### 5.6 Labeled Color System

OpenOlat defines a semantic color system with 8 base colors, each having 3 variants:

| Color | Base | Solid (labeled) | Light (outline) | Mega (tinted bg) |
|-------|------|-----------------|-----------------|-------------------|
| Blue | `#105CAD` | White on `#146DCC` | Blue on white, blue border | Blue on `#cde3f9` |
| Green | `#268000` | White on `#37AD00` | Green on white, green border | Green on `#C4E6B8` |
| Yellow | | `#574000` on `#FBD774` | `#574000` on white | `#805e00` on `#fbe6a7` |
| Orange | `#D17A00` | `#663B00` on `#F4AC47` | Orange on white | `#663B00` on `#FFDAA6` |
| Red | | White on `#b30018` | Red on white | Red on `#ffc9bd` |
| Brown | | White on `#804A33` | Brown on white | Brown on `#E7D2BC` |
| Grey | | White on `#595959` | Grey on white | `#342c24` on `#F6F6F6` |

Variables: `$o-labeled-{color}-{variant}-{property}` where variant = (none)/light/mega and property = color/bg-color/border-color.

---

## 6. Key SCSS Modules Reference

### 6.1 Core Modules

| Module | File | Purpose |
|--------|------|---------|
| Bootstrap | `_bootstrap.scss` | Bootstrap 3.4.1 imports + overrides (tooltips, modals, dropdowns, a11y fixes) |
| Mixins | `_mixins.scss` | `o-make-message-box`, `o-make-icon`, `o-add-icon`, `o-add-button-hover`, etc. |
| Icons | `_icons.scss` | Font Awesome 6 icon definitions, `o_icon` base class |
| Helpers | `_helpers.scss` | Spacing, flex, scrolling, buttons, panels, typography, DnD |
| Type | `_type.scss` | Typography and heading styles |

### 6.2 Layout Modules

| Module | File | Purpose |
|--------|------|---------|
| Layout | `_layout.scss` | `html`/`body` base, footer margin |
| Main | `_main.scss` | `#o_main_wrapper` / `#o_main` CSS Grid, left/center/right columns |
| Navbar | `_navbar.scss` | `#o_navbar_wrapper`, site tabs, brand, tools |
| Toolbar | `_toolbar.scss` | `.o_toolbar`, breadcrumb, tools alignment, segments |
| Footer | `_footer.scss` | `#o_footer_wrapper`, social links |
| Offcanvas | `_offcanvas.scss` | Right-side mobile menu |

### 6.3 Component Modules

| Module | File | Purpose |
|--------|------|---------|
| Form | `_form.scss` | Form layouts, date pickers, radio cards, toggles, sliders |
| Table | `_table.scss` | FlexiTable, filters, pagination, sticky columns, DnD |
| Tree | `_tree.scss` | Menu tree, indentation levels, DnD, badges, course status |
| Dialog | `_dialog.scss` | Bootstrap modal overrides, floating windows |
| Breadcrumb | `_breadcrumb.scss` | Toolbar breadcrumb styling |
| Wizard | `_wizard.scss` | Multi-step wizard progress |
| Card | `_card.scss` | Card layouts for catalog, media browser |
| Bento | `_bento.scss` | Bento grid layout |
| Autocomplete | `_autocomplete.scss` | Auto-complete input fields |

### 6.4 Feature Modules (selection)

| Module | File | Purpose |
|--------|------|---------|
| Content Editor | `_contenteditor.scss` | Page builder, drag-drop, inspector panel |
| Course | `_course.scss` | Course node styles, learning path, assessment status |
| QTI | `_qti21.scss` | Assessment interactions, review, scoring |
| Calendar | `_cal.scss` | FullCalendar integration |
| Forum | `_forum.scss` | Forum messages, quotes, attachments |
| Portfolio | `_portfolio_v2.scss` | Portfolio entries, binders, media |
| Repository | `_repository.scss` | Course catalog, access control |
| DMZ | `_dmz.scss` | Login/registration page (de-militarized zone) |

---

## 7. AJAX Rendering Mechanism

### 7.1 Request/Response Cycle

1. **User action** → JavaScript captures event (click, submit, change)
2. **XHR POST** → `o_XHREvent()` or form submit to server
3. **Server dispatch** → Window finds target component by ID
4. **Controller event** → Business logic, state updates, `component.setDirty(true)`
5. **Render phase** → Only dirty components re-rendered
6. **JSON response** → Array of commands: `[{action: "updcmp", id: "o_c12345", content: "..."}]`
7. **DOM update** → `jQuery('#o_c12345').replaceWith(newHTML)`

### 7.2 Polling

Background AJAX polling for server-push updates:

```javascript
jQuery.periodic({period: 5000, decay: 1.005}, function() {
  jQuery.ajax({
    method: 'POST', url: '/olat/auth/ajax',
    data: {_csrf: token, oow: window.name},
    success: function(commands) { o_ainvoke(commands); }
  });
});
```

### 7.3 Component Dirty Marking

```java
// Only changed components are re-rendered
if (itemDeleted) {
  tableComponent.setDirty(true);   // Re-render table
  messagePanel.setDirty(true);     // Show success message
  // Navbar, toolbar, tree stay clean → not re-rendered
}
```

---

## 8. Responsive Design

OpenOlat uses Bootstrap 3 breakpoints:

| Breakpoint | Variable | Width |
|-----------|----------|-------|
| xs (phone) | `$screen-xs-max` | < 768px |
| sm (tablet) | `$screen-sm-max` | < 992px |
| md (desktop) | `$screen-md-max` | < 1200px |
| lg (large) | | >= 1200px |

Key responsive behaviors:
- **Navbar**: Collapses to hamburger menu on xs/sm
- **Main layout**: Left column collapses to offcanvas drawer on xs
- **Tables**: Horizontal scroll via `o_scrollable_wrapper` with shadow indicators
- **Toolbar**: Tool icons shrink, text hides on xs
- **Forms**: Full-width inputs on xs
- **Cards/Grid**: Column count reduces on smaller screens

---

## 9. Renderer Statistics

### 9.1 Renderer Count by Category

| Category | Count | Key Renderers |
|----------|-------|---------------|
| Form elements | 24 | TextElementRenderer, SelectboxRenderer, CheckboxElementRenderer |
| FlexiTable | 9 | FlexiTableClassicRenderer, FlexiFilterTabsComponentRenderer |
| Panels/Containers | 13 | PanelRenderer, IconPanelRenderer, VelocityContainerRenderer |
| Navigation | 10 | TabbedPaneRenderer, DropdownRenderer, MenuTreeRenderer |
| Widgets | 4 | WidgetRenderer, TextWidgetRenderer, FigureWidgetRenderer |
| Media/Files | 4 | ImageRenderer, DownloadComponentRenderer |
| Charts | 7 | BarChartComponentRenderer, ProgressBarRenderer |
| Assessment/QTI | 10 | AssessmentTestComponentRenderer |
| Content Editor | 6 | ContentEditorComponentRenderer |
| Specialized Cell | 250+ | PassedCellRenderer, CompletionRenderer, GradeCellRenderer |
| Other | 200+ | Curriculum, Portfolio, Badge, Lecture renderers |
| **Total** | **~540** | |

---

## 10. Key CSS IDs and Their Roles

| ID | Element | Purpose |
|----|---------|---------|
| `#o_body` | `<body>` | Root body, gets `.o_dmz` on login page |
| `#o_navbar_wrapper` | `<div>` | Top navigation bar container |
| `#o_navbar_container` | `<div>` | Navbar inner container |
| `#o_main_wrapper` | `<div>` | Main content outer wrapper |
| `#o_main_container` | `<div>` | Main content inner container |
| `#o_main` | `<div>` | CSS Grid 3-column layout |
| `#o_main_left` | `<div>` | Left column (tree menu) |
| `#o_main_center` | `<div>` | Center column (content) |
| `#o_main_right` | `<div>` | Right column (sidebar) |
| `#o_main_center_content_inner` | `<div>` | Content area inner wrapper |
| `#o_footer_wrapper` | `<div>` | Footer container |
| `#o_footer_container` | `<div>` | Footer inner container |
| `#o_toplink` | `<a>` | Scroll-to-top link |
| `#o_bg` | `<div>` | Background image (login page) |

---

## 11. Third-Party Libraries

| Library | Version | Purpose | CSS Impact |
|---------|---------|---------|------------|
| Bootstrap | 3.4.1 | CSS framework | Grid, buttons, forms, navs, modals |
| Font Awesome | 6 | Icon set | `fa-*` / `o_icon_*` classes |
| jQuery | 3.x | DOM manipulation | (no CSS) |
| TinyMCE | 6 | Rich text editor | Own CSS, themed via `_tinymce.scss` |
| FullCalendar | 6 | Calendar widget | `fc-*` classes, themed via `_cal.scss` |
| jQuery UI | 1.x | Datepicker, sortable | `ui-*` classes, themed via `_thirdparty.scss` |
| MathLive | | Math input | Themed via `_mathlive.scss` |
| Chart.js | 4.x | Charts | `<canvas>` rendering, minimal CSS |

---

## 12. Print Styles

`_print.scss` defines `@media print` rules:
- Hides navbar, footer, toolbar tools, tree menu
- Shows breadcrumb as text trail
- Forces white backgrounds
- Avoids page breaks in tables and forms
- Adds print-specific header with logo via `#o_print_brand`
