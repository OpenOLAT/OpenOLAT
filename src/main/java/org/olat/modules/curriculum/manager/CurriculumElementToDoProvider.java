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
package org.olat.modules.curriculum.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.tag.Tag;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.confirmation.ConfirmationController;
import org.olat.core.gui.control.generic.confirmation.ConfirmationController.ButtonType;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.DateUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementRef;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumModule;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumElementRefImpl;
import org.olat.modules.curriculum.model.CurriculumMember;
import org.olat.modules.curriculum.model.SearchMemberParameters;
import org.olat.modules.curriculum.ui.CurriculumElementContextPicker;
import org.olat.modules.curriculum.ui.CurriculumElementToDoMemberProvider;
import org.olat.modules.curriculum.ui.CurriculumUIFactory;
import org.olat.modules.todo.ToDoContext;
import org.olat.modules.todo.ToDoContextFilter;
import org.olat.modules.todo.ToDoDateUnit;
import org.olat.modules.todo.ToDoProvider;
import org.olat.modules.todo.ToDoRelativeDates;
import org.olat.modules.todo.ToDoRight;
import org.olat.modules.todo.ToDoRole;
import org.olat.modules.todo.ToDoService;
import org.olat.modules.todo.ToDoStatus;
import org.olat.modules.todo.ToDoTask;
import org.olat.modules.todo.ToDoTaskMembers;
import org.olat.modules.todo.ToDoTaskRef;
import org.olat.modules.todo.ToDoTaskSearchParams;
import org.olat.modules.todo.ToDoTaskSecurityCallback;
import org.olat.modules.todo.ToDoTaskTag;
import org.olat.modules.todo.ui.ToDoDateResolver;
import org.olat.modules.todo.ui.ToDoTaskContextConfig;
import org.olat.modules.todo.ui.ToDoTaskDateConfig;
import org.olat.modules.todo.ui.ToDoTaskDetailsController;
import org.olat.modules.todo.ui.ToDoTaskEditController;
import org.olat.modules.todo.ui.ToDoTaskListController;
import org.olat.modules.todo.ui.ToDoTaskMemberConfig;
import org.olat.modules.todo.ui.ToDoTaskMemberSelection;
import org.olat.modules.todo.ui.ToDoUIFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * Initial date: 29 Apr 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
@Service
public class CurriculumElementToDoProvider implements ToDoProvider, ToDoContextFilter {

	public static final String TYPE = "curriculum.element";
	public static final String DATE_REF_BEFORE_BEGIN   = "BEFORE_BEGIN";
	public static final String DATE_REF_AFTER_BEGIN    = "AFTER_BEGIN";
	public static final String DATE_REF_BEFORE_END     = "BEFORE_END";
	public static final String DATE_REF_AFTER_END      = "AFTER_END";
	public static final String DATE_REF_SAME_DAY_BEGIN = "SAME_DAY_BEGIN";
	public static final String DATE_REF_SAME_DAY_END   = "SAME_DAY_END";

	private static final ToDoRight[] ASSIGNEE_RIGHTS = new ToDoRight[] {ToDoRight.all};

	@Autowired
	private DB dbInstance;
	@Autowired
	private CurriculumModule curriculumModule;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private ToDoService toDoService;

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public boolean isEnabled() {
		return curriculumModule.isEnabled();
	}

	@Override
	public String getBusinessPath(ToDoTask toDoTask) {
		if (toDoTask.getOriginId() == null || toDoTask.getOriginSubPath() == null) {
			return null;
		}
		Long elementKey = Long.valueOf(toDoTask.getOriginSubPath());
		return "[CurriculumAdmin:0][Curriculum:" + toDoTask.getOriginId() + "][CurriculumElement:" + elementKey + "][ToDos:0][" + ToDoTaskListController.TYPE_TODO +":" + toDoTask.getKey() + "]";
	}

	@Override
	public String getDisplayName(Locale locale) {
		return Util.createPackageTranslator(CurriculumUIFactory.class, locale).translate("curriculum.element.todo.provider.name");
	}

