# OpenOlat Frontend Architecture Reference

**Release:** OpenOlat 21.1.0 | **Last updated:** 2026-11-08 | **Companion pages:** `doc/openolat-frontend.html` (visual version with diagrams), `doc/component-library/index.html` (live component examples)

This Markdown file and the HTML version have the same 37 chapters with the same numbering and headings. The HTML version adds the SVG diagrams; the Markdown names them with their caption.

Markers: `**New in 21.1.0**` under a heading and `**New in 21.1.0:**` / `**Changed in 21.1.0:**` at the start of a paragraph flag elements and behaviour that came with release 21.1.0. Changes that shipped in 21.0.x have no marker. `**Changed in 21.2.0:**` flags the focus changes of OO-9603 and the colour changes of the epic OO-9989 (development of 21.2.0). Chapter 37 lists all changes of this update.

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

`compiletheme.sh` compiles every non-partial `.scss` file in a theme folder, so `oo-docs.css` is built together with `theme.css` for `light`. `oo-docs.scss` imports `functions`, `palette`, `config`, `palette_theme`, `palette_neutral`, `modules/mixins`, `modules/icons`, `modules/palette_vars` and `modules/tokens` (`_icons.scss` reads tokens), and loads the fonts from a sibling `fonts/` folder.

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

Compile: `./compiletheme.sh mytheme` (requires Dart Sass ≥ 1.79.0)

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
| Status colours | none | `$o-states` in `_palette.scss`, `$o-state-wiring` in `modules/_states.scss` (chapter 26) |
| Brand colors | 73–96 | `$brand-info`, `$brand-warning`, `$brand-danger` |
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

Button and field borders use two tokens: `$btn-default-border` (`#6c757d`) and `$btn-default-hover-border` (`#667085`) for buttons and dropdown toggles, `$input-border` (`#d0d5dd`) for text fields, radio cards, tag selection and the toolbar nav. Ghost buttons use the link colour `var(--o-link)` with a transparent hover background. `$o-font-size-section-title` (about 18px) sets the size of section headings.

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

// The background with the lowest luminance: a colour that must keep its contrast
// on all of them is adjusted against it (OO-10043, D26)
@function o-darkest-color($colors...) { ... }

// The fill of a state: $bg darker (or lighter) by $amount; a fill that cannot
// get darker (black) gets lighter by the same amount (OO-10043, D20)
@function o-contrast-shade($bg, $amount, $function: "darken") { ... }

// Largest amount from $min up to $max (steps of 1%) at which $fg keeps $ratio
// on the darker (or lighter) $bg; used for hover and pressed fills (OO-10043, D9, D13)
@function o-contrast-shade-amount($fg, $bg, $max, $function: "darken", $ratio: 4.5, $min: 3%) { ... }

// The text of a state on its fill: $fg, or $fg adjusted for the fill (D13)
@function o-contrast-state-text($fg, $bg, $state-bg, $ratio: 4.5) { ... }
// o-contrast-state-text() on the fill of o-contrast-shade-amount()
@function o-contrast-shade-text($fg, $bg, $max, $function: "darken", $ratio: 4.5, $state-bg: null) { ... }

// Usage in the shade tokens and the text tokens of the states:
"btn-primary-light-bg-active": ("btn-primary-light-bg", o-contrast-shade-amount($o-button-primary-light-color, $o-button-primary-light-bg, 17%)),
"btn-primary-light-fg-active": (o-contrast-shade-text($o-button-primary-light-color, $o-button-primary-light-bg, 17%), ("btn-primary-light-fg",)),
```

`o-a11y-color()` changes the lightness of a colour in steps of 1% until the pair has the ratio, at most 100 steps. When it does not reach the ratio, it returns a colour outside the range, e.g. `hsl(0, 0%, -48%)`. A browser shows such a colour with each channel at its limit (black or white). `o-hex()` of `_palette.scss` writes the same limit into the token. `o-token-literal()` of `_tokens.scss` does the same for a colour with transparency.

`o-contrast-shade-amount()` returns `$max` in three cases: the text passes at `$max` (no change), the text already fails on `$bg` (a shade cannot repair the normal state), or no amount from `$min` (3%) passes. A fill less than 3% darker or lighter does not show the state (`#FCFCFC` on white), so the state keeps the amount of Bootstrap and its text changes (decision D13). 3% is the smallest amount of the default themes (`#F7F7F7`, light danger button).

`o-contrast-state-text()` returns `$fg` when it passes on the fill of the state, and when it fails on the fill of the normal state. Else it returns `o-a11y-color($fg, $state-bg)`. When `o-a11y-color()` cannot reach the ratio (dark text on a fill that gets darker), it returns white or black (`o-contrast-text()`). The adjusted text passes also as rounded hex value on the rounded fill. `o-contrast-shade-text()` applies it to the fill of `o-contrast-shade-amount()`. With `$state-bg` (a fill that a theme sets with a Sass variable, e.g. `$o-scope-toggle-on-bg-hover-color`), it changes the text only when the fill is the computed one.

### 6.1 Colour Palette for User Colour Choosers

Every colour a user can pick (calendar, course element category, video markers, project avatar, user initials, rooms, page editor blocks, badges, evaluation form report series) comes from one palette in `light/_palette.scss`.

```
$o-palette      14 hues × 10 steps: red, orange, yellow, lime, green, teal, cyan,
                blue, violet, magenta, pink, brown, slate, gray; steps 50 … 900,
                plus the theme colours theme1 … themeN (_palette_theme.scss)
$o-neutral      the neutral scale, 18 greys: steps 0 (white) … 1000 (black)
                (_palette_neutral.scss), no hue, no swatch, not in the choosers
$o-swatches     per hue: bg, fg, border, soft-bg, soft-fg, soft-border, text,
                tint-* and dark-* variants; fg chosen by o-on-color() at compile time,
                the compile fails below 4.5:1
.o_swatch_{hue} sets --o-sw-bg, --o-sw-fg, --o-sw-border, … (modules/_swatches.scss)
--o-{hue}-{step} every palette value on :root in theme.css and content.css
                (modules/_palette_vars.scss), not in email.css
--o-neutral-{step} every neutral step, same files
```

Rules:

- Chooser components read `var(--o-sw-*)`. They never carry a hex literal.
- Stored legacy keys and classes (`o_color_lightblue`, `o_cal_blue`, `o_colcat_light_blue`, ...) stay valid: the alias map in `_palette.scss` extends them from the swatch classes, and `ColorService.resolve()` maps them in Java.
- Java reads the palette from the compiled CSS. `Theme.init()` parses the `--o-{hue}-{step}` properties of the selected theme's `theme.css`, with fallback to `light/theme.css`, in the same way it loads `email.css`. `ColorService.getPalette()` returns the palette of the current GUI theme. There is no generated palette file, and `compiletheme.sh` writes nothing but CSS.
- A client theme may override the values of `$o-palette` (declared `!default`). It must keep all 14 hue names, the alias tables need them. Its `theme.css` then carries the client values, and badges and report series follow.
- **Theme colours:** `light/_palette_theme.scss` (imported after `_config.scss`) generates 10 steps for each colour of `$o-theme-colors` (default `($brand-primary)`), in OKLCH with the luminance targets of the palette. The brand colour itself is the solid fill when a text colour reaches 4.5:1 on it, never lighter than step 300; otherwise the generated step 500 (400 for a light colour). Output: `--o-theme{n}-{step}`, `--o-theme{n}` (the solid fill), `.o_swatch_theme{n}`. Java: `Palette.getThemeColors()` / `ColorService.getThemeColors()` return only the colours the theme defines (`theme1` up to the first gap); `resolve("theme3")` falls back to `theme1`. The openolat theme defines 4 (Sea, Peach, Berry, Sprout). Needs Dart Sass 1.79.0 or later.
- **Neutral scale:** `light/_palette_neutral.scss` (imported after `_palette_theme.scss`) defines the greys of the theme: 18 steps `0, 25, 50, 100, 150, 200, 250, 300, 400, 450, 500, 600, 700, 750, 800, 900, 950, 1000`, emitted as `--o-neutral-{step}`. 0 is white, 1000 is black. The default steps are tinted with slate (OKLCH hue 260, chroma 0.038). Steps 50 to 900 have the luminance of the palette steps. Steps 25, 150, 250, 750 and 950 are anchors: they keep the light colours of `--o-bg-subtle`, `--o-border`, `--o-border-strong`, `--o-text` and the shadow colour `rgba(16, 24, 40, a)`. Step 450 is the lightest grey with 3:1 on white. The scale is a fixed ramp: a client theme that sets `$text-color` does not change step 750.
- A theme config sets the tint with `$o-neutral-hue` (OKLCH hue, default `260`) and `$o-neutral-chroma` (OKLCH chroma, default `0.038`, `0` gives pure greys). The compile converts each default step to OKLCH, keeps its lightness, turns its hue by `$o-neutral-hue - 260` and multiplies its chroma by `$o-neutral-chroma / 0.038`. A colour outside of sRGB is mapped into sRGB (`local-minde`). With the default values the compile gives exactly the default hex values (`ColorTokensTest.neutralScale`). The contrast of a step on white changes by 0.08 or less for a pure grey scale.
- The modules do not read a neutral step directly. A semantic or element token reads a step, e.g. `$o-tokens: ("text-muted": "neutral-600")`. A dark mode maps the semantic tokens to other steps. Java does not read the neutral scale as a hue (`Palette.NEUTRAL`), the choosers do not offer it. SCSS: `o-neutral($step)` returns a step at compile time.
- Live reference with contrast values: `doc/component-library/colors.html`.

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

`ToolBarRenderer` writes the slot lists in the visual order left, center, right, rightEdge, so the tab order and the reading order follow the visual order (WCAG 2.4.3). `.container-fluid` is a flex container: the center list takes the free space between left and right (`flex: 1 1 0`) and wraps its tools inside that space, so left and right stay in the first row; right and rightEdge get `margin-left: auto`. Do not position the slots with floats, CSS `order` or a positive `tabindex`.

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
    <div class="container-fluid">
      <ul class="o_tools o_tools_left list-inline"><li class="o_tool"><a class="btn btn-sm">Action</a></li></ul>
      <ul class="o_tools o_tools_center list-inline">...</ul>
      <ul class="o_tools o_tools_right list-inline">...</ul>
      <ul class="o_tools o_tools_right_edge list-inline">...</ul>
    </div>
    <ul class="o_tools o_tools_segments list-inline">...</ul>
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
| `btn o_button_ghost btn-danger` **(New in 21.1.0)** | Ghost button for destructive actions: `--o-state-danger-text` as text and hover border. Java: `link.setGhost(true)` plus `btn-danger`. Example: "Leave course" on the info page |
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

Disabled buttons: `LinkRenderer` writes `<a class="o_disabled disabled btn btn-default">`. Button borders use `$btn-default-border` (`#6c757d`), text fields the lighter `$input-border` (`#d0d5dd`). A `FormLink` with the class `input-group-addon` in an input group gets the button border and background (`_form_bootstrap.scss`); the text field keeps `$input-bg`, also when it is disabled.

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
| `o_fact_sheet` | Box with border (`$o-fact-sheet-border-width`, `$o-fact-sheet-border-style`, token `fact-sheet-border`), radius and 20px padding |
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
| `o_passed` | Text colour of the success state (`$o-course-state-passed-color`) |
| `o_failed` | Text colour of the danger state (`var(--o-state-danger-text)`) |
| `o_unknown` | Text colour of the warning state (`var(--o-state-warning-text)`) |
| `o_noinfo` | Muted text colour (`$text-muted`) |

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

