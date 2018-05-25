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

import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataSourceModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataSourceDelegate;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 23.05.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class SessionSelectionModel extends DefaultFlexiTableDataSourceModel<SessionSelectionRow> {
	
	private final ReportHelper reportHelper;
	
	public SessionSelectionModel(FlexiTableDataSourceDelegate<SessionSelectionRow> dataSource,
			FlexiTableColumnModel columnsModel, ReportHelper reportHelper) {
		super(dataSource, columnsModel);
		this.reportHelper = reportHelper;
	}

	@Override
	public Object getValueAt(int row, int col) {
		SessionSelectionRow sessionSelectionRow = getObject(row);
		switch(SessionSelectionCols.values()[col]) {
			case submissionDate: return sessionSelectionRow.getSession().getSubmissionDate();
			case firstname: return sessionSelectionRow.getSession().getFirstname();
			case lastname: {
				boolean hasFirstname = StringHelper.containsNonWhitespace(sessionSelectionRow.getSession().getFirstname());
				String lastname = sessionSelectionRow.getSession().getLastname();
				boolean hasLastname = StringHelper.containsNonWhitespace(lastname);
				return hasFirstname || hasLastname
						? lastname
						: reportHelper.getLegend(sessionSelectionRow.getSession()).getName();
			}
			case email: return sessionSelectionRow.getSession().getEmail();
			case age: return sessionSelectionRow.getSession().getAge();
			case gender: return sessionSelectionRow.getSession().getGender();
			case orgUnit: return sessionSelectionRow.getSession().getOrgUnit();
			case studySubject: return sessionSelectionRow.getSession().getStudySubject();
			default: return null;
		}	
	}

	@Override
	public DefaultFlexiTableDataSourceModel<SessionSelectionRow> createCopyWithEmptyList() {
		return new SessionSelectionModel(getSourceDelegate(),  getTableColumnModel(), reportHelper);
	}
	
	public enum SessionSelectionCols implements FlexiSortableColumnDef {
		submissionDate("report.session.submission.date"),
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
