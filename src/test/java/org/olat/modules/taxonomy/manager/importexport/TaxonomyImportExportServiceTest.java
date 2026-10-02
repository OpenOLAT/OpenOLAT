/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.taxonomy.manager.importexport;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.Logger;
import org.dhatim.fastexcel.reader.ReadableWorkbook;
import org.dhatim.fastexcel.reader.Row;
import org.dhatim.fastexcel.reader.Sheet;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.filter.FilterFactory;
import org.olat.core.util.i18n.I18nItem;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.i18n.I18nModule;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyLevelManagedFlag;
import org.olat.modules.taxonomy.TaxonomyLevelType;
import org.olat.modules.taxonomy.TaxonomyLevelTypeManagedFlag;
import org.olat.modules.taxonomy.TaxonomyManagedFlag;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.manager.importexport.FileRow.Status;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Round trip of the taxonomy import/export: export a complex taxonomy, import it
 * as a new taxonomy, export the new taxonomy and compare the two exports.
 *
 * Initial date: 30 Sept 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 */
public class TaxonomyImportExportServiceTest extends OlatTestCase {

	private static final Logger log = Tracing.createLoggerFor(TaxonomyImportExportServiceTest.class);

	private static final String[] STRANGE_TITLES = {
		"Deutsch – Französisch",
		"Café d’été € 5",
		"Das \"Beste\" & mehr",
		"Rechnen = Mathe",
		"Wirtschaft, Recht; Politik",
		"Modul: Grundlagen?",
		"Deutsch/Englisch \\ Backslash",
		"<b>kein HTML</b> ]]> <![CDATA[ x",
		"Smiley ☺ und 中文",
		"${not.a.key} {0} 'apostrophe' $org.olat.core:ok",
		"|pipe| *star* `backtick` #hash %percent"
	};

	@Autowired
	private DB dbInstance;
	@Autowired
	private I18nModule i18nModule;
	@Autowired
	private I18nManager i18nManager;
	@Autowired
	private TaxonomyService taxonomyService;
	@Autowired
	private TaxonomyImportExportService importExportService;

	private final List<File> tmpFiles = new ArrayList<>();

	@After
	public void cleanUpFiles() {
		for(File file:tmpFiles) {
			FileUtils.deleteDirsAndFiles(file, true, true);
		}
	}

	/**
	 * The definition of done of OO-9860.
	 */
	@Test
	public void roundTrip() throws Exception {
		Identity doer = JunitTestHelper.createAndPersistIdentityAsRndUser("tax-importexport-1");
		Taxonomy taxonomy = createComplexTaxonomy(doer);

		File export1 = export(taxonomy, doer);

		TaxonomyFileData data = importExportService.read(export1, export1.getName());
		importExportService.validate(data, null);
		assertNoErrors(data);
		Assert.assertTrue(data.getLevels().stream().allMatch(level -> level.getStatus() == Status.created));

		TaxonomyImportResult result = importExportService.importData(data, null,
				new TaxonomyImportOptions(null, null, TaxonomyImportMode.addNewAndUpdateExisting, doer));
		data.cleanUp();
		dbInstance.commitAndCloseSession();

		Taxonomy importedTaxonomy = result.getTaxonomy();
		Assert.assertNotEquals(taxonomy.getKey(), importedTaxonomy.getKey());
		// same instance: the reference of the original is taken, the import adds a suffix;
		// the external ID is never edited in OpenOlat and taken as it is
		Assert.assertEquals(taxonomy.getIdentifier() + "-2", importedTaxonomy.getIdentifier());
		Assert.assertEquals(taxonomy.getExternalId(), importedTaxonomy.getExternalId());
		Assert.assertEquals(taxonomyService.getTaxonomyLevels(taxonomy).size(), result.getCreatedLevels());
		Assert.assertEquals(taxonomyService.getTaxonomyLevelTypes(taxonomy).size(), result.getCreatedTypes());

		File export2 = export(importedTaxonomy, doer);
		assertSameExport(export1, export2);
	}

	/**
	 * Import the export into the same taxonomy: all rows are unchanged.
	 */
	@Test
	public void reimportUnchanged() throws Exception {
		Identity doer = JunitTestHelper.createAndPersistIdentityAsRndUser("tax-importexport-2");
		Taxonomy taxonomy = createComplexTaxonomy(doer);
		File export = export(taxonomy, doer);

		TaxonomyFileData data = importExportService.read(export, export.getName());
		importExportService.validate(data, taxonomy);
		assertNoErrors(data);
		for(FileLevelType type:data.getLevelTypes()) {
			Assert.assertEquals("Type " + type.getIdentifier(), Status.unchanged, type.getStatus());
		}
		for(FileLevel level:data.getLevels()) {
			// same images are unchanged too
			Assert.assertEquals("Level " + level.getPath(), Status.unchanged, level.getStatus());
		}
		data.cleanUp();
	}

