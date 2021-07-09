/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.course.run.userview;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.course.ICourse;
import org.olat.course.assessment.manager.EfficiencyStatementManager;
import org.olat.course.certificate.CertificatesManager;
import org.olat.course.condition.interpreter.ConditionInterpreter;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.scoring.AssessmentAccounting;
import org.olat.course.run.scoring.NoEvaluationAccounting;
import org.olat.course.run.scoring.ScoreAccounting;
import org.olat.group.BusinessGroup;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntrySecurity;
import org.olat.repository.model.RepositoryEntryLifecycle;

/**
 * Initial Date:  Feb 6, 2004
 * @author Felix Jost
 *
 */
public class UserCourseEnvironmentImpl implements UserCourseEnvironment {
	private final IdentityEnvironment identityEnvironment;
	private final CourseEnvironment courseEnvironment;
	private ConditionInterpreter conditionInterpreter;
	private ScoreAccounting scoreAccounting;
	private RepositoryEntryLifecycle lifecycle;
	private RepositoryEntry courseRepoEntry;
	private List<BusinessGroup> coachedGroups;
	private List<BusinessGroup> participatingGroups;
	private List<BusinessGroup> waitingLists;
	private List<CurriculumElement> coachedCurriculums;
	private List<String> usernames;
	
	private final WindowControl windowControl;
	
	private Boolean admin;
	private Boolean coach;
	private Boolean participant;
	private Boolean allUsersParticipant;
	private Boolean adminAnyCourse;
	private Boolean coachAnyCourse;
	private Boolean participantAnyCourse;
	
	private Boolean certification;
	private Boolean courseReadOnlyByStatus;
	private Boolean courseReadOnlyByRole;
	
	public UserCourseEnvironmentImpl(IdentityEnvironment identityEnvironment, CourseEnvironment courseEnvironment) {
		this(identityEnvironment, courseEnvironment, null, null, null, null, null, null, null, null, null);
		if(courseEnvironment != null && courseEnvironment.getCourseGroupManager().getCourseEntry().getEntryStatus() != null) {
			courseReadOnlyByStatus = courseEnvironment.getCourseGroupManager().getCourseEntry().getEntryStatus().decommissioned();
		}
	}

	/**
	 * Constructor used for previewing the course.
	 * 
	 * @param identityEnvironment The identity environment
	 * @param courseEnvironment The course environment
	 * @param scoreAccounting Fix the score accounting implementation
	 */
	public UserCourseEnvironmentImpl(IdentityEnvironment identityEnvironment, CourseEnvironment courseEnvironment, ScoreAccounting scoreAccounting) {
		this(identityEnvironment, courseEnvironment, null, null, null, null, null, null, null, null, null);
		this.scoreAccounting = scoreAccounting;
	}
	
	public UserCourseEnvironmentImpl(IdentityEnvironment identityEnvironment, CourseEnvironment courseEnvironment,
			CourseReadOnlyDetails courseReadOnlyDetails) {
		this(identityEnvironment, courseEnvironment, null, null, null, null, null, null, null,
				courseReadOnlyDetails.getByStatus(), courseReadOnlyDetails.getByRole());
	}
	
	public UserCourseEnvironmentImpl(IdentityEnvironment identityEnvironment, CourseEnvironment courseEnvironment, WindowControl windowControl,
			List<BusinessGroup> coachedGroups, List<BusinessGroup> participatingGroups, List<BusinessGroup> waitingLists,
			RepositoryEntrySecurity reSecurity) {
		this(identityEnvironment, courseEnvironment, windowControl, coachedGroups, participatingGroups, waitingLists,
				reSecurity.isCoach(), reSecurity.isEntryAdmin() || reSecurity.isPrincipal() || reSecurity.isMasterCoach() , reSecurity.isParticipant(),
				reSecurity.isReadOnly(), reSecurity.isOnlyPrincipal() || reSecurity.isOnlyMasterCoach());
	}
	
