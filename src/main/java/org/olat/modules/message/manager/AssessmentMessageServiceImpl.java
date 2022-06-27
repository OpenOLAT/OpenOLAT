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

import java.util.Date;
import java.util.List;

import org.olat.basesecurity.IdentityRef;
import org.olat.commons.calendar.CalendarUtils;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.DateUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.Coordinator;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.message.AssessmentMessage;
import org.olat.modules.message.AssessmentMessagePublicationEnum;
import org.olat.modules.message.AssessmentMessageService;
import org.olat.modules.message.model.AssessmentMessageInfos;
import org.olat.modules.message.model.AssessmentMessageLogImpl;
import org.olat.modules.message.model.AssessmentMessageWithReadFlag;
import org.olat.modules.message.ui.AssessmentMessageEvent;
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
public class AssessmentMessageServiceImpl implements AssessmentMessageService {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private Coordinator coordinator;
	@Autowired
	private AssessmentMessageDAO assessmentMessageDao;
	@Autowired
	private AssessmentMessageLogDAO assessmentMessageLogDao;

	@Override
	public AssessmentMessage createAndPersistMessage(String text, Date publicationDate, Date expirationDate,
			AssessmentMessagePublicationEnum publication, RepositoryEntry entry, String resSubPath, Identity author) {
		AssessmentMessage message = assessmentMessageDao.createMessage(text, publicationDate, expirationDate, publication, entry, resSubPath, author);
		dbInstance.commit();
		
		if(publication == AssessmentMessagePublicationEnum.asap) {
			OLATResourceable messageOres = getEventResourceable(entry, resSubPath);
			AssessmentMessageEvent event = new AssessmentMessageEvent(AssessmentMessageEvent.PUBLISHED,
					message.getKey(), entry.getKey(), resSubPath, author.getKey());
			coordinator.getEventBus().fireEventToListenersOf(event, messageOres);
		}
		
		return message;
	}
	
	@Override
	public AssessmentMessage updateMessage(AssessmentMessage message, Identity actor) {
		message = assessmentMessageDao.updateMessage(message);
		dbInstance.commit();
		
		Date now = new Date();
		if(message.getPublicationDate().before(now) && message.getExpirationDate().after(now)) {
			OLATResourceable messageOres = getEventResourceable(message.getEntry(), message.getResSubPath());
			AssessmentMessageEvent event = new AssessmentMessageEvent(AssessmentMessageEvent.PUBLISHED,
					message.getKey(), message.getEntry().getKey(), message.getResSubPath(), actor.getKey());
			coordinator.getEventBus().fireEventToListenersOf(event, messageOres);
		}
		return message;
	}

	public void publishMessage() {
		Date now = new Date();
		List<AssessmentMessage> messages = assessmentMessageDao.getMessages(now);
		for(AssessmentMessage message:messages) {
			OLATResourceable messageOres = getEventResourceable(message.getEntry(), message.getResSubPath());
			AssessmentMessageEvent event = new AssessmentMessageEvent(AssessmentMessageEvent.PUBLISHED,
					message.getKey(), message.getEntry().getKey(), message.getResSubPath(), null);
			coordinator.getEventBus().fireEventToListenersOf(event, messageOres);
		}
		
		// Send expiration events during five minutes after the effective expiration
		Date expirationWindowStart = DateUtils.addMinutes(now, -5);
		List<AssessmentMessage> expiredMessages = assessmentMessageDao.getExpiredMessages(expirationWindowStart, now);
		for(AssessmentMessage message:expiredMessages) {
			OLATResourceable messageOres = getEventResourceable(message.getEntry(), message.getResSubPath());
			AssessmentMessageEvent event = new AssessmentMessageEvent(AssessmentMessageEvent.EXPIRED,
					message.getKey(), message.getEntry().getKey(), message.getResSubPath(), null);
			coordinator.getEventBus().fireEventToListenersOf(event, messageOres);
		}
	}
	
