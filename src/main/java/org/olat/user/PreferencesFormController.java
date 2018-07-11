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
package org.olat.user;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import org.olat.admin.user.SystemRolesAndRightsController;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.WindowManager;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.Preferences;
import org.olat.core.util.ArrayHelper;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.i18n.I18nModule;
import org.olat.core.util.mail.MailModule;
import org.springframework.beans.factory.annotation.Autowired;

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
	
	@Autowired
	private I18nModule i18nModule;
	@Autowired
	private MailModule mailModule;
	@Autowired
	private I18nManager i18nManager;
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private NotificationsManager notificiationMgr;

	/**
	 * Constructor for the user preferences form
	 * 
	 * @param ureq
	 * @param wControl
	 * @param tobeChangedIdentity the Identity which preferences are displayed and
	 *          edited. Not necessarily the same as ureq.getIdentity()
	 */
	public PreferencesFormController(UserRequest ureq, WindowControl wControl, Identity tobeChangedIdentity) {
		super(ureq, wControl, Util.createPackageTranslator(SystemRolesAndRightsController.class, ureq.getLocale()));
		this.tobeChangedIdentity = tobeChangedIdentity;
		initForm(ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// Refresh user from DB to prevent stale object issues
		tobeChangedIdentity = securityManager.loadIdentityByKey(tobeChangedIdentity.getKey());
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
			WindowManager wm = getWindowControl().getWindowBackOffice().getWindowManager();
			if(fontSize != wm.getFontSize()) {
				getWindowControl().getWindowBackOffice().getWindow().setDirty(true);
			}
		}
		
		if(mailSystem != null && mailSystem.isOneSelected()) {
			String val = mailSystem.isSelected(1) ? "true" : "false";
			prefs.setReceiveRealMail(val);
		}

		if (userManager.updateUserFromIdentity(tobeChangedIdentity)) {
			// Language change needs logout / login
			showInfo("preferences.successful");
		} else {
			showInfo("preferences.unsuccessful");
		}

		userManager.setUserCharset(tobeChangedIdentity, charset.getSelectedKey());
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("title.prefs");
		setFormContextHelp("Configuration#_einstellungen");
		
		// load preferences
		Preferences prefs = tobeChangedIdentity.getUser().getPreferences();

		// Username
		StaticTextElement username = uifactory.addStaticTextElement("form.username", tobeChangedIdentity.getName(), formLayout);
		username.setElementCssClass("o_sel_home_settings_username");
		username.setEnabled(false);

		// Roles
		String[] roleKeys = new String[] {
			OrganisationRoles.usermanager.name(), OrganisationRoles.groupmanager.name(),
			OrganisationRoles.poolmanager.name(), OrganisationRoles.curriculummanager.name(),
			OrganisationRoles.author.name(), OrganisationRoles.learnresourcemanager.name(),
			OrganisationRoles.administrator.name()
		};

		String[] roleValues = new String[]{
				translate("rightsForm.isUsermanager"), translate("rightsForm.isGroupmanager"),
				translate("rightsForm.isPoolmanager"), translate("rightsForm.isCurriculummanager"),
				translate("rightsForm.isAuthor"), translate("rightsForm.isInstitutionalResourceManager"),
				translate("rightsForm.isAdmin")
		};
		
		StringBuilder userRolesSb = new StringBuilder();
		List<String> roles = securityManager.getRolesAsString(tobeChangedIdentity);
		for(String role:roles) {
			for(int i=0; i<roleKeys.length; i++) {
				if(roleKeys[i].equals(role)) {
					if(userRolesSb.length() > 0) userRolesSb.append(", ");
					userRolesSb.append(roleValues[i]);
				}
			}
		}
		
		String userRoles;
		if (userRolesSb.length() == 0) {
			userRoles = translate("rightsForm.isAnonymous.false");
		} else {
			userRoles = userRolesSb.toString();
		}
		uifactory.addStaticTextElement("rightsForm.roles", userRoles, formLayout);
		username.setElementCssClass("o_sel_home_settings_username");
		username.setEnabled(false);

		// Language
		Map<String, String> languages = i18nManager.getEnabledLanguagesTranslated();
		String[] langKeys = StringHelper.getMapKeysAsStringArray(languages);
		String[] langValues = StringHelper.getMapValuesAsStringArray(languages);
		ArrayHelper.sort(langKeys, langValues, false, true, false);
		language = uifactory.addDropdownSingleselect("form.language", formLayout, langKeys, langValues, null);
		language.setElementCssClass("o_sel_home_settings_language");
		String langKey = prefs.getLanguage();
		// Preselect the users language if available. Maye not anymore enabled on
		// this server
		if (prefs.getLanguage() != null && i18nModule.getEnabledLanguageKeys().contains(langKey)) {
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
		fontsize.setElementCssClass("o_sel_home_settings_fontsize");
		fontsize.select(prefs.getFontsize(), true);
		fontsize.addActionListener(FormEvent.ONCHANGE);
		
		// Email notification interval
		List<String> intervals = notificiationMgr.getEnabledNotificationIntervals();
		if (!intervals.isEmpty()) {
			String[] intervalKeys = new String[intervals.size()];
			intervals.toArray(intervalKeys);
			String[] intervalValues = new String[intervalKeys.length];
			String i18nPrefix = "interval.";
			for (int i = 0; i < intervalKeys.length; i++) {
				intervalValues[i] = translate(i18nPrefix + intervalKeys[i]);
			}
			notificationInterval = uifactory.addDropdownSingleselect("form.notification", formLayout, intervalKeys, intervalValues, null);
			notificationInterval.setElementCssClass("o_sel_home_settings_notification_interval");
			notificationInterval.select(prefs.getNotificationInterval(), true);			
		}
		
		if(mailModule.isInternSystem()) {
			String userEmail = userManager.getUserDisplayEmail(tobeChangedIdentity, ureq.getLocale());
			String[] mailInternLabels = new String[] { translate("mail." + mailIntern[0], userEmail), translate("mail." + mailIntern[1], userEmail) };
			mailSystem = uifactory.addRadiosVertical("mail-system", "mail.system", formLayout, mailIntern, mailInternLabels);
			mailSystem.setElementCssClass("o_sel_home_settings_mail");
			
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
		String currentCharset = userManager.getUserCharset(tobeChangedIdentity);
		String[] csKeys = StringHelper.getMapKeysAsStringArray(charsets);
		charset = uifactory.addDropdownSingleselect("form.charset", formLayout, csKeys, csKeys, null);
		charset.setElementCssClass("o_sel_home_settings_charset");
		if(currentCharset != null) {
			for(String csKey:csKeys) {
				if(csKey.equals(currentCharset)) {
					charset.select(currentCharset, true);
				}
			}
		}
		if(!charset.isOneSelected() && charsets.containsKey("UTF-8")) {
			charset.select("UTF-8", true);
		}

		// Submit and cancel buttons
		final FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("button_layout", getTranslator());
		formLayout.add(buttonLayout);
		buttonLayout.setElementCssClass("o_sel_home_settings_prefs_buttons");
		uifactory.addFormSubmitButton("submit", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
	}

	@Override
	protected void formInnerEvent (UserRequest ureq, FormItem source, FormEvent event) {
		if (source == fontsize && ureq.getIdentity().equalsByPersistableKey(tobeChangedIdentity)) {
			int fontSize = Integer.parseInt(fontsize.getSelectedKey());
			WindowManager wm = getWindowControl().getWindowBackOffice().getWindowManager();
			if(fontSize != wm.getFontSize()) {
				getWindowControl().getWindowBackOffice().getWindow().setDirty(true);
			}
		}
	}

	@Override
	protected void doDispose() {
		// nothing to do
	}
}
