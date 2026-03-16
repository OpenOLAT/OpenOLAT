/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.feedback.appsfeedback.wizard;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.id.Identity;

/**
 * 
 * Initial date: 7 mai 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AbstractFeedbackMembersContext {
	
	private List<Identity> selectedMembers;
	private final List<Identity> members = new ArrayList<>();
	
	public List<Identity> getMembers() {
		return members;
	}
	
	public void setMembers(List<Identity> members) {
		this.members.addAll(members);
	}
	
	public List<Identity> getSelectedMembers() {
		return selectedMembers;
	}

	public void setSelectedMembers(List<Identity> selectedMembers) {
		this.selectedMembers = selectedMembers;
	}

}
