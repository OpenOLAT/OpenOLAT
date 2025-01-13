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
package org.olat.modules.curriculum.ui.wizard;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.olat.basesecurity.GroupMembershipStatus;
import org.olat.core.id.Identity;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.ui.member.MemberDetailsController;
import org.olat.modules.curriculum.ui.member.MembershipModification;
import org.olat.modules.curriculum.ui.member.ModificationStatusSummary;
import org.olat.user.UserPropertiesRow;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: 12 déc. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ReviewEditedMembershipsRow extends UserPropertiesRow {
	
	private final Identity identity;
	private List<MembershipModification> modifications;
	private ModificationStatusSummary modificationSummary;
	private Map<RoleByElement,GroupMembershipStatus> statusByRoles = new HashMap<>();
	
	private MemberDetailsController detailsCtrl;
	
	public ReviewEditedMembershipsRow(Identity identity, List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		super(identity, userPropertyHandlers, locale);
		this.identity = identity;
	}
	
	public Identity getIdentity() {
		return identity;
	}
	
	public ModificationStatusSummary getModificationSummary() {
		return modificationSummary;
	}

	public void setModificationSummary(ModificationStatusSummary modificationSummary) {
		this.modificationSummary = modificationSummary;
	}

	public int getNumOfModifications(CurriculumRoles role) {
		int count = 0;
		if(modifications != null) {
			for(MembershipModification modification:modifications) {
				if(modification.role().equals(role)) {
					count++;
				}
			}
		}
		return count;
	}
	
	public List<MembershipModification> getModifications() {
		return modifications;
	}
	
	public GroupMembershipStatus getModification(Long curriculumElementKey, CurriculumRoles role) {
		if(modifications != null) {
			for(MembershipModification modification:modifications) {
				if(curriculumElementKey.equals(modification.curriculumElement().getKey())
						&& role.equals(modification.role())) {
					return modification.nextStatus();
				}
			}
		}
		return null;
	}

	public void setModifications(List<MembershipModification> modifications) {
		this.modifications = modifications;
	}
	
	public Map<RoleByElement,GroupMembershipStatus> getStatusByRoles() {
		return statusByRoles;
	}
	
	public GroupMembershipStatus getStatusBy(Long curriculumElementKey, CurriculumRoles role) {
		return statusByRoles.get(new RoleByElement(curriculumElementKey, role));
	}
	
	public void addStatus(Long curriculumElementKey, CurriculumRoles role, GroupMembershipStatus status) {
		statusByRoles.put(new RoleByElement(curriculumElementKey, role), status);
	}

	public boolean isDetailsControllerAvailable() {
		if(detailsCtrl != null) {
			return detailsCtrl.getInitialFormItem().isVisible();
		}
		return false;
	}

	public MemberDetailsController getDetailsController() {
		return detailsCtrl;
	}
	
	public String getDetailsControllerName() {
		if(detailsCtrl != null) {
			return detailsCtrl.getInitialFormItem().getComponent().getComponentName();
		}
		return null;
	}
	
	public void setDetailsController(MemberDetailsController detailsCtrl) {
		this.detailsCtrl = detailsCtrl;
	}
	
	public record RoleByElement(Long curriculumElementKey, CurriculumRoles role) {
		
		@Override
		public int hashCode() {
			return curriculumElementKey.hashCode() + role.hashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			if(this == obj) {
				return true;
			}
			if(obj instanceof RoleByElement rbe) {
				return curriculumElementKey.equals(rbe.curriculumElementKey)
						&& role.equals(rbe.role);
			}
			return false;
		}
	}
}
