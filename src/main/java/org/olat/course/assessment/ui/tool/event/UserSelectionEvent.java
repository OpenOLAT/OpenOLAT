package org.olat.course.assessment.ui.tool.event;

import java.util.List;

import org.olat.core.gui.control.Event;

/**
 * 
 * Initial date: 04.05.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserSelectionEvent extends Event {

	private static final long serialVersionUID = -4842433236044263696L;
	private static final String CMD = "select-user";
	
	private final Long identityKey;
	private final List<String> courseNodeIdents;
	
	public UserSelectionEvent(Long identityKey, List<String> courseNodeIdents) {
		super(CMD);
		this.identityKey = identityKey;
		this.courseNodeIdents = courseNodeIdents;
	}

	public Long getIdentityKey() {
		return identityKey;
	}

	public List<String> getCourseNodeIdents() {
		return courseNodeIdents;
	}
}
