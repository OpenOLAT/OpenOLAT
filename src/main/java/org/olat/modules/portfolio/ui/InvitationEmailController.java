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
package org.olat.modules.portfolio.ui;

import java.util.Collections;
import java.util.List;

import org.olat.basesecurity.BaseSecurity;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.mail.MailHelper;
import org.olat.modules.portfolio.Binder;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * 
 * Initial date: 29.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class InvitationEmailController extends FormBasicController {
	
	private TextElement mailEl;
	private SingleSelection existingInviteeEl;
	
	private Binder binder;
	private Identity invitee;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	
	public InvitationEmailController(UserRequest ureq, WindowControl wControl, Binder binder) {
		super(ureq, wControl);
		this.binder = binder;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_pf_invitation_form");

		mailEl = uifactory.addTextElement("mail", "mail", 128, "", formLayout);
		mailEl.setElementCssClass("o_sel_pf_invitation_mail");
		mailEl.setMandatory(true);
		mailEl.setNotEmptyCheck("map.share.empty.warn");
		
		existingInviteeEl = uifactory.addDropdownSingleselect("invitee", "existing.invitee.selection", formLayout, new String[0], new String[0]);
		existingInviteeEl.enableNoneSelection();
		existingInviteeEl.setVisible(false);

		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		buttonsCont.setRootForm(mainForm);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
		uifactory.addFormSubmitButton("validate.email", buttonsCont);
	}
	
	@Override
	protected void doDispose() {
		// 
	}
	
	public Binder getBinder() {
		return binder;
	}
	
	public String getEmail() {
		return mailEl.getValue();
	}
	
	public Identity getInvitee() {
		return invitee;
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
	
		mailEl.clearError();
		String mail = mailEl.getValue();
		if (StringHelper.containsNonWhitespace(mail)) {
			if (!MailHelper.isValidEmailAddress(mail)) {
				mailEl.setErrorKey("error.mail.invalid", null);
				allOk &= false;
			}
		} else {
			mailEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		existingInviteeEl.clearError();
		if(existingInviteeEl.isVisible() && !existingInviteeEl.isOneSelected()) {
			existingInviteeEl.setErrorKey("error.existing.invitee.selection", null);
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(existingInviteeEl.isVisible() && existingInviteeEl.isOneSelected()) {
			String inviteeKey = existingInviteeEl.getSelectedKey();
			invitee = securityManager.loadIdentityByKey(Long.valueOf(inviteeKey));
			fireEvent(ureq, Event.DONE_EVENT);
		} else {
			String mail = mailEl.getValue();
			List<Identity> shareWithIdentities = userManager.findIdentitiesByEmail(Collections.singletonList(mail));
			if(shareWithIdentities.isEmpty()) {
				fireEvent(ureq, Event.DONE_EVENT);
			} else if(shareWithIdentities.size() == 1) {
				invitee = shareWithIdentities.get(0);
				fireEvent(ureq, Event.DONE_EVENT);
			} else {
				String[] inviteeKeys = new String[shareWithIdentities.size()];
				String[] inviteeValues = new String[shareWithIdentities.size()];
				for(int i=shareWithIdentities.size(); i-->0; ) {
					inviteeKeys[i] = shareWithIdentities.get(i).getKey().toString();
					inviteeValues[i] = userManager.getUserDisplayName(shareWithIdentities.get(i));
				}
				existingInviteeEl.setKeysAndValues(inviteeKeys, inviteeValues, null);
				existingInviteeEl.setErrorKey("error.existing.invitee.selection", null);
				existingInviteeEl.setVisible(true);
			}
		}
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}