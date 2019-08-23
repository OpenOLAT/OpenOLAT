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
*/

package org.olat.ims.qti.container;
/**
 * 
 */
public class DecimalVariable extends Variable {
	private float floatValue= 0.0f;
	private float minValue, maxValue, cutValue, defaultValue;
	private boolean mindeclared= false, maxdeclared= false, cutdeclared= false, defaultdeclared= false;
	
	public DecimalVariable(String varName,	String maxValue, String minValue, String cutValue, String defaultValue) {
		super(varName);
		if (maxValue != null) {
			this.maxValue= parseFloat(maxValue);
			maxdeclared= true;
		}
		if (minValue != null) {
			this.minValue= parseFloat(minValue);
			mindeclared= true;
		}
		if (cutValue != null) {
			this.cutValue= parseFloat(cutValue);
			cutdeclared= true;
		}
		if (defaultValue != null) {
			float def= parseFloat(defaultValue);
			this.defaultValue= def;
			this.floatValue= def;
			defaultdeclared= true;
		}
	}
	
	public void reset() {
		if (defaultdeclared)
			floatValue= defaultValue;
		else
			floatValue= 0.0f;
	}
	
	public void add(String value) {
		floatValue += parseFloat(value);
	}
	
	public void subtract(String value) {
		floatValue -= parseFloat(value);
	}
	
	public void multiply(String value) {
		floatValue *= parseFloat(value);
	}
	
	public void divide(String value) {
		floatValue /= parseFloat(value);
	}
	
	private float parseFloat(String value) {
		value= value.trim();
		float f= Float.parseFloat(value);
		return f;
	}
	
	public float getTruncatedValue(boolean nanAsZero) {
		float tmp = floatValue;
		if (nanAsZero && Float.isNaN(tmp)) {
			tmp = 0.0f;
		}
		if (maxdeclared) {
			if (tmp > maxValue)
			tmp = maxValue;
		}
		if (mindeclared) {
			if (tmp < minValue)
			tmp = minValue;
		}
		return tmp;
	}
	
	public String toString() {
		return "(float)" + floatValue + ":" + super.toString();
	}
	
	public void setValue(String value) {
		floatValue= parseFloat(value);
	}

	public void setFloatValue(float floatValue) {
		this.floatValue = floatValue;
	}
	/**
	 * Returns the cutValue.
	 * @return float
	 */
	public float getCutValue() {
		return cutValue;
	}
	/**
	 * Returns the defaultValue.
	 * @return float
	 */
	public float getDefaultValue() {
		return defaultValue;
	}
	/**
	 * Returns the maxValue.
	 * @return float
	 */
	public float getMaxValue() {
		return maxValue;
	}
	/**
	 * Returns the minValue.
	 * @return float
	 */
	public float getMinValue() {
		return minValue;
	}
	/**
	 * @see org.olat.ims.qti.container.Variable#hasCutValue()
	 */
	public boolean hasCutValue() {
		return cutdeclared;
	}
	/**
	 * @see org.olat.ims.qti.container.Variable#hasDefaultValue()
	 */
	public boolean hasDefaultValue() {
		return defaultdeclared;
	}
	/**
	 * @see org.olat.ims.qti.container.Variable#hasMaxValue()
	 */
	public boolean hasMaxValue() {
		return maxdeclared;
	}
	/**
	 * @see org.olat.ims.qti.container.Variable#hasMinValue()
	 */
	public boolean hasMinValue() {
		return mindeclared;
	}
	/**
	 * @see org.olat.ims.qti.container.Variable#getValue(boolean)
	 */
	public float getValue(boolean nanAsZero) {
		if (nanAsZero && Float.isNaN(floatValue)) {
			return 0.0f;
		}
		return floatValue;
	}
	/**
	 * @see org.olat.ims.qti.container.Variable#isLessThan(java.lang.String)
	 */
	public boolean isLessThan(String operand) {
		float cmp= parseFloat(operand);
		return cmp < floatValue;
	}
	/**
	 * @see org.olat.ims.qti.container.Variable#isMoreThan(java.lang.String)
	 */
	public boolean isMoreThan(String operand) {
		float cmp= parseFloat(operand);
		return cmp > floatValue;
	}
	/**
	 * @see org.olat.ims.qti.container.Variable#isEqual(java.lang.String)
	 */
	public boolean isEqual(String operand) {
		float cmp= parseFloat(operand);
		return cmp == floatValue;
	}
}
