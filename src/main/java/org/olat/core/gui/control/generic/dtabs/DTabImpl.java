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
import org.olat.core.gui.control.Disposable;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.layout.MainLayoutController;
import org.olat.core.gui.control.guistack.GuiStack;
import org.olat.core.gui.control.navigation.DefaultNavElement;
import org.olat.core.gui.control.navigation.NavElement;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.StackedBusinessControl;
import org.olat.repository.ui.RepositoyUIFactory;

/**
 * Description: <br>
 * 
 * @author Felix Jost
 */
public class DTabImpl implements Disposable, DTab {
	
	private OLATResourceable ores;
	private OLATResourceable initialOres;
	private Controller controller;
	private GuiStack guiStackHandle;
	private String title;
	private String description;
	private final WindowControl wControl;
	private NavElement navElement;

	/**
	 * @param ores
	 * @param title
	 * @param wControl
	 */
	public DTabImpl(OLATResourceable ores, OLATResourceable initialOres, String title, WindowControl wOrigControl) {
		this.ores = ores;
		this.initialOres = initialOres;
		this.title = title;
		//Root the JumpInPath - typically all resources are opened in tabs
		StackedBusinessControl businessControl = new StackedBusinessControl(null, wOrigControl.getBusinessControl());
		wControl = BusinessControlFactory.getInstance().createBusinessWindowControl(businessControl, wOrigControl);

		String iconCSSClass = RepositoyUIFactory.getIconCssClass(ores.getResourceableTypeName());
		navElement = new DefaultNavElement(title, title, iconCSSClass);
	}

	/**
	 * [used by velocity]
	 * @return the navigation element for this dtab
	 */
	public NavElement getNavElement() {
		return navElement;
	}
	
	/**
	 * @return the controller
	 */
	public Controller getController() {
		return controller;
	}

	/**
	 * @return the gui stack handle
	 */
	public GuiStack getGuiStackHandle() {
		if (guiStackHandle == null) {
			guiStackHandle = wControl.getWindowBackOffice().createGuiStack(controller.getInitialComponent());
		}
		return guiStackHandle;
	}

	/**
	 * @return the title
	 */
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
	public OLATResourceable getOLATResourceable() {
		return ores;
	}

	@Override
	//fxdiff BAKS-7 Resume function
	public OLATResourceable getInitialOLATResourceable() {
		return initialOres;
	}

	/**
	 * @see org.olat.core.gui.control.Disposable#dispose(boolean)
	 */
	public void dispose() {
		if(controller != null){//OLAT-3500
			controller.dispose();
		}
	}

	/**
	 * @see org.olat.core.gui.control.generic.dtabs.DTab#getWindowControl()
	 */
	public WindowControl getWindowControl() {
		//TODO: wrap it in own windowcontrol for docking/undocking feature
		return wControl;
	}

	/**
	 * @see org.olat.core.gui.control.generic.dtabs.DTab#setController(org.olat.core.gui.control.Controller)
	 */
	public void setController(Controller launchController) {
		this.controller = launchController;
		
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "ores: "+ores.getResourceableTypeName()+","+ores.getResourceableId()+", title: "+title;
	}

	/**
	 * @see org.olat.core.gui.components.htmlheader.jscss.CustomCSSProvider#getCustomCSS()
	 */
	public CustomCSS getCustomCSS() {
		// delegate to content controller if of type main layout controller
		if (controller != null && controller instanceof MainLayoutController) {
				MainLayoutController layoutController = (MainLayoutController) controller;
				return layoutController.getCustomCSS();
			}
		return null;
	}

}