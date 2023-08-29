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
package org.olat.modules.project.ui;

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;

/**
 * 
 * Initial date: 12 Dez 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjFileDataModel extends DefaultFlexiTableDataModel<ProjFileRow> implements SortableFlexiTableDataModel<ProjFileRow> {
	
	private static final FileCols[] COLS = FileCols.values();

	private final Locale locale;
	
	public ProjFileDataModel(FlexiTableColumnModel columnsModel, Locale locale) {
		super(columnsModel);
		this.locale = locale;
	}

	@Override
	public void sort(SortKey orderBy) {
		List<ProjFileRow> rows = new ProjFileRowSortDelegate(orderBy, this, locale).sort();
		super.setObjects(rows);
	}

	@Override
	public Object getValueAt(int row, int col) {
		ProjFileRow file = getObject(row);
		return getValueAt(file, col);
	}

	@Override
	public Object getValueAt(ProjFileRow row, int col) {
		switch(COLS[col]) {
		case id: return row.getKey();
		case displayName: return row.getSelectClassicLink();
		case tags: return row.getFormattedTags();
		case involved: return row.getUserPortraits();
		case creationDate: return row.getCreationDate();
		case lastModifiedDate: return row.getLastModifiedDate();
		case lastModifiedBy: return row.getLastModifiedByName();
		case deletedDate: return row.getDeletedDate();
		case deletedBy: return row.getDeletedByName();
		case tools: return row.getToolsLink();
		default: return null;
		}
	}
	
	public enum FileCols implements FlexiSortableColumnDef {
		id("id"),
		displayName("title"),
		tags("tags"),
		involved("involved"),
		creationDate("created"),
		lastModifiedDate("last.modified.date"),
		lastModifiedBy("last.modified.by"),
		deletedBy("deleted.by"),
		deletedDate("deleted.date"),
		tools("tools");
		
		private final String i18nKey;
		
		private FileCols(String i18nKey) {
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