	@Override
	public String getContextFilterType() {
		return TYPE;
	}

	@Override
	public int getFilterSortOrder() {
		return 150;
	}

	@Override
	public String getModifiedBy(Locale locale, ToDoTask toDoTask) {
		return null;
	}

	@Override
	public void upateStatus(Identity doer, ToDoTaskRef toDoTask, Long originId, String originSubPath, ToDoStatus status) {
		updateStatus(doer, toDoTask, status);
	}

	@Override
	public Controller createCreateController(UserRequest ureq, WindowControl wControl, Identity doer, Long originId,
			String originSubPath) {
		CurriculumElement element = curriculumService.getCurriculumElement(new CurriculumElementRefImpl(Long.valueOf(originSubPath)));
		ToDoContext context = ToDoContext.of(CurriculumElementToDoProvider.TYPE, element.getCurriculum().getKey(),
				element.getKey().toString(), element.getCurriculum().getDisplayName(), element.getDisplayName());
		return createEditController(ureq, wControl, null, null, element, context);
	}

	@Override
	public boolean isCopyable() {
		return true;
	}

	@Override
	public boolean isRestorable() {
		return true;
	}

	@Override
	public Controller createCopyController(UserRequest ureq, WindowControl wControl, Identity doer,
			ToDoTask sourceToDoTask, boolean showContext) {
		CurriculumElement element = curriculumService.getCurriculumElement(new CurriculumElementRefImpl(Long.valueOf(sourceToDoTask.getOriginSubPath())));
		ToDoContext context = ToDoContext.of(CurriculumElementToDoProvider.TYPE, element.getCurriculum().getKey(),
				element.getKey().toString(), element.getCurriculum().getDisplayName(), element.getDisplayName());
		return createEditController(ureq, wControl, null, sourceToDoTask, element, context);
	}

	@Override
	public Controller createEditController(UserRequest ureq, WindowControl wControl, ToDoTask toDoTask,
			boolean showContext, boolean showSingleAssignee) {
		CurriculumElement element = curriculumService.getCurriculumElement(new CurriculumElementRefImpl(Long.valueOf(toDoTask.getOriginSubPath())));
		return createEditController(ureq, wControl, toDoTask, null, element, toDoTask);
	}

	private Controller createEditController(UserRequest ureq, WindowControl wControl, ToDoTask toDoTask,
			ToDoTask toDoTaskCopySource, CurriculumElement element, ToDoContext context) {
		Set<Identity> candidates = getCandidates(element).stream()
				.map(CurriculumMember::getIdentity)
				.collect(Collectors.toSet());
		CurriculumElement implementation = curriculumService.getImplementationOf(element);
		CurriculumElement effectiveRoot = implementation != null ? implementation : element;
		CurriculumElementType implementationType = effectiveRoot.getType();
		boolean isStructuredProduct = implementationType == null || !implementationType.isSingleElement();
		ToDoTaskContextConfig contextConfig = isStructuredProduct
				? ToDoTaskContextConfig.picker(new CurriculumElementContextPicker(effectiveRoot.getKey(), element.getKey()), context)
				: ToDoTaskContextConfig.off(context);
		CurriculumElementToDoMemberProvider memberSearchProvider = new CurriculumElementToDoMemberProvider(element);
		ToDoTaskDateConfig dateConfig = createDateConfig(ureq.getLocale(), element);
		return new ToDoTaskEditController(ureq, wControl, toDoTask, toDoTaskCopySource,
				contextConfig,
				ToDoTaskMemberConfig.search(candidates, memberSearchProvider).notMandatory(),
				ToDoTaskMemberConfig.search(candidates, memberSearchProvider),
				ToDoTaskMemberSelection.empty(),
				dateConfig,
				createTagSearchParams(), ASSIGNEE_RIGHTS);
	}

