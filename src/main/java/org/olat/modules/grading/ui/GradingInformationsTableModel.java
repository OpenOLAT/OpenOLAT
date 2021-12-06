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
package org.olat.modules.grading.ui;

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.modules.lecture.ui.TeacherRollCallController;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: 22 janv. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GradingInformationsTableModel extends DefaultFlexiTableDataModel<GradingInformationsRow>
implements SortableFlexiTableDataModel<GradingInformationsRow> {

	private static final GInfosCol[] COLS = GInfosCol.values();
	
	private final Locale locale;
	private final List<UserPropertyHandler> userPropertyHandlers;
	
	public GradingInformationsTableModel(FlexiTableColumnModel columnModel, List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		super(columnModel);
		this.locale = locale;
		this.userPropertyHandlers = userPropertyHandlers;
	}

	@Override
	public void sort(SortKey sortKey) {
		//
	}

	@Override
	public Object getValueAt(int row, int col) {
		GradingInformationsRow infosRow = getObject(row);
		return getValueAt(infosRow, col);
	}

	@Override
	public Object getValueAt(GradingInformationsRow row, int col) {
		if(col >= 0 && col < COLS.length) {
			switch(COLS[col]) {
				case status: return row.getGraderStatus();
				case absence: return row;
				default: return "ERROR";
			}
		}

		if (col >= TeacherRollCallController.USER_PROPS_OFFSET && col < userPropertyHandlers.size() + TeacherRollCallController.USER_PROPS_OFFSET) {
			// get user property for this column
			UserPropertyHandler userPropertyHandler = userPropertyHandlers.get(col - TeacherRollCallController.USER_PROPS_OFFSET);
			String value = userPropertyHandler.getUserProperty(row.getGrader().getUser(), locale);
			return (value == null ? "n/a" : value);
		}
		return "ERROR";
	}

	public enum GInfosCol implements FlexiSortableColumnDef {
		status("table.header.status"),
		absence("table.header.absence.leave");
		
		private final String i18nKey;
		
		private GInfosCol(String i18nKey) {
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
