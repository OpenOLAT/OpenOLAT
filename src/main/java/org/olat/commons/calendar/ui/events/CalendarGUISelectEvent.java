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

import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.commons.calendar.ui.components.KalendarRenderWrapper;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;

/**
 * 
 * Initial date: 09.04.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CalendarGUISelectEvent extends FormEvent {

	private static final long serialVersionUID = 2410000447684587428L;
	public static final String CMD_SELECT = "scalevent";
	
	private String targetDomId;
	private KalendarEvent	event;
	private KalendarRenderWrapper calendarWrapper;
	
	public CalendarGUISelectEvent(FormItem item, KalendarEvent event, KalendarRenderWrapper calendarWrapper, String targetDomId) {
		super(CMD_SELECT, item);
		this.targetDomId = targetDomId;
		this.event = event;
		this.calendarWrapper = calendarWrapper;
	}

	public String getTargetDomId() {
		return targetDomId;
	}

	public KalendarEvent getKalendarEvent() {
		return event;
	}

	public KalendarRenderWrapper getKalendarRenderWrapper() {
		return calendarWrapper;
	}
}
