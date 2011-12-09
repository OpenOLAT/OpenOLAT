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
import org.olat.core.gui.components.panel.Panel;

/**
 * Description:<br>
 * TODO: Felix Jost Class Description for Trans
 * 
 * <P>
 * Initial Date: 24.01.2007 <br>
 * @author Felix Jost, http://www.goodsolutions.ch
 */
public interface GuiStack {

	/**
	 * 
	 * @param title the title of the modal dialog, can be null
	 * @param content the component to push as modal dialog
	 */
	public abstract void pushModalDialog(Component content);

	/**
	 * @see org.olat.core.gui.control.GuiStackHandle#pushContent(org.olat.core.gui.components.Component)
	 */
	public abstract void pushContent(Component newContent);

	/**
	 * @see org.olat.core.gui.control.GuiStackHandle#popContent()
	 */
	public abstract void popContent();

	/**
	 * @return
	 */
	public abstract Panel getPanel();

	/**
	 * @return Returns the modalPanel, which should be put so that it looks modal (e.g. alpha-blended background) may be null if no modal panel is needed
	 */
	public abstract Panel getModalPanel();

}