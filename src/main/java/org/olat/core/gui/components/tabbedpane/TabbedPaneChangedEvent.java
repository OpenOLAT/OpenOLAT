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

package org.olat.core.gui.components.tabbedpane;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.Event;

/**
 * @author Mike Stock
 */
public class TabbedPaneChangedEvent extends Event {

	private static final long serialVersionUID = 2481268566284582151L;

	/**
	 * <code>TAB_CHANGED</code>
	 */
	public static final String TAB_CHANGED = "tabChanged";

	private Component oldComponent;
	private Component newComponent;

	/**
	 * @param oldComponent
	 * @param newComponent
	 */
	public TabbedPaneChangedEvent(Component oldComponent, Component newComponent) {
		super(TAB_CHANGED);
		this.oldComponent = oldComponent;
		this.newComponent = newComponent;
	}

	/**
	 * @return Returns the newComponent.
	 */
	public Component getNewComponent() {
		return newComponent;
	}

	/**
	 * @return Returns the oldComponent.
	 */
	public Component getOldComponent() {
		return oldComponent;
	}
}