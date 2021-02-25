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
*/

package org.olat.user;

import java.util.Set;

import org.olat.core.CoreSpringFactory;
import org.olat.core.dispatcher.DispatcherModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.WindowManager;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.media.RedirectMediaResource;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.id.context.HistoryManager;
import org.olat.core.id.context.HistoryModule;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.i18n.I18nModule;
import org.olat.core.util.prefs.Preferences;
import org.olat.core.util.prefs.PreferencesFactory;
import org.olat.core.util.session.UserSessionManager;
import org.olat.properties.PropertyManager;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * Initial Date: Apr 27, 2004
 * 
 * @author gnaegi Comment: This controller allows the user to edit the
 *         preferences of any subject. Make sure you check for security when
 *         creating this controller since this controller does not have any
 *         security checks whatsoever.
 */

public class ChangePrefsController extends BasicController {
	
	private VelocityContainer myContent;
	private Controller generalPrefsCtr;
	private Controller specialPrefsCtr;
	private Controller toolsPrefsCtr;
	private Controller resetCtr;
	
	/**
	 * Constructor for the change user preferences controller
	 * 
	 * @param ureq The user request
	 * @param wControl The current window controller
	 * @param changeableIdentity The subject whose profile should be changed
	 */
	public ChangePrefsController(UserRequest ureq, WindowControl wControl, Identity changeableIdentity) {
		super(ureq, wControl);

		myContent = createVelocityContainer("prefs");

		generalPrefsCtr = new PreferencesFormController(ureq, wControl, changeableIdentity);
		listenTo(generalPrefsCtr);
		
		specialPrefsCtr = new SpecialPrefsForm(ureq, wControl, changeableIdentity);
		listenTo(specialPrefsCtr);
		
		resetCtr = new UserPrefsResetForm(ureq, wControl, changeableIdentity);
		listenTo(resetCtr);
		
		toolsPrefsCtr = new ToolsPrefsController(ureq, wControl, changeableIdentity);
		listenTo(toolsPrefsCtr);
		
		myContent.put("general", generalPrefsCtr.getInitialComponent());
		myContent.put("special", specialPrefsCtr.getInitialComponent());
		myContent.put("tools", toolsPrefsCtr.getInitialComponent());
		myContent.put("reset", resetCtr.getInitialComponent());
		
		putInitialPanel(myContent);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.control.Event)
	 */
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == myContent) {
			if (event.getCommand().equals("exeBack")) {
				fireEvent(ureq, Event.CANCELLED_EVENT);
			}
		} 
	}
	
	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == generalPrefsCtr) {
			if (event == Event.DONE_EVENT) {
				fireEvent(ureq, Event.DONE_EVENT);				
			} else if (event == Event.CANCELLED_EVENT) {
				fireEvent(ureq, Event.CANCELLED_EVENT);
			}
		}
		
	} 
	
	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	@Override
	protected void doDispose() {
		//
	}
}

/**
 * 
 * Description:<br>
 * The special prefs form is used to configure the delivery mode. Users can
 * choose between web 1.0, web 2.0 and web 2.a. The configuration for web 1.0
 * and web 2.0 is only available for user managers. Normal users can only
 * enable/disable the web 2.a mode
 * 
 * <P>
 * Initial Date: 09.11.2010 <br>
 * 
 * @author gnaegi
 */
class SpecialPrefsForm extends FormBasicController {

	private Identity tobeChangedIdentity;
	private Preferences prefs;
	
	private SingleSelection resumeElement;
	private TextElement landingPageEl;
	private String[] resumeKeys, resumeValues;

	@Autowired
	private HistoryModule historyModule;
	@Autowired
	private UserSessionManager sessionManager;
	
