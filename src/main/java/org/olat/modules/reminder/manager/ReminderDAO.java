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
package org.olat.modules.reminder.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.olat.modules.reminder.Reminder;
import org.olat.modules.reminder.SentReminder;
import org.olat.modules.reminder.model.ReminderImpl;
import org.olat.modules.reminder.model.ReminderInfos;
import org.olat.modules.reminder.model.SentReminderImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 01.04.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class ReminderDAO {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private UserManager userManager;

	public Reminder createReminder(RepositoryEntry entry, Identity creator) {
		ReminderImpl reminder = new ReminderImpl();
		Date now = new Date();
		reminder.setCreationDate(now);
		reminder.setLastModified(now);
		reminder.setEntry(entry);
		reminder.setCreator(creator);
		return reminder;
	}

	public Reminder save(Reminder reminder) {
		Reminder mergedReminder;
		if(reminder.getKey() != null) {
			reminder.setLastModified(new Date());
			mergedReminder = dbInstance.getCurrentEntityManager().merge(reminder);
		} else {
			dbInstance.getCurrentEntityManager().persist(reminder);
			mergedReminder = reminder;
		}
		return mergedReminder;
	}

	public Reminder loadByKey(Long key) {
		List<Reminder> reminders = dbInstance.getCurrentEntityManager()
				.createNamedQuery("loadReminderByKey", Reminder.class)
				.setParameter("reminderKey", key)
				.getResultList();
		return reminders.isEmpty() ? null : reminders.get(0);
	}

	public Reminder duplicate(Reminder toCopy, Identity creator) {
		ReminderImpl reminder = new ReminderImpl();
		Date now = new Date();
		reminder.setCreationDate(now);
		reminder.setLastModified(now);
		if(toCopy.getEntry() != null) {
			RepositoryEntry entryRef = dbInstance.getCurrentEntityManager()
					.getReference(RepositoryEntry.class, toCopy.getEntry().getKey());
			reminder.setEntry(entryRef);
		}
		reminder.setCreator(creator);
		reminder.setDescription(toCopy.getDescription() + " (Copy)");
		reminder.setConfiguration(toCopy.getConfiguration());
		reminder.setEmailSubject(toCopy.getEmailSubject());
		reminder.setEmailBody(toCopy.getEmailBody());
		reminder.setEmailCopy(new HashSet<>(toCopy.getEmailCopy()));
		reminder.setCustomEmailCopy(toCopy.getCustomEmailCopy());
		dbInstance.getCurrentEntityManager().persist(reminder);
		return reminder;
	}
	
	public Reminder duplicate(Reminder toCopy, RepositoryEntry newEntry, Identity creator) {
		ReminderImpl reminder = new ReminderImpl();
		Date now = new Date();
		reminder.setCreationDate(now);
		reminder.setLastModified(now);
		if(newEntry != null) {
			reminder.setEntry(newEntry);
		}
		reminder.setCreator(creator);
		reminder.setDescription(toCopy.getDescription() + " (Copy)");
		reminder.setConfiguration(toCopy.getConfiguration());
		reminder.setEmailSubject(toCopy.getEmailSubject());
		reminder.setEmailBody(toCopy.getEmailBody());
		reminder.setEmailCopy(new HashSet<>(toCopy.getEmailCopy()));
		reminder.setCustomEmailCopy(toCopy.getCustomEmailCopy());
		dbInstance.getCurrentEntityManager().persist(reminder);
		return reminder;
	}

	public int delete(Reminder reminder) {
		ReminderImpl ref = dbInstance.getCurrentEntityManager()
				.getReference(ReminderImpl.class, reminder.getKey());
		String del = "delete from sentreminder sent where sent.reminder.key=:reminderKey";
		int numOfDeletedRows = dbInstance.getCurrentEntityManager()
				.createQuery(del)
				.setParameter("reminderKey", reminder.getKey())
				.executeUpdate();
		dbInstance.getCurrentEntityManager().remove(ref);
		numOfDeletedRows++;
		return numOfDeletedRows;
	}

	public int delete(RepositoryEntry entry) {
		int rowsDeleted = 0;
		List<Reminder> reminders = getReminders(entry);
		for(Reminder reminder:reminders) {
			rowsDeleted += delete(reminder);
		}
		return rowsDeleted;
	}
	
	/**
	 * Get all reminders of active repository entries (status must be
	 * open and not "softly" deleted).
	 * 
	 * @param startDate
	 * @return A list of reminders
	 */
	public List<Reminder> getReminders(Date startDate) {
		QueryBuilder sb = new QueryBuilder(256);
		sb.append("select rem from reminder rem")
		  .append(" inner join rem.entry entry")
		  .append(" where (rem.startDate is null or rem.startDate<=:startDate)")
		  .append(" and entry.status ").in(RepositoryEntryStatusEnum.preparationToPublished());

		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Reminder.class)
				.setParameter("startDate", startDate)
				.getResultList();
	}

	/**
	 * Get all reminders without restrictions.
	 * 
	 * @param entry
	 * @return A list of remidners
	 */
	public List<Reminder> getReminders(RepositoryEntryRef entry) {
		String q = "select rem from reminder rem inner join rem.entry entry where entry.key=:entryKey";
		return dbInstance.getCurrentEntityManager()
				.createQuery(q, Reminder.class)
				.setParameter("entryKey", entry.getKey())
				.getResultList();
	}
	
	public List<ReminderInfos> getReminderInfos(RepositoryEntryRef entry) {
		StringBuilder sb = new StringBuilder();
		sb.append("select rem.key, rem.description, rem.configuration, rem.sendTime, rem.creationDate, rem.lastModified, rem.creator.key,")
		  .append(" (select count(sentrem.key) from sentreminder sentrem")
		  .append("    where sentrem.reminder.key=rem.key")
		  .append(" ) as numOfRemindersSent ")
		  .append(" from reminder rem")
		  .append(" where rem.entry.key=:entryKey");

		List<Object[]> results = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("entryKey", entry.getKey())
				.getResultList();
		
		List<ReminderInfos> infos = new ArrayList<>(results.size());
		for(Object[] result:results) {
			Long key = (Long)result[0];
			String desc = (String)result[1];
			String configuration = (String)result[2];
			String sendTime = (String)result[3];
			Date creationDate = (Date)result[4];
			Date lastModified = (Date)result[5];
			Long creatorKey = (Long)result[6];
			String creator = userManager.getUserDisplayName(creatorKey);
			int numOfRemindersSent = 0;
			if(result[7] != null) {
				numOfRemindersSent = ((Number)result[7]).intValue();
			}

			infos.add(new ReminderInfos(key, creationDate, lastModified, desc, configuration, sendTime, creatorKey,
					creator, numOfRemindersSent));
		}
		return infos;
	}

	public SentReminderImpl markAsSend(Reminder reminder, Identity identity, String status) {
		SentReminderImpl send = new SentReminderImpl();
		send.setCreationDate(new Date());
		send.setStatus(status);
		send.setReminder(reminder);
		send.setIdentity(identity);
		dbInstance.getCurrentEntityManager().persist(send);
		return send;
	}
	
	public List<SentReminder> getSendReminders(Reminder reminder) {
		String q = "select sent from sentreminder sent inner join fetch sent.identity ident inner join fetch ident.user as identUser where sent.reminder.key=:reminderKey";
		return dbInstance.getCurrentEntityManager()
				.createQuery(q, SentReminder.class)
				.setParameter("reminderKey", reminder.getKey())
				.getResultList();
	}
	
	public List<SentReminder> getSendReminders(RepositoryEntryRef entry) {
		String q = "select sent from sentreminder sent inner join fetch sent.reminder rem inner join fetch sent.identity ident inner join fetch ident.user as identUser where rem.entry.key=:entryKey";
		return dbInstance.getCurrentEntityManager()
				.createQuery(q, SentReminder.class)
				.setParameter("entryKey", entry.getKey())
				.getResultList();
	}
	
	public List<Long> getReminderRecipientKeys(Reminder reminder) {
		String q = "select sent.identity.key from sentreminder sent where sent.reminder.key=:reminderKey";
		return dbInstance.getCurrentEntityManager()
				.createQuery(q, Long.class)
				.setParameter("reminderKey", reminder.getKey())
				.getResultList();
	}
	
	/**
	 * The query is limited to the default group of the repository entry
	 * and the business groups 
	 * @param entry
	 * @param identities
	 * @return
	 */
	public Map<Long,Date> getCourseEnrollmentDates(RepositoryEntryRef entry, List<Identity> identities) {
		if(identities == null || identities.isEmpty()) {
			return new HashMap<>();
		}

		List<Long> identityKeys = PersistenceHelper.toKeys(identities);

		StringBuilder sb = new StringBuilder(512);
		sb.append("select membership.identity.key, membership.creationDate from repositoryentry as v ")
		  .append(" inner join v.groups as relGroup")
		  .append(" inner join relGroup.group as baseGroup")
		  .append(" inner join baseGroup.members as membership")
		  .append(" left join businessgroup as businessGroup on (businessGroup.baseGroup.key=baseGroup.key)")
		  .append(" where v.key=:repoKey and (relGroup.defaultGroup=true or businessGroup.key is not null)");

		Set<Long> identityKeySet = null;
		if(identityKeys.size() < 100) {
			sb.append(" and membership.identity.key in (:identityKeys)");
		} else {
			identityKeySet = new HashSet<>(identityKeys);
		}

		TypedQuery<Object[]> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("repoKey", entry.getKey());
		if(identityKeys.size() < 100) {
			query.setParameter("identityKeys", identityKeys);
		}

		List<Object[]> infoList = query.getResultList();
		Map<Long,Date> dateMap = new HashMap<>();
		for(Object[] infos:infoList) {
			Long identityKey = (Long)infos[0];
			if(identityKeySet == null || identityKeySet.contains(identityKey)) {
				Date enrollmantDate = (Date)infos[1];
				dateMap.put(identityKey, enrollmantDate);
			}
		}
		return dateMap;
	}

}
