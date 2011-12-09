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
import java.util.List;

/**
 * Initial Date: Jan 30, 2004
 * @author Mike Stock Comment:
 */
public class Condition implements Serializable, Cloneable {
	transient private String conditionId = null;
	private String condition = null;
	private boolean expertMode = false;

	private String easyModeBeginDate = null;
	private String easyModeEndDate = null;
	private String easyModeGroupAccess = null;
	private String easyModeGroupAreaAccess = null;
	private String easyModeNodePassedId = null;
	private String easyModeCutValue = null;

	// true: ONLY coaches and admins have access, students are blocked out
	// false: no such rule
	private boolean easyModeCoachesAndAdmins;

	// true: coaches and admins have ALWAYS access
	// false: rules defined for students do also apply for coaches and
	// administrators
	private boolean easyModeAlwaysAllowCoachesAndAdmins;

	// This is the MapList in which the extended easy mode conditions are stored
	private List<ExtendedCondition> attributeConditions = null;
	private Boolean attributeconditionsConnectorIsAND = null;
	
	/**
	 * Default constructor.
	 */
	public Condition() {
		super();
	}

	/**
	 * Condition object, intialized with a condition.
	 * 
	 * @param condition
	 */
	public Condition(String condition) {
		this.condition = condition;
		setExpertMode(true);
	}

	/**
	 * @return condition expression.
	 */
	public String getConditionExpression() {
		return condition;
	}

	/**
	 * @param string
	 */
	public void setConditionExpression(String string) {
		condition = string;
	}

	/**
	 * @return Returns the expertMode.
	 */
	public boolean isExpertMode() {
		return expertMode;
	}

	/**
	 * @param expertMode The expertMode to set.
	 */
	public void setExpertMode(boolean expertMode) {
		this.expertMode = expertMode;
	}

	/**
	 * @return Returns the easyModeBeginDate.
	 */
	public String getEasyModeBeginDate() {
		return easyModeBeginDate;
	}

	/**
	 * @param easyModeBeginDate The easyModeBeginDate to set.
	 */
	public void setEasyModeBeginDate(String easyModeBeginDate) {
		if (easyModeBeginDate != null && easyModeBeginDate.equals("")) this.easyModeBeginDate = null;
		else this.easyModeBeginDate = easyModeBeginDate;
	}

	/**
	 * @return Returns the easyModeEndDate.
	 */
	public String getEasyModeEndDate() {
		return easyModeEndDate;
	}

	/**
	 * @param easyModeEndDate The easyModeEndDate to set.
	 */
	public void setEasyModeEndDate(String easyModeEndDate) {
		if (easyModeEndDate != null && easyModeEndDate.equals("")) this.easyModeEndDate = null;
		else this.easyModeEndDate = easyModeEndDate;
	}

	/**
	 * @return Returns the easyModeGroupAccess.
	 */
	public String getEasyModeGroupAccess() {
		return easyModeGroupAccess;
	}

	/**
	 * @param easyModeGroupAccess The easyModeGroupAccess to set.
	 */
	public void setEasyModeGroupAccess(String easyModeGroupAccess) {
		if (easyModeGroupAccess != null && easyModeGroupAccess.equals("")) this.easyModeGroupAccess = null;
		else this.easyModeGroupAccess = easyModeGroupAccess;
	}

	/**
	 * @return Returns the easyModeGroupAreaAccess.
	 */
	public String getEasyModeGroupAreaAccess() {
		return easyModeGroupAreaAccess;
	}

	/**
	 * @param easyModeGroupAreaAccess The easyModeGroupAreaAccess to set.
	 */
	public void setEasyModeGroupAreaAccess(String easyModeGroupAreaAccess) {
		if (easyModeGroupAreaAccess != null && easyModeGroupAreaAccess.equals("")) this.easyModeGroupAreaAccess = null;
		else this.easyModeGroupAreaAccess = easyModeGroupAreaAccess;
	}

	/**
	 * @return the easy mode configuration acces only for coaches and admins
	 */
	public boolean isEasyModeCoachesAndAdmins() {
		return easyModeCoachesAndAdmins;
	}

	/**
	 * @param easyModeCoachesAndAdmins true: access only for coaches and admins
	 */
	public void setEasyModeCoachesAndAdmins(boolean easyModeCoachesAndAdmins) {
		this.easyModeCoachesAndAdmins = easyModeCoachesAndAdmins;
	}

