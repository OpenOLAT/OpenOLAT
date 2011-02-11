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
package org.olat.core.gui.components.form.flexible.impl.elements;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;

/**
 * @author patrickb
 *
 */
abstract class FormButton extends FormItemImpl {

	/**
	 * @param name
	 */
	public FormButton(String name) {
		super(name);
	}

	
	/* (non-Javadoc)
	 * @see org.olat.core.gui.components.form.flexible.impl.FormItemImpl#evalFormRequest(org.olat.core.gui.UserRequest)
	 */
	@Override
	public void evalFormRequest(UserRequest ureq) {
		// Buttons do not evaluate
	}

	/* (non-Javadoc)
	 * @see org.olat.core.gui.components.form.flexible.impl.FormItemImpl#validate(java.util.List)
	 */
	@Override
	public void validate(List validationResults) {
		// Buttons do not validate
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
