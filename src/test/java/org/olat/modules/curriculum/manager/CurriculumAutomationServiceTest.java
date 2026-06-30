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
package org.olat.modules.curriculum.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.components.date.OffsetDirection;
import org.olat.core.util.DateUtils;
import org.olat.modules.curriculum.AutomationContext;
import org.olat.modules.curriculum.AutomationDependingOn;
import org.olat.modules.curriculum.AutomationType;
import org.olat.modules.curriculum.AutomationUnit;
import org.olat.modules.curriculum.CurriculumAutomationConfig;
import org.olat.modules.curriculum.CurriculumAutomationRule;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;

/**
 * Initial date: 2026-06-26<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 */
public class CurriculumAutomationServiceTest {

	@Mock
	private DB dbInstance;
	@Mock
	private CurriculumService curriculumService;
	@Mock
	private RepositoryManager repositoryManager;
	@Mock
	private RepositoryService repositoryService;

	@InjectMocks
	private CurriculumAutomationServiceImpl sut;

	@Before
	public void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void testExecutionPeriodBefore_fires() {
		Date beginDate = DateUtils.addDays(new Date(), -10);
		CurriculumElement element = mock(CurriculumElement.class);
		when(element.getBeginDate()).thenReturn(beginDate);
		when(element.getElementStatus()).thenReturn(CurriculumElementStatus.active);

		CurriculumAutomationRule rule = new CurriculumAutomationRule();
		rule.setEnabled(true);
		rule.setDependingOn(AutomationDependingOn.EXECUTION_PERIOD);
		rule.setDirection(OffsetDirection.BEFORE);
		rule.setValue(5);
		rule.setUnit(AutomationUnit.DAYS);
		rule.setContext(AutomationContext.ELEMENT);
		rule.setAutomationType(AutomationType.STATUS_CHANGE);
		rule.setTargetStatus(CurriculumElementStatus.finished);

		Date today = DateUtils.getStartOfDay(new Date());
		sut.processRule(element, rule, today);

		verify(curriculumService).updateCurriculumElementStatus(eq(null), eq(element),
				eq(CurriculumElementStatus.finished), eq(false), eq(null));
	}

	@Test
	public void testExecutionPeriodBefore_notYet() {
		Date beginDate = DateUtils.addDays(new Date(), 20);
		CurriculumElement element = mock(CurriculumElement.class);
		when(element.getBeginDate()).thenReturn(beginDate);
		when(element.getElementStatus()).thenReturn(CurriculumElementStatus.active);

		CurriculumAutomationRule rule = new CurriculumAutomationRule();
		rule.setEnabled(true);
		rule.setDependingOn(AutomationDependingOn.EXECUTION_PERIOD);
		rule.setDirection(OffsetDirection.BEFORE);
		rule.setValue(5);
		rule.setUnit(AutomationUnit.DAYS);
		rule.setContext(AutomationContext.ELEMENT);
		rule.setAutomationType(AutomationType.STATUS_CHANGE);
		rule.setTargetStatus(CurriculumElementStatus.finished);

		Date today = DateUtils.getStartOfDay(new Date());
		sut.processRule(element, rule, today);

		verify(curriculumService, never()).updateCurriculumElementStatus(any(), any(), any(), eq(false), any());
	}

	@Test
	public void testStatusTrigger_fires() {
		CurriculumElement element = mock(CurriculumElement.class);
		when(element.getElementStatus()).thenReturn(CurriculumElementStatus.active);

		CurriculumAutomationRule rule = new CurriculumAutomationRule();
		rule.setEnabled(true);
		rule.setDependingOn(AutomationDependingOn.STATUS);
		rule.setDependingOnStatus(Set.of(CurriculumElementStatus.active.name()));
		rule.setContext(AutomationContext.ELEMENT);
		rule.setAutomationType(AutomationType.STATUS_CHANGE);
		rule.setTargetStatus(CurriculumElementStatus.finished);

		Date today = DateUtils.getStartOfDay(new Date());
		sut.processRule(element, rule, today);

		verify(curriculumService).updateCurriculumElementStatus(eq(null), eq(element),
				eq(CurriculumElementStatus.finished), eq(false), eq(null));
	}

	@Test
	public void testStatusTrigger_notMatch() {
		CurriculumElement element = mock(CurriculumElement.class);
		when(element.getElementStatus()).thenReturn(CurriculumElementStatus.confirmed);

		CurriculumAutomationRule rule = new CurriculumAutomationRule();
		rule.setEnabled(true);
		rule.setDependingOn(AutomationDependingOn.STATUS);
		rule.setDependingOnStatus(Set.of(CurriculumElementStatus.active.name()));
		rule.setContext(AutomationContext.ELEMENT);
		rule.setAutomationType(AutomationType.STATUS_CHANGE);
		rule.setTargetStatus(CurriculumElementStatus.finished);

		Date today = DateUtils.getStartOfDay(new Date());
		sut.processRule(element, rule, today);

		verify(curriculumService, never()).updateCurriculumElementStatus(any(), any(), any(), eq(false), any());
	}

	@Test
	public void testOnlyWhenStatus_blocks() {
		Date beginDate = DateUtils.addDays(new Date(), -10);
		CurriculumElement element = mock(CurriculumElement.class);
		when(element.getBeginDate()).thenReturn(beginDate);
		when(element.getElementStatus()).thenReturn(CurriculumElementStatus.confirmed);

		CurriculumAutomationRule rule = new CurriculumAutomationRule();
		rule.setEnabled(true);
		rule.setDependingOn(AutomationDependingOn.EXECUTION_PERIOD);
		rule.setDirection(OffsetDirection.BEFORE);
		rule.setValue(5);
		rule.setUnit(AutomationUnit.DAYS);
		rule.setOnlyWhenStatus(Set.of(CurriculumElementStatus.active.name()));
		rule.setContext(AutomationContext.ELEMENT);
		rule.setAutomationType(AutomationType.STATUS_CHANGE);
		rule.setTargetStatus(CurriculumElementStatus.finished);

		Date today = DateUtils.getStartOfDay(new Date());
		sut.processRule(element, rule, today);

		verify(curriculumService, never()).updateCurriculumElementStatus(any(), any(), any(), eq(false), any());
	}

