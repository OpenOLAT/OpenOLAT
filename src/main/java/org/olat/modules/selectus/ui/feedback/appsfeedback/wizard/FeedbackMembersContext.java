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

import org.olat.core.id.Identity;

import org.olat.modules.selectus.model.ApplicationLight;
import org.olat.modules.selectus.model.ApplicationsFeedbackConfiguration;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.ui.RecruitingMailTemplate;
import org.olat.modules.selectus.ui.committee.wizard.CommitteeMember;

/**
 * 
 * Initial date: 24 avr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FeedbackMembersContext {
	
	public static final String FACULTY_MEMBER_PSEUDO_ROLE = "role.faculty.member";
	
	private final List<CommitteeMember> members = new ArrayList<>();

	private final Position position;
	private final Identity secretary;
	private final Identity headOfCommittee;
	private final RecruitingMailTemplate mailTemplate;
	private final ApplicationsFeedbackConfiguration feedbackConfiguration;
	
	private Date deadline;
	private boolean sendMail = true;
	private List<ApplicationLight> selectedApps;
	
	public FeedbackMembersContext(Position position, ApplicationsFeedbackConfiguration feedbackConfiguration,
			List<ApplicationLight> apps, Identity secretary, Identity headOfCommittee,
			RecruitingMailTemplate mailTemplate) {
		this.position = position;
		this.secretary = secretary;
		this.feedbackConfiguration = feedbackConfiguration;
		this.headOfCommittee = headOfCommittee;
		this.mailTemplate = mailTemplate;
		this.selectedApps = new ArrayList<>(apps);
	}
	
	public Position getPosition() {
		return position;
	}
	
	public ApplicationsFeedbackConfiguration getConfiguration() {
		return feedbackConfiguration;
	}
	
	public List<ApplicationLight> getSelectedApps() {
		return selectedApps;
	}

	public void setSelectedApps(List<ApplicationLight> selectedApps) {
		this.selectedApps = selectedApps;
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

	public List<CommitteeMember> getMembers() {
		return members;
	}
	
	public boolean hasMember(String email) {
		for(CommitteeMember member:members) {
			if(email.equalsIgnoreCase(member.getEmail())) {
				return true;
			}
		}
		return false;
	}
	
	public CommitteeMember getMember(String email) {
		for(CommitteeMember member:members) {
			if(email.equalsIgnoreCase(member.getEmail())) {
				return member;
			}
		}
		return null;
	}
	
	public void addMember(CommitteeMember member) {
		members.add(member);
	}

}
