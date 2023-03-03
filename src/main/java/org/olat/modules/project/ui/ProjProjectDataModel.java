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
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiBusinessPathModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;

/**
 * 
 * Initial date: 23 Nov 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjProjectDataModel extends DefaultFlexiTableDataModel<ProjProjectRow> implements SortableFlexiTableDataModel<ProjProjectRow>, FlexiBusinessPathModel {
	
	private static final ProjectCols[] COLS = ProjectCols.values();

	private final Locale locale;
	
	public ProjProjectDataModel(FlexiTableColumnModel columnsModel, Locale locale) {
		super(columnsModel);
		this.locale = locale;
	}
	
	public ProjProjectRow getObjectByKey(Long key) {
		List<ProjProjectRow> rows = getObjects();
		for (ProjProjectRow row: rows) {
			if (row != null && row.getKey().equals(key)) {
				return row;
			}
		}
		return null;
	}

	@Override
	public void sort(SortKey orderBy) {
		List<ProjProjectRow> rows = new ProjProjectRowSortDelegate(orderBy, this, locale).sort();
		super.setObjects(rows);
	}
	
	@Override
	public String getUrl(Component source, Object object, String action) {
		if(action == null) return null;
		
		ProjProjectRow row = (ProjProjectRow)object;
		if("select".equals(action)) {
			return row.getUrl();
		}
		return null;
	}

	@Override
	public Object getValueAt(int row, int col) {
		ProjProjectRow project = getObject(row);
		return getValueAt(project, col);
	}

	@Override
	public Object getValueAt(ProjProjectRow row, int col) {
		switch(COLS[col]) {
		case id: return row.getKey();
		case externalRef: return row.getExternalRef();
		case title: return row.getTitle();
		case teaser: return row.getTeaser();
		case status: return row;
		case lastAcitivityDate: return row.getLastActivityDate();
		case owners: return row.getOwnersNames();
		default: return null;
		}
	}
	
	public enum ProjectCols implements FlexiSortableColumnDef {
		id("id"),
		externalRef("project.external.ref"),
		title("project.title"),
		teaser("project.teaser"),
		status("status"),
		lastAcitivityDate("project.last.activity.date"),
		owners("project.owners");
		
		private final String i18nKey;
		
		private ProjectCols(String i18nKey) {
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
