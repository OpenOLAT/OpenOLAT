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
package org.olat.modules.message;

import java.util.Date;
import java.util.List;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.modules.message.model.AssessmentMessageInfos;
import org.olat.modules.message.model.AssessmentMessageWithReadFlag;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 14 mars 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface AssessmentMessageService {
	
	
	public AssessmentMessage createAndPersistMessage(String text, Date publicationDate, Date expirationDate,
			AssessmentMessagePublicationEnum publication, RepositoryEntry entry, String resSubPath, Identity author);
	
	public AssessmentMessage updateMessage(AssessmentMessage message, Identity actor);
	
	public AssessmentMessage getAssessmentMessage(Long messageKey);
	
	public List<AssessmentMessageInfos> getMessagesInfos(RepositoryEntry entry, String resSubPath);
	
	public AssessmentMessageInfos getMessageInfos(Long messageKey);
	
	/**
	 * List the messages to the specified user and and valid at the specified date.
	 * 
	 * @param entry The repository entry (mandatory)
	 * @param resSubPath The resource sub-path (course element identifier) (mandatory)
	 * @param identity The identity (mandatory)
	 * @param date The date of validity (mandatory)
	 * @return A list of message with the information read/not read
	 */
	public List<AssessmentMessageWithReadFlag> getMessagesFor(RepositoryEntry entry, String resSubPath, IdentityRef identity, Date date);
	
	public boolean hasMessagesFor(RepositoryEntry entry, String resSubPath, Date date);
	
	public void markAsRead(AssessmentMessage message, Identity identity);
	
	public void deleteMessage(AssessmentMessage message);
	
	public void withdrawMessage(AssessmentMessage message);
	
	/**
	 * To listen to new messages.
	 * 
	 * @param entry The entry
	 * @param resSubPath The resource sub-path
	 * @return A resource to listen to
	 */
	public OLATResourceable getEventResourceable(RepositoryEntry entry, String resSubPath);

}
