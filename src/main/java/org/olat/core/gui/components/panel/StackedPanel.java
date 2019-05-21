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

package org.olat.core.gui.components.panel;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentCollection;

/**
 * Description: <br>
 * The panel implements a place holder component with a stack to hold zero, one
 * or more components. Only the highest component on the stack is shown.
 * 
 * @author Felix Jost
 */
public interface StackedPanel extends ComponentCollection {

	public Component getContent();
	
	public void setContent(Component component);
	
	public Component popContent();
	
	/**
	 * @param newContent may not be null
	 */
	public void pushContent(Component newContent);

	/**
	 * Set a css class to be applied to the stack itself
	 * @param stackCss
	 */
	public void setCssClass(String stackCss);
	
	/**
	 * The css class for the entire stack
	 * @return
	 */
	public String getCssClass();
}