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

import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.components.date.OffsetDirection;
import org.olat.core.logging.Tracing;
import org.olat.core.util.DateUtils;
import org.olat.modules.curriculum.AutomationContext;
import org.olat.modules.curriculum.AutomationDependingOn;
import org.olat.modules.curriculum.AutomationType;
import org.olat.modules.curriculum.AutomationUnit;
import org.olat.modules.curriculum.CurriculumAutomationConfig;
import org.olat.modules.curriculum.CurriculumAutomationRule;
import org.olat.modules.curriculum.CurriculumAutomationService;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Initial date: 2026-06-26<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 */
@Service
public class CurriculumAutomationServiceImpl implements CurriculumAutomationService {

	private static final Logger log = Tracing.createLoggerFor(CurriculumAutomationServiceImpl.class);

	@Autowired
	private DB dbInstance;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private RepositoryService repositoryService;

	@Override
	public CurriculumAutomationConfig getDefaultConfig(boolean implOnly, int maxRepositoryEntryRelations) {
		return CurriculumAutomationStandardRules.createStandardConfig(implOnly, maxRepositoryEntryRelations);
	}

	@Override
	public void execute() {
		log.info("Start curriculum automation");
		Date today = DateUtils.getStartOfDay(new Date());
		List<CurriculumElement> candidates = curriculumService.loadAutomationCandidates();
		for (CurriculumElement element : candidates) {
			processElement(element, today);
			dbInstance.commitAndCloseSession();
		}
		dbInstance.commitAndCloseSession();
		log.info("Curriculum automation done");
	}

	private void processElement(CurriculumElement element, Date today) {
		CurriculumElementType type = element.getType();
		CurriculumAutomationConfig elementConfig = element.getAutomationConfig();
		CurriculumAutomationConfig config = elementConfig != null && elementConfig.getRules() != null
				? elementConfig
				: (type != null ? type.getAutomationConfig() : null);
		if (config == null || config.getRules() == null) {
			return;
		}
		for (CurriculumAutomationRule rule : config.getRules()) {
			if (!rule.isEnabled()) {
				continue;
			}
			element = processRule(element, rule, today);
		}
	}

	CurriculumElement processRule(CurriculumElement element, CurriculumAutomationRule rule, Date today) {
		if (rule.getDependingOn() == AutomationDependingOn.EXECUTION_PERIOD) {
			Date referenceDate = computeTriggerDate(element, rule);
			if (referenceDate == null) {
				return element;
			}
			if (referenceDate.after(today)) {
				return element;
			}
		} else {
			if (rule.getDependingOnStatus() == null || rule.getDependingOnStatus().isEmpty()) {
				return element;
			}
			String currentStatus = element.getElementStatus() != null ? element.getElementStatus().name() : null;
			if (!rule.getDependingOnStatus().contains(currentStatus)) {
				return element;
			}
		}

		if (rule.getOnlyWhenStatus() != null && !rule.getOnlyWhenStatus().isEmpty()) {
			String currentStatus = element.getElementStatus() != null ? element.getElementStatus().name() : null;
			if (!rule.getOnlyWhenStatus().contains(currentStatus)) {
				return element;
			}
		}

		AutomationContext ctx = rule.getContext();
		AutomationType type = rule.getAutomationType();
		if ((ctx == AutomationContext.IMPLEMENTATION || ctx == AutomationContext.ELEMENT)
				&& type == AutomationType.STATUS_CHANGE) {
			element = applyElementStatusChange(element, rule);
		} else if (ctx == AutomationContext.CONTENT && type == AutomationType.INSTANTIATION) {
			applyContentInstantiation(element);
		} else if (ctx == AutomationContext.CONTENT && type == AutomationType.STATUS_CHANGE) {
			applyContentStatusChange(element, rule);
		}
		return element;
	}

