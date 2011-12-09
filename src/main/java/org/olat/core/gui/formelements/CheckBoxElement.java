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

package org.olat.core.gui.formelements;

import org.olat.core.logging.AssertException;

/**
 * Description: <br>
 * 
 * @author Felix Jost
 */
public class CheckBoxElement extends AbstractFormElement {
	private boolean checked;
	private Boolean original;

	/**
	 * @param labelKey
	 * 
	 */
	public CheckBoxElement(String labelKey) {
		this(labelKey, false);
	}

	/**
	 * @param labelKey
	 * @param checked
	 */
	public CheckBoxElement(String labelKey, boolean checked) {
		setLabelKey(labelKey);
		setChecked(checked);
	}

	/**
	 * @see org.olat.core.gui.formelements.FormElement#setValues(java.lang.String[])
	 */
	public void setValues(String[] values) {
		if (values == null) {
			setChecked(false);
		} else {
			if (values.length != 1) throw new AssertException("got multiple values for checkbox field:" + getName());
			// value is not interesting, since this is a single checkbox as a
			// formelement
			setChecked(true);
		}
	}

	/**
	 * Sets the checked.
	 * 
	 * @param checked The checked to set
	 */
	public void setChecked(boolean checked) {
		this.checked = checked;
		if (original == null) original = new Boolean(checked);
	}

	/**
	 * @return boolean
	 */
	public boolean isChecked() {
		return checked;
	}

	/**
	 * @see org.olat.core.gui.formelements.FormElement#isDirty()
	 */
	public boolean isDirty() {
		return (original != null && original.booleanValue() == checked) ? false : true;
	}

}