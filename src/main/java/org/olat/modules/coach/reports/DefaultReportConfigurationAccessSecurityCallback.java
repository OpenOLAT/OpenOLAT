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
import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
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

	public DefaultReportConfigurationAccessSecurityCallback(Identity identity, Roles roles, boolean coachingContext,
															boolean curriculumContext) {
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
}
