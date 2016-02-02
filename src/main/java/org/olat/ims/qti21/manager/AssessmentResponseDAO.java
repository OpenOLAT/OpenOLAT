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

import org.olat.core.commons.persistence.DB;
import org.olat.ims.qti21.AssessmentItemSession;
import org.olat.ims.qti21.AssessmentResponse;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.model.ResponseLegality;
import org.olat.ims.qti21.model.jpa.AssessmentResponseImpl;
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

}
