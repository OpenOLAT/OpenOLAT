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
import java.util.Map;
import java.util.stream.Collectors;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
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
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.assessment.ui.tool.AssessmentToolConstants;
import org.olat.course.nodes.FormCourseNode;
import org.olat.course.nodes.form.FormManager;
import org.olat.course.nodes.form.ui.FormParticipationTableModel.ParticipationCols;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.modules.forms.EvaluationFormParticipation;
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
	
	private FormLink resetButton;
	private FormLink excelButton;
	private final List<UserPropertyHandler> userPropertyHandlers;
	private FormParticipationTableModel dataModel;
	private FlexiTableElement tableEl;

	private TooledStackedPanel stackPanel;
	private CloseableModalController cmc;
	private FormParticipationController particpationCtrl;
	private FormResetDataConfirmationController resetDataConfirmationCtrl;
	
	private final FormCourseNode courseNode;
	private final UserCourseEnvironment coachCourseEnv;
	private final RepositoryEntry courseEntry;
	private final EvaluationFormSurvey survey;

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
		super(ureq, wControl, LAYOUT_BAREBONE);
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
			resetButton = uifactory.addFormLink("reset.data", buttonsTopCont, Link.BUTTON); 
			resetButton.setIconLeftCSS("o_icon o_icon-fw o_icon_delete_item");
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
		Map<Long, EvaluationFormParticipationStatus> identityKeyToStatus = formManager.getParticipations(survey, null, false)
				.stream()
				.collect(Collectors.toMap(
						participation -> participation.getExecutor().getKey(), 
						EvaluationFormParticipation::getStatus));
		
		List<FormParticipationRow> rows = new ArrayList<>(coachedIdentities.size());
		for (Identity identiy: coachedIdentities) {
			FormParticipationRow row = new FormParticipationRow(identiy, userPropertyHandlers, getLocale());
			row.setStatus(identityKeyToStatus.get(identiy.getKey()));
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
		} else if (source == excelButton) {
			doExport(ureq);
		} else if (source == resetButton) {
			doConfirmDeleteAllData(ureq);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		 if (source == resetDataConfirmationCtrl) {
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
		removeAsListenerAndDispose(cmc);
		resetDataConfirmationCtrl = null;
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
		
		Identity coachedIdentity = securityManager.loadIdentityByKey(row.getIdentityKey());
		String fullName = userManager.getUserDisplayName(coachedIdentity);
		
		OLATResourceable identityOres = OresHelper.createOLATResourceableInstance(ORES_TYPE_IDENTITY, coachedIdentity.getKey());
		WindowControl bwControl = addToHistory(ureq, identityOres, null);
		
		IdentityEnvironment identityEnv = new IdentityEnvironment();
		identityEnv.setIdentity(coachedIdentity);
		UserCourseEnvironment coachedCourseEnv = new UserCourseEnvironmentImpl(identityEnv, coachCourseEnv.getCourseEnvironment());
		particpationCtrl = new FormParticipationController(ureq, bwControl, courseNode, coachedCourseEnv);
		listenTo(particpationCtrl);
		stackPanel.pushController(fullName, particpationCtrl);
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
				resetDataConfirmationCtrl.getInitialComponent(), true, translate("reset.data.title"), true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doDeleteAllData() {
		formManager.deleteAllData(survey, courseNode, coachCourseEnv);
		loadModel();
	}

}
