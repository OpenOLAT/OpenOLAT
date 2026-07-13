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
import org.olat.modules.curriculum.AutomationExecutionResult;
import org.olat.modules.curriculum.CurriculumAutomationExecution;
import org.olat.modules.curriculum.CurriculumAutomationRule;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.model.CurriculumAutomationExecutionImpl;
import org.olat.modules.curriculum.model.CurriculumAutomationRuleImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Initial date: 2026-07-10<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 */
@Service
public class CurriculumAutomationExecutionDAO {

	@Autowired
	private DB dbInstance;

	public CurriculumAutomationExecutionImpl createExecution(CurriculumElement element, CurriculumElementType type,
			CurriculumAutomationRule sourceRule, AutomationExecutionResult result) {
		Date now = new Date();

		CurriculumAutomationRuleImpl rule = CurriculumAutomationRuleImpl.copyOf(sourceRule, now);

		CurriculumAutomationExecutionImpl execution = new CurriculumAutomationExecutionImpl();
		execution.setCreationDate(now);
		execution.setExecutionDate(now);
		execution.setCurriculumElementKey(element.getKey());
		execution.setElementTypeKey(type != null ? type.getKey() : null);
		execution.setResult(result);
		execution.setRule(rule);

		dbInstance.getCurrentEntityManager().persist(execution);
		return execution;
	}

	public List<CurriculumAutomationExecution> getExecutions(Collection<CurriculumElement> elements) {
		if (elements == null || elements.isEmpty()) {
			return new ArrayList<>(0);
		}

		String query = """
				select exec from curriculumautomationexecution exec
				inner join fetch exec.rule rule
				where exec.curriculumElementKey in :keys""";

		List<Long> keys = elements.stream().map(CurriculumElement::getKey).collect(Collectors.toList());
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, CurriculumAutomationExecution.class)
				.setParameter("keys", keys)
				.getResultList();
	}
}
