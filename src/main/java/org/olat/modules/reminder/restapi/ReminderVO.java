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
package org.olat.modules.reminder.restapi;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

import org.olat.core.util.StringHelper;
import org.olat.modules.reminder.Reminder;
import org.olat.modules.reminder.ReminderRule;
import org.olat.modules.reminder.manager.ReminderRulesXStream;
import org.olat.modules.reminder.model.ReminderRules;

/**
 * 
 * Initial date: 25 mars 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "reminderVO")
public class ReminderVO {
	
	private Long key;
	private Long repoEntryKey;
	
	private String description;
	private String emailSubject;
	private String emailBody;
	
	private List<ReminderRuleVO> rules = new ArrayList<>();
	
	public static ReminderVO valueOf(Reminder reminder, Long repositoryEntryKey) {
		ReminderVO vo = new ReminderVO();
		vo.setKey(reminder.getKey());
		vo.setRepoEntryKey(repositoryEntryKey);
		vo.setDescription(reminder.getDescription());
		vo.setEmailSubject(reminder.getEmailSubject());
		vo.setEmailBody(reminder.getEmailBody());
		
		String configuration = reminder.getConfiguration();
		if(StringHelper.containsNonWhitespace(configuration)) {
			ReminderRules reminderRules = ReminderRulesXStream.toRules(configuration);
			List<ReminderRule> rules = reminderRules.getRules();
			List<ReminderRuleVO> ruleVoes = new ArrayList<>();
			for(ReminderRule rule:rules) {
				ruleVoes.add(ReminderRuleVO.valueOf(rule));
			}
			vo.setRules(ruleVoes);
		}
		
		return vo;
	}
	
	public Long getKey() {
		return key;
	}
	
	public void setKey(Long key) {
		this.key = key;
	}
	
	public Long getRepoEntryKey() {
		return repoEntryKey;
	}
	
	public void setRepoEntryKey(Long repoEntryKey) {
		this.repoEntryKey = repoEntryKey;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getEmailSubject() {
		return emailSubject;
	}

	public void setEmailSubject(String emailSubject) {
		this.emailSubject = emailSubject;
	}

	public String getEmailBody() {
		return emailBody;
	}

	public void setEmailBody(String emailBody) {
		this.emailBody = emailBody;
	}

	public List<ReminderRuleVO> getRules() {
		return rules;
	}

	public void setRules(List<ReminderRuleVO> rules) {
		this.rules = rules;
	}
	
	

}