	@Override
	public AssessmentMessage getAssessmentMessage(Long messageKey) {
		return assessmentMessageDao.loadByKey(messageKey);
	}

	@Override
	public void withdrawMessage(AssessmentMessage message) {
		if(message == null || message.getKey() == null) return;
		
		AssessmentMessage reloadedMessage = assessmentMessageDao.loadByKey(message.getKey());
		if(reloadedMessage != null) {
			reloadedMessage.setExpirationDate(CalendarUtils.removeSeconds(new Date()));
			dbInstance.commit();
			
			OLATResourceable messageOres = getEventResourceable(reloadedMessage.getEntry(), reloadedMessage.getResSubPath());
			AssessmentMessageEvent event = new AssessmentMessageEvent(AssessmentMessageEvent.EXPIRED,
					message.getKey(), message.getEntry().getKey(), message.getResSubPath(), null);
			coordinator.getEventBus().fireEventToListenersOf(event, messageOres);
		}
		
	}

	@Override
	public void deleteMessage(AssessmentMessage message) {
		if(message == null || message.getKey() == null) return;
		
		AssessmentMessage reloadedMessage = assessmentMessageDao.loadByKey(message.getKey());
		if(reloadedMessage != null) {
			assessmentMessageLogDao.deleteLog(reloadedMessage);
			assessmentMessageDao.deleteMessage(reloadedMessage);
			dbInstance.commit();
			
			OLATResourceable messageOres = getEventResourceable(reloadedMessage.getEntry(), reloadedMessage.getResSubPath());
			AssessmentMessageEvent event = new AssessmentMessageEvent(AssessmentMessageEvent.DELETED,
					message.getKey(), message.getEntry().getKey(), message.getResSubPath(), null);
			coordinator.getEventBus().fireEventToListenersOf(event, messageOres);
		}
	}

	@Override
	public List<AssessmentMessageInfos> getMessagesInfos(RepositoryEntry entry, String resSubPath) {
		return assessmentMessageDao.getMessagesInfos(entry, resSubPath);
	}

	@Override
	public AssessmentMessageInfos getMessageInfos(Long messageKey) {
		return assessmentMessageDao.getMessageInfos(messageKey);
	}

	@Override
	public List<AssessmentMessageWithReadFlag> getMessagesFor(RepositoryEntry entry, String resSubPath, IdentityRef identity, Date date) {
		return assessmentMessageDao.getMessages(entry, resSubPath, identity, date);
	}

	@Override
	public boolean hasMessagesFor(RepositoryEntry entry, String resSubPath, Date date) {
		return assessmentMessageDao.hasMessages(entry, resSubPath, date);
	}

	@Override
	public void markAsRead(AssessmentMessage message, Identity identity) {
		AssessmentMessageLogImpl log = assessmentMessageLogDao.loadBy(message, identity);
		if(log == null) {
			assessmentMessageLogDao.createMessage(message, identity, true);
			dbInstance.commit();
		} else if(!log.isRead()) {
			log.setRead(true);
			assessmentMessageLogDao.updateMessage(log);
			dbInstance.commit();
		}
		
		// Send always the event
		OLATResourceable messageOres = getEventResourceable(message.getEntry(), message.getResSubPath());
		AssessmentMessageEvent event = new AssessmentMessageEvent(AssessmentMessageEvent.PUBLISHED,
				message.getKey(), message.getEntry().getKey(), message.getResSubPath(), identity.getKey());
		coordinator.getEventBus().fireEventToListenersOf(event, messageOres);
	}

	@Override
	public OLATResourceable getEventResourceable(RepositoryEntry entry, String resSubPath) {
		if(StringHelper.containsNonWhitespace(resSubPath)) {
			return OresHelper.createOLATResourceableInstance("Assessement-message-".concat(resSubPath), entry.getKey());
		}
		return OresHelper.createOLATResourceableInstance("Assessement-message", entry.getKey());
	}
}