	@Test
	public void testIdempotentElementStatus() {
		CurriculumElement element = mock(CurriculumElement.class);
		when(element.getElementStatus()).thenReturn(CurriculumElementStatus.finished);

		CurriculumAutomationRule rule = new CurriculumAutomationRule();
		rule.setEnabled(true);
		rule.setDependingOn(AutomationDependingOn.STATUS);
		rule.setDependingOnStatus(Set.of(CurriculumElementStatus.finished.name()));
		rule.setContext(AutomationContext.ELEMENT);
		rule.setAutomationType(AutomationType.STATUS_CHANGE);
		rule.setTargetStatus(CurriculumElementStatus.finished);

		Date today = DateUtils.getStartOfDay(new Date());
		sut.processRule(element, rule, today);

		verify(curriculumService, never()).updateCurriculumElementStatus(any(), any(), any(), eq(false), any());
	}

	@Test
	public void testContentInstantiation_skippedWhenHasEntries() {
		CurriculumElement element = mock(CurriculumElement.class);
		when(element.getElementStatus()).thenReturn(CurriculumElementStatus.active);
		when(curriculumService.hasRepositoryEntries(element)).thenReturn(true);

		CurriculumAutomationRule rule = new CurriculumAutomationRule();
		rule.setEnabled(true);
		rule.setDependingOn(AutomationDependingOn.STATUS);
		rule.setDependingOnStatus(Set.of(CurriculumElementStatus.active.name()));
		rule.setContext(AutomationContext.CONTENT);
		rule.setAutomationType(AutomationType.INSTANTIATION);

		Date today = DateUtils.getStartOfDay(new Date());
		sut.processRule(element, rule, today);

		verify(curriculumService, never()).instantiateTemplate(any(), any(), any(), any(), any(), any(), any());
	}

	@Test
	public void testComputeTriggerDate_beforeDays() {
		Date beginDate = new Date(0);
		CurriculumElement element = mock(CurriculumElement.class);
		when(element.getBeginDate()).thenReturn(beginDate);

		CurriculumAutomationRule rule = new CurriculumAutomationRule();
		rule.setDirection(OffsetDirection.BEFORE);
		rule.setValue(7);
		rule.setUnit(AutomationUnit.DAYS);

		Date result = sut.computeTriggerDate(element, rule);

		Date expected = DateUtils.addDays(DateUtils.getStartOfDay(beginDate), -7);
		assertThat(result).isEqualTo(expected);
	}

	@Test
	public void testComputeTriggerDate_afterDays() {
		Date endDate = new Date(0);
		CurriculumElement element = mock(CurriculumElement.class);
		when(element.getBeginDate()).thenReturn(null);
		when(element.getEndDate()).thenReturn(endDate);

		CurriculumAutomationRule rule = new CurriculumAutomationRule();
		rule.setDirection(OffsetDirection.AFTER);
		rule.setValue(3);
		rule.setUnit(AutomationUnit.DAYS);

		Date result = sut.computeTriggerDate(element, rule);

		Date expected = DateUtils.addDays(DateUtils.getStartOfDay(endDate), 3);
		assertThat(result).isEqualTo(expected);
	}

	@Test
	public void testComputeTriggerDate_noReferenceDate_returnsNull() {
		CurriculumElement element = mock(CurriculumElement.class);
		when(element.getBeginDate()).thenReturn(null);
		when(curriculumService.getCurriculumElementParentLine(element)).thenReturn(List.of());

		CurriculumAutomationRule rule = new CurriculumAutomationRule();
		rule.setDirection(OffsetDirection.BEFORE);
		rule.setValue(3);
		rule.setUnit(AutomationUnit.DAYS);

		Date result = sut.computeTriggerDate(element, rule);

		assertThat(result).isNull();
	}

	@Test
	public void testComputeTriggerDate_sameDay_returnsReferenceDate() {
		Date beginDate = new Date(0);
		CurriculumElement element = mock(CurriculumElement.class);
		when(element.getBeginDate()).thenReturn(beginDate);

		CurriculumAutomationRule rule = new CurriculumAutomationRule();
		rule.setDirection(OffsetDirection.BEFORE);
		rule.setUnit(AutomationUnit.SAME_DAY);

		Date result = sut.computeTriggerDate(element, rule);

		assertThat(result).isEqualTo(DateUtils.getStartOfDay(beginDate));
	}

	@Test
	public void testComputeTriggerDate_nullValue_returnsReferenceDate() {
		Date beginDate = new Date(0);
		CurriculumElement element = mock(CurriculumElement.class);
		when(element.getBeginDate()).thenReturn(beginDate);

		CurriculumAutomationRule rule = new CurriculumAutomationRule();
		rule.setDirection(OffsetDirection.BEFORE);
		rule.setUnit(AutomationUnit.DAYS);
		rule.setValue(null);

		Date result = sut.computeTriggerDate(element, rule);

		assertThat(result).isEqualTo(DateUtils.getStartOfDay(beginDate));
	}

