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
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.TooledStackedPanel;
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
import org.olat.course.assessment.ui.tool.AssessmentToolConstants;
import org.olat.course.nodes.FormCourseNode;
import org.olat.course.nodes.form.FormManager;
import org.olat.course.nodes.form.ui.FormParticipationTableModel.ParticipationCols;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.forms.EvaluationFormParticipationStatus;
import org.olat.modules.forms.EvaluationFormSession;
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
			FormCourseNode courseNode, UserCourseEnvironment coachCourseEnv) {
		super(ureq, wControl, "participation_list");
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		this.stackPanel = stackPanel;
		this.courseNode = courseNode;
		this.coachCourseEnv = coachCourseEnv;
		courseEntry = coachCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		
		EvaluationFormSurveyIdentifier surveyIdent = formManager.getSurveyIdentifier(courseNode, courseEntry);
		survey = formManager.loadSurvey(surveyIdent);
		
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(FormParticipationTableModel.USAGE_IDENTIFIER, isAdministrativeUser);
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer buttonsTopCont = FormLayoutContainer.createButtonLayout("buttons.top", getTranslator());
		buttonsTopCont.setElementCssClass("o_button_group o_button_group_right");
		buttonsTopCont.setRootForm(mainForm);
		formLayout.add(buttonsTopCont);
			
		if (coachCourseEnv.isAdmin()) {
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
			boolean visible = UserManager.getInstance().isMandatoryUserProperty(AssessmentToolConstants.usageIdentifyer , userPropertyHandler);
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(), colIndex, CMD_SELECT, true, "userProp-" + colIndex));
			if(!options.hasDefaultOrderBy()) {
				options.setDefaultOrderBy(new SortKey("userProp-" + colIndex, true));
			}
			colIndex++;
		}
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ParticipationCols.status, new ParticipationStatusCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ParticipationCols.submissionDate));
		DefaultFlexiColumnModel toolsColumn = new DefaultFlexiColumnModel(ParticipationCols.tools);
		toolsColumn.setIconHeader("o_icon o_icon_actions o_icon-lg");
		toolsColumn.setExportable(false);
		toolsColumn.setAlwaysVisible(true);
		columnsModel.addFlexiColumnModel(toolsColumn);
		
		dataModel = new FormParticipationTableModel(columnsModel, getLocale()); 
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 20, false, getTranslator(), formLayout);
		tableEl.setExportEnabled(true);
		tableEl.setSortSettings(options);
		tableEl.setEmptyTableSettings("default.tableEmptyMessage", null, FormCourseNode.ICON_CSS);
		tableEl.setAndLoadPersistedPreferences(ureq, "course.element.form");
		
		List<FlexiTableFilter> filters = new ArrayList<>();
		filters.add(new FlexiTableFilter(translate("participation.status.notStart"), FormParticipationTableModel.FILTER_NOT_START));
		filters.add(new FlexiTableFilter(translate("participation.status.inProgress"), FormParticipationTableModel.FILTER_IN_PROGRESS));
		filters.add(new FlexiTableFilter(translate("participation.status.done"), FormParticipationTableModel.FILTER_DONE));
		filters.add(FlexiTableFilter.SPACER);
		filters.add(new FlexiTableFilter(translate("table.filter.show.all"), "showAll", true));
		tableEl.setFilters("", filters, false);
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

	private void loadModel() {
		List<Identity> coachedIdentities = formManager.getCoachedIdentities(coachCourseEnv);
		Map<Long, EvaluationFormParticipation> identityKeyToParticipations = formManager.getParticipations(survey, null, false)
				.stream()
				.collect(Collectors.toMap(
						participation -> participation.getExecutor().getKey(), 
						Function.identity()));
		Map<Long, Date> participationKeyToSubmissionDate = formManager.getDoneSessions(survey)
				.stream()
				.collect(Collectors.toMap(
						session -> session.getParticipation().getKey(),
						EvaluationFormSession::getSubmissionDate));
		
		List<FormParticipationRow> rows = new ArrayList<>(coachedIdentities.size());
		for (Identity identiy: coachedIdentities) {
			FormParticipationRow row = new FormParticipationRow(identiy, userPropertyHandlers, getLocale());
			EvaluationFormParticipation participation = identityKeyToParticipations.get(identiy.getKey());
			if (participation != null) {
				row.setStatus(participation.getStatus());
				if (EvaluationFormParticipationStatus.done == participation.getStatus()) {
					row.setSubmissionDate(participationKeyToSubmissionDate.get(participation.getKey()));
				}
				
				String linkName = "tools-" + counter++;
				FormLink toolsLink = uifactory.addFormLink(linkName, "", null, flc, Link.LINK | Link.NONTRANSLATED);
				toolsLink.setIconRightCSS("o_icon o_icon_actions o_icon-lg");
				toolsLink.setUserObject(participation);
				row.setToolsLink(toolsLink);
			}
			rows.add(row);
		}
		
		dataModel.setObjects(rows);
		tableEl.reset(true, true, true);
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
			}
		} else if (source instanceof FormLink) {
			FormLink link = (FormLink)source;
			String cmd = link.getCmd();
			if(cmd != null && cmd.startsWith("tools-")) {
				EvaluationFormParticipation participation = (EvaluationFormParticipation)link.getUserObject();
				doOpenTools(ureq, participation, link);
			}
		} else if (source == excelButton) {
			doExport(ureq);
		} else if (source == resetAllButton) {
			doConfirmDeleteAllData(ureq);
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
				EvaluationFormParticipation participation = (EvaluationFormParticipation)resetParticipationCtrl.getUserObject();
				doResetParticipation(participation);
			}
		} else if (source == reopenParticipationCtrl) {
			if (DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event)) {
				EvaluationFormParticipation participation = (EvaluationFormParticipation)reopenParticipationCtrl.getUserObject();
				doReopenParticipation(participation);
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
	
	private void doOpenTools(UserRequest ureq, EvaluationFormParticipation participation, FormLink link) {
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);

		toolsCtrl = new ToolsController(ureq, getWindowControl(), participation);
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
		loadModel();
	}
	
	private void doConfirmReset(UserRequest ureq, EvaluationFormParticipation participation) {
		String title = translate("reset.title");
		String text = translate("reset.text");
		resetParticipationCtrl = activateYesNoDialog(ureq, title, text, resetParticipationCtrl);
		resetParticipationCtrl.setUserObject(participation);
	}
	
	private void doResetParticipation(EvaluationFormParticipation participation) {
		formManager.deleteParticipation(participation, courseNode, coachCourseEnv.getCourseEnvironment());
		loadModel();
	}
	
	private void doConfirmReopen(UserRequest ureq, EvaluationFormParticipation participation) {
		String title = translate("reopen.title");
		String text = translate("reopen.text");
		reopenParticipationCtrl = activateYesNoDialog(ureq, title, text, resetParticipationCtrl);
		reopenParticipationCtrl.setUserObject(participation);
	}
	
	private void doReopenParticipation(EvaluationFormParticipation participation) {
		formManager.reopenParticipation(participation, courseNode, coachCourseEnv.getCourseEnvironment());
		loadModel();
	}
	
	
	private class ToolsController extends BasicController {
		
		private Link reopenLink;
		private Link resetLink;
		
		private final EvaluationFormParticipation participation;
		
		public ToolsController(UserRequest ureq, WindowControl wControl, EvaluationFormParticipation participation) {
			super(ureq, wControl);
			this.participation = participation;
			
			VelocityContainer mainVC = createVelocityContainer("participation_tools");
			
			if (EvaluationFormParticipationStatus.done == participation.getStatus()) {
				reopenLink = LinkFactory.createLink("reopen", "reopen", getTranslator(), mainVC, this, Link.LINK);
				reopenLink.setIconLeftCSS("o_icon o_icon-fw o_icon_reopen");
			}
			resetLink = LinkFactory.createLink("reset", "reset", getTranslator(), mainVC, this, Link.LINK);
			resetLink.setIconLeftCSS("o_icon o_icon-fw o_icon_delete_item");

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
				doConfirmReopen(ureq, participation);
			} else if(resetLink == source) {
				doConfirmReset(ureq, participation);
			}
		}
		
	}

}
