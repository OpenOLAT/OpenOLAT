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
package org.olat.core.gui.control.guistack;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.StackedPanel;
import org.olat.core.gui.control.generic.closablewrapper.CalloutSettings;

/**
 * 
 * <P>
 * Initial Date: 24.01.2007 <br>
 * @author Felix Jost, http://www.goodsolutions.ch
 */
public interface GuiStack {

	/**
	 * 
	 * @param content the component to push as modal dialog
	 */
	public void pushModalDialog(Component content);
	
	public boolean removeModalDialog(Component content);
	
	/**
	 * Push a different but single modal dialog atop (per z-index)
	 * all others dialog.
	 * 
	 * @param content A component
	 */
	public void pushTopModalDialog(Component content);
	
	public boolean removeTopModalDialog(Component content);
	
	/**
	 * 
	 * @param content The component to push as callout window
	 * @param targetId The target element
	 */
	public void pushCallout(Component content, String targetId, CalloutSettings settings);

	public void pushContent(Component newContent);

	public Component popContent();

	/**
	 * @return
	 */
	public StackedPanel getPanel();

	/**
	 * @return Returns the modalPanel, which should be put so that it looks modal (e.g. alpha-blended background) may be null if no modal panel is needed
	 */
	public StackedPanel getModalPanel();
	
	public StackedPanel getTopModalPanel();

}