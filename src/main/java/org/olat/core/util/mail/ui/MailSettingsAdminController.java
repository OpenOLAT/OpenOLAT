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

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.core.util.mail.MailModule;
import org.olat.core.util.mail.MailUIFactory;

/**
 * 
 * Description:<br>
 * Small administration to set on/off the intern mail system
 * 
 * <P>
 * Initial Date:  14 avr. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class MailSettingsAdminController extends FormBasicController  {

	private MultipleSelectionElement enabled;
	private SingleSelection userDefaultSettingEl;
	
	private String[] values = {""};
	private String[] keys = {"on"};
	

	private String[] userSettingValues ;
	private String[] userSettingKeys = {"intern.only","send.copy"};

	public MailSettingsAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, null, Util.createPackageTranslator(MailModule.class, ureq.getLocale()));
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("mail.admin.title");
		setFormDescription("mail.admin.description");
		setFormContextHelp("E-Mail Settings");

		boolean internEnabled = isEnabled();
		enabled = uifactory.addCheckboxesHorizontal("mail.admin.intern.enabled", formLayout, keys, values);
		enabled.select(keys[0], internEnabled);
		enabled.addActionListener(FormEvent.ONCHANGE);
		
		boolean realMailSetting = isUserDefaultSetting();
		userSettingValues = new String[] {
			translate("mail.admin.intern.only"),
			translate("mail.admin.intern.real.mail")
		};
		userDefaultSettingEl = uifactory.addRadiosVertical("mail-system", "mail.admin.default.settings", formLayout, userSettingKeys, userSettingValues);
		if(realMailSetting) {
			userDefaultSettingEl.select(userSettingKeys[1], true);
		} else {
			userDefaultSettingEl.select(userSettingKeys[0], true);
		}
		userDefaultSettingEl.setEnabled(internEnabled);

		final FormLayoutContainer buttonGroupLayout = FormLayoutContainer.createButtonLayout("buttonLayout", getTranslator());
		buttonGroupLayout.setRootForm(mainForm);
		formLayout.add(buttonGroupLayout);
		
		uifactory.addFormSubmitButton("save", buttonGroupLayout);
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		boolean on = !enabled.getSelectedKeys().isEmpty();
		setEnabled(on);
		
		if(userDefaultSettingEl.isOneSelected()) {
			boolean realMailSetting = userDefaultSettingEl.getSelected() == 1;
			setUserDefaultSetting(realMailSetting);
		}
		userDefaultSettingEl.setEnabled(on);
		
		getWindowControl().setInfo("saved");
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == enabled) {
			boolean on = !enabled.getSelectedKeys().isEmpty();
			userDefaultSettingEl.setEnabled(on);
		}
		super.formInnerEvent(ureq, source, event);
	}

	private boolean isEnabled() {
		MailModule config = (MailModule) CoreSpringFactory.getBean("mailModule");
		return config.isInternSystem();
	}
	
	private void setEnabled(boolean enabled) {
		MailModule config = (MailModule) CoreSpringFactory.getBean("mailModule");
		config.setInterSystem(enabled);
	}
	
	
	private boolean isUserDefaultSetting() {
		MailModule config = (MailModule) CoreSpringFactory.getBean("mailModule");
		return config.isReceiveRealMailUserDefaultSetting();
	}
	
	private void setUserDefaultSetting(boolean enabled) {
		MailModule config = (MailModule) CoreSpringFactory.getBean("mailModule");
		config.setReceiveRealMailUserDefaultSetting(enabled);
	}
}