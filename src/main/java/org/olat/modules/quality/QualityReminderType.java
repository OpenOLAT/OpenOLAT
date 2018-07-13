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
package org.olat.modules.quality;

import org.olat.modules.forms.EvaluationFormParticipationStatus;

/**
 * 
 * Initial date: 10.07.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public enum QualityReminderType {
	
	INVITATION("reminder.invitation.subject", "reminder.invitation.body", null),
	REMINDER1("reminder.reminder1.subject", "reminder.reminder1.body", EvaluationFormParticipationStatus.prepared),
	REMINDER2("reminder.reminder2.subject", "reminder.reminder2.body", EvaluationFormParticipationStatus.prepared);
	
	private final String subjectI18nKey;
	private final String bodyI18nKey;
	private final EvaluationFormParticipationStatus status;
	
	private QualityReminderType(String subjectI18nKey, String bodyI18nKey, EvaluationFormParticipationStatus status) {
		this.subjectI18nKey = subjectI18nKey;
		this.bodyI18nKey = bodyI18nKey;
		this.status = status;
	}

	public String getSubjectI18nKey() {
		return subjectI18nKey;
	}

	public String getBodyI18nKey() {
		return bodyI18nKey;
	}

	public EvaluationFormParticipationStatus getParticipationStatus() {
		return status;
	}
	
}
