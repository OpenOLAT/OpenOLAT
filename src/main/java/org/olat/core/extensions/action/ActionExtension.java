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

package org.olat.core.extensions.action;

import java.util.Locale;

import org.olat.core.extensions.ExtensionElement;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;

/**
 * Description:<br>
 * Initial Date:  02.08.2005 <br>
 * 
 * do not directly implement this interface. Subclass GenericActionExtension instead!
 * 
 * @author Felix
 */
public interface ActionExtension extends ExtensionElement {

	/**
	 * creates a GenericTreeNode to be used in Menu/Navigation trees
	 * 
	 * @param ureq
	 * @return
	 */
	public GenericTreeNode createMenuNode(UserRequest ureq);
	
	
	/**
	 * @param loc
	 * @return the description
	 */
	public String getDescription(Locale loc);
	
	/**
	 * @param loc
	 * @return the text of the html link
	 */
	public String getActionText(Locale loc);
	
	/**
	 * @param ureq
	 * @param wControl
	 */
	public Controller createController(UserRequest ureq, WindowControl wControl, Object arg);

}
