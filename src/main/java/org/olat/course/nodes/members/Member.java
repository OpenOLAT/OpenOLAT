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
package org.olat.course.nodes.members;

import java.util.List;
import java.util.Locale;

import org.olat.commons.memberlist.model.CurriculumElementInfos;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.user.UserPropertiesRow;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: 21.12.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class Member extends UserPropertiesRow {
	
	private final String firstName;
	private final String lastName;
	private final String fullName;
	private final String gender;
	private final Long key;
	private boolean portrait;
	private String portraitCssClass;
	private final CurriculumElementInfos curriculumElementInfos;
	
	private FormLink emailLink;
	private FormLink chatLink;
	private FormLink idLink;
	private FormLink removeLink;
	
	public Member(Identity identity, String fullName, CurriculumElementInfos curriculumElementInfos,
			List<UserPropertyHandler> userPropertyHandlers, Locale locale, boolean portrait, String portraitCssClass) {
		super(identity, userPropertyHandlers, locale);
		this.firstName = identity.getUser().getProperty(UserConstants.FIRSTNAME, locale);
		this.lastName = identity.getUser().getProperty(UserConstants.LASTNAME, locale);
		String genderEn = identity.getUser().getProperty(UserConstants.GENDER, Locale.ENGLISH);
		if(genderEn != null) {
			gender = genderEn.toLowerCase();
		} else {
			gender = "-";
		}
		this.fullName = fullName;
		this.key = identity.getKey();
		this.curriculumElementInfos = curriculumElementInfos;
		this.portrait = portrait;
		this.portraitCssClass = portraitCssClass;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}
	
	public String getGender() {
		return gender;
	}
	
	public String getPortraitCssClass() {
		return portraitCssClass;
	}
	
	public boolean isPortraitAvailable() {
		return portrait; 
	}
	
	public CurriculumElementInfos getCurriculumElementInfos() {
		return curriculumElementInfos;
	}
	
	public String getCurriculumDisplayName() {
		return curriculumElementInfos == null ? "" : curriculumElementInfos.getCurriculumDisplayName();
	}
	
	public String getCurriculumRootElementDisplayName() {
		return curriculumElementInfos == null ? "" : curriculumElementInfos.getRootElementDisplayName();
	}
	
	public String getCurriculumRootElementIdentifier() {
		return curriculumElementInfos == null ? "" : curriculumElementInfos.getRootElementIdentifier();
	}

	public FormLink getIdLink() {
		return idLink;
	}

	public void setIdLink(FormLink idLink) {
		this.idLink = idLink;
	}

	public FormLink getEmailLink() {
		return emailLink;
	}
	
	public String getEmailComponentName() {
		return emailLink == null ? null : emailLink.getComponent().getComponentName();
	}

	public void setEmailLink(FormLink emailLink) {
		this.emailLink = emailLink;
	}

	public FormLink getChatLink() {
		return chatLink;
	}
	
	public String getChatComponentName() {
		return chatLink == null ? null : chatLink.getComponent().getComponentName();
	}

	public void setChatLink(FormLink chatLink) {
		this.chatLink = chatLink;
	}

	public FormLink getRemoveLink() {
		return removeLink;
	}
	
	public String getRemoveComponentName() {
		return removeLink == null ? null : removeLink.getComponent().getComponentName();
	}

	public void setRemoveLink(FormLink removeLink) {
		this.removeLink = removeLink;
	}

	public String getFullName() {
		return fullName;
	}

	public Long getKey() {
		return key;
	}
	
	@Override
	public int hashCode() {
		return key.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof Member) {
			Member member = (Member)obj;
			return key != null && key.equals(member.key);
		}
		return false;
	}
}
