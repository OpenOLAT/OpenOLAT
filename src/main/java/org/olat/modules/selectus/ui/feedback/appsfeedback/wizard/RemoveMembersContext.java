/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
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
