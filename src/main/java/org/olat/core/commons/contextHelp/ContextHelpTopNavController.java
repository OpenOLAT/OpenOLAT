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
package org.olat.core.commons.contextHelp;

import java.util.Collection;
import java.util.Locale;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.chiefcontrollers.LanguageChangedEvent;
import org.olat.core.commons.fullWebApp.DefaultMinimalTopNavController;
import org.olat.core.commons.fullWebApp.LockableController;
import org.olat.core.dispatcher.impl.StaticMediaDispatcher;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.Windows;
import org.olat.core.gui.components.Window;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.FormUIFactory;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowBackOffice;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.media.RedirectMediaResource;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.ArrayHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.Util;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.i18n.I18nModule;
import org.olat.core.util.resource.OresHelper;
import org.olat.search.SearchServiceUIFactory;
import org.olat.search.SearchServiceUIFactory.DisplayOption;
import org.olat.search.ui.SearchInputController;

/**
 * <h3>Description:</h3> A simple top nav controller that features a close
 * window and print link and change language selector
 * 
 * <h3>Events thrown by this controller:</h3>
 * <ul>
 * <li>LanguageChangedEvent when the language selector is used. The event is
 * fired in the channel CHANGE_LANG_RESOURCE</li>
 * </ul>
 * <p>
 * Initial Date: 04.11.2008 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
  */
public class ContextHelpTopNavController extends FormBasicController implements LockableController {
	static final OLATResourceable CHANGE_LANG_RESOURCE = OresHelper.createOLATResourceableType("ContextHelp:ChangeLanguageChannel");

	private SingleSelection langSelection;
	private FormLink closeLink;
	private SearchInputController searchController;

	/**
	 * Constructor, creates a velocity page with the links and the selector
	 * 
	 * @param ureq
	 * @param wControl
	 */
	public ContextHelpTopNavController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "contexthelptopnav", Util.createPackageTranslator(DefaultMinimalTopNavController.class, ureq.getLocale()));
		// Get lang from URL
		String[] uriParts = ureq.getNonParsedUri().split("/");
		String lang = uriParts[0];
		Locale newLocale = I18nManager.getInstance().getLocaleOrNull(lang);
		if ( newLocale == null || !I18nModule.getEnabledLanguageKeys().contains(newLocale.toString())) {
			newLocale = I18nModule.getDefaultLocale();
		}
		setLocale(newLocale, true);
		// Initialize lang selection form
		initForm(ureq);
	}

	@Override
	public void lockResource(OLATResourceable resource) {
		//
	}
	
	@Override
	public void unlockResource() {
		//
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		UserSession usess = ureq.getUserSession();
		if(usess != null && usess.getRoles() != null && usess.getIdentity() != null) {
			SearchServiceUIFactory searchUIFactory = (SearchServiceUIFactory)CoreSpringFactory.getBean(SearchServiceUIFactory.class);
			searchController = searchUIFactory.createInputController(ureq, getWindowControl(), DisplayOption.STANDARD, mainForm);
			searchController.setResourceContextEnable(false);
			searchController.setDocumentType("type.contexthelp");
			flc.add("search_input", searchController.getFormItem());
		}
		
		// Add target languages without overlays
		I18nManager i18nMgr = I18nManager.getInstance();
		Collection<String> availableLangKeys = I18nModule.getEnabledLanguageKeys();
		String[] targetlangKeys = ArrayHelper.toArray(availableLangKeys);
		String[] targetLangValues = new String[targetlangKeys.length];
		for (int i = 0; i < targetlangKeys.length; i++) {
			String key = targetlangKeys[i];
			String name = i18nMgr.getLanguageTranslated(key, I18nModule.isOverlayEnabled());
			targetLangValues[i] = name;
		}
		ArrayHelper.sort(targetlangKeys, targetLangValues, false, true, false);
		// Build css classes for reference languages
		langSelection = uifactory.addDropdownSingleselect("contexthelp.langSelection", formLayout, targetlangKeys, targetLangValues, null);
		langSelection.addActionListener(FormEvent.ONCHANGE);						
		// Preselect language from URL
		langSelection.select(getLocale().toString(), true);
		flc.contextPut("lang", getLocale().toString());
		// Add window close link
		closeLink = FormUIFactory.getInstance().addFormLink("header.topnav.close", this.flc);
	}


	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#formInnerEvent(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.form.flexible.FormItem,
	 *      org.olat.core.gui.components.form.flexible.impl.FormEvent)
	 */
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == closeLink) {
			// close window (a html page which calls Window.close onLoad
			ureq.getDispatchResult().setResultingMediaResource(
					new RedirectMediaResource(StaticMediaDispatcher.createStaticURIFor("closewindow.html")));
			// release all resources and close window
			WindowBackOffice wbo = getWindowControl().getWindowBackOffice();
			Window w = wbo.getWindow();
			Windows.getWindows(ureq).deregisterWindow(w);
			wbo.dispose();

		} else if (source == langSelection) {
			String langKey = langSelection.getSelectedKey();
			Locale locale = I18nManager.getInstance().getLocaleOrDefault(langKey);
			setLocale(locale, true);
			this.flc.contextPut("lang", getLocale().toString());

			// notify my children
			LanguageChangedEvent mue = new LanguageChangedEvent(locale, ureq);
			ureq.getUserSession().getSingleUserEventCenter().fireEventToListenersOf(mue, CHANGE_LANG_RESOURCE);
			
		}		
	}


	@Override
	protected void formOK(UserRequest ureq) {
		// nothing to do
	}


	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose()
	 */
	@Override
	protected void doDispose() {
		if(searchController != null) {
			searchController.dispose();
		}
	}
}
