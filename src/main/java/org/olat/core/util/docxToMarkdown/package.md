# DOCX-to-Markdown Converter

**Package:** `org.olat.core.util.docxToMarkdown`
**Type:** Core Utility (zero external dependencies beyond fmath for math conversion)
**Design:** Stateless `@Service`, thread-safe — all state is local to each `convert()` call
**Files:** 16 Java files (~3,000 LOC), 2 i18n property files
**Pipeline:** 6-stage SAX-based conversion from DOCX (ZIP) to Markdown

---

## Public API

Only 3 public classes. Everything else is package-private.

### DocxToMarkdownService

Spring `@Service` entry point. Registered via component-scan in `utilCorecontext.xml`.

```java
@Service
public class DocxToMarkdownService {
    /** Convert from a java.io.File */
    public DocxToMarkdownResult convert(File docxFile);
    /** Convert from a VFSLeaf (local files accessed directly, non-local copied to temp) */
    public DocxToMarkdownResult convert(VFSLeaf vfsLeaf);
}
```

Usage with VFS:
```java
@Autowired
private DocxToMarkdownService docxService;

// From a VFS leaf (preferred in OpenOlat)
DocxToMarkdownResult result = docxService.convert(vfsLeaf);

// From a java.io.File
DocxToMarkdownResult result = docxService.convert(docxFile);

// Process result
String markdown = result.markdown();
File mediaDir = result.basePath(); // temp dir with media/ subdirectory
// Clean up mediaDir when done
```

### DocxToMarkdownResult

Conversion result record.

```java
public record DocxToMarkdownResult(
    String markdown,          // front matter + body, never null
    File basePath,            // temp dir containing media/ subdirectory (may be null)
    List<DocxConversionMessage> messages
) {
    public boolean hasMessages();
    /** Renders as HTML <ul><li>...</li></ul> — HTML-escaped, safe for UI display */
    public String renderMessagesAsHtml(Translator translator);
    /** Renders as plain text "- message\n- message" — for logging or non-HTML contexts */
    public String renderMessagesAsText(Translator translator);
}
```

### DocxConversionMessage

A single conversion message with i18n key and substitution arguments.

```java
public record DocxConversionMessage(
    Level level,    // INFO, WARNING, ERROR
    String i18nKey, // e.g. "docx.convert.warn.image.skipped"
    String[] args   // substitution arguments for {0}, {1}, ...
) {
    public String translate(Translator translator);
}
```

### Usage Example

```java
@Autowired
private DocxToMarkdownService docxService;

DocxToMarkdownResult result = docxService.convert(docxFile);
String markdown = result.markdown();
File mediaDir = result.basePath(); // contains media/ subdirectory with images

if (result.hasMessages()) {
    String warnings = result.renderMessagesAsText(translator);
    log.info("Conversion messages:\n{}", warnings);
}

// Clean up temp directory when done
FileUtils.deleteDirsAndFiles(result.basePath(), true, true);
```

---

## Pipeline Architecture

```
DOCX (ZIP)
  |
  v
[1] DocxZipExtractor
  |   Validates ZIP structure, checks for path traversal,
  |   macros (vbaProject.bin), entry count (max 64),
  |   entry size (max 50 MB). Extracts auxiliary XML as byte[].
  |   document.xml is NOT buffered — streamed later.
  |
  +---> byte[] relsXml ---------> [2] DocxRelationshipParser
  |                                    word/_rels/document.xml.rels
  |                                    -> Map<id, DocxRelTarget>
  |
  +---> byte[] numberingXml ----> [3] DocxNumberingParser
  |                                    word/numbering.xml
  |                                    -> Map<numId, DocxNumberingDef>
  |
  +---> byte[] stylesXml -------> [4] DocxStyleParser
  |                                    word/styles.xml
  |                                    -> Map<styleId, name>
  |
  +---> byte[] corePropsXml ----> [5] DocxMetadataParser
  |     byte[] appPropsXml            docProps/core.xml + app.xml
  |                                    -> DocxMetadata
  |
  +---> byte[] footnotesXml ---> [5b] DocxFootnoteParser
  |     byte[] endnotesXml            word/footnotes.xml + endnotes.xml
  |                                    -> Map<id, text>
  |
  v
[6] DocxToMarkdownHandler (SAX on document.xml, streamed from ZIP)
  |   Uses outputs from stages 2-5b as lookup tables.
  |   Images/videos extracted on-demand from open ZipFile.
  |   VML shapes converted via VmlToSvgConverter.
  |   Math equations converted via DocxMathConverter (fmath).
  |
  v
Markdown string + media files on disk (temp directory)
```

---

## Supported Features