	/**
	 * @return true
	 */
	public boolean isEasyModeAlwaysAllowCoachesAndAdmins() {
		return easyModeAlwaysAllowCoachesAndAdmins;
	}

	/**
	 * @param easyModeAlwaysAllowCoachesAndAdmins
	 */
	public void setEasyModeAlwaysAllowCoachesAndAdmins(boolean easyModeAlwaysAllowCoachesAndAdmins) {
		this.easyModeAlwaysAllowCoachesAndAdmins = easyModeAlwaysAllowCoachesAndAdmins;
	}

	/**
	 * @return String
	 */
	public String getEasyModeNodePassedId() {
		return easyModeNodePassedId;
	}

	/**
	 * @param easyModeNodePassedId
	 */
	public void setEasyModeNodePassedId(String easyModeNodePassedId) {
		this.easyModeNodePassedId = easyModeNodePassedId;
	}

	/**
	 * @return String
	 */
	public String getEasyModeCutValue() {
		return easyModeCutValue;
	}

	/**
	 * @param easyModeCutValue
	 */
	public void setEasyModeCutValue(String easyModeCutValue) {
		this.easyModeCutValue = easyModeCutValue;
	}

	/**
	 * Sets the condition using the easy mode configuration parameters.
	 * 
	 * @return String the calculated condition
	 */
	public String getConditionFromEasyModeConfiguration() {
		boolean needsAmpersand = false;
		StringBuilder sb = new StringBuilder();

		sb.append("( "); // BEGIN all enclosing bracket

		if (getEasyModeBeginDate() != null) {
			sb.append("(now >= date(\"");
			sb.append(getEasyModeBeginDate());
			sb.append("\"))");
			needsAmpersand = true;
		}
		if (getEasyModeEndDate() != null) {
			if (needsAmpersand) sb.append(" & ");
			sb.append("(now <= date(\"");
			sb.append(getEasyModeEndDate());
			sb.append("\"))");
			needsAmpersand = true;
		}
		if (getEasyModeGroupAccess() != null) {
			if (getEasyModeGroupAreaAccess() != null) {
				if (needsAmpersand) sb.append(" & (");
				else sb.append(" (");
			} else if (needsAmpersand) {
				sb.append(" & ");
			}
			// Delimiter for more than one groups is a comma
			String[] groups = getEasyModeGroupAccess().split(",");
			if (groups.length > 1) sb.append("(");
			for (int i = 0; i < groups.length; i++) {
				sb.append("inLearningGroup(\"");
				sb.append(groups[i].trim());
				sb.append("\")");
				// Append OR parameter if not last parameter
				if (i + 1 < groups.length) sb.append(" | ");
			}
			if (groups.length > 1) sb.append(")");
			needsAmpersand = true;
		}
		if (getEasyModeGroupAreaAccess() != null) {
			if (getEasyModeGroupAccess() != null && needsAmpersand) {
				sb.append("|");
			} else if (needsAmpersand) {
				sb.append(" & ");
			}
			// Delimiter for more than one group area is a comma
			String[] areas = getEasyModeGroupAreaAccess().split(",");
			if (areas.length > 1) sb.append("(");
			for (int i = 0; i < areas.length; i++) {
				sb.append("inLearningArea(\"");
				sb.append(areas[i].trim());
				sb.append("\")");
				// Append OR parameter if not last parameter
				if (i + 1 < areas.length) sb.append(" | ");
			}
			if (areas.length > 1) sb.append(")");
			needsAmpersand = true;
			if (getEasyModeGroupAccess() != null) {
				sb.append(")");
			}
		}
		if (getEasyModeNodePassedId() != null) {
			if (needsAmpersand) sb.append(" & ");
			if (getEasyModeCutValue() != null) {
				sb.append(" ( getScore(\"");
				sb.append(getEasyModeNodePassedId());
				sb.append("\") >= ");
				sb.append(getEasyModeCutValue());
				sb.append(" )");
			} else {
				sb.append(" getPassed(\"");
				sb.append(getEasyModeNodePassedId());
				sb.append("\")");
			}
			needsAmpersand = true;
		}
		if (isEasyModeCoachesAndAdmins()) {
			if (needsAmpersand) sb.append(" & ");
			sb.append(" ( isCourseCoach(0) | isCourseAdministrator(0) )");
			// do not set needs ampersand here to not douplicate this rule by the
			// isEasyModeAlwaysAllowCoachesAndAdmins() rule
		}
		
		// create extended mode expert condition
		
		if (attributeConditions != null) {
			if (needsAmpersand) sb.append(" &"); else needsAmpersand = true;
			sb.append(" (");	// open extended section enclosing bracket
			
			boolean extNeedsConnector = false;
			
			for (ExtendedCondition ec : attributeConditions) {
				if (extNeedsConnector) sb.append((attributeconditionsConnectorIsAND) ? " & " : " | "); else extNeedsConnector = true;
				sb.append(" (").append(ec.buildExpression()).append(" )");
			}
			
			sb.append(" )");	// end extended section enclosing bracket
		}
		
		// end create extended mode expert condition
		
		sb.append(" )"); // END all enclosing bracket

		if (isEasyModeAlwaysAllowCoachesAndAdmins()) {
			if (needsAmpersand) {
				sb.append(" | ");
				sb.append(" ( isCourseCoach(0) | isCourseAdministrator(0) )");
				needsAmpersand = true;
			}
			// else makes no sense
		}

		if (sb.length() == 4) {
			// don't use "( )" as a condition!
			return null;
		} else {
			return sb.toString();
		}
	}

