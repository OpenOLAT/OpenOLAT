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
package org.olat.modules.forms.ui;

import java.util.List;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;

/**
 * 
 * Initial date: 23.05.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class SessionSelectionModel extends DefaultFlexiTableDataModel<SessionSelectionRow>
		implements SortableFlexiTableDataModel<SessionSelectionRow> {
	
	public SessionSelectionModel(FlexiTableColumnModel columnsModel) {
		super(columnsModel);
	}

	@Override
	public void sort(SortKey sortKey) {
		List<SessionSelectionRow> rows = new SortableFlexiTableModelDelegate<>(sortKey, this, null).sort();
		super.setObjects(rows);
	}

	@Override
	public Object getValueAt(int row, int col) {
		SessionSelectionRow sessionSelectionRow = getObject(row);
		return getValueAt(sessionSelectionRow, col);
	}

	@Override
	public Object getValueAt(SessionSelectionRow row, int col) {
		switch(SessionSelectionCols.values()[col]) {
			case participant: return row.getParticipant();
			case firstname: return row.getSession().getFirstname();
			case lastname: return row.getSession().getLastname();
			case email: return row.getSession().getEmail();
			case age: return row.getSession().getAge();
			case gender: return row.getSession().getGender();
			case orgUnit: return row.getSession().getOrgUnit();
			case studySubject: return row.getSession().getStudySubject();
			default: return null;
		}	
	}
	
	public enum SessionSelectionCols implements FlexiSortableColumnDef {
		participant("report.session.participant"),
		firstname("report.session.dummy"),
		lastname("report.session.dummy"),
		email("report.session.dummy"),
		age("report.session.dummy"),
		gender("report.session.dummy"),
		orgUnit("report.session.dummy"),
		studySubject("report.session.dummy");
		
		private final String i18nKey;
		
		private SessionSelectionCols(String i18nKey) {
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
