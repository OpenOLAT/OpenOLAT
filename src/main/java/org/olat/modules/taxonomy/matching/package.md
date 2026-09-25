# Taxonomy Matching

**Package:** `org.olat.modules.taxonomy.matching`

Embedding-based matching of free text to taxonomy levels. The AI image description returns a free-text `subject`. This package finds the taxonomy levels that fit that subject best. It embeds every taxonomy level once with an embedding model and stores the vectors. At query time it embeds the subject and ranks the levels by cosine similarity.

The feature shipped with OO-9428 in 21.0. It has no changes that are only in 21.1.0.

Key features:

- Embedding model from any `AiEmbeddingSPI` of the AI service: OpenAI, a generic OpenAI-compatible instance, or the in-process `LocalOnnxSPI`
- Three text variants per level and per language: name, path with name, path with name and description
- Vector search with pgvector on PostgreSQL (HNSW index), or an in-memory cosine search on every other setup
- Durable, asynchronous indexing queue (`o_tax_level_index_state`) on the `aiBatch` task pool
- A nightly Quartz job that recovers stuck rows, retries failed rows and re-embeds rows of an old model
- Usage logging of every embedding call as feature `TaxonomyMatching` in `o_ai_usage_log`
- Fallback to an exact, case-insensitive name match when the feature is off

## Package Structure

| Directory | Purpose |
|-----------|---------|
| `matching/` (root) | Service interface, `TaxonomyMatchingModule`, `TaxonomyMatchingHelper`, enums `PgVectorType` and `TaxonomyEmbeddingTextVariant`, `TaxonomyMatchingReindexEvent` |
| `manager/` | `TaxonomyMatchingServiceImpl`, the two DAOs, `TaxonomyEmbeddingTextBuilder`, `TaxonomyEmbeddingIndexJob` |
| `model/` | JPA entities `TaxonomyLevelEmbeddingImpl` and `TaxonomyLevelIndexStateImpl`, interface `TaxonomyLevelIndexState`, record `TaxonomyMatch` |
| `_spring/` | `taxonomyMatchingJobContext.xml`: Quartz trigger and job beans of the nightly job |

The package has no `ui/` sub-package. The configuration form lives in the AI administration (`AiFeaturesAdminController`, see section 7).

---

## 1. Class Overview

### 1.1 Root package

| Class | Responsibility |
|-------|---------------|
| `TaxonomyMatchingService` | Public API: `suggestLevels(...)` for the search, `scheduleIndex`, `scheduleSubtree`, `startIndexing`, `scheduleFullReindex` for the index, `deleteIndexState` and `deleteEmbeddings` for clean-up. |
| `TaxonomyMatchingModule` | `@Service` extending `AbstractSpringModule`. Holds the configuration (`taxonomy.matching.*`), resolves the query and passage prefixes from the model name, and tracks whether pgvector is active (`isPgVectorActive()`). Listens for `TaxonomyMatchingReindexEvent` on the resource type `TaxonomyMatching`. |
| `TaxonomyMatchingHelper` | Static entry point for callers: `matchTaxonomyLevels(context, subject, taxonomyRefs, taxonomyService, matchingService, aiModule, locale)`. Uses the embedding search when `AiModule.isTaxonomyMatchingEnabled()` is true, else the legacy exact match on the translated display name or the identifier. |
| `TaxonomyEmbeddingTextVariant` | Enum `NAME`, `PATH_NAME`, `FULL`. The text form of one embedding row. |
| `PgVectorType` | Enum `VECTOR` (`vector`, float32, HNSW up to 2000 dimensions) and `HALFVEC` (`halfvec`, float16, HNSW up to 4000 dimensions). `sqlType()` returns the SQL type name. |
| `TaxonomyMatchingReindexEvent` | `MultiUserEvent` (`taxonomy-matching-reindex-completed`). Fired cluster-wide when the index is complete, so every node sets pgvector active. |