	@Test
	public void testComputeTriggerDate_weeks() {
		Date beginDate = new Date(0);
		CurriculumElement element = mock(CurriculumElement.class);
		when(element.getBeginDate()).thenReturn(beginDate);

		CurriculumAutomationRule rule = new CurriculumAutomationRule();
		rule.setDirection(OffsetDirection.BEFORE);
		rule.setValue(2);
		rule.setUnit(AutomationUnit.WEEKS);

		Date result = sut.computeTriggerDate(element, rule);

		assertThat(result).isEqualTo(DateUtils.addDays(DateUtils.getStartOfDay(beginDate), -14));
	}

	@Test
	public void testComputeTriggerDate_months() {
		Date beginDate = new Date(0);
		CurriculumElement element = mock(CurriculumElement.class);
		when(element.getBeginDate()).thenReturn(beginDate);

		CurriculumAutomationRule rule = new CurriculumAutomationRule();
		rule.setDirection(OffsetDirection.BEFORE);
		rule.setValue(1);
		rule.setUnit(AutomationUnit.MONTHS);

		Date result = sut.computeTriggerDate(element, rule);

		Calendar cal = Calendar.getInstance();
		cal.setTime(DateUtils.getStartOfDay(beginDate));
		cal.add(Calendar.MONTH, -1);
		assertThat(result).isEqualTo(cal.getTime());
	}

	@Test
	public void testComputeTriggerDate_years() {
		Date endDate = new Date(0);
		CurriculumElement element = mock(CurriculumElement.class);
		when(element.getEndDate()).thenReturn(endDate);

		CurriculumAutomationRule rule = new CurriculumAutomationRule();
		rule.setDirection(OffsetDirection.AFTER);
		rule.setValue(1);
		rule.setUnit(AutomationUnit.YEARS);

		Date result = sut.computeTriggerDate(element, rule);

		Calendar cal = Calendar.getInstance();
		cal.setTime(DateUtils.getStartOfDay(endDate));
		cal.add(Calendar.YEAR, 1);
		assertThat(result).isEqualTo(cal.getTime());
	}

	@Test
	public void testComputeTriggerDate_beginFromParentLine() {
		Date parentBeginDate = new Date(0);
		CurriculumElement element = mock(CurriculumElement.class);
		when(element.getBeginDate()).thenReturn(null);
		CurriculumElement parent = mock(CurriculumElement.class);
		when(parent.getBeginDate()).thenReturn(parentBeginDate);
		when(curriculumService.getCurriculumElementParentLine(element)).thenReturn(List.of(parent));

		CurriculumAutomationRule rule = new CurriculumAutomationRule();
		rule.setDirection(OffsetDirection.BEFORE);
		rule.setUnit(AutomationUnit.SAME_DAY);

		Date result = sut.computeTriggerDate(element, rule);

		assertThat(result).isEqualTo(DateUtils.getStartOfDay(parentBeginDate));
	}

	@Test
	public void testComputeTriggerDate_endFromParentLine() {
		Date parentEndDate = new Date(0);
		CurriculumElement element = mock(CurriculumElement.class);
		when(element.getEndDate()).thenReturn(null);
		CurriculumElement parent = mock(CurriculumElement.class);
		when(parent.getEndDate()).thenReturn(parentEndDate);
		when(curriculumService.getCurriculumElementParentLine(element)).thenReturn(List.of(parent));

		CurriculumAutomationRule rule = new CurriculumAutomationRule();
		rule.setDirection(OffsetDirection.AFTER);
		rule.setUnit(AutomationUnit.SAME_DAY);

		Date result = sut.computeTriggerDate(element, rule);

		assertThat(result).isEqualTo(DateUtils.getStartOfDay(parentEndDate));
	}

	@Test
	public void testImplementationContext_statusChange_fires() {
		CurriculumElement element = mock(CurriculumElement.class);
		when(element.getElementStatus()).thenReturn(CurriculumElementStatus.confirmed);

		CurriculumAutomationRule rule = new CurriculumAutomationRule();
		rule.setEnabled(true);
		rule.setDependingOn(AutomationDependingOn.STATUS);
		rule.setDependingOnStatus(Set.of(CurriculumElementStatus.confirmed.name()));
		rule.setContext(AutomationContext.IMPLEMENTATION);
		rule.setAutomationType(AutomationType.STATUS_CHANGE);
		rule.setTargetStatus(CurriculumElementStatus.finished);

		sut.processRule(element, rule, DateUtils.getStartOfDay(new Date()));

		verify(curriculumService).updateCurriculumElementStatus(eq(null), eq(element),
				eq(CurriculumElementStatus.finished), eq(false), eq(null));
	}

	@Test
	public void testStatusTrigger_emptyDependingOnStatus_skips() {
		CurriculumElement element = mock(CurriculumElement.class);
		when(element.getElementStatus()).thenReturn(CurriculumElementStatus.active);

		CurriculumAutomationRule rule = new CurriculumAutomationRule();
		rule.setEnabled(true);
		rule.setDependingOn(AutomationDependingOn.STATUS);
		rule.setDependingOnStatus(Set.of());
		rule.setContext(AutomationContext.ELEMENT);
		rule.setAutomationType(AutomationType.STATUS_CHANGE);
		rule.setTargetStatus(CurriculumElementStatus.finished);

		sut.processRule(element, rule, DateUtils.getStartOfDay(new Date()));

		verify(curriculumService, never()).updateCurriculumElementStatus(any(), any(), any(), anyBoolean(), any());
	}

