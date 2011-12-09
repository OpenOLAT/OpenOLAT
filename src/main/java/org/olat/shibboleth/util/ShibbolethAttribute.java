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
package org.olat.shibboleth.util;

import java.io.UnsupportedEncodingException;
import java.util.regex.Pattern;

import org.olat.core.logging.AssertException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;

/**
 * Description:<br>
 * Represents a shibboleth attribut, which may contain values delimited by ";".
 * 
 * 
 * <P>
 * Initial Date:  Oct 27, 2010 <br>
 * @author patrick
 */
public final class ShibbolethAttribute{
	
	
	
	private static final Pattern SPLITTER = Pattern.compile(";");
	private final String attributeName;
	private final String originalValue;
	private final String[] splittedValues;

	private final static ShibbolethAttribute INVALID_ATTRIBUTE = new ShibbolethAttribute("INVALIDMARKED","INVALIDMARKED");
	
	ShibbolethAttribute(String name, String value) {
		if(isInvalidValue(value)){
			throw new IllegalArgumentException("value must be not null and not empty");
		}
		
		this.attributeName = name;
		this.originalValue = value;
		this.splittedValues = SPLITTER.split(value);
	}
	
	public String getName(){
		return attributeName;
	}
	
	public String getValueString(){
		return originalValue;
	}
	
	public String[] getValues(){
		return splittedValues;
	}
	
	String getFirstValue(){
		return splittedValues[0];
	}
	
	public boolean isValid(){
		return this != INVALID_ATTRIBUTE;
	}
	
	
	public static ShibbolethAttribute createFromUserRequestValue(String name, String rawRequestValue){
		if(isInvalidValue(rawRequestValue)){
			if(Tracing.isDebugEnabled(ShibbolethAttribute.class)){
				Tracing.logDebug("invalid attribute: " + name + " attributeValue: " + rawRequestValue, ShibbolethAttribute.class);
			}
			return INVALID_ATTRIBUTE;
		}
		
		try {
	
			String utf8Value = new String(rawRequestValue.getBytes("ISO-8859-1"), "UTF-8");
			return new ShibbolethAttribute(name, utf8Value);
		
		} catch (UnsupportedEncodingException e) {
			//bad luck
			throw new AssertException("ISO-8859-1, or UTF-8 Encoding not supported",e);
		}
	}
	
	private static boolean isInvalidValue(String value){
		return ( ! StringHelper.containsNonWhitespace(value));
	}
	
}
