# Language overlays for the i18n translation reference

One file per language, `<lang>.yaml`, keyed by the concept id of the OpenOlat concept map.

## What an overlay is

`../i18n-translation-reference.yaml` takes its translations from the
`LocalStrings_<lang>.properties` files in the source tree. A term that no properties file
carries yet has no translation there.

An overlay closes that gap. It holds the terms a native speaker decided on a translation
worksheet before the strings existed in the code. The generator merges the overlay over the
code values and marks every merged value in the reference:

| Marker | Meaning |
|---|---|
| `<lang>_source: proposed` | The value comes from the overlay, not from the code |
| `<lang>_source: proposed_unsure` | The same, and the speaker asked for a review |
| no marker | The value comes from `LocalStrings_<lang>.properties` |

The overlay wins over the properties files. It is the decided term; the code is the state of
the port.

## Fields

| Field | Meaning |
|---|---|
| `<lang>` | The decided translation |
| `status` | `new`, `ok` or `unsure`, as the speaker marked the row |
| `replaces_in_code` | The properties value this entry corrects |
| `worksheet_id` | The concept id on the worksheet, when the concept has been renamed since |

## Do not edit by hand

Both this file set and the reference are generated. To change a translation, refill the
worksheet and import it again:

```bash
cd fxIntelligence/knowledge/openolat/concept-map
python3 scripts/15_gen_language_worksheet.py --lang <code>     # produce the worksheet
python3 scripts/16_import_language_worksheet.py --lang <code> --file <filled.xlsx>
python3 scripts/14_gen_translation_reference.py                # rebuild the reference
```

## Retiring an overlay entry

When a translation reaches `LocalStrings_<lang>.properties`, delete its entry from the
overlay and rebuild. The reference then reads the value from the code and the `proposed`
marker disappears.