	@Test
	public void testExecutionPeriod_nullReferenceDate_skips() {
		CurriculumElement element = mock(CurriculumElement.class);
		when(element.getBeginDate()).thenReturn(null);
		when(curriculumService.getCurriculumElementParentLine(element)).thenReturn(List.of());

		CurriculumAutomationRule rule = new CurriculumAutomationRule();
		rule.setEnabled(true);
		rule.setDependingOn(AutomationDependingOn.EXECUTION_PERIOD);
		rule.setDirection(OffsetDirection.BEFORE);
		rule.setValue(5);
		rule.setUnit(AutomationUnit.DAYS);
		rule.setContext(AutomationContext.ELEMENT);
		rule.setAutomationType(AutomationType.STATUS_CHANGE);
		rule.setTargetStatus(CurriculumElementStatus.finished);

		sut.processRule(element, rule, DateUtils.getStartOfDay(new Date()));

		verify(curriculumService, never()).updateCurriculumElementStatus(any(), any(), any(), anyBoolean(), any());
	}

	@Test
	public void testElementStatusChange_nullTargetStatus_skips() {
		CurriculumElement element = mock(CurriculumElement.class);
		when(element.getElementStatus()).thenReturn(CurriculumElementStatus.active);

		CurriculumAutomationRule rule = new CurriculumAutomationRule();
		rule.setEnabled(true);
		rule.setDependingOn(AutomationDependingOn.STATUS);
		rule.setDependingOnStatus(Set.of(CurriculumElementStatus.active.name()));
		rule.setContext(AutomationContext.ELEMENT);
		rule.setAutomationType(AutomationType.STATUS_CHANGE);
		rule.setTargetStatus((String) null);

		sut.processRule(element, rule, DateUtils.getStartOfDay(new Date()));

		verify(curriculumService, never()).updateCurriculumElementStatus(any(), any(), any(), anyBoolean(), any());
	}

	@Test
	public void testContentInstantiation_fires() {
		CurriculumElementType type = mock(CurriculumElementType.class);
		when(type.getMaxRepositoryEntryRelations()).thenReturn(-1);
		CurriculumElement element = mock(CurriculumElement.class);
		when(element.getElementStatus()).thenReturn(CurriculumElementStatus.active);
		when(element.getType()).thenReturn(type);
		when(curriculumService.countRepositoryEntries(element)).thenReturn(0L);
		RepositoryEntry template = mock(RepositoryEntry.class);
		when(curriculumService.getRepositoryTemplates(element)).thenReturn(List.of(template));

		CurriculumAutomationRule rule = new CurriculumAutomationRule();
		rule.setEnabled(true);
		rule.setDependingOn(AutomationDependingOn.STATUS);
		rule.setDependingOnStatus(Set.of(CurriculumElementStatus.active.name()));
		rule.setContext(AutomationContext.CONTENT);
		rule.setAutomationType(AutomationType.INSTANTIATION);

		sut.processRule(element, rule, DateUtils.getStartOfDay(new Date()));

		verify(curriculumService).instantiateTemplate(eq(template), eq(element), any(), any(), any(), any(), any());
	}

	@Test
	public void testContentInstantiation_maxOne_twoTemplates_instantiatesOnce() {
		CurriculumElementType type = mock(CurriculumElementType.class);
		when(type.getMaxRepositoryEntryRelations()).thenReturn(1);
		CurriculumElement element = mock(CurriculumElement.class);
		when(element.getElementStatus()).thenReturn(CurriculumElementStatus.active);
		when(element.getType()).thenReturn(type);
		when(curriculumService.countRepositoryEntries(element)).thenReturn(0L);
		RepositoryEntry template1 = mock(RepositoryEntry.class);
		RepositoryEntry template2 = mock(RepositoryEntry.class);
		when(curriculumService.getRepositoryTemplates(element)).thenReturn(List.of(template1, template2));

		CurriculumAutomationRule rule = new CurriculumAutomationRule();
		rule.setEnabled(true);
		rule.setDependingOn(AutomationDependingOn.STATUS);
		rule.setDependingOnStatus(Set.of(CurriculumElementStatus.active.name()));
		rule.setContext(AutomationContext.CONTENT);
		rule.setAutomationType(AutomationType.INSTANTIATION);

		sut.processRule(element, rule, DateUtils.getStartOfDay(new Date()));

		verify(curriculumService).instantiateTemplate(eq(template1), eq(element), any(), any(), any(), any(), any());
		verify(curriculumService, never()).instantiateTemplate(eq(template2), eq(element), any(), any(), any(), any(), any());
	}

	@Test
	public void testContentInstantiation_maxOne_alreadyFull_skips() {
		CurriculumElementType type = mock(CurriculumElementType.class);
		when(type.getMaxRepositoryEntryRelations()).thenReturn(1);
		CurriculumElement element = mock(CurriculumElement.class);
		when(element.getElementStatus()).thenReturn(CurriculumElementStatus.active);
		when(element.getType()).thenReturn(type);
		when(curriculumService.countRepositoryEntries(element)).thenReturn(1L);

		CurriculumAutomationRule rule = new CurriculumAutomationRule();
		rule.setEnabled(true);
		rule.setDependingOn(AutomationDependingOn.STATUS);
		rule.setDependingOnStatus(Set.of(CurriculumElementStatus.active.name()));
		rule.setContext(AutomationContext.CONTENT);
		rule.setAutomationType(AutomationType.INSTANTIATION);

		sut.processRule(element, rule, DateUtils.getStartOfDay(new Date()));

		verify(curriculumService, never()).instantiateTemplate(any(), any(), any(), any(), any(), any(), any());
	}

