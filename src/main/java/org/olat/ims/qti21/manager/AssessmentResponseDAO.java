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

import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.persistence.TypedQuery;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.util.StringHelper;
import org.olat.ims.qti21.AssessmentItemSession;
import org.olat.ims.qti21.AssessmentResponse;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.model.ResponseLegality;
import org.olat.ims.qti21.model.jpa.AssessmentResponseImpl;
import org.olat.repository.RepositoryEntryRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uk.ac.ed.ph.jqtiplus.types.ResponseData.ResponseDataType;

/**
 * 
 * Initial date: 29.01.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class AssessmentResponseDAO {

	@Autowired
	private DB dbInstance;
	
	public AssessmentResponse createAssessmentResponse(AssessmentTestSession assessmentTestSession, AssessmentItemSession assessmentItemSession,
			String responseIdentifier, ResponseLegality legality, ResponseDataType type) {
		AssessmentResponseImpl response = new AssessmentResponseImpl();
		Date now = new Date();
		response.setCreationDate(now);
		response.setLastModified(now);
		response.setResponseDataType(type.name());
		response.setResponseLegality(legality.name());
		response.setAssessmentItemSession(assessmentItemSession);
		response.setAssessmentTestSession(assessmentTestSession);
		response.setResponseIdentifier(responseIdentifier);
		return response;
	}
	
	public List<AssessmentResponse> getResponses(AssessmentItemSession assessmentItemSession) {
		StringBuilder sb = new StringBuilder();
		sb.append("select response from qtiassessmentresponse response where")
		  .append(" response.assessmentItemSession.key=:assessmentItemSessionKey");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), AssessmentResponse.class)
				.setParameter("assessmentItemSessionKey", assessmentItemSession.getKey())
				.getResultList();
	}
	
	public void save(Collection<AssessmentResponse> responses) {
		if(responses != null && responses.isEmpty()) return;
		
		for(AssessmentResponse response:responses) {
			if(response.getKey() != null) {
				dbInstance.getCurrentEntityManager().merge(response);
			} else {
				dbInstance.getCurrentEntityManager().persist(response);
			}
		}
	}
	
	/**
	 * Check if there are some responses from a terminated session.
	 * 
	 * @param courseEntry
	 * @param subIdent
	 * @param testEntry
	 * @return
	 */
	public boolean hasResponses(RepositoryEntryRef courseEntry, String subIdent, RepositoryEntryRef testEntry,
			boolean participant, boolean users, boolean anonymUsers) {
		StringBuilder sb = new StringBuilder();
		sb.append("select response.key from qtiassessmentresponse response ")
		  .append(" inner join response.assessmentItemSession itemSession")
		  .append(" inner join itemSession.assessmentTestSession testSession")
		  .append(" where testSession.repositoryEntry.key=:repoEntryKey")
		  .append("  and testSession.testEntry.key=:testEntryKey")
		  .append("  and testSession.subIdent=:subIdent")
		  .append("  and testSession.finishTime is not null")
		  .append("  and (");
		if(users) {
			sb.append(" testSession.identity.key is not null");
		} else if(participant) {
			sb.append(" testSession.identity.key in (select membership.identity.key from  bgroupmember as membership, repoentrytogroup as rel")
			  .append("   where rel.entry.key=:repoEntryKey and rel.group.key=membership.group.key ")
			  .append("   and membership.role='").append(GroupRoles.participant.name()).append("'")
			  .append(" )");
		}
		if(anonymUsers) {
			if(participant || users) sb.append(" or ");
			sb.append(" testSession.anonymousIdentifier is not null");
		}
		sb.append("))");
		
		List<Long> responses = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("repoEntryKey", courseEntry.getKey())
				.setParameter("testEntryKey", testEntry.getKey())
				.setParameter("subIdent", subIdent)
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return responses.size() > 0 && responses.get(0) != null;
	}
	
	public List<AssessmentResponse> getResponse(RepositoryEntryRef courseEntry, String subIdent, RepositoryEntryRef testEntry,
			boolean participant, boolean users, boolean anonymUsers) {
		StringBuilder sb = new StringBuilder();
		sb.append("select response from qtiassessmentresponse response ")
		  .append(" inner join fetch response.assessmentItemSession itemSession")
		  .append(" inner join fetch itemSession.assessmentTestSession testSession")
		  .append(" inner join fetch testSession.assessmentEntry assessmentEntry")
		  .append(" left join assessmentEntry.identity as ident")
		  .append(" left join ident.user as usr")
		  .append(" where testSession.testEntry.key=:testEntryKey")
		  .append("  and testSession.finishTime is not null");
		if(courseEntry != null) {
			sb.append(" and testSession.repositoryEntry.key=:repoEntryKey");
		}
		if(StringHelper.containsNonWhitespace(subIdent)) {
			sb.append(" and testSession.subIdent=:subIdent");
		}
		
		sb.append(" and (");
		if(users) {
			sb.append(" testSession.identity.key is not null");
		} else if(participant) {
			sb.append(" testSession.identity.key in (select membership.identity.key from  bgroupmember as membership, repoentrytogroup as rel")
			  .append("   where rel.entry.key=:repoEntryKey and rel.group.key=membership.group.key ")
			  .append("   and membership.role='").append(GroupRoles.participant.name()).append("'")
			  .append(" )");
		}
		if(anonymUsers) {
			if(participant || users) sb.append(" or ");
			sb.append(" testSession.anonymousIdentifier is not null");
		}
		sb.append(")");

		//need to be anonymized
		sb.append(" order by usr.lastName, testSession.key, itemSession.key");
		
		TypedQuery<AssessmentResponse> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), AssessmentResponse.class)
				.setParameter("testEntryKey", testEntry.getKey());
		if(courseEntry != null) {
			query.setParameter("repoEntryKey", courseEntry.getKey());
		}
		if(StringHelper.containsNonWhitespace(subIdent)) {
			query.setParameter("subIdent", subIdent);
		}
		return query.getResultList();
	}
}
