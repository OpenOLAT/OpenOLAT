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
package org.olat.modules.coach.reports;

import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.modules.coach.RoleSecurityCallback;
import org.olat.modules.coach.security.RoleSecurityCallbackFactory;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.manager.CurriculumDAO;
import org.olat.modules.curriculum.manager.CurriculumElementDAO;
import org.olat.repository.manager.RepositoryEntryRelationDAO;

/**
 * Initial date: 2025-02-04<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class DefaultReportConfigurationAccessSecurityCallback implements ReportConfigurationAccessSecurityCallback {

	private final Roles roles;
	private final boolean coachingContext;
	private final boolean curriculumContext;
	
	private final boolean curriculumOwner;
	private final boolean curriculumManager;
	
	private final boolean curriculumElementCoach;
	private final boolean curriculumElementMasterCoach;

	private final boolean courseCoach;
	
	private final boolean principal;
	private final boolean administrator;

	private boolean showAbsencesReports;
	private boolean showInvoicesReports;
	
	public DefaultReportConfigurationAccessSecurityCallback(Identity identity, Roles roles, boolean coachingContext,
															boolean curriculumContext, OrganisationService organisationService) {
		this.roles = roles;
		this.coachingContext = coachingContext;
		this.curriculumContext = curriculumContext;
		
		CurriculumDAO curriculumDao = CoreSpringFactory.getImpl(CurriculumDAO.class);
		curriculumOwner = curriculumDao.hasCurriculumRole(identity, CurriculumRoles.curriculumowner.name());
		curriculumManager = curriculumDao.hasCurriculumRole(identity, CurriculumRoles.curriculummanager.name());
		
		CurriculumElementDAO curriculumElementDao = CoreSpringFactory.getImpl(CurriculumElementDAO.class);
		curriculumElementCoach = curriculumElementDao.hasCurriculumElementRole(identity, CurriculumRoles.coach.name());
		curriculumElementMasterCoach = curriculumDao.hasCurriculumRole(identity, CurriculumRoles.mastercoach.name());
		
		RepositoryEntryRelationDAO repositoryEntryRelationDao = CoreSpringFactory.getImpl(RepositoryEntryRelationDAO.class);
		courseCoach = repositoryEntryRelationDao.hasRoleExpanded(identity, GroupRoles.coach.name());
		
		administrator = roles.isAdministrator();
		principal = roles.isPrincipal();
		
		setOrgRights(identity, organisationService);
	}

	private void setOrgRights(Identity identity, OrganisationService organisationService) {
		showAbsencesReports = false;
		showInvoicesReports = false;
		if (roles.isLineManager()) {
			RoleSecurityCallback roleSecurityCallback = getRoleSecurityCallback(identity, organisationService, OrganisationRoles.linemanager);
			if (roleSecurityCallback.canViewAbsenceReport()) {
				showAbsencesReports = true;
			}
			if (roleSecurityCallback.canViewInvoicesReport()) {
				showInvoicesReports = true;
			}
		}
		
		if (showAbsencesReports && showInvoicesReports) {
			return; // We got what we came here for, no need for more DB queries.
		}

		if (roles.isEducationManager()) {
			RoleSecurityCallback roleSecurityCallback = getRoleSecurityCallback(identity, organisationService, OrganisationRoles.educationmanager);
			if (roleSecurityCallback.canViewAbsenceReport()) {
				showAbsencesReports = true;
			}
			if (roleSecurityCallback.canViewInvoicesReport()) {
				showInvoicesReports = true;
			}
		}
	}
	
	RoleSecurityCallback getRoleSecurityCallback(Identity identity, OrganisationService organisationService, OrganisationRoles role) {
		return RoleSecurityCallbackFactory.create(organisationService.getGrantedOrganisationsRights(
						organisationService.getOrganisations(identity, role), role), role);
	}

	@Override
	public boolean isCoachingContext() {
		return coachingContext;
	}

	@Override
	public boolean isCurriculumContext() {
		return curriculumContext;
	}

	@Override
	public boolean isLineOrEducationManager() {
		return roles.isLineManager() || roles.isEducationManager();
	}

	@Override
	public boolean isCurriculumManager() {
		return curriculumManager || roles.isCurriculumManager();
	}

	@Override
	public boolean isCurriculumOwner() {
		return curriculumOwner;
	}

	@Override
	public boolean isCurriculumCoach() {
		return curriculumElementCoach || curriculumElementMasterCoach;
	}

	@Override
	public boolean isCourseCoach() {
		return courseCoach;
	}
	
	@Override
	public boolean isPrincipal() {
		return principal;
	}

	@Override
	public boolean isAdministrator() {
		return administrator;
	}

	@Override
	public boolean isShowAbsencesReports() {
		return showAbsencesReports;
	}

	@Override
	public boolean isShowInvoicesReports() {
		return showInvoicesReports;
	}
}
