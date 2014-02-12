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
package org.olat.course.nodes.cl.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.olat.NewControllerFactory;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.SecurityGroup;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.id.UserConstants;
import org.olat.course.nodes.CheckListCourseNode;
import org.olat.course.nodes.cl.CheckboxManager;
import org.olat.course.nodes.cl.model.AssessmentDataView;
import org.olat.course.nodes.cl.model.Checkbox;
import org.olat.course.nodes.cl.model.CheckboxList;
import org.olat.course.nodes.cl.ui.CheckboxAssessmentDataModel.Cols;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupMembership;
import org.olat.group.BusinessGroupService;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.model.RepositoryEntryMembership;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: 07.02.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CheckListAssessmentController extends FormBasicController implements ControllerEventListener {
	
	protected static final String USER_PROPS_ID = CheckListAssessmentController.class.getCanonicalName();

	private final Date dueDate;
	private final Boolean closeAfterDueDate;
	private final OLATResourceable courseOres;
	private final CheckListCourseNode courseNode;
	private final ModuleConfiguration config;
	private final UserCourseEnvironment userCourseEnv;
	private final boolean isAdministrativeUser;
	
	private CheckboxAssessmentDataModel model;
	private FlexiTableElement table;
	private final List<UserPropertyHandler> userPropertyHandlers;
	
	private CloseableModalController cmc;
	private AssessedIdentityOverviewController editCtrl;
	
	private final UserManager userManager;
	private final BaseSecurity securityManager;
	private final CheckboxManager checkboxManager;
	private final RepositoryManager repositoryManager;
	private final BusinessGroupService businessGroupService;
	
	/**
	 * Use this constructor to launch the checklist.
	 * 
	 * @param ureq
	 * @param wControl
	 * @param courseNode
	 */
	public CheckListAssessmentController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv,
			OLATResourceable courseOres, CheckListCourseNode courseNode) {
		super(ureq, wControl, "assessment_list");

		userManager = CoreSpringFactory.getImpl(UserManager.class);
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		securityManager = CoreSpringFactory.getImpl(BaseSecurity.class);
		checkboxManager = CoreSpringFactory.getImpl(CheckboxManager.class);
		repositoryManager = CoreSpringFactory.getImpl(RepositoryManager.class);
		businessGroupService = CoreSpringFactory.getImpl(BusinessGroupService.class);
		BaseSecurityModule securityModule = CoreSpringFactory.getImpl(BaseSecurityModule.class);
		
		this.courseOres = courseOres;
		this.courseNode = courseNode;
		this.userCourseEnv = userCourseEnv;
		config = courseNode.getModuleConfiguration();
		Roles roles = ureq.getUserSession().getRoles();
		isAdministrativeUser = securityModule.isUserAllowedAdminProps(roles);
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(USER_PROPS_ID, isAdministrativeUser);
		
		closeAfterDueDate = (Boolean)config.get(CheckListCourseNode.CONFIG_KEY_CLOSE_AFTER_DUE_DATE);
		if(closeAfterDueDate != null && closeAfterDueDate.booleanValue()) {
			dueDate = (Date)config.get(CheckListCourseNode.CONFIG_KEY_DUE_DATE);
		} else {
			dueDate = null;
		}

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			if(dueDate != null) {
				layoutCont.contextPut("dueDate", dueDate);
			}
		}
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		if(isAdministrativeUser) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.username.i18nKey(), Cols.username.ordinal()));
		}
		
		int i=0;
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			int colIndex = CheckboxAssessmentDataModel.USER_PROPS_OFFSET + i++;
			if (userPropertyHandler == null) continue;
			
			String propName = userPropertyHandler.getName();
			boolean visible = userManager.isMandatoryUserProperty(USER_PROPS_ID , userPropertyHandler);
			if(visible) {
				FlexiColumnModel col;
				if(UserConstants.FIRSTNAME.equals(propName)
						|| UserConstants.LASTNAME.equals(propName)) {
					col = new StaticFlexiColumnModel(userPropertyHandler.i18nColumnDescriptorLabelKey(),
							colIndex, userPropertyHandler.getName(), true, propName,
							new StaticFlexiCellRenderer(userPropertyHandler.getName(), new TextFlexiCellRenderer()));
				} else {
					col = new DefaultFlexiColumnModel(true, userPropertyHandler.i18nColumnDescriptorLabelKey(), colIndex, true, propName);
				}
				columnsModel.addFlexiColumnModel(col);
			}
		}
		
		CheckboxList list = (CheckboxList)config.get(CheckListCourseNode.CONFIG_KEY_CHECKBOX);
		List<Checkbox> checkboxList = list.getList();
		int j = 0;
		for(Checkbox box:checkboxList) {
			int colIndex = CheckboxAssessmentDataModel.CHECKBOX_OFFSET + j++;
			String colName = "checkbox_" + colIndex;
			DefaultFlexiColumnModel column = new DefaultFlexiColumnModel(true, colName, colIndex, true, colName);
			column.setHeaderLabel(box.getTitle());
			columnsModel.addFlexiColumnModel(column);
		}

		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, Cols.totalPoints.i18nKey(), Cols.totalPoints.ordinal(), true, "points"));
		columnsModel.addFlexiColumnModel(new StaticFlexiColumnModel("edit.checkbox", translate("edit.checkbox"), "edit"));
		
		String[] keys = null;
		String[] values = null;
		if(userCourseEnv instanceof UserCourseEnvironmentImpl) {
			UserCourseEnvironmentImpl env = (UserCourseEnvironmentImpl)userCourseEnv;
			List<BusinessGroup> coachedGroups = env.getCoachedGroups();
			keys = new String[coachedGroups.size() + 1];
			values = new String[coachedGroups.size() + 1];
			
			keys[0] = "all";
			values[0] = translate("filter.all");
			for(int k=0; k<coachedGroups.size(); k++) {
				BusinessGroup group = coachedGroups.get(k);
				keys[k+1] = group.getKey().toString();
				values[k+1] = group.getName();
			}
		}
		
		List<AssessmentDataView> datas = loadDatas();
		model = new CheckboxAssessmentDataModel(datas, columnsModel);
		table = uifactory.addTableElement(ureq, getWindowControl(), "checkbox-list", model, getTranslator(), formLayout);
		table.setFilterKeysAndValues("participants", keys, values);
		table.setExportEnabled(true);
	}
	
	private List<AssessmentDataView> loadDatas() {
		if(!(userCourseEnv instanceof UserCourseEnvironmentImpl)) {
			return Collections.emptyList();
		}

		UserCourseEnvironmentImpl env = (UserCourseEnvironmentImpl)userCourseEnv;
		CheckboxList list = (CheckboxList)config.get(CheckListCourseNode.CONFIG_KEY_CHECKBOX);
		List<Checkbox> checkboxList = list.getList();

		List<SecurityGroup> secGroups = new ArrayList<SecurityGroup>();
		Map<Long,Long> groupToSecGroupKey = new HashMap<Long,Long>();

		RepositoryEntry re = env.getCourseRepositoryEntry();
		boolean courseTutor = securityManager.isIdentityInSecurityGroup(getIdentity(), re.getTutorGroup());
		
		Set<Long> missingIdentityKeys = new HashSet<>();
		if(courseTutor) {
			secGroups.add(re.getParticipantGroup());
			List<RepositoryEntryMembership> repoMemberships = repositoryManager.getRepositoryEntryMembership(re);
			for(RepositoryEntryMembership repoMembership:repoMemberships) {
				if(repoMembership.getParticipantRepoKey() == null) continue;
				missingIdentityKeys.add(repoMembership.getIdentityKey());
			}
		}

		List<BusinessGroup> coachedGroups = env.getCoachedGroups();
		for(BusinessGroup group:coachedGroups) {
			secGroups.add(group.getPartipiciantGroup());
			groupToSecGroupKey.put(group.getKey(), group.getPartipiciantGroup().getKey());
		}
		
		List<AssessmentDataView> boxList = checkboxManager.getAssessmentDataViews(courseOres, courseNode.getIdent(),
				checkboxList, secGroups, userPropertyHandlers, getLocale());
		Map<Long,AssessmentDataView> identityToView = new HashMap<>();
		for(AssessmentDataView box:boxList) {
			identityToView.put(box.getIdentityKey(), box);
			missingIdentityKeys.remove(box.getIdentityKey());
		}
		
		List<BusinessGroupMembership> memberships = businessGroupService.getBusinessGroupsMembership(coachedGroups);
		for(BusinessGroupMembership membership:memberships) {
			if(!membership.isParticipant()) continue;
			Long identityKey = membership.getIdentityKey();
			if(!identityToView.containsKey(identityKey)) {
				missingIdentityKeys.add(identityKey);
			}
		}

		List<Identity> missingIdentities = securityManager.loadIdentityByKeys(missingIdentityKeys);
		for(Identity missingIdentity:missingIdentities) {
			AssessmentDataView view = new AssessmentDataView(missingIdentity, null, null, userPropertyHandlers, getLocale());
			identityToView.put(missingIdentity.getKey(), view);
		}
		
		for(BusinessGroupMembership membership:memberships) {
			if(!membership.isParticipant()) continue;
			AssessmentDataView view = identityToView.get(membership.getIdentityKey());
			if(view != null) {
				view.addGroupKey(membership.getGroupKey());
			}
		}
		
		List<AssessmentDataView> views = new ArrayList<>();
		views.addAll(identityToView.values());
		return views;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(table == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				if("edit".equals(cmd)) {
					AssessmentDataView row = model.getObject(se.getIndex());
					doOpenEdit(ureq, row);
				} else if(UserConstants.FIRSTNAME.equals(cmd) || UserConstants.LASTNAME.equals(cmd)) {
					AssessmentDataView row = model.getObject(se.getIndex());
					doOpenIdentity(ureq, row);
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(editCtrl == source) {
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(editCtrl);
		removeAsListenerAndDispose(cmc);
		editCtrl = null;
		cmc = null;
	}

	@Override
	protected void doDispose() {
		//
	}
	
	private void doOpenIdentity(UserRequest ureq, AssessmentDataView row) {
		String businessPath = "[Identity:" + row.getIdentityKey() + "]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}
	
	private void doOpenEdit(UserRequest ureq, AssessmentDataView row) {
		if(editCtrl != null) return;
		
		Identity assessedIdentity = securityManager.loadIdentityByKey(row.getIdentityKey());
		editCtrl = new AssessedIdentityOverviewController(ureq, getWindowControl(), assessedIdentity, courseOres, courseNode);
		listenTo(editCtrl);

		String title = courseNode.getShortTitle();
		Component content = editCtrl.getInitialComponent();
		cmc = new CloseableModalController(getWindowControl(), "close", content, true, title);
		listenTo(cmc);
		cmc.activate();
	}
}