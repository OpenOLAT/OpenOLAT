/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.nodes.gta.model;

import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.forms.EvaluationFormSession;

/**
 * 
 * Initial date: 17 juin 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SessionParticipationStatistics {
	
	private final SessionStatistics statistics;
	
	private EvaluationFormSession session;
	private EvaluationFormParticipation participation;
	
	public SessionParticipationStatistics(EvaluationFormSession session, EvaluationFormParticipation participation, SessionStatistics statistics) {
		this.session = session;
		this.participation = participation;
		this.statistics = statistics;
	}

	public EvaluationFormSession getSession() {
		return session;
	}

	public void setSession(EvaluationFormSession session) {
		this.session = session;
	}

	public EvaluationFormParticipation getParticipation() {
		return participation;
	}

	public void setParticipation(EvaluationFormParticipation participation) {
		this.participation = participation;
	}

	public SessionStatistics statistics() {
		return statistics;
	}
}
