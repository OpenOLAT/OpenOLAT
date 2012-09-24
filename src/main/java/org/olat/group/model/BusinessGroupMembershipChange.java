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
package org.olat.group.model;

import org.olat.core.gui.control.Event;
import org.olat.core.id.Identity;
import org.olat.group.BusinessGroupShort;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class BusinessGroupMembershipChange extends Event {
	private static final long serialVersionUID = 8499004967313689825L;

	private final Identity member;
	private final Long groupKey;
	private BusinessGroupShort group;
	
	private Boolean tutor;
	private Boolean participant;
	private Boolean waitingList;
	
	public BusinessGroupMembershipChange(Identity member, BusinessGroupShort group) {
		this(member, group.getKey());
		this.group = group;
	}
	
	public BusinessGroupMembershipChange(Identity member, BusinessGroupMembershipChange origin) {
		this(member, origin.getGroup());
		tutor = origin.tutor;
		participant = origin.participant;
		waitingList = origin.waitingList;	
	}
	
	public BusinessGroupMembershipChange(Identity member, Long groupKey) {
		super("id-perm-changed");
		this.groupKey = groupKey;
		this.member = member;
	}

	public BusinessGroupShort getGroup() {
		return group;
	}

	public Identity getMember() {
		return member;
	}

	public Boolean getTutor() {
		return tutor;
	}

	public void setTutor(Boolean tutor) {
		this.tutor = tutor;
	}

	public Boolean getParticipant() {
		return participant;
	}

	public void setParticipant(Boolean participant) {
		this.participant = participant;
	}

	public Boolean getWaitingList() {
		return waitingList;
	}

	public void setWaitingList(Boolean waitingList) {
		this.waitingList = waitingList;
	}

	public Long getGroupKey() {
		return group == null ? groupKey : group.getKey();
	}
}
