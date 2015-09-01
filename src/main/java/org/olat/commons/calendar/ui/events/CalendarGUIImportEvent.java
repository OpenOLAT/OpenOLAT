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
 * <h3>Description:</h3>
 * <p>
 * Initial Date:  4 feb. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, www.frentix.com
 */
public class CalendarGUIImportEvent extends Event {

	private static final long serialVersionUID = 6716347235221049088L;
	public static final String CMD_IMPORT = "acalevent";
	private final KalendarRenderWrapper calendar;
	
	public CalendarGUIImportEvent(KalendarRenderWrapper calendar) {
		super(CMD_IMPORT);
		this.calendar = calendar;
	}

	public KalendarRenderWrapper getCalendar() {
		return calendar;
	}
}
