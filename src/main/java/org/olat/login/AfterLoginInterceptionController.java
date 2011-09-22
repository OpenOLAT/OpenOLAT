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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.login;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.creator.AutoCreator;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.wizard.WizardInfoController;
import org.olat.properties.Property;
import org.olat.properties.PropertyManager;

/**
 * Description: Presents running once controllers to the user right after login.
 * checks if they run already or if a timeout has passed by and they should run
 * again. 
 * Controller is defined in serviceconfig/org/olat/_spring/olatextconfig.xml ->
 * "fullWebApp.AfterLoginInterceptionControllerCreator" - bean.
 * 
 * the Controllers itself need to be appended with AfterLoginInterceptorManager.addAfterLoginControllerConfig().
 * 
 * Initial Date: 01.10.2009 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, www.frentix.com
 */
public class AfterLoginInterceptionController extends BasicController {

	private CloseableModalController cmc;
	private WizardInfoController wiz;
	private VelocityContainer vC;
	private Panel actualPanel;
	private Controller actCtrl;
	private List<Map<String, Object>> aftctrls;
	private int actualCtrNr;
	private List<Property> ctrlPropList;
	private PropertyManager pm;
	private boolean actualForceUser;
	// must match with keys in XML
	private static final String CONTROLLER_KEY = "controller";
	private static final String FORCEUSER_KEY = "forceUser";
	private static final String REDOTIMEOUT_KEY = "redoTimeout";
	private static final String I18NINTRO_KEY = "i18nIntro";

	private static final String PROPERTY_CAT = "afterLogin";

	public AfterLoginInterceptionController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		vC = createVelocityContainer("afterlogin");
		actualPanel = new Panel("actualPanel");
		AfterLoginInterceptionManager aLIM = AfterLoginInterceptionManager.getInstance();
		if (!aLIM.containsAnyController()) return;		
		List<Map<String, Object>> aftctrlsTmp = aLIM.getAfterLoginControllerList();
		aftctrls = (List<Map<String, Object>>) ((ArrayList<Map<String, Object>>) aftctrlsTmp).clone();
		// load all UserProps concerning afterlogin/runOnce workflow => only 1
		// db-call for all props
		pm = PropertyManager.getInstance();
		ctrlPropList = pm.listProperties(ureq.getIdentity(), null, null, null, PROPERTY_CAT, null);

		// loop over possible controllers and check if user already did it before
		// configured timeout
		int initialSize = aftctrls.size();
		int j = 0;
		for (int i = 0; i < initialSize; i++) {
			int correction = 0;
			Map<String, Object> ctrlMap = aftctrls.get(j);
			String ctrlName = ((AutoCreator) ctrlMap.get(CONTROLLER_KEY)).getClassName();
			// checking for recurring entries
			if (ctrlMap.containsKey(REDOTIMEOUT_KEY)) {
				// redo-timeout not yet over, so don't do again
				Long redoTimeout = Long.parseLong((String) ctrlMap.get(REDOTIMEOUT_KEY));
				if (((Calendar.getInstance().getTimeInMillis() / 1000) - redoTimeout) < getLastRunTimeForController(ctrlName)) {
					aftctrls.remove(ctrlMap);
					correction = -1;
				}
			} else { // check if run already for non-recurring entries
				if (getRunStateForController(ctrlName)) {
					aftctrls.remove(ctrlMap);
					correction = -1;
				}
			}
			j = i + correction + 1;
		}

		// break if nothing survived cleanup or invalid configuration
		if (aftctrls == null || aftctrls.size() == 0) { return; }

		wiz = new WizardInfoController(ureq, aftctrls.size());
		vC.put("wizard", wiz.getInitialComponent());
		vC.contextPut("ctrlCount", aftctrls.size());

		// get first Ctrl into Wizard
		putControllerToPanel(ureq, wControl, 0);
		vC.put("actualPanel", actualPanel);

		cmc = new CloseableModalController(getWindowControl(), translate("close"), vC, true, translate("runonce.title"));
		cmc.activate();
		listenTo(cmc);
	}

	private Long getLastRunTimeForController(String ctrlName) {
		for (Property prop : ctrlPropList) {
			if (prop.getName().equals(ctrlName)) { return new Long(prop.getLastModified().getTime() / 1000); }
		}
		return new Long(0);
	}

	private boolean getRunStateForController(String ctrlName) {
		for (Property prop : ctrlPropList) {
			if (prop.getName().equals(ctrlName)) { return Boolean.parseBoolean(prop.getStringValue()); }
		}
		return false;
	}

	private void saveOrUpdatePropertyForController(UserRequest ureq, String ctrlName) {
		for (Property prop : ctrlPropList) {
			if (prop.getName().equals(ctrlName)) {
				prop.setStringValue(Boolean.TRUE.toString());
				pm.updateProperty(prop);
				return;
			}
		}
		Property prop = pm.createPropertyInstance(ureq.getIdentity(), null, null, PROPERTY_CAT, ctrlName, null, null, "true", null);
		pm.saveProperty(prop);
	}

	/**
	 * refreshes modalPanel with n-th controller found in config sets actual
	 * controller, controller-nr and force status
	 * 
	 * @param ureq
	 * @param wControl
	 * @param ctrNr
	 */
	private void putControllerToPanel(UserRequest ureq, WindowControl wControl, int ctrNr) {
		if (aftctrls.get(ctrNr) == null) return;
		actualCtrNr = ctrNr;
		wiz.setCurStep(ctrNr + 1);
		Map<String, Object> mapEntry = aftctrls.get(ctrNr);
		AutoCreator ctrCreator = null;
		if (mapEntry.containsKey(CONTROLLER_KEY)) {
			ctrCreator = (AutoCreator) mapEntry.get(CONTROLLER_KEY);
		} else {
			throw new RuntimeException("at least a controller must be defined");
		}
		actualForceUser = false;
		if (mapEntry.containsKey(FORCEUSER_KEY)) {
			actualForceUser = Boolean.valueOf(mapEntry.get(FORCEUSER_KEY).toString());
		}

		actCtrl = ctrCreator.createController(ureq, wControl);
		listenTo(actCtrl);
		if (mapEntry.containsKey(I18NINTRO_KEY)) {
			String[] introComb = ((String) mapEntry.get(I18NINTRO_KEY)).split(":");
			vC.contextPut("introPkg", introComb[0]);
			vC.contextPut("introKey", introComb[1]);
		}

		actualPanel.setContent(actCtrl.getInitialComponent());
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose()
	 */
	@Override
	protected void doDispose() {
		// nothing to dispose as we listen to actCtrl
		aftctrls = null;
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.control.Event)
	 */
	@Override
	@SuppressWarnings("unused")
	protected void event(UserRequest ureq, Component source, Event event) {
	// no such events
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == cmc && event == cmc.CLOSE_MODAL_EVENT && actualForceUser) {
			// show warning if this is a task, where user is forced to do it
			showWarning("runonce.forced");
			cmc.activate();
		}
		// controllers workflow finished. (controller should send a done-event!)
		if (source == actCtrl && event == Event.DONE_EVENT) {

			// save state of this controller
			String ctrlName = actCtrl.getClass().getName();
			saveOrUpdatePropertyForController(ureq, ctrlName);

			// go to next
			if ((actualCtrNr + 1) < aftctrls.size()) {
				putControllerToPanel(ureq, getWindowControl(), actualCtrNr + 1);
			} else {
				cmc.deactivate();
			}
		}
	}

}
