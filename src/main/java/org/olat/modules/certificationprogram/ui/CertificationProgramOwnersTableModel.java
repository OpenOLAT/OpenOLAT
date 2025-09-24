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

/**
 * 
 * Initial date: 24 sept. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CertificationProgramOwnersTableModel extends DefaultFlexiTableDataModel<CertificationProgramOwnerRow>
implements SortableFlexiTableDataModel<CertificationProgramOwnerRow>, FilterableFlexiTableModel {
	
	private static final CertificationProgramOwnersCols[] COLS = CertificationProgramOwnersCols.values();
	
	private final Locale locale;
	
	public CertificationProgramOwnersTableModel(FlexiTableColumnModel columnModel, Locale locale) {
		super(columnModel);
		this.locale = locale;
	}
	
	@Override
	public void filter(String searchString, List<FlexiTableFilter> filters) {
		//
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			SortableFlexiTableModelDelegate<CertificationProgramOwnerRow> sort = new SortableFlexiTableModelDelegate<>(orderBy, this, locale);
			super.setObjects(sort.sort());
		}
	}

	@Override
	public Object getValueAt(int row, int col) {
		CertificationProgramOwnerRow ownerRow = getObject(row);
		return getValueAt(ownerRow, col);
	}
	
	@Override
	public Object getValueAt(CertificationProgramOwnerRow row, int col) {
		if(col >= 0 && col < COLS.length) {
			return switch(COLS[col]) {
				case id -> row.getIdentityKey();
				case tools -> Boolean.TRUE;
				default -> "ERROR";
			};
		}
		
		int propPos = col - CertificationProgramOwnersController.USER_PROPS_OFFSET;
		return row.getIdentityProp(propPos);
	}

	public enum CertificationProgramOwnersCols implements FlexiSortableColumnDef {
		id("table.header.id"),
		tools("action.more");
		
		private final String i18nKey;
		
		private CertificationProgramOwnersCols(String i18nKey) {
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
