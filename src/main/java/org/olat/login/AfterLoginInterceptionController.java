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
package org.olat.login;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
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
//import org.olat.core.gui.control.creator.AutoCreator;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.wizard.WizardInfoController;
import org.olat.properties.Property;
import org.olat.properties.PropertyManager;
import org.springframework.beans.factory.annotation.Autowired;

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
	private boolean actualForceUser;
	// must match with keys in XML
	private static final String CONTROLLER_KEY = "controller";
	private static final String CONTROLLER = "controller-instance";
	private static final String FORCEUSER_KEY = "forceUser";
	private static final String REDOTIMEOUT_KEY = "redoTimeout";
	private static final String I18NINTRO_KEY = "i18nIntro"; 
	protected static final String ORDER_KEY = "order";
	
	private static final String PROPERTY_CAT = "afterLogin";
	
	@Autowired
	private PropertyManager pm;
	@Autowired
	private AfterLoginInterceptionManager aLIM;

	public AfterLoginInterceptionController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		vC = createVelocityContainer("afterlogin");
		actualPanel = new Panel("actualPanel");
		if (!aLIM.containsAnyController()) {
			dispose();
			return;		
		}
		
		List<Map<String, Object>> aftctrlsTmp = aLIM.getAfterLoginControllerList();
		aftctrls = (List<Map<String, Object>>) ((ArrayList<Map<String, Object>>) aftctrlsTmp).clone();
		// sort controllers according to config
		aftctrls = AfterLoginInterceptionManager.sortControllerListByOrder(aftctrls);
		
		// load all UserProps concerning afterlogin/runOnce workflow => only 1
		// db-call for all props
		ctrlPropList = pm.listProperties(ureq.getIdentity(), null, null, null, PROPERTY_CAT, null);

		// loop over possible controllers and check if user already did it before configured timeout
		// instantiate controllers in advance to allow back
		for(Iterator<Map<String,Object>> mapInfosIt=aftctrls.iterator(); mapInfosIt.hasNext(); ) {
			Map<String,Object> mapInfos = mapInfosIt.next();
			if (mapInfos.containsKey(CONTROLLER_KEY)) {
				ControllerCreator creator = (ControllerCreator) mapInfos.get(CONTROLLER_KEY);
				Controller ctrl = creator.createController(ureq, wControl);
				//no controller to show
				if (ctrl == null ){
					mapInfosIt.remove();
					continue;
				}
				
				String ctrlName = ctrl.getClass().getName();

				// check if the time between to appearance is ago
				if (mapInfos.containsKey(REDOTIMEOUT_KEY)) {
					// redo-timeout not yet over, so don't do again
					Long redoTimeout = Long.parseLong((String) mapInfos.get(REDOTIMEOUT_KEY));
					if (((Calendar.getInstance().getTimeInMillis() / 1000) - redoTimeout) < getLastRunTimeForController(ctrlName)) {
						ctrl.dispose();
						mapInfosIt.remove();
						continue;
					}
				// check if run already for non-recurring entries
				} else if (getRunStateForController(ctrlName)) {
					ctrl.dispose();
					mapInfosIt.remove();
					continue;
				}

				// check if interception criteria is needed
				if(ctrl instanceof SupportsAfterLoginInterceptor) {
					SupportsAfterLoginInterceptor loginInterceptor = (SupportsAfterLoginInterceptor)ctrl;
					if(loginInterceptor.isInterceptionRequired(ureq)) {
						mapInfos.put(CONTROLLER, ctrl);
					} else {
						ctrl.dispose();
						mapInfosIt.remove();
					}
				} else {
					mapInfos.put(CONTROLLER, ctrl);
				}
			} else {
				mapInfosIt.remove();
			}
		}
		
		if (aftctrls.isEmpty()) {
			fireEvent(ureq, Event.DONE_EVENT);
			dispose();
			return;
		}
		
		wiz = new WizardInfoController(ureq, aftctrls.size());
		vC.put("wizard", wiz.getInitialComponent());
		vC.contextPut("ctrlCount", aftctrls.size());
		listenTo(wiz);

		// get first Ctrl into Wizard
		putControllerToPanel(0);
		vC.put("actualPanel", actualPanel);

		cmc = new CloseableModalController(getWindowControl(), translate("close"), vC, true, translate("runonce.title"), false);	
		cmc.activate();
		listenTo(cmc);
		// if controller could not be created, go to the next one ore close the wizzard 
		if (actCtrl == null) {
			activateNextOrCloseModal(ureq);
		}
	}

	private Long getLastRunTimeForController(String ctrlName) {
		for (Property prop : ctrlPropList) {
			if (prop.getName().equals(ctrlName)) { return new Long(prop.getLastModified().getTime() / 1000); }
		}
		return new Long(0);
	}

	private boolean getRunStateForController(String ctrlName) {
		for (Property prop : ctrlPropList) {
			if (prop.getName().equals(ctrlName)) { 
				return Boolean.parseBoolean(prop.getStringValue());
			}
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
	private void putControllerToPanel(int ctrNr) {
		if (aftctrls.get(ctrNr) == null) return;
		actualCtrNr = ctrNr;
		wiz.setCurStep(ctrNr + 1);
		Map<String, Object> mapEntry = aftctrls.get(ctrNr);
				
		actualForceUser = false;
		if (mapEntry.containsKey(FORCEUSER_KEY)) {
			actualForceUser = Boolean.valueOf(mapEntry.get(FORCEUSER_KEY).toString());
		}

		removeAsListenerAndDispose(actCtrl);
		actCtrl = (Controller)mapEntry.get(CONTROLLER);
		if (actCtrl == null) {
			return;
		}
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
		removeAsListenerAndDispose(actCtrl);
		aftctrls = null;
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.control.Event)
	 */
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
	// no such events
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == cmc && event == CloseableModalController.CLOSE_MODAL_EVENT && actualForceUser) {
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
			activateNextOrCloseModal(ureq);
		} else if (source == actCtrl && event == Event.CANCELLED_EVENT && !actualForceUser){
			// do not persist state. controller sent an cancel event
			activateNextOrCloseModal(ureq);
		}
	}
	
	/**
	 * failsafe continuing
	 */
	private void activateNextOrCloseModal(UserRequest ureq){
		if ((actualCtrNr + 1) < aftctrls.size()) {
			putControllerToPanel(actualCtrNr + 1);
		} else {
			removeAsListenerAndDispose(actCtrl);
			cmc.deactivate();
			fireEvent(ureq, Event.DONE_EVENT);
		}
	}
}