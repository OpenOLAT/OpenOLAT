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
package org.olat.modules.message.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.TemporalType;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.olat.modules.message.AssessmentMessage;
import org.olat.modules.message.AssessmentMessagePublicationEnum;
import org.olat.modules.message.model.AssessmentMessageImpl;
import org.olat.modules.message.model.AssessmentMessageInfos;
import org.olat.modules.message.model.AssessmentMessageLogImpl;
import org.olat.modules.message.model.AssessmentMessageWithReadFlag;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 14 mars 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class AssessmentMessageDAO {
	
	@Autowired
	private DB dbInstance;
	
	public AssessmentMessage createMessage(String text, Date publicationDate, Date expirationDate,
			AssessmentMessagePublicationEnum publication, RepositoryEntry entry, String resSubPath, Identity author) {
		AssessmentMessageImpl message = new AssessmentMessageImpl();
		message.setCreationDate(new Date());
		message.setLastModified(message.getCreationDate());
		message.setMessage(text);
		message.setPublicationDate(publicationDate);
		message.setExpirationDate(expirationDate);
		message.setMessageSent(false);
		message.setEntry(entry);
		message.setResSubPath(resSubPath);
		message.setAuthor(author);
		message.setPublicationType(publication);
		dbInstance.getCurrentEntityManager().persist(message);
		return message;
	}
	
	public AssessmentMessage updateMessage(AssessmentMessage message) {
		message.setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(message);
	}
	
	public AssessmentMessage loadByKey(Long messageKey) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select msg from assessmentmessage as msg")
		  .append(" left join fetch msg.author as authorIdent")
		  .append(" left join fetch authorIdent.user as authorUser")
		  .append(" inner join fetch msg.entry as v")
		  .append(" inner join fetch v.olatResource as res")
		  .where().append(" msg.key=:messageKey");
		
		List<AssessmentMessage> messages = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), AssessmentMessage.class)
				.setParameter("messageKey", messageKey)
				.getResultList();
		return messages != null && !messages.isEmpty() ? messages.get(0) : null;
	}
	
	public List<AssessmentMessage> getMessages(Date date) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select msg from assessmentmessage as msg")
		  .append(" inner join fetch msg.entry as v")
		  .where().append(" msg.publicationDate<=:date and msg.expirationDate>=:date");
		
		return dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), AssessmentMessage.class)
			.setParameter("date", date, TemporalType.TIMESTAMP)
			.getResultList();
	}
	
	public List<AssessmentMessage> getExpiredMessages(Date startExpiration, Date endExpiration) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select msg from assessmentmessage as msg")
		  .append(" inner join fetch msg.entry as v")
		  .where().append(" msg.expirationDate>=:startExpiration and msg.expirationDate<=:endExpiration");
		
		return dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), AssessmentMessage.class)
			.setParameter("startExpiration", startExpiration, TemporalType.TIMESTAMP)
			.setParameter("endExpiration", endExpiration, TemporalType.TIMESTAMP)
			.getResultList();
	}
	
	public boolean hasMessages(RepositoryEntry entry, String resSubPath, Date date) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select msg.key from assessmentmessage as msg")
		  .where().append(" msg.entry.key=:entryKey and msg.resSubPath=:resSubPath")
		  .and().append(" msg.publicationDate<=:date and msg.expirationDate>=:date");
		
		List<Long> keys = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Long.class)
			.setParameter("entryKey", entry.getKey())
			.setParameter("resSubPath", resSubPath)
			.setParameter("date", date, TemporalType.TIMESTAMP)
			.setFirstResult(0)
			.setMaxResults(1)
			.getResultList();
		return keys != null && !keys.isEmpty() && keys.get(0) != null && keys.get(0).longValue() > 0;
	}
	
	public List<AssessmentMessageWithReadFlag> getMessages(RepositoryEntry entry, String resSubPath, IdentityRef identity, Date date) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select msg, log from assessmentmessage as msg")
		  .append(" left join assessmentmessagelog as log on (log.message.key=msg.key and log.identity.key=:identityKey)")
		  .where().append(" msg.entry.key=:entryKey and msg.resSubPath=:resSubPath")
		  .and().append(" msg.publicationDate<=:date and msg.expirationDate>=:date");
		
		List<Object[]> rawObjects = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Object[].class)
			.setParameter("entryKey", entry.getKey())
			.setParameter("resSubPath", resSubPath)
			.setParameter("identityKey", identity.getKey())
			.setParameter("date", date, TemporalType.TIMESTAMP)
			.getResultList();
		
		List<AssessmentMessageWithReadFlag> infos = new ArrayList<>(rawObjects.size());
		for(Object[] rawObject:rawObjects) {
			AssessmentMessage message = (AssessmentMessage)rawObject[0];
			AssessmentMessageLogImpl messageLog = (AssessmentMessageLogImpl)rawObject[1];
			boolean read = messageLog != null && messageLog.isRead();
			infos.add(new AssessmentMessageWithReadFlag(message, read));
		}
		return infos;
	}
	
	public AssessmentMessageInfos getMessageInfos(Long messageKey) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select msg,")
		  .append(" (select count(read.key) from assessmentmessagelog as read where")
		  .append("   read.message.key=msg.key and read.read=true")
		  .append(" ) as numOfRead")
		  .append(" from assessmentmessage as msg")
		  .append(" left join fetch msg.author as authorIdent")
		  .append(" left join fetch authorIdent.user as authorUser")
		  .where().append(" msg.key=:messageKey");
		
		List<Object[]> rawObjects = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Object[].class)
			.setParameter("messageKey", messageKey)
			.setFirstResult(0)
			.setMaxResults(1)
			.getResultList();
		
		if(rawObjects.size() == 1) {
			Object[] rawObject = rawObjects.get(0);
			AssessmentMessage message = (AssessmentMessage)rawObject[0];
			long numOfRead = PersistenceHelper.extractPrimitiveLong(rawObject, 1);
			return new AssessmentMessageInfos(message, numOfRead);
		}
		return null;
	}
	
	public List<AssessmentMessageInfos> getMessagesInfos(RepositoryEntry entry, String resSubPath) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select msg,")
		  .append(" (select count(read.key) from assessmentmessagelog as read where")
		  .append("   read.message.key=msg.key and read.read=true")
		  .append(" ) as numOfRead")
		  .append(" from assessmentmessage as msg")
		  .append(" left join fetch msg.author as authorIdent")
		  .append(" left join fetch authorIdent.user as authorUser")
		  .where().append(" msg.entry.key=:entryKey and msg.resSubPath=:resSubPath");
		
		List<Object[]> rawObjects = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Object[].class)
			.setParameter("entryKey", entry.getKey())
			.setParameter("resSubPath", resSubPath)
			.getResultList();
		
		List<AssessmentMessageInfos> infos = new ArrayList<>(rawObjects.size());
		for(Object[] rawObject:rawObjects) {
			AssessmentMessage message = (AssessmentMessage)rawObject[0];
			long numOfRead = PersistenceHelper.extractPrimitiveLong(rawObject, 1);
			infos.add(new AssessmentMessageInfos(message, numOfRead));
		}
		return infos;
	}
	
	public void deleteMessage(AssessmentMessage message) {
		dbInstance.getCurrentEntityManager().remove(message);
	}

}
