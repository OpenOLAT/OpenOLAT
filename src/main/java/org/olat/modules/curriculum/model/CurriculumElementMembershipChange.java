/**
 * <a href="http://www.openolat.org">
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
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.curriculum.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.olat.basesecurity.GroupMembershipStatus;
import org.olat.core.gui.control.Event;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.ui.member.ConfirmationByEnum;
import org.olat.modules.curriculum.ui.member.ConfirmationMembershipEnum;

/**
 * 
 * Initial date: 8 juin 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementMembershipChange extends Event {
	private static final long serialVersionUID = 8499004967313689825L;

	private final Identity member;
	private final CurriculumElement curriculumElement;
	
	private Date confirmUntil;
	private ConfirmationByEnum confirmationBy;
	private ConfirmationMembershipEnum confirmation;

	private EnumMap<CurriculumRoles,GroupMembershipStatus> modifications = new EnumMap<>(CurriculumRoles.class);
	private EnumMap<CurriculumRoles,String> adminNotes = new EnumMap<>(CurriculumRoles.class);
	
	private CurriculumElementMembershipChange(Identity member, CurriculumElement curriculumElement) {
		super("id-perm-changed");
		this.member = member;
		this.curriculumElement = curriculumElement;
	}

	public static final CurriculumElementMembershipChange valueOf(Identity member, CurriculumElement curriculumElement) {
		return new CurriculumElementMembershipChange(member, curriculumElement);
	}
	
	public static final CurriculumElementMembershipChange copy(Identity member, CurriculumElementMembershipChange origin) {
		CurriculumElementMembershipChange change = new CurriculumElementMembershipChange(member, origin.getCurriculumElement());
		change.modifications.putAll(origin.modifications);
		return change;
	}
	
	public static final CurriculumElementMembershipChange addMembership(Identity member, CurriculumElement curriculumElement, CurriculumRoles role) {
		CurriculumElementMembershipChange change = new CurriculumElementMembershipChange(member, curriculumElement);
		change.modifications.put(role, GroupMembershipStatus.active);
		return change;
	}

	public CurriculumElement getCurriculumElement() {
		return curriculumElement;
	}

	public Identity getMember() {
		return member;
	}
	
	public GroupMembershipStatus getNextStatus(CurriculumRoles role) {
		return modifications.get(role);
	}
	
	public void setNextStatus(CurriculumRoles role, GroupMembershipStatus nextStatus) {
		if(nextStatus == null) {
			modifications.remove(role);
		} else {
			modifications.put(role, nextStatus);
		}
	}
	
	public String getAdminNoteBy(CurriculumRoles role) {
		return adminNotes.get(role);
	}
	
	public void setAdminNote(CurriculumRoles role, String adminNote) {
		if(StringHelper.containsNonWhitespace(adminNote)) {		
			adminNotes.put(role, adminNote);
		} else {
			adminNotes.clear();
		}
	}
	
	public Date getConfirmUntil() {
		return confirmUntil;
	}

	public void setConfirmUntil(Date confirmUntil) {
		this.confirmUntil = confirmUntil;
	}

	public ConfirmationByEnum getConfirmationBy() {
		return confirmationBy;
	}

	public void setConfirmationBy(ConfirmationByEnum confirmationBy) {
		this.confirmationBy = confirmationBy;
	}

	public ConfirmationMembershipEnum getConfirmation() {
		return confirmation;
	}

	public void setConfirmation(ConfirmationMembershipEnum confirmation) {
		this.confirmation = confirmation;
	}

	public boolean isEmpty() {
		return modifications.isEmpty();
	}
	
	public List<CurriculumRoles> getRoles() {
		List<CurriculumRoles> roles = new ArrayList<>();
		for(Map.Entry<CurriculumRoles, GroupMembershipStatus> roleEntry:modifications.entrySet()) {
			roles.add(roleEntry.getKey());
		}
		return roles;
	}

	public boolean addRole() {
		for(Map.Entry<CurriculumRoles, GroupMembershipStatus> roleEntry:modifications.entrySet()) {
			if(roleEntry.getValue() == GroupMembershipStatus.active) {
				return true;
			}
		}
		return false;
	}
	
	public int numOfSegments() {
		String path = curriculumElement.getMaterializedPathKeys();
		int count = 0;
		if(StringHelper.containsNonWhitespace(path)) {
			char[] pathArr = path.toCharArray();
			for(int i=pathArr.length; i-->1; ) {
				if(pathArr[i] == '/') {
					count++;
				}
			}
		}
		return count;
	}
}
