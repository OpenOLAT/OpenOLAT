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

import static org.olat.modules.taxonomy.manager.importexport.TaxonomyFileFormat.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.logging.log4j.Logger;
import org.dhatim.fastexcel.reader.Cell;
import org.dhatim.fastexcel.reader.ReadableWorkbook;
import org.dhatim.fastexcel.reader.Row;
import org.dhatim.fastexcel.reader.Sheet;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.openxml.AbstractExcelReader;
import org.olat.modules.taxonomy.TaxonomyModule;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.ICSVParser;

/**
 * Reads a taxonomy import file: a ZIP (workbook and media), a workbook
 * (xlsx) or a CSV with the columns of the levels sheet. The reader checks the
 * syntax of the cells only; the validation against a target taxonomy is done
 * by the {@link TaxonomyImportExportService}.
 *
 * Initial date: 30 Sept 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 */
public class TaxonomyFileReader extends AbstractExcelReader {

	private static final Logger log = Tracing.createLoggerFor(TaxonomyFileReader.class);

	private static final Pattern LANGUAGE_KEY = Pattern.compile("^[a-zA-Z]{2,3}([_-][a-zA-Z0-9]{2,8}){0,2}$");
	private static final Pattern INTEGER = Pattern.compile("^-?\\d+$");

	public static final long MAX_TEASER_SIZE = TaxonomyModule.TEASER_IMAGE_MAX_SIZE_KB * 1024l;
	public static final long MAX_BACKGROUND_SIZE = TaxonomyModule.BACKGROUND_IMAGE_MAX_SIZE_KB * 1024l;
	public static final long MAX_SHEET_SIZE = 100l * 1024l * 1024l;

	private final Set<String> headerLabels;
	private final Set<String> languageKeys;

	private int mediaCounter = 0;
	private final Map<String,File> extractedMedia = new HashMap<>();

	/**
	 * @param headerLabels The labels of the column "path" in all languages, used
	 * 		to recognize a header row in files without a fixed header (CSV).
	 * @param languageKeys The enabled language keys, used to recognize the layout of the format version 1
	 */
	public TaxonomyFileReader(Collection<String> headerLabels, Collection<String> languageKeys) {
		this.languageKeys = new HashSet<>();
		if(languageKeys != null) {
			for(String languageKey:languageKeys) {
				this.languageKeys.add(normalizeLanguage(languageKey));
			}
		}
		this.headerLabels = new HashSet<>();
		if(headerLabels != null) {
			for(String label:headerLabels) {
				String normalized = normalizeHeader(label);
				if(normalized != null) {
					this.headerLabels.add(normalized);
				}
			}
		}
	}

	/**
	 * Read the file.
	 *
	 * @param file The uploaded file
	 * @param filename The original file name (used to detect the type)
	 * @return The data, never null. Errors of the file are in {@link TaxonomyFileData#getMessages()}.
	 */
	public TaxonomyFileData read(File file, String filename) {
		TaxonomyFileData data = new TaxonomyFileData();
		String name = filename == null ? file.getName() : filename;
		String lowerName = name.toLowerCase(Locale.ROOT);
		try {
			if(lowerName.endsWith(".csv")) {
				readCsv(file, data);
			} else if(lowerName.endsWith(".xlsx")) {
				readWorkbook(file, data);
			} else if(lowerName.endsWith(".zip")) {
				readZip(file, data);
			} else {
				data.addError("importexport.error.file.type");
			}
		} catch(Exception e) {
			log.warn("Cannot read taxonomy import file: {}", name, e);
			data.addError("importexport.error.file.unreadable");
		}
		return data;
	}

