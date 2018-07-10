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

/**
 * 
 * Initial date: 10.07.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public enum QualityReminderType {
	
	INVITATION("reminder.template.invitation.subject", "reminder.template.invtation.body"),
	REMINDER1("reminder.template.reminder1.subject", "reminder.template.reminder1.body"),
	REMINDER2("reminder.template.reminder2.subject", "reminder.template.reminder2.body");
	
	private final String subjectI18nKey;
	private final String bodyI18nKey;
	
	private QualityReminderType(String subjectI18nKey, String bodyI18nKey) {
		this.subjectI18nKey = subjectI18nKey;
		this.bodyI18nKey = bodyI18nKey;
	}

	public String getSubjectI18nKey() {
		return subjectI18nKey;
	}

	public String getBodyI18nKey() {
		return bodyI18nKey;
	}
	
}