	public SpecialPrefsForm(final UserRequest ureq, final WindowControl wControl, final Identity changeableIdentity) {
		super(ureq, wControl);
		tobeChangedIdentity = changeableIdentity;
		
		// OLAT-6429 load GUI prefs from user session for myself, load it from factory for other users (as user manager)
		if (ureq.getIdentity().equalsByPersistableKey(tobeChangedIdentity)) {
			prefs = ureq.getUserSession().getGuiPreferences();
		} else {
			prefs = PreferencesFactory.getInstance().getPreferencesFor(tobeChangedIdentity, false);			
		}
		
		resumeKeys = new String[]{"none", "auto", "ondemand"}; 
		resumeValues = new String[] {
				translate("resume.none"),
				translate("resume.auto"),
				translate("resume.ondemand"),
		};

		initForm(ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// OLAT-6429 don't change another users GUI prefs when he is logged in 
		if (!ureq.getIdentity().equalsByPersistableKey(tobeChangedIdentity)) {
			if (sessionManager.isSignedOnIdentity(tobeChangedIdentity.getKey())) {
				String fullName = CoreSpringFactory.getImpl(UserManager.class).getUserDisplayName(tobeChangedIdentity);
				showError("error.user.logged.in",fullName);
				return;
			}
		}
		
		if(resumeElement != null) {
			prefs.put(WindowManager.class, "resume-prefs", resumeElement.getSelectedKey());
		}
		String landingPage = landingPageEl.isVisible() ? landingPageEl.getValue() : "";
		prefs.put(WindowManager.class, "landing-page", landingPage);
		
		if (ureq.getIdentity().equalsByPersistableKey(tobeChangedIdentity)) {
			showInfo("preferences.successful");
		}
		
		// finally, save preferences
		prefs.save();
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(resumeElement == source) {
			if(resumeElement.isOneSelected()) {
				landingPageEl.setVisible(!resumeElement.getSelectedKey().equals("auto"));
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		update();
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("title.prefs.special");
		setFormContextHelp("Configuration#_specifics");

		if(historyModule.isResumeEnabled()) {
			resumeElement = uifactory.addRadiosVertical("resume", "resume.label", formLayout, resumeKeys, resumeValues);
			resumeElement.setElementCssClass("o_sel_home_settings_resume");
			resumeElement.addActionListener(FormEvent.ONCHANGE);
		}
		
		landingPageEl = uifactory.addTextElement("landingpages", "landing.pages", 256, "", formLayout);
		landingPageEl.setElementCssClass("o_sel_home_settings_landing_page");
		
		update();
		
		final FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("button_layout", getTranslator());
		formLayout.add(buttonLayout);
		buttonLayout.setElementCssClass("o_sel_home_settings_gui_buttons");
		uifactory.addFormSubmitButton("submit", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
	}

	private void update() {
		boolean landingPageVisible = true;
		if(resumeElement != null) {
			String resumePrefs = (String)prefs.get(WindowManager.class, "resume-prefs");
			if(StringHelper.containsNonWhitespace(resumePrefs)) {
				resumeElement.select(resumePrefs, true);
			} else {
				String defaultSetting = historyModule.getResumeDefaultSetting();
				try {
					resumeElement.select(defaultSetting, true);
				} catch (Exception e) {
					logError("Unavailable setting for resume function: " + defaultSetting, e);
				}
			}
			
			if(resumeElement.isOneSelected()) {
				landingPageVisible = !resumeElement.getSelectedKey().equals("auto");
			}
		}

		String landingPage = (String)prefs.get(WindowManager.class, "landing-page");
		landingPageEl.setValue(landingPage);
		landingPageEl.setVisible(landingPageVisible);
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
}

/**
 * Controller to reset the users GUI prefs and other preferences
 */
class UserPrefsResetForm extends FormBasicController {

	private Identity tobeChangedIdentity;
	private MultipleSelectionElement resetElements;
	private String[] keys, values;

	@Autowired
	private I18nModule i18nModule;
	@Autowired
	private HistoryManager historyManager;
	@Autowired
	private UserSessionManager sessionManager;
	
	public UserPrefsResetForm(UserRequest ureq, WindowControl wControl, Identity changeableIdentity) {
		super(ureq, wControl);
		tobeChangedIdentity = changeableIdentity;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("reset.title");
		setFormDescription("reset.desc");
		
		keys = new String[]{"guiprefs", "sysprefs", "resume"};
		values = new String[] {translate("reset.elements.guiprefs"), translate("reset.elements.sysprefs"), translate("reset.elements.resume")};
		
		resetElements = uifactory.addCheckboxesVertical("prefs", "reset.elements", formLayout, keys, values, 1);
		resetElements.setElementCssClass("o_sel_home_settings_reset_sysprefs");
		resetElements.setMandatory(true);
		
		final FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("button_layout", getTranslator());
		formLayout.add(buttonLayout);
		buttonLayout.setElementCssClass("o_sel_home_settings_reset_sysprefs_buttons");
		uifactory.addFormSubmitButton("reset.submit", buttonLayout);
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		
		resetElements.clearError();
		if(!resetElements.isAtLeastSelected(1)) {
			resetElements.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		return allOk & super.validateFormLogic(ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if (resetElements.isAtLeastSelected(1)) {
			// Log out user first if logged in
			boolean logout = false;
			Set<UserSession> sessions = sessionManager.getAuthenticatedUserSessions();
			for (UserSession session : sessions) {
				Identity ident = session.getIdentity();
				if (ident != null && tobeChangedIdentity.equalsByPersistableKey(ident)) {
					sessionManager.signOffAndClear(session);
					logout = true;
					break;
				}
			}
			// Delete gui prefs
			if (resetElements.isSelected(0)) {
				PropertyManager pm = PropertyManager.getInstance();
				pm.deleteProperties(tobeChangedIdentity, null, null, null, "v2guipreferences");
			}
			// Reset preferences
			if (resetElements.isSelected(1)) {
				UserManager um = UserManager.getInstance();
				User user = um.loadUserByKey(tobeChangedIdentity.getUser().getKey());
				org.olat.core.id.Preferences preferences = user.getPreferences();
				preferences.setNotificationInterval(null);
				preferences.setPresenceMessagesPublic(false);
				preferences.setReceiveRealMail(null);
				um.updateUser(user);
				PropertyManager pm = PropertyManager.getInstance();
				pm.deleteProperties(tobeChangedIdentity, null, null, null, "charset");
			}
			// Reset history
			if (resetElements.isSelected(2)) {
				historyManager.deleteHistory(tobeChangedIdentity);
			}
			// reset form buttons
			resetElements.uncheckAll();
			
			if(logout) {
				//if logout, need a redirect to the login page
				String lang = i18nModule.getLocaleKey(ureq.getLocale());
				ureq.getDispatchResult().setResultingMediaResource(
						new RedirectMediaResource(WebappHelper.getServletContextPath() + DispatcherModule.getPathDefault() + "?lang=" + lang + "&logout=true"));
			}
		}
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
}
