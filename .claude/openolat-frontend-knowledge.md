# OpenOlat Frontend Knowledge Base

> Compressed reference for developers working with the OpenOlat CSS/SASS framework and frontend rendering.
> For full documentation see `doc/openolat-frontend.md` and `doc/openolat-frontend.html`.
> The md and html references have the same 37 chapters: 13.1 form section, 13.2 search element, 16.1 callout a11y, 24.3 fact sheet, 24.4 sections, 35 info page pattern, 36 accessibility.
> State: release 21.1.0 (2026-09-25). `**New in 21.1.0:**` marks changes of 21.1.0; changes shipped in 21.0.x have no marker.

## 1. Technology Stack

- **Bootstrap 3.4.1** (SASS version): CSS foundation (grid, buttons, forms, navs, modals)
- **Font Awesome 6.7.2**: icons via `o_icon` base class + `o_icon_{name}` or `fa-{name}`
- **jQuery 3.7.1**: DOM manipulation, AJAX
- **Apache Velocity**: server-side HTML templating (`.html` files in `_content/`)
- **Java ComponentRenderers** (~540 classes): programmatic HTML generation
- **Custom SASS** (~100 modules): OpenOlat-specific styling
- **Dart Sass >= 1.79.0**: compilation via `compiletheme.sh`
- **TinyMCE 6.8.6** (folder `js/tinymce4/`): rich text editing
- **FullCalendar 6.1.15**: calendar widget
- **Vanilla JS Datepicker 1.3.4** (`js/datepicker/`): date picker
- **jQuery UI 1.13.2**: dialog, drag and drop, resize, slider
- **typeahead.js**: autocompletion (`tt-*`)
- **D3.js 7.9.0**: charts via jQuery plugins in `js/jquery/openolat/` (no Chart.js)
- **Leaflet 1.9.4**: maps (room management)
- **MathLive**: math input

No client-side framework (no React/Angular/Vue). All state lives on the server.

## 2. Theme Architecture

### Directory Layout
```
src/main/webapp/static/
  bootstrap/                    # Bootstrap 3.4.1 SASS partials
  themes/
    compiletheme.sh             # Compile script
    light/                      # BASE THEME (never modify directly)
      theme.scss                # Entry: license → functions → config → modules → patches
      content.scss              # iFrame content styles
      email.scss                # Email notification styles
      oo-docs.scss              # Icons only → oo-docs.css for the user manual
      _config.scss              # ALL variables (~2000 lines, !default)
      _functions.scss           # luminance(), contrast(), o-a11y-color(), o-on-color()
      _palette.scss             # Colour palette: $o-palette (14 hues x 10 steps), $o-swatches, aliases
      _palette_theme.scss       # Theme colours theme1..N from $o-theme-colors (brand colours)
      _modules.scss             # Import manifest (~100 modules)
      modules/                  # SCSS partials by feature
    openolat/                   # PRODUCT THEME (derives from light)
      theme.scss                # fonts → openolat_config → light/theme → openolat_theme
      _openolat_config.scss     # ~35 variable overrides
      _openolat_theme.scss      # CSS rule overrides (logo, login)
```

### Inheritance Model
```
Bootstrap 3.4.1 variables (!default)
  ← light/_config.scss overrides (!default)
    ← custom_config.scss overrides (no !default, imported BEFORE light)
```

Variables with `!default` are only set if not already defined. Custom themes import their config BEFORE `light/theme`, so their values win.

### Three CSS Outputs
| File | Purpose |
|------|---------|
| `theme.css` | Main application styles (every page) |
| `content.css` | Learning content in iFrames |
| `email.css` | Email notification styles (inlined) |
| `oo-docs.css` | Icon classes + fonts, copied into OpenOLAT-docs |

### Variable Naming
- `$o-` prefix = OpenOlat-specific (e.g., `$o-navbar-height`, `$o-tree-link-color`)
- No prefix = Bootstrap standard (e.g., `$brand-primary`, `$font-size-base`)

### Key Variable Categories in _config.scss
| Section | Lines | Examples |
|---------|-------|---------|
| Bootstrap overrides | 34-80 | `$btn-default-bg`, `$btn-default-border`, `$input-border` |
| Status colours | none | `$o-states` (`_palette.scss`), `$o-state-wiring` (`modules/_states.scss`) |
| Brand colors | 73-96 | `$brand-primary`, `$brand-info`, `$brand-warning`, `$brand-danger`, `$brand-success` |
| Layout | 273-286 | `$o-page-width-max`, `$o-main-*-bg` |
| Navbar | 289-346 | `$o-navbar-height`, `$o-navbar-bg` |
| Toolbar | 380-424 | `$o-toolbar-bg-color`, `$o-toolbar-breadcrumb-*` |
| Form | 427-445 | `$o-radio-card-*`, `$o-date-*` |
| Tree | 448-475 | `$o-tree-*`, indentation, padding |
| Course | 677-760 | `$o-course-*`, `$o-lp-*` |
| Content Editor | 963-1017 | `$o-editor-*`, `$o-ce-padding-*` |
| QTI | 1021-1115 | `$o-qti-*`, interaction styles |
| Email | 1488-1503 | `$o-email-*` |
| Fact sheet | 1873-1885 | `$o-fact-sheet-*`, `$o-fact-*` (**New in 21.1.0**) |

Line numbers as of 21.1.0. Borders: `$btn-default-border` `#6c757d` (buttons, dropdown toggles), `$input-border` `#d0d5dd` (fields). Ghost button text = `$btn-primary-bg`. Section headings: `$o-font-size-section-title` (~18px).

### Accessibility Functions (_functions.scss)
```scss
luminance($color)                              // Relative luminance (0=black, 1=white)
contrast($color1, $color2)                     // Contrast ratio
o-a11y-color($color, $background, $target:4.5) // Auto-adjust for WCAG 4.5:1
o-on-color($bg, $candidates, $ratio:4.5)       // First candidate with enough contrast on $bg; compile error if none
o-contrast-hover($hover, $base, $background, $ratio:3) // Hover colour when it has $ratio or more contrast than the base, else the base
o-darkest-color($colors...)                    // The colour with the lowest luminance: adjust a colour against several backgrounds (D26)
o-contrast-shade-amount($fg, $bg, $max, $function:"darken", $ratio:4.5, $min:3%) // Largest amount from $min up to $max at which $fg keeps $ratio on the shaded $bg, else $max (D9, D13)
o-contrast-state-text($fg, $bg, $state-bg, $ratio:4.5) // Text of a state: $fg, or $fg adjusted for the state fill (white or black when o-a11y-color() fails) (D13)
o-contrast-shade-text($fg, $bg, $max, $function:"darken", $ratio:4.5, $state-bg:null) // o-contrast-state-text() on the fill of o-contrast-shade-amount() (D13)
```

