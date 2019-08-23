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

package org.olat.ims.qti.editor.beecom.objects;

import org.dom4j.Attribute;

/**
 * @author rkulow
 *
 */
public class FIBResponse extends Response {
	
	public static String TYPE_CONTENT = "CONTENT";
	public static String TYPE_BLANK = "BLANK";
	public static String CASE_YES = "Yes";
	public static String CASE_NO = "No";
	public static int 	 SIZE_DEFAULT = 20;
	public static int 	 MAXLENGTH_DEFAULT = 50;
	public static String SYNONYM_SPLIT_STRING = ";";
	public static String SYNONYM_SPLIT_ESCAPED_STRING = ";;";

	private String type = null;
	private String correctBlank = null;
	private String caseSensitive = null;
	private int size, maxLength;
	
	public FIBResponse() {
		super();
	}
	
	/**
	 * Returns the type.
	 * @return String
	 */
	public String getType() {
		return type;
	}

	/**
	 * Sets the type.
	 * @param type The type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return
	 */
	public String getCorrectBlank() {
		return correctBlank;
	}

	/**
	 * Returns all correct synonyms.
	 *
	 * @return Array of strings
	 */
	public String[] getCorrectSynonyms() {
		if (correctBlank != null) {
			String tmp = correctBlank.replaceAll(SYNONYM_SPLIT_ESCAPED_STRING, "[ESCAPED]");
			String[] splits = tmp.split(SYNONYM_SPLIT_STRING);
			for (int i = 0; i < splits.length; i++) {
				splits[i] = splits[i].replaceAll("\\[ESCAPED]", SYNONYM_SPLIT_ESCAPED_STRING);
			}
			return splits;
		} else {
			return null;
		}
	}

	/**
	 * @param string
	 */
	public void setCorrectBlank(String string) {
		correctBlank = string;
	}

	/**
	 * @return
	 */
	public String getCaseSensitive() {
		if (caseSensitive != null) return caseSensitive;
		else return CASE_YES;
	}

	/**
	 * Sets case sensitiveness of this response. String can be 'Yes' or 'No'. 
	 * In any other cases, including string==null, the case will be set to 'Yes'.
	 * @param string
	 */
	public void setCaseSensitive(String string) {
		if (string != null && string.equals(CASE_NO)) caseSensitive = CASE_NO;
		else caseSensitive = CASE_YES; // default
	}

	/**
	 * Returns the size for this fib blank. If size is set to 0, the default size
	 * is returned
	 * @return the current size
	 */
	public int getSize() {
		if (size == 0) return SIZE_DEFAULT;
		else return size;
	}

	/**
	 * Returns the maxLength for this fib blank. If maxLength is set to 0, the default maxLength
	 * is returned
	 * @return the current maxLength
	 */
	public int getMaxLength() {
		if (maxLength == 0) return MAXLENGTH_DEFAULT;
		else return maxLength;
	}

	/**
	 * Sets the size. If the given int is 0 the default size is used
	 * instead
	 * @param i
	 */
	public void setSize(int i) {
		if (i == 0) size = SIZE_DEFAULT;
		else size = i;
	}

	/**
	 * Sets the size. If the given int is 0 the default size is used
	 * instead
	 * @param i
	 */
	public void setMaxLength(int i) {
		if (i == 0) maxLength = MAXLENGTH_DEFAULT;
		else maxLength = i;
	}

	/**
	 * Sets se size to the value stored in this column attribute. 
	 * It the attribute is null the size is set to the default 
	 * value
	 * @param i
	 */
	public void setSizeFromColumns(Attribute i) {
		if (i == null) {
			size = SIZE_DEFAULT;
		} else {
			String value = i.getStringValue();
			setSizeFromString(value);
		}
	}

	/**
	 * Sets se size to the value stored in this column attribute. 
	 * It the attribute is null the size is set to the default 
	 * value
	 * @param i
	 */
	public void setMaxLengthFromMaxChar(Attribute i) {
		if (i == null) {
			maxLength = MAXLENGTH_DEFAULT;
		} else {
			String value = i.getStringValue();
			setMaxLengthFromString(value);
		}
	}

	/**
	 * Sets se size to the given string value. If string is null or a not  
	 * stored in this column attribute.It the attribute is null the size 
	 * is set to the default size 
	 * value
	 * @param i
	 */
	public void setSizeFromString(String value) {
		if (value == null){
			size = SIZE_DEFAULT;
			return;
		}
		try {
			setSize(Integer.parseInt(value));
		}
		catch (NumberFormatException e) {
			size = SIZE_DEFAULT;
		}
	}

	/**
	 * Sets se size to the given string value. If string is null or a not  
	 * stored in this column attribute.It the attribute is null the size 
	 * is set to the default size 
	 * value
	 * @param i
	 */
	public void setMaxLengthFromString(String value) {
		if (value == null){
			maxLength = MAXLENGTH_DEFAULT;
			return;
		}
		try {
			setMaxLength(Integer.parseInt(value));
		}
		catch (NumberFormatException e) {
			maxLength = MAXLENGTH_DEFAULT;
		}
	}

}
