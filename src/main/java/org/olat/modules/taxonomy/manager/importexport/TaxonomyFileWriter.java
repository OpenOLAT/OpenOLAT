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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.output.CloseShieldOutputStream;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.ZipUtil;
import org.olat.core.util.openxml.OpenXMLWorkbook;
import org.olat.core.util.openxml.OpenXMLWorksheet;
import org.olat.core.util.openxml.OpenXMLWorksheet.Row;
import org.olat.core.util.openxml.workbookstyle.CellStyle;

/**
 * Writes a {@link TaxonomyFileData} as workbook or as ZIP with the
 * workbook, the media files and the readme. The writer does not access the
 * database: the same data always produces the same sheets.
 *
 * Initial date: 30 Sept 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 */
public class TaxonomyFileWriter {

	public static final String MANDATORY = " *";

	private final Translator translator;

	/**
	 * @param translator A translator of the package <code>org.olat.modules.taxonomy.ui</code>
	 */
	public TaxonomyFileWriter(Translator translator) {
		this.translator = translator;
	}

	/**
	 * Write the ZIP: workbook, media files, readme.
	 */
	public void writeZip(TaxonomyFileData data, OutputStream out) throws IOException {
		ZipOutputStream zout = new ZipOutputStream(out);
		zout.setLevel(9);

		zout.putNextEntry(new ZipEntry(WORKBOOK_NAME));
		writeWorkbook(data, CloseShieldOutputStream.wrap(zout));
		zout.closeEntry();

		Set<String> written = new HashSet<>();
		for(FileLevel level:data.getLevels()) {
			writeMedia(level.getTeaserImage(), written, zout);
			writeMedia(level.getBackgroundImage(), written, zout);
		}

		try(InputStream readme = TaxonomyFileWriter.class.getResourceAsStream(README_NAME)) {
			if(readme != null) {
				zout.putNextEntry(new ZipEntry(README_NAME));
				readme.transferTo(zout);
				zout.closeEntry();
			}
		}
		zout.finish();
		zout.flush();
	}

	private void writeMedia(FileMedia media, Set<String> written, ZipOutputStream zout) {
		if(media == null || media.file() == null || !media.file().exists() || written.contains(media.zipPath())) {
			return;
		}
		written.add(media.zipPath());
		ZipUtil.addFileToZip(media.zipPath(), media.file(), zout);
	}

	/**
	 * Write the workbook with the sheets levels, level types and information.
	 * The method closes the output stream.
	 */
	public void writeWorkbook(TaxonomyFileData data, OutputStream out) {
		List<String> sheetNames = List.of(
				translator.translate("importexport.sheet.levels"),
				translator.translate("importexport.sheet.types"),
				translator.translate("importexport.sheet.information"));
		try(OpenXMLWorkbook workbook = new OpenXMLWorkbook(out, 3, sheetNames)) {
			OpenXMLWorksheet levelsSheet = workbook.nextWorksheet();
			writeLevels(data, workbook, levelsSheet);
			OpenXMLWorksheet typesSheet = workbook.nextWorksheet();
			writeLevelTypes(data, workbook, typesSheet);
			OpenXMLWorksheet infoSheet = workbook.nextWorksheet();
			writeInformation(data, workbook, infoSheet);
		} catch(IOException e) {
			throw new UncheckedIOException("Cannot write taxonomy workbook", e);
		}
	}

