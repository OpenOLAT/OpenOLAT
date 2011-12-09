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

package org.olat.core.gui.formelements;
/**
 * Description:<BR>
 * A form selection element can have a list of VisibilityDependsOnSelectionRules. Each of 
 * this rules in this list defines a visibility / editability constraint that does apply 
 * to another form elements when the value of the selection element changes. 
 * The rule consists of a value that must matched the form selections value. If the value 
 * matches, the form element will be changed to readOnly or readWrite depending on the 
 * visibilityRuleResul configuration.
 * <P>
 * Initial Date:  Dec 9, 2004
 *
 * @author gnaegi 
 * @author BPS (<a href="http://www.bps-system.de/">BPS Bildungsportal Sachsen GmbH</a>)
 */
public class VisibilityDependsOnSelectionRule {
	SelectionElement selectionElement;
	FormElement dependentElement;
	String visibilityRuleValue;
	boolean visibilityRuleResult;
	String resetValue;
	boolean hideDisabledElements;
	/**
	 * If preventOppositeAction is set to true, then this rule only works for the given
	 * direction and is ignored in the other case. E.g. if the rule is to hide an element
	 * if something is true, then (with preventOppositeAction == true) it is not displayed
	 * again, if this something changes from true to false.
	 */
	boolean preventOppositeAction;

	/**
	 * Constructor for a visibility-depends-on-selection rule. This rule describes
	 * when the dependent element is editable
	 * @param selectionElement The element that triggers the rule
	 * @param dependentElement The dependent form element
	 * @param visibilityRuleValue The selection value must match this value
	 * @param visibilityRuleResult If a match exists this is the visibility rule
	 * @param resetValue Value to be set on the dependent element when the visibilty is set 
	 * to false (elements default value)
	 * @param hideDisabledElements Configuration flag: true: disabled form elements should 
	 * be removed from the GUI completely, false: disabled form elements will be disabled in the
	 * GUI and appear greyed but still visible
	 */
	public VisibilityDependsOnSelectionRule(SelectionElement selectionElement, FormElement dependentElement, 
			String visibilityRuleValue, boolean visibilityRuleResult, String resetValue, boolean hideDisabledElements) {
		this(selectionElement, dependentElement, visibilityRuleValue, visibilityRuleResult, resetValue, hideDisabledElements, false);
	}

	/**
	 * @return Returns the preventOppositeAction.
	 */
	public boolean isPreventOppositeAction() {
		return preventOppositeAction;
	}

	/**
	 * @param preventOppositeAction The preventOppositeAction to set.
	 */
	public void setPreventOppositeAction(boolean preventOppositeAction) {
		this.preventOppositeAction = preventOppositeAction;
	}

	/**
	 * Constructor for a visibility-depends-on-selection rule. This rule describes
	 * when the dependent element is editable
	 * @param selectionElement The element that triggers the rule
	 * @param dependentElement The dependent form element
	 * @param visibilityRuleValue The selection value must match this value
	 * @param visibilityRuleResult If a match exists this is the visibility rule
	 * @param resetValue Value to be set on the dependent element when the visibilty is set 
	 * to false (elements default value)
	 * @param hideDisabledElements Configuration flag: true: disabled form elements should 
	 * be removed from the GUI completely, false: disabled form elements will be disabled in the
	 * GUI and appear greyed but still visible
	 */
	public VisibilityDependsOnSelectionRule(SelectionElement selectionElement, FormElement dependentElement, 
			String visibilityRuleValue, boolean visibilityRuleResult, String resetValue, boolean hideDisabledElements, boolean preventOppositeAction) {
		super();
		// some checks if valid configuration
		if (dependentElement instanceof MultipleSelectionElement && hideDisabledElements == false) {
			throw new AssertionError("Programming error: hideDisabledElements=false in VisibilityDependsOnSelectionRule for MultipleSelectionElement not supported. Set hideDisabledElements=true");
		}
		
		this.selectionElement = selectionElement;
		this.dependentElement = dependentElement;
		this.visibilityRuleValue = visibilityRuleValue;
		this.visibilityRuleResult = visibilityRuleResult;
		this.resetValue = resetValue;
		this.hideDisabledElements = hideDisabledElements;
		this.preventOppositeAction = preventOppositeAction;
	}

	/**
	 * @return The form element that triggers this rule
	 */
	public SelectionElement getSelectionElement() {
		return selectionElement;
	}
	/**
	 * @return The form element that depends on this rule
	 */
	public FormElement getDependentElement() {
		return dependentElement;
	}
	/**
	 * @return The value that must match the selection elements
	 * value to make the rule active.
	 */
	public String getVisibilityRuleValue() {
		return visibilityRuleValue;
	}
	/**
	 * @return The result that should be applied when the rule matches
	 */
	public boolean isVisibilityRuleResult() {
		return visibilityRuleResult;
	}
	/**
	 * @return The value that should be set to the dependet element if the rule matches
	 */
	public String getResetValue() {
		return resetValue;
	}
	
	/**
	 * @return boolean true: hide elements completely, false: only disable elements
	 */
	public boolean isHideDisabledElements() {
		return hideDisabledElements;
	}
}
