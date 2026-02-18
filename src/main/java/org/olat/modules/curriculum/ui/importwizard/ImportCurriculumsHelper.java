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
package org.olat.modules.curriculum.ui.importwizard;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.Logger;
import org.dhatim.fastexcel.reader.ReadableWorkbook;
import org.dhatim.fastexcel.reader.Row;
import org.dhatim.fastexcel.reader.Sheet;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.ModifiedInfo;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.openxml.AbstractExcelReader;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumElementToTaxonomyLevel;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumModule;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.CurriculumStatus;
import org.olat.modules.curriculum.ui.CurriculumExportType;
import org.olat.modules.curriculum.ui.importwizard.ImportCurriculumsReviewTableModel.ImportCurriculumsCols;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockToTaxonomyLevel;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyModule;
import org.olat.modules.taxonomy.TaxonomyRef;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryEntryToTaxonomyLevel;
import org.olat.repository.RepositoryModule;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 9 févr. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class ImportCurriculumsHelper extends AbstractExcelReader {
	
	private static final Logger log = Tracing.createLoggerFor(ImportCurriculumsHelper.class);
	
	private final Translator translator;
	
	public static final String ON = "ON";
	public static final String OFF = "OFF";
	public static final String DEFAULT = "DEFAULT";
	
	@Autowired
	private LectureService lectureService;
	@Autowired
	private TaxonomyModule taxonomyModule;
	@Autowired
	private CurriculumModule curriculumModule;
	@Autowired
	private RepositoryModule repositoryModule;
	@Autowired
	private TaxonomyService taxonomyService;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private OrganisationService organisationService;
	
	public ImportCurriculumsHelper(Translator translator) {
		CoreSpringFactory.autowireObject(this);
		this.translator = translator;
	}
	
	private String translate(String i18nKey) {
		return translator.translate(i18nKey);
	}
	
	public void loadCurrentCurriculums(List<ImportedRow> importedRows) {
		Map<String,Organisation> organisationMap = new HashMap<>();
		
		for(ImportedRow importedRow:importedRows) {
			List<Curriculum> curriculums = curriculumService.getCurriculumsByIdentifier(importedRow.getIdentifier(), CurriculumStatus.active);
			if(curriculums.isEmpty()) {
				importedRow.setStatus(ImportCurriculumsStatus.NEW);
			} else if(curriculums.size() == 1) {
				importedRow.setCurriculum(curriculums.get(0));
			} else {
				// Duplicate
				notUniqueIdentifierError(importedRow);
				importedRow.setStatus(ImportCurriculumsStatus.ERROR);
			}
			
			String organisationIdentifier = importedRow.getOrganisationIdentifier();
			if(StringHelper.containsNonWhitespace(organisationIdentifier)) {
				Organisation organisation = organisationMap.get(organisationIdentifier);
				if(organisation == null) {
					List<Organisation> organisations = organisationService.findOrganisationByIdentifier(organisationIdentifier);
					if(organisations.size() == 1) {
						organisation = organisations.get(0);
						organisationMap.put(organisationIdentifier, organisation);
					}
				}
				importedRow.setOrganisation(organisation);
			}
		}
	}
	
	public void loadCurrentElements(List<ImportedRow> importedRows, List<ImportedRow> curriculumsRows) {
		Map<String,ImportedRow> curriculumsMap = curriculumsRows.stream()
				.filter(r -> StringHelper.containsNonWhitespace(r.getCurriculumIdentifier()))
				.collect(Collectors.toMap(ImportedRow::getCurriculumIdentifier, r -> r, (u, v) -> u));
		
		List<CurriculumElementType> types = curriculumService.getCurriculumElementTypes();
		Map<String,CurriculumElementType> typesMap = types.stream()
				.filter(t -> StringHelper.containsNonWhitespace(t.getIdentifier()))
				.collect(Collectors.toMap(CurriculumElementType::getIdentifier, t -> t, (u, v) -> u));

		// First implementations
		Map<String,ImportedRow> implementations = new HashMap<>();
		for(ImportedRow importedRow:importedRows) {
			if(importedRow.type() == CurriculumExportType.IMPL) {
				loadElement(importedRow, curriculumsMap, typesMap);
				if(StringHelper.containsNonWhitespace(importedRow.getIdentifier())) {
					implementations.put(importedRow.getIdentifier(), importedRow);
				}
			}
		}
		
		Map<String,ImportedRow> elements = new HashMap<>();
		for(ImportedRow importedRow:importedRows) {
			if(importedRow.type() == CurriculumExportType.ELEM) {
				loadElement(importedRow, curriculumsMap, typesMap);
				if(StringHelper.containsNonWhitespace(importedRow.getIdentifier())) {
					elements.put(importedRow.getIdentifier(), importedRow);
				}
				if(StringHelper.containsNonWhitespace(importedRow.getImplementationIdentifier())) {
					importedRow.setImplementationRow(implementations.get(importedRow.getImplementationIdentifier()));
				}
			}
		}

		Map<String,RepositoryEntry> entries = new HashMap<>();
		for(ImportedRow importedRow:importedRows) {
			if(importedRow.type() == CurriculumExportType.COURSE || importedRow.type() == CurriculumExportType.TMPL) {
				RepositoryEntry entry = loadRepositoryEntry(importedRow, implementations, elements, curriculumsMap);
				if(entry != null) {
					entries.put(entry.getExternalRef(), entry);
				}
			}
		}
		
		for(ImportedRow importedRow:importedRows) {
			if(importedRow.type() == CurriculumExportType.EVENT) {
				loadLectureBlock(importedRow, entries, implementations, elements, curriculumsMap);
			}
		}
	}
	
	public void loadTaxonomy(List<ImportedRow> importedRows) {
		if(!taxonomyModule.isEnabled()) return;
		
		List<TaxonomyRef> taxonomyRefs = new ArrayList<>();
		taxonomyRefs.addAll(repositoryModule.getTaxonomyRefs());
		taxonomyRefs.addAll(curriculumModule.getTaxonomyRefs());
		if(taxonomyRefs.isEmpty()) return;
		
		List<TaxonomyLevel> levels = taxonomyService.getTaxonomyLevels(taxonomyRefs);
		Map<String,TaxonomyLevel> levelsMap = levels.stream()
				.filter(t -> StringHelper.containsNonWhitespace(t.getMaterializedPathIdentifiers()))
		        .collect(Collectors.toMap(TaxonomyLevel::getMaterializedPathIdentifiers, t -> t, (u, v) -> u));

		for(ImportedRow importedRow:importedRows) {
			String subjects = importedRow.getSubjects();
			if(StringHelper.containsNonWhitespace(subjects)) {
				String[] subjectsArr = subjects.split(";");
				for(String subject:subjectsArr) {
					if(StringHelper.containsNonWhitespace(subject)) {
						TaxonomyLevel level = levelsMap.get(subject);
						if(level != null) {
							importedRow.addTaxonomyLevel(level);
						}
					}
				}
			}
		}
	}
	
	private RepositoryEntry loadRepositoryEntry(ImportedRow importedRow,
			Map<String,ImportedRow> implementations, Map<String,ImportedRow> elements,
			Map<String,ImportedRow> curriculumsMap) {
		
		List<RepositoryEntry> entries = repositoryService.loadRepositoryEntriesByExternalRef(importedRow.getIdentifier());
		RepositoryEntry entry = null;
		if(entries.isEmpty()) {
			importedRow.setStatus(ImportCurriculumsStatus.ERROR);
		} else if(entries.size() == 1) {
			if(importedRow.type() == CurriculumExportType.COURSE) {
				importedRow.setCourse(entries.get(0));
				loadTaxonomyLevels(importedRow);
				entry = entries.get(0);
			} else if(importedRow.type() == CurriculumExportType.TMPL) {
				importedRow.setTemplate(entries.get(0));
				loadTaxonomyLevels(importedRow);
				entry = entries.get(0);
			}
		} else {
			// Duplicate
			notUniqueIdentifierError(importedRow);
			importedRow.setStatus(ImportCurriculumsStatus.ERROR);
		}
		
		if(StringHelper.containsNonWhitespace(importedRow.getImplementationIdentifier())) {
			if(implementations.containsKey(importedRow.getImplementationIdentifier())) {
				ImportedRow parentRow = implementations.get(importedRow.getImplementationIdentifier());
				importedRow.setImplementationRow(parentRow);
				importedRow.setCurriculumElementParentRow(parentRow);
			} else if(elements.containsKey(importedRow.getImplementationIdentifier())) {
				importedRow.setCurriculumElementParentRow(elements.get(importedRow.getImplementationIdentifier()));
			}
		}
		
		// Map curriculum
		if(StringHelper.containsNonWhitespace(importedRow.getCurriculumIdentifier())) {
			importedRow.setCurriculumRow(curriculumsMap.get(importedRow.getCurriculumIdentifier()));
		} 
		
		return entry;
	}
	
	private LectureBlock loadLectureBlock(ImportedRow importedRow,
			Map<String,RepositoryEntry> entries, Map<String,ImportedRow> implementations,
			Map<String,ImportedRow> elements, Map<String,ImportedRow> curriculumsMap) {
		List<LectureBlock> lectureBlocks = lectureService.loadLectureBlocksByExternalRef(importedRow.getIdentifier());
		LectureBlock lectureBlock = null;
		
		if(lectureBlocks.isEmpty()) {
			importedRow.setStatus(ImportCurriculumsStatus.NEW);
		} else if(lectureBlocks.size() == 1) {
			importedRow.setLectureBlock(lectureBlocks.get(0));
			lectureBlock = lectureBlocks.get(0);
			loadTaxonomyLevels(importedRow);
		} else {
			// Duplicate
			notUniqueIdentifierError(importedRow);
			importedRow.setStatus(ImportCurriculumsStatus.ERROR);
		}
		
		if(StringHelper.containsNonWhitespace(importedRow.getReferenceExternalRef())) {
			RepositoryEntry course = entries.get(importedRow.getReferenceExternalRef());
			if(course == null) {
				// For error handling
				List<RepositoryEntry> moreEntries = repositoryService.loadRepositoryEntriesByExternalRef(importedRow.getReferenceExternalRef());
				if(moreEntries.size() == 1) {
					course = moreEntries.get(0);
				}
			}
			importedRow.setCourse(course);
		}
		
		if(StringHelper.containsNonWhitespace(importedRow.getImplementationIdentifier())) {
			if(implementations.containsKey(importedRow.getImplementationIdentifier())) {
				ImportedRow parentRow = implementations.get(importedRow.getImplementationIdentifier());
				importedRow.setImplementationRow(parentRow);
				importedRow.setCurriculumElementParentRow(parentRow);
			} else if(elements.containsKey(importedRow.getImplementationIdentifier())) {
				importedRow.setCurriculumElementParentRow(elements.get(importedRow.getImplementationIdentifier()));
			}
		}
		
		// Map curriculum
		if(StringHelper.containsNonWhitespace(importedRow.getCurriculumIdentifier())) {
			importedRow.setCurriculumRow(curriculumsMap.get(importedRow.getCurriculumIdentifier()));
		} 

		return lectureBlock;
	}

	private CurriculumElement loadElement(ImportedRow importedRow, Map<String,ImportedRow> curriculumsMap, Map<String,CurriculumElementType> typesMap) {
		String identifier = importedRow.getIdentifier();
		
		// Search existing element
		List<CurriculumElement> elements = curriculumService.searchCurriculumElements(null, identifier, null, CurriculumElementStatus.notDeleted());
		if(elements.isEmpty()) {
			importedRow.setStatus(ImportCurriculumsStatus.NEW);
		} else if(elements.size() == 1) {
			importedRow.setCurriculumElement(elements.get(0));
			if(importedRow.type() == CurriculumExportType.IMPL) {
				importedRow.setImplementation(elements.get(0));
			}
			loadTaxonomyLevels(importedRow);
		} else {
			// Duplicate
			notUniqueIdentifierError(importedRow);
			importedRow.setStatus(ImportCurriculumsStatus.ERROR);
		}
		
		// Map curriculum element type
		if(StringHelper.containsNonWhitespace(importedRow.getCurriculumElementTypeIdentifier())) {
			importedRow.setCurriculumElementType(typesMap.get(importedRow.getCurriculumElementTypeIdentifier()));
		}
		
		// Map curriculum
		if(StringHelper.containsNonWhitespace(importedRow.getCurriculumIdentifier())) {
			importedRow.setCurriculumRow(curriculumsMap.get(importedRow.getCurriculumIdentifier()));
		}
		
		return elements.size() == 1 ? elements.get(0) : null;
	}
	
	private List<String> loadTaxonomyLevels(ImportedRow importedRow) {
		List<String> levels;
		if((importedRow.type() == CurriculumExportType.IMPL || importedRow.type() == CurriculumExportType.ELEM)
				&& importedRow.getCurriculumElement() != null) {
			levels = importedRow.getCurriculumElement().getTaxonomyLevels().stream()
					.map(CurriculumElementToTaxonomyLevel::getTaxonomyLevel)
					.map(TaxonomyLevel::getMaterializedPathIdentifiers)
					.toList();
		} else if(importedRow.type() == CurriculumExportType.COURSE && importedRow.getCourse() != null) {
			levels = importedRow.getCourse().getTaxonomyLevels().stream()
					.map(RepositoryEntryToTaxonomyLevel::getTaxonomyLevel)
					.map(TaxonomyLevel::getMaterializedPathIdentifiers)
					.toList();
		} else if(importedRow.type() == CurriculumExportType.TMPL && importedRow.getTemplate() != null) {
			levels = importedRow.getTemplate().getTaxonomyLevels().stream()
					.map(RepositoryEntryToTaxonomyLevel::getTaxonomyLevel)
					.map(TaxonomyLevel::getMaterializedPathIdentifiers)
					.toList();
		} else if(importedRow.type() == CurriculumExportType.EVENT && importedRow.getLectureBlock() != null) {
			levels = importedRow.getLectureBlock().getTaxonomyLevels().stream()
					.map(LectureBlockToTaxonomyLevel::getTaxonomyLevel)
					.map(TaxonomyLevel::getMaterializedPathIdentifiers)
					.toList();
		} else {
			levels = List.of();
		}
		return levels;
	}
	
	public void validateCurriculumsUniqueIdentifiers(List<ImportedRow> importedRows) {
		Map<String, ImportedRow> identifiersMap = new HashMap<>();
		for(ImportedRow row:importedRows) {
			String identifier = row.getIdentifier();
			if(identifiersMap.containsKey(identifier)) {
				notUniqueIdentifierError(row);
				notUniqueIdentifierError(identifiersMap.get(identifier));
			} else {
				identifiersMap.put(identifier, row);
			}
		}
	}
	
	public void validateUniqueIdentifiers(List<ImportedRow> importedRows) {
		Map<String, ImportedRow> elementsIdentifiersMap = new HashMap<>();
		Map<String, ImportedRow> eventsIdentifiersMap = new HashMap<>();
		for(ImportedRow row:importedRows) {
			String identifier = row.getIdentifier();
			if(row.type() == CurriculumExportType.IMPL
					|| row.type() == CurriculumExportType.ELEM) {
				if(elementsIdentifiersMap.containsKey(identifier)) {
					notUniqueIdentifierError(row);
					notUniqueIdentifierError(elementsIdentifiersMap.get(identifier));
				} else {
					elementsIdentifiersMap.put(identifier, row);
				}
			} else if(row.type() == CurriculumExportType.EVENT) {
				if(eventsIdentifiersMap.containsKey(identifier)) {
					notUniqueIdentifierError(row);
					notUniqueIdentifierError(eventsIdentifiersMap.get(identifier));
				} else {
					eventsIdentifiersMap.put(identifier, row);
				}
			}
			
			//TODO import course, template
		}
	}
	
	private void notUniqueIdentifierError(ImportedRow importedRow) {
		String column = translator.translate(ImportCurriculumsCols.identifier.i18nHeaderKey());
		importedRow.addValidationError(ImportCurriculumsCols.identifier, column, null, translate("error.value.duplicate"));
	}
	
	public void validate(ImportedRow importedRow, Roles roles) {
		CurriculumExportType type = importedRow.type();
		if(type == CurriculumExportType.CUR) {
			validateCurriculumRow(importedRow, roles);
		} else if(type == CurriculumExportType.IMPL || type == CurriculumExportType.ELEM) {
			validateCurriculumElementRow(importedRow);
		} else if(type == CurriculumExportType.COURSE) {
			validateCourseRow(importedRow);
		} else if(type == CurriculumExportType.TMPL) {
			validateTemplateRow(importedRow);
		} else if(type == CurriculumExportType.EVENT) {
			validateLectureBlockRow(importedRow);
		} else {
			unkownType(importedRow);
		}
		
		if(importedRow.getStatus() == null) {
			CurriculumImportedStatistics statistics = importedRow.getValidationStatistics();
			if(statistics.errors() > 0) {
				importedRow.setStatus(ImportCurriculumsStatus.ERROR);
			} else if(statistics.changes() > 0) {
				importedRow.setStatus(ImportCurriculumsStatus.MODIFIED);
			} else {
				importedRow.setStatus(ImportCurriculumsStatus.NO_CHANGES);
			}
		}
	}
	
	private void validateCurriculumElementRow(ImportedRow importedRow) {
		// Curriculum and implementation
		validateCurriculumAndImplementation(importedRow);
		
		// Display name / title
		if(validateMandatory(importedRow, importedRow.getDisplayName(), ImportCurriculumsCols.displayName)
				&& validateTruncateLength(importedRow, importedRow.getDisplayName(), 255, ImportCurriculumsCols.displayName)
				&& !importedRow.isNew()) {
			String column = translator.translate(ImportCurriculumsCols.displayName.i18nHeaderKey());
			importedRow.addChanged(column, importedRow.getCurriculumElement().getDisplayName(), importedRow.getDisplayName(), ImportCurriculumsCols.displayName);
		}
		
		validateIdentifier(importedRow);
		
		// Curriculum element type
		if(validateMandatory(importedRow, importedRow.getCurriculumElementTypeIdentifier(), ImportCurriculumsCols.elementType)) {
			String column = translator.translate(ImportCurriculumsCols.elementType.i18nHeaderKey());
			if(importedRow.getCurriculumElementType() == null) {
				importedRow.addValidationError(ImportCurriculumsCols.elementType, column, importedRow.getCurriculumElementTypeIdentifier(),
						translator.translate("error.not.exist", importedRow.getCurriculumElementTypeIdentifier()));
			} else if(!importedRow.isNew() && !Objects.equals(importedRow.getCurriculumElement().getType(), importedRow.getCurriculumElementType())) {
				importedRow.addValidationError(ImportCurriculumsCols.referenceIdentifier, column, importedRow.getCurriculumElementTypeIdentifier(), translator.translate("error.no.update"));
			}
		}
		
		// Status
		if(validateMandatory(importedRow, importedRow.getElementStatus(), ImportCurriculumsCols.elementStatus)) {
			if(!validateCurriculumElementStatus(importedRow.getElementStatus())) {
				String column = translator.translate(ImportCurriculumsCols.elementStatus.i18nHeaderKey());
				if(importedRow.isNew()) {
					importedRow.addValidationError(ImportCurriculumsCols.elementStatus, column,
							importedRow.getElementStatus(), translator.translate("warning.value.not.supported", importedRow.getElementStatus()));
				} else {
					importedRow.addValidationWarning(ImportCurriculumsCols.elementStatus, column,
							importedRow.getElementStatus(), translator.translate("warning.value.not.supported.ignore", importedRow.getElementStatus()));
				}
			}
		}
		
		// Dates
		if(validateDate(importedRow, importedRow.getStartDate(), ImportCurriculumsCols.startDate, true)
				&& importedRow.getStartDate() != null && !importedRow.isNew()) {
			String column = translator.translate(ImportCurriculumsCols.startDate.i18nHeaderKey());
			importedRow.addChanged(column, importedRow.getCurriculumElement().getBeginDate(), importedRow.getStartDate().date(), ImportCurriculumsCols.startDate);
		}
		validateEmpty(importedRow, importedRow.getStartTime(), ImportCurriculumsCols.startTime);
		
		if(validateDate(importedRow, importedRow.getEndDate(), ImportCurriculumsCols.endDate, true)
				&& validateAfter(importedRow, ImportCurriculumsCols.endDate)
				&& importedRow.getEndDate() != null && !importedRow.isNew()) {
			String column = translator.translate(ImportCurriculumsCols.endDate.i18nHeaderKey());
			importedRow.addChanged(column, importedRow.getCurriculumElement().getEndDate(), importedRow.getEndDate().date(), ImportCurriculumsCols.endDate);
		}
		validateEmpty(importedRow, importedRow.getEndTime(), ImportCurriculumsCols.endTime);
		
		// Absences / calendar / progress
		if(validateMandatory(importedRow, importedRow.getAbsences(), ImportCurriculumsCols.absences)
				&& validateOnOff(importedRow, ImportCurriculumsCols.absences, importedRow.getAbsences())
				&& !importedRow.isNew()) {
			changes(importedRow, toExcelFormat(importedRow.getCurriculumElement().getLectures()), importedRow.getAbsences(), ImportCurriculumsCols.absences);
		}
		if(validateMandatory(importedRow, importedRow.getCalendar(), ImportCurriculumsCols.calendar)
				&& validateOnOff(importedRow, ImportCurriculumsCols.calendar, importedRow.getCalendar())
				&& !importedRow.isNew()) {
			changes(importedRow, toExcelFormat(importedRow.getCurriculumElement().getCalendars()), importedRow.getCalendar(), ImportCurriculumsCols.calendar);
		}
		if(validateMandatory(importedRow, importedRow.getProgress(), ImportCurriculumsCols.progress)
				&& validateOnOff(importedRow, ImportCurriculumsCols.progress, importedRow.getProgress())
				&& !importedRow.isNew()) {
			changes(importedRow, toExcelFormat(importedRow.getCurriculumElement().getLearningProgress()), importedRow.getProgress(), ImportCurriculumsCols.progress);
		}
		
		// Taxonomy
		validateTaxonomy(importedRow);
		
		// Last modified
		validateLastModified(importedRow, importedRow.getCurriculumElement());
	}
	

	private void validateCourseRow(ImportedRow importedRow) {
		// Curriculum and implementation
		validateCurriculumAndImplementation(importedRow);
				
		RepositoryEntry entry = importedRow.getCourse();
		// Display name / title
		if(validateMandatory(importedRow, importedRow.getDisplayName(), ImportCurriculumsCols.displayName)
				&& validateTruncateLength(importedRow, importedRow.getDisplayName(), 255, ImportCurriculumsCols.displayName)
				&& entry != null) {
			String column = translator.translate(ImportCurriculumsCols.displayName.i18nHeaderKey());
			importedRow.addChanged(column, importedRow.getCourse().getDisplayname(), importedRow.getDisplayName(), ImportCurriculumsCols.displayName);
		}
		
		validateIdentifier(importedRow);
		validateRepositoryEntry(importedRow, entry, ImportCurriculumsCols.identifier);
		
		// Dates
		if(validateDate(importedRow, importedRow.getStartDate(), ImportCurriculumsCols.startDate, true)
				&& importedRow.getStartDate() != null && !importedRow.isNew()) {
			String column = translator.translate(ImportCurriculumsCols.startDate.i18nHeaderKey());
			Date from = importedRow.getCourse().getLifecycle() == null ? null : importedRow.getCourse().getLifecycle().getValidFrom();
			importedRow.addChanged(column, from, importedRow.getStartDate().date(), ImportCurriculumsCols.startDate);
		}
		validateEmpty(importedRow, importedRow.getStartTime(), ImportCurriculumsCols.startTime);
		
		if(validateDate(importedRow, importedRow.getEndDate(), ImportCurriculumsCols.endDate, true)
				&& validateAfter(importedRow, ImportCurriculumsCols.endDate)
				&& importedRow.getEndDate() != null && !importedRow.isNew()) {
			String column = translator.translate(ImportCurriculumsCols.endDate.i18nHeaderKey());
			Date to = importedRow.getCourse().getLifecycle() == null ? null : importedRow.getCourse().getLifecycle().getValidTo();
			importedRow.addChanged(column, to, importedRow.getEndDate().date(), ImportCurriculumsCols.endDate);
		}
		validateEmpty(importedRow, importedRow.getEndTime(), ImportCurriculumsCols.endTime);
		
		// Taxonomy
		validateTaxonomy(importedRow);
		
		// Last modified
		validateLastModified(importedRow, entry);
	}
	
	private void validateTemplateRow(ImportedRow importedRow) {
		// Curriculum and implementation
		validateCurriculumAndImplementation(importedRow);
				
		RepositoryEntry entry = importedRow.getTemplate();
		// Display name / title
		if(validateMandatory(importedRow, importedRow.getDisplayName(), ImportCurriculumsCols.displayName)
				&& validateTruncateLength(importedRow, importedRow.getDisplayName(), 255, ImportCurriculumsCols.displayName)
				&& entry != null) {
			String column = translator.translate(ImportCurriculumsCols.displayName.i18nHeaderKey());
			importedRow.addChanged(column, importedRow.getTemplate().getDisplayname(), importedRow.getDisplayName(), ImportCurriculumsCols.displayName);
		}

		validateIdentifier(importedRow);
		validateRepositoryEntry(importedRow, entry, ImportCurriculumsCols.identifier);
		
		// Dates
		if(validateDate(importedRow, importedRow.getStartDate(), ImportCurriculumsCols.startDate, true)
				&& importedRow.getStartDate() != null && !importedRow.isNew()) {
			String column = translator.translate(ImportCurriculumsCols.startDate.i18nHeaderKey());
			Date from = importedRow.getTemplate().getLifecycle() == null ? null : importedRow.getTemplate().getLifecycle().getValidFrom();
			importedRow.addChanged(column, from, importedRow.getStartDate().date(), ImportCurriculumsCols.startDate);
		}
		validateEmpty(importedRow, importedRow.getStartTime(), ImportCurriculumsCols.startTime);
		
		if(validateDate(importedRow, importedRow.getEndDate(), ImportCurriculumsCols.endDate, true)
				&& validateAfter(importedRow, ImportCurriculumsCols.endDate)
				&& importedRow.getEndDate() != null && !importedRow.isNew()) {
			String column = translator.translate(ImportCurriculumsCols.endDate.i18nHeaderKey());
			Date to = importedRow.getTemplate().getLifecycle() == null ? null : importedRow.getTemplate().getLifecycle().getValidTo();
			importedRow.addChanged(column, to, importedRow.getEndDate().date(), ImportCurriculumsCols.endDate);
		}
		validateEmpty(importedRow, importedRow.getEndTime(), ImportCurriculumsCols.endTime);
		
		// Status
		if(validateMandatory(importedRow, importedRow.getElementStatus(), ImportCurriculumsCols.elementStatus)) {
			if(!validateRepositoryEntryStatus(importedRow.getElementStatus())) {
				String column = translator.translate(ImportCurriculumsCols.elementStatus.i18nHeaderKey());
				if(importedRow.isNew()) {
					importedRow.addValidationError(ImportCurriculumsCols.elementStatus, column,
							importedRow.getElementStatus(), translator.translate("warning.value.not.supported", importedRow.getElementStatus()));
				} else {
					importedRow.addValidationWarning(ImportCurriculumsCols.elementStatus, column,
							importedRow.getElementStatus(), translator.translate("warning.value.not.supported.ignore", importedRow.getElementStatus()));
				}
			}
		}
		
		// Taxonomy
		validateTaxonomy(importedRow);

		// Last modified
		validateLastModified(importedRow, entry);
	}
	
	private boolean validateRepositoryEntry(ImportedRow importedRow, RepositoryEntry entry, ImportCurriculumsCols col) {
		boolean allOk = true;
		if(entry == null) {
			String column = translator.translate(col.i18nHeaderKey());
			importedRow.addValidationError(col, column, null, translator.translate("error.not.exist", importedRow.type().name()));
			allOk &= false;
		}
		return allOk;
	}
	
	private void validateLectureBlockRow(ImportedRow importedRow) {
		// Curriculum and implementation
		validateCurriculumAndImplementation(importedRow);
		
		// Display name / title
		if(validateMandatory(importedRow, importedRow.getDisplayName(), ImportCurriculumsCols.displayName)
				&& validateTruncateLength(importedRow, importedRow.getDisplayName(), 255, ImportCurriculumsCols.displayName)
				&& !importedRow.isNew()) {
			String column = translator.translate(ImportCurriculumsCols.displayName.i18nHeaderKey());
			importedRow.addChanged(column, importedRow.getLectureBlock().getTitle(), importedRow.getDisplayName(), ImportCurriculumsCols.displayName);
		}
		
		validateIdentifier(importedRow);

		// Reference to course
		String referenceColumn = translator.translate(ImportCurriculumsCols.referenceIdentifier.i18nHeaderKey());
		if(!StringHelper.containsNonWhitespace(importedRow.getReferenceExternalRef())) {
			importedRow.addValidationError(ImportCurriculumsCols.referenceIdentifier, referenceColumn,
					translator.translate("error.no.value"), translator.translate("error.value.required"));
		} else if(importedRow.getCourse() == null) {
			importedRow.addValidationError(ImportCurriculumsCols.referenceIdentifier, referenceColumn,
					null, translator.translate("error.not.exist", importedRow.getReferenceExternalRef()));
		} else if(importedRow.getLectureBlock() != null && importedRow.getCourse() != null
				&& !Objects.equals(importedRow.getLectureBlock().getEntry(), importedRow.getCourse())) {
			importedRow.addValidationError(ImportCurriculumsCols.referenceIdentifier, referenceColumn,
					null, translator.translate("error.no.update"));
		}
		
		// Dates
		if(validateMandatory(importedRow, importedRow.getStartDate(), ImportCurriculumsCols.startDate)
				&& validateDate(importedRow, importedRow.getStartDate(), ImportCurriculumsCols.startDate, true)
				&& !importedRow.isNew()) {
			String column = translator.translate(ImportCurriculumsCols.startDate.i18nHeaderKey());
			importedRow.addChanged(column, importedRow.getLectureBlock().getStartDate(), importedRow.getStartDate().date(), ImportCurriculumsCols.startDate);
		}
		if(validateMandatory(importedRow, importedRow.getStartTime(), ImportCurriculumsCols.startTime)
				&& validateTime(importedRow, importedRow.getStartTime(), ImportCurriculumsCols.startTime, true)
				&& !importedRow.isNew()) {
			String column = translator.translate(ImportCurriculumsCols.startTime.i18nHeaderKey());
			importedRow.addChanged(column, importedRow.getLectureBlock().getStartDate(), importedRow.getStartTime().time(), ImportCurriculumsCols.startTime);
		}
		if(validateMandatory(importedRow, importedRow.getEndDate(), ImportCurriculumsCols.endDate)
				&& validateDate(importedRow, importedRow.getEndDate(), ImportCurriculumsCols.endDate, true)
				&& validateAfter(importedRow, ImportCurriculumsCols.endDate)
				&& !importedRow.isNew()) {
			String column = translator.translate(ImportCurriculumsCols.endDate.i18nHeaderKey());
			importedRow.addChanged(column, importedRow.getLectureBlock().getEndDate(), importedRow.getEndDate().date(), ImportCurriculumsCols.endDate);
		}
		if(validateMandatory(importedRow, importedRow.getEndTime(), ImportCurriculumsCols.endTime)
				&& validateTime(importedRow, importedRow.getEndTime(), ImportCurriculumsCols.endTime, true)
				&& !importedRow.isNew()) {
			String column = translator.translate(ImportCurriculumsCols.endTime.i18nHeaderKey());
			importedRow.addChanged(column, importedRow.getLectureBlock().getEndDate(), importedRow.getEndTime().time(), ImportCurriculumsCols.endTime);
		}
		
		// Unit
		if(StringHelper.containsNonWhitespace(importedRow.getLocation())
				&& validatePlannedLectures(importedRow, importedRow.getUnit(), ImportCurriculumsCols.unit)
				&& !importedRow.isNew()) {
			String col = translator.translate(ImportCurriculumsCols.unit.i18nHeaderKey());
			importedRow.addChanged(col, Integer.toString(importedRow.getLectureBlock().getPlannedLecturesNumber()), importedRow.getUnit(), ImportCurriculumsCols.unit);
		}
		
		// Location
		if(StringHelper.containsNonWhitespace(importedRow.getLocation())
				&& validateTruncateLength(importedRow, importedRow.getLocation(), 128, ImportCurriculumsCols.location)
				&& !importedRow.isNew()) {
			String col = translator.translate(ImportCurriculumsCols.displayName.i18nHeaderKey());
			importedRow.addChanged(col, importedRow.getLectureBlock().getLocation(), importedRow.getLocation(), ImportCurriculumsCols.location);
		}
		
		// Taxonomy
		validateTaxonomy(importedRow);
		
		// Last modified
		validateLastModified(importedRow, importedRow.getImplementation());
	}
	
	private void validateCurriculumRow(ImportedRow importedRow, Roles roles) {
		// Display name / title
		if(validateMandatory(importedRow, importedRow.getDisplayName(), ImportCurriculumsCols.displayName)
				&& validateTruncateLength(importedRow, importedRow.getDisplayName(), 255, ImportCurriculumsCols.displayName)
				&& !importedRow.isNew()) {
			String column = translator.translate(ImportCurriculumsCols.displayName.i18nHeaderKey());
			importedRow.addChanged(column, importedRow.getCurriculum().getDisplayName(), importedRow.getDisplayName(), ImportCurriculumsCols.displayName);
		}
		
		validateIdentifier(importedRow);
		
		// Absences
		if(validateMandatory(importedRow, importedRow.getAbsences(), ImportCurriculumsCols.absences)
				&& validateOnOff(importedRow, ImportCurriculumsCols.absences, importedRow.getAbsences())
				&& !importedRow.isNew()) {
			changes(importedRow, importedRow.getCurriculum().isLecturesEnabled(), "ON".equalsIgnoreCase(importedRow.getAbsences()), ImportCurriculumsCols.absences);
		}

		// Description
		if(importedRow.getCurriculum() != null) {
			String descriptionColumn = translator.translate(ImportCurriculumsCols.description.i18nHeaderKey());
			importedRow.addChanged(descriptionColumn, importedRow.getCurriculum().getDescription(), importedRow.getDescription(), ImportCurriculumsCols.description);
		}
		
		// Organisation
		if(validateMandatory(importedRow, importedRow.getOrganisationIdentifier(), ImportCurriculumsCols.organisationIdentifier)) {
			String organisationColumn = translator.translate(ImportCurriculumsCols.organisationIdentifier.i18nHeaderKey());
			if(importedRow.getOrganisation() == null) {
				String placeholder = StringHelper.containsNonWhitespace(importedRow.getOrganisationIdentifier())
						? null
						: translator.translate("error.no.value");
				importedRow.addValidationError(ImportCurriculumsCols.organisationIdentifier, organisationColumn,
						placeholder, translator.translate("error.value.required"));
			} else if(!hasOrganisationPermission(importedRow.getOrganisation(), roles)) {
				importedRow.addValidationError(ImportCurriculumsCols.organisationIdentifier, organisationColumn,
						null, translator.translate("error.permissions"));
			} else if(importedRow.getCurriculum() != null && !Objects.equals(importedRow.getCurriculum().getOrganisation(), importedRow.getOrganisation())) {
				importedRow.addValidationError(ImportCurriculumsCols.organisationIdentifier, organisationColumn,
						null, translator.translate("error.no.update"));
			}
		}
		
		// Last modified
		validateLastModified(importedRow, importedRow.getCurriculum());
	}
	
	/**
	 * Validate the curriculum identifier / external ref. and the implementation identifier
	 * / external ref. which are mandatory for all elements.
	 * @param importedRow The row
	 */
	private void validateCurriculumAndImplementation(ImportedRow importedRow) {
		if(validateMandatory(importedRow, importedRow.getCurriculumIdentifier(), ImportCurriculumsCols.curriculumIdentifier)) {
			if(importedRow.getCurriculum() == null &&  importedRow.getCurriculumRow() == null) {
				String column = translator.translate(ImportCurriculumsCols.curriculumIdentifier.i18nHeaderKey());
				importedRow.addValidationError(ImportCurriculumsCols.curriculumIdentifier, column,
						null, translator.translate("error.not.exist", importedRow.getCurriculumIdentifier()));
			}
		}
		
		if(validateMandatory(importedRow, importedRow.getImplementationIdentifier(), ImportCurriculumsCols.implementationIdentifier)) {
			if(importedRow.getImplementation() == null &&  importedRow.getImplementationRow() == null) {
				String column = translator.translate(ImportCurriculumsCols.implementationIdentifier.i18nHeaderKey());
				importedRow.addValidationError(ImportCurriculumsCols.implementationIdentifier, column,
						null, translator.translate("error.not.exist", importedRow.getImplementationIdentifier()));
			}
		}
	}
	
	private void validateIdentifier(ImportedRow importedRow) {
		// Identifier / external ref.
		validateMandatory(importedRow, importedRow.getIdentifier(), ImportCurriculumsCols.identifier);
		validateLength(importedRow, importedRow.getIdentifier(), 255, ImportCurriculumsCols.identifier);
	}
	
	private boolean hasOrganisationPermission(Organisation organisation, Roles roles) {
		return roles
				.getOrganisationsWithRoles(OrganisationRoles.curriculummanager, OrganisationRoles.administrator)
				.stream()
				.anyMatch(ref -> ref.getKey().equals(organisation.getKey()));
	}
	
	private boolean validateLastModified(ImportedRow importedRow, ModifiedInfo object) {
		boolean allOk = true;
		if(object != null && importedRow.getLastModified() != null) {
			LocalDateTime currentDate = DateUtils.toLocalDateTime(object.getLastModified())
					.withSecond(0).withNano(0);
			LocalDateTime date = importedRow.getLastModified()
					.withSecond(0).withNano(0);
			
			if(date.isBefore(currentDate)) {
				String column = translator.translate(ImportCurriculumsCols.lastModified.i18nHeaderKey());
				importedRow.addValidationWarning(ImportCurriculumsCols.lastModified, column, null,
						translator.translate("warning.last.modified"));
				allOk &= false;
			}
		}
		return allOk;
	}
	
	private boolean validateTaxonomy(ImportedRow importedRow) {
		boolean allOk = true;
		
		String subjects = importedRow.getSubjects();
		List<String> subjectsList = importedRow.getSubjectsList();
		List<String> currentPaths = loadTaxonomyLevels(importedRow);
		String column = translator.translate(ImportCurriculumsCols.taxonomyLevels.i18nHeaderKey());
		
		for(String subject:subjectsList) {
			if(importedRow.getTaxonomyLevel(subject) == null) {
				if(importedRow.isNew()) {
					importedRow.addValidationError(ImportCurriculumsCols.taxonomyLevels, column, null,
							translator.translate("error.not.exist", subjects));
					allOk &= false;
				} else {
					importedRow.addValidationWarning(ImportCurriculumsCols.taxonomyLevels, column, null,
							translator.translate("warning.not.exist.ignore", subjects));
					allOk &= false;
				}
				break;// only one error per field
			}
		}
		
		if(allOk && (!subjectsList.isEmpty() || !currentPaths.isEmpty())) {
			importedRow.addChanged(column, currentPaths, subjectsList, ImportCurriculumsCols.taxonomyLevels);
		}
		return allOk;
	}
	
	private boolean validateMandatory(ImportedRow importedRow, String val, ImportCurriculumsCols col) {
		boolean allOk = true;
		if(!StringHelper.containsNonWhitespace(val)) {
			String column = translator.translate(col.i18nHeaderKey());
			importedRow.addValidationError(col, column, translator.translate("error.no.value"), translator.translate("error.value.required"));
			allOk &= false;
		}
		return allOk;
	}
	
	private boolean validateMandatory(ImportedRow importedRow, ReaderLocalDate val, ImportCurriculumsCols col) {
		boolean allOk = true;
		if(val != null && val.date() == null && !StringHelper.containsNonWhitespace(val.val())) {
			String column = translator.translate(col.i18nHeaderKey());
			importedRow.addValidationError(col, column, translator.translate("error.no.value"), translator.translate("error.value.required"));
			allOk &= false;
		}
		return allOk;
	}
	
	private boolean validateMandatory(ImportedRow importedRow, ReaderLocalTime val, ImportCurriculumsCols col) {
		boolean allOk = true;
		if(val != null && val.time() == null && !StringHelper.containsNonWhitespace(val.val())) {
			String column = translator.translate(col.i18nHeaderKey());
			importedRow.addValidationError(col, column, translator.translate("error.no.value"), translator.translate("error.value.required"));
			allOk &= false;
		}
		return allOk;
	}
	
	private boolean validateEmpty(ImportedRow importedRow, Object val, ImportCurriculumsCols col) {
		boolean allOk = true;
		if(val != null) {
			String column = translator.translate(col.i18nHeaderKey());
			importedRow.addValidationError(col, column, null, translator.translate("error.value.must.be.empty"));
			allOk &= false;
		}
		return allOk;
	}
	
	private boolean validateLength(ImportedRow importedRow, String val, int maxLength, ImportCurriculumsCols col) {
		boolean allOk = true;
		if(StringHelper.containsNonWhitespace(val) && val.length() > maxLength) {
			String column = translator.translate(col.i18nHeaderKey());
			importedRow.addValidationError(col, column, null, translator.translate("error.value.length"));
			allOk &= false;
		}
		return allOk;
	}
	
	private boolean validateDate(ImportedRow importedRow, ReaderLocalDate val, ImportCurriculumsCols col, boolean warningForCurrent) {
		boolean allOk = true;
		if(val != null && val.date() == null && StringHelper.containsNonWhitespace(val.val())) {
			String column = translator.translate(col.i18nHeaderKey());
			if(!importedRow.isNew() && warningForCurrent) {
				importedRow.addValidationWarning(col, column, null, translator.translate("warning.format.invalid.ignore", val.val()));
			} else {
				importedRow.addValidationError(col, column, null, translator.translate("error.format.invalid", val.val()));
			}
			allOk &= false;
		}
		return allOk;
	}
	
	private boolean validateTime(ImportedRow importedRow, ReaderLocalTime val, ImportCurriculumsCols col, boolean warningForCurrent) {
		boolean allOk = true;
		if(val != null && val.time() == null && StringHelper.containsNonWhitespace(val.val())) {
			String column = translator.translate(col.i18nHeaderKey());
			if(!importedRow.isNew() && warningForCurrent) {
				importedRow.addValidationWarning(col, column, null, translator.translate("warning.format.invalid.ignore", val.val()));
			} else {
				importedRow.addValidationError(col, column, null, translator.translate("error.format.invalid", val.val()));
			}
			allOk &= false;
		}
		return allOk;
	}
	
	private boolean validateAfter(ImportedRow importedRow, ImportCurriculumsCols col) {
		boolean allOk = true;
		Date first = AbstractExcelReader.toDate(importedRow.getStartDate(), importedRow.getStartTime());
		Date second = AbstractExcelReader.toDate(importedRow.getEndDate(), importedRow.getEndTime());
		if(first != null && second != null && first.compareTo(second) > 0) {
			String column = translator.translate(col.i18nHeaderKey());
			importedRow.addValidationError(col, column, null, translator.translate("error.invalid.period"));
			allOk &= false;
		}
		return allOk;
	}
	
	private boolean validatePlannedLectures(ImportedRow importedRow, String val, ImportCurriculumsCols col) {
		boolean allOk = true;
		
		if(!StringHelper.isLong(val)) {
			allOk &= false;
		} else {
			int plannedLectures = Integer.parseInt(val);
			if(plannedLectures < 0 || plannedLectures > 12) {
				allOk &= false;
			}
		}
		
		if(!allOk) {
			String column = translator.translate(col.i18nHeaderKey());
			if(val == null) {
				val = "";
			}
			if(importedRow.isNew()) {
				importedRow.addValidationError(col, column, null, translator.translate("warning.value.not.supported", val));
			} else {
				importedRow.addValidationWarning(col, column, null, translator.translate("warning.value.not.supported", val));
			}
		}
		
		return allOk;
	}
	
	private boolean validateTruncateLength(ImportedRow importedRow, String val, int maxLength, ImportCurriculumsCols col) {
		boolean allOk = true;
		if(StringHelper.containsNonWhitespace(val) && val.length() > maxLength) {
			String column = translator.translate(col.i18nHeaderKey());
			importedRow.addValidationWarning(col, column, null, translator.translate("warning.value.truncate"));
			allOk &= false;
		}
		return allOk;
	}
	
	private boolean validateOnOff(ImportedRow importedRow, ImportCurriculumsCols optionColumn, String val) {
		boolean allOk = true;
		
		if(!"ON".equalsIgnoreCase(val) && !"OFF".equalsIgnoreCase(val)) {
			String message = translator.translate("warning.value.not.supported", val);
			String column = translator.translate(optionColumn.i18nHeaderKey());
			if(importedRow.isNew()) {
				importedRow.addValidationError(optionColumn, column, null, message);	
			} else {
				importedRow.addValidationWarning(optionColumn, column, null, message);
			}
			
			allOk &= false;
		}
		return allOk;
	}
	
	private void unkownType(ImportedRow importedRow) {
		String column = translator.translate(ImportCurriculumsCols.objectType.i18nHeaderKey());
		if(StringHelper.containsNonWhitespace(importedRow.getRawType())) {
			importedRow.addValidationError(ImportCurriculumsCols.objectType, column, importedRow.getRawType(),
					translator.translate("warning.value.not.supported", importedRow.getRawType()));
		} else {
			importedRow.addValidationError(ImportCurriculumsCols.objectType, column,
					translator.translate("error.no.value"), translator.translate("error.value.required"));
		}
	}
	
	private void changes(ImportedRow importedRow, Object currentValue, Object newValue, ImportCurriculumsCols col) {
		String column = translator.translate(col.i18nHeaderKey());
		importedRow.addChanged(column, currentValue, newValue, col);
	}
	
	private String toExcelFormat(Enum<?> val) {
		if(val == null) return null;
		
		if("enabled".equals(val.name())) {
			return "ON";
		}
		if("disabled".equals(val.name())) {
			return "OFF";
		}
		if("inherited".equals(val.name())) {
			return "DEFAULT";
		}	
		return null;
	}
	
	private static boolean validateCurriculumElementStatus(String val) {
		boolean ok = false;
		if(StringHelper.containsNonWhitespace(val)) {
			for(CurriculumElementStatus status:CurriculumElementStatus.values()) {
				if(status.name().equalsIgnoreCase(val)) {
					ok = true;
				}
			}
		}
		return ok;
	}
	
	private static boolean validateRepositoryEntryStatus(String val) {
		boolean ok = false;
		if(StringHelper.containsNonWhitespace(val)) {
			for(RepositoryEntryStatusEnum status:RepositoryEntryStatusEnum.values()) {
				if(status.name().equalsIgnoreCase(val)) {
					ok = true;
				}
			}
		}
		return ok;
	}
	
	public Import loadFile(File file) {

		List<ImportedRow> elementsRows = null;
		List<ImportedRow> curriculumsRows = null;
		
		try(ReadableWorkbook wb = new ReadableWorkbook(file)) {
			Sheet curriculumsSheet = wb.getFirstSheet();
			try (Stream<Row> rows = curriculumsSheet.openStream()) {
				curriculumsRows = rows
					.filter(r -> r.getRowNum() > 1)
					.map(r -> {
						// Title, Ext. ref., Organisation ref., absences, description, creation date, last modified
						String title = getString(r, 0);
						String externalRef = getString(r, 1);
						String organisationExtRel = getString(r, 2);
						String absences = getString(r, 3);
						String description = getString(r, 4);
						LocalDateTime creationDate = getDateTime(r, 5);
						LocalDateTime lastModified = getDateTime(r, 6);
						
						return new ImportedRow(r.getRowNum(), title, externalRef, organisationExtRel,
								absences, description, creationDate, lastModified);
					}).toList();
			} catch(Exception e) {
				log.error("", e);
			}
			
			Sheet elementsSheet = wb.getSheet(1)
					.orElse(null);
			try (Stream<Row> rows = elementsSheet.openStream()) {
				elementsRows = rows
					.filter(r -> r.getRowNum() > 1)
					.map(r -> {
						// Prod. ext. ref, Impl. ext. ref., Object type, Title, Ext. ref., Organisation ref., absences, description, creation date, last modified
						String curriculumIdentifier = getString(r, 0);
						String implementationIdentifier = getString(r, 1);
						String type = getString(r, 2);
						String level = getString(r, 3);
						
						String title = getString(r, 4);
						String externalRef = getString(r, 5);
						String elementStatus = getString(r, 6);
						
						ReaderLocalDate startDate = getDate(r, 7);
						ReaderLocalTime startTime = getTime(r, 8);
						ReaderLocalDate endDate = getDate(r, 9);
						ReaderLocalTime endTime = getTime(r, 10);
						
						String unit = getNumberAsString(r, 11);
						String referenceExternalRef = getString(r, 12);
						String location = getString(r, 13);
						String curriculumElementType = getString(r, 14);
						
						String calendar = getString(r, 15);
						String absences = getString(r, 16);
						String progress = getString(r, 17);
						
						String subjects = getString(r, 18);
						
						LocalDateTime creationDate = getDateTime(r, 19);
						LocalDateTime lastModified = getDateTime(r, 20);
						
						if(!StringHelper.containsNonWhitespace(type)
								&& !StringHelper.containsNonWhitespace(curriculumIdentifier)
								&& !StringHelper.containsNonWhitespace(implementationIdentifier)) {
							return null;
						}
						return new ImportedRow(type, r.getRowNum(), title, externalRef,
									curriculumIdentifier, implementationIdentifier, level, elementStatus, curriculumElementType,
									referenceExternalRef, unit, startDate, startTime, endDate, endTime, location,
									calendar, absences, progress, subjects, creationDate, lastModified);
					})
					.filter(r -> r != null)
					.toList();
			} catch(Exception e) {
				log.error("", e);
			}
			
		} catch(Exception e) {
			log.error("", e);
		}
		return new Import(curriculumsRows, elementsRows);
	}
	
	public record Import(List<ImportedRow> curriculumsRows, List<ImportedRow> elementsRows) {
		//
	}
	

}
