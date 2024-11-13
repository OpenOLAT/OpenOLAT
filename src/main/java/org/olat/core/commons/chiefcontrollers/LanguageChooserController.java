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
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/ 
package org.olat.core.commons.chiefcontrollers;

import java.util.Locale;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.dropdown.DropdownItem;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.ArrayHelper;
import org.olat.core.util.StringHelper;
import org.olat.core.util.event.MultiUserEvent;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.resource.OresHelper;

/**
 * Description:<br>
 * The LanguageChooserController creates a dropdown to choose the language. 
 * <P>
 * Initial Date: 25.01.2007 <br>
 * 
 * @author patrickb
 */
public class LanguageChooserController extends FormBasicController {

	private DropdownItem langDropdown;
	
	public LanguageChooserController(WindowControl wControl, UserRequest ureq, String id) {
		super(ureq, wControl, id, "langchooser");
		
		initForm(ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source instanceof FormLink link && link.getUserObject() instanceof String langKey) {
			String langValue = link.getI18nKey();
			langDropdown.setTranslatedLabel(langValue);
			
			Locale loc = I18nManager.getInstance().getLocaleOrDefault(langKey);
			MultiUserEvent mue = new LanguageChangedEvent(loc, ureq);
			ureq.getUserSession().setLocale(loc);
			ureq.getUserSession().putEntry("negotiated-locale", loc);
			I18nManager.updateLocaleInfoToThread(ureq.getUserSession());
			OLATResourceable wrappedLocale = OresHelper.createOLATResourceableType(Locale.class);
			ureq.getUserSession().getSingleUserEventCenter().fireEventToListenersOf(mue, wrappedLocale);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, final UserRequest ureq) {
		Map<String, String> languages = I18nManager.getInstance().getEnabledLanguagesTranslated();
		String[] langKeys = StringHelper.getMapKeysAsStringArray(languages);
		String[] langValues = StringHelper.getMapValuesAsStringArray(languages);
		ArrayHelper.sort(langKeys, langValues, false, true, false);
		
		langDropdown = uifactory.addDropdownMenu("select.language", null, null, formLayout, getTranslator());
		langDropdown.setOrientation(DropdownOrientation.right);
		langDropdown.addActionListener(FormEvent.ONCHANGE);
		String currentLang = ureq.getLocale().toString();
		for (int i = 0; i < langKeys.length; i++) {
			String langKey = langKeys[i];
			String langValue = langValues[i];
			
			FormLink link = uifactory.addFormLink("lang_" + langKey, formLayout, Link.LINK + Link.NONTRANSLATED);
			link.setI18nKey(langValue);
			link.setUserObject(langKey);
			langDropdown.addElement(link);
			
			if (currentLang.equalsIgnoreCase(langKey)) {
				langDropdown.setTranslatedLabel(translate("noTransOnlyParam", langValue));
			}
		}
	}

}