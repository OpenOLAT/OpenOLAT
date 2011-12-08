/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * s
 * <p>
 */
package org.olat.user;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowBackOffice;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.Preferences;
import org.olat.core.id.UserConstants;
import org.olat.core.util.ArrayHelper;
import org.olat.core.util.StringHelper;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.i18n.I18nModule;
import org.olat.core.util.mail.MailModule;
import org.olat.core.util.notifications.NotificationsManager;

/**
 * This form controller provides an interface to change the user's system
 * preferences, like language and font size.
 * <p>
 * Events fired by this event:
 * <ul>
 * <li>Event.DONE_EVENT when something has been changed</li>
 * <li>Event.DONE_CANELLED when user cancelled action</li>
 * </ul>
 * <P>
 * Initial Date: Dec 11, 2009 <br>
 * 
 * @author gwassmann
 */
public class PreferencesFormController extends FormBasicController {
	private static final String[] cssFontsizeKeys = new String[] { "80", "90", "100", "110", "120", "140" };
	private Identity tobeChangedIdentity;
	private SingleSelection language, fontsize, charset, notificationInterval, mailSystem;
	private static final String[] mailIntern = new String[]{"intern.only","send.copy"};

	/**
	 * Constructor for the user preferences form
	 * 
	 * @param ureq
	 * @param wControl
	 * @param tobeChangedIdentity the Identity which preferences are displayed and
	 *          edited. Not necessarily the same as ureq.getIdentity()
	 */
	public PreferencesFormController(UserRequest ureq, WindowControl wControl, Identity tobeChangedIdentity) {
		super(ureq, wControl);
		this.tobeChangedIdentity = tobeChangedIdentity;
		initForm(ureq);
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#formOK(org.olat.core.gui.UserRequest)
	 */
	protected void formOK(UserRequest ureq) {
		UserManager um = UserManager.getInstance();
		BaseSecurity secMgr = BaseSecurityManager.getInstance();
		// Refresh user from DB to prevent stale object issues
		tobeChangedIdentity = secMgr.loadIdentityByKey(tobeChangedIdentity.getKey());
		Preferences prefs = tobeChangedIdentity.getUser().getPreferences();
		prefs.setLanguage(language.getSelectedKey());
		prefs.setFontsize(fontsize.getSelectedKey());
		if (notificationInterval != null) {
			// only read notification interval if available, could be disabled by configuration
			prefs.setNotificationInterval(notificationInterval.getSelectedKey());			
		}

		// Maybe the user changed the font size
		if (ureq.getIdentity().equalsByPersistableKey(tobeChangedIdentity)) {
			int fontSize = Integer.parseInt(fontsize.getSelectedKey());
			WindowBackOffice wbo = getWindowControl().getWindowBackOffice();
			wbo.getWindowManager().setFontSize(fontSize);
			// set window dirty to force full page refresh
			wbo.getWindow().setDirty(true);
		}
		//fxdiff VCRP-16: intern mail system
		if(mailSystem != null && mailSystem.isOneSelected()) {
			String val = mailSystem.isSelected(1) ? "true" : "false";
			prefs.setReceiveRealMail(val);
		}

		if (um.updateUserFromIdentity(tobeChangedIdentity)) {
			// Language change needs logout / login
			showInfo("preferences.successful");
		} else {
			showInfo("preferences.unsuccessful");
		}

		um.setUserCharset(tobeChangedIdentity, charset.getSelectedKey());
		fireEvent(ureq, Event.DONE_EVENT);
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#formCancelled(org.olat.core.gui.UserRequest)
	 */
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#initForm(org.olat.core.gui.components.form.flexible.FormItemContainer,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.UserRequest)
	 */
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("title.prefs");
		setFormContextHelp(this.getClass().getPackage().getName(), "home-prefs.html", "help.hover.prefs");
		
		// load preferences
		Preferences prefs = tobeChangedIdentity.getUser().getPreferences();

		// Username
		StaticTextElement username = uifactory.addStaticTextElement("form.username", tobeChangedIdentity.getName(), formLayout);
		username.setEnabled(false);

		// Language
		Map<String, String> languages = I18nManager.getInstance().getEnabledLanguagesTranslated();
		String[] langKeys = StringHelper.getMapKeysAsStringArray(languages);
		String[] langValues = StringHelper.getMapValuesAsStringArray(languages);
		ArrayHelper.sort(langKeys, langValues, false, true, false);
		language = uifactory.addDropdownSingleselect("form.language", formLayout, langKeys, langValues, null);
		String langKey = prefs.getLanguage();
		// Preselect the users language if available. Maye not anymore enabled on
		// this server
		if (prefs.getLanguage() != null && I18nModule.getEnabledLanguageKeys().contains(langKey)) {
			language.select(prefs.getLanguage(), true);
		} else {
			language.select(I18nModule.getDefaultLocale().toString(), true);
		}

		// Font size
		String[] cssFontsizeValues = new String[] {
				translate("form.fontsize.xsmall"),
				translate("form.fontsize.small"),
				translate("form.fontsize.normal"),
				translate("form.fontsize.large"),
				translate("form.fontsize.xlarge"),
				translate("form.fontsize.presentation")
		};
		fontsize = uifactory.addDropdownSingleselect("form.fontsize", formLayout, cssFontsizeKeys, cssFontsizeValues, null);
		fontsize.select(prefs.getFontsize(), true);
		fontsize.addActionListener(this, FormEvent.ONCHANGE);
		
		// Email notification interval
		NotificationsManager nMgr = NotificationsManager.getInstance();
		List<String> intervals = nMgr.getEnabledNotificationIntervals();
		if (intervals.size() > 0) {
			String[] intervalKeys = new String[intervals.size()];
			intervals.toArray(intervalKeys);
			String[] intervalValues = new String[intervalKeys.length];
			String i18nPrefix = "interval.";
			for (int i = 0; i < intervalKeys.length; i++) {
				intervalValues[i] = translate(i18nPrefix + intervalKeys[i]);
			}
			notificationInterval = uifactory.addDropdownSingleselect("form.notification", formLayout, intervalKeys, intervalValues, null);
			notificationInterval.select(prefs.getNotificationInterval(), true);			
		}
		//fxdiff VCRP-16: intern mail system
		MailModule mailModule = (MailModule)CoreSpringFactory.getBean("mailModule");
		if(mailModule.isInternSystem()) {
			String userEmail = tobeChangedIdentity.getUser().getProperty(UserConstants.EMAIL, getLocale());
			String[] mailInternLabels = new String[] { translate("mail." + mailIntern[0], userEmail), translate("mail." + mailIntern[1], userEmail) };
			mailSystem = uifactory.addRadiosVertical("mail-system", "mail.system", formLayout, mailIntern, mailInternLabels);
			
			String mailPrefs = prefs.getReceiveRealMail();
			if(StringHelper.containsNonWhitespace(mailPrefs)) {
				if("true".equals(mailPrefs)) {
					mailSystem.select(mailIntern[1], true);
				} else {
					mailSystem.select(mailIntern[0], true);
				}
			} else if(mailModule.isReceiveRealMailUserDefaultSetting()) {
				mailSystem.select(mailIntern[1], true);
			} else {
				mailSystem.select(mailIntern[0], true);
			}
		}

		// Text encoding
		Map<String, Charset> charsets = Charset.availableCharsets();
		String currentCharset = UserManager.getInstance().getUserCharset(tobeChangedIdentity);
		String[] csKeys = StringHelper.getMapKeysAsStringArray(charsets);
		charset = uifactory.addDropdownSingleselect("form.charset", formLayout, csKeys, csKeys, null);
		charset.select(currentCharset, true);

		// Submit and cancel buttons
		final FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("button_layout", getTranslator());
		formLayout.add(buttonLayout);
		uifactory.addFormSubmitButton("submit", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
	}

	protected void formInnerEvent (UserRequest ureq, FormItem source, FormEvent event) {
		if (source == fontsize && ureq.getIdentity().equalsByPersistableKey(tobeChangedIdentity)) {
			int fontSize = Integer.parseInt(fontsize.getSelectedKey());
			WindowBackOffice wbo = getWindowControl().getWindowBackOffice();
			wbo.getWindowManager().setFontSize(fontSize);
			wbo.getWindow().setDirty(true);
		}
	}
	
	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose()
	 */
	protected void doDispose() {
	// nothing to do
	}

}
