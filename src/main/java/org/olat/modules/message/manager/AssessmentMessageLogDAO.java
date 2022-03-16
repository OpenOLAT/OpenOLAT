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
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.message.AssessmentMessage;
import org.olat.modules.message.model.AssessmentMessageLogImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 14 mars 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class AssessmentMessageLogDAO {
	
	@Autowired
	private DB dbInstance;
	
	public AssessmentMessageLogImpl createMessage(AssessmentMessage message, Identity identity, boolean read) {
		AssessmentMessageLogImpl messageLog = new AssessmentMessageLogImpl();
		messageLog.setCreationDate(new Date());
		messageLog.setLastModified(message.getCreationDate());
		messageLog.setIdentity(identity);
		messageLog.setMessage(message);
		messageLog.setRead(read);
		dbInstance.getCurrentEntityManager().persist(messageLog);
		return messageLog;
	}
	
	public AssessmentMessageLogImpl updateMessage(AssessmentMessageLogImpl messageLog) {
		messageLog.setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(messageLog);
	}
	
	public AssessmentMessageLogImpl loadBy(AssessmentMessage message, IdentityRef identity) {
		String query = "select log from assessmentmessagelog as log where log.message.key=:messageKey and log.identity.key=:identityKey";
		List<AssessmentMessageLogImpl> messageLog = dbInstance.getCurrentEntityManager().createQuery(query, AssessmentMessageLogImpl.class)
			.setParameter("messageKey", message.getKey())
			.setParameter("identityKey", identity.getKey())
			.getResultList();
		return messageLog != null && !messageLog.isEmpty() ? messageLog.get(0) : null;
	}
	
	public void deleteLog(AssessmentMessage message) {
		String query = "delete from assessmentmessagelog as log where log.message.key=:messageKey";
		dbInstance.getCurrentEntityManager().createQuery(query)
			.setParameter("messageKey", message.getKey())
			.executeUpdate();
	}

}
