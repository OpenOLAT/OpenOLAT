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
import org.olat.course.certificate.CertificateEmailRightProvider;
import org.olat.course.groupsandrights.ViewCourseCalendarRightProvider;
import org.olat.course.groupsandrights.ViewEfficiencyStatementRightProvider;
import org.olat.modules.coach.UserRelationSecurityCallback;
import org.olat.modules.quality.QualityReportAccessRightProvider;

/* 
 * Initial date: 17 Jun 2020<br>
 * @author aboeckle, alexander.boeckle@frentix.com
 */
public class UserRelationSecurityCallbackFactory {
	/**
	 * Creates a security callback for the given relationRole
	 *
	 * @param relationRights
	 * @return
	 */
	public static UserRelationSecurityCallback create(Set<RelationRoleToRight> relationRights) {
		return new UserRelationSecurityCallbackImpl(relationRights);
	}

	private static class UserRelationSecurityCallbackImpl implements UserRelationSecurityCallback {

		private final List<String> relationRoleRights;

		public UserRelationSecurityCallbackImpl(Set<RelationRoleToRight> relationRights) {
			relationRoleRights = new ArrayList<>();

			for (RelationRoleToRight relationRoleToRight : relationRights) {
				relationRoleRights.add(relationRoleToRight.getRelationRight().getRight());
			}
		}

		@Override
		public boolean canResetPassword() {
			return relationRoleRights.contains(ResetPasswordRightProvider.RELATION_RIGHT);
		}

		@Override
		public boolean canViewEfficiencyStatements() {
			return relationRoleRights.contains(ViewEfficiencyStatementRightProvider.RELATION_RIGHT);
		}

		@Override
		public boolean canViewCalendar() {
			return relationRoleRights.contains(ViewCourseCalendarRightProvider.RELATION_RIGHT);
		}

		@Override
		public boolean canReceiveCertificatesMail() {
			return relationRoleRights.contains(CertificateEmailRightProvider.RELATION_RIGHT);
		}

		@Override
		public boolean canContact() {
			return relationRoleRights.contains(ContactFormRightProvider.RELATION_RIGHT);
		}

		@Override
		public boolean canViewCourseProgressAndStatus() {
			return relationRoleRights.contains(CourseProgressAndStatusRightProvider.RELATION_RIGHT);
		}

		@Override
		public boolean canViewCoursesAndCurriculum() {
			return relationRoleRights.contains(CoursesAndCurriculumRightProvider.RELATION_RIGHT);
		}

		@Override
		public boolean canViewLecturesAndAbsences() {
			return relationRoleRights.contains(LecturesAndAbsencesRightProvider.RELATION_RIGHT);
		}

		@Override
		public boolean canViewQualityReport() {
			return relationRoleRights.contains(QualityReportAccessRightProvider.RELATION_RIGHT);
		}

		@Override
		public boolean canViewResourcesAndBookings() {
			return relationRoleRights.contains(ResourcesAndBookingsRightProvider.RELATION_RIGHT);
		}

		@Override
		public boolean canViewAndEditProfile() {
			return relationRoleRights.contains(ViewAndEditProfileRightProvider.RELATION_RIGHT);
		}

		@Override
		public boolean canViewGroupMemberships() {
			return relationRoleRights.contains(ViewGroupMembershipsRightProvider.RELATION_RIGHT);
		}

		@Override
		public boolean isAdministrativeUser() {
			return relationRoleRights.contains(AdministrativePropertiesRightProvider.RELATION_RIGHT);
		}
	}
}