	public List<CurriculumMember> getCandidates(CurriculumElement element) {
		SearchMemberParameters elementParams = new SearchMemberParameters(element);
		elementParams.setRoles(List.of(CurriculumRoles.curriculumelementowner, CurriculumRoles.owner));
		List<CurriculumMember> elementMembers = curriculumService.getCurriculumElementsMembers(elementParams);

		SearchMemberParameters curriculumParams = new SearchMemberParameters(element.getCurriculum());
		curriculumParams.setRoles(List.of(CurriculumRoles.curriculummanager, CurriculumRoles.curriculumowner));
		List<CurriculumMember> curriculumMembers = curriculumService.getCurriculumMembers(curriculumParams);

		List<CurriculumMember> members = new ArrayList<>(elementMembers.size() + curriculumMembers.size());
		members.addAll(elementMembers);
		members.addAll(curriculumMembers);
		return members;
	}

	private ToDoTaskDateConfig createDateConfig(Locale locale, CurriculumElement element) {
		Translator translator = Util.createPackageTranslator(CurriculumUIFactory.class, locale);
		SelectionValues relativeRefs = new SelectionValues();
		relativeRefs.add(SelectionValues.entry(DATE_REF_BEFORE_BEGIN, translator.translate("curriculum.element.todo.date.ref.before.begin")));
		relativeRefs.add(SelectionValues.entry(DATE_REF_AFTER_BEGIN,  translator.translate("curriculum.element.todo.date.ref.after.begin")));
		relativeRefs.add(SelectionValues.entry(DATE_REF_BEFORE_END,   translator.translate("curriculum.element.todo.date.ref.before.end")));
		relativeRefs.add(SelectionValues.entry(DATE_REF_AFTER_END,    translator.translate("curriculum.element.todo.date.ref.after.end")));
		SelectionValues sameDayRefs = new SelectionValues();
		sameDayRefs.add(SelectionValues.entry(DATE_REF_SAME_DAY_BEGIN, translator.translate("curriculum.element.todo.date.ref.same.day.begin")));
		sameDayRefs.add(SelectionValues.entry(DATE_REF_SAME_DAY_END,   translator.translate("curriculum.element.todo.date.ref.same.day.end")));
		ToDoDateResolver resolver = (ref, unit, value) ->
				computeRelativeDate(ref, unit, value, element.getBeginDate(), element.getEndDate());
		return ToDoTaskDateConfig.absoluteOrRelative(relativeRefs, sameDayRefs, resolver);
	}

	private ToDoTaskSearchParams createTagSearchParams() {
		ToDoTaskSearchParams params = new ToDoTaskSearchParams();
		params.setTypes(List.of(TYPE));
		return params;
	}

	@Override
	public FormBasicController createDetailController(UserRequest ureq, WindowControl wControl, Form mainForm,
			ToDoTaskSecurityCallback secCallback, ToDoTask toDoTask, List<Tag> tags, Identity creator,
			Identity modifier, Set<Identity> assignees, Set<Identity> delegatees) {
		return new ToDoTaskDetailsController(ureq, wControl, mainForm, secCallback, toDoTask, tags, creator, modifier, assignees, delegatees);
	}

	@Override
	public void deleteToDoTaskSoftly(Identity doer, ToDoTask toDoTask) {
		updateStatus(doer, toDoTask, ToDoStatus.deleted);
	}

	@Override
	public Controller createDeleteConfirmationController(UserRequest ureq, WindowControl wControl, Locale locale,
			ToDoTask toDoTask) {
		Translator translator = Util.createPackageTranslator(ToDoUIFactory.class, locale);
		return new ConfirmationController(ureq, wControl,
				translator.translate("task.delete.conformation.message", StringHelper.escapeHtml(ToDoUIFactory.getDisplayName(translator, toDoTask))),
				translator.translate("task.delete.confirmation.confirm"),
				translator.translate("delete"), ButtonType.danger);
	}

	private ToDoTask getToDoTask(ToDoTaskRef toDoTaskRef, boolean active) {
		ToDoTask toDoTask = toDoService.getToDoTask(toDoTaskRef);
		if (toDoTask == null) {
			return null;
		}
		if (active && toDoTask.getStatus() == ToDoStatus.deleted) {
			return null;
		}
		return toDoTask;
	}