	private UserCourseEnvironmentImpl(IdentityEnvironment identityEnvironment, CourseEnvironment courseEnvironment, WindowControl windowControl,
			List<BusinessGroup> coachedGroups, List<BusinessGroup> participatingGroups, List<BusinessGroup> waitingLists,
			Boolean coach, Boolean admin, Boolean participant, Boolean courseReadOnlyByStatus, Boolean courseReadOnlyByRole) {
		this.courseEnvironment = courseEnvironment;
		this.identityEnvironment = identityEnvironment;
		this.conditionInterpreter = new ConditionInterpreter(this);
		this.coachedGroups = coachedGroups;
		this.participatingGroups = participatingGroups;
		this.waitingLists = waitingLists;
		this.coach = coach;
		this.admin = admin;
		this.participant = participant;
		this.windowControl = windowControl;
		this.courseReadOnlyByStatus = courseReadOnlyByStatus;
		this.courseReadOnlyByRole = courseReadOnlyByRole;
	}

	public static UserCourseEnvironmentImpl load(UserRequest ureq, ICourse course, RepositoryEntrySecurity reSecurity, WindowControl wControl) {
		CourseGroupManager cgm = course.getCourseEnvironment().getCourseGroupManager();
		List<BusinessGroup> coachedGroups;
		if(reSecurity.isGroupCoach()) {
			coachedGroups = cgm.getOwnedBusinessGroups(ureq.getIdentity());
		} else {
			coachedGroups = Collections.emptyList();
		}
		List<BusinessGroup> participatedGroups;
		if(reSecurity.isGroupParticipant()) {
			participatedGroups = cgm.getParticipatingBusinessGroups(ureq.getIdentity());
		} else {
			participatedGroups = Collections.emptyList();
		}
		List<BusinessGroup> waitingLists;
		if(reSecurity.isGroupWaiting()) {
			waitingLists = cgm.getWaitingListGroups(ureq.getIdentity());
		} else {
			waitingLists = Collections.emptyList();
		}

		return new UserCourseEnvironmentImpl(ureq.getUserSession().getIdentityEnvironment(), course.getCourseEnvironment(), wControl,
				coachedGroups, participatedGroups, waitingLists, reSecurity);
	}
	
	@Override
	public CourseEnvironment getCourseEnvironment() {
		return courseEnvironment;
	}

	@Override
	public IdentityEnvironment getIdentityEnvironment() {
		return identityEnvironment;
	}

	@Override
	public WindowControl getWindowControl() {
		return windowControl;
	}

	@Override
	public ConditionInterpreter getConditionInterpreter() {
		return conditionInterpreter;
	}

	@Override
	public ScoreAccounting getScoreAccounting() {
		if (scoreAccounting == null) {
			initScoreAccounting();
		}
		return scoreAccounting;
	}
	
	private void initScoreAccounting() {
		scoreAccounting = isParticipant() ? new AssessmentAccounting(this): new NoEvaluationAccounting();
	}

	@Override
	public CourseEditorEnv getCourseEditorEnv() {
		// return null signalling this is real user environment
		return null;
	}
	
	@Override
	public boolean isIdentityInCourseGroup(Long groupKey) {
		if(coachedGroups != null && participatingGroups != null) {
			return PersistenceHelper.listContainsObjectByKey(participatingGroups, groupKey)
					|| PersistenceHelper.listContainsObjectByKey(coachedGroups, groupKey);
		}
		CourseGroupManager cgm = courseEnvironment.getCourseGroupManager();
		return cgm.isIdentityInGroup(identityEnvironment.getIdentity(), groupKey);
	}

	@Override
	public boolean isInOrganisation(String organisationIdentifier, OrganisationRoles... roles) {
		CourseGroupManager cgm = courseEnvironment.getCourseGroupManager();
		return cgm.isIdentityInOrganisation(identityEnvironment.getIdentity(), organisationIdentifier, roles);
	}

	@Override
	public boolean isCoach() {
		if(coach != null) {
			return coach.booleanValue();
		}
		//lazy loading
		CourseGroupManager cgm = courseEnvironment.getCourseGroupManager();
		boolean coachLazy = cgm.isIdentityCourseCoach(identityEnvironment.getIdentity());
		coach = Boolean.valueOf(coachLazy);
		return coachLazy;
	}

