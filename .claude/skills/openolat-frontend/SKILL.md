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
- **Dart Sass >= 1.79.0** — compilation via `compiletheme.sh`
- **No client-side framework** — all UI state lives on the server

## Key Files

| File | Purpose |
|------|---------|
| `src/main/webapp/static/themes/light/_config.scss` | ALL variables (~2000 lines) |
| `src/main/webapp/static/themes/light/_functions.scss` | a11y functions: `luminance()`, `contrast()`, `o-a11y-color()`, `o-on-color()` |
| `src/main/webapp/static/themes/light/_palette.scss` | Colour palette: `$o-palette` (14 hues × 10 steps), `$o-swatches` roles, legacy key aliases; theme colours in `_palette_theme.scss` |
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

## Status Colours and Token Layers

Status colours: 8 semantic states in `$o-states` (`_palette.scss`): neutral blue, success green, warning yellow, critical orange, danger red, finished brown, inactive slate, highlight magenta. 5 styles: solid (default), dark (counters, deleted), tint (draft, preparation), light (not seen), text (outline, `o_labeled_light`). All foregrounds reach 4.5:1, checked at compile time.

- New status: `<span class="o_labeled o_state_success">`, modifiers `o_state_dark` / `o_state_tint` / `o_state_light`, outline `o_labeled_light o_state_<state>`. Or add the status class to `$o-state-wiring` in `modules/_states.scss` (395 classes, generated, `@extend %o_state_<state>_<style>`, imported last). Never set a colour on a status class in a module.
- Status colour without a label (icon, marker, bar, chart): `o-state-color($state, $role)`, roles `text` (text, icons), `bg` / `soft-bg` (fills), `border` (borders, markers, chart lines), `fg` / `soft-fg` / `soft-border`. Not `$brand-success|danger|warning|info` or `$state-*-text`.
- Fixed colour without a meaning (stars, pen colours): `o-palette($hue, $step)`. Swatch role of a hue: `o-swatch-color($hue, $role)`.
- No new Sass colour variable, no `darken()`/`lighten()`/`rgba($var)` on palette or state colours.
- Token layers (`modules/_tokens.scss`, OO-9994, OO-10043, not in `email.css`): **base** = palette (literal colours: `--o-{hue}-{step}`, `--o-theme{n}-{step}`, the neutral scale `--o-neutral-{step}`, `--o-white`, base colours `--o-base-*` from `$text-color`, `$brand-primary`, `$link-color`, ...), the 22 semantic tokens `--o-text`, `--o-text-muted`, `--o-text-disabled`, `--o-heading`, `--o-bg`, `--o-bg-subtle`, `--o-bg-hover`, `--o-bg-selected`, `--o-bg-raised`, `--o-border`, `--o-border-subtle`, `--o-border-strong`, `--o-primary`, `--o-primary-fg`, `--o-primary-hover`, `--o-primary-text`, `--o-link`, `--o-link-hover`, `--o-focus`, `--o-shadow-color`, `--o-backdrop`, `--o-scrim` (the last 3 are opaque: the rule sets the transparency with `color-mix(in srgb, var(--o-backdrop) 50%, transparent)`), the 56 state tokens `--o-state-{state}-{role}`, and the global elements (buttons, content text and headings: `$o-token-layer-base`, 23); **framework** = layout and generic UI of `org.olat.core.gui` and `org.olat.core.commons.services` (navbar, footer, login, dialogs, tree, toolbar, scope toggle, table, wizard, form elements, tags, date picker, ...: `$o-token-layer-framework`, 127); **component** = elements of one feature module (course, course site, QTI tests and question pool, practice, forum, chat, lectures, topic broker, folder, selectus, immunity proof, ...: `$o-token-layer-component`, 115).
- Element token = a token of the three layer maps, 277 in total, `--o-{element}-{part}`. Default `($sass-variable, (tokens))` (134): the Sass variable of the theme config feeds the token (client themes keep setting it), the token reads the first listed token whose default has the same colour (follows its override), else the palette step, else `--o-base-{name}`. The default can be a Sass expression for a text colour that `o-a11y-color()` adjusts for the background of one rule, e.g. `"link-subtle": (o-a11y-color($link-color, $panel-default-heading-bg), ("link",))`; the breadcrumb, table, course element and forum link tokens reference it. No module calls `o-a11y-color()` (test `rulesReadAdjustedTextTokens`): a new adjusted text colour is a new token. Default `"token-name"` (143): reference to another token of any layer, e.g. `"selectus-review-glow": "primary"` emits `var(--o-primary)`, `"qti-menu-link": "tree-link"`; no Sass variable, set with `$o-tokens`; loop or unknown name stops the compile. Literal colours only in the palette and in the shade tokens, every other token is a `var()` reference. A module reads the element token of its element (`var(--o-selectus-review-glow)`, not `var(--o-primary)`), else an exact token; `_mixins.scss` stays Sass (email).
- Shade token = the darker or lighter colour of an element token or a semantic token, computed at compile time (`$o-token-shades` in `modules/_tokens.scss`, 63 tokens): `"name": ("token", percent)` darkens, `("token", percent, "lighten")` lightens. Buttons (24): `--o-{element}-bg-hover` (10%), `-bg-active` (17%), `-border-hover` (12%), `-border-focus` (25%) for `btn-primary`, `btn-default`, `btn-danger`, `btn-dirty`, `coursesite-start`, `coursesite-book`. Module colour maths (39, the percentage of the module, so a client Sass variable gives the same colour): tags (`tag-bg-hover`, `tag-selection-selected-border-active`, ...), `chelp-bg-hover`, `chelp-border-hover`, `content-h5-hover`, `content-dimmed-hover`, `coursesite-title-hover`, `focus-edge` (from the semantic token `focus`), `file-upload-btn-bg-hover` (from `btn-default-bg`), `immunity-proof-{validated|claimed|none}-bg-tint`, `-fg`, `-bg-edge`, `toolbar-close-hover`, `addremove-{add|remove}-bg-hover`, `tag-selected-border-hover|active`, `tag-selection-remove-selected-{bg|border}-hover|active`, `competence-selected-border-hover|active`, `tinymce-danger-bg-edge`. Suffixes: `-hover`, `-active`, `-focus` (state), `-edge` (darker edge: border, outline, text stroke), `-tint` (lighter surface), `-stop-{n}` (gradient stop); a shade for an element without an own token takes the element and role name. It follows its token (Sass variable or `$o-tokens`), `$o-tokens` overrides it like an element token; a run time redefinition (dark mode) redefines the shade tokens too. No `darken()` or `lighten()` of a token colour in a module: add or read a shade token.
- Translucent colour of a token = no token: `color-mix(in srgb, var(--o-focus) 15%, transparent)` for `rgba($input-border-focus, 0.15)` (same alpha, decision M3). Black shadows read `color-mix(in srgb, var(--o-shadow-color) 20%, transparent)`, the dark modal layer `--o-backdrop`, light veils `--o-scrim`, each with the alpha of the old `rgba()`; shadows and veils with another colour (`rgba(16, 24, 40, a)`) keep their `rgba()` (M8). The state label hovers of `_states.scss` keep `color-mix(in srgb, var(--o-sw-bg), #000 10%)` (M2). Muted text: `var(--o-text-muted)`, disabled text `var(--o-text-disabled)`, never `lighten($text-color, ...)`.
- Bootstrap colour variables that a module reads are framework tokens with the exact value (OO-10043 step 4, decision M7 a): `input-bg`, `input-fg`, `input-disabled-bg`, `input-placeholder`, `table-border`, `table-striped-bg`, `table-hover-bg`, `dropdown-link`, `dropdown-link-hover`, `dropdown-link-hover-bg`, `dropdown-divider`, `popover-bg`, `popover-border`, `modal-content-border`, `modal-header-border`, `code-block-bg|fg|border` (`$pre-*`), `legend`, `legend-border`, `panel-bg`, `progress-bar-fg`, `thumbnail-border`, `blockquote-border`. A module reads `var(--o-input-bg)`, not `$input-bg`; a client theme keeps setting the Bootstrap variable. The Bootstrap greys (`$gray`, `$gray-light`, `$gray-dark`, `$gray-lighter`) have element tokens of their element (`alert-close`, `chart-help-line`, `qti-help`, ...); a light border reads `--o-border-subtle`. White: `--o-white` for text on a fill, `--o-bg` for a surface, `--o-bg-raised` for a popover, dialog, lightbox or floating tool bar. A hex grey within 3 of 255 of a token reads the token (`--o-bg-subtle`, a neutral step); other hex values stay until their module group slice (decision B4 a).
- Buttons: `@include button-variant(var(--o-btn-primary-fg), var(--o-btn-primary-bg), var(--o-btn-primary-border));` (`modules/_button_variant.scss`, replaces the Bootstrap mixin). A token argument reads the shade tokens for hover, focus and active, a token without shade tokens stops the compile; a colour argument keeps the Bootstrap `darken()` states. `_bootstrap.scss` and `content.scss` import the mixin. Contrast pairs: the `-fg` token on `-bg`, `-bg-hover` and `-bg-active`. `.btn-default`, `.btn-primary`, `.btn-danger` (`btn-danger-bg|fg|border`, defaults `state-danger-bg|fg|border`, `_bootstrap.scss` hands them to the Bootstrap import), the dirty button and the course site start and book buttons read tokens; `.btn-success|info|warning` read Sass variables (no use in OpenOlat).
- Links: four families. `--o-link`/`--o-link-hover` (`$link-color`): the Bootstrap rules `a`, `a:hover|focus`, `.btn-link`, pagination hover read them (`_bootstrap.scss` hands `var(--o-link)` to the Bootstrap import and restores the Sass values after it), plus element tokens with the reference `link` (`segments-tab-link` = filter tab row above tables, `selectus-review-comment-link`, `toolbar-dropdown-active`, `im-buddies-link`). `--o-link-subtle` (`$link-color` adjusted to 4.5:1 on `$panel-default-heading-bg`): referenced by `toolbar-breadcrumb-link`, `table-link`, `table-striped-link`, `course-style-link`, `forum-message-header-author`; `$o-table-link-color`, `$o-toolbar-breadcrumb-link-color`, `$o-forum-message-header-author-color`, `$o-forum-message-footer-attachment-color` are removed. `--o-content-link` (`$o-content-link-color`): content links. `--o-nav-link`/`--o-nav-link-hover` (`$navbar-default-link-color`): tree and toolbar links (`tree-link`, `toolbar-link` follow it when their Sass variable has the same colour; `toolbar-nav-link` references `toolbar-link`). Tree, navbar, footer and login help keep own tokens. `.o_segments_linked` reads `link`. `$o-content-link-hover-color` and `$o-footer-default-link-hover-color` stay Sass (client values a token cannot carry).
- New customization point: add an element token with a reference default to the map of its layer and read it with `var()`; never a new Sass colour variable.
- New code reads `var(--o-*)`: `color: var(--o-text-muted)`, `border-left: 4px solid var(--o-state-success-border)`. No hex, no `$text-color`/`$gray-*`/`$brand-*`, no new Sass colour variable. `ThemeColorRatchetTest` fails when a module gets more old colour references than `theme_color_baseline.properties`.
- Client override: `$o-tokens: ("text-muted": "slate-600", "state-success-bg": "green-600", "state-success-fg": "white", "navbar-bg": "primary", "navbar-text": "primary-fg", "navbar-link": "primary-fg", "navbar-tools": "primary-fg", "qti-menu-link": "link", "selectus-review-glow": "orange-500");` in the theme config; key = any semantic, state or element token, value = any token name (`var()` reference, chains allowed, a loop stops the compile), `"{hue}-{step}"`, `"neutral-{step}"`, `"white"` or a colour. Compile stops for unknown token/step and broken contrast pairs (element pairs too). An override does not reach rules that still read the Sass variable.
- Contrast check (palette, theme colours, `$o-tokens`): `$o-contrast-check: "error"` (default, compile stops) or `"warn"` (reports the pair, accepts it). Only a client theme whose client insists on the colours sets `"warn"` in its config; `light` and `openolat` never.
- Brand colours to states (OO-10043 step 5): module rules read the state tokens, never `$brand-success|info|warning|danger`, `$state-*-text|bg|border`, `$alert-*`, `$btn-success|info|warning|danger-*` (test `stateRulesReadTokens`). Mapping: `$brand-{state}` = role `bg`, icons and texts = role `text`, `$state-*-text` = `text`, `$state-*-bg`/`$alert-*-bg` = `soft-bg`, `$state-*-border` = `soft-border`; info = state `neutral`. A `_config.scss` variable with a state default uses `o-state-color(state, role)` and stays the customisation point. Dirty and book button: warning fill, dark `fg` text (`#321E00`); an own theme fill keeps the adjusted white. The Bootstrap rules `.btn-success|info|warning`, alerts, labels, progress bars and the message boxes and form messages (`$o-box-*`, `$o-color-*`) still read `$brand-*` (later step).

