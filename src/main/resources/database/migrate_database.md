▗ ▗   ▖ ▖  Claude Code v2.1.169
           Opus 4.8 · Claude Team
  ▘▘ ▝▝    ~/…/Work/ws_sidedfeatures/OpenOLAT/src/main/resources/database

  Feature of the week: /loop — run a prompt or slash command on a recurring interval


❯ How can I migrate a database from MariaDB to PostgreSQL? We don't have BLOGs  
  or binary data but a lot of foreign keys.                                     

⏺ Good question. Let me check what OpenOlat ships for database setup so I can
  give you advice that fits the project, not just generic SQL migration steps.

  Listed 1 directory (ctrl+o to expand)

⏺ Bash(cd /Users/srosse/Developer/Work/ws_sidedfeatures/OpenOLAT/src/main/resour
      ces/database; echo "=== readme.txt ==="; cat readme.txt; echo; echo "===
      postgresql ===…)
  ⎿  === readme.txt ===
     OLAT DATABASE
     ==================================
     … +170 lines (ctrl+o to expand)

⏺ Bash(cd /Users/srosse/Developer/Work/ws_sidedfeatures/OpenOLAT/src/main/resour
      ces/database; echo "=== pg: FK / constraint style ==="; grep -ci "foreign
      key" postgres…)
  ⎿  === pg: FK / constraint style ===
     623
     639
     … +19 lines (ctrl+o to expand)

⏺ OpenOlat ships native DDL for both engines (src/main/resources/database/mysql/
  and .../postgresql/), and that's the key to doing this cleanly. Don't
  translate the MariaDB schema to Postgres — you'll fight type mismatches
  forever. Instead, build the schema from OpenOlat's own PostgreSQL scripts and 
  migrate only the data.

  Recommended approach: native schema + data-only migration

  1. Create the target schema from OpenOlat's PG DDL

  Run the PostgreSQL setupDatabase.sql matching your current version, then apply
  the alter_* scripts in order up to your exact release. This gives you a
  schema that is byte-for-byte what Hibernate expects (datetime→timestamp,
  auto_increment→bigserial, etc. — all already handled for you).

  A convenient detail in your favor: in the PG scripts the foreign keys are not
  inline — they're 623 separate ALTER TABLE ... ADD CONSTRAINT statements at the
  end of the file. So you can split the DDL into tables-only and constraints
  halves.

  2. Migrate the data with FK enforcement deferred

  Your "lots of FKs, no BLOBs" situation is the easy case. Two options:

  - pgloader (recommended workhorse). It connects to both DBs, copies data,
  converts types/booleans, disables triggers during load, and resets sequences.
  With a schema already in place, use a load file that only copies data:
  LOAD DATABASE
    FROM mysql://user:pass@host/olat
    INTO postgresql://user:pass@host/olat
  WITH data only, disable triggers, reset sequences,
       batch rows = 5000
  SET work_mem to '256MB', maintenance_work_mem to '512MB';
  - disable triggers is what frees you from FK insert-ordering; reset sequences
  fixes the bigserial counters (see gotcha below).
  - Manual dump/load if you prefer control: create tables only → load data → add
  the FK constraints last. Since there's no binary data, a CSV or INSERT dump
  transfers fine.

  Gotchas specific to MariaDB → PostgreSQL (and to OpenOlat)

  Issue: Sequences
  What to do: PG bigserial columns have independent sequences. After load
  they're
    at 1 → new inserts will collide with existing PKs. Reset every sequence to
    max(id)+1 (pgloader's reset sequences does this; otherwise script setval).
    Verify this before going live — it's the #1 cause of post-migration
  failures.
  ────────────────────────────────────────
  Issue: Booleans
  What to do: MariaDB stores tinyint(1) 0/1; PG wants true/false. pgloader casts

    automatically; a manual dump needs explicit conversion.
  ────────────────────────────────────────
  Issue: Zero dates
  What to do: MariaDB allows 0000-00-00 00:00:00; PG rejects it. Scan for these
    first and convert to NULL.
  ────────────────────────────────────────
  Issue: Empty string vs NULL
  What to do: Watch columns that are '' in MariaDB but defined NOT NULL
    differently — usually fine, but check after load.
  ────────────────────────────────────────
  Issue: Encoding
  What to do: Ensure the PG database is created as UTF8.
  ────────────────────────────────────────
  Issue: Case sensitivity
  What to do: PG comparisons are case-sensitive where MySQL's default collation
    isn't. Rarely an issue for OpenOlat's surrogate-key model, but relevant if
    you have custom queries.

  Validation before cutover

  1. Row counts per table match between source and target.
  2. FK integrity: all ADD CONSTRAINT statements applied without error =
  referential integrity is intact (any orphan row would fail here — a free
  data-quality check).
  3. Sequence sanity: SELECT max(id) FROM <table> vs nextval for the busiest
  tables.
  4. Switch olat.local.properties to the Postgres db.vendor/JDBC URL, start
  OpenOlat, and let Hibernate validate the schema on boot.

  One thing to confirm

  What OpenOlat version is the source DB on? The migration is only safe if the
  PG schema you build is for the exact same version. If you tell me the version,
  I can list the precise sequence of setupDatabase.sql + alter_*.sql files to
  run for the PostgreSQL side.

  Would you like me to draft a concrete step-by-step runbook (pgloader-based)
  once I know the version?

✻ Sautéed for 54s