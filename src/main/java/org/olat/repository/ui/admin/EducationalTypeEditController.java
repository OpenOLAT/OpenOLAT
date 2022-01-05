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
package org.olat.repository.ui.admin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.i18n.I18nItem;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.i18n.I18nModule;
import org.olat.repository.RepositoryEntryEducationalType;
import org.olat.repository.RepositoryManager;
import org.olat.repository.ui.RepositoyUIFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 18 Jan 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class EducationalTypeEditController extends FormBasicController {
	
	private static final String TRANSLATOR_PACKAGE = RepositoryManager.class.getPackage().getName();
	private static final String[] onKeys = new String[] { "on" };
	
	private TextElement identifierEl;
	private TextElement cssClassEl;
	private List<TextElement> localeElements;
	private List<TextElement> presetLocaleElements;
	private MultipleSelectionElement presetMyCoursesEl;
	
	private RepositoryEntryEducationalType type;
	
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private I18nManager i18nManager;
	@Autowired
	private I18nModule i18nModule;

	public EducationalTypeEditController(UserRequest ureq, WindowControl wControl, RepositoryEntryEducationalType type) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(RepositoryManager.class, getLocale(), getTranslator()));
		this.type = type;
		
		initForm(ureq);
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if (type == null) {
			identifierEl = uifactory.addTextElement("educational.type.identifier", 128, null, formLayout);
			identifierEl.setMandatory(true);
		} else {
			uifactory.addStaticTextElement("educational.type.identifier", type.getIdentifier(), formLayout);
		}
		
		Collection<String> enabledLanguageKeys = i18nModule.getEnabledLanguageKeys();
		localeElements = new ArrayList<>(enabledLanguageKeys.size());
		String i18nKey = type == null ? null: RepositoyUIFactory.getI18nKey(type);
		for (String languageKey : enabledLanguageKeys) {
			TextElement localeEl = uifactory.addTextElement("locale." + languageKey, "educational.type.translation.lang", 255, null, formLayout);
			String languageDisplayName = Locale.forLanguageTag(languageKey.substring(0,2)).getDisplayLanguage(getLocale());
			localeEl.setLabel(translate("educational.type.translation.lang", languageDisplayName, languageKey), null, false);
			Locale locale = i18nManager.getLocaleOrNull(languageKey);
			localeEl.setUserObject(locale);
			setTranslatedValue(localeEl, i18nKey, locale);
			localeElements.add(localeEl);
		}
		
		String cssClass = type != null? type.getCssClass(): null;
		cssClassEl = uifactory.addTextElement("educational.type.css.class", 128, cssClass, formLayout);
		
		presetMyCoursesEl = uifactory.addCheckboxesHorizontal("educational.preset.mycourses", "educational.preset.mycourses", formLayout, onKeys, new String[] { "" });
		presetMyCoursesEl.select(onKeys[0], type != null && type.isPresetMyCourses());
		presetMyCoursesEl.addActionListener(FormEvent.ONCHANGE);
		
		presetLocaleElements = new ArrayList<>(enabledLanguageKeys.size());
		String presetI18nKey = type == null ? null : RepositoyUIFactory.getPresetI18nKey(type);
		for (String languageKey : enabledLanguageKeys) {
			TextElement localeEl = uifactory.addTextElement("locale.preset." + languageKey, "educational.type.translation.lang", 255, null, formLayout);
			String languageDisplayName = Locale.forLanguageTag(languageKey.substring(0,2)).getDisplayLanguage(getLocale());
			localeEl.setLabel(translate("educational.type.preset.translation.lang", languageDisplayName, languageKey), null, false);
			Locale locale = i18nManager.getLocaleOrNull(languageKey);
			localeEl.setUserObject(locale);
			setTranslatedValue(localeEl, presetI18nKey, locale);
			presetLocaleElements.add(localeEl);
		}
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("save", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}

	private void setTranslatedValue(TextElement localeEl, String i18nKey, Locale locale) {
		if (type != null) {
			String translation = i18nManager.getLocalizedString(TRANSLATOR_PACKAGE, i18nKey, null, locale, false, false, false, true, 0);
			String overlayTranslation = i18nManager.getLocalizedString(TRANSLATOR_PACKAGE, i18nKey, null, locale, true, false, false, true, 0);
			if (StringHelper.containsNonWhitespace(overlayTranslation)) {
				localeEl.setValue(overlayTranslation);
			} else {
				localeEl.setValue(translation);
			}
			localeEl.setPlaceholderText(translation);
		}
	}
	
	private void updateUI() {
		boolean presetMyCourses = presetMyCoursesEl.isAtLeastSelected(1);
		for(TextElement localEl:presetLocaleElements) {
			localEl.setVisible(presetMyCourses);
		}
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		if (identifierEl != null) {
			identifierEl.clearError();
			String identifier = identifierEl.getValue();
			if (!StringHelper.containsNonWhitespace(identifier)) {
				identifierEl.setErrorKey("form.legende.mandatory", null);
				allOk &= false;
			} else if (!repositoryManager.isEducationalTypeIdentifierAvailable(identifier)) {
				identifierEl.setErrorKey("error.educational.identifier.not.available", null);
				allOk &= false;
			}
		}
		
		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == presetMyCoursesEl) {
			updateUI();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if (type == null) {
			type = repositoryManager.createEducationalType(identifierEl.getValue());
		}
		
		String i18nKey = RepositoyUIFactory.getI18nKey(type);
		Map<Locale, Locale> allOverlays = i18nModule.getOverlayLocales();
		for (TextElement localeEl : localeElements) {
			saveTranslation(localeEl, i18nKey, allOverlays);
		}
		
		boolean presetMyCourses = presetMyCoursesEl.isAtLeastSelected(1);
		if(presetMyCourses) {
			String presetI18nKey = RepositoyUIFactory.getPresetI18nKey(type);
			for (TextElement localeEl : presetLocaleElements) {
				saveTranslation(localeEl, presetI18nKey, allOverlays);
			}
		}
		
		String cssClass = cssClassEl.getValue();
		type.setCssClass(cssClass);
		type.setPresetMyCourses(presetMyCourses);
		
		repositoryManager.updateEducationalType(type);
		
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	private void saveTranslation(TextElement localeEl, String i18nKey, Map<Locale, Locale> allOverlays) {
		String translation = localeEl.getValue();
		Locale locale = (Locale)localeEl.getUserObject();
		I18nItem item = i18nManager.getI18nItem(TRANSLATOR_PACKAGE, i18nKey, allOverlays.get(locale));
		i18nManager.saveOrUpdateI18nItem(item, translation);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
