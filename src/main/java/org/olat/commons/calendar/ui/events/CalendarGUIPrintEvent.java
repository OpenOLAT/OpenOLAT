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

import java.util.Date;

import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;

/**
 * 
 * Initial date: 17.05.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CalendarGUIPrintEvent extends FormEvent {

	private static final long serialVersionUID = -3476226273509422030L;
	public static final String CMD_PRINT = "printcalevent";
	
	private Date from;
	private Date to;
	private String targetDomId;
	
	public CalendarGUIPrintEvent(FormItem source, String targetDomId) {
		super(CMD_PRINT, source);
		this.targetDomId = targetDomId;
	}
	public CalendarGUIPrintEvent(Date from, Date to) {
		super(CMD_PRINT, null);
		this.from = from;
		this.to = to;
	}

	public String getTargetDomId() {
		return targetDomId;
	}
	
	public Date getFrom() {
		return from;
	}

	public Date getTo() {
		return to;
	}
}
