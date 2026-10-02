# OpenOlat taxonomy export

This ZIP contains a complete taxonomy of OpenOlat: the metadata of the taxonomy, the
level types, the levels with their translations, and the teaser and background images.
You can import the ZIP into the same or into another OpenOlat. You can also edit the
file `taxonomy.xlsx` and add images before you import it.

Not part of the export: the lost+found, the management users of the taxonomy, the
competences, the relations to courses, questions and other objects, and the documents
of the document pool.


## 1. Content of the ZIP

```
taxonomy.xlsx                          the taxonomy (3 sheets, see section 4)
readme.md                              this file
media/MATH/teaser/math.png             teaser image of the level /MATH/
media/MATH/background/formula.jpg      background image of the level /MATH/
media/MATH/GEO/teaser/triangle.png     teaser image of the level /MATH/GEO/
```

The workbook has three sheets, always in this order. The names of the sheets depend on
the language of the export (e.g. "Ebenen" in German); the import uses the order, not
the names.

1. **Levels**: one row per taxonomy level
2. **Level types**: one row per level type
3. **Information**: the metadata of the taxonomy and of the export


## 2. Import

Import in OpenOlat under **Administration > Modules > Taxonomy**:

* **Import taxonomy** below the list of taxonomies creates a new taxonomy. The reference
  (identifier) of the taxonomy comes from the file. If another taxonomy already uses it,
  OpenOlat suggests the reference with a suffix, e.g. `QPOOL-2`.
* **More > Import taxonomy levels** in a taxonomy adds levels to this taxonomy or updates
  its levels. In the last step you select the import mode:
  * **Update only**: existing level types and levels get the values of the file (images,
    titles, translations, descriptions, ...). New rows are not created.
  * **Add new only**: new level types and levels are created. Existing ones stay unchanged.
  * **Add new and update existing**: both.

The import never deletes a level type or a level. A row that you remove from the file
stays in the taxonomy.

The import accepts:

* this ZIP (with the images),
* the file `taxonomy.xlsx` alone (without images),
* a CSV file with the columns of the sheet "Levels" (without images, see section 6).

Before the import, OpenOlat shows every row with its status (new, changed, unchanged,
error). Click **Changed** to see the values that change. Rows with an error and the levels
below them are not imported; the import continues with the other rows.


## 3. Edit the file: step by step

Edit `taxonomy.xlsx` in Excel, LibreOffice or Numbers. Keep the order of the columns and
the header row.

**Important: format all cells as text** before you type numbers. Otherwise the program
changes the values: the reference `0042` becomes `42`, the sort order `4.30` becomes
`4.3`, and `1/2` becomes a date.

### 3.1 Add a level

1. Add a row to the sheet "Levels".
2. Column A (path): write the references from the top level to the new level, each
   between `/`. Example: `/MATH/` for a top level, `/MATH/GEO/` for a level below MATH.
3. Column B (reference): write the last part of the path, e.g. `GEO`.
4. Columns I, J, K: write the language key, the title and the description, e.g.
   `en`, `Geometry`, `<p>Shapes and spaces</p>`.

The parent level (here `/MATH/`) must exist in the taxonomy or in the file. The order of
the rows does not matter.

### 3.2 Add a translation

Every level has groups of three columns: language, title, description. The export writes
one group per language. To add a language, add three columns at the end of the sheet
with the language key (e.g. `fr`), the title and the description, in every row that you
want to translate.

A group with an empty title and an empty description is ignored. Use the language keys
of OpenOlat: `de`, `en`, `fr`, `it`, ... A language that is not enabled in the target
OpenOlat is ignored with a warning.

### 3.3 Add or replace an image

A level can have a teaser image (column G) and a background image (column H). The cell
holds the path of the image file in the ZIP.

1. Unpack the ZIP into a folder.
2. Copy the image into the folder `media`. The import does not require a fixed
   structure, but this one keeps the ZIP clear:
   `media/<path of the level>/teaser/<file>`, e.g. `media/MATH/GEO/teaser/triangle.png`.
3. Write the path in the row of the level: column G for the teaser image, column H for
   the background image. The path starts below the folder that contains `taxonomy.xlsx`
   and uses `/`. Example: `media/MATH/GEO/teaser/triangle.png`
4. Save `taxonomy.xlsx` and pack the folder again (see 3.5).

To replace an image, overwrite the file or write the path of the new file. To keep the
current image of an existing level, leave the cell empty.

Limits: teaser image max. 2 MB, background image max. 5 MB, image formats only (PNG,
JPEG, GIF, ...). Several levels can use the same image file.

