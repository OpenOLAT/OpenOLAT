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
package org.olat.modules.lecture.ui;

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.modules.lecture.RollCallSecurityCallback;

/**
 * 
 * Initial date: 27 mars 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TeacherRollCallDataModel extends DefaultFlexiTableDataModel<TeacherRollCallRow>
	implements SortableFlexiTableDataModel<TeacherRollCallRow> {

	private final Locale locale;
	private final RollCallSecurityCallback secCallback;
	
	public TeacherRollCallDataModel(FlexiTableColumnModel columnModel, RollCallSecurityCallback secCallback, Locale locale) {
		super(columnModel);
		this.locale = locale;
		this.secCallback = secCallback;
	}

	@Override
	public void sort(SortKey orderBy) {
		List<TeacherRollCallRow> rows = new TeacherRollCallSortDelegate(orderBy, this, locale).sort();
		super.setObjects(rows);
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		TeacherRollCallRow rollCall = getObject(row);
		return getValueAt(rollCall, col);
	}

	@Override
	public Object getValueAt(TeacherRollCallRow row, int col) {
		if(col < TeacherRollCallController.USER_PROPS_OFFSET) {
			switch(RollCols.values()[col]) {
				case username: return row.getIdentityName();
				case status: return row.getRollCallStatusEl();
				case authorizedAbsence: return row.getAuthorizedAbsenceCont();
				case comment: return row.getCommentEl();
				case all: return row.getAllLink();
				default: return null;
			}
		} else if(col < TeacherRollCallController.CHECKBOX_OFFSET) {
			int propPos = col - TeacherRollCallController.USER_PROPS_OFFSET;
			return row.getIdentityProp(propPos);
		}
		
		int propPos = col - TeacherRollCallController.CHECKBOX_OFFSET;
		return row.getCheck(propPos);
	}

	@Override
	public DefaultFlexiTableDataModel<TeacherRollCallRow> createCopyWithEmptyList() {
		return new TeacherRollCallDataModel(getTableColumnModel(), secCallback, locale);
	}
	
	public enum RollCols implements FlexiSortableColumnDef {
		username("table.header.username"),
		status("table.header.status"),
		authorizedAbsence("table.header.authorized.absence"),
		comment("table.header.comment"),
		all("all");
		
		private final String i18nKey;
		
		private RollCols(String i18nKey) {
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