	@Test
	public void testContentInstantiation_maxTwo_oneExisting_twoTemplates_instantiatesOne() {
		CurriculumElementType type = mock(CurriculumElementType.class);
		when(type.getMaxRepositoryEntryRelations()).thenReturn(2);
		CurriculumElement element = mock(CurriculumElement.class);
		when(element.getElementStatus()).thenReturn(CurriculumElementStatus.active);
		when(element.getType()).thenReturn(type);
		when(curriculumService.countRepositoryEntries(element)).thenReturn(1L);
		RepositoryEntry template1 = mock(RepositoryEntry.class);
		RepositoryEntry template2 = mock(RepositoryEntry.class);
		when(curriculumService.getRepositoryTemplates(element)).thenReturn(List.of(template1, template2));

		CurriculumAutomationRule rule = new CurriculumAutomationRule();
		rule.setEnabled(true);
		rule.setDependingOn(AutomationDependingOn.STATUS);
		rule.setDependingOnStatus(Set.of(CurriculumElementStatus.active.name()));
		rule.setContext(AutomationContext.CONTENT);
		rule.setAutomationType(AutomationType.INSTANTIATION);

		sut.processRule(element, rule, DateUtils.getStartOfDay(new Date()));

		verify(curriculumService).instantiateTemplate(eq(template1), eq(element), any(), any(), any(), any(), any());
		verify(curriculumService, never()).instantiateTemplate(eq(template2), eq(element), any(), any(), any(), any(), any());
	}

	@Test
	public void testContentInstantiation_unlimited_twoTemplates_instantiatesBoth() {
		CurriculumElementType type = mock(CurriculumElementType.class);
		when(type.getMaxRepositoryEntryRelations()).thenReturn(-1);
		CurriculumElement element = mock(CurriculumElement.class);
		when(element.getElementStatus()).thenReturn(CurriculumElementStatus.active);
		when(element.getType()).thenReturn(type);
		when(curriculumService.countRepositoryEntries(element)).thenReturn(0L);
		RepositoryEntry template1 = mock(RepositoryEntry.class);
		RepositoryEntry template2 = mock(RepositoryEntry.class);
		when(curriculumService.getRepositoryTemplates(element)).thenReturn(List.of(template1, template2));

		CurriculumAutomationRule rule = new CurriculumAutomationRule();
		rule.setEnabled(true);
		rule.setDependingOn(AutomationDependingOn.STATUS);
		rule.setDependingOnStatus(Set.of(CurriculumElementStatus.active.name()));
		rule.setContext(AutomationContext.CONTENT);
		rule.setAutomationType(AutomationType.INSTANTIATION);

		sut.processRule(element, rule, DateUtils.getStartOfDay(new Date()));

		verify(curriculumService).instantiateTemplate(eq(template1), eq(element), any(), any(), any(), any(), any());
		verify(curriculumService).instantiateTemplate(eq(template2), eq(element), any(), any(), any(), any(), any());
	}

	@Test
	public void testContentStatusChange_nullTargetStatus_skips() {
		CurriculumElement element = mock(CurriculumElement.class);
		when(element.getElementStatus()).thenReturn(CurriculumElementStatus.active);

		CurriculumAutomationRule rule = new CurriculumAutomationRule();
		rule.setEnabled(true);
		rule.setDependingOn(AutomationDependingOn.STATUS);
		rule.setDependingOnStatus(Set.of(CurriculumElementStatus.active.name()));
		rule.setContext(AutomationContext.CONTENT);
		rule.setAutomationType(AutomationType.STATUS_CHANGE);
		rule.setTargetStatus((String) null);

		sut.processRule(element, rule, DateUtils.getStartOfDay(new Date()));

		verify(repositoryManager, never()).setStatus(any(), any());
		verify(repositoryService, never()).closeRepositoryEntry(any(), any(), anyBoolean());
	}

	@Test
	public void testContentStatusChange_alreadyAtTarget_skips() {
		CurriculumElement element = mock(CurriculumElement.class);
		when(element.getElementStatus()).thenReturn(CurriculumElementStatus.active);
		RepositoryEntry entry = mock(RepositoryEntry.class);
		when(entry.getEntryStatus()).thenReturn(RepositoryEntryStatusEnum.published);
		when(curriculumService.getRepositoryEntries(element)).thenReturn(List.of(entry));

		CurriculumAutomationRule rule = new CurriculumAutomationRule();
		rule.setEnabled(true);
		rule.setDependingOn(AutomationDependingOn.STATUS);
		rule.setDependingOnStatus(Set.of(CurriculumElementStatus.active.name()));
		rule.setContext(AutomationContext.CONTENT);
		rule.setAutomationType(AutomationType.STATUS_CHANGE);
		rule.setTargetStatus(RepositoryEntryStatusEnum.published);

		sut.processRule(element, rule, DateUtils.getStartOfDay(new Date()));

		verify(repositoryManager, never()).setStatus(any(), any());
		verify(repositoryService, never()).closeRepositoryEntry(any(), any(), anyBoolean());
	}

	@Test
	public void testContentStatusChange_closed_callsCloseEntry() {
		CurriculumElement element = mock(CurriculumElement.class);
		when(element.getElementStatus()).thenReturn(CurriculumElementStatus.active);
		RepositoryEntry entry = mock(RepositoryEntry.class);
		when(entry.getEntryStatus()).thenReturn(RepositoryEntryStatusEnum.published);
		when(curriculumService.getRepositoryEntries(element)).thenReturn(List.of(entry));

		CurriculumAutomationRule rule = new CurriculumAutomationRule();
		rule.setEnabled(true);
		rule.setDependingOn(AutomationDependingOn.STATUS);
		rule.setDependingOnStatus(Set.of(CurriculumElementStatus.active.name()));
		rule.setContext(AutomationContext.CONTENT);
		rule.setAutomationType(AutomationType.STATUS_CHANGE);
		rule.setTargetStatus(RepositoryEntryStatusEnum.closed);

		sut.processRule(element, rule, DateUtils.getStartOfDay(new Date()));

		verify(repositoryService).closeRepositoryEntry(eq(entry), eq(null), eq(true));
		verify(repositoryManager, never()).setStatus(any(), any());
	}

