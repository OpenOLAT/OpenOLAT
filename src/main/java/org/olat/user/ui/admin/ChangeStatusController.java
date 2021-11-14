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
package org.olat.user.ui.admin;

import java.util.List;

import org.olat.admin.user.UsermanagerUserSearchController;
import org.olat.admin.user.bulkChange.UserBulkChangeManager;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 3 juin 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ChangeStatusController extends FormBasicController {

	private SingleSelection statusEl;
	private MultipleSelectionElement sendLoginDeniedEmailEl;
	
	private final List<Identity> editedIdentities;
	
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private UserBulkChangeManager userBulkChangeManager;
	
	public ChangeStatusController(UserRequest ureq, WindowControl wControl, List<Identity> editedIdentities) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(UsermanagerUserSearchController.class, getLocale(), getTranslator()));
		this.editedIdentities = editedIdentities;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
		SelectionValues statusKeys = new SelectionValues();
		statusKeys.add(SelectionValues.entry(Integer.toString(Identity.STATUS_ACTIV), translate("rightsForm.status.activ")));
		statusKeys.add(SelectionValues.entry(Integer.toString(Identity.STATUS_PERMANENT), translate("rightsForm.status.permanent")));
		statusKeys.add(SelectionValues.entry(Integer.toString(Identity.STATUS_PENDING), translate("rightsForm.status.pending")));
		statusKeys.add(SelectionValues.entry(Integer.toString(Identity.STATUS_INACTIVE), translate("rightsForm.status.inactive")));
		statusKeys.add(SelectionValues.entry(Integer.toString(Identity.STATUS_LOGIN_DENIED), translate("rightsForm.status.login_denied")));
		statusEl = uifactory.addRadiosVertical("status", "rightsForm.status", formLayout, statusKeys.keys(), statusKeys.values());
		statusEl.addActionListener(FormEvent.ONCHANGE);
		
		sendLoginDeniedEmailEl = uifactory.addCheckboxesHorizontal("rightsForm.sendLoginDeniedEmail", formLayout, new String[]{"y"}, new String[]{translate("rightsForm.sendLoginDeniedEmail")});
		sendLoginDeniedEmailEl.setLabel(null, null);
		sendLoginDeniedEmailEl.setVisible(false);
		
		FormLayoutContainer buttonsLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsLayout);
		
		uifactory.addFormCancelButton("cancel", buttonsLayout, ureq, getWindowControl());
		uifactory.addFormSubmitButton("modify.status", buttonsLayout);
	}
	
	public Integer getStatus() {
		if(statusEl.isOneSelected()) {
			return Integer.valueOf(statusEl.getSelectedKey());
		}
		return null;
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		statusEl.clearError();
		if(!statusEl.isOneSelected()) {
			statusEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(statusEl == source) {
			sendLoginDeniedEmailEl.setVisible(statusEl.isOneSelected()
					&& Identity.STATUS_LOGIN_DENIED.toString().equals(statusEl.getSelectedKey()));
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		for(Identity editedIdentity:editedIdentities) {
			Integer oldStatus = editedIdentity.getStatus();
			Integer newStatus = getStatus();
			if(!oldStatus.equals(newStatus) && Identity.STATUS_LOGIN_DENIED.equals(newStatus)) {
				userBulkChangeManager.sendLoginDeniedEmail(editedIdentity);
			}
			securityManager.saveIdentityStatus(editedIdentity, newStatus, getIdentity());
		}
		fireEvent(ureq, Event.CHANGED_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
