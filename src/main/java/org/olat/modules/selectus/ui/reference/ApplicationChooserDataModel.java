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
package org.olat.modules.selectus.ui.reference;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.core.util.StringHelper;

import org.olat.modules.selectus.model.ApplicationLight;

/**
 * 
 * Initial date: 15.09.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ApplicationChooserDataModel extends DefaultFlexiTableDataModel<ApplicationLight>
	implements SortableFlexiTableDataModel<ApplicationLight> {
	
	private static final AppCols[] COLS = AppCols.values();
	
	private final Locale locale;
	private List<ApplicationLight> backupRows;
	
	public ApplicationChooserDataModel(FlexiTableColumnModel columnsModel, Locale locale) {
		super(columnsModel);
		this.locale = locale;
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<ApplicationLight> views = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
			super.setObjects(views);
		}
	}
	
	public void quickSearch(String searchString) {
		if(StringHelper.containsNonWhitespace(searchString)) {
			String search = searchString.toLowerCase();
			List<ApplicationLight> filteredRows = new ArrayList<>();
			for(ApplicationLight row:backupRows) {
				if(accept(search, row)) {
					filteredRows.add(row);
				}
			}
			super.setObjects(filteredRows);
		} else {
			super.setObjects(backupRows);
		}
	}
	
	private boolean accept(String searchString, ApplicationLight row) {
		for(AppCols col:COLS) {
			Object obj = getValueAt(row, col.ordinal());
			if(obj instanceof Integer) {
				obj = obj.toString();
			}
			if(obj instanceof String) {
				String str = (String)obj;
				if(str.toLowerCase().contains(searchString)) {
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		ApplicationLight ref = getObject(row);
		return getValueAt(ref, col);
	}

	@Override
	public Object getValueAt(ApplicationLight app, int col) {
		switch(COLS[col]) {
			case id: return app.getId();
			case title: return app.getPerson().getTitle();
			case firstName: return app.getPerson().getFirstName();
			case lastName: return app.getPerson().getLastName();
			case mail: return app.getPerson().getMail();
			case projectTitle: return app.getProject().getTitle();
			default: return "ERROR";
		}
	}

	@Override
	public void setObjects(List<ApplicationLight> objects) {
		backupRows = new ArrayList<>(objects);
		super.setObjects(objects);
	}
	
	public enum AppCols implements FlexiSortableColumnDef {
		id("edit.application.id"),
		title("edit.application.title"),
		firstName("edit.application.firstName"),
		lastName("edit.application.lastName"),
		mail("edit.application.mail"),
		//project
		projectTitle("table.header.project.title"),
		;
		
		private final String i18nKey;
		
		private AppCols(String i18nKey) {
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
