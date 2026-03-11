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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.util.DateUtils;
import org.olat.core.util.StringHelper;
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
	private Map<ImportCurriculumsCols,CurriculumImportedValues> validationMap = new EnumMap<>(ImportCurriculumsCols.class);
	
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
	
	public CurriculumImportedValue getValidationByImportance(ImportCurriculumsCols col) {
		CurriculumImportedValues values = validationMap.get(col);
		if(values == null || values.values().isEmpty()) {
			return null;
		}
		if(values.values().size() == 1) {
			return values.values().get(0);
		}
		
		List<CurriculumImportedValue> list = new ArrayList<>(values.values());
		Collections.sort(list, new CurriculumImportedValueComparator());
		return list.get(0);
	}

	public List<CurriculumImportedValue> getValidation(ImportCurriculumsCols col) {
		CurriculumImportedValues values = validationMap.get(col);
		return values == null ? List.of() : values.values();
	}
	
	public List<CurriculumImportedValue> getValues() {
		List<CurriculumImportedValue> list = new ArrayList<>(validationMap.size() + 3);
		for(CurriculumImportedValues values:validationMap.values()) {
			list.addAll(values.values());
		}
		return list;
	}
	
	public void addValidationWarning(ImportCurriculumsCols col, String column, String placeholder, String message) {
		validationMap.computeIfAbsent(col, c -> CurriculumImportedValues.valueOf(column))
			.addWarning(placeholder, message);
	}
	
	public void addValidationError(ImportCurriculumsCols col, String column, String placeholder, String message) {
		validationMap.computeIfAbsent(col, c -> CurriculumImportedValues.valueOf(column))
			.addError(placeholder, message);
	}
	
	public void addChanged(String column, Object currentValue, Object newValue, ImportCurriculumsCols col) {
		if(currentValue instanceof Date date && newValue instanceof LocalDate) {
			currentValue = DateUtils.toLocalDate(date);	
		} else if(currentValue instanceof Date date && newValue instanceof LocalTime) {
			currentValue = DateUtils.toLocalTime(date);	
		}
		if(!Objects.equals(currentValue, newValue)) {
			validationMap.computeIfAbsent(col, c -> CurriculumImportedValues.valueOf(column))
				.addChanged(currentValue, newValue);
		}
	}
	
	public void addChanged(String column, String currentValue, String newValue, ImportCurriculumsCols col) {
		if(!equalsString(currentValue, newValue)) {
			validationMap.computeIfAbsent(col, c -> CurriculumImportedValues.valueOf(column))
				.addChanged(currentValue, newValue);
		}
	}
	private boolean equalsString(String s1, String s2) {
		if(!StringHelper.containsNonWhitespace(s1) && !StringHelper.containsNonWhitespace(s2)) {
			return true;
		}
		return s1 != null && s2 != null && s1.equals(s2);
	}
	
	public boolean hasValidationError(ImportCurriculumsCols col) {
		CurriculumImportedValues values = validationMap.get(col);
		return values != null && values.values().stream().anyMatch(v -> v.isError());
	}
	
	public boolean hasValidationErrors() {
		for(CurriculumImportedValues values:validationMap.values()) {
			for(CurriculumImportedValue val:values.values()) {
				if(val.isError()) {
					return true;
				}
			}
		}
		return false;
	}
	
	public CurriculumImportedStatistics getValidationStatistics() {
		int errors = 0;
		int warnings = 0;
		int changes = 0;
		
		for(CurriculumImportedValues values:validationMap.values()) {
			for(CurriculumImportedValue val:values.values()) {
				if(val.isChanged()) {
					changes++;
				}
				if(val.isError()) {
					errors++;
				} else if(val.isWarning()) {
					warnings++;
				}
			}
		}
		
		return new CurriculumImportedStatistics(errors, warnings, changes);
	}

}
