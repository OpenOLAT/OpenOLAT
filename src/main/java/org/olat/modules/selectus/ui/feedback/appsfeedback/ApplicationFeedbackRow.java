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

import java.util.Locale;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.id.Identity;
import org.olat.core.util.Formatter;

import org.olat.modules.selectus.model.ApplicationFeedback;
import org.olat.modules.selectus.ui.RecruitingHelper;
import org.olat.modules.selectus.ui.components.DateCellRenderer;

/**
 * 
 * Initial date: 29 avr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ApplicationFeedbackRow {
	
	private final Locale locale;
	private final Identity member;
	private final String memberFullname;
	private final String feedbackNumber;
	private final ApplicationFeedback feedback;

	private boolean open = true;
	private FormLink deleteLink;
	
	public ApplicationFeedbackRow(Identity member, ApplicationFeedback feedback, FormLink deleteLink,
			int feedbackNumber, Locale locale) {
		this.member = member;
		this.locale = locale;
		this.feedback = feedback;
		this.deleteLink = deleteLink;
		this.feedbackNumber = Integer.toString(feedbackNumber);
		memberFullname = RecruitingHelper.formatLastnameFirstName(member);
	}
	
	public String getId() {
		return feedback.getKey().toString();
	}
	
	public String getFullName() {
		return memberFullname;
	}
	
	public String getFeedbackNumber() {
		return feedbackNumber;
	}
	
	public Identity getMember() {
		return member;
	}
	
	public ApplicationFeedback getFeedback() {
		return feedback;
	}
	
	public String getFeedbackDate() {
		if(feedback.getCommentDate() == null) {
			return "";
		}
		return DateCellRenderer.format(feedback.getCommentDate(), locale);
	}
	
	public String getFeedbackTime() {
		if(feedback.getCommentDate() == null) {
			return "";
		}
		return Formatter.getInstance(locale).formatTimeShort(feedback.getCommentDate());
	}
	
	public String getComment() {
		StringBuilder comment = Formatter.escWithBR(feedback.getComment());
		return comment == null ? "" : comment.toString();
	}
	
	public boolean isOpen() {
		return open;
	}

	public void setOpen(boolean open) {
		this.open = open;
	}
	
	public FormLink getDeleteLink() {
		return deleteLink;
	}

}
