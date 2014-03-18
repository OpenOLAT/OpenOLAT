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
package org.olat.course.nodes.cal;

import java.text.DateFormat;
import java.util.List;

import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.OLATRuntimeException;

/**
 * <h3>Description:</h3>
 * <p>Datamodel for the table of events in the PeekViewController
 * <p>
 * Initial Date:  10 nov. 2009 <br>
 * @author srosse, stephane.rosse@frentix.com, www.frentix.com
 */
public class CourseCalendarPeekViewModel extends DefaultTableDataModel<KalendarEvent> {
	private static final int COLUMNS = 2;
	private int MAX_SUBJECT_LENGTH = 30;
	
	private final Translator translator;
	private final DateFormat dateFormat, timeFormat, dateOnlyFormat;

	public CourseCalendarPeekViewModel(List<KalendarEvent> events, Translator translator) {
		super(events);
		this.translator = translator;
		
		dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, translator.getLocale());
		timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT, translator.getLocale());
		dateOnlyFormat = DateFormat.getDateInstance(DateFormat.SHORT, translator.getLocale());
	}

	public int getColumnCount() {
		return COLUMNS;
	}

	public Object getValueAt(int row, int col) {
		KalendarEvent event = getObject(row);
		switch (col) {
			case 0:
				if (event.isToday() && event.isAllDayEvent()) return translator.translate("calendar.today");
				if (event.isToday()) return translator.translate("calendar.today") + " " + timeFormat.format(event.getBegin()) + " - "
						+ timeFormat.format(event.getEnd());
				if (event.isAllDayEvent()) return dateOnlyFormat.format(event.getBegin());
				if (event.isWithinOneDay()) return dateOnlyFormat.format(event.getBegin()) + " " + timeFormat.format(event.getBegin()) + " - "
						+ timeFormat.format(event.getEnd());
				return dateFormat.format(event.getBegin()) + " - " + dateFormat.format(event.getEnd());
			case 1:
				String subj = event.getSubject();
				if (subj.length() > MAX_SUBJECT_LENGTH) subj = subj.substring(0, MAX_SUBJECT_LENGTH) + "...";
				return subj;
		}
		throw new OLATRuntimeException("Unreacheable code.", null);
	}
}