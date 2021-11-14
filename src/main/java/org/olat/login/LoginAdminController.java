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
package org.olat.login;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.search.SearchModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 14.01.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LoginAdminController extends FormBasicController {
	
	private MultipleSelectionElement guestLoginEl, guestLinkEl, invitationLoginEl, fullTextSearchEl;
	
	private static final String[] keys = new String[]{ "on" };
	private final String[] values;
	
	@Autowired
	private LoginModule loginModule;
	@Autowired
	private SearchModule searchModule;
	
	public LoginAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		values = new String[]{ translate("enabled") };
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("login.admin.title");
		
		guestLoginEl = uifactory.addCheckboxesHorizontal("guest.login", "guest.login", formLayout, keys, values);
		guestLoginEl.select(keys[0], loginModule.isGuestLoginEnabled());
		guestLoginEl.addActionListener(FormEvent.ONCHANGE);
		
		guestLinkEl = uifactory.addCheckboxesHorizontal("guest.login.links", "guest.login.links", formLayout, keys, values);
		guestLinkEl.select(keys[0], loginModule.isGuestLoginLinksEnabled());
		guestLinkEl.addActionListener(FormEvent.ONCHANGE);
		
		invitationLoginEl = uifactory.addCheckboxesHorizontal("invitation.login", "invitation.login", formLayout, keys, values);
		invitationLoginEl.select(keys[0], loginModule.isInvitationEnabled());
		invitationLoginEl.addActionListener(FormEvent.ONCHANGE);
		
		fullTextSearchEl = uifactory.addCheckboxesHorizontal("guest.search", "guest.search", formLayout, keys, values);
		fullTextSearchEl.select(keys[0], searchModule.isGuestEnabled());
		fullTextSearchEl.addActionListener(FormEvent.ONCHANGE);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(guestLoginEl == source) {
			boolean enabled = guestLoginEl.isAtLeastSelected(1);
			loginModule.setGuestLoginEnabled(enabled);
		} else if(guestLinkEl == source) {
			boolean enabled = guestLinkEl.isAtLeastSelected(1);
			loginModule.setGuestLoginLinksEnabled(enabled);
		} else if(invitationLoginEl == source) {
			boolean enabled = invitationLoginEl.isAtLeastSelected(1);
			loginModule.setInvitationEnabled(enabled);
		} else if(fullTextSearchEl == source) {
			boolean enabled = fullTextSearchEl.isAtLeastSelected(1);
			searchModule.setGuestEnabled(enabled);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}