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
package org.olat.user.propertyhandlers.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.Windows;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemCollection;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;
import org.olat.core.gui.components.form.flexible.impl.elements.FormLinkImpl;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.ChiefController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.User;
import org.olat.core.util.Util;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: 7 févr. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SmsPhoneElement extends FormItemImpl implements FormItemCollection, ControllerEventListener {
	
	private final SmsPhoneComponent component;
	
	private String phone;
	private boolean forceFormDirty;
	private boolean hasChanged = false;
	private final User editedUser;
	private final UserPropertyHandler handler;
	
	private FormLink editLink;
	private FormLink removeLink;
	private SmsPhoneController smsPhoneCtrl;
	private CloseableModalController cmc;
	
	public SmsPhoneElement(String name, UserPropertyHandler handler, User editedUser, Locale locale) {
		super(name);
		setTranslator(Util.createPackageTranslator(SmsPhoneElement.class, locale));
		this.handler = handler;
		this.editedUser = editedUser;
		setPhone(editedUser.getProperty(handler.getName(), locale));
		component = new SmsPhoneComponent(this);
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}
	
	public boolean hasChanged() {
		return hasChanged;
	}

	public FormLink getEditLink() {
		return editLink;
	}
	
	public FormLink getRemoveLink() {
		return removeLink;
	}
	
	public boolean getAndResetFormDirty() {
		boolean ffd = forceFormDirty;
		forceFormDirty = false;
		return ffd;
	}

	@Override
	public void dispatchEvent(UserRequest ureq, Controller source, Event event) {
		if(cmc == source) {
			smsPhoneCtrl = null;
			cmc = null;
		} else if(smsPhoneCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				setPhone(smsPhoneCtrl.getPhone());
				hasChanged = true;
				forceFormDirty = true;
				component.setDirty(true);
			}
			cmc.deactivate();
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				String msg = getTranslator().translate("sms.phone.number.changed");
				smsPhoneCtrl.getWindowControl().setInfo(msg);
			}
			smsPhoneCtrl = null;
			cmc = null;
		}
	}

	@Override
	public Iterable<FormItem> getFormItems() {
		List<FormItem> items = new ArrayList<>(1);
		if(editLink != null) {
			items.add(editLink);
		}
		if(removeLink != null) {
			items.add(removeLink);
		}
		return items;
	}

	@Override
	public FormItem getFormComponent(String name) {
		return null;
	}
	
	@Override
	public void setRootForm(Form rootForm) {
		String dispatchId = component.getDispatchID();
		editLink = new FormLinkImpl(dispatchId + "_editSmsButton", "editSms", "edit", Link.BUTTON);
		editLink.setDomReplacementWrapperRequired(false);
		editLink.setTranslator(getTranslator());
		editLink.setIconLeftCSS("o_icon o_icon_edit");
		
		removeLink = new FormLinkImpl(dispatchId + "_removeSmsButton", "removeSms", "remove", Link.BUTTON);
		removeLink.setDomReplacementWrapperRequired(false);
		removeLink.setTranslator(getTranslator());
		removeLink.setIconLeftCSS("o_icon o_icon_delete");
		super.setRootForm(rootForm);
	}

	@Override
	protected Component getFormItemComponent() {
		return component;
	}

	@Override
	protected void rootFormAvailable() {
		if(editLink != null && editLink.getRootForm() != getRootForm()) {
			editLink.setRootForm(getRootForm());
		}
		if(removeLink != null && removeLink.getRootForm() != getRootForm()) {
			removeLink.setRootForm(getRootForm());
		}
	}

	@Override
	public void evalFormRequest(UserRequest ureq) {
		Form form = getRootForm();
		String dispatchuri = form.getRequestParameter("dispatchuri");
		if(editLink != null && editLink.getFormDispatchId().equals(dispatchuri)) {
			doEdit(ureq);
		} else if(removeLink != null && removeLink.getFormDispatchId().equals(dispatchuri)) {
			doRemove();
		}
	}

	@Override
	public void reset() {
		//
	}
	
	private void doEdit(UserRequest ureq) {
		ChiefController chief = Windows.getWindows(ureq).getChiefController(ureq);
		WindowControl wControl = chief.getWindowControl();
		if (wControl != null) {
			smsPhoneCtrl = new SmsPhoneController(ureq, wControl, handler, editedUser);
			smsPhoneCtrl.addControllerListener(this);
			
			String propLabel = CoreSpringFactory.getImpl(UserManager.class)
					.getPropertyHandlerTranslator(getTranslator()).translate(handler.i18nFormElementLabelKey());
			String title = getTranslator().translate("sms.title", new String[] { propLabel });
			cmc = new CloseableModalController(wControl, "close", smsPhoneCtrl.getInitialComponent(), true, title);
			cmc.suppressDirtyFormWarningOnClose();
			cmc.activate();
		}
	}

	private void doRemove() {
		setPhone(null);
		hasChanged = true;
		component.setDirty(true);
	}
}