### Custom Theme Creation Pattern
1. Copy `openolat/` → `mytheme/`
2. Rename `_openolat_*` → `_mytheme_*`, update imports
3. Override variables in `_mytheme_config.scss` (typically 20-50 vars: brand colors, fonts, navbar, footer, login)
4. Override CSS rules in `_mytheme_theme.scss` (typically 50-150 lines: logo, login page)
5. Compile: `./compiletheme.sh mytheme`

## 3. Page DOM Skeleton

```
<body id="o_body" class="[o_dmz]">
  #o_navbar_wrapper > #o_navbar_container > .o_navbar
    .o_navbar-brand                    // Logo
    .o_navbar_tabs                     // Site tabs (Home, Courses, Groups...)
    ul#o_navbar_tools_permanent        // #o_navbar_imclient, #o_navbar_search_opener, #o_navbar_my_menu (logout inside)
  #o_main_wrapper > #o_main_container
    TooledStackedPanel (.o_with_toolbar .o_with_breadcrumb)  // wraps toolbar + content
      #o_main_toolbar .o_toolbar       // SIBLING of #o_main (breadcrumb + tools)
      #o_main [CSS Grid]              // 3-column layout
        #o_main_left > #o_main_left_content     // Tree menu (resizable)
        #o_main_center > #o_main_center_content // Main content
        #o_main_right > #o_main_right_content   // Optional sidebar
  #o_footer_wrapper > #o_footer_container       // Footer
```

**Important:** `#o_main_toolbar` is a **sibling** of `#o_main`, NOT a child. Both are children of the TooledStackedPanel wrapper. Some pages (catalog, coaching) have no `#o_main` — they render directly inside the panel.

### CSS Grid Layout
```scss
#o_main {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto;  // left | center | right
  &.o_hide_main_left  { grid-template-columns: minmax(0, 1fr) auto; }
  &.o_hide_main_right { grid-template-columns: auto minmax(0, 1fr); }
  &.o_hide_main_left.o_hide_main_right { grid-template-columns: minmax(0, 1fr); }
}
```

## 4. Component Rendering

### AJAX Cycle
```
User action → o_XHREvent() → POST → Server dispatch → Controller.event()
  → Business logic → setDirty(true) → RENDER dirty components
  → JSON: [{action:"updcmp", id:"o_c12345", content:"<html>"}]
  → jQuery('#o_c12345').replaceWith(newHTML)
```

### Component ID Convention
Every component: `id="o_c{dispatchID}"`. Used for AJAX DOM replacement.

### Key Renderers (~540 total)

**Containers:** VelocityContainerRenderer (templates), PanelRenderer, LayeredPanelRenderer (modals)

**Links:** LinkRenderer → `<a id="o_c{id}" onclick="o_XHREvent(...)">` with optional `.btn`, `.o_button_ghost`; disabled: `o_disabled disabled`

**Callout triggers:** `setAriaDialogOpener()` → `role="button" aria-haspopup="dialog" aria-expanded`; focus returns to the trigger on close (`pushdialogfocus`/`popdialogfocus`). **New in 21.1.0:** rolled out to table action columns, table tools, filters and many buttons; focus goes to the first visible element in the callout.

**Form Elements:**
| Element | DOM |
|---------|-----|
| TextElement | `<input type="text" class="form-control">` in `.form-group` |
| TextAreaElement | `<textarea class="form-control">` |
| SelectboxElement | `<select class="form-control">` |
| SingleSelection (radio) | `<div class="radio"><label><input type="radio">` |
| MultipleSelection | `<div class="checkbox"><label><input type="checkbox">` |
| DateChooser | `<div class="o_date"><div class="input-group o_date_picker"><input class="form-control o_date_day datepicker-input">` |
| FormToggle | `<button role="switch" class="o_button_toggle o_toggle_on">` |
| FormLink | `<a class="btn btn-default">` |
| FormSubmit | `<button class="btn btn-primary">` |
| ObjectSelection | `button.o_expand_button.o_selection_display[aria-haspopup=dialog]` + `button.o_selection_browse_button`; disabled: `disabled o_disabled`; current user first with "(me)" |
| RelativeDate | `div.o_relative_date.input-group` + read-only input |
| SearchElement (**New in 21.1.0**) | `div.o_search.o_search_{default,large,typeahead}[role=search] > .input-group > .o_search_input_wrapper > input.o_search_input[role=searchbox]` + `a.o_search_reset` + `a.o_search_button` |
| FormSection (**New in 21.1.0**) | `fieldset.o_form_section` > `div.o_section_sub_title` (SUB_TITLE) or `legend > h4.o_section_title[.o_section_toggle]` + `div.o_section_content[.collapse]` |

**New in 21.1.0:** label icon `FormItem.setLabelIconCss("o_icon o_icon-fw o_icon_locked")` → `<i aria-hidden="true">` before the label text. Settings page blueprint: GUI demo "Example settings" (`GuiDemoSettingsController`).

**FlexiTable:** Full structure (each section optional):
```
.o_table_tabs                          // Tab buttons (Favoriten, Suche...)
.o_table_filters_wrapper.o_expanded    // Extended filter pills
  .o_table_filters_row > ul.nav.nav-pills.o_table_filters
    > li > a.btn.btn-default.o_table_filter[.o_filter_active]
      > span (label) + i.o_icon.o_icon-fw.o_icon_caret
.o_table_toolbar                       // Search (.o_table_search > .o_search, 21.1.0) + tools (.o_table_tools)
.o_table_batch_buttons                 // Bulk actions (shown when rows selected)
.o_table_wrapper.o_table_flexi         // Table container
  CLASSIC: .o_scrollable_wrapper > .o_scrollable > table.table > thead/tbody
  CUSTOM:  .o_table_body > .o_table_row > .o_repo_entry_list_item
  .o_table_pagination                  // Pagination
```
Filter buttons: `FlexiFiltersComponentRenderer` renders `<a class="btn btn-default o_table_filter">`. Active state `o_filter_active` goes on the `<a>`, NOT the `<li>`. Styled via `button-variant()` mixin with `$o-table-filter-color/bg/border` and `$o-table-filter-active-*` variables. Caret icon: `<i class="o_icon o_icon-fw o_icon_caret">`.

View toggle: `.o_sel_table` (classic) / `.o_sel_custom` (card/list). Active has `.active` class. Selected row: `tr.o_row_selected`.

**New in 21.1.0:** `TranslateCellRenderer` → link with `o_icon_language` + term (action `translate`).

**Tree:** `.o_tree > ul[role=tree].o_tree_l0 > li[role=treeitem] > div.o_tree_l{n}` → `a.o_tree_oc_l{n}` (opener) + `span.o_tree_link.o_tree_l{n} > a > span.o_tree_item`

