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
package org.olat.modules.quality.manager;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.quality.QualityDataCollectionRef;
import org.olat.modules.quality.QualityReminder;
import org.olat.modules.quality.QualityReminderType;
import org.olat.modules.quality.model.QualityDataCollectionImpl;
import org.olat.modules.quality.model.QualityReminderImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 10.07.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class QualityReminderDAO {
	
	@Autowired
	private DB dbInstance;

	QualityReminder create(QualityDataCollectionRef dataCollectionRef, Date sendDate, QualityReminderType type) {
		QualityReminderImpl reminder = new QualityReminderImpl();
		reminder.setCreationDate(new Date());
		reminder.setLastModified(reminder.getCreationDate());
		reminder.setSendPlaned(sendDate);
		reminder.setType(type);
		QualityDataCollection dataCollection = dbInstance.getCurrentEntityManager()
				.getReference(QualityDataCollectionImpl.class, dataCollectionRef.getKey());
		reminder.setDataCollection(dataCollection);
		dbInstance.getCurrentEntityManager().persist(reminder);
		return reminder;
	}

	QualityReminder updateDatePlaned(QualityReminder reminder, Date datePlaned) {
		if (reminder instanceof QualityReminderImpl) {
			QualityReminderImpl reminderImpl = (QualityReminderImpl) reminder;
			reminderImpl.setSendPlaned(datePlaned);
			return save(reminderImpl);
		}
		return reminder;
	}
	
	QualityReminder updateDateDone(QualityReminder reminder, Date dateDone) {
		if (reminder instanceof QualityReminderImpl) {
			QualityReminderImpl reminderImpl = (QualityReminderImpl) reminder;
			reminderImpl.setSendDone(dateDone);
			return save(reminderImpl);
		}
		return reminder;
	}

	private QualityReminder save(QualityReminder reminder) {
		reminder.setLastModified(new Date());
		reminder = dbInstance.getCurrentEntityManager().merge(reminder);
		return reminder;
	}

	QualityReminder load(QualityDataCollectionRef dataCollectionRef, QualityReminderType type) {
		if (dataCollectionRef == null || dataCollectionRef.getKey() == null || type == null) return null;
		
		StringBuilder sb = new StringBuilder(256);
		sb.append("select reminder");
		sb.append("  from qualityreminder as reminder");
		sb.append(" where reminder.dataCollection.key = :dataCollectionKey");
		sb.append("   and reminder.type = :reminderType");
		
		List<QualityReminder> reminders = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), QualityReminder.class)
				.setParameter("dataCollectionKey", dataCollectionRef.getKey())
				.setParameter("reminderType", type)
				.getResultList();
		return reminders.isEmpty() ? null : reminders.get(0);
	}

	List<QualityReminder> loadPending(Date until) {
		if (until == null) return Collections.emptyList();
		
		StringBuilder sb = new StringBuilder(256);
		sb.append("select reminder");
		sb.append("  from qualityreminder as reminder");
		sb.append("  join fetch reminder.dataCollection as collection");
		sb.append(" where reminder.sendDone is null");
		sb.append("   and reminder.sendPlaned <= :until");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), QualityReminder.class)
				.setParameter("until", until)
				.getResultList();
	}


	public void delete(QualityReminder reminder) {
		if (reminder == null || reminder.getKey() == null) return;
		
		StringBuilder sb = new StringBuilder(256);
		sb.append("delete from qualityreminder as reminder");
		sb.append(" where reminder.key = :reminderKey");
		
		dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("reminderKey", reminder.getKey())
				.executeUpdate();
	}

}