Details: `doc/openolat-frontend.md` chapter 26.

## Colour Palette (user colour choosers)

All user-chosen colours (calendar, course element category, video markers, project avatar, user initials, rooms, page editor blocks, badges, report series) come from one palette in `_palette.scss`: 14 hues (red, orange, yellow, lime, green, teal, cyan, blue, violet, magenta, pink, brown, slate, gray) × 10 steps (50 to 900), plus the theme colours `theme1` … `themeN` (brand colours of the theme, `$o-theme-colors` in `_palette_theme.scss`, default `$brand-primary`). The theme emits every value as `--o-{hue}-{step}` on `:root` in `theme.css` and `content.css`. The greys are not a hue: the neutral scale `--o-neutral-{step}` (`_palette_neutral.scss`, 18 steps 0, 25, 50, 100, 150, 200, 250, 300, 400, 450, 500, 600, 700, 750, 800, 900, 950, 1000; 0 white, 1000 black; slate-tinted, `$o-neutral-hue` default 260 and `$o-neutral-chroma` default 0.038 change the tint, the lightness stays). Modules never read a step directly, a semantic or element token reads it; the choosers and Java (`Palette.NEUTRAL`) skip it.

- **Swatches:** `$o-swatches` gives each hue the roles `bg`, `fg`, `border`, `soft-bg`, `soft-fg`, `text`, plus tint and dark variants. `o-on-color()` picks the foreground and fails the compile below 4.5:1. The class `.o_swatch_{hue}` sets `--o-sw-*`; components read `var(--o-sw-bg)` etc. Never add a hex literal for a chooser colour.
- **Legacy keys:** stored values (`o_color_lightblue`, `o_cal_blue`, `o_colcat_light_blue`, ...) stay valid through the alias map in `_palette.scss` and `ColorService.resolve()` in Java.
- **Java:** `Theme.init()` reads the `--o-{hue}-{step}` properties from the selected theme's compiled `theme.css` (fallback `light`) into a `Palette`; `ColorService.getPalette()` returns it. No generated palette file exists. A client theme may override the values of `$o-palette` (keep the 14 hue names) and Java follows. `Palette.getThemeColors()` / `ColorService.getThemeColors()` return only the theme colours the theme defines; a stored `theme{n}` above that count resolves to `theme1`.
- Live reference with contrast values: `doc/component-library/colors.html`.

