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
package org.olat.user.ui.organisation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.olat.admin.user.UserSearchController;
import org.olat.admin.user.UserTableDataModel;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.OrganisationManagedFlag;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.events.MultiIdentityChosenEvent;
import org.olat.basesecurity.events.SingleIdentityChosenEvent;
import org.olat.basesecurity.model.IdentityRefImpl;
import org.olat.basesecurity.model.OrganisationMember;
import org.olat.basesecurity.model.SearchMemberParameters;
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
import org.olat.core.id.Organisation;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.olat.user.ui.organisation.OrganisationUserManagementTableModel.MemberCols;
import org.olat.user.ui.organisation.component.InheritanceModeFlexiCellRenderer;
import org.olat.user.ui.organisation.component.RoleFlexiCellRenderer;
import org.olat.user.ui.organisation.event.RoleEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 14 mars 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OrganisationUserManagementController extends FormBasicController {
	
	public static final int USER_PROPS_OFFSET = 500;
	public static final String usageIdentifyer = UserTableDataModel.class.getCanonicalName();
	
	private FlexiTableElement tableEl;
	private FormLink addMemberButton;
	private FormLink removeMembershipButton;
	private OrganisationUserManagementTableModel tableModel;
	
	private CloseableModalController cmc;
	private UserSearchController userSearchCtrl;
	private DialogBoxController confirmRemoveCtrl;
	
	private RoleListController roleListCtrl;
	private CloseableCalloutWindowController calloutCtrl;
	
	private Organisation organisation;
	private final boolean membersManaged;
	private final List<UserPropertyHandler> userPropertyHandlers;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private OrganisationService organisationService;
	
	public OrganisationUserManagementController(UserRequest ureq, WindowControl wControl, Organisation organisation) {
		super(ureq, wControl, "organisation_user_mgmt");
		this.organisation = organisation;
		
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(usageIdentifyer, isAdministrativeUser);
		
		membersManaged = OrganisationManagedFlag.isManaged(organisation, OrganisationManagedFlag.members);

		initForm(ureq);
		loadModel(true);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(!membersManaged) {
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
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MemberCols.role, new RoleFlexiCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MemberCols.inheritance, new InheritanceModeFlexiCellRenderer(getTranslator())));

		tableModel = new OrganisationUserManagementTableModel(columnsModel, getLocale()); 
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setExportEnabled(true);
		tableEl.setSelectAllEnable(true);
		tableEl.setMultiSelect(true);
		tableEl.setSearchEnabled(true);
		tableEl.setAndLoadPersistedPreferences(ureq, "organisation-user-list-v2");
		tableEl.addBatchButton(removeMembershipButton);
	}
	
	private void loadModel(boolean reset) {
		SearchMemberParameters params = new SearchMemberParameters();
		params.setSearchString(tableEl.getQuickSearchString());
		params.setUserProperties(userPropertyHandlers);
		List<OrganisationMember> members = organisationService.getMembers(organisation, params);
		List<OrganisationUserRow> rows = new ArrayList<>(members.size());
		for(OrganisationMember member:members) {
			rows.add(new OrganisationUserRow(member, userPropertyHandlers, getLocale()));
		}
		tableModel.setObjects(rows);
		tableEl.reset(reset, reset, true);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(confirmRemoveCtrl == source) {
			if (DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event)) {
				@SuppressWarnings("unchecked")
				List<OrganisationUserRow> rows = (List<OrganisationUserRow>)confirmRemoveCtrl.getUserObject();
				doRemove(rows);
			}
		} else if(userSearchCtrl == source) {
			if (event instanceof SingleIdentityChosenEvent) {
				SingleIdentityChosenEvent singleEvent = (SingleIdentityChosenEvent)event;
				Identity choosenIdentity = singleEvent.getChosenIdentity();
				if (choosenIdentity != null) {
					List<Identity> toAdd = Collections.singletonList(choosenIdentity);
					doAddMember(toAdd, (OrganisationRoles)userSearchCtrl.getUserObject());
				}
			} else if (event instanceof MultiIdentityChosenEvent) {
				MultiIdentityChosenEvent multiEvent = (MultiIdentityChosenEvent)event;
				if(!multiEvent.getChosenIdentities().isEmpty()) {
					doAddMember(multiEvent.getChosenIdentities(), (OrganisationRoles)userSearchCtrl.getUserObject());
				}
			}
			cmc.deactivate();
			cleanUp();
		} else if(roleListCtrl == source) {
			calloutCtrl.deactivate();
			cleanUp();
			if(event instanceof RoleEvent) {
				RoleEvent re = (RoleEvent)event;
				doSearchMember(ureq, re.getSelectedRole());
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
		if(selectedRows.isEmpty()) {
			showWarning("warning.atleastone.member");
		} else {
			List<OrganisationUserRow> rows = new ArrayList<>(selectedRows.size());
			for(Integer selectedRow:selectedRows) {
				rows.add(tableModel.getObject(selectedRow.intValue()));
			}
			String title = translate("confirm.remove.title");
			confirmRemoveCtrl = activateYesNoDialog(ureq, title, translate("confirm.remove.text", ""), confirmRemoveCtrl);
			confirmRemoveCtrl.setUserObject(rows);
		}
	}
	
	private void doRemove(List<OrganisationUserRow> membersToRemove) {
		boolean warningUser = false;
		for(OrganisationUserRow memberToRemove:membersToRemove) {
			if(OrganisationRoles.isValue(memberToRemove.getRole()) && isAlwaysUser(memberToRemove)) {
				organisationService.removeMember(organisation, new IdentityRefImpl(memberToRemove.getIdentityKey()),
						OrganisationRoles.valueOf(memberToRemove.getRole()), false);
			} else {
				warningUser = true;
			}
		}
		loadModel(true);
		if(warningUser) {
			showWarning("warning.user.orphan");
		}
	}
	
	private boolean isAlwaysUser(OrganisationUserRow memberToRemove) {
		if(OrganisationRoles.user.name().equals(memberToRemove.getRole())) {
			List<Organisation> orgAsUser = organisationService
					.getOrganisations(new IdentityRefImpl(memberToRemove.getIdentityKey()), OrganisationRoles.user);
			return orgAsUser.size() > 1;
		}
		return true;
	}
	
	private void doRolleCallout(UserRequest ureq) {
		removeAsListenerAndDispose(calloutCtrl);
		removeAsListenerAndDispose(roleListCtrl);
		
		String title = translate("add.member");
		roleListCtrl = new RoleListController(ureq, getWindowControl(), OrganisationRoles.valuesWithoutGuestAndInvitee());
		listenTo(roleListCtrl);
		
		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(), roleListCtrl.getInitialComponent(), addMemberButton, title, true, null);
		listenTo(calloutCtrl);
		calloutCtrl.activate();	
	}
	
	private void doSearchMember(UserRequest ureq, OrganisationRoles role) {
		if(guardModalController(userSearchCtrl)) return;

		userSearchCtrl = new UserSearchController(ureq, getWindowControl(), true, true, false,
				new OrganisationRoles[] { OrganisationRoles.guest, OrganisationRoles.invitee }, true);
		userSearchCtrl.setUserObject(role);
		listenTo(userSearchCtrl);
		
		String title = translate("add.member.role", translate("role.".concat(role.name())));
		cmc = new CloseableModalController(getWindowControl(), translate("close"), userSearchCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doAddMember(List<Identity> identitiesToAdd, OrganisationRoles role) {
		for(Identity identityToAdd:identitiesToAdd) {
			organisationService.addMember(organisation, identityToAdd, role);
		}
		loadModel(true);
	}
}
