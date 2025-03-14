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
package org.olat.modules.curriculum.ui.copy;

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
 * Initial date: 19 févr. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CopyElementDetailsResourcesTableModel extends DefaultFlexiTableDataModel<CopyElementDetailsResourcesRow>
implements SortableFlexiTableDataModel<CopyElementDetailsResourcesRow> {
	
	private CopyResourcesCols[] COLS = CopyResourcesCols.values();
	
	private final Locale locale;
	
	public CopyElementDetailsResourcesTableModel(FlexiTableColumnModel columnModel, Locale locale) {
		super(columnModel);
		this.locale = locale;
	}
	
	@Override
	public void sort(SortKey orderBy) {
		if (orderBy != null) {
			List<CopyElementDetailsResourcesRow> sorted = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
			super.setObjects(sorted);
		}
	}

	@Override
	public Object getValueAt(int row, int col) {
		CopyElementDetailsResourcesRow resourcesRow = getObject(row);
		return getValueAt(resourcesRow, col);
	}

	@Override
	public Object getValueAt(CopyElementDetailsResourcesRow row, int col) {
		return switch(COLS[col]) {
			case activity -> row.copySetting();
			case displayname -> row.displayName();
			case externalId -> row.externalId();
			case externalRef -> row.externalRef();
			case access -> row.entryStatus();
			case lifecycle -> row.lifecycle();
			case numOfEvents -> row.numOfEvents();
			default -> "ERROR";
		};
	}
	
	public enum CopyResourcesCols implements FlexiSortableColumnDef {
		activity("table.header.activity"),
		key("table.header.key"),
		displayname("table.header.title"),
		externalId("table.header.external.id"),
		externalRef("table.header.external.ref"),
		access("table.header.access"),
		lifecycle("table.header.lifecycle"),
		numOfEvents("table.header.num.of.lecture.blocks");

		private final String i18nKey;

		private CopyResourcesCols(String i18nKey) {
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
