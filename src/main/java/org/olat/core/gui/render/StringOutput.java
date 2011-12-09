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

package org.olat.core.gui.render;

/**
 * @author Felix Jost
 */
public class StringOutput {

	private StringBuilder sb;


	/**
	 * @param len
	 */
	public StringOutput(int len) {
		sb = new StringBuilder(len);
	}

	/**
	 * 
	 */
	public StringOutput() {
		sb = new StringBuilder();
	}

	/**
	 * @param val
	 * @return
	 */
	public StringOutput append(String val) {
		sb.append(val);
		return this;
	}

	/**
	 * @param i
	 * @return
	 */
	public StringOutput append(int i) {
		sb.append(String.valueOf(i));
		return this;
	}

	/**
	 * @param stringOutput
	 * @return
	 */
	public StringOutput append(StringOutput stringOutput) {
		sb.append(stringOutput.toString());
		return this;
	}

	/**
	 * @param sMin
	 * @return
	 */
	public StringOutput append(long sMin) {
		sb.append(String.valueOf(sMin));
		return this;
	}

	/**
	 * @param buffer
	 * @return
	 */
	public StringOutput append(StringBuilder buffer) {
		sb.append(buffer);
		return this;
	}

	/**
	 * @return The length of the string output
	 */
	public int length() {
		return sb.length();
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return sb.toString();
	}
	
}