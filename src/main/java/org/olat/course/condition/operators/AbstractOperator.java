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

package org.olat.course.condition.operators;

/**
 * Description:<br>
 * Abstract class for the operators in extended easy mode.
 
 * 
 * <P>
 * Initial Date:  23.10.2006 <br>
 * @author Lars Eberle (<a href="http://www.bps-system.de/">BPS Bildungsportal Sachsen GmbH</a>)
 */
public abstract class AbstractOperator implements Operator {

	/**
	 * Builds the expression using the given attribute and value and the operator (depending which class implements this method).
	 * @param attribute
	 * @param value
	 * @return an expression like <pre>hasAttribute("attributeValue", "valueToBeTested")</pre>
	 */
	public String buildExpression(String attribute, String value) {
		return "(" + getOperator() + "(\"" + attribute + "\",\"" + value + "\"))";
	}
	
	
	/**
	 * Builds the SQL statement using the given attribute and value and the operator.
	 * @param attribute
	 * @param value
	 * @return
	 */
	public String buildSQLStatement(String attribute, String value) {
		return "";
	}
	
}