	private void writeLevels(TaxonomyFileData data, OpenXMLWorkbook workbook, OpenXMLWorksheet sheet) {
		sheet.setHeaderRows(1);
		CellStyle headerStyle = workbook.getStyles().getHeaderStyle();

		Row header = sheet.newRow();
		header.addCell(LEVEL_PATH, translator.translate("importexport.level.path") + MANDATORY, headerStyle);
		header.addCell(LEVEL_IDENTIFIER, translator.translate("importexport.level.identifier") + MANDATORY, headerStyle);
		header.addCell(LEVEL_EXTERNAL_ID, translator.translate("importexport.level.external.id"), headerStyle);
		header.addCell(LEVEL_TYPE, translator.translate("importexport.level.type"), headerStyle);
		header.addCell(LEVEL_SORT_ORDER, translator.translate("importexport.level.sort.order"), headerStyle);
		header.addCell(LEVEL_MANAGED_FLAGS, translator.translate("importexport.managed.flags"), headerStyle);
		header.addCell(LEVEL_TEASER, translator.translate("importexport.level.teaser"), headerStyle);
		header.addCell(LEVEL_BACKGROUND, translator.translate("importexport.level.background"), headerStyle);
		int col = LEVEL_FIRST_LANGUAGE;
		for(int i=0; i<data.getLanguages().size(); i++) {
			header.addCell(col++, translator.translate("importexport.level.language"), headerStyle);
			header.addCell(col++, translator.translate("importexport.level.title"), headerStyle);
			header.addCell(col++, translator.translate("importexport.level.description"), headerStyle);
		}

		for(FileLevel level:data.getLevels()) {
			Row row = sheet.newRow();
			addCell(row, LEVEL_PATH, level.getPath());
			addCell(row, LEVEL_IDENTIFIER, level.getIdentifier());
			addCell(row, LEVEL_EXTERNAL_ID, level.getExternalId());
			addCell(row, LEVEL_TYPE, level.getType());
			addCell(row, LEVEL_SORT_ORDER, level.getSortOrder());
			addCell(row, LEVEL_MANAGED_FLAGS, level.getManagedFlags());
			addCell(row, LEVEL_TEASER, level.getTeaserImage() == null ? null : level.getTeaserImage().zipPath());
			addCell(row, LEVEL_BACKGROUND, level.getBackgroundImage() == null ? null : level.getBackgroundImage().zipPath());

			col = LEVEL_FIRST_LANGUAGE;
			for(String language:data.getLanguages()) {
				FileTranslation translation = level.getTranslation(language);
				addCell(row, col++, language);
				addCell(row, col++, translation == null ? null : translation.title());
				addCell(row, col++, translation == null ? null : translation.description());
			}
		}
	}

	private void writeLevelTypes(TaxonomyFileData data, OpenXMLWorkbook workbook, OpenXMLWorksheet sheet) {
		sheet.setHeaderRows(1);
		CellStyle headerStyle = workbook.getStyles().getHeaderStyle();

		Row header = sheet.newRow();
		header.addCell(TYPE_IDENTIFIER, translator.translate("importexport.type.identifier") + MANDATORY, headerStyle);
		header.addCell(TYPE_DISPLAY_NAME, translator.translate("importexport.type.display.name") + MANDATORY, headerStyle);
		header.addCell(TYPE_DESCRIPTION, translator.translate("importexport.type.description"), headerStyle);
		header.addCell(TYPE_EXTERNAL_ID, translator.translate("importexport.type.external.id"), headerStyle);
		header.addCell(TYPE_CSS_CLASS, translator.translate("importexport.type.css.class"), headerStyle);
		header.addCell(TYPE_VISIBLE, translator.translate("importexport.type.visible"), headerStyle);
		header.addCell(TYPE_ALLOWED_AS_SUBJECT, translator.translate("importexport.type.allowed.subject"), headerStyle);
		header.addCell(TYPE_ALLOWED_AS_COMPETENCE, translator.translate("importexport.type.allowed.competence"), headerStyle);
		header.addCell(TYPE_SUB_TYPES, translator.translate("importexport.type.sub.types"), headerStyle);
		header.addCell(TYPE_MANAGED_FLAGS, translator.translate("importexport.managed.flags"), headerStyle);
		header.addCell(TYPE_LIBRARY_ENABLED, translator.translate("importexport.type.library.enabled"), headerStyle);
		header.addCell(TYPE_LIBRARY_MANAGE, translator.translate("importexport.type.library.manage"), headerStyle);
		header.addCell(TYPE_LIBRARY_TEACH_READ, translator.translate("importexport.type.library.teach.read"), headerStyle);
		header.addCell(TYPE_LIBRARY_TEACH_READ_PARENT_LEVELS, translator.translate("importexport.type.library.teach.read.levels"), headerStyle);
		header.addCell(TYPE_LIBRARY_TEACH_WRITE, translator.translate("importexport.type.library.teach.write"), headerStyle);
		header.addCell(TYPE_LIBRARY_HAVE_READ, translator.translate("importexport.type.library.have.read"), headerStyle);
		header.addCell(TYPE_LIBRARY_TARGET_READ, translator.translate("importexport.type.library.target.read"), headerStyle);

		for(FileLevelType type:data.getLevelTypes()) {
			if(type.isImplicit()) continue;

			Row row = sheet.newRow();
			addCell(row, TYPE_IDENTIFIER, type.getIdentifier());
			addCell(row, TYPE_DISPLAY_NAME, type.getDisplayName());
			addCell(row, TYPE_DESCRIPTION, type.getDescription());
			addCell(row, TYPE_EXTERNAL_ID, type.getExternalId());
			addCell(row, TYPE_CSS_CLASS, type.getCssClass());
			addCell(row, TYPE_VISIBLE, toOnOff(type.isVisible()));
			addCell(row, TYPE_ALLOWED_AS_SUBJECT, toOnOff(type.isAllowedAsSubject()));
			addCell(row, TYPE_ALLOWED_AS_COMPETENCE, toOnOff(type.isAllowedAsCompetence()));
			addCell(row, TYPE_SUB_TYPES, joinMultiValues(type.getSubTypes()));
			addCell(row, TYPE_MANAGED_FLAGS, type.getManagedFlags());
			addCell(row, TYPE_LIBRARY_ENABLED, toOnOff(type.isLibraryEnabled()));
			addCell(row, TYPE_LIBRARY_MANAGE, toOnOff(type.isLibraryManage()));
			addCell(row, TYPE_LIBRARY_TEACH_READ, toOnOff(type.isLibraryTeachRead()));
			addCell(row, TYPE_LIBRARY_TEACH_READ_PARENT_LEVELS, Integer.toString(type.getLibraryTeachReadParentLevels()));
			addCell(row, TYPE_LIBRARY_TEACH_WRITE, toOnOff(type.isLibraryTeachWrite()));
			addCell(row, TYPE_LIBRARY_HAVE_READ, toOnOff(type.isLibraryHaveRead()));
			addCell(row, TYPE_LIBRARY_TARGET_READ, toOnOff(type.isLibraryTargetRead()));
		}
	}

