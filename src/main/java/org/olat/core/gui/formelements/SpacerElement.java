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

package org.olat.core.gui.formelements;

/**
 * @author Felix Jost
 */
public class SpacerElement extends AbstractFormElement {

	private boolean hr, br;

	/**
	 * 
	 */
	public SpacerElement() {
		this(true, false);
	}

	/**
	 * @param hr
	 * @param br
	 */
	public SpacerElement(boolean hr, boolean br) {
		setLabelKey(null);
		this.hr = hr;
		this.br = br;
		super.setReadOnly(true);
	}

	/**
	 * the spacer ignores setValues
	 * 
	 * @see org.olat.core.gui.formelements.FormElement#setValues(java.lang.String[])
	 */
	public void setValues(String[] values) {
	//
	}

	/**
	 * @return
	 */
	public boolean isBr() {
		return br;
	}

	/**
	 * @return
	 */
	public boolean isHr() {
		return hr;
	}

	/**
	 * @param b
	 */
	public void setBr(boolean b) {
		br = b;
	}

	/**
	 * @param b
	 */
	public void setHr(boolean b) {
		hr = b;
	}

	/**
	 * 
	 * @see org.olat.core.gui.formelements.FormElement#setReadOnly(boolean)
	 */
	public void setReadOnly(boolean readOnly) {
		// ignore, since we are always readonly
	}
	
	/**
	 * @see org.olat.core.gui.formelements.FormElement#isDirty()
	 */
	public boolean isDirty() {
		return false;
	}

}