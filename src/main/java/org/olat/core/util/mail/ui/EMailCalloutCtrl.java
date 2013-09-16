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
package org.olat.core.util.mail.ui;

import java.util.List;

import org.olat.admin.user.UserSearchListProvider;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.events.SingleIdentityChosenEvent;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.Windows;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.ajax.autocompletion.EntriesChosenEvent;
import org.olat.core.gui.control.generic.ajax.autocompletion.FlexiAutoCompleterController;
import org.olat.core.gui.control.generic.ajax.autocompletion.ListProvider;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.core.util.mail.MailHelper;
import org.olat.core.util.mail.MailModule;
import org.olat.user.UserManager;

/**
 * 
 * Initial date: 28.06.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EMailCalloutCtrl extends FormBasicController {

	private FlexiAutoCompleterController autocompleterC;
	private final BaseSecurityModule securityModule;
	private final boolean allowExternalAddress;
	
	public EMailCalloutCtrl(UserRequest ureq, WindowControl wControl, boolean allowExternalAddress) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		setBasePackage(MailModule.class);
		
		this.allowExternalAddress = allowExternalAddress;
		securityModule = CoreSpringFactory.getImpl(BaseSecurityModule.class); 
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {

		Roles roles = ureq.getUserSession().getRoles();
		boolean autoCompleteAllowed = securityModule.isUserAllowedAutoComplete(roles);
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(roles);
		boolean ajax = Windows.getWindows(ureq).getWindowManager().isAjaxEnabled();
		if (ajax && autoCompleteAllowed) {
			ListProvider provider = new UserSearchListProvider();
			autocompleterC = new FlexiAutoCompleterController(ureq, getWindowControl(), provider, null, isAdministrativeUser, allowExternalAddress, 60, 3, null, mainForm);
			autocompleterC.setFormElement(false);
			listenTo(autocompleterC);
			
			FormItem item = autocompleterC.getInitialFormItem();
			formLayout.add(item);
		}
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if(source == autocompleterC) {
			if(event instanceof EntriesChosenEvent) {
				EntriesChosenEvent ce = (EntriesChosenEvent)event;
				List<String> entries = ce.getEntries();
				if(entries != null && entries.size() == 1) {
					processSelection(ureq, entries.get(0));
				}
			}
		}
		super.event(ureq, source, event);
	}
	
	private void processSelection(UserRequest ureq, String mail) {
		Identity identity = null;
		if(StringHelper.isLong(mail)) {
			identity = BaseSecurityManager.getInstance().loadIdentityByKey(Long.parseLong(mail));
		}
		if(MailHelper.isValidEmailAddress(mail)) {
			if(identity == null) {
				identity = UserManager.getInstance().findIdentityByEmail(mail);
			}
			if(identity == null) {
				identity = new EMailIdentity(mail, getLocale());
			}
		}
		
		if(identity != null) {
			fireEvent(ureq, new SingleIdentityChosenEvent(identity));
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void doDispose() {
		//
	}
}