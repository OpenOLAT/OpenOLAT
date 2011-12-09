/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/
package org.olat.portal.calendar;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.core.gui.components.table.ColumnDescriptor;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.translator.Translator;

/**
 * @author Christian Guretzki
 */
class PortletDateColumnDescriptor extends DefaultColumnDescriptor implements ColumnDescriptor {

	private DateFormat timeFormat;
	private DateFormat dateOnlyFormat;
	private DateFormat dateFormat;
	private Translator translator;

	public PortletDateColumnDescriptor(String headerKey, int dataColumn, Translator translator) {
		super(headerKey, dataColumn, null, translator.getLocale());
		this.translator = translator;
		timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT, locale);
		dateOnlyFormat = DateFormat.getDateInstance(DateFormat.SHORT, locale);
		dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale);
	}
	
	/**
	 * Render different for all-day events and none all-day events like e.g. :
	 * Today all day - 31.03.10
	 * Today 18:00 - 19:00
	 * 30.03.10 all day
	 * 30.03.10 - 31.03.10 all day
	 * 30.03.10 07:00 - 08:00
	 * @see org.olat.core.gui.components.table.DefaultColumnDescriptor#renderValue(org.olat.core.gui.render.StringOutput, int, org.olat.core.gui.render.Renderer)
	 */
	@Override
	public void renderValue(StringOutput sb, int row, Renderer renderer) {
		Object val = getModelData(row);
		if (val instanceof KalendarEvent) {
			KalendarEvent event = (KalendarEvent)val;
			if (event.isToday() && event.isAllDayEvent()) {
				sb.append( translator.translate("calendar.today.all.day") );
			} else if (event.isToday()) {
				sb.append( translator.translate("calendar.title") +" "+ timeFormat.format(event.getBegin()) + " - " + timeFormat.format(event.getEnd()) );
			} else if (event.isAllDayEvent()) {
				Calendar tomorrow = Calendar.getInstance();
				tomorrow.add(Calendar.DATE, 1);
				if (event.getBegin().before( new Date() ) ) {
					sb.append( translator.translate("calendar.today.all.day") );
					if ( event.getEnd().after( tomorrow.getTime() ) ) {
						sb.append( " - ");
						sb.append( dateOnlyFormat.format(event.getEnd()));
					}
				} else {
					sb.append( dateOnlyFormat.format(event.getBegin()));
					if ( event.getEnd().after( tomorrow.getTime() ) ) {
						sb.append( " - ");
						sb.append( dateOnlyFormat.format(event.getEnd()));
					}
					sb.append(" ");
					sb.append( translator.translate("calendar.tomorrow.all.day") );
				}
			} else if (event.isWithinOneDay()) {
				sb.append( dateOnlyFormat.format(event.getBegin()) +" "+ timeFormat.format(event.getBegin()) + " - " + timeFormat.format(event.getEnd()) );
			} else {
				sb.append( dateFormat.format(event.getBegin()) + " - " + dateFormat.format(event.getEnd()) );
			}
		} else {
			sb.append(val.toString());
		}
	}
	
	@Override
	public int compareTo(int rowa, int rowb) {
		Object a = table.getTableDataModel().getValueAt(rowa,dataColumn);
		Object b = table.getTableDataModel().getValueAt(rowb,dataColumn);
		if ( (a instanceof KalendarEvent) && (b instanceof KalendarEvent) ) {
			Date begin0 = ((KalendarEvent) a).getBegin();
			Date begin1 = ((KalendarEvent) b).getBegin();
			return begin0.compareTo(begin1);
		}
		return 0;
	}

}