	/**
	 * The example of the import wizard can be imported without errors.
	 */
	@Test
	public void exampleWorkbook() throws Exception {
		File dir = FileUtils.createTempDir("tax-example", null, new File(WebappHelper.getTmpDir()));
		tmpFiles.add(dir);
		File file = new File(dir, "taxonomy_example.xlsx");
		try(OutputStream out = Files.newOutputStream(file.toPath())) {
			new TaxonomyFileWriter(Util.createPackageTranslator(TaxonomyUIFactory.class, Locale.ENGLISH))
				.writeWorkbook(importExportService.createExampleData(), out);
		}

		TaxonomyFileData data = importExportService.read(file, file.getName());
		importExportService.validate(data, null);
		assertNoErrors(data);
		Assert.assertEquals(2, data.getLevelTypes().size());
		Assert.assertEquals(3, data.getLevels().size());
		Assert.assertEquals("Mathematics", data.getLevels().get(0).getTranslation("en").title());
	}

	/**
	 * The three import modes: add new only, update only, add new and update existing.
	 * Every file contains a changed existing level and a new level.
	 */
	@Test
	public void importModes() throws Exception {
		Identity doer = JunitTestHelper.createAndPersistIdentityAsRndUser("tax-importexport-3");
		Taxonomy taxonomy = createComplexTaxonomy(doer);

		// add new only: the new level is created, the existing level is unchanged
		TaxonomyFileData data = loadChanged(taxonomy, doer, "/media/teaser/", "NEW1");
		// the review shows what changes: sort order and English title
		FileLevel changed = data.getLevels().stream().filter(level -> "/media/teaser/".equals(level.getPath())).findFirst().get();
		Assert.assertEquals(FileRow.Status.changed, changed.getStatus());
		Assert.assertTrue(changed.getChanges().contains(new FileChange("importexport.level.sort.order", null, null, "99", false)));
		Assert.assertTrue(changed.getChanges().stream()
				.anyMatch(change -> "importexport.level.title".equals(change.column()) && "en".equals(change.language())
						&& "Changed title".equals(change.after())));
		Assert.assertEquals(2, changed.getChanges().size());
		TaxonomyImportResult added = importExportService.importData(data, taxonomy,
				new TaxonomyImportOptions(null, null, TaxonomyImportMode.addNew, doer));
		dbInstance.commitAndCloseSession();
		Assert.assertEquals(1, added.getCreatedLevels());
		Assert.assertEquals(0, added.getUpdatedLevels());
		Assert.assertNull(getLevel(taxonomy, "teaser").getSortOrder());

		// update only: the existing level is updated, the new level is not created
		data = loadChanged(taxonomy, doer, "/media/teaser/", "NEW2");
		TaxonomyImportResult updated = importExportService.importData(data, taxonomy,
				new TaxonomyImportOptions(null, null, TaxonomyImportMode.updateExisting, doer));
		dbInstance.commitAndCloseSession();
		Assert.assertEquals(0, updated.getCreatedLevels());
		Assert.assertEquals(1, updated.getUpdatedLevels());
		Assert.assertTrue(taxonomyService.getTaxonomyLevels(taxonomy).stream().noneMatch(level -> "NEW2".equals(level.getIdentifier())));
		Assert.assertEquals(Integer.valueOf(99), getLevel(taxonomy, "teaser").getSortOrder());
		Assert.assertEquals("Changed title", importExportService.loadExportData(taxonomy, doer, Locale.ENGLISH).getLevels().stream()
				.filter(level -> "/media/teaser/".equals(level.getPath())).findFirst().get().getTranslation("en").title());

		// both
		data = loadChanged(taxonomy, doer, "/media/=SUM(1;2)/", "NEW3");
		TaxonomyImportResult both = importExportService.importData(data, taxonomy,
				new TaxonomyImportOptions(null, null, TaxonomyImportMode.addNewAndUpdateExisting, doer));
		dbInstance.commitAndCloseSession();
		Assert.assertEquals(1, both.getCreatedLevels());
		Assert.assertEquals(1, both.getUpdatedLevels());
		Assert.assertEquals(Integer.valueOf(99), getLevel(taxonomy, "=SUM(1;2)").getSortOrder());
	}