	@Override
	public Date computeTriggerDate(CurriculumElement element, CurriculumAutomationRule rule) {
		Date referenceDate;
		String ref = rule.getReference();
		boolean referenceEnd;
		if (ref != null) {
			referenceEnd = CurriculumAutomationRule.REFERENCE_END.equals(ref);
			referenceDate = referenceEnd ? getEndDate(element) : getBeginDate(element);
		} else if (rule.getDirection() == OffsetDirection.AFTER) {
			referenceEnd = true;
			referenceDate = getEndDate(element);
		} else {
			referenceEnd = false;
			referenceDate = getBeginDate(element);
		}
		if (referenceDate == null) {
			return null;
		}
		boolean finishAtEndOfDay = referenceEnd && CurriculumElementStatus.finished.name().equals(rule.getTargetStatus());
		referenceDate = finishAtEndOfDay ? DateUtils.getEndOfDay(referenceDate) : DateUtils.getStartOfDay(referenceDate);
		AutomationUnit unit = rule.getUnit();
		if (unit == null || unit == AutomationUnit.SAME_DAY) {
			return referenceDate;
		}
		Integer value = rule.getValue();
		if (value == null) {
			return referenceDate;
		}
		if (rule.getDirection() == OffsetDirection.BEFORE) {
			return unit.before(referenceDate, value);
		}
		return unit.after(referenceDate, value);
	}

	private Date getBeginDate(CurriculumElement element) {
		if (element.getBeginDate() != null) {
			return element.getBeginDate();
		}
		List<CurriculumElement> parentLine = curriculumService.getCurriculumElementParentLine(element);
		for (int i = parentLine.size(); i-- > 0; ) {
			CurriculumElement parent = parentLine.get(i);
			if (parent.getBeginDate() != null) {
				return parent.getBeginDate();
			}
		}
		return null;
	}

	private Date getEndDate(CurriculumElement element) {
		if (element.getEndDate() != null) {
			return element.getEndDate();
		}
		List<CurriculumElement> parentLine = curriculumService.getCurriculumElementParentLine(element);
		for (int i = parentLine.size(); i-- > 0; ) {
			CurriculumElement parent = parentLine.get(i);
			if (parent.getEndDate() != null) {
				return parent.getEndDate();
			}
		}
		return null;
	}

	private CurriculumElement applyElementStatusChange(CurriculumElement element, CurriculumAutomationRule rule) {
		if (!CurriculumElementStatus.isValueOf(rule.getTargetStatus())) {
			return element;
		}
		CurriculumElementStatus targetStatus = CurriculumElementStatus.valueOf(rule.getTargetStatus());
		if (element.getElementStatus() == targetStatus) {
			return element;
		}
		curriculumService.updateCurriculumElementStatus(null, element, targetStatus, false, null);
		log.info("Automation: element status changed to {} for element {} (key={})",
				targetStatus, element.getDisplayName(), element.getKey());
		return curriculumService.getCurriculumElement(element);
	}

	private void applyContentInstantiation(CurriculumElement element) {
		CurriculumElementType type = element.getType();
		int max = type != null ? type.getMaxRepositoryEntryRelations() : -1;
		long count = curriculumService.countRepositoryEntries(element);
		if (max >= 0 && count >= max) {
			return;
		}
		List<RepositoryEntry> templates = curriculumService.getRepositoryTemplates(element);
		for (RepositoryEntry template : templates) {
			if (max >= 0 && count >= max) {
				break;
			}
			String externalRef = element.getIdentifier() != null ? element.getIdentifier() : "";
			curriculumService.instantiateTemplate(template, element,
					element.getDisplayName(), externalRef,
					element.getBeginDate(), element.getEndDate(), null);
			log.info("Automation: instantiated template {} (key={}) for element {} (key={})",
					template.getDisplayname(), template.getKey(), element.getDisplayName(), element.getKey());
			count++;
		}
	}

	private void applyContentStatusChange(CurriculumElement element, CurriculumAutomationRule rule) {
		if (!RepositoryEntryStatusEnum.isValid(rule.getTargetStatus())) {
			return;
		}
		RepositoryEntryStatusEnum targetStatus = RepositoryEntryStatusEnum.valueOf(rule.getTargetStatus());
		List<RepositoryEntry> entries = curriculumService.getRepositoryEntries(element);
		for (RepositoryEntry entry : entries) {
			RepositoryEntryStatusEnum current = entry.getEntryStatus();
			if (current == targetStatus) {
				continue;
			}
			if (targetStatus == RepositoryEntryStatusEnum.closed) {
				repositoryService.closeRepositoryEntry(entry, null, true);
			} else {
				repositoryManager.setStatus(entry, targetStatus);
			}
			log.info("Automation: content status changed to {} for entry {} (key={}) in element {} (key={})",
					targetStatus, entry.getDisplayname(), entry.getKey(), element.getDisplayName(), element.getKey());
		}
	}

}