	private void updateStatus(Identity doer, ToDoTaskRef toDoTask, ToDoStatus status) {
		ToDoTask reloadedToDoTask = getToDoTask(toDoTask, false);
		if (reloadedToDoTask == null) {
			return;
		}
		ToDoStatus previousStatus = reloadedToDoTask.getStatus();
		if (previousStatus == status) {
			return;
		}

		reloadedToDoTask.setStatus(status);
		reloadedToDoTask.setContentModifiedDate(new Date());
		toDoService.update(doer, reloadedToDoTask, previousStatus);
	}

	public void onCurriculumUpdated(Curriculum curriculum) {
		toDoService.updateOriginTitle(TYPE, curriculum.getKey(), null,
				curriculum.getDisplayName(), null);
	}

	public void onCurriculumElementUpdated(CurriculumElement element) {
		updateRelativeDates(element);
		if (element.getCurriculum() != null) {
			toDoService.updateOriginTitle(TYPE,
					element.getCurriculum().getKey(),
					element.getKey().toString(),
					element.getCurriculum().getDisplayName(),
					element.getDisplayName());
		}
	}

	public void updateRelativeDates(CurriculumElement curriculumElement) {
		if (curriculumElement == null) return;
		ToDoTaskSearchParams searchParams = new ToDoTaskSearchParams();
		searchParams.setTypes(List.of(TYPE));
		searchParams.setOriginSubPaths(List.of(String.valueOf(curriculumElement.getKey())));
		searchParams.setRelativeDatesNull(Boolean.FALSE);
		List<ToDoTask> toDoTasks = toDoService.getToDoTasks(searchParams);
		for (ToDoTask toDoTask : toDoTasks) {
			materializeDates(toDoTask, curriculumElement.getBeginDate(), curriculumElement.getEndDate());
		}
	}

	private void materializeDates(ToDoTask toDoTask, Date beginDate, Date endDate) {
		ToDoRelativeDates config = toDoTask.getRelativeDates();
		if (config == null) return;

		boolean changed = false;
		if (config.getStartRef() != null && config.getStartUnit() != null) {
			Date startDate = computeRelativeDate(config.getStartRef(), config.getStartUnit(), config.getStartValue(), beginDate, endDate);
			if (!Objects.equals(startDate, toDoTask.getStartDate())) {
				toDoTask.setStartDate(startDate);
				changed = true;
			}
		}
		if (config.getDueRef() != null && config.getDueUnit() != null) {
			Date dueDate = computeRelativeDate(config.getDueRef(), config.getDueUnit(), config.getDueValue(), beginDate, endDate);
			if (!Objects.equals(dueDate, toDoTask.getDueDate())) {
				toDoTask.setDueDate(dueDate);
				changed = true;
			}
		}
		if (changed) {
			toDoTask.setContentModifiedDate(new Date());
			toDoService.update(null, toDoTask, toDoTask.getStatus());
		}
	}

	public static Date computeRelativeDate(String ref, ToDoDateUnit unit, Integer value, Date beginDate, Date endDate) {
		if (ref == null || unit == null) {
			return null;
		}
		Date refDate = switch (ref) {
			case DATE_REF_BEFORE_BEGIN, DATE_REF_AFTER_BEGIN, DATE_REF_SAME_DAY_BEGIN -> beginDate;
			case DATE_REF_BEFORE_END,   DATE_REF_AFTER_END,   DATE_REF_SAME_DAY_END   -> endDate;
			default -> null;
		};
		if (refDate == null) {
			return null;
		}
		if (unit == ToDoDateUnit.SAME_DAY) {
			return refDate;
		}
		if (value == null) {
			return null;
		}
		int signed = (ref.equals(DATE_REF_BEFORE_BEGIN) || ref.equals(DATE_REF_BEFORE_END))
				? -value.intValue()
				:  value.intValue();
		return switch (unit) {
			case DAYS   -> DateUtils.addDays(refDate, signed);
			case WEEKS  -> DateUtils.addWeeks(refDate, signed);
			case MONTHS -> DateUtils.addMonth(refDate, signed);
			case YEARS  -> DateUtils.addYears(refDate, signed);
			case SAME_DAY -> refDate;
		};
	}

