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
package org.olat.course.reminder.ui;

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.course.assessment.ui.tool.AssessmentToolConstants;

/**
 * 
 * Initial date: 3 Jun 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CourseReminderSendTableModel extends DefaultFlexiTableDataModel<CourseReminderSendRow>
	implements SortableFlexiTableDataModel<CourseReminderSendRow> {
	
	public static final int USER_PROPS_OFFSET = 500;
	public static final String USAGE_IDENTIFIER = CourseReminderSendTableModel.class.getCanonicalName();

	private static final SendCols[] COLS = SendCols.values();
	
	private final Locale locale;
	
	public CourseReminderSendTableModel(FlexiTableColumnModel columnModel, Locale locale) {
		super(columnModel);
		this.locale = locale;
	}
	
	@Override
	public void sort(SortKey orderBy) {
		if (orderBy != null) {
			List<CourseReminderSendRow> views = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
			super.setObjects(views);
		}
	}

	@Override
	public Object getValueAt(int row, int col) {
		CourseReminderSendRow identityRow = getObject(row);
		return getValueAt(identityRow, col);
	}

	@Override
	public Object getValueAt(CourseReminderSendRow row, int col) {
		if(col >= 0 && col < SendCols.values().length) {
			switch(COLS[col]) {
				case sendDate: return row.getSendDate();
				default: return "ERROR";
			}
		}
		int propPos = col - AssessmentToolConstants.USER_PROPS_OFFSET;
		return row.getIdentityProp(propPos);
	}

	@Override
	public DefaultFlexiTableDataModel<CourseReminderSendRow> createCopyWithEmptyList() {
		return new CourseReminderSendTableModel(getTableColumnModel(), locale);
	}
	
	public enum SendCols implements FlexiSortableColumnDef {
		sendDate("table.header.send");
		
		private final String i18nKey;

		private SendCols(String i18nKey) {
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