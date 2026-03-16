/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.feedback.appsfeedback;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.id.Identity;

import org.olat.modules.selectus.model.ApplicationFeedback;
import org.olat.modules.selectus.ui.RecruitingHelper;

/**
 * 
 * Initial date: 24 avr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FeedbackMember {
	
	private final Identity member;
	private final String memberFullname;
	private final ApplicationFeedback feedback;
	
	private FormLink sendMail;
	
	public FeedbackMember(Identity member, ApplicationFeedback feedback, FormLink sendMail) {
		this.member = member;
		this.sendMail = sendMail;
		this.feedback = feedback;
		memberFullname = RecruitingHelper.formatLastnameFirstName(member);
	}
	
	public String getFullname() {
		return memberFullname;
	}
	
	public Identity getMember() {
		return member;
	}
	
	public ApplicationFeedback getFeedback() {
		return feedback;
	}
	
	public FormLink getSendLink() {
		return sendMail;
	}
}
