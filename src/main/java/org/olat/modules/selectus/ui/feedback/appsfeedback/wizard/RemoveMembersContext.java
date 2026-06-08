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

import java.util.List;

import org.olat.core.id.Identity;

import org.olat.modules.selectus.model.ApplicationFeedback;
import org.olat.modules.selectus.model.Position;

/**
 * 
 * Initial date: 24 avr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RemoveMembersContext extends AbstractFeedbackMembersContext {
	
	public static final String FACULTY_MEMBER_PSEUDO_ROLE = "role.faculty.member";

	private final Position position;
	private final List<ApplicationFeedback> feedbacks;
	
	public RemoveMembersContext(Position position, List<ApplicationFeedback> feedbacks, List<Identity> members) {
		this.feedbacks = feedbacks;
		this.position = position;
		setMembers(members);
	}
	
	public Position getPosition() {
		return position;
	}
	
	public List<ApplicationFeedback> getFeedbacks() {
		return feedbacks;
	}

}
