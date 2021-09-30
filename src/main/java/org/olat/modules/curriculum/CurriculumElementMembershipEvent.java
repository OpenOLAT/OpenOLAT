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
package org.olat.modules.curriculum;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.util.event.MultiUserEvent;

/**
 * 
 * Initial date: 9 Dec 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementMembershipEvent extends MultiUserEvent {

	private static final long serialVersionUID = -4147709424125454372L;
	
	public static final String MEMEBER_ADDED = "curriculum.element.member.added.event";
	public static final String MEMEBER_REMOVED = "curriculum.element.member.removed.event";

	private final Long curriculumElementKey;
	private final Long identityKey;
	private final CurriculumRoles role;
	
	public CurriculumElementMembershipEvent(String command, CurriculumElementRef element, IdentityRef identity, CurriculumRoles role) {
		super(command);
		this.curriculumElementKey = element.getKey();
		this.identityKey = identity.getKey();
		this.role = role;
	}
	
	public static CurriculumElementMembershipEvent identityAdded(CurriculumElementRef element, IdentityRef identity, CurriculumRoles role) {
		return new CurriculumElementMembershipEvent(MEMEBER_ADDED, element, identity, role);
	}
	
	public static CurriculumElementMembershipEvent identityRemoved(CurriculumElementRef element, IdentityRef identity) {
		return new CurriculumElementMembershipEvent(MEMEBER_REMOVED, element, identity, null);
	}
	
	public static CurriculumElementMembershipEvent identityRemoved(CurriculumElementRef element, IdentityRef identity, String roleValue) {
		CurriculumRoles role = CurriculumRoles.isValueOf(roleValue)? CurriculumRoles.valueOf(roleValue): null;
		return identityRemoved(element, identity, role);
	}
	
	public static CurriculumElementMembershipEvent identityRemoved(CurriculumElementRef element, IdentityRef identity, CurriculumRoles role) {
		return new CurriculumElementMembershipEvent(MEMEBER_REMOVED, element, identity, role);
	}
	
	public Long getCurriculumElementKey() {
		return curriculumElementKey;
	}

	public Long getIdentityKey() {
		return identityKey;
	}

	public CurriculumRoles getRole() {
		return role;
	}

}