### 1.2 `manager/`

| Class | Responsibility |
|-------|---------------|
| `TaxonomyMatchingServiceImpl` | Implements the service. Embeds the query, runs the pgvector search or the in-memory search, and builds the `TaxonomyMatch` list. Owns the indexing worker (inner class `IndexingTask`, a `TaskRunnable` on `Queue.aiBatch`). Starts after `FrameworkStartedEvent`. |
| `TaxonomyLevelEmbeddingDAO` | Native SQL on `o_tax_level_embedding`: `upsert`, `findByTaxonomy`, `findSimilar` (pgvector `<=>` cosine distance), `deleteAll`, `deleteByLevel`, `deleteByTaxonomy`, `countByTaxonomy`, `isPgVectorAvailable()`, `ensureVectorColumnAndIndex(dim, allowRebuild)`. |
| `TaxonomyLevelIndexStateDAO` | The indexing queue on `o_tax_level_index_state`: `upsertScheduled`, `scheduleSubtree`, `scheduleAll`, `claimNextScheduled`, `markIndexed`, `markFailed`, `resetInWorkToScheduled`, `rescheduleFailed(maxAttempts)`, `rescheduleStaleModel(modelId)`, `countByStatus(es)`, `findFailed`, `deleteByLevel`. |
| `TaxonomyEmbeddingTextBuilder` | Static `build(level, translator, levelMap, textVariant)`. Builds the text of one variant from the translated display name, the ancestor path and the description. Returns `null` when the variant has no useful text, for example `FULL` without a description. |
| `TaxonomyEmbeddingIndexJob` | Nightly `JobWithDB`, `@DisallowConcurrentExecution`. Resets stuck rows, reschedules failed rows up to 5 attempts, reschedules rows of an old model, and calls `startIndexing()`. |

### 1.3 `model/`

| Class | Responsibility |
|-------|---------------|
| `TaxonomyLevelEmbeddingImpl` | JPA entity `ctaxonomylevelembedding`, table `o_tax_level_embedding`. One row per level, language, model and text variant. |
| `TaxonomyLevelIndexState` / `TaxonomyLevelIndexStateImpl` | JPA entity `ctaxonomylevelindexstate`, table `o_tax_level_index_state`. One row per level. Enum `IndexStatus`: `scheduled`, `indexing`, `indexed`, `failed`. |
| `TaxonomyMatch` | Record `(level, score, matchedLocale, matchedText, exactMatch)`. Sorts exact matches first, then by descending score. |

---

## 2. Flows

### 2.1 Matching flow

1. A caller passes the AI `subject` to `TaxonomyMatchingHelper.matchTaxonomyLevels(...)`.
2. The helper calls `suggestLevels(context, subject, taxonomy, 3, minScore)` for each taxonomy. `minScore` comes from `AiModule.getTaxonomyMatchingMinScore()`.
3. The service builds the embedding model through `AiModule.getConfiguredEmbeddingSPI().buildEmbeddingModel(model)`. It embeds the query prefix plus the subject.
4. With pgvector it runs `findSimilar(...)`. Without pgvector it loads all rows of the taxonomy (`findByTaxonomy`) and computes the cosine similarity in Java.
5. It keeps the best row per level. A level whose embedding text equals the subject (case-insensitive) is an exact match and ranks first, whatever its score. Other levels need `score >= minScore`.
6. The service returns at most `limit` matches. Any error returns an empty list, so the caller never fails because of the matching.

### 2.2 Indexing flow

