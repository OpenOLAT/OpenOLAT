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

import java.util.ArrayList;
import java.util.List;

import org.olat.basesecurity.Authentication;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateTimeFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableEmptyNextPrimaryActionEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.login.webauthn.ui.PasskeyListTableModel.PasskeyCols;

/**
 * The authentication are transient.
 * 
 * Initial date: 20 oct. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RegistrationPasskeyListController extends FormBasicController {

	private final Identity identityToChange;
	
	private FlexiTableElement tableEl;
	private PasskeyListTableModel tableModel;

	public RegistrationPasskeyListController(UserRequest ureq, WindowControl wControl, Identity identityToChange) {
		super(ureq, wControl, "passkey_list_registration");
		this.identityToChange = identityToChange;
		initForm(ureq);
		
		flc.setFormTitle(translate("passkey.new.title"));
		flc.setFormDescription(translate("passkey.new.desc"));
	}
	
	public RegistrationPasskeyListController(UserRequest ureq, WindowControl wControl, Form rootForm) {
		super(ureq, wControl, LAYOUT_CUSTOM, "passkey_list_registration", rootForm);
		this.identityToChange = null;
		initForm(ureq);
	}
	
	public boolean hasPasskeys() {
		return tableModel != null && tableModel.getRowCount() >= 1;
	}
	
	public List<Authentication> getPasskeys() {
		return tableModel.getObjects().stream()
				.map(PasskeyRow::getAuthentication).toList();
	}

	public Identity getIdentityToChange() {
		return identityToChange;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PasskeyCols.username));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PasskeyCols.creationDate,
				new DateTimeFlexiCellRenderer(getLocale())));

		tableModel = new PasskeyListTableModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 25, false, getTranslator(), formLayout);
		tableEl.setLabel("passkeys", null);
		tableEl.setNumOfRowsEnabled(false);
		tableEl.setCustomizeColumns(false);
		tableEl.setEmptyTableSettings("table.empty.registration.passkeys", "table.empty.registration.passkeys.hint", "o_icon_password", "new.passkey", null, false);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof FlexiTableEmptyNextPrimaryActionEvent) {
				fireEvent(ureq, event);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	public void loadAuthentication(UserRequest ureq, Authentication authentication) {
		List<PasskeyRow> rows = new ArrayList<>(tableModel.getObjects());
		rows.add(new PasskeyRow(authentication));
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
}