	/**
	 * Managed flags of existing levels are kept and protect their fields.
	 */
	@Test
	public void managedFlagsKeptOnUpdate() throws Exception {
		Identity doer = JunitTestHelper.createAndPersistIdentityAsRndUser("tax-importexport-4");
		Taxonomy taxonomy = createComplexTaxonomy(doer);

		// MATH is managed: identifier, displayName
		TaxonomyFileData data = loadChanged(taxonomy, doer, "/MATH/");
		FileLevel math = data.getLevels().stream().filter(level -> "/MATH/".equals(level.getPath())).findFirst().get();
		math.setManagedFlags(null);
		importExportService.validate(data, taxonomy);
		importExportService.importData(data, taxonomy, new TaxonomyImportOptions(null, null, TaxonomyImportMode.addNewAndUpdateExisting, doer));
		dbInstance.commitAndCloseSession();

		TaxonomyLevel reloaded = getLevel(taxonomy, "MATH");
		Assert.assertTrue(TaxonomyLevelManagedFlag.isManaged(reloaded, TaxonomyLevelManagedFlag.displayName));
		// sort order is not managed: updated, title is managed: unchanged
		Assert.assertEquals(Integer.valueOf(99), reloaded.getSortOrder());
		FileLevel exported = importExportService.loadExportData(taxonomy, doer, Locale.ENGLISH).getLevels().stream()
				.filter(level -> "/MATH/".equals(level.getPath())).findFirst().get();
		Assert.assertNotEquals("Changed title", exported.getTranslation("en").title());
	}

	/**
	 * The lost+found levels of the document pool are not exported.
	 */
	@Test
	public void lostAndFoundNotExported() throws Exception {
		Identity doer = JunitTestHelper.createAndPersistIdentityAsRndUser("tax-importexport-5");
		Taxonomy taxonomy = taxonomyService.createTaxonomy("LF-" + UUID.randomUUID().toString().substring(0, 8), "Lost", null, null);
		TaxonomyLevel lostAndFound = taxonomyService.createTaxonomyLevel("lost+found", taxonomyService.createI18nSuffix(), null, null, null, taxonomy);
		taxonomyService.createTaxonomyLevel("orphan", taxonomyService.createI18nSuffix(), null, null, lostAndFound, taxonomy);
		taxonomyService.createTaxonomyLevel("kept", taxonomyService.createI18nSuffix(), null, null, null, taxonomy);
		dbInstance.commitAndCloseSession();

		List<String> paths = importExportService.loadExportData(taxonomy, doer, Locale.ENGLISH).getLevels().stream()
				.map(FileLevel::getPath).toList();
		Assert.assertEquals(List.of("/kept/"), paths);
	}

	@Test
	public void normalizeEntryName() {
		Assert.assertEquals("base/media/a.png", TaxonomyFileReader.normalizeEntryName("base/", "media/a.png"));
		Assert.assertEquals("base/media/a.png", TaxonomyFileReader.normalizeEntryName("base/", "./media/a.png"));
		Assert.assertEquals("base/media/a.png", TaxonomyFileReader.normalizeEntryName("base/", "media\\a.png"));
		Assert.assertNull(TaxonomyFileReader.normalizeEntryName("base/", "../a.png"));
		Assert.assertNull(TaxonomyFileReader.normalizeEntryName("base/", "media/../../a.png"));
		Assert.assertNull(TaxonomyFileReader.normalizeEntryName("base/", "/etc/passwd"));
	}

	/**
	 * A ZIP of the format version 1 (CSV rows and media/<identifiers>/teaser/) and an
	 * image larger than the limit.
	 */
	@Test
	public void legacyZipMediaAndOversizeImage() throws Exception {
		File dir = FileUtils.createTempDir("tax-zip", null, new File(WebappHelper.getTmpDir()));
		tmpFiles.add(dir);
		File zip = new File(dir, "legacy.zip");
		try(java.util.zip.ZipOutputStream zout = new java.util.zip.ZipOutputStream(Files.newOutputStream(zip.toPath()))) {
			zout.putNextEntry(new ZipEntry("Export/levels.csv"));
			zout.write("/MATH/\tMATH\t\t\tDE\tMathematik\t\n/BIG/\tBIG\t\t\tDE\tGross\t\n".getBytes(StandardCharsets.UTF_8));
			zout.closeEntry();
			zout.putNextEntry(new ZipEntry("Export/media/MATH/teaser/math.png"));
			zout.write(Files.readAllBytes(createImage(Color.RED).toPath()));
			zout.closeEntry();
			zout.putNextEntry(new ZipEntry("Export/media/BIG/teaser/big.png"));
			zout.write(new byte[(int)TaxonomyFileReader.MAX_TEASER_SIZE + 10]);
			zout.closeEntry();
		}

		TaxonomyFileData data = importExportService.read(zip, zip.getName());
		Assert.assertEquals(1, data.getFormatVersion());
		FileLevel math = data.getLevels().get(0);
		Assert.assertNotNull(math.getTeaserImage());
		Assert.assertNotNull(math.getTeaserImage().file());
		Assert.assertTrue(math.getTeaserImage().file().length() > 0);
		FileLevel big = data.getLevels().get(1);
		Assert.assertTrue(big.getMessages().stream().anyMatch(message -> "importexport.error.media.size".equals(message.i18nKey())));
		data.cleanUp();
	}

