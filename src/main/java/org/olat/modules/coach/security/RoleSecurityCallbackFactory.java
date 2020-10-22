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
package org.olat.modules.coach.security;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.olat.basesecurity.RelationRoleToRight;
import org.olat.basesecurity.RightProvider;
import org.olat.course.certificate.CertificateEmailRightProvider;
import org.olat.course.groupsandrights.ViewCourseCalendarRightProvider;
import org.olat.course.groupsandrights.ViewEfficiencyStatementRightProvider;
import org.olat.modules.coach.RoleSecurityCallback;
import org.olat.modules.quality.QualityReportAccessRightProvider;

/* 
 * Initial date: 17 Jun 2020<br>
 * @author aboeckle, alexander.boeckle@frentix.com
 */
public class RoleSecurityCallbackFactory {
	/**
	 * Creates a security callback for the given relationRole
	 *
	 * @param relationRights Relation righs
	 * @return Securitycallback for given relation rights
	 */
	public static RoleSecurityCallback create(Set<RelationRoleToRight> relationRights) {
		List<String> roleRights = new ArrayList<>(relationRights.size());
		for (RelationRoleToRight right : relationRights) {
			roleRights.add(right.getRelationRight().getRight());
		}
		return new RoleSecurityCallbackImpl(roleRights);
	}

	/**
	 * Creates a security callback for given right providers
	 *
	 * @param rightProviders Right providers
	 * @return Securitycallback for given right providers
	 */
	public static RoleSecurityCallback create(List<RightProvider> rightProviders) {
		List<String> roleRights = new ArrayList<>(rightProviders.size());
		for (RightProvider rightProvider : rightProviders) {
			roleRights.add(rightProvider.getRight());
		}
		return new RoleSecurityCallbackImpl(roleRights);
	}
	
	/**
	 * Creates a security callback for given right providers
	 * 
	 * @param rightProviders An array of rights as strings
	 * @return A security callback
	 */
	public static RoleSecurityCallback createFromStringsList(List<String> rightProviders) {
		return new RoleSecurityCallbackImpl(rightProviders);
	}

	private static class RoleSecurityCallbackImpl implements RoleSecurityCallback {

		private final List<String> roleRights;
		
		public RoleSecurityCallbackImpl(List<String> roleRights) {
			this.roleRights = roleRights;
		}

		@Override
		public boolean canResetPassword() {
			return roleRights.contains(ResetPasswordRightProvider.RELATION_RIGHT);
		}

		@Override
		public boolean canViewEfficiencyStatements() {
			return roleRights.contains(ViewEfficiencyStatementRightProvider.RELATION_RIGHT);
		}

		@Override
		public boolean canViewCalendar() {
			return roleRights.contains(ViewCourseCalendarRightProvider.RELATION_RIGHT);
		}

		@Override
		public boolean canReceiveCertificatesMail() {
			return roleRights.contains(CertificateEmailRightProvider.RELATION_RIGHT);
		}

		@Override
		public boolean canContact() {
			return roleRights.contains(ContactFormRightProvider.RELATION_RIGHT);
		}

		@Override
		public boolean canViewCourseProgressAndStatus() {
			return roleRights.contains(CourseProgressAndStatusRightProvider.RELATION_RIGHT);
		}

		@Override
		public boolean canViewCoursesAndCurriculum() {
			return roleRights.contains(CoursesAndCurriculumRightProvider.RELATION_RIGHT);
		}

		@Override
		public boolean canViewLecturesAndAbsences() {
			return roleRights.contains(LecturesAndAbsencesRightProvider.RELATION_RIGHT);
		}

		@Override
		public boolean canViewQualityReport() {
			return roleRights.contains(QualityReportAccessRightProvider.RELATION_RIGHT);
		}

		@Override
		public boolean canViewResourcesAndBookings() {
			return roleRights.contains(ResourcesAndBookingsRightProvider.RELATION_RIGHT);
		}

		@Override
		public boolean canViewAndEditProfile() {
			return roleRights.contains(ViewAndEditProfileRightProvider.RELATION_RIGHT);
		}

		@Override
		public boolean canViewGroupMemberships() {
			return roleRights.contains(ViewGroupMembershipsRightProvider.RELATION_RIGHT);
		}

		@Override
		public boolean isAdministrativeUser() {
			return roleRights.contains(AdministrativePropertiesRightProvider.RELATION_RIGHT);
		}
	}
}
