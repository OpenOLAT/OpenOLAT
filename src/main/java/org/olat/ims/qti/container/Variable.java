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

import java.io.Serializable;

/**
 * @author Felix Jost
 */
public abstract class Variable implements Serializable{
	/*
	 * <!ELEMENT decvar (#PCDATA)>
		<!ATTLIST decvar  %I_VarName;
                   vartype     (Integer | 
                                String | 
                                Decimal | 
                                Scientific | 
                                Boolean | 
                                Enumerated | 
                                Set )  'Integer'
                   defaultval CDATA  #IMPLIED
                   minvalue   CDATA  #IMPLIED
                   maxvalue   CDATA  #IMPLIED
                   members    CDATA  #IMPLIED
                   cutvalue   CDATA  #IMPLIED >
	 */
	

	private String varName;
	
	
	/**
	 * 
	 */
	public Variable(String theVarName) {
		this.varName = theVarName.trim();
	}

	/**
	 * Returns the varName.
	 * @return String
	 */
	public String getVarName() {
		return varName;
	}
	
	public abstract float getCutValue();
	public abstract float getDefaultValue();
	public abstract float getMaxValue();
	public abstract float getMinValue();
	
	public abstract float getValue(boolean nanAsZero);
	public abstract float getTruncatedValue(boolean nanAsZero);
	public abstract void setValue(String value);
	public abstract void setFloatValue(float value);

	public abstract boolean hasCutValue();
	public abstract boolean hasDefaultValue();
	public abstract boolean hasMaxValue();
	public abstract boolean hasMinValue();
	
	public abstract boolean isLessThan(String value);
	public abstract boolean isMoreThan(String value);	
	public abstract boolean isEqual(String value);
	
	public abstract void add(String value);	
	public abstract void subtract(String value);	
	public abstract void multiply(String value);	
	public abstract void divide(String value);	
	
	public abstract void reset();

}
