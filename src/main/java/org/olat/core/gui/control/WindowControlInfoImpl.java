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

import org.olat.core.gui.control.info.WindowControlInfo;

public class WindowControlInfoImpl implements WindowControlInfo {
	private final WindowControlInfo parentWindowControlInfo;
	private final Class<? extends DefaultController> defaultControllerClass;
	
	
	/**
	 * @param an instance of defaultcontroller
	 * @param parentWindowControlInfo
	 */
	public WindowControlInfoImpl(DefaultController defaultController, WindowControlInfo parentWindowControlInfo) {
		this.defaultControllerClass = defaultController.getClass();
		this.parentWindowControlInfo = parentWindowControlInfo;
	}

	/**
	 * @see org.olat.core.gui.control.info.WindowControlInfo#getControllerClassName()
	 */
	public String getControllerClassName() {
		return defaultControllerClass.getName();
	}

	/**
	 * can be null if now windowcontrol given
	 * @see org.olat.core.gui.control.info.WindowControlInfo#getParentWindowControlInfo()
	 */
	public WindowControlInfo getParentWindowControlInfo() {
		return parentWindowControlInfo;
	}
	
	public String toString() {
		return defaultControllerClass.getName()+":"+super.toString();
	}

}