	private TaxonomyFileData loadChanged(Taxonomy taxonomy, Identity doer, String path) {
		return loadChanged(taxonomy, doer, path, null);
	}

	private TaxonomyFileData loadChanged(Taxonomy taxonomy, Identity doer, String path, String newIdentifier) {
		TaxonomyFileData data = importExportService.loadExportData(taxonomy, doer, Locale.ENGLISH);
		if(newIdentifier != null) {
			FileLevel newLevel = new FileLevel();
			newLevel.setPath("/" + newIdentifier + "/");
			newLevel.setIdentifier(newIdentifier);
			newLevel.putTranslation("en", "New " + newIdentifier, null);
			data.getLevels().add(newLevel);
		}
		FileLevel level = data.getLevels().stream().filter(l -> path.equals(l.getPath())).findFirst().get();
		level.setSortOrder("99");
		FileTranslation en = level.getTranslation("en");
		level.putTranslation("en", "Changed title", en == null ? null : en.description());
		importExportService.validate(data, taxonomy);
		return data;
	}

	private TaxonomyLevel getLevel(Taxonomy taxonomy, String identifier) {
		return taxonomyService.getTaxonomyLevels(taxonomy).stream()
				.filter(level -> identifier.equals(level.getIdentifier()))
				.findFirst().get();
	}

	/**
	 * An import never creates a second taxonomy with the same reference: QPOOL
	 * and QPOOL-2 are used, the import suggests QPOOL-3. The suffix respects the
	 * maximum length.
	 */
	@Test
	public void uniqueIdentifier() {
		String base = "Q" + UUID.randomUUID().toString().substring(0, 8);
		Assert.assertEquals(base, importExportService.getUniqueTaxonomyIdentifier(base));
		taxonomyService.createTaxonomy(base, "Question pool", null, base);
		taxonomyService.createTaxonomy(base + "-2", "Question pool copy", null, base);
		dbInstance.commitAndCloseSession();

		Assert.assertTrue(importExportService.isTaxonomyIdentifierUsed(base));
		Assert.assertTrue(importExportService.isTaxonomyIdentifierUsed(base + "-2"));
		Assert.assertEquals(base + "-3", importExportService.getUniqueTaxonomyIdentifier(base));

		String longId = ("L" + UUID.randomUUID().toString().replace("-", "") + "x".repeat(40))
				.substring(0, TaxonomyImportExportService.MAX_TAXONOMY_IDENTIFIER);
		taxonomyService.createTaxonomy(longId, "Long", null, null);
		dbInstance.commitAndCloseSession();
		String uniqueLongId = importExportService.getUniqueTaxonomyIdentifier(longId);
		Assert.assertEquals(TaxonomyImportExportService.MAX_TAXONOMY_IDENTIFIER, uniqueLongId.length());
		Assert.assertTrue(uniqueLongId.endsWith("-2"));
	}

	@Test
	public void readCsv() throws Exception {
		String csv = "\uFEFFPath;Identifier;External ID;Type;Sort order;Managed flags;Teaser image;Background image;Language;Title;Description\n"
				+ "/CSV1/;CSV1;EXT-1;Subject;1;;;;de;\"Deutsch; mit \"\"Anf\u00fchrung\"\"\";\"<p>Zeile 1\nZeile 2</p>\"\n"
				+ "/CSV1/CSV2/;CSV2;;;;;;;EN;Title 2;\n"
				+ "/CSV1/CSV3/;CSV3;;;;;;;xx;Unknown language;\n";
		File file = writeTmpFile("levels.csv", csv);
		TaxonomyFileData data = importExportService.read(file, file.getName());
		Assert.assertFalse(data.hasErrors());
		Assert.assertEquals(3, data.getLevels().size());

		FileLevel level1 = data.getLevels().get(0);
		Assert.assertEquals("CSV1", level1.getIdentifier());
		Assert.assertEquals("EXT-1", level1.getExternalId());
		Assert.assertEquals("1", level1.getSortOrder());
		Assert.assertEquals("Deutsch; mit \"Anf\u00fchrung\"", level1.getTranslation("de").title());
		Assert.assertEquals("<p>Zeile 1\nZeile 2</p>", level1.getTranslation("de").description());

		importExportService.validate(data, null);
		assertNoErrors(data);
		// language key normalized to the enabled key
		Assert.assertNotNull(data.getLevels().get(1).getTranslation("en"));
		// unknown language: warning, level without title: error
		FileLevel level3 = data.getLevels().get(2);
		Assert.assertTrue(level3.hasWarnings());
		Assert.assertTrue(level3.hasErrors());
		// implicit type
		FileLevelType implicitType = data.getLevelType("Subject");
		Assert.assertNotNull(implicitType);
		Assert.assertTrue(implicitType.isImplicit());
	}

