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

package org.olat.ims.qti.process.elements;

import org.dom4j.Element;
import org.olat.ims.qti.container.ItemContext;
import org.olat.ims.qti.container.ItemInput;

/**
 * 
 */
public class QTI_vargt implements BooleanEvaluable {

	/**
	 * var greater than or equal qti ims 1.2.1 <!ELEMENT vargte (#PCDATA)>
	 * <!ATTLIST vargte %I_RespIdent; %I_Index; > e.g. <vargte respident =
	 * "NUM01">3.141 </vargte>
	 * @param boolElement
	 * @param userContext
	 * @param ect
	 * @return
	 */
	public boolean eval(Element boolElement, ItemContext userContext, EvalContext ect) {
		ItemInput iinp = userContext.getItemInput();
		if (iinp.isEmpty()) return false; // user has given no answer
		String respident = boolElement.attributeValue("respident");
		String shouldVal = boolElement.getText(); // the answer is tested against
																							// content of elem.
		String isVal = iinp.getSingle(respident);
		// the isVal and shouldVal must be numeric
		// we use Float so we are on the safe side, even if comparison was only
		// Integer
		shouldVal = shouldVal.trim();
		isVal = isVal.trim();
		float fs = Float.parseFloat(shouldVal);
		float fi;
		try {
			fi = Float.parseFloat(isVal);
		} catch (NumberFormatException e) {
			//try to replace , -> .
			isVal = isVal.replace(',', '.');
			try {
				fi = Float.parseFloat(isVal);
			} catch (NumberFormatException e1) {
				//we try all what we can to understand the input value -> false
				return false;
			}
		}
		boolean ok = (fi > fs);
		return ok;
	}

}