	public void setConditionId(String condId) {
		conditionId = condId;
	}

	public String getConditionId() {
		return conditionId;
	}

	public void clearEasyConfig() {
		// do not clear this.condition = null; as this will be set during a save in easy mode
		// do not clear this.conditionId = null;
		this.easyModeAlwaysAllowCoachesAndAdmins = false;
		this.easyModeBeginDate = null;
		this.easyModeCoachesAndAdmins = false;
		this.easyModeCutValue = null;
		this.easyModeEndDate = null;
		this.easyModeGroupAccess = null;
		this.easyModeGroupAreaAccess = null;
		this.easyModeNodePassedId = null;
		this.attributeConditions = null;
		// do not clear this.expertMode = false;
	}

	/**
	 * @see java.lang.Object#clone()
	 */
	protected Object clone() throws CloneNotSupportedException {
		super.clone();
		Condition retVal = new Condition();
		retVal.condition = this.condition;
		retVal.conditionId = this.conditionId;
		retVal.easyModeAlwaysAllowCoachesAndAdmins = this.easyModeAlwaysAllowCoachesAndAdmins;
		retVal.easyModeBeginDate = this.easyModeBeginDate;
		retVal.easyModeCoachesAndAdmins = this.easyModeCoachesAndAdmins;
		retVal.easyModeCutValue = this.easyModeCutValue;
		retVal.easyModeEndDate = this.easyModeEndDate;
		retVal.easyModeGroupAccess = this.easyModeGroupAccess;
		retVal.easyModeGroupAreaAccess = this.easyModeGroupAreaAccess;
		retVal.easyModeNodePassedId = this.easyModeNodePassedId;
		retVal.expertMode = this.expertMode;
		retVal.condition = this.condition;
		retVal.attributeConditions = this.attributeConditions;
		retVal.attributeconditionsConnectorIsAND = this.attributeconditionsConnectorIsAND;
		return retVal;
	}
	/**
	 * Set the extendedConditions list.
	 */
	public void setAttributeConditions(List<ExtendedCondition> extendedConditions) {
		this.attributeConditions = extendedConditions;
	}

	/**
	 * @return Returns the extendedConditions list in correct order.
	 */
	public List<ExtendedCondition> getAttributeConditions() {
		return attributeConditions;
	}

	/**
	 * @return Returns true, if the conditions connector is AND, false otherwise.
	 */
	public boolean isConditionsConnectorIsAND() {
		return (attributeconditionsConnectorIsAND == null) ? true : attributeconditionsConnectorIsAND.booleanValue();
	}

	/**
	 * @param conditionsConnectorIsAND Set to true, if the conditions connector is AND, to false otherwise.
	 */
	public void setAttributeConditionsConnectorIsAND(Boolean conditionsConnectorIsAND) {
		this.attributeconditionsConnectorIsAND = conditionsConnectorIsAND;
	}
}
