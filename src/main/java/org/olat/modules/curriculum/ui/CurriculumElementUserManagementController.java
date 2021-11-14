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
package org.olat.modules.curriculum.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.olat.admin.user.UserSearchController;
import org.olat.admin.user.UserTableDataModel;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.GroupMembershipInheritance;
import org.olat.basesecurity.events.MultiIdentityChosenEvent;
import org.olat.basesecurity.events.SingleIdentityChosenEvent;
import org.olat.basesecurity.model.IdentityRefImpl;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.Identity;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementManagedFlag;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumSecurityCallback;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumMember;
import org.olat.modules.curriculum.model.SearchMemberParameters;
import org.olat.modules.curriculum.ui.CurriculumUserManagementTableModel.CurriculumMemberCols;
import org.olat.modules.curriculum.ui.event.RoleEvent;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.olat.user.ui.organisation.component.RoleFlexiCellRenderer;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 9 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementUserManagementController extends FormBasicController {

	public static final int USER_PROPS_OFFSET = 500;
	public static final String usageIdentifyer = UserTableDataModel.class.getCanonicalName();
	
	private FlexiTableElement tableEl;
	private FormLink addMemberButton;
	private FormLink removeMembershipButton;
	private CurriculumUserManagementTableModel tableModel;
	
	private CloseableModalController cmc;
	private UserSearchController userSearchCtrl;
	private DialogBoxController confirmRemoveCtrl;
	
	private RoleListController roleListCtrl;
	private CloseableCalloutWindowController calloutCtrl;
	
	private final CurriculumElement curriculumElement;
	private final boolean membersManaged;
	private final CurriculumSecurityCallback secCallback;
	private final List<UserPropertyHandler> userPropertyHandlers;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private CurriculumService curriculumService;
	
	
	public CurriculumElementUserManagementController(UserRequest ureq, WindowControl wControl,
			CurriculumElement curriculumElement, CurriculumSecurityCallback secCallback) {
		super(ureq, wControl, "curriculum_element_user_mgmt");
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		
		this.curriculumElement = curriculumElement;
		this.secCallback = secCallback;
		
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(usageIdentifyer, isAdministrativeUser);
		
		membersManaged = CurriculumElementManagedFlag.isManaged(curriculumElement, CurriculumElementManagedFlag.members);

		initForm(ureq);
		loadModel(true);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(!membersManaged && secCallback.canManagerCurriculumElementUsers(curriculumElement)) {
			addMemberButton = uifactory.addFormLink("add.member", formLayout, Link.BUTTON);
			addMemberButton.setIconLeftCSS("o_icon o_icon-fw o_icon_add_member");
			addMemberButton.setIconRightCSS("o_icon o_icon_caret");
		
			removeMembershipButton = uifactory.addFormLink("remove.memberships", formLayout, Link.BUTTON);
		}
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();

		int colIndex = USER_PROPS_OFFSET;
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			UserPropertyHandler userPropertyHandler	= userPropertyHandlers.get(i);
			boolean visible = userManager.isMandatoryUserProperty(usageIdentifyer , userPropertyHandler);
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(), colIndex, null, true, "userProp-" + colIndex));
			colIndex++;
		}
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CurriculumMemberCols.role, new RoleFlexiCellRenderer(getTranslator())));

		tableModel = new CurriculumUserManagementTableModel(columnsModel, getLocale()); 
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setExportEnabled(true);
		tableEl.setSelectAllEnable(true);
		tableEl.setMultiSelect(true);
		tableEl.setSearchEnabled(true);
	}
	
	private void loadModel(boolean reset) {
		SearchMemberParameters params = new SearchMemberParameters();
		params.setSearchString(tableEl.getQuickSearchString());
		params.setUserProperties(userPropertyHandlers);
		List<CurriculumMember> members = curriculumService.getMembers(curriculumElement, params);
		List<CurriculumMemberRow> rows = new ArrayList<>(members.size());
		for(CurriculumMember member:members) {
			rows.add(new CurriculumMemberRow(member, userPropertyHandlers, getLocale()));
		}
		tableModel.setObjects(rows);
		tableEl.reset(reset, reset, true);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(confirmRemoveCtrl == source) {
			if (DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event)) {
				@SuppressWarnings("unchecked")
				List<CurriculumMemberRow> rows = (List<CurriculumMemberRow>)confirmRemoveCtrl.getUserObject();
				doRemove(rows);
			}
		} else if(userSearchCtrl == source) {
			if (event instanceof SingleIdentityChosenEvent) {
				SingleIdentityChosenEvent singleEvent = (SingleIdentityChosenEvent)event;
				Identity choosenIdentity = singleEvent.getChosenIdentity();
				if (choosenIdentity != null) {
					List<Identity> toAdd = Collections.singletonList(choosenIdentity);
					doAddMember(toAdd, (CurriculumRoles)userSearchCtrl.getUserObject());
				}
			} else if (event instanceof MultiIdentityChosenEvent) {
				MultiIdentityChosenEvent multiEvent = (MultiIdentityChosenEvent)event;
				if(!multiEvent.getChosenIdentities().isEmpty()) {
					doAddMember(multiEvent.getChosenIdentities(), (CurriculumRoles)userSearchCtrl.getUserObject());
				}
			}
			cmc.deactivate();
			cleanUp();
		} else if(roleListCtrl == source) {
			calloutCtrl.deactivate();
			cleanUp();
			if(event instanceof RoleEvent) {
				RoleEvent re = (RoleEvent)event;
				doSearchMember(ureq, re.getRole());
			}
		} else if(cmc == source || calloutCtrl == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(confirmRemoveCtrl);
		removeAsListenerAndDispose(userSearchCtrl);
		removeAsListenerAndDispose(calloutCtrl);
		removeAsListenerAndDispose(cmc);
		confirmRemoveCtrl = null;
		userSearchCtrl = null;
		calloutCtrl = null;
		cmc = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(addMemberButton == source) {
			doRolleCallout(ureq);
		} else if(removeMembershipButton == source) {
			doConfirmRemoveAllMemberships(ureq);
		} else if(tableEl == source) {
			if(event instanceof FlexiTableSearchEvent) {
				loadModel(true);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doConfirmRemoveAllMemberships(UserRequest ureq) {
		Set<Integer> selectedRows = tableEl.getMultiSelectedIndex();
		List<CurriculumMemberRow> rows = new ArrayList<>(selectedRows.size());
		for(Integer selectedRow:selectedRows) {
			CurriculumMemberRow row = tableModel.getObject(selectedRow.intValue());
			if(row.getInheritanceMode() == GroupMembershipInheritance.root || row.getInheritanceMode() == GroupMembershipInheritance.none) {
				rows.add(row);
			}
		}
		
		if(rows.isEmpty()) {
			showWarning("warning.atleastone.member");
		} else {
			String title = translate("confirm.remove.member.title");
			confirmRemoveCtrl = activateYesNoDialog(ureq, title, translate("confirm.remove.member.text", ""), confirmRemoveCtrl);
			confirmRemoveCtrl.setUserObject(rows);
		}
	}
	
	private void doRemove(List<CurriculumMemberRow> membersToRemove) {
		for(CurriculumMemberRow memberToRemove:membersToRemove) {
			if(CurriculumRoles.isValueOf(memberToRemove.getRole())) {
				CurriculumRoles role = CurriculumRoles.valueOf(memberToRemove.getRole());
				curriculumService.removeMember(curriculumElement, new IdentityRefImpl(memberToRemove.getIdentityKey()), role);
			}
		}
		loadModel(true);
	}
	
	private void doRolleCallout(UserRequest ureq) {
		removeAsListenerAndDispose(calloutCtrl);
		removeAsListenerAndDispose(roleListCtrl);
		
		String title = translate("add.member");
		CurriculumRoles[] roles = new CurriculumRoles[] {
				CurriculumRoles.curriculumelementowner, CurriculumRoles.mastercoach,
				CurriculumRoles.owner, CurriculumRoles.coach, CurriculumRoles.participant
		};
		roleListCtrl = new RoleListController(ureq, getWindowControl(), roles);
		listenTo(roleListCtrl);
		
		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(), roleListCtrl.getInitialComponent(), addMemberButton, title, true, null);
		listenTo(calloutCtrl);
		calloutCtrl.activate();	
	}
	
	private void doSearchMember(UserRequest ureq, CurriculumRoles role) {
		if(guardModalController(userSearchCtrl)) return;

		userSearchCtrl = new UserSearchController(ureq, getWindowControl(), true, true, false);
		userSearchCtrl.setUserObject(role);
		listenTo(userSearchCtrl);
		
		String title = translate("add.member.role", new String[] { translate("role.".concat(role.name())) });
		cmc = new CloseableModalController(getWindowControl(), translate("close"), userSearchCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doAddMember(List<Identity> identitiesToAdd, CurriculumRoles role) {
		for(Identity identityToAdd:identitiesToAdd) {
			curriculumService.addMember(curriculumElement, identityToAdd, role);
		}
		loadModel(true);
	}
	
}
