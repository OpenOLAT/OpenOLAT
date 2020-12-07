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
package org.olat.ims.qti21.ui.assessment.model;

import java.util.List;
import java.util.Locale;

import org.olat.core.id.Identity;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: 28 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CorrectionIdentityRow extends CorrectionRow {
	
	private final String user;
	private final Identity identity;
	private final AssessmentTestSession candidateSession;
	private final String[] identityProps;

	private int numQuestions = 0;
	private int numAutoCorrectedQuestions = 0;
	private int numAutoCorrectedNotAnswered = 0;
	
	public CorrectionIdentityRow(String user, Identity identity, AssessmentTestSession candidateSession,
			List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		this.user = user;
		this.identity = identity;
		this.candidateSession = candidateSession;
		
		identityProps = new String[userPropertyHandlers.size()];
		for(int i=userPropertyHandlers.size(); i-->0; ) {
			identityProps[i] = userPropertyHandlers.get(i).getUserProperty(identity.getUser(), locale);
		}
	}
	
	public String getUser() {
		return user;
	}

	public Identity getIdentity() {
		return identity;
	}

	public AssessmentTestSession getCandidateSession() {
		return candidateSession;
	}
	
	public Long getIdentityKey() {
		return identity.getKey();
	}

	public String getIdentityName() {
		return identity.getName();
	}
	
	public String[] getIdentityProps() {
		return identityProps;
	}
	
	public String getIdentityProp(int index) {
		return identityProps[index];
	}
	
	public int getNumQuestions() {
		return numQuestions;
	}
	
	public int addQuestion() {
		numQuestions++;
		return numQuestions;
	}
	
	public int getNumAutoCorrectedQuestions() {
		return numAutoCorrectedQuestions;
	}
	
	public int addAutoCorrectedQuestion() {
		numAutoCorrectedQuestions++;
		return numAutoCorrectedQuestions;
	}
	
	public int getNumAutoCorrectedNotAnswered() {
		return numAutoCorrectedNotAnswered;
	}
	
	public int addAutoCorrectedNotAnswered() {
		numAutoCorrectedNotAnswered++;
		return numAutoCorrectedNotAnswered;
	}
}
