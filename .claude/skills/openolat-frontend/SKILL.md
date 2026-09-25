---
name: openolat-frontend
description: Use this skill when working with OpenOlat CSS/SASS themes, styling components, creating custom themes, debugging DOM/layout issues, or understanding the frontend rendering pipeline. Provides theme architecture, CSS class reference, DOM structure, and component renderer knowledge.
allowed-tools: Read, Grep, Glob, Bash(sass *), Bash(./compiletheme.sh *)
---

# OpenOlat Frontend & Theming Assistant

You are an expert in OpenOlat's frontend architecture: SASS theming, CSS class conventions, DOM structure, component rendering, and responsive design. Use the knowledge below and the reference files to help developers style components, create themes, debug layout issues, and understand the rendering pipeline.

For compressed frontend knowledge, read `.claude/openolat-frontend-knowledge.md`

For detailed frontend documentation, read: `doc/openolat-frontend.md`

For visual documentation with SVG diagrams, open: `doc/openolat-frontend.html`

For live rendered component examples with theme switching, open: `doc/component-library/index.html` (pages: buttons, forms, tables, navigation, feedback, layout, data display, AI UI, room management; all examples use real markup read from the renderers or the localhost DOM)

`doc/openolat-frontend.md` and `doc/openolat-frontend.html` have the same 37 chapters and numbering. Chapter 35 is the info page pattern, chapter 36 the accessibility chapter.

State of this skill: release 21.1.0 (2026-09-25). `**New in 21.1.0:**` marks changes of 21.1.0. For new components check the GUI demo site (navbar More > gui_demo, needs `guidemo.enabled=true`) and read the rendered DOM there.

## Technology Stack

- **Bootstrap 3.4.1** (SASS) — grid, buttons, forms, navs, modals
- **Font Awesome 6.7.2**: icons (`o_icon o_icon_{name}`)
- **jQuery 3.7.1**: DOM manipulation, AJAX
- **Vanilla JS Datepicker 1.3.4**, **typeahead.js**, **D3.js 7.9.0** (charts), **TinyMCE 6.8.6**, **FullCalendar 6.1.15**, **Leaflet 1.9.4**
- **Apache Velocity** — server-side `.html` templates in `_content/` dirs
- **~540 Java ComponentRenderers** — programmatic HTML generation
- **~100 SASS modules** — OpenOlat-specific styling in `themes/light/modules/`
- **Dart Sass >= 1.33** — compilation via `compiletheme.sh`
- **No client-side framework** — all UI state lives on the server

## Key Files

| File | Purpose |
|------|---------|
| `src/main/webapp/static/themes/light/_config.scss` | ALL variables (~2000 lines) |
| `src/main/webapp/static/themes/light/_functions.scss` | a11y functions: `luminance()`, `contrast()`, `o-a11y-color()` |
| `src/main/webapp/static/themes/light/_modules.scss` | Import manifest for ~100 modules |
| `src/main/webapp/static/themes/light/modules/` | SCSS partials by feature |
| `src/main/webapp/static/themes/openolat/` | Default product theme (example of customization) |
| `src/main/webapp/static/themes/compiletheme.sh` | SASS compilation script |
| `src/main/webapp/static/themes/themes.README` | Theme creation guide |
| `src/main/webapp/static/themes/light/oo-docs.scss` | Icons-only bundle `oo-docs.css` for the user manual |
| `src/main/webapp/static/js/functions.js` | AJAX pipeline, focus handling, section toggle handler |

## Theme Inheritance

```
Custom theme config (pre-import, no !default)
  → light/theme.scss
    → _functions.scss (a11y color utilities)
    → _config.scss (1500+ variables with !default — custom values win)
    → _modules.scss (100 module files including Bootstrap)
    → _patches.scss
  → Custom theme CSS overrides (post-import)
```

Three CSS outputs: `theme.css` (main app), `content.css` (iFrames), `email.css` (notifications). `light` also builds `oo-docs.css` (icons for the manual).

## Variable Naming

- `$o-` prefix = OpenOlat-specific (e.g., `$o-navbar-height`, `$o-tree-link-color`)
- No prefix = Bootstrap standard (e.g., `$brand-primary`, `$font-size-base`)

## Page DOM Skeleton

```html
<body id="o_body" class="[o_dmz]">
  #o_navbar_wrapper > #o_navbar_container > .o_navbar
    .o_navbar-brand | .o_navbar_tabs | ul#o_navbar_tools_permanent (#o_navbar_search_opener, #o_navbar_my_menu)
  #o_main_wrapper > #o_main_container
    TooledStackedPanel (.o_with_toolbar .o_with_breadcrumb)
      #o_main_toolbar .o_toolbar       ← breadcrumb + tools (SIBLING of #o_main)
      #o_main (CSS Grid: auto | 1fr | auto)
        #o_main_left (tree) | #o_main_center (content) | #o_main_right (sidebar)
  #o_footer_wrapper > #o_footer_container
```

