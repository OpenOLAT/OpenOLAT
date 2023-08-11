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

import org.olat.basesecurity.Authentication;
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
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
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
	
	private FlexiTableElement tableEl;
	private PasskeyListTableModel tableModel;
	
	private CloseableModalController cmc;
	private ConfirmDeletePasskeyController confirmDeleteCtrl;
	
	@Autowired
	private OLATWebAuthnManager webAuthnManager;
	
	public PasskeyListController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("passkey.list.title");
		setFormDescription("passkey.list.description");
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PasskeyCols.username));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PasskeyCols.aaguid));
		DefaultFlexiColumnModel deletCol = new DefaultFlexiColumnModel("delete", translate("delete"), "delete");
		deletCol.setAlwaysVisible(true);
		deletCol.setExportable(false);
		columnsModel.addFlexiColumnModel(deletCol);
		
		tableModel = new PasskeyListTableModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 25, false, getTranslator(), formLayout);
		tableEl.setLabel("passkeys", null);
		tableEl.setNumOfRowsEnabled(false);
		tableEl.setCustomizeColumns(false);
		tableEl.setEmptyTableSettings("table.empty.passkeys", null, "o_icon_password");
	}
	
	private void loadModel() {
		List<Authentication> authentications = webAuthnManager.getPasskeyAuthentications(getIdentity());
		List<PasskeyRow> rows = authentications.stream()
				.map(PasskeyRow::new)
				.collect(Collectors.toList());
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(confirmDeleteCtrl == source) {
			if(event == Event.DONE_EVENT) {
				loadModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(confirmDeleteCtrl);
		removeAsListenerAndDispose(cmc);
		confirmDeleteCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if (event instanceof SelectionEvent se) {
				if ("delete".equals(se.getCommand())) {
					PasskeyRow passkey = tableModel.getObject(se.getIndex());
					doConfirmDelete(ureq, passkey);
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
		confirmDeleteCtrl = new ConfirmDeletePasskeyController(ureq, getWindowControl(), row);
		listenTo(confirmDeleteCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), confirmDeleteCtrl.getInitialComponent(),
				true, translate("delete.passkey"));
		cmc.activate();
		listenTo(cmc);
	}
}
