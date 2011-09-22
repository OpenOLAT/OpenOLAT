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
 * Copyright (c) 2005-2006 by JGS goodsolutions GmbH, Switzerland<br>
 * http://www.goodsolutions.ch All rights reserved.
 * <p>
 */
package org.olat;

import java.util.HashMap;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.Windows;
import org.olat.core.gui.components.Window;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.DTab;
import org.olat.core.gui.control.generic.dtabs.DTabs;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.ContextEntryControllerCreator;
import org.olat.core.logging.LogDelegator;
import org.olat.core.util.resource.OresHelper;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;

/**
 * Description:<br>
 * input: e.g. [repoentry:123] or [repoentry:123][CourseNode:456] or ...
 * 
 * 
 * <P>
 * Initial Date: 16.06.2006 <br>
 * 
 * @author Felix Jost
 */
public class NewControllerFactory extends LogDelegator {
	private static NewControllerFactory INSTANCE = new NewControllerFactory();
	// map of controller creators, setted by Spring configuration
	private Map<String, ContextEntryControllerCreator> contextEntryControllerCreators = new HashMap<String, ContextEntryControllerCreator>();

	/**
	 * Get an instance of the new controller factory
	 * 
	 * @return
	 */
	public static NewControllerFactory getInstance() {
		return INSTANCE;
	}

	/**
	 * Singleton constructor
	 */
	private NewControllerFactory() {
	//
	}

	/**
	 * Add a context entry controller creator for a specific key. This is used to
	 * add new creators at runtime, e.g. from a self contained module. It is
	 * allowed to overwrite existing ContextEntryControllerCreator. Use the
	 * canLaunch() method to check if for a certain key something is already
	 * defined.
	 * 
	 * @param key
	 * @param controllerCreator
	 */
	public synchronized void addContextEntryControllerCreator(String key, ContextEntryControllerCreator controllerCreator) {
		ContextEntryControllerCreator oldCreator = contextEntryControllerCreators.get(key);
		contextEntryControllerCreators.put(key, controllerCreator);
		// Add config logging to console
		logInfo("Adding context entry controller creator for key::" + key + " and value::" + controllerCreator.getClass().getCanonicalName() 
				+ (oldCreator == null ? "" : " replaceing existing controller creator ::" + oldCreator.getClass().getCanonicalName()), null);
	}

	/**
	 * Check if a context entry controller creator is available for the given key
	 * 
	 * @param key
	 * @return true: key is known; false: key can not be used
	 */
	public boolean canLaunch(String key) {
		return contextEntryControllerCreators.containsKey(key);
	}

	/**
	 * Check first context entry can be launched
	 * a further check is mostly not possible, as it gets validated through the BC-stack while building the controller-chain
	 * 
	 * return true: if this will be launchable at least for the first step.
	 */
	public boolean validateCEWithContextControllerCreator(final UserRequest ureq, final WindowControl wControl, ContextEntry ce){
		String firstType = ce.getOLATResourceable().getResourceableTypeName();
		if (canLaunch(firstType)){
			return contextEntryControllerCreators.get(firstType).validateContextEntryAndShowError(ce, ureq, wControl);
		}
		return false;
	}

	/**
	 * Launch a controller in a tab or site in the given window from a user
	 * request url
	 * 
	 * @param ureq
	 * @param wControl
	 */
	public void launch(UserRequest ureq, WindowControl wControl) {
		BusinessControl bc = wControl.getBusinessControl();
		ContextEntry mainCe = bc.popLauncherContextEntry();
		OLATResourceable ores = mainCe.getOLATResourceable();

		// Check for RepositoryEntry resource
		boolean ceConsumed = false;
		if (ores.getResourceableTypeName().equals(OresHelper.calculateTypeName(RepositoryEntry.class))) {
			// It is a repository-entry => get OLATResourceable from RepositoryEntry
			RepositoryManager repom = RepositoryManager.getInstance();
			RepositoryEntry re = repom.lookupRepositoryEntry(ores.getResourceableId());
			if (re != null){
				ores = re.getOlatResource();
				ceConsumed = true;
			}
		}

		// was brasato:: DTabs dts = wControl.getDTabs();
		Window window = Windows.getWindows(ureq.getUserSession()).getWindow(ureq);

		if (window == null) {
			logDebug("Found no window for jumpin => take WindowBackOffice", null);
			window = wControl.getWindowBackOffice().getWindow();
		}
		DTabs dts = (DTabs) window.getAttribute("DTabs");
		DTab dt = dts.getDTab(ores);
		if (dt != null) {
			// tab already open => close it
			dts.removeDTab(dt);// disposes also dt and controllers
		}

		String firstType = mainCe.getOLATResourceable().getResourceableTypeName();
		// String firstTypeId = ClassToId.getInstance().lookup() BusinessGroup
		ContextEntryControllerCreator typeHandler = contextEntryControllerCreators.get(firstType);
		if (typeHandler == null) {
			logWarn("Cannot found an handler for context entry: " + mainCe, null);
			return;//simply return and don't throw a red screen
		}
		if (!typeHandler.validateContextEntryAndShowError(mainCe, ureq, wControl)){
			//simply return and don't throw a red screen
			return;
		} 
		
		String siteClassName = typeHandler.getSiteClassName(mainCe);
		// open in existing site
		if (siteClassName != null) {
			// use special activation key to trigger the activate method
			String viewIdentifyer = null;
			if (bc.hasContextEntry()) {
				ContextEntry subContext = bc.popLauncherContextEntry();
				if (subContext != null) {
					OLATResourceable subResource = subContext.getOLATResourceable();
					if (subResource != null) {
						viewIdentifyer = subResource.getResourceableTypeName();
						if (subResource.getResourceableId() != null) {
							// add resource instance id if available. The ':' is a common
							// separator in the activatable interface
							viewIdentifyer = viewIdentifyer + ":" + subResource.getResourceableId();
						}
					}
				}
			} else if (!ceConsumed) {
				//the olatresourceable is not in a dynamic tab but in a fix one
				if(ores != null) {
					viewIdentifyer = ores.getResourceableTypeName();
					if (ores.getResourceableId() != null) {
						// add resource instance id if available. The ':' is a common
						// separator in the activatable interface
						viewIdentifyer = viewIdentifyer + ":" + ores.getResourceableId();
					}
				}
			}
			dts.activateStatic(ureq, siteClassName, viewIdentifyer);
		} else {
			// or create new tab
			String tabName = typeHandler.getTabName(mainCe);
			// create and add Tab
			dt = dts.createDTab(ores, tabName);
			if (dt == null) {
				// tabs are full: TODO
				// user error message is generated in BaseFullWebappController, nothing
				// to do here
			} else {

				WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(bc, dt.getWindowControl());
				Controller launchC = typeHandler.createController(mainCe, ureq, bwControl);

				dt.setController(launchC);
				dts.addDTab(dt);
			}

			dts.activate(ureq, dt, null); // null: do not activate to a certain view
		}
	}

}
