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
package org.olat.commons.calendar.manager;

import org.olat.commons.calendar.CalendarManager;
import org.olat.core.id.Identity;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.user.UserDataDeletable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 03.05.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service("calendarUserDataDeleteManager")
public class ICalFileCalendarUserDeleteManager implements UserDataDeletable {
	
	private static final Logger log = Tracing.createLoggerFor(ICalFileCalendarUserDeleteManager.class);

	@Autowired
	private CalendarManager calendarManager;
	@Autowired
	private ImportToCalendarManager importToCalendarManager;

	@Override
	public void deleteUserData(Identity identity, String newDeletedUserName) {
		importToCalendarManager.deletePersonalImportedCalendars(identity);
		log.debug("Personal imported calendars deleted for identity={}", identity);
		calendarManager.deletePersonalCalendar(identity);
		log.debug("Personal calendar deleted for identity={}", identity);
	}
}
