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
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.components.date.OffsetDirection;
import org.olat.core.logging.Tracing;
import org.olat.core.util.DateUtils;
import org.olat.modules.curriculum.AutomationContext;
import org.olat.modules.curriculum.AutomationDependingOn;
import org.olat.modules.curriculum.AutomationExecutionResult;
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
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
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
	@Autowired
	private Scheduler scheduler;
	@Autowired
	private CurriculumAutomationConfigDAO automationConfigDao;
	@Autowired
	private CurriculumAutomationExecutionDAO automationExecutionDao;

	@Override
	public List<CurriculumAutomationConfig> getDefaultConfig(boolean implOnly, int maxRepositoryEntryRelations) {
		return CurriculumAutomationStandardRules.createStandardConfig(implOnly, maxRepositoryEntryRelations);
	}

	@Override
	public List<CurriculumAutomationConfig> getConfigs(CurriculumElementType type) {
		return automationConfigDao.getConfigs(type);
	}

	@Override
	public List<CurriculumAutomationConfig> getConfigs(CurriculumElement element) {
		return automationConfigDao.getConfigs(element);
	}

	@Override
	public List<CurriculumAutomationConfig> updateConfigs(CurriculumElementType type, List<CurriculumAutomationConfig> configs) {
		automationConfigDao.deleteConfigs(type);
		return persistConfigs(type, null, configs);
	}

	@Override
	public List<CurriculumAutomationConfig> updateConfigs(CurriculumElement element, List<CurriculumAutomationConfig> configs) {
		automationConfigDao.deleteConfigs(element);
		return persistConfigs(null, element, configs);
	}

	private List<CurriculumAutomationConfig> persistConfigs(CurriculumElementType type, CurriculumElement element,
			List<CurriculumAutomationConfig> configs) {
		List<CurriculumAutomationConfig> persisted = new ArrayList<>();
		if (configs == null) {
			return persisted;
		}
		for (CurriculumAutomationConfig config : configs) {
			persisted.add(automationConfigDao.createConfig(type, element, config.getRule(), config.isEnabled()));
		}
		return persisted;
	}

	@Override
	public void execute() {
		log.info("Start curriculum automation");
		Date today = DateUtils.getStartOfDay(new Date());
		List<CurriculumElement> candidates = curriculumService.loadAutomationCandidates();

		Set<CurriculumElementType> types = candidates.stream()
				.map(CurriculumElement::getType)
				.filter(type -> type != null)
				.collect(Collectors.toSet());
		Map<Long, List<CurriculumAutomationConfig>> configsByElementKey = automationConfigDao.getConfigsByCurriculumElements(candidates).stream()
				.collect(Collectors.groupingBy(config -> config.getCurriculumElement().getKey()));
		Map<Long, List<CurriculumAutomationConfig>> configsByTypeKey = automationConfigDao.getConfigsByElementTypes(types).stream()
				.collect(Collectors.groupingBy(config -> config.getElementType().getKey()));
		Map<Long, Set<String>> executedByElement = automationExecutionDao.getExecutedRuleIdentifiers(candidates);
		if (executedByElement == null) {
			executedByElement = new HashMap<>();
		}

		for (CurriculumElement element : candidates) {
			processElement(element, today, configsByElementKey, configsByTypeKey, executedByElement, rule -> true);
			dbInstance.commitAndCloseSession();
		}
		dbInstance.commitAndCloseSession();
		log.info("Curriculum automation done");
	}

	@Override
	public void processStatusChange(Collection<CurriculumElement> elements) {
		if (elements == null || elements.isEmpty()) {
			return;
		}
		Date today = DateUtils.getStartOfDay(new Date());
		List<CurriculumElement> elementList = elements.stream()
				.map(curriculumService::getCurriculumElement)
				.filter(element -> element != null)
				.toList();
		if (elementList.isEmpty()) {
			return;
		}
		Set<CurriculumElementType> types = elementList.stream()
				.map(CurriculumElement::getType)
				.filter(type -> type != null)
				.collect(Collectors.toSet());
		Map<Long, List<CurriculumAutomationConfig>> configsByElementKey = automationConfigDao.getConfigsByCurriculumElements(elementList).stream()
				.collect(Collectors.groupingBy(config -> config.getCurriculumElement().getKey()));
		Map<Long, List<CurriculumAutomationConfig>> configsByTypeKey = automationConfigDao.getConfigsByElementTypes(types).stream()
				.collect(Collectors.groupingBy(config -> config.getElementType().getKey()));
		Map<Long, Set<String>> executedByElement = automationExecutionDao.getExecutedRuleIdentifiers(elementList);
		if (executedByElement == null) {
			executedByElement = new HashMap<>();
		}
		for (CurriculumElement element : elementList) {
			processElement(element, today, configsByElementKey, configsByTypeKey, executedByElement,
					rule -> rule.getDependingOn() != AutomationDependingOn.EXECUTION_PERIOD);
			dbInstance.commitAndCloseSession();
		}
	}

	private void processElement(CurriculumElement element, Date today, Map<Long, List<CurriculumAutomationConfig>> configsByElementKey,
			Map<Long, List<CurriculumAutomationConfig>> configsByTypeKey, Map<Long, Set<String>> executedByElement,
			Predicate<CurriculumAutomationRule> ruleFilter) {
		CurriculumElementType type = element.getType();
		List<CurriculumAutomationConfig> elementConfigs = configsByElementKey.getOrDefault(element.getKey(), List.of());
		boolean fromType = elementConfigs.isEmpty();
		List<CurriculumAutomationConfig> configs = !fromType
				? elementConfigs
				: (type != null ? configsByTypeKey.getOrDefault(type.getKey(), List.of()) : List.of());
		CurriculumElementType originType = fromType ? type : null;
		Set<String> executedIdentifiers = executedByElement.computeIfAbsent(element.getKey(), key -> new HashSet<>());
		List<CurriculumAutomationConfig> orderedConfigs = configs.stream()
				.sorted(Comparator.comparingInt(config -> ruleOrder(config.getRule())))
				.toList();

		for (CurriculumAutomationConfig config : orderedConfigs) {
			if (!config.isEnabled()) {
				continue;
			}
			CurriculumAutomationRule rule = config.getRule();
			if (!ruleFilter.test(rule)) {
				continue;
			}
			String identifier = ruleIdentifier(rule);
			if (executedIdentifiers.contains(identifier)) {
				continue;
			}
			RuleOutcome outcome = processRule(element, rule, today);
			element = outcome.element();
			if (outcome.result() != null) {
				automationExecutionDao.createExecution(element, originType, rule, outcome.result());
				executedIdentifiers.add(identifier);
			}
		}
	}

	private String ruleIdentifier(CurriculumAutomationRule rule) {
		return rule.getContext() + "::" + rule.getAutomationType() + "::" + (rule.getTargetStatus() == null ? "" : rule.getTargetStatus());
	}

	private int ruleOrder(CurriculumAutomationRule rule) {
		AutomationContext ctx = rule.getContext();
		AutomationType type = rule.getAutomationType();
		if ((ctx == AutomationContext.IMPLEMENTATION || ctx == AutomationContext.ELEMENT)
				&& type == AutomationType.STATUS_CHANGE) {
			return 1;
		}
		if (ctx == AutomationContext.CONTENT && type == AutomationType.INSTANTIATION) {
			return 2;
		}
		if (ctx == AutomationContext.CONTENT && type == AutomationType.STATUS_CHANGE) {
			return 3;
		}
		return 4;
	}

	private record RuleOutcome(CurriculumElement element, AutomationExecutionResult result) {
	}

	RuleOutcome processRule(CurriculumElement element, CurriculumAutomationRule rule, Date today) {
		if (rule.getDependingOn() == AutomationDependingOn.EXECUTION_PERIOD) {
			Date referenceDate = computeTriggerDate(element, rule);
			if (referenceDate == null) {
				return new RuleOutcome(element, null);
			}
			if (referenceDate.after(today)) {
				return new RuleOutcome(element, null);
			}
		} else {
			if (rule.getDependingOnStatus() == null || rule.getDependingOnStatus().isEmpty()) {
				return new RuleOutcome(element, null);
			}
			String currentStatus = element.getElementStatus() != null ? element.getElementStatus().name() : null;
			if (!rule.getDependingOnStatus().contains(currentStatus)) {
				return new RuleOutcome(element, null);
			}
		}

		if (rule.getOnlyWhenStatus() != null && !rule.getOnlyWhenStatus().isEmpty()) {
			String currentStatus = element.getElementStatus() != null ? element.getElementStatus().name() : null;
			if (!rule.getOnlyWhenStatus().contains(currentStatus)) {
				return new RuleOutcome(element, null);
			}
		}

		AutomationContext ctx = rule.getContext();
		AutomationType type = rule.getAutomationType();
		if ((ctx == AutomationContext.IMPLEMENTATION || ctx == AutomationContext.ELEMENT)
				&& type == AutomationType.STATUS_CHANGE) {
			ElementStatusChangeOutcome outcome = applyElementStatusChange(element, rule);
			return new RuleOutcome(outcome.element(), outcome.result());
		} else if (ctx == AutomationContext.CONTENT && type == AutomationType.INSTANTIATION) {
			return new RuleOutcome(element, applyContentInstantiation(element));
		} else if (ctx == AutomationContext.CONTENT && type == AutomationType.STATUS_CHANGE) {
			return new RuleOutcome(element, applyContentStatusChange(element, rule));
		}
		return new RuleOutcome(element, null);
	}

	@Override
	public Date getNextExecutionTime() {
		try {
			Trigger trigger = scheduler.getTrigger(TriggerKey.triggerKey("curriculumAutomationTrigger"));
			if (trigger != null) {
				return trigger.getNextFireTime();
			}
		} catch (SchedulerException e) {
			log.error("", e);
		}
		return null;
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

	private record ElementStatusChangeOutcome(CurriculumElement element, AutomationExecutionResult result) {
	}

	private ElementStatusChangeOutcome applyElementStatusChange(CurriculumElement element, CurriculumAutomationRule rule) {
		if (!CurriculumElementStatus.isValueOf(rule.getTargetStatus())) {
			log.warn("Automation: invalid target status {} for element {} (key={})",
					rule.getTargetStatus(), element.getDisplayName(), element.getKey());
			return new ElementStatusChangeOutcome(element, null);
		}
		CurriculumElementStatus targetStatus = CurriculumElementStatus.valueOf(rule.getTargetStatus());
		CurriculumElementStatus currentStatus = element.getElementStatus();
		if (currentStatus != null && targetStatus.ordinal() <= currentStatus.ordinal()) {
			return new ElementStatusChangeOutcome(element, AutomationExecutionResult.UNCHANGED);
		}
		curriculumService.updateCurriculumElementStatus(null, element, targetStatus, false, null);
		log.info("Automation: element status changed to {} for element {} (key={})",
				targetStatus, element.getDisplayName(), element.getKey());
		return new ElementStatusChangeOutcome(curriculumService.getCurriculumElement(element), AutomationExecutionResult.CHANGED);
	}

	private AutomationExecutionResult applyContentInstantiation(CurriculumElement element) {
		CurriculumElementType type = element.getType();
		int max = type != null ? type.getMaxRepositoryEntryRelations() : -1;
		long count = curriculumService.countRepositoryEntries(element);
		if (max >= 0 && count >= max) {
			return AutomationExecutionResult.UNCHANGED;
		}
		List<RepositoryEntry> templates = curriculumService.getRepositoryTemplates(element);
		if (templates.isEmpty()) {
			return AutomationExecutionResult.UNCHANGED;
		}
		AutomationExecutionResult result = AutomationExecutionResult.UNCHANGED;
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
			result = AutomationExecutionResult.CHANGED;
		}
		return result;
	}

	private AutomationExecutionResult applyContentStatusChange(CurriculumElement element, CurriculumAutomationRule rule) {
		if (!RepositoryEntryStatusEnum.isValid(rule.getTargetStatus())) {
			log.warn("Automation: invalid target status {} for element {} (key={})",
					rule.getTargetStatus(), element.getDisplayName(), element.getKey());
			return null;
		}
		RepositoryEntryStatusEnum targetStatus = RepositoryEntryStatusEnum.valueOf(rule.getTargetStatus());
		List<RepositoryEntry> entries = curriculumService.getRepositoryEntries(element);
		if (entries.isEmpty()) {
			return AutomationExecutionResult.UNCHANGED;
		}
		AutomationExecutionResult result = AutomationExecutionResult.UNCHANGED;
		for (RepositoryEntry entry : entries) {
			RepositoryEntryStatusEnum current = entry.getEntryStatus();
			if (current != null && targetStatus.ordinal() <= current.ordinal()) {
				continue;
			}
			if (targetStatus == RepositoryEntryStatusEnum.closed) {
				repositoryService.closeRepositoryEntry(entry, null, true);
			} else {
				repositoryManager.setStatus(entry, targetStatus);
			}
			log.info("Automation: content status changed to {} for entry {} (key={}) in element {} (key={})",
					targetStatus, entry.getDisplayname(), entry.getKey(), element.getDisplayName(), element.getKey());
			result = AutomationExecutionResult.CHANGED;
		}
		return result;
	}

}