**Toolbar:** `.o_toolbar` → `.o_breadcrumb > ol.breadcrumb` + `.o_tools_container > .o_tools.o_tools_{left|center|right|right_edge}` + `.o_tools_segments > li > div.o_segments.btn-group` (selected `a.btn-primary`)

**Modal:** `div.o_layered_panel.o_layer_{n}` → `dialog.dialog.modal.show[aria-modal]` → `.modal-dialog.modal-lg` → `.modal-content` → `.modal-header` + `.modal-body` + `.modal-footer`

**New in 21.1.0:** `FactSheet` → `.o_fact_sheet > h3.o_fact_sheet_title + .o_facts (grid, min 320px) > .o_fact > .o_fact_icon + .o_fact_body (.o_fact_label, .o_fact_value, .o_fact_sub_value)` + `.o_fact_sheet_footer_buttons`. `ComponentList` → `div.o_component_list` (flex, gap 10px). `Sections` → `.o_sections > .o_section > fieldset > legend > h4.o_section_title.o_section_toggle` + `.o_section_content.collapse` (same header as FormSection; one delegated handler in `functions.js`).

**TabbedPane:** `.o_tabbed_pane` → `ul.nav.nav-tabs[role=tablist]` + `.o_tabbed_pane_content[role=tabpanel]`

## 5. CSS Class Reference

### Naming Convention
| Prefix | Origin | Example |
|--------|--------|---------|
| (none) | Bootstrap 3 | `btn`, `form-control`, `table`, `nav-tabs` |
| `o_` | OpenOlat core | `o_toolbar`, `o_tree`, `o_table_wrapper` |
| `o_icon_` | Icon classes | `o_icon_delete`, `o_icon_edit` |
| `o_sel_` | Test selectors | `o_sel_course_add_member` |
| `fa-` | Font Awesome | `fa-check`, `fa-times` |

### Spacing
```
.o_block          // margin: 1em 0
.o_block_small    // margin: 0.5em 0
.o_block_large    // margin: 2em 0
.o_block_top      // margin-top: 1em
.o_block_bottom   // margin-bottom: 1em
```

### Flex Helpers
```
.o_flex_block_one_line_left  // flex, nowrap, width:100%
.o_flex_item_fix             // no grow, no shrink
.o_flex_item_max             // grow, overflow ellipsis
```

### Typography
```
.o_xsmall / .o_small / .o_large / .o_xlarge  // font sizes
.o_disabled / .o_dimmed / .o_muted            // visual states
.o_nowrap / .o_deleted / .o_selected          // text treatments
.o_clickable / .o_hidden / .o_undecorated     // interaction hints
```

### Button Variants
```
Bootstrap: .btn.btn-default, .btn.btn-primary, .btn.btn-danger, .btn-xs/sm/lg
OpenOlat:  .btn.o_button_ghost, .btn.o_button_mega, .btn.o_button_primary_light
           .btn.o_button_call_to_action, .btn.o_button_dirty
           .btn.o_button_ghost.btn-danger (New in 21.1.0)
Groups:    .o_button_group, .o_button_group_left, .o_button_group_right
           .o_button_group.compact (New in 21.1.0), .o_btn_group_nowrap.btn-group
Add-on:    a.btn.input-group-addon in .input-group (button border, field keeps $input-bg)
```

### Mega Buttons
```
ul.o_mega_buttons                      // CSS Grid: repeat(auto-fill, minmax(207px, 1fr)), gap 8px
  > li > button.btn.o_button_mega      // 55px height, flex row
    > i.o_icon.o_icon_{name}           // Left icon
    > span                             // Text container
```
Used in course element selection screens (e.g., course Planner "Add Element").

### Message Boxes
```
.o_info / .o_note / .o_tip / .o_important / .o_success / .o_warning / .o_error
.o_warning_line / .o_error_line  // inline variants
```

### Form Field Errors
`SimpleFormErrorText` renders as `<div class="o_error">` (block element, NOT `<span>`), positioned below the input within `.form-group`.

### Widgets
```
.o_widgets                             // CSS Grid container (auto-fill, minmax 260px)
  .o_widget                            // Card: border, flex column, min-height
    .o_widget_header                   // .o_widget_title + .o_widget_icon
    .o_widget_content                  // Content area
    .o_widget_additional               // Optional footer area
```
Widget content types: `.o_widget_main.o_widget_main_figure` (figure), `.o_widget_main.o_widget_main_text` (text), `.o_widget_main` + `.o_widget_link` (component, e.g. mini table). Dashboard: `o_dashboard_widget`.

### Scrolling
```
.o_scrollblock           // overflow-x: auto
.o_scrollable_wrapper    // with shadow indicators
.o_scrollable            // inner scrollable area
```

## 6. Status Colours and Token Layers

Status colours: 8 semantic states in `$o-states` (`_palette.scss`): neutral blue, success green, warning yellow, critical orange, danger red, finished brown, inactive slate, highlight magenta. 5 styles: solid (default), dark (counters, deleted), tint (draft, preparation), light (not seen), text (outline, `o_labeled_light`). All foregrounds reach 4.5:1, checked at compile time.

