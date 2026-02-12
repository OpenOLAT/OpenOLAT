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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.openxml.AbstractExcelReader;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.ui.importwizard.ImportCurriculumsReviewCurriculumsTableModel.ImportCurriculumsCols;
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
	
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private OrganisationService organisationService;
	
	public ImportCurriculumsHelper(Translator translator) {
		CoreSpringFactory.autowireObject(this);
		this.translator = translator;
	}
	
	private String translate(String i18nKey) {
		return translator.translate(i18nKey);
	}
	
	public void loadCurrentCurriculums(List<CurriculumImportedRow> importedRows) {
		Map<String,Organisation> organisationMap = new HashMap<>();
		
		for(CurriculumImportedRow importedRow:importedRows) {
			List<Curriculum> curriculums = curriculumService.getCurriculumsByIdentifier(importedRow.getIdentifier());
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
	
	public void validateUniqueIdentifiers(List<CurriculumImportedRow> importedRows) {
		Map<String, CurriculumImportedRow> identifiersMap = new HashMap<>();
		for(CurriculumImportedRow row:importedRows) {
			String identifier = row.getIdentifier();
			if(identifiersMap.containsKey(identifier)) {
				notUniqueIdentifierError(row);
				notUniqueIdentifierError(identifiersMap.get(identifier));
			} else {
				identifiersMap.put(identifier, row);
			}
		}
	}
	
	private void notUniqueIdentifierError(CurriculumImportedRow importedRow) {
		String column = translator.translate(ImportCurriculumsCols.identifier.i18nHeaderKey());
		importedRow.addValidationError(ImportCurriculumsCols.identifier, column, null, translate("error.value.duplicate"));
	}
		
	public void validateCurriculumRow(CurriculumImportedRow importedRow, Roles roles) {
		// Display name / title
		if(validateMandatory(importedRow, importedRow.getDisplayName(), ImportCurriculumsCols.displayName)
				&& validateTruncateLength(importedRow, importedRow.getDisplayName(), 255, ImportCurriculumsCols.displayName)
				&& !importedRow.isNew()) {
			String column = translator.translate(ImportCurriculumsCols.displayName.i18nHeaderKey());
			importedRow.addChanged(column, importedRow.getCurriculum().getDisplayName(), importedRow.getDisplayName(), ImportCurriculumsCols.displayName);
		}

		// Identifier / external ref.
		validateMandatory(importedRow, importedRow.getIdentifier(), ImportCurriculumsCols.identifier);
		validateLength(importedRow, importedRow.getIdentifier(), 255, ImportCurriculumsCols.identifier);
		
		// Absences
		if(validateMandatory(importedRow, importedRow.getAbsences(), ImportCurriculumsCols.absences)
				&& validateAbsences(importedRow) && !importedRow.isNew()) {
			String column = translator.translate(ImportCurriculumsCols.absences.i18nHeaderKey());
			importedRow.addChanged(column, importedRow.getCurriculum().isLecturesEnabled(), "ON".equalsIgnoreCase(importedRow.getAbsences()), ImportCurriculumsCols.absences);
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
		if(importedRow.getCurriculum() != null && importedRow.getLastModified() != null) {
			validateLastModified(importedRow);
		}
		
		if(importedRow.getStatus() == null) {
			CurriculumImportedStatistics statistics = importedRow.getValidationStatistics();
			if(statistics.changes() > 0) {
				importedRow.setStatus(ImportCurriculumsStatus.MODIFIED);
			} else {
				importedRow.setStatus(ImportCurriculumsStatus.NO_CHANGES);
			}
		}
	}
	
	private boolean hasOrganisationPermission(Organisation organisation, Roles roles) {
		return roles
				.getOrganisationsWithRoles(OrganisationRoles.curriculummanager, OrganisationRoles.administrator)
				.stream()
				.anyMatch(ref -> ref.getKey().equals(organisation.getKey()));
	}
	
	private boolean validateLastModified(CurriculumImportedRow importedRow) {
		boolean allOk = true;
		
		LocalDateTime currentDate = DateUtils.toLocalDateTime(importedRow.getCurriculum().getLastModified())
				.withSecond(0).withNano(0);
		LocalDateTime date = importedRow.getLastModified()
				.withSecond(0).withNano(0);
		
		if(date.isBefore(currentDate)) {
			String column = translator.translate(ImportCurriculumsCols.lastModified.i18nHeaderKey());
			importedRow.addValidationWarning(ImportCurriculumsCols.lastModified, column, translator.translate("warning.last.modified"));
			allOk &= false;
		}
		return allOk;
	}
	
	private boolean validateMandatory(CurriculumImportedRow importedRow, String val, ImportCurriculumsCols col) {
		boolean allOk = true;
		if(!StringHelper.containsNonWhitespace(val)) {
			String column = translator.translate(col.i18nHeaderKey());
			importedRow.addValidationError(col, column, translator.translate("error.no.value"), translator.translate("error.value.required"));
			allOk &= false;
		}
		return allOk;
	}
	
	private boolean validateLength(CurriculumImportedRow importedRow, String val, int maxLength, ImportCurriculumsCols col) {
		boolean allOk = true;
		if(StringHelper.containsNonWhitespace(val) && val.length() > maxLength) {
			String column = translator.translate(col.i18nHeaderKey());
			importedRow.addValidationError(col, column, null, translator.translate("error.value.length"));
			allOk &= false;
		}
		return allOk;
	}
	
	private boolean validateTruncateLength(CurriculumImportedRow importedRow, String val, int maxLength, ImportCurriculumsCols col) {
		boolean allOk = true;
		if(StringHelper.containsNonWhitespace(val) && val.length() > maxLength) {
			String column = translator.translate(col.i18nHeaderKey());
			importedRow.addValidationWarning(col, column, translator.translate("warning.value.truncate"));
			allOk &= false;
		}
		return allOk;
	}
	
	private boolean validateAbsences(CurriculumImportedRow importedRow) {
		boolean allOk = true;
		
		String val = importedRow.getAbsences();
		if(!"ON".equalsIgnoreCase(val) && !"OFF".equalsIgnoreCase(val)) {
			String message = translator.translate("warning.value.not.supported", val);
			String column = translator.translate(ImportCurriculumsCols.absences.i18nHeaderKey());
			if(importedRow.isNew()) {
				importedRow.addValidationError(ImportCurriculumsCols.absences, column, null, message);	
			} else {
				importedRow.addValidationWarning(ImportCurriculumsCols.absences, column, message);
			}
			
			allOk &= false;
		}
		return allOk;
	}
	
	public List<CurriculumImportedRow> loadFile(File file) {
		try(ReadableWorkbook wb = new ReadableWorkbook(file)) {
			Sheet sheet = wb.getFirstSheet();
			try (Stream<Row> rows = sheet.openStream()) {
				return rows
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
						
						return new CurriculumImportedRow(r.getRowNum(), title, externalRef, organisationExtRel,
								absences, description, creationDate, lastModified);
					}).toList();
			} catch(Exception e) {
				log.error("", e);
			}
		} catch(Exception e) {
			log.error("", e);
		}
		return null;
	}
}