**Important:** `#o_main_toolbar` is a **sibling** of `#o_main`, not a child. Both live inside the TooledStackedPanel. Some pages (catalog, coaching) have no `#o_main` at all.

Column visibility via: `.o_hide_main_left`, `.o_hide_main_right` on `#o_main`.

## CSS Class Conventions

| Prefix | Origin | Example |
|--------|--------|---------|
| (none) | Bootstrap 3 | `btn`, `form-control`, `table`, `nav-tabs` |
| `o_` | OpenOlat core | `o_toolbar`, `o_tree`, `o_table_wrapper` |
| `o_icon_` | Icons | `o_icon_delete`, `o_icon_edit` |
| `o_sel_` | Test selectors | `o_sel_course_add_member` |
| `fa-` | Font Awesome | `fa-check`, `fa-times` |

## Common Utility Classes

**Spacing:** `o_block`, `o_block_small`, `o_block_large`, `o_block_top`, `o_block_bottom`
**Flex:** `o_flex_block_one_line_left`, `o_flex_item_fix`, `o_flex_item_max`
**Typography:** `o_xsmall`, `o_small`, `o_large`, `o_disabled`, `o_dimmed`, `o_muted`, `o_nowrap`
**Buttons:** `o_button_ghost` (+ `btn-danger`, **New in 21.1.0**), `o_button_mega`, `o_button_primary_light`, `o_button_call_to_action`, `o_button_group.compact` (**New in 21.1.0**)
**Messages:** `o_info`, `o_note`, `o_tip`, `o_important`, `o_success`, `o_warning`, `o_error`
**Scrolling:** `o_scrollblock`, `o_scrollable_wrapper`, `o_scrollable`

## Labeled Color System

8 colors (blue, green, yellow, orange, red, brown, grey) x 3 variants (solid, light/outline, mega/tinted).
Variables: `$o-labeled-{color}-{variant}-{property}` where variant = (none)/light/mega, property = color/bg-color/border-color.

## Component DOM Patterns

**FlexiTable:** Full structure (each part optional):
`.o_table_tabs` → `.o_table_filters_wrapper.o_expanded > .o_table_filters_row > ul.nav.nav-pills.o_table_filters > li > a.btn.btn-default.o_table_filter[.o_filter_active]` → `.o_table_toolbar` (search + tools) → `.o_table_batch_buttons` → `.o_table_wrapper.o_table_flexi` (CLASSIC: `.o_scrollable_wrapper > .o_scrollable > table.table`; CUSTOM: `.o_table_body > .o_table_row`) → `.o_table_pagination`.
Filter buttons: active state `o_filter_active` goes on the `<a>`, NOT the `<li>`. Caret: `<i class="o_icon o_icon-fw o_icon_caret">`. Toggle: `.o_sel_table` / `.o_sel_custom`.

**Tree Menu:** `.o_tree > ul[role=tree] > li[role=treeitem] > div.o_tree_l{n}` > `a.o_tree_oc_l{n}` (opener) + `span.o_tree_link > a > span.o_tree_item`; levels `o_tree_l{0-11}`

**Toolbar:** `.o_toolbar > .o_breadcrumb + .o_tools_container > .o_tools.o_tools_{left|center|right|right_edge}`; segments `ul.o_tools_segments > li > div.o_segments.btn-group` (selected `a.btn-primary`)

**Modal:** `div.o_layered_panel.o_layer_{n} > dialog.dialog.modal.show[aria-modal] > .modal-dialog > .modal-content > .modal-header + .modal-body + .modal-footer`

**Tabs:** `.o_tabbed_pane > ul.nav.nav-tabs + .o_tabbed_pane_content`

**Form Elements:** All wrapped in `.form-group` with Bootstrap `.form-control` inputs. Radio cards use `.o_radio_cards`. Toggles are `button.o_button_toggle[role=switch]` (`o_toggle_on`). Form errors: `<div class="o_error">` (block div, NOT span) below the input within `.form-group`. Object selection: `button.o_expand_button.o_selection_display[aria-haspopup=dialog]`, disabled = `disabled o_disabled`.

**New in 21.1.0, form section:** `FormSection` → `fieldset.o_form_section` with `div.o_section_sub_title` (level SUB_TITLE) or `legend > h4.o_section_title`; collapsible adds `.o_section_toggle[role=button][aria-expanded]` + `.o_section_content.collapse`. Blueprint for settings pages: GUI demo "Example settings". See `doc/openolat-frontend.md` 13.1.

**New in 21.1.0, search:** `SearchElement` → `div.o_search.o_search_{default|large|typeahead}[role=search]` with `input.o_search_input[role=searchbox]`, `a.o_search_reset`, `a.o_search_button`. Used in tables (`.o_table_search`), navbar (`#o_navbar_search`), overviews and selections. See 13.2.

