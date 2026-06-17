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
package org.olat.modules.selectus.ui;

import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Roles;

import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingSecurityCallback;
import org.olat.modules.selectus.model.CommitteeMembershipsStats;
import org.olat.modules.selectus.model.PositionRole;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  9 aug. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class RecruitingSecurityCallbackImpl implements RecruitingSecurityCallback {

	private final Roles roles;
	private final CommitteeMembershipsStats positionRoles;
	private final RecruitingModule recruitingModule;
	
	public RecruitingSecurityCallbackImpl(Roles roles, CommitteeMembershipsStats positionRoles) {
		this.roles = roles;
		this.positionRoles = positionRoles;
		recruitingModule = CoreSpringFactory.getImpl(RecruitingModule.class);
	}
	
	@Override
	public boolean canAddPosition() {
		return roles.isSelectusManager();
	}

	@Override
	public boolean canEditPosition() {
		return roles.isSelectusManager();
	}

	@Override
	public boolean canDeletePosition() {
		return roles.isSelectusManager();
	}

	@Override
	public boolean canArchivePosition() {
		return roles.isSelectusManager();
	}

	@Override
	public boolean canCopyPosition() {
		return recruitingModule.isPositionCopyEnabled() && canAddPosition();
	}
	
	@Override
	public boolean canReportingPosition() {
		return recruitingModule.isReportingEnabled() && roles.isSelectusManager();
	}

	@Override
	public boolean canSearchPositionByOrgUnits() {
		return roles.isSelectusManager();
	}

	@Override
	public boolean canSearchPositionByGlobalAttributes() {
		return roles.isSelectusManager();
	}

	@Override
	public boolean canSeePositionURL() {
		return roles.isSelectusManager();
	}

	@Override
	public boolean canAddCommitteeMember() {
		return roles.isSelectusManager();
	}
	
	@Override
	public boolean canImportCommitteeMembers() {
		return recruitingModule.isPositionCopyEnabled() && canAddPosition();
	}
	
	@Override
	public boolean canEditCommitteeMember() {
		return roles.isSelectusManager();
	}

	@Override
	public boolean canRemoveCommitteeMember() {
		return roles.isSelectusManager();
	}

	// Selectus manager and committee secretary as configured
	@Override
	public boolean canSendMailToCommittee() {
		return roles.isSelectusManager();
	}
	
	// Selectus manager and committee secretary as configured
	@Override
	public boolean canSearchApplications() {
		return roles.isSelectusManager() || canSearchApplicationsByRoles();
	}
	
	private boolean canSearchApplicationsByRoles() {
		PositionRole[] searchRoles = recruitingModule.getRolesAllowedToSearchApplications();
		for(PositionRole role:searchRoles) {
			if((role == PositionRole.secretary && positionRoles.getNumAsSecretary() > 0)
					|| (role == PositionRole.head && positionRoles.getNumAsHead() > 0)
					|| (role == PositionRole.exofficio && positionRoles.getNumAsExOfficio() > 0)) {
				return true;
			}
		}
		return false;
	}

	//staff and committee secretary
	@Override
	public boolean canExcelListCommittee() {
		return roles.isSelectusManager();
	}
	
	@Override
	public boolean canExcelReviewStatistics() {
		return roles.isSelectusManager();
	}

	@Override
	public boolean canPDFApplicationList() {
		return roles.isSelectusManager();
	}
	
	@Override
	public boolean canExcelApplicationList() {
		return roles.isSelectusManager();
	}

	@Override
	public boolean canPDFRatings() {
		return roles.isSelectusManager();
	}

	@Override
	public boolean canMailCenter() {
		return roles.isSelectusManager();
	}

	@Override
	public boolean canMailCenterExportLog() {
		return roles.isSelectusManager();
	}

	@Override
	public boolean canMailCenterViewEmail() {
		return roles.isSelectusManager();
	}

	@Override
	public boolean canMailCenterResendEmail() {
		return roles.isSelectusManager();
	}

	@Override
	public boolean canSendMailToApplicant() {
		return roles.isSelectusManager();
	}

	@Override
	public boolean canDecisionTool() {
		return roles.isSelectusManager();
	}

	@Override
	public boolean canConfigureDecisionTool() {
		return roles.isSelectusManager();
	}

	@Override
	public boolean canEditDecisionRubrics() {
		return roles.isSelectusManager();
	}

	@Override
	public boolean canViewPositionListLog() {
		return recruitingModule.isNotificationsToolEnabled();
	}

	@Override
	public boolean canDeleteCache() {
		return roles.isAdministrator();
	}	
}