	private void readZip(File file, TaxonomyFileData data) throws IOException {
		try(ZipFile zipFile = new ZipFile(file)) {
			// A workbook is a ZIP too
			if(zipFile.getEntry("[Content_Types].xml") != null) {
				readWorkbook(file, data);
				return;
			}

			ZipEntry sheetEntry = findSheetEntry(zipFile);
			if(sheetEntry == null) {
				data.addError("importexport.error.zip.no.sheet");
				return;
			}

			String sheetName = sheetEntry.getName();
			String baseDir = sheetName.contains("/") ? sheetName.substring(0, sheetName.lastIndexOf('/') + 1) : "";
			File workingDir = createWorkingDirectory(data);
			File sheetFile = new File(workingDir, sheetName.toLowerCase(Locale.ROOT).endsWith(".csv") ? "levels.csv" : "workbook.xlsx");
			try(InputStream in = zipFile.getInputStream(sheetEntry);
					OutputStream out = Files.newOutputStream(sheetFile.toPath())) {
				// bounded: the size in the ZIP header can be faked (ZIP bomb)
				if(IOUtils.copyLarge(in, out, 0, MAX_SHEET_SIZE + 1) > MAX_SHEET_SIZE) {
					data.addError("importexport.error.file.too.large", sheetName, Long.toString(MAX_SHEET_SIZE / 1024 / 1024));
					return;
				}
			}

			if(sheetFile.getName().endsWith(".csv")) {
				readCsv(sheetFile, data);
			} else {
				readWorkbook(sheetFile, data);
			}
			resolveMedia(data, zipFile, baseDir, workingDir);
		}
	}

	/**
	 * The workbook <code>taxonomy.xlsx</code> or the first workbook or CSV in the
	 * root of the ZIP or one folder below.
	 */
	private ZipEntry findSheetEntry(ZipFile zipFile) {
		List<ZipEntry> candidates = new ArrayList<>();
		for(Enumeration<? extends ZipEntry> entries = zipFile.entries(); entries.hasMoreElements(); ) {
			ZipEntry entry = entries.nextElement();
			String name = entry.getName();
			if(entry.isDirectory() || name.startsWith("__MACOSX") || name.contains("/.") || name.startsWith(".")) {
				continue;
			}
			String lowerName = name.toLowerCase(Locale.ROOT);
			int depth = name.split("/").length - 1;
			if(depth <= 1 && (lowerName.endsWith(".xlsx") || lowerName.endsWith(".csv"))) {
				candidates.add(entry);
			}
		}

		Optional<ZipEntry> taxonomyEntry = candidates.stream()
				.filter(entry -> entry.getName().equals(WORKBOOK_NAME) || entry.getName().endsWith("/" + WORKBOOK_NAME))
				.findFirst();
		if(taxonomyEntry.isPresent()) {
			return taxonomyEntry.get();
		}
		return candidates.stream()
				.sorted((e1, e2) -> e1.getName().compareTo(e2.getName()))
				.findFirst().orElse(null);
	}

	private void readWorkbook(File file, TaxonomyFileData data) throws IOException {
		try(ReadableWorkbook wb = new ReadableWorkbook(file)) {
			List<Sheet> sheets = wb.getSheets().toList();

			Map<String,String> information = new HashMap<>();
			if(sheets.size() > SHEET_INFORMATION) {
				information = readInformation(sheets.get(SHEET_INFORMATION));
			}

			boolean formatV2 = information.containsKey(INFO_FORMAT);
			List<List<String>> levelRows = readRows(sheets.get(SHEET_LEVELS));
			// The first row of a workbook is always the header
			if(!levelRows.isEmpty()) {
				levelRows = levelRows.subList(1, levelRows.size());
			}
			boolean legacy = !formatV2 && isLegacyLayout(levelRows);
			readLevels(levelRows, legacy, data, 2);

			if(formatV2 && sheets.size() > SHEET_TYPES) {
				List<List<String>> typeRows = readRows(sheets.get(SHEET_TYPES));
				readLevelTypes(typeRows.subList(Math.min(1, typeRows.size()), typeRows.size()), data);
			}
			if(formatV2) {
				applyInformation(information, data);
			}
			data.setFormatVersion(formatV2 ? FORMAT_VERSION : 1);
		}
	}

	private void readCsv(File file, TaxonomyFileData data) throws IOException {
		List<List<String>> rows = new ArrayList<>();
		char separator = detectSeparator(file);
		CSVParser parser = new CSVParserBuilder()
				.withSeparator(separator)
				.withQuoteChar('"')
				.withEscapeChar(ICSVParser.NULL_CHARACTER)
				.withIgnoreQuotations(false)
				.build();
		try(Reader reader = new BufferedReader(new InputStreamReader(openWithoutBom(file), StandardCharsets.UTF_8));
				CSVReader csvReader = new CSVReaderBuilder(reader)
					.withCSVParser(parser)
					.withKeepCarriageReturn(false)
					.build()) {
			String[] line;
			while((line = csvReader.readNextSilently()) != null) {
				List<String> row = new ArrayList<>(line.length);
				for(String cell:line) {
					row.add(StringHelper.containsNonWhitespace(cell) ? cell : null);
				}
				rows.add(row);
			}
		}

		int firstRowNum = 1;
		if(!rows.isEmpty() && isHeader(rows.get(0))) {
			rows = rows.subList(1, rows.size());
			firstRowNum = 2;
		}
		boolean legacy = isLegacyLayout(rows);
		readLevels(rows, legacy, data, firstRowNum);
		data.setFormatVersion(legacy ? 1 : FORMAT_VERSION);
	}

