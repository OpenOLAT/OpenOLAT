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
package org.olat.modules.project.ui.event;

import org.olat.core.gui.control.Event;

/**
 * 
 * Initial date: 26 Apr 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class QuickStartEvent extends Event {
	
	private static final long serialVersionUID = -3043710614034939494L;
	
	public static final Event CALENDAR_EVENT = new QuickStartEvent(true, false, false, false);
	public static final Event TODOS_EVENT = new QuickStartEvent(false, true, false, false);
	public static final Event NOTES_EVENT = new QuickStartEvent(false, false, true, false);
	public static final Event FILES_EVENT = new QuickStartEvent(false, false, false, true);
	
	private final boolean calendar;
	private final boolean todos;
	private final boolean notes;
	private final boolean files;
	
	private QuickStartEvent(boolean calendar, boolean todos, boolean notes, boolean files) {
		super("quick-start");
		this.calendar = calendar;
		this.todos = todos;
		this.notes = notes;
		this.files = files;
	}

	public boolean isCalendar() {
		return calendar;
	}

	public boolean isTodos() {
		return todos;
	}

	public boolean isNotes() {
		return notes;
	}

	public boolean isFiles() {
		return files;
	}

}