The colour of each badge comes from its state in `$o-state-wiring` ([Color System](#26-color-system)).

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

**Changed in 21.2.0:** The status colours come from 8 semantic states on the palette (OO-10042). The `$o-labeled-*` variables and the `%o_labeled_<colour>` placeholders are removed. The theme emits the colour tokens of three layers as CSS custom properties (OO-9994, OO-10043, 26.1, 26.5). The modules read the tokens with `var(--o-*)` (OO-10043). `_config.scss` no longer has the 226 colour variables that nothing read.

### 26.1 Token Layers

Every colour token belongs to one of three layers. The layer tells which code owns the element. It does not limit the references: a token can reference a token of any layer.

*Figure 5: The three colour token layers: base, framework, component (diagram in the HTML version)*

| Layer | Holds | Example | Map in `modules/_tokens.scss` | Tokens |
|---|---|---|---|---|
| Base | the palette with the literal colours: `--o-{hue}-{step}`, `--o-theme{n}-{step}`, the neutral scale `--o-neutral-{step}`, `--o-white`, the base colours `--o-base-{name}`; the semantic tokens; the state tokens `--o-state-{state}-{role}`; the tokens of the global elements: buttons, content text and headings | `--o-yellow-400`, `--o-base-text`, `--o-primary`, `--o-state-success-text`, `--o-btn-default-fg`, `--o-content-h1` | `$o-token-base` (base colours), `$o-token-layer-base` (global elements) | 22 semantic, 56 state, 50 element tokens, 53 shade tokens; 18 neutral steps |
| Framework | layout and generic UI elements of `org.olat.core.gui` and `org.olat.core.commons.services`: navigation bar, footer, login page, dialogs, menu tree, toolbar, scope selection, table, wizard, form elements, tags, date picker, ... | `--o-navbar-bg`, `--o-tree-link`, `--o-tag-bg` | `$o-token-layer-framework` | 258 element tokens, 55 shade tokens |
| Component | the elements of one feature module: course, course site, tests and question pool (QTI 2.1), practice, forum, chat, lectures, topic broker, folder, selectus, ... | `--o-coursesite-start-bg`, `--o-qti-menu-link`, `--o-selectus-review-glow` | `$o-token-layer-component` | 217 element tokens, 42 shade tokens |

The palette steps come from `_palette.scss`, `_palette_theme.scss`, `_palette_neutral.scss` and `modules/_palette_vars.scss`. All other tokens come from `modules/_tokens.scss`.

An **element token** is a token of the maps `$o-token-layer-base`, `$o-token-layer-framework` and `$o-token-layer-component`: 844 tokens `--o-{element}-{part}`. A **shade token** is the darker or lighter colour of an element token or a semantic token for a state, an edge or a tint, e.g. `--o-btn-primary-bg-hover` (26.5). The layer of a shade token is the layer of its token. Literal colours exist only in the palette and in the shade tokens. Every other token is a `var()` reference: to a semantic token, to a palette step, to another token or to its base colour.

Goal: the modules read every colour with `var(--o-*)`. A client theme changes a colour with one CSS line, a dark mode redefines the semantic tokens, and the removal of Bootstrap ports modules that already read tokens. Spec and module inventory: OO-10043.

### 26.2 Semantic States

`$o-states` in `_palette.scss` maps each state to a palette hue (`!default`, a client theme can change the hue of a state):

| State | Hue | Meaning |
|---|---|---|
| `neutral` | blue | planned, in preparation, running, information |
| `success` | green | done, valid, active, published |
| `warning` | yellow | waits for an action: review, pending, ready |
| `critical` | orange | needs attention: expired, late, declined, invalid |
| `danger` | red | failed, revoked, rejected, locked |
| `finished` | brown | finished, closed, kept for the record |
| `inactive` | slate | inactive, cancelled, in the trash, deleted, not seen |
| `highlight` | magenta | must stand out: AI, surplus |

Each state has 5 styles. All foreground colours come from the swatch of the hue and reach 4.5:1; the compile fails otherwise.

| Style | Look | Use |
|---|---|---|
| `solid` | fill of the hue, tested text colour | default status label |
| `dark` | darker fill | stronger weight: counters (`o_mid*`), deleted entries |
| `tint` | light fill, dark text | draft, preparation, provisional |
| `light` | very light fill, dark text | not seen, in process |
| `text` | white fill, text and border in the text colour of the hue | outline label (`o_labeled_light`) |

Classes for new code:

```html
<span class="o_labeled o_state_success">Published</span>              <!-- solid -->
<span class="o_labeled o_state_neutral o_state_tint">Draft</span>      <!-- tint -->
<span class="o_labeled_light o_state_danger">Revoked</span>           <!-- text -->
```

The existing status classes keep their names. `$o-state-wiring` in `modules/_states.scss` assigns each of the 395 status classes (e.g. `o_repo_status_published`, `o_lp_in_progress`, `label-success`) a state and a style; the module `@extend`s the placeholder `%o_state_<state>_<style>`. The map is generated from the OO-colors mapping; do not edit it by hand. `_states.scss` is imported after all other modules. GUI demo: Colors > tab "Badges / Status".

### 26.3 Status Colour Without a Label

A component that shows a status without a label (icon, marker, progress bar, chart, timeline) takes the colour of the matching state with `o-state-color($state, $role, $style)`:

| Role | Use |
|---|---|
| `text` | text and icons on white |
| `bg`, `soft-bg` | fills |
| `border` | borders, markers, chart lines and bars |
| `fg`, `soft-fg`, `soft-border` | text on a fill, hover colour, light borders |

```scss
.o_icon_ok        { color: o-state-color("success", "text"); }
.o_lectures_absent { color: o-state-color("danger", "text") !important; }
.bar_green        { fill: o-state-color("success", "border"); }
.o_badge_added    { @extend %o_state_success_solid; }
```

`o-swatch-color($hue, $role)` returns a swatch colour of a hue for a colour that is not a status. `o-palette($hue, $step)` returns a palette step.

### 26.4 Rules for New Code

1. A new status uses a state: `o_labeled o_state_<state>` in the template, or a status class added to `$o-state-wiring`. Do not set a colour on a status class in a module.
2. Choose the state by the meaning (26.2), not by the colour.
3. A status colour outside a label reads the state token `var(--o-state-<state>-<role>)` (26.5). `o-state-color()` returns the same colour at compile time, for `email.scss` and for Sass that needs the value. Do not use `$brand-success`, `$brand-danger`, `$brand-warning`, `$brand-info` or `$state-*-text` for a status.
4. A fixed colour that is not a status (rating stars, pen colours, chart series) comes from the palette (`o-palette()`), not from a hex value.
5. Do not add a Sass colour variable. A new customization point is an element token in the map of its layer (26.1, 26.5), with a reference to an existing token as default: `"my-element-border": "primary"`. The module reads `var(--o-my-element-border)`.
6. Do not use colour maths (`darken()`, `lighten()`, `mix()`, `rgba($var)`) on a palette or state colour. Use a role (`soft-fg`, `dark-bg`) or another palette step.
7. Text, surface, border, brand, link and focus colours read the semantic tokens (`var(--o-text)`, `var(--o-border)`, ...), not `$text-color`, `$gray-*` or a hex value. The unit test `ThemeColorRatchetTest` fails when a module gets more old colour references than `theme_color_baseline.properties` allows.
8. Keep `modules/_mixins.scss` in Sass colours, without `var()`. `email.scss` uses its mixins, and mail clients do not support custom properties.

The module rules, the Bootstrap state rules (`.btn-success`, `.btn-info`, `.btn-warning`, alerts, labels, progress bars, panels, list groups, form validation, contextual table rows, `.text-*`, `.bg-*`, `mark`), the message boxes and the evaluation form read the state tokens (26.5). The Bootstrap variables `$brand-*` and `$state-*` do not change these rules. Only `email.css`, the login link of the navigation bar and the own rules of a client theme still read them (26.5, **Old state variables**).

### 26.5 Colour Tokens

`modules/_tokens.scss` emits the tokens on `:root` in `theme.css`, `content.css` and `oo-docs.css`, not in `email.css` (mail clients do not support custom properties). The component library page "Colours", section "Colour Tokens", shows every token with its value and contrast for the selected theme.

**Base colours** (base layer, palette) carry the colour inputs of the theme 1:1. A client theme keeps setting the Sass variables in its config.

| Token | Source | Semantic token | Use |
|---|---|---|---|
| `--o-base-text` | `$text-color` | `--o-text` | body text |
| `--o-base-text-muted` | `$text-muted` | `--o-text-muted` | secondary text |
| `--o-base-heading` | `$headings-color`, else `$text-color` | `--o-heading` | headings, legend, dialog title |
| `--o-base-bg` | `$body-bg` | `--o-bg` | page surface |
| `--o-base-bg-subtle` | `$panel-default-heading-bg` | `--o-bg-subtle` | panel heading, striped row |
| `--o-base-border` | `$panel-default-border` | `--o-border` | panel, divider |
| `--o-base-border-strong` | `$input-border` | `--o-border-strong` | form field |
| `--o-base-primary` | `$brand-primary` | `--o-primary` | brand fill, primary button |
| `--o-base-primary-fg` | text colour on `$brand-primary` with 4.5:1 | `--o-primary-fg` | text on primary |
| `--o-base-primary-hover` | `$brand-primary` 10 % darker (Bootstrap button hover) | `--o-primary-hover` | primary button hover |
| `--o-base-primary-text` | `$o-brand-text` | `--o-primary-text` | brand colour as text on white |
| `--o-base-link` | `$link-color` | `--o-link` | link |
| `--o-base-link-hover` | `$link-hover-color` | `--o-link-hover` | link hover |
| `--o-base-focus` | `$o-focus-color`: `$brand-primary` adjusted to 3:1 on `$body-bg`, or `$input-border-focus` when the theme sets it (36.6) | `--o-focus` | focus ring of every control |
| `--o-base-text-disabled` | `$btn-link-disabled-color` (Bootstrap `$gray-light`) | `--o-text-disabled` | text of a disabled control |
| `--o-base-bg-hover` | `$table-bg-hover` | `--o-bg-hover` | row or item under the pointer |
| `--o-base-bg-selected` | `$table-bg-active` | `--o-bg-selected` | selected row or item |
| `--o-base-bg-raised` | `$dropdown-bg` | `--o-bg-raised` | dropdown menu, popover, dialog |
| `--o-base-border-subtle` | `$hr-border` | `--o-border-subtle` | light separator line: `hr`, list item, blockquote |
| `--o-base-shadow-color` | black, the colour of all shadows | `--o-shadow-color` | shadows |
| `--o-base-backdrop` | `$modal-backdrop-bg` | `--o-backdrop` | dark layer behind a modal dialog |
| `--o-base-scrim` | white, the colour of the light veils of the modules | `--o-scrim` | light veil over content, an image or a video (dark in a dark mode) |

Each semantic token reads its base colour: `--o-text: var(--o-base-text)`. A dark mode redefines the semantic tokens.

The light and openolat values of the last 8 tokens: `text-disabled` `#777777`, `bg-hover` and `bg-selected` `#F5F5F5`, `bg-raised` `#FFFFFF`, `border-subtle` `#EEEEEE`, `shadow-color` and `backdrop` `#000000`, `scrim` `#FFFFFF`. `shadow-color`, `backdrop` and `scrim` are opaque. A rule sets the transparency: `color-mix(in srgb, var(--o-backdrop) 50%, transparent)`. `--o-backdrop` is the dark layer behind a modal dialog. `--o-scrim` is the light veil over content; a dark mode redefines it as a dark colour.

**State tokens:** `--o-state-{state}-{role}` for the 8 states of `$o-states` (26.2) and the roles `text`, `bg`, `fg`, `border`, `soft-bg`, `soft-fg`, `soft-border` (56 tokens). Each one reads the palette step of its hue, e.g. `--o-state-success-bg: var(--o-green-400)`, `--o-state-danger-fg: var(--o-white)`. The word "neutral" names two things: the state `neutral` (`--o-state-neutral-*`, blue: information, planned, running; the Bootstrap name is `info`) and the neutral scale of greys (`--o-neutral-{step}`, 6.1). The prefix `state-` keeps them apart (decision D6).

**Element tokens:** `--o-{element}-{part}` holds the colour of one element: a global element (base layer), a framework element or a component element (26.1). 844 tokens in `$o-token-layer-base`, `$o-token-layer-framework` and `$o-token-layer-component` of `modules/_tokens.scss`. The default of an element token has one of two forms:

| Default | Tokens | Meaning | Example |
|---|---|---|---|
| `($sass-default, (tokens))` | 692 | A Sass value feeds the token. For 570 tokens it is a Sass variable of the theme config: a client theme that sets the Sass variable keeps working. For 56 tokens it is a Sass expression: the text colour that `o-a11y-color()` adjusts for the background of one rule (`"link-subtle": (o-a11y-color($link-color, $panel-default-heading-bg), ("link",))`; also for a pressed fill or the navigation bar: `competence-selected`, `toggle-text`, `navbar-tab-close`, `selectus-rating-b-fg`; the focus rings `navbar-focus` and `footer-focus`, 3:1 on the navigation bar and the footer, decision D24), a fill that `o-a11y-color()` adjusts for its text (`selectus-rating-c-bg`), a text or a mark that `o-a11y-color()` adjusts for the darkest of the page background, the striped and the hovered table row (`o-darkest-color($body-bg, $table-bg-accent, $table-bg-hover)`: `selectus-rating-b-text` and `selectus-rating-c-text` 4.5:1, `selectus-rating-b-marker` 3:1, decisions D18 and D26), the text of a state while the Sass variable of the element has its default (`box-*-text`, `evaluation-*-text`), the text of a hover or pressed state on its fill (`o-contrast-shade-text()`, decision D13: `btn-primary-light-fg-hover`, `-fg-active`, the same for `btn-danger-light`, `table-filter`, `table-filter-active`, and `scope-toggle-on-hover-fg`), or a computed fill (`page-code-box-bg`), or a state colour in a style without a state token (`lecture-rollcall-pending`: `o-state-color("inactive", "border", "light")`), or the colour of a removed shorthand border (`activity-add-border`: `o-state-color("success", "bg")`), or a swatch role or a state style of today (`figure-card-icon-blue`: `o-swatch-color("blue", "tint-fg")`, `user-status-inactive-border`: `o-state-color("inactive", "border", "tint")`; slice 18; `qpool-arrow-draft-border`: `o-state-color("neutral", "border", "tint")`, slice 19), or a function of the theme config (`highscore-position-title`: `o-hs-position-title-color()`, decision D32 E2). For 66 tokens it is a fixed colour without a Sass variable: a palette step (`"rating-star": (o-palette("yellow", 400), ())`) or a literal colour (`labeled-fg`, `navbar-highlight`, `not-available-dark`, `selectus-rating-a-bg`, `course-style-border` and the borders of the removed shorthand variables `card-border`, `widget-border`, `dashboard-widget-border`, `table-details-container-border`, `repo-info-border`, `repo-info-separator-border`, `empty-border`, `icon-panel-border`, `assessment-inspection-border`, `curriculum-widget-border`, `curriculum-widget-card-border`, `highscore-podium-border`, `user-info-border`, `user-info-profile-border`, `user-info-portrait-border`, `panel-authentication-border`, `step-border`, `ac-billing-address-border`, `folder-row-thumbnail-image-border`, `topicbroker-placeholder-title-abbr-border`; since decision D29 the colours of today of the one-time code field `otp-*`, the selectus status icons `selectus-status-*`, the recorder `recorder-*`, the sticky table columns, the drop target of the menu tree and 5 more). The Sass value must be a colour or `transparent`: `none` or `!important` stops the compile. A named Sass variable with a translucent default feeds the token with that value (decision D31 Q2): `"login-bg": ($o-login-bg-color, ())` emits `--o-base-login-bg: color-mix(in srgb, var(--o-scrim) 95%, transparent)`; such a token has no contrast pair and no shade token (the compile stops). A rule reads the token or an element token with a reference to it. No module calls `o-a11y-color()`. | `"navbar-bg": ($o-navbar-bg, ("bg",))` |
| `"token-name"` | 152 | A reference to another token. There is no Sass variable: a client theme changes the token with `$o-tokens`. A loop or an unknown name stops the compile. | `"selectus-review-glow": "primary"` emits `--o-selectus-review-glow: var(--o-primary)`; `"qti-menu-link": "tree-link"` emits `var(--o-tree-link)` |

An element token reads, in this order:

1. the value of `$o-tokens` for it (client override, see below);
2. its reference default `"token-name"`;
3. the first token of its list whose default has the colour of the Sass value; the element token then follows an override of that token;
4. the palette step with the same colour;
5. its base colour `--o-base-{name}`, a literal value next to the token.

The light theme has these defaults. "Sass default" is `-` for a token with a reference default. The groups are the comments of the maps in `modules/_tokens.scss`.

**Base layer: global elements** (`$o-token-layer-base`, 55 tokens: 44 with a Sass default, 11 with a reference default):

| Group | Token | Sass default | Default in light |
|---|---|---|---|
| Buttons | `--o-btn-primary-bg` | `$btn-primary-bg` | `var(--o-primary)` |
| Buttons | `--o-btn-primary-fg` | `$btn-primary-color` | `var(--o-primary-fg)` |
| Buttons | `--o-btn-primary-border` | `$btn-primary-border` | `#295FAA` (base colour) |
| Buttons | `--o-btn-default-bg` | `$btn-default-bg` | `var(--o-bg)` |
| Buttons | `--o-btn-default-fg` | `$btn-default-color` | `var(--o-text)` |
| Buttons | `--o-btn-default-border` | `$btn-default-border` | `#6C757D` (base colour) |
| Buttons | `--o-btn-dirty-bg` | `$o-button-dirty-bg` | `var(--o-state-warning-bg)` |
| Buttons | `--o-btn-dirty-fg` | `$o-button-dirty-color` | `var(--o-state-warning-fg)` |
| Buttons | `--o-btn-dirty-border` | `$o-button-dirty-border` | `#DFA81D` (base colour) |
| Buttons | `--o-btn-ghost-fg` | - | `var(--o-link)` |
| Buttons | `--o-btn-ghost-bg` | `$o-button-ghost-background-color` | `transparent` (base colour) |
| Buttons | `--o-btn-ghost-border` | `$o-button-ghost-border-color` | `transparent` (base colour) |
| Buttons | `--o-btn-ghost-hover-fg` | `$o-button-ghost-hover-color` | `var(--o-link)` |
| Buttons | `--o-btn-ghost-hover-bg` | `$o-button-ghost-hover-background-color` | `transparent` (base colour) |
| Buttons | `--o-btn-ghost-hover-border` | `$o-button-ghost-hover-border-color` | `var(--o-link)` |
| Buttons | `--o-btn-mega-bg` | `$o-button-mega-bg-color` | `var(--o-white)` |
| Buttons | `--o-btn-mega-hover-bg` | `$o-button-mega-hover-bg-color` | `#E6E6E6` (base colour) |
| Buttons | `--o-btn-primary-light-bg` | `$o-button-primary-light-bg` | `var(--o-white)` |
| Buttons | `--o-btn-primary-light-fg` | `$o-button-primary-light-color` | `var(--o-theme1-500)` |
| Buttons | `--o-btn-primary-light-border` | `$o-button-primary-light-border` | `var(--o-btn-primary-light-fg)` |
| Buttons | `--o-btn-primary-light-hover-bg` | `$o-button-primary-light-hover-bg` | `var(--o-btn-primary-light-fg)` |
| Buttons | `--o-btn-primary-light-hover-fg` | `$o-button-primary-light-hover-color` | `var(--o-btn-primary-light-bg)` |
| Buttons | `--o-btn-primary-light-hover-border` | `$o-button-primary-light-hover-border` | `#667085` (base colour) |
| Buttons | `--o-btn-danger-light-bg` | `$o-button-danger-light-bg` | `var(--o-white)` |
| Buttons | `--o-btn-danger-light-fg` | `$o-button-danger-light-color` | `var(--o-red-500)` |
| Buttons | `--o-btn-danger-light-border` | `$o-button-danger-light-border` | `var(--o-btn-danger-light-fg)` |
| Buttons | `--o-btn-danger-light-hover-bg` | `$o-button-danger-light-hover-bg` | `var(--o-btn-danger-light-fg)` |
| Buttons | `--o-btn-danger-light-hover-fg` | `$o-button-danger-light-hover-color` | `var(--o-btn-danger-light-bg)` |
| Buttons | `--o-btn-danger-light-hover-border` | `$o-button-danger-light-hover-border` | `var(--o-btn-danger-light-fg)` |
| Buttons | `--o-btn-primary-light-fg-hover` | `o-contrast-shade-text($o-button-primary-light-color, $o-button-primary-light-bg, 10%)`: the text on the fill of the mixin states (D13) | `var(--o-btn-primary-light-fg)` |
| Buttons | `--o-btn-primary-light-fg-active` | the same with 17%: the text on the pressed fill | `var(--o-btn-primary-light-fg)` |
| Buttons | `--o-btn-danger-light-fg-hover` | `o-contrast-shade-text($o-button-danger-light-color, $o-button-danger-light-bg, 10%)` | `var(--o-btn-danger-light-fg)` |
| Buttons | `--o-btn-danger-light-fg-active` | the same with 17% | `var(--o-btn-danger-light-fg)` |
| Buttons | `--o-btn-danger-bg` | - | `var(--o-state-danger-bg)` |
| Buttons | `--o-btn-danger-fg` | - | `var(--o-state-danger-fg)` |
| Buttons | `--o-btn-danger-border` | - | `var(--o-state-danger-border)` |
| Content text and headings | `--o-content-header` | `$o-content-header-base-color` | `var(--o-heading)` |
| Content text and headings | `--o-content-link` | `$o-content-link-color` | `var(--o-link)` |
| Content text and headings | `--o-content-bg` | - | `var(--o-bg)` |
| Content text and headings | `--o-content-dimmed` | `$o-content-dimmed-color` | `var(--o-text-muted)` |
| Content text and headings | `--o-content-disabled` | - | `var(--o-text-muted)` |
| Content text and headings | `--o-content-h1` | - | `var(--o-content-header)` |
| Content text and headings | `--o-content-h2` | - | `var(--o-content-header)` |
| Content text and headings | `--o-content-h3` | - | `var(--o-content-header)` |
| Content text and headings | `--o-content-h4` | - | `var(--o-content-header)` |
| Content text and headings | `--o-content-h5` | `$o-content-h5-color` | `var(--o-content-header)` |
| Content text and headings | `--o-content-text` | - | `var(--o-text)` |
| Content text and headings | `--o-content-hr` | `$o-content-hr-color` | `#EEEEEE` (base colour) |
| Content text and headings | `--o-content-fieldset-legend` | `$o-content-fieldset-legend-color` | `var(--o-text)` |
| Content text and headings | `--o-content-border-box-border` | `$o-content-border-box-border` | `#777777` (base colour) |
| Content text and headings | `--o-content-table-border` | `$o-content-table-border` | `#777777` (base colour) |
| Content text and headings | `--o-content-border-img-border` | `$o-content-border-img-border` | `var(--o-thumbnail-border)` |
| Content text and headings | `--o-content-table-header-bg` | `$gray-lighter` | `#EEEEEE` (base colour) |
| Content text and headings | `--o-content-table-bordered-header-bg` | `$gray-base` | `#000000` (base colour) |
| Links | `--o-link-subtle` | `o-a11y-color($link-color, $panel-default-heading-bg)` | `var(--o-link)` |

**Framework layer** (`$o-token-layer-framework`, 363 tokens: 305 with a Sass default, 58 with a reference default):

| Group | Token | Sass default | Default in light |
|---|---|---|---|
| Alert and message boxes | `--o-alert-close` | `$gray-light` | `#777777` (base colour) |
| Alert and message boxes | `--o-alert-close-hover` | `$gray` | `#555555` (base colour) |
| Alert and message boxes | `--o-message-table-border` | `$gray-dark` | `#333333` (base colour) |
| Blockquote and code blocks | `--o-blockquote-border` | `$blockquote-border-color` | `var(--o-border-subtle)` |
| Blockquote and code blocks | `--o-code-block-bg` | `$pre-bg` | `#F5F5F5` (base colour) |
| Blockquote and code blocks | `--o-code-block-fg` | `$pre-color` | `#333333` (base colour) |
| Blockquote and code blocks | `--o-code-block-border` | `$pre-border-color` | `rgba(0, 0, 0, 0.1)` (base colour) |
| Bookmarks | `--o-bookmark` | `$o-bookmark-color` | `#BC2D0C` (base colour) |
| Comments | `--o-comment-highlight-glow` | - | `var(--o-state-warning-bg)` |
| Cards and fact sheet | `--o-card-hover` | - | `var(--o-primary)` |
| Cards and fact sheet | `--o-card-title` | - | `var(--o-coursesite-title)` |
| Cards and fact sheet | `--o-fact-icon` | - | `var(--o-primary)` |
| Cards and fact sheet | `--o-widget-subtitle` | `$o-widget-subtitle-color` | `#777777` (base colour) |
| Cards and fact sheet | `--o-widget-text-additional` | `$o-widget-text-additional-color` | `#777777` (base colour) |
| Cards and fact sheet | `--o-widget-figure-desc` | `$o-widget-figure-desc-color` | `#2D2926` (base colour) |
| Cards and fact sheet | `--o-dashboard-widget-bg` | `$o-dashboard-widget-bg-color` | `var(--o-panel-bg)` |
| Cards and fact sheet | `--o-dashboard-widget-header-footer-bg` | `$o-dashboard-widget-header-footer-bg-color` | `#FCFCFC` (base colour) |
| Cards and fact sheet | `--o-dashboard-widget-table-bg` | `$o-dashboard-widget-table-bg-color` | `var(--o-white)` |
| Cards and fact sheet | `--o-fact-icon-bg` | `$o-fact-icon-bg-color` | `color-mix(in srgb, var(--o-primary) 10%, transparent)` (base value) |
| Cards and fact sheet | `--o-card-border` | `#E4E7EC` | `#E4E7EC` (base colour) |
| Cards and fact sheet | `--o-card-bg` | `$o-card-bg-color` | `#F9FAFB` (base colour) |
| Cards and fact sheet | `--o-widget-border` | `#E4E7EC` | `#E4E7EC` (base colour) |
| Cards and fact sheet | `--o-fact-sheet-border` | - | `var(--o-repo-info-border)` |
| Cards and fact sheet | `--o-fact-sheet-separator-border` | - | `var(--o-repo-info-separator-border)` |
| Cards and fact sheet | `--o-dashboard-widget-border` | `#E4E7EC` | `#E4E7EC` (base colour) |
| Charts | `--o-chart-help-line` | `$gray-light` | `#777777` (base colour) |
| Charts | `--o-chart-bar-light` | `$o-d3chart-light` | `#6193D9` (base colour) |
| Charts | `--o-chart-bar-dark` | `$o-d3chart-dark` | `#1F4881` (base colour) |
| Context help | `--o-chelp-bg` | `$o-chelp-bg` | `var(--o-btn-primary-bg)` |
| Context help | `--o-chelp-border` | `$o-chelp-border` | `var(--o-btn-primary-border)` |
| Context help | `--o-chelp-fg` | - | `var(--o-btn-primary-fg)` |
| Context help | `--o-chelp-tooltip` | `$o-chelp-tooltip-color` | `var(--o-text-muted)` |
| Glossary | `--o-gloss-tooltip-border` | `$o-gloss-tooltip-border-color` | `#AAAAAA` (base colour) |
| Date component and date picker | `--o-datecomp-day-bg` | - | `var(--o-bg)` |
| Date component and date picker | `--o-datecomp-day-fg` | - | `var(--o-text)` |
| Date component and date picker | `--o-datecomp-month-bg` | - | `var(--o-primary)` |
| Date component and date picker | `--o-datecomp-month-fg` | `$o-datecomp-month-color` | `var(--o-white)` |
| Date component and date picker | `--o-datecomp-border` | `$o-datecomp-border` | `#555555` (base colour) |
| Date component and date picker | `--o-datepicker-today-border` | - | `var(--o-primary)` |
| Date component and date picker | `--o-datepicker-today` | - | `var(--o-primary)` |
| Date component and date picker | `--o-day-nav-day-bg` | `$o-day-nav-day-bg-color` | `var(--o-white)` |
| Date component and date picker | `--o-day-nav-day-hover-border` | `$o-day-nav-day-hover-border-color` | `#98A2B3` (base colour) |
| Date component and date picker | `--o-day-nav-day-selected-bg` | `$o-day-nav-day-selected-bg-color` | `#3273DC` (base colour) |
| Date component and date picker | `--o-day-nav-day-selected-fg` | `$o-day-nav-day-selected-color` | `var(--o-white)` |
| Date component and date picker | `--o-day-nav-day-weekend-bg` | `$o-day-nav-day-weekend-bg-color` | `#FCFCFC` (base colour) |
| Dialogs | `--o-dialog-header` | `$o-dialog-header-color` | `var(--o-primary-text)` |
| Dialogs | `--o-dialog-header-bg` | `$o-dialog-header-background` | `#F9FAFB` (base colour) |
| Dialogs | `--o-dialog-footer-bg` | `$o-dialog-footer-background` | `var(--o-dialog-header-bg)` |
| Dropdown menus | `--o-dropdown-link` | `$dropdown-link-color` | `#333333` (base colour) |
| Dropdown menus | `--o-dropdown-link-hover` | `$dropdown-link-hover-color` | `#262626` (base colour) |
| Dropdown menus | `--o-dropdown-link-hover-bg` | `$dropdown-link-hover-bg` | `var(--o-bg-hover)` |
| Dropdown menus | `--o-dropdown-divider` | `$dropdown-divider-bg` | `#E5E5E5` (base colour) |
| Dropdown menus | `--o-dropdown-link-active` | `$dropdown-link-active-color` | `var(--o-white)` |
| Dropdown menus | `--o-dropdown-link-active-bg` | `$dropdown-link-active-bg` | `var(--o-primary)` |
| Empty state and indicators | `--o-box-plot-values` | - | `var(--o-primary)` |
| Empty state and indicators | `--o-box-plot-quartile-fill` | `$o-box-plot-quartile-fill` | `#DCE7F7` (base colour) |
| Empty state and indicators | `--o-box-plot-axis` | `$o-box-plot-axis-color` | `#333333` (base colour) |
| Empty state and indicators | `--o-empty-msg` | `$o-empty-msg-color` | `var(--o-text)` |
| Empty state and indicators | `--o-empty-panel-icon` | - | `var(--o-primary)` |
| Empty state and indicators | `--o-empty-border` | `#E4E7EC` | `#E4E7EC` (base colour) |
| Empty state and indicators | `--o-empty-indicator` | `$o-empty-indicator-color` | `#495057` (base colour) |
| Empty state and indicators | `--o-empty-icon` | `$o-empty-icon-color` | `#C6C9CD` (base colour) |
| Empty state and indicators | `--o-empty-panel-icon-bg` | `$o-empty-panel-icon-bg-color` | `color-mix(in srgb, var(--o-primary) 10%, transparent)` (base value) |
| Empty state and indicators | `--o-icon-panel-border` | `#E4E7EC` | `#E4E7EC` (base colour) |
| Empty state and indicators | `--o-icon-panel-icon-bg` | `$o-icon-panel-icon-bg-color` | `#F9FAFB` (base colour) |
| Empty state and indicators | `--o-icon-panel-content-bg` | `$o-icon-panel-content-bg-color` | `var(--o-panel-bg)` |
| Empty state and indicators | `--o-not-available-dark` | `#000000` | `#000000` (base colour) |
| Empty state and indicators | `--o-not-available-light` | `#FFFFFF` | `var(--o-white)` |
| Empty state and indicators | `--o-indicator-label` | - | `var(--o-text-muted)` |
| Empty state and indicators | `--o-indicator-separator-border` | `$o-dialog-cn-box-border-color` | `var(--o-border)` |
| Empty state and indicators | `--o-speedometer-not-border` | `$o-o_speedometer-not-border-color` | `#EEEEEE` (base colour) |
| Empty state and indicators | `--o-speedometer-default-border` | `$o-o_speedometer-default-border-color` | `var(--o-state-neutral-border)` |
| Empty state and indicators | `--o-speedometer-success-border` | `$o-o_speedometer-success-border-color` | `var(--o-state-success-border)` |
| Empty state and indicators | `--o-speedometer-warning-border` | `$o-o_speedometer-warning-border-color` | `var(--o-state-warning-border)` |
| Empty state and indicators | `--o-speedometer-danger-border` | `$o-o_speedometer-danger-border-color` | `var(--o-state-danger-border)` |
| Empty state and indicators | `--o-speedometer-default-text` | `$o-o_speedometer-default-border-color` | `var(--o-speedometer-default-border)` |
| Empty state and indicators | `--o-speedometer-success-text` | `$o-o_speedometer-success-border-color` | `var(--o-speedometer-success-border)` |
| Empty state and indicators | `--o-speedometer-warning-text` | `$o-o_speedometer-warning-border-color` | `var(--o-speedometer-warning-border)` |
| Empty state and indicators | `--o-speedometer-danger-text` | `$o-o_speedometer-danger-border-color` | `var(--o-speedometer-danger-border)` |
| Empty state and indicators | `--o-radial-progress-addon` | - | `var(--o-text-muted)` |
| Empty state and indicators | `--o-radial-progress-inset-bg` | `$o-radial-progress-inset-color` | `var(--o-white)` |
| Empty state and indicators | `--o-radial-progress-circle-bg` | `$o-radial-progress-circle-bg` | `#EEEEEE` (base colour) |
| Empty state and indicators | `--o-radial-progress-circle` | `$o-radial-progress-circle-color` | `var(--o-progress-bar-bg)` |
| Empty state and indicators | `--o-radial-progress-number` | `$o-radial-progress-number-color` | `var(--o-progress-bar-bg)` |
| Empty state and indicators | `--o-radial-progress-circle-success` | `$o-radial-progress-circle-success-color` | `var(--o-state-success-bg)` |
| Empty state and indicators | `--o-radial-progress-percentage-success` | `$o-radial-progress-percentage-success-color` | `var(--o-state-success-text)` |
| Empty state and indicators | `--o-radial-progress-circle-neutral` | `$o-radial-progress-circle-neutral-color` | `var(--o-progress-bar-neutral-bg)` |
| Empty state and indicators | `--o-radial-progress-percentage-neutral` | `$o-radial-progress-percentage-neutral-color` | `var(--o-state-success-text)` |
| Empty state and indicators | `--o-radial-progress-circle-info` | `$o-radial-progress-circle-info-color` | `var(--o-state-neutral-bg)` |
| Empty state and indicators | `--o-radial-progress-percentage-info` | `$o-radial-progress-percentage-info-color` | `var(--o-state-neutral-text)` |
| Empty state and indicators | `--o-radial-progress-circle-danger` | `$o-radial-progress-circle-danger-color` | `var(--o-state-danger-bg)` |
| Empty state and indicators | `--o-radial-progress-percentage-danger` | `$o-radial-progress-percentage-danger-color` | `var(--o-state-danger-text)` |
| Empty state and indicators | `--o-radial-progress-circle-warning` | `$o-radial-progress-circle-warning-color` | `var(--o-state-warning-bg)` |
| Empty state and indicators | `--o-radial-progress-percentage-warning` | `$o-radial-progress-percentage-warning-color` | `var(--o-state-warning-text)` |
| Figure cards | `--o-figure-card-chevron` | - | `var(--o-text-muted)` |
| Figure cards | `--o-figure-card-figure` | - | `var(--o-primary)` |
| Figure cards | `--o-figure-card-icon` | `$o-figure-card-icon-color` | `var(--o-primary)` |
| Figure cards | `--o-figure-card-bg` | `$o-figure-card-bg-color` | `var(--o-white)` |
| Figure cards | `--o-figure-card-icon-bg` | `$o-figure-card-icon-bg-color` | `color-mix(in srgb, var(--o-primary) 10%, transparent)` (base value) |
| Figure cards | `--o-figure-card-icon-blue` | `o-swatch-color("blue", "tint-fg")` | `var(--o-blue-900)` |
| Figure cards | `--o-figure-card-icon-blue-bg` | `o-swatch-color("blue", "tint-bg")` | `var(--o-blue-300)` |
| Figure cards | `--o-figure-card-icon-green` | `o-swatch-color("green", "tint-fg")` | `var(--o-green-900)` |
| Figure cards | `--o-figure-card-icon-green-bg` | `o-swatch-color("green", "tint-bg")` | `var(--o-green-300)` |
| Figure cards | `--o-figure-card-icon-yellow` | `o-swatch-color("yellow", "tint-fg")` | `var(--o-yellow-900)` |
| Figure cards | `--o-figure-card-icon-yellow-bg` | `o-swatch-color("yellow", "tint-bg")` | `var(--o-yellow-300)` |
| Figure cards | `--o-figure-card-icon-orange` | `o-swatch-color("orange", "tint-fg")` | `var(--o-orange-900)` |
| Figure cards | `--o-figure-card-icon-orange-bg` | `o-swatch-color("orange", "tint-bg")` | `var(--o-orange-300)` |
| Figure cards | `--o-figure-card-icon-red` | `o-swatch-color("red", "tint-fg")` | `var(--o-red-900)` |
| Figure cards | `--o-figure-card-icon-red-bg` | `o-swatch-color("red", "tint-bg")` | `var(--o-red-300)` |
| Figure cards | `--o-figure-card-icon-grey` | `o-swatch-color("gray", "tint-fg")` | `var(--o-gray-900)` |
| Figure cards | `--o-figure-card-icon-grey-bg` | `o-swatch-color("gray", "tint-bg")` | `var(--o-gray-300)` |
| Footer | `--o-footer-bg` | `$o-footer-outer-bg` | `#F9FAFB` (base colour) |
| Footer | `--o-footer-inner-bg` | `$o-footer-inner-bg` | `#F9FAFB` (base colour) |
| Footer | `--o-footer-text` | `$o-footer-default-color` | `var(--o-text-muted)` |
| Footer | `--o-footer-link` | `$o-footer-default-link-color` | `var(--o-text-muted)` |
| Footer | `--o-footer-focus` | `o-a11y-color(o-contrast-hover($o-focus-color, $o-footer-default-link-color, $o-footer-inner-bg, 3), $o-footer-inner-bg, 3)` | `var(--o-focus)` |
| Form elements | `--o-input-bg` | - | `var(--o-bg)` |
| Form elements | `--o-input-fg` | `$input-color` | `#555555` (base colour) |
| Form elements | `--o-input-disabled-bg` | - | `var(--o-bg-subtle)` |
| Form elements | `--o-input-placeholder` | `$input-color-placeholder` | `#999999` (base colour) |
| Form elements | `--o-legend` | `$legend-color` | `#333333` (base colour) |
| Form elements | `--o-legend-border` | `$legend-border-color` | `#E5E5E5` (base colour) |
| Form elements | `--o-addremove-add-bg` | `$o-addremove-add-color` | `var(--o-state-success-soft-bg)` |
| Form elements | `--o-addremove-remove-bg` | `$o-addremove-remove-color` | `var(--o-state-danger-soft-bg)` |
| Form elements | `--o-autocomplete-suggestion-bg` | - | `var(--o-btn-primary-bg)` |
| Form elements | `--o-autocomplete-suggestion-fg` | - | `var(--o-btn-primary-fg)` |
| Form elements | `--o-autocomplete-fg` | `$o-autocomplete-color` | `var(--o-input-fg)` |
| Form elements | `--o-autocomplete-bg` | `$o-autocomplete-bg-color` | `var(--o-input-bg)` |
| Form elements | `--o-file-upload-border` | - | `var(--o-text-muted)` |
| Form elements | `--o-panel-placeholder-border-active` | - | `var(--o-primary)` |
| Form elements | `--o-panel-placeholder-border` | `$o-panel-placeholder-border-color` | `#D0D5DD` (base colour) |
| Form elements | `--o-panel-placeholder-bg` | `$o-panel-placeholder-bg` | `var(--o-white)` |
| Form elements | `--o-panel-placeholder-fg` | `$o-panel-placeholder-color` | `#667085` (base colour) |
| Form elements | `--o-panel-placeholder-bg-active` | `$o-panel-placeholder-bg-active` | `#F9FAFB` (base colour) |
| Image text and activity log | `--o-imagebg-text-bg` | `$o-imagebg-text-background` | `color-mix(in srgb, var(--o-scrim) 80%, transparent)` (base value) |
| Image text and activity log | `--o-activity-add-border` | `o-state-color("success", "bg")` | `var(--o-state-success-bg)` |
| Image text and activity log | `--o-activity-modify-border` | `o-state-color("warning", "bg")` | `var(--o-state-warning-bg)` |
| Image text and activity log | `--o-activity-remove-border` | `o-swatch-color("gray", "border")` | `var(--o-gray-600)` |
| Form elements | `--o-radio-card-border` | - | `var(--o-border-strong)` |
| Form elements | `--o-radio-card-image-border` | - | `var(--o-border-strong)` |
| Form elements | `--o-radio-card-label-hover` | - | `var(--o-btn-default-fg)` |
| Form elements | `--o-radio-card-hover-border` | `$o-radio-card-border-color-hover` | `#BEC5D0` (base colour) |
| Form elements | `--o-radio-card-hover-bg` | `$o-radio-card-bg-hover` | `#F2F2F2` (base colour) |
| Form elements | `--o-radio-card-checked-border` | `$o-radio-card-border-color-checked` | `#1D7ECC` (base colour) |
| Form elements | `--o-radio-card-checked-bg` | `$o-radio-card-bg-checked` | `#ECF5FC` (base colour) |
| Form elements | `--o-radio-card-checked-label` | `$o-radio-card-label-color-checked` | `#1D7ECC` (base colour) |
| Form elements | `--o-radio-card-bg` | `$o-radio-card-bg` | `var(--o-input-bg)` |
| Form elements | `--o-color-picker-selected-fg` | `$o-bright-text-and-border` | `#FCFCFC` (base colour) |
| Form elements | `--o-slider-handler-bg` | - | `var(--o-primary)` |
| Form elements | `--o-slider-handler-border` | - | `var(--o-primary)` |
| Form elements | `--o-slider-border` | `$o-slider-border` | `#AAAAAA` (base colour) |
| Form elements | `--o-slider-bg` | `$o-slider-background` | `#F9F9F9` (base colour) |
| jQuery UI dialog | `--o-dialog-window-bg` | `$o-dialog-window-bg` | `var(--o-white)` |
| jQuery UI dialog | `--o-dialog-window-bar-bg` | `$o-dialog-window-bar-bg` | `#F2F4F7` (base colour) |
| Form elements | `--o-toggle-on-bg` | - | `var(--o-primary)` |
| Form elements | `--o-toggle-on-border` | - | `var(--o-primary)` |
| Form elements | `--o-toggle-text` | `o-a11y-color($text-muted, $o-toggle-bg)`: the muted text, adjusted to 4.5:1 on the track | `#626B7F` (base colour) |
| Form elements | `--o-toggle-border` | `$o-toggle-border-color` | `#777777` (base colour) |
| Form elements | `--o-toggle-bg` | `$o-toggle-bg` | `#EEEEEE` (base colour) |
| Form elements | `--o-toggle-handle` | `$o-toggle-color` | `#777777` (base colour) |
| Form elements | `--o-toggle-on-handle` | `$o-toggle-on-color` | `#EEEEEE` (base colour) |
| Form elements | `--o-toggle-on-text` | `$o-toggle-on-text-color` | `#EEEEEE` (base colour) |
| Form elements | `--o-tinymce-danger-bg` | - | `var(--o-state-danger-soft-bg)` |
| Icons | `--o-icon-changes` | `$o-base-blue-color` | `#105CAD` (base colour) |
| Icons | `--o-colored-icon-blue` | `$o-base-blue-color` | `#105CAD` (base colour) |
| Icons | `--o-colored-icon-green` | `$o-base-green-color` | `#268000` (base colour) |
| Icons | `--o-colored-icon-orange` | `$o-base-orange-color` | `#D17A00` (base colour) |
| Icons | `--o-icon-mandatory` | `$o-base-orange-color` | `#D17A00` (base colour) |
| Labels without a status class | `--o-labeled-fg` | `#FFFFFF` | `var(--o-white)` |
| Labels without a status class | `--o-labeled-bg` | `o-palette("gray", 600)` | `var(--o-gray-600)` |
| Labels without a status class | `--o-labeled-border` | `o-palette("gray", 700)` | `var(--o-gray-700)` |
| Labels without a status class | `--o-labeled-light-fg` | `o-palette("gray", 600)` | `var(--o-gray-600)` |
| Labels without a status class | `--o-labeled-light-bg` | `#FFFFFF` | `var(--o-white)` |
| Labels without a status class | `--o-labeled-light-border` | `o-palette("gray", 600)` | `var(--o-gray-600)` |
| Login page | `--o-login-header` | `$o-login-header-color` | `var(--o-primary-text)` |
| Login page | `--o-login-help-link` | `$o-login-help-link-color` | `#2C66B7` (base colour) |
| Login page | `--o-login-lead` | `$o-login-lead-color` | `var(--o-text)` |
| Login page | `--o-login-lead-header` | `$o-login-lead-header-color` | `var(--o-content-header)` |
| Login page | `--o-login-infomessage-border` | `$o-login-infomessage-border` | `transparent` (base colour) |
| Login page | `--o-login-help-bg` | `$o-login-help-bg-color` | `#E4E7EC` (base colour) |
| Login page | `--o-login-border` | `$o-login-border-color` | `#E4E7EC` (base colour) |
| Login page | `--o-login-bg` | `$o-login-bg-color` | `color-mix(in srgb, var(--o-scrim) 95%, transparent)` (base value) |
| Login page | `--o-login-infomessage-bg` | `$o-login-infomessage-bg` | `color-mix(in srgb, var(--o-scrim) 50%, transparent)` (base value) |
| Login page | `--o-otp-cell-bg` | `#EEEEEE` | `#EEEEEE` (base colour) |
| Login page | `--o-otp-cell-active-bg` | `#BBBBBB` | `#BBBBBB` (base colour) |
| Login page | `--o-otp-caret` | `#222222` | `var(--o-gray-900)` |
| Menu tree | `--o-tree-link` | `$o-tree-link-color` | `var(--o-nav-link)` |
| Menu tree | `--o-tree-link-hover` | `$o-tree-link-hover-color` | `var(--o-nav-link-hover)` |
| Menu tree | `--o-tree-link-hover-bg` | `$o-tree-link-hover-bg` | `var(--o-white)` |
| Menu tree | `--o-tree-link-active` | `$o-tree-link-active-color` | `var(--o-link)` |
| Menu tree | `--o-tree-link-active-hover` | `$o-tree-link-active-hover-color` | `var(--o-link-hover)` |
| Menu tree | `--o-tree-link-active-hover-bg` | `$o-tree-link-active-hover-bg` | `#EEEEEE` (base colour) |
| Menu tree | `--o-tree-link-active-parent` | `$o-tree-link-active-parent-color` | `var(--o-tree-link)` |
| Menu tree | `--o-tree-link-active-parent-hover` | `$o-tree-link-active-parent-hover-color` | `var(--o-tree-link-hover)` |
| Menu tree | `--o-tree-dnd-over-bg` | `#FFFF60` | `#FFFF60` (base colour) |
| Menu tree | `--o-tree-border` | `$o-tree-border-color` | `var(--o-gray-200)` |
| Menu tree | `--o-tree-insertion-disabled` | `$o-tree-insertion-disabled-color` | `#777777` (base colour) |
| Menu tree | `--o-tree-insertion-source-border` | `$o-tree-insertion-source-border` | `#FFDBAC` (base colour) |
| Menu tree | `--o-tree-insertion-source-bg` | `$o-tree-insertion-source-bg` | `#FFCF91` (base colour) |
| Menu tree | `--o-tree-insertion-source-fg` | `$o-tree-insertion-source-color` | `#522D00` (base colour) |
| Main layout | `--o-main-outer-bg` | `$o-main-outer-bg` | `var(--o-white)` |
| Main layout | `--o-main-inner-bg` | `$o-main-inner-bg` | `var(--o-white)` |
| Main layout | `--o-main-left-bg` | `$o-main-left-bg` | `var(--o-white)` |
| Message boxes of a state | `--o-box-info-border` | `$o-box-info-color` | `var(--o-state-neutral-bg)` |
| Message boxes of a state | `--o-box-info-text` | `$o-box-info-color`; with its default the text of the state `neutral` | `var(--o-state-neutral-text)` |
| Message boxes of a state | `--o-box-success-border` | `$o-box-success-color` | `var(--o-state-success-bg)` |
| Message boxes of a state | `--o-box-success-text` | `$o-box-success-color`; with its default the text of the state `success` | `var(--o-state-success-text)` |
| Message boxes of a state | `--o-box-warning-border` | `$o-box-warning-color` | `var(--o-state-warning-bg)` |
| Message boxes of a state | `--o-box-warning-text` | `$o-box-warning-color`; with its default the text of the state `warning` | `var(--o-state-warning-text)` |
| Message boxes of a state | `--o-box-error-border` | `$o-box-error-color` | `var(--o-state-danger-bg)` |
| Message boxes of a state | `--o-box-error-text` | `$o-box-error-color`; with its default the text of the state `danger` | `var(--o-state-danger-text)` |
| Message boxes of a state | `--o-box-info-bg` | `$o-box-info-bg` | `#FCFCFC` (base colour) |
| Message boxes of a state | `--o-box-note-bg` | `$o-box-note-bg` | `#FAFAFB` (base colour) |
| Message boxes of a state | `--o-box-success-bg` | `$o-box-success-bg` | `#F3F9F5` (base colour) |
| Message boxes of a state | `--o-box-warning-bg` | `$o-box-warning-bg` | `#FEFDF8` (base colour) |
| Message boxes of a state | `--o-box-error-bg` | `$o-box-error-bg` | `#FFF8F6` (base colour) |
| Message boxes | `--o-box-note-border` | `$o-box-note-color` | `#666666` (base colour) |
| Message boxes | `--o-box-note-text` | `$o-box-note-color` | `var(--o-box-note-border)` |
| Message boxes | `--o-box-tip-bg` | `$o-box-tip-bg` | `#F2F5F4` (base colour) |
| Message boxes | `--o-box-tip-border` | `$o-box-tip-color` | `#345566` (base colour) |
| Message boxes | `--o-box-important-bg` | `$o-box-important-bg` | `#F4FAFA` (base colour) |
| Message boxes | `--o-box-important-border` | `$o-box-important-color` | `#41BBDB` (base colour) |
| Message boxes | `--o-message-line-warning-bg` | `$o-line-warning-bg` | `var(--o-orange-300)` |
| Message boxes | `--o-message-line-warning-fg` | `$o-line-warning-color` | `var(--o-orange-900)` |
| Message boxes | `--o-message-line-error-bg` | `$o-line-error-bg` | `var(--o-red-300)` |
| Message boxes | `--o-message-line-error-fg` | `$o-line-error-color` | `var(--o-red-900)` |
| Message boxes of a state | `--o-brand-box-border` | `$o-brand-box-border` | `#295FAA` (base colour) |
| Message boxes of a state | `--o-brand-box-bg` | `$o-brand-box-bg` | `color-mix(in srgb, var(--o-primary) 10%, transparent)` (base value) |
| Modal dialogs and popovers | `--o-modal-content-border` | `$modal-content-border-color` | `var(--o-border)` |
| Modal dialogs and popovers | `--o-modal-header-border` | `$modal-header-border-color` | `#E5E5E5` (base colour) |
| Modal dialogs and popovers | `--o-popover-bg` | - | `var(--o-bg-raised)` |
| Modal dialogs and popovers | `--o-popover-border` | `$popover-border-color` | `rgba(0, 0, 0, 0.2)` (base colour) |
| Modal dialogs and popovers | `--o-popover-title-bg` | `$popover-title-bg` | `#F7F7F7` (base colour) |
| Modal dialogs and popovers | `--o-popover-fallback-border` | `$popover-fallback-border-color` | `#CCCCCC` (base colour) |
| Navigation bar | `--o-navbar-bg` | `$o-navbar-bg` | `var(--o-bg)` |
| Navigation bar | `--o-navbar-text` | `$o-navbar-color` (default `o-a11y-color($navbar-default-color, $o-navbar-bg)`, decision D23) | `#747474` (base colour) |
| Navigation bar | `--o-navbar-link` | `$o-navbar-link-color` | `#475467` (base colour) |
| Navigation bar | `--o-navbar-link-hover` | `$o-navbar-link-hover-color` | `#323B49` (base colour) |
| Navigation bar | `--o-navbar-link-hover-bg` | `$o-navbar-link-hover-bg` | `transparent` (base colour) |
| Navigation bar | `--o-navbar-link-active` | `$o-navbar-link-active-color` | `#555555` (base colour) |
| Navigation bar | `--o-navbar-link-active-bg` | `$o-navbar-link-active-bg` | `#EEEEEE` (base colour) |
| Navigation bar | `--o-navbar-link-disabled` | `$o-navbar-link-disabled-color` | `#CCCCCC` (base colour) |
| Navigation bar | `--o-navbar-link-disabled-bg` | `$o-navbar-link-disabled-bg` | `transparent` (base colour) |
| Navigation bar | `--o-navbar-tools` | `$o-navbar-tools-permanent-color` (default `o-a11y-color($navbar-default-color, $o-navbar-bg)`, decision D23) | `#747474` (base colour) |
| Navigation bar | `--o-navbar-toggle-hover-bg` | `$o-navbar-toggle-hover-bg` | `var(--o-gray-200)` |
| Navigation bar | `--o-navbar-toggle-icon` | `$o-navbar-toggle-icon-bar-bg` | `#888888` (base colour) |
| Navigation bar | `--o-navbar-toggle-border` | `$o-navbar-toggle-border-color` | `var(--o-gray-200)` |
| Navigation bar | `--o-navbar-brand` | `$o-navbar-brand-color` | `#475467` (base colour) |
| Navigation bar | `--o-navbar-brand-hover` | `$o-navbar-brand-hover-color` | `#323B49` (base colour) |
| Navigation bar | `--o-navbar-brand-hover-bg` | `$o-navbar-brand-hover-bg` | `transparent` (base colour) |
| Navigation bar | `--o-navbar-tab-close` | `o-a11y-color($o-navbar-tab-close-color, $o-navbar-bg, 3)` | `var(--o-state-danger-text)` |
| Navigation bar | `--o-navbar-tab-close-hover` | `o-a11y-color($o-navbar-tab-close-hover-color, $o-navbar-bg, 3)` | `var(--o-state-danger-text-hover)` |
| Navigation bar | `--o-dropdown-tab-close` | `$o-navbar-tab-close-color` | `var(--o-state-danger-text)` |
| Navigation bar | `--o-dropdown-tab-close-hover` | `$o-navbar-tab-close-hover-color` | `var(--o-state-danger-text-hover)` |
| Navigation bar | `--o-navbar-focus` | `o-a11y-color(o-contrast-hover($o-focus-color, $o-navbar-link-color, $o-navbar-bg, 3), $o-navbar-bg, 3)` | `var(--o-focus)` |
| Navigation bar | `--o-navbar-highlight` | `#FFFFFF` | `var(--o-white)` |
| Navigation bar | `--o-navbar-logout` | `$o-navbar-logout-color` | `var(--o-state-danger-text)` |
| Navigation links | `--o-nav-link` | `$navbar-default-link-color` | `#475467` (base colour) |
| Navigation links | `--o-nav-link-hover` | `$navbar-default-link-hover-color` | `#333333` (base colour) |
| Offcanvas menu | `--o-offcanvas-logout` | `$o-offcanvas-logout-color` | `#FF8B9B` (base colour) |
| Offcanvas menu | `--o-offcanvas-logout-hover` | `$o-offcanvas-logout-hover-color` | `#FF586F` (base colour) |
| Offcanvas menu | `--o-offcanvas-link-hover` | `$o-offcanvas-link-hover-color` | `#939393` (base colour) |
| Offcanvas menu | `--o-offcanvas-category-border` | `#888888` | `#888888` (base colour) |
| Offcanvas menu | `--o-offcanvas-link` | `$o-offcanvas-link-color` | `#ACACAC` (base colour) |
| Offcanvas menu | `--o-offcanvas-link-hover-bg` | `$o-offcanvas-link-hover-bg` | `transparent` (base colour) |
| Offcanvas menu | `--o-offcanvas-dropdown-bg` | `$o-offcanvas-dropdown-bg` | `var(--o-gray-900)` |
| Offcanvas menu | `--o-offcanvas-link-active-bg` | `$o-offcanvas-link-active-bg` | `#090909` (base colour) |
| Offcanvas menu | `--o-offcanvas-link-active` | `$o-offcanvas-link-active-color` | `var(--o-white)` |
| Offcanvas menu | `--o-offcanvas-bg` | `$o-offcanvas-bg` | `var(--o-gray-900)` |
| Offcanvas menu | `--o-offcanvas-border` | `$o-offcanvas-border` | `#090909` (base colour) |
| Panels, progress bars and thumbnails | `--o-panel-bg` | `$panel-bg` | `var(--o-bg)` |
| Panels, progress bars and thumbnails | `--o-panel-default-border` | `$panel-default-border` | `#E4E7EC` (base colour) |
| Panels, progress bars and thumbnails | `--o-progress-bar-fg` | `$progress-bar-color` | `var(--o-white)` |
| Panels, progress bars and thumbnails | `--o-progress-bar-bg` | `$progress-bar-bg` | `var(--o-primary)` |
| Panels, progress bars and thumbnails | `--o-progress-bar-neutral-bg` | `$progress-bar-neutral-bg` | `var(--o-base-progress-bar-neutral-bg)` |
| Panels, progress bars and thumbnails | `--o-thumbnail-border` | `$thumbnail-border` | `var(--o-gray-200)` |
| Portal and portlets | `--o-portlet-border` | - | `var(--o-border)` |
| Portal and portlets | `--o-portlet-header-bg` | - | `var(--o-bg-subtle)` |
| Portal and portlets | `--o-portlet-edit-bg` | `$o-portlet-edit-bg` | `var(--o-state-warning-soft-bg)` |
| Portal and portlets | `--o-portlet-edit-border` | `$o-portlet-edit-border` | `var(--o-state-warning-soft-border)` |
| Portal and portlets | `--o-portlet-bg` | `$o-portlet-bg` | `var(--o-panel-bg)` |
| Scope selection | `--o-scope-toggle-border` | - | `var(--o-primary)` |
| Scope selection | `--o-scope-toggle-fg` | - | `var(--o-text)` |
| Scope selection | `--o-scope-toggle-on-bg` | `$o-scope-toggle-on-bg-color` | `var(--o-primary)` |
| Scope selection | `--o-scope-toggle-bg` | `$o-scope-toggle-bg-color` | `var(--o-white)` |
| Scope selection | `--o-scope-toggle-hover-bg` | `$o-scope-toggle-bg-hover-color` | `#E6E6E6` (base colour) |
| Scope selection | `--o-scope-toggle-on-fg` | `$o-scope-toggle-on-color` | `var(--o-white)` |
| Scope selection | `--o-scope-toggle-on-border` | `$o-scope-toggle-on-border-color` | `var(--o-white)` |
| Scope selection | `--o-scope-toggle-on-hover-bg` | `$o-scope-toggle-on-bg-hover-color` | `#3374CE` (base colour) |
| Scope selection | `--o-scope-toggle-on-hover-fg` | `o-contrast-shade-text($o-scope-toggle-on-color, $o-scope-toggle-on-bg-color, 10%, "lighten", $state-bg: $o-scope-toggle-on-bg-hover-color)` (D13) | `var(--o-scope-toggle-on-fg)` |
| Segments | `--o-segments-tab-link` | - | `var(--o-link)` |
| Table | `--o-table-border` | `$table-border-color` | `var(--o-gray-200)` |
| Table | `--o-table-striped-bg` | `$table-bg-accent` | `var(--o-bg-subtle)` |
| Table | `--o-table-hover-bg` | - | `var(--o-bg-hover)` |
| Table | `--o-table-header` | - | `var(--o-text)` |
| Table | `--o-table-link` | - | `var(--o-link-subtle)` |
| Table | `--o-table-striped-link` | - | `var(--o-link-subtle)` |
| Table | `--o-table-details-bg` | `$o-table-details-bg` | `#F6FBFD` (base colour) |
| Table | `--o-table-details-container-bg` | `$o-table-details-container-bg` | `var(--o-white)` |
| Table | `--o-table-edit-border` | `$o-table-edit-border` | `#FF9900` (base colour) |
| Table | `--o-table-edit-bg` | `$o-table-edit-bg` | `#FEFBF6` (base colour) |
| Table | `--o-table-row-selected-bg` | `$o-table-row-selected-color` | `var(--o-state-neutral-soft-bg)` |
| Table | `--o-table-filter-collapse-border` | `$o-table-filter-collapse-color` | `#E4E7EC` (base colour) |
| Table | `--o-table-details-container-border` | `#E4E7EC` | `#E4E7EC` (base colour) |
| Table | `--o-table-sticky-col-bg` | `#FAFAFA` | `#FAFAFA` (base colour) |
| Table | `--o-table-sticky-col-striped-bg` | `#F5F5F5` | `#F5F5F5` (base colour) |
| Table | `--o-table-filter-fg` | `$o-table-filter-color` | `var(--o-btn-primary-border)` |
| Table | `--o-table-filter-bg` | `$o-table-filter-bg` | `var(--o-white)` |
| Table | `--o-table-filter-border` | `$o-table-filter-border` | `var(--o-btn-primary-border)` |
| Table | `--o-table-filter-active-fg` | `$o-table-filter-active-color` | `var(--o-white)` |
| Table | `--o-table-filter-active-bg` | `$o-table-filter-active-bg` | `var(--o-btn-primary-border)` |
| Table | `--o-table-filter-active-border` | `$o-table-filter-active-border` | `var(--o-btn-primary-border)` |
| Table | `--o-table-filter-fg-hover` | `o-contrast-shade-text($o-table-filter-color, $o-table-filter-bg, 10%)` (D13) | `var(--o-table-filter-fg)` |
| Table | `--o-table-filter-fg-active` | the same with 17% | `var(--o-table-filter-fg)` |
| Table | `--o-table-filter-active-fg-hover` | `o-contrast-shade-text($o-table-filter-active-color, $o-table-filter-active-bg, 10%)` (D13) | `var(--o-table-filter-active-fg)` |
| Table | `--o-table-filter-active-fg-active` | the same with 17% | `var(--o-table-filter-active-fg)` |
| Tags | `--o-tag-bg` | `$o-tag-background-color` | `var(--o-primary)` |
| Tags | `--o-tag-selected-bg` | `$o-tag-selected-background-color` | `var(--o-btn-dirty-bg)` |
| Tags | `--o-tag-selected-fg` | - | `var(--o-btn-dirty-fg)` |
| Tags | `--o-tag-selected-border` | `$o-tag-selected-border-color` | `var(--o-btn-dirty-border)` |
| Tags | `--o-tag-selection-bg` | `$o-tag-selection-background-color` | `var(--o-btn-default-bg)` |
| Tags | `--o-tag-selection-border` | `$o-tag-selection-border-color` | `var(--o-border-strong)` |
| Tags | `--o-tag-selection-fg` | - | `var(--o-btn-default-fg)` |
| Tags | `--o-tag-selection-selected-bg` | `$o-tag-selection-selected-background-color` | `var(--o-btn-primary-bg)` |
| Tags | `--o-tag-selection-selected-border` | `$o-tag-selection-selected-border-color` | `var(--o-btn-primary-border)` |
| Tags | `--o-tag-selection-selected-fg` | - | `var(--o-btn-primary-fg)` |
| Tags | `--o-tag-selection-remove-selected-bg` | `$o-tag-selection-remove-selected-background-color` | `var(--o-btn-danger-bg)` |
| Tags | `--o-tag-selection-remove-selected-border` | `$o-tag-selection-remove-selected-border-color` | `var(--o-btn-danger-border)` |
| Tags | `--o-tag-selection-remove-selected-fg` | `$o-tag-selection-remove-selected-font-color` | `var(--o-btn-danger-fg)` |
| Tags | `--o-tag-fg` | `$o-tag-font-color` | `var(--o-white)` |
| Timeline and swiper | `--o-swiper-button-bg` | - | `var(--o-btn-default-bg)` |
| Timeline and swiper | `--o-swiper-button-fg` | - | `var(--o-btn-default-fg)` |
| Timeline and swiper | `--o-swiper-button-hover` | - | `var(--o-primary)` |
| Timeline and swiper | `--o-swiper-theme` | `$o-swiper-color` | `var(--o-btn-primary-bg)` |
| Timeline and swiper | `--o-swiper-button-border` | `$btn-default-border` | `var(--o-btn-default-border)` |
| Timeline and swiper | `--o-timeline-path` | - | `var(--o-border)` |
| Timeline and swiper | `--o-timeline-row-bg` | `$o-v-timeline-row-bg` | `var(--o-white)` |
| Toolbar and breadcrumb | `--o-toolbar-breadcrumb-bg` | `$o-toolbar-breadcrumb-bg-color` | `var(--o-bg-subtle)` |
| Toolbar and breadcrumb | `--o-toolbar-close` | `$o-toolbar-close-color` | `var(--o-state-danger-text)` |
| Toolbar and breadcrumb | `--o-toolbar-breadcrumb-link` | - | `var(--o-link-subtle)` |
| Toolbar and breadcrumb | `--o-toolbar-dropdown-active` | - | `var(--o-link)` |
| Toolbar and breadcrumb | `--o-toolbar-link` | `$o-toolbar-link-color` | `var(--o-nav-link)` |
| Toolbar and breadcrumb | `--o-toolbar-link-hover` | `$o-toolbar-link-hover-color` | `var(--o-nav-link-hover)` |
| Toolbar and breadcrumb | `--o-toolbar-link-active` | `$o-toolbar-link-active-color` | `var(--o-link)` |
| Toolbar and breadcrumb | `--o-toolbar-nav-link` | - | `var(--o-toolbar-link)` |
| Toolbar and breadcrumb | `--o-toolbar-nav-link-hover` | - | `var(--o-toolbar-link-hover)` |
| Toolbar and breadcrumb | `--o-toolbar-link-disabled` | `$o-toolbar-link-disabled-color` | `var(--o-text-disabled)` |
| Toolbar and breadcrumb | `--o-toolbar-nav-link-disabled` | `$o-toolbar-nav-link-disabled-color` | `var(--o-toolbar-link-disabled)` |
| Toolbar and breadcrumb | `--o-toolbar-nav-border` | - | `var(--o-border-strong)` |
| Toolbar and breadcrumb | `--o-toolbar-border` | `$o-toolbar-border-color` | `#EAECF0` (base colour) |
| Toolbar and breadcrumb | `--o-toolbar-bg` | `$o-toolbar-bg-color` | `var(--o-white)` |
| Toolbar and breadcrumb | `--o-toolbar-link-active-bg` | `$o-toolbar-link-active-bg` | `transparent` (base colour) |
| Toolbar and breadcrumb | `--o-toolbar-dropdown-active-bg` | `$o-toolbar-dropdown-active-bg` | `transparent` (base colour) |
| Toolbar and breadcrumb | `--o-toolbar-nav-bg` | `$o-toolbar-nav-bg-color` | `#EEEEEE` (base colour) |
| Toolbar and breadcrumb | `--o-toolbar-edit-bg` | `$o-toolbar-edit-bg-color` | `var(--o-white)` |
| Toolbar and breadcrumb | `--o-toolbar-edit-stripe` | `$o-toolbar-edit-stripe-color` | `#FFF0D9` (base colour) |
| Wizard | `--o-wizard-step-main-active-bg` | - | `var(--o-btn-primary-bg)` |
| Wizard | `--o-wizard-step-main-active-fg` | - | `var(--o-btn-primary-fg)` |
| Wizard | `--o-wizard-step-sub-active-border` | - | `var(--o-primary)` |
| Wizard | `--o-wizard-step-sub-active` | - | `var(--o-text)` |
| Wizard | `--o-wizard-steps-border` | `$o-wizard-steps-border-color` | `#D0D5DD` (base colour) |
| Wizard | `--o-wizard-steps-bg` | `$o-wizard-steps-bg-color` | `#F9FAFB` (base colour) |
| Wizard | `--o-wizard-step-bg` | `$o-wizard-step-bg-color` | `#EAECF0` (base colour) |

**Component layer** (`$o-token-layer-component`, 426 tokens: 343 with a Sass default, 83 with a reference default):

| Group | Token | Sass default | Default in light |
|---|---|---|---|
| Absences and lectures | `--o-lecture-timeline-next-bg` | - | `var(--o-state-warning-soft-bg)` |
| Absences and lectures | `--o-lecture-timeline-next-border` | - | `var(--o-state-warning-border)` |
| Absences and lectures | `--o-lecture-timeline-running-bg` | - | `var(--o-state-neutral-soft-bg)` |
| Absences and lectures | `--o-lecture-timeline-running-border` | - | `var(--o-state-neutral-border)` |
| Absences and lectures | `--o-lecture-timeline-path` | - | `var(--o-border)` |
| Absences and lectures | `--o-lecture-rollcall-pending` | `o-state-color("inactive", "border", "light")` | `var(--o-slate-500)` |
| Absences and lectures | `--o-lecture-current-border` | `$o-lm-current-border-color` | `transparent` (base colour) |
| Absences and lectures | `--o-lecture-current-bg` | `$o-lm-current-bg` | `var(--o-white)` |
| Absences and lectures | `--o-lecture-excluded` | `$o-lm-excluded-color` | `var(--o-state-danger-text)` |
| Absences and lectures | `--o-lecture-timeline-row-bg` | `$o-lm-timeline-row-bg` | `var(--o-white)` |
| AI feedback | `--o-ai-mark-correct` | - | `var(--o-state-success-bg)` |
| AI feedback | `--o-ai-mark-ambiguous` | - | `var(--o-state-warning-bg)` |
| AI feedback | `--o-ai-mark-wrong` | - | `var(--o-state-danger-bg)` |
| Appointments | `--o-appointment-planned` | `$o-ap-planned-color` | `var(--o-state-warning-bg)` |
| Appointments | `--o-appointment-select-icon` | `$o-ap-select-icon-color` | `var(--o-state-warning-fg)` |
| Appointments | `--o-appointment-confirmed` | `$o-ap-confirmed-color` | `var(--o-state-success-bg)` |
| Calendar | `--o-cal-desc-bg` | `$o-cal-desc-background-color` | `var(--o-white)` |
| Badges | `--o-badge-row-expanded-link` | `o-a11y-color($link-color, $o-table-details-bg)` | `var(--o-link)` |
| Badges | `--o-badge-row-expanded-link-hover` | `o-a11y-color($link-color, darken($o-table-details-bg, 10%))` | `#2B64B3` (base colour) |
| Badges | `--o-badge-status-info` | `o-a11y-color(o-state-color("neutral", "text"), $o-toolbar-bg-color)` | `var(--o-state-neutral-text)` |
| Badges | `--o-badge-status-success` | `o-a11y-color(o-state-color("success", "text"), $o-toolbar-bg-color)` | `var(--o-state-success-text)` |
| Badges | `--o-badge-status-warning` | `o-a11y-color(o-state-color("warning", "text"), $o-toolbar-bg-color)` | `var(--o-state-warning-text)` |
| Badges | `--o-badge-status-danger` | `o-a11y-color(o-state-color("danger", "text"), $o-toolbar-bg-color)` | `var(--o-state-danger-text)` |
| Badges | `--o-badge-template-selected-bg` | `#DDDDDD` | `var(--o-gray-200)` |
| Badges | `--o-export-area-meta` | `$o-export-area-meta-color` | `#777777` (base colour) |
| Credit points | `--o-creditpoint-withdraw` | `$o-cp-withdraw-color` | `var(--o-state-danger-text)` |
| Credit points | `--o-creditpoint-deposit` | `$o-cp-deposit-color` | `var(--o-state-success-text)` |
| Course quota | `--o-course-quota-intern-quota` | `$o-course-quota-progress-iq-text-color` | `var(--o-cyan-500)` |
| Course quota | `--o-course-quota-intern` | `$o-course-quota-progress-i-text-color` | `var(--o-orange-500)` |
| Course quota | `--o-course-quota-extern` | `$o-course-quota-progress-ex-text-color` | `var(--o-magenta-700)` |
| Course quota | `--o-course-quota-intern-quota-bg` | `$o-course-quota-progress-iq-bg-color` | `var(--o-cyan-400)` |
| Course quota | `--o-course-quota-intern-bg` | `$o-course-quota-progress-i-bg-color` | `var(--o-orange-400)` |
| Course quota | `--o-course-quota-extern-bg` | `$o-course-quota-progress-ex-bg-color` | `var(--o-magenta-700)` |
| Contact tracing | `--o-contact-tracing-confirmation-bg` | `$o-ct-confirmation-bg-color` | `#32CD32` (base colour) |
| Course element File dialog | `--o-dialog-cn-label-new-border` | `$o-dialog-cn-label-new-border-color` | `#555555` (base colour) |
| Course element File dialog | `--o-dialog-cn-label-new` | `$o-dialog-cn-label-new-color` | `#8A7400` (base colour) |
| To-dos | `--o-todo-overdue` | `$o-todo-overdue-color` | `var(--o-state-danger-text)` |
| Certification | `--o-certification-certified` | `$o-base-green-color` | `#268000` (base colour) |
| Certification | `--o-certification-certifying` | `$o-base-blue-color` | `#105CAD` (base colour) |
| Chat | `--o-im-summary-bg` | `$o-im-summary-bg` | `#667085` (base colour) |
| Chat | `--o-im-buddies-link` | - | `var(--o-link)` |
| Chat | `--o-im-buddies-link-hover` | - | `var(--o-link-hover)` |
| Chat | `--o-im-chat-date` | - | `var(--o-text-muted)` |
| Chat | `--o-im-groupchat-user` | - | `var(--o-text)` |
| Chat | `--o-im-chat-from` | `$o-im-chat-from-color` | `#777777` (base colour) |
| Chat | `--o-im-chat-roster-bg` | `$o-im-chat-roster-bg` | `#FBFBFB` (base colour) |
| Chat | `--o-im-summary-fg` | `$o-im-summary-color` | `var(--o-white)` |
| Chat | `--o-im-chat-border` | `$o-im-chat-border` | `#EEEEEE` (base colour) |
| Chat | `--o-im-chat-history-border` | `$o-im-chat-history-border` | `#EEEEEE` (base colour) |
| Chat | `--o-im-chat-message-bg` | `$o-im-chat-message-bg` | `var(--o-white)` |
| Chat | `--o-im-chat-message-status-bg` | `$o-im-chat-message-bg-status` | `var(--o-state-warning-soft-bg)` |
| Chat | `--o-im-chat-message-error-bg` | `$o-im-chat-message-bg-error` | `var(--o-state-danger-soft-bg)` |
| Chat | `--o-im-chat-message-me-bg` | `$o-im-chat-message-bg-me` | `#EEEEEE` (base colour) |
| Chat | `--o-im-groupchat-vip` | `$o-im-groupchat-vip-color` | `var(--o-state-success-text)` |
| Chat | `--o-im-groupchat-anonymous` | `$o-im-groupchat-anonymous-color` | `var(--o-state-neutral-text)` |
| Chat | `--o-im-newmessage` | `$o-im-newmessage-color` | `var(--o-state-danger-text)` |
| Chat | `--o-im-newmessage-hover` | `$o-im-newmessage-hover-color` | `var(--o-state-warning-text)` |
| Chat | `--o-im-status-available` | `$o-im-status-available-color` | `var(--o-state-success-text)` |
| Chat | `--o-im-status-dnd` | `$o-im-status-dnd-color` | `var(--o-state-critical-text)` |
| Chat | `--o-im-status-unavailable` | `$o-im-status-unavailable-color` | `var(--o-state-danger-text)` |
| Competences | `--o-competence-selected` | `o-a11y-color($o-button-dirty-color, darken(#FFFFFF, 15%))`: the text of the selected tag, adjusted to 4.5:1 on the pressed competence fill | `var(--o-tag-selected-fg)` |
| Competences | `--o-competence-selected-border` | `$o-competences-selected-border-color` | `var(--o-btn-dirty-border)` |
| Competences | `--o-competence-bg` | `#FFFFFF` | `var(--o-white)` |
| Competences | `--o-competence-border` | `$o-competences-color` | `#295FAA` (base colour) |
| Competences | `--o-competence-fg` | `$o-competences-font-color` | `var(--o-competence-border)` |
| Content editor | `--o-editor-inspector-title` | - | `var(--o-primary)` |
| Content editor | `--o-editor-layout-pseudo-icon-bg-active` | - | `var(--o-primary)` |
| Content editor | `--o-editor-selected-border` | `$o-editor-selected-border-color` | `var(--o-primary)` |
| Content editor | `--o-editor-selected-bg` | - | `var(--o-primary)` |
| Content editor | `--o-editor-media-unavailable` | `$gray-lighter` | `#EEEEEE` (base colour) |
| Content editor | `--o-editor-part-toolbar-link` | `#FFFFFF` | `var(--o-white)` |
| Content editor | `--o-editor-image-shadow-border` | `$gray` | `#555555` (base colour) |
| Content editor | `--o-editor-border` | `$o-editor-border-color` | `#D0D5DD` (base colour) |
| Content editor | `--o-editor-hover-text` | `$o-editor-border-color` | `var(--o-editor-border)` |
| Content editor | `--o-editor-inspector-border` | `$o-editor-inspector-border-color` | `var(--o-editor-border)` |
| Content editor | `--o-editor-layout-inner-h-border` | `$o-editor-border-color` | `var(--o-editor-border)` |
| Content editor | `--o-editor-layout-inner-v-border` | `$o-editor-border-color` | `var(--o-editor-border)` |
| Content editor | `--o-editor-layout-pseudo-icon-border` | `$o-editor-layout-pseudo-icon-border-color` | `#808080` (base colour) |
| Content editor | `--o-editor-layout-pseudo-icon-bg` | `$o-editor-layout-pseudo-icon-bg-color` | `#EAECF0` (base colour) |
| Content editor | `--o-editor-container-border` | `$o-editor-container-border-color` | `#EAECF0` (base colour) |
| Content editor | `--o-editor-hover-border` | `$o-editor-hover-border-color` | `#EF00EF` (base colour) |
| Content editor | `--o-editor-edit-bg` | `$o-editor-edit-background-color` | `#F2F4F7` (base colour) |
| Content editor | `--o-editor-alert-box-active-border` | `$o-color-light-gray` | `var(--o-gray-700)` |
| Content editor | `--o-page-code-box-bg` | `color.mix($o-color-light-gray, #FFFFFF, 5%)` | `#F6F6F6` (base colour) |
| Content editor | `--o-page-table-border` | `$gray-base` | `#000000` (base colour) |
| Content editor | `--o-milkdown-table-border` | `$gray-base` | `#000000` (base colour) |
| Content editor | `--o-milkdown-table-header-bg` | `$gray-base` | `#000000` (base colour) |
| Course | `--o-course-style-bg` | `$o-cn-style-bg` | `#F9FAFB` (base colour) |
| Course | `--o-course-style-border` | `#E4E7EC`: the colour of the old shorthand `$o-cn-style-border`, follows `border`; width and style are `$o-cn-style-border-line-width` (1px) and `$o-cn-style-border-line-style` (solid) (D19) | `var(--o-border)` |
| Course | `--o-course-style-image-bg` | `$o-cn-style-image-bg` | `var(--o-primary)` |
| Course | `--o-course-style-link` | - | `var(--o-link-subtle)` |
| Course | `--o-course-style-peekview-link` | `o-a11y-color($link-color, mix(...))`: the peekview background `$o-cn-style-peekview-bg` (translucent) on `$o-cn-style-bg` | `var(--o-link)` |
| Course | `--o-course-assessment-part-bg` | - | `var(--o-bg-subtle)` |
| Course | `--o-assessment-mode-today-border` | `$o-assessment-mode-today-border-color` | `var(--o-state-success-text)` |
| Course | `--o-assessment-mode-today-bg` | `$o-assessment-mode-today-bg` | `var(--o-state-success-soft-bg)` |
| Course | `--o-assessment-mode-end-soon-border` | `$o-assessment-mode-end-soon-border-color` | `var(--o-state-danger-text)` |
| Course | `--o-assessment-mode-end-soon-bg` | `$o-assessment-mode-end-soon-bg` | `var(--o-state-danger-soft-bg)` |
| Course | `--o-assessment-inspection-border` | `#D0D5DD` | `#D0D5DD` (base colour) |
| Course | `--o-course-state-failed` | - | `var(--o-state-danger-text)` |
| Course | `--o-course-state-passed` | `$o-course-state-passed-color` | `var(--o-state-success-text)` |
| Course | `--o-course-state-unknown` | - | `var(--o-state-warning-text)` |
| Course | `--o-course-file-box-border` | `$o-dialog-cn-box-border-color` | `var(--o-border)` |
| Course | `--o-course-node-deleted` | `$o-course-node-deleted` | `var(--o-text-disabled)` |
| Course | `--o-course-style-cover-text-bg` | `$o-cn-style-cover-text-bg` | `color-mix(in srgb, var(--o-scrim) 95%, transparent)` (base value) |
| Course | `--o-edu-type-standard-course` | `$o-edu-type-standard-course-color` | `#555555` (base colour) |
| Course | `--o-edu-type-exam-course` | `$o-edu-type-exam-course-color` | `var(--o-red-500)` |
| Course | `--o-edu-type-certification-course` | `$o-edu-type-certification-course-color` | `var(--o-orange-500)` |
| Course | `--o-edu-type-date-course` | `$o-edu-type-date-course-color` | `var(--o-magenta-700)` |
| Course | `--o-edu-type-seminar-course` | `$o-edu-type-seminar-course-color` | `var(--o-cyan-500)` |
| Course | `--o-edu-type-lecture-course` | `$o-edu-type-lecture-course-color` | `var(--o-lime-500)` |
| Course | `--o-edu-type-blendedlearning-course` | `$o-edu-type-blendedlearning-course-color` | `var(--o-yellow-500)` |
| Course | `--o-edu-type-selfstudy-course` | `$o-edu-type-selfstudy-course-color` | `var(--o-green-500)` |
| Course | `--o-lp-in-progress` | `$o-lp-in-progress` | `var(--o-state-neutral-text)` |
| Course | `--o-lp-pagination-in-progress-border` | - | `var(--o-state-neutral-text)` |
| Course | `--o-lp-pagination-in-progress-icon` | - | `var(--o-state-neutral-text)` |
| Course | `--o-lp-pagination-in-progress-icon-hover` | - | `var(--o-state-neutral-text)` |
| Course | `--o-lp-border` | `$navbar-default-border` | `#EAECF0` (base colour) |
| Course | `--o-lp-bg` | `$o-lp-bg` | `color-mix(in srgb, var(--o-backdrop) 2.5%, transparent)` (base value) |
| Course | `--o-lp-icon-bg` | `$o-lp-icon-bg-color` | `var(--o-white)` |
| Course | `--o-lp-pagination-done-bg` | `$o-lp-pagination-done-bgcolor` | `var(--o-state-success-text)` |
| Course | `--o-lp-pagination-done-border` | `$o-lp-pagination-done-border-color` | `#2E5C2E` (base colour) |
| Course | `--o-lp-pagination-done-fg` | `$o-lp-pagination-done-color` | `var(--o-white)` |
| Course | `--o-lp-pagination-in-progress-bg` | `$o-lp-pagination-in-progress-bgcolor` | `color-mix(in srgb, var(--o-lp-in-progress) 20%, transparent)` (base value) |
| Course | `--o-lp-pagination-in-progress-icon-bg` | `$o-lp-pagination-in-progress-icon-bgcolor` | `#FCFCFC` (base colour) |
| Course | `--o-reminder-marker-bg` | - | `var(--o-btn-primary-bg)` |
| Course | `--o-reminder-marker-fg` | - | `var(--o-btn-primary-fg)` |
| Course | `--o-course-peekview-bg` | - | `var(--o-bg)` |
| Course | `--o-course-style-peekview-bg` | `$o-cn-style-peekview-bg` | `rgba(16, 24, 40, 0.02)` (base colour) |
| Course element Task | `--o-step-active` | - | `var(--o-state-warning-border)` |
| Course element Task | `--o-step-done` | - | `var(--o-state-success-border)` |
| Course element Task | `--o-step-done-marker` | - | `var(--o-step-done)` |
| Course element Task | `--o-step-expired` | - | `var(--o-state-danger-border)` |
| Course element Task | `--o-step-status-expired` | - | `var(--o-state-danger-text)` |
| Course element Task | `--o-step-status-extended` | - | `var(--o-state-neutral-text)` |
| Course element Task | `--o-step-status-late` | - | `var(--o-state-critical-text)` |
| Process steps | `--o-step-border` | `#E4E7EC` | `#E4E7EC` (base colour) |
| Process steps | `--o-step-border-header-bg` | `$o-step-border-header-bg-color` | `#F9FAFB` (base colour) |
| Process steps | `--o-step-marker` | `$o-step-color` | `#999999` (base colour) |
| Process steps | `--o-step-active-marker` | `$o-step-active-marker-color` | `var(--o-step-marker)` |
| Process steps | `--o-step-notavailable` | `$o-step-notavailable-color` | `var(--o-step-marker)` |
| Process steps | `--o-step-notavailable-marker` | `$o-step-notavailable-marker-color` | `var(--o-step-notavailable)` |
| Process steps | `--o-step-expired-marker` | `$o-step-expired-marker-color` | `var(--o-step-marker)` |
| Course site and repository | `--o-coursesite-start-bg` | `$o-coursesite-start-bg` | `var(--o-btn-primary-bg)` |
| Course site and repository | `--o-coursesite-start-fg` | `$o-coursesite-start-color` | `var(--o-white)` |
| Course site and repository | `--o-coursesite-start-border` | `$o-coursesite-start-border` | `#295FAA` (base colour) |
| Course site and repository | `--o-coursesite-book-bg` | `$o-coursesite-book-bg` | `var(--o-state-warning-bg)` |
| Course site and repository | `--o-coursesite-book-fg` | `$o-coursesite-book-color` | `var(--o-state-warning-fg)` |
| Course site and repository | `--o-coursesite-book-border` | `$o-coursesite-book-border` | `#DFA81D` (base colour) |
| Course site and repository | `--o-coursesite-border` | `$o-coursesite-rowelem-border` | `var(--o-border-strong)` |
| Course site and repository | `--o-coursesite-title` | `$o-coursesite-meta-title-color` | `var(--o-heading)` |
| Course site and repository | `--o-coursesite-catalog-border` | `$o-coursesite-catalog-border` | `var(--o-coursesite-border)` |
| Course site and repository | `--o-coursesite-start-xs-bg` | - | `var(--o-coursesite-start-bg)` |
| Course site and repository | `--o-coursesite-start-xs-fg` | `$o-coursesite-go-xs-color` | `var(--o-coursesite-start-fg)` |
| Course site and repository | `--o-coursesite-meta-lifecycle` | - | `var(--o-text)` |
| Course site and repository | `--o-coursesite-meta-author` | `$o-coursesite-meta-author-color` | `var(--o-text)` |
| Course site and repository | `--o-coursesite-sidecol-bg` | - | `var(--o-bg-subtle)` |
| Course site and repository | `--o-coursesite-sidecol-border` | - | `var(--o-border)` |
| Course site and repository | `--o-coursesite-row-hover` | `$gray-light` | `#777777` (base colour) |
| Course site and repository | `--o-coursesite-row-bg` | `$o-coursesite-rowelem-bg` | `var(--o-white)` |
| Course site and repository | `--o-coursesite-row-hover-bg` | `$o-coursesite-rowelem-hover-bg` | `color-mix(in srgb, var(--o-coursesite-row-hover) 1%, transparent)` (base value) |
| Course site and repository | `--o-coursesite-progress-icon` | `$progress-bar-neutral-bg` | `var(--o-progress-bar-neutral-bg)` |
| Course site and repository | `--o-entry-benefit-border` | `$o-entry-benefit-border-color` | `var(--o-blue-500)` |
| Course site and repository | `--o-entry-benefit` | `$o-entry-benefit-color` | `var(--o-blue-500)` |
| Course site and repository | `--o-catalog-header-box-bg` | `$o-cat2-header-box-bg-color` | `color-mix(in srgb, var(--o-scrim) 70%, transparent)` (base value) |
| Course site and repository | `--o-repo-info-border` | `#D4D4D4` | `#D4D4D4` (base colour) |
| Course site and repository | `--o-repo-info-separator-border` | `#D4D4D4` | `#D4D4D4` (base colour) |
| Course site and repository | `--o-ac-billing-address-bg` | `$o-ac-ba-bg-color` | `#F9FAFB` (base colour) |
| Course site and repository | `--o-ac-billing-address-border` | `#D0D5DD` | `#D0D5DD` (base colour) |
| Course site and repository | `--o-ac-billing-address-temporary-bg` | `$o-ac-ba-no-ident-bg-color` | `#FEFDF8` (base colour) |
| Curriculum | `--o-curriculum-avatar-bg` | - | `var(--o-primary)` |
| Curriculum | `--o-curriculum-widget-template-label` | - | `var(--o-text)` |
| Curriculum | `--o-curriculum-element-inactive` | `$o-curriculum-element-inactive-color` | `#777777` (base colour) |
| Curriculum | `--o-curriculum-avatar-fg` | `$o-curriculum-avatar-color` | `var(--o-white)` |
| Curriculum | `--o-curriculum-widget-card-border` | `#D0D5DD` | `#D0D5DD` (base colour) |
| Curriculum | `--o-curriculum-widget-border` | `#E4E7EC` | `#E4E7EC` (base colour) |
| Curriculum | `--o-curriculum-widget-bg` | `$o-curriculum-widget-bg-color` | `var(--o-panel-bg)` |
| Curriculum | `--o-curriculum-widget-template-label-bg` | `$o-curriculum-widget-template-label-bg` | `var(--o-white)` |
| Curriculum | `--o-curriculum-import-error-bg` | `$o-curriculum-import-bg-error` | `#FFF8F6` (base colour) |
| Curriculum | `--o-curriculum-import-new-bg` | `$o-curriculum-import-bg-new` | `#F3F9F5` (base colour) |
| Curriculum | `--o-curriculum-import-changed-bg` | `$o-curriculum-import-bg-changed` | `#FCFCFC` (base colour) |
| Curriculum | `--o-curriculum-import-ignored-bg` | `$o-curriculum-import-bg-ignored` | `#FAFAFB` (base colour) |
| Evaluation form and quality | `--o-evaluation-insufficient` | `$o-evaluation-insufficient-color` | `var(--o-state-danger-bg)` |
| Evaluation form and quality | `--o-evaluation-insufficient-text` | `$o-evaluation-insufficient-color`; with its default the text of the state `danger` | `var(--o-state-danger-text)` |
| Evaluation form and quality | `--o-evaluation-insufficient-fill` | `$o-evaluation-insufficient-fill` | `var(--o-red-200)` |
| Evaluation form and quality | `--o-evaluation-neutral` | `$o-evaluation-neutral-color` | `var(--o-state-warning-bg)` |
| Evaluation form and quality | `--o-evaluation-neutral-text` | `$o-evaluation-neutral-color`; with its default the text of the state `warning` | `var(--o-state-warning-text)` |
| Evaluation form and quality | `--o-evaluation-neutral-fill` | `$o-evaluation-neutral-fill` | `var(--o-yellow-200)` |
| Evaluation form and quality | `--o-evaluation-sufficient` | `$o-evaluation-sufficient-color` | `var(--o-state-success-bg)` |
| Evaluation form and quality | `--o-evaluation-sufficient-text` | `$o-evaluation-sufficient-color`; with its default the text of the state `success` | `var(--o-state-success-text)` |
| Evaluation form and quality | `--o-evaluation-sufficient-fill` | `$o-evaluation-sufficient-fill` | `var(--o-green-200)` |
| Evaluation form and quality | `--o-evaluation-step-bg` | `$o-evaluation-step-background-color` | `var(--o-table-striped-bg)` |
| Evaluation form and quality | `--o-evaluation-slider-hover-bg` | `$o-evaluation-slider-hover-background-color` | `var(--o-table-hover-bg)` |
| Evaluation form and quality | `--o-quality-heatmap-base-bg` | `$o-qual-hm-base-background-color` | `#337AB7` (base colour) |
| Evaluation form and quality | `--o-quality-unrated` | `$o-qual-hm-base-background-color` | `var(--o-quality-heatmap-base-bg)` |
| Folder | `--o-folder-card-thumbnail-folder-icon` | - | `var(--o-primary)` |
| Folder | `--o-folder-muted` | - | `var(--o-text-muted)` |
| Folder | `--o-folder-row-thumbnail-folder-icon` | - | `var(--o-primary)` |
| Folder | `--o-folder-row-thumbnail-image-border` | `#E4E7EC` | `#E4E7EC` (base colour) |
| Folder | `--o-folder-card-thumbnail-icon` | `$o-folder-card-thumbnail-icon-color` | `#CCCCCC` (base colour) |
| Forum | `--o-forum-message-footer-bg` | - | `var(--o-forum-message-header-bg)` |
| Forum | `--o-forum-message-footer-border` | - | `var(--o-forum-message-header-border)` |
| Forum | `--o-forum-message-header-bg` | `$o-forum-message-header-bg` | `var(--o-bg-subtle)` |
| Forum | `--o-forum-message-header-border` | - | `var(--o-border)` |
| Forum | `--o-forum-message-header-author` | - | `var(--o-link-subtle)` |
| Forum | `--o-forum-message-header-modified` | `$o-forum-message-header-modified-color` | `var(--o-state-warning-text)` |
| Forum | `--o-forum-message-footer-attachment` | - | `var(--o-forum-message-header-author)` |
| Forum | `--o-forum-quote-author` | - | `var(--o-text-muted)` |
| Forum | `--o-forum-quote` | `$o-forum-quote-color` | `#555555` (base colour) |
| Forum | `--o-citation-quote` | `$o-cit-quote-color` | `#555555` (base colour) |
| Forum | `--o-forum-message-new-glow` | - | `var(--o-state-success-bg)` |
| Forum | `--o-forum-message-highlight-glow` | - | `var(--o-state-warning-bg)` |
| High score | `--o-highscore-bar-myself` | - | `var(--o-primary)` |
| High score | `--o-highscore-score` | - | `var(--o-text-muted)` |
| High score | `--o-highscore-podium-first-bg` | `$o-hs-podium-first-bg` | `#FFD700` (base colour) |
| High score | `--o-highscore-position-title` | `o-hs-position-title-color()` | `var(--o-text)` |
| High score | `--o-highscore-podium-second-bg` | `$o-hs-podium-second-bg` | `#C0C0C0` (base colour) |
| High score | `--o-highscore-podium-third-bg` | `$o-hs-podium-third-bg` | `#CD7F32` (base colour) |
| High score | `--o-highscore-bar-myself-hover` | `$o-hs-histogram-bar-myself-hover-color` | `#4C85D4` (base colour) |
| High score | `--o-highscore-bar-other-hover` | `$o-hs-histogram-bar-other-hover-color` | `#919191` (base colour) |
| High score | `--o-highscore-podium-border` | `#D9D9D9` | `#D9D9D9` (base colour) |
| High score | `--o-highscore-rank-first` | `$o-hs-rank-first-color` | `#666666` (base colour) |
| High score | `--o-highscore-rank-second` | `$o-hs-rank-second-color` | `#666666` (base colour) |
| High score | `--o-highscore-rank-third` | `$o-hs-rank-third-color` | `#666666` (base colour) |
| High score | `--o-highscore-histogram-text` | `$o-hs-histogram-text-color` | `#888888` (base colour) |
| High score | `--o-highscore-histogram-axis` | `$o-hs-histogram-axis-color` | `#888888` (base colour) |
| High score | `--o-highscore-bar-other` | `$o-hs-histogram-bar-other-color` | `#777777` (base colour) |
| High score | `--o-highscore-bar-empty` | `$o-hs-histogram-bar-empty-color` | `#000000` (base colour) |
| Immunity proof | `--o-immunity-proof-validated-bg` | `$o-ip-validated-bg-color` | `#61E868` (base colour) |
| Immunity proof | `--o-immunity-proof-claimed-bg` | `$o-ip-claimed-bg-color` | `#FFB100` (base colour) |
| Immunity proof | `--o-immunity-proof-none-bg` | `$o-ip-none-bg-color` | `#E82A0C` (base colour) |
| Portfolio | `--o-portfolio-lead-bg` | `$o-portfolio-lead-background-color` | `#F2F2F2` (base colour) |
| Portfolio | `--o-portfolio-lead-border` | `$o-portfolio-lead-border-color` | `#D9D9D9` (base colour) |
| Portfolio | `--o-portfolio-lead-ended-bg` | `$o-portfolio-lead-ended-background-color` | `#EEB1B4` (base colour) |
| Portfolio | `--o-portfolio-lead-ended-border` | `$o-portfolio-lead-ended-border-color` | `#E5878C` (base colour) |
| Projects | `--o-proj-avatar-fg` | `$o-proj-avatar-color` | `var(--o-white)` |
| Projects | `--o-proj-timeline-overdue` | `$o-proj-timeline-overdue-color` | `var(--o-state-danger-bg)` |
| Projects | `--o-proj-timeline-today` | `$o-proj-timeline-today-color` | `var(--o-state-neutral-bg)` |
| Portfolio | `--o-portfolio-assignment` | `$o-portfolio-assignment-color` | `var(--o-state-danger-bg)` |
| Portfolio | `--o-portfolio-assignment-bg` | `$o-portfolio-assignment-background-color` | `var(--o-state-danger-soft-bg)` |
| Portfolio | `--o-portfolio-media-content-bg` | `$o-portfolio-media-content-background-color` | `#F9FAFB` (base colour) |
| Portfolio | `--o-media-version-label-border` | `$o-media-version-label-color` | `var(--o-state-neutral-text)` |
| Portfolio | `--o-media-version-label-fg` | `$o-media-version-label-text` | `var(--o-state-neutral-text)` |
| Portfolio | `--o-media-version-label-bg` | `$o-media-version-label-bg` | `var(--o-state-neutral-soft-bg)` |
| Practice | `--o-practice-challenge-progress-bg` | - | `var(--o-primary)` |
| Practice | `--o-practice-level-disc` | - | `var(--o-primary)` |
| Practice | `--o-practice-play-main-border` | - | `var(--o-primary)` |
| Practice | `--o-practice-rank-me` | - | `var(--o-primary)` |
| Practice | `--o-practice-icon` | `#CCCCCC` | `#CCCCCC` (base colour) |
| Practice | `--o-practice-progress-not-bg` | `$o-practice-progress-not-bg-color` | `#CCCCCC` (base colour) |
| Practice | `--o-practice-progress-l1-bg` | `$o-practice-progress-l1-bg-color` | `var(--o-cyan-400)` |
| Practice | `--o-practice-progress-l2-bg` | `$o-practice-progress-l2-bg-color` | `var(--o-magenta-700)` |
| Practice | `--o-practice-progress-l3-bg` | `$o-practice-progress-l3-bg-color` | `var(--o-orange-400)` |
| Practice | `--o-practice-progress-l4-bg` | `$o-practice-progress-l4-bg-color` | `var(--o-yellow-400)` |
| Practice | `--o-practice-progress-l5-bg` | `$o-practice-progress-l5-bg-color` | `var(--o-lime-400)` |
| Practice | `--o-practice-play-bg` | `$o-practice-play-bg-color` | `#F9FAFB` (base colour) |
| Practice | `--o-practice-play-border` | `$o-practice-play-border-color` | `#E4E7EC` (base colour) |
| Practice | `--o-practice-play-main-bg` | `$o-practice-play-main-bg-color` | `color-mix(in srgb, var(--o-primary) 10%, transparent)` (base value) |
| Practice | `--o-practice-play-start-link-bg` | `$o-practice-play-start-link-bg-color` | `#F2F4F7` (base colour) |
| Rating | `--o-rating-star` | `o-palette("yellow", 400)` | `var(--o-yellow-400)` |
| Room management | `--o-room-status-inactive` | `$gray-light` | `#777777` (base colour) |
| Users and authentication | `--o-user-info-portrait-border` | `#FFFFFF` | `var(--o-white)` |
| Users and authentication | `--o-user-initials-fg` | `$o-user-initials-color` | `var(--o-white)` |
| Users and authentication | `--o-user-initials-base-bg` | `$o-user-initials-base-bg-color` | `#667085` (base colour) |
| Users and authentication | `--o-user-info-profile-border` | `#CCCCCC` | `#CCCCCC` (base colour) |
| Users and authentication | `--o-user-status-inactive-border` | `o-state-color("inactive", "border", "tint")` | `var(--o-slate-500)` |
| Users and authentication | `--o-user-status-deleted-border` | `o-state-color("inactive", "border", "dark")` | `var(--o-slate-800)` |
| Users and authentication | `--o-user-info-border` | `#D4D4D4` | `#D4D4D4` (base colour) |
| Users and authentication | `--o-user-info-divider-border` | - | `var(--o-user-info-border)` |
| Users and authentication | `--o-panel-authentication-border` | `#E4E7EC` | `#E4E7EC` (base colour) |
| Users and authentication | `--o-authentication-level1` | `$o-authentication-color-level1` | `#793E02` (base colour) |
| Users and authentication | `--o-authentication-level2` | `$o-authentication-color-level2` | `#0E375F` (base colour) |
| Users and authentication | `--o-authentication-level3` | `$o-authentication-color-level3` | `#274E15` (base colour) |
| Selectus | `--o-selectus-review-glow` | - | `var(--o-primary)` |
| Selectus | `--o-selectus-review-comment-link` | - | `var(--o-link)` |
| Selectus | `--o-selectus-review-comments-bg` | `$navbar-default-bg` | `var(--o-bg)` |
| Selectus | `--o-selectus-review-comments-border` | `$navbar-default-border` | `#EAECF0` (base colour) |
| Selectus | `--o-selectus-onhold` | `$o-selectus-onhold-color` | `#9E9E9E` (base colour) |
| Selectus | `--o-selectus-review-comment-glow` | `$o-selectus-review-comment-glowing-color` | `var(--o-state-warning-bg)` |
| Selectus | `--o-selectus-review-comment-date` | `$o-selectus-review-comment-date-color` | `#777777` (base colour) |
| Selectus | `--o-selectus-mail-disabled` | `$o-selectus-mail-disabled-color` | `#9E9E9E` (base colour) |
| Selectus | `--o-selectus-rating-a-bg` | `green` | `#008000` (base colour) |
| Selectus | `--o-selectus-rating-a-fg` | `white` | `var(--o-white)` |
| Selectus | `--o-selectus-rating-b-bg` | `#FFCD00` | `#FFCD00` (base colour) |
| Selectus | `--o-selectus-rating-b-fg` | `o-a11y-color(white, darken(#FFCD00, 17%))`: dark text, adjusted to 4.5:1 on the pressed fill | `#242424` (base colour) |
| Selectus | `--o-selectus-rating-c-bg` | `o-a11y-color(red, white)`: the red fill, darkened until the white text has 4.5:1 | `#EB0000` (base colour) |
| Selectus | `--o-selectus-rating-c-fg` | `white` | `var(--o-white)` |
| Selectus | `--o-selectus-rating-abstain-bg` | `#555566` | `#555566` (base colour) |
| Selectus | `--o-selectus-rating-abstain-fg` | `white` | `var(--o-white)` |
| Selectus | `--o-selectus-rating-b-text` | `o-a11y-color(#FFCD00, o-darkest-color($body-bg, $table-bg-accent, $table-bg-hover))`: the letter B in the explanation of the rating policy, 4.5:1 on a white, a striped and a hovered table row (D18, D26) | `#856B00` (base colour) |
| Selectus | `--o-selectus-rating-b-marker` | `o-a11y-color(#FFCD00, o-darkest-color($body-bg, $table-bg-accent, $table-bg-hover), 3)`: the mark of rating B in the overview, 3:1 on the three rows (D18, D26) | `#A88700` (base colour) |
| Selectus | `--o-selectus-rating-c-text` | `o-a11y-color(o-a11y-color(red, white), o-darkest-color($body-bg, $table-bg-accent, $table-bg-hover))`: the letter C in the explanation of the rating policy, the fill of button C adjusted to 4.5:1 on the three rows (D26) | `#E00000` (base colour) |
| Selectus | `--o-selectus-decision-a-bg` | `$o-selectus-table-a-decision-bg` | `#C7F1C7` (base colour) |
| Selectus | `--o-selectus-decision-b-bg` | `$o-selectus-table-b-decision-bg` | `#FCFED7` (base colour) |
| Selectus | `--o-selectus-decision-c-bg` | `$o-selectus-table-c-decision-bg` | `#F7E5D7` (base colour) |
| Selectus | `--o-selectus-decision-a-hover-bg` | `$o-selectus-table-a-decision-bg-hover` | `#B3D9B3` (base colour) |
| Selectus | `--o-selectus-decision-b-hover-bg` | `$o-selectus-table-b-decision-bg-hover` | `#E3E5C2` (base colour) |
| Selectus | `--o-selectus-decision-c-hover-bg` | `$o-selectus-table-c-decision-bg-hover` | `#DECEC2` (base colour) |
| Selectus | `--o-selectus-status-preparation` | `#949494` | `#949494` (base colour) |
| Selectus | `--o-selectus-status-published` | `#006E0D` | `#006E0D` (base colour) |
| Selectus | `--o-selectus-status-published-screening` | `#17882E` | `#17882E` (base colour) |
| Selectus | `--o-selectus-status-screening` | `#E7C33F` | `#E7C33F` (base colour) |
| Selectus | `--o-selectus-status-closed-screening` | `#DE5A3E` | `#DE5A3E` (base colour) |
| Selectus | `--o-selectus-status-closed` | `#BD1F10` | `#BD1F10` (base colour) |
| Selectus | `--o-selectus-status-reporting` | `#303030` | `#303030` (base colour) |
| Selectus | `--o-selectus-status-deactivated` | `#CCCCCC` | `#CCCCCC` (base colour) |
| Tests and question pool | `--o-drawing-border` | - | `var(--o-primary)` |
| Tests and question pool | `--o-drawing-select-border` | - | `var(--o-primary)` |
| Tests and question pool | `--o-drawing-select-bg` | `$o-drawing-select-bg` | `color-mix(in srgb, var(--o-primary) 50%, transparent)` (base value) |
| Tests and question pool | `--o-qpool-arrow-selected-bg` | - | `var(--o-navbar-link-active-bg)` |
| Tests and question pool | `--o-qpool-arrow-selected-fg` | - | `var(--o-navbar-link-active)` |
| Tests and question pool | `--o-qpool-arrow-bg` | `$o-qpool-arrow-bg-color` | `var(--o-white)` |
| Tests and question pool | `--o-qpool-arrow-draft-border` | `o-state-color("neutral", "border", "tint")` | `var(--o-blue-500)` |
| Tests and question pool | `--o-qti-attempts` | - | `var(--o-text-muted)` |
| Tests and question pool | `--o-qti-gap-border` | `$o-qti-gap-border-color` | `var(--o-border-strong)` |
| Tests and question pool | `--o-qti-text-entry-border` | `$o-qti-textEntryInteraction-border-color` | `var(--o-qti-gap-border)` |
| Tests and question pool | `--o-qti-text-entry-bg` | `$o-qti-textEntryInteraction-bg` | `var(--o-white)` |
| Tests and question pool | `--o-qti-text-entry-fg` | `$o-qti-textEntryInteraction-color` | `var(--o-text)` |
| Tests and question pool | `--o-qti-text-entry-filled-fg` | `$o-qti-textEntryInteraction-filled-color` | `var(--o-text)` |
| Tests and question pool | `--o-qti-text-entry-filled-bg` | `$o-qti-textEntryInteraction-filled-bg` | `var(--o-state-neutral-soft-bg)` |
| Tests and question pool | `--o-qti-text-entry-filled-border` | `$o-qti-textEntryInteraction-filled-border-color` | `var(--o-state-neutral-soft-border)` |
| Tests and question pool | `--o-qti-gap-drag-border` | - | `var(--o-primary)` |
| Tests and question pool | `--o-qti-gap-selected-border` | - | `var(--o-primary)` |
| Tests and question pool | `--o-qti-text-gap-border` | `#000000` | `#000000` (base colour) |
| Tests and question pool | `--o-qti-gaptext-alternative-bg` | `$gray-lighter` | `#EEEEEE` (base colour) |
| Tests and question pool | `--o-qti-gaptext-alternative-fg` | `$gray` | `#555555` (base colour) |
| Tests and question pool | `--o-qti-help` | `$gray-light` | `#777777` (base colour) |
| Tests and question pool | `--o-qti-match-selected-bg` | `$o-qti-match-selected-bg` | `#F3DCA3` (base colour) |
| Tests and question pool | `--o-qti-match-drop-accept-border` | `$o-qti-match-drop-accept-border-color` | `#022F5B` (base colour) |
| Tests and question pool | `--o-qti-match-true-false-unanswered-bg` | `$o-qti-match-true-false-unanswered-bg` | `#FBFBFB` (base colour) |
| Tests and question pool | `--o-qti-match-true-false-wrong-bg` | `$o-qti-match-true-false-wrong-bg` | `#F7DBDC` (base colour) |
| Tests and question pool | `--o-qti-match-kprim-wrong-bg` | `$o-qti-match-kprim-wrong-bg` | `#F7DBDC` (base colour) |
| Tests and question pool | `--o-qti-order-drop-accept-border` | `$o-qti-order-drop-accept-border-color` | `#022F5B` (base colour) |
| Tests and question pool | `--o-qti-hottext-checked` | - | `var(--o-text)` |
| Tests and question pool | `--o-qti-hottext` | - | `var(--o-text)` |
| Tests and question pool | `--o-qti-menu-link-active` | - | `var(--o-tree-link-active)` |
| Tests and question pool | `--o-qti-menu-link-active-hover-bg` | - | `var(--o-tree-link-active-hover-bg)` |
| Tests and question pool | `--o-qti-menu-link-active-hover` | - | `var(--o-tree-link-active-hover)` |
| Tests and question pool | `--o-qti-menu-link` | - | `var(--o-tree-link)` |
| Tests and question pool | `--o-qti-menu-link-hover` | - | `var(--o-tree-link-hover)` |
| Tests and question pool | `--o-qti-status-correct` | - | `var(--o-state-success-text)` |
| Tests and question pool | `--o-qti-status-incorrect` | - | `var(--o-state-danger-text)` |
| Tests and question pool | `--o-qti-status-review` | - | `var(--o-state-warning-text)` |
| Tests and question pool | `--o-qti-status-ended` | `$o-qti-status-ended-color` | `var(--o-state-finished-text)` |
| Tests and question pool | `--o-qti-status-invalid` | `$o-qti-status-invalid-color` | `var(--o-state-danger-text)` |
| Tests and question pool | `--o-qti-status-not-presented` | `$o-qti-status-notPresented-color` | `var(--o-slate-500)` |
| Tests and question pool | `--o-qti-status-not-answered` | `$o-qti-status-notAnswered-color` | `var(--o-state-critical-text)` |
| Tests and question pool | `--o-qti-bad-response-warning` | `$o-qti-badResponse-warning-color` | `var(--o-state-warning-text)` |
| Tests and question pool | `--o-qti-bad-response` | `$o-qti-badResponse-color` | `var(--o-state-danger-text)` |
| Tests and question pool | `--o-qti-success-bg` | `$o-qti-success` | `var(--o-state-success-bg)` |
| Tests and question pool | `--o-qti-failure-bg` | `$o-qti-failure` | `var(--o-state-warning-bg)` |
| Tests and question pool | `--o-qti-error-bg` | `$o-qti-error` | `var(--o-state-danger-bg)` |
| Tests and question pool | `--o-qti-error-fg` | - | `var(--o-state-danger-fg)` |
| Tests and question pool | `--o-qti-menu-bg` | `$o-qti-menu-bg` | `var(--o-white)` |
| Tests and question pool | `--o-qti-menu-border` | `$o-qti-menu-border-color` | `var(--o-toolbar-border)` |
| Tests and question pool | `--o-qti-run-infos-bg` | `$o-qti-run-infos-bg` | `var(--o-qti-menu-bg)` |
| Tests and question pool | `--o-qti-run-infos-border` | `$o-qti-run-infos-border-color` | `var(--o-qti-menu-border)` |
| Tests and question pool | `--o-qti-run-infos-progress-bg` | `$o-qti-run-infos-progress-bg` | `#EEEEEE` (base colour) |
| Tests and question pool | `--o-qti-run-infos-score-progress-bg` | `$o-qti-run-infos-score-progress-color` | `var(--o-progress-bar-neutral-bg)` |
| Tests and question pool | `--o-qti-run-infos-question-progress-bg` | `$o-qti-run-infos-question-progress-color` | `var(--o-progress-bar-neutral-bg)` |
| Tests and question pool | `--o-qti-item-title-bg` | `$o-qti-item-title-bg` | `var(--o-qti-menu-bg)` |
| Tests and question pool | `--o-qti-item-title-border` | `$o-qti-item-title-border-color` | `var(--o-qti-menu-border)` |
| Tests and question pool | `--o-qti-item-border` | `$o-qti-item-border-color` | `var(--o-qti-menu-border)` |
| Tests and question pool | `--o-qti-item-controls-bg` | `$o-qti-item-controls-bg` | `var(--o-qti-menu-bg)` |
| Tests and question pool | `--o-qti-item-controls-border` | `$o-qti-item-controls-border-color` | `var(--o-qti-menu-border)` |
| Tests and question pool | `--o-qti-item-max-score-border` | `$o-qti-item-max-score-border-color` | `#000000` (base colour) |
| Tests and question pool | `--o-qti-order-source-bg` | `$o-qti-order-source-bg` | `var(--o-state-warning-soft-bg)` |
| Tests and question pool | `--o-qti-order-sources-bg` | `$o-qti-order-sources-bg` | `var(--o-qti-run-infos-bg)` |
| Tests and question pool | `--o-qti-order-sources-border` | `$o-qti-order-sources-border-color` | `var(--o-qti-run-infos-border)` |
| Tests and question pool | `--o-qti-order-target-bg` | `$o-qti-order-target-bg` | `var(--o-state-neutral-soft-bg)` |
| Tests and question pool | `--o-qti-order-target-border` | `$o-qti-order-target-border-color` | `var(--o-state-neutral-bg)` |
| Tests and question pool | `--o-qti-choice-tablelayout-bg` | `$o-qti-choice-tablelayout-bg` | `#E7E7E7` (base colour) |
| Tests and question pool | `--o-qti-match-kprim-correct-bg` | `$o-qti-match-kprim-correct-bg` | `var(--o-state-success-soft-bg)` |
| Tests and question pool | `--o-qti-match-sources-bg` | `$o-qti-match-sources-bg` | `var(--o-qti-run-infos-bg)` |
| Tests and question pool | `--o-qti-match-sources-border` | `$o-qti-match-sources-border-color` | `var(--o-qti-run-infos-border)` |
| Tests and question pool | `--o-qti-match-source-bg` | `$o-qti-match-source-bg` | `var(--o-state-warning-soft-bg)` |
| Tests and question pool | `--o-qti-match-selected-border` | `$o-qti-match-selected-border-color` | `var(--o-state-warning-bg)` |
| Tests and question pool | `--o-qti-match-drag-border` | `$o-qti-match-drag-border-color` | `var(--o-state-warning-bg)` |
| Tests and question pool | `--o-qti-match-target-bg` | `$o-qti-match-target-bg` | `var(--o-state-neutral-soft-bg)` |
| Tests and question pool | `--o-qti-match-target-border` | `$o-qti-match-target-border-color` | `var(--o-state-neutral-bg)` |
| Tests and question pool | `--o-qti-match-choosed-border` | `$o-qti-match-choosed-border-color` | `#777777` (base colour) |
| Tests and question pool | `--o-qti-match-true-false-right-bg` | `$o-qti-match-true-false-right-bg` | `var(--o-state-success-soft-bg)` |
| Tests and question pool | `--o-qti-hottext-bg` | `$o-qti-hottext-bg` | `var(--o-qti-menu-bg)` |
| Tests and question pool | `--o-qti-hottext-border` | `$o-qti-hottext-border-color` | `var(--o-qti-menu-border)` |
| Tests and question pool | `--o-qti-hottext-checked-bg` | `$o-qti-hottext-checked-bg` | `var(--o-state-neutral-soft-bg)` |
| Tests and question pool | `--o-qti-hottext-checked-border` | `$o-qti-hottext-checked-border-color` | `var(--o-state-neutral-soft-border)` |
| Tests and question pool | `--o-qti-highlight-border` | `$o-qti-highlight-color` | `var(--o-state-danger-bg)` |
| Tests and question pool | `--o-qti-box-text` | `$o-qti-box-color` | `#666666` (base colour) |
| Tests and question pool | `--o-qti-box-bg` | `$o-qti-box-bg` | `#EEEEEE` (base colour) |
| Tests and question pool | `--o-qti-editor-box-border` | `$o-qti-editor-box-border-color` | `#E4E7EC` (base colour) |
| Tests and question pool | `--o-qti-log-viewer-manual-correction-bg` | `$o-qti-log-viewer-manual-correction-bg-color` | `#FEFFEE` (base colour) |
| Tests and question pool | `--o-qti-inline-choice-print-border` | `$o-qti-inline-choice-print-border-color` | `#999999` (base colour) |
| Topic broker | `--o-topicbroker-enrolled` | - | `var(--o-state-success-text)` |
| Topic broker | `--o-topicbroker-fillin` | - | `var(--o-text-muted)` |
| Topic broker | `--o-topicbroker-waiting` | - | `var(--o-state-warning-text)` |
| Topic broker | `--o-topicbroker-title-abbr-bg` | - | `var(--o-btn-primary-bg)` |
| Topic broker | `--o-topicbroker-title-abbr-fg` | - | `var(--o-btn-primary-fg)` |
| Topic broker | `--o-topicbroker-title-abbr-border` | `$btn-primary-color` | `var(--o-btn-primary-fg)` |
| Topic broker | `--o-topicbroker-placeholder-title-abbr-border` | `#FFFFFF` | `var(--o-white)` |
| Topic broker | `--o-topicbroker-details-separator-border` | `$panel-default-border` | `var(--o-panel-default-border)` |
| Topic broker | `--o-topicbroker-weighting-grid-border` | `$panel-default-border` | `var(--o-panel-default-border)` |
| User authentication | `--o-authentication-label-bg` | - | `var(--o-primary)` |
| Video | `--o-video-slider-bg` | `$gray` | `#555555` (base colour) |
| Video | `--o-video-bright` | `$o-video-color` | `#FCFCFC` (base colour) |
| Video | `--o-video-border` | `$o-video-border-color` | `#FCFCFC` (base colour) |
| Video | `--o-video-controls-bg` | `$o-video-controls-bg` | `rgba(50, 50, 50, 0.95)` (base colour) |
| Video | `--o-video-marker-bg` | `$o-video-marker-bg-color` | `#EAECF0` (base colour) |
| Video | `--o-video-time-marker-default-bg` | `$o-video-time-marker-default-bg-color` | `var(--o-state-neutral-bg)` |
| Video | `--o-video-timeline-correct-bg` | `$o-video-timeline-correct-bg` | `var(--o-state-success-bg)` |
| Video | `--o-video-timeline-incorrect-bg` | `$o-video-timeline-incorrect-bg` | `var(--o-state-danger-bg)` |
| Video | `--o-recorder-decoration` | `#000000` | `#000000` (base colour) |
| Video | `--o-recorder-recording` | `#FF9999` | `#FF9999` (base colour) |
| Video | `--o-recorder-record` | `#FF0000` | `#FF0000` (base colour) |
| Video | `--o-recorder-record-hover` | `#FF4040` | `#FF4040` (base colour) |
| Video | `--o-recorder-record-active` | `#FF6060` | `#FF6060` (base colour) |
| Video | `--o-recorder-symbol` | `#000000` | `#000000` (base colour) |
| Video | `--o-recorder-symbol-hover` | `#202020` | `#202020` (base colour) |
| Video | `--o-recorder-symbol-active` | `#404040` | `#404040` (base colour) |
| Video | `--o-video-category-active-bg` | `#DDDDDD` | `var(--o-gray-200)` |
| Video task | `--o-video-startbox-main-border` | - | `var(--o-primary)` |
| Video task | `--o-video-startbox-main-bg` | `$o-video-startbox-main-bg-color` | `color-mix(in srgb, var(--o-primary) 10%, transparent)` (base value) |
| Video task | `--o-video-practice` | `$o-video-color-practice` | `var(--o-white)` |
| Video task | `--o-video-recording-info-bg` | `$o-color-note-text-bg` | `#FAFAFB` (base colour) |
| Video task | `--o-video-recording-info-fg` | `$o-color-note` | `#666666` (base colour) |
| Video task | `--o-video-correct-segments` | `$o-video-correct-segments-color` | `var(--o-state-success-text)` |
| Video task | `--o-video-notcorrect-segments` | `$o-video-notcorrect-segments-color` | `var(--o-state-danger-text)` |
| Video task | `--o-videotask-marker-assign-bg` | `$gray-dark` | `#333333` (base colour) |

**Shade tokens:** a shade token holds the darker or lighter colour of an element token, a semantic token or a state token. `$o-token-shades` in `modules/_tokens.scss` holds 150 shade tokens in four forms: `"name": ("token", percent)` is darker (114 tokens), `"name": ("token", percent, "lighten")` is lighter (27), `"name": ("token", angle, "hue")` turns the hue with `adjust-hue()` (1, a stop of a gradient), `"name": ("token", percent, "state")` is a state fill: darker, lighter by the same amount when it cannot get darker (8, the hover and pressed fills of the light buttons and the table filters, `o-contrast-shade()`, decision D20). The compile computes the colour from the colour of the token and emits a literal value. The browser does no colour maths. The name is the name of the token and a suffix:

| Suffix | Colour | Example |
|---|---|---|
| `-hover`, `-active`, `-focus` | the colour of a state of a control | `--o-btn-primary-bg-hover`, `--o-tag-bg-active` |
| `-edge` | a darker edge around a fill: border, outline, text stroke | `--o-focus-edge`, `--o-immunity-proof-none-bg-edge` |
| `-tint` | a lighter tint of the colour as a surface | `--o-immunity-proof-none-bg-tint` |
| `-stop-{n}` | the stop `n` of a gradient, from 1 | `--o-highscore-podium-first-bg-stop-1`, `--o-catalog-header-noimage-stop-4` |

A shade for an element without an own token for the role takes the name of the element and the role: `--o-file-upload-btn-bg-hover` (`btn-default-bg`, 5% darker, the button in the drop area of the file upload) and `--o-immunity-proof-{state}-fg` (the text on the fill, lighter).

| Shade token | Colour | Button state |
|---|---|---|
| `--o-{element}-bg-hover` | `{element}-bg`, 10% darker (light buttons and table filters: at most 10%) | background: hover, focus, active |
| `--o-{element}-bg-active` | `{element}-bg`, 17% darker (16% for a bright fill with dark text; light buttons and table filters: at most 17%) | background: active together with hover or focus |
| `--o-{element}-border-hover` | `{element}-border`, 12% darker | border: hover, active |
| `--o-{element}-border-focus` | `{element}-border`, 25% darker | border: focus, active together with hover or focus |

The percentages are the percentages of the Bootstrap mixin `button-variant`, with two exceptions. First, `o-token-active-percent($fg, $bg)` in `modules/_tokens.scss` gives the active fill 16% when the text is darker than the fill, else 17%. With 17%, the dark text `#321E00` of the warning state has 4.34:1 on the active fill `#A97F16`. With 16%, it has 4.54:1 on `#AE8217`. A fill with light text keeps 17%, so its contrast does not go down. In light, `btn-dirty`, `coursesite-book` and `state-warning` get 16%.

Second, hover and pressed states keep the contrast of the normal state (decision D9). The fills of `btn-primary-light`, `btn-danger-light`, `table-filter` and `table-filter-active` get darker by the largest amount up to 10% (hover) and 17% (pressed) at which the text keeps 4.5:1. `o-contrast-shade-amount($fg, $bg, $max)` in `_functions.scss` (chapter 6) computes the amount at compile time. In light, the light primary button gets 7% for both states, the light danger button 3%, the pressed table filter 14%. The borders keep 12% and 25%.

Third, a state must be visible (decision D13). When no amount from 3% keeps 4.5:1 (a client theme whose button text has about 4.5:1 on white), the fill keeps 10% and 17% and the text of the state changes: the text tokens `{element}-fg-hover` and `{element}-fg-active` (`o-contrast-shade-text()`, chapter 6) hold the text adjusted for the fill. The mixin `button-variant` reads them as its optional arguments `$color-hover` (states with `-bg-hover`) and `$color-active` (pressed with hover or focus, `-bg-active`). In light and openolat the text tokens follow `-fg`. In 86 client themes the light primary button, in 9 the table filter and in 7 the active filter get the darker fill with the adjusted text; on the active filter the dark text turns white. A black fill cannot get darker: `o-contrast-shade()` (chapter 6) makes it lighter by the same amount (decision D20; 9 client themes with a black active filter get `#1A1A1A` under the pointer and `#2B2B2B` when pressed, the white text stays). The compile stops when one of the 8 fills has the colour of its normal fill (`$o-token-state-fills`).

These 13 elements have the 4 button shade tokens (52 tokens). The mixin `button-variant` reads them:

| Element | Layer | `-bg-hover` | `-bg-active` | `-border-hover` | `-border-focus` | Read by |
|---|---|---|---|---|---|---|
| `btn-primary` | base | `#245496` | `#1D4479` | `#1D4479` | `#102644` | `.btn-primary` (`_bootstrap.scss`), the primary buttons of the modules |
| `btn-default` | base | `#E6E6E6` | `#D4D4D4` | `#50565C` | `#313539` | `.btn-default` (`_bootstrap.scss`), `_cal.scss` |
| `btn-danger` | base | `#AD262D` | `#8F2025` | `#79131A` | `#400A0E` | `.btn-danger` (`_bootstrap.scss`) |
| `btn-dirty` | base | `#C9971A` | `#AE8217` (16%) | `#A97F16` | `#6E530F` | dirty button `.o_button_dirty` (`_form.scss`) |
| `btn-primary-light` | base | `#EDEDED` (7%) | `#EDEDED` (7%) | `#224F8E` | `#153158` | `.o_button_primary_light` (`_helpers.scss`) |
| `btn-danger-light` | base | `#F7F7F7` (3%) | `#F7F7F7` (3%) | `#A4242B` | `#6E181D` | `.o_button_danger_light` (`_helpers.scss`) |
| `state-neutral` | base | `#0356A6` | `#024483` | `#003970` | `#00172E` | `.btn-info` (`_bootstrap.scss`); `-bg-hover` also `.label-info[href]:hover` |
| `state-success` | base | `#52BF4B` | `#43A93D` | `#135214` | `#071C07` | `.btn-success` (`_bootstrap.scss`); `-bg-hover` also `.label-success[href]:hover` |
| `state-warning` | base | `#C9971A` | `#AE8217` (16%) | `#573F04` | `#181101` | `.btn-warning` (`_bootstrap.scss`); `-bg-hover` also `.label-warning[href]:hover` |
| `table-filter` | framework | `#E6E6E6` | `#DBDBDB` (14%) | `#1D4479` | `#102644` | filter button of a table `.o_table_filter` (`_table.scss`) |
| `table-filter-active` | framework | `#1F4881` | `#183865` | `#1D4479` | `#102644` | active and open filter button (`_table.scss`) |
| `coursesite-start` | component | `#245496` | `#1D4479` | `#1D4479` | `#102644` | start button of the course site (`_coursesite.scss`) |
| `coursesite-book` | component | `#C9971A` | `#AE8217` (16%) | `#A97F16` | `#6E530F` | book button of the course site (`_coursesite.scss`) |

The other 98 shade tokens have the percentage of the colour maths that they replace. The shades of the state tokens are 5% darker for a soft fill and 10% darker for a text. A client theme that sets the Sass variable of the token gets the same colour as before. A mixin with a token argument builds the name of the shade token: `label-variant`, `table-row-variant`, `list-group-item-variant`, `bg-variant`, `text-emphasis-variant` and `form-control-validation` (`modules/_state_variant.scss`), `o-make-glossy-bg` (`modules/_mixins.scss`), `labeled-variant` (`modules/_various_modules.scss`).

| Shade token | Token | Colour | Light | Read by |
|---|---|---|---|---|
| `--o-selectus-rating-a-bg-hover` | `selectus-rating-a-bg` | 10% darker | `#004D00` | rating buttons of selectus (`_selectus.scss`, mixin `button-variant`) |
| `--o-selectus-rating-a-bg-active` | `selectus-rating-a-bg` | 17% darker | `#002900` | rating buttons of selectus (`_selectus.scss`, mixin `button-variant`) |
| `--o-selectus-rating-b-bg-hover` | `selectus-rating-b-bg` | 10% darker | `#CCA400` | rating buttons of selectus (`_selectus.scss`, mixin `button-variant`) |
| `--o-selectus-rating-b-bg-active` | `selectus-rating-b-bg` | 17% darker | `#A88700` | rating buttons of selectus (`_selectus.scss`, mixin `button-variant`) |
| `--o-selectus-rating-c-bg-hover` | `selectus-rating-c-bg` | 10% darker | `#B80000` | rating buttons of selectus (`_selectus.scss`, mixin `button-variant`) |
| `--o-selectus-rating-c-bg-active` | `selectus-rating-c-bg` | 17% darker | `#940000` | rating buttons of selectus (`_selectus.scss`, mixin `button-variant`) |
| `--o-selectus-rating-abstain-bg-hover` | `selectus-rating-abstain-bg` | 10% darker | `#3E3E4A` | rating buttons of selectus (`_selectus.scss`, mixin `button-variant`) |
| `--o-selectus-rating-abstain-bg-active` | `selectus-rating-abstain-bg` | 17% darker | `#2E2E37` | rating buttons of selectus (`_selectus.scss`, mixin `button-variant`) |
| `--o-addremove-add-bg-hover` | `addremove-add-bg` | 10% darker | `#CFEACC` | `_addremove.scss` |
| `--o-addremove-remove-bg-hover` | `addremove-remove-bg` | 10% darker | `#F9C7C7` | `_addremove.scss` |
| `--o-competence-selected-border-hover` | `competence-selected-border` | 10% darker | `#B28617` | `_taxonomy.scss` |
| `--o-competence-selected-border-active` | `competence-selected-border` | 15% darker | `#9C7514` | `_taxonomy.scss` |
| `--o-chelp-bg-hover` | `chelp-bg` | 8% darker | `#26599E` | `_various_modules.scss` |
| `--o-chelp-border-hover` | `chelp-border` | 12% darker | `#1D4479` | `_various_modules.scss` |
| `--o-content-h5-hover` | `content-h5` | 10% darker | `#212835` | `_course.scss` |
| `--o-content-dimmed-hover` | `content-dimmed` | 10% darker | `#505868` | `_course.scss` |
| `--o-coursesite-title-hover` | `coursesite-title` | 10% darker | `#212835` | `_coursesite.scss` |
| `--o-focus-edge` | `focus` | 10% darker | `#245496` | `_card.scss` |
| `--o-file-upload-btn-bg-hover` | `btn-default-bg` | 5% darker | `#F2F2F2` | `_form.scss` |
| `--o-immunity-proof-validated-bg-tint` | `immunity-proof-validated-bg` | 31% lighter | `#EBFCEC` | `_immunity_proof.scss` |
| `--o-immunity-proof-validated-fg` | `immunity-proof-validated-bg` | 33% lighter | `#F4FDF4` | `_immunity_proof.scss` |
| `--o-immunity-proof-validated-bg-edge` | `immunity-proof-validated-bg` | 31% darker | `#16951C` | `_immunity_proof.scss` |
| `--o-immunity-proof-claimed-bg-tint` | `immunity-proof-claimed-bg` | 45% lighter | `#FFF7E5` | `_immunity_proof.scss` |
| `--o-immunity-proof-claimed-fg` | `immunity-proof-claimed-bg` | 45% lighter | `#FFF7E5` | `_immunity_proof.scss` |
| `--o-immunity-proof-claimed-bg-edge` | `immunity-proof-claimed-bg` | 23% darker | `#8A6000` | `_immunity_proof.scss` |
| `--o-immunity-proof-none-bg-tint` | `immunity-proof-none-bg` | 48% lighter | `#FEEDEB` | `_immunity_proof.scss` |
| `--o-immunity-proof-none-fg` | `immunity-proof-none-bg` | 48% lighter | `#FEEDEB` | `_immunity_proof.scss` |
| `--o-immunity-proof-none-bg-edge` | `immunity-proof-none-bg` | 25% darker | `#6F1406` | `_immunity_proof.scss` |
| `--o-tag-bg-hover` | `tag-bg` | 10% darker | `#245496` | `_tags.scss` |
| `--o-tag-bg-active` | `tag-bg` | 15% darker | `#1F4881` | `_tags.scss` |
| `--o-tag-selected-bg-hover` | `tag-selected-bg` | 10% darker | `#C9971A` | `_tags.scss` |
| `--o-tag-selected-bg-active` | `tag-selected-bg` | 15% darker | `#B28617` | `_tags.scss` |
| `--o-tag-selection-bg-hover` | `tag-selection-bg` | 10% darker | `#E6E6E6` | `_tags.scss` |
| `--o-tag-selection-bg-active` | `tag-selection-bg` | 15% darker | `#D9D9D9` | `_tags.scss` |
| `--o-tag-selection-border-hover` | `tag-selection-border` | 12% darker | `#ACB5C3` | `_tags.scss` |
| `--o-tag-selection-selected-bg-hover` | `tag-selection-selected-bg` | 10% darker | `#245496` | `_tags.scss` |
| `--o-tag-selection-selected-bg-active` | `tag-selection-selected-bg` | 15% darker | `#1F4881` | `_tags.scss` |
| `--o-tag-selection-selected-border-hover` | `tag-selection-selected-border` | 10% darker | `#1F4881` | `_tags.scss` |
| `--o-tag-selection-selected-border-active` | `tag-selection-selected-border` | 15% darker | `#1A3D6D` | `_tags.scss` |
| `--o-tag-selected-border-hover` | `tag-selected-border` | 10% darker | `#B28617` | `_tags.scss` |
| `--o-tag-selected-border-active` | `tag-selected-border` | 15% darker | `#9C7514` | `_tags.scss` |
| `--o-tag-selection-remove-selected-bg-hover` | `tag-selection-remove-selected-bg` | 10% darker | `#AD262D` | `_tags.scss` |
| `--o-tag-selection-remove-selected-bg-active` | `tag-selection-remove-selected-bg` | 15% darker | `#982228` | `_tags.scss` |
| `--o-tag-selection-remove-selected-border-hover` | `tag-selection-remove-selected-border` | 10% darker | `#82141C` | `_tags.scss` |
| `--o-tag-selection-remove-selected-border-active` | `tag-selection-remove-selected-border` | 15% darker | `#6C1118` | `_tags.scss` |
| `--o-tinymce-danger-bg-edge` | `tinymce-danger-bg` | 25% darker | `#F28181` | `_tinymce.scss` |
| `--o-toolbar-close-hover` | `toolbar-close` | 15% darker | `#982228` | `_breadcrumb.scss`, `_toolbar.scss` |
| `--o-box-info-table-border` | `box-info-bg` | 5% lighter | `#FFFFFF` | table in a message box (`_content_embedded.scss`) |
| `--o-box-info-table-stripe-bg` | `box-info-bg` | 5% lighter | `#FFFFFF` | table in a message box (`_content_embedded.scss`) |
| `--o-box-info-table-header-bg` | `box-info-bg` | 10% darker | `#E3E3E3` | table in a message box (`_content_embedded.scss`) |
| `--o-box-note-table-border` | `box-note-bg` | 5% lighter | `#FFFFFF` | table in a message box (`_content_embedded.scss`) |
| `--o-box-note-table-stripe-bg` | `box-note-bg` | 5% lighter | `#FFFFFF` | table in a message box (`_content_embedded.scss`) |
| `--o-box-note-table-header-bg` | `box-note-bg` | 10% darker | `#DEDEE4` | table in a message box (`_content_embedded.scss`) |
| `--o-box-success-table-border` | `box-success-bg` | 5% lighter | `#FFFFFF` | table in a message box (`_content_embedded.scss`) |
| `--o-box-success-table-stripe-bg` | `box-success-bg` | 5% lighter | `#FFFFFF` | table in a message box (`_content_embedded.scss`) |
| `--o-box-success-table-header-bg` | `box-success-bg` | 10% darker | `#D1E8D9` | table in a message box (`_content_embedded.scss`) |
| `--o-box-warning-table-border` | `box-warning-bg` | 5% lighter | `#FFFFFF` | table in a message box (`_content_embedded.scss`) |
| `--o-box-warning-table-stripe-bg` | `box-warning-bg` | 5% lighter | `#FFFFFF` | table in a message box (`_content_embedded.scss`) |
| `--o-box-warning-table-header-bg` | `box-warning-bg` | 10% darker | `#F8F0CB` | table in a message box (`_content_embedded.scss`) |
| `--o-box-error-table-border` | `box-error-bg` | 5% lighter | `#FFFFFF` | table in a message box (`_content_embedded.scss`) |
| `--o-box-error-table-stripe-bg` | `box-error-bg` | 5% lighter | `#FFFFFF` | table in a message box (`_content_embedded.scss`) |
| `--o-box-error-table-header-bg` | `box-error-bg` | 10% darker | `#FFD0C3` | table in a message box (`_content_embedded.scss`) |
| `--o-registration-otp-error-bg-focus` | `box-error-bg` | 10% darker | `#FFD0C3` | `_dmz.scss` |
| `--o-page-spacer-border-hover` | `border-subtle` | 10% darker | `#D5D5D5` | `_contenteditor.scss` |
| `--o-editor-part-toolbar-link-hover` | `editor-part-toolbar-link` | 10% darker | `#E6E6E6` | `_contenteditor.scss` |
| `--o-popover-title-bg-edge` | `popover-title-bg` | 5% darker | `#EBEBEB` | `_dialog.scss` |
| `--o-im-chat-from-hover` | `im-chat-from` | 10% darker | `#5E5E5E` | `_im.scss` |
| `--o-panel-placeholder-border-hover` | `panel-placeholder-border` | 10% darker | `#B2BBC8` | `_form.scss` |
| `--o-highscore-podium-first-bg-stop-1` | `highscore-podium-first-bg` | 40% lighter | `#FFF7CC` | podium of the high score (`_highscore.scss`, mixin `o-make-glossy-bg`) |
| `--o-highscore-podium-first-bg-stop-2` | `highscore-podium-first-bg` | 10% lighter | `#FFDF33` | podium of the high score (`_highscore.scss`, mixin `o-make-glossy-bg`) |
| `--o-highscore-podium-first-bg-stop-4` | `highscore-podium-first-bg` | 20% lighter | `#FFE766` | podium of the high score (`_highscore.scss`, mixin `o-make-glossy-bg`) |
| `--o-highscore-podium-second-bg-stop-1` | `highscore-podium-second-bg` | 40% lighter | `#FFFFFF` | podium of the high score (`_highscore.scss`, mixin `o-make-glossy-bg`) |
| `--o-highscore-podium-second-bg-stop-2` | `highscore-podium-second-bg` | 10% lighter | `#DADADA` | podium of the high score (`_highscore.scss`, mixin `o-make-glossy-bg`) |
| `--o-highscore-podium-second-bg-stop-4` | `highscore-podium-second-bg` | 20% lighter | `#F3F3F3` | podium of the high score (`_highscore.scss`, mixin `o-make-glossy-bg`) |
| `--o-highscore-podium-third-bg-stop-1` | `highscore-podium-third-bg` | 40% lighter | `#F5E5D6` | podium of the high score (`_highscore.scss`, mixin `o-make-glossy-bg`) |
| `--o-highscore-podium-third-bg-stop-2` | `highscore-podium-third-bg` | 10% lighter | `#D7995B` | podium of the high score (`_highscore.scss`, mixin `o-make-glossy-bg`) |
| `--o-highscore-podium-third-bg-stop-4` | `highscore-podium-third-bg` | 20% lighter | `#E1B284` | podium of the high score (`_highscore.scss`, mixin `o-make-glossy-bg`) |
| `--o-table-sticky-col-edge` | `table-border` | 10% darker | `#C4C4C4` | `_table.scss` |
| `--o-table-details-bg-hover` | `table-details-bg` | 10% darker | `#CCE8F4` | `_table.scss` |
| `--o-competence-bg-hover` | `competence-bg` | 10% darker | `#E6E6E6` | `_taxonomy.scss` |
| `--o-competence-bg-active` | `competence-bg` | 15% darker | `#D9D9D9` | `_taxonomy.scss` |
| `--o-labeled-bg-hover` | `labeled-bg` | 10% darker | `#3F3F3F` | label without a status class `.o_labeled`, `.o_labeled_light` (`_various_modules.scss`, mixin `labeled-variant`) |
| `--o-labeled-border-hover` | `labeled-border` | 12% darker | `#272727` | label without a status class `.o_labeled`, `.o_labeled_light` (`_various_modules.scss`, mixin `labeled-variant`) |
| `--o-labeled-light-bg-hover` | `labeled-light-bg` | 10% darker | `#E6E6E6` | label without a status class `.o_labeled`, `.o_labeled_light` (`_various_modules.scss`, mixin `labeled-variant`) |
| `--o-labeled-light-border-hover` | `labeled-light-border` | 12% darker | `#3A3A3A` | label without a status class `.o_labeled`, `.o_labeled_light` (`_various_modules.scss`, mixin `labeled-variant`) |
| `--o-video-bright-edge` | `video-bright` | 10% darker | `#E3E3E3` | `_videotask.scss` |
| `--o-catalog-header-noimage-stop-1` | `primary` | 80% lighter | `#FFFFFF` | catalog header without an image: default of `$o-cat2-header-noimage-bg` (`_config.scss`) |
| `--o-catalog-header-noimage-stop-3` | `primary` | 55% lighter | `#FFFFFF` | catalog header without an image: default of `$o-cat2-header-noimage-bg` (`_config.scss`) |
| `--o-catalog-header-noimage-stop-4` | `primary` | hue 5deg | `#2E5FBF` | catalog header without an image: default of `$o-cat2-header-noimage-bg` (`_config.scss`) |
| `--o-state-danger-bg-hover` | `state-danger-bg` | 10% darker | `#AD262D` | `.label-danger[href]:hover` (mixin `label-variant`) |
| `--o-state-neutral-soft-bg-hover` | `state-neutral-soft-bg` | 5% darker | `#D9EAFF` | `.table-hover` row `tr.*`, `.list-group-item-*`, `a.bg-*` (mixins `table-row-variant`, `list-group-item-variant`, `bg-variant`) |
| `--o-state-success-soft-bg-hover` | `state-success-soft-bg` | 5% darker | `#E0F2DE` | `.table-hover` row `tr.*`, `.list-group-item-*`, `a.bg-*` (mixins `table-row-variant`, `list-group-item-variant`, `bg-variant`) |
| `--o-state-warning-soft-bg-hover` | `state-warning-soft-bg` | 5% darker | `#F5EFD8` | `.table-hover` row `tr.*`, `.list-group-item-*`, `a.bg-*` (mixins `table-row-variant`, `list-group-item-variant`, `bg-variant`) |
| `--o-state-danger-soft-bg-hover` | `state-danger-soft-bg` | 5% darker | `#FCDEDE` | `.table-hover` row `tr.*`, `.list-group-item-*`, `a.bg-*` (mixins `table-row-variant`, `list-group-item-variant`, `bg-variant`) |
| `--o-state-neutral-text-hover` | `state-neutral-text` | 10% darker | `#0356A6` | `a.text-*` hover, focus border of `.has-*` (mixins `text-emphasis-variant`, `form-control-validation`) |
| `--o-state-success-text-hover` | `state-success-text` | 10% darker | `#155A16` | `a.text-*` hover, focus border of `.has-*` (mixins `text-emphasis-variant`, `form-control-validation`) |
| `--o-state-warning-text-hover` | `state-warning-text` | 10% darker | `#614604` | `a.text-*` hover, focus border of `.has-*` (mixins `text-emphasis-variant`, `form-control-validation`) |
| `--o-state-danger-text-hover` | `state-danger-text` | 10% darker | `#AD262D` | `a.text-*` hover, focus border of `.has-*` (mixins `text-emphasis-variant`, `form-control-validation`) |

A lighter shade of a light colour stops at white: `lighten()` does not go above a lightness of 100%.

**Translucent colours:** a translucent colour is no token. The rule mixes the token with `transparent` and keeps the alpha of the Sass call: `rgba($modal-backdrop-bg, 0.5)` is `color-mix(in srgb, var(--o-backdrop) 50%, transparent)`. The browser gives the same colour, and an override of the token changes the translucent colour too. A named `$o-*` variable with a translucent default stays the customisation point: its default is the `color-mix()`, e.g. `$o-login-bg-color: color-mix(in srgb, var(--o-scrim) 95%, transparent)`. A client theme that sets the variable keeps its value.

Three semantic tokens exist only for translucent colours. They are opaque, and the rule sets the alpha of the old value (decision M8):

| Token | Light | Meaning | Example |
|---|---|---|---|
| `--o-shadow-color` | `#000000` | the colour of all shadows: the Bootstrap shadows, the text shadows and the shadows of panels, dropdown menus, the toolbar, the login box, the cards and the content editor | `rgba(0, 0, 0, .2)` is `color-mix(in srgb, var(--o-shadow-color) 20%, transparent)` |
| `--o-backdrop` | `#000000` | the dark layer behind a modal dialog: `dialog::backdrop`, the callout overlay, the lightbox | `color-mix(in srgb, var(--o-backdrop) 50%, transparent)` |
| `--o-scrim` | `#FFFFFF` | the light veil over content: below a text, a video control or a progress indicator. A dark mode redefines it as a dark colour. | `color-mix(in srgb, var(--o-scrim) 95%, transparent)` |

The shadows with the colour `rgba(16, 24, 40, a)` read `--o-shadow-color` with the same alpha (decision D3): `$o-dropdown-box-shadow`, `$o-toolbar-box-shadow`, `$o-icon-panel-box-shadow`, `$o-login-box-shadow`, `$o-cc-box-shadow`, `$o-card-box-shadow`, `$o-card-box-hover-shadow` and `.panel`. The Sass variables stay customisation points. The two shadows of the immunity proof (`.o_immunity_proof_wrapper`, `.o_immunity_proof_status`) read `--o-shadow-color` with their alpha too (decision D11).

Colours without a role read a token with the alpha of the old `rgba()` (decision D11):

| Colour | Token | Rules |
|---|---|---|
| dark tints | `--o-backdrop` | skeleton (`_helpers.scss`), print progress bar (`_print.scss`), dark overlay of a quiz box (`_contenteditor_quiz.scss`), AI feedback paragraph (`_ai_feedback.scss`), image comparison handle (`_thirdparty.scss`), stripes of the admin tags (`_various_modules.scss`), dark layer of the video comment panel (`.o_video_comment_container`, `_video.scss`, 50%, decision D12), the defaults of `$dropdown-border` and `$o-lp-bg` |
| white highlight line at the top of the navigation bar (`box-shadow` inset) | `--o-navbar-highlight` (framework, `var(--o-white)`) | `_navbar.scss` |
| background of the peekview of a styled course element | `--o-course-style-peekview-bg` (component, default `$o-cn-style-peekview-bg`, translucent `rgba(16, 24, 40, 0.02)`) | `_course.scss` |
| stops of the gradients of the not available visual | `--o-primary`, `--o-not-available-dark` (black), `--o-not-available-light` (white) | default of `$o-not-available-bg` |

`$o-not-available-bg` is a background with gradients, not a colour: it stays the customisation point, its stops read the tokens. `navbar-highlight` is not `scrim`: a highlight stays light in a dark mode, the veil does not. The dark layer of the video comment panel `.o_video_comment_container` reads `backdrop`, not `scrim` (decision D12 corrects D11): `scrim` would turn the dark layer white under the white close icon.

The module sources and the defaults of `_config.scss` hold 58 references of `--o-shadow-color`, 14 of `--o-backdrop` and 20 of `--o-scrim`.

Rules for a shade token:

- It follows its token. A new value of the Sass variable (`$btn-primary-bg`) or an override of the token in `$o-tokens` gives a new shade colour.
- `$o-tokens` overrides it like an element token, e.g. `"btn-primary-bg-hover": "blue-800"`.
- The background and the border have separate tokens. A theme that overrides `btn-primary-bg` in `$o-tokens` overrides `btn-primary-border` too. Without that, the border keeps the colour of `$btn-primary-border`.
- The compile computes the colour. A stylesheet that redefines a token at run time (dark mode) redefines its shade tokens too.
- Its token is an element token, a semantic token or a state token. A shade of a shade token or of a palette step stops the compile.

**Mixin `button-variant`:** `modules/_button_variant.scss` replaces the Bootstrap mixin `button-variant($color, $background, $border)` with the same rules and selectors. Each argument is a colour or a token:

- A colour gets the state colours of Bootstrap, computed with `darken()` at compile time. A client theme that calls the mixin with colours keeps working.
- A token `var(--o-{name})` gets the shade tokens of the table above. A token without shade tokens stops the compile.
- Two optional arguments give the text of the states (decision D13): `$color-hover` for focus, hover and active (fill `-bg-hover`), `$color-active` for the pressed state with hover or focus (fill `-bg-active`). Default: `$color`. The light buttons and the table filters pass their text tokens `-fg-hover` and `-fg-active`.

`modules/_bootstrap.scss` (`theme.scss`) and `content.scss` import the mixin after the Bootstrap mixins.

```scss
.my_button { @include button-variant(var(--o-btn-primary-fg), var(--o-btn-primary-bg), var(--o-btn-primary-border)); }
```

The Bootstrap classes `.btn-default`, `.btn-primary` and `.btn-danger`, the dirty button, the start and book buttons of the course site and the button rules of the modules read the tokens with this mixin. `.btn-success`, `.btn-info` and `.btn-warning` read the state tokens: `_bootstrap.scss` hands `var(--o-state-{state}-fg)`, `-bg` and `-border` of the states `success`, `neutral` and `warning` to the Bootstrap import, and the mixin reads their shade tokens. The text is the role `fg` of the state: `#012902` on success, white on neutral, `#321E00` on warning. No OpenOlat page uses these three classes. `.btn-link` reads the link tokens (see **Links** below), and its disabled text reads `--o-text-disabled`. A new button with its own colours needs the element tokens `{element}-fg`, `{element}-bg`, `{element}-border` and 4 rows in `$o-token-shades`.

The other buttons of OpenOlat read their own element tokens:

| Button | Rule | Tokens |
|---|---|---|
| ghost button | `.btn.o_button_ghost` (`_helpers.scss`) | `btn-ghost-fg` (reference `link`), `btn-ghost-bg`, `btn-ghost-border`; under the pointer `btn-ghost-hover-fg`, `btn-ghost-hover-bg`, `btn-ghost-hover-border`. With `.btn-danger` the text and the hover border read `--o-state-danger-text`. |
| mega button | `.btn.o_button_mega` (`_helpers.scss`) | `btn-mega-bg`, `btn-mega-hover-bg` |
| light primary and light danger button | `.o_button_primary_light`, `.o_button_danger_light` (`_helpers.scss`) | mixin `button-variant` with `btn-primary-light-fg`, `-bg`, `-border` (and `btn-danger-light-*`), their 4 shade tokens and the text tokens of the states `-fg-hover`, `-fg-active` (decision D13); the hover and focus rule reads `-hover-fg`, `-hover-bg`, `-hover-border` |
| filter button of a table | `.o_table_filter`, `.o_filter_active`, `.o_filter_open` (`_table.scss`) | mixin `button-variant` with `table-filter-fg`, `-bg`, `-border` and `table-filter-active-fg`, `-bg`, `-border`, their shade tokens and the text tokens of the states `-fg-hover`, `-fg-active` |
| toggle switch | `.o_button_toggle` (`_form.scss`) | `toggle-border`, `toggle-bg`, `toggle-handle`, `toggle-text`, `toggle-on-bg`, `toggle-on-border`, `toggle-on-handle`, `toggle-on-text` |
| scope toggle | `.o_scope_toggle` (`_scope.scss`) | `scope-toggle-fg`, `-bg`, `-border`, `-hover-bg`, `-on-fg`, `-on-bg`, `-on-border`, `-on-hover-bg`, `-on-hover-fg` |
| confirm button of the appointments | `.o_appointments ... .o_button_confirm` (`_appointments.scss`) | `appointment-planned` (reference to the fill of the warning state), `appointment-select-icon` |
| rating buttons of selectus | `.o_btn_rating_on`, `.o_btn_rating_abstain` (`_selectus.scss`) | mixin `button-variant` with `selectus-rating-{a,b,c,abstain}-fg` and `-bg` (fixed colours; the B text `#242424` and the C fill `#EB0000` are adjusted to 4.5:1, decision D9) and the border `btn-default-border`; shade tokens `-bg-hover`, `-bg-active`. The rating bars of the statistics (`.progress-bar.o_bar_rating_on[title=B|C]`, `.o_bar_rating_abstain`) read the fill of the rating button and its hover shade as gradient (mixin `progress-bar-rating-styles($token)`, decisions D15, D16): bar C `#EB0000` to `#B80000`, the abstention bar `#555566` to `#3E3E4A` with the white text `selectus-rating-abstain-fg`; the label of bar B reads `selectus-rating-b-fg` (D17); bar A is rendered with `progress-bar-success` and has no own rule (D25). The marks of the ratings (`f_rating_0|1|2|3`, `f_rating_abstention`) read the fill of the button: `btn-default-bg` for no rating (a rating button that is not selected), `selectus-rating-c-bg`, `selectus-rating-b-marker` (3:1), `selectus-rating-a-bg` and `selectus-rating-abstain-bg` (D18, D28). The letters of the explanation (`o_rating_expl`) read `selectus-rating-a-bg`, `selectus-rating-b-text` and `selectus-rating-c-text` (4.5:1). The mark B and the letters B and C keep their ratio on a white, a striped and a hovered table row (D26) |

**State rules of Bootstrap:** the Bootstrap rules of the states read the state tokens. `_bootstrap.scss` and `content.scss` hand the state tokens to the Bootstrap import and set the Sass values again after it. `modules/_state_variant.scss` replaces the Bootstrap mixins `label-variant`, `progress-bar-variant`, `alert-variant`, `form-control-validation`, `table-row-variant`, `list-group-item-variant`, `bg-variant` and `text-emphasis-variant` with the same rules and selectors. A token argument reads the shade tokens of the token. A colour argument includes the Bootstrap mixin as it is (`meta.get-mixin()`), so a client theme and the labels `label-default` and `label-primary` compile as before. The Bootstrap name `info` is the state `neutral`. A rule for `danger` and `error` reads the state `danger`.

| Bootstrap rules | Colours | Under the pointer or with the focus |
|---|---|---|
| `.btn-success`, `.btn-info`, `.btn-warning` | fill `bg`, text `fg`, border `border` | mixin `button-variant`: `state-*-bg-hover`, `-bg-active`, `-border-hover`, `-border-focus` |
| labels `.label-success`, `-info`, `-warning`, `-danger` | fill `bg`, text `fg` | `[href]:hover`: `state-*-bg-hover` |
| progress bars `.progress-bar-success`, `-info`, `-warning`, `-danger` | fill `bg`, text `fg` | - |
| alerts `.alert-success`, `-info`, `-warning`, `-danger` | fill `soft-bg`, border `soft-border`, text `soft-fg`; `hr` the border token, `.alert-link` the text token | - |
| panels `.panel-success`, `-info`, `-warning`, `-danger` | heading text `text`, heading fill `soft-bg`, borders `soft-border` | - |
| list groups `.list-group-item-*` | text `text`, fill `soft-bg`; `.active` fill and border `text`, text `--o-white` | `a.`, `button.` hover: `state-*-soft-bg-hover` |
| form validation `.has-success`, `.has-warning`, `.has-error` | text and border `text`, fill of the add-on `soft-bg` | focus border `state-*-text-hover`, glow `color-mix(in srgb, var(--o-state-*-text) 60%, transparent)` |
| contextual table rows `tr.success`, `.info`, `.warning`, `.danger` | fill `soft-bg` | `.table-hover` row: `state-*-soft-bg-hover` |
| `.text-success`, `-info`, `-warning`, `-danger` | text `text` | `a.text-*:hover`, `:focus`: `state-*-text-hover` |
| `.bg-success`, `-info`, `-warning`, `-danger`, `mark` | fill `soft-bg` (`mark`: warning) | `a.bg-*:hover`, `:focus`: `state-*-soft-bg-hover` |

The print rules of the progress bars (`_print.scss`) read the role `bg`, the sticky cells of `tr.success` (`_table.scss`) the role `soft-bg`.

**Message boxes and evaluation form:** element tokens follow the state tokens. A client theme that sets the Sass variable gets its colour for the border and the text.

| Element | Tokens | Default |
|---|---|---|
| message boxes `.o_info`, `.o_success`, `.o_warning`, `.o_error` and their `_with_icon` variants (`_dialog.scss`); the state boxes and icons of 8 more modules (`_qti21.scss`, `_practice.scss`, `_contenteditor.scss`, `_contenteditor_quiz.scss`, `_curriculum.scss`, `_dmz.scss`, `_toolbar.scss`, `_videotask.scss`) | `box-{info,success,warning,error}-border` (border), `box-*-text` (icon, coloured text) | `$o-box-*-color` (default `$o-color-*`, the `bg` of the state); the text: the `text` of the state while `$o-box-*-color` has its default |
| fill of the message boxes | `box-{info,note,success,warning,error}-bg` (`$o-box-*-bg`) | the boxes read the Sass value `$o-box-*-bg`; the tables in a box (`_content_embedded.scss`) read the token and its shade tokens |
| note, tip and important boxes | Sass values `$o-box-note-*`, `$o-box-tip-*`, `$o-box-important-*` | hex values, no token |
| evaluation form and quality: charts, heat map, box plot lines | `evaluation-{insufficient,neutral,sufficient}` | `$o-evaluation-*-color`, default the `bg` of the states danger, warning and success |
| rubric icons, radio card text, slider labels, trend text | `evaluation-*-text` | the `text` of the state while `$o-evaluation-*-color` has its default |
| box of the box plot | `evaluation-*-fill` | `$o-evaluation-*-fill`, default the light fill of the state (palette step 200) |
| portlet in the edit mode of the portal (`_portal.scss`) | `portlet-edit-bg`, `portlet-edit-border` | `soft-bg` and `soft-border` of the warning state |
| bad response warning of a test question (`_qti21.scss`) | `qti-bad-response-warning` | `text` of the warning state |

**Rating stars:** the filled and the empty star of `.o_rating .o_rating_items .o_icon` read the component token `rating-star` (`_rating_and_comments.scss`). The default is the fixed palette step `yellow-400` (`#E4B132` in light, decision D1). The star is a fixed colour without a meaning, not a status: a client theme changes it with `$o-tokens: ("rating-star": "orange-400")`.

**Text and icons on a fill:** the selected tag reads `tag-selected-fg` (reference `btn-dirty-fg`, the text of the dirty button on the same fill). The error box of a test question reads `qti-error-fg` (reference `state-danger-fg`) on `qti-error-bg`. The text of a selected competence reads `competence-selected` (decision D8): the text of the selected tag, adjusted by the compile to 4.5:1 on the pressed competence fill when it is too light (a theme with white text on its dirty fill). The chat icon in the navigation bar (`im-newmessage`, `im-newmessage-hover`) and the close icon of a tab (`navbar-tab-close`, `navbar-tab-close-hover`) stand on `--o-navbar-bg`. The Sass defaults of their hover colours call `o-contrast-hover($hover, $base, $background)` (`_functions.scss`). The function returns the hover colour when it has 3:1 on the background or more contrast than the base colour, else the base colour. The compile adjusts the close icon and its hover to 3:1 on the navigation bar with `o-a11y-color(..., $o-navbar-bg, 3)` (decision D10). `light` and `openolat` do not change (4.89:1 and 6.79:1 on white). The close icon of a tab in the "More" dropdown stands on the raised surface of the dropdown, not on the navigation bar. It reads `dropdown-tab-close` and `dropdown-tab-close-hover`: the Sass colours without the adjustment. The contrast pairs below check the four navigation bar icons on `navbar-bg` and the two dropdown icons on `bg-raised` (3:1).

The off text of the toggle switch reads `toggle-text`: the muted text, adjusted to 4.5:1 on the track (`#626B7F` in light, decision D9). The fill of the selected scope toggle under the pointer (`$o-scope-toggle-on-bg-hover-color`) is lighter by the largest amount from 3% up to 10% at which the white text keeps 4.5:1 (`o-contrast-shade-amount(..., "lighten")`, `#3374CE` in light). When no amount from 3% passes, the fill is 10% lighter and its text `scope-toggle-on-hover-fg` is adjusted for it (decision D13: in 23 client themes the white text turns dark, e.g. `#333333` on `#00AACA`). A hover fill that the theme sets keeps the white text.

**Links:** the links have four colour families:

| Family | Tokens | Read by |
|---|---|---|
| link | `--o-link`, `--o-link-hover` (`$link-color`, `$link-hover-color`) | the Bootstrap rules `a`, `a:hover`, `a:focus`, `.btn-link` and its hover, the pagination hover, the border of `.nav .open > a` and `a.thumbnail`; the element tokens with the reference `link`, e.g. `toolbar-dropdown-active`, `im-buddies-link`, `segments-tab-link`, `selectus-review-comment-link` |
| subtle link | `--o-link-subtle` (`$link-color`, adjusted to 4.5:1 on `$panel-default-heading-bg`) | the element tokens with the reference `link-subtle`: `toolbar-breadcrumb-link`, `table-link`, `table-striped-link`, `course-style-link`, `forum-message-header-author` (and `forum-message-footer-attachment` through it) |
| content link | `--o-content-link` (`$o-content-link-color`) | links in the content (`content.css`, `.o_user_content_block`) |
| navigation link | `--o-nav-link`, `--o-nav-link-hover` (`$navbar-default-link-color`, `$navbar-default-link-hover-color`) | the menu tree links (`tree-link`, `tree-link-hover`; the QTI menu through them) and the toolbar links (`toolbar-link`, `toolbar-nav-link`) |

`modules/_bootstrap.scss` sets `$link-color`, `$link-hover-color` and `$pagination-hover-color` to `var(--o-link)` and `var(--o-link-hover)` during the Bootstrap import and sets the Sass values again after it. The Bootstrap partials use the three variables only as plain values. An override of `link` with `$o-tokens` reaches the standard link rules.

The menu tree, the toolbar, the navigation bar, the footer and the login help keep their own tokens, because client themes set their Sass variables. `tree-link`, `tree-link-hover`, `toolbar-link` and `toolbar-link-hover` follow `nav-link` and `nav-link-hover` when their Sass variable has the same colour. The Sass variables `$o-table-link-color`, `$o-toolbar-breadcrumb-link-color`, `$o-forum-message-header-author-color` and `$o-forum-message-footer-attachment-color` are removed: set `link-subtle` or the element token with `$o-tokens`. The filter tab row above a table (`.o_segments_tab`) reads `segments-tab-link`: the text of the tabs, the line of the selected tab and the line under the row. The linked segments (`.o_segments_linked`) read `link`.

Four element colours stay Sass in the modules, because client themes set a value that a token cannot carry: `$o-navbar-border-color` and `$o-tree-link-active-bg` (`none`), `$o-footer-default-link-hover-color` (an invalid hex value) and `$o-content-link-hover-color` (`!important` in the variable). They get a token when these client themes set a colour.

`$o-button-primary-light-color` and `$o-button-danger-light-color` (default from `o-a11y-color()`) feed the tokens `btn-primary-light-fg` and `btn-danger-light-fg` of the light buttons. Three modules still read a Sass variable with a default from `o-a11y-color()`: `_topicbroker.scss` (`$o-button-primary-light-color`), `_course.scss` (`$o-edu-type-standard-course-color`) and `_dialog_coursenode.scss` (`$o-dialog-cn-label-new-color`).

**Contrast pairs:** `$o-token-contrast-pairs` in `modules/_tokens.scss` holds 278 pairs of semantic, element and shade tokens. A loop over `$o-states` adds 5 pairs per state (40 pairs): `fg` on `bg`, `soft-fg` on `soft-bg`, `text` on white and on `soft-bg` (4.5:1), `border` on white (3:1). The compile knows 318 pairs. The 278 pairs of the list (3:1 for the focus rings, the handles of the toggle switch and the icons on the navigation bar and in its "More" dropdown):

| Foreground | Background | Ratio |
|---|---|---|
| `text` | `bg`, `bg-hover`, `bg-selected`, `bg-raised` | 4.5:1 |
| `text-muted` | `bg` | 4.5:1 |
| `heading` | `bg` | 4.5:1 |
| `link` | `bg` | 4.5:1 |
| `link-hover` | `bg` | 4.5:1 |
| `primary-text` | `bg` | 4.5:1 |
| `primary-fg` | `primary` | 4.5:1 |
| `focus` | `bg` | 3:1 |
| `btn-primary-fg` | `btn-primary-bg`, `btn-primary-bg-hover`, `btn-primary-bg-active` | 4.5:1 |
| `btn-default-fg` | `btn-default-bg`, `btn-default-bg-hover`, `btn-default-bg-active` | 4.5:1 |
| `btn-dirty-fg` | `btn-dirty-bg`, `btn-dirty-bg-hover`, `btn-dirty-bg-active` | 4.5:1 |
| `btn-danger-fg` | `btn-danger-bg`, `btn-danger-bg-hover`, `btn-danger-bg-active` | 4.5:1 |
| `btn-ghost-fg` | `bg` | 4.5:1 |
| `btn-ghost-hover-fg` | `bg` | 4.5:1 |
| `btn-primary-light-fg` | `btn-primary-light-bg` | 4.5:1 |
| `btn-primary-light-fg-hover`, `btn-primary-light-fg-active` | `btn-primary-light-bg-hover`, `btn-primary-light-bg-active` (the text of the state on its fill) | 4.5:1 |
| `btn-primary-light-hover-fg` | `btn-primary-light-hover-bg` | 4.5:1 |
| `btn-danger-light-fg` | `btn-danger-light-bg` | 4.5:1 |
| `btn-danger-light-fg-hover`, `btn-danger-light-fg-active` | `btn-danger-light-bg-hover`, `btn-danger-light-bg-active` | 4.5:1 |
| `btn-danger-light-hover-fg` | `btn-danger-light-hover-bg` | 4.5:1 |
| `table-filter-fg` | `table-filter-bg` | 4.5:1 |
| `table-filter-fg-hover`, `table-filter-fg-active` | `table-filter-bg-hover`, `table-filter-bg-active` | 4.5:1 |
| `table-filter-active-fg` | `table-filter-active-bg` | 4.5:1 |
| `table-filter-active-fg-hover`, `table-filter-active-fg-active` | `table-filter-active-bg-hover`, `table-filter-active-bg-active` | 4.5:1 |
| `toggle-text` | `toggle-bg` | 4.5:1 |
| `toggle-on-text` | `toggle-on-bg` | 4.5:1 |
| `toggle-handle` | `toggle-bg` | 3:1 |
| `toggle-on-handle` | `toggle-on-bg` | 3:1 |
| `scope-toggle-fg` | `scope-toggle-bg`, `scope-toggle-hover-bg` | 4.5:1 |
| `scope-toggle-on-fg` | `scope-toggle-on-bg` | 4.5:1 |
| `scope-toggle-on-hover-fg` | `scope-toggle-on-hover-bg` | 4.5:1 |
| `tag-selected-fg` | `tag-selected-bg`, `tag-selected-bg-hover`, `tag-selected-bg-active` | 4.5:1 |
| `competence-selected` | `competence-bg`, `competence-bg-hover`, `competence-bg-active` | 4.5:1 |
| `navbar-tab-close` | `navbar-bg` | 3:1 |
| `navbar-tab-close-hover` | `navbar-bg` | 3:1 |
| `navbar-focus` | `navbar-bg` | 3:1 |
| `footer-focus` | `footer-inner-bg` | 3:1 |
| `dropdown-tab-close` | `bg-raised` | 3:1 |
| `dropdown-tab-close-hover` | `bg-raised` | 3:1 |
| `im-newmessage` | `navbar-bg` | 3:1 |
| `im-newmessage-hover` | `navbar-bg` | 3:1 |
| `labeled-fg` | `labeled-bg`, `labeled-bg-hover` | 4.5:1 |
| `labeled-light-fg` | `labeled-light-bg`, `labeled-light-bg-hover` | 4.5:1 |
| `chelp-tooltip` | `bg` | 4.5:1 |
| `qti-error-fg` | `qti-error-bg` | 4.5:1 |
| `appointment-select-icon` | `appointment-planned` | 4.5:1 |
| `selectus-rating-a-fg` | `selectus-rating-a-bg`, `selectus-rating-a-bg-hover`, `selectus-rating-a-bg-active` | 4.5:1 |
| `selectus-rating-b-fg` | `selectus-rating-b-bg`, `selectus-rating-b-bg-hover`, `selectus-rating-b-bg-active` | 4.5:1 |
| `selectus-rating-c-fg` | `selectus-rating-c-bg`, `selectus-rating-c-bg-hover`, `selectus-rating-c-bg-active` | 4.5:1 |
| `selectus-rating-abstain-fg` | `selectus-rating-abstain-bg`, `selectus-rating-abstain-bg-hover`, `selectus-rating-abstain-bg-active` | 4.5:1 |
| `selectus-rating-a-bg`, `selectus-rating-b-text`, `selectus-rating-c-text` | `bg`, `table-striped-bg`, `table-hover-bg` | 4.5:1 |
| `selectus-rating-b-marker`, `selectus-rating-c-bg`, `selectus-rating-abstain-bg` | `bg`, `table-striped-bg`, `table-hover-bg` | 3:1 |
| `state-neutral-fg` | `state-neutral-bg-hover`, `state-neutral-bg-active` | 4.5:1 |
| `state-success-fg` | `state-success-bg-hover`, `state-success-bg-active` | 4.5:1 |
| `state-warning-fg` | `state-warning-bg-hover`, `state-warning-bg-active` | 4.5:1 |
| `state-danger-fg` | `state-danger-bg-hover` | 4.5:1 |
| `box-info-text` | white | 4.5:1 |
| `box-success-text` | white | 4.5:1 |
| `box-warning-text` | white | 4.5:1 |
| `box-error-text` | white | 4.5:1 |
| `evaluation-insufficient-text` | white | 4.5:1 |
| `evaluation-neutral-text` | white | 4.5:1 |
| `evaluation-sufficient-text` | white | 4.5:1 |
| `qti-bad-response-warning` | white | 4.5:1 |
| `tag-selection-remove-selected-fg` | `tag-selection-remove-selected-bg` | 4.5:1 |
| `navbar-text` | `navbar-bg` | 4.5:1 |
| `navbar-link` | `navbar-bg` | 4.5:1 |
| `navbar-tools` | `navbar-bg` | 4.5:1 |
| `footer-text` | `footer-inner-bg` | 4.5:1 |
| `footer-link` | `footer-inner-bg` | 4.5:1 |
| `dialog-header` | `bg` | 4.5:1 |
| `content-header` | `bg` | 4.5:1 |
| `content-link` | `bg` | 4.5:1 |
| `link-subtle` | `bg-subtle` | 4.5:1 |
| `nav-link` | `bg` | 4.5:1 |
| `nav-link-hover` | `bg` | 4.5:1 |
| `segments-tab-link` | `bg` | 4.5:1 |
| `selectus-review-comment-link` | `bg` | 4.5:1 |
| `toolbar-breadcrumb-link` | `toolbar-breadcrumb-bg` | 4.5:1 |
| `coursesite-start-fg` | `coursesite-start-bg`, `coursesite-start-bg-hover`, `coursesite-start-bg-active` | 4.5:1 |
| `coursesite-book-fg` | `coursesite-book-bg`, `coursesite-book-bg-hover`, `coursesite-book-bg-active` | 4.5:1 |
| `coursesite-start-xs-fg` | `coursesite-start-xs-bg` | 4.5:1 |
| `coursesite-title` | `bg` | 4.5:1 |
| `autocomplete-suggestion-fg` | `autocomplete-suggestion-bg` | 4.5:1 |
| `chelp-fg` | `chelp-bg`, `chelp-bg-hover` | 4.5:1 |
| `coursesite-title-hover` | `bg` | 4.5:1 |
| `datecomp-day-fg` | `datecomp-day-bg` | 4.5:1 |
| `swiper-button-fg` | `swiper-button-bg` | 4.5:1 |
| `tag-selection-fg` | `tag-selection-bg`, `tag-selection-bg-hover`, `tag-selection-bg-active` | 4.5:1 |
| `tag-selection-selected-fg` | `tag-selection-selected-bg`, `tag-selection-selected-bg-hover`, `tag-selection-selected-bg-active` | 4.5:1 |
| `wizard-step-main-active-fg` | `wizard-step-main-active-bg` | 4.5:1 |
| `reminder-marker-fg` | `reminder-marker-bg` | 4.5:1 |
| `topicbroker-title-abbr-fg` | `topicbroker-title-abbr-bg` | 4.5:1 |
| `forum-message-header-author` | `forum-message-header-bg` | 4.5:1 |
| `forum-message-header-modified` | `forum-message-header-bg` | 4.5:1 |
| `forum-message-footer-attachment` | `forum-message-footer-bg` | 4.5:1 |
| `otp-caret` | `otp-cell-bg`, `otp-cell-active-bg` | 4.5:1 |
| `offcanvas-link-hover` | `$o-offcanvas-bg` (the Sass colour of the menu) | 4.5:1 |
| `qti-text-gap-border` | `bg` | 3:1 |
| `selectus-status-preparation`, `-published`, `-published-screening`, `-closed-screening`, `-closed`, `-reporting` | `bg` | 3:1 |
| `recorder-record`, `recorder-record-hover`, `recorder-symbol`, `recorder-symbol-hover`, `recorder-symbol-active` | `bg` | 3:1 |
| `box-note-border` | `box-note-bg` | 3:1 |
| `box-tip-border` | `box-tip-bg` | 3:1 |
| `message-line-warning-fg` | `message-line-warning-bg` | 4.5:1 |
| `message-line-error-fg` | `message-line-error-bg` | 4.5:1 |
| `dialog-header` | `dialog-header-bg` | 4.5:1 |
| `login-lead`, `login-lead-header` | `bg` | 4.5:1 |
| `login-help-link` | `login-help-bg` | 4.5:1 |
| `widget-figure-desc` | `bg` | 4.5:1 |
| `navbar-logout` | `bg-raised` | 4.5:1 |
| `offcanvas-link` | `$o-offcanvas-bg`, `offcanvas-dropdown-bg` | 4.5:1 |
| `offcanvas-link-active` | `offcanvas-link-active-bg` | 4.5:1 |
| `tree-insertion-source-fg` | `tree-insertion-source-bg` | 4.5:1 |
| `panel-placeholder-fg` | `panel-placeholder-bg` | 4.5:1 |
| `icon-changes`, `icon-mandatory`, `certification-certified`, `certification-certifying`, `portfolio-assignment`, `im-status-available`, `im-status-dnd`, `im-status-unavailable`, `qti-status-ended`, `qti-status-invalid`, `lecture-rollcall-pending` | `bg` | 3:1 |
| `box-note-text` | `box-note-bg` | 4.5:1 |
| `qti-box-text` | `qti-order-sources-bg`, `qti-order-target-bg` | 4.5:1 |
| `coursesite-meta-author`, `video-correct-segments`, `video-notcorrect-segments` | `bg` | 4.5:1 |
| `practice-progress-l2-bg` | `bg` | 3:1 |
| `datecomp-month-fg` | `datecomp-month-bg` | 4.5:1 |
| `day-nav-day-selected-fg` | `day-nav-day-selected-bg` | 4.5:1 |
| `empty-indicator`, `colored-icon-blue`, `-green`, `-orange`, `coursesite-progress-icon`, `user-status-inactive-border`, `user-status-deleted-border` | `bg` | 3:1 |
| `radial-progress-number`, `radial-progress-percentage-{success,neutral,info,danger,warning}` | `radial-progress-inset-bg` | 4.5:1 |
| `figure-card-icon-{blue,green,yellow,orange,red,grey}` | `figure-card-icon-{hue}-bg` | 3:1 |
| `im-summary-fg` | `im-summary-bg` | 4.5:1 |
| `im-groupchat-vip`, `im-groupchat-anonymous` | `im-chat-message-bg` | 4.5:1 |
| `edu-type-*-course` (8), `entry-benefit`, `proj-timeline-overdue`, `proj-timeline-today`, `authentication-level{1,2,3}` | `bg` | 4.5:1 |
| `lp-pagination-done-fg` | `lp-pagination-done-bg` | 4.5:1 |
| `curriculum-avatar-fg` | `curriculum-avatar-bg` | 4.5:1 |
| `curriculum-widget-template-label` | `curriculum-widget-template-label-bg` | 4.5:1 |
| `highscore-rank-first`, `highscore-rank-second` | `highscore-podium-first-bg`, `-second-bg` | 3:1 (large number) |
| `user-initials-fg` | `user-initials-base-bg` | 4.5:1 |
| `content-fieldset-legend`, `lecture-excluded`, `creditpoint-withdraw`, `creditpoint-deposit`, `course-quota-intern-quota`, `course-quota-intern`, `course-quota-extern`, `todo-overdue`, `quality-unrated`, `forum-quote`, `citation-quote`, `qti-bad-response` | `bg` | 4.5:1 |
| `dropdown-link-active` | `dropdown-link-active-bg` | 4.5:1 |
| `autocomplete-fg` | `autocomplete-bg` | 4.5:1 |
| `color-picker-selected-fg` | `primary` | 4.5:1 |
| `tag-fg` | `tag-bg`, `tag-bg-hover`, `tag-bg-active` | 4.5:1 |
| `competence-fg` | `competence-bg`, `competence-bg-hover` | 4.5:1 |
| `media-version-label-fg` | `media-version-label-bg` | 4.5:1 |
| `qti-text-entry-fg` | `qti-text-entry-bg` | 4.5:1 |
| `qti-text-entry-filled-fg` | `qti-text-entry-filled-bg` | 4.5:1 |
| `highscore-position-title` | `bg` | 3:1 (large text) |
| `qti-status-not-presented`, `qti-status-not-answered` | `bg` | 3:1 |
| `qpool-arrow-draft-border` | `qpool-arrow-bg` | 3:1 |
| `speedometer-default-text`, `speedometer-success-text`, `speedometer-warning-text`, `speedometer-danger-text` | `bg` | 3:1 (value, 48px) |
| `video-recording-info-fg` | `video-recording-info-bg` | 4.5:1 |

The compile checks a pair only when one of its colours differs from the default, with the rules of the client override below.

Hover, pressed and off states must keep 4.5:1 like the normal state (decision D9). The light buttons, the table filters, the toggle switch, the scope toggle and the rating buttons of selectus pass in light. An override that breaks one of their pairs stops the compile. The pair of a hover or pressed state is the text token of the state on its fill (decision D13). The 8 hover and pressed fills of the light buttons and the table filters must differ from their normal fill (`$o-token-state-fills`; a black fill gets lighter, decision D20): an equal colour stops the compile like a contrast pair (`$o-contrast-check: "warn"` reports it).

The light defaults fail 1 pair: `focus` on `bg`, `#66AFE9` on `#FFFFFF` has 2.37:1 (3:1 needed). It stays unchanged until the decision on the focus ring (decision D23). The navbar text and the navbar tools passed with 4.48:1 before decision D23: `$o-navbar-color` and `$o-navbar-tools-permanent-color` default to `o-a11y-color($navbar-default-color, $o-navbar-bg)` now, the Bootstrap `#777777` adjusted to 4.5:1 on the navbar (`#747474`, 4.67:1). A theme that sets the variables keeps its value.

```css
.my_note        { color: var(--o-text-muted); border: 1px solid var(--o-border); background: var(--o-bg-subtle); }
.my_marker_done { border-left: 4px solid var(--o-state-success-border); }
```

**Client override:** a client theme sets the map `$o-tokens` in its config, before the import of the light theme. The key is the name of a semantic, state or element token without `--o-`. The value is one of these:

- the name of any other token: semantic, state or element (`"primary"`, `"state-success-text"`, `"tree-link"`). It gives a `var()` reference to that token. A chain of references is allowed: `"qti-menu-link": "link"` and `"link": "blue-700"` give the QTI menu link the colour `blue-700`. A loop of references stops the compile;
- a palette step `"{hue}-{step}"`, also `"theme1-500"`. It gives `var(--o-{hue}-{step})`;
- a neutral step `"neutral-{step}"`, e.g. `"neutral-600"`. It gives `var(--o-neutral-{step})`;
- `"white"` or a colour. A colour that is not a palette step gives a literal value; for an element token, the literal value is its base colour `--o-base-{name}`.

```scss
$o-tokens: (
	"text-muted": "slate-600",
	"state-success-bg": "green-600",
	"state-success-fg": "white",
	"link": #1A4F99,
	"navbar-bg": "primary",
	"navbar-text": "primary-fg",
	"navbar-link": "primary-fg",
	"navbar-tools": "primary-fg",
	"qti-menu-link": "link",
	"selectus-review-glow": "orange-500",
);
```

The override `"navbar-bg": "primary"` needs navbar text, link and tool colours with 4.5:1 on it, here `"primary-fg"`. Without them, the compile stops: the default link colour `#475467` has 1.45:1 and the default text colour `#777777` has 1.18:1 on the primary colour of `light`.

An override changes every rule that reads the token. A rule that still reads the Sass variable (colour maths, Bootstrap) keeps the Sass value. To change both, set the Sass variable of the theme: this works only for an element token with a Sass variable default.

Example: `"btn-primary-bg": "green-700"` changes the background of the primary button, its shade tokens `-bg-hover` and `-bg-active` and the shadow of `.btn-primary` (`color-mix()` of the token). The border token `btn-primary-border` and its shade tokens keep the colour of the Sass variable. A theme that sets `$btn-primary-bg` changes all of them.

A shade token is not an element token: the default of an element token cannot be a shade token. An override in `$o-tokens` can reference a shade token (`"tree-link": "btn-primary-bg-hover"`).

The compile stops for an unknown token, an unknown palette step, a value that is not a colour, and an override that breaks a contrast pair: 4.5:1 for text (`text`, `text-muted`, `heading`, `link`, `link-hover`, `primary-text` on `bg`; `text` on `bg-hover`, `bg-selected` and `bg-raised`; `primary-fg` on `primary`; per state `fg` on `bg`, `soft-fg` on `soft-bg`, `text` on white and on `soft-bg`), 3:1 for `focus` on `bg` and the state `border` on white. A pair that already fails with the default colours fails only when its foreground is overridden or when its ratio gets lower than with the default colours. The default `--o-focus` (`#66AFE9`, Bootstrap) has 2.37:1 on white.

The contrast check covers the palette, the theme colours and `$o-tokens`. `$o-contrast-check` in `_functions.scss` sets its mode: `"error"` (default) stops the compile at a pair below its ratio, `"warn"` reports each such pair as a Sass warning and accepts it. A client theme whose client insists on colours with too little contrast sets `$o-contrast-check: "warn";` in its config; the theme then does not meet WCAG 2.1 AA. The `light` and `openolat` themes always use `"error"`.

**Modules:** a module reads a token where a token has exactly the same colour: `color: $text-muted` is now `color: var(--o-text-muted)`, `o-state-color("success", "text")` is `var(--o-state-success-text)`, `o-palette("blue", 500)` is `var(--o-blue-500)`. A colour of an element with an element token reads that token, not the shared token of the same colour: `var(--o-selectus-review-glow)`, not `var(--o-primary)`. The buttons read their tokens through the mixin `button-variant`. An override of a token changes every module rule that reads the token. `theme_color_baseline.properties` counts the old colour references that remain: 140 in 30 modules (152 before the follow-up of slice 19 that counts names with `_`, 302 before slice 19 with the pattern of decision D32 E1, 285 with the pattern before, 501 before slice 18, 851 before slice 17, 944 before slice 16, 1102 before decision D29). These colours stay Sass, because no exact token exists or a token changes the colour:

- the literals that decision D29 keeps: the 5 hotspot styles of the QTI editor (they copy the colours of `hotspotInteraction.html`, a comment names the template), the image styles, figures and text markers stored in content, the brand colours of third-party logins and of XING, the developer tools (`_devel.scss`) and the print sheets (member list, offline test);
- the rules without a reader and the invalid declarations of the B4 review (block B4-18, 45 literals in 11 files): they go after a check of the stored HTML content;
- the copy of the edu-sharing stylesheet (`_edusharing.scss`);
- the four element colours of the links section above (`$o-content-link-hover-color`, `$o-footer-default-link-hover-color`, `$o-navbar-login-color`, ...) and the three Sass variables with a default from `o-a11y-color()`;
- the rules of the mixins that `email.css` reads (`o-make-message-box` in `_mixins.scss`; `o-alert-variant` reads `var(--o-text)` since slice 19, `email.css` does not include it) and the `darken()` of `_button_variant.scss` for a colour argument;
- values inside Sass functions and the arguments of mixins that do not take tokens;
- `o-state-color()` with the styles `tint`, `light` and `dark` in a function or a mixin (no state token; a rule reads an element token with the Sass expression as default: `lecture-rollcall-pending`, `user-status-inactive-border`, `user-status-deleted-border`);
- the variables with the default `none` (`$o-tree-bg`, `$o-tree-link-bg`, `$o-tree-link-active-bg`) or `inherit` (`$o-main-right-bg`, `$o-main-center-bg`) and `$o-navbar-border-color` (client themes set `none`), and the gradient stop list `$o-login-form-bg-gradient-steps` (not counted, decision D31). Since OO-10043 slice 17 a named variable with a translucent `color-mix()` default feeds an element token with that default (`login-bg`, `login-infomessage-bg`, `imagebg-text-bg`, `fact-icon-bg`, `drawing-select-bg`, `practice-play-main-bg`, `video-startbox-main-bg`; D31 Q2), and the shorthand border variables are width, style and a colour token: `border: $o-card-border-width $o-card-border-style var(--o-card-border)` (`card-border`, `widget-border`, `fact-sheet-border`, `fact-sheet-separator-border`, `dashboard-widget-border`, `table-details-container-border`, `activity-{add,modify,remove}-border`, `repo-info-border`, `repo-info-separator-border`, `editor-layout-inner-{h,v}-border`; D31 Q1). The shorthands are removed: a theme that sets one gets one compile warning per stylesheet, the value has no effect. `$o-login-border` stays valid (default `null`): when a theme sets it, the login box uses the value as it is (`o-login-border()` in `_config.scss`) and the compile prints a warning. Since slice 18 the same holds for the course, curriculum, user, chat, high score, projects, radial progress, Selectus and various modules: 14 more shorthands are removed (colour tokens `empty-border`, `icon-panel-border`, `lp-border`, `curriculum-widget-border`, `curriculum-widget-card-border`, `assessment-inspection-border`, `highscore-podium-border` of `$o-hs-podium-border`, `user-info-portrait-border` of `$o-user-info-portrait-border` and `$o-users-portrait-img-border`, `user-info-border`, `user-info-profile-border`, `user-info-divider-border`, `panel-authentication-border`, `indicator-separator-border`), and 8 more translucent defaults feed a token (`brand-box-bg`, `empty-panel-icon-bg`, `figure-card-icon-bg`, `course-style-cover-text-bg`, `lp-bg`, `lp-pagination-in-progress-bg`, `coursesite-row-hover-bg`, `catalog-header-box-bg`). The gradients `$o-not-available-bg` and `$o-cat2-header-noimage-bg` stay the customisation point (decision D11), their stops read tokens. Since slice 19 the other modules read element tokens of their Sass variables too, and 8 more shorthands are removed (colour tokens `step-border` of `$o-step-border` with `$o-step-border-line-width`, `-style`; `ac-billing-address-border` of `$o-ac-ba-border`; `folder-row-thumbnail-image-border`; `swiper-button-border`; `topicbroker-details-separator-border`, `topicbroker-title-abbr-border`, `topicbroker-placeholder-title-abbr-border`, `topicbroker-weighting-grid-border` of the `$o-tb-*` shorthands). `$o-hs-position-title-color` stays valid (default `null`, decision D32 E2): the switch is `$o-hs-position-title-gradient` (default `true`), the colour without the gradient is the token `highscore-position-title`; a theme that sets the old variable keeps its gradient or colour (`o-hs-position-title-gradient()`, `o-hs-position-title-color()` in `_config.scss`) and the compile prints a warning.

**Colour maths:** a hover, active, edge, tint or gradient colour of a token reads a shade token (see **Shade tokens** above). A translucent colour mixes its token with `transparent` (see **Translucent colours** above). A named `$o-*` colour variable with an opaque computed default (`$o-qti-match-selected-bg`, `$o-scope-toggle-on-bg-hover-color`, ...) feeds its own element token, and the default keeps its maths in `_config.scss`. `rgba(x, 0)` is the keyword `transparent`. A muted text reads `--o-text-muted`, a disabled text `--o-text-disabled`, never `lighten($text-color, ...)` (decision M9): the help texts of the file upload and of the TinyMCE dialogs, the unfiltered curriculum rows, the context help icon next to a label and the help tooltip (`chelp-tooltip`), the deleted course elements in the editor overview (`course-node-deleted`) and the disabled toolbar links (`toolbar-link-disabled`, `toolbar-nav-link-disabled`). The state label hovers of `_states.scss` keep `color-mix()` with black (decision M2).

Six rules read the semantic tokens of OO-10043 step 3, because the rule held exactly the Sass variable of the token in the same role: `.o_highlight_on_hover:hover` (`_helpers.scss`), the sticky columns of a hovered table row (`_table.scss`) and the hovered video category (`_video_edit.scss`) read `--o-bg-hover`; the item of `ul.o_dropdown` (`_various_modules.scss`) reads `--o-bg-raised`; the two item lines of `.o_assistance_accordion` read `--o-border-subtle`. The other references of `$table-bg-hover`, `$hr-border` and `$dropdown-bg` have another role and stay Sass for now.

**Bootstrap colour variables and hex values:** the Bootstrap colour variables that the modules read are tokens with the exact value (decision M7 a): `$input-*` (`input-bg`, `input-fg`, `input-disabled-bg`, `input-placeholder`), `$table-*` (`table-border`, `table-striped-bg`, `table-hover-bg`), `$dropdown-*`, `$popover-*`, `$modal-*`, `$pre-*` (`code-block-bg`, `code-block-fg`, `code-block-border`), `$legend-*`, `$panel-bg`, `$progress-bar-color`, `$thumbnail-border`, `$blockquote-border-color` and `$navbar-default-bg`, `$navbar-default-border` of the selectus review comments. A token with a Sass variable default keeps the Bootstrap variable as customisation point. `input-bg`, `input-disabled-bg`, `popover-bg` and `table-hover-bg` reference the semantic token whose base colour has the value of the Bootstrap variable in every theme. The Bootstrap greys `$gray`, `$gray-light`, `$gray-dark`, `$gray-lighter` and `$gray-base` are element tokens of their element with the exact value (`alert-close`, `chart-help-line`, `qti-gaptext-alternative-bg`, `page-table-border`, ...). A light border with `$gray-lighter` or `$hr-border` reads `--o-border-subtle` (`$gray-lighter` is `$hr-border` in every theme), `$input-border-focus` reads `--o-focus`, `$text-color` reads `--o-text`. White reads `--o-white` as text on a fill, `--o-bg` as a surface, and `--o-bg-raised` as the surface of a popover, a dialog, a lightbox or a floating tool bar. A hex value within 3 of 255 of a token reads the token (decision B3 a): light surfaces read `--o-bg-subtle`, a disabled field reads `--o-neutral-100`, a gradient stop reads `--o-neutral-50`, the text on the video task markers reads `--o-neutral-0`. `#EEEEEE` as a border reads `--o-border-subtle`.

**Brand colours to states:** the rules read the state tokens instead of the Bootstrap state colours (decisions S1 a to S5 a, S7 a, S8 a, A1 c). The role follows the kind of the reference: `$brand-{state}` is the role `bg`, `$state-*-text` and `$alert-*-text` are `text`, `$state-*-bg` and `$alert-*-bg` are `soft-bg`, `$state-*-border` is `soft-border`. An alert text is `soft-fg`, a text on a solid fill is `fg`. `info` is the state `neutral`. An icon or a text that read `$brand-*` reads the role `text` (`var(--o-state-danger-text)`), because the role `bg` of success and warning has less than 3:1 on white. A Sass variable of `_config.scss` with a state colour as default (`$o-toolbar-close-color`, `$o-qti-success`, `$o-im-newmessage-color`, `$o-color-info`, ...) gets the default `o-state-color(state, role)`: `text` when all its rules set a text or icon colour, else the role of the reference. The variable stays the customisation point. The selected table row and the right cells of the Kprim and true/false tables read the soft fill of their state. The dirty button and the book button of the course site have the fill of the warning state and the dark text of the role `fg` (`#321E00`, 8.05:1); a theme that sets its own fill keeps the adjusted white text. `.btn-danger` reads the tokens `btn-danger-bg`, `btn-danger-fg`, `btn-danger-border` with the defaults `state-danger-bg`, `state-danger-fg`, `state-danger-border` and 4 shade tokens. `_bootstrap.scss` hands them to the Bootstrap import, like the tokens of the primary button. A client theme changes the danger button alone with `$o-tokens: ("btn-danger-bg": ..., "btn-danger-border": ...)` and the whole danger state with the state tokens.

**Old state variables:** the rules of `theme.css` and `content.css` do not read the Bootstrap variables `$brand-danger`, `$brand-warning`, `$brand-success`, `$brand-info` and `$state-{danger,warning,success,info}-{text,bg,border}` any more (tests `stateRulesReadTokens`, `bootstrapStateRulesReadTokens`). These variables still exist. Three things read them:

- `email.css`: the mail clients do not support custom properties, so `email.scss` reads Sass values (`$o-box-info-color`, `$btn-success-bg`, ...);
- the login link of the navigation bar: `$o-navbar-login-color`, default `$brand-warning`;
- the own rules of a client theme.

A client config that sets one of the 16 variables to a value other than the default of the light theme gets one compile warning for `theme.css` and one for `content.css`, none for `email.css`. `$o-legacy-color-defaults` in `_config.scss` holds the light defaults, the mixin `o-legacy-color-warning` in `modules/_tokens.scss` compares them. The warning does not stop the compile. To change a state colour, set `$o-palette` (the colour of a hue), `$o-states` (the hue of a state) or `$o-tokens` (one token), e.g. `$o-tokens: ("state-danger-bg": "red-600", "state-danger-text": "red-600")`. A theme that keeps the variables on purpose sets `$o-legacy-color-warning: false;` in its config. `themes.README` has the steps for theme authors.

OO-10043 removed 226 colour variables from `_config.scss` that nothing read (checked in `light`, `openolat` and 198 client themes). A client theme that sets one of them compiles without an error, and the setting has no effect. 125 of them gave the colour of a module element before OO-10043: this colour is now an element token with a reference default, e.g. `$o-selectus-review-glowing-color` is `--o-selectus-review-glow`. A theme changes it with `$o-tokens`.

`oo-docs.scss` (icons for the user manual) imports `palette_theme`, `palette_neutral`, `modules/palette_vars` and `modules/tokens`, because `_icons.scss` reads tokens.

**Tests:** `ColorTokensTest` checks the compiled `light/theme.css`: the semantic, state and element tokens, the contrast pairs (`elementContrast` for the element tokens), the shade tokens of the buttons, each with the colour of `darken()`, for the light buttons and the table filters with the amount of `o-contrast-shade-amount()` (`buttonShadeTokens`), that the button rules read the tokens and the shade tokens (`buttonRulesReadTokens`), the shade tokens of the module colour maths, each with the colour of `darken()` or `lighten()` (`moduleShadeTokens`), that a rule reads each of them, that the translucent colours mix the token and that the muted texts read the text tokens (`colorMathRulesReadTokens`), that the gradient stops, the labels, the named variables and the translucent defaults of the remaining colour maths read tokens (`remainingColorMathRulesReadTokens`), that no module calls `o-a11y-color()` and that a rule reads each token with an adjusted text colour (`rulesReadAdjustedTextTokens`), that the link rules read the link tokens (`linkRulesReadTokens`), the light values of the new semantic tokens (`newSemanticTokenDefaults`) and the rules that read them (`surfaceRulesReadTokens`), the light values of the tokens of the Bootstrap colour variables and greys and a rule that reads each of them (`bootstrapComponentTokens`), the shadows, the dark modal layer and the light veils (`shadowAndVeilRulesReadTokens`), the 18 default steps of the neutral scale in all token files (`neutralScale`), that no module reads a Bootstrap state colour and that the rules read the state tokens (`stateRulesReadTokens`), that the Bootstrap state rules read the state tokens (`bootstrapStateRulesReadTokens`), the shade tokens of the state tokens and a rule that reads each of them (`stateShadeTokens`), the defaults of the danger, dirty and book button tokens and of the selected tags and the soft fills of the selected row and the right cells (`stateButtonTokens`), the tokens of the message boxes, the evaluation form, the portlet edit mode and the bad response warning (`messageBoxAndEvaluationTokens`), the rating stars, the shadow colour and the four contrast fixes of decision D4 (`decisionRulesReadTokens`), the selected competence, the hover, pressed and off states, the close icons of a tab, the dark tints, the immunity proof shadows, the navigation bar highlight, the peekview background, the not available visual and the removed dead block of `_config.scss` (decisions D8 to D11, `decisionD8ToD11RulesReadTokens`), the video comment layer, the text tokens of the hover and pressed states and their rules, the unchanged fills of light, the minimum amount of 3% and the selectus rating bars (decisions D12, D13, D15, `decisionD12D13D15RulesReadTokens`), the selectus abstention bar, the label of bar B, the rating marks and explanation texts and their contrast, the course style border, the shade function `"state"` of the 8 state fills with its model for a black fill and the navbar text (decisions D16 to D20, D23, `decisionD16ToD23RulesReadTokens`), the selectus bar A without a rule and its class in the Java code, the mark B and the letters B and C on a white, a striped and a hovered table row with their adjustment against the darkest row, the 8 state fills as the only shade tokens with the function `"state"` and the marks of no rating and of the abstention (decisions D25 to D28, `decisionD25ToD28Selectus`), the literal colours of the B4 review: per block the rules read the tokens of the recommended option, including the long selector lists of the file upload hover, the element tokens of the AI marks and the glows reference the state fills, the own tokens keep the colours of today, the new pairs pass, the kept literals and the rows of B4-18 are still in the SCSS (decision D29, `decisionD29LiteralsReadTokens`), the focus ring: the token default, the ring rule on `:focus-visible`, no ring of the browser, no halo or glow on a focused field, the inset rings, the navigation bar and footer tokens, the unchanged radio cards and no literal of the old focus colour in the theme SCSS (decision D24, `decisionD24FocusRing`), the framework modules: the rules read the element tokens of their Sass variables and the new pairs pass (OO-10043 slice 16, `frameworkModulesReadTokens`), the assessment and content modules, the translucent defaults and the split shorthand borders (OO-10043 slice 17, `assessmentAndContentModulesReadTokens`), the course, curriculum, user, chat, high score, projects, radial progress, Selectus and various modules (OO-10043 slice 18, `courseUserAndVariousModulesReadTokens`), the other modules, the toolbar, the high score switch and the variables with upper-case letters (OO-10043 slice 19, `otherModulesReadTokens`), the other button rules (`otherButtonRulesReadTokens`), the compile warning for the old state variables and its switch (`legacyColorWarning`), that every token of `_tokens.scss` resolves to a colour or to a `color-mix()` of a colour (`tokensResolveToColors`), and that every `var(--o-*)` in `theme.css`, `content.css` and `oo-docs.css` of `light` and `openolat` has a definition (`referencesHaveDefinitions`). `ThemeColorRatchetTest` compares the old colour references per module with `theme_color_baseline.properties`: the baseline is 140 references in 30 modules. It does not count `color-mix(`, the decided form of a translucent colour (decision M3); a Sass `mix(` or `color.mix(` counts (pattern `(?<![\w-])mix\(`, OO-10043 slice 17). It counts hex values, named colours, `$brand-*` (not a local variable such as `$brand-primary-bg`), the Bootstrap colour variables of all components (`$panel-*`, `$dropdown-*`, `$popover-*`, `$modal-*`, `$hr-border`, ...), the `$o-*` colour variables, colour maths, `rgb()` and `hsl()` literals and the compile-time colour functions (`o-a11y-color()`, `o-state-color()`, `o-palette()`, `o-swatch-color()`). It does not count variables with radius, width, height, padding, size, margin or offset in the name (`$btn-border-radius-base`, `$o-proj-header-background-height-max`), nor variables whose name ends with "style" (`$o-qti-match-source-border-style`: `dashed`), "gradient-to", "gradient-steps", "-img", "-position-left", "-position-top", "-ratio", "-break" or "-blend-mode" (the direction, the stop list, the image and the position of the login background; slice 17, decision D31; the ratio and the break point of the project header image and the blend mode of the catalog header, slice 18). The name classes of the patterns accept upper-case letters (slice 19, decision D32 E1: `$o-qti-textEntryInteraction-bg`, `$o-qti-badResponse-color`). Sass treats `_` and `-` in a name as equal, so the test replaces `_` by `-` in a variable name before the count (`$o-o_speedometer-danger-border-color`, `$o_color_note`; test `countUnderscoreVariables`). Since OO-10043 slice 10 the variables of the course element styles count (`$o-cn-style-bg`, `$o-cn-style-cover-text-bg`): "style" in the middle of a name is their namespace, not the property (test `countStyleVariables`). Slice 11 splits the shorthand `$o-cn-style-border` into `$o-cn-style-border-line-width`, `$o-cn-style-border-line-style` (not counted) and the token `course-style-border` (decision D19); a theme that still sets `$o-cn-style-border` gets one compile warning per stylesheet, the value has no effect. It does not count `_bootstrap.scss`: the file only hands the Sass variables to the Bootstrap import. The test `bootstrapHasNoOwnRules` fails when `_bootstrap.scss` gets a rule; put the rule into the module of its component.

## 27. SCSS Module Index

### 27.1 Core Modules

| Module | Purpose |
|---|---|
| `_bootstrap.scss` | Bootstrap 3.4.1 imports. Hands the Sass variables to the Bootstrap import, has no own rules |
| `_tooltip.scss`, `_modal.scss`, `_progress.scss`, `_panel.scss`, `_form_bootstrap.scss`, `_buttons.scss`, `_navbar_bootstrap.scss`, `_dropdown.scss`, `_accessibility.scss` | Own rules of the Bootstrap components (tooltip, popover and glossary tooltip, modal, progress bar, panel, form controls and input groups, buttons, navbar, dropdown, `.sr-only`). `_modules.scss` imports them directly after `modules/bootstrap` (OO-10043) |
| `_button_variant.scss` | Mixin `button-variant` that reads the colour tokens and their shade tokens (replaces the Bootstrap mixin) |
| `_mixins.scss` | OpenOlat SASS mixins (message boxes, icons, buttons) |
| `_icons.scss` | Font Awesome 6 icon definitions |
| `_helpers.scss` | Spacing, flex, scrolling, buttons, typography utilities |
| `_fonts.scss` | Font imports and declarations |
| `_colors.scss` | Colour classes of the colour chooser (`o_color_*`) on the swatch tokens |
| `_swatches.scss` | Swatch classes `o_swatch_{hue}` with the tokens `--o-sw-*` |
| `_palette_vars.scss` | Palette values as `--o-{hue}-{step}` on `:root` |
| `_states.scss` | Semantic states: `o_state_*` classes and the wiring of the status classes (imported last) |
| `_tokens.scss` | Base, semantic, state, element and shade tokens on `:root`, client overrides `$o-tokens` (not in `email.css`) |
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

**Changed in 21.2.0:** One rule in `o_ainvoke()` puts the focus back after a server redraw. It replaces `o_info.lastFormFocusEl` and the class `o_can_have_focus`, which are removed (OO-9603).

| Mechanism | Behaviour |
|---|---|
| Intentional focus move | `FormItem.setFocus()` and `Link.setFocus()` render `autofocus` or an inline `focus()`. Use them only when the focus must move to a new place. |
| Form focus (command 11) | The server sends command 11 with `formName`, `formItemId` and `validation`. `o_ffSetFocusArray()` tries each target with `o_ffSetFocus()`. Priority: 1. the first error field (`o_errorFieldSelector`) when `validation` is `true`; 2. the given `formItemId`; 3. weak: an error field without the flag, then `[autofocus]`. A weak candidate does not act when the response returns the focus to a dialog opener, or when the element that had the focus survives a redraw. When the redraw removed that element, the weak candidate wins over the ancestor fallback (for example the password field after the user name step of the login). Only command 11 selects the text of an input. Nothing happens while the focus is inside an open `<dialog>`. |
| Validation flag | `FormBasicController` sets `validation: true` on `EVNT_VALIDATION_NOK` and after `formInnerEvent()` when the source is a `FormLink` and the base `validateFormLogic()` ran in that event (for example a confirm link that validates the form). Inner events without a validation, and field changes that validate, keep the focus where it is. |
| Redraw rule | When command 2 replaces a component, `o_redrawFocusAnchor()` records the focused element (after a mouse click: the clicked element, see "Mouse opener"). At the end of the response, `o_restoreFocus(o_resolveFocusTarget(anchor))` puts the focus back, but only when the focus is on `body`, no dialog opens, no dialog opener got the focus back and no deferred focus is pending. The rule also covers a redraw that arrives with a later poll (for example after a download). |
| Fallback chain | `o_resolveFocusTarget()`: the same id, else the table row with the same `data-row-key` and the same slot, else the table, else the closest ancestor that survived the redraw and has a height (an empty container with 0 px is skipped). The ancestor gets a temporary `tabindex="-1"` (`o_focusableUntilBlur()`) and the focus moves there without scrolling, because a container is often taller than the window. The focus never falls back to `body`. |
| Busy dialog | While `#o_ajax_busy` is open, `o_applyFocus()` defers the focus of a visible, focusable element; `removeAjaxBusy()` applies it. Any other element is not deferred, so the next candidate gets its chance. |
| Mouse opener | Safari and Firefox do not focus a button or link on click. A `mousedown` listener in the capture phase records the clicked link, button, form control or `label.control` in `o_info.lastClickedEl`. `o_beforeserver()` keeps it as `o_info.focusBeforeServer`; `o_ainvoke()` reads this value once and uses it for the dialog stack and the redraw rule. |
| Scroll and focus | `o_scrollTopAndFocus()` scrolls to the top first, then sets the focus, so the page does not jump twice. A redraw with a scroll to the top restores the focus without scrolling. |
| Dialogs and callouts | `o_waitForVisibleThenFocusDialog()` waits until the new dialog is visible and focuses its first focusable element. **(Changed in 21.1.0)** Clipped (hidden) elements are skipped, so the focus no longer lands on the `sr-only` close button. |
| Focus return | `pushdialogfocus` / `popdialogfocus` keep a stack of focus anchors (`o_info.focusReturnStack`). After the close, `o_ainvoke()` resolves `o_info.pendingFocusReturn` with `o_resolveFocusTarget()` (same fallback chain as the redraw rule) and scrolls it into view only when needed (`o_scrollToElementIfInvisible()`). See [Callout Triggers and Focus](#161-callout-triggers-and-focus-a11y). |
| Dropdown return | An action in a Bootstrap dropdown returns the focus to the toggle (`hide.bs.dropdown`). A focus anchor of a menu item points to the toggle of its menu, except when a redraw replaces only a part inside the open menu. |
| Skip link | The first element of the page is `<a href="#o_main_container" class="sr-only">` "Skip to main content" (`fullwebapplayout.html`). |

*Figure 9: Focus after a server response — command 2 records the focused element, then command 11, the dialog return, the dialog open focus and the redraw rule run in this order; the dialog return and the redraw rule share the fallback chain (diagram in the HTML version)*

Order at the end of `o_ainvoke()`:

```
command 2   record the anchor of the focused element (first replaced component only)
1.          command 11: first error (validation), formItemId, weak candidates
2.          dialog return (pendingFocusReturn)
3.          focus into a newly opened dialog (pendingDialogFocus)
4.          redraw rule, only when the focus is on body
```

Rules for new code:

- Do not add focus opt-in classes or `onfocus` hooks. The redraw rule covers every focusable element.
- A focusable element needs a DOM id that stays the same across redraws. Give it an id in the renderer, and keep component objects when a controller rebuilds its toolbar (for example `RepositoryEntryRuntimeController.initToolbar()` reuses the status and role dropdowns).
- Do not send command 11 to restore the focus after a redraw. Use `setFocus()` only for an intentional move.

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
| Space / Enter in the colour picker and the icon selector | Selects the entry and closes the dropdown. **(Changed in 21.2.0)** One `keydown` handler with `preventDefault()`: Enter no longer submits the surrounding form. |
| Enter / Space / Arrow Down / Arrow Up on a dropdown toggle | Opens the menu. When a key opened it, the focus moves to the first item (Arrow Up: the last item). |
| Escape in a dropdown | Closes the menu and returns the focus to the toggle, also when the focus is in a field of the menu. |
| Tab in a dropdown | Closes the menu when the focus leaves it; in a `role="menu"` menu the focus moves on from the toggle. |

### 36.5 A11y Helpers in the JS Layer

| Function / command | Purpose |
|---|---|
| `o_ffSetFocus()`, `o_ffSetFocusArray()` | Form focus (command 11): error field, `formItemId`, `[autofocus]` |
| `o_redrawFocusAnchor()` | Records the focused element when command 2 replaces its component (**Changed in 21.2.0**) |
| `o_focusAnchor()`, `o_resolveFocusAnchor()` | Anchor of an element (id, ancestor ids, table, row key, slot) and its lookup after a table reload |
| `o_resolveFocusTarget()` | Anchor lookup with the fallback to the closest surviving ancestor, never `body` (**Changed in 21.2.0**) |
| `o_restoreFocus()` | Puts the focus back without selecting the text; keeps the caret when the value did not change (**Changed in 21.2.0**) |
| `o_applyFocus()` | Focuses an element, defers it while the busy dialog is open, reports success; when the focus succeeded, a second attempt after the next tick keeps it against typeahead.js (**Changed in 21.2.0**) |
| `o_waitForVisibleThenFocusDialog()` | Initial focus in dialogs and callouts |
| `pushdialogfocus`, `popdialogfocus` | Focus return to the trigger |
| `setattribute` | Change one attribute (e.g. `aria-expanded`) without re-render |
| `o_scrollToElementIfInvisible()` | Scroll only when needed, also inside modal content |
| `triggerClick()` | Keyboard activation for link buttons |
| `o_doEscapeDispatch()` | Escape closes the top layer |
| section handler (`click.oSection keydown.oSection`) | Disclosure state for `.o_section_toggle` (**New in 21.1.0**) |

### 36.6 A11y Rules of the Theme

- **Contrast:** `o-a11y-color()` in `_functions.scss` adjusts text colours to 4.5:1 against their background (see [Accessibility Functions](#6-accessibility-functions)).
- **Screen-reader text:** `.sr-only` is forced to black on white (`_accessibility.scss`, not in `content.css`) so contrast checkers do not report it; templates use `$r.screenreaderOnly("text")`.
- **Visible focus:** one focus ring for every control, see the focus ring below.
- **Reduced motion:** `prefers-reduced-motion` rules in 10 modules, for example ghost button transitions (`_helpers.scss`), the AI pulse (all animation off) and the wavy AI marks (solid).
- **Icons:** icon fonts are decorative; links render them with `aria-hidden="true"` and put the text in a `span` or `sr-only` span.

**Focus ring (OO-10043, decision D24).** Every control shows the same ring on `:focus-visible`: `outline: 2px solid var(--o-focus); outline-offset: 2px`. The gap of 2px shows the page background, so the ring needs 3:1 on the page background only (contrast pair `focus` on `bg`). A mouse click on a button, a link or a checkbox shows no ring. A text field shows the ring also after a click (the browser rule of `:focus-visible`).

- Colour: the token `focus`, base colour `$o-focus-color` (`_config.scss`). Default: `$brand-primary` adjusted to 3:1 on `$body-bg` with `o-a11y-color()`. A theme that sets `$input-border-focus` keeps that colour (`_config.scss` reads `meta.global-variable-exists("input-border-focus")` before the Bootstrap defaults). A value that the theme sets for `$o-focus-color` or `$input-border-focus` is not adjusted. Light `#2E6BBF` (5.29:1 on white), openolat `#126365` (7.00:1). `$input-border-focus` keeps its value, the radio cards read it.
- Mixins (`modules/_focus.scss`, imported after the Bootstrap mixins in `_bootstrap.scss` and `content.scss`): `o-focus-ring($offset, $color)`, `o-focus-ring-inset($color)` (offset minus the ring width), `o-focus-ring-on-fill` (`currentColor`, offset minus twice the ring width). `tab-focus` and `form-control-focus` replace the Bootstrap mixins: the Bootstrap rules of links, buttons, checkboxes, radios, file inputs and `.form-control` set `outline: 0` on `:focus` and the ring on `:focus-visible`. A focused form field has the border `var(--o-focus)`, no halo and no glow (`.form-control`, TinyMCE dialog fields, QTI text entry, file chooser, table search, autocomplete menu, formula field).
- Fallback: `:where(:focus-visible)` in `_accessibility.scss` gives the ring to every other focusable element. Its specificity is 0, so a rule that sets an outline wins, as over the ring of the browser.
- Width and gap: `$o-focus-ring-width` (2px), `$o-focus-ring-offset` (2px).
- Navigation bar and footer: the tokens `navbar-focus` and `footer-focus` are the focus colour when it has 3:1 on the navigation bar (footer), else the link colour of the navigation bar (footer), adjusted to 3:1 (`o-contrast-hover()`, `o-a11y-color()`). The dropdown menus of the navigation bar keep `focus`.
- New code: no own focus style. A control that touches its neighbours or that a container with `overflow: hidden` cuts gets `@include o-focus-ring-inset;` in a rule with `:focus:focus-visible`; on a fill in the colour of the ring `@include o-focus-ring-on-fill;`.

| Controls | Ring | Reason |
|---|---|---|
| Button groups with two buttons or more (`.btn-group:has(...)`), `.btn-group-vertical`, segments (`.o_segments .btn`) | inset | the buttons touch (margin -1px) |
| Input groups (`.input-group .form-control`, `.input-group-btn > .btn`) | inset | the field and its buttons touch |
| Pills and table filters (`.nav-pills > li > a`), tabs (`.nav-tabs > li > a`) | inset | pills are 2px apart; tabs touch each other and the line below them |
| Pagination (`.pagination > li > a`, `> span`) | inset | the items touch |
| Dropdown menu items (`.dropdown-menu > li > a`) | inset | the item has the full width of the menu, and the menu can scroll |
| Checkbox button group (`.o_checkbox_button_group > div:has(input:focus-visible)`) | inset, on the item | the radio is invisible; the items touch |
| Navigation bar items and logo (`.o_navbar .o_navbar-nav > li > a`, `.o_navbar-brand`) | inset, `navbar-focus` | the top edge of the window cuts the ring; the items touch |
| Course list entry (`.o_repo_entry_list_item a, button`) | inset | image, title and access area cut their content (`overflow: hidden`) |
| Curriculum implementation row (`.o_row_implementation a, button`) | inset | the row cuts its content |
| Custom table row link (`.o_row_link`) | inset | the link is the full row: the rows touch, a scrolling table body cuts the ring |
| A fill in the colour of the ring in these groups: `.btn-primary`, `.btn.active`, `.nav-pills > li.active > a`, `.o_filter_active`, `.o_filter_open`, `.pagination > .active`, `.dropdown-menu > .active > a`, start and book button of the course list | `currentColor`, offset -4px (`o-focus-ring-on-fill`) | an inset ring in the colour of the fill has 1:1; the text colour of the fill has 4.5:1 (contrast pairs); at -4px the fill shows on both sides of the ring |
| Radio card (`.o_radio_cards label input:focus-visible ~ .o_radio_card`) | with the gap, on the card | the card is the control; the radio or checkbox inside it shows no ring (decision D30) |
| File chooser (`.o_realchooser:focus-visible + .o_fakechooser`, `+ .o_dnd .btn`) | on the visible field or button | the chooser input is invisible |
| Formula field (`math-field:focus`, `:focus-within`) | ring also after a click | a text field; the focus is in the shadow tree of the field |

### 36.7 Known Gaps (verified 2026-09-25, updated 2026-10-27)

- Object selection: the button `aria-label` contains raw HTML from `IdentitySelectionSource`; `label for` points to a non-existent id.
- Error fields have no `aria-invalid`. Only `TextElementRenderer` links the error text with `aria-describedby`.
- An element without a stable id falls back to the closest surviving ancestor. Example: after a stack panel pop (assessment form "Zwischenspeichern") the focus goes to the stack panel container.
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

### 37.2 21.2.0 (in development)

Colour epic OO-9989:

| Issue | Change | Section |
|---|---|---|
| OO-9991 | Palette of 14 hues x 10 steps, swatch tokens `--o-sw-*`, `--o-{hue}-{step}` on `:root` | [Colour Palette](#61-colour-palette-for-user-colour-choosers) |
| OO-9992, OO-9993 | Colour choosers on the palette, theme colours `--o-theme{n}` | [Colour Palette](#61-colour-palette-for-user-colour-choosers) |
| OO-10042 | 8 semantic states in 5 styles, `o_state_*` classes, `$o-state-wiring` for 395 status classes, `o-state-color()`, `o-swatch-color()`; `$o-labeled-*` removed | [Color System](#26-color-system) |
| OO-9994 | Base, semantic and state tokens `--o-base-*`, `--o-text`, `--o-state-{state}-{role}` on `:root`, client overrides with `$o-tokens`, unit tests `ColorTokensTest` and `ThemeColorRatchetTest` | [Colour Tokens](#265-colour-tokens) |
| OO-10043 | Modules read the tokens with `var(--o-*)`, 226 unused colour variables removed from `_config.scss` | [Colour Tokens](#265-colour-tokens) |
| OO-10043 | Token layers base, framework and component; 211 element tokens `--o-{element}-{part}` with a Sass variable default (86) or a reference default (125); 15 of them hold the text colours that a module adjusted with `o-a11y-color()`, no module calls the function; `$o-tokens` references any token, chains allowed; 38 contrast pairs of the element tokens, test `elementContrast`; 20 shade tokens for the button states, mixin `button-variant` reads tokens | [Token Layers](#261-token-layers), [Colour Tokens](#265-colour-tokens) |
| OO-10043 | Colour maths: 27 new shade tokens (47 in total) for hover, active, edge and tint, computed with `darken()` or `lighten()`, also from a semantic token (`focus-edge`); element tokens `course-node-deleted` and `immunity-proof-{state}-bg`; translucent colours as `color-mix()` of the token with `transparent`; 5 muted texts read `text-muted` or `text-disabled`; 6 new contrast pairs; tests `moduleShadeTokens` and `colorMathRulesReadTokens` | [Colour Tokens](#265-colour-tokens) |
| OO-10043 | Neutral scale `--o-neutral-{step}` (18 steps, `$o-neutral-hue`, `$o-neutral-chroma`); semantic tokens `text-disabled`, `bg-hover`, `bg-selected`, `bg-raised`, `border-subtle`, `shadow-color`, `backdrop`, `scrim`; 3 contrast pairs `text` on `bg-hover`, `bg-selected`, `bg-raised` | [Colour Palette](#61-colour-palette-for-user-colour-choosers), [Colour Tokens](#265-colour-tokens) |
| OO-10043 | Bootstrap colour variables that the modules read as 26 tokens with the exact value (`input-*`, `table-*`, `dropdown-*`, `popover-*`, `modal-*`, `code-block-*`, `legend`, ...), 12 element tokens for the Bootstrap greys; white reads `--o-white`, `--o-bg` or `--o-bg-raised`; 18 hex values within 3 of a token read the token; shadows, dark modal layer and light veils read `shadow-color`, `backdrop`, `scrim`; tests `bootstrapComponentTokens`, `shadowAndVeilRulesReadTokens` | [Colour Tokens](#265-colour-tokens) |
| OO-10043 | Brand colours to states: the module rules read the state tokens (25 icons and texts read the role `text`, tints read `soft-bg`); Sass variables with a state default read `o-state-color()`; danger button tokens `btn-danger-bg|fg|border` with 4 shade tokens and 3 contrast pairs; dirty and book button follow the warning state with the `fg` text; 9 new element tokens, 16 new shade tokens; tests `stateRulesReadTokens`, `stateButtonTokens` | [Colour Tokens](#265-colour-tokens) |
| OO-10043 | Bootstrap state rules read the state tokens: `.btn-success`, `.btn-info`, `.btn-warning`, alerts, labels, progress bars, panels, list groups, form validation, contextual table rows, `.text-*`, `.bg-*`, `mark` (mixins of `_state_variant.scss`); message boxes, evaluation form, portlet edit mode and bad response warning read element tokens that follow the state tokens; 21 shade tokens of the state tokens; tests `bootstrapStateRulesReadTokens`, `stateShadeTokens`, `messageBoxAndEvaluationTokens` | [Colour Tokens](#265-colour-tokens) |
| OO-10043 | Rating stars read `rating-star` (palette step `yellow-400`); all shadows read `shadow-color`; text of the selected tag (`tag-selected-fg`), chat icon hover, QTI error text (`qti-error-fg`) and tab close icon on a dark navigation bar (`o-contrast-hover()`) reach their contrast; active fill 16% darker for a bright fill with dark text (`o-token-active-percent()`); tokens of the ghost, mega, light, table filter, toggle, scope toggle, appointment and selectus rating buttons; tests `decisionRulesReadTokens`, `otherButtonRulesReadTokens` | [Colour Tokens](#265-colour-tokens) |
| OO-10043 | Remaining colour maths read shade tokens (darker, lighter, turned hue), own element tokens or `color-mix()` of a token; context help icon and disabled toolbar links read `text-muted` and `text-disabled`; compile warning for a client config that sets `$brand-*` or `$state-*`, switch `$o-legacy-color-warning`; 415 element tokens, 150 shade tokens, 161 contrast pairs in total; tests `remainingColorMathRulesReadTokens`, `legacyColorWarning` | [Colour Tokens](#265-colour-tokens) |
| OO-10043 | Hover, pressed and off states keep 4.5:1 (`o-contrast-shade-amount()` for the light buttons, the table filters and the scope toggle hover; `toggle-text`, selectus B text and C fill adjusted); text of a selected competence follows the selected tag (`competence-selected`); close icon of a tab adjusted to 3:1 on the navigation bar, own tokens `dropdown-tab-close`, `dropdown-tab-close-hover` in the "More" dropdown; dark tints read `backdrop`, immunity proof shadows read `shadow-color`; new tokens `navbar-highlight`, `not-available-dark`, `not-available-light`, `course-style-peekview-bg`; dead contrast block of `_config.scss` removed; 421 element tokens, 150 shade tokens, 174 contrast pairs in total; test `decisionD8ToD11RulesReadTokens` | [Colour Tokens](#265-colour-tokens) |
| OO-10043 | Video comment layer reads `backdrop` (D12); hover and pressed fills at least 3% darker or lighter, else the amount of Bootstrap with an adjusted text: text tokens `-fg-hover`, `-fg-active` of the light buttons and the table filters, `scope-toggle-on-hover-fg`, `o-contrast-shade-text()`, optional text arguments of `button-variant`, compile check `$o-token-state-fills` (D13); selectus rating bars read the button tokens (D15); `course-style-bg`; ratchet counts `$o-cn-style-*` colour variables; test `decisionD12D13D15RulesReadTokens`. Slice 11: abstention bar, label of bar B, rating marks and explanation texts read the rating tokens, new `selectus-rating-b-text`, `selectus-rating-b-marker` (D16 to D18); `$o-cn-style-border` split into `$o-cn-style-border-line-width`, `$o-cn-style-border-line-style` and `course-style-border` (D19); a black state fill gets lighter (`o-contrast-shade()`, shade function `"state"`, D20); navbar text and tools adjusted to 4.5:1 (D23); 434 element tokens, 138 contrast pairs; test `decisionD16ToD23RulesReadTokens` | [Colour Tokens](#265-colour-tokens) |
| OO-10043 | Focus ring (D24): token `focus` reads `$o-focus-color` (`$brand-primary` adjusted to 3:1 on `$body-bg`; light `#2E6BBF`, 5.29:1); one ring `outline: 2px solid var(--o-focus); outline-offset: 2px` on `:focus-visible` for every control (`modules/_focus.scss`: `tab-focus` and `form-control-focus` replace the Bootstrap mixins; fallback `:where(:focus-visible)`); no halo and no glow on the form fields; inset ring for the controls that touch or that a container cuts, `currentColor` on a fill; new tokens `navbar-focus`, `footer-focus`; 436 element tokens, 140 contrast pairs; test `decisionD24FocusRing` | [A11y Rules of the Theme](#366-a11y-rules-of-the-theme) |
| OO-10043 | Selectus marks (D25 to D28): the unused rule of bar A goes (bar A has `progress-bar-success`); mark B (`#A88700`) and letters B (`#856B00`) and C (new token `selectus-rating-c-text`, `#E00000`) keep 3:1 and 4.5:1 on a white, a striped and a hovered table row (`o-darkest-color()` in `_functions.scss`); the marks of no rating and of the abstention read `btn-default-bg` and `selectus-rating-abstain-bg` (`#555566` instead of black); 437 element tokens, 154 contrast pairs; test `decisionD25ToD28Selectus` | [Colour Tokens](#265-colour-tokens) |
| OO-10043 | Literal colours of the B4 review (D29): greys read `text`, `heading`, `text-muted`, `input-placeholder`, `border`, `border-strong`, `bg-subtle`, `bg-hover`, `bg-raised`, `bg`; the hover of the file upload button reads the default button; scroll shades, rank shadows and layers on images and video read `shadow-color`, `scrim`, `backdrop` with the alpha of today; the AI marks and the glows of new and highlighted posts read new element tokens with a reference to a state fill; status icons and texts read the state tokens; decorative colours read palette steps; own tokens with the colours of today for the one-time code field, the off-canvas menu, the selectus status icons, the recorder, the sticky table columns and 5 more; the hotspot styles, the styles stored in content, brand colours, developer tools and print sheets keep their literals; B4-18 waits for a check of the stored content; 471 element tokens, 169 contrast pairs; test `decisionD29LiteralsReadTokens` | [Colour Tokens](#265-colour-tokens) |
| OO-10043 | Framework modules (slice 16): the message boxes read `box-*-bg`; dialog header and footer, note, tip and important boxes, message lines, login page, widgets and dashboard widgets, personal menu and offcanvas menu, table, menu tree, status icons and drop places read 52 new element tokens fed by their Sass variables; no colour change; 525 element tokens, 195 contrast pairs; test `frameworkModulesReadTokens` | [Colour Tokens](#265-colour-tokens) |
| OO-10043 | Assessment and content modules (slice 17) and decision D31: the QTI, page editor, practice, video and video task modules read 90 new element tokens fed by their Sass variables (framework 16, component 74); an element token accepts a translucent `color-mix()` default; the shorthand border variables are width, style and a colour token (`$o-login-border` stays valid); the ratchet does not count `color-mix(` and the non-colour login variables; no colour change; 615 element tokens, 202 contrast pairs, ratchet 501; test `assessmentAndContentModulesReadTokens` | [Colour Tokens](#265-colour-tokens) |
| OO-10043 | Course, user and various modules (slice 18): the modules `_various_modules`, `_course`, `_selectus`, `_curriculum`, `_user`, `_highscore`, `_projects`, `_coursesite`, `_radial-progress`, `_im` read 123 new element tokens fed by their Sass variables (framework 48, component 75); 14 more shorthand borders are width, style and a colour token; the other modules read the tokens of `$o-toolbar-border-color`, `$o-slider-border`, `$o-portfolio-media-content-background-color`, `$o-coursesite-meta-author-color`, `$o-table-row-selected-color`; the ratchet does not count `-ratio`, `-break`, `-blend-mode`; no colour change; 738 element tokens, 246 contrast pairs, ratchet 285; test `courseUserAndVariousModulesReadTokens` | [Colour Tokens](#265-colour-tokens) |
| OO-10043 | The other modules (slice 19, decision D32): the ratchet pattern counts names with upper-case letters (E1, 285 to 302); 47 modules read 95 new element tokens fed by their Sass variables (base 5, framework 32, component 58) or existing tokens of their variables; 8 more shorthand borders are width, style and a colour token; `$o-hs-position-title-gradient` and the token `highscore-position-title` (E2, `$o-hs-position-title-color` stays valid); the toolbar reads tokens (E3); no colour change; 833 element tokens, 273 contrast pairs, ratchet 140 in 30 modules; test `otherModulesReadTokens`. Follow-up: the count treats `_` in a name as `-` (+12, 152); the speedometer and the recording info of the video recorder read 11 new tokens; 844 element tokens, 278 contrast pairs, ratchet 140; test `countUnderscoreVariables` | [Colour Tokens](#265-colour-tokens) |