	/**
	 * External IDs come from external systems and are imported as they are. A
	 * duplicate within the taxonomy (in the file or with the target) is a warning.
	 */
	@Test
	public void duplicateExternalIdWarning() throws Exception {
		Taxonomy taxonomy = taxonomyService.createTaxonomy("DUP-" + UUID.randomUUID().toString().substring(0, 8), "Duplicates", null, null);
		taxonomyService.createTaxonomy("DUP-OTHER", "Other", null, null);
		TaxonomyLevel existing = taxonomyService.createTaxonomyLevel("EXISTING", taxonomyService.createI18nSuffix(), "EXT-T", null, null, taxonomy);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(existing);

		String csv = """
				Path,Identifier,External ID,Type,Sort order,Managed flags,Teaser image,Background image,Language,Title,Description
				/A/,A,EXT-F,,,,,,en,A,
				/B/,B,EXT-F,,,,,,en,B,
				/C/,C,EXT-T,,,,,,en,C,
				/D/,D,EXT-D,,,,,,en,D,
				/EXISTING/,EXISTING,EXT-T,,,,,,en,Existing,
				""";
		File file = writeTmpFile("duplicates.csv", csv);
		TaxonomyFileData data = importExportService.read(file, file.getName());
		importExportService.validate(data, taxonomy);
		assertNoErrors(data);

		List<FileLevel> levels = data.getLevels();
		Assert.assertTrue("duplicate in the file", hasWarning(levels.get(0), "importexport.warning.external.id.duplicate"));
		Assert.assertTrue("duplicate in the file", hasWarning(levels.get(1), "importexport.warning.external.id.duplicate"));
		Assert.assertTrue("duplicate with the target", hasWarning(levels.get(2), "importexport.warning.external.id.duplicate"));
		Assert.assertFalse("unique", hasWarning(levels.get(3), "importexport.warning.external.id.duplicate"));
		// the existing level itself is not a duplicate of itself, but of the row C of the file
		Assert.assertTrue(hasWarning(levels.get(4), "importexport.warning.external.id.duplicate"));
		// imported as it is
		Assert.assertEquals("EXT-F", levels.get(1).getExternalId());
	}

	private boolean hasWarning(FileRow row, String i18nKey) {
		return row.getMessages().stream().anyMatch(message -> !message.isError() && i18nKey.equals(message.i18nKey()));
	}

	/**
	 * Rows copied from the export and template of the format version 1: four
	 * fixed columns, then language groups. Empty groups are skipped.
	 */
	@Test
	public void readLegacyCsv() throws Exception {
		String csv = """
				/MATH/\tMATH\tF\t1\tDE\tMathematik\tMathematik ist wichtig.\tEN\tMathematics\t
				/MATH/GEO/\tGEO\t\t\tDE\t\t\tEN\tGeometry\t
				""";
		File file = writeTmpFile("legacy.csv", csv);
		TaxonomyFileData data = importExportService.read(file, file.getName());
		Assert.assertEquals(1, data.getFormatVersion());
		importExportService.validate(data, null);
		assertNoErrors(data);

		FileLevel math = data.getLevels().get(0);
		Assert.assertEquals("F", math.getType());
		Assert.assertEquals("1", math.getSortOrder());
		Assert.assertEquals("Mathematik", math.getTranslation("de").title());
		Assert.assertEquals("Mathematics", math.getTranslation("en").title());

		FileLevel geo = data.getLevels().get(1);
		Assert.assertNull(geo.getTranslation("de"));
		Assert.assertEquals("Geometry", geo.getTranslation("en").title());
	}

	@Test
	public void validateStrangeTitles() throws Exception {
		StringBuilder csv = new StringBuilder("Path,Identifier,External ID,Type,Sort order,Managed flags,Teaser image,Background image,Language,Title,Description\n");
		for(int i=0; i<STRANGE_TITLES.length; i++) {
			csv.append("/S").append(i).append("/,S").append(i).append(",,,,,,,de,\"")
				.append(STRANGE_TITLES[i].replace("\"", "\"\"")).append("\",\n");
		}
		File file = writeTmpFile("strange.csv", csv.toString());
		TaxonomyFileData data = importExportService.read(file, file.getName());
		importExportService.validate(data, null);
		assertNoErrors(data);
		for(int i=0; i<STRANGE_TITLES.length; i++) {
			Assert.assertEquals(STRANGE_TITLES[i], data.getLevels().get(i).getTranslation("de").title());
		}
	}

