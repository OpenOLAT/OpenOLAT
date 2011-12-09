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

package org.olat.core.gui.render;

import org.olat.core.gui.GlobalSettings;
import org.olat.core.gui.control.JSAndCSSAdder;
import org.olat.core.logging.AssertException;

/**
 * @author Felix Jost
 */
public class ValidationResult {
	private String newModuleURI;
	private final GlobalSettings globalSettings;
	private final JSAndCSSAdder jsAndCSSAdder;
	 

	public ValidationResult(GlobalSettings globalSettings, JSAndCSSAdder jsAndCSSAdder) {
		this.globalSettings = globalSettings;
		this.jsAndCSSAdder = jsAndCSSAdder;
	}
	
	/**
	 * @return the new module uri, e.g. infos/tech/index.html
	 */
	public String getNewModuleURI() {
		return newModuleURI;
	}

	/**
	 * @param newModuleURI
	 */
	public void setNewModuleURI(String newModuleURI) {
		if (this.newModuleURI != null) throw new AssertException("moduleUri can only be set once: old moduleuri:" + this.newModuleURI
				+ ", new module uri:" + newModuleURI);
		this.newModuleURI = newModuleURI;
	}
 
	
	/**
	 * @return Returns the globalSetting.
	 */
	public GlobalSettings getGlobalSettings() {
		return globalSettings;
	}

	/**
	 * @return Returns the jsAndCSSAdder.
	 */
	public JSAndCSSAdder getJsAndCSSAdder() {
		return jsAndCSSAdder;
	}

	
}