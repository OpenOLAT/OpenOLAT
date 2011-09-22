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

import org.olat.core.logging.OLATRuntimeException;

/**
* @author Felix Jost
*/
public class IntegerElement extends TextElement {


	private int intvalue;
	private boolean checked;

	/**
	 * @param labelKey
	 * @param intvalue
	 * @param size
	 */
	public IntegerElement(String labelKey, int intvalue, int size) {
		super(labelKey, ""+intvalue, false,  size, size);
		this.intvalue = intvalue;
	}

	/**
	 * @param labelKey
	 * @param intvalue
	 */
	public IntegerElement(String labelKey, int intvalue) {
		super(labelKey, ""+intvalue, false, 20, 20);
		this.intvalue = intvalue;
	}
	
	/**
	 * @param labelKey
	 */
	public IntegerElement(String labelKey) {
			super(labelKey,"", false, 20, 20);
			this.intvalue = 0;
	}
	
	/**
	 * @param errorKey
	 * @return
	 */
	public boolean isInteger(String errorKey) {
		String val = getValue();
		boolean ok = false;
		try {
			this.intvalue = Integer.parseInt(val);
			clearError();
			ok = true;
			this.checked = true;
		}
		catch (NumberFormatException nfe) {
			setErrorKey(errorKey);
		}
		return ok;
	}
	
	/**
	 * Make sure you called isInteger prior to this method. Otherwhise the invalue is invalid
	 * and a runtime exception will be thrown
	 * @return int
	 */
	public int getIntvalue() {
	    if (this.checked)
	        return intvalue;
	    else
	        throw new OLATRuntimeException(IntegerElement.class, 
                "You must call isInteger() in the form validate method prior to calling getIntvalue!", null);
	}

	/**
	 * Sets the intvalue.
	 * @param intvalue The intvalue to set
	 */
	public void setIntvalue(int intvalue) {
		this.intvalue = intvalue;
		super.setValue(new StringBuilder().append(intvalue).toString());
	}

}