	private char detectSeparator(File file) throws IOException {
		String firstLine;
		try(BufferedReader reader = new BufferedReader(new InputStreamReader(openWithoutBom(file), StandardCharsets.UTF_8))) {
			firstLine = reader.readLine();
		}
		if(firstLine == null) {
			return ',';
		}
		int tabs = count(firstLine, '\t');
		int semicolons = count(firstLine, ';');
		int commas = count(firstLine, ',');
		if(tabs >= semicolons && tabs >= commas && tabs > 0) {
			return '\t';
		}
		return semicolons > commas ? ';' : ',';
	}

	private static int count(String line, char ch) {
		int count = 0;
		boolean quoted = false;
		for(int i=0; i<line.length(); i++) {
			char c = line.charAt(i);
			if(c == '"') {
				quoted = !quoted;
			} else if(c == ch && !quoted) {
				count++;
			}
		}
		return count;
	}

	private InputStream openWithoutBom(File file) throws IOException {
		return BOMInputStream.builder().setFile(file).get();
	}

	private boolean isHeader(List<String> row) {
		String firstCell = row.isEmpty() ? null : normalizeHeader(row.get(0));
		return firstCell != null && headerLabels.contains(firstCell);
	}

	private static String normalizeLanguage(String languageKey) {
		return languageKey == null ? null : languageKey.trim().toLowerCase(Locale.ROOT).replace('-', '_');
	}

	private static String normalizeHeader(String label) {
		if(label == null) return null;
		String normalized = label.replace("*", "").trim().toLowerCase(Locale.ROOT);
		return normalized.isEmpty() ? null : normalized;
	}

	/**
	 * The format version 1 has 4 fixed columns and the language key in the
	 * fifth column. In the format version 2, the fifth column is the sort order.
	 */
	private boolean isLegacyLayout(List<List<String>> rows) {
		for(List<String> row:rows) {
			String value = get(row, LEGACY_LEVEL_FIRST_LANGUAGE);
			if(StringHelper.containsNonWhitespace(value)) {
				value = value.trim();
				return !INTEGER.matcher(value).matches() && LANGUAGE_KEY.matcher(value).matches()
						&& languageKeys.contains(normalizeLanguage(value));
			}
		}
		return false;
	}

	private List<List<String>> readRows(Sheet sheet) throws IOException {
		List<List<String>> rows = new ArrayList<>();
		try(Stream<Row> stream = sheet.openStream()) {
			stream.forEach(r -> {
				// fill missing rows to keep the row numbers
				while(rows.size() < r.getRowNum() - 1) {
					rows.add(Collections.emptyList());
				}
				List<String> cells = new ArrayList<>(r.getCellCount());
				for(int i=0; i<r.getCellCount(); i++) {
					cells.add(getText(r, i));
				}
				rows.add(cells);
			});
		}
		return rows;
	}

	/**
	 * @return The value of the cell as text: strings as they are, numbers
	 * 		and booleans as written in the file.
	 */
	private String getText(Row r, int pos) {
		if(r.getCellCount() <= pos) return null;
		Cell cell = r.getCell(pos);
		if(cell == null) return null;

		String value = switch(cell.getType()) {
			case STRING -> cell.asString();
			case NUMBER, FORMULA -> cell.getRawValue();
			case BOOLEAN -> Boolean.TRUE.equals(cell.asBoolean()) ? ON : OFF;
			default -> null;
		};
		return StringHelper.containsNonWhitespace(value) ? value : null;
	}

	private Map<String,String> readInformation(Sheet sheet) throws IOException {
		Map<String,String> information = new HashMap<>();
		for(List<String> row:readRows(sheet)) {
			String key = get(row, INFO_KEY);
			if(StringHelper.containsNonWhitespace(key)) {
				information.put(key.trim(), get(row, INFO_VALUE));
			}
		}
		return information;
	}

