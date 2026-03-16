/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.feedback.appsfeedback.wizard;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.id.Identity;

import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.ApplicationFeedback;
import org.olat.modules.selectus.model.ApplicationsFeedbackConfiguration;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.ui.RecruitingMailTemplate;

/**
 * 
 * Initial date: 24 avr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ContactMembersContext  extends AbstractFeedbackMembersContext {
	
	public static final String FACULTY_MEMBER_PSEUDO_ROLE = "role.faculty.member";

	private final List<ApplicationFeedback> feedbacks;
	private List<ApplicationFeedback> selectedFeedbacks;

	private final Position position;
	private final Identity secretary;
	private final Identity headOfCommittee;
	private final RecruitingMailTemplate mailTemplate;
	private final ApplicationsFeedbackConfiguration feedbackConfiguration;
	
	private Date deadline;
	private boolean sendMail;
	
	public ContactMembersContext(Position position, ApplicationsFeedbackConfiguration feedbackConfiguration,
			List<ApplicationFeedback> feedbacks, Identity secretary, Identity headOfCommittee,
			RecruitingMailTemplate mailTemplate) {
		this.position = position;
		this.secretary = secretary;
		this.feedbacks = new ArrayList<>(feedbacks);
		selectedFeedbacks = new ArrayList<>(feedbacks);
		this.feedbackConfiguration = feedbackConfiguration;
		this.headOfCommittee = headOfCommittee;
		this.mailTemplate = mailTemplate;
	}
	
	public Position getPosition() {
		return position;
	}
	
	public ApplicationsFeedbackConfiguration getConfiguration() {
		return feedbackConfiguration;
	}

	public Identity getSecretary() {
		return secretary;
	}
	
	public Identity getHeadOfCommittee() {
		return headOfCommittee;
	}
	
	public RecruitingMailTemplate getMailTemplate() {
		return mailTemplate;
	}

	public boolean isSendMail() {
		return sendMail;
	}

	public void setSendMail(boolean sendMail) {
		this.sendMail = sendMail;
	}

	public Date getDeadline() {
		return deadline;
	}

	public void setDeadline(Date deadline) {
		this.deadline = deadline;
	}
	
	public List<ApplicationFeedback> getFeedbacks() {
		return feedbacks;
	}
	
	public List<ApplicationFeedback> getSelectedFeedbacks() {
		return selectedFeedbacks;
	}
	
	public void setSelectedFeedbacks(List<ApplicationFeedback> selectedFeedbacks) {
		this.selectedFeedbacks = new ArrayList<>(selectedFeedbacks);
	}
	
	public List<Application> getApplications() {
		return getSelectedFeedbacks().stream()
				.map(ApplicationFeedback::getApplication)
				.distinct()
				.collect(Collectors.toList());
	}

	public List<Application> getApplicationsOf(Identity member) {
		Set<Application> apps = getSelectedFeedbacks().stream()
				.filter(feedback -> feedback.getIdentity().equals(member))
				.map(ApplicationFeedback::getApplication)
				.collect(Collectors.toSet());
		return new ArrayList<>(apps);
	}
	
	public List<ApplicationFeedback> getApplicationsFeedbackOf(Identity member) {
		Set<ApplicationFeedback> fbacks = selectedFeedbacks.stream()
				.filter(feedback -> feedback.getIdentity().equals(member))
				.collect(Collectors.toSet());
		return new ArrayList<>(fbacks);
	}

	public Identity getMember(String memberKey) {
		return getMembers().stream()
			.filter(member -> member.getKey().toString().equals(memberKey))
			.findFirst().orElse(null);
	}

}
