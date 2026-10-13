/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* frentix GmbH, Switzerland, http://www.frentix.com
* <p>
*/

package org.olat.user.propertyhandlers.ui;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;

/**
 *
 * Description:<br>
 * table model for the UserPropertyUsageContext rows
 *
 * <P>
 * Initial Date:  29.08.2011 <br>
 * @author strentini
 */
public class UsrPropContextCfgTableModel extends DefaultFlexiTableDataModel<UsrPropContextRow>
		implements SortableFlexiTableDataModel<UsrPropContextRow> {

	private static final ContextCols[] COLS = ContextCols.values();

	public UsrPropContextCfgTableModel(FlexiTableColumnModel columnModel) {
		super(columnModel);
	}

	@Override
	public void sort(SortKey orderBy) {
		if (orderBy != null) {
			setObjects(new SortableFlexiTableModelDelegate<>(orderBy, this, null).sort());
		}
	}

	@Override
	public Object getValueAt(int row, int col) {
		return getValueAt(getObject(row), col);
	}

	@Override
	public Object getValueAt(UsrPropContextRow row, int col) {
		return switch (COLS[col]) {
			case name -> row.name();
			case description -> row.description();
			case count -> Integer.valueOf(row.activeCount());
		};
	}

	public enum ContextCols implements FlexiSortableColumnDef {
		name("upc.name"),
		description("upc.description"),
		count("upc.context.propcount");

		private final String i18nKey;

		private ContextCols(String i18nKey) {
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