	private void writeInformation(TaxonomyFileData data, OpenXMLWorkbook workbook, OpenXMLWorksheet sheet) {
		sheet.setHeaderRows(1);
		CellStyle headerStyle = workbook.getStyles().getHeaderStyle();

		Row header = sheet.newRow();
		header.addCell(INFO_KEY, translator.translate("importexport.info.key"), headerStyle);
		header.addCell(INFO_LABEL, translator.translate("importexport.info.label"), headerStyle);
		header.addCell(INFO_VALUE, translator.translate("importexport.info.value"), headerStyle);

		for(Map.Entry<String, String> info:data.getExportInformation().entrySet()) {
			addInformation(sheet, headerStyle, info.getKey(), info.getValue());
		}
		addInformation(sheet, headerStyle, INFO_FORMAT, Integer.toString(FORMAT_VERSION));
		addInformation(sheet, headerStyle, INFO_TAXONOMY_IDENTIFIER, data.getIdentifier());
		addInformation(sheet, headerStyle, INFO_TAXONOMY_DISPLAY_NAME, data.getDisplayName());
		addInformation(sheet, headerStyle, INFO_TAXONOMY_DESCRIPTION, data.getDescription());
		addInformation(sheet, headerStyle, INFO_TAXONOMY_EXTERNAL_ID, data.getExternalId());
		addInformation(sheet, headerStyle, INFO_TAXONOMY_MANAGED_FLAGS, data.getManagedFlags());
	}

	private void addInformation(OpenXMLWorksheet sheet, CellStyle labelStyle, String key, String value) {
		Row row = sheet.newRow();
		row.addCell(INFO_KEY, key);
		row.addCell(INFO_LABEL, translator.translate("importexport.info." + key), labelStyle);
		addCell(row, INFO_VALUE, value);
	}

	private void addCell(Row row, int col, String value) {
		if(StringHelper.containsNonWhitespace(value)) {
			row.addCell(col, value);
		}
	}
}
