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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.commons.persistence.DB;
import org.olat.modules.curriculum.CurriculumAutomationConfig;
import org.olat.modules.curriculum.CurriculumAutomationRule;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.model.CurriculumAutomationConfigImpl;
import org.olat.modules.curriculum.model.CurriculumAutomationRuleImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Initial date: 2026-07-10<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 */
@Service
public class CurriculumAutomationConfigDAO {

	@Autowired
	private DB dbInstance;

	public CurriculumAutomationConfig createConfig(CurriculumElementType type, CurriculumElement element,
			CurriculumAutomationRule sourceRule, boolean enabled) {
		Date now = new Date();

		CurriculumAutomationRuleImpl rule = CurriculumAutomationRuleImpl.copyOf(sourceRule, now);

		CurriculumAutomationConfigImpl config = new CurriculumAutomationConfigImpl();
		config.setCreationDate(now);
		config.setLastModified(now);
		config.setElementType(type);
		config.setCurriculumElement(element);
		config.setRule(rule);
		config.setEnabled(enabled);
		dbInstance.getCurrentEntityManager().persist(config);
		return config;
	}

	public List<CurriculumAutomationConfig> getConfigs(CurriculumElementType type) {
		String query = """
				select config from curriculumautomationconfig config
				left join fetch config.rule rule
				where config.elementType.key=:typeKey
				order by config.key""";

		return dbInstance.getCurrentEntityManager()
				.createQuery(query, CurriculumAutomationConfig.class)
				.setParameter("typeKey", type.getKey())
				.getResultList();
	}

	public List<CurriculumAutomationConfig> getConfigs(CurriculumElement element) {
		String query = """
				select config from curriculumautomationconfig config
				left join fetch config.rule rule
				where config.curriculumElement.key=:elementKey
				order by config.key""";

		return dbInstance.getCurrentEntityManager()
				.createQuery(query, CurriculumAutomationConfig.class)
				.setParameter("elementKey", element.getKey())
				.getResultList();
	}

	public List<CurriculumAutomationConfig> getConfigsByElementTypes(Collection<CurriculumElementType> types) {
		if (types == null || types.isEmpty()) return new ArrayList<>(0);

		String query = """
				select config from curriculumautomationconfig config
				left join fetch config.rule rule
				where config.elementType.key in :keys
				order by config.key""";

		List<Long> keys = types.stream().map(CurriculumElementType::getKey).collect(Collectors.toList());
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, CurriculumAutomationConfig.class)
				.setParameter("keys", keys)
				.getResultList();
	}

	public List<CurriculumAutomationConfig> getConfigsByCurriculumElements(Collection<CurriculumElement> elements) {
		if (elements == null || elements.isEmpty()) return new ArrayList<>(0);

		String query = """
				select config from curriculumautomationconfig config
				left join fetch config.rule rule
				where config.curriculumElement.key in :keys
				order by config.key""";

		List<Long> keys = elements.stream().map(CurriculumElement::getKey).collect(Collectors.toList());
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, CurriculumAutomationConfig.class)
				.setParameter("keys", keys)
				.getResultList();
	}

	public CurriculumAutomationConfig update(CurriculumAutomationConfig config) {
		((CurriculumAutomationConfigImpl)config).setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(config);
	}

	public void deleteConfigs(CurriculumElementType type) {
		String ruleKeysQuery = "select config.rule.key from curriculumautomationconfig config where config.elementType.key=:typeKey";
		List<Long> ruleKeys = dbInstance.getCurrentEntityManager()
				.createQuery(ruleKeysQuery, Long.class)
				.setParameter("typeKey", type.getKey())
				.getResultList();

		String deleteConfigsQuery = "delete from curriculumautomationconfig config where config.elementType.key=:typeKey";
		dbInstance.getCurrentEntityManager()
				.createQuery(deleteConfigsQuery)
				.setParameter("typeKey", type.getKey())
				.executeUpdate();

		deleteRules(ruleKeys);
	}

	public void deleteConfigs(CurriculumElement element) {
		String ruleKeysQuery = "select config.rule.key from curriculumautomationconfig config where config.curriculumElement.key=:elementKey";
		List<Long> ruleKeys = dbInstance.getCurrentEntityManager()
				.createQuery(ruleKeysQuery, Long.class)
				.setParameter("elementKey", element.getKey())
				.getResultList();

		String deleteConfigsQuery = "delete from curriculumautomationconfig config where config.curriculumElement.key=:elementKey";
		dbInstance.getCurrentEntityManager()
				.createQuery(deleteConfigsQuery)
				.setParameter("elementKey", element.getKey())
				.executeUpdate();

		deleteRules(ruleKeys);
	}

	private void deleteRules(List<Long> ruleKeys) {
		if (ruleKeys == null || ruleKeys.isEmpty()) {
			return;
		}
		String query = "delete from curriculumautomationrule rule where rule.key in :keys";
		dbInstance.getCurrentEntityManager()
				.createQuery(query)
				.setParameter("keys", ruleKeys)
				.executeUpdate();
	}
}
