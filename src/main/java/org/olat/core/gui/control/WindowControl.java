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

package org.olat.core.gui.control;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.generic.closablewrapper.CalloutSettings;
import org.olat.core.gui.control.info.WindowControlInfo;
import org.olat.core.id.context.BusinessControl;

/**
 * Description: <br>
 * an Interface to control some Gui things like messages, navigational issues,
 * modal dialogs etc; It is passed from Controller to all subcontrollers to
 * allow the setting of info, warn, and error messages and to
 * activate/deactivate modal dialogs and to replace the complete gui stack (e.g.
 * when a header link is chosen)
 * 
 * @author Felix Jost
 */
public interface WindowControl {

	/**
	 * put the component on the stack pane of the -current- guistack. useful for
	 * modal dialogs and such.
	 * 
	 * @deprecated todo merge with pushAsModalDialog (pb)
	 * @param comp
	 */
	public void pushToMainArea(Component comp);

	/**
	 * @param comp
	 */
	public void pushAsModalDialog(Component comp);
	
	public boolean removeModalDialog(Component comp);
	
	/**
	 * This is a unique slot for a modal dialog above all others, especially the
	 * dialogs of TinyMCE (which have a z-index of 65535). It is mostly used to
	 * prevent redraw of the main component and/or the modals stacks because of
	 * the presence of a TinyMCE component. A redraw of the rich text component
	 * will discard all its own modal dialogs.
	 * 
	 * @param comp The component to push as top modal dialog
	 */
	public void pushAsTopModalDialog(Component comp);
	
	public boolean removeTopModalDialog(Component comp);
	
	/**
	 * 
	 * @param comp
	 * @param targetId
	 */
	public void pushAsCallout(Component comp, String targetId, CalloutSettings settings);
	
	
	public void addInstanteMessagePanel(Component comp);
	
	public boolean removeInstanteMessagePanel(Component comp);

	/**
	 * Push a controller to full screen mode.
	 * @param ctrl 
	 * @param bodyClass An additional body class (optional)
	 */
	public void pushFullScreen(Controller ctrl, String bodyClass);

	/**
	 * 
	 */
	public void pop();

	/**
	 * @param string The text
	 */
	public void setInfo(String string);
	
	/**
	 * 
	 * @param title The title
	 * @param text The text
	 */
	public void setInfo(String title, String text);

	/**
	 * @param string
	 */
	public void setError(String string);

	/**
	 * @param string
	 */
	public void setWarning(String string);
	
	/**
	 * 
	 * @return the windowcontrolinfo
	 */
	public WindowControlInfo getWindowControlInfo();
	
	/**
	 * Use only when coding the Activatable interface.
	 * flatens (that is, does 0 to n pop() 's to the level where this windowcontrol is located.
	 * Useful only when implementing the Activatable interface to dump all possible 
	 * pushed components and return to "the base" which is the level at which the controller's initialcomponent resides.
	 *
	 */
	public void makeFlat();
	
	/**
	 * 
	 * @return the businesscontrol, never null
	 */
	public BusinessControl getBusinessControl();
	
	
	public WindowBackOffice getWindowBackOffice();

}