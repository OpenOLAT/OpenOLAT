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
package org.olat.ims.lti13.manager;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.TypedQuery;

import org.olat.basesecurity.Authentication;
import org.olat.core.commons.persistence.DB;
import org.olat.ims.lti13.LTI13Context;
import org.olat.ims.lti13.LTI13Service;
import org.olat.ims.lti13.LTI13Tool;
import org.olat.ims.lti13.model.AssessmentEntryWithUserId;
import org.olat.modules.assessment.AssessmentEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 28 avr. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class LTI13AssessmentEntryDAO {
	
	@Autowired
	private DB dbInstance;
	

	public List<AssessmentEntryWithUserId> getAssessmentEntriesWithUserIds(LTI13Context ltiContext, int firstResult, int maxResults) {
		String q = """
				select data, auth from assessmententry data
				inner join fetch data.identity as ident
				inner join authentication as auth on (ident.key=auth.identity.key)
				where data.repositoryEntry.key=:repositoryEntryKey and data.subIdent=:subIdent
				and auth.provider=:provider and auth.issuer=:issuer
				order by data.key asc""";

		TypedQuery<Object[]> query = dbInstance.getCurrentEntityManager()
				.createQuery(q, Object[].class);
		if(maxResults > 0) {
			if(firstResult < 0) {
				firstResult = 0;
			}
			query.setFirstResult(firstResult);
			query.setMaxResults(maxResults);
		}
		LTI13Tool tool = ltiContext.getDeployment().getTool();
		List<Object[]> rawObjects = query
				.setParameter("provider", LTI13Service.LTI_PROVIDER)
				.setParameter("issuer", tool.getToolDomain())
				.setParameter("repositoryEntryKey", ltiContext.getEntry().getKey())
				.setParameter("subIdent", ltiContext.getSubIdent())
				.getResultList();
		
		List<AssessmentEntryWithUserId> entries = new ArrayList<>();
		for(Object[] rawObject:rawObjects) {
			AssessmentEntry assessmentEntry = (AssessmentEntry)rawObject[0];
			Authentication authentication = (Authentication)rawObject[1];
			if(assessmentEntry != null && authentication != null) {
				entries.add(new AssessmentEntryWithUserId(assessmentEntry, authentication.getAuthusername()));
			}
		}
		return entries;
	}
}
