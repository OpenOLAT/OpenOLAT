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

import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import org.olat.core.util.DateUtils;
import org.olat.modules.curriculum.CurriculumModule;
import org.olat.test.OlatTestCase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


public class ReportTimeUnitTest extends OlatTestCase {

	@Mock
	private CurriculumModule curriculumModule;
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);

		when(curriculumModule.getReportsAccountingFiscalYearStartDay()).thenReturn(1);
		when(curriculumModule.getReportsAccountingFiscalYearStartMonth()).thenReturn(7);
	}
	
	@Test
	public void fiscalYearStartWithReferenceDateJustBefore() {
		int fiscalYearStartDay = 1;
		int fiscalYearStartMonth = 2;
		
		LocalDate referenceLocalDate = LocalDate.of(2025, 1, 31);
		Date referenceDate = DateUtils.toDate(referenceLocalDate);
		
		Date resultDate = ReportTimeUnit.fiscalYearStart(referenceDate, fiscalYearStartDay, fiscalYearStartMonth);
		LocalDate resultLocalDate = DateUtils.toLocalDate(resultDate);

		Assert.assertEquals(LocalDate.of(2024, 2, 1), resultLocalDate);
	}
	
	@Test
	public void fiscalYearStartOnReferenceDate() {
		int fiscalYearStartDay = 1;
		int fiscalYearStartMonth = 2;

		LocalDate referenceLocalDate = LocalDate.of(2025, 2, 1);
		Date referenceDate = DateUtils.toDate(referenceLocalDate);

		Date resultDate = ReportTimeUnit.fiscalYearStart(referenceDate, fiscalYearStartDay, fiscalYearStartMonth);
		LocalDate resultLocalDate = DateUtils.toLocalDate(resultDate);

		Assert.assertEquals(LocalDate.of(2025, 2, 1), resultLocalDate);
	}
	
	@Test
	public void fiscalYearStartWithReferenceDateJustAfter() {
		int fiscalYearStartDay = 1;
		int fiscalYearStartMonth = 2;
		
		LocalDate referenceLocalDate = LocalDate.of(2025, 2, 2);
		Date referenceDate = DateUtils.toDate(referenceLocalDate);

		Date resultDate = ReportTimeUnit.fiscalYearStart(referenceDate, fiscalYearStartDay, fiscalYearStartMonth);
		LocalDate resultLocalDate = DateUtils.toLocalDate(resultDate);

		Assert.assertEquals(LocalDate.of(2025, 2, 1), resultLocalDate);
	}
	
	@Test
	public void lastFiscalYear() {
		ReportTimeUnit lastFiscalYear = ReportTimeUnit.lastFiscalYear;
		LocalDateTime referenceLocalDateTime = LocalDateTime.of(2025, 6, 30, 12, 00, 00);
		Date referenceDate = DateUtils.toDate(referenceLocalDateTime);
		
		Date fromDate = lastFiscalYear.fromDate(referenceDate, 0, curriculumModule);
		LocalDateTime fromLocalDateTime = DateUtils.toLocalDateTime(fromDate);

		Date toDate = lastFiscalYear.toDate(referenceDate, curriculumModule);
		LocalDateTime toLocalDateTime = DateUtils.toLocalDateTime(toDate);
		
		Assert.assertEquals(LocalDateTime.of(2023, 7, 1, 0, 0, 0), fromLocalDateTime);
		Assert.assertEquals(LocalDateTime.of(2024, 7, 1, 0, 0, 0), toLocalDateTime);		
	}

	@Test
	public void currentFiscalYear() {
		ReportTimeUnit currentFiscalYear = ReportTimeUnit.currentFiscalYear;
		LocalDateTime referenceLocalDateTime = LocalDateTime.of(2025, 6, 30, 12, 00, 00);
		Date referenceDate = DateUtils.toDate(referenceLocalDateTime);
		
		Date fromDate = currentFiscalYear.fromDate(referenceDate, 0, curriculumModule);
		LocalDateTime fromLocalDateTime = DateUtils.toLocalDateTime(fromDate);

		Date toDate = currentFiscalYear.toDate(referenceDate, curriculumModule);
		LocalDateTime toLocalDateTime = DateUtils.toLocalDateTime(toDate);
		
		Assert.assertEquals(LocalDateTime.of(2024, 7, 1, 0, 0, 0), fromLocalDateTime);
		Assert.assertEquals(referenceLocalDateTime, toLocalDateTime);		
	}
}