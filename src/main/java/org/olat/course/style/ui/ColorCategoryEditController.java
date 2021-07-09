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
package org.olat.course.style.ui;

import static org.olat.core.gui.components.util.SelectionValues.entry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.i18n.I18nItem;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.i18n.I18nModule;
import org.olat.course.style.ColorCategory;
import org.olat.course.style.CourseStyleService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 23 Jun 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ColorCategoryEditController extends FormBasicController {
	
	private static final String TRANSLATOR_PACKAGE = ColorCategoryEditController.class.getPackage().getName();
	
	private TextElement identifierEl;
	private SingleSelection enableEl;
	private TextElement cssClassEl;
	private List<TextElement> localeElements;
	
	private ColorCategory colorCategory;
	
	@Autowired
	private CourseStyleService courseStyleService;
	@Autowired
	private I18nManager i18nManager;
	@Autowired
	private I18nModule i18nModule;

	public ColorCategoryEditController(UserRequest ureq, WindowControl wControl, ColorCategory colorCategory) {
		super(ureq, wControl);
		this.colorCategory = colorCategory;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if (colorCategory == null) {
			identifierEl = uifactory.addTextElement("color.category.identifier", 128, null, formLayout);
			identifierEl.setMandatory(true);
		} else {
			uifactory.addStaticTextElement("color.category.identifier", colorCategory.getIdentifier(), formLayout);
		}
		
		SelectionValues activeKeyValues = new SelectionValues();
		activeKeyValues.add(entry("true", translate("color.category.enabled")));
		activeKeyValues.add(entry("false", translate("color.category.disabled")));
		enableEl = uifactory.addRadiosHorizontal("color.category.enabled.label", "color.category.enabled.label", formLayout,
				activeKeyValues.keys(), activeKeyValues.values());
		if(colorCategory != null) {
			enableEl.select(Boolean.toString(colorCategory.isEnabled()), true);
		} else {
			enableEl.select("true", true);
		}
		
		Collection<String> enabledLanguageKeys = i18nModule.getEnabledLanguageKeys();
		localeElements = new ArrayList<>(enabledLanguageKeys.size());
		for (String languageKey : enabledLanguageKeys) {
			TextElement localeEl = uifactory.addTextElement("locale." + languageKey, 255, null, formLayout);
			String languageDisplayName = Locale.forLanguageTag(languageKey.substring(0,2)).getDisplayLanguage(getLocale());
			localeEl.setLabel(translate("color.category.translation.lang", new String[] {languageDisplayName, languageKey} ), null, false);
			Locale locale = i18nManager.getLocaleOrNull(languageKey);
			localeEl.setUserObject(locale);
			setTranslatedValue(localeEl, locale);
			localeElements.add(localeEl);
		}
		
		String cssClass = colorCategory != null? colorCategory.getCssClass(): null;
		cssClassEl = uifactory.addTextElement("color.category.css.class", 128, cssClass, formLayout);
		cssClassEl.setMandatory(true);

		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("save", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}

	private void setTranslatedValue(TextElement localeEl, Locale locale) {
		if (colorCategory != null) {
			String i18nKey = CourseStyleUIFactory.getI18nKey(colorCategory);
			String translation = i18nManager.getLocalizedString(TRANSLATOR_PACKAGE, i18nKey, null, locale, false, false, false, false, 0);
			String overlayTranslation = i18nManager.getLocalizedString(TRANSLATOR_PACKAGE, i18nKey, null, locale, true, false, false, false, 0);
			if (StringHelper.containsNonWhitespace(overlayTranslation)) {
				localeEl.setValue(overlayTranslation);
			} else {
				localeEl.setValue(translation);
			}
			localeEl.setPlaceholderText(translation);
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
			} else if (!courseStyleService.isColorCategoryIdentifierAvailable(identifier)) {
				identifierEl.setErrorKey("error.color.category.identifier.not.available", null);
				allOk &= false;
			}
		}
		
		cssClassEl.clearError();
		if (!StringHelper.containsNonWhitespace(cssClassEl.getValue())) {
			cssClassEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		} 
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if (colorCategory == null) {
			colorCategory = courseStyleService.createColorCategory(identifierEl.getValue());
		}
		
		String i18nKey = CourseStyleUIFactory.getI18nKey(colorCategory);
		Map<Locale, Locale> allOverlays = i18nModule.getOverlayLocales();
		for (TextElement localeEl : localeElements) {
			String translation = localeEl.getValue();
			Locale locale = (Locale)localeEl.getUserObject();
			I18nItem item = i18nManager.getI18nItem(TRANSLATOR_PACKAGE, i18nKey, allOverlays.get(locale));
			i18nManager.saveOrUpdateI18nItem(item, translation);
		}
		
		boolean enabled = "true".equals(enableEl.getSelectedKey());
		colorCategory.setEnabled(enabled);
		
		String cssClass = cssClassEl.getValue();
		colorCategory.setCssClass(cssClass);
		
		courseStyleService.updateColorCategory(colorCategory);
		
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void doDispose() {
		//
	}

}
