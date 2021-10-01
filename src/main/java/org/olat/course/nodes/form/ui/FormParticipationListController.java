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
package org.olat.course.nodes.form.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.learningpath.manager.LearningPathNodeAccessProvider;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.nodes.FormCourseNode;
import org.olat.course.nodes.form.FormManager;
import org.olat.course.nodes.form.FormParticipation;
import org.olat.course.nodes.form.FormParticipationSearchParams;
import org.olat.course.nodes.form.FormSecurityCallback;
import org.olat.course.nodes.form.ui.FormParticipationTableModel.ParticipationCols;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.assessment.model.AssessmentObligation;
import org.olat.modules.assessment.ui.AssessedIdentityListState;
import org.olat.modules.forms.EvaluationFormParticipationRef;
import org.olat.modules.forms.EvaluationFormParticipationStatus;
import org.olat.modules.forms.EvaluationFormSurvey;
import org.olat.modules.forms.EvaluationFormSurveyIdentifier;
import org.olat.modules.forms.SessionFilterFactory;
import org.olat.modules.forms.ui.EvaluationFormExcelExport;
import org.olat.modules.forms.ui.EvaluationFormExcelExport.UserColumns;
import org.olat.modules.forms.ui.UserPropertiesColumns;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 21 Apr 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class FormParticipationListController extends FormBasicController implements Activateable2 {
	
	private static final String ORES_TYPE_IDENTITY = "Identity";
	private static final String CMD_SELECT = "select";
	
	private FormLink resetAllButton;
	private FormLink excelButton;
	private final List<UserPropertyHandler> userPropertyHandlers;
	private FormParticipationTableModel dataModel;
	private FlexiTableElement tableEl;

	private TooledStackedPanel stackPanel;
	private CloseableModalController cmc;
	private FormParticipationController particpationCtrl;
	private FormResetDataConfirmationController resetDataConfirmationCtrl;
	private CloseableCalloutWindowController toolsCalloutCtrl;
	private ToolsController toolsCtrl;
	private DialogBoxController resetParticipationCtrl;
	private DialogBoxController reopenParticipationCtrl;
	
	private final FormCourseNode courseNode;
	private final UserCourseEnvironment coachCourseEnv;
	private final FormSecurityCallback secCallback;
	private final RepositoryEntry courseEntry;
	private final EvaluationFormSurvey survey;
	private int counter = 0;

	@Autowired
	private FormManager formManager;
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	protected BaseSecurity securityManager;
	
	
	public FormParticipationListController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			FormCourseNode courseNode, UserCourseEnvironment coachCourseEnv, FormSecurityCallback secCallback) {
		super(ureq, wControl, "participation_list");
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		this.stackPanel = stackPanel;
		this.courseNode = courseNode;
		this.coachCourseEnv = coachCourseEnv;
		this.secCallback = secCallback;
		courseEntry = coachCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		
		EvaluationFormSurveyIdentifier surveyIdent = formManager.getSurveyIdentifier(courseNode, courseEntry);
		survey = formManager.loadSurvey(surveyIdent);
		
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(FormParticipationTableModel.USAGE_IDENTIFIER, isAdministrativeUser);
		
		initForm(ureq);
		reload();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer buttonsTopCont = FormLayoutContainer.createButtonLayout("buttons.top", getTranslator());
		buttonsTopCont.setElementCssClass("o_button_group o_button_group_right");
		buttonsTopCont.setRootForm(mainForm);
		formLayout.add(buttonsTopCont);
			
		if (secCallback.canResetAll()) {
			resetAllButton = uifactory.addFormLink("reset.all", buttonsTopCont, Link.BUTTON); 
			resetAllButton.setIconLeftCSS("o_icon o_icon-fw o_icon_delete_item");
		}
		
		excelButton = uifactory.addFormLink("excel.export", buttonsTopCont, Link.BUTTON); 
		excelButton.setIconLeftCSS("o_icon o_icon-fw o_icon_eva_export");
		
		FlexiTableSortOptions options = new FlexiTableSortOptions();
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		
		int colIndex = FormParticipationTableModel.USER_PROPS_OFFSET;
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			UserPropertyHandler userPropertyHandler	= userPropertyHandlers.get(i);
			boolean visible = UserManager.getInstance().isMandatoryUserProperty(FormParticipationTableModel.USAGE_IDENTIFIER, userPropertyHandler);
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(), colIndex, CMD_SELECT, true, "userProp-" + colIndex));
			if(!options.hasDefaultOrderBy()) {
				options.setDefaultOrderBy(new SortKey("userProp-" + colIndex, true));
			}
			colIndex++;
		}
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ParticipationCols.status, new ParticipationStatusCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ParticipationCols.submissionDate));
		if (secCallback.canReset() || secCallback.canReopen()) {
			DefaultFlexiColumnModel toolsColumn = new DefaultFlexiColumnModel(ParticipationCols.tools);
			toolsColumn.setIconHeader("o_icon o_icon_actions o_icon-fws o_icon-lg");
			toolsColumn.setExportable(false);
			toolsColumn.setAlwaysVisible(true);
			columnsModel.addFlexiColumnModel(toolsColumn);
		}
		
		dataModel = new FormParticipationTableModel(columnsModel, getLocale()); 
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 20, false, getTranslator(), formLayout);
		tableEl.setExportEnabled(true);
		tableEl.setSortSettings(options);
		tableEl.setEmptyTableSettings("default.tableEmptyMessage", null, FormCourseNode.ICON_CSS);
		tableEl.setAndLoadPersistedPreferences(ureq, "course.element.form");
		initFilters();
	}

	private void initFilters() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>(2);
		if (LearningPathNodeAccessProvider.TYPE.equals(NodeAccessType.of(coachCourseEnv).getType())) {
			SelectionValues obligationValues = new SelectionValues();
			obligationValues.add(SelectionValues.entry(AssessmentObligation.mandatory.name(), translate("filter.mandatory")));
			obligationValues.add(SelectionValues.entry(AssessmentObligation.optional.name(), translate("filter.optional")));
			obligationValues.add(SelectionValues.entry(AssessmentObligation.excluded.name(), translate("filter.excluded")));
			FlexiTableMultiSelectionFilter obligationFilter = new FlexiTableMultiSelectionFilter(translate("filter.obligation"),
					AssessedIdentityListState.FILTER_OBLIGATION, obligationValues, true);
			obligationFilter.setValues(List.of(AssessmentObligation.mandatory.name(), AssessmentObligation.optional.name()));
			filters.add(obligationFilter);
		}
		
		SelectionValues statusValues = new SelectionValues();
		statusValues.add(SelectionValues.entry(FormParticipationSearchParams.Status.notStarted.name(), translate("participation.status.notStart")));
		statusValues.add(SelectionValues.entry(FormParticipationSearchParams.Status.inProgress.name(), translate("participation.status.inProgress")));
		statusValues.add(SelectionValues.entry(FormParticipationSearchParams.Status.done.name(), translate("participation.status.done")));
		filters.add(new FlexiTableMultiSelectionFilter(translate("filter.status"),
				AssessedIdentityListState.FILTER_STATUS, statusValues, true));
		
		tableEl.setFilters(true, filters, false, true);
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries != null && !entries.isEmpty()) {
			ContextEntry entry = entries.get(0);
			String resourceType = entry.getOLATResourceable().getResourceableTypeName();
			if(ORES_TYPE_IDENTITY.equals(resourceType)) {
				Long identityKey = entries.get(0).getOLATResourceable().getResourceableId();
				for(int i=dataModel.getRowCount(); i--> 0; ) {
					FormParticipationRow row = dataModel.getObject(i);
					if(row.getIdentityKey().equals(identityKey)) {
						doSelect(ureq, row);
					}
				}
			}
		}
	}

	public void reload() {
		List<FormParticipation> formParticipations = formManager.getFormParticipations(survey, coachCourseEnv, getSearchParameters());
		
		List<FormParticipationRow> rows = new ArrayList<>(formParticipations.size());
		for (FormParticipation formParticipation: formParticipations) {
			FormParticipationRow row = new FormParticipationRow(formParticipation.getIdentity(), userPropertyHandlers, getLocale());
			if (formParticipation.getEvaluationFormParticipationRef() != null) {
				row.setStatus(formParticipation.getParticipationStatus());
				if (EvaluationFormParticipationStatus.done == formParticipation.getParticipationStatus()) {
					row.setSubmissionDate(formParticipation.getSubmissionDate());
				}
				
				String linkName = "tools-" + counter++;
				FormLink toolsLink = uifactory.addFormLink(linkName, "", null, flc, Link.LINK | Link.NONTRANSLATED);
				toolsLink.setIconRightCSS("o_icon o_icon_actions o_icon-fws o_icon-lg");
				toolsLink.setUserObject(formParticipation);
				row.setToolsLink(toolsLink);
			}
			rows.add(row);
		}
		
		dataModel.setObjects(rows);
		tableEl.reset(false, false, true);
	}
	
	private FormParticipationSearchParams getSearchParameters() {
		FormParticipationSearchParams params = new FormParticipationSearchParams();
		
		List<FlexiTableFilter> filters = tableEl.getSelectedFilters();
		FlexiTableFilter obligationFilter = FlexiTableFilter.getFilter(filters, "obligation");
		if (obligationFilter != null) {
			List<String> filterValues = ((FlexiTableExtendedFilter)obligationFilter).getValues();
			if (!filterValues.isEmpty()) {
				List<AssessmentObligation> assessmentObligations = filterValues.stream()
						.map(AssessmentObligation::valueOf)
						.collect(Collectors.toList());
				params.setObligations(assessmentObligations);
			}
		}
		
		FlexiTableFilter statusFilter = FlexiTableFilter.getFilter(filters, "status");
		if (statusFilter != null) {
			List<String> filterValues = ((FlexiTableExtendedFilter)statusFilter).getValues();
			if (!filterValues.isEmpty()) {
				List<FormParticipationSearchParams.Status> status = filterValues.stream()
						.map(FormParticipationSearchParams.Status::valueOf)
						.collect(Collectors.toList());
				params.setStatus(status);
			}
		}
		
		return params;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				FormParticipationRow row = dataModel.getObject(se.getIndex());
				if(CMD_SELECT.equals(cmd)) {
					doSelect(ureq, row);
				}
			} else if(event instanceof FlexiTableSearchEvent) {
				reload();
			}
		} else if (source == excelButton) {
			doExport(ureq);
		} else if (source == resetAllButton) {
			doConfirmDeleteAllData(ureq);
		} else if (source instanceof FormLink) {
			FormLink link = (FormLink)source;
			String cmd = link.getCmd();
			if(cmd != null && cmd.startsWith("tools-")) {
				FormParticipation formParticipation = (FormParticipation)link.getUserObject();
				doOpenTools(ureq, formParticipation, link);
			}

		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (toolsCtrl == source) {
			if (event == Event.DONE_EVENT) {
				if(toolsCalloutCtrl != null) {
					toolsCalloutCtrl.deactivate();
					cleanUp();
				}
			}
		} else if (source == resetParticipationCtrl) {
			if (DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event)) {
				EvaluationFormParticipationRef participationRef = (EvaluationFormParticipationRef)resetParticipationCtrl.getUserObject();
				doResetParticipation(participationRef);
			}
		} else if (source == reopenParticipationCtrl) {
			if (DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event)) {
				EvaluationFormParticipationRef participationRef= (EvaluationFormParticipationRef)reopenParticipationCtrl.getUserObject();
				doReopenParticipation(participationRef);
			}
		} else if (source == resetDataConfirmationCtrl) {
			if (event == Event.DONE_EVENT) {
				doDeleteAllData();
			}
			cmc.deactivate();
			cleanUp();
		} 
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(resetDataConfirmationCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(cmc);
		resetDataConfirmationCtrl = null;
		toolsCalloutCtrl = null;
		toolsCtrl = null;
		cmc = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void doDispose() {
		//
	}
	
	private void doSelect(UserRequest ureq, FormParticipationRow row) {
		removeAsListenerAndDispose(particpationCtrl);
		
		OLATResourceable identityOres = OresHelper.createOLATResourceableInstance(ORES_TYPE_IDENTITY, row.getIdentityKey());
		WindowControl bwControl = addToHistory(ureq, identityOres, null);
		
		Identity coachedIdentity = securityManager.loadIdentityByKey(row.getIdentityKey());
		UserCourseEnvironment coachedCourseEnv = AssessmentHelper.createAndInitUserCourseEnvironment(
				coachedIdentity, coachCourseEnv.getCourseEnvironment());
		particpationCtrl = new FormParticipationController(ureq, bwControl, courseNode, coachedCourseEnv);
		listenTo(particpationCtrl);
		
		String fullName = userManager.getUserDisplayName(row.getIdentityKey());
		stackPanel.pushController(fullName, particpationCtrl);
	}
	
	private void doOpenTools(UserRequest ureq, FormParticipation formParticipation, FormLink link) {
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);

		toolsCtrl = new ToolsController(ureq, getWindowControl(), formParticipation);
		listenTo(toolsCtrl);
	
		toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(toolsCalloutCtrl);
		toolsCalloutCtrl.activate();
	}

	private void doExport(UserRequest ureq) {
		UserColumns userColumns = new UserPropertiesColumns(userPropertyHandlers, getTranslator());
		EvaluationFormExcelExport excelExport = formManager.getExcelExport(courseNode, survey.getIdentifier(), userColumns);
		ureq.getDispatchResult().setResultingMediaResource(excelExport.createMediaResource());
	}
	
	private void doConfirmDeleteAllData(UserRequest ureq) {
		Long allSessions = formManager.getSessionsCount(SessionFilterFactory.create(survey));
		Long doneSessions = formManager.getSessionsCount(SessionFilterFactory.createSelectDone(survey));
		resetDataConfirmationCtrl = new FormResetDataConfirmationController(ureq, getWindowControl(), allSessions, doneSessions);
		listenTo(resetDataConfirmationCtrl);
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				resetDataConfirmationCtrl.getInitialComponent(), true, translate("reset.all.title"), true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doDeleteAllData() {
		formManager.deleteAllData(survey, courseNode, coachCourseEnv);
		reload();
	}
	
	private void doConfirmReset(UserRequest ureq, EvaluationFormParticipationRef evaluationFormParticipationRef) {
		String title = translate("reset.title");
		String text = translate("reset.text");
		resetParticipationCtrl = activateYesNoDialog(ureq, title, text, resetParticipationCtrl);
		resetParticipationCtrl.setUserObject(evaluationFormParticipationRef);
	}
	
	private void doResetParticipation(EvaluationFormParticipationRef participationRef) {
		formManager.deleteParticipation(participationRef, courseNode, coachCourseEnv.getCourseEnvironment());
		reload();
	}
	
	private void doConfirmReopen(UserRequest ureq, EvaluationFormParticipationRef participationRef) {
		String title = translate("reopen.title");
		String text = translate("reopen.text");
		reopenParticipationCtrl = activateYesNoDialog(ureq, title, text, resetParticipationCtrl);
		reopenParticipationCtrl.setUserObject(participationRef);
	}
	
	private void doReopenParticipation(EvaluationFormParticipationRef participationRef) {
		formManager.reopenParticipation(participationRef, courseNode, coachCourseEnv.getCourseEnvironment());
		reload();
	}
	
	
	private class ToolsController extends BasicController {
		
		private Link reopenLink;
		private Link resetLink;
		
		private final FormParticipation formParticipation;
		
		public ToolsController(UserRequest ureq, WindowControl wControl, FormParticipation formParticipation) {
			super(ureq, wControl);
			this.formParticipation = formParticipation;
			
			VelocityContainer mainVC = createVelocityContainer("participation_tools");
			
			if (secCallback.canReopen() && EvaluationFormParticipationStatus.done == formParticipation.getParticipationStatus()) {
				reopenLink = LinkFactory.createLink("reopen", "reopen", getTranslator(), mainVC, this, Link.LINK);
				reopenLink.setIconLeftCSS("o_icon o_icon-fw o_icon_reopen");
			}
			if (secCallback.canReset()) {
				resetLink = LinkFactory.createLink("reset", "reset", getTranslator(), mainVC, this, Link.LINK);
				resetLink.setIconLeftCSS("o_icon o_icon-fw o_icon_delete_item");
			}
			
			putInitialPanel(mainVC);
		}

		@Override
		protected void doDispose() {
			//
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			this.fireEvent(ureq, Event.DONE_EVENT);
			if(reopenLink == source) {
				doConfirmReopen(ureq, formParticipation.getEvaluationFormParticipationRef());
			} else if(resetLink == source) {
				doConfirmReset(ureq, formParticipation.getEvaluationFormParticipationRef());
			}
		}
		
	}

}
