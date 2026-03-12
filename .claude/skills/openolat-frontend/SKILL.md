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

## Technology Stack

- **Bootstrap 3.4.1** (SASS) — grid, buttons, forms, navs, modals
- **Font Awesome 6** — icons (`o_icon o_icon_{name}`)
- **jQuery 3.x** — DOM manipulation, AJAX
- **Apache Velocity** — server-side `.html` templates in `_content/` dirs
- **~540 Java ComponentRenderers** — programmatic HTML generation
- **~100 SASS modules** — OpenOlat-specific styling in `themes/light/modules/`
- **Dart Sass >= 1.33** — compilation via `compiletheme.sh`
- **No client-side framework** — all UI state lives on the server

## Key Files

| File | Purpose |
|------|---------|
| `src/main/webapp/static/themes/light/_config.scss` | ALL variables (~1500 lines) |
| `src/main/webapp/static/themes/light/_functions.scss` | a11y functions: `luminance()`, `contrast()`, `o-a11y-color()` |
| `src/main/webapp/static/themes/light/_modules.scss` | Import manifest for ~100 modules |
| `src/main/webapp/static/themes/light/modules/` | SCSS partials by feature |
| `src/main/webapp/static/themes/openolat/` | Default product theme (example of customization) |
| `src/main/webapp/static/themes/compiletheme.sh` | SASS compilation script |
| `src/main/webapp/static/themes/themes.README` | Theme creation guide |

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

Three CSS outputs: `theme.css` (main app), `content.css` (iFrames), `email.css` (notifications).

## Variable Naming

- `$o-` prefix = OpenOlat-specific (e.g., `$o-navbar-height`, `$o-tree-link-color`)
- No prefix = Bootstrap standard (e.g., `$brand-primary`, `$font-size-base`)

## Page DOM Skeleton

```html
<body id="o_body" class="[o_dmz]">
  #o_navbar_wrapper > #o_navbar_container > .o_navbar
    .o_navbar-brand | .o_navbar_tabs | .o_navbar_tools
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
| `o_sel_` | Test selectors | `o_sel_course_list` |
| `fa-` | Font Awesome | `fa-check`, `fa-times` |

## Common Utility Classes

**Spacing:** `o_block`, `o_block_small`, `o_block_large`, `o_block_top`, `o_block_bottom`
**Flex:** `o_flex_block_one_line_left`, `o_flex_item_fix`, `o_flex_item_max`
**Typography:** `o_xsmall`, `o_small`, `o_large`, `o_disabled`, `o_dimmed`, `o_muted`, `o_nowrap`
**Buttons:** `o_button_ghost`, `o_button_mega`, `o_button_primary_light`, `o_button_call_to_action`
**Messages:** `o_info`, `o_note`, `o_tip`, `o_important`, `o_success`, `o_warning`, `o_error`
**Scrolling:** `o_scrollblock`, `o_scrollable_wrapper`, `o_scrollable`

## Labeled Color System

8 colors (blue, green, yellow, orange, red, brown, grey) x 3 variants (solid, light/outline, mega/tinted).
Variables: `$o-labeled-{color}-{variant}-{property}` where variant = (none)/light/mega, property = color/bg-color/border-color.

## Component DOM Patterns

**FlexiTable:** Full structure (each part optional):
`.o_table_tabs` → `.o_table_filters_wrapper` → `.o_table_toolbar` (search + tools) → `.o_table_batch_buttons` → `.o_table_wrapper.o_table_flexi` (CLASSIC: `.o_scrollable_wrapper > table.table`; CUSTOM: `.o_table_body > .o_table_row`) → `.o_table_pagination`.
Toggle: `.o_sel_table` / `.o_sel_custom` buttons.

**Tree Menu:** `.o_tree > ul > li > div` with level classes `.o_tree_l{0-11}` for indentation

**Toolbar:** `.o_toolbar > .o_breadcrumb + .o_tools_container > .o_tools.o_tools_{left|center|right|right_edge}`

**Modal:** `.o_modal_wrapper > dialog.modal-dialog > .modal-content > .modal-header + .modal-body + .modal-footer`

**Tabs:** `.o_tabbed_pane > ul.nav.nav-tabs + .o_tabbed_pane_content`

**Form Elements:** All wrapped in `.form-group` with Bootstrap `.form-control` inputs. Radio cards use `.o_radio_cards`. Toggles use `.o_toggle`.

## AJAX Rendering

Components render with `id="o_c{dispatchID}"`. On user action, server re-renders dirty components, responds with JSON `[{action:"updcmp", id:"o_c12345", content:"<html>"}]`, jQuery replaces DOM. Background polling every 5s (with decay) picks up cross-session updates.

## Custom Theme Checklist

1. Copy `openolat/` → `mytheme/`, rename files
2. Override variables in config (key: `$brand-primary`, `$text-color`, `$link-color`, `$font-family-base`, `$o-navbar-*`, `$o-footer-*`)
3. Override CSS rules for logo (`.o_navbar-brand`), login page (`#o_body.o_dmz`), footer
4. Compile: `./compiletheme.sh mytheme`

## Accessibility

- `o-a11y-color($color, $bg)` auto-adjusts for WCAG 4.5:1 contrast
- Used extensively in `_config.scss` for button text, toolbar links, etc.
- Bootstrap 3 ARIA attributes on tabs, modals, forms
- `$r.screenreaderOnly("text")` → `<span class="sr-only">` in templates

## Responsive Design

Bootstrap 3 breakpoints: xs (<768px), sm (<992px), md (<1200px), lg (>=1200px).
Navbar → hamburger, left column → offcanvas, tables → horizontal scroll, toolbar text hides on xs.
