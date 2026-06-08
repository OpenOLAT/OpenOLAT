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
package org.olat.modules.selectus.model;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.Identity;

/**
 * 
 * Initial date: 17 août 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface RecruitingAuditLog extends CreateInfo {
	
	public Long getKey();
	
	public Action getActionEnum();
	
	public ActionTarget getTargetEnum();
	
	public String getBefore();
	
	public String getAfter();
	
	public String getMessage();
	
	public String getMessageI18n();
	
	public String[] getMessageValues();
	
	public Long getPositionKey();
	
	public Long getApplicationKey();
	
	public Long getCommentKey();
	
	public Identity getIdentity();
	
	
	public enum Action {
		add,
		update,
		changeConfiguration,
		changeStatus,
		committeeReminder,
		withdraw,
		revertWithdraw,
		onhold,
		revertOnhold,
		rejected,
		revertRejected,
		noteligible,
		revertNoteligible,
		granted,
		revertGranted,
		delete,
		remove,
		sendMail,
		accepted,
		declined,
		reset,
		deactivated,
		reactivated,
		comment,
		hired,
		revertHired,
		copy
	}
	
	public enum ActionTarget {
		position,
		committee,
		application,
		rating,
		review,
		comment,
		referee,
		referenceLetter,
		expert,
		expertOpinion,
		comparativeExpert,
		comparativeAssessment,
		categories,
		assignment,
		publicFeedback,
		publicFeedbackLink,
		memberFeedback,
		memberFeedbackMgmt,
		decision
	}

}