## Component DOM Patterns

**FlexiTable:** Full structure (each part optional):
`.o_table_tabs` → `.o_table_filters_wrapper.o_expanded > .o_table_filters_row > ul.nav.nav-pills.o_table_filters > li > a.btn.btn-default.o_table_filter[.o_filter_active]` → `.o_table_toolbar` (search + tools) → `.o_table_batch_buttons` → `.o_table_wrapper.o_table_flexi` (CLASSIC: `.o_scrollable_wrapper > .o_scrollable > table.table`; CUSTOM: `.o_table_body > .o_table_row`) → `.o_table_pagination`.
Filter buttons: active state `o_filter_active` goes on the `<a>`, NOT the `<li>`. Caret: `<i class="o_icon o_icon-fw o_icon_caret">`. Toggle: `.o_sel_table` / `.o_sel_custom`.

**Tree Menu:** `.o_tree > ul[role=tree] > li[role=treeitem] > div.o_tree_l{n}` > `a.o_tree_oc_l{n}` (opener) + `span.o_tree_link > a > span.o_tree_item`; levels `o_tree_l{0-11}`

**Toolbar:** `.o_toolbar > .o_breadcrumb + .o_tools_container > .container-fluid (flex) > ul.o_tools.o_tools_{left|center|right|right_edge}` in this DOM order; segments `ul.o_tools_segments > li > div.o_segments.btn-group` (selected `a.btn-primary`)

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
- Focus after a redraw: one rule in `o_ainvoke()` (since OO-9603). Command 2 records the focused element, the end of the response puts it back when the focus is on `body`. Order: command 11 (first error field only with `validation: true`, else `formItemId`, weak: error field, `[autofocus]`), dialog return, dialog open focus, redraw rule. Nothing happens while an open `<dialog>` has the focus
- Fallback chain (`o_resolveFocusTarget()`): same id, table row key + slot, table, closest surviving ancestor (temporary `tabindex="-1"`), never `body`
- No new focus opt-in classes or `onfocus` hooks (`o_can_have_focus` and `o_info.lastFormFocusEl` are removed); a focusable element needs a DOM id that stays the same across redraws
- Live regions: use `role="status"` / `aria-live="polite"` for async state (AI overlays, `#o_dashboard_edit_live`); `#o_messages` info boxes are NOT announced
- Keyboard: `triggerClick(event, enter, space)` on link buttons; Escape closes the top layer via `o_doEscapeDispatch()`
- Callout triggers: `Link/FormLink.setAriaDialogOpener()` → `role="button" aria-haspopup="dialog" aria-expanded`; `CloseableCalloutWindowController` toggles `aria-expanded` and returns the focus to the trigger (`pushdialogfocus`/`popdialogfocus`). Pass the trigger component, not its DOM id. **New in 21.1.0:** used on action columns, table tools and filters; the focus goes to the first visible element of the callout.
- Disabled links: `LinkRenderer` writes `role`, `aria-disabled="true"` and `aria-haspopup` only for links with an ARIA role (all `Link.BUTTON*` presentations, `setAriaRole`, `setAriaDialogOpener`); plain links and breadcrumbs get no `aria-disabled`. Never `aria-expanded` on a disabled trigger. Do not add `aria-disabled` by hand. The tooltip is `setTextReasonForDisabling(...)`, fallback the translated title.
- Toolbar (`ToolBarRenderer`): the slot lists are in the DOM in visual order (left, center, right, rightEdge), so the tab order is the visual order. `.container-fluid` is the flex container, the center list takes the free space (`flex: 1 1 0`) and wraps its tools inside, right/rightEdge get `margin-left: auto`. Never position slots with floats, CSS `order` or a positive `tabindex` (WCAG 2.4.3, F44).
- Breadcrumb (`BreadcrumbBarRenderer`): `<nav aria-label>` > `<ol>` > plain `<li>`; the current crumb is `<span aria-current="page">`, not a link. `role="menuitem"` only inside the "more" menu (`ul role="menu"`).

## Responsive Design

Bootstrap 3 breakpoints: xs (<768px), sm (<992px), md (<1200px), lg (>=1200px).
Navbar → hamburger, left column → offcanvas, tables → horizontal scroll, toolbar text hides on xs. `o_form_two_cols` is 2 columns only from 991px.