	@Test
	public void validateErrors() throws Exception {
		String csv = """
				Path,Identifier,External ID,Type,Sort order,Managed flags,Teaser image,Background image,Language,Title,Description
				/A/,A,,,,,,,en,A,
				/A/,A,,,,,,,en,A again,
				/B/,X,,,,,,,en,B,
				/C/D/,D,,,,,,,en,D,
				/E/,E,,,abc,,,,en,E,
				/F/,F,,,,unknownFlag,,,en,F,
				""";
		File file = writeTmpFile("errors.csv", csv);
		TaxonomyFileData data = importExportService.read(file, file.getName());
		importExportService.validate(data, null);
		List<FileLevel> levels = data.getLevels();
		Assert.assertFalse(levels.get(0).getMessages().toString(), levels.get(0).hasErrors());
		Assert.assertTrue("duplicate path", levels.get(1).hasErrors());
		Assert.assertTrue("identifier not in path", levels.get(2).hasErrors());
		Assert.assertTrue("parent missing", levels.get(3).hasErrors());
		Assert.assertTrue("sort order", levels.get(4).hasErrors());
		Assert.assertFalse("unknown flag is a warning", levels.get(5).hasErrors());
		Assert.assertTrue("unknown flag is a warning", levels.get(5).hasWarnings());
	}

	// ------------------------------------------------------------------------

	private Taxonomy createComplexTaxonomy(Identity doer) throws IOException {
		String random = UUID.randomUUID().toString().substring(0, 8);
		Taxonomy taxonomy = taxonomyService.createTaxonomy("EXCH-" + random, "Taxonomie – \"Export\" & Import",
				"<p>Beschreibung mit <strong>HTML</strong> &amp; Umlaut ä</p>", "EXT-" + random);
		taxonomy.setManagedFlags(new TaxonomyManagedFlag[] { TaxonomyManagedFlag.externalId });
		taxonomy = taxonomyService.updateTaxonomy(taxonomy);

		// Types
		TaxonomyLevelType subject = taxonomyService.createTaxonomyLevelType("Fach", "Fach – ÄÖÜ & Co",
				"<p>Typ Beschreibung</p>", "T-EXT-1", true, taxonomy);
		subject.setCssClass("o_subject");
		subject.setAllowedAsSubject(true);
		subject.setManagedFlags(new TaxonomyLevelTypeManagedFlag[] { TaxonomyLevelTypeManagedFlag.cssClass, TaxonomyLevelTypeManagedFlag.delete });
		subject = taxonomyService.updateTaxonomyLevelType(subject);
		TaxonomyLevelType domain = taxonomyService.createTaxonomyLevelType("Domäne, 1", "Domäne", null, null, false, taxonomy);
		domain.setVisible(false);
		domain.setDocumentsLibraryEnabled(true);
		domain.setDocumentsLibraryManageCompetenceEnabled(true);
		domain.setDocumentsLibraryTeachCompetenceReadEnabled(true);
		domain.setDocumentsLibraryTeachCompetenceReadParentLevels(3);
		domain.setDocumentsLibraryTeachCompetenceWriteEnabled(true);
		domain.setDocumentsLibraryHaveCompetenceReadEnabled(true);
		domain.setDocumentsLibraryTargetCompetenceReadEnabled(true);
		domain = taxonomyService.updateTaxonomyLevelType(domain);
		TaxonomyLevelType competence = taxonomyService.createTaxonomyLevelType("Kompetenz/\"A\"", "Kompetenz", null, null, true, taxonomy);
		competence.setAllowedAsSubject(false);
		competence = taxonomyService.updateTaxonomyLevelType(competence);
		taxonomyService.createTaxonomyLevelType("unused", "Unused type", null, null, true, taxonomy);
		subject = taxonomyService.updateTaxonomyLevelType(subject, List.of(domain, competence));
		domain = taxonomyService.updateTaxonomyLevelType(domain, List.of(competence));
		dbInstance.commitAndCloseSession();

		// Levels
		List<String> languages = getLanguages();
		TaxonomyLevel math = createLevel("MATH", null, subject, 1, taxonomy, languages, 0);
		TaxonomyLevel mathLower = createLevel("math", null, null, 2, taxonomy, languages, 1);
		TaxonomyLevel geo = createLevel("Géo & Co", math, domain, null, taxonomy, languages, 2);
		TaxonomyLevel geo2 = createLevel("Geo & Co", math, null, 3, taxonomy, languages, 3);
		TaxonomyLevel slash = createLevel("a/b", geo, competence, -5, taxonomy, languages, 4);
		createLevel("back\\slash", slash, null, null, taxonomy, languages, 5);
		createLevel("0042", math, null, 10, taxonomy, languages, 6);
		createLevel("4.30", math, null, null, taxonomy, languages, 7);
		TaxonomyLevel media = createLevel("media", null, null, null, taxonomy, languages, 8);
		createLevel("teaser", media, null, null, taxonomy, languages, 9);
		createLevel("=SUM(1;2)", media, null, null, taxonomy, languages, 10);
		createLevel("Smiley ☺", media, null, null, taxonomy, languages, 11);
		createLevel("C++ (Level 1) [x]", media, null, null, taxonomy, languages, 12);
		dbInstance.commitAndCloseSession();

		// Managed flags and external IDs
		math = taxonomyService.getTaxonomyLevel(math);
		math.setManagedFlags(new TaxonomyLevelManagedFlag[] { TaxonomyLevelManagedFlag.identifier, TaxonomyLevelManagedFlag.displayName });
		math.setExternalId("MATH-EXT");
		math = taxonomyService.updateTaxonomyLevel(math);

		// Images: teaser and background with names that must be cleaned
		taxonomyService.storeTeaserImage(math, doer, createImage(Color.RED), "Bild mit Umlaut ä & Co.png");
		taxonomyService.storeBackgroundImage(math, doer, createImage(Color.BLUE), "background.jpg");
		taxonomyService.storeTeaserImage(geo, doer, createImage(Color.GREEN), "geo.png");
		taxonomyService.storeTeaserImage(taxonomyService.getTaxonomyLevel(slash), doer, createImage(Color.YELLOW), "slash.png");
		// same media folder after normalization: Geo__Co, math (case insensitive)
		taxonomyService.storeTeaserImage(geo2, doer, createImage(Color.BLACK), "geo.png");
		taxonomyService.storeBackgroundImage(mathLower, doer, createImage(Color.WHITE), "math.png");
		dbInstance.commitAndCloseSession();
		return taxonomy;
	}

