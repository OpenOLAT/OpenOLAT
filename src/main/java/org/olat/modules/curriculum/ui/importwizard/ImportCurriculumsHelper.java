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
import org.olat.core.id.Identity;
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
import org.olat.modules.curriculum.CurriculumElementTypeToType;
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
import org.olat.repository.RepositoryEntryRuntimeType;
import org.olat.repository.RepositoryEntrySecurity;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryEntryToTaxonomyLevel;
import org.olat.repository.RepositoryManager;
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

	private final Roles roles;
	private final Identity identity;
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
	private RepositoryManager repositoryManager;
	@Autowired
	private OrganisationService organisationService;
	
	public ImportCurriculumsHelper(Identity identity, Roles roles, Translator translator) {
		CoreSpringFactory.autowireObject(this);
		this.roles = roles;
		this.identity = identity;
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

		// First load the implementations
		Map<String,ImportedRow> implementations = new HashMap<>();
		for(ImportedRow importedRow:importedRows) {
			if(importedRow.type() == CurriculumExportType.IMPL) {
				loadElement(importedRow, curriculumsMap, typesMap);
				if(StringHelper.containsNonWhitespace(importedRow.getIdentifier())) {
					implementations.put(importedRow.getIdentifier(), importedRow);
				}
			}
		}
		
		// Load the curriculum elements
		Map<String,ImportedRow> elements = new HashMap<>();
		Map<LevelKey,ImportedRow> levelElementsMap = new HashMap<>();
		for(ImportedRow importedRow:importedRows) {
			if(importedRow.type() == CurriculumExportType.ELEM) {
				// Match the implementation
				if(StringHelper.containsNonWhitespace(importedRow.getImplementationIdentifier())) {
					importedRow.setImplementationRow(implementations.get(importedRow.getImplementationIdentifier()));
					if(StringHelper.containsNonWhitespace(importedRow.getLevel())) {
						levelElementsMap.put(new LevelKey(importedRow.getImplementationIdentifier(), importedRow.getLevel()), importedRow);
					}
				}
				
				loadElement(importedRow, curriculumsMap, typesMap);
				if(StringHelper.containsNonWhitespace(importedRow.getIdentifier())) {
					elements.put(importedRow.getIdentifier(), importedRow);
				}
			}
		}
		
		// Build the structure with key { implementation : level }
		for(ImportedRow importedRow:importedRows) {
			if(importedRow.type() == CurriculumExportType.ELEM) {
				String parentLevel = importedRow.getParentLevel();
				if(parentLevel == null) {
					importedRow.setCurriculumElementParentRow(importedRow.getImplementationRow());
				} else {
					ImportedRow parentRow = levelElementsMap.get(new LevelKey(importedRow.getImplementationIdentifier(), parentLevel));
					if(parentRow == null) {
						levelNotFoundError(importedRow, parentLevel);
					} else {
						importedRow.setCurriculumElementParentRow(parentRow);
					}
				}
			} else if(importedRow.type() == CurriculumExportType.COURSE
					|| importedRow.type() == CurriculumExportType.TMPL
					|| importedRow.type() == CurriculumExportType.EVENT) {
				if(StringHelper.containsNonWhitespace(importedRow.getImplementationIdentifier())) {
					importedRow.setImplementationRow(implementations.get(importedRow.getImplementationIdentifier()));
				}
				
				String level = importedRow.getLevel();
				if(level == null) {
					importedRow.setCurriculumElementParentRow(importedRow.getImplementationRow());
				} else {
					ImportedRow parentRow = levelElementsMap.get(new LevelKey(importedRow.getImplementationIdentifier(), level));
					if(parentRow == null) {
						levelNotFoundError(importedRow, level);
					} else {
						importedRow.setCurriculumElementParentRow(parentRow);
					}
				}
			}
			
			if(StringHelper.containsNonWhitespace(importedRow.getCurriculumIdentifier())) {
				importedRow.setCurriculumRow(curriculumsMap.get(importedRow.getCurriculumIdentifier()));
			} 
		}

		// Load the courses and templates
		Map<String,RepositoryEntry> entries = new HashMap<>();
		for(ImportedRow importedRow:importedRows) {
			if(importedRow.type() == CurriculumExportType.COURSE || importedRow.type() == CurriculumExportType.TMPL) {
				RepositoryEntry entry = loadRepositoryEntry(importedRow, curriculumsMap);
				if(entry != null) {
					entries.put(entry.getExternalRef(), entry);
				}
			}
		}
		
		// Load the events
		for(ImportedRow importedRow:importedRows) {
			if(importedRow.type() == CurriculumExportType.EVENT) {
				loadLectureBlock(importedRow, entries);
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

		// Map curriculum
		if(StringHelper.containsNonWhitespace(importedRow.getCurriculumIdentifier())) {
			importedRow.setCurriculumRow(curriculumsMap.get(importedRow.getCurriculumIdentifier()));
		} 
		
		return entry;
	}
	
	private LectureBlock loadLectureBlock(ImportedRow importedRow, Map<String,RepositoryEntry> entries) {
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

		return lectureBlock;
	}

	private CurriculumElement loadElement(ImportedRow importedRow, Map<String,ImportedRow> curriculumsMap, Map<String,CurriculumElementType> typesMap) {
		// Map curriculum
		if(StringHelper.containsNonWhitespace(importedRow.getCurriculumIdentifier())) {
			importedRow.setCurriculumRow(curriculumsMap.get(importedRow.getCurriculumIdentifier()));
		}
		
		String identifier = importedRow.getIdentifier();
		
		// Search existing element
		List<CurriculumElement> elements;
		if(importedRow.type() == CurriculumExportType.IMPL) {
			if(importedRow.getCurriculum() != null) {
				elements = curriculumService.searchCurriculumElements(importedRow.getCurriculum(), null, null, identifier, null, CurriculumElementStatus.notDeleted());
			} else {
				elements = List.of();
			}
		} else if(importedRow.type() == CurriculumExportType.ELEM) {
			if(importedRow.getImplementation() != null) {
				elements = curriculumService.searchCurriculumElements(importedRow.getCurriculum(), importedRow.getImplementation(), null, identifier, null, CurriculumElementStatus.notDeleted());
			} else {
				elements = List.of();
			}
		} else {
			return null;
		}

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
		// Validate uniqueness of implementations identifiers
		Map<String, ImportedRow> implementationsIdentifiersMap = new HashMap<>();
		for(ImportedRow row:importedRows) {
			String identifier = row.getIdentifier();
			if(row.type() == CurriculumExportType.IMPL) {
				if(implementationsIdentifiersMap.containsKey(identifier)) {
					notUniqueIdentifierError(row);
					notUniqueIdentifierError(implementationsIdentifiersMap.get(identifier));
				} else {
					implementationsIdentifiersMap.put(identifier, row);
				}
			}
		}
		
		// Validate uniqueness of identifiers within an implementation
		validateUniqueIdentifiersInImplementation(importedRows, CurriculumExportType.ELEM);
		validateUniqueIdentifiersInImplementation(importedRows, CurriculumExportType.EVENT);
		validateUniqueIdentifiersInImplementation(importedRows, CurriculumExportType.COURSE);
		validateUniqueIdentifiersInImplementation(importedRows, CurriculumExportType.TMPL);
	}

	private void validateUniqueIdentifiersInImplementation(List<ImportedRow> importedRows, CurriculumExportType type) {
		Map<Identifier, ImportedRow> identifiersMap = new HashMap<>();
		for(ImportedRow row:importedRows) {
			if(row.type() == type) {
				Identifier key = new Identifier(row.getImplementationIdentifier(), row.getIdentifier());
				if(identifiersMap.containsKey(key)) {
					notUniqueIdentifierError(row);
					notUniqueIdentifierError(identifiersMap.get(key));
				} else {
					identifiersMap.put(key, row);
				}
			}
		}
	}
	
	private void notUniqueIdentifierError(ImportedRow importedRow) {
		String column = translate(ImportCurriculumsCols.identifier.i18nHeaderKey());
		importedRow.addValidationError(ImportCurriculumsCols.identifier, column, null, translate("error.value.duplicate"));
	}
	
	private void levelNotFoundError(ImportedRow importedRow, String level) {
		String column = translate(ImportCurriculumsCols.level.i18nHeaderKey());
		importedRow.addValidationError(ImportCurriculumsCols.level, column, null, translator.translate("error.level.not.found", level));
	}
	
	public void validate(ImportedRow importedRow) {
		CurriculumExportType type = importedRow.type();
		if(type == CurriculumExportType.CUR) {
			validateCurriculumRow(importedRow);
		} else if(type == CurriculumExportType.IMPL || type == CurriculumExportType.ELEM) {
			validateCurriculumElementRow(importedRow);
		} else if(type == CurriculumExportType.COURSE) {
			validateCourseAndTemplateRow(importedRow, importedRow.getCourse());
		} else if(type == CurriculumExportType.TMPL) {
			validateCourseAndTemplateRow(importedRow, importedRow.getTemplate());
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
			String column = translate(ImportCurriculumsCols.displayName.i18nHeaderKey());
			importedRow.addChanged(column, importedRow.getCurriculumElement().getDisplayName(), importedRow.getDisplayName(), ImportCurriculumsCols.displayName);
		}
		
		validateIdentifier(importedRow);
		
		// Level only for element
		if(importedRow.type() == CurriculumExportType.ELEM) {
			validateLevel(importedRow);
		}
		
		// Curriculum
		if(importedRow.getCurriculum() != null && importedRow.getCurriculumElement() != null
				&& !importedRow.getCurriculum().equals(importedRow.getCurriculumElement().getCurriculum())) {
			String column = translate(ImportCurriculumsCols.curriculumIdentifier.i18nHeaderKey());
			importedRow.addValidationError(ImportCurriculumsCols.curriculumIdentifier, column, null,
					translator.translate("error.no.update", importedRow.getCurriculumIdentifier()));
		} else if(importedRow.type() == CurriculumExportType.ELEM
				&& importedRow.getImplementation() != null && importedRow.getCurriculumElement() != null
				&& !importedRow.getImplementation().equals(importedRow.getCurriculumElement().getImplementation())) {
			String column = translate(ImportCurriculumsCols.implementationIdentifier.i18nHeaderKey());
			importedRow.addValidationError(ImportCurriculumsCols.implementationIdentifier, column, null,
					translator.translate("error.no.update", importedRow.getImplementationIdentifier()));
		}
		
		// Curriculum element type
		if(validateMandatory(importedRow, importedRow.getCurriculumElementTypeIdentifier(), ImportCurriculumsCols.elementType)) {
			String column = translate(ImportCurriculumsCols.elementType.i18nHeaderKey());
			if(importedRow.getCurriculumElementType() == null) {
				importedRow.addValidationError(ImportCurriculumsCols.elementType, column, importedRow.getCurriculumElementTypeIdentifier(),
						translator.translate("error.not.exist", importedRow.getCurriculumElementTypeIdentifier()));
			} else if(!importedRow.isNew() && !Objects.equals(importedRow.getCurriculumElement().getType(), importedRow.getCurriculumElementType())) {
				importedRow.addValidationError(ImportCurriculumsCols.elementType, column, null,
						translate("error.no.update"));
			} else {
				validateCurriculumElementStructuralType(importedRow, importedRow.getCurriculumElementType());
			}
		}
		
		// Status
		if(validateMandatory(importedRow, importedRow.getElementStatus(), ImportCurriculumsCols.elementStatus)) {
			if(!validateCurriculumElementStatus(importedRow.getElementStatus())) {
				String column = translate(ImportCurriculumsCols.elementStatus.i18nHeaderKey());
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
			String column = translate(ImportCurriculumsCols.startDate.i18nHeaderKey());
			importedRow.addChanged(column, importedRow.getCurriculumElement().getBeginDate(), importedRow.getStartDate().date(), ImportCurriculumsCols.startDate);
		}
		validateEmpty(importedRow, importedRow.getStartTime(), ImportCurriculumsCols.startTime);
		
		if(validateDate(importedRow, importedRow.getEndDate(), ImportCurriculumsCols.endDate, true)
				&& validateAfter(importedRow, ImportCurriculumsCols.endDate)
				&& importedRow.getEndDate() != null && !importedRow.isNew()) {
			String column = translate(ImportCurriculumsCols.endDate.i18nHeaderKey());
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
	
	
	private boolean validateCurriculumElementStructuralType(ImportedRow importedRow, CurriculumElementType type) {
		boolean allOk = true;

		String column = translate(ImportCurriculumsCols.elementType.i18nHeaderKey());
		if(!type.isAllowedAsRootElement() && importedRow.type() == CurriculumExportType.IMPL) {
			importedRow.addValidationError(ImportCurriculumsCols.elementType, column,
					null, translate("error.impletation.not.allowed"));
			allOk &= false;
		} else if(type.getMaxRepositoryEntryRelations() == 0
				&& importedRow.getNumResources(CurriculumExportType.COURSE) + importedRow.getNumResources(CurriculumExportType.TMPL) > 0) {
			importedRow.addValidationError(ImportCurriculumsCols.elementType, column,
					null, translate("error.content.not.allowed"));
			allOk &= false;
		} else if(type.getMaxRepositoryEntryRelations() == 1
				&& importedRow.getNumResources(CurriculumExportType.COURSE) + importedRow.getNumResources(CurriculumExportType.TMPL) > 1) {
			importedRow.addValidationError(ImportCurriculumsCols.elementType, column,
					null, translate("error.content.one.allowed"));
			allOk &= false;
		} else if(type.getMaxRepositoryEntryRelations() == -1 && importedRow.getNumResources(CurriculumExportType.TMPL) > 0) {
			importedRow.addValidationError(ImportCurriculumsCols.elementType, column,
					null, translate("error.content.no.template"));
			allOk &= false;
		} else if(type.isSingleElement() && importedRow.getNumOfSubCurriculumElements() > 0) {
			importedRow.addValidationError(ImportCurriculumsCols.elementType, column,
					null, translate("error.no.sub.elements"));
			allOk &= false;
		} else if(importedRow.getCurriculumElementParentRow() != null
				&& importedRow.getCurriculumElementParentRow().getCurriculumElementType() != null
				&& !matchSubType(importedRow.getCurriculumElementParentRow().getCurriculumElementType(), type)) {
			importedRow.addValidationError(ImportCurriculumsCols.elementType, column,
					null, translate("error.no.sub.element.of.type"));
			allOk &= false;
		}

		return allOk;
	}
	
	private boolean matchSubType(CurriculumElementType parentType, CurriculumElementType type) {
		// Don't replace with anyMatch, useful for debugging
		List<CurriculumElementType> types = parentType.getAllowedSubTypes().stream()
				.map(CurriculumElementTypeToType::getAllowedSubType)
				.toList();
		return types.contains(type);
	}
	
	private void validateCourseAndTemplateRow(ImportedRow importedRow, RepositoryEntry entry) {
		// Curriculum and implementation
		validateCurriculumAndImplementation(importedRow);
		
		// Level
		validateLevel(importedRow);

		// Identifier
		validateIdentifier(importedRow);
		
		// Identifier matches a repository entry
		if(entry == null) {
			String column = translate(ImportCurriculumsCols.identifier.i18nHeaderKey());
			importedRow.addValidationError(ImportCurriculumsCols.identifier, column, null, translator.translate("error.not.exist", importedRow.getIdentifier()));
		} else {
			RepositoryEntrySecurity reSecurity = repositoryManager.isAllowed(identity, roles, entry);
			if(!reSecurity.isAdministrativeUser()) {
				String column = translate(ImportCurriculumsCols.identifier.i18nHeaderKey());
				importedRow.addValidationError(ImportCurriculumsCols.identifier, column, null, translate("error.permissions"));
			}
		}
		
		// Display name / title
		if(validateMandatory(importedRow, importedRow.getDisplayName(), ImportCurriculumsCols.displayName)
				&& validateTruncateLength(importedRow, importedRow.getDisplayName(), 255, ImportCurriculumsCols.displayName)
				&& entry != null) {
			String column = translate(ImportCurriculumsCols.displayName.i18nHeaderKey());
			importedRow.addChanged(column, entry.getDisplayname(), importedRow.getDisplayName(), ImportCurriculumsCols.displayName);
		}
		
		// Object type matches repository entry runtime type
		if(entry != null && ((importedRow.type() == CurriculumExportType.TMPL && entry.getRuntimeType() != RepositoryEntryRuntimeType.template)
				|| (importedRow.type() == CurriculumExportType.COURSE && entry.getRuntimeType() != RepositoryEntryRuntimeType.curricular))) {
			String column = translate(ImportCurriculumsCols.objectType.i18nHeaderKey());
			importedRow.addValidationError(ImportCurriculumsCols.objectType, column, null, translate("error.wrong.runtime.type"));
		}

		// Dates
		if(validateDate(importedRow, importedRow.getStartDate(), ImportCurriculumsCols.startDate, true)
				&& importedRow.getStartDate() != null && !importedRow.isNew()) {
			String column = translate(ImportCurriculumsCols.startDate.i18nHeaderKey());
			Date from = entry.getLifecycle() == null ? null : entry.getLifecycle().getValidFrom();
			importedRow.addChanged(column, from, importedRow.getStartDate().date(), ImportCurriculumsCols.startDate);
		}
		validateEmpty(importedRow, importedRow.getStartTime(), ImportCurriculumsCols.startTime);
		if(validateDate(importedRow, importedRow.getEndDate(), ImportCurriculumsCols.endDate, true)
				&& validateAfter(importedRow, ImportCurriculumsCols.endDate)
				&& importedRow.getEndDate() != null && !importedRow.isNew()) {
			String column = translate(ImportCurriculumsCols.endDate.i18nHeaderKey());
			Date to = entry.getLifecycle() == null ? null : entry.getLifecycle().getValidTo();
			importedRow.addChanged(column, to, importedRow.getEndDate().date(), ImportCurriculumsCols.endDate);
		}
		validateEmpty(importedRow, importedRow.getEndTime(), ImportCurriculumsCols.endTime);
				
		// Status
		if(validateMandatory(importedRow, importedRow.getElementStatus(), ImportCurriculumsCols.elementStatus)) {
			if(!validateRepositoryEntryStatus(importedRow.getElementStatus())) {
				String column = translate(ImportCurriculumsCols.elementStatus.i18nHeaderKey());
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
	
	private void validateLectureBlockRow(ImportedRow importedRow) {
		// Curriculum and implementation
		validateCurriculumAndImplementation(importedRow);
		
		// Display name / title
		if(validateMandatory(importedRow, importedRow.getDisplayName(), ImportCurriculumsCols.displayName)
				&& validateTruncateLength(importedRow, importedRow.getDisplayName(), 255, ImportCurriculumsCols.displayName)
				&& !importedRow.isNew()) {
			String column = translate(ImportCurriculumsCols.displayName.i18nHeaderKey());
			importedRow.addChanged(column, importedRow.getLectureBlock().getTitle(), importedRow.getDisplayName(), ImportCurriculumsCols.displayName);
		}
		
		validateIdentifier(importedRow);

		// Reference to course
		String referenceColumn = translate(ImportCurriculumsCols.referenceIdentifier.i18nHeaderKey());
		if(!StringHelper.containsNonWhitespace(importedRow.getReferenceExternalRef())) {
			importedRow.addValidationError(ImportCurriculumsCols.referenceIdentifier, referenceColumn,
					translate("error.no.value"), translate("error.value.required"));
		} else if(importedRow.getCourse() == null) {
			importedRow.addValidationError(ImportCurriculumsCols.referenceIdentifier, referenceColumn,
					null, translator.translate("error.not.exist", importedRow.getReferenceExternalRef()));
		} else if(importedRow.getLectureBlock() != null && importedRow.getCourse() != null
				&& !Objects.equals(importedRow.getLectureBlock().getEntry(), importedRow.getCourse())) {
			importedRow.addValidationError(ImportCurriculumsCols.referenceIdentifier, referenceColumn,
					null, translate("error.no.update"));
		}
		
		// Dates: dates are mandatory, need a valid format, and end date must be after the start
		if(validateMandatory(importedRow, importedRow.getStartDate(), ImportCurriculumsCols.startDate)
				&& validateDate(importedRow, importedRow.getStartDate(), ImportCurriculumsCols.startDate, true)
				&& !importedRow.isNew()) {
			String column = translate(ImportCurriculumsCols.startDate.i18nHeaderKey());
			importedRow.addChanged(column, importedRow.getLectureBlock().getStartDate(), importedRow.getStartDate().date(), ImportCurriculumsCols.startDate);
		}
		if(validateMandatory(importedRow, importedRow.getStartTime(), ImportCurriculumsCols.startTime)
				&& validateTime(importedRow, importedRow.getStartTime(), ImportCurriculumsCols.startTime, true)
				&& !importedRow.isNew()) {
			String column = translate(ImportCurriculumsCols.startTime.i18nHeaderKey());
			importedRow.addChanged(column, importedRow.getLectureBlock().getStartDate(), importedRow.getStartTime().time(), ImportCurriculumsCols.startTime);
		}
		if(validateMandatory(importedRow, importedRow.getEndDate(), ImportCurriculumsCols.endDate)
				&& validateDate(importedRow, importedRow.getEndDate(), ImportCurriculumsCols.endDate, true)
				&& validateAfter(importedRow, ImportCurriculumsCols.endDate)
				&& !importedRow.isNew()) {
			String column = translate(ImportCurriculumsCols.endDate.i18nHeaderKey());
			importedRow.addChanged(column, importedRow.getLectureBlock().getEndDate(), importedRow.getEndDate().date(), ImportCurriculumsCols.endDate);
		}
		if(validateMandatory(importedRow, importedRow.getEndTime(), ImportCurriculumsCols.endTime)
				&& validateTime(importedRow, importedRow.getEndTime(), ImportCurriculumsCols.endTime, true)
				&& !importedRow.isNew()) {
			String column = translate(ImportCurriculumsCols.endTime.i18nHeaderKey());
			importedRow.addChanged(column, importedRow.getLectureBlock().getEndDate(), importedRow.getEndTime().time(), ImportCurriculumsCols.endTime);
		}
		
		// Unit
		if(StringHelper.containsNonWhitespace(importedRow.getLocation())
				&& validatePlannedLectures(importedRow, importedRow.getUnit(), ImportCurriculumsCols.unit)
				&& !importedRow.isNew()) {
			String col = translate(ImportCurriculumsCols.unit.i18nHeaderKey());
			importedRow.addChanged(col, Integer.toString(importedRow.getLectureBlock().getPlannedLecturesNumber()), importedRow.getUnit(), ImportCurriculumsCols.unit);
		}
		
		// Location
		if(StringHelper.containsNonWhitespace(importedRow.getLocation())
				&& validateTruncateLength(importedRow, importedRow.getLocation(), 128, ImportCurriculumsCols.location)
				&& !importedRow.isNew()) {
			String col = translate(ImportCurriculumsCols.displayName.i18nHeaderKey());
			importedRow.addChanged(col, importedRow.getLectureBlock().getLocation(), importedRow.getLocation(), ImportCurriculumsCols.location);
		}
		
		// Taxonomy
		validateTaxonomy(importedRow);
		
		// Last modified
		validateLastModified(importedRow, importedRow.getImplementation());
	}
	
	private void validateCurriculumRow(ImportedRow importedRow) {
		// Display name / title
		if(validateMandatory(importedRow, importedRow.getDisplayName(), ImportCurriculumsCols.displayName)
				&& validateTruncateLength(importedRow, importedRow.getDisplayName(), 255, ImportCurriculumsCols.displayName)
				&& !importedRow.isNew()) {
			String column = translate(ImportCurriculumsCols.displayName.i18nHeaderKey());
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
			String descriptionColumn = translate(ImportCurriculumsCols.description.i18nHeaderKey());
			importedRow.addChanged(descriptionColumn, importedRow.getCurriculum().getDescription(), importedRow.getDescription(), ImportCurriculumsCols.description);
		}
		
		// Organisation
		if(validateMandatory(importedRow, importedRow.getOrganisationIdentifier(), ImportCurriculumsCols.organisationIdentifier)) {
			String organisationColumn = translate(ImportCurriculumsCols.organisationIdentifier.i18nHeaderKey());
			if(importedRow.getOrganisation() == null) {
				String placeholder = StringHelper.containsNonWhitespace(importedRow.getOrganisationIdentifier())
						? null
						: translate("error.no.value");
				importedRow.addValidationError(ImportCurriculumsCols.organisationIdentifier, organisationColumn,
						placeholder, translate("error.value.required"));
			} else if(!hasOrganisationPermission(importedRow.getOrganisation())) {
				importedRow.addValidationError(ImportCurriculumsCols.organisationIdentifier, organisationColumn,
						null, translate("error.permissions"));
			} else if(importedRow.getCurriculum() != null && !Objects.equals(importedRow.getCurriculum().getOrganisation(), importedRow.getOrganisation())) {
				importedRow.addValidationError(ImportCurriculumsCols.organisationIdentifier, organisationColumn,
						null, translate("error.no.update"));
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
				String column = translate(ImportCurriculumsCols.curriculumIdentifier.i18nHeaderKey());
				importedRow.addValidationError(ImportCurriculumsCols.curriculumIdentifier, column,
						null, translator.translate("error.not.exist", importedRow.getCurriculumIdentifier()));
			}
		}
		
		if(validateMandatory(importedRow, importedRow.getImplementationIdentifier(), ImportCurriculumsCols.implementationIdentifier)) {
			if(importedRow.type() == CurriculumExportType.IMPL) {
				if(!Objects.equals(importedRow.getImplementationIdentifier(), importedRow.getIdentifier())) {
					importedRow.addValidationError(ImportCurriculumsCols.implementationIdentifier, translate(ImportCurriculumsCols.implementationIdentifier.i18nHeaderKey()),
							null, translator.translate("error.impletation.identifiers.doesnt.match", importedRow.getIdentifier()));
				}
			} else if(importedRow.getImplementation() == null && importedRow.getImplementationRow() == null) {
				String column = translate(ImportCurriculumsCols.implementationIdentifier.i18nHeaderKey());
				importedRow.addValidationError(ImportCurriculumsCols.implementationIdentifier, column,
						null, translator.translate("error.not.exist", importedRow.getImplementationIdentifier()));
			}
		}
	}
	
	private boolean validateIdentifier(ImportedRow importedRow) {
		// Identifier / external ref.
		boolean allOk = validateMandatory(importedRow, importedRow.getIdentifier(), ImportCurriculumsCols.identifier);
		allOk &= validateLength(importedRow, importedRow.getIdentifier(), 255, ImportCurriculumsCols.identifier);
		return allOk;
	}
	
	private void validateLevel(ImportedRow importedRow) {
		// Identifier / external ref.
		validateMandatory(importedRow, importedRow.getLevel(), ImportCurriculumsCols.level);
	}
	
	private boolean hasOrganisationPermission(Organisation organisation) {
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
				String column = translate(ImportCurriculumsCols.lastModified.i18nHeaderKey());
				importedRow.addValidationWarning(ImportCurriculumsCols.lastModified, column, null,
						translate("warning.last.modified"));
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
		String column = translate(ImportCurriculumsCols.taxonomyLevels.i18nHeaderKey());
		
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
			String column = translate(col.i18nHeaderKey());
			importedRow.addValidationError(col, column, translate("error.no.value"), translate("error.value.required"));
			allOk &= false;
		}
		return allOk;
	}
	
	private boolean validateMandatory(ImportedRow importedRow, ReaderLocalDate val, ImportCurriculumsCols col) {
		boolean allOk = true;
		if(val != null && val.date() == null && !StringHelper.containsNonWhitespace(val.val())) {
			String column = translate(col.i18nHeaderKey());
			importedRow.addValidationError(col, column, translate("error.no.value"), translate("error.value.required"));
			allOk &= false;
		}
		return allOk;
	}
	
	private boolean validateMandatory(ImportedRow importedRow, ReaderLocalTime val, ImportCurriculumsCols col) {
		boolean allOk = true;
		if(val != null && val.time() == null && !StringHelper.containsNonWhitespace(val.val())) {
			String column = translate(col.i18nHeaderKey());
			importedRow.addValidationError(col, column, translate("error.no.value"), translate("error.value.required"));
			allOk &= false;
		}
		return allOk;
	}
	
	private boolean validateEmpty(ImportedRow importedRow, Object val, ImportCurriculumsCols col) {
		boolean allOk = true;
		if(val != null) {
			String column = translate(col.i18nHeaderKey());
			importedRow.addValidationError(col, column, null, translate("error.value.must.be.empty"));
			allOk &= false;
		}
		return allOk;
	}
	
	private boolean validateLength(ImportedRow importedRow, String val, int maxLength, ImportCurriculumsCols col) {
		boolean allOk = true;
		if(StringHelper.containsNonWhitespace(val) && val.length() > maxLength) {
			String column = translate(col.i18nHeaderKey());
			importedRow.addValidationError(col, column, null, translate("error.value.length"));
			allOk &= false;
		}
		return allOk;
	}
	
	private boolean validateDate(ImportedRow importedRow, ReaderLocalDate val, ImportCurriculumsCols col, boolean warningForCurrent) {
		boolean allOk = true;
		if(val != null && val.date() == null && StringHelper.containsNonWhitespace(val.val())) {
			String column = translate(col.i18nHeaderKey());
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
			String column = translate(col.i18nHeaderKey());
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
			String column = translate(col.i18nHeaderKey());
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
			if(plannedLectures < 0 || plannedLectures > LectureBlock.MAX_PLANNED_LECTURES) {
				allOk &= false;
			}
		}
		
		if(!allOk) {
			String column = translate(col.i18nHeaderKey());
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
			String column = translate(col.i18nHeaderKey());
			importedRow.addValidationWarning(col, column, null, translator.translate("warning.value.truncate"));
			allOk &= false;
		}
		return allOk;
	}
	
	private boolean validateOnOff(ImportedRow importedRow, ImportCurriculumsCols optionColumn, String val) {
		boolean allOk = true;
		
		if(!"ON".equalsIgnoreCase(val) && !"OFF".equalsIgnoreCase(val)) {
			String message = translator.translate("warning.value.not.supported", val);
			String column = translate(optionColumn.i18nHeaderKey());
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
		String column = translate(ImportCurriculumsCols.objectType.i18nHeaderKey());
		if(StringHelper.containsNonWhitespace(importedRow.getRawType())) {
			importedRow.addValidationError(ImportCurriculumsCols.objectType, column, importedRow.getRawType(),
					translator.translate("warning.value.not.supported", importedRow.getRawType()));
		} else {
			importedRow.addValidationError(ImportCurriculumsCols.objectType, column,
					translate("error.no.value"), translate("error.value.required"));
		}
	}
	
	private void changes(ImportedRow importedRow, Object currentValue, Object newValue, ImportCurriculumsCols col) {
		String column = translate(col.i18nHeaderKey());
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
						String level = getHierarchicalNumber(r, 3);
						
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
	
	public record Identifier(String implementationIdentifier, String identifier) {
		@Override
		public int hashCode() {
			return (implementationIdentifier == null ? 0 : implementationIdentifier.hashCode())
					+ (identifier == null ? 0 : identifier.hashCode());
		}
		
		@Override
		public boolean equals(Object obj) {
			if(obj == this) {
				return true;
			}
			if(obj instanceof Identifier key) {
				return implementationIdentifier != null && implementationIdentifier.equals(key.implementationIdentifier)
						&& identifier != null && identifier.equals(key.identifier);
			}
			return false;
		}
	}
	
	public record LevelKey(String implementationIdentifier, String level) {
		@Override
		public int hashCode() {
			return (implementationIdentifier == null ? 0 : implementationIdentifier.hashCode())
					+ (level == null ? 0 : level.hashCode());
		}
		
		@Override
		public boolean equals(Object obj) {
			if(obj == this) {
				return true;
			}
			if(obj instanceof LevelKey key) {
				return implementationIdentifier != null && implementationIdentifier.equals(key.implementationIdentifier)
						&& level != null && level.equals(key.level);
			}
			return false;
		}
	}
}
