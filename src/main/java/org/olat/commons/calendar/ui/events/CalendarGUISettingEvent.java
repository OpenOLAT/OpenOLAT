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
package org.olat.commons.calendar.ui.events;

import org.olat.commons.calendar.ui.components.KalendarRenderWrapper;
import org.olat.core.gui.control.Event;

/**
 * 
 * Initial date: 26.08.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CalendarGUISettingEvent extends Event {

	private static final long serialVersionUID = -5821116764500481869L;
	public static final String CMD_CHANGE = "chgsettingcla";
	private final KalendarRenderWrapper calendar;
	
	public CalendarGUISettingEvent(KalendarRenderWrapper calendar) {
		super(CMD_CHANGE);
		this.calendar = calendar;
	}

	public KalendarRenderWrapper getCalendar() {
		return calendar;
	}
}
