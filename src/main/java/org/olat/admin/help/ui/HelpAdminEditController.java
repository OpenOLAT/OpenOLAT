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
package org.olat.admin.help.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.olat.core.commons.services.help.HelpModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.TranslatorHelper;
import org.olat.core.util.StringHelper;
import org.olat.core.util.i18n.I18nItem;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.i18n.I18nModule;
import org.olat.core.util.mail.EmailAddressValidator;
import org.olat.course.CourseFactory;
import org.springframework.beans.factory.annotation.Autowired;

/* 
 * Initial date: 8 Apr 2020<br>
 * @author aboeckle, alexander.boeckle@frentix.com
 */
public class HelpAdminEditController extends FormBasicController {

	private static final String TRANSLATOR_PACKAGE = HelpAdminController.class.getPackage().getName();
	private final static String[] onKeys = new String[]{ "enabled" }; 

	private List<TextElement> localeElements;
	private TextElement iconEl;
	private TextElement inputEl;
	private MultipleSelectionElement displayEl;
	private MultipleSelectionElement newWindowEl;

	private String[] displayKeys = new String[3];
	private String[] displayValues = new String[displayKeys.length];
	private String[] onValues = new String[onKeys.length];

	private String helpPlugin;

	@Autowired
	private HelpModule helpModule;
	@Autowired 
	private I18nModule i18nModule;
	@Autowired
	private I18nManager i18nManager;

	public HelpAdminEditController(UserRequest ureq, WindowControl wControl, String helpPluginToAdd) {
		super(ureq, wControl);
		this.helpPlugin = helpPluginToAdd;

		init();
		initForm(ureq);
		
		setInputLabel(helpPlugin);
	}

	public HelpAdminEditController(UserRequest ureq, WindowControl wControl, HelpAdminTableContentRow row) {
		super(ureq, wControl);
		this.helpPlugin = row.getHelpPlugin();
		
		init();
		initForm(ureq);

		setInputLabel(helpPlugin);
		
		iconEl.setValue(StringHelper.containsNonWhitespace(row.getIcon()) ? row.getIcon() : HelpModule.DEFAULT_ICON);
		displayEl.select(displayKeys[0], row.isAuthoringSet());
		displayEl.select(displayKeys[1], row.isUsertoolSet());
		displayEl.select(displayKeys[2], row.isLoginSet());
	}
	
	private void init() {
		for (int i = 0; i < onKeys.length; i++) {
			onValues[i] = translate(onKeys[i]);
		}

		displayKeys[0] = "help.admin.display.authorsite";
		displayKeys[1] = "help.admin.display.usertool";
		displayKeys[2] = "help.admin.display.login";

		displayValues = TranslatorHelper.translateAll(getTranslator(), displayKeys);
	}

