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
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.tag.Tag;
import org.olat.core.commons.services.tag.TagInfo;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.date.RelativeDateDisplayValue;
import org.olat.repository.ExecutionPeriodRelativeDateContext;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.confirmation.ConfirmationController;
import org.olat.core.gui.control.generic.confirmation.ConfirmationController.ButtonType;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
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
import org.olat.modules.curriculum.ui.CurriculumElementToDoMemberController;
import org.olat.modules.curriculum.ui.CurriculumUIFactory;
import org.olat.modules.todo.ToDoContext;
import org.olat.modules.todo.ToDoContextFilter;
import org.olat.modules.todo.ToDoDateUnit;
import org.olat.modules.todo.ToDoPriority;
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
import org.olat.modules.todo.ui.ToDoTaskContextConfig;
import org.olat.modules.todo.ui.ToDoTaskDateConfig;
import org.olat.modules.todo.ui.ToDoTaskDatePicker;
import org.olat.modules.todo.ui.ToDoTaskDetailsController;
import org.olat.modules.todo.ui.ToDoTaskEditController;
import org.olat.modules.todo.ui.ToDoTaskListController;
import org.olat.modules.todo.ui.ToDoTaskMemberConfig;
import org.olat.modules.todo.ui.ToDoUIFactory;
import org.olat.user.IdentitySelectionSource;
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

	private static final ToDoRight[] ASSIGNEE_RIGHTS = new ToDoRight[] {ToDoRight.status};

	@Autowired
	private DB dbInstance;
	@Autowired
	private CurriculumModule curriculumModule;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private OrganisationService organisationService;
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
				element.getKey().toString(), element.getCurriculum().getIdentifier(), getContextSubTitle(element));
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
				element.getKey().toString(), element.getCurriculum().getIdentifier(), getContextSubTitle(element));
		return createEditController(ureq, wControl, null, sourceToDoTask, element, context);
	}

	@Override
	public Controller createEditController(UserRequest ureq, WindowControl wControl, ToDoTask toDoTask,
			boolean showContext, boolean showSingleAssignee, ToDoRight[] assigneeRightsOverride) {
		CurriculumElement element = curriculumService.getCurriculumElement(new CurriculumElementRefImpl(Long.valueOf(toDoTask.getOriginSubPath())));
		return createEditController(ureq, wControl, toDoTask, null, element, toDoTask, assigneeRightsOverride);
	}

	private Controller createEditController(UserRequest ureq, WindowControl wControl, ToDoTask toDoTask,
			ToDoTask toDoTaskCopySource, CurriculumElement element, ToDoContext context) {
		return createEditController(ureq, wControl, toDoTask, toDoTaskCopySource, element, context, null);
	}

	private Controller createEditController(UserRequest ureq, WindowControl wControl, ToDoTask toDoTask,
			ToDoTask toDoTaskCopySource, CurriculumElement element, ToDoContext context, ToDoRight[] assigneeRightsOverride) {
		ToDoTask sourceOrCurrent = toDoTask != null ? toDoTask : toDoTaskCopySource;
		ToDoTaskMembers members = toDoService.getToDoTaskMembers(sourceOrCurrent, ToDoRole.ALL);
		Set<Identity> assignees = members.getMembers(ToDoRole.assignee);
		Set<Identity> delegatees = members.getMembers(ToDoRole.delegatee);
		Supplier<Collection<Identity>> candidatesSupplier = new Supplier<>() {
			private Collection<Identity> cached;
			@Override
			public Collection<Identity> get() {
				if (cached == null) {
					cached = getCandidates(element).stream().map(CurriculumMember::getIdentity).collect(Collectors.toSet());
				}
				return cached;
			}
		};
		IdentitySelectionSource assigneeSource = new IdentitySelectionSource(
				ureq.getLocale(), assignees, candidatesSupplier,
				multi -> (u, w) -> new CurriculumElementToDoMemberController(u, w, element),
				ureq.getIdentity());
		IdentitySelectionSource delegateeSource = new IdentitySelectionSource(
				ureq.getLocale(), delegatees, candidatesSupplier,
				multi -> (u, w) -> new CurriculumElementToDoMemberController(u, w, element),
				ureq.getIdentity());
		CurriculumElement implementation = curriculumService.getImplementationOf(element);
		CurriculumElement effectiveRoot = implementation != null ? implementation : element;
		CurriculumElementType implementationType = effectiveRoot.getType();
		boolean isStructuredProduct = implementationType == null || !implementationType.isSingleElement();
		ToDoTaskContextConfig contextConfig = isStructuredProduct && canManageContext(ureq.getIdentity(), element)
				? ToDoTaskContextConfig.picker(new CurriculumElementContextPicker(effectiveRoot.getKey(), element.getKey()), context)
				: ToDoTaskContextConfig.dropdown(List.of(context), context);
		ToDoTaskDateConfig dateConfig = createDateConfig(ureq.getLocale(), element);
		return new ToDoTaskEditController(ureq, wControl, toDoTask, toDoTaskCopySource,
				contextConfig,
				ToDoTaskMemberConfig.editable(assigneeSource, false),
				ToDoTaskMemberConfig.editable(delegateeSource, false),
				toDoTask != null ? members : null, dateConfig,
				createTagSearchParams(), ASSIGNEE_RIGHTS, assigneeRightsOverride);
	}

	private boolean canManageContext(Identity identity, CurriculumElement element) {
		// Curriculum element owners are not allowed to change the context.
		return curriculumService.hasRoleExpanded(element.getCurriculum(), identity,
				OrganisationRoles.administrator.name(),
				OrganisationRoles.curriculummanager.name(),
				CurriculumRoles.curriculumowner.name());
	}

	public List<CurriculumMember> getCandidates(CurriculumElement element) {
		SearchMemberParameters elementParams = new SearchMemberParameters(element);
		elementParams.setRoles(List.of(CurriculumRoles.curriculumelementowner, CurriculumRoles.owner));
		List<CurriculumMember> elementMembers = curriculumService.getCurriculumElementsMembers(elementParams);

		SearchMemberParameters curriculumParams = new SearchMemberParameters(element.getCurriculum());
		curriculumParams.setRoles(List.of(CurriculumRoles.curriculumowner));
		List<CurriculumMember> curriculumMembers = curriculumService.getCurriculumMembers(curriculumParams);

		List<CurriculumMember> members = new ArrayList<>(elementMembers.size() + curriculumMembers.size());
		members.addAll(elementMembers);
		members.addAll(curriculumMembers);

		Organisation organisation = element.getCurriculum().getOrganisation();
		if (organisation != null) {
			for (Identity identity : organisationService.getMembersIdentity(organisation, OrganisationRoles.curriculummanager)) {
				members.add(new CurriculumMember(identity, OrganisationRoles.curriculummanager.name(), null, null));
			}
			for (Identity identity : organisationService.getMembersIdentity(organisation, OrganisationRoles.administrator)) {
				members.add(new CurriculumMember(identity, OrganisationRoles.administrator.name(), null, null));
			}
		}

		return members;
	}

	public Set<Identity> getCandidateIntersection(Collection<CurriculumElement> elements) {
		if (elements.isEmpty()) {
			return Set.of();
		}
		Map<Long, Set<Identity>> sharedByCurriculum = new HashMap<>();
		Set<Identity> result = null;
		for (CurriculumElement element : elements) {
			Set<Identity> shared = sharedByCurriculum.computeIfAbsent(
					element.getCurriculum().getKey(),
					k -> getSharedCandidateIdentities(element));
			Set<Identity> combined = new HashSet<>(getElementOwnerIdentities(element));
			combined.addAll(shared);
			if (result == null) {
				result = combined;
			} else {
				result.retainAll(combined);
			}
			if (result.isEmpty()) {
				return result;
			}
		}
		return result;
	}

	private Set<Identity> getElementOwnerIdentities(CurriculumElement element) {
		SearchMemberParameters params = new SearchMemberParameters(element);
		params.setRoles(List.of(CurriculumRoles.curriculumelementowner, CurriculumRoles.owner));
		return curriculumService.getCurriculumElementsMembers(params).stream()
				.map(CurriculumMember::getIdentity)
				.collect(Collectors.toCollection(HashSet::new));
	}

	private Set<Identity> getSharedCandidateIdentities(CurriculumElement element) {
		SearchMemberParameters curriculumParams = new SearchMemberParameters(element.getCurriculum());
		curriculumParams.setRoles(List.of(CurriculumRoles.curriculumowner));
		Set<Identity> shared = curriculumService.getCurriculumMembers(curriculumParams).stream()
				.map(CurriculumMember::getIdentity)
				.collect(Collectors.toCollection(HashSet::new));
		Organisation organisation = element.getCurriculum().getOrganisation();
		if (organisation != null) {
			shared.addAll(organisationService.getMembersIdentity(organisation, OrganisationRoles.curriculummanager));
			shared.addAll(organisationService.getMembersIdentity(organisation, OrganisationRoles.administrator));
		}
		return shared;
	}

	public void createToDoTasks(Identity doer, Collection<CurriculumElement> elements,
			String title, String description, ToDoStatus status, ToDoPriority priority,
			Date startDate, Date dueDate, Long expenditureOfWork,
			Collection<? extends IdentityRef> assignees,
			Collection<? extends IdentityRef> delegatees,
			List<String> tagDisplayNames) {
		createToDoTasks(doer, elements, title, description, status, priority,
				startDate, dueDate, null, expenditureOfWork, assignees, delegatees, tagDisplayNames);
	}

	public void createToDoTasks(Identity doer, Collection<CurriculumElement> elements,
			String title, String description, ToDoStatus status, ToDoPriority priority,
			Date startDate, Date dueDate, ToDoRelativeDates relativeDates, Long expenditureOfWork,
			Collection<? extends IdentityRef> assignees,
			Collection<? extends IdentityRef> delegatees,
			List<String> tagDisplayNames) {
		int count = 0;
		for (CurriculumElement element : elements) {
			ToDoTask task = toDoService.createToDoTask(doer, TYPE,
					element.getCurriculum().getKey(),
					element.getKey().toString(),
					element.getCurriculum().getIdentifier(),
					getContextSubTitle(element), null);
			task.setTitle(title);
			task.setDescription(description);
			task.setStatus(status);
			task.setPriority(priority);
			if (relativeDates != null) {
				task.setRelativeDates(relativeDates);
				if (relativeDates.getStartRef() != null && relativeDates.getStartUnit() != null) {
					task.setStartDate(computeRelativeDate(relativeDates.getStartRef(), relativeDates.getStartUnit(),
							relativeDates.getStartValue(), element.getBeginDate(), element.getEndDate()));
				} else {
					task.setStartDate(startDate);
				}
				if (relativeDates.getDueRef() != null && relativeDates.getDueUnit() != null) {
					task.setDueDate(computeRelativeDate(relativeDates.getDueRef(), relativeDates.getDueUnit(),
							relativeDates.getDueValue(), element.getBeginDate(), element.getEndDate()));
				} else {
					task.setDueDate(dueDate);
				}
			} else {
				task.setStartDate(startDate);
				task.setDueDate(dueDate);
			}
			task.setExpenditureOfWork(expenditureOfWork);
			task.setAssigneeRights(ASSIGNEE_RIGHTS);
			task.setContentModifiedDate(new Date());
			task = toDoService.update(doer, task, null);
			if ((assignees != null && !assignees.isEmpty()) || (delegatees != null && !delegatees.isEmpty())) {
				toDoService.updateMember(doer, task, assignees, delegatees);
			}
			if (tagDisplayNames != null && !tagDisplayNames.isEmpty()) {
				toDoService.updateTags(task, tagDisplayNames);
			}
			if (++count % 10 == 0) {
				dbInstance.intermediateCommit();
			}
		}
	}

	public List<TagInfo> getTagInfos() {
		return toDoService.getTagInfos(createTagSearchParams(), null);
	}

	public ToDoTaskDateConfig createBulkDateConfig(Locale locale) {
		return ToDoTaskDateConfig.absoluteOrRelative(
				new CurriculumElementsBulkDatePicker(locale));
	}

	private ToDoTaskDateConfig createDateConfig(Locale locale, CurriculumElement element) {
		return ToDoTaskDateConfig.absoluteOrRelative(
				new CurriculumElementDatePicker(locale, curriculumService, element));
	}

	private static final class CurriculumElementDatePicker implements ToDoTaskDatePicker {

		private final Locale locale;
		private final CurriculumService curriculumService;
		private CurriculumElement element;
		private ExecutionPeriodRelativeDateContext context;

		CurriculumElementDatePicker(Locale locale, CurriculumService curriculumService, CurriculumElement element) {
			this.locale = locale;
			this.curriculumService = curriculumService;
			this.element = element;
			this.context = buildContext(locale, element);
		}

		private static ExecutionPeriodRelativeDateContext buildContext(Locale locale, CurriculumElement element) {
			Date begin = element != null ? element.getBeginDate() : null;
			Date end = element != null ? element.getEndDate() : null;
			Translator t = Util.createPackageTranslator(CurriculumUIFactory.class, locale,
					Util.createPackageTranslator(ToDoUIFactory.class, locale));
			return new ExecutionPeriodRelativeDateContext(t, begin, end);
		}

		@Override
		public ExecutionPeriodRelativeDateContext getContext() {
			return context;
		}

		@Override
		public void contextChanged(ToDoContext toDoContext) {
			if (toDoContext == null || !TYPE.equals(toDoContext.getType())
					|| !StringHelper.containsNonWhitespace(toDoContext.getOriginSubPath())) {
				return;
			}
			Long elementKey;
			try {
				elementKey = Long.valueOf(toDoContext.getOriginSubPath());
			} catch (NumberFormatException e) {
				return;
			}
			if (element != null && elementKey.equals(element.getKey())) {
				return;
			}
			element = curriculumService.getCurriculumElement(new CurriculumElementRefImpl(elementKey));
			context = buildContext(locale, element);
		}

		@Override
		public RelativeDateDisplayValue getDisplayValue(ToDoRelativeDates rd, boolean start) {
			String ref = start ? rd.getStartRef() : rd.getDueRef();
			ToDoDateUnit unit = start ? rd.getStartUnit() : rd.getDueUnit();
			Integer value = start ? rd.getStartValue() : rd.getDueValue();
			if (ref == null) {
				return new RelativeDateDisplayValue("", null);
			}
			return context.getDisplayValue(ref, toUnitKey(unit, value), value);
		}

		@Override
		public Date resolve(ToDoRelativeDates rd, boolean start) {
			String ref = start ? rd.getStartRef() : rd.getDueRef();
			ToDoDateUnit unit = start ? rd.getStartUnit() : rd.getDueUnit();
			Integer value = start ? rd.getStartValue() : rd.getDueValue();
			Date beginDate = element != null ? element.getBeginDate() : null;
			Date endDate = element != null ? element.getEndDate() : null;
			return computeRelativeDate(ref, unit, value, beginDate, endDate);
		}

	}

	private static final class CurriculumElementsBulkDatePicker implements ToDoTaskDatePicker {

		private final ExecutionPeriodRelativeDateContext context;

		CurriculumElementsBulkDatePicker(Locale locale) {
			Translator t = Util.createPackageTranslator(CurriculumUIFactory.class, locale,
					Util.createPackageTranslator(ToDoUIFactory.class, locale));
			this.context = new ExecutionPeriodRelativeDateContext(t, null, null);
		}

		@Override
		public ExecutionPeriodRelativeDateContext getContext() {
			return context;
		}

		@Override
		public void contextChanged(ToDoContext toDoContext) {
			//
		}

		@Override
		public RelativeDateDisplayValue getDisplayValue(ToDoRelativeDates rd, boolean start) {
			String ref = start ? rd.getStartRef() : rd.getDueRef();
			ToDoDateUnit unit = start ? rd.getStartUnit() : rd.getDueUnit();
			Integer value = start ? rd.getStartValue() : rd.getDueValue();
			return context.getDisplayValue(ref, toUnitKey(unit, value), value);
		}

		@Override
		public Date resolve(ToDoRelativeDates rd, boolean start) {
			return null;
		}
	}

	private static String toUnitKey(ToDoDateUnit unit, Integer value) {
		if (unit == null || unit == ToDoDateUnit.SAME_DAY) {
			return null;
		}
		return (value != null && value == 1)
				? unit.name().toLowerCase().replaceAll("s$", "")
				: unit.name().toLowerCase();
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
	
	String getContextSubTitle(CurriculumElement element) {
		return element.getDisplayName() + " - " + element.getIdentifier();
	}

	public void onCurriculumUpdated(Curriculum curriculum) {
		toDoService.updateOriginTitle(TYPE, curriculum.getKey(), null,
				curriculum.getIdentifier(), null);
	}

	public void onCurriculumElementUpdated(Identity doer, CurriculumElement element) {
		updateRelativeDates(doer, element);
		if (element.getCurriculum() != null) {
			toDoService.updateOriginTitle(TYPE,
					element.getCurriculum().getKey(),
					element.getKey().toString(),
					element.getCurriculum().getIdentifier(),
					getContextSubTitle(element));
		}
	}

	public void updateRelativeDates(Identity doer, CurriculumElement curriculumElement) {
		if (curriculumElement == null) return;
		ToDoTaskSearchParams searchParams = new ToDoTaskSearchParams();
		searchParams.setTypes(List.of(TYPE));
		searchParams.setOriginSubPaths(List.of(String.valueOf(curriculumElement.getKey())));
		searchParams.setRelativeDatesNull(Boolean.FALSE);
		List<ToDoTask> toDoTasks = toDoService.getToDoTasks(searchParams);
		for (ToDoTask toDoTask : toDoTasks) {
			materializeDates(doer, toDoTask, curriculumElement.getBeginDate(), curriculumElement.getEndDate());
		}
	}

	private void materializeDates(Identity doer, ToDoTask toDoTask, Date beginDate, Date endDate) {
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
			toDoService.update(doer, toDoTask, toDoTask.getStatus());
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
			boolean copyAssignments, Set<Long> includedTaskKeys, Identity doer) {
		if (source == null || target == null
				|| source.getCurriculum() == null || target.getCurriculum() == null) {
			return;
		}

		ToDoTaskSearchParams searchParams = createActiveSearchParams(List.of(source.getKey().toString()));
		List<ToDoTask> sourceTasks = toDoService.getToDoTasks(searchParams);
		if (sourceTasks.isEmpty()) {
			return;
		}
		if (includedTaskKeys != null) {
			sourceTasks = sourceTasks.stream()
					.filter(t -> includedTaskKeys.contains(t.getKey()))
					.toList();
			if (sourceTasks.isEmpty()) {
				return;
			}
		}

		List<ToDoTaskTag> sourceTags = toDoService.getToDoTaskTags(searchParams);
		Map<Long, ToDoTaskMembers> sourceMembers = copyAssignments
				? toDoService.getToDoTaskGroupKeyToMembers(sourceTasks, ToDoRole.ASSIGNEE_DELEGATEE)
				: Map.of();

		Long targetCurriculumKey = target.getCurriculum().getKey();
		String targetSubPath = target.getKey().toString();
		String targetCurriculumTitle = target.getCurriculum().getIdentifier();
		String targetElementTitle = getContextSubTitle(target);

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
