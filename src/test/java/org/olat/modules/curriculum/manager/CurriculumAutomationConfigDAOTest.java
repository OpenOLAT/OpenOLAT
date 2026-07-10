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
import java.util.Set;

import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.components.date.OffsetDirection;
import org.olat.modules.curriculum.AutomationContext;
import org.olat.modules.curriculum.AutomationDependingOn;
import org.olat.modules.curriculum.AutomationType;
import org.olat.modules.curriculum.AutomationUnit;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumAutomationConfig;
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
public class CurriculumAutomationConfigDAOTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private CurriculumAutomationConfigDAO curriculumAutomationConfigDao;
	@Autowired
	private CurriculumElementTypeDAO curriculumElementTypeDao;
	@Autowired
	private CurriculumElementDAO curriculumElementDao;
	@Autowired
	private CurriculumDAO curriculumDao;

	@Test
	public void createAndReload_typeConfig() {
		CurriculumElementType type = curriculumElementTypeDao.createCurriculumElementType(random(), random(), random(), random());
		dbInstance.commitAndCloseSession();

		CurriculumAutomationRule rule = new CurriculumAutomationRuleImpl();
		rule.setContext(AutomationContext.IMPLEMENTATION);
		rule.setAutomationType(AutomationType.STATUS_CHANGE);
		rule.setTargetStatus("confirmed");
		rule.setDependingOn(AutomationDependingOn.EXECUTION_PERIOD);
		rule.setValue(14);
		rule.setUnit(AutomationUnit.DAYS);
		rule.setDirection(OffsetDirection.BEFORE);
		rule.setOnlyWhenStatus(Set.of("preparation"));

		CurriculumAutomationConfig config = curriculumAutomationConfigDao.createConfig(type, null, rule, true);
		dbInstance.commitAndCloseSession();

		assertThat(config).isNotNull();
		assertThat(config.getKey()).isNotNull();
		assertThat(config.getCreationDate()).isNotNull();
		assertThat(config.getLastModified()).isNotNull();

		List<CurriculumAutomationConfig> reloaded = curriculumAutomationConfigDao.getConfigs(type);
		assertThat(reloaded).hasSize(1);
		CurriculumAutomationConfig reloadedConfig = reloaded.get(0);
		assertThat(reloadedConfig.isEnabled()).isTrue();
		assertThat(reloadedConfig.getElementType().getKey()).isEqualTo(type.getKey());
		assertThat(reloadedConfig.getCurriculumElement()).isNull();

		CurriculumAutomationRule reloadedRule = reloadedConfig.getRule();
		assertThat(reloadedRule.getContext()).isEqualTo(AutomationContext.IMPLEMENTATION);
		assertThat(reloadedRule.getAutomationType()).isEqualTo(AutomationType.STATUS_CHANGE);
		assertThat(reloadedRule.getTargetStatus()).isEqualTo("confirmed");
		assertThat(reloadedRule.getDependingOn()).isEqualTo(AutomationDependingOn.EXECUTION_PERIOD);
		assertThat(reloadedRule.getValue()).isEqualTo(14);
		assertThat(reloadedRule.getUnit()).isEqualTo(AutomationUnit.DAYS);
		assertThat(reloadedRule.getDirection()).isEqualTo(OffsetDirection.BEFORE);
		assertThat(reloadedRule.getOnlyWhenStatus()).containsExactly("preparation");
	}

	@Test
	public void createAndReload_curriculumElementConfig() {
		Curriculum curriculum = curriculumDao.createAndPersist(random(), random(), random(), false, null);
		CurriculumElement element = curriculumElementDao.createCurriculumElement(random(), random(),
				CurriculumElementStatus.active, null, null, null, null, null, null, null, curriculum);
		dbInstance.commitAndCloseSession();

		CurriculumAutomationRule rule = new CurriculumAutomationRuleImpl();
		rule.setContext(AutomationContext.ELEMENT);
		rule.setAutomationType(AutomationType.STATUS_CHANGE);
		rule.setTargetStatus("finished");

		curriculumAutomationConfigDao.createConfig(null, element, rule, false);
		dbInstance.commitAndCloseSession();

		List<CurriculumAutomationConfig> reloaded = curriculumAutomationConfigDao.getConfigs(element);
		assertThat(reloaded).hasSize(1);
		CurriculumAutomationConfig reloadedConfig = reloaded.get(0);
		assertThat(reloadedConfig.isEnabled()).isFalse();
		assertThat(reloadedConfig.getCurriculumElement().getKey()).isEqualTo(element.getKey());
		assertThat(reloadedConfig.getElementType()).isNull();
		assertThat(reloadedConfig.getRule().getTargetStatus()).isEqualTo("finished");
	}

	@Test
	public void getConfigsByElementType_multipleRows() {
		CurriculumElementType type = curriculumElementTypeDao.createCurriculumElementType(random(), random(), random(), random());
		dbInstance.commitAndCloseSession();

		curriculumAutomationConfigDao.createConfig(type, null, new CurriculumAutomationRuleImpl(), true);
		curriculumAutomationConfigDao.createConfig(type, null, new CurriculumAutomationRuleImpl(), false);
		dbInstance.commitAndCloseSession();

		List<CurriculumAutomationConfig> configs = curriculumAutomationConfigDao.getConfigs(type);
		assertThat(configs).hasSize(2);
	}

	@Test
	public void getConfigsByElementType_noRows() {
		CurriculumElementType type = curriculumElementTypeDao.createCurriculumElementType(random(), random(), random(), random());
		dbInstance.commitAndCloseSession();

		List<CurriculumAutomationConfig> configs = curriculumAutomationConfigDao.getConfigs(type);
		assertThat(configs).isEmpty();
	}

	@Test
	public void getConfigsByCurriculumElement_noRows() {
		Curriculum curriculum = curriculumDao.createAndPersist(random(), random(), random(), false, null);
		CurriculumElement element = curriculumElementDao.createCurriculumElement(random(), random(),
				CurriculumElementStatus.active, null, null, null, null, null, null, null, curriculum);
		dbInstance.commitAndCloseSession();

		List<CurriculumAutomationConfig> configs = curriculumAutomationConfigDao.getConfigs(element);
		assertThat(configs).isEmpty();
	}

	@Test
	public void update_bumpsLastModifiedAndPersistsEnabled() {
		CurriculumElementType type = curriculumElementTypeDao.createCurriculumElementType(random(), random(), random(), random());
		dbInstance.commitAndCloseSession();

		CurriculumAutomationConfig config = curriculumAutomationConfigDao.createConfig(type, null, new CurriculumAutomationRuleImpl(), true);
		dbInstance.commitAndCloseSession();

		config.setEnabled(false);
		curriculumAutomationConfigDao.update(config);
		dbInstance.commitAndCloseSession();

		List<CurriculumAutomationConfig> reloaded = curriculumAutomationConfigDao.getConfigs(type);
		assertThat(reloaded).hasSize(1);
		assertThat(reloaded.get(0).isEnabled()).isFalse();
	}

	@Test
	public void deleteConfigsByElementType() {
		CurriculumElementType type = curriculumElementTypeDao.createCurriculumElementType(random(), random(), random(), random());
		dbInstance.commitAndCloseSession();

		curriculumAutomationConfigDao.createConfig(type, null, new CurriculumAutomationRuleImpl(), true);
		curriculumAutomationConfigDao.createConfig(type, null, new CurriculumAutomationRuleImpl(), true);
		dbInstance.commitAndCloseSession();

		curriculumAutomationConfigDao.deleteConfigs(type);
		dbInstance.commitAndCloseSession();

		List<CurriculumAutomationConfig> reloaded = curriculumAutomationConfigDao.getConfigs(type);
		assertThat(reloaded).isEmpty();
	}

	@Test
	public void deleteConfigsByCurriculumElement() {
		Curriculum curriculum = curriculumDao.createAndPersist(random(), random(), random(), false, null);
		CurriculumElement element = curriculumElementDao.createCurriculumElement(random(), random(),
				CurriculumElementStatus.active, null, null, null, null, null, null, null, curriculum);
		dbInstance.commitAndCloseSession();

		curriculumAutomationConfigDao.createConfig(null, element, new CurriculumAutomationRuleImpl(), true);
		dbInstance.commitAndCloseSession();

		curriculumAutomationConfigDao.deleteConfigs(element);
		dbInstance.commitAndCloseSession();

		List<CurriculumAutomationConfig> reloaded = curriculumAutomationConfigDao.getConfigs(element);
		assertThat(reloaded).isEmpty();
	}
}
