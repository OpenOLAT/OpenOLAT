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

package org.olat.core.gui.control.navigation;

import org.olat.core.gui.components.htmlheader.jscss.CustomCSS;
import org.olat.core.gui.components.htmlheader.jscss.CustomCSSProvider;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Disposable;
import org.olat.core.gui.control.generic.layout.MainLayoutController;
import org.olat.core.gui.control.guistack.GuiStack;

/**
 * Initial Date: 19.07.2005 <br>
 * 
 * @author Felix Jost
 */
public class BornSiteInstance implements Disposable, CustomCSSProvider {
	private GuiStack guiStackHandle;
	private Controller controller;

	/**
	 * @param guiStackHandle
	 * @param controller
	 */
	public BornSiteInstance(GuiStack guiStackHandle, Controller controller) {
		this.guiStackHandle = guiStackHandle;
		this.controller = controller;
	}

	/**
	 * @return the controller
	 */
	public Controller getController() {
		return controller;
	}

	/**
	 * @return the guistack
	 */
	public GuiStack getGuiStackHandle() {
		return guiStackHandle;
	}

	@Override
	public void dispose() {
		if (controller != null) {
			controller.dispose();			
		}
	}

	@Override
	public CustomCSS getCustomCSS() {
		// delegate to content controller if of type main layout controller
		if (controller instanceof MainLayoutController) {
			MainLayoutController layoutController = (MainLayoutController) controller;
			return layoutController.getCustomCSS();
		}
		return null;
	}

}
