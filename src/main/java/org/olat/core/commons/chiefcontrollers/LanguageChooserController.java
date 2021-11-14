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
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
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

	private SingleSelection langs;

	String curlang;
	public LanguageChooserController(WindowControl wControl, UserRequest ureq, String id) {
		super(ureq, wControl, id, "langchooser");
		// init variables
		curlang = ureq.getLocale().toString();
		initForm(ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		Locale loc = I18nManager.getInstance().getLocaleOrDefault(getSelectedLanguage());
		MultiUserEvent mue = new LanguageChangedEvent(loc, ureq);
		ureq.getUserSession().setLocale(loc);
		ureq.getUserSession().putEntry("negotiated-locale", loc);
		I18nManager.updateLocaleInfoToThread(ureq.getUserSession());
		OLATResourceable wrappedLocale = OresHelper.createOLATResourceableType(Locale.class);
		ureq.getUserSession().getSingleUserEventCenter().fireEventToListenersOf(mue, wrappedLocale);
		// Update in velocity for flag
		flc.contextPut("languageCode", loc.toString());
	}

	/**
	 * selected language
	 * 
	 * @return
	 */
	public String getSelectedLanguage() {
		return langs.getSelectedKey();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, final UserRequest ureq) {

		// SingleSelectionImpl creates following $r.render("..") names in velocity
		// languages_LABEL -> access label of singleselection
		// languages_ERROR -> access error of singleselection
		// languages_EXAMPLE -> access example of singleselection
		// languages_SELBOX -> render whole selection as selectionbox
		// radiobuttons are accessed by appending the key, for example by
		// languages_yes languages_no
		//
		Map<String, String> languages = I18nManager.getInstance().getEnabledLanguagesTranslated();
		String[] langKeys = StringHelper.getMapKeysAsStringArray(languages);
		String[] langValues = StringHelper.getMapValuesAsStringArray(languages);
		ArrayHelper.sort(langKeys, langValues, false, true, false);
		// Build css classes for reference languages
		langs = uifactory.addDropdownSingleselect(mainForm.getFormId() + "_select", "select.language", "select.language", formLayout, langKeys, langValues, null); 
		langs.addActionListener(FormEvent.ONCHANGE);
		langs.select(curlang, true);
		// Add to velocity for flag
		flc.contextPut("languageCode", curlang);
	}

	@Override
	protected void doDispose() {
		langs = null;
        super.doDispose();
	}

}