	private TaxonomyLevel createLevel(String identifier, TaxonomyLevel parent, TaxonomyLevelType type, Integer sortOrder,
			Taxonomy taxonomy, List<String> languages, int index) {
		TaxonomyLevel level = taxonomyService.createTaxonomyLevel(identifier, taxonomyService.createI18nSuffix(),
				null, null, parent, taxonomy);
		level.setType(type);
		level.setSortOrder(sortOrder);
		level = taxonomyService.updateTaxonomyLevel(level);

		for(int i=0; i<languages.size(); i++) {
			// leave gaps: not every level has every language
			if((index + i) % 3 == 2) continue;
			String title = STRANGE_TITLES[(index + i) % STRANGE_TITLES.length] + " " + languages.get(i);
			if(index == 0 && i == 0) {
				title = "L".repeat(255);
			}
			String description = (index + i) % 2 == 0 ? null : FilterFactory.getXSSFilter()
					.filter("<p>Zeile 1 &amp; ä</p>\n<p>Zeile <strong>2</strong>&nbsp;" + identifier + "</p>");
			saveI18n(TaxonomyUIFactory.PREFIX_DISPLAY_NAME + level.getI18nSuffix(), languages.get(i), title);
			saveI18n(TaxonomyUIFactory.PREFIX_DESCRIPTION + level.getI18nSuffix(), languages.get(i), description);
		}
		return level;
	}

	private void saveI18n(String key, String languageKey, String value) {
		Locale locale = i18nManager.getLocaleOrNull(languageKey);
		Locale overlayLocale = i18nModule.getOverlayLocales().get(locale);
		I18nItem item = i18nManager.getI18nItem(TaxonomyUIFactory.BUNDLE_NAME, key, overlayLocale);
		i18nManager.saveOrUpdateI18nItem(item, value);
	}

	private List<String> getLanguages() {
		List<String> languages = new ArrayList<>();
		for(String key:List.of("de", "en", "fr")) {
			if(i18nModule.getEnabledLanguageKeys().contains(key)) {
				languages.add(key);
			}
		}
		Assert.assertFalse(languages.isEmpty());
		return languages;
	}

