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
package org.olat.basesecurity;

import java.util.Collection;
import java.util.Collections;
import java.util.Locale;

/**
 * 
 * Initial date: 19 Feb 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface RightProvider {

	Collection<OrganisationRoles> defaultRoles = Collections.singletonList(OrganisationRoles.linemanager);

	String getRight();
	RightProvider getParent();

	boolean isUserRelationsRight();
	int getUserRelationsPosition();

	default Collection<OrganisationRoles> getOrganisationRoles() {
		return defaultRoles;
	};

	int getOrganisationPosition();

	String getTranslatedName(Locale locale);

	enum UserRelationRightsOrder {
		CourseAndCurriculumRight,
		CourseProgressAndStatusRight,
		LecturesAndAbsencesRight,
		ViewEfficiencyStatementRight,
		CertificateUploadExternalRight,
		ViewCourseCalendarRight,
		ResourceAndBookinsRight,
		ViewGroupMemebershipsRight,
		ViewAndEditProfileRight,
		ResetPasswordRight,
		ContactFormRight,
		QualityReportAccessRight,
		CertificateEmailRight,
		AdministrativePropertiesRight
	}

	enum OrganisationRightsOrder {
		CourseAndCurriculumRight,
		CourseProgressAndStatusRight,
		LecturesAndAbsencesRight,
		ViewEfficiencyStatementRight,
		CertificateUploadExternalRight,
		ViewCourseCalendarRight,
		ResourceAndBookinsRight,
		ViewGroupMemebershipsRight,
		ViewAndEditProfileRight,
		ResetPasswordRight,
		ContactFormRight,
		AdministrativePropertiesRight
	}
}
