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
import static org.olat.test.JunitTestHelper.random;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.components.date.OffsetDirection;
import org.olat.modules.curriculum.AutomationContext;
import org.olat.modules.curriculum.AutomationDependingOn;
import org.olat.modules.curriculum.AutomationExecutionResult;
import org.olat.modules.curriculum.AutomationType;
import org.olat.modules.curriculum.AutomationUnit;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumAutomationExecution;
import org.olat.modules.curriculum.CurriculumAutomationRule;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.model.CurriculumAutomationRuleImpl;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2026-07-10<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 */
public class CurriculumAutomationExecutionDAOTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private CurriculumAutomationExecutionDAO curriculumAutomationExecutionDao;
	@Autowired
	private CurriculumElementTypeDAO curriculumElementTypeDao;
	@Autowired
	private CurriculumElementDAO curriculumElementDao;
	@Autowired
	private CurriculumDAO curriculumDao;

	@Test
	public void createExecution_elementLevel_copiesRule() {
		Curriculum curriculum = curriculumDao.createAndPersist(random(), random(), random(), false, null);
		CurriculumElement element = curriculumElementDao.createCurriculumElement(random(), random(),
				CurriculumElementStatus.active, null, null, null, null, null, null, null, curriculum);
		dbInstance.commitAndCloseSession();

		CurriculumAutomationRule sourceRule = createRule();

		CurriculumAutomationExecution execution = curriculumAutomationExecutionDao.createExecution(element, null, sourceRule, AutomationExecutionResult.CHANGED);
		dbInstance.commitAndCloseSession();

		assertThat(execution).isNotNull();
		assertThat(execution.getKey()).isNotNull();
		assertThat(execution.getCreationDate()).isNotNull();
		assertThat(execution.getExecutionDate()).isNotNull();
		assertThat(execution.getCurriculumElementKey()).isEqualTo(element.getKey());
		assertThat(execution.getElementTypeKey()).isNull();
		assertThat(execution.getResult()).isEqualTo(AutomationExecutionResult.CHANGED);

		CurriculumAutomationRule copiedRule = execution.getRule();
		assertThat(copiedRule).isNotNull();
		assertThat(copiedRule.getKey()).isNotEqualTo(sourceRule.getKey());
		assertThat(copiedRule.getContext()).isEqualTo(sourceRule.getContext());
		assertThat(copiedRule.getAutomationType()).isEqualTo(sourceRule.getAutomationType());
		assertThat(copiedRule.getTargetStatus()).isEqualTo(sourceRule.getTargetStatus());
		assertThat(copiedRule.getDependingOn()).isEqualTo(sourceRule.getDependingOn());
		assertThat(copiedRule.getReference()).isEqualTo(sourceRule.getReference());
		assertThat(copiedRule.getValue()).isEqualTo(sourceRule.getValue());
		assertThat(copiedRule.getUnit()).isEqualTo(sourceRule.getUnit());
		assertThat(copiedRule.getDirection()).isEqualTo(sourceRule.getDirection());
		assertThat(copiedRule.getDependingOnStatus()).containsExactlyInAnyOrderElementsOf(sourceRule.getDependingOnStatus());
		assertThat(copiedRule.getOnlyWhenStatus()).containsExactlyInAnyOrderElementsOf(sourceRule.getOnlyWhenStatus());
	}

	@Test
	public void createExecution_typeLevel_setsElementTypeKey() {
		Curriculum curriculum = curriculumDao.createAndPersist(random(), random(), random(), false, null);
		CurriculumElementType type = curriculumElementTypeDao.createCurriculumElementType(random(), random(), random(), random());
		CurriculumElement element = curriculumElementDao.createCurriculumElement(random(), random(),
				CurriculumElementStatus.active, null, null, null, type, null, null, null, curriculum);
		dbInstance.commitAndCloseSession();

		CurriculumAutomationRule sourceRule = createRule();

		CurriculumAutomationExecution execution = curriculumAutomationExecutionDao.createExecution(element, type, sourceRule, AutomationExecutionResult.UNCHANGED);
		dbInstance.commitAndCloseSession();

		assertThat(execution.getElementTypeKey()).isEqualTo(type.getKey());
		assertThat(execution.getCurriculumElementKey()).isEqualTo(element.getKey());
		assertThat(execution.getResult()).isEqualTo(AutomationExecutionResult.UNCHANGED);
	}

	@Test
	public void getExecutedRuleIdentifiers_groupsByElement() {
		Curriculum curriculum = curriculumDao.createAndPersist(random(), random(), random(), false, null);
		CurriculumElement element1 = curriculumElementDao.createCurriculumElement(random(), random(),
				CurriculumElementStatus.active, null, null, null, null, null, null, null, curriculum);
		CurriculumElement element2 = curriculumElementDao.createCurriculumElement(random(), random(),
				CurriculumElementStatus.active, null, null, null, null, null, null, null, curriculum);
		dbInstance.commitAndCloseSession();

		CurriculumAutomationRule rule1 = createRule();
		rule1.setContext(AutomationContext.ELEMENT);
		rule1.setAutomationType(AutomationType.STATUS_CHANGE);
		rule1.setTargetStatus(CurriculumElementStatus.confirmed);
		curriculumAutomationExecutionDao.createExecution(element1, null, rule1, AutomationExecutionResult.CHANGED);

		CurriculumAutomationRule rule2 = createRule();
		rule2.setContext(AutomationContext.CONTENT);
		rule2.setAutomationType(AutomationType.INSTANTIATION);
		rule2.setTargetStatus((String) null);
		curriculumAutomationExecutionDao.createExecution(element2, null, rule2, AutomationExecutionResult.CHANGED);
		dbInstance.commitAndCloseSession();

		Map<Long, Set<String>> executedByElement = curriculumAutomationExecutionDao.getExecutedRuleIdentifiers(List.of(element1, element2));

		assertThat(executedByElement).containsOnlyKeys(element1.getKey(), element2.getKey());
		assertThat(executedByElement.get(element1.getKey())).containsExactly("ELEMENT::STATUS_CHANGE::confirmed");
		assertThat(executedByElement.get(element2.getKey())).containsExactly("CONTENT::INSTANTIATION::");
	}

	@Test
	public void getExecutedRuleIdentifiers_emptyCollection_returnsEmptyMap() {
		assertThat(curriculumAutomationExecutionDao.getExecutedRuleIdentifiers(List.of())).isEmpty();
	}

	@Test
	public void getExecutedRuleIdentifiers_nullCollection_returnsEmptyMap() {
		assertThat(curriculumAutomationExecutionDao.getExecutedRuleIdentifiers(null)).isEmpty();
	}

	private CurriculumAutomationRule createRule() {
		CurriculumAutomationRule rule = new CurriculumAutomationRuleImpl();
		rule.setContext(AutomationContext.IMPLEMENTATION);
		rule.setAutomationType(AutomationType.STATUS_CHANGE);
		rule.setTargetStatus("confirmed");
		rule.setDependingOn(AutomationDependingOn.EXECUTION_PERIOD);
		rule.setReference(CurriculumAutomationRule.REFERENCE_BEGIN);
		rule.setValue(7);
		rule.setUnit(AutomationUnit.DAYS);
		rule.setDirection(OffsetDirection.BEFORE);
		rule.setDependingOnStatus(Set.of("preparation", "provisional"));
		rule.setOnlyWhenStatus(Set.of("preparation"));
		return rule;
	}
}
