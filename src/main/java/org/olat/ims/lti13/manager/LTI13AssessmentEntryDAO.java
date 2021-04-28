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

import javax.persistence.TypedQuery;

import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.AuthenticationImpl;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.ims.lti13.LTI13Service;
import org.olat.ims.lti13.LTI13ToolDeployment;
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
	
	/**
	 * 
	 * @param deployment
	 * @return
	 */
	public List<AssessmentEntryWithUserId> getAssessmentEntriesWithUserIds(LTI13ToolDeployment deployment, int firstResult, int maxResults) {
		QueryBuilder sb = new QueryBuilder(1024);
		sb.append("select data, auth from assessmententry data")
		  .append(" inner join fetch data.identity as ident")
		  .append(" inner join ").append(AuthenticationImpl.class.getName()).append(" as auth on (ident.key=auth.identity.key)")
		  .append(" where data.repositoryEntry.key=:repositoryEntryKey and data.subIdent=:subIdent")
		  .append(" and auth.provider=:provider and auth.issuer=:issuer")
		  .append(" order by data.key asc");

		TypedQuery<Object[]> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class);
		if(maxResults > 0) {
			if(firstResult < 0) {
				firstResult = 0;
			}
			query.setFirstResult(firstResult);
			query.setMaxResults(maxResults);
		}
		List<Object[]> rawObjects = query
				.setParameter("provider", LTI13Service.LTI_PROVIDER)
				.setParameter("issuer", deployment.getTool().getToolDomain())
				.setParameter("repositoryEntryKey", deployment.getEntry().getKey())
				.setParameter("subIdent", deployment.getSubIdent())
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
