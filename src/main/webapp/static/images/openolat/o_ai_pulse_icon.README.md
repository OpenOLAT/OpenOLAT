# OpenOlat AI Pulse Icon

An animated 80×80 SVG indicator for AI activity. Iridescent gradient stroke
(pink → purple → blue → cyan), three traveling colored flares, breathing
glow, and ambient sparkles. Two states: **idle** (sparkles slowly twinkle,
no flares, no glow) and **active** (full treatment for "AI is working").

## Files

| Path | Purpose |
|------|---------|
| `src/main/webapp/static/themes/light/modules/_ai_pulse.scss` | All CSS classes, keyframes, sizing helpers, reduced-motion fallback. Imported in `_modules.scss`. |
| `src/main/java/org/olat/core/commons/services/ai/ui/_content/ai_pulse_icon.html` | Velocity fragment with the inline SVG markup (no internal `<style>` — relies on the SCSS). **Use this for inline embedding.** |
| `src/main/webapp/static/images/openolat/o_ai_pulse_icon_static.svg` | Standalone idle SVG. Self-contained — works as `<img src="…">`. |
| `src/main/webapp/static/images/openolat/o_ai_pulse_icon_active.svg` | Standalone always-animating SVG. Self-contained — works as `<img src="…">`. |
| `src/main/webapp/static/images/openolat/o_ai_pulse_icon.README.md` | This file. |

## CSS classes

| Class | What it does |
|-------|--------------|
| `o_ai_pulse` | Base class — required on the host `<svg>` for the sparkles to animate at all. |
| `o_ai_pulse_active` | Adds breath, halo, glow, traveling flares, and brighter twinkles. Toggle on/off to switch states. |
| `o_ai_pulse_xs` … `o_ai_pulse_xl` | Size helpers: 32 / 40 / 64 / 80 / 120 px. |
| `o_ai_pulse_badge` | Wrapper utility for the inverse / colored-circle-background look. |

## How to use it

### Option A — inline SVG via Velocity (recommended)

This is the right choice when you need to **toggle** between idle and active
in response to user actions, because external CSS classes only affect inline
SVGs (not `<img>`).

In your Velocity template:

```velocity
## defaults: 80×80, idle
#parse("/org/olat/core/commons/services/ai/ui/_content/ai_pulse_icon.html")

## active state, 64 px size
#set( $aiPulseExtraClass = "o_ai_pulse_active o_ai_pulse_md" )
#parse("/org/olat/core/commons/services/ai/ui/_content/ai_pulse_icon.html")
#set( $aiPulseExtraClass = "" )  ## reset for sibling renders
```

To toggle from a controller, re-render the parent template after changing
the context variable, **or** flip the class via JS on the `<svg>` element.
Example listener that activates while a request is running:

```js
const svg = document.querySelector(".o_my_ai_panel .o_ai_pulse");
svg.classList.add("o_ai_pulse_active");
// ... when done:
svg.classList.remove("o_ai_pulse_active");
```

### Option B — drop-in `<img>`

Simplest, but you cannot toggle states via CSS. Swap the `src` instead.

```velocity
<img src="$r.staticLink('images/openolat/o_ai_pulse_icon_static.svg')"
     alt="OpenOlat AI"
     width="80" height="80"/>

<img src="$r.staticLink('images/openolat/o_ai_pulse_icon_active.svg')"
     alt="OpenOlat AI working"
     width="80" height="80"/>
```

### Option C — colored-background badge

The icon's gradient is fully saturated and reads well on most backgrounds.
For an inverse / "chip" style, wrap it:

```velocity
<span class="o_ai_pulse_badge">
    #parse("/org/olat/core/commons/services/ai/ui/_content/ai_pulse_icon.html")
</span>
```

The `o_ai_pulse_badge` background is `$brand-primary` by default; override
in your own SCSS if a feature needs a different color.

## Design notes

- **Color palette is fixed** (pink/purple/blue/cyan iridescent). The icon
  is intentionally not theme-able — it's a brand AI signature.
- **Geometry:** kissing circles (two perfect lobes meeting at the centre).
  An alternative true-infinity shape (diagonal crossing) is checked into
  `agent-outbox/` of the AI tooling repo if a future redesign wants to swap.
- **Performance:** the icon uses SVG filters (`feGaussianBlur`) and SMIL
  (`animateTransform`, `animateMotion`). At 80 px these are cheap. Avoid
  rendering more than 4–5 instances on the same page simultaneously.
- **Accessibility:** the `<svg>` carries `role="img"` and an `aria-label`.
  Set a translated label if the icon stands alone (no surrounding label
  text). Override the default by setting `$aiPulseAriaLabel` before the
  `#parse`.
- **Reduced motion:** the SCSS honors `prefers-reduced-motion: reduce`
  and falls back to a tasteful resting frame (visible icon, no animation).

## Multiple instances on one page

The inline SVG declares its own `<defs>` (gradient, filters, route path)
with stable IDs (`aip-rainbow`, `aip-halo`, `aip-glow`, `aip-flare`,
`aip-route`, `aip-spark`, `aip-spark-glow`). Browsers resolve `url(#…)`
references against the first matching ID in document order, so duplicate
IDs across multiple instances are functionally fine — but technically
invalid HTML5. If you need many simultaneous instances, consider hoisting
the `<defs>` block into a hidden master `<svg>` once on the page and
removing it from the per-instance markup.

## Editing the design

The canonical source of the design is `agent-outbox/ai-pulse-icon*.svg`
plus `agent-outbox/ai-pulse-icon-demo.html` in the AI tooling repo. Iterate
there with the demo HTML, then sync three things back into OpenOlat:

1. The SCSS — `themes/light/modules/_ai_pulse.scss`
2. The Velocity template — `core/commons/services/ai/ui/_content/ai_pulse_icon.html`
3. The standalone `<img>` files — `static/images/openolat/o_ai_pulse_icon_*.svg`

The SCSS classes and the SVG inner class names (`.lobe`, `.core`, `.glow`,
`.halo`, `.breath`, `.flares`, `.spark`, `.spark-fixed`, `.spark-ambient`)
must stay in lockstep, otherwise the inline icon won't animate.