| Category | Feature | Markdown Output |
|---|---|---|
| **Headings** | H1--H6, Title -> H1, Subtitle -> H2 | `#` -- `######` |
| **Formatting** | Bold, Italic, Strikethrough | `**`, `*`, `~~` |
| | Underline | `<span style="text-decoration:underline">` |
| | Superscript, Subscript | `<sup>`, `<sub>` |
| | Highlight | `<mark>` |
| **Character styles** | Emphasis, IntenseEmphasis, Strong, SubtleEmphasis, BookTitle | Mapped to formatting |
| **Lists** | Bullet, Numbered, Nested, Mixed | `- `, `1. `, indented |
| **Tables** | Header detection (explicit/bold/none), GFM format | `\|col\|col\|` |
| **Images** | Inline, Anchored, VML legacy | `![alt](media/file.png){width=N height=N}` |
| **Shapes** | DrawingML custom geometry, Preset shapes, Groups | SVG files in media/ |
| **Math** | Display and inline (via fmath) | `$$...$$` with text fallback |
| **Hyperlinks** | External, Internal anchors, Mailto | `[text](url)` |
| **Blockquotes** | Quote, IntenseQuote | `> `, `> [!IMPORTANT]` |
| **Text boxes** | w:txbxContent | `> [!NOTE]` admonition |
| **Footnotes/Endnotes** | Numbered references | `[^N]` + definitions |
| **Checkboxes** | SDT checkboxes | `[x]` / `[ ]` |
| **Videos** | a:videoFile | `[Video: name](media/file.mp4)` streamed |
| **SmartArt diagrams** | diagrams/drawing*.xml shapes | Rendered to SVG with shapes + text |
| **Charts** | mc:Fallback images | Fallback PNG/EMF extracted |
| **Track changes** | Insertions accepted, Deletions skipped | |
| **Metadata** | docProps -> YAML front matter | `---\ntitle: ...\n---` |
| **Image dimensions** | wp:extent -> CommonMark attributes | `{width=N height=N}` |

### Image Dimension Mapping

Image dimensions come from `wp:extent` in OOXML (EMU units), representing the **display size in the Word document layout** — not the native image pixel resolution. A standard Word page content area is ~605px wide at 96 DPI (~16cm). The importer maps image width as a proportion of the Word page width to the content editor's `ImageSize` CSS class:

| Word page proportion | Pixel range | ImageSize | CSS width |
|---|---|---|---|
| ≤ 35% | ≤ 212px | `small` | 25% |
| 35–55% | 213–333px | `medium` | 40% |
| 55–80% | 334–484px | `large` | 60% |
| > 80% | > 484px | `fill` | 100% |

---

## Security Architecture

| # | Attack Vector | Protection |
|---|---|---|
| 1 | XXE | `XMLFactories` + `disallow-doctype-decl` on ALL parsers |
| 2 | Zip bomb | Entry count limit (64) + per-entry size limit (50 MB) |
| 3 | Zip slip | Entry name validation (rejects paths containing `..`) |
| 4 | Macros | `vbaProject.bin` scan during ZIP extraction |
| 5 | JavaScript URLs | Protocol whitelist: `http`, `https`, `mailto` only |
| 6 | OLE/ActiveX | Entries skipped |
| 7 | HTML injection | Pure markdown output; `renderMessagesAsHtml()` escapes `&`, `<`, `>` |
| 8 | SVG script injection | Generated SVGs contain only `<path>` shape data |

---

## Memory and Scaling

| Component | Strategy | Peak Memory |
|---|---|---|
| document.xml | **Streamed** from ZIP to SAX | ~0 (constant) |
| Auxiliary XMLs | byte[] (rels, styles, numbering, etc.) | < 1 MB total |
| Images | Read one at a time, max 10 MB, written to disk | 10 MB max |
| Videos | `transferTo()` streaming | Constant |
| Markdown output | StringBuilder | Proportional to text |
| Table rows | Buffered for header detection | Proportional to largest table |

### Scaling

| DOCX Size | Peak Heap |
|---|---|
| 10 MB | ~10 MB |
| 100 MB | ~25 MB |
| 500 MB | ~30 MB |
| 1 GB | ~35 MB |

Memory is proportional to text content, not file size. Images and videos are streamed to disk and never held in memory beyond the 10 MB single-image limit.

---

## Edge Cases and Limitations

| Situation | Handling |
|---|---|
| Nested tables | Flattened with warning |
| Merged cells | Ignored (GFM limitation) |
| Math (OOXML) | fmath conversion attempted; text fallback via `extractMathText()` if it fails |
| Charts without fallback | Skipped |
| Encrypted DOCX | Rejected with error |
| .docm files | Rejected (macro detection via `vbaProject.bin`) |
| Images > 10 MB | Skipped with warning |
| Missing numbering.xml | Treated as bullet list with warning |
| Comments | Skipped with warning |
| OLE objects | Skipped with warning |

---

## Package Structure

### Public API (3 classes)

| Class | Type | Purpose |
|---|---|---|
| `DocxToMarkdownService` | `@Service` | Entry point: `convert(File) -> DocxToMarkdownResult` |
| `DocxToMarkdownResult` | record | Result: `markdown`, `basePath`, `messages` |
| `DocxConversionMessage` | record | Message: `level`, `i18nKey`, `args` |

### Internal (13 package-private classes)