- New status: `<span class="o_labeled o_state_success">`, modifiers `o_state_dark` / `o_state_tint` / `o_state_light`, outline `o_labeled_light o_state_<state>`. Or add the status class to `$o-state-wiring` in `modules/_states.scss` (395 classes, generated, `@extend %o_state_<state>_<style>`, imported last). Never set a colour on a status class in a module.
- Status colour without a label (icon, marker, bar, chart): `o-state-color($state, $role)`, roles `text` (text, icons), `bg` / `soft-bg` (fills), `border` (borders, markers, chart lines), `fg` / `soft-fg` / `soft-border`. Not `$brand-success|danger|warning|info` or `$state-*-text`.
- Fixed colour without a meaning (stars, pen colours): `o-palette($hue, $step)`. Swatch role of a hue: `o-swatch-color($hue, $role)`.
- No new Sass colour variable, no `darken()`/`lighten()`/`rgba($var)` on palette or state colours.
- Token layers (`modules/_tokens.scss`, OO-9994, OO-10043, not in `email.css`): **base** = palette (literal colours: `--o-{hue}-{step}`, `--o-theme{n}-{step}`, the neutral scale `--o-neutral-{step}`, `--o-white`, base colours `--o-base-*` from `$text-color`, `$brand-primary`, `$link-color`, ...), the 22 semantic tokens `--o-text`, `--o-text-muted`, `--o-text-disabled`, `--o-heading`, `--o-bg`, `--o-bg-subtle`, `--o-bg-hover`, `--o-bg-selected`, `--o-bg-raised`, `--o-border`, `--o-border-subtle`, `--o-border-strong`, `--o-primary`, `--o-primary-fg`, `--o-primary-hover`, `--o-primary-text`, `--o-link`, `--o-link-hover`, `--o-focus`, `--o-shadow-color`, `--o-backdrop`, `--o-scrim` (the last 3 are opaque: the rule sets the transparency with `color-mix(in srgb, var(--o-backdrop) 50%, transparent)`; `shadow-color` = all shadows, `backdrop` = dark layer behind a modal dialog, `scrim` = light veil over content, dark in a dark mode), the 56 state tokens `--o-state-{state}-{role}`, and the global elements (buttons, content text and headings: `$o-token-layer-base`, 55); **framework** = layout and generic UI of `org.olat.core.gui` and `org.olat.core.commons.services` (navbar, footer, login, dialogs, tree, toolbar, scope toggle, table, wizard, form elements, tags, message boxes, labels, date picker, ...: `$o-token-layer-framework`, 363); **component** = elements of one feature module (course, course site, QTI tests and question pool, practice, forum, chat, lectures, topic broker, folder, selectus, immunity proof, evaluation form, rating, ...: `$o-token-layer-component`, 426).
- Element token = a token of the three layer maps, 844 in total, `--o-{element}-{part}`. Default `($sass-default, (tokens))` (692: 570 a Sass variable, 56 a Sass expression, 66 a fixed colour such as `o-palette("yellow", 400)` or the colour of today of an own token, D29): the Sass value of the theme config feeds the token (client themes keep setting it), the token reads the first listed token whose default has the same colour (follows its override), else the palette step, else `--o-base-{name}`. The default can be a Sass expression for a text colour that `o-a11y-color()` adjusts for the background of one rule, e.g. `"link-subtle": (o-a11y-color($link-color, $panel-default-heading-bg), ("link",))`; the breadcrumb, table, course element and forum link tokens reference it. No module calls `o-a11y-color()` (test `rulesReadAdjustedTextTokens`): a new adjusted text colour is a new token. Default `"token-name"` (152): reference to another token of any layer, e.g. `"selectus-review-glow": "primary"` emits `var(--o-primary)`, `"qti-menu-link": "tree-link"`; no Sass variable, set with `$o-tokens`; loop or unknown name stops the compile. Literal colours only in the palette and in the shade tokens, every other token is a `var()` reference. A module reads the element token of its element (`var(--o-selectus-review-glow)`, not `var(--o-primary)`), else an exact token; `_mixins.scss` stays Sass (email).
- Shade token = the darker or lighter colour of an element, semantic or state token, computed at compile time (`$o-token-shades` in `modules/_tokens.scss`, 150 tokens): `"name": ("token", percent)` darkens (114), `("token", percent, "lighten")` lightens (27), `("token", angle, "hue")` turns the hue with `adjust-hue()` (1), `("token", percent, "state")` darkens a state fill or lightens it when it cannot get darker (8, the `-bg-hover|active` of the light buttons and the table filters; `o-contrast-shade()`, D20: a black active filter gets `#1A1A1A` and `#2B2B2B`). Button states (13 elements, 52 tokens): `--o-{element}-bg-hover` (10%), `-bg-active` (17%; 16% for a bright fill with dark text, `o-token-active-percent()`, so `#321E00` keeps 4.54:1 on the warning fill), `-border-hover` (12%), `-border-focus` (25%) for `btn-primary`, `btn-default`, `btn-danger`, `btn-dirty`, `btn-primary-light`, `btn-danger-light`, `state-neutral`, `state-success`, `state-warning`, `table-filter`, `table-filter-active`, `coursesite-start`, `coursesite-book`. Hover and pressed states keep the 4.5:1 of the normal state (D9): the `-bg-hover` and `-bg-active` of `btn-primary-light`, `btn-danger-light`, `table-filter`, `table-filter-active` take `o-contrast-shade-amount($fg, $bg, $max)` (`_functions.scss`), the largest amount up to 10% or 17% (steps of 1%) at which the text keeps 4.5:1; `$max` when the text passes at `$max` or already fails on `$bg`; `$max` too when no amount from 3% passes (`$min`, D13): the state keeps 10% or 17% and the text token of the state `{element}-fg-hover`, `-fg-active` (`o-contrast-shade-text()`, `_functions.scss`; follows `-fg` in light) holds the text adjusted for the fill, white or black when `o-a11y-color()` cannot reach 4.5:1 (light: `#EDEDED` 7%, `#F7F7F7` 3%, pressed table filter `#DBDBDB` 14%). The compile stops when one of these 8 fills has the colour of its normal fill (`$o-token-state-fills`). The other 98 keep the percentage of the colour maths they replace: tags, context help, immunity proof, `focus-edge`, tables in a message box (`box-*-table-border|stripe-bg|header-bg`), high score podium (`-stop-1|2|4`), labels without status (`labeled-*`), selectus rating buttons, catalog header (`catalog-header-noimage-stop-1|3|4`), state shades for the Bootstrap mixins (`state-*-soft-bg-hover` 5%, `state-*-text-hover` 10%, `state-danger-bg-hover`), ... Suffixes: `-hover`, `-active`, `-focus` (state), `-edge` (darker edge: border, outline, text stroke), `-tint` (lighter surface), `-stop-{n}` (gradient stop); a shade for an element without an own token takes the element and role name. It follows its token (Sass variable or `$o-tokens`), `$o-tokens` overrides it like an element token; a run time redefinition (dark mode) redefines the shade tokens too. A mixin with a token argument builds the shade name: `button-variant`, 6 mixins of `_state_variant.scss`, `o-make-glossy-bg`, `labeled-variant`. No `darken()` or `lighten()` of a token colour in a module: add or read a shade token.
- Translucent colour of a token = no token: `color-mix(in srgb, var(--o-backdrop) 50%, transparent)` for `rgba($modal-backdrop-bg, 0.5)` (same alpha, decision M3). Black shadows read `color-mix(in srgb, var(--o-shadow-color) 20%, transparent)`, the dark modal layer `--o-backdrop`, light veils `--o-scrim`, each with the alpha of the old `rgba()`; the shadows with `rgba(16, 24, 40, a)` read `shadow-color` with the same alpha too (D3, M8). Colours without a role (D11): dark tints read `backdrop` (skeleton, print, quiz overlay, AI feedback, admin tag stripes, `$dropdown-border`, `$o-lp-bg`), the immunity proof shadows `shadow-color`, the navbar highlight line `navbar-highlight` (`var(--o-white)`, not `scrim`: it stays light in a dark mode), the styled peekview `course-style-peekview-bg` (default `$o-cn-style-peekview-bg`, translucent), the stops of `$o-not-available-bg` read `primary`, `not-available-dark`, `not-available-light`. The dark layer of the video comment panel (`_video.scss`) reads `backdrop` 50% (D12). A named `$o-*` variable with a translucent default stays the customisation point, its default is the `color-mix()` (`$o-login-bg-color`); it feeds an element token with that default (`"login-bg": ($o-login-bg-color, ())`, D31 Q2; also `brand-box-bg`, `course-style-cover-text-bg`, `lp-bg`, `catalog-header-box-bg`, ...), the token has no contrast pair and no shade token. The state label hovers of `_states.scss` keep `color-mix(in srgb, var(--o-sw-bg), #000 10%)` (M2). Muted text: `var(--o-text-muted)`, disabled text `var(--o-text-disabled)`, never `lighten($text-color, ...)`.
- Bootstrap colour variables that a module reads are framework tokens with the exact value (OO-10043 step 4, decision M7 a): `input-bg`, `input-fg`, `input-disabled-bg`, `input-placeholder`, `table-border`, `table-striped-bg`, `table-hover-bg`, `dropdown-link`, `dropdown-link-hover`, `dropdown-link-hover-bg`, `dropdown-divider`, `popover-bg`, `popover-border`, `modal-content-border`, `modal-header-border`, `code-block-bg|fg|border` (`$pre-*`), `legend`, `legend-border`, `panel-bg`, `progress-bar-fg`, `thumbnail-border`, `blockquote-border`. A module reads `var(--o-input-bg)`, not `$input-bg`; a client theme keeps setting the Bootstrap variable. The Bootstrap greys (`$gray`, `$gray-light`, `$gray-dark`, `$gray-lighter`) have element tokens of their element (`alert-close`, `chart-help-line`, `qti-help`, ...); a light border reads `--o-border-subtle`. White: `--o-white` for text on a fill, `--o-bg` for a surface, `--o-bg-raised` for a popover, dialog, lightbox or floating tool bar. A hex grey within 3 of 255 of a token reads the token (`--o-bg-subtle`, a neutral step). Decision D29 (B4 review): greys read the semantic token of their role (text `text`, `heading`, `text-muted`, `input-placeholder`; lines `border`, `border-strong`, chart axes `text`, white rings `bg`; fills `bg-subtle`, `bg-hover`, `bg-raised`, `bg`), status icons and texts the state token of the meaning, decorative colours a palette step (`var(--o-orange-500)`), translucent literals `color-mix()` of `shadow-color`, `scrim`, `backdrop` or of an element token with a reference to a state fill (`ai-mark-*`, `*-glow`) with the alpha of today; own tokens with the colours of today where the role has no token (`otp-*`, `offcanvas-link-hover`, `selectus-status-*`, `recorder-*`, `table-sticky-col-bg`, ...). Literals that stay: the QTI hotspot editor styles (copy of `hotspotInteraction.html`), styles stored in content (image styles, figures, `o_tm_*`), brand colours, `_devel.scss`, print sheets (marker `o-color-keep`, D34); the rules without a reader of block B4-18 wait for a check of the stored content. New code never adds a literal.
- Buttons: `@include button-variant(var(--o-btn-primary-fg), var(--o-btn-primary-bg), var(--o-btn-primary-border));` (`modules/_button_variant.scss`, replaces the Bootstrap mixin). A token argument reads the shade tokens for hover, focus and active, a token without shade tokens stops the compile; a colour argument keeps the Bootstrap `darken()` states. Optional 4th and 5th argument `$color-hover`, `$color-active`: the text of the states (default `$color`); light buttons and table filters pass `-fg-hover`, `-fg-active` (D13). `_bootstrap.scss` and `content.scss` import the mixin. Contrast pairs: the `-fg` token on `-bg`, `-bg-hover` and `-bg-active`. `.btn-default`, `.btn-primary`, `.btn-danger` (`btn-danger-bg|fg|border`, defaults `state-danger-bg|fg|border`, `_bootstrap.scss` hands them to the Bootstrap import), the dirty button and the course site start and book buttons read tokens; `.btn-success|info|warning` read the state tokens (`state-{success|neutral|warning}-fg|bg|border` and their 4 shades; no use in OpenOlat). Other buttons: ghost (`btn-ghost-*`), mega (`btn-mega-*`), light primary and danger (mixin with `btn-primary-light-*`, `btn-danger-light-*`, text tokens `-fg-hover`, `-fg-active`), table filter (`table-filter-*`, `table-filter-active-*`), toggle switch (`toggle-*`), scope toggle (`scope-toggle-*`, hover text `scope-toggle-on-hover-fg`), appointment confirm (`appointment-*`), selectus rating (`selectus-rating-*`; the rating bars B, C and abstention read `selectus-rating-{b,c,abstain}-bg` and `-bg-hover` as gradient, D15, D16; label of bar B `selectus-rating-b-fg`, D17; bar A is `progress-bar-success` and has no rule, D25; marks `f_rating_1|2|3` read `selectus-rating-{c,a}-bg`, for B `selectus-rating-b-marker` 3:1, no rating `f_rating_0` reads `btn-default-bg`, abstention `selectus-rating-abstain-bg`, D18, D28; letters `o_rating_expl` A `selectus-rating-a-bg`, B `selectus-rating-b-text`, C `selectus-rating-c-text` 4.5:1; mark B and letters B, C adjusted against the darkest of `$body-bg`, `$table-bg-accent`, `$table-bg-hover` with `o-darkest-color()`, pairs on `bg`, `table-striped-bg`, `table-hover-bg`, D26), disabled `.btn-link` (`text-disabled`).
- Links: four families. `--o-link`/`--o-link-hover` (`$link-color`): the Bootstrap rules `a`, `a:hover|focus`, `.btn-link`, pagination hover read them (`_bootstrap.scss` hands `var(--o-link)` to the Bootstrap import and restores the Sass values after it), plus element tokens with the reference `link` (`segments-tab-link` = filter tab row above tables, `selectus-review-comment-link`, `toolbar-dropdown-active`, `im-buddies-link`). `--o-link-subtle` (`$link-color` adjusted to 4.5:1 on `$panel-default-heading-bg`): referenced by `toolbar-breadcrumb-link`, `table-link`, `table-striped-link`, `course-style-link`, `forum-message-header-author`; `$o-table-link-color`, `$o-toolbar-breadcrumb-link-color`, `$o-forum-message-header-author-color`, `$o-forum-message-footer-attachment-color` are removed. `--o-content-link` (`$o-content-link-color`): content links. `--o-nav-link`/`--o-nav-link-hover` (`$navbar-default-link-color`): tree and toolbar links (`tree-link`, `toolbar-link` follow it when their Sass variable has the same colour; `toolbar-nav-link` references `toolbar-link`). Tree, navbar, footer and login help keep own tokens. `.o_segments_linked` reads `link`. `$o-content-link-hover-color` and `$o-footer-default-link-hover-color` stay Sass (client values a token cannot carry).
- New customization point: add an element token with a reference default to the map of its layer and read it with `var()`; never a new Sass colour variable.
- New code reads `var(--o-*)`: `color: var(--o-text-muted)`, `border-left: 4px solid var(--o-state-success-border)`. No hex, no `$text-color`/`$gray-*`/`$brand-*`, no new Sass colour variable. `ThemeColorRatchetTest` fails when a module gets more old colour references than `theme_color_baseline.properties` (59 in 18 modules; `color-mix(` and the non-colour variables `-gradient-to`, `-gradient-steps`, `-img`, `-position-left|top`, `-ratio`, `-break`, `-blend-mode` do not count, D31; names with upper-case letters count, D32 E1; `_` in a name counts as `-`). A reference that stays by design has a silent marker (D34): `// o-color-keep(reason)` at the end of a line, `// o-color-keep-start(reason)` ... `// o-color-keep-end` on own lines around a block; reasons `hotspot`, `print`, `brand`, `devtool`, `stored-content`, `gradient`, `mask`, `email`, `mixin-argument`, `vendor-copy`, `a11y-checker` (`KEEP_REASONS`); the module count excludes it, the baseline line `keep.<reason>` holds the total (81) and must match. Never mark a leftover (B4-18, `none`/`inherit` defaults, link hover variables).
- Client override: `$o-tokens: ("text-muted": "slate-600", "state-success-bg": "green-600", "state-success-fg": "white", "navbar-bg": "primary", "navbar-text": "primary-fg", "navbar-link": "primary-fg", "navbar-tools": "primary-fg", "qti-menu-link": "link", "selectus-review-glow": "orange-500");` in the theme config; key = any semantic, state or element token, value = any token name (`var()` reference, chains allowed, a loop stops the compile), `"{hue}-{step}"`, `"neutral-{step}"`, `"white"` or a colour. Compile stops for unknown token/step and broken contrast pairs (element pairs too). An override does not reach rules that still read the Sass variable.
- Contrast check (palette, theme colours, `$o-tokens`): `$o-contrast-check: "error"` (default, compile stops) or `"warn"` (reports the pair, accepts it). Only a client theme whose client insists on the colours sets `"warn"` in its config; `light` and `openolat` never.
- Brand colours to states (OO-10043): all rules read the state tokens, never `$brand-success|info|warning|danger`, `$state-*-text|bg|border`, `$alert-*`, `$btn-success|info|warning|danger-*` (tests `stateRulesReadTokens`, `bootstrapStateRulesReadTokens`). Mapping: `$brand-{state}` = role `bg`, icons and texts = role `text`, `$state-*-text` = `text`, `$state-*-bg`/`$alert-*-bg` = `soft-bg`, `$state-*-border` = `soft-border`, alert text = `soft-fg`, text on a solid fill = `fg`; info = state `neutral`. A `_config.scss` variable with a state default uses `o-state-color(state, role)` and stays the customisation point. Dirty and book button: warning fill, dark `fg` text (`#321E00`); an own theme fill keeps the adjusted white.
- Bootstrap state rules: `_bootstrap.scss` and `content.scss` hand the state tokens to the Bootstrap import; `modules/_state_variant.scss` replaces `label-variant`, `progress-bar-variant`, `alert-variant`, `form-control-validation`, `table-row-variant`, `list-group-item-variant`, `bg-variant`, `text-emphasis-variant` (token argument reads shade tokens, colour argument = the Bootstrap mixin via `meta.get-mixin()`). Labels and progress bars: fill `bg`, text `fg`; alerts: `soft-bg`, `soft-border`, `soft-fg`; panels: heading `text` on `soft-bg`, border `soft-border`; list groups, table rows, `.text-*`, `.bg-*`, `mark`, form validation: `text` and `soft-bg`. Message boxes: `box-{info,success,warning,error}-border|text` (fill stays `$o-box-*-bg`); evaluation form: `evaluation-{insufficient,neutral,sufficient}`, `-text`, `-fill`; `portlet-edit-bg|border`; `qti-bad-response-warning`.
- Old state variables: `$brand-danger|warning|success|info` and `$state-{danger,warning,success,info}-{text,bg,border}` still exist, but only `email.css`, the navbar login link (`$o-navbar-login-color`) and the own rules of a client theme read them. A client config that sets one of the 16 to a value other than the light default (`$o-legacy-color-defaults` in `_config.scss`) gets one compile warning for `theme.css` and one for `content.css`; switch `$o-legacy-color-warning: false;`. Change a state colour with `$o-palette`, `$o-states` or `$o-tokens` (test `legacyColorWarning`).
- Fixed colours and contrast fixes: rating stars read `rating-star` (palette `yellow-400`, D1). Course element style border: `border: $o-cn-style-border-line-width $o-cn-style-border-line-style var(--o-course-style-border)` (D19, the shorthand `$o-cn-style-border` is removed; a theme that sets it gets a compile warning). The other shorthand borders follow the same pattern (D31 Q1): `border: $o-card-border-width $o-card-border-style var(--o-card-border)` (`card`, `widget`, `fact-sheet`, `fact-sheet-separator`, `dashboard-widget`, `table-details-container`, `activity-{add,modify,remove}`, `repo-info`, `repo-info-separator`, `editor-layout-inner-{h,v}`; slice 18: `empty`, `icon-panel`, `lp`, `curriculum-widget`, `curriculum-widget-card`, `assessment-inspection`, `indicator-separator`, `user-info`, `user-info-profile`, `user-info-divider`, `user-info-portrait`, `panel-authentication`, and `$o-hs-podium-border-width` with the token `highscore-podium-border`; slice 19: `folder-row-thumbnail-image`, `swiper-button`, `$o-ac-ba-border-width` with `ac-billing-address-border`, the `$o-tb-*` borders with `topicbroker-*-border`, `$o-step-border-line-width` with `step-border`); `$o-login-border` stays valid for a theme that sets it: the login rules read `o-login-border()` (`_config.scss`). `$o-hs-position-title-color` stays valid too (D32 E2): the switch is `$o-hs-position-title-gradient`, the colour without the gradient the token `highscore-position-title`. `tag-selected-fg` references `btn-dirty-fg`, `qti-error-fg` references `state-danger-fg`. The navbar icon hovers `im-newmessage-hover` and `navbar-tab-close-hover` get their default from `o-contrast-hover($hover, $base, $background)` (`_functions.scss`): the hover colour when it has 3:1 on the navbar or more contrast than the base, else the base. The compile adjusts `navbar-tab-close` and `-hover` to 3:1 on `navbar-bg` (`o-a11y-color(..., $o-navbar-bg, 3)`, D10); the close icon in the "More" dropdown reads `dropdown-tab-close`, `-hover` (no adjustment, pair 3:1 on `bg-raised`). `competence-selected` follows `tag-selected-fg`, adjusted to 4.5:1 on the pressed competence fill (D8). D9: `toggle-text` is the muted text adjusted for the track, the scope toggle hover lightens by `o-contrast-shade-amount(..., "lighten")` (below 3%: 10% and `scope-toggle-on-hover-fg` adjusted, D13; a hover fill of the theme keeps its text), selectus B has dark text `#242424`, selectus C the fill `#EB0000`.
- The word neutral: `--o-state-neutral-*` is the state (blue, Bootstrap `info`), `--o-neutral-{step}` the grey scale; the prefix `state-` keeps them apart (D6).
- Contrast pairs: 278 in `$o-token-contrast-pairs` plus 40 from the loop over `$o-states` (318). Hover, pressed and off states have pairs like the normal state (D9): the text token of the state on its fill (D13). No pair of the light defaults fails (the focus ring passes since D24); an override must not break a pair. `navbar-text` and `navbar-tools` pass since D23: `$o-navbar-color` and `$o-navbar-tools-permanent-color` default to `o-a11y-color($navbar-default-color, $o-navbar-bg)` (`#747474`).
- Focus ring (D24): one ring for every control on `:focus-visible`, `outline: 2px solid var(--o-focus); outline-offset: 2px` (`$o-focus-ring-width`, `$o-focus-ring-offset`). Token `focus` reads `$o-focus-color` (`_config.scss`): `$brand-primary` adjusted to 3:1 on `$body-bg` (light `#2E6BBF` 5.29:1, openolat `#126365`); a theme that sets `$input-border-focus` keeps it (checked with `meta.global-variable-exists()` before the Bootstrap defaults); a theme value is not adjusted. `$input-border-focus` keeps its value (radio cards). `modules/_focus.scss` (mixins only, imported after the Bootstrap mixins in `_bootstrap.scss` and `content.scss`): `o-focus-ring($offset, $color)`, `o-focus-ring-inset($color)` (-2px), `o-focus-ring-on-fill` (`currentColor`, -4px); `tab-focus` and `form-control-focus` replace the Bootstrap mixins: `outline: 0` on `:focus` (no ring after a click on a button, link or checkbox), the ring on `:focus-visible`, form fields with border `var(--o-focus)`, no halo, no glow. Fallback `:where(:focus-visible)` (`_accessibility.scss`, specificity 0). Inset ring (rule with `:focus:focus-visible`): button groups with 2 buttons or more, segments, input groups, pills and table filters, tabs, pagination, dropdown items (`_accessibility.scss`), navbar items and logo (`_navbar.scss`, colour `navbar-focus`), course list entry (`_coursesite.scss`), curriculum rows (`_curriculum.scss`), `.o_row_link` (`_table.scss`); on a fill in the ring colour (primary, active, active filter, pagination active, active dropdown item, start and book button) `o-focus-ring-on-fill`. `navbar-focus`, `footer-focus`: the focus colour when it has 3:1 on the navbar (footer), else the navbar (footer) link colour, adjusted to 3:1. New code: no own focus style; a control that touches its neighbours or sits in an `overflow: hidden` container takes `o-focus-ring-inset`. Test `decisionD24FocusRing`. Details: `doc/openolat-frontend.md` 36.6.

