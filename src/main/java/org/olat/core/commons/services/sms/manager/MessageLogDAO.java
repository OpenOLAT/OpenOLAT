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
package org.olat.core.commons.services.sms.manager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.commons.services.sms.MessageLog;
import org.olat.core.commons.services.sms.model.MessageLogImpl;
import org.olat.core.commons.services.sms.model.MessageStatistics;
import org.olat.core.id.Identity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 3 févr. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class MessageLogDAO {
	
	@Autowired
	private DB dbInstance;
	
	/**
	 * Create a transient log entry.
	 * 
	 * @param recipient
	 * @return The log entry
	 */
	public MessageLog create(Identity recipient, String serviceId) {
		MessageLogImpl log = new MessageLogImpl();
		log.setCreationDate(new Date());
		log.setLastModified(log.getCreationDate());
		log.setMessageUuid(UUID.randomUUID().toString());
		log.setServiceId(serviceId);
		log.setRecipient(recipient);
		dbInstance.getCurrentEntityManager().persist(log);
		return log;
	}
	
	public MessageLog save(MessageLog log) {
		MessageLog mergedLog;
		if(log.getKey() == null) {
			dbInstance.getCurrentEntityManager().persist(log);
			mergedLog = log;
		} else {
			((MessageLogImpl)log).setLastModified(new Date());
			mergedLog = dbInstance.getCurrentEntityManager().merge(log);
		}
		return mergedLog;
	}
	
	public MessageLog loadMessageByKey(Long key) {
		StringBuilder sb = new StringBuilder();
		sb.append("select mlog from smsmessagelog mlog where mlog.key=:messageKey");
		
		List<MessageLog> mLogs = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), MessageLog.class)
				.setParameter("messageKey", key)
				.getResultList();
		return mLogs == null || mLogs.isEmpty() ? null : mLogs.get(0);
	}
	
	public List<MessageStatistics> getStatisticsPerMonth(String serviceId) {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(mlog.key) as numOfMessages, month(mlog.creationDate) as month, year(mlog.creationDate) as year")
		  .append(" from smsmessagelog mlog")
		  .append(" where mlog.serviceId=:serviceId")
		  .append(" group by month(mlog.creationDate), year(mlog.creationDate)");
		
		List<Object[]> objects = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("serviceId", serviceId)
				.getResultList();
		
		Calendar cal = Calendar.getInstance();
		List<MessageStatistics> stats = new ArrayList<>(objects.size());
		for(Object[] object:objects) {
			int pos = 0;
			Long numOfMessages = PersistenceHelper.extractLong(object, pos++);
			Long month = PersistenceHelper.extractLong(object, pos++);
			Long year = PersistenceHelper.extractLong(object, pos++);
			if(numOfMessages != null) {
				cal.set(Calendar.YEAR, year.intValue());
				cal.set(Calendar.MONTH, month.intValue() - 1);
				cal.set(Calendar.DAY_OF_MONTH, 1);
				cal.set(Calendar.HOUR_OF_DAY, 0);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 0);
				cal.set(Calendar.MILLISECOND, 0);
				
				Date date = cal.getTime();
				stats.add(new MessageStatistics(serviceId, date, numOfMessages));
			}
		}
		return stats;
	}
}