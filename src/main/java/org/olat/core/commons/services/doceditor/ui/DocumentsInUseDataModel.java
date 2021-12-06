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
package org.olat.core.commons.services.doceditor.ui;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.commons.services.doceditor.DocEditor.Mode;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FilterableFlexiTableModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.modules.fo.ui.ForumUserDataModel.UserCols;

/**
 * 
 * Initial date: 26 Mar 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class DocumentsInUseDataModel extends DefaultFlexiTableDataModel<DocumentsInUseRow>
implements SortableFlexiTableDataModel<DocumentsInUseRow>, FilterableFlexiTableModel {
	
	private final Locale locale;
	private List<DocumentsInUseRow> backups;
	
	public DocumentsInUseDataModel(FlexiTableColumnModel columnsModel, Locale locale) {
		super(columnsModel);
		this.locale = locale;
	}
	
	@Override
	public void setObjects(List<DocumentsInUseRow> objects) {
		super.setObjects(objects);
		backups = objects;
	}
	
	@Override
	public void filter(String searchString, List<FlexiTableFilter> filters) {
		if(filters != null && !filters.isEmpty() && filters.get(0) != null && !filters.get(0).isShowAll()) {
			String filterKey = filters.get(0).getFilter();
			Mode mode = Mode.valueOf(filterKey);
			List<DocumentsInUseRow> filteredRows = backups.stream()
						.filter(row -> mode == row.getMode())
						.collect(Collectors.toList());
			super.setObjects(filteredRows);
		} else {
			super.setObjects(backups);
		}
	}

	@Override
	public void sort(SortKey orderBy) {
		List<DocumentsInUseRow> rows = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
		super.setObjects(rows);
	}

	@Override
	public Object getValueAt(int row, int col) {
		DocumentsInUseRow reason = getObject(row);
		return getValueAt(reason, col);
	}

	@Override
	public Object getValueAt(DocumentsInUseRow row, int col) {
		if(col >= 0 && col < UserCols.values().length) {
			switch(DocumentsInUseCols.values()[col]) {
			case fileName: return row.getFilename();
			case app: return row.getApp();
			case edit: return row.getMode();
			case opened: return row.getOpened();
			case editStart: return row.getEditStartDate();
			default: return null;
		}
		}
		
		int propPos = col - DocumentsInUseListController.USER_PROPS_OFFSET;
		return row.getIdentityProp(propPos);
	}
	
	public enum DocumentsInUseCols implements FlexiSortableColumnDef {
		fileName("table.header.file.name"),
		app("table.header.app"),
		edit("table.header.edit"),
		opened("table.header.opened"),
		editStart("table.header.edit.start");
		
		private final String i18nKey;
		
		private DocumentsInUseCols(String i18nKey) {
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
