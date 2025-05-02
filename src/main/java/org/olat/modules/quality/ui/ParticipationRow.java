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
package org.olat.modules.quality.ui;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.modules.quality.QualityContextRef;
import org.olat.modules.quality.QualityContextRole;
import org.olat.modules.quality.QualityParticipation;
import org.olat.modules.quality.ui.wizard.AddEmailContext.EmailIdentity;

/**
 * 
 * Initial date: 13.06.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ParticipationRow {

	private final String firstname;
	private final String lastname;
	private final String email;
	private final QualityContextRef contextRef;
	private final QualityContextRole role;
	private final String audienceRepositoryEntryName;
	private final String audienceCurriculumElementName;

	private FormLink toolsLink;

	public ParticipationRow(QualityParticipation participation) {
		firstname = participation.getFirstname();
		lastname = participation.getLastname();
		email = participation.getEmail();
		contextRef = participation.getContextRef();
		role = participation.getRole();
		audienceRepositoryEntryName = participation.getAudienceRepositoryEntryName();
		audienceCurriculumElementName = participation.getAudienceCurriculumElementName();
	}

	public ParticipationRow(EmailIdentity emailIdentity) {
		firstname = emailIdentity.identity() != null? emailIdentity.identity().getUser().getFirstName() : emailIdentity.emailExecutor().firstName();
		lastname = emailIdentity.identity() != null? emailIdentity.identity().getUser().getLastName() : emailIdentity.emailExecutor().lastName();
		email = emailIdentity.emailExecutor().email();
		contextRef = null;
		role = emailIdentity.identity() != null? null: QualityContextRole.email;
		audienceRepositoryEntryName = null;
		audienceCurriculumElementName = null;
	}
	
	public String getFirstname() {
		return firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public String getEmail() {
		return email;
	}
	
	public QualityContextRef getContextRef() {
		return contextRef;
	}
	
	public QualityContextRole getRole() {
		return role;
	}
	
	public String getAudienceRepositoryEntryName() {
		return audienceRepositoryEntryName;
	}
	
	public String getAudienceCurriculumElementName() {
		return audienceCurriculumElementName;
	}

	public FormLink getToolsLink() {
		return toolsLink;
	}

	public void setToolsLink(FormLink toolsLink) {
		this.toolsLink = toolsLink;
	}

}