	private void setInputLabel(String helpPlugin) {
		// Enable the DMZ location in case it was hidden
		displayEl.setEnabled("help.admin.display.login", true);
		
		switch (helpPlugin) {
		case HelpModule.ACADEMY:
			inputEl.setLabel("help.admin.input.academy", null);
			inputEl.setValue(helpModule.getAcademyLink());
			iconEl.setValue("o_icon_video");
			setElementVisible(inputEl, true, true, true);
			break;
		case HelpModule.OOTEACH:
			inputEl.setLabel("help.admin.input.ooTeach", null);
			inputEl.setValue(helpModule.getOOTeachLink());
			iconEl.setValue("o_icon_video");
			setElementVisible(inputEl, true, true, true);
			break;
		case HelpModule.CONFLUENCE:
			String[] confluenceKeys = new String[displayKeys.length + 1];
			System.arraycopy(displayKeys, 0, confluenceKeys, 1, displayKeys.length);
			confluenceKeys[0] = "help.admin.display.context";
			String[] confluenceValues = TranslatorHelper.translateAll(getTranslator(), confluenceKeys);
			
			displayEl.setKeysAndValues(confluenceKeys, confluenceValues);
			displayEl.setEnabled("help.admin.display.context", false);
			displayEl.select("help.admin.display.context", true);
			
			inputEl.setLabel("help.admin.input.confluence", null);
			iconEl.setValue("o_icon_manual");
			setElementVisible(inputEl, false, false, false);
			break;
		case HelpModule.SUPPORT:
			inputEl.setLabel("help.admin.input.support", null);
			inputEl.setValue(helpModule.getSupportEmail());
			iconEl.setValue("o_icon_mail");
			setElementVisible(inputEl, true, true, true);
			break;
		case HelpModule.COURSE:
			inputEl.setLabel("help.admin.input.course", null);
			setElementVisible(inputEl, true, true, true);
			inputEl.setValue(helpModule.getCourseSoftkey());
			// Disable the DMZ for legacy course help
			displayEl.setEnabled("help.admin.display.login", false);
			iconEl.setValue("o_course_icon");
			break;
		case HelpModule.CUSTOM_1:
			inputEl.setLabel("help.admin.input.url", null);
			newWindowEl.select(onKeys[0], helpModule.isCustom1NewWindow());
			inputEl.setValue(helpModule.getCustom1Link());
			iconEl.setValue("o_icon_external_link");
			setElementVisible(inputEl, true, true, true);
			setElementVisible(newWindowEl, true, true, false);
			break;
		case HelpModule.CUSTOM_2: 
			inputEl.setLabel("help.admin.input.url", null);
			newWindowEl.select(onKeys[0], helpModule.isCustom2NewWindow());
			inputEl.setValue(helpModule.getCustom2Link());
			iconEl.setValue("o_icon_external_link");
			setElementVisible(inputEl, true, true, true);
			setElementVisible(newWindowEl, true, true, false);
			break;
		case HelpModule.CUSTOM_3: 
			inputEl.setLabel("help.admin.input.url", null);
			newWindowEl.select(onKeys[0], helpModule.isCustom3NewWindow());
			inputEl.setValue(helpModule.getCustom3Link());
			iconEl.setValue("o_icon_external_link");
			setElementVisible(inputEl, true, true, true);
			setElementVisible(newWindowEl, true, true, false);
			break;
		default:
			break;
		}
	}

	private void setElementVisible(FormItem formItem, boolean visible, boolean enabled, boolean mandatory) {
		formItem.setVisible(visible);
		formItem.setEnabled(enabled);
		formItem.setMandatory(mandatory);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttonLayout", getTranslator());
		buttonLayout.setRootForm(mainForm);

		String type = translate("help.admin." + helpPlugin);
		uifactory.addStaticTextElement("help.admin.type", type, formLayout);

		Collection<String> enabledLanguageKeys = i18nModule.getEnabledLanguageKeys();
		localeElements = new ArrayList<>(enabledLanguageKeys.size());
		for (String languageKey : enabledLanguageKeys) {
			TextElement localeEl = uifactory.addTextElement("locale." + languageKey, 255, null, formLayout);
			localeEl.setLabel(translate("help.admin.label") + " " + languageKey.toUpperCase(), null, false);
			Locale locale = i18nManager.getLocaleOrNull(languageKey);
			localeEl.setUserObject(locale);
			setTranslatedValue(localeEl, locale);
			localeElements.add(localeEl);
		}
		
		iconEl = uifactory.addTextElement("help.admin.icon", 255, "", formLayout);
		iconEl.setValue(HelpModule.DEFAULT_ICON);
		iconEl.setExampleKey("help.admin.icon.examples", new String[] {"o_icon_help, o_icon_manual, o_icon_mail, o_icon_video, o_icon_wiki, o_course_icon, o_icon_external_link, o_icon_link, ..."});
		iconEl.setMandatory(true);
		
		inputEl = uifactory.addTextElement("help.admin.input.support", 255, "", formLayout);
		inputEl.setMandatory(true);
		
		newWindowEl = uifactory.addCheckboxesVertical("help.admin.new.window", formLayout, onKeys, onValues, 1);
		setElementVisible(newWindowEl, false, false, false);
		
		displayEl = uifactory.addCheckboxesVertical("help.admin.display", formLayout, displayKeys, displayValues, 1);

		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
		uifactory.addFormSubmitButton("submit", buttonLayout);
		formLayout.add(buttonLayout);
	}
	
