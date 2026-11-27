/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.modules.coach.reports;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Test;
import org.olat.modules.curriculum.reports.AccountingReportConfiguration;

/**
 * Which template is available in which context. No database, no Spring.
 *
 * Initial date: 27 Nov 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 */
public class ReportTemplatesAccessTest {

	private final ReportConfiguration bookings = new AccountingReportConfiguration();
	private final ReportConfiguration absences = new AbsencesReportConfiguration();
	private final ReportConfiguration certificates = new CertificatesReportConfiguration();
	private final ReportConfiguration openBookingOrders = new OpenBookingOrdersReportConfiguration();

	@Test
	public void bookingsCoursePlannerOwner() {
		ReportConfigurationAccessSecurityCallback secCallback = curriculumContext(false);
		when(secCallback.isCurriculumOwner()).thenReturn(true);
		Assert.assertTrue(bookings.hasAccess(secCallback));
	}

	@Test
	public void bookingsCoursePlannerRootManager() {
		ReportConfigurationAccessSecurityCallback secCallback = curriculumContext(true);
		when(secCallback.isCurriculumManager()).thenReturn(true);
		Assert.assertTrue(bookings.hasAccess(secCallback));
	}

	@Test
	public void bookingsCoursePlannerAdministratorAndPrincipal() {
		ReportConfigurationAccessSecurityCallback administrator = curriculumContext(false);
		when(administrator.isAdministrator()).thenReturn(true);
		Assert.assertTrue(bookings.hasAccess(administrator));

		ReportConfigurationAccessSecurityCallback principal = curriculumContext(false);
		when(principal.isPrincipal()).thenReturn(true);
		Assert.assertTrue(bookings.hasAccess(principal));
	}

	@Test
	public void bookingsCoursePlannerWithoutRole() {
		Assert.assertFalse(bookings.hasAccess(curriculumContext(false)));
	}

	@Test
	public void bookingsNotInCoaching() {
		ReportConfigurationAccessSecurityCallback secCallback = coachingContext();
		when(secCallback.isCurriculumOwner()).thenReturn(true);
		when(secCallback.isAdministrator()).thenReturn(true);
		Assert.assertFalse(bookings.hasAccess(secCallback));
	}

	@Test
	public void coachingTemplatesInCoaching() {
		ReportConfigurationAccessSecurityCallback secCallback = coachingContext();
		when(secCallback.isShowAbsencesReports()).thenReturn(true);
		when(secCallback.isShowInvoicesReports()).thenReturn(true);
		when(secCallback.isCourseCoach()).thenReturn(true);
		Assert.assertTrue(absences.hasAccess(secCallback));
		Assert.assertTrue(openBookingOrders.hasAccess(secCallback));
		Assert.assertTrue(certificates.hasAccess(secCallback));
	}

	@Test
	public void coachingTemplatesWithoutRights() {
		ReportConfigurationAccessSecurityCallback secCallback = coachingContext();
		Assert.assertFalse(absences.hasAccess(secCallback));
		Assert.assertFalse(openBookingOrders.hasAccess(secCallback));
		Assert.assertFalse(certificates.hasAccess(secCallback));
	}

	@Test
	public void coachingTemplatesNotInCoursePlanner() {
		ReportConfigurationAccessSecurityCallback secCallback = curriculumContext(true);
		when(secCallback.isShowAbsencesReports()).thenReturn(true);
		when(secCallback.isShowInvoicesReports()).thenReturn(true);
		when(secCallback.isCourseCoach()).thenReturn(true);
		when(secCallback.isAdministrator()).thenReturn(true);
		Assert.assertFalse(absences.hasAccess(secCallback));
		Assert.assertFalse(openBookingOrders.hasAccess(secCallback));
		Assert.assertFalse(certificates.hasAccess(secCallback));
	}

	private static ReportConfigurationAccessSecurityCallback coachingContext() {
		ReportConfigurationAccessSecurityCallback secCallback = mock(ReportConfigurationAccessSecurityCallback.class);
		when(secCallback.isCoachingContext()).thenReturn(true);
		return secCallback;
	}

	private static ReportConfigurationAccessSecurityCallback curriculumContext(boolean root) {
		ReportConfigurationAccessSecurityCallback secCallback = mock(ReportConfigurationAccessSecurityCallback.class);
		when(secCallback.isCurriculumContext()).thenReturn(true);
		when(secCallback.isCurriculumRootContext()).thenReturn(root);
		return secCallback;
	}
}
