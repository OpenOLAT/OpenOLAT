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
- **Dart Sass >= 1.33**: compilation via `compiletheme.sh`
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
      _functions.scss           # luminance(), contrast(), o-a11y-color()
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
| Color system | 82-142 | `$o-labeled-{color}-{variant}-{property}` |
| Brand colors | 150-175 | `$o-color-info`, `$o-color-warning` |
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

## 6. Labeled Color System

8 colors x 3 variants: solid (default), light/outline, mega/tinted.

| Color | Solid bg | Light | Mega bg |
|-------|----------|-------|---------|
| Blue | `#146DCC` | blue border, white bg | `#cde3f9` |
| Green | `#37AD00` | green border, white bg | `#C4E6B8` |
| Yellow | `#FBD774` | yellow border, white bg | `#fbe6a7` |
| Orange | `#F4AC47` | orange border, white bg | `#FFDAA6` |
| Red | `#b30018` | red border, white bg | `#ffc9bd` |
| Brown | `#804A33` | brown border, white bg | `#E7D2BC` |
| Grey | `#595959` | grey border, white bg | `#F6F6F6` |

Variables: `$o-labeled-{color}-{variant}-{property}` (color/bg-color/border-color)

## 7. Key SCSS Modules

### Core
| Module | Purpose |
|--------|---------|
| `_bootstrap.scss` | Bootstrap 3.4.1 imports + overrides |
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

- Focus: `o_ffSetFocus()` / `o_ffSetFocusArray()` after AJAX (only inputs, selects, textareas, buttons and `.btn` links with `o_can_have_focus`; skipped while an open `<dialog>` has focus); `o_waitForVisibleThenFocusDialog()` for new dialogs (**New in 21.1.0:** skips clipped elements); `pushdialogfocus`/`popdialogfocus` focus return; skip link `a.sr-only[href="#o_main_container"]`.
- ARIA: `dialog.modal[aria-modal][aria-labelledby=o_md_*]`; dialog opener `role=button aria-haspopup=dialog aria-expanded`; disclosure `role=button aria-expanded aria-controls` + `collapse`; FormToggle `role=switch`; scopes `role=checkbox`; tabs/tree roles; search `role=search`/`searchbox`.
- Live regions: `role=status` (+ `aria-live=polite`) on AI overlays, `#o_dashboard_edit_live`; `role=alert` on the AI correction error. Gap: `#o_messages` info boxes are not announced.
- Keyboard: `triggerClick(event, enter, space)`; Escape → `o_doEscapeDispatch()` closes the top layer.
- Theme: `o-a11y-color()`, `.sr-only` forced black on white, `.dropdown-toggle:focus-visible` (tab-focus), `prefers-reduced-motion` in 10 modules.
- Known gaps: object selection aria-label with raw HTML, `label for` mismatch, focus lost after callout close; colour picker `aria-expanded='true'` when closed.

## 12. Info Page Pattern (**New in 21.1.0**, chapter 35)

`.o_info_page > .o_info_page_column_aside (thumbnail, get_started, my_course, events) + .o_info_page_column_main (header, facts, sections, licence, rating)`, items `o_info_page_item`. Mobile: one column, wrappers `display: contents`, `order` 1-10. sm: `1fr auto`. md+: `2fr 1fr`. Print: one column + `.o_info_page_qrcode`.

## 13. Print Styles

`_print.scss` (`@media print`): hides navbar/footer/toolbar/tree, white backgrounds, avoids page breaks in tables/forms (`o_print_break_avoid/before/after`), print header via `#o_print_brand`. **New in 21.1.0:** info page print layout (`.o_info_page`, one column, QR code `.o_info_page_qrcode`).
