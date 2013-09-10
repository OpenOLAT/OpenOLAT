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

import java.util.List;
import java.util.Map;
/**
 * @author Felix Jost
 */
public interface ItemInput {
	
	
	public String getIdent();
	
	/**
	 * returns the value of the Variable
	 * @param varName
	 * @return String the value of the Variable
	 */
	public String getSingle(String varName);
	
	/**
	 * returns a List of Strings with the corresponding answers, e.g. for a multiple choice
	 * with multiple response it could be mr01 = { "A", "C", "D"} if the user chose a,c, and d
	 * from the five answers with values a,b,c,d,e,f
	 * @param varName
	 * @return List the List containing the String(s)
	 */
	public List<String> getAsList(String varName);

	/**
	 * returns a map of all inputs for all response_xxx. response_xxx are keys that
	 * have corresponding Lists of Strings with the answers for that response_xxx
	 * @return
	 */
	public Map<String,List<String>> getInputMap();

	/**
	 * 
	 * @param varName
	 * @param value
	 * @return
	 */
	public boolean contains(String varName, String value);
	public boolean containsIgnoreCase(String varName, String value);

	/**
	 * @return true if the user gave an empty answer (did not select anything)
	 */
	public boolean isEmpty();
	
}
