/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.certificationprogram.ui;

import java.util.List;
import java.util.Set;

import org.olat.admin.user.UserTableDataModel;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.events.MultiIdentityChosenEvent;
import org.olat.basesecurity.events.SingleIdentityChosenEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.ActionsCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.ActionsColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Identity;
import org.olat.modules.certificationprogram.CertificationProgram;
import org.olat.modules.certificationprogram.CertificationProgramService;
import org.olat.modules.certificationprogram.ui.CertificationProgramOwnersTableModel.CertificationProgramOwnersCols;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 24 sept. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CertificationProgramOwnersController extends FormBasicController {

	public static final int USER_PROPS_OFFSET = 500;
	public static final String usageIdentifyer = UserTableDataModel.class.getCanonicalName();
	
	private FormLink addOwnerButton;
	private FormLink removeOwnersButton;
	private FlexiTableElement tableEl;
	private CertificationProgramOwnersTableModel tableModel;
	
	private CertificationProgram certificationProgram;
	protected final List<UserPropertyHandler> userPropertyHandlers;

	private ToolsController toolsCtrl;
	private CloseableModalController cmc;
	private UserSearchController addOwnerCtrl;
	private CloseableCalloutWindowController calloutCtrl;
	private ConfirmationRemoveOwnersController confirmRemoveCtrl;
	
	@Autowired
	protected UserManager userManager;
	@Autowired
	protected BaseSecurity securityManager;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private CertificationProgramService certificationProgramService;
	
	
	public CertificationProgramOwnersController(UserRequest ureq, WindowControl wControl, CertificationProgram certificationProgram) {
		super(ureq, wControl, "owners_list");
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		this.certificationProgram = certificationProgram;
		
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(usageIdentifyer, isAdministrativeUser);
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		addOwnerButton = uifactory.addFormLink("add.owner", formLayout, Link.BUTTON);
		addOwnerButton.setIconLeftCSS("o_icon o_icon_add_member");
		
		removeOwnersButton = uifactory.addFormLink("remove", formLayout, Link.BUTTON);
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, CertificationProgramOwnersCols.id));

		int colIndex = USER_PROPS_OFFSET;
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			UserPropertyHandler userPropertyHandler	= userPropertyHandlers.get(i);
			boolean visible = userManager.isMandatoryUserProperty(usageIdentifyer , userPropertyHandler);
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(),
					colIndex, null, true, "userProp-" + colIndex));
			colIndex++;
		}
		
        ActionsColumnModel actionsCol = new ActionsColumnModel(CertificationProgramOwnersCols.tools);
        actionsCol.setCellRenderer(new ActionsCellRenderer(getTranslator()));
		columnsModel.addFlexiColumnModel(actionsCol);
		
		tableModel = new CertificationProgramOwnersTableModel(columnsModel, getLocale()); 
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setSelectAllEnable(false);
		tableEl.setMultiSelect(true);
		tableEl.setSearchEnabled(true);
		tableEl.setAndLoadPersistedPreferences(ureq, "certification-programs-element-owners");
		
		tableEl.addBatchButton(removeOwnersButton);
	}
	
	private void loadModel() {
		List<Identity> owners = certificationProgramService.getCertificationProgramOwners(certificationProgram);
		List<CertificationProgramOwnerRow> rows = owners.stream()
				.map(owner -> new CertificationProgramOwnerRow(owner, userPropertyHandlers, getLocale()))
				.toList();
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(addOwnerCtrl == source) {
			if(event instanceof SingleIdentityChosenEvent sice) {
				doAddOwner(List.of(sice.getChosenIdentity()));
				loadModel();
			} else if(event instanceof MultiIdentityChosenEvent mice) {
				doAddOwner(mice.getChosenIdentities());
				loadModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if(confirmRemoveCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				loadModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if(toolsCtrl == source) {
			if(event == Event.CLOSE_EVENT) {
				calloutCtrl.deactivate();
				cleanUp();
			}
		} else if(cmc == source || calloutCtrl == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(confirmRemoveCtrl);
		removeAsListenerAndDispose(addOwnerCtrl);
		removeAsListenerAndDispose(calloutCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(cmc);
		confirmRemoveCtrl = null;
		addOwnerCtrl = null;
		calloutCtrl = null;
		toolsCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(addOwnerButton == source) {
			doChooseOwner(ureq);
		} else if(removeOwnersButton == source) {
			doConfirmRemoveOwners(ureq);
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent se) {
				if(ActionsCellRenderer.CMD_ACTIONS.equals(se.getCommand())) {
					String targetId = ActionsCellRenderer.getId(se.getIndex());
					CertificationProgramOwnerRow selectedRow = tableModel.getObject(se.getIndex());
					doOpenTools(ureq, selectedRow, targetId);
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doChooseOwner(UserRequest ureq) {
		addOwnerCtrl = new UserSearchController(ureq, getWindowControl());
		listenTo(addOwnerCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), addOwnerCtrl.getInitialComponent(),
				true, translate("add.owner.title"), true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doAddOwner(List<Identity> identities) {
		for(Identity identity:identities) {
			certificationProgramService.addCertificationProgramOwner(certificationProgram, identity);
			getLogger().info("Add owner {} to certification program {}", identity.getKey(), certificationProgram.getKey());
		}
	}

	private void doConfirmRemoveOwners(UserRequest ureq) {
		Set<Integer> selectedIndexes = tableEl.getMultiSelectedIndex();
		if(selectedIndexes.isEmpty()) {
			showWarning("warning.atleastone");
		} else if(selectedIndexes.size() == tableModel.getRowCount()) {
			showWarning("warning.atleastone.owner");
		} else {
			List<Long> identititesKeys = selectedIndexes.stream()
					.map(index -> tableModel.getObject(index.intValue()))
					.map(CertificationProgramOwnerRow::getIdentityKey)
					.toList();
			
			List<Identity> identities = securityManager.loadIdentityByKeys(identititesKeys);
			doConfirmRemoveOwners(ureq, identities);
		}
	}
	
	private void doConfirmRemoveOwner(UserRequest ureq, CertificationProgramOwnerRow row) {
		if(tableModel.getRowCount() == 1) {
			showWarning("warning.atleastone.owner");
		} else {
			Identity identity = securityManager.loadIdentityByKey(row.getIdentityKey());
			doConfirmRemoveOwners(ureq, List.of(identity));
		}
	}
	
	private void doConfirmRemoveOwners(UserRequest ureq, List<Identity> identities) {
		String titleI18n = identities.size() == 1
				? "remove.owner.title"
				: "remove.owners.title";
		String messageI18n = identities.size() == 1
				? "remove.owner.text"
				: "remove.owners.text";
		confirmRemoveCtrl = new ConfirmationRemoveOwnersController(ureq, getWindowControl(),
				translate(messageI18n, Integer.toString(identities.size())), null, translate("remove"),
				certificationProgram, identities);
		listenTo(confirmRemoveCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), confirmRemoveCtrl.getInitialComponent(),
				true, translate(titleI18n), true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doOpenTools(UserRequest ureq, CertificationProgramOwnerRow row, String targetId) {
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(calloutCtrl);

		toolsCtrl = new ToolsController(ureq, getWindowControl(), row);
		listenTo(toolsCtrl);
	
		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), targetId, "", true, "");
		listenTo(calloutCtrl);
		calloutCtrl.activate();
	}
	
	private class ToolsController extends BasicController {

		private Link removeOwnerLink;
		
		private final CertificationProgramOwnerRow row;
		
		public ToolsController(UserRequest ureq, WindowControl wControl, CertificationProgramOwnerRow row) {
			super(ureq, wControl);
			this.row = row;

			VelocityContainer mainVC = createVelocityContainer("tool_owners");
			
			removeOwnerLink = LinkFactory.createLink("remove.owner", "remove.owner", getTranslator(), mainVC, this, Link.LINK);
			removeOwnerLink.setIconLeftCSS("o_icon o_icon-fw o_icon_remove_member");

			putInitialPanel(mainVC);
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			fireEvent(ureq, Event.CLOSE_EVENT);
			if(removeOwnerLink == source) {
				doConfirmRemoveOwner(ureq, row);
			}
		}
	}
}