**New in 21.1.0, label icon:** `FormItem.setLabelIconCss(css)` → icon before the label text (`aria-hidden`), e.g. `o_icon_locked` for read-only fields.

**Mega Buttons:** `ul.o_mega_buttons` (CSS Grid, auto-fill) > `li` > `button.btn.o_button_mega` (55px, flex row) > `i.o_icon` + `span`. Used in course element selection.

**Widgets:** `.o_widgets` (CSS Grid, auto-fill minmax 260px) > `.o_widget` (card with `.o_widget_header` + `.o_widget_content` + optional `.o_widget_additional`). Content types: `.o_widget_main_figure`, `.o_widget_main_text`, component (`.o_widget_main` + `.o_widget_link`).

**New in 21.1.0, fact sheet:** `.o_fact_sheet > h3.o_fact_sheet_title + .o_facts > .o_fact > .o_fact_icon + .o_fact_body (.o_fact_label, .o_fact_value, .o_fact_sub_value)`; grid columns min 320px; variables `$o-fact-*`. `ComponentList` → `.o_component_list`. `Sections` → `.o_sections > .o_section` (collapsible, same header as FormSection). See 24.3 and 24.4.

**New in 21.1.0, tables:** `TranslateCellRenderer` → `o_icon_language` link before the term.

**New in 21.1.0, info page:** `.o_info_page` with `o_info_page_column_aside` / `o_info_page_column_main` and `o_info_page_item` + item class; one column below `$screen-sm-min` sorted by `order` (column wrappers `display: contents`), `2fr 1fr` from `$screen-md-min`, own print layout. See chapter 35.

**AI UI (21.0):** pulse icon `svg.o_ai_pulse` (template `ai_pulse_icon.html`, sizes `o_ai_pulse_xs..xl`, state `o_ai_pulse_active`), essay marks `o_ai_mark_correct/ambiguous/wrong`, status boxes with `role="status"`. **Room management (21.0):** `o_room_card`, `o_rm_details`, `o_building_color_ref`, `o_rm_status_*`. Library pages `ai-ui.html`, `room-management.html`.

**Scopes:** `button.o_toggle_button.btn.o_scope_toggle[role=checkbox][aria-checked]` with `o_toggle_on/off`, `div.o_scope > .o_scope_title + .o_scope_hint`.

## AJAX Rendering

Components render with `id="o_c{dispatchID}"`. On user action, server re-renders dirty components, responds with JSON `[{action:"updcmp", id:"o_c12345", content:"<html>"}]`, jQuery replaces DOM. Background polling every 5s (with decay) picks up cross-session updates. Form events send `{name, value}` pairs (`o_ffXHREvent`), so checkbox groups keep all values.

Collapsible sections need no per-instance script: one delegated handler in `functions.js` on `.o_section_toggle`.

## Custom Theme Checklist

1. Copy `openolat/` → `mytheme/`, rename files
2. Override variables in config (key: `$brand-primary`, `$text-color`, `$link-color`, `$font-family-base`, `$o-navbar-*`, `$o-footer-*`)
3. Override CSS rules for logo (`.o_navbar-brand`), login page (`#o_body.o_dmz`), footer
4. Compile: `./compiletheme.sh mytheme`

## Accessibility

- Full chapter: `doc/openolat-frontend.md` 36 (focus management, ARIA patterns, live regions, keyboard patterns, JS helpers, theme rules, known gaps)
- `o-a11y-color($color, $bg)` auto-adjusts for WCAG 4.5:1 contrast
- Used extensively in `_config.scss` for button text, toolbar links, etc.
- Bootstrap 3 ARIA attributes on tabs, modals, forms
- `$r.screenreaderOnly("text")` → `<span class="sr-only">` in templates
- Focus: `o_ffSetFocus()` only focuses inputs, selects, textareas and buttons or `.btn` links with `o_can_have_focus`; nothing happens while an open `<dialog>` has the focus
- Live regions: use `role="status"` / `aria-live="polite"` for async state (AI overlays, `#o_dashboard_edit_live`); `#o_messages` info boxes are NOT announced
- Keyboard: `triggerClick(event, enter, space)` on link buttons; Escape closes the top layer via `o_doEscapeDispatch()`
- Callout triggers: `Link/FormLink.setAriaDialogOpener()` → `role="button" aria-haspopup="dialog" aria-expanded`; `CloseableCalloutWindowController` toggles `aria-expanded` and returns the focus to the trigger (`pushdialogfocus`/`popdialogfocus`). Pass the trigger component, not its DOM id. **New in 21.1.0:** used on action columns, table tools and filters; the focus goes to the first visible element of the callout.

## Responsive Design

Bootstrap 3 breakpoints: xs (<768px), sm (<992px), md (<1200px), lg (>=1200px).
Navbar → hamburger, left column → offcanvas, tables → horizontal scroll, toolbar text hides on xs. `o_form_two_cols` is 2 columns only from 991px.
