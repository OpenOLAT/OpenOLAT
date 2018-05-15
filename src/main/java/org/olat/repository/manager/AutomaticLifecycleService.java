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
package org.olat.repository.manager;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.olat.commons.calendar.CalendarUtils;
import org.olat.core.commons.persistence.DB;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryLifeCycleValue;
import org.olat.repository.RepositoryEntryManagedFlag;
import org.olat.repository.RepositoryEntryStatus;
import org.olat.repository.RepositoryModule;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 2 déc. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class AutomaticLifecycleService {
	
	private static final OLog log = Tracing.createLoggerFor(AutomaticLifecycleService.class);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private RepositoryModule repositoryModule;
	@Autowired
	private RepositoryService repositoryService;
	
	public void manage() {
		close();
		unpublish();
		delete();
	}
	
	private void close() {
		String autoClose = repositoryModule.getLifecycleAutoClose();
		if(StringHelper.containsNonWhitespace(autoClose)) {
			RepositoryEntryLifeCycleValue autoCloseVal = RepositoryEntryLifeCycleValue.parse(autoClose);
			Date markerDate = autoCloseVal.limitDate(new Date());
			List<RepositoryEntry> entriesToClose = getRepositoryEntriesToClose(markerDate);
			for(RepositoryEntry entry:entriesToClose) {
				try {
					boolean closeManaged = RepositoryEntryManagedFlag.isManaged(entry, RepositoryEntryManagedFlag.close);
					if(!closeManaged) {
						log.audit("Automatic closing course: " + entry.getDisplayname() + " [" + entry.getKey() + "]");
						repositoryService.closeRepositoryEntry(entry);
						dbInstance.commit();
					}
				} catch (Exception e) {
					log.error("",  e);
					dbInstance.commitAndCloseSession();
				}
			}
		}
	}
	
	public List<RepositoryEntry> getRepositoryEntriesToClose(Date date) {
		StringBuilder sb = new StringBuilder();
		sb.append("select v from repositoryentry as v ")
		  .append(" inner join fetch v.olatResource as ores")
		  .append(" inner join fetch v.statistics as statistics")
		  .append(" inner join fetch v.lifecycle as lifecycle")
		  .append(" where lifecycle.validTo<:now and v.statusCode=").append(RepositoryEntryStatus.REPOSITORY_STATUS_OPEN)
		  .append(" and v.access>=").append(RepositoryEntry.ACC_OWNERS);
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		CalendarUtils.getEndOfDay(cal);
		Date endOfDay = cal.getTime();
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RepositoryEntry.class)
				.setParameter("now", endOfDay)
				.getResultList();
	}
	
	private void unpublish() {
		String autoUnpublish = repositoryModule.getLifecycleAutoUnpublish();
		if(StringHelper.containsNonWhitespace(autoUnpublish)) {
			RepositoryEntryLifeCycleValue autoUnpublishVal = RepositoryEntryLifeCycleValue.parse(autoUnpublish);
			Date markerDate = autoUnpublishVal.limitDate(new Date());
			List<RepositoryEntry> entriesToUnpublish = getRepositoryEntriesToUnpublish(markerDate);
			for(RepositoryEntry entry:entriesToUnpublish) {
				try {
					boolean closeManaged = RepositoryEntryManagedFlag.isManaged(entry, RepositoryEntryManagedFlag.close);
					if(!closeManaged) {
						log.audit("Automatic unpublishing course: " + entry.getDisplayname() + " [" + entry.getKey() + "]");
						repositoryService.unpublishRepositoryEntry(entry);
						dbInstance.commit();
					}
				} catch (Exception e) {
					log.error("",  e);
					dbInstance.commitAndCloseSession();
				}
			}
		}
	}
	
	public List<RepositoryEntry> getRepositoryEntriesToUnpublish(Date date) {
		StringBuilder sb = new StringBuilder();
		sb.append("select v from repositoryentry as v ")
		  .append(" inner join fetch v.olatResource as ores")
		  .append(" inner join fetch v.statistics as statistics")
		  .append(" inner join fetch v.lifecycle as lifecycle")
		  .append(" where lifecycle.validTo<:now and not(v.statusCode=").append(RepositoryEntryStatus.REPOSITORY_STATUS_UNPUBLISHED).append(")")
		  .append(" and v.access>=").append(RepositoryEntry.ACC_OWNERS);
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		CalendarUtils.getEndOfDay(cal);
		Date endOfDay = cal.getTime();
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RepositoryEntry.class)
				.setParameter("now", endOfDay)
				.getResultList();
	}
	
	private void delete() {
		String autoDelete = repositoryModule.getLifecycleAutoDelete();
		if(StringHelper.containsNonWhitespace(autoDelete)) {
			RepositoryEntryLifeCycleValue autoDeleteVal = RepositoryEntryLifeCycleValue.parse(autoDelete);
			Date markerDate = autoDeleteVal.limitDate(new Date());
			List<RepositoryEntry> entriesToDelete = getRepositoryEntriesToDelete(markerDate);
			for(RepositoryEntry entry:entriesToDelete) {
				try {
					boolean deleteManaged = RepositoryEntryManagedFlag.isManaged(entry, RepositoryEntryManagedFlag.delete);
					if(!deleteManaged) {
						log.audit("Automatic deleting (soft) course: " + entry.getDisplayname() + " [" + entry.getKey() + "]");
						repositoryService.deleteSoftly(entry, null, true);
						dbInstance.commit();
					}
				} catch (Exception e) {
					log.error("",  e);
					dbInstance.commitAndCloseSession();
				}
			}
		}
	}
	
	public List<RepositoryEntry> getRepositoryEntriesToDelete(Date date) {
		StringBuilder sb = new StringBuilder();
		sb.append("select v from repositoryentry as v ")
		  .append(" inner join fetch v.olatResource as ores")
		  .append(" inner join fetch v.statistics as statistics")
		  .append(" inner join fetch v.lifecycle as lifecycle")
		  .append(" where lifecycle.validTo<:now and v.access>=").append(RepositoryEntry.ACC_OWNERS);
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		CalendarUtils.getEndOfDay(cal);
		Date endOfDay = cal.getTime();
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RepositoryEntry.class)
				.setParameter("now", endOfDay)
				.getResultList();
	}
}