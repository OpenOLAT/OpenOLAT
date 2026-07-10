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
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.components.date.OffsetDirection;
import org.olat.core.util.DateUtils;
import org.olat.modules.curriculum.AutomationContext;
import org.olat.modules.curriculum.AutomationDependingOn;
import org.olat.modules.curriculum.AutomationExecutionResult;
import org.olat.modules.curriculum.AutomationType;
import org.olat.modules.curriculum.AutomationUnit;
import org.olat.modules.curriculum.CurriculumAutomationConfig;
import org.olat.modules.curriculum.CurriculumAutomationRule;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumAutomationRuleImpl;
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
	@Mock
	private CurriculumAutomationConfigDAO automationConfigDao;
	@Mock
	private CurriculumAutomationExecutionDAO automationExecutionDao;

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

		CurriculumAutomationRule rule = new CurriculumAutomationRuleImpl();
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

		CurriculumAutomationRule rule = new CurriculumAutomationRuleImpl();
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

		CurriculumAutomationRule rule = new CurriculumAutomationRuleImpl();
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

		CurriculumAutomationRule rule = new CurriculumAutomationRuleImpl();
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

		CurriculumAutomationRule rule = new CurriculumAutomationRuleImpl();
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

		CurriculumAutomationRule rule = new CurriculumAutomationRuleImpl();
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

		CurriculumAutomationRule rule = new CurriculumAutomationRuleImpl();
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

		CurriculumAutomationRule rule = new CurriculumAutomationRuleImpl();
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

		CurriculumAutomationRule rule = new CurriculumAutomationRuleImpl();
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

		CurriculumAutomationRule rule = new CurriculumAutomationRuleImpl();
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

		CurriculumAutomationRule rule = new CurriculumAutomationRuleImpl();
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

		CurriculumAutomationRule rule = new CurriculumAutomationRuleImpl();
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

		CurriculumAutomationRule rule = new CurriculumAutomationRuleImpl();
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

		CurriculumAutomationRule rule = new CurriculumAutomationRuleImpl();
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

		CurriculumAutomationRule rule = new CurriculumAutomationRuleImpl();
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

		CurriculumAutomationRule rule = new CurriculumAutomationRuleImpl();
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

		CurriculumAutomationRule rule = new CurriculumAutomationRuleImpl();
		rule.setDirection(OffsetDirection.AFTER);
		rule.setUnit(AutomationUnit.SAME_DAY);

		Date result = sut.computeTriggerDate(element, rule);

		assertThat(result).isEqualTo(DateUtils.getStartOfDay(parentEndDate));
	}

	@Test
	public void testImplementationContext_statusChange_fires() {
		CurriculumElement element = mock(CurriculumElement.class);
		when(element.getElementStatus()).thenReturn(CurriculumElementStatus.confirmed);

		CurriculumAutomationRule rule = new CurriculumAutomationRuleImpl();
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

		CurriculumAutomationRule rule = new CurriculumAutomationRuleImpl();
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

		CurriculumAutomationRule rule = new CurriculumAutomationRuleImpl();
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

		CurriculumAutomationRule rule = new CurriculumAutomationRuleImpl();
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

		CurriculumAutomationRule rule = new CurriculumAutomationRuleImpl();
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

		CurriculumAutomationRule rule = new CurriculumAutomationRuleImpl();
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

		CurriculumAutomationRule rule = new CurriculumAutomationRuleImpl();
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

		CurriculumAutomationRule rule = new CurriculumAutomationRuleImpl();
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

		CurriculumAutomationRule rule = new CurriculumAutomationRuleImpl();
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

		CurriculumAutomationRule rule = new CurriculumAutomationRuleImpl();
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

		CurriculumAutomationRule rule = new CurriculumAutomationRuleImpl();
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

		CurriculumAutomationRule rule = new CurriculumAutomationRuleImpl();
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

		CurriculumAutomationRule rule = new CurriculumAutomationRuleImpl();
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
	public void testContentStatusChange_lowerTargetStatus_skipsChange() {
		CurriculumElement element = mock(CurriculumElement.class);
		when(element.getElementStatus()).thenReturn(CurriculumElementStatus.active);
		RepositoryEntry entry = mock(RepositoryEntry.class);
		when(entry.getEntryStatus()).thenReturn(RepositoryEntryStatusEnum.published);
		when(curriculumService.getRepositoryEntries(element)).thenReturn(List.of(entry));

		CurriculumAutomationRule rule = new CurriculumAutomationRuleImpl();
		rule.setDependingOn(AutomationDependingOn.STATUS);
		rule.setDependingOnStatus(Set.of(CurriculumElementStatus.active.name()));
		rule.setContext(AutomationContext.CONTENT);
		rule.setAutomationType(AutomationType.STATUS_CHANGE);
		rule.setTargetStatus(RepositoryEntryStatusEnum.coachpublished);

		sut.processRule(element, rule, DateUtils.getStartOfDay(new Date()));

		verify(repositoryManager, never()).setStatus(any(), any());
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

		CurriculumAutomationRule rule = new CurriculumAutomationRuleImpl();
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

		CurriculumAutomationRule rule = new CurriculumAutomationRuleImpl();
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

		CurriculumAutomationRule rule = new CurriculumAutomationRuleImpl();
		rule.setReference(CurriculumAutomationRule.REFERENCE_END);
		rule.setDirection(OffsetDirection.AFTER);
		rule.setUnit(AutomationUnit.SAME_DAY);

		Date result = sut.computeTriggerDate(element, rule);

		assertThat(result).isEqualTo(DateUtils.getStartOfDay(endDate));
	}

	@Test
	public void testComputeTriggerDate_finishedRule_referenceEnd_returnsEndOfDay() {
		Date endDate = new Date(0);
		CurriculumElement element = mock(CurriculumElement.class);
		when(element.getEndDate()).thenReturn(endDate);

		CurriculumAutomationRule rule = new CurriculumAutomationRuleImpl();
		rule.setReference(CurriculumAutomationRule.REFERENCE_END);
		rule.setDirection(OffsetDirection.AFTER);
		rule.setUnit(AutomationUnit.SAME_DAY);
		rule.setTargetStatus(CurriculumElementStatus.finished);

		Date result = sut.computeTriggerDate(element, rule);

		assertThat(result).isEqualTo(DateUtils.getEndOfDay(endDate));
	}

	@Test
	public void testComputeTriggerDate_nonFinishedRule_referenceEnd_stillReturnsStartOfDay() {
		Date endDate = new Date(0);
		CurriculumElement element = mock(CurriculumElement.class);
		when(element.getEndDate()).thenReturn(endDate);

		CurriculumAutomationRule rule = new CurriculumAutomationRuleImpl();
		rule.setReference(CurriculumAutomationRule.REFERENCE_END);
		rule.setDirection(OffsetDirection.AFTER);
		rule.setUnit(AutomationUnit.SAME_DAY);
		rule.setTargetStatus(CurriculumElementStatus.active);

		Date result = sut.computeTriggerDate(element, rule);

		assertThat(result).isEqualTo(DateUtils.getStartOfDay(endDate));
	}

	@Test
	public void testProcessRule_finishedRule_referenceEnd_onExactEndDay_doesNotFire() {
		Date today = DateUtils.getStartOfDay(new Date());
		CurriculumElement element = mock(CurriculumElement.class);
		when(element.getEndDate()).thenReturn(today);
		when(element.getElementStatus()).thenReturn(CurriculumElementStatus.confirmed);

		CurriculumAutomationRule rule = new CurriculumAutomationRuleImpl();
		rule.setDependingOn(AutomationDependingOn.EXECUTION_PERIOD);
		rule.setReference(CurriculumAutomationRule.REFERENCE_END);
		rule.setDirection(OffsetDirection.AFTER);
		rule.setUnit(AutomationUnit.SAME_DAY);
		rule.setContext(AutomationContext.ELEMENT);
		rule.setAutomationType(AutomationType.STATUS_CHANGE);
		rule.setTargetStatus(CurriculumElementStatus.finished);

		sut.processRule(element, rule, today);

		verify(curriculumService, never()).updateCurriculumElementStatus(any(), any(), any(), anyBoolean(), any());
	}

	@Test
	public void testProcessRule_finishedRule_referenceEnd_dayAfterEndDate_fires() {
		Date endDate = DateUtils.addDays(new Date(), -1);
		Date today = DateUtils.getStartOfDay(new Date());
		CurriculumElement element = mock(CurriculumElement.class);
		when(element.getEndDate()).thenReturn(endDate);
		when(element.getElementStatus()).thenReturn(CurriculumElementStatus.confirmed);

		CurriculumAutomationRule rule = new CurriculumAutomationRuleImpl();
		rule.setDependingOn(AutomationDependingOn.EXECUTION_PERIOD);
		rule.setReference(CurriculumAutomationRule.REFERENCE_END);
		rule.setDirection(OffsetDirection.AFTER);
		rule.setUnit(AutomationUnit.SAME_DAY);
		rule.setContext(AutomationContext.ELEMENT);
		rule.setAutomationType(AutomationType.STATUS_CHANGE);
		rule.setTargetStatus(CurriculumElementStatus.finished);

		sut.processRule(element, rule, today);

		verify(curriculumService).updateCurriculumElementStatus(eq(null), eq(element),
				eq(CurriculumElementStatus.finished), eq(false), eq(null));
	}

	@Test
	public void testProcessRule_executionPeriodOnExactDay_fires() {
		Date today = DateUtils.getStartOfDay(new Date());
		CurriculumElement element = mock(CurriculumElement.class);
		when(element.getBeginDate()).thenReturn(today);
		when(element.getElementStatus()).thenReturn(CurriculumElementStatus.active);

		CurriculumAutomationRule rule = new CurriculumAutomationRuleImpl();
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

		CurriculumAutomationRule rule = new CurriculumAutomationRuleImpl();
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

		CurriculumAutomationRule rule = new CurriculumAutomationRuleImpl();
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

		CurriculumAutomationRule rule = new CurriculumAutomationRuleImpl();
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

		CurriculumAutomationRule rule = new CurriculumAutomationRuleImpl();
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

		CurriculumAutomationRule rule = new CurriculumAutomationRuleImpl();
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

		CurriculumAutomationRule rule = new CurriculumAutomationRuleImpl();
		rule.setDependingOn(AutomationDependingOn.EXECUTION_PERIOD);
		rule.setReference(CurriculumAutomationRule.REFERENCE_BEGIN);
		rule.setDirection(OffsetDirection.BEFORE);
		rule.setValue(5);
		rule.setUnit(AutomationUnit.DAYS);
		rule.setContext(AutomationContext.ELEMENT);
		rule.setAutomationType(AutomationType.STATUS_CHANGE);
		rule.setTargetStatus(CurriculumElementStatus.finished);
		CurriculumAutomationConfig config = mockConfig(rule, true);
		CurriculumElementType elementType = mock(CurriculumElementType.class);
		when(element.getType()).thenReturn(elementType);
		when(config.getElementType()).thenReturn(elementType);
		when(automationConfigDao.getConfigsByCurriculumElements(List.of(element))).thenReturn(List.of());
		when(automationConfigDao.getConfigsByElementTypes(Set.of(elementType))).thenReturn(List.of(config));

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

		CurriculumAutomationRule elementRule = new CurriculumAutomationRuleImpl();
		elementRule.setDependingOn(AutomationDependingOn.STATUS);
		elementRule.setDependingOnStatus(Set.of(CurriculumElementStatus.active.name()));
		elementRule.setContext(AutomationContext.ELEMENT);
		elementRule.setAutomationType(AutomationType.STATUS_CHANGE);
		elementRule.setTargetStatus(CurriculumElementStatus.finished);
		CurriculumAutomationConfig elementConfig = mockConfig(elementRule, true);
		when(elementConfig.getCurriculumElement()).thenReturn(element);
		when(automationConfigDao.getConfigsByCurriculumElements(List.of(element))).thenReturn(List.of(elementConfig));

		CurriculumAutomationRule typeRule = new CurriculumAutomationRuleImpl();
		typeRule.setDependingOn(AutomationDependingOn.STATUS);
		typeRule.setDependingOnStatus(Set.of(CurriculumElementStatus.active.name()));
		typeRule.setContext(AutomationContext.ELEMENT);
		typeRule.setAutomationType(AutomationType.STATUS_CHANGE);
		typeRule.setTargetStatus(CurriculumElementStatus.confirmed);
		CurriculumAutomationConfig typeConfig = mockConfig(typeRule, true);
		CurriculumElementType elementType = mock(CurriculumElementType.class);
		when(element.getType()).thenReturn(elementType);
		when(typeConfig.getElementType()).thenReturn(elementType);
		when(automationConfigDao.getConfigsByElementTypes(Set.of(elementType))).thenReturn(List.of(typeConfig));

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

		CurriculumAutomationRule rule = new CurriculumAutomationRuleImpl();
		rule.setDependingOn(AutomationDependingOn.STATUS);
		rule.setDependingOnStatus(Set.of(CurriculumElementStatus.active.name()));
		rule.setContext(AutomationContext.ELEMENT);
		rule.setAutomationType(AutomationType.STATUS_CHANGE);
		rule.setTargetStatus(CurriculumElementStatus.finished);
		CurriculumAutomationConfig config = mockConfig(rule, true);
		when(config.getCurriculumElement()).thenReturn(element);
		when(automationConfigDao.getConfigsByCurriculumElements(List.of(element))).thenReturn(List.of(config));

		sut.execute();

		verify(curriculumService).updateCurriculumElementStatus(eq(null), eq(element),
				eq(CurriculumElementStatus.finished), eq(false), eq(null));
	}

	@Test
	public void testProcessElement_disabledRule_skips() {
		CurriculumElement element = mock(CurriculumElement.class);
		when(element.getElementStatus()).thenReturn(CurriculumElementStatus.active);
		when(curriculumService.loadAutomationCandidates()).thenReturn(List.of(element));

		CurriculumAutomationRule rule = new CurriculumAutomationRuleImpl();
		rule.setDependingOn(AutomationDependingOn.STATUS);
		rule.setDependingOnStatus(Set.of(CurriculumElementStatus.active.name()));
		rule.setContext(AutomationContext.ELEMENT);
		rule.setAutomationType(AutomationType.STATUS_CHANGE);
		rule.setTargetStatus(CurriculumElementStatus.finished);
		CurriculumAutomationConfig config = mockConfig(rule, false);
		CurriculumElementType elementType = mock(CurriculumElementType.class);
		when(element.getType()).thenReturn(elementType);
		when(config.getElementType()).thenReturn(elementType);
		when(automationConfigDao.getConfigsByCurriculumElements(List.of(element))).thenReturn(List.of());
		when(automationConfigDao.getConfigsByElementTypes(Set.of(elementType))).thenReturn(List.of(config));

		sut.execute();

		verify(curriculumService, never()).updateCurriculumElementStatus(any(), any(), any(), anyBoolean(), any());
	}

	@Test
	public void testExecute_firstRun_createsExecutionAndFires() {
		Date beginDate = DateUtils.addDays(new Date(), -10);
		CurriculumElement element = mock(CurriculumElement.class);
		when(element.getBeginDate()).thenReturn(beginDate);
		when(element.getElementStatus()).thenReturn(CurriculumElementStatus.active);
		when(curriculumService.loadAutomationCandidates()).thenReturn(List.of(element));

		CurriculumAutomationRule rule = new CurriculumAutomationRuleImpl();
		rule.setDependingOn(AutomationDependingOn.EXECUTION_PERIOD);
		rule.setReference(CurriculumAutomationRule.REFERENCE_BEGIN);
		rule.setDirection(OffsetDirection.BEFORE);
		rule.setValue(5);
		rule.setUnit(AutomationUnit.DAYS);
		rule.setContext(AutomationContext.ELEMENT);
		rule.setAutomationType(AutomationType.STATUS_CHANGE);
		rule.setTargetStatus(CurriculumElementStatus.finished);
		CurriculumAutomationConfig config = mockConfig(rule, true);
		CurriculumElementType elementType = mock(CurriculumElementType.class);
		when(element.getType()).thenReturn(elementType);
		when(config.getElementType()).thenReturn(elementType);
		when(automationConfigDao.getConfigsByCurriculumElements(List.of(element))).thenReturn(List.of());
		when(automationConfigDao.getConfigsByElementTypes(Set.of(elementType))).thenReturn(List.of(config));
		when(automationExecutionDao.getExecutedRuleIdentifiers(List.of(element))).thenReturn(new HashMap<>());
		when(curriculumService.getCurriculumElement(element)).thenReturn(element);

		sut.execute();

		verify(curriculumService).updateCurriculumElementStatus(eq(null), eq(element),
				eq(CurriculumElementStatus.finished), eq(false), eq(null));
		verify(automationExecutionDao, times(1)).createExecution(eq(element), eq(elementType), eq(rule), any());
	}

	@Test
	public void testExecute_ruleAlreadyExecuted_skips() {
		Date beginDate = DateUtils.addDays(new Date(), -10);
		CurriculumElement element = mock(CurriculumElement.class);
		when(element.getKey()).thenReturn(1L);
		when(element.getBeginDate()).thenReturn(beginDate);
		when(element.getElementStatus()).thenReturn(CurriculumElementStatus.active);
		when(curriculumService.loadAutomationCandidates()).thenReturn(List.of(element));

		CurriculumAutomationRule rule = new CurriculumAutomationRuleImpl();
		rule.setDependingOn(AutomationDependingOn.EXECUTION_PERIOD);
		rule.setReference(CurriculumAutomationRule.REFERENCE_BEGIN);
		rule.setDirection(OffsetDirection.BEFORE);
		rule.setValue(5);
		rule.setUnit(AutomationUnit.DAYS);
		rule.setContext(AutomationContext.ELEMENT);
		rule.setAutomationType(AutomationType.STATUS_CHANGE);
		rule.setTargetStatus(CurriculumElementStatus.finished);
		CurriculumAutomationConfig config = mockConfig(rule, true);
		CurriculumElementType elementType = mock(CurriculumElementType.class);
		when(element.getType()).thenReturn(elementType);
		when(config.getElementType()).thenReturn(elementType);
		when(automationConfigDao.getConfigsByCurriculumElements(List.of(element))).thenReturn(List.of());
		when(automationConfigDao.getConfigsByElementTypes(Set.of(elementType))).thenReturn(List.of(config));

		Map<Long, Set<String>> executedByElement = new HashMap<>();
		executedByElement.put(1L, new HashSet<>(Set.of("ELEMENT::STATUS_CHANGE::finished")));
		when(automationExecutionDao.getExecutedRuleIdentifiers(List.of(element))).thenReturn(executedByElement);

		sut.execute();

		verify(curriculumService, never()).updateCurriculumElementStatus(any(), any(), any(), anyBoolean(), any());
		verify(automationExecutionDao, never()).createExecution(any(), any(), any(), any());
	}

	@Test
	public void testExecute_alreadyExecutedForOneElement_stillFiresForOther() {
		Date beginDate = DateUtils.addDays(new Date(), -10);
		CurriculumElement elementA = mock(CurriculumElement.class);
		when(elementA.getKey()).thenReturn(1L);
		when(elementA.getBeginDate()).thenReturn(beginDate);
		when(elementA.getElementStatus()).thenReturn(CurriculumElementStatus.active);
		CurriculumElement elementB = mock(CurriculumElement.class);
		when(elementB.getKey()).thenReturn(2L);
		when(elementB.getBeginDate()).thenReturn(beginDate);
		when(elementB.getElementStatus()).thenReturn(CurriculumElementStatus.active);
		when(curriculumService.loadAutomationCandidates()).thenReturn(List.of(elementA, elementB));

		CurriculumAutomationRule rule = new CurriculumAutomationRuleImpl();
		rule.setDependingOn(AutomationDependingOn.EXECUTION_PERIOD);
		rule.setReference(CurriculumAutomationRule.REFERENCE_BEGIN);
		rule.setDirection(OffsetDirection.BEFORE);
		rule.setValue(5);
		rule.setUnit(AutomationUnit.DAYS);
		rule.setContext(AutomationContext.ELEMENT);
		rule.setAutomationType(AutomationType.STATUS_CHANGE);
		rule.setTargetStatus(CurriculumElementStatus.finished);
		CurriculumAutomationConfig config = mockConfig(rule, true);
		CurriculumElementType elementType = mock(CurriculumElementType.class);
		when(elementA.getType()).thenReturn(elementType);
		when(elementB.getType()).thenReturn(elementType);
		when(config.getElementType()).thenReturn(elementType);
		when(automationConfigDao.getConfigsByCurriculumElements(List.of(elementA, elementB))).thenReturn(List.of());
		when(automationConfigDao.getConfigsByElementTypes(Set.of(elementType))).thenReturn(List.of(config));
		when(curriculumService.getCurriculumElement(elementB)).thenReturn(elementB);

		Map<Long, Set<String>> executedByElement = new HashMap<>();
		executedByElement.put(1L, new HashSet<>(Set.of("ELEMENT::STATUS_CHANGE::finished")));
		when(automationExecutionDao.getExecutedRuleIdentifiers(List.of(elementA, elementB))).thenReturn(executedByElement);

		sut.execute();

		verify(curriculumService, never()).updateCurriculumElementStatus(eq(null), eq(elementA),
				eq(CurriculumElementStatus.finished), eq(false), eq(null));
		verify(automationExecutionDao, never()).createExecution(eq(elementA), any(), any(), any());
		verify(curriculumService, times(1)).updateCurriculumElementStatus(eq(null), eq(elementB),
				eq(CurriculumElementStatus.finished), eq(false), eq(null));
		verify(automationExecutionDao, times(1)).createExecution(eq(elementB), eq(elementType), eq(rule), any());
	}

	@Test
	public void testElementStatusChange_lowerTargetStatus_skipsChange() {
		CurriculumElement element = mock(CurriculumElement.class);
		when(element.getElementStatus()).thenReturn(CurriculumElementStatus.active);

		CurriculumAutomationRule rule = new CurriculumAutomationRuleImpl();
		rule.setDependingOn(AutomationDependingOn.STATUS);
		rule.setDependingOnStatus(Set.of(CurriculumElementStatus.active.name()));
		rule.setContext(AutomationContext.ELEMENT);
		rule.setAutomationType(AutomationType.STATUS_CHANGE);
		rule.setTargetStatus(CurriculumElementStatus.confirmed);

		Date today = DateUtils.getStartOfDay(new Date());
		sut.processRule(element, rule, today);

		verify(curriculumService, never()).updateCurriculumElementStatus(any(), any(), any(), anyBoolean(), any());
	}

	@Test
	public void testElementStatusChange_higherTargetStatus_changes() {
		CurriculumElement element = mock(CurriculumElement.class);
		when(element.getElementStatus()).thenReturn(CurriculumElementStatus.confirmed);

		CurriculumAutomationRule rule = new CurriculumAutomationRuleImpl();
		rule.setDependingOn(AutomationDependingOn.STATUS);
		rule.setDependingOnStatus(Set.of(CurriculumElementStatus.confirmed.name()));
		rule.setContext(AutomationContext.ELEMENT);
		rule.setAutomationType(AutomationType.STATUS_CHANGE);
		rule.setTargetStatus(CurriculumElementStatus.active);

		Date today = DateUtils.getStartOfDay(new Date());
		sut.processRule(element, rule, today);

		verify(curriculumService).updateCurriculumElementStatus(eq(null), eq(element),
				eq(CurriculumElementStatus.active), eq(false), eq(null));
	}

	@Test
	public void testExecute_downgradePrevented_createsUnchangedExecution() {
		CurriculumElement element = mock(CurriculumElement.class);
		when(element.getElementStatus()).thenReturn(CurriculumElementStatus.active);
		when(curriculumService.loadAutomationCandidates()).thenReturn(List.of(element));

		CurriculumAutomationRule rule = new CurriculumAutomationRuleImpl();
		rule.setDependingOn(AutomationDependingOn.STATUS);
		rule.setDependingOnStatus(Set.of(CurriculumElementStatus.active.name()));
		rule.setContext(AutomationContext.ELEMENT);
		rule.setAutomationType(AutomationType.STATUS_CHANGE);
		rule.setTargetStatus(CurriculumElementStatus.confirmed);
		CurriculumAutomationConfig config = mockConfig(rule, true);
		CurriculumElementType elementType = mock(CurriculumElementType.class);
		when(element.getType()).thenReturn(elementType);
		when(config.getElementType()).thenReturn(elementType);
		when(automationConfigDao.getConfigsByCurriculumElements(List.of(element))).thenReturn(List.of());
		when(automationConfigDao.getConfigsByElementTypes(Set.of(elementType))).thenReturn(List.of(config));
		when(automationExecutionDao.getExecutedRuleIdentifiers(List.of(element))).thenReturn(new HashMap<>());

		sut.execute();

		verify(curriculumService, never()).updateCurriculumElementStatus(any(), any(), any(), anyBoolean(), any());
		verify(automationExecutionDao, times(1)).createExecution(eq(element), eq(elementType), eq(rule),
				eq(AutomationExecutionResult.UNCHANGED));
	}

	@Test
	public void testExecute_processesRulesInOrder_elementThenInstantiationThenContentStatus() {
		CurriculumElementType elementType = mock(CurriculumElementType.class);
		when(elementType.getMaxRepositoryEntryRelations()).thenReturn(-1);
		CurriculumElement element = mock(CurriculumElement.class);
		when(element.getElementStatus()).thenReturn(CurriculumElementStatus.active);
		when(element.getType()).thenReturn(elementType);
		when(curriculumService.loadAutomationCandidates()).thenReturn(List.of(element));

		CurriculumElement updatedElement = mock(CurriculumElement.class);
		when(updatedElement.getElementStatus()).thenReturn(CurriculumElementStatus.finished);
		when(updatedElement.getType()).thenReturn(elementType);
		when(curriculumService.getCurriculumElement(element)).thenReturn(updatedElement);

		CurriculumAutomationRule elementStatusRule = new CurriculumAutomationRuleImpl();
		elementStatusRule.setDependingOn(AutomationDependingOn.STATUS);
		elementStatusRule.setDependingOnStatus(Set.of(CurriculumElementStatus.active.name()));
		elementStatusRule.setContext(AutomationContext.ELEMENT);
		elementStatusRule.setAutomationType(AutomationType.STATUS_CHANGE);
		elementStatusRule.setTargetStatus(CurriculumElementStatus.finished);
		CurriculumAutomationConfig elementStatusConfig = mockConfig(elementStatusRule, true);
		when(elementStatusConfig.getElementType()).thenReturn(elementType);

		CurriculumAutomationRule instantiationRule = new CurriculumAutomationRuleImpl();
		instantiationRule.setDependingOn(AutomationDependingOn.STATUS);
		instantiationRule.setDependingOnStatus(Set.of(CurriculumElementStatus.finished.name()));
		instantiationRule.setContext(AutomationContext.CONTENT);
		instantiationRule.setAutomationType(AutomationType.INSTANTIATION);
		CurriculumAutomationConfig instantiationConfig = mockConfig(instantiationRule, true);
		when(instantiationConfig.getElementType()).thenReturn(elementType);
		RepositoryEntry template = mock(RepositoryEntry.class);
		when(curriculumService.getRepositoryTemplates(updatedElement)).thenReturn(List.of(template));

		CurriculumAutomationRule contentStatusRule = new CurriculumAutomationRuleImpl();
		contentStatusRule.setDependingOn(AutomationDependingOn.STATUS);
		contentStatusRule.setDependingOnStatus(Set.of(CurriculumElementStatus.finished.name()));
		contentStatusRule.setContext(AutomationContext.CONTENT);
		contentStatusRule.setAutomationType(AutomationType.STATUS_CHANGE);
		contentStatusRule.setTargetStatus(RepositoryEntryStatusEnum.published);
		CurriculumAutomationConfig contentStatusConfig = mockConfig(contentStatusRule, true);
		when(contentStatusConfig.getElementType()).thenReturn(elementType);
		RepositoryEntry entry = mock(RepositoryEntry.class);
		when(entry.getEntryStatus()).thenReturn(RepositoryEntryStatusEnum.preparation);
		when(curriculumService.getRepositoryEntries(updatedElement)).thenReturn(List.of(entry));

		when(automationConfigDao.getConfigsByCurriculumElements(List.of(element))).thenReturn(List.of());
		when(automationConfigDao.getConfigsByElementTypes(Set.of(elementType)))
				.thenReturn(List.of(contentStatusConfig, instantiationConfig, elementStatusConfig));
		when(automationExecutionDao.getExecutedRuleIdentifiers(List.of(element))).thenReturn(new HashMap<>());

		sut.execute();

		InOrder inOrder = inOrder(curriculumService, repositoryManager);
		inOrder.verify(curriculumService).updateCurriculumElementStatus(eq(null), eq(element),
				eq(CurriculumElementStatus.finished), eq(false), eq(null));
		inOrder.verify(curriculumService).instantiateTemplate(any(), any(), any(), any(), any(), any(), any());
		inOrder.verify(repositoryManager).setStatus(any(), eq(RepositoryEntryStatusEnum.published));
	}

	@Test
	public void testExecute_contentRuleSeesUpdatedElementStatus() {
		CurriculumElement element = mock(CurriculumElement.class);
		when(element.getElementStatus()).thenReturn(CurriculumElementStatus.confirmed);
		when(curriculumService.loadAutomationCandidates()).thenReturn(List.of(element));

		CurriculumElement updatedElement = mock(CurriculumElement.class);
		when(updatedElement.getElementStatus()).thenReturn(CurriculumElementStatus.active);
		when(curriculumService.getCurriculumElement(element)).thenReturn(updatedElement);

		CurriculumAutomationRule elementStatusRule = new CurriculumAutomationRuleImpl();
		elementStatusRule.setDependingOn(AutomationDependingOn.STATUS);
		elementStatusRule.setDependingOnStatus(Set.of(CurriculumElementStatus.confirmed.name()));
		elementStatusRule.setContext(AutomationContext.ELEMENT);
		elementStatusRule.setAutomationType(AutomationType.STATUS_CHANGE);
		elementStatusRule.setTargetStatus(CurriculumElementStatus.active);
		CurriculumAutomationConfig elementStatusConfig = mockConfig(elementStatusRule, true);
		when(elementStatusConfig.getCurriculumElement()).thenReturn(element);

		CurriculumAutomationRule contentStatusRule = new CurriculumAutomationRuleImpl();
		contentStatusRule.setDependingOn(AutomationDependingOn.STATUS);
		contentStatusRule.setDependingOnStatus(Set.of(CurriculumElementStatus.active.name()));
		contentStatusRule.setContext(AutomationContext.CONTENT);
		contentStatusRule.setAutomationType(AutomationType.STATUS_CHANGE);
		contentStatusRule.setTargetStatus(RepositoryEntryStatusEnum.published);
		CurriculumAutomationConfig contentStatusConfig = mockConfig(contentStatusRule, true);
		when(contentStatusConfig.getCurriculumElement()).thenReturn(element);
		RepositoryEntry entry = mock(RepositoryEntry.class);
		when(entry.getEntryStatus()).thenReturn(RepositoryEntryStatusEnum.preparation);
		when(curriculumService.getRepositoryEntries(updatedElement)).thenReturn(List.of(entry));

		when(automationConfigDao.getConfigsByCurriculumElements(List.of(element)))
				.thenReturn(List.of(contentStatusConfig, elementStatusConfig));
		when(automationExecutionDao.getExecutedRuleIdentifiers(List.of(element))).thenReturn(new HashMap<>());

		sut.execute();

		verify(repositoryManager).setStatus(eq(entry), eq(RepositoryEntryStatusEnum.published));
	}

	@Test
	public void testProcessStatusChange_statusRuleFires_executionPeriodRuleSkipped() {
		Date beginDate = DateUtils.addDays(new Date(), -10);
		CurriculumElement element = mock(CurriculumElement.class);
		when(element.getKey()).thenReturn(1L);
		when(element.getBeginDate()).thenReturn(beginDate);
		when(element.getElementStatus()).thenReturn(CurriculumElementStatus.active);
		when(curriculumService.getCurriculumElement(element)).thenReturn(element);

		CurriculumAutomationRule statusRule = new CurriculumAutomationRuleImpl();
		statusRule.setDependingOn(AutomationDependingOn.STATUS);
		statusRule.setDependingOnStatus(Set.of(CurriculumElementStatus.active.name()));
		statusRule.setContext(AutomationContext.ELEMENT);
		statusRule.setAutomationType(AutomationType.STATUS_CHANGE);
		statusRule.setTargetStatus(CurriculumElementStatus.cancelled);
		CurriculumAutomationConfig statusConfig = mockConfig(statusRule, true);

		CurriculumAutomationRule executionPeriodRule = new CurriculumAutomationRuleImpl();
		executionPeriodRule.setDependingOn(AutomationDependingOn.EXECUTION_PERIOD);
		executionPeriodRule.setReference(CurriculumAutomationRule.REFERENCE_BEGIN);
		executionPeriodRule.setDirection(OffsetDirection.BEFORE);
		executionPeriodRule.setValue(5);
		executionPeriodRule.setUnit(AutomationUnit.DAYS);
		executionPeriodRule.setContext(AutomationContext.ELEMENT);
		executionPeriodRule.setAutomationType(AutomationType.STATUS_CHANGE);
		executionPeriodRule.setTargetStatus(CurriculumElementStatus.finished);
		CurriculumAutomationConfig executionPeriodConfig = mockConfig(executionPeriodRule, true);
		when(statusConfig.getCurriculumElement()).thenReturn(element);
		when(executionPeriodConfig.getCurriculumElement()).thenReturn(element);

		when(automationConfigDao.getConfigsByCurriculumElements(List.of(element)))
				.thenReturn(List.of(statusConfig, executionPeriodConfig));
		when(automationExecutionDao.getExecutedRuleIdentifiers(List.of(element))).thenReturn(new HashMap<>());

		sut.processStatusChange(List.of(element));

		verify(curriculumService).updateCurriculumElementStatus(eq(null), eq(element),
				eq(CurriculumElementStatus.cancelled), eq(false), eq(null));
		verify(curriculumService, never()).updateCurriculumElementStatus(eq(null), eq(element),
				eq(CurriculumElementStatus.finished), eq(false), eq(null));
	}

	@Test
	public void testProcessStatusChange_alreadyExecuted_skips() {
		CurriculumElement element = mock(CurriculumElement.class);
		when(element.getKey()).thenReturn(1L);
		when(element.getElementStatus()).thenReturn(CurriculumElementStatus.active);
		when(curriculumService.getCurriculumElement(element)).thenReturn(element);

		CurriculumAutomationRule statusRule = new CurriculumAutomationRuleImpl();
		statusRule.setDependingOn(AutomationDependingOn.STATUS);
		statusRule.setDependingOnStatus(Set.of(CurriculumElementStatus.active.name()));
		statusRule.setContext(AutomationContext.ELEMENT);
		statusRule.setAutomationType(AutomationType.STATUS_CHANGE);
		statusRule.setTargetStatus(CurriculumElementStatus.cancelled);
		CurriculumAutomationConfig statusConfig = mockConfig(statusRule, true);
		when(statusConfig.getCurriculumElement()).thenReturn(element);

		when(automationConfigDao.getConfigsByCurriculumElements(List.of(element))).thenReturn(List.of(statusConfig));
		Map<Long, Set<String>> executedByElement = new HashMap<>();
		executedByElement.put(1L, new HashSet<>(Set.of("ELEMENT::STATUS_CHANGE::cancelled")));
		when(automationExecutionDao.getExecutedRuleIdentifiers(List.of(element))).thenReturn(executedByElement);

		sut.processStatusChange(List.of(element));

		verify(curriculumService, never()).updateCurriculumElementStatus(any(), any(), any(), anyBoolean(), any());
	}

	@Test
	public void testProcessStatusChange_emptyElements_noop() {
		sut.processStatusChange(List.of());

		verify(automationConfigDao, never()).getConfigsByCurriculumElements(any());
	}

	@Test
	public void testContentInstantiation_noTemplates_writesUnchanged() {
		CurriculumElement element = mock(CurriculumElement.class);
		when(element.getKey()).thenReturn(1L);
		when(element.getElementStatus()).thenReturn(CurriculumElementStatus.active);
		when(curriculumService.loadAutomationCandidates()).thenReturn(List.of(element));
		when(curriculumService.getCurriculumElement(element)).thenReturn(element);

		CurriculumAutomationRule rule = new CurriculumAutomationRuleImpl();
		rule.setDependingOn(AutomationDependingOn.STATUS);
		rule.setDependingOnStatus(Set.of(CurriculumElementStatus.active.name()));
		rule.setContext(AutomationContext.CONTENT);
		rule.setAutomationType(AutomationType.INSTANTIATION);
		CurriculumAutomationConfig config = mockConfig(rule, true);
		CurriculumElementType elementType = mock(CurriculumElementType.class);
		when(elementType.getMaxRepositoryEntryRelations()).thenReturn(-1);
		when(element.getType()).thenReturn(elementType);
		when(config.getElementType()).thenReturn(elementType);
		when(automationConfigDao.getConfigsByCurriculumElements(List.of(element))).thenReturn(List.of());
		when(automationConfigDao.getConfigsByElementTypes(Set.of(elementType))).thenReturn(List.of(config));
		when(automationExecutionDao.getExecutedRuleIdentifiers(List.of(element))).thenReturn(new HashMap<>());

		sut.execute();

		verify(curriculumService, never()).instantiateTemplate(any(), any(), any(), any(), any(), any(), any());
		verify(automationExecutionDao, times(1)).createExecution(eq(element), eq(elementType), eq(rule),
				eq(AutomationExecutionResult.UNCHANGED));
	}

	@Test
	public void testContentInstantiation_alreadyReferenced_writesUnchanged() {
		CurriculumElement element = mock(CurriculumElement.class);
		when(element.getKey()).thenReturn(1L);
		when(element.getElementStatus()).thenReturn(CurriculumElementStatus.active);
		when(curriculumService.loadAutomationCandidates()).thenReturn(List.of(element));
		when(curriculumService.getCurriculumElement(element)).thenReturn(element);
		when(curriculumService.countRepositoryEntries(element)).thenReturn(1L);

		CurriculumAutomationRule rule = new CurriculumAutomationRuleImpl();
		rule.setDependingOn(AutomationDependingOn.STATUS);
		rule.setDependingOnStatus(Set.of(CurriculumElementStatus.active.name()));
		rule.setContext(AutomationContext.CONTENT);
		rule.setAutomationType(AutomationType.INSTANTIATION);
		CurriculumAutomationConfig config = mockConfig(rule, true);
		CurriculumElementType elementType = mock(CurriculumElementType.class);
		when(elementType.getMaxRepositoryEntryRelations()).thenReturn(-1);
		when(element.getType()).thenReturn(elementType);
		when(config.getElementType()).thenReturn(elementType);
		when(automationConfigDao.getConfigsByCurriculumElements(List.of(element))).thenReturn(List.of());
		when(automationConfigDao.getConfigsByElementTypes(Set.of(elementType))).thenReturn(List.of(config));
		when(automationExecutionDao.getExecutedRuleIdentifiers(List.of(element))).thenReturn(new HashMap<>());

		sut.execute();

		verify(curriculumService, never()).instantiateTemplate(any(), any(), any(), any(), any(), any(), any());
		verify(automationExecutionDao, times(1)).createExecution(eq(element), eq(elementType), eq(rule),
				eq(AutomationExecutionResult.UNCHANGED));
	}

	@Test
	public void testContentStatusChange_noEntries_writesUnchanged() {
		CurriculumElement element = mock(CurriculumElement.class);
		when(element.getKey()).thenReturn(1L);
		when(element.getElementStatus()).thenReturn(CurriculumElementStatus.active);
		when(curriculumService.loadAutomationCandidates()).thenReturn(List.of(element));
		when(curriculumService.getCurriculumElement(element)).thenReturn(element);

		CurriculumAutomationRule rule = new CurriculumAutomationRuleImpl();
		rule.setDependingOn(AutomationDependingOn.STATUS);
		rule.setDependingOnStatus(Set.of(CurriculumElementStatus.active.name()));
		rule.setContext(AutomationContext.CONTENT);
		rule.setAutomationType(AutomationType.STATUS_CHANGE);
		rule.setTargetStatus(RepositoryEntryStatusEnum.published);
		CurriculumAutomationConfig config = mockConfig(rule, true);
		CurriculumElementType elementType = mock(CurriculumElementType.class);
		when(element.getType()).thenReturn(elementType);
		when(config.getElementType()).thenReturn(elementType);
		when(automationConfigDao.getConfigsByCurriculumElements(List.of(element))).thenReturn(List.of());
		when(automationConfigDao.getConfigsByElementTypes(Set.of(elementType))).thenReturn(List.of(config));
		when(automationExecutionDao.getExecutedRuleIdentifiers(List.of(element))).thenReturn(new HashMap<>());

		sut.execute();

		verify(repositoryManager, never()).setStatus(any(), any());
		verify(automationExecutionDao, times(1)).createExecution(eq(element), eq(elementType), eq(rule),
				eq(AutomationExecutionResult.UNCHANGED));
	}

	private CurriculumAutomationConfig mockConfig(CurriculumAutomationRule rule, boolean enabled) {
		CurriculumAutomationConfig config = mock(CurriculumAutomationConfig.class);
		when(config.getRule()).thenReturn(rule);
		when(config.isEnabled()).thenReturn(enabled);
		return config;
	}

}
