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
package org.olat.modules.selectus.ui.mail;

import org.olat.modules.selectus.model.ApplicationsFeedbackConfiguration;
import org.olat.modules.selectus.model.PositionMailTemplate;

/**
 * 
 * Initial date: 23 févr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PositionMailTemplateRow {
	
	private String id;
	private String name;
	private final Type type;
	private final boolean enabled;
	private final boolean customized;
	private final String recipient;
	private final boolean withLetter;
	private String letterName;

	private PositionMailTemplate mailTemplate;
	private ApplicationsFeedbackConfiguration feedbackConfiguration;

	public PositionMailTemplateRow(String id, String name, Type type, String recipient,
			boolean customized, boolean enabled, boolean withLetter) {
		this.id = id;
		this.name = name;
		this.type = type;
		this.enabled = enabled;
		this.recipient = recipient;
		this.customized = customized;
		this.withLetter = withLetter;
	}
	
	public PositionMailTemplateRow(PositionMailTemplate template, String recipient, String letterName) {
		id = template.getId();
		name = template.getName();
		this.mailTemplate = template;
		customized = true;
		type = Type.custom;
		enabled = true;
		this.recipient = recipient;
		this.withLetter = true;
		this.letterName = letterName;
	}
	
	public PositionMailTemplateRow(String name, ApplicationsFeedbackConfiguration feedbackConfiguration, String recipient,
			boolean customized, boolean enabled) {
		id = null;
		this.name = (name == null ? feedbackConfiguration.getConfigurationName() : name);
		this.feedbackConfiguration = feedbackConfiguration;
		type = Type.feedback;
		this.enabled = enabled;
		this.recipient = recipient;
		this.customized = customized;
		this.withLetter = false;
	}
	
	public String getId() {
		return id;
	}
	
	public String getName() {
		return mailTemplate == null ? name : mailTemplate.getName();
	}
	
	public boolean isWithLetter() {
		return withLetter;
	}
	
	public String getLetterName() {
		return letterName;
	}
	
	public void setLetterName(String letterName) {
		this.letterName = letterName;
	}

	public Type getType() {
		return type;
	}
	
	public String getRecipient() {
		return recipient;
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public boolean isCustomized() {
		return customized;
	}
	
	public boolean isSystemTemplate() {
		return type == Type.system;
	}

	public PositionMailTemplate getMailTemplate() {
		return mailTemplate;
	}
	
	public ApplicationsFeedbackConfiguration getFeedbackConfiguration() {
		return feedbackConfiguration;
	}

	public void setMailTemplate(PositionMailTemplate mailTemplate) {
		this.mailTemplate = mailTemplate;
	}
	
	public enum Type {
		system,
		referee,
		expert,
		comparativeExpert,
		feedback,
		committeeReminder,
		confirmationApplication,
		confirmationApplicationWithRefereeManagement,
		confirmationApplicationDuplicate,
		custom
		
	}

}
