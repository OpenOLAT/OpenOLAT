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

import org.olat.commons.calendar.model.ImportedToCalendar;
import org.olat.core.commons.persistence.DB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 27.08.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class ImportedToCalendarDAO {
	
	@Autowired
	private DB dbInstance;
	
	public ImportedToCalendar createImportedToCalendar(String toCalendarId, String toType, String url, Date lastUpdate) {
		ImportedToCalendar calendar = new ImportedToCalendar();
		calendar.setCreationDate(new Date());
		calendar.setLastModified(calendar.getCreationDate());
		calendar.setLastUpdate(lastUpdate);
		calendar.setToCalendarId(toCalendarId);
		calendar.setToType(toType);
		calendar.setUrl(url);
		dbInstance.getCurrentEntityManager().persist(calendar);
		return calendar;
	}
	
	public ImportedToCalendar update(ImportedToCalendar importedToCalendar) {
		importedToCalendar.setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(importedToCalendar);
	}
	
	public List<ImportedToCalendar> getImportedToCalendars(String toCalendarId, String toType, String url) {
		return dbInstance.getCurrentEntityManager()
				.createNamedQuery("importedToCalendarByIdTypeAndUrl", ImportedToCalendar.class)
				.setParameter("toCalendarId", toCalendarId)
				.setParameter("toType", toType)
				.setParameter("url", url)
				.getResultList();
	}
	
	public List<ImportedToCalendar> getImportedToCalendars(String toCalendarId, String toType) {
		return dbInstance.getCurrentEntityManager()
				.createNamedQuery("importedToCalendarByIdAndType", ImportedToCalendar.class)
				.setParameter("toCalendarId", toCalendarId)
				.setParameter("toType", toType)
				.getResultList();
	}
	
	public List<ImportedToCalendar> getImportedToCalendars() {
		return dbInstance.getCurrentEntityManager()
				.createNamedQuery("importedToCalendars", ImportedToCalendar.class)
				.getResultList();
	}
	
	public void delete(ImportedToCalendar importedToCalendar) {
		ImportedToCalendar reloadedImportedToCalendar = dbInstance.getCurrentEntityManager()
			.getReference(ImportedToCalendar.class, importedToCalendar.getKey());
		dbInstance.getCurrentEntityManager()
			.remove(reloadedImportedToCalendar);
	}
}
