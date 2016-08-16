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
package org.olat.ims.qti21.manager;

import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.ims.qti21.AssessmentItemSession;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.model.ParentPartItemRefs;
import org.olat.ims.qti21.model.jpa.AssessmentItemSessionImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 02.02.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class AssessmentItemSessionDAO {
	
	@Autowired
	private DB dbInstance;
	
	public AssessmentItemSession createAndPersistAssessmentItemSession(AssessmentTestSession assessmentTestSession, ParentPartItemRefs parentParts, String assessmentItemIdentifier) {
		AssessmentItemSessionImpl itemSession = new AssessmentItemSessionImpl();
		Date now = new Date();
		itemSession.setCreationDate(now);
		itemSession.setLastModified(now);
		itemSession.setAssessmentItemIdentifier(assessmentItemIdentifier);
		itemSession.setAssessmentTestSession(assessmentTestSession);
		if(parentParts != null) {
			itemSession.setSectionIdentifier(parentParts.getSectionIdentifier());
			itemSession.setTestPartIdentifier(parentParts.getTestPartIdentifier());
		}
		dbInstance.getCurrentEntityManager().persist(itemSession);
		return itemSession;
	}
	
	public AssessmentItemSession getAssessmentItemSession(AssessmentTestSession assessmentTestSession, String assessmentItemIdentifier) {
		StringBuilder sb = new StringBuilder();
		sb.append("select itemSession from qtiassessmentitemsession itemSession")
		  .append(" where itemSession.assessmentItemIdentifier=:assessmentItemIdentifier")
		  .append(" and itemSession.assessmentTestSession.key=:assessmentTestSessionKey");
		
		List<AssessmentItemSession> itemSessions = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), AssessmentItemSession.class)
			.setParameter("assessmentItemIdentifier", assessmentItemIdentifier)
			.setParameter("assessmentTestSessionKey", assessmentTestSession.getKey())
			.getResultList();
		return itemSessions == null || itemSessions.isEmpty() ? null : itemSessions.get(0);
	}
	
	public List<AssessmentItemSession> getAssessmentItemSessions(AssessmentTestSession assessmentTestSession) {
		StringBuilder sb = new StringBuilder();
		sb.append("select itemSession from qtiassessmentitemsession itemSession")
		  .append(" where itemSession.assessmentTestSession.key=:assessmentTestSessionKey");
		
		return dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), AssessmentItemSession.class)
			.setParameter("assessmentTestSessionKey", assessmentTestSession.getKey())
			.getResultList();
	}
	
	public AssessmentItemSession merge(AssessmentItemSession itemSession) {
		return dbInstance.getCurrentEntityManager().merge(itemSession);
	}
}
