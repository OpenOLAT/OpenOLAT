/**
 * <a href="http://www.openolat.org">
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
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.resource.accesscontrol.ui;

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;

/**
 * 
 * Initial date: 5 Nov 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CostCenterDataModel extends DefaultFlexiTableDataModel<CostCenterRow>
implements SortableFlexiTableDataModel<CostCenterRow> {
	
	private static final CostCenterCols[] COLS = CostCenterCols.values();
	private final Locale locale;
	
	public CostCenterDataModel(FlexiTableColumnModel columnsModel, Locale locale) {
		super(columnsModel);
		this.locale = locale;
	}

	@Override
	public void sort(SortKey orderBy) {
		List<CostCenterRow> rows = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
		super.setObjects(rows);
	}

	@Override
	public Object getValueAt(int row, int col) {
		CostCenterRow emailDomainRow = getObject(row);
		return getValueAt(emailDomainRow, col);
	}

	@Override
	public Object getValueAt(CostCenterRow row, int col) {
		switch(COLS[col]) {
			case id: return row.getCostCenter().getKey();
			case name: return row.getCostCenter().getName();
			case account: return row.getCostCenter().getAccount();
			case enabled: return Boolean.valueOf(row.getCostCenter().isEnabled());
			case numOffers: return row.getNumOffers();
			case tools: return row.getToolsLink();
			default: return null;
		}
	}
	
	public enum CostCenterCols implements FlexiSortableColumnDef {
		id("cost.center.id"),
		name("cost.center.name"),
		account("cost.center.account"),
		numOffers("cost.center.num.offers"),
		enabled("cost.center.enabled"),
		tools("tools");
		
		private final String i18nKey;
		
		private CostCenterCols(String i18nKey) {
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