## 6a. Colour Palette (user colour choosers)

One palette in `_palette.scss`: `$o-palette` with 14 hues (red, orange, yellow, lime, green, teal, cyan, blue, violet, magenta, pink, brown, slate, gray) × 10 steps (50 to 900), plus the theme colours `theme1` … `themeN` from `$o-theme-colors` (`_palette_theme.scss`, default `$brand-primary`, Dart Sass >= 1.79). Emitted as `--o-{hue}-{step}` on `:root` in `theme.css` and `content.css`, not in `email.css`.

- Neutral scale (greys, no hue): `_palette_neutral.scss`, `$o-neutral`, 18 steps 0, 25, 50, 100, 150, 200, 250, 300, 400, 450, 500, 600, 700, 750, 800, 900, 950, 1000 (0 white, 1000 black), emitted as `--o-neutral-{step}`. Slate-tinted default; anchors 25/150/250/750/950 = light `bg-subtle`, `border`, `border-strong`, `text`, shadow `#101828`. `$o-neutral-hue` (default 260) and `$o-neutral-chroma` (default 0.038, 0 = pure grey) change the tint in OKLCH, the lightness of each step stays; defaults give the table values exactly. `o-neutral($step)` at compile time. Modules never read a step directly (a semantic or element token reads it). Java skips it (`Palette.NEUTRAL`), the choosers do not offer it.

