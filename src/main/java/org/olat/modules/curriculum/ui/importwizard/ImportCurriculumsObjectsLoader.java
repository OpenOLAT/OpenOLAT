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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.model.FindNamedIdentityCollection;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
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
public class ImportCurriculumsObjectsLoader extends AbstractExcelReader {
	
	private final Translator translator;
	
	public static final String ON = "ON";
	public static final String OFF = "OFF";
	public static final String DEFAULT = "DEFAULT";
	
	private Map<String,Organisation> organisationMap = new HashMap<>();

	@Autowired
	private BaseSecurity securityManager;
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
	
	public ImportCurriculumsObjectsLoader(Translator translator) {
		CoreSpringFactory.autowireObject(this);
		this.translator = translator;
	}
	
	public void loadCurrentCurriculums(List<ImportedRow> importedRows) {

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
				Organisation organisation = loadOrganisation(organisationIdentifier);
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
	
	protected static final List<String> loadTaxonomyLevels(ImportedRow importedRow) {
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
	
	public void loadMemberships(List<ImportedMembershipRow> rows, List<ImportedRow> curriculumsRows, List<ImportedRow> elements, List<ImportedUserRow> users) {
		Map<String, ImportedRow> curriculumsMaps = curriculumsRows.stream()
				.filter(r -> StringHelper.containsNonWhitespace(r.getCurriculumIdentifier()))
				.collect(Collectors.toMap(ImportedRow::getCurriculumIdentifier, r -> r, (u, v) -> u));
		
		Map<String, ImportedRow> elementsMaps = elements.stream()
				.filter(r -> StringHelper.containsNonWhitespace(r.getIdentifier()))
				.filter(r -> r.type() == CurriculumExportType.IMPL || r.type() == CurriculumExportType.ELEM)
				.collect(Collectors.toMap(ImportedRow::getIdentifier, r -> r, (u, v) -> u));
		
		Map<String, ImportedUserRow> usersMaps = users.stream()
				.filter(r -> StringHelper.containsNonWhitespace(r.getUsername()))
				.collect(Collectors.toMap(ImportedUserRow::getUsername, r -> r, (u, v) -> u));
		
		for(ImportedMembershipRow row:rows) {
			if(StringHelper.containsNonWhitespace(row.getCurriculumIdentifier())) {
				row.setCurriculumRow(curriculumsMaps.get(row.getCurriculumIdentifier()));
			}
			if(StringHelper.containsNonWhitespace(row.getImplementationIdentifier())) {
				ImportedRow elementRow = elementsMaps.get(row.getImplementationIdentifier());
				if(elementRow != null && elementRow.type() == CurriculumExportType.IMPL) {
					row.setImplementationRow(elementRow);
				}
			}
			if(StringHelper.containsNonWhitespace(row.getIdentifier())) {
				row.setElementRow(elementsMaps.get(row.getIdentifier()));
			}
			if(StringHelper.containsNonWhitespace(row.getUsername())) {
				row.setUserRow(usersMaps.get(row.getUsername()));
			}
		}
	}
	
	public void loadUsers(List<ImportedUserRow> rows) {
		List<String> usernames = rows.stream()
				.map(ImportedUserRow::getUsername)
				.filter(username -> StringHelper.containsNonWhitespace(username))
				.toList();
		
		FindNamedIdentityCollection identities = securityManager
				.findAndCollectIdentitiesBy(usernames, Identity.STATUS_VISIBLE_LIMIT, List.of());
		Map<String,Set<Identity>> identitiesMap = identities.getNameToIdentities();
		
		for(ImportedUserRow importedRow:rows) {
			String username = importedRow.getUsername();
			if(StringHelper.containsNonWhitespace(username)) {
				Set<Identity> namedIdentities = identitiesMap.get(username);
				if(namedIdentities == null || namedIdentities.isEmpty()) {
					importedRow.setStatus(ImportCurriculumsStatus.NEW);
				} else if(namedIdentities.size() == 1) {
					importedRow.setIdentity(namedIdentities.iterator().next());
				} else {
					notUniqueIdentifierError(importedRow);
				}
			}
			
			Organisation organisation = loadOrganisation(importedRow.getOrganisationIdentifier());
			importedRow.setOrganisation(organisation);
		}
	}
	
	private Organisation loadOrganisation(String organisationIdentifier) {
		Organisation organisation = organisationMap.get(organisationIdentifier);
		if(organisation == null) {
			List<Organisation> organisations = organisationService.findOrganisationByIdentifier(organisationIdentifier);
			if(organisations.size() == 1) {
				organisation = organisations.get(0);
				organisationMap.put(organisationIdentifier, organisation);
			}
		}
		return organisation;
	}
	
	private void notUniqueIdentifierError(AbstractImportRow importedRow) {
		String column = translator.translate(ImportCurriculumsCols.identifier.i18nHeaderKey());
		importedRow.addValidationError(ImportCurriculumsCols.identifier, column, null, translator.translate("error.value.duplicate"));
	}
	
	private void levelNotFoundError(AbstractImportRow importedRow, String level) {
		String column = translator.translate(ImportCurriculumsCols.level.i18nHeaderKey());
		importedRow.addValidationError(ImportCurriculumsCols.level, column, null, translator.translate("error.level.not.found", level));
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