1. A change to a level writes a `scheduled` row. `TaxonomyServiceImpl` calls `scheduleIndex(level)` on create and update, and `scheduleSubtree(level)` on update and move. A delete calls `deleteEmbeddings(level)` and `deleteIndexState(level)`.
2. After the commit, the controller calls `startIndexing()`: `EditTaxonomyLevelController`, `MoveTaxonomyLevelController`, `TypeTaxonomyLevelController`, the import wizard of `TaxonomyTreeTableController`, and `TaxonomyWebService`.
3. `startIndexing()` submits one `IndexingTask` to `Queue.aiBatch`. An `AtomicBoolean` keeps a single worker per node.
4. The task claims rows with `claimNextScheduled()` until the queue is empty. For each level it embeds 3 text variants in German and English (the languages are fixed in `getConfiguredLocales()`). It prefixes each text with the passage prefix and stores it with `upsert(...)`.
5. A failure marks the row `failed` with the error text. The nightly job retries it.
6. When no row is `scheduled` or `indexing` any more, the task sets pgvector active and fires `TaxonomyMatchingReindexEvent`.

### 2.3 Full reindex

`scheduleFullReindex()` runs when the administrator saves a new provider or model (`AiFeaturesAdminController`), and at startup when the index state table is empty. It resolves the vector column for the new model, deletes all embeddings, schedules every level and starts the worker. A generation counter stops a worker that still runs for the old model. On PostgreSQL with pgvector, the reindex stops and keeps the old embeddings when the column type cannot be resolved.

---

## 3. Vector Storage

`o_tax_level_embedding` always stores the vector as JSON in `t_vector_json`. On PostgreSQL the service adds a native vector column `t_vector` at runtime when all of these are true:

- `taxonomy.matching.pgvector.enabled=true` (default)
- the `vector` type exists (`select to_regtype('vector') is not null`)
- the dimension of the model is known: from `AiEmbeddingSPI.getEmbeddingDimension(model)`, from the stored `taxonomy.matching.vector.dim`, or from a probe call that embeds the text `dim`

`ensureVectorColumnAndIndex(dim, allowRebuild)` picks the column type:

| Dimensions | Column type | Index |
|------------|-------------|-------|
| up to 2000 | `vector(dim)` | HNSW `idx_tax_emb_vector` with `vector_cosine_ops` (m = 16, ef_construction = 64) |
| 2001 to 4000 | `halfvec(dim)` | HNSW with `halfvec_cosine_ops` |
| more than 4000 | `halfvec(dim)` | no index, exact sequential scan |

A dimension change drops and re-creates the column only in a full reindex (`allowRebuild=true`). The setup scripts do not create `t_vector`. MySQL and Oracle always use the in-memory search.

Unique index `idx_tax_emb_unique` on `(fk_level, t_locale, t_model_id, t_text_variant)`.

---

## 4. Embedding Providers

The service does not know the provider. It asks `AiModule.getConfiguredEmbeddingSPI()`, which resolves `taxonomy.matching.spi` to an enabled `AiEmbeddingSPI` with `isEmbeddingEnabled()`. Implementations in `org.olat.core.commons.services.ai.spi`:

| SPI | Notes |
|-----|-------|
| `OpenAiSPI` | OpenAI embedding models |
| `GenericAiSpiInstance` | OpenAI-compatible server, for example vLLM or Ollama |
| `LocalOnnxSPI` | In-process ONNX model. `QwenOnnxEmbeddingModel` runs the uploaded `model.onnx` and `tokenizer.json`. The files live in `taxonomy.matching.local.model.dir`. `LocalOnnxModelAdminController` uploads them. |

`AiModule.isTaxonomyMatchingEnabled()` is true only when the module is enabled, the SPI resolves and a model name is set.

### 4.1 Query and passage prefixes

Some embedding models need a prefix. `TaxonomyMatchingModule.getQueryPrefix()` and `getPassagePrefix()` use the override properties first. Without an override they derive the prefix from the model name:

| Model name contains | Query prefix | Passage prefix |
|---------------------|--------------|----------------|
| `e5` | `query: ` | `passage: ` |
| `nomic` | `search_query: ` | `search_document: ` |
| `qwen` | `Instruct: Given a topic, retrieve the most relevant taxonomy level\nQuery: ` | none |
| other (bge-m3, OpenAI) | none | none |