- `$o-swatches`: per hue the roles `bg`, `fg`, `border`, `soft-bg`, `soft-fg`, `soft-border`, `text`, tint and dark variants. Foreground chosen by `o-on-color()` at compile time (4.5:1 enforced).
- `modules/_swatches.scss`: `.o_swatch_{hue}`, `.o_swatch_tint`, `.o_swatch_dark` set `--o-sw-*`. Chooser components (`_colors`, `_cal`, `_course`, `_video*`, `_projects`, `_user`, `_room_management`, `_contenteditor`) read `var(--o-sw-*)`. Legacy classes (`o_color_*`, `o_cal_*`, `o_colcat_*`, ...) extend the swatch classes via the alias map.
- Java: `Theme.init()` parses `--o-{hue}-{step}` from the selected theme's `theme.css` (fallback `light`) into `Palette`; `ColorService.getPalette()`, `resolve()`, `toCssClass()`. No generated file. Used by badges and report series. `getThemeColors()` returns only the theme colours the CSS defines; `resolve("theme3")` falls back to `theme1`.
- Reference: `doc/component-library/colors.html` (all swatches with live contrast values).

## 7. Key SCSS Modules

### Core
| Module | Purpose |
|--------|---------|
| `_bootstrap.scss` | Bootstrap 3.4.1 imports, hands the Sass variables to the import, no own rules (test `bootstrapHasNoOwnRules`) |
| `_tooltip`, `_modal`, `_progress`, `_panel`, `_form_bootstrap`, `_buttons`, `_navbar_bootstrap`, `_dropdown`, `_accessibility` | Own rules of the Bootstrap components, imported directly after `modules/bootstrap` (OO-10043); `.sr-only` in `_accessibility` (not in `content.css`) |
| `_mixins.scss` | `o-make-message-box`, `o-make-icon`, `o-add-icon`, `o-add-button-hover` |
| `_icons.scss` | Font Awesome 6 definitions, `o_icon` base |
| `_helpers.scss` | Spacing, flex, scrolling, buttons, panels, typography, DnD, skeleton |

