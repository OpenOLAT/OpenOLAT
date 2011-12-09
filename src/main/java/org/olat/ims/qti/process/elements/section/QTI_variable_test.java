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

package org.olat.ims.qti.process.elements.section;

import org.dom4j.Element;
import org.olat.ims.qti.process.elements.ScoreBooleanEvaluable;

/**
* @author Felix Jost
*/
public class QTI_variable_test implements ScoreBooleanEvaluable {

	/* (non-Javadoc)
	 * @see org.olat.ims.qti.process.elements.section.SectionBooleanEvaluable#eval(org.dom4j.Element, org.olat.ims.qti.container.SectionContext)
	 */
	public boolean eval(Element boolElement, float score) {
		/*Attributes:
		* varname (optional. Default = 'SCORE'). The name of the variable that is to be tested. The default name is 'SCORE'.
		Data-type = String (max of 256 chars).
		* testoperator (mandatory with selection from the enumerated list of: EQ, NEQ, LT, LTE, GT, GTE). Identifies the nature of the variable comparison that is to be applied.
		Data-type = string (1-16 chars). 
		*/
		String testOperator = boolElement.attributeValue("testoperator");
		String val = boolElement.getText();
		float nval = Integer.parseInt(val);
		// test if sum of scores is ... the ival
		if (testOperator.equals("EQ")) { // equals
			return (nval == score);
		} else if (testOperator.equals("NEQ")) { // not equals
			return (nval != score);
		} else if (testOperator.equals("GT")) { // greater than
			return (nval < score);
		} else if (testOperator.equals("GTE")) { // greater or equal than
			return (nval <= score);
		} else if (testOperator.equals("LT")) { // less than
			return (nval > score);
		} else if (testOperator.equals("LTE")) { // less or equal than
			return (nval >= score);
		}
		//else // no else because of xml validation with dtd
		throw new RuntimeException(); // otherwise java compiler barks
	}

}
