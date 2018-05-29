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
package org.olat.modules.forms.ui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.EvaluationFormSessionRef;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 23.05.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class SessionInformationLegendNameGenerator implements LegendNameGenerator {
	
	private Map<Long, String> sessionKeyToName;

	private final List<? extends EvaluationFormSessionRef> sessionRefs;
	
	@Autowired
	private EvaluationFormManager evaluationFormManager;
	
	public SessionInformationLegendNameGenerator(List<? extends EvaluationFormSessionRef> sessionRefs) {
		this.sessionRefs = sessionRefs;
		CoreSpringFactory.autowireObject(this);
	}

	@Override
	public String getName(EvaluationFormSession session, Identity identity) {
		if (session == null) return null;
		
		if (sessionKeyToName == null) {
			initSessionKeyToName();
		}
		return sessionKeyToName.get(session.getKey());
	}

	private void initSessionKeyToName() {
		sessionKeyToName = new HashMap<>();
		List<EvaluationFormSession> sessions = evaluationFormManager.loadSessionsByKey(sessionRefs, 0, -1);
		for (EvaluationFormSession session: sessions) {
			String name = conacatName(session.getFirstname(), session.getLastname());
			sessionKeyToName.put(session.getKey(), name);
		}
	}

	private String conacatName(String firstname, String lastname) {
		boolean hasFirstname = StringHelper.containsNonWhitespace(firstname);
		boolean hasLastname = StringHelper.containsNoneOfCoDouSemi(lastname);
		if (hasFirstname && hasLastname) return new StringBuilder().append(firstname).append(" ").append(lastname).toString();
		if (hasFirstname) return firstname;
		if (hasLastname) return lastname;
		return null;
	}

}