	private File createImage(Color color) throws IOException {
		BufferedImage image = new BufferedImage(20, 10, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = image.createGraphics();
		g.setColor(color);
		g.fillRect(0, 0, 20, 10);
		g.dispose();
		File file = File.createTempFile("tax-img", ".png", new File(WebappHelper.getTmpDir()));
		ImageIO.write(image, "png", file);
		tmpFiles.add(file);
		return file;
	}

	private File export(Taxonomy taxonomy, Identity doer) throws IOException {
		File file = File.createTempFile("taxonomy-export", ".zip", new File(WebappHelper.getTmpDir()));
		tmpFiles.add(file);
		try(OutputStream out = Files.newOutputStream(file.toPath())) {
			importExportService.exportZip(taxonomy, doer, Locale.ENGLISH, out);
		}
		dbInstance.commitAndCloseSession();
		return file;
	}

	private File writeTmpFile(String name, String content) throws IOException {
		File dir = FileUtils.createTempDir("tax-csv", null, new File(WebappHelper.getTmpDir()));
		tmpFiles.add(dir);
		File file = new File(dir, name);
		Files.writeString(file.toPath(), content, StandardCharsets.UTF_8);
		return file;
	}

	private void assertNoErrors(TaxonomyFileData data) {
		List<String> errors = new ArrayList<>();
		data.getMessages().stream().filter(FileMessage::isError)
			.forEach(message -> errors.add("file: " + message));
		for(FileLevelType type:data.getLevelTypes()) {
			type.getMessages().stream().filter(FileMessage::isError)
				.forEach(message -> errors.add("type " + type.getIdentifier() + ": " + message));
		}
		for(FileLevel level:data.getLevels()) {
			// the test with the unknown language expects an error
			if("CSV3".equals(level.getIdentifier())) continue;
			level.getMessages().stream().filter(FileMessage::isError)
				.forEach(message -> errors.add("level " + level.getPath() + ": " + message.i18nKey() + " " + List.of(message.args())));
		}
		Assert.assertTrue(errors.toString(), errors.isEmpty());
	}

	private void assertSameExport(File export1, File export2) throws IOException {
		try(ZipFile zip1 = new ZipFile(export1);
				ZipFile zip2 = new ZipFile(export2)) {
			Map<String,ZipEntry> entries1 = entries(zip1);
			Map<String,ZipEntry> entries2 = entries(zip2);
			Assert.assertEquals(entries1.keySet(), entries2.keySet());
			Assert.assertTrue("readme in the ZIP", entries1.containsKey(TaxonomyFileFormat.README_NAME));

			for(String name:entries1.keySet()) {
				if(TaxonomyFileFormat.WORKBOOK_NAME.equals(name)) {
					continue;
				}
				byte[] content1 = read(zip1, entries1.get(name));
				byte[] content2 = read(zip2, entries2.get(name));
				Assert.assertArrayEquals("Entry " + name, content1, content2);
			}

			List<List<List<String>>> sheets1 = readSheets(zip1, entries1.get(TaxonomyFileFormat.WORKBOOK_NAME));
			List<List<List<String>>> sheets2 = readSheets(zip2, entries2.get(TaxonomyFileFormat.WORKBOOK_NAME));
			Assert.assertEquals(3, sheets1.size());
			Assert.assertEquals(3, sheets2.size());
			Assert.assertFalse(sheets1.get(TaxonomyFileFormat.SHEET_LEVELS).size() < 10);
			Assert.assertEquals(sheets1.get(TaxonomyFileFormat.SHEET_LEVELS), sheets2.get(TaxonomyFileFormat.SHEET_LEVELS));
			Assert.assertEquals(sheets1.get(TaxonomyFileFormat.SHEET_TYPES), sheets2.get(TaxonomyFileFormat.SHEET_TYPES));
			Assert.assertEquals(taxonomyRows(sheets1.get(TaxonomyFileFormat.SHEET_INFORMATION)),
					taxonomyRows(sheets2.get(TaxonomyFileFormat.SHEET_INFORMATION)));

			log.info("Taxonomy round trip: {} levels, {} types, {} ZIP entries",
					sheets1.get(0).size() - 1, sheets1.get(1).size() - 1, entries1.size());
		}
	}

	private List<List<String>> taxonomyRows(List<List<String>> infoRows) {
		return infoRows.stream()
				.filter(row -> !row.isEmpty() && row.get(0) != null
					&& (row.get(0).startsWith("taxonomy.") || row.get(0).equals(TaxonomyFileFormat.INFO_FORMAT))
					&& !row.get(0).equals(TaxonomyFileFormat.INFO_TAXONOMY_USAGE)
					&& !row.get(0).equals(TaxonomyFileFormat.INFO_TAXONOMY_IDENTIFIER))
				.toList();
	}

	private Map<String,ZipEntry> entries(ZipFile zip) {
		Map<String,ZipEntry> entries = new TreeMap<>();
		for(Enumeration<? extends ZipEntry> en = zip.entries(); en.hasMoreElements(); ) {
			ZipEntry entry = en.nextElement();
			entries.put(entry.getName(), entry);
		}
		return entries;
	}

	private byte[] read(ZipFile zip, ZipEntry entry) throws IOException {
		try(InputStream in = zip.getInputStream(entry)) {
			return in.readAllBytes();
		}
	}

	private List<List<List<String>>> readSheets(ZipFile zip, ZipEntry entry) throws IOException {
		List<List<List<String>>> sheets = new ArrayList<>();
		try(InputStream in = zip.getInputStream(entry);
				ReadableWorkbook wb = new ReadableWorkbook(in)) {
			for(Sheet sheet:wb.getSheets().toList()) {
				List<List<String>> rows = new ArrayList<>();
				try(Stream<Row> stream = sheet.openStream()) {
					stream.forEach(r -> {
						List<String> cells = new ArrayList<>();
						for(int i=0; i<r.getCellCount(); i++) {
							cells.add(r.getCell(i) == null ? null : r.getCell(i).getText());
						}
						rows.add(cells);
					});
				}
				sheets.add(rows);
			}
		}
		return sheets;
	}
}
