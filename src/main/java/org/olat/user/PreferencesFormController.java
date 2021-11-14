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
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.olat.admin.user.SystemRolesAndRightsController;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.commons.services.doceditor.DocEditor;
import org.olat.core.commons.services.doceditor.DocEditorService;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.Preferences;
import org.olat.core.util.ArrayHelper;
import org.olat.core.util.Formatter;
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
	private Identity tobeChangedIdentity;
	
	private SingleSelection charset;
	private SingleSelection language;
	private SingleSelection mailSystem;
	private SingleSelection notificationInterval;
	private SingleSelection documentEditorEl;
	
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
	private BaseSecurityModule securityModule;
	@Autowired
	private NotificationsManager notificiationMgr;
	@Autowired
	private DocEditorService docEditorService;
	@Autowired
	private UserLifecycleManager userLifecycleManager;

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
		if (notificationInterval != null) {
			// only read notification interval if available, could be disabled by configuration
			prefs.setNotificationInterval(notificationInterval.getSelectedKey());			
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
		
		if (documentEditorEl != null && documentEditorEl.isOneSelected()) {
			docEditorService.setPreferredEditorType(getIdentity(), documentEditorEl.getSelectedKey());
		}
		
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
		String name = securityManager.findAuthenticationName(tobeChangedIdentity);
		if(!StringHelper.containsNonWhitespace(name) && !securityModule.isIdentityNameAutoGenerated()) {
			name = tobeChangedIdentity.getName();
		}
		StaticTextElement username = uifactory.addStaticTextElement("form.username", name, formLayout);
		username.setElementCssClass("o_sel_home_settings_username");
		username.setEnabled(false);
		
		StringBuilder userRolesSb = new StringBuilder();
		Set<String> roles = new HashSet<>(securityManager.getRolesAsString(tobeChangedIdentity));
		for(String role:roles) {
			if(userRolesSb.length() > 0) userRolesSb.append(", ");
			userRolesSb.append(translate("role.".concat(role)));
		}
		
		String userRoles;
		if (userRolesSb.length() == 0) {
			userRoles = translate("role.guest.false");
		} else {
			userRoles = userRolesSb.toString();
		}
		uifactory.addStaticTextElement("rightsForm.roles", userRoles, formLayout);
		username.setElementCssClass("o_sel_home_settings_username");
		username.setEnabled(false);
		
		// Expiration
		boolean expirationDateVisible = tobeChangedIdentity.getExpirationDate() != null
				|| tobeChangedIdentity.getInactivationDate() != null || tobeChangedIdentity.getReactivationDate() != null;
		
		Date expirationDate = userLifecycleManager.getDateUntilDeactivation(tobeChangedIdentity);
		String expDate = Formatter.getInstance(getLocale()).formatDate(expirationDate);
		StaticTextElement expirationDateEl = uifactory.addStaticTextElement("rightsForm.expiration.date", expDate, formLayout);
		expirationDateEl.setVisible(expirationDateVisible);

		long days = userLifecycleManager.getDaysUntilDeactivation(tobeChangedIdentity, ureq.getRequestTimestamp());
		StaticTextElement expirationDaysEl = uifactory.addStaticTextElement("rightsForm.days.inactivation", Long.toString(days), formLayout);
		expirationDaysEl.setVisible(expirationDateVisible);

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
		
		// Document editor
		List<DocEditor> editors = docEditorService.getExternalEditors(getIdentity(), ureq.getUserSession().getRoles());
		if (editors.size() >= 2) {
			SelectionValues editorKV = new SelectionValues();
			editors.stream()
					.sorted((e1, e2) -> e1.getDisplayName(getLocale()).compareTo(e2.getDisplayName(getLocale())))
					.forEach(e -> editorKV.add(SelectionValues.entry(e.getType(), e.getDisplayName(getLocale()))));
			documentEditorEl = uifactory.addDropdownSingleselect("form.document.editor", formLayout, editorKV.keys(), editorKV.values());
			String preferredEditorType = docEditorService.getPreferredEditorType(getIdentity());
			if (Arrays.asList(documentEditorEl.getKeys()).contains(preferredEditorType)) {
				documentEditorEl.select(preferredEditorType, true);
			}
		}

		// Submit and cancel buttons
		final FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("button_layout", getTranslator());
		formLayout.add(buttonLayout);
		buttonLayout.setElementCssClass("o_sel_home_settings_prefs_buttons");
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
		uifactory.addFormSubmitButton("submit", buttonLayout);
	}
}
