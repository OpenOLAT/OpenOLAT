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

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.id.Organisation;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.ui.importwizard.ImportCurriculumsReviewCurriculumsTableModel.ImportCurriculumsCols;

/**
 * 
 * Initial date: 9 févr. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CurriculumImportedRow {
	
	private final int rowNum;
	private final String identifier;
	private final String displayName;
	private final String organisationIdentifier;
	private final String absences;
	private final String description;
	private final LocalDateTime creationDate;
	private final LocalDateTime lastModified;
	
	private ImportCurriculumsStatus status;
	
	private Curriculum curriculum;
	private Organisation organisation;
	
	
	private FormLink validationResultsLink;
	private MultipleSelectionElement ignoreEl;
	
	private Map<ImportCurriculumsCols,CurriculumImportedValue> validationMap = new EnumMap<>(ImportCurriculumsCols.class);
	
	public CurriculumImportedRow(int rowNum, String displayName, String identifier, String organisationIdentifier, String absences,
			String description,LocalDateTime creationDate, LocalDateTime lastModified) {
		this.rowNum = rowNum;
		this.identifier = identifier;
		this.displayName = displayName;
		this.organisationIdentifier = organisationIdentifier;
		this.absences = absences;
		this.description = description;
		this.creationDate = creationDate;
		this.lastModified = lastModified;
	}
	
	public int getRowNum() {
		return rowNum;
	}
	
	public boolean isNew() {
		return curriculum == null;
	}

	public ImportCurriculumsStatus getStatus() {
		return status;
	}

	public void setStatus(ImportCurriculumsStatus status) {
		this.status = status;
	}

	public Curriculum getCurriculum() {
		return curriculum;
	}

	public void setCurriculum(Curriculum curriculum) {
		this.curriculum = curriculum;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getIdentifier() {
		return identifier;
	}

	public String getOrganisationIdentifier() {
		return organisationIdentifier;
	}

	public String getAbsences() {
		return absences;
	}

	public String getDescription() {
		return description;
	}

	public LocalDateTime getCreationDate() {
		return creationDate;
	}

	public LocalDateTime getLastModified() {
		return lastModified;
	}
	
	public Organisation getOrganisation() {
		return organisation;
	}

	public void setOrganisation(Organisation organisation) {
		this.organisation = organisation;
	}

	public boolean isIgnored() {
		return ignoreEl != null && ignoreEl.isAtLeastSelected(1);
	}

	public MultipleSelectionElement getIgnoreEl() {
		return ignoreEl;
	}

	public void setIgnoreEl(MultipleSelectionElement ignoreEl) {
		this.ignoreEl = ignoreEl;
	}
	
	public FormLink getValidationResultsLink() {
		return validationResultsLink;
	}

	public void setValidationResultsLink(FormLink validationResultsLink) {
		this.validationResultsLink = validationResultsLink;
	}

	public CurriculumImportedValue getValidation(ImportCurriculumsCols col) {
		return validationMap.get(col);
	}
	
	public List<CurriculumImportedValue> getValues() {
		return List.copyOf(validationMap.values());
	}
	
	public void addValidationWarning(ImportCurriculumsCols col, String column, String message) {
		validationMap.computeIfAbsent(col, c -> new CurriculumImportedValue(column))
			.setWarning(message);
	}
	
	public void addValidationError(ImportCurriculumsCols col, String column, String placeholder, String message) {
		validationMap.computeIfAbsent(col, c -> new CurriculumImportedValue(column))
			.setError(placeholder, message);
	}
	
	public void addChanged(String column, Object currentValue, Object newValue, ImportCurriculumsCols col) {
		if(!Objects.equals(currentValue, newValue)) {
			validationMap.computeIfAbsent(col, c -> new CurriculumImportedValue(column))
				.setChanged(currentValue, newValue);
		}
	}
	
	public CurriculumImportedStatistics getValidationStatistics() {
		int errors = 0;
		int warnings = 0;
		int changes = 0;
		
		for(Map.Entry<ImportCurriculumsCols, CurriculumImportedValue> entries:validationMap.entrySet()) {
			CurriculumImportedValue val = entries.getValue();
			if(val.isChanged()) {
				changes++;
			}
			if(val.isError()) {
				errors++;
			} else if(val.isWarning()) {
				warnings++;
			}
		}
		
		return new CurriculumImportedStatistics(errors, warnings, changes);
	}
}
