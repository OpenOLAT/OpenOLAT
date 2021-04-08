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
package org.olat.user.ui.admin.lifecycle;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.model.DeletedIdentity;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.user.UserManager;
import org.olat.user.ui.admin.lifecycle.DeletedUsersTableModel.DeletedCols;
import org.olat.user.ui.organisation.OrganisationUserManagementController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 22 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DeletedUsersController extends FormBasicController {

	private FlexiTableElement tableEl;
	private DeletedUsersTableModel tableModel;
	
	private DialogBoxController confirmClearCtrl;
	
	private final boolean isAdministrativeUser;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private BaseSecurityModule securityModule;
	
	public DeletedUsersController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "deleted_users");
		setTranslator(Util.createPackageTranslator(OrganisationUserManagementController.class, getLocale(), getTranslator()));

		Roles roles = ureq.getUserSession().getRoles();
		isAdministrativeUser = securityModule.isUserAllowedAdminProps(roles);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		if(isAdministrativeUser) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(DeletedCols.username));
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(DeletedCols.firstName));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(DeletedCols.lastName));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(DeletedCols.deletedDate));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(DeletedCols.lastLogin));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(DeletedCols.creationDate));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, DeletedCols.deletedRoles,
				new DeletedRolesCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(DeletedCols.deletedBy));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("clear", DeletedCols.clear.ordinal(), "clear",
				new BooleanCellRenderer(new StaticFlexiCellRenderer(translate("clear"), "clear"), null)));
		
		tableModel = new DeletedUsersTableModel(new DeletedUserDataSource(), userManager, columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 25, true, getTranslator(), formLayout);
		tableEl.setCustomizeColumns(true);
		tableEl.setEmptyTableSettings("error.no.user.found", null, "o_icon_user");
		tableEl.setExportEnabled(false);
		tableEl.setAndLoadPersistedPreferences(ureq, "deleted-user-list-v2");
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(confirmClearCtrl == source) {
			if (DialogBoxUIFactory.isOkEvent(event) || DialogBoxUIFactory.isYesEvent(event)) {
				doClear((Identity)confirmClearCtrl.getUserObject());
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				if("clear".equals(se.getCommand())) {
					DeletedIdentity deletedIdentity = tableModel.getObject(se.getIndex());
					doConfirmClear(ureq, deletedIdentity);
				}
				
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doConfirmClear(UserRequest ureq, DeletedIdentity deletedIdentity) {
		if(!StringHelper.containsNonWhitespace(deletedIdentity.getIdentityFirstName())
				&& !StringHelper.containsNonWhitespace(deletedIdentity.getIdentityLastName())) return;
		
		Identity identity = securityManager.loadIdentityByKey(deletedIdentity.getIdentityKey());
		String fullname = userManager.getUserDisplayName(identity);
		String text = translate("confirm.clear.identity", new String[] { fullname });
		confirmClearCtrl = activateOkCancelDialog(ureq, translate("clear"), text, confirmClearCtrl);
		confirmClearCtrl.setUserObject(identity);
	}
	
	private void doClear(Identity deletedIdentity) {
		userManager.clearAllUserProperties(deletedIdentity);
		tableModel.clear();
		tableEl.reset(false, false, true);
	}
}