---

## 5. Usage Logging

Every embedding call with a usage context goes through `AiLoggingEmbeddingModel` (`org.olat.core.commons.services.ai.manager`). It writes one `o_ai_usage_log` row with feature `taxonomy-matching`. The indexing worker uses `usageContextType = taxonomy-embedding-index`, one shared context id per worker run, `resourceType = TaxonomyLevel`, the level key and the language as sub-id. A search uses the context of the caller, for example the media upload.

---

## 6. Callers

| Caller | Use |
|--------|-----|
| `MediaAiMetadataService` (`modules.cemedia.manager`) | Background AI metadata task: matches the image subject to the taxonomy levels of the media center ([package doc](../../cemedia/package.md)) |
| `MediaUploadController`, `CollectImageMediaController` (`modules.cemedia.ui`) | Explicit "Generate metadata with AI" button: same match, applied to the open form |
| `TaxonomyServiceImpl` | Schedules index rows on create, update and move; deletes embeddings on delete |
| `EditTaxonomyLevelController`, `MoveTaxonomyLevelController`, `TypeTaxonomyLevelController`, `TaxonomyTreeTableController`, `TaxonomyWebService` | Start the worker after a level change |
| `AiFeaturesAdminController` | Configuration form; triggers the full reindex on a model change |
| `LocalOnnxSPI`, `LocalOnnxModelAdminController` | Read `getLocalModelDir()` |

---

## 7. Configuration

`TaxonomyMatchingModule` persists its values to `{userdata}/system/configuration/org.olat.modules.taxonomy.matching.TaxonomyMatchingModule.properties`. Defaults in `olat.properties`:

| Key | Default | Purpose |
|-----|---------|---------|
| `taxonomy.matching.enabled` | `false` | Switches the feature on |
| `taxonomy.matching.pgvector.enabled` | `true` | Uses pgvector when available. `false` forces the in-memory search, for tests. |
| `taxonomy.matching.spi` | empty | Id of the embedding SPI |
| `taxonomy.matching.model` | empty | Embedding model name |
| `taxonomy.matching.min.score` | `0.65` | Minimum cosine similarity of a non-exact match |
| `taxonomy.matching.local.model.dir` | `${userdata.dir}/ai/models` | Folder of the local ONNX model |
| `taxonomy.matching.query.prefix` | empty | Override of the query prefix |
| `taxonomy.matching.passage.prefix` | empty | Override of the passage prefix |
| `taxonomy.matching.vector.key`, `taxonomy.matching.vector.dim` | not in `olat.properties` | Written by the service: the `spi:model` key and the dimension of the current vector column |

The Quartz trigger `taxonomyEmbeddingIndexTrigger` runs the job daily at 03:00 (`0 0 3 * * ?`), 5 minutes after startup at the earliest. It runs only on the cluster singleton node (`${cluster.singleton.services}`).

### 7.1 Admin UI

The AI administration, segment "AI features", shows the section "Taxonomy Matching (Embeddings)" (`ai.feature.taxonomy-matching`). It holds the toggle, the embedding SPI (only SPIs that implement `AiEmbeddingSPI`) and the model (`ai.feature.taxonomy-matching.model`). The minimum score and the prefixes have no form field. The form respects the read-only mode of the AI administration.

---

## 8. Database

| Table | Content |
|-------|---------|
| `o_tax_level_embedding` | `t_text_variant`, `t_locale`, `t_embedding_text`, `t_model_id`, `t_model_version`, `t_vector_json`, optional `t_vector`, `fk_level`, `fk_taxonomy` |
| `o_tax_level_index_state` | `t_status`, `t_attempt_count`, `t_last_error`, `t_indexed_model_id`, `t_indexed_model_version`, `t_last_index_date`, `fk_level` (unique) |

Both entities are registered in `META-INF/persistence.xml`.