| Class | Type | Purpose |
|---|---|---|
| `DocxZipExtractor` | utility | ZIP reading + security checks |
| `DocxArchiveContent` | record | byte[] holders for auxiliary XML parts |
| `DocxRelationshipParser` | SAX handler | `.rels` -> `Map<id, DocxRelTarget>` |
| `DocxRelTarget` | record | Relationship type + target |
| `DocxNumberingParser` | SAX handler | `numbering.xml` -> `Map<numId, DocxNumberingDef>` |
| `DocxNumberingDef` | record | `orderedByLevel` map |
| `DocxStyleParser` | SAX handler | `styles.xml` -> `Map<styleId, name>` |
| `DocxMetadataParser` | SAX handler | `docProps` -> `DocxMetadata` |
| `DocxMetadata` | record | title, author, keywords, etc. + `toYamlFrontMatter()` |
| `DocxFootnoteParser` | SAX handler | footnotes/endnotes -> `Map<id, text>` |
| `DocxToMarkdownHandler` | SAX handler | Main converter (2100+ LOC) |
| `DocxMathConverter` | utility | OOXML math -> LaTeX via fmath |
| `VmlToSvgConverter` | utility | VML/DrawingML -> SVG |

---

## i18n

Keys are in `_i18n/LocalStrings_en.properties` and `_i18n/LocalStrings_de.properties`.

Pattern: `docx.convert.<level>.<category>.<detail>`

| Key | Level | Description |
|---|---|---|
| `docx.convert.warn.image.skipped` | WARNING | Image too large (>10 MB) |
| `docx.convert.warn.image.missing` | WARNING | Image not found in archive |
| `docx.convert.warn.table.nested` | WARNING | Nested table flattened |
| `docx.convert.warn.math.failed` | WARNING | Math conversion failed |
| `docx.convert.warn.element.unsupported` | WARNING | Unsupported element |
| `docx.convert.warn.numbering.missing` | WARNING | Numbering definitions missing |
| `docx.convert.warn.track.changes` | WARNING | Track changes detected |
| `docx.convert.warn.superscript` | WARNING | Superscript/subscript note |
| `docx.convert.warn.footnotes` | WARNING | Footnotes/endnotes skipped |
| `docx.convert.warn.comments` | WARNING | Comments skipped |
| `docx.convert.warn.smartart` | WARNING | SmartArt/chart skipped |
| `docx.convert.warn.ole` | WARNING | Embedded object skipped |
| `docx.convert.warn.merged.cells` | WARNING | Merged cells ignored |
| `docx.convert.warn.url.rejected` | WARNING | Unsafe URL protocol |
| `docx.convert.error.macro.detected` | ERROR | Macro detected |
| `docx.convert.error.invalid.format` | ERROR | Not a valid DOCX |
| `docx.convert.error.encrypted` | ERROR | Encrypted document |
| `docx.convert.error.read.failed` | ERROR | Read failure |
| `docx.convert.error.zip.bomb` | ERROR | Suspicious archive |
| `docx.convert.error.zip.slip` | ERROR | Invalid entry path |
| `docx.convert.info.metadata.partial` | INFO | Some metadata empty |

---

## Integration

### From a Controller

1. Add `@Autowired DocxToMarkdownService docxService;`
2. Call `docxService.convert(file)` with the uploaded `.docx` file
3. Use `result.markdown()` for the converted text
4. Use `result.basePath()` to access extracted media files (in `media/` subdirectory)
5. Clean up `result.basePath()` when done -- it is a temp directory

### Spring Context

The service is registered via component-scan in `utilCorecontext.xml`. No additional configuration is required.

### Dependencies

- **fmath** -- used by `DocxMathConverter` for OOXML math to LaTeX conversion
- **OpenOlat core** -- `XMLFactories` for secure SAX parser creation, `Tracing` for logging
- No other external dependencies. The converter uses only JDK classes (`java.util.zip`, `javax.xml.parsers`, `org.xml.sax`).

---

## Package Structure (17 classes)

```
Public API:
  DocxToMarkdownService (@Service)  — convert(File), convert(VFSLeaf) → DocxToMarkdownResult
  DocxToMarkdownResult (record)     — markdown, basePath, messages
  DocxConversionMessage (record)    — level, i18nKey, args

Internal (package-private):
  DocxZipExtractor                  — ZIP reading + security checks
  DocxArchiveContent (record)       — byte[] for auxiliary XML parts
  DocxRelationshipParser            — .rels → Map<id, DocxRelTarget>
  DocxRelTarget (record)            — type + target
  DocxNumberingParser               — numbering.xml → Map<numId, DocxNumberingDef>
  DocxNumberingDef (record)         — orderedByLevel map
  DocxStyleParser                   — styles.xml → Map<styleId, name>
  DocxMetadataParser                — docProps → DocxMetadata
  DocxMetadata (record)             — title, author, keywords, etc.
  DocxFootnoteParser                — footnotes/endnotes → Map<id, text>
  DocxToMarkdownHandler             — Main SAX handler (~2000 LOC)
  DocxMathConverter                 — OOXML math → LaTeX via fmath
  VmlToSvgConverter                 — VML/DrawingML shapes → SVG
  SmartArtRenderer                  — SmartArt diagrams → SVG
```
