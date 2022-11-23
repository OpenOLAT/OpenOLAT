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
package org.olat.core.gui.components.form.flexible.impl.elements;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;

/**
 * @author patrickb
 *
 */
abstract class FormButton extends FormItemImpl {
	
	private String iconLeftCSS;
	private String iconRightCSS;
	
	private boolean newWindowAfterDispatchUrl;

	/**
	 * @param name
	 */
	public FormButton(String name) {
		super(name);
	}
	
	/**
	 * @param id A fix identifier for state-less behavior, must be unique or null
	 * @param name
	 */
	public FormButton(String id, String name) {
		super(id, name, false);
	}
	
	public String getIconLeftCSS() {
		return iconLeftCSS;
	}

	public void setIconLeftCSS(String iconLeftCSS) {
		this.iconLeftCSS = iconLeftCSS;
	}

	public String getIconRightCSS() {
		return iconRightCSS;
	}

	public void setIconRightCSS(String iconRightCSS) {
		this.iconRightCSS = iconRightCSS;
	}
	
	public boolean isNewWindowAfterDispatchUrl() {
		return newWindowAfterDispatchUrl;
	}

	public void setNewWindowAfterDispatchUrl(boolean newWindowAfterDispatchUrl) {
		this.newWindowAfterDispatchUrl = newWindowAfterDispatchUrl;
	}

	@Override
	public void evalFormRequest(UserRequest ureq) {
		// Buttons do not evaluate
	}

	@Override
	public void reset() {
		// Buttons can not be resetted.
	}
	
	/**
	 * translated representation of the button text to be 
	 * rendered
	 * @return
	 */
	abstract String getTranslated();
	
}
