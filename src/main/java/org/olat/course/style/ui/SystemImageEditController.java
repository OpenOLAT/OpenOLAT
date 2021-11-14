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

import static org.olat.course.style.CourseStyleService.IMAGE_LIMIT_KB;
import static org.olat.course.style.CourseStyleService.IMAGE_MIME_TYPES;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.i18n.I18nItem;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.i18n.I18nModule;
import org.olat.course.style.CourseStyleService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 22 July 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class SystemImageEditController extends FormBasicController {
	
	private static final String TRANSLATOR_PACKAGE = SystemImageEditController.class.getPackage().getName();
	
	private FileElement uploadEl;
	private List<TextElement> localeElements;
	
	private String filename;
	
	@Autowired
	private CourseStyleService courseStyleService;
	@Autowired
	private I18nManager i18nManager;
	@Autowired
	private I18nModule i18nModule;

	public SystemImageEditController(UserRequest ureq, WindowControl wControl, String filename) {
		super(ureq, wControl);
		this.filename = filename;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if (filename == null) {
			uploadEl = uifactory.addFileElement(getWindowControl(), getIdentity(), "system.image.upload", formLayout);
			uploadEl.setMandatory(true);
			uploadEl.setMaxUploadSizeKB(IMAGE_LIMIT_KB, null, null);
			uploadEl.setExampleKey("teaser.image.upload.example", null);
			uploadEl.addActionListener(FormEvent.ONCHANGE);
			uploadEl.limitToMimeType(IMAGE_MIME_TYPES, "error.mimetype", new String[]{ IMAGE_MIME_TYPES.toString()} );
		} else {
			uifactory.addStaticTextElement("system.image.filename", filename, formLayout);
		}
		
		Collection<String> enabledLanguageKeys = i18nModule.getEnabledLanguageKeys();
		localeElements = new ArrayList<>(enabledLanguageKeys.size());
		for (String languageKey : enabledLanguageKeys) {
			TextElement localeEl = uifactory.addTextElement("locale." + languageKey, 255, null, formLayout);
			String languageDisplayName = Locale.forLanguageTag(languageKey.substring(0,2)).getDisplayLanguage(getLocale());
			localeEl.setLabel(translate("system.image.translation.lang", new String[] {languageDisplayName, languageKey} ), null, false);
			Locale locale = i18nManager.getLocaleOrNull(languageKey);
			localeEl.setUserObject(locale);
			setTranslatedValue(localeEl, locale);
			localeElements.add(localeEl);
		}
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("save", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}

	private void setTranslatedValue(TextElement localeEl, Locale locale) {
		if (filename != null) {
			String i18nKey = CourseStyleUIFactory.getSystemImageI18nKey(filename);
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
		
		if (uploadEl != null) {
			uploadEl.clearError();
			if (uploadEl.isVisible()) {
				if (uploadEl.getUploadFile() == null) {
					uploadEl.setErrorKey("form.legende.mandatory", null);
					allOk &= false;
				} else if (courseStyleService.existsSystemImage(uploadEl.getUploadFileName())) {
					uploadEl.setErrorKey("error.system.image.exists", null);
					allOk &= false;
				}
			}
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if (filename == null) {
			courseStyleService.storeSystemImage(uploadEl.getUploadFile(), uploadEl.getUploadFileName());
			filename = uploadEl.getUploadFileName();
		}
		
		String i18nKey = CourseStyleUIFactory.getSystemImageI18nKey(filename);
		Map<Locale, Locale> allOverlays = i18nModule.getOverlayLocales();
		for (TextElement localeEl : localeElements) {
			String translation = localeEl.getValue();
			Locale locale = (Locale)localeEl.getUserObject();
			I18nItem item = i18nManager.getI18nItem(TRANSLATOR_PACKAGE, i18nKey, allOverlays.get(locale));
			i18nManager.saveOrUpdateI18nItem(item, translation);
		}

		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

}
