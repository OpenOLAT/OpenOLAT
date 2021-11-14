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
* Initial code contributed and copyrighted by<br>
* frentix GmbH, http://www.frentix.com
* <p>
*/
package de.bps.olat.portal.links;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.ValidationError;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.elements.ItemValidatorProvider;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.i18n.I18nManager;

/**
 * Initial Date:  08.06.2011 <br>
 * @author rhaag
 */
public class LinksPortletEditController extends FormBasicController {

	private static final String TARGET_BLANK = "blank";
	private static final String TARGET_SELF = "self";
	private PortletLink portletLink;
	private MultipleSelectionElement openPopup;
	private SingleSelection language;
	private RichTextElement desc;
	private TextElement title;
	private TextElement linkURL;

	public LinksPortletEditController(UserRequest ureq, WindowControl wControl, PortletLink portletLink) {
		super(ureq, wControl);
		this.portletLink = portletLink;
		
		initForm(ureq);
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#initForm(org.olat.core.gui.components.form.flexible.FormItemContainer, org.olat.core.gui.control.Controller, org.olat.core.gui.UserRequest)
	 */
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
	
		title = uifactory.addTextElement("link.title", "link.title", 200, portletLink.getTitle(), formLayout);
		title.setMandatory(true);
		title.setNotEmptyCheck("link.title.not.empty");
		desc = uifactory.addRichTextElementForStringDataMinimalistic("link.desc", "link.desc", portletLink.getDescription(), 5, -1, formLayout, getWindowControl());
		linkURL = uifactory.addTextElement("link.url", "link.url", 1024, portletLink.getUrl(), formLayout);
		linkURL.setMandatory(true);
		linkURL.setNotEmptyCheck("link.url.not.empty");
		linkURL.setItemValidatorProvider(new ItemValidatorProvider() {
			@Override
			public boolean isValidValue(String value, final ValidationError validationError, final Locale locale) {
				try {
					if (!value.contains("://")) {
						value = "http://".concat(value);
					}
					new URL(value);
				} catch (final MalformedURLException e) {
					validationError.setErrorKey("link.url.not.empty");
					return false;
				}
				return true;
			}
		});
		openPopup = uifactory.addCheckboxesHorizontal("link.open.new.window", "link.open.new.window", formLayout, new String[]{TARGET_BLANK}, new String[]{""});
		if (portletLink.getTarget().equals(TARGET_BLANK)) {
			openPopup.selectAll();
		}
		
		// language
		Map<String, String> locdescs = I18nManager.getInstance().getEnabledLanguagesTranslated();
		Set<String> lkeys = locdescs.keySet();
		String[] languageKeys = new String[lkeys.size()+1];
		String[] languageValues = new String[lkeys.size()+1];
		languageKeys[0] = "*";
		languageValues[0] = translate("link.lang.all");
		int p = 1;
		for (Iterator<String> iter = lkeys.iterator(); iter.hasNext();) {
			String key = iter.next();
			languageKeys[p] = key;
			languageValues[p] = locdescs.get(key);
			p++;
		}		
		language = uifactory.addDropdownSingleselect("link.language", formLayout, languageKeys, languageValues, null);
		String langKey = portletLink.getLanguage();
		if(Arrays.asList(languageKeys).contains(langKey)){ 
			language.select(langKey, true);
		}

		uifactory.addFormSubmitButton("save", formLayout);
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#formOK(org.olat.core.gui.UserRequest)
	 */
	@Override
	protected void formOK(UserRequest ureq) {
		
		// persist changes
		portletLink.setTitle(title.getValue());
		portletLink.setDescription(desc.getValue());
		String urlToSet = linkURL.getValue();
		if (!urlToSet.contains("://")) {
			urlToSet = "http://".concat(urlToSet);
		}
		portletLink.setUrl(urlToSet);
		if (openPopup.isSelected(0)) {
			portletLink.setTarget(TARGET_BLANK);
		} else {
			portletLink.setTarget(TARGET_SELF);
		}
		portletLink.setLanguage(language.getSelectedKey());
		
		LinksPortlet.updateLink(portletLink);
		fireEvent(ureq, Event.DONE_EVENT);
	}
}
