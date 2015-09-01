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

import java.util.Date;
import java.util.List;

import org.olat.basesecurity.IdentityRef;
import org.olat.commons.calendar.model.ImportedCalendar;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 27.08.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class ImportedCalendarDAO {
	
	@Autowired
	private DB dbInstance;
	
	public ImportedCalendar createImportedCalendar(Identity owner, String displayName,
			String calendarId, String type, String url, Date lastUpdate) {
		
		ImportedCalendar calendar = new ImportedCalendar();
		calendar.setCreationDate(new Date());
		calendar.setLastModified(calendar.getCreationDate());
		calendar.setLastUpdate(lastUpdate);
		calendar.setDisplayName(displayName);
		calendar.setCalendarId(calendarId);
		calendar.setType(type);
		calendar.setUrl(url);
		calendar.setIdentity(owner);
		dbInstance.getCurrentEntityManager().persist(calendar);
		return calendar;
	}
	
	public ImportedCalendar update(ImportedCalendar importedCalendar) {
		importedCalendar.setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(importedCalendar);
	}
	
	public List<ImportedCalendar> getImportedCalendar(IdentityRef identity, String calendarId, String type) {
		return dbInstance.getCurrentEntityManager()
				.createNamedQuery("importedCalendarByIdentityCalendarIdAndType", ImportedCalendar.class)
				.setParameter("identityKey", identity.getKey())
				.setParameter("calendarId", calendarId)
				.setParameter("type", type)
				.getResultList();
	}
	
	public List<ImportedCalendar> getImportedCalendars(IdentityRef identity) {
		StringBuilder sb = new StringBuilder();
		sb.append("select cal from importedcal cal where cal.identity.key=:identityKey");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), ImportedCalendar.class)
				.setParameter("identityKey", identity.getKey())
				.getResultList();
	}
	
	public void deleteImportedCalendar(IdentityRef identity, String calendarId, String type) {
		List<ImportedCalendar> calendars = getImportedCalendar(identity, calendarId, type);
		for(ImportedCalendar calendar:calendars) {
			dbInstance.getCurrentEntityManager().remove(calendar);
		}
	}
}
