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
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */

package org.olat.user;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.WindowManager;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.core.util.UserSession;

import org.olat.core.util.prefs.Preferences;
import org.olat.core.util.prefs.PreferencesFactory;


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
		
		myContent.put("general", generalPrefsCtr.getInitialComponent());
		myContent.put("special", specialPrefsCtr.getInitialComponent());
		
		putInitialPanel(myContent);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == myContent) {
			if (event.getCommand().equals("exeBack")) {
				fireEvent(ureq, Event.CANCELLED_EVENT);
			}
		} 
	}

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
	private MultipleSelectionElement prefsElement;
	private String[] keys, values;
	private boolean useAjaxCheckbox = false;
	
	public SpecialPrefsForm(final UserRequest ureq, final WindowControl wControl, final Identity changeableIdentity) {
		super(ureq, wControl);
		tobeChangedIdentity = changeableIdentity;
		// OLAT-6429 load GUI prefs from user session for myself and from factory for other users (as user manager)
		if (ureq.getIdentity().equalsByPersistableKey(tobeChangedIdentity)) {
			prefs = ureq.getUserSession().getGuiPreferences();
		} else {
			prefs = PreferencesFactory.getInstance().getPreferencesFor(tobeChangedIdentity, false);			
		}
		// The ajax configuration is only for user manager (technical stuff)
		useAjaxCheckbox = ureq.getUserSession().getRoles().isUserManager();
		// initialize checkbox keys depending on useAjaxCheckbox flag
		if (useAjaxCheckbox) {
			keys = new String[]{"ajax", "web2a"}; 
			values = new String[] {
					translate("ajaxon.label"),
					translate("accessibility.web2aMode.label")
			};			
		} else {
			keys = new String[]{"web2a"}; 
			values = new String[] {
					translate("accessibility.web2aMode.label")
			};
		}
		
		initForm(ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// OLAT-6429 don't change another users GUI prefs when he is logged in 
		if (!ureq.getIdentity().equalsByPersistableKey(tobeChangedIdentity)) {
			if (UserSession.getSignedOnIdentity(tobeChangedIdentity.getName()) != null) {
				showError("error.user.logged.in", tobeChangedIdentity.getName());
				prefsElement.reset();
				return;
			}
		}

		if (useAjaxCheckbox) {
			prefs.putAndSave(WindowManager.class, "ajax-beta-on", prefsElement.getSelectedKeys().contains("ajax"));
		}
		prefs.putAndSave(WindowManager.class, "web2a-beta-on", prefsElement.getSelectedKeys().contains("web2a"));
		if (ureq.getIdentity().equalsByPersistableKey(tobeChangedIdentity)) {
			showInfo("preferences.successful");
		}
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		update();
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
		setFormTitle("title.prefs.special");
		setFormContextHelp(this.getClass().getPackage().getName(), "home-prefs-special.html", "help.hover.home.prefs.special");
		
		prefsElement = uifactory.addCheckboxesVertical("prefs", "title.prefs.accessibility", formLayout, keys, values, null, 1);

		
		update();
		
		final FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("button_layout", getTranslator());
		formLayout.add(buttonLayout);
		uifactory.addFormSubmitButton("submit", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
	}

	private void update() {
		Boolean web2a = (Boolean) prefs.get(WindowManager.class, "web2a-beta-on");
		Boolean ajax  = (Boolean) prefs.get(WindowManager.class, "ajax-beta-on");
		if (useAjaxCheckbox) {
			prefsElement.select("ajax", ajax == null ? true: ajax.booleanValue());
		}
		prefsElement.select("web2a", web2a == null ? false: web2a.booleanValue());
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
}

