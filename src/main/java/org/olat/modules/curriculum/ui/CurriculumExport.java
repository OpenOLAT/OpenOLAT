/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.modules.curriculum.ui;

import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.openxml.OpenXMLWorkbook;
import org.olat.core.util.openxml.OpenXMLWorkbookResource;
import org.olat.core.util.openxml.OpenXMLWorksheet;
import org.olat.core.util.openxml.OpenXMLWorksheet.Row;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumCalendars;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumLearningProgress;
import org.olat.modules.curriculum.CurriculumLectures;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.RepositoryEntryInfos;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.model.LectureBlockWithTeachers;
import org.olat.modules.lecture.model.LecturesBlockSearchParameters;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 30 janv. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CurriculumExport {
	
	private static final Logger log = Tracing.createLoggerFor(CurriculumExport.class);
	
	private static final String MANDATORY = " *";

	private final String url;
	private final Translator translator;
	private final Identity identity;
	private final Formatter formatter;
	
	private final List<Curriculum> curriculums;
	private final List<CurriculumElement> implementations;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private UserManager userManager;
	@Autowired
	private LectureService lectureService;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private CurriculumService curriculumService;
	
	public CurriculumExport(List<Curriculum> curriculums, Identity identity, String url, Translator translator) {
		this(curriculums, null, identity, url, translator);
	}
	
	public CurriculumExport(List<Curriculum> curriculums, List<CurriculumElement> implementations,
			Identity identity, String url, Translator translator) {
		CoreSpringFactory.autowireObject(this);
		this.url = url;
		this.identity = identity;
		this.translator = translator;
		this.curriculums = curriculums;
		this.implementations = implementations;
		formatter = Formatter.getInstance(translator.getLocale());
	}
	
	public OpenXMLWorkbookResource createMediaResource() {
		final String name = translator.translate("export.filename", formatter.formatDateAndTime(new Date()));
		final String fileName = StringHelper.transformDisplayNameToFileSystemName(name) + ".xlsx";
		
		return new OpenXMLWorkbookResource(fileName) {
			@Override
			protected void generate(OutputStream out) {
				createWorkbook(out);
			}
		};
	}
	
	private void createWorkbook(OutputStream out) {
		List<String> sheetNames = List.of(translator.translate("export.products"),
				translator.translate("export.implementations"), translator.translate("export.informations"));
		try(OpenXMLWorkbook workbook = new OpenXMLWorkbook(out, 3, sheetNames)) {
			OpenXMLWorksheet curriculumsSheet = workbook.nextWorksheet();
			addCurriculumsHeader(workbook, curriculumsSheet);
			addCurriculumsContent(workbook, curriculumsSheet);
			OpenXMLWorksheet implementationsSheet = workbook.nextWorksheet();
			addImplementationsHeader(workbook, implementationsSheet);
			if(implementations == null) {
				addCurriculumsImplementationsContent(workbook, implementationsSheet);
			} else {
				addImplementationsContent(workbook, implementationsSheet);
			}
			OpenXMLWorksheet infosSheet = workbook.nextWorksheet();
			addInformations(workbook, infosSheet);
			dbInstance.commitAndCloseSession();
		} catch (Exception e) {
			log.error("", e);
		}
	}
	
	private void addCurriculumsHeader(OpenXMLWorkbook workbook, OpenXMLWorksheet exportSheet) {
		exportSheet.setHeaderRows(1);
		
		int col = 0;
		Row headerRow = exportSheet.newRow();
		headerRow.addCell(col++, translator.translate("table.header.id"), workbook.getStyles().getHeaderStyle());
		headerRow.addCell(col++, translator.translate("table.header.title") + MANDATORY, workbook.getStyles().getHeaderStyle());
		headerRow.addCell(col++, translator.translate("table.header.external.ref") + MANDATORY, workbook.getStyles().getHeaderStyle());
		headerRow.addCell(col++, translator.translate("export.organisation.external.ref") + MANDATORY, workbook.getStyles().getHeaderStyle());
		headerRow.addCell(col++, translator.translate("table.header.lectures") + MANDATORY, workbook.getStyles().getHeaderStyle());
		headerRow.addCell(col++, translator.translate("curriculum.description"), workbook.getStyles().getHeaderStyle());
		headerRow.addCell(col++, translator.translate("export.creation.date"), workbook.getStyles().getHeaderStyle());
		headerRow.addCell(col++, translator.translate("export.last.modified"), workbook.getStyles().getHeaderStyle());
	}
	
	private void addCurriculumsContent(OpenXMLWorkbook workbook, OpenXMLWorksheet exportSheet) {
		for(Curriculum curriculum:curriculums) {
			curriculum = curriculumService.getCurriculum(curriculum);
			addCurriculumContent(curriculum, workbook, exportSheet);
		}
	}
	
	private void addCurriculumContent(Curriculum curriculum, OpenXMLWorkbook workbook, OpenXMLWorksheet exportSheet) {
		int col = 0;
		Row row = exportSheet.newRow();
		row.addCell(col++, curriculum.getKey(), null, null);
		row.addCell(col++, curriculum.getDisplayName());
		row.addCell(col++, curriculum.getIdentifier());
		
		String organisation = curriculum.getOrganisation() != null
				? curriculum.getOrganisation().getIdentifier()
				: null;
		row.addCell(col++, organisation);
		
		String lectures = curriculum.isLecturesEnabled() ? translator.translate("on") : translator.translate("off");
		row.addCell(col++, lectures.toUpperCase());
		row.addCell(col++, curriculum.getDescription());
		row.addCell(col++, curriculum.getCreationDate(), workbook.getStyles().getDateTimeStyle());
		row.addCell(col++, curriculum.getLastModified(), workbook.getStyles().getDateTimeStyle());
	}
	
	private void addImplementationsHeader(OpenXMLWorkbook workbook, OpenXMLWorksheet exportSheet) {
		exportSheet.setHeaderRows(1);
		
		int col = 0;
		Row headerRow = exportSheet.newRow();
		headerRow.addCell(col++, translator.translate("export.implementation.prod.external.ref") + MANDATORY, workbook.getStyles().getHeaderStyle());
		headerRow.addCell(col++, translator.translate("export.implementation.impl.external.ref") + MANDATORY, workbook.getStyles().getHeaderStyle());
		headerRow.addCell(col++, translator.translate("table.header.id"), workbook.getStyles().getHeaderStyle());
		headerRow.addCell(col++, translator.translate("export.implementation.object.type") + MANDATORY, workbook.getStyles().getHeaderStyle());
		headerRow.addCell(col++, translator.translate("export.number"), workbook.getStyles().getHeaderStyle());
		headerRow.addCell(col++, translator.translate("table.header.title") + MANDATORY, workbook.getStyles().getHeaderStyle());
		headerRow.addCell(col++, translator.translate("table.header.external.ref"), workbook.getStyles().getHeaderStyle());
		headerRow.addCell(col++, translator.translate("table.header.status"), workbook.getStyles().getHeaderStyle());
		headerRow.addCell(col++, translator.translate("export.start.date"), workbook.getStyles().getHeaderStyle());
		headerRow.addCell(col++, translator.translate("export.start.time"), workbook.getStyles().getHeaderStyle());
		headerRow.addCell(col++, translator.translate("export.end.date"), workbook.getStyles().getHeaderStyle());
		headerRow.addCell(col++, translator.translate("export.end.time"), workbook.getStyles().getHeaderStyle());
		headerRow.addCell(col++, translator.translate("export.lectures"), workbook.getStyles().getHeaderStyle());
		headerRow.addCell(col++, translator.translate("export.reference.id"), workbook.getStyles().getHeaderStyle());
		headerRow.addCell(col++, translator.translate("table.header.location"), workbook.getStyles().getHeaderStyle());
		headerRow.addCell(col++, translator.translate("export.element.type"), workbook.getStyles().getHeaderStyle());
		headerRow.addCell(col++, translator.translate("table.header.calendars"), workbook.getStyles().getHeaderStyle());
		headerRow.addCell(col++, translator.translate("table.header.lectures"), workbook.getStyles().getHeaderStyle());
		headerRow.addCell(col++, translator.translate("table.header.progress"), workbook.getStyles().getHeaderStyle());
		headerRow.addCell(col++, translator.translate("export.taxonomy"), workbook.getStyles().getHeaderStyle());
		headerRow.addCell(col++, translator.translate("export.creation.date"), workbook.getStyles().getHeaderStyle());
		headerRow.addCell(col++, translator.translate("export.last.modified"), workbook.getStyles().getHeaderStyle());
	}

	private void addCurriculumsImplementationsContent(OpenXMLWorkbook workbook, OpenXMLWorksheet exportSheet) {
		for(Curriculum curriculum:curriculums) {
			List<CurriculumElement> implementations = curriculumService.getImplementations(curriculum, CurriculumElementStatus.notDeleted());
			for(CurriculumElement implementation:implementations) {
				List<CurriculumElement> descendants = curriculumService.getCurriculumElementsDescendants(implementation);
				addContent(implementation, workbook, exportSheet);
				for(CurriculumElement descendant:descendants) {
					addContent(descendant,  workbook, exportSheet);
				}
				dbInstance.commitAndCloseSession();
			}
		}
	}
	
	private void addImplementationsContent(OpenXMLWorkbook workbook, OpenXMLWorksheet exportSheet) {
		for(CurriculumElement element:implementations) {
			element = curriculumService.getCurriculumElement(element);
			List<CurriculumElement> descendants = curriculumService.getCurriculumElementsDescendants(element);
			addContent(element, workbook, exportSheet);
			for(CurriculumElement descendant:descendants) {
				addContent(descendant,  workbook, exportSheet);
			}
			dbInstance.commitAndCloseSession();
		}
	}

	private void addContent(CurriculumElement element, OpenXMLWorkbook workbook, OpenXMLWorksheet exportSheet) {
		addCurriculumElementContent(element, workbook, exportSheet);

		List<RepositoryEntryInfos> entries = curriculumService.getRepositoryEntriesWithInfos(element);
		for(RepositoryEntryInfos entry:entries) {
			addRepositoryEntryContent(entry.repositoryEntry(), CurriculumExportType.COURSE, element, workbook, exportSheet);
		}
		
		List<RepositoryEntry> templates = curriculumService.getRepositoryTemplates(element);
		for(RepositoryEntry template:templates) {
			addRepositoryEntryContent(template, CurriculumExportType.TMPL, element, workbook, exportSheet);
		}
		
		LecturesBlockSearchParameters searchParams = new LecturesBlockSearchParameters();
		searchParams.setCurriculumElementPath(null);
		searchParams.setCurriculumElement(element);
		searchParams.setLectureConfiguredRepositoryEntry(false);
		
		List<LectureBlockWithTeachers> blocks = lectureService.getLectureBlocksWithOptionalTeachers(searchParams);
		for(LectureBlockWithTeachers block:blocks) {
			addLectureBlockContent(block.lectureBlock(), element, workbook, exportSheet);
		}
	}
	
	private void addCurriculumElementContent(CurriculumElement element, OpenXMLWorkbook workbook, OpenXMLWorksheet exportSheet) {
		int col = 0;
		Row row = exportSheet.newRow();
		String curriculumExternalRef = StringHelper.containsNonWhitespace(element.getCurriculum().getIdentifier())
				? element.getCurriculum().getIdentifier()
				: element.getCurriculum().getDisplayName();
		row.addCell(col++, curriculumExternalRef);

		String implementationExternalRef;
		if(element.getImplementation() == null) {
			implementationExternalRef = StringHelper.containsNonWhitespace(element.getIdentifier())
					? element.getIdentifier()
					: element.getDisplayName();
		} else {
			implementationExternalRef = StringHelper.containsNonWhitespace(element.getImplementation().getIdentifier())
					? element.getImplementation().getIdentifier()
					: element.getImplementation().getDisplayName();
		}
		row.addCell(col++, implementationExternalRef);
		row.addCell(col++, element.getKey(), null, null);
		
		if(element.getParent() == null) {
			row.addCell(col++, CurriculumExportType.IMPL.name());
		} else {
			row.addCell(col++, CurriculumExportType.ELEM.name());
		}
		row.addCell(col++, element.getNumberImpl());
		row.addCell(col++, element.getDisplayName());
		row.addCell(col++, element.getIdentifier());
		row.addCell(col++, element.getElementStatus().name().toUpperCase());
	
		row.addCell(col++, element.getBeginDate(), workbook.getStyles().getDateStyle());
		row.addCell(col++, null);// No time
		row.addCell(col++, element.getEndDate(), workbook.getStyles().getDateStyle());
		row.addCell(col++, null);// No time
		row.addCell(col++, null);// No lectures
		row.addCell(col++, null);// No ref. id.
		row.addCell(col++, null);// No location
		
		String elementType = element.getType() != null
				? element.getType().getIdentifier()
				: null;
		row.addCell(col++, elementType);
		row.addCell(col++, getValue(element.getCalendars()));
		row.addCell(col++, getValue(element.getLectures()));
		row.addCell(col++, getValue(element.getLearningProgress()));
		
		String taxonomy = getTaxonomyLevels(element);
		row.addCell(col++, taxonomy);
		row.addCell(col++, element.getCreationDate(), workbook.getStyles().getDateTimeStyle());
		row.addCell(col++, element.getLastModified(), workbook.getStyles().getDateTimeStyle());
	}
	
	private String getTaxonomyLevels(CurriculumElement element) {
		List<TaxonomyLevel> levels = curriculumService.getTaxonomy(element);
		if(levels == null || levels.isEmpty()) return null;
		
		List<String> displayNames = levels.stream()
				.map(level -> TaxonomyUIFactory.translateDisplayName(translator, level))
				.filter(Objects::nonNull)
				.toList();
		return String.join("; ", displayNames);
	}
	
	private String getValue(CurriculumCalendars val) {
		if(val == null) return CurriculumExportOnOff.DEFAULT.name();
		return switch(val) {
			case enabled -> CurriculumExportOnOff.ON.name();
			case disabled -> CurriculumExportOnOff.OFF.name();
			default -> CurriculumExportOnOff.DEFAULT.name();
		};
	}
	
	private String getValue(CurriculumLectures val) {
		if(val == null) return CurriculumExportOnOff.DEFAULT.name();
		return switch(val) {
			case enabled -> CurriculumExportOnOff.ON.name();
			case disabled -> CurriculumExportOnOff.OFF.name();
			default -> CurriculumExportOnOff.DEFAULT.name();
		};
	}
	
	private String getValue(CurriculumLearningProgress val) {
		if(val == null) return CurriculumExportOnOff.DEFAULT.name();
		return switch(val) {
			case enabled -> CurriculumExportOnOff.ON.name();
			case disabled -> CurriculumExportOnOff.OFF.name();
			default -> CurriculumExportOnOff.DEFAULT.name();
		};
	}
	
	private void addRepositoryEntryContent(RepositoryEntry entry, CurriculumExportType type, CurriculumElement element,
			OpenXMLWorkbook workbook, OpenXMLWorksheet exportSheet) {
		int col = 0;
		Row row = exportSheet.newRow();
		String curriculumExternalRef = StringHelper.containsNonWhitespace(element.getCurriculum().getIdentifier())
				? element.getCurriculum().getIdentifier()
				: element.getCurriculum().getDisplayName();
		row.addCell(col++, curriculumExternalRef);

		String implementationExternalRef;
		if(element.getImplementation() == null) {
			implementationExternalRef = StringHelper.containsNonWhitespace(element.getIdentifier())
					? element.getIdentifier()
					: element.getDisplayName();
		} else {
			implementationExternalRef = StringHelper.containsNonWhitespace(element.getImplementation().getIdentifier())
					? element.getImplementation().getIdentifier()
					: element.getImplementation().getDisplayName();
		}
		row.addCell(col++, implementationExternalRef);
		row.addCell(col++, entry.getKey(), null, null);
		row.addCell(col++, type.name());

		row.addCell(col++, element.getNumberImpl());// No number impl
		row.addCell(col++, entry.getDisplayname());
		row.addCell(col++, entry.getExternalRef());
		row.addCell(col++, entry.getStatus().toUpperCase());
		
		Date validFrom = entry.getLifecycle() != null
				? entry.getLifecycle().getValidFrom()
				: null;
		Date validTo = entry.getLifecycle() != null
				? entry.getLifecycle().getValidTo()
				: null;
	
		row.addCell(col++, validFrom, workbook.getStyles().getDateStyle());
		row.addCell(col++, null);// No time
		row.addCell(col++, validTo, workbook.getStyles().getDateStyle());
		row.addCell(col++, null);// No time
		row.addCell(col++, null);// No lectures
		row.addCell(col++, null);// No ref. id.
		row.addCell(col++, null);// No location
		
		row.addCell(col++, null); // No element type
		row.addCell(col++, null); // No calendar option
		row.addCell(col++, null); // No lectures option
		row.addCell(col++, null); // No learning progress option
		
		String taxonomy = getTaxonomyLevels(entry);
		row.addCell(col++, taxonomy);
		row.addCell(col++, entry.getCreationDate(), workbook.getStyles().getDateTimeStyle());
		row.addCell(col++, entry.getLastModified(), workbook.getStyles().getDateTimeStyle());
	}
	
	private String getTaxonomyLevels(RepositoryEntry entry) {
		List<TaxonomyLevel> levels = repositoryService.getTaxonomy(entry);
		if(levels == null || levels.isEmpty()) return null;
		
		List<String> displayNames = levels.stream()
				.map(level -> TaxonomyUIFactory.translateDisplayName(translator, level))
				.filter(Objects::nonNull)
				.toList();
		return String.join("; ", displayNames);
	}
	
	private void addLectureBlockContent(LectureBlock lectureBlock, CurriculumElement element,
			OpenXMLWorkbook workbook, OpenXMLWorksheet exportSheet) {
		int col = 0;
		Row row = exportSheet.newRow();
		String curriculumExternalRef = StringHelper.containsNonWhitespace(element.getCurriculum().getIdentifier())
				? element.getCurriculum().getIdentifier()
				: element.getCurriculum().getDisplayName();
		row.addCell(col++, curriculumExternalRef);

		String implementationExternalRef;
		if(element.getImplementation() == null) {
			implementationExternalRef = StringHelper.containsNonWhitespace(element.getIdentifier())
					? element.getIdentifier()
					: element.getDisplayName();
		} else {
			implementationExternalRef = StringHelper.containsNonWhitespace(element.getImplementation().getIdentifier())
					? element.getImplementation().getIdentifier()
					: element.getImplementation().getDisplayName();
		}
		row.addCell(col++, implementationExternalRef);
		row.addCell(col++, lectureBlock.getKey(), null, null);
		row.addCell(col++, CurriculumExportType.EVENT.name());

		row.addCell(col++, element.getNumberImpl());// Numbering
		row.addCell(col++, lectureBlock.getTitle());
		row.addCell(col++, lectureBlock.getExternalRef());
		row.addCell(col++, null);//No status

		row.addCell(col++, lectureBlock.getStartDate(), workbook.getStyles().getDateStyle());
		String startTime = lectureBlock.getStartDate() != null
				? formatter.formatTimeShort(lectureBlock.getStartDate())
				: null;
		row.addCell(col++, startTime);
		row.addCell(col++, lectureBlock.getEndDate(), workbook.getStyles().getDateStyle());
		String endTime = lectureBlock.getEndDate() != null
				? formatter.formatTimeShort(lectureBlock.getEndDate())
				: null;
		row.addCell(col++, endTime);
		
		row.addCell(col++, lectureBlock.getPlannedLecturesNumber(), workbook.getStyles().getIntegerStyle());
		Long entryKey = lectureBlock.getEntry() != null
				? lectureBlock.getEntry().getKey()
				: null;
		row.addCell(col++, entryKey, null);// Ref. to course
		row.addCell(col++, lectureBlock.getLocation());// Location
		
		row.addCell(col++, null); // No element type
		row.addCell(col++, null); // No calendar option
		row.addCell(col++, null); // No lectures option
		row.addCell(col++, null); // No learning progress option
		
		String taxonomy = getTaxonomyLevels(lectureBlock);
		row.addCell(col++, taxonomy);
		row.addCell(col++, lectureBlock.getCreationDate(), workbook.getStyles().getDateTimeStyle());
		row.addCell(col++, lectureBlock.getLastModified(), workbook.getStyles().getDateTimeStyle());
	}
	
	private String getTaxonomyLevels(LectureBlock lectureBlock) {
		List<TaxonomyLevel> levels = lectureService.getTaxonomy(lectureBlock);
		if(levels == null || levels.isEmpty()) return null;
		
		List<String> displayNames = levels.stream()
				.map(level -> TaxonomyUIFactory.translateDisplayName(translator, level))
				.filter(Objects::nonNull)
				.toList();
		return String.join("; ", displayNames);
	}
	
	private void addInformations(OpenXMLWorkbook workbook, OpenXMLWorksheet exportSheet) {
		exportSheet.setHeaderRows(1);

		Row headerRow = exportSheet.newRow();
		headerRow.addCell(0, translator.translate("export.information.info"), workbook.getStyles().getHeaderStyle());
		headerRow.addCell(1, translator.translate("export.information.value"), workbook.getStyles().getHeaderStyle());
		// URL
		Row row = exportSheet.newRow();
		row.addCell(0, translator.translate("export.information.url"), workbook.getStyles().getHeaderStyle());
		row.addCell(1, url, null);
		// Version
		row = exportSheet.newRow();
		row.addCell(0, translator.translate("export.information.version"), workbook.getStyles().getHeaderStyle());
		row.addCell(1, Settings.getVersion(), null);
		// Language
		row = exportSheet.newRow();
		row.addCell(0, translator.translate("export.information.language"), workbook.getStyles().getHeaderStyle());
		row.addCell(1, translator.getLocale().getDisplayLanguage(), null);
		// Date
		row = exportSheet.newRow();
		row.addCell(0, translator.translate("export.information.date"), workbook.getStyles().getHeaderStyle());
		row.addCell(1, new Date(), workbook.getStyles().getDateTimeStyle());
		// Date
		row = exportSheet.newRow();
		row.addCell(0, translator.translate("export.information.by"), workbook.getStyles().getHeaderStyle());
		String fullname = userManager.getUserDisplayName(identity);
		row.addCell(1, fullname, null);
	}
}