### Layout
| Module | Purpose |
|--------|---------|
| `_layout.scss` | `html`/`body` base |
| `_main.scss` | CSS Grid 3-column layout |
| `_navbar.scss` | Top navigation bar |
| `_toolbar.scss` | Breadcrumb + tools bar |
| `_footer.scss` | Footer |
| `_offcanvas.scss` | Mobile right-side menu |

### Components
| Module | Purpose |
|--------|---------|
| `_form.scss` | Form layouts, radio cards, toggles, sliders |
| `_table.scss` | FlexiTable, filters, pagination, sticky columns |
| `_tree.scss` | Menu tree, indentation levels (0-11), DnD |
| `_dialog.scss` | Modal dialogs |
| `_wizard.scss` | Multi-step wizard |
| `_card.scss` | Card layouts, dashboard widgets, fact sheet (**New in 21.1.0**) |
| `_search.scss` | `SearchElement` styles (**New in 21.1.0:** rewritten) |
| `_bento.scss` | Bento grid |
| `_autocomplete.scss` | Auto-complete inputs |

### Feature Modules (selection)
| Module | Purpose |
|--------|---------|
| `_contenteditor.scss` | Page builder, drag-drop, inspector |
| `_course.scss` | Course nodes, learning path, assessment |
| `_qti21.scss` | Assessment interactions, review |
| `_cal.scss` | FullCalendar integration |
| `_forum.scss` | Forum messages, quotes |
| `_portfolio_v2.scss` | Portfolio entries, binders |
| `_repository.scss` | Course catalog |
| `_dmz.scss` | Login/registration page |
| `_room_management.scss` | Buildings, rooms, scheduling |
| `_ai_pulse.scss` / `_ai_feedback.scss` | AI activity indicator (`o_ai_pulse`), AI correction marks |
| `_selectus.scss` | Selectus module |