	@Override
	public boolean isAdmin() {
		if(admin != null) {
			return admin.booleanValue();
		}
		//lazy loading
		CourseGroupManager cgm = courseEnvironment.getCourseGroupManager();
		boolean admiLazy = cgm.isIdentityCourseAdministrator(identityEnvironment.getIdentity());
		admin = Boolean.valueOf(admiLazy);
		return admiLazy;
	}

	@Override
	public boolean isParticipant() {
		// User is membership participant
		if(participant != null && participant.booleanValue()) {
			return true;
		}
		
		// If a course is open to all users, a user may is not a member but has access as well.
		// Such a user has to act as a participant as well.
		if (allUsersParticipant == null) {
			boolean allUsersOnly = !isAdmin() && !isCoach() && !isGuestOnly();
			allUsersParticipant = Boolean.valueOf(allUsersOnly);
		}
		if (allUsersParticipant.booleanValue()) {
			return true;
		}
		
		// User is not membership participant
		if(participant != null && !participant.booleanValue()) {
			return false;
		}
		
		//lazy loading
		CourseGroupManager cgm = courseEnvironment.getCourseGroupManager();
		boolean partLazy = cgm.isIdentityCourseParticipant(identityEnvironment.getIdentity());
		participant = Boolean.valueOf(partLazy);
		return partLazy;
	}
	
	private boolean isGuestOnly() {
		Roles roles = getIdentityEnvironment().getRoles() != null
				? getIdentityEnvironment().getRoles()
				: CoreSpringFactory.getImpl(BaseSecurity.class).getRoles(getIdentityEnvironment().getIdentity());
		return roles.isGuestOnly();
	}

	@Override
	public boolean isMemberParticipant() {
		return courseEnvironment.getCourseGroupManager().isIdentityCourseParticipant(identityEnvironment.getIdentity());
	}

	@Override
	public boolean isAdministratorOfAnyCourse() {
		if(adminAnyCourse != null) {
			return adminAnyCourse.booleanValue();
		}

		CourseGroupManager cgm = courseEnvironment.getCourseGroupManager();
		boolean adminLazy = identityEnvironment.getRoles().isAdministrator()
				|| identityEnvironment.getRoles().isLearnResourceManager()
				|| cgm.isIdentityAnyCourseAdministrator(identityEnvironment.getIdentity());
		adminAnyCourse = Boolean.valueOf(adminLazy);
		return adminLazy;
	}

	@Override
	public boolean isCoachOfAnyCourse() {
		if(coachAnyCourse != null) {
			return coachAnyCourse.booleanValue();
		}

		CourseGroupManager cgm = courseEnvironment.getCourseGroupManager();
		boolean coachLazy = cgm.isIdentityAnyCourseCoach(identityEnvironment.getIdentity());
		coachAnyCourse = Boolean.valueOf(coachLazy);
		return coachLazy;
	}

	@Override
	public boolean isParticipantOfAnyCourse() {
		if(participantAnyCourse != null) {
			return participantAnyCourse.booleanValue();
		}

		CourseGroupManager cgm = courseEnvironment.getCourseGroupManager();
		boolean participantLazy = cgm.isIdentityAnyCourseParticipant(identityEnvironment.getIdentity());
		participantAnyCourse = Boolean.valueOf(participantLazy);
		return participantLazy;
	}

	@Override
	public RepositoryEntryLifecycle getLifecycle() {
		if(lifecycle == null) {
			RepositoryEntry re = getCourseRepositoryEntry();
			if(re != null) {
				lifecycle = re.getLifecycle();
			}
		}
		return lifecycle;
	}

	public RepositoryEntry getCourseRepositoryEntry() {
		if(courseRepoEntry == null) {
			courseRepoEntry = courseEnvironment.getCourseGroupManager().getCourseEntry();
		}
		return courseRepoEntry;
	}

