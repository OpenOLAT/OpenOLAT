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
* Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <p>
*/ 
package org.olat.core.gui.components.panel;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.render.ValidationResult;

/**
 * Description:<br>
 * A Panel which shows a certain content only one render time, and then is hidden until a new content is set.
 * useful for e.g. a message on the screen which should automatically disappear after having been rendered once until
 * a new message appears.
 * <P>
 * Initial Date: 19.01.2007 <br>
 * @author Felix Jost, http://www.goodsolutions.ch
 */
public class OncePanel extends Panel {

	private boolean hideOnNextValidate;

	/**
	 * @param name
	 */
	public OncePanel(String name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see org.olat.core.gui.components.Component#validate(org.olat.core.gui.UserRequest, org.olat.core.gui.render.ValidationResult)
	 */
	@Override
	public void validate(UserRequest ureq, ValidationResult vr) {
		super.validate(ureq, vr);
		// after a change, flag that the content will be displayed only once
		if (isDirty()) {
			hideOnNextValidate = true;
		} else {
			// not dirty, check if flag is set.
			if (hideOnNextValidate) {
				hideOnNextValidate = false;
				setContent(null); // set dirty flag and causes a rerendering
			}
		}
	}

	
	
}
