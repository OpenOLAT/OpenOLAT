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
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FilterableFlexiTableModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 25 nov. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CertificationProgramCandidatesTableModel extends DefaultFlexiTableDataModel<CertificationProgramCandidateRow>
implements SortableFlexiTableDataModel<CertificationProgramCandidateRow>, FilterableFlexiTableModel {
	
	private static final CertificationProgramCandidatesCols[] COLS = CertificationProgramCandidatesCols.values();
	
	private final Locale locale;
	private List<CertificationProgramCandidateRow> backupList;
	
	public CertificationProgramCandidatesTableModel(FlexiTableColumnModel columnModel, Locale locale) {
		super(columnModel);
		this.locale = locale;
	}
	
	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			SortableFlexiTableModelDelegate<CertificationProgramCandidateRow> sort = new CertificationProgramCandidatesTableSortDelegate(orderBy, this, locale);
			super.setObjects(sort.sort());
		}
	}
	
	@Override
	public void filter(String searchString, List<FlexiTableFilter> filters) {
		if(StringHelper.containsNonWhitespace(searchString)) {
			final String loweredSearchString = searchString == null || !StringHelper.containsNonWhitespace(searchString)
					? null : searchString.toLowerCase();
			
			List<CertificationProgramCandidateRow> filteredRows = new ArrayList<>(backupList.size());
			for(CertificationProgramCandidateRow row:backupList) {
				boolean accept = accept(loweredSearchString, row);
				if(accept) {
					filteredRows.add(row);
				}
			}
			super.setObjects(filteredRows);
		} else {
			super.setObjects(backupList);
		}
	}
	
	private boolean accept(String searchValue, CertificationProgramCandidateRow memberRow) {
		if(searchValue == null) return true;
		if(accept(searchValue, memberRow.getIdentityExternalId())) {
			return true;
		}
		
		for(String prop : memberRow.getIdentityProps()) {
			if(accept(searchValue, prop)) {
				return true;
			}
		}
		return false;
	}
	
	private boolean accept(String searchValue, String val) {
		return val != null && val.toLowerCase().contains(searchValue);
	}

	@Override
	public Object getValueAt(int row, int col) {
		CertificationProgramCandidateRow candidateRow = getObject(row);
		return getValueAt(candidateRow, col);
	}

	@Override
	public Object getValueAt(CertificationProgramCandidateRow row, int col) {
		if(col >= 0 && col < COLS.length) {
			return switch(COLS[col]) {
				case elements -> row.getElementsLink();
				default -> "ERROR";
			};
		}
		
		int propPos = col - CertificationProgramCandidatesController.USER_PROPS_OFFSET;
		return row.getIdentityProp(propPos);
	}
	
	@Override
	public void setObjects(List<CertificationProgramCandidateRow> objects) {
		this.backupList = objects;
		super.setObjects(objects);
	}
	
	public enum CertificationProgramCandidatesCols implements FlexiSortableColumnDef {
		elements("table.header.elements");
		
		private final String i18nKey;
		
		private CertificationProgramCandidatesCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return true;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}

}
