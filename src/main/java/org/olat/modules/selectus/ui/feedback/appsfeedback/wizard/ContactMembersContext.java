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
