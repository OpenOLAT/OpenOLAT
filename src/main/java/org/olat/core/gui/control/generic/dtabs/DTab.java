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

import org.olat.core.gui.components.htmlheader.jscss.CustomCSSProvider;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Disposable;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.guistack.GuiStack;
import org.olat.core.gui.control.navigation.NavElement;
import org.olat.core.id.OLATResourceable;

/**
 * Description:<br>
 * the api user view
 * <P>
 * Initial Date: 19.07.2005 <br>
 * 
 * @author Felix Jost
 */
public interface DTab extends Disposable, CustomCSSProvider {

	/**
	 * @return the windowcontrol
	 */
	public WindowControl getWindowControl();
	
	public GuiStack getGuiStackHandle();

	/**
	 * @return the title
	 */
	public String getTitle();
	
	/**
	 * 
	 * @return
	 */
	public NavElement getNavElement();

	/**
	 * @return the olat resourceable
	 */
	public OLATResourceable getOLATResourceable();
	
	/**
	 * @return the olat resourceable
	 */
	public OLATResourceable getInitialOLATResourceable();

	/**
	 * 
	 * @return The root controller
	 */
	public Controller getController();

}