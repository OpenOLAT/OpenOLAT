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
package org.olat.course.member.wizard;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.olat.basesecurity.BaseSecurityManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.mail.MailHelper;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 5 juil. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class InvitationMailValidationController extends StepFormBasicController {
	
	private TextElement emailEl;
	private SingleSelection existingInviteeEl;
	
	private final InvitationContext context;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurityManager securityManager;
	
	public InvitationMailValidationController(UserRequest ureq, WindowControl wControl, Form rootForm,
			StepsRunContext runContext, InvitationContext context) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_DEFAULT, null);
		this.context = context;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		emailEl = uifactory.addTextElement("invitation.mail", 255, "", formLayout);
		emailEl.setFocus(true);
		emailEl.setMandatory(true);
		
		existingInviteeEl = uifactory.addDropdownSingleselect("invitee", "existing.invitee.selection", formLayout, new String[0], new String[0]);
		existingInviteeEl.enableNoneSelection();
		existingInviteeEl.setVisible(false);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		emailEl.clearError();
		String mail = emailEl.getValue();
		if (StringHelper.containsNonWhitespace(mail)) {
			if (!MailHelper.isValidEmailAddress(mail)) {
				emailEl.setErrorKey("error.mail.invalid", null);
				allOk &= false;
			}
		} else {
			emailEl.setErrorKey("form.legende.mandatory", null);
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
	protected void formNext(UserRequest ureq) {
		if(validateFormLogic(ureq) && !Objects.equals(context.getEmail(), emailEl.getValue())) {
			String mail = emailEl.getValue();
			if(existingInviteeEl.isVisible() && existingInviteeEl.isOneSelected()) {
				String inviteeKey = existingInviteeEl.getSelectedKey();
				Identity invitee = securityManager.loadIdentityByKey(Long.valueOf(inviteeKey));
				boolean inviteeOnly = securityManager.getRoles(invitee).isInviteeOnly();
				context.setEmail(mail);
				context.setIdentity(invitee, inviteeOnly);
				fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
			} else {
				List<Identity> shareWithIdentities = userManager.findIdentitiesByEmail(Collections.singletonList(mail));
				if(shareWithIdentities.isEmpty()) {
					context.setEmail(mail);
					context.setIdentity(null, true);
					existingInviteeEl.setVisible(false);
					fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
				} else if(shareWithIdentities.size() == 1) {
					Identity invitee = shareWithIdentities.get(0);
					boolean inviteeOnly = securityManager.getRoles(invitee).isInviteeOnly();
					context.setEmail(mail);
					context.setIdentity(invitee, inviteeOnly);
					existingInviteeEl.setVisible(false);
					fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
				} else {
					SelectionValues inviteeKeyValues = new SelectionValues();
					for(Identity id:shareWithIdentities) {
						String fullname = userManager.getUserDisplayName(id);
						inviteeKeyValues.add(SelectionValues.entry(id.getKey().toString(), fullname));
					}
					existingInviteeEl.setKeysAndValues(inviteeKeyValues.keys(), inviteeKeyValues.values(), null);
					existingInviteeEl.setErrorKey("error.existing.invitee.selection", null);
					existingInviteeEl.setVisible(true);
				}
			}
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