	private void applyInformation(Map<String,String> information, TaxonomyFileData data) {
		String format = information.get(INFO_FORMAT);
		if(format != null && !Integer.toString(FORMAT_VERSION).equals(format.trim())) {
			data.addWarning("importexport.warning.format.version", format);
		}
		data.setIdentifier(trim(information.get(INFO_TAXONOMY_IDENTIFIER)));
		data.setDisplayName(information.get(INFO_TAXONOMY_DISPLAY_NAME));
		data.setDescription(information.get(INFO_TAXONOMY_DESCRIPTION));
		data.setExternalId(trim(information.get(INFO_TAXONOMY_EXTERNAL_ID)));
		data.setManagedFlags(trim(information.get(INFO_TAXONOMY_MANAGED_FLAGS)));

		for(String key:List.of(INFO_EXPORT_URL, INFO_EXPORT_VERSION, INFO_EXPORT_LANGUAGE, INFO_EXPORT_DATE, INFO_EXPORT_BY, INFO_TAXONOMY_USAGE)) {
			if(information.containsKey(key)) {
				data.getExportInformation().put(key, information.get(key));
			}
		}
	}

	private void readLevels(List<List<String>> rows, boolean legacy, TaxonomyFileData data, int firstRowNum) {
		Set<String> languages = new LinkedHashSet<>();
		int firstLanguageCol = legacy ? LEGACY_LEVEL_FIRST_LANGUAGE : LEVEL_FIRST_LANGUAGE;

		for(int i=0; i<rows.size(); i++) {
			List<String> row = rows.get(i);
			if(isEmpty(row)) continue;

			FileLevel level = new FileLevel();
			level.setRowNum(firstRowNum + i);
			level.setPath(trim(get(row, LEVEL_PATH)));
			level.setIdentifier(trim(get(row, LEVEL_IDENTIFIER)));
			if(legacy) {
				level.setType(trim(get(row, 2)));
				level.setSortOrder(trim(get(row, 3)));
			} else {
				level.setExternalId(trim(get(row, LEVEL_EXTERNAL_ID)));
				level.setType(trim(get(row, LEVEL_TYPE)));
				level.setSortOrder(trim(get(row, LEVEL_SORT_ORDER)));
				level.setManagedFlags(trim(get(row, LEVEL_MANAGED_FLAGS)));
				String teaser = trim(get(row, LEVEL_TEASER));
				if(teaser != null) {
					level.setTeaserImage(new FileMedia(teaser, filenameOf(teaser), null));
				}
				String background = trim(get(row, LEVEL_BACKGROUND));
				if(background != null) {
					level.setBackgroundImage(new FileMedia(background, filenameOf(background), null));
				}
			}

			for(int col=firstLanguageCol; col<row.size(); col+=LEVEL_LANGUAGE_GROUP_SIZE) {
				String language = trim(get(row, col));
				String title = get(row, col + 1);
				String description = get(row, col + 2);
				if(language == null) {
					if(title != null || description != null) {
						level.addError("importexport.level.language", "importexport.error.language.missing");
					}
					continue;
				}
				languages.add(language);
				if(level.getTranslations().containsKey(language)) {
					level.addError("importexport.level.language", "importexport.error.language.duplicate", language);
				} else {
					level.putTranslation(language, title, description);
				}
			}
			data.getLevels().add(level);
		}
		data.getLanguages().addAll(languages);
	}

