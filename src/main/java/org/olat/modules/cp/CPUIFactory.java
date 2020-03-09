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
package org.olat.modules.cp;

import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsPreviewController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.iframe.DeliveryOptions;
import org.olat.core.gui.control.generic.layout.MainLayout3ColumnsController;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.vfs.VFSContainer;

/**
 * Description:<br>
 * The CPUIFactory provides methods to create content packaging display
 * controllers for various setups.
 * 
 * <P>
 * Initial Date: 08.10.2007 <br>
 * 
 * @author Felix Jost, http://www.goodsolutions.ch
 * @author Florian Gnägi, http://www.frentix.com
 */
public class CPUIFactory {
	private static CPUIFactory INSTANCE = new CPUIFactory();
	
	private CPUIFactory() {
		// singleton
	}
	
	public static CPUIFactory getInstance() {
		return INSTANCE;
	}
	
	/**
	 * Creates a controller that displays only the content part of a content
	 * packaging. Using the public method of the CPDisplayController one has
	 * access to the corresponding menu tree.
	 * <p>
	 * Use this to embedd a CP something where the layout in handled by another
	 * controller, e.b. with in course
	 * 
	 * @param ureq
	 * @param wControl
	 * @param rootContainer The VFS root container where the CP is found on disk
	 * @param activateFirstPage true to automatically activate the first node with
	 *          content
	 * @param initialUri can be NULL, will use first page then
	 * @param cpAssessmentProvider 
	 * @return a CPDisplayController
	 */
	public CPDisplayController createContentOnlyCPDisplayController(UserRequest ureq, WindowControl wControl, VFSContainer rootContainer,
			boolean activateFirstPage, boolean showNavigation, DeliveryOptions deliveryOptions,
			String initialUri, OLATResourceable ores, String identPrefix, boolean preview, CPAssessmentProvider cpAssessmentProvider) {
		return new CPDisplayController(ureq, wControl, rootContainer, false, showNavigation, activateFirstPage, true, deliveryOptions,
				initialUri, ores, identPrefix, preview, cpAssessmentProvider);
	}
	
	/**
	 * Creates a main layout controller. The layout uses one or two columns
	 * depending the the showMenu flag.
	 * <p>
	 * Use this where you have no main layout present, e.g. in a pop up in a
	 * stand-alone view
	 * 
	 * @param ureq
	 * @param wControl
	 * @param rootContainer The VFS root container where the CP is found on disk
	 * @param showMenu true to display the menu, false to hide the menu
	 * @param cpAssessmentProvider 
	 * @return A main layout controller
	 */
	public MainLayout3ColumnsController createMainLayoutController(UserRequest ureq, WindowControl wControl, VFSContainer rootContainer,
			boolean showMenu, DeliveryOptions deliveryOptions, CPAssessmentProvider cpAssessmentProvider) {
		CPDisplayController cpCtr = new CPDisplayController(ureq, wControl, rootContainer, showMenu, true, true, true,
				deliveryOptions, null, null, "", false, cpAssessmentProvider);
		MainLayout3ColumnsController layoutCtr = new LayoutMain3ColsController(ureq, wControl, cpCtr.getMenuComponent(), cpCtr.getInitialComponent(), rootContainer.getName());
		layoutCtr.addDisposableChildController(cpCtr); // cascade disposing requests
		return layoutCtr;
	}

	/**
	 * Creates a main layout controller that can be activated. It provides a
	 * "close preview" link that automatically deactivates this controller form
	 * the GUI stack
	 * <p>
	 * Use this when you want the user to be able to preview a CP
	 * 
	 * @param ureq
	 * @param wControl
	 * @param rootContainer The VFS root container where the CP is found on disk
	 * @param showMenu true to display the menu, false to hide the menu
	 * @return A main layout preview controller
	 */
	public LayoutMain3ColsPreviewController createMainLayoutPreviewController(UserRequest ureq, WindowControl wControl, VFSContainer rootContainer,
			boolean showMenu, DeliveryOptions deliveryOptions) {
		CPDisplayController cpCtr = new CPDisplayController(ureq, wControl, rootContainer, showMenu, true, true, true,
				deliveryOptions, null, null, "", false, DryRunAssessmentProvider.create());
		LayoutMain3ColsPreviewController layoutCtr = new LayoutMain3ColsPreviewController(ureq, wControl, cpCtr.getMenuComponent(), cpCtr.getInitialComponent(), rootContainer.getName());
		layoutCtr.addDisposableChildController(cpCtr); // cascade disposing requests
		return layoutCtr;
	}
	
	/**
	 * Creates a main layout controller that can be activated. It provides a
	 * "close preview" link that automatically deactivates this controller form
	 * the GUI stack
	 * <p>
	 * Use this when you want the user to be able to preview a CP
	 * 
	 * @param ureq
	 * @param wControl
	 * @param rootContainer The VFS root container where the CP is found on disk
	 * @param showMenu true to display the menu, false to hide the menu
	 * @return A main layout preview controller
	 */
	public LayoutMain3ColsController createMainLayoutPreviewController_v2(UserRequest ureq, WindowControl wControl, VFSContainer rootContainer,
			boolean showMenu, DeliveryOptions deliveryOptions) {
		CPDisplayController cpCtr = new CPDisplayController(ureq, wControl, rootContainer, showMenu, true, true, true,
				deliveryOptions, null, null, "", false, DryRunAssessmentProvider.create());
		LayoutMain3ColsController layoutCtr = new LayoutMain3ColsController(ureq, wControl, cpCtr.getMenuComponent(), cpCtr.getInitialComponent(), rootContainer.getName());
		layoutCtr.addDisposableChildController(cpCtr); // cascade disposing requests
		layoutCtr.addCssClassToMain("o_preview");
		return layoutCtr;
	}
		
}