### 3.4 Add a level type

1. Add a row to the sheet "Level types": reference (column A) and title (column B).
2. Write the reference of the type in column D of the levels of this type.
3. Optional: in column I (allowed sub-types), write the references of the types that are
   allowed below this type, one per line in the cell (Alt+Enter in Excel).

A type that a level uses but that is not in the sheet "Level types" and not in the
target taxonomy is created with its reference as title.

### 3.5 Pack the ZIP

Pack the folder with `taxonomy.xlsx` and the folder `media` into a ZIP:

* Windows: right-click the folder > **Send to > Compressed (zipped) folder**.
* macOS: right-click the folder > **Compress**.

`taxonomy.xlsx` can be at the top of the ZIP or in one folder below. The paths of the
images in columns G and H are relative to the folder of `taxonomy.xlsx`.


## 4. Column reference

### Sheet "Levels"

Columns marked with `*` are mandatory.

| Column | Content |
|---|---|
| A Path * | References from the top level to the level, separated by `/`, e.g. `/MATH/GEO/`. Write `\/` for a slash and `\\` for a backslash inside a reference. |
| B Reference * | Reference (identifier) of the level. Must be the last part of the path. |
| C External ID | ID of an external system. Imported as it is. |
| D Type | Reference of a level type of the sheet "Level types" or of the target taxonomy. |
| E Sort order | Integer, or empty. |
| F Managed flags | Fields that an external system manages, comma separated, e.g. `identifier,displayName`. |
| G Teaser image | Path of the image in the ZIP, e.g. `media/MATH/teaser/math.png` (see 3.3). |
| H Background image | Path of the image in the ZIP (see 3.3). |
| I, J, K | First language group: language key, title, description (HTML). |
| L, M, N, ... | More language groups. |

### Sheet "Level types"

| Column | Content |
|---|---|
| A Reference * | Reference (identifier) of the type |
| B Title * | |
| C Description | HTML |
| D External ID | Imported as it is |
| E CSS class | |
| F Visible | `ON` or `OFF` |
| G Evidence of achievement | `ON` or `OFF` |
| H Competences | `ON` or `OFF` |
| I Sub types | References of the allowed sub-types, one per line |
| J Managed flags | Comma separated |
| K to Q | Settings of the document pool: `ON` or `OFF`; column N is the number of levels above that a teacher may read |

### Sheet "Information"

Column A holds a fixed key, column B the label, column C the value. Do not change
column A.

* `taxonomy.identifier`, `taxonomy.displayname`, `taxonomy.description`,
  `taxonomy.externalid`, `taxonomy.managedflags`: the metadata of the taxonomy. Used
  when the import creates a new taxonomy. An import into an existing taxonomy never
  changes its title, description or external ID.
* `export.*` and `taxonomy.usage`: information about the export, not imported. After the
  import of a new taxonomy, activate it in the modules (catalog, question bank, ...).


## 5. Rules

* A new level needs a title in at least one language.
* Two rows with the same path are an error.
* The reference in column B must be the last part of the path in column A.
* External IDs come from external systems and are imported as they are. If two levels
  (or two level types) of a taxonomy have the same external ID, the import shows a
  warning: this is most probably an error in the data.
* Managed flags protect the fields of existing levels: the import does not change a
  managed field, and it does not change the managed flags of existing levels.


## 6. CSV

A CSV file has the columns of the sheet "Levels" (A to K and more), with or without
header row. It imports levels only: no level types (an unknown type is created), no
images, no taxonomy metadata.

* Encoding UTF-8.
* Separator `;`, `,` or tab (detected from the first row).
* Put a value in quotes if it contains the separator, a quote or a line break, and
  double the quotes inside: `"Wirtschaft, Recht"`, `"Das ""Beste"""`.


## 7. Common errors

| Message | Solution |
|---|---|
| The parent level ... does not exist. | Add a row for the parent level, or check the spelling of the path. |
| The path must end with the reference ... | Column B must be the last part of column A, e.g. `GEO` for `/MATH/GEO/`. |
| A new level needs a title in at least one language. | Fill in the title of at least one language group. |
| The image ... is not in the ZIP. | Check the path in column G or H: it is relative to the folder of `taxonomy.xlsx` and case sensitive. |
| ... is not an integer. | Format the cell as text and write the number again. |
| The ZIP does not contain an XLSX or CSV file. | Put `taxonomy.xlsx` at the top of the ZIP or in one folder below. |