	private void readLevelTypes(List<List<String>> rows, TaxonomyFileData data) {
		for(int i=0; i<rows.size(); i++) {
			List<String> row = rows.get(i);
			if(isEmpty(row)) continue;

			FileLevelType type = new FileLevelType();
			type.setRowNum(i + 2);
			type.setIdentifier(trim(get(row, TYPE_IDENTIFIER)));
			type.setDisplayName(get(row, TYPE_DISPLAY_NAME));
			type.setDescription(get(row, TYPE_DESCRIPTION));
			type.setExternalId(trim(get(row, TYPE_EXTERNAL_ID)));
			type.setCssClass(trim(get(row, TYPE_CSS_CLASS)));
			type.setVisible(getBoolean(row, TYPE_VISIBLE, true, type, "importexport.type.visible"));
			type.setAllowedAsSubject(getBoolean(row, TYPE_ALLOWED_AS_SUBJECT, true, type, "importexport.type.allowed.subject"));
			type.setAllowedAsCompetence(getBoolean(row, TYPE_ALLOWED_AS_COMPETENCE, true, type, "importexport.type.allowed.competence"));
			type.setSubTypes(splitMultiValues(get(row, TYPE_SUB_TYPES)));
			type.setManagedFlags(trim(get(row, TYPE_MANAGED_FLAGS)));
			type.setLibraryEnabled(getBoolean(row, TYPE_LIBRARY_ENABLED, false, type, "importexport.type.library.enabled"));
			type.setLibraryManage(getBoolean(row, TYPE_LIBRARY_MANAGE, false, type, "importexport.type.library.manage"));
			type.setLibraryTeachRead(getBoolean(row, TYPE_LIBRARY_TEACH_READ, false, type, "importexport.type.library.teach.read"));
			type.setLibraryTeachWrite(getBoolean(row, TYPE_LIBRARY_TEACH_WRITE, false, type, "importexport.type.library.teach.write"));
			type.setLibraryHaveRead(getBoolean(row, TYPE_LIBRARY_HAVE_READ, false, type, "importexport.type.library.have.read"));
			type.setLibraryTargetRead(getBoolean(row, TYPE_LIBRARY_TARGET_READ, false, type, "importexport.type.library.target.read"));

			String parentLevels = trim(get(row, TYPE_LIBRARY_TEACH_READ_PARENT_LEVELS));
			if(parentLevels != null) {
				if(INTEGER.matcher(parentLevels).matches()) {
					type.setLibraryTeachReadParentLevels(Integer.parseInt(parentLevels));
				} else {
					type.addError("importexport.type.library.teach.read.levels", "importexport.error.integer", parentLevels);
				}
			}
			data.getLevelTypes().add(type);
		}
	}

	private boolean getBoolean(List<String> row, int col, boolean defaultValue, FileRow fileRow, String column) {
		String value = get(row, col);
		if(!StringHelper.containsNonWhitespace(value)) {
			return defaultValue;
		}
		Boolean bool = parseOnOff(value);
		if(bool == null) {
			fileRow.addError(column, "importexport.error.on.off", value);
			return defaultValue;
		}
		return bool.booleanValue();
	}

	/**
	 * Extract the images referenced by the levels. Images without a reference
	 * in the sheet are looked up in the layout of the format version 1:
	 * <code>media/&lt;identifiers&gt;/teaser|background/&lt;file&gt;</code>.
	 */
	private void resolveMedia(TaxonomyFileData data, ZipFile zipFile, String baseDir, File workingDir) {
		Map<String,ZipEntry> legacyTeasers = new HashMap<>();
		Map<String,ZipEntry> legacyBackgrounds = new HashMap<>();
		collectLegacyMedia(zipFile, baseDir, legacyTeasers, legacyBackgrounds);

		for(FileLevel level:data.getLevels()) {
			FileMedia teaser = level.getTeaserImage();
			if(teaser != null) {
				level.setTeaserImage(extract(teaser.zipPath(), zipFile, baseDir, workingDir, MAX_TEASER_SIZE, level, "importexport.level.teaser"));
			} else if(legacyTeasers.containsKey(level.getNormalizedLegacyPath())) {
				ZipEntry entry = legacyTeasers.get(level.getNormalizedLegacyPath());
				level.setTeaserImage(extract(entry.getName().substring(baseDir.length()), zipFile, baseDir, workingDir, MAX_TEASER_SIZE, level, "importexport.level.teaser"));
			}

			FileMedia background = level.getBackgroundImage();
			if(background != null) {
				level.setBackgroundImage(extract(background.zipPath(), zipFile, baseDir, workingDir, MAX_BACKGROUND_SIZE, level, "importexport.level.background"));
			} else if(legacyBackgrounds.containsKey(level.getNormalizedLegacyPath())) {
				ZipEntry entry = legacyBackgrounds.get(level.getNormalizedLegacyPath());
				level.setBackgroundImage(extract(entry.getName().substring(baseDir.length()), zipFile, baseDir, workingDir, MAX_BACKGROUND_SIZE, level, "importexport.level.background"));
			}
		}
	}