	@Test
	public void testContentStatusChange_otherStatus_callsSetStatus() {
		CurriculumElement element = mock(CurriculumElement.class);
		when(element.getElementStatus()).thenReturn(CurriculumElementStatus.active);
		RepositoryEntry entry = mock(RepositoryEntry.class);
		when(entry.getEntryStatus()).thenReturn(RepositoryEntryStatusEnum.preparation);
		when(curriculumService.getRepositoryEntries(element)).thenReturn(List.of(entry));

		CurriculumAutomationRule rule = new CurriculumAutomationRule();
		rule.setEnabled(true);
		rule.setDependingOn(AutomationDependingOn.STATUS);
		rule.setDependingOnStatus(Set.of(CurriculumElementStatus.active.name()));
		rule.setContext(AutomationContext.CONTENT);
		rule.setAutomationType(AutomationType.STATUS_CHANGE);
		rule.setTargetStatus(RepositoryEntryStatusEnum.published);

		sut.processRule(element, rule, DateUtils.getStartOfDay(new Date()));

		verify(repositoryManager).setStatus(eq(entry), eq(RepositoryEntryStatusEnum.published));
		verify(repositoryService, never()).closeRepositoryEntry(any(), any(), anyBoolean());
	}

	@Test
	public void testProcessElement_nullConfig_skips() {
		CurriculumElement element = mock(CurriculumElement.class);
		when(element.getType()).thenReturn(null);
		when(curriculumService.loadAutomationCandidates()).thenReturn(List.of(element));

		sut.execute();

		verify(curriculumService, never()).updateCurriculumElementStatus(any(), any(), any(), anyBoolean(), any());
	}

	@Test
	public void testComputeTriggerDate_referenceBegin_afterDirection() {
		Date beginDate = new Date(0);
		CurriculumElement element = mock(CurriculumElement.class);
		when(element.getBeginDate()).thenReturn(beginDate);

		CurriculumAutomationRule rule = new CurriculumAutomationRule();
		rule.setReference(CurriculumAutomationRule.REFERENCE_BEGIN);
		rule.setDirection(OffsetDirection.AFTER);
		rule.setValue(3);
		rule.setUnit(AutomationUnit.DAYS);

		Date result = sut.computeTriggerDate(element, rule);

		assertThat(result).isEqualTo(DateUtils.addDays(DateUtils.getStartOfDay(beginDate), 3));
	}

	@Test
	public void testComputeTriggerDate_referenceEnd_beforeDirection() {
		Date endDate = new Date(0);
		CurriculumElement element = mock(CurriculumElement.class);
		when(element.getEndDate()).thenReturn(endDate);

		CurriculumAutomationRule rule = new CurriculumAutomationRule();
		rule.setReference(CurriculumAutomationRule.REFERENCE_END);
		rule.setDirection(OffsetDirection.BEFORE);
		rule.setValue(5);
		rule.setUnit(AutomationUnit.DAYS);

		Date result = sut.computeTriggerDate(element, rule);

		assertThat(result).isEqualTo(DateUtils.addDays(DateUtils.getStartOfDay(endDate), -5));
	}

	@Test
	public void testComputeTriggerDate_referenceEndWinsOverBeginDate() {
		Date beginDate = new Date(0);
		Date endDate = DateUtils.addDays(new Date(0), 30);
		CurriculumElement element = mock(CurriculumElement.class);
		when(element.getBeginDate()).thenReturn(beginDate);
		when(element.getEndDate()).thenReturn(endDate);

		CurriculumAutomationRule rule = new CurriculumAutomationRule();
		rule.setReference(CurriculumAutomationRule.REFERENCE_END);
		rule.setDirection(OffsetDirection.AFTER);
		rule.setUnit(AutomationUnit.SAME_DAY);

		Date result = sut.computeTriggerDate(element, rule);

		assertThat(result).isEqualTo(DateUtils.getStartOfDay(endDate));
	}

	@Test
	public void testProcessRule_executionPeriodOnExactDay_fires() {
		Date today = DateUtils.getStartOfDay(new Date());
		CurriculumElement element = mock(CurriculumElement.class);
		when(element.getBeginDate()).thenReturn(today);
		when(element.getElementStatus()).thenReturn(CurriculumElementStatus.active);

		CurriculumAutomationRule rule = new CurriculumAutomationRule();
		rule.setEnabled(true);
		rule.setDependingOn(AutomationDependingOn.EXECUTION_PERIOD);
		rule.setReference(CurriculumAutomationRule.REFERENCE_BEGIN);
		rule.setDirection(OffsetDirection.BEFORE);
		rule.setUnit(AutomationUnit.SAME_DAY);
		rule.setContext(AutomationContext.ELEMENT);
		rule.setAutomationType(AutomationType.STATUS_CHANGE);
		rule.setTargetStatus(CurriculumElementStatus.finished);

		sut.processRule(element, rule, today);

		verify(curriculumService).updateCurriculumElementStatus(eq(null), eq(element),
				eq(CurriculumElementStatus.finished), eq(false), eq(null));
	}

