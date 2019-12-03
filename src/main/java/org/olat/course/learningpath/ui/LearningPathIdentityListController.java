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
package org.olat.course.learningpath.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.learningpath.ui.LearningPathIdentityDataModel.LearningPathIdentityCols;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.AssessmentService;
import org.olat.modules.assessment.ui.component.LearningProgressCompletionCellRenderer;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.RepositoryService;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 2 Dec 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class LearningPathIdentityListController extends FormBasicController implements Activateable2 {
	
	private static final String USAGE_IDENTIFIER = LearningPathIdentityListController.class.getCanonicalName();
	private static final String ORES_TYPE_IDENTITY = "Identity";
	private static final String CMD_SELECT = "select";
	
	private FlexiTableElement tableEl;
	private LearningPathIdentityDataModel dataModel;

	private LearningPathIdentityCtrl currentIdentityCtrl;
	
	private final TooledStackedPanel stackPanel;
	private final UserCourseEnvironment coachCourseEnv;
	private final List<UserPropertyHandler> userPropertyHandlers;
	private final boolean isAdministrativeUser;

	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private AssessmentService assessmentService;

	public LearningPathIdentityListController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			UserCourseEnvironment coachCourseEnv) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		this.stackPanel = stackPanel;
		this.coachCourseEnv = coachCourseEnv;
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		
		isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(USAGE_IDENTIFIER, isAdministrativeUser);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		if(isAdministrativeUser) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(LearningPathIdentityCols.username, CMD_SELECT));
		}
		
		int colIndex = LearningPathIdentityDataModel.USER_PROPS_OFFSET;
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			UserPropertyHandler userPropertyHandler = userPropertyHandlers.get(i);
			boolean visible = userManager.isMandatoryUserProperty(USAGE_IDENTIFIER, userPropertyHandler);
			columnsModel.addFlexiColumnModel(
					new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(), colIndex,
							CMD_SELECT, true, "userProp-" + colIndex));
			colIndex++;
		}
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(LearningPathIdentityCols.completion, new LearningProgressCompletionCellRenderer()));
		
		dataModel = new LearningPathIdentityDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 20, false, getTranslator(), formLayout);
		tableEl.setEmtpyTableMessageKey("table.empty.identities");
		tableEl.setExportEnabled(true);
		
		loadModel();
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if (entries == null || entries.isEmpty()) return;
		
		String resourceType = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if(ORES_TYPE_IDENTITY.equalsIgnoreCase(resourceType)) {
			Long identityKey = entries.get(0).getOLATResourceable().getResourceableId();
			for(int i=dataModel.getRowCount(); i--> 0; ) {
				LearningPathIdentityRow row = dataModel.getObject(i);
				if(row.getIdentityKey().equals(identityKey)) {
					doSelect(ureq, row);
				}
			}
		}	
		
	}

	private void loadModel() {
		CourseGroupManager cgm = coachCourseEnv.getCourseEnvironment().getCourseGroupManager();
		RepositoryEntry re = cgm.getCourseEntry();
		String subIdent = coachCourseEnv.getCourseEnvironment().getRunStructure().getRootNode().getIdent();
		
		List<Identity> coachedIdentities = coachCourseEnv.isAdmin()
				? repositoryService.getMembers(re, RepositoryEntryRelationType.all, GroupRoles.participant.name())
				: repositoryService.getCoachedParticipants(getIdentity(), re);
		
		List<AssessmentEntry> assessmentEntries = assessmentService.loadAssessmentEntriesBySubIdent(re, subIdent);
		Map<Long, Double> identityKeyToCompletion = new HashMap<>();
		for (AssessmentEntry assessmentEntry : assessmentEntries) {
			identityKeyToCompletion.put(assessmentEntry.getIdentity().getKey(), assessmentEntry.getCompletion());
		}
		
		List<LearningPathIdentityRow> rows = new ArrayList<>(coachedIdentities.size());
		for (Identity coachedIdentity : coachedIdentities) {
			Double completion = identityKeyToCompletion.get(coachedIdentity.getKey());
			LearningPathIdentityRow row = new LearningPathIdentityRow(coachedIdentity, userPropertyHandlers,
					getLocale(), completion);
			rows.add(row);
		}
		dataModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (tableEl == source) {
			if (event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				LearningPathIdentityRow row = dataModel.getObject(se.getIndex());
				if (CMD_SELECT.equals(cmd)) {
					doSelect(ureq, row);
				}
			}
		}
		
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doSelect(UserRequest ureq, LearningPathIdentityRow row) {
		removeAsListenerAndDispose(currentIdentityCtrl);
		
		Identity coachedIdentity = securityManager.loadIdentityByKey(row.getIdentityKey());
		String fullName = userManager.getUserDisplayName(coachedIdentity);
		
		OLATResourceable identityOres = OresHelper.createOLATResourceableInstance(ORES_TYPE_IDENTITY, coachedIdentity.getKey());
		WindowControl bwControl = addToHistory(ureq, identityOres, null);
		
		IdentityEnvironment identityEnv = new IdentityEnvironment();
		identityEnv.setIdentity(coachedIdentity);
		UserCourseEnvironment coachedCourseEnv = new UserCourseEnvironmentImpl(identityEnv, coachCourseEnv.getCourseEnvironment());
		currentIdentityCtrl = new LearningPathIdentityCtrl(ureq, bwControl, stackPanel, coachedCourseEnv);
		listenTo(currentIdentityCtrl);
		stackPanel.pushController(fullName, currentIdentityCtrl);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void doDispose() {
		//
	}

}
