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
package org.olat.modules.certificationprogram.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FilterableFlexiTableModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.core.util.StringHelper;
import org.olat.modules.certificationprogram.CertificationProgram;

/**
 * 
 * Initial date: 26 août 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CertificationProgramListTableModel extends DefaultFlexiTableDataModel<CertificationProgramRow>
implements SortableFlexiTableDataModel<CertificationProgramRow>, FilterableFlexiTableModel {
	
	private static final ProgramCols[] COLS = ProgramCols.values();
	
	private final Locale locale;
	private List<CertificationProgramRow> backupList;
	
	public CertificationProgramListTableModel(FlexiTableColumnModel columnsModel, Locale locale) {
		super(columnsModel);
		this.locale = locale;
	}
	
	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<CertificationProgramRow> views = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
			super.setObjects(views);
		}
	}
	
	@Override
	public void filter(String searchString, List<FlexiTableFilter> filters) {
		if(StringHelper.containsNonWhitespace(searchString) || !filters.isEmpty()) {
			final String loweredSearchString = searchString == null || !StringHelper.containsNonWhitespace(searchString)
					? null : searchString.toLowerCase();

			final List<String> status = getFilteredList(filters, CertificationProgramListController.FILTER_PROGRAM_STATUS);
			final List<String> systems = getFilteredList(filters, CertificationProgramListController.FILTER_CREDITPOINT_SYSTEM);

			List<CertificationProgramRow> filteredRows = new ArrayList<>(backupList.size());
			for(CertificationProgramRow row:backupList) {
				boolean accept = accept(loweredSearchString, row)
						&& acceptStatus(status, row)
						&& acceptCreditPointSystem(systems, row);
				if(accept) {
					filteredRows.add(row);
				}
			}
			super.setObjects(filteredRows);
		} else {
			super.setObjects(backupList);
		}
	}
	
	private List<String> getFilteredList(List<FlexiTableFilter> filters, String filterName) {
    	FlexiTableFilter filter = FlexiTableFilter.getFilter(filters, filterName);
		if(filter instanceof FlexiTableExtendedFilter extendedFilter) {
			List<String> filterValues = extendedFilter.getValues();
			return filterValues != null && !filterValues.isEmpty() ? filterValues : null;
		}
		return null;
	}
	
	private boolean acceptCreditPointSystem(List<String> systems, CertificationProgramRow row) {
		if(systems == null || systems.isEmpty()) return true;
		
		String val = row.getCreditPointSystem() == null ? null : row.getCreditPointSystem().getKey().toString();
		return systems.contains(val);
	}
	
	private boolean accept(String searchValue, CertificationProgramRow entry) {
		if(searchValue == null) return true;
		return accept(searchValue, entry.getDisplayName())
				|| accept(searchValue, entry.getIdentifier());
	}
	
	private boolean accept(String searchValue, String val) {
		return val != null && val.toLowerCase().contains(searchValue);
	}
	
	private boolean acceptStatus(List<String> refs, CertificationProgramRow programRow) {
		if(refs == null || refs.isEmpty()) return true;
		return refs.stream()
				.anyMatch(ref -> programRow.getStatus() != null && ref.equals(programRow.getStatus().name()));
	}
	
	public CertificationProgramRow getRow(CertificationProgram program) {
		return this.getObjects().stream()
				.filter(row -> program.getKey().equals(row.getKey()))
				.findFirst()
				.orElse(null);
	}
	
	public CertificationProgramRow getRow(Long key) {
		return this.getObjects().stream()
				.filter(row -> key.equals(row.getKey()))
				.findFirst()
				.orElse(null);
	}

	@Override
	public Object getValueAt(int row, int col) {
		CertificationProgramRow programRow = getObject(row);
		return getValueAt(programRow, col);
	}

	@Override
	public Object getValueAt(CertificationProgramRow row, int col) {
		return switch(COLS[col]) {
			case key -> row.getKey();
			case identifier -> row.getIdentifier();
			case displayName -> row.getDisplayName();
			case recertificationMode -> row.getRecertificationMode();
			case validityPeriod -> row.getValidityPeriod();
			case activeUsers -> Long.valueOf(row.getActiveUsers());
			case candidates -> Long.valueOf(row.getCandidates());
			case removedUsers -> Long.valueOf(row.getRemovedUsers());
			case requiredCreditPoint -> row.getCreditPoints();
			case programStatus -> row.getStatus();
			default -> "ERROR";
		};
	}
	
	@Override
	public void setObjects(List<CertificationProgramRow> objects) {
		this.backupList = new ArrayList<>(objects);
		super.setObjects(objects);
	}

	public enum ProgramCols implements FlexiSortableColumnDef {
		key("table.header.key"),
		identifier("table.header.identifier"),
		displayName("table.header.displayname"),
		validityPeriod("table.header.validity.period"),
		recertificationMode("table.header.recertification.mode"),
		requiredCreditPoint("table.header.required.credit.point"),
		activeUsers("table.header.users.active"),
		candidates("table.header.users.candidates"),
		removedUsers("table.header.users.removed"),
		programStatus("table.header.program.status");
		
		private final String i18nHeaderKey;
		
		private ProgramCols(String i18nHeaderKey) {
			this.i18nHeaderKey = i18nHeaderKey;
		}

		@Override
		public boolean sortable() {
			return true;
		}

		@Override
		public String sortKey() {
			return name();
		}

		@Override
		public String i18nHeaderKey() {
			return i18nHeaderKey;
		}
	}
}
