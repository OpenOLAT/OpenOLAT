/**
 * <a href="http://www.openolat.org">
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
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.quality.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.Dropdown;
import org.olat.core.gui.components.dropdown.Dropdown.Spacer;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilterValue;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.ActionsColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableEmptyNextPrimaryActionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiTableFilterTabEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumModule;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.forms.EvaluationFormEmailExecutor;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.quality.QualityContextRef;
import org.olat.modules.quality.QualityContextRole;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.quality.QualityParticipation;
import org.olat.modules.quality.QualityService;
import org.olat.modules.quality.ui.ParticipationDataModel.ParticipationCols;
import org.olat.modules.quality.ui.security.DataCollectionSecurityCallback;
import org.olat.modules.quality.ui.wizard.AddCourseUser_1_ChooseCourseStep;
import org.olat.modules.quality.ui.wizard.AddCurriculumElementUser_1_ChooseCurriculumElementStep;
import org.olat.modules.quality.ui.wizard.AddEmailAddress_1_AddStep;
import org.olat.modules.quality.ui.wizard.AddEmailContext;
import org.olat.modules.quality.ui.wizard.AddEmailContext.EmailIdentity;
import org.olat.modules.quality.ui.wizard.AddUser_1_ChooseUserStep;
import org.olat.modules.quality.ui.wizard.CourseContext;
import org.olat.modules.quality.ui.wizard.CurriculumElementContext;
import org.olat.modules.quality.ui.wizard.IdentityContext;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 13.06.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ParticipationListController extends FormBasicController {
	
	private static final String CMD_DELETE = "delete";
	
	private static final String TAB_ID_ALL = "All";
	private static final String TAB_ID_USER = "Users";
	private static final String TAB_ID_EMAIL = "Invitations";
	private static final String FILTER_ROLE = "role";
	
	private Dropdown addDropdown;
	private Link addUsersLink;
	private Link addCourseUsersLink;
	private Link addCurriculumElementUsersLink;
	private Link addEmailAddressLink;
	private FormLink bulkDeleteLink;
	private ParticipationDataModel dataModel;
	private FlexiTableElement tableEl;
	private FlexiFiltersTab tabRoleEmail;
	
	private StepsMainRunController wizard;
	private StepsMainRunController emailAddressWizard;
	private CloseableModalController cmc;
	private ParticipationRemoveConfirmationController removeConfirmationCtrl;
	private CloseableCalloutWindowController toolsCalloutCtrl;
	private ToolsController toolsCtrl;
	
	private final TooledStackedPanel stackPanel;
	private DataCollectionSecurityCallback secCallback;
	private QualityDataCollection dataCollection;
	
	@Autowired
	private QualityService qualityService;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private CurriculumModule curriculumModule;
	@Autowired
	private CurriculumService curriculumService;

	private Controller toolCtrl;

	public ParticipationListController(UserRequest ureq, WindowControl windowControl,
			DataCollectionSecurityCallback secCallback, TooledStackedPanel stackPanel,
			QualityDataCollection dataCollection) {
		super(ureq, windowControl, "participants");
		this.secCallback = secCallback;
		this.stackPanel = stackPanel;
		this.dataCollection = dataCollection;
		
		initForm(ureq);
		
		updateUI();
		loadModel();
	}
	
	public void onChanged(QualityDataCollection dataCollection, DataCollectionSecurityCallback secCallback) {
		this.dataCollection = dataCollection;
		this.secCallback = secCallback;
		
		initTools();
		updateUI();
		updateEmptyMessageUI();
		loadModel();
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ParticipationCols.firstname));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ParticipationCols.lastname));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ParticipationCols.email));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ParticipationCols.role, new QualityContextRoleRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ParticipationCols.repositoryEntryName));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ParticipationCols.curriculumElementName));
		columnsModel.addFlexiColumnModel(new ActionsColumnModel(ParticipationCols.tools));
		
		dataModel = new ParticipationDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 25, true, getTranslator(), flc);
		tableEl.setAndLoadPersistedPreferences(ureq, "quality-participations");
		
		initBulkLinks();
		initFilters();
		initFilterTabs(ureq);
		updateEmptyMessageUI();
	}
	
	private void initBulkLinks() {
		bulkDeleteLink = uifactory.addFormLink("participation.remove", flc, Link.BUTTON);
		bulkDeleteLink.setIconLeftCSS("o_icon o_icon-fw o_icon_delete_item");
		tableEl.addBatchButton(bulkDeleteLink);
	}
	
	private void initFilters() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>(1);
		
		SelectionValues roleValues = new SelectionValues();
		roleValues.add(SelectionValues.entry(QualityContextRole.owner.name(), translate("participation.role.owner")));
		roleValues.add(SelectionValues.entry(QualityContextRole.coach.name(), translate("participation.role.coach")));
		roleValues.add(SelectionValues.entry(QualityContextRole.participant.name(), translate("participation.role.participant")));
		roleValues.add(SelectionValues.entry(QualityContextRole.email.name(), translate("participation.role.email")));
		roleValues.add(SelectionValues.entry(QualityContextRole.none.name(), translate("participation.role.none")));
		filters.add(new FlexiTableMultiSelectionFilter(translate("participation.role"), FILTER_ROLE, roleValues, true));
		
		tableEl.setFilters(true, filters, false, false);
	}

	private void initFilterTabs(UserRequest ureq) {
		List<FlexiFiltersTab> tabs = new ArrayList<>(3);

		FlexiFiltersTab tabAll = FlexiFiltersTabFactory.tabWithImplicitFilters(
				TAB_ID_ALL,
				translate("all"),
				TabSelectionBehavior.nothing,
				List.of());
		tabs.add(tabAll);
		
		FlexiFiltersTab tabRoleUser = FlexiFiltersTabFactory.tabWithImplicitFilters(
				TAB_ID_USER,
				translate("tab.participation.role.user"),
				TabSelectionBehavior.nothing,
				List.of(FlexiTableFilterValue.valueOf(FILTER_ROLE,
						List.of(QualityContextRole.owner.name(),
								QualityContextRole.coach.name(),
								QualityContextRole.participant.name(),
								QualityContextRole.none.name()))));
		tabs.add(tabRoleUser);
		
		tabRoleEmail = FlexiFiltersTabFactory.tabWithImplicitFilters(
				TAB_ID_EMAIL,
				translate("tab.participation.role.email"),
				TabSelectionBehavior.nothing,
				List.of(FlexiTableFilterValue.valueOf(FILTER_ROLE, QualityContextRole.email.name())));
		tabs.add(tabRoleEmail);
		
		tableEl.setFilterTabs(true, tabs);
		tableEl.setSelectedFilterTab(ureq, tabAll);
	}
	
	private void loadModel() {
		List<QualityParticipation> participations = qualityService.loadParticipations(dataCollection);
		List<ParticipationRow> rows = new ArrayList<>();
		for (QualityParticipation participation : participations) {
			ParticipationRow row = new ParticipationRow(participation);
			forgeToolsLink(row);
			rows.add(row);
		}
		applyFilter(rows);
		dataModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}

	private void forgeToolsLink(ParticipationRow row) {
		if (!secCallback.canRemoveParticipation()) {
			return;
		}
		
		FormLink toolsLink = ActionsColumnModel.createLink(uifactory, getTranslator());
		toolsLink.setUserObject(row);
		row.setToolsLink(toolsLink);
	}
	
	private void applyFilter(List<ParticipationRow> rows) {
		if (tableEl.getFilters() != null && !tableEl.getFilters().isEmpty()) {
			FlexiTableFilter roleFilter = FlexiTableFilter.getFilter(tableEl.getFilters(), FILTER_ROLE);
			if (roleFilter instanceof FlexiTableExtendedFilter extFilter && extFilter.getValues() != null && !extFilter.getValues().isEmpty()) {
				Set<QualityContextRole> roles = extFilter.getValues().stream().map(QualityContextRole::valueOf).collect(Collectors.toSet());
				rows.removeIf(row -> !roles.contains(row.getRole()));
			}
		}
	}

	private void updateUI() {
		tableEl.setMultiSelect(secCallback.canRemoveParticipation());
		tableEl.setSelectAllEnable(secCallback.canRemoveParticipation());
		bulkDeleteLink.setVisible(secCallback.canRemoveParticipation());
	}

	private void updateEmptyMessageUI() {
		if (secCallback.canAddParticipants()) {
			if (tableEl.getSelectedFilterTab() != null && tableEl.getSelectedFilterTab() == tabRoleEmail) {
				tableEl.setEmptyTableSettings("participation.empty.table", null, "o_icon_user", "participation.user.email.add", "o_icon_mail", false);
			} else {
				tableEl.setEmptyTableSettings("participation.empty.table", null, "o_icon_user", "participation.add.participants", "o_icon_add_member", false);
			}
		} else {
			tableEl.setEmptyTableSettings("participation.empty.table", null, "o_icon_user", null, null, false);
		}
	}
	
	public void initTools(Controller ctrl) {
		this.toolCtrl = ctrl;
		initTools();
	}

	private void initTools() {
		stackPanel.removeTool(addDropdown);
		
		if (secCallback.canAddParticipants()) {
			addDropdown = new Dropdown("add.participants", "participation.add.participants", false, getTranslator());
			addDropdown.setIconCSS("o_icon o_icon-lg o_icon_add_member");
			addDropdown.setOrientation(DropdownOrientation.right);
			stackPanel.addTool(addDropdown, Align.right, true, null, toolCtrl);
			
			addUsersLink = LinkFactory.createToolLink("participation.user.add", translate("participation.user.add.search"), this);
			addUsersLink.setIconLeftCSS("o_icon o_icon-fw o_icon_user");
			addDropdown.addComponent(addUsersLink);
			
			addCourseUsersLink = LinkFactory.createToolLink("participation.user.add.course", translate("participation.user.course.add"), this);
			addCourseUsersLink.setIconLeftCSS("o_icon o_icon-fw o_CourseModule_icon");
			addDropdown.addComponent(addCourseUsersLink);
			
			if (curriculumModule.isEnabled()) {
				addCurriculumElementUsersLink = LinkFactory.createToolLink("participation.user.add.curele", translate("participation.user.curele.add"), this);
				addCurriculumElementUsersLink.setIconLeftCSS("o_icon o_icon-fw o_icon_curriculum");
				addDropdown.addComponent(addCurriculumElementUsersLink);
			}
			
			addDropdown.addComponent(new Spacer("extern"));
			
			addEmailAddressLink = LinkFactory.createToolLink("participation.user.add.course", translate("participation.user.email.add"), this);
			addEmailAddressLink.setIconLeftCSS("o_icon o_icon-fw o_icon_mail");
			addDropdown.addComponent(addEmailAddressLink);
		}
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == tableEl) {
			if (event instanceof FlexiTableEmptyNextPrimaryActionEvent) {
				if (tableEl.getSelectedFilterTab() != null && tableEl.getSelectedFilterTab() == tabRoleEmail) {
					doAddEmailAddresses(ureq);
				} else {
					doAddUsers(ureq);
				}
			} else if(event instanceof FlexiTableSearchEvent) {
				loadModel();
			} else if (event instanceof FlexiTableFilterTabEvent) {
				updateEmptyMessageUI();
				loadModel();
			}
		} else if (source instanceof FormLink) {
			FormLink link = (FormLink) source;
			if (link == bulkDeleteLink) {
				List<QualityContextRef> contextRefs = getSelectedContextRefs();
				doConfirmRemove(ureq, contextRefs);
			} else if ("tools".equals(link.getCmd()) && link.getUserObject() instanceof ParticipationRow row) {
				doOpenTools(ureq, row, link);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (addUsersLink == source) {
			doAddUsers(ureq);
		} else if (addCourseUsersLink == source) {
			doAddCourseUsers(ureq);
		} else if (addCurriculumElementUsersLink == source) {
			doAddCurriculumElementUsers(ureq);
		} else if (addEmailAddressLink == source) {
			doAddEmailAddresses(ureq);
		}
		super.event(ureq, source, event);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (wizard == source || emailAddressWizard == source) {
			if (event == Event.CANCELLED_EVENT || event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				getWindowControl().pop();
				if (event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
					loadModel();
					updateUI();
				}
				cleanUp();
			}
		} else if (source == removeConfirmationCtrl) {
			if (Event.DONE_EVENT.equals(event)) {
				List<QualityContextRef> contextRefs = removeConfirmationCtrl.getContextRefs();
				doRemove(contextRefs);
			}
			cmc.deactivate();
			cleanUp();
		} else if (source == cmc) {
			cmc.deactivate();
			cleanUp();
		} else if (toolsCalloutCtrl == source) {
			cleanUp();
		} else if (toolsCtrl == source) {
			if (event == Event.DONE_EVENT) {
				if (toolsCalloutCtrl != null) {
					toolsCalloutCtrl.deactivate();
					cleanUp();
				}
			}
		}
	}

	private void cleanUp() {
		removeAsListenerAndDispose(removeConfirmationCtrl);
		removeAsListenerAndDispose(emailAddressWizard);
		removeAsListenerAndDispose(toolsCalloutCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(wizard);
		removeAsListenerAndDispose(cmc);
		removeConfirmationCtrl = null;
		emailAddressWizard = null;
		toolsCalloutCtrl = null;
		toolsCtrl = null;
		wizard = null;
		cmc = null;
	}
	
	private List<QualityContextRef> getSelectedContextRefs() {
		return tableEl.getMultiSelectedIndex().stream()
				.map(index -> dataModel.getObject(index.intValue()))
				.map(ParticipationRow::getContextRef)
				.collect(Collectors.toList());
	}

	private void doAddUsers(UserRequest ureq) {
		removeAsListenerAndDispose(wizard);
		wizard = new StepsMainRunController(ureq, getWindowControl(), new AddUser_1_ChooseUserStep(ureq),
				addSelectedUsers(), null, translate("participation.user.add.title"), "");
		listenTo(wizard);
		getWindowControl().pushAsModalDialog(wizard.getInitialComponent());
	}
	
	private StepRunnerCallback addSelectedUsers() {
		return (uureq, wControl, runContext) -> {
			IdentityContext identityContext = (IdentityContext) runContext.get("context");
			Collection<Identity> identities = identityContext.getIdentities();
			addUserParticipantions(identities);
			return StepsMainRunController.DONE_MODIFIED;
		};
	}

	private void addUserParticipantions(Collection<Identity> identities) {
		List<EvaluationFormParticipation> participations = qualityService.addParticipations(dataCollection, identities);
		for (EvaluationFormParticipation participation: participations) {
			qualityService.createContextBuilder(dataCollection, participation).build();
		}
	}
	
	private void doAddCourseUsers(UserRequest ureq) {
		removeAsListenerAndDispose(wizard);
		wizard = new StepsMainRunController(ureq, getWindowControl(), new AddCourseUser_1_ChooseCourseStep(ureq),
				addSelectedCourseUsers(), null, translate("participation.user.course.add.title"), "");
		listenTo(wizard);
		getWindowControl().pushAsModalDialog(wizard.getInitialComponent());
	}
	
	private StepRunnerCallback addSelectedCourseUsers() {
		return (uureq, wControl, runContext) -> {
			CourseContext courseContext = (CourseContext) runContext.get("context");
			for (GroupRoles role: courseContext.getRoles()) {
				String roleName = role.name();
				for (RepositoryEntry repositoryEntry: courseContext.getRepositoryEntries()) {
					Collection<Identity> identities = repositoryService.getMembers(repositoryEntry, RepositoryEntryRelationType.all, roleName);
					List<EvaluationFormParticipation> participations = qualityService.addParticipations(dataCollection, identities);
					for (EvaluationFormParticipation participation: participations) {
						qualityService.createContextBuilder(dataCollection, participation, repositoryEntry, role).build();
					}
				}
			}
			return StepsMainRunController.DONE_MODIFIED;
		};
	}
	
	private void doAddCurriculumElementUsers(UserRequest ureq) {
		List<Organisation> organisations = qualityService.loadDataCollectionOrganisations(dataCollection);
		removeAsListenerAndDispose(wizard);
		wizard = new StepsMainRunController(ureq, getWindowControl(),
				new AddCurriculumElementUser_1_ChooseCurriculumElementStep(ureq, organisations),
				addSelectedCurriculumElementUsers(), null, translate("participation.user.curele.add.title"), "");
		listenTo(wizard);
		getWindowControl().pushAsModalDialog(wizard.getInitialComponent());
	}
	
	private StepRunnerCallback addSelectedCurriculumElementUsers() {
		return (uureq, wControl, runContext) -> {
			CurriculumElementContext curriculumElementContext = (CurriculumElementContext) runContext.get("context");
			CurriculumElement curriculumElement = curriculumElementContext.getCurriculumElement();
			for (CurriculumRoles role: curriculumElementContext.getRoles()) {
				List<Identity> identities = curriculumService.getMembersIdentity(curriculumElement, role);
				List<EvaluationFormParticipation> participations = qualityService.addParticipations(dataCollection, identities);
				for (EvaluationFormParticipation participation: participations) {
					qualityService.createContextBuilder(dataCollection, participation, curriculumElement, role).build();
				}
			}
			return StepsMainRunController.DONE_MODIFIED;
		};
	}
	
	private void doAddEmailAddresses(UserRequest ureq) {
		removeAsListenerAndDispose(emailAddressWizard);
		
		Step start = new AddEmailAddress_1_AddStep(ureq);
		StepRunnerCallback finish = addEmailUsers();
		emailAddressWizard = new StepsMainRunController(ureq, getWindowControl(), start, finish, null,
				translate("participation.user.email.add"), null);
		listenTo(emailAddressWizard);
		getWindowControl().pushAsModalDialog(emailAddressWizard.getInitialComponent());
	}
	
	private StepRunnerCallback addEmailUsers() {
		return (uureq, wControl, runContext) -> {
			AddEmailContext context = (AddEmailContext) runContext.get("context");
			
			Collection<EvaluationFormEmailExecutor> emailExecutors = new HashSet<>();
			Collection<Identity> identities = new HashSet<>();
			for (EmailIdentity emailIdentity : context.getEmailToIdentity().values()) {
				if (emailIdentity.identity() == null) {
					emailExecutors.add(emailIdentity.emailExecutor());
				} else {
					identities.add(emailIdentity.identity());
				}
			}
			
			addUserParticipantions(identities);
			addEmailParticipantions(emailExecutors);
			
			return StepsMainRunController.DONE_MODIFIED;
		};
	}

	private void addEmailParticipantions(Collection<EvaluationFormEmailExecutor> emailExecutors) {
		qualityService.addParticipationsEmail(dataCollection, emailExecutors);
	}

	private void doConfirmRemove(UserRequest ureq, List<QualityContextRef> contextRefs) {
		if (contextRefs.isEmpty()) {
			showWarning("participation.none.selected");
		} else {
			removeConfirmationCtrl = new ParticipationRemoveConfirmationController(ureq, getWindowControl(), contextRefs);
			listenTo(removeConfirmationCtrl);
			
			cmc = new CloseableModalController(getWindowControl(), translate("close"),
					removeConfirmationCtrl.getInitialComponent(), true, translate("participation.remove"));
			cmc.activate();
			listenTo(cmc);
		}
	}
	
	private void doRemove(List<QualityContextRef> contextRefs) {
		qualityService.deleteContextsAndParticipations(contextRefs);
		loadModel();
		updateUI();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doOpenTools(UserRequest ureq, ParticipationRow row, FormLink link) {
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);

		toolsCtrl = new ToolsController(ureq, getWindowControl(), row);
		listenTo(toolsCtrl);
	
		toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(toolsCalloutCtrl);
		toolsCalloutCtrl.activate();
	}
	
	private class ToolsController extends BasicController {
		
		private final VelocityContainer mainVC;
		
		private final ParticipationRow row;
		private final List<String> names = new ArrayList<>(1);
		
		public ToolsController(UserRequest ureq, WindowControl wControl, ParticipationRow row) {
			super(ureq, wControl);
			this.row = row;
			
			mainVC = createVelocityContainer("tools");
			putInitialPanel(mainVC);
			
			addLink("delete", CMD_DELETE, "o_icon o_icon-fw o_icon_delete_item");
			
			mainVC.contextPut("names", names);
		}
		
		private void addLink(String name, String cmd, String iconCSS) {
			Link link = LinkFactory.createLink(name, cmd, getTranslator(), mainVC, this, Link.LINK);
			if (iconCSS != null) {
				link.setIconLeftCSS(iconCSS);
			}
			mainVC.put(name, link);
			names.add(name);
		}
		
		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			fireEvent(ureq, Event.DONE_EVENT);
			if(source instanceof Link) {
				Link link = (Link)source;
				String cmd = link.getCommand();
				if (CMD_DELETE.equals(cmd)) {
					doConfirmRemove(ureq, List.of(row.getContextRef()));
				}
			}
		}
	}

}
