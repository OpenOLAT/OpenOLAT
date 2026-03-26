# OpenOlat Development — Claude Code Guide

## Project Overview

OpenOlat is a web-based LMS (Learning Management System) built with Java 17+, Jakarta EE, Spring 7, Hibernate 7, and Apache Velocity. It uses a **server-centric** UI architecture — all state lives on the server, the browser receives HTML fragments via AJAX. There is no client-side framework.

- **License:** Apache 2.0, developed by frentix GmbH, Zurich, Switzerland
- **Naming:** Always write "OpenOlat" in prose (not "OpenOLAT")
- **Source root:** `src/main/java/org/olat/`

## Skills

When helping with OpenOlat development tasks, use the `openolat-dev` skill:

```
/openolat-dev
```

It provides detailed architecture knowledge, code patterns, and conventions for controllers, forms, FlexiTables, Velocity templates, persistence, VFS, i18n, and more.

## Build

```bash
mvn compile -pl :openolat-lms -q
```

## Key Architecture Rules

1. **No business logic during render phase** — all DB access and service calls must complete before the render phase begins
2. **Always use `listenTo()`** for child controllers to ensure automatic disposal
3. **Always use `removeAsListenerAndDispose()`** before replacing a controller
4. **Always escape user text** in Velocity templates with `$r.escapeHtml($var)` — variables from `contextPut()` are NOT auto-escaped
5. **Never access `bcroot/` directly** — always use VFS (`VFSManager`, `VFSContainer`, `VFSLeaf`)
6. **Never use string concatenation in JPQL** — always use named parameters (`:paramName`)
7. **Always deregister EventBus listeners** in `doDispose()`
8. **Always use `HttpClientService`** for outbound HTTP requests — never use `java.net.http.HttpClient`, other HTTP libraries, or instantiate Apache `HttpClient` directly

## Project Layout

```
src/main/java/org/olat/          # Java source
  gui/                           # Core UI framework (controllers, components, FlexiTable, etc.)
  core/                          # Core services
  basesecurity/                  # Identity & roles
  course/                        # Course module
  modules/                       # Feature modules
  restapi/                       # REST API
src/main/resources/
  META-INF/persistence.xml       # All JPA entity registrations
  serviceconfig/olat.properties  # Default configuration
src/main/webapp/
  static/themes/                 # SASS themes
  WEB-INF/                       # Web config
```

Templates (`*.html` Velocity files) are colocated with their controller package in a `_content/` subdirectory.
i18n files (`LocalStrings_XX.properties`) are colocated in `_i18n/` subdirectories.

## Configuration

- `olat.local.properties` — local overrides (not committed)
- `olat.local.properties.sample` — template for local config

## Documentation

- **End-user docs:** https://docs.openolat.org — non-technical documentation for end users. Consult this when writing or proposing end-user documentation, or verifying that a feature is correctly described in the docs or when exploring how a feature works.
- **Technical architecture:** `.claude/openolat-architecture-knowledge.md` and `doc/openolat-architecture.md` (loaded via the `openolat-dev` skill)

## MCP Tools Available

- **YouTrack** https://track.frentix.com — issue tracking (`mcp__youtrack__*` tools available for looking up issues, creating tasks, etc.)