	@Override
	public List<BusinessGroup> getCoachedGroups() {
		if(coachedGroups == null) {
			return Collections.emptyList();
		}
		return coachedGroups;
	}

	@Override
	public List<BusinessGroup> getParticipatingGroups() {
		if(participatingGroups == null) {
			return Collections.emptyList();
		}
		return participatingGroups;
	}

	@Override
	public List<BusinessGroup> getWaitingLists() {
		if(waitingLists == null) {
			return Collections.emptyList();
		}
		return waitingLists;
	}
	
	/**
	 * The curriculum elements are lazy loaded.
	 */
	@Override
	public List<CurriculumElement> getCoachedCurriculumElements() {
		if(coachedCurriculums == null) {
			coachedCurriculums = courseEnvironment.getCourseGroupManager().getCoachedCurriculumElements(identityEnvironment.getIdentity());
		}
		return coachedCurriculums;
	}

	@Override
	public boolean isCourseReadOnly() {
		return (courseReadOnlyByStatus != null && courseReadOnlyByStatus.booleanValue())
				|| (courseReadOnlyByRole != null && courseReadOnlyByRole.booleanValue());
	}
	
	@Override
	public CourseReadOnlyDetails getCourseReadOnlyDetails() {
		return new CourseReadOnlyDetails(courseReadOnlyByStatus, courseReadOnlyByRole);
	}

	public Boolean getCourseReadOnlyByStatus() {
		return courseReadOnlyByStatus;
	}
	
	public Boolean getCourseReadOnlyByRole() {
		return courseReadOnlyByRole;
	}
	
	public void setCourseReadOnlyByStatus(Boolean readOnly) {
		courseReadOnlyByStatus = readOnly;
	}
	
	public void setCourseReadOnlyByRole(Boolean readOnly) {
		courseReadOnlyByRole = readOnly;
	}
	
	@Override
	public boolean hasEfficiencyStatementOrCertificate(boolean update) {
		if(certification == null || update) {
			EfficiencyStatementManager efficiencyStatementManager = CoreSpringFactory.getImpl(EfficiencyStatementManager.class);
			boolean hasStatement = efficiencyStatementManager
					.hasUserEfficiencyStatement(getCourseRepositoryEntry().getKey(), identityEnvironment.getIdentity());
			if(hasStatement) {
				certification = Boolean.TRUE;
			} else {
				CertificatesManager certificatesManager = CoreSpringFactory.getImpl(CertificatesManager.class);
				certification = certificatesManager
						.hasCertificate(identityEnvironment.getIdentity(), getCourseRepositoryEntry().getOlatResource().getKey());
			} 
		}
		return certification.booleanValue();
	}

	@Override
	public List<String> getUsernames() {
		if(usernames != null) return usernames;
		
		Set<String> nameSet = new HashSet<>();
		if(identityEnvironment != null && identityEnvironment.getIdentity() != null) {
			Identity identity = identityEnvironment.getIdentity();
			nameSet.add(identity.getName());
			if(StringHelper.containsNonWhitespace(identity.getUser().getNickName())) {
				nameSet.add(identity.getUser().getNickName());
			}
			
			List<Authentication> authentications = CoreSpringFactory.getImpl(BaseSecurity.class).getAuthentications(identity);
			for(Authentication authentication:authentications) {
				nameSet.add(authentication.getAuthusername());
			}
		}
		
		usernames = List.copyOf(nameSet);
		return usernames;
	}

	public void setGroupMemberships(List<BusinessGroup> coachedGroups,
			List<BusinessGroup> participatingGroups, List<BusinessGroup> waitingLists) {
		this.coachedGroups = coachedGroups;
		this.participatingGroups = participatingGroups;
		this.waitingLists = waitingLists;
	}
	
	public void setUserRoles(boolean admin, boolean coach, boolean participant) {
		this.admin = Boolean.valueOf(admin);
		this.coach = Boolean.valueOf(coach);
		boolean participantChanged = isParticipant() != participant;
		this.participant = Boolean.valueOf(participant);
		if (participantChanged) {
			initScoreAccounting();
		}
		this.allUsersParticipant = null;
	}
}