	private void setTranslatedValue(TextElement localeEl, Locale locale) {
		String i18nKey = getPluginI18nKey();
		String translation = i18nManager.getLocalizedString(TRANSLATOR_PACKAGE, i18nKey, null, locale, false, false, false, false, 0);
		String overlayTranslation = i18nManager.getLocalizedString(TRANSLATOR_PACKAGE, i18nKey, null, locale, true, false, false, false, 0);
		if (StringHelper.containsNonWhitespace(overlayTranslation)) {
			localeEl.setValue(overlayTranslation);
		} else {
			localeEl.setValue(translation);
		}
		localeEl.setPlaceholderText(translation);
	}

	private String getPluginI18nKey() {
		return "help." + helpPlugin;
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		super.formCancelled(ureq);

		fireEvent(ureq, FormEvent.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {		
		helpModule.saveHelpPlugin(
				helpPlugin, 
				iconEl.getValue(), 
				inputEl.getValue(), 
				displayEl.getSelectedKeys().contains("help.admin.display.usertool"), 
				displayEl.getSelectedKeys().contains("help.admin.display.authorsite"), 
				displayEl.getSelectedKeys().contains("help.admin.display.login"),
				newWindowEl.getSelectedKeys().contains(onKeys[0]));
		
		Map<Locale, Locale> allOverlays = i18nModule.getOverlayLocales();
		String i18nKey = getPluginI18nKey();
		for (TextElement localeEl : localeElements) {
			String translation = localeEl.getValue();
			Locale locale = (Locale)localeEl.getUserObject();
			I18nItem item = i18nManager.getI18nItem(TRANSLATOR_PACKAGE, i18nKey, allOverlays.get(locale));
			i18nManager.saveOrUpdateI18nItem(item, translation);
		}

		fireEvent(ureq, FormEvent.DONE_EVENT);
	}

	@Override
	protected void doDispose() {

	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOK = super.validateFormLogic(ureq);

		for (TextElement localeEl : localeElements) {
			allOK &= validateTextInput(localeEl, 255, false);
			
		}
		
		allOK &= validateTextInput(iconEl, 255, false);
		
		switch (helpPlugin) {
		case HelpModule.SUPPORT:
			allOK &= validateTextInput(inputEl, 255, true);
			break;
		case HelpModule.CUSTOM_1:
		case HelpModule.CUSTOM_2:
		case HelpModule.CUSTOM_3:
			allOK &= validateTextInput(inputEl, 255, false);
			break;
		case HelpModule.COURSE:
			allOK &= validateTextInput(inputEl, 255, false);
			if (allOK) {
				allOK &= CourseFactory.isHelpCourseExisting(inputEl.getValue());
				if (!allOK) {
					inputEl.setErrorKey("help.admin.course.key.wrong", null);
				}
			}
			break;
		default:
			break;
		}
		

		return allOK;
	}

	private boolean validateTextInput(TextElement textElement, int lenght, boolean isMail) {		
		textElement.clearError();
		if(StringHelper.containsNonWhitespace(textElement.getValue())) {
			if (isMail) {
				if (!EmailAddressValidator.isValidEmailAddress(textElement.getValue())) {
					textElement.setErrorKey("help.admin.input.wrong.mail", null);
					return false;
				}
			} if(lenght != -1 && textElement.getValue().length() > lenght) {
				textElement.setErrorKey("input.toolong", new String[]{ String.valueOf(lenght) });
				return false;
			}
		} else if (textElement.isMandatory()) {
			textElement.setErrorKey("form.legende.mandatory", null);
			return false;
		}

		return true;
	}

}
