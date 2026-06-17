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
package org.olat.modules.selectus.ui.feedback.appsfeedback;

import java.util.List;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.id.Identity;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.ApplicationFeedback;
import org.olat.modules.selectus.ui.RecruitingHelper;
import org.olat.modules.selectus.ui.model.AppToCategory;

/**
 * 
 * Initial date: 29 avr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PositionFeedbackRow {
	
	private final Identity member;
	private final String memberFullname;
	private final String applicationUrl;
	private final ApplicationFeedback feedback;

	private final FormLink sendMail;
	
	private List<AppToCategory> tags;
	
	public PositionFeedbackRow(Identity member, ApplicationFeedback feedback, String applicationUrl, FormLink sendMail) {
		this.member = member;
		this.sendMail = sendMail;
		this.feedback = feedback;
		this.applicationUrl = applicationUrl;
		memberFullname = RecruitingHelper.formatLastnameFirstName(member);
	}
	
	public String getFullName() {
		return memberFullname;
	}
	
	public Identity getMember() {
		return member;
	}
	
	public ApplicationFeedback getFeedback() {
		return feedback;
	}
	
	public Application getApplication() {
		return feedback.getApplication();
	}
	
	public String getApplicationUrl() {
		return applicationUrl;
	}
	
	public boolean hasComment() {
		return StringHelper.containsNonWhitespace(feedback.getComment());
	}
	
	public String getComment() {
		return Formatter.escWithBR(feedback.getComment()).toString();
	}
	
	public List<AppToCategory> getCategories() {
		return tags;
	}

	public void setCategorie(List<AppToCategory> tags) {
		this.tags = tags;
	}
	
	public FormLink getSendLink() {
		return sendMail;
	}
}