	@Test
	public void testOnlyWhenStatus_matching_proceeds() {
		Date beginDate = DateUtils.addDays(new Date(), -10);
		CurriculumElement element = mock(CurriculumElement.class);
		when(element.getBeginDate()).thenReturn(beginDate);
		when(element.getElementStatus()).thenReturn(CurriculumElementStatus.active);

		CurriculumAutomationRule rule = new CurriculumAutomationRule();
		rule.setEnabled(true);
		rule.setDependingOn(AutomationDependingOn.EXECUTION_PERIOD);
		rule.setReference(CurriculumAutomationRule.REFERENCE_BEGIN);
		rule.setDirection(OffsetDirection.BEFORE);
		rule.setValue(5);
		rule.setUnit(AutomationUnit.DAYS);
		rule.setOnlyWhenStatus(Set.of(CurriculumElementStatus.active.name()));
		rule.setContext(AutomationContext.ELEMENT);
		rule.setAutomationType(AutomationType.STATUS_CHANGE);
		rule.setTargetStatus(CurriculumElementStatus.finished);

		sut.processRule(element, rule, DateUtils.getStartOfDay(new Date()));

		verify(curriculumService).updateCurriculumElementStatus(eq(null), eq(element),
				eq(CurriculumElementStatus.finished), eq(false), eq(null));
	}

	@Test
	public void testProcessRule_unmatchedContextTypeCombination_skips() {
		CurriculumElement element = mock(CurriculumElement.class);
		when(element.getElementStatus()).thenReturn(CurriculumElementStatus.active);

		CurriculumAutomationRule rule = new CurriculumAutomationRule();
		rule.setEnabled(true);
		rule.setDependingOn(AutomationDependingOn.STATUS);
		rule.setDependingOnStatus(Set.of(CurriculumElementStatus.active.name()));
		rule.setContext(AutomationContext.IMPLEMENTATION);
		rule.setAutomationType(AutomationType.INSTANTIATION);

		sut.processRule(element, rule, DateUtils.getStartOfDay(new Date()));

		verify(curriculumService, never()).updateCurriculumElementStatus(any(), any(), any(), anyBoolean(), any());
		verify(curriculumService, never()).instantiateTemplate(any(), any(), any(), any(), any(), any(), any());
	}

	@Test
	public void testContentInstantiation_emptyTemplateList_noInstantiate() {
		CurriculumElement element = mock(CurriculumElement.class);
		when(element.getElementStatus()).thenReturn(CurriculumElementStatus.active);
		when(curriculumService.hasRepositoryEntries(element)).thenReturn(false);
		when(curriculumService.getRepositoryTemplates(element)).thenReturn(List.of());

		CurriculumAutomationRule rule = new CurriculumAutomationRule();
		rule.setEnabled(true);
		rule.setDependingOn(AutomationDependingOn.STATUS);
		rule.setDependingOnStatus(Set.of(CurriculumElementStatus.active.name()));
		rule.setContext(AutomationContext.CONTENT);
		rule.setAutomationType(AutomationType.INSTANTIATION);

		sut.processRule(element, rule, DateUtils.getStartOfDay(new Date()));

		verify(curriculumService, never()).instantiateTemplate(any(), any(), any(), any(), any(), any(), any());
	}

	@Test
	public void testGetBeginDate_nearestParentWins() {
		Date grandParentDate = DateUtils.addDays(new Date(), -20);
		Date parentDate = DateUtils.addDays(new Date(), -10);
		CurriculumElement element = mock(CurriculumElement.class);
		when(element.getBeginDate()).thenReturn(null);
		CurriculumElement grandParent = mock(CurriculumElement.class);
		when(grandParent.getBeginDate()).thenReturn(grandParentDate);
		CurriculumElement parent = mock(CurriculumElement.class);
		when(parent.getBeginDate()).thenReturn(parentDate);
		when(curriculumService.getCurriculumElementParentLine(element)).thenReturn(List.of(grandParent, parent));

		CurriculumAutomationRule rule = new CurriculumAutomationRule();
		rule.setReference(CurriculumAutomationRule.REFERENCE_BEGIN);
		rule.setDirection(OffsetDirection.BEFORE);
		rule.setUnit(AutomationUnit.SAME_DAY);

		Date result = sut.computeTriggerDate(element, rule);

		assertThat(result).isEqualTo(DateUtils.getStartOfDay(parentDate));
	}

	@Test
	public void testGetEndDate_nearestParentWins() {
		Date grandParentDate = DateUtils.addDays(new Date(), 10);
		Date parentDate = DateUtils.addDays(new Date(), 20);
		CurriculumElement element = mock(CurriculumElement.class);
		when(element.getEndDate()).thenReturn(null);
		CurriculumElement grandParent = mock(CurriculumElement.class);
		when(grandParent.getEndDate()).thenReturn(grandParentDate);
		CurriculumElement parent = mock(CurriculumElement.class);
		when(parent.getEndDate()).thenReturn(parentDate);
		when(curriculumService.getCurriculumElementParentLine(element)).thenReturn(List.of(grandParent, parent));

		CurriculumAutomationRule rule = new CurriculumAutomationRule();
		rule.setReference(CurriculumAutomationRule.REFERENCE_END);
		rule.setDirection(OffsetDirection.AFTER);
		rule.setUnit(AutomationUnit.SAME_DAY);

		Date result = sut.computeTriggerDate(element, rule);

		assertThat(result).isEqualTo(DateUtils.getStartOfDay(parentDate));
	}

