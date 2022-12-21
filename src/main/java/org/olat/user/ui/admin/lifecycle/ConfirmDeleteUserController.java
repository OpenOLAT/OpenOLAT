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

import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.user.UserLifecycleManager;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 18 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConfirmDeleteUserController extends FormBasicController {
	
	private static final String[] confirmKeys = new String[] { "confirm" };
	
	private FormLink deleteButton;
	private MultipleSelectionElement confirmEl;
	
	private List<Identity> toDelete;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private UserManager userManager;
	@Autowired
	private UserLifecycleManager userLifecycleManager;
	
	public ConfirmDeleteUserController(UserRequest ureq, WindowControl wControl, List<Identity> toDelete) {
		super(ureq, wControl, "confirm_delete");
		this.toDelete = toDelete;
		initForm(ureq);
	}
	
	public List<Identity> getToDelete() {
		return toDelete;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layout = (FormLayoutContainer)formLayout;
			String names = buildUserNameList(toDelete);
			String message;
			if(toDelete.size() == 1) {
				message = translate("readyToDelete.delete.confirm.single", names);
			} else {
				message = translate("readyToDelete.delete.confirm", names);
			}
			layout.contextPut("msg", message);
			
			FormLayoutContainer layoutCont = FormLayoutContainer.createDefaultFormLayout("confirm", getTranslator());
			formLayout.add("confirm", layoutCont);
			layoutCont.setRootForm(mainForm);
			
			String[] confirmValues = new String[] { translate("readyToDelete.delete.confirm.check") };
			confirmEl = uifactory.addCheckboxesHorizontal("readyToDelete.delete.confirm.check.label", layoutCont, confirmKeys, confirmValues);
			confirmEl.setElementCssClass("o_sel_confirm_delete_user");
			
			FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
			layoutCont.add(buttonsCont);
			uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
			deleteButton = uifactory.addFormLink("delete", buttonsCont, Link.BUTTON);
			deleteButton.setElementCssClass("o_sel_delete_user");
		}
	}
	
	/**
	 * Build comma separated list of usernames.
	 * @param toDelete
	 * @return
	 */
	private String buildUserNameList(List<Identity> toDeleteIdentities) {
		StringBuilder sb = new StringBuilder();
		for (Identity identity : toDeleteIdentities) {
			if (sb.length() > 0) {
				sb.append(", ");
			}
			sb.append(userManager.getUserDisplayName(identity));
		}
		return sb.toString();
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		confirmEl.clearError();
		if(!confirmEl.isAtLeastSelected(1)) {
			confirmEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		return allOk;
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(deleteButton == source && validateFormLogic(ureq)) {
			doDelete();
			fireEvent(ureq, Event.DONE_EVENT);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doDelete() {
		boolean success = doDeleteIdentities(getToDelete());	
		if (success) {
			showInfo("deleted.users.msg");					
		}
	}
	
	private boolean doDeleteIdentities(List<Identity> toDeleteIdentities) {
		boolean totalSuccess = true;
		for (int i = 0; i < toDeleteIdentities.size(); i++) {
			Identity identity = toDeleteIdentities.get(i);
			boolean success = userLifecycleManager.deleteIdentity(identity, getIdentity());
			if (!success) {							
				totalSuccess = false;
				showError("error.delete", userManager.getUserDisplayName(identity));
			}
			dbInstance.commit();	
		}
		return totalSuccess;
	}
}
