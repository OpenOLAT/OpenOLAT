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
package org.olat.login.webauthn.ui;

import java.util.List;
import java.util.stream.Collectors;

import org.olat.admin.user.SendTokenToUserForm;
import org.olat.basesecurity.Authentication;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateTimeFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableEmptyNextPrimaryActionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Identity;
import org.olat.login.webauthn.OLATWebAuthnManager;
import org.olat.login.webauthn.ui.PasskeyListTableModel.PasskeyCols;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * 
 * Initial date: 11 août 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PasskeyListController extends FormBasicController {
	
	private FormLink newPasskeyButton;
	private FlexiTableElement tableEl;
	private PasskeyListTableModel tableModel;
	
	private CloseableModalController cmc;
	private NewPasskeyController newPasskeyCtrl;
	private SendTokenToUserForm sendTokenToUserCtrl;
	private ConfirmDeletePasskeyController confirmDeleteCtrl;
	
	private final boolean asAdmin;
	private final boolean withInUse;
	private final Identity identityToModify;
	private final boolean withLastOneWarning;
	private final boolean canSendPasswordLink;
	
	@Autowired
	private OLATWebAuthnManager webAuthnManager;
	
	public PasskeyListController(UserRequest ureq, WindowControl wControl,
			Identity identityToModify, boolean withLastOneWarning, boolean withInUse,
			boolean asAdmin, boolean canSendPasswordLink) {
		super(ureq, wControl, "passkey_list");
		this.asAdmin = asAdmin;
		this.withInUse = withInUse;
		this.identityToModify = identityToModify;
		this.withLastOneWarning = withLastOneWarning;
		this.canSendPasswordLink = canSendPasswordLink;
		
		initForm(ureq);
		loadModel();
	}
	
	public boolean hasPasskeys() {
		return tableModel != null && tableModel.getRowCount() >= 1;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle(asAdmin ? "passkey.list.title.admin" : "passkey.list.title");
		setFormInfo("passkey.list.description");
		setFormInfoHelp("manual_user/login_registration/");
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PasskeyCols.username));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, PasskeyCols.aaguid));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PasskeyCols.creationDate,
				new DateTimeFlexiCellRenderer(getLocale())));
		DefaultFlexiColumnModel deletCol = new DefaultFlexiColumnModel("delete", translate("delete"), "delete");
		deletCol.setAlwaysVisible(true);
		deletCol.setExportable(false);
		columnsModel.addFlexiColumnModel(deletCol);
		
		tableModel = new PasskeyListTableModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 25, false, getTranslator(), formLayout);
		tableEl.setLabel("passkeys", null);
		tableEl.setNumOfRowsEnabled(false);
		if(identityToModify.equals(getIdentity())) {
			tableEl.setEmptyTableSettings("table.empty.passkeys", null, "o_icon_password");
			newPasskeyButton = uifactory.addFormLink("new.passkey", formLayout, Link.BUTTON);
			newPasskeyButton.setIconLeftCSS("o_icon o_ac_token_icon");
		} else {
			String actionKey = canSendPasswordLink ? "send.password.link" : null;
			tableEl.setEmptyTableSettings("table.empty.passkeys.admin", null, "o_icon_password", actionKey, null, false);
		}
	}
	
	public void loadModel() {
		List<Authentication> authentications = webAuthnManager.getPasskeyAuthentications(identityToModify);
		List<PasskeyRow> rows = authentications.stream()
				.map(PasskeyRow::new)
				.collect(Collectors.toList());
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
		
		flc.contextPut("inUse", Boolean.valueOf(withInUse && !authentications.isEmpty()));
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(confirmDeleteCtrl == source) {
			if(event == Event.DONE_EVENT) {
				loadModel();
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		} else if(sendTokenToUserCtrl == source || newPasskeyCtrl == source) {
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(confirmDeleteCtrl);
		removeAsListenerAndDispose(newPasskeyCtrl);
		removeAsListenerAndDispose(cmc);
		confirmDeleteCtrl = null;
		newPasskeyCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(newPasskeyButton == source) {
			doGeneratePasskey(ureq);
		} else if(tableEl == source) {
			if (event instanceof SelectionEvent se) {
				if ("delete".equals(se.getCommand())) {
					PasskeyRow passkey = tableModel.getObject(se.getIndex());
					doConfirmDelete(ureq, passkey);
				}
			} else if(event instanceof FlexiTableEmptyNextPrimaryActionEvent) {
				if(identityToModify.equals(getIdentity())) {
					doGeneratePasskey(ureq);
				} else {
					doSendToken(ureq);
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doConfirmDelete(UserRequest ureq, PasskeyRow row) {
		confirmDeleteCtrl = new ConfirmDeletePasskeyController(ureq, getWindowControl(),
				row, identityToModify, withLastOneWarning);
		listenTo(confirmDeleteCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), confirmDeleteCtrl.getInitialComponent(),
				true, translate("delete.passkey"));
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doSendToken(UserRequest ureq) {
		sendTokenToUserCtrl = new SendTokenToUserForm(ureq, getWindowControl(), identityToModify, false, false, true);
		listenTo(sendTokenToUserCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), sendTokenToUserCtrl.getInitialComponent(),
				true, translate("send.password.link"));
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doGeneratePasskey(UserRequest ureq) {
		newPasskeyCtrl = new NewPasskeyController(ureq, getWindowControl(), getIdentity(), false, false, true);
		newPasskeyCtrl.setFormInfo("new.passkey.info");
		listenTo(newPasskeyCtrl);
		
		String title = translate("new.passkey.title");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), newPasskeyCtrl.getInitialComponent(), true, title);
		cmc.activate();
		listenTo(cmc);
	}
}