	private void collectLegacyMedia(ZipFile zipFile, String baseDir, Map<String,ZipEntry> teasers, Map<String,ZipEntry> backgrounds) {
		String mediaPrefix = baseDir + MEDIA_FOLDER + "/";
		for(Enumeration<? extends ZipEntry> entries = zipFile.entries(); entries.hasMoreElements(); ) {
			ZipEntry entry = entries.nextElement();
			String name = entry.getName();
			if(entry.isDirectory() || !name.startsWith(mediaPrefix) || name.contains("/.")) {
				continue;
			}
			// <identifiers...>/teaser/<file>
			String[] segments = name.substring(mediaPrefix.length()).split("/");
			if(segments.length < 3) {
				continue;
			}
			String marker = segments[segments.length - 2];
			String path = "/" + String.join("/", List.of(segments).subList(0, segments.length - 2)) + "/";
			if(TEASER.equals(marker)) {
				teasers.putIfAbsent(path, entry);
			} else if(BACKGROUND.equals(marker)) {
				backgrounds.putIfAbsent(path, entry);
			}
		}
	}

	private FileMedia extract(String zipPath, ZipFile zipFile, String baseDir, File workingDir,
			long maxSize, FileLevel level, String column) {
		String filename = filenameOf(zipPath);
		String entryName = normalizeEntryName(baseDir, zipPath);
		// an image used by many levels is extracted once
		File alreadyExtracted = entryName == null ? null : extractedMedia.get(entryName);
		if(alreadyExtracted != null && alreadyExtracted.length() <= maxSize) {
			return new FileMedia(zipPath, filename, alreadyExtracted);
		}
		ZipEntry entry = entryName == null ? null : zipFile.getEntry(entryName);
		if(entry == null || entry.isDirectory()) {
			level.addError(column, "importexport.error.media.missing", zipPath);
			return new FileMedia(zipPath, filename, null);
		}
		if(!isImage(filename)) {
			level.addError(column, "importexport.error.media.type", zipPath);
			return new FileMedia(zipPath, filename, null);
		}

		// The name on disk is chosen by us: no path from the ZIP reaches the file system
		File target = new File(new File(workingDir, "media" + (++mediaCounter)), FileUtils.cleanFilename(filename));
		target.getParentFile().mkdirs();
		try(InputStream in = zipFile.getInputStream(entry);
				OutputStream out = Files.newOutputStream(target.toPath())) {
			if(IOUtils.copyLarge(in, out, 0, maxSize + 1) > maxSize) {
				level.addError(column, "importexport.error.media.size", zipPath, Long.toString(maxSize / 1024));
				return new FileMedia(zipPath, filename, null);
			}
		} catch(IOException e) {
			log.warn("Cannot extract taxonomy image: {}", zipPath, e);
			level.addError(column, "importexport.error.media.missing", zipPath);
			return new FileMedia(zipPath, filename, null);
		}
		extractedMedia.put(entryName, target);
		return new FileMedia(zipPath, filename, target);
	}

	/**
	 * @return The name of the entry in the ZIP or null if the path leaves the base directory.
	 */
	static String normalizeEntryName(String baseDir, String zipPath) {
		String path = zipPath.replace('\\', '/');
		while(path.startsWith("./")) {
			path = path.substring(2);
		}
		if(path.startsWith("/")) {
			return null;
		}
		for(String segment:path.split("/")) {
			if("..".equals(segment)) {
				return null;
			}
		}
		return baseDir + path;
	}

	private static boolean isImage(String filename) {
		String mimeType = WebappHelper.getMimeType(filename);
		return mimeType != null && mimeType.startsWith("image/");
	}

	private static String filenameOf(String zipPath) {
		String path = zipPath.replace('\\', '/');
		int index = path.lastIndexOf('/');
		return index >= 0 ? path.substring(index + 1) : path;
	}

	private File createWorkingDirectory(TaxonomyFileData data) {
		File workingDir = FileUtils.createTempDir("taxonomy_import", null, new File(WebappHelper.getTmpDir()));
		data.setWorkingDirectory(workingDir);
		return workingDir;
	}

	private static String get(List<String> row, int col) {
		if(row == null || col >= row.size()) return null;
		return row.get(col);
	}

	private static String trim(String value) {
		if(value == null) return null;
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

	private static boolean isEmpty(List<String> row) {
		if(row == null) return true;
		return row.stream().noneMatch(StringHelper::containsNonWhitespace);
	}
}
