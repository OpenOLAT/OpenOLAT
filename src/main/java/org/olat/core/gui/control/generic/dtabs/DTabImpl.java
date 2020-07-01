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

package org.olat.core.gui.control.generic.dtabs;

import org.olat.core.gui.components.htmlheader.jscss.CustomCSS;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.layout.MainLayoutController;
import org.olat.core.gui.control.guistack.GuiStack;
import org.olat.core.gui.control.navigation.DefaultNavElement;
import org.olat.core.gui.control.navigation.NavElement;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.StackedBusinessControl;
import org.olat.core.util.resource.OresHelper;
import org.olat.repository.ui.RepositoyUIFactory;

/**
 * Description: <br>
 * 
 * @author Felix Jost
 */
public class DTabImpl implements DTab {
	
	private final OLATResourceable ores;
	private final OLATResourceable initialOres;
	private final Controller controller;
	private GuiStack guiStackHandle;
	private final String title;
	private String description;
	private final WindowControl wControl;
	private final NavElement navElement;

	/**
	 * @param ores
	 * @param title
	 * @param wControl
	 */
	public DTabImpl(OLATResourceable ores, OLATResourceable initialOres, String title, Controller controller, WindowControl wOrigControl) {
		this.ores = ores;
		this.title = title;
		this.controller = controller;
		this.initialOres = initialOres;
		
		//Root the JumpInPath - typically all resources are opened in tabs
		StackedBusinessControl businessControl = new StackedBusinessControl(null, wOrigControl.getBusinessControl());
		wControl = BusinessControlFactory.getInstance().createBusinessWindowControl(businessControl, wOrigControl);

		String iconCSSClass = RepositoyUIFactory.getIconCssClass(ores.getResourceableTypeName());
		navElement = new DefaultNavElement(OresHelper.toBusinessPath(ores), title, title, iconCSSClass);
	}

	/**
	 * [used by velocity]
	 * @return the navigation element for this dtab
	 */
	@Override
	public NavElement getNavElement() {
		return navElement;
	}
	
	/**
	 * @return the controller
	 */
	@Override
	public Controller getController() {
		return controller;
	}

	/**
	 * @return the gui stack handle
	 */
	@Override
	public GuiStack getGuiStackHandle() {
		if (guiStackHandle == null) {
			guiStackHandle = wControl.getWindowBackOffice().createGuiStack(controller.getInitialComponent());
		}
		return guiStackHandle;
	}

	/**
	 * @return the title
	 */
	@Override
	public String getTitle() {
		return title;
	}

	/**
	 * @return the short title
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return the olat resourceable
	 */
	@Override
	public OLATResourceable getOLATResourceable() {
		return ores;
	}

	@Override
	public OLATResourceable getInitialOLATResourceable() {
		return initialOres;
	}

	@Override
	public void dispose() {
		if(controller != null) {
			controller.dispose();
		}
	}

	@Override
	public WindowControl getWindowControl() {
		return wControl;
	}

	@Override
	public String toString() {
		return "ores: "+ores.getResourceableTypeName()+","+ores.getResourceableId()+", title: "+title;
	}

	@Override
	public CustomCSS getCustomCSS() {
		// delegate to content controller if of type main layout controller
		if (controller != null && controller instanceof MainLayoutController) {
			MainLayoutController layoutController = (MainLayoutController) controller;
			return layoutController.getCustomCSS();
		}
		return null;
	}
}