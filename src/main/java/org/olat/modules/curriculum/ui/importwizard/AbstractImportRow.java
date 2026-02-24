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

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.util.DateUtils;
import org.olat.modules.curriculum.ui.importwizard.ImportCurriculumsReviewTableModel.ImportCurriculumsCols;

/**
 * 
 * Initial date: 20 févr. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AbstractImportRow {

	private final int rowNum;
	private ImportedRow curriculumElementParentRow;
	
	private FormLink validationResultsLink;
	private MultipleSelectionElement ignoreEl;

	private ImportCurriculumsStatus status;
	private Map<ImportCurriculumsCols,CurriculumImportedValue> validationMap = new EnumMap<>(ImportCurriculumsCols.class);
	
	public AbstractImportRow(int rowNum) {
		this.rowNum = rowNum;
	}
	
	public int getRowNum() {
		return rowNum;
	}
	
	public ImportedRow getCurriculumElementParentRow() {
		return curriculumElementParentRow;
	}

	public void setCurriculumElementParentRow(ImportedRow curriculumElementParentRow) {
		this.curriculumElementParentRow = curriculumElementParentRow;
	}
	
	public ImportCurriculumsStatus getStatus() {
		return status;
	}

	public void setStatus(ImportCurriculumsStatus status) {
		this.status = status;
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
	
	public void addValidationWarning(ImportCurriculumsCols col, String column, String placeholder, String message) {
		validationMap.computeIfAbsent(col, c -> new CurriculumImportedValue(column))
			.setWarning(placeholder, message);
	}
	
	public void addValidationError(ImportCurriculumsCols col, String column, String placeholder, String message) {
		validationMap.computeIfAbsent(col, c -> new CurriculumImportedValue(column))
			.setError(placeholder, message);
	}
	
	public void addChanged(String column, Object currentValue, Object newValue, ImportCurriculumsCols col) {
		if(currentValue instanceof Date date && newValue instanceof LocalDate) {
			currentValue = DateUtils.toLocalDate(date);	
		} else if(currentValue instanceof Date date && newValue instanceof LocalTime) {
			currentValue = DateUtils.toLocalTime(date);	
		}
		if(!Objects.equals(currentValue, newValue)) {
			validationMap.computeIfAbsent(col, c -> new CurriculumImportedValue(column))
				.setChanged(currentValue, newValue);
		}
	}
	
	public boolean hasValidationError(ImportCurriculumsCols col) {
		CurriculumImportedValue val = validationMap.get(col);
		return val != null && val.isError();
	}
	
	public boolean hasValidationErrors() {
		return validationMap.values().stream().anyMatch(v -> v.isError());
	}
	
	public CurriculumImportedStatistics getValidationStatistics() {
		int errors = 0;
		int warnings = 0;
		int changes = 0;
		
		for(CurriculumImportedValue val:validationMap.values()) {
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
