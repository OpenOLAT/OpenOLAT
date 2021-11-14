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
package org.olat.user.ui.role;

import java.util.ArrayList;
import java.util.List;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.IdentityRelationshipService;
import org.olat.basesecurity.RelationRole;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.util.Util;
import org.olat.core.util.i18n.ui.SingleKeyTranslatorController;
import org.olat.user.UserModule;
import org.olat.user.ui.role.RelationRolesTableModel.RelationRoleCols;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 29 janv. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RelationRolesAdminController extends FormBasicController {
	
	private static final String[] onKeys = new String[] { "on" };
	
	private FlexiTableElement tableEl;
	private RelationRolesTableModel tableModel;
	
	private FormLink addRoleButton;
	private MultipleSelectionElement enableEl;
	
	private CloseableModalController cmc;
	private DialogBoxController confirmDeleteCtrl;
	private EditRelationRoleController editRoleCtrl;
	private SingleKeyTranslatorController translatorCtrl;
	
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private IdentityRelationshipService identityRelationsService;

	public RelationRolesAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "relation_roles", Util.createPackageTranslator(UserModule.class, ureq.getLocale()));
		initForm(ureq);
		loadModel();
		updateEnableDisable();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String[] onValues = new String[] { translate("on") };
		enableEl = uifactory.addCheckboxesHorizontal("relations.enable", formLayout, onKeys, onValues);
		enableEl.addActionListener(FormEvent.ONCHANGE);
		if(securityModule.isRelationRoleEnabled()) {
			enableEl.select(onKeys[0], true);
		}

		addRoleButton = uifactory.addFormLink("add.role", formLayout, Link.BUTTON);
		addRoleButton.setIconLeftCSS("o_icon o_icon_add");
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, RelationRoleCols.key));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RelationRoleCols.managed, new ManagedCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RelationRoleCols.identifier));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RelationRoleCols.role));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.header.translate", translate("table.header.translate"), "translate_role"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RelationRoleCols.descriptionRole));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.header.translate", translate("table.header.translate"), "translate_description"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RelationRoleCols.roleContra));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.header.translate", translate("table.header.translate"), "translate_role_contra"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RelationRoleCols.descriptionContraRole));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.header.translate", translate("table.header.translate"), "translate_description_contra"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("edit", translate("edit"), "edit"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("delete", RelationRoleCols.delete.ordinal(), "delete",
				new BooleanCellRenderer(
						new StaticFlexiCellRenderer(translate("delete"), "delete"), null)));
		
		tableModel = new RelationRolesTableModel(columnsModel, getTranslator(), getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 24, false, getTranslator(), formLayout);
	}
	
	private void updateEnableDisable() {
		boolean enabled = enableEl.isAtLeastSelected(1);
		tableEl.setVisible(enabled);
		addRoleButton.setVisible(enabled);
	}
	
	private void loadModel() {
		List<RelationRole> relationRoles = identityRelationsService.getAvailableRoles();

		List<RelationRoleRow> rows = new ArrayList<>(relationRoles.size());
		for(RelationRole relationRole:relationRoles) {
			rows.add(new RelationRoleRow(relationRole));
		}
		
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(editRoleCtrl == source || translatorCtrl == source) {
			if(event == Event.DONE_EVENT) {
				System.out.println("save");
			}
			loadModel();
			cmc.deactivate();
			cleanUp();
		} else if(confirmDeleteCtrl == source) {
			if(DialogBoxUIFactory.isOkEvent(event) || DialogBoxUIFactory.isYesEvent(event)) {
				doDelete((RelationRole)confirmDeleteCtrl.getUserObject());
				loadModel();
			}
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(confirmDeleteCtrl);
		removeAsListenerAndDispose(translatorCtrl);
		removeAsListenerAndDispose(editRoleCtrl);
		removeAsListenerAndDispose(cmc);
		confirmDeleteCtrl = null;
		translatorCtrl = null;
		editRoleCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(addRoleButton == source) {
			doAddRole(ureq);
		} else if(enableEl == source) {
			securityModule.setRelationRoleEnabled(enableEl.isAtLeastSelected(1));
			updateEnableDisable();
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				RelationRoleRow row = tableModel.getObject(se.getIndex());
				if("edit".equals(se.getCommand())) {
					doEditRole(ureq, row);
				} else if("delete".equals(se.getCommand())) {
					doConfirmDelete(ureq, row);
				} else if("translate_role".equals(se.getCommand())) {
					doTranslate(ureq, RelationRolesAndRightsUIFactory.TRANS_ROLE_PREFIX , row);
				} else if("translate_role_contra".equals(se.getCommand())) {
					doTranslate(ureq, RelationRolesAndRightsUIFactory.TRANS_ROLE_CONTRA_PREFIX , row);
				} else if("translate_description".equals(se.getCommand())) {
					doTranslate(ureq, RelationRolesAndRightsUIFactory.TRANS_ROLE_DESCRIPTION_PREFIX , row);
				} else if("translate_description_contra".equals(se.getCommand())) {
					doTranslate(ureq, RelationRolesAndRightsUIFactory.TRANS_ROLE_CONTRA_DESCRIPTION_PREFIX , row);
				} 
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doAddRole(UserRequest ureq) {
		if(guardModalController(editRoleCtrl)) return;

		editRoleCtrl = new EditRelationRoleController(ureq, getWindowControl(), null);
		listenTo(editRoleCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), "close", editRoleCtrl.getInitialComponent(), true, translate("add.role"));
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doEditRole(UserRequest ureq, RelationRoleRow row) {
		if(guardModalController(editRoleCtrl)) return;
		
		RelationRole role = identityRelationsService.getRole(row.getKey());
		if(role == null) {
			showWarning("error.relation.role.deleted");
			loadModel();
		} else {
			editRoleCtrl = new EditRelationRoleController(ureq, getWindowControl(), role);
			listenTo(editRoleCtrl);
			String title = translate("edit.role", new String[] { row.getRole() });
			cmc = new CloseableModalController(getWindowControl(), "close", editRoleCtrl.getInitialComponent(), true, title);
			listenTo(cmc);
			cmc.activate();
		}
	}
	
	private void doTranslate(UserRequest ureq, String prefix, RelationRoleRow relationRole) {
		String i18nKey = prefix.concat(relationRole.getKey().toString());
		translatorCtrl = new SingleKeyTranslatorController(ureq, getWindowControl(), i18nKey, UserModule.class);
		listenTo(translatorCtrl);
		String title = translate("translate.title", new String[] { relationRole.getRole() });
		cmc = new CloseableModalController(getWindowControl(), "close", translatorCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doConfirmDelete(UserRequest ureq, RelationRoleRow row) {
		RelationRole role = identityRelationsService.getRole(row.getKey());
		if(role == null) {
			showWarning("error.relation.role.deleted");
			loadModel();
		} else if(identityRelationsService.isInUse(role)) {
			showWarning("error.relation.role.in.use", new String[] { role.getRole() });
		} else {
			String title = translate("confirm.delete.role.title", new String[] { role.getRole() });
			String text = translate("confirm.delete.role.text", new String[] { role.getRole() });
			confirmDeleteCtrl = activateOkCancelDialog(ureq, title, text, confirmDeleteCtrl);
			confirmDeleteCtrl.setUserObject(role);
		}
	}
	
	private void doDelete(RelationRole role) {
		identityRelationsService.deleteRole(role);
		showInfo("info.role.delete", new String[] { role.getRole() });
	}
}