	@Test
	public void testExecute_firingRule_invokesActionAndCommits() {
		Date beginDate = DateUtils.addDays(new Date(), -10);
		CurriculumElement element = mock(CurriculumElement.class);
		when(element.getBeginDate()).thenReturn(beginDate);
		when(element.getElementStatus()).thenReturn(CurriculumElementStatus.active);
		when(curriculumService.loadAutomationCandidates()).thenReturn(List.of(element));

		CurriculumAutomationRule rule = new CurriculumAutomationRule();
		rule.setEnabled(true);
		rule.setDependingOn(AutomationDependingOn.EXECUTION_PERIOD);
		rule.setReference(CurriculumAutomationRule.REFERENCE_BEGIN);
		rule.setDirection(OffsetDirection.BEFORE);
		rule.setValue(5);
		rule.setUnit(AutomationUnit.DAYS);
		rule.setContext(AutomationContext.ELEMENT);
		rule.setAutomationType(AutomationType.STATUS_CHANGE);
		rule.setTargetStatus(CurriculumElementStatus.finished);
		CurriculumAutomationConfig config = new CurriculumAutomationConfig();
		config.addRule(rule);
		CurriculumElementType elementType = mock(CurriculumElementType.class);
		when(element.getType()).thenReturn(elementType);
		when(elementType.getAutomationConfig()).thenReturn(config);

		sut.execute();

		verify(curriculumService).updateCurriculumElementStatus(eq(null), eq(element),
				eq(CurriculumElementStatus.finished), eq(false), eq(null));
		verify(dbInstance, atLeast(1)).commitAndCloseSession();
	}

	@Test
	public void testProcessElement_elementConfigOverridesType() {
		CurriculumElement element = mock(CurriculumElement.class);
		when(element.getElementStatus()).thenReturn(CurriculumElementStatus.active);
		when(curriculumService.loadAutomationCandidates()).thenReturn(List.of(element));

		CurriculumAutomationRule elementRule = new CurriculumAutomationRule();
		elementRule.setEnabled(true);
		elementRule.setDependingOn(AutomationDependingOn.STATUS);
		elementRule.setDependingOnStatus(Set.of(CurriculumElementStatus.active.name()));
		elementRule.setContext(AutomationContext.ELEMENT);
		elementRule.setAutomationType(AutomationType.STATUS_CHANGE);
		elementRule.setTargetStatus(CurriculumElementStatus.finished);
		CurriculumAutomationConfig elementConfig = new CurriculumAutomationConfig();
		elementConfig.addRule(elementRule);
		when(element.getAutomationConfig()).thenReturn(elementConfig);

		CurriculumAutomationRule typeRule = new CurriculumAutomationRule();
		typeRule.setEnabled(true);
		typeRule.setDependingOn(AutomationDependingOn.STATUS);
		typeRule.setDependingOnStatus(Set.of(CurriculumElementStatus.active.name()));
		typeRule.setContext(AutomationContext.ELEMENT);
		typeRule.setAutomationType(AutomationType.STATUS_CHANGE);
		typeRule.setTargetStatus(CurriculumElementStatus.confirmed);
		CurriculumAutomationConfig typeConfig = new CurriculumAutomationConfig();
		typeConfig.addRule(typeRule);
		CurriculumElementType elementType = mock(CurriculumElementType.class);
		when(element.getType()).thenReturn(elementType);
		when(elementType.getAutomationConfig()).thenReturn(typeConfig);

		sut.execute();

		verify(curriculumService).updateCurriculumElementStatus(eq(null), eq(element),
				eq(CurriculumElementStatus.finished), eq(false), eq(null));
		verify(curriculumService, never()).updateCurriculumElementStatus(eq(null), eq(element),
				eq(CurriculumElementStatus.confirmed), eq(false), eq(null));
	}

	@Test
	public void testProcessElement_elementConfig_typeNull() {
		CurriculumElement element = mock(CurriculumElement.class);
		when(element.getElementStatus()).thenReturn(CurriculumElementStatus.active);
		when(element.getType()).thenReturn(null);
		when(curriculumService.loadAutomationCandidates()).thenReturn(List.of(element));

		CurriculumAutomationRule rule = new CurriculumAutomationRule();
		rule.setEnabled(true);
		rule.setDependingOn(AutomationDependingOn.STATUS);
		rule.setDependingOnStatus(Set.of(CurriculumElementStatus.active.name()));
		rule.setContext(AutomationContext.ELEMENT);
		rule.setAutomationType(AutomationType.STATUS_CHANGE);
		rule.setTargetStatus(CurriculumElementStatus.finished);
		CurriculumAutomationConfig config = new CurriculumAutomationConfig();
		config.addRule(rule);
		when(element.getAutomationConfig()).thenReturn(config);

		sut.execute();

		verify(curriculumService).updateCurriculumElementStatus(eq(null), eq(element),
				eq(CurriculumElementStatus.finished), eq(false), eq(null));
	}

	@Test
	public void testProcessElement_disabledRule_skips() {
		CurriculumElement element = mock(CurriculumElement.class);
		when(element.getElementStatus()).thenReturn(CurriculumElementStatus.active);
		when(curriculumService.loadAutomationCandidates()).thenReturn(List.of(element));

		CurriculumAutomationRule rule = new CurriculumAutomationRule();
		rule.setEnabled(false);
		rule.setDependingOn(AutomationDependingOn.STATUS);
		rule.setDependingOnStatus(Set.of(CurriculumElementStatus.active.name()));
		rule.setContext(AutomationContext.ELEMENT);
		rule.setAutomationType(AutomationType.STATUS_CHANGE);
		rule.setTargetStatus(CurriculumElementStatus.finished);
		CurriculumAutomationConfig config = new CurriculumAutomationConfig();
		config.addRule(rule);
		CurriculumElementType elementType = mock(CurriculumElementType.class);
		when(element.getType()).thenReturn(elementType);
		when(elementType.getAutomationConfig()).thenReturn(config);

		sut.execute();

		verify(curriculumService, never()).updateCurriculumElementStatus(any(), any(), any(), anyBoolean(), any());
	}

}