	public void onCurriculumElementDeletedSoftly(CurriculumElement element, Identity doer) {
		if (element.getCurriculum() == null) {
			return;
		}
		toDoService.updateOriginDeleted(TYPE,
				element.getCurriculum().getKey(),
				element.getKey().toString(),
				true, new Date(), doer);
	}
	
	public void copyToDoTasks(CurriculumElement source, CurriculumElement target,
			boolean copyAssignments, Identity doer) {
		if (source == null || target == null
				|| source.getCurriculum() == null || target.getCurriculum() == null) {
			return;
		}

		ToDoTaskSearchParams searchParams = createActiveSearchParams(List.of(source.getKey().toString()));
		List<ToDoTask> sourceTasks = toDoService.getToDoTasks(searchParams);
		if (sourceTasks.isEmpty()) {
			return;
		}

		List<ToDoTaskTag> sourceTags = toDoService.getToDoTaskTags(searchParams);
		Map<Long, ToDoTaskMembers> sourceMembers = copyAssignments
				? toDoService.getToDoTaskGroupKeyToMembers(sourceTasks, ToDoRole.ASSIGNEE_DELEGATEE)
				: Map.of();

		Long targetCurriculumKey = target.getCurriculum().getKey();
		String targetSubPath = target.getKey().toString();
		String targetCurriculumTitle = target.getCurriculum().getDisplayName();
		String targetElementTitle = target.getDisplayName();

		int count = 0;
		for (ToDoTask sourceTask : sourceTasks) {
			ToDoTask copy = toDoService.createToDoTask(doer, TYPE, targetCurriculumKey, targetSubPath,
					targetCurriculumTitle, targetElementTitle, null);
			copy.setTitle(sourceTask.getTitle());
			copy.setDescription(sourceTask.getDescription());
			copy.setPriority(sourceTask.getPriority());
			copy.setExpenditureOfWork(sourceTask.getExpenditureOfWork());
			copy.setStartDate(sourceTask.getStartDate());
			copy.setDueDate(sourceTask.getDueDate());
			copy.setAssigneeRights(sourceTask.getAssigneeRights());
			copy.setRelativeDates(ToDoRelativeDates.copy(sourceTask.getRelativeDates()));
			copy.setContentModifiedDate(new Date());
			toDoService.update(doer, copy, null);

			List<String> tagNames = sourceTags.stream()
					.filter(tag -> tag.getToDoTask().getKey().equals(sourceTask.getKey()))
					.map(tag -> tag.getTag().getDisplayName())
					.toList();
			if (!tagNames.isEmpty()) {
				toDoService.updateTags(copy, tagNames);
			}

			if (copyAssignments) {
				ToDoTaskMembers members = sourceMembers.get(sourceTask.getBaseGroup().getKey());
				if (members != null) {
					toDoService.updateMember(doer, copy,
							members.getMembers(ToDoRole.assignee),
							members.getMembers(ToDoRole.delegatee));
				}
			}

			if (++count % 10 == 0) {
				dbInstance.intermediateCommit();
			}
		}
	}

	public long countActiveToDoTasks(CurriculumElement curriculumElement,
			List<? extends CurriculumElementRef> elements) {
		if (curriculumElement.getCurriculum() == null) {
			return 0;
		}
		List<String> originSubPaths = elements.stream()
				.map(ref -> ref.getKey().toString())
				.toList();
		ToDoTaskSearchParams params = createActiveSearchParams(originSubPaths);
		Long count = toDoService.getToDoTaskCount(params);
		return count != null ? count : 0;
	}

	public ToDoTaskSearchParams createActiveSearchParams(List<String> originSubPaths) {
		ToDoTaskSearchParams params = new ToDoTaskSearchParams();
		params.setTypes(List.of(TYPE));
		params.setOriginSubPaths(originSubPaths);
		params.setOriginDeleted(Boolean.FALSE);
		params.setStatus(ToDoStatus.OPEN_TO_DONE);
		return params;
	}


}
