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

package org.olat.course.condition;

import java.io.Serializable;

import org.olat.core.logging.OLATRuntimeException;
import org.olat.course.condition.operators.Operator;
import org.olat.course.condition.operators.OperatorManager;

/**
 * Description:<br>
 * This class represents conditions for the extended easy mode in course editor.
 * Each condition consists of an attribute, an operator and a value to be tested.
 * 
 * <P>
 * Initial Date:  23.10.2006 <br>
 * @author Lars Eberle (<a href="http://www.bps-system.de/">BPS Bildungsportal Sachsen GmbH</a>)
 */
public class ExtendedCondition implements Serializable {

	private String attribute;
	private Operator operator = null;
	private String value;
	
	public transient static final String CONDITION_CONNECTOR_AND = "and";
	public transient static final String CONDITION_CONNECTOR_OR = "or";
	
	/**
	 * Defines a new extended condition.
	 */
	public ExtendedCondition(String attribute, Operator operator, String value) {
		this.attribute = attribute;
		this.operator = operator;
		this.value = value;
	}
	
	/**
	 * Defines a new extended condition and tries to determine the operator class
	 * from this given String.
	 * If the operator isn't recognized, an Exception will be thrown, so be careful
	 * using this method!
	 * 
	 * @throws IllegalArgumentException if the given operator string doesn't match an registered operator. 
	 */
	public ExtendedCondition(String attribute, String operator, String value) {
		this.attribute = attribute;
		for (Operator o : OperatorManager.getAvailableOperators()) {
			if (o.getOperatorKey().equals(operator)) {
				this.operator = o;
				break;
			}			
		}
		if (this.operator == null)
			throw new OLATRuntimeException("Attention: LimitCheck " + operator + " given for extended condition, but no belonging LimitCheck class found. Taking EqualsOperator fallback!", new IllegalArgumentException());
		this.value = value;
	}
	
	/**
	 * @return Returns the attribute.
	 */
	public String getAttribute() {
		return attribute;
	}
	
	/**
	 * @param attribute The attribute to set.
	 */
	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}
	
	/**
	 * @return Returns the operator.
	 */
	public Operator getOperator() {
		return operator;
	}
	
	/**
	 * @param operator The operator to set.
	 */
	public void setOperator(Operator operator) {
		this.operator = operator;
	}
	
	/**
	 * @return Returns the value.
	 */
	public String getValue() {
		return value;
	}
	
	/**
	 * @param value The value to set.
	 */
	public void setValue(String value) {
		this.value = value;
	}
	
	/**
	 * @return The expression of this condition, e.g. hasAttribute("attributName", "valueToBeTested")
	 */
	public String buildExpression() {
		return operator.buildExpression(attribute, value);
	}
	
}
