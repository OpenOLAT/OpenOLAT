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
package org.olat.modules.bigbluebutton.ui.recurring;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.commons.calendar.CalendarModule;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;

/**
 * 
 * Initial date: 6 avr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RecurringMeetingsTableModel extends DefaultFlexiTableDataModel<RecurringMeeting>
implements SortableFlexiTableDataModel<RecurringMeeting> {
	
	private static final RecurringCols[] COLS = RecurringCols.values();
	
	private final Locale locale;
	private final Translator translator;
	
	public RecurringMeetingsTableModel(FlexiTableColumnModel columnsModel, Translator translator) {
		super(columnsModel);
		locale = translator.getLocale();
		this.translator = Util.createPackageTranslator(CalendarModule.class, locale, translator);
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<RecurringMeeting> rows = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
			super.setObjects(rows);
		}
	}

	@Override
	public Object getValueAt(int row, int col) {
		RecurringMeeting meeting = getObject(row);
		return getValueAt(meeting, col);
	}

	@Override
	public Object getValueAt(RecurringMeeting row, int col) {
		switch(COLS[col]) {
			case available: return row.isSlotAvailable();
			case dayOfWeek: return getDayOfWeek(row);
			case start: return row.getStartDate();
			case end: return row.getEndDate();
			case delete: return row.isDeleted();
			default: return "ERROR";
		}
	}
	
	private String getDayOfWeek(RecurringMeeting row) {
		Date start = row.getStartDate();
		Calendar cal = Calendar.getInstance();
		cal.setTime(start);
		int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
		switch(dayOfWeek) {
			case Calendar.MONDAY: return translator.translate("cal.mon");
			case Calendar.TUESDAY: return translator.translate("cal.tue");
			case Calendar.WEDNESDAY: return translator.translate("cal.wed");
			case Calendar.THURSDAY: return translator.translate("cal.thu");
			case Calendar.FRIDAY: return translator.translate("cal.fri");
			case Calendar.SATURDAY: return translator.translate("cal.sat");
			case Calendar.SUNDAY: return translator.translate("cal.sun");
			default: return "";
		}
	}
	
	public enum RecurringCols implements FlexiSortableColumnDef {

		available("table.header.available"),
		dayOfWeek("table.header.day.week"),
		start("meeting.start"),
		end("meeting.end"),
		delete("delete");
		
		private final String i18nHeaderKey;
		
		private RecurringCols(String i18nHeaderKey) {
			this.i18nHeaderKey = i18nHeaderKey;
		}

		@Override
		public boolean sortable() {
			return true;
		}

		@Override
		public String sortKey() {
			return name();
		}

		@Override
		public String i18nHeaderKey() {
			return i18nHeaderKey;
		}
	}
}
