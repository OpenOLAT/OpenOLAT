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
package org.olat.modules.curriculum.ui.member;

import java.util.Date;

import org.olat.basesecurity.GroupMembershipHistory;
import org.olat.basesecurity.GroupMembershipStatus;
import org.olat.core.id.Identity;
import org.olat.group.ui.main.CourseMembership;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.site.ComparableCurriculumElementRow;

/**
 * 
 * Initial date: 26 nov. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MemberHistoryDetailsRow extends AbstractCurriculumElementRow {
	
	private Identity identity;
	private String userDisplayName;
	private String actorDisplayName;
	private final CurriculumRoles role;
	private final CourseMembership membership;
	private final GroupMembershipHistory point;
	
	public MemberHistoryDetailsRow(CurriculumElement curriculumElement, GroupMembershipHistory point) {
		super(curriculumElement);
		this.point = point;
		membership = new CourseMembership();
		membership.setCurriculumElementRole(point.getRole());
		role = CurriculumRoles.isValueOf(point.getRole()) ? CurriculumRoles.valueOf(point.getRole()) : null;
	}
	
	public MemberHistoryDetailsRow(Identity identity, String userDisplayName, GroupMembershipHistory point) {
		super(null);
		this.point = point;
		this.identity = identity;
		this.userDisplayName = userDisplayName;
		membership = new CourseMembership();
		membership.setCurriculumElementRole(point.getRole());
		role = CurriculumRoles.isValueOf(point.getRole()) ? CurriculumRoles.valueOf(point.getRole()) : null;
	}

	@Override
	public ComparableCurriculumElementRow getParent() {
		return null;
	}
	
	public Long getHistoryKey() {
		return point.getKey();
	}
	
	public Date getDate() {
		return point.getCreationDate();
	}
	
	public CurriculumRoles getRole() {
		return role;
	}
	
	public CourseMembership getMembership() {
		return membership;
	}
	
	public GroupMembershipStatus getStatus() {
		return point.getStatus();
	}
	
	public Identity getIdentity() {
		return identity;
	}

	public String getUserDisplayName() {
		return userDisplayName;
	}

	public void setUserDisplayName(String userDisplayName) {
		this.userDisplayName = userDisplayName;
	}

	public String getActorDisplayName() {
		return actorDisplayName;
	}

	public void setActorDisplayName(String actorDisplayName) {
		this.actorDisplayName = actorDisplayName;
	}
}