## 8. Key Mixins

```scss
@mixin o-make-message-box($color, $bg)      // Message box with icon + content
@mixin o-alert-variant($bg, $border, $text)  // Alert box styling
@mixin o-make-icon($width, $height, $lh)     // Icon base sizing
@mixin o-add-icon-font($icon, $weight)        // Font Awesome icon assignment
@mixin o-add-icon($selector, $icon, $weight)  // Icon by CSS class
@mixin o-add-button-icon-left($icon, $w)      // Button with left icon
@mixin o-add-button-hover($color)             // Button hover effect
@mixin o-make-glossy-bg($color)               // Gradient background
```

## 9. Responsive Design

Bootstrap 3 breakpoints:
| Breakpoint | Max Width |
|-----------|-----------|
| xs (phone) | < 768px |
| sm (tablet) | < 992px |
| md (desktop) | < 1200px |
| lg (large) | >= 1200px |

Responsive behaviors: navbar → hamburger, left column → offcanvas drawer, tables → horizontal scroll with shadow, toolbar text hides, forms go full-width, card column count reduces. `o_form_two_cols` is 2 columns only from 991px. **New in 21.1.0:** section sub-title rule moves under the title on xs; fact sheet columns via `auto-fill`; table quick search 170px below 540px.

AJAX form data: `o_ffXHREvent()` sends `{name, value}` pairs (all values of checkbox groups).

## 10. Key CSS IDs

| ID | Purpose |
|----|---------|
| `#o_body` | Root body, `.o_dmz` on login page |
| `#o_navbar_wrapper` | Top navigation |
| `#o_main_toolbar` | Toolbar (breadcrumb + tools), **sibling** of `#o_main` |
| `#o_main` | CSS Grid 3-column layout |
| `#o_main_left` | Left column (tree menu, resizable) |
| `#o_main_center` | Center column (content) |
| `#o_main_right` | Right column (sidebar, optional) |
| `#o_footer_wrapper` | Footer |

## 11. Accessibility (chapter 36 of the reference)

- Focus: **Changed in 21.2.0 (OO-9603):** redraw rule in `o_ainvoke()`: command 2 records the focused element (`o_redrawFocusAnchor()`), the end of the response restores it with `o_restoreFocus(o_resolveFocusTarget(anchor))` when the focus is on `body`. Order: command 11 (`o_ffSetFocus()`: first error field only with `validation: true`, set on `EVNT_VALIDATION_NOK` and after a validating `FormLink`; else `formItemId`; weak: error field, `[autofocus]`), dialog return, dialog open focus, redraw rule. Fallback chain: id, row key + slot, table, closest surviving ancestor, never `body`. `o_info.lastFormFocusEl` and `o_can_have_focus` are removed. Skipped while an open `<dialog>` has focus; `o_waitForVisibleThenFocusDialog()` for new dialogs (**New in 21.1.0:** skips clipped elements); `pushdialogfocus`/`popdialogfocus` focus return; skip link `a.sr-only[href="#o_main_container"]`.
- ARIA: `dialog.modal[aria-modal][aria-labelledby=o_md_*]`; dialog opener `role=button aria-haspopup=dialog aria-expanded`; disclosure `role=button aria-expanded aria-controls` + `collapse`; FormToggle `role=switch`; scopes `role=checkbox`; tabs/tree roles; search `role=search`/`searchbox`.
- Live regions: `role=status` (+ `aria-live=polite`) on AI overlays, `#o_dashboard_edit_live`; `role=alert` on the AI correction error. Gap: `#o_messages` info boxes are not announced.
- Keyboard: `triggerClick(event, enter, space)`; Escape → `o_doEscapeDispatch()` closes the top layer; dropdown: a key on the toggle focuses the first item (Arrow Up: last), Escape returns to the toggle; colour picker and icon selector entries handle Enter and Space on `keydown` with `preventDefault()` (no form submit).
- Theme: `o-a11y-color()`, `.sr-only` forced black on white, one focus ring for every control (D24, `modules/_focus.scss`, see the colour section), `prefers-reduced-motion` in 10 modules.
- Known gaps: object selection aria-label with raw HTML, `label for` mismatch; error fields without `aria-invalid` (`aria-describedby` only on text fields); an element without a stable id falls back to the closest surviving container.

## 12. Info Page Pattern (**New in 21.1.0**, chapter 35)

`.o_info_page > .o_info_page_column_aside (thumbnail, get_started, my_course, events) + .o_info_page_column_main (header, facts, sections, licence, rating)`, items `o_info_page_item`. Mobile: one column, wrappers `display: contents`, `order` 1-10. sm: `1fr auto`. md+: `2fr 1fr`. Print: one column + `.o_info_page_qrcode`.

## 13. Print Styles

`_print.scss` (`@media print`): hides navbar/footer/toolbar/tree, white backgrounds, avoids page breaks in tables/forms (`o_print_break_avoid/before/after`), print header via `#o_print_brand`. **New in 21.1.0:** info page print layout (`.o_info_page`, one column, QR code `.o_info_page_qrcode`).
