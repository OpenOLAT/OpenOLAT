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
import java.util.ArrayList;
import java.util.List;

import org.olat.core.util.StringHelper;

/**
 * Initial Date: Jan 30, 2004
 * @author Mike Stock Comment:
 */
public class Condition implements Serializable, Cloneable {
	private transient String conditionId = null;
	private String condition = null;
	private boolean expertMode = false;

	private String easyModeBeginDate;
	private String easyModeEndDate;
	private String easyModeGroupAccess;
	private String easyModeGroupAccessIds;
	private String easyModeGroupAreaAccess;
	private String easyModeGroupAreaAccessIds;
	private String easyModeNodePassedId;
	private String easyModeCutValue;

	// true: ONLY coaches and admins have access, students are blocked out
	// false: no such rule
	private boolean easyModeCoachesAndAdmins;

	// true: coaches and admins have ALWAYS access
	// false: rules defined for students do also apply for coaches and
	// administrators
	private boolean easyModeAlwaysAllowCoachesAndAdmins;
	
	// true: only in assessment mode
	private boolean assessmentMode;
	private boolean assessmentModeViewResults;
	private String easyModeAssessmentModeNodeId;

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

	public String getConditionUpgraded() {
		return null;
	}

	public void setConditionUpgraded(String conditionUpgraded) {
		//only for compatibility reason, don't delete these methods
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

	public String getEasyModeGroupAccessIds() {
		return easyModeGroupAccessIds;
	}
	
	public void setEasyModeGroupAccessIds(String easyModeGroupAccessIds) {
		if (easyModeGroupAccessIds != null && easyModeGroupAccessIds.equals("")) {
			this.easyModeGroupAccessIds = null;
		} else {
			this.easyModeGroupAccessIds = easyModeGroupAccessIds;
		}
	}
	
	public List<Long> getEasyModeGroupAccessIdList() {
		return getAccessIdList(easyModeGroupAccessIds);
	}
	
	public void setEasyModeGroupAccessIdList(List<Long> keys) {
		setEasyModeGroupAccessIds(getAccessIdList(keys));
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
	
	public String getEasyModeGroupAreaAccessIds() {
		return easyModeGroupAreaAccessIds;
	}
	
	public void setEasyModeGroupAreaAccessIds(String easyModeGroupAreaAccessIds) {
		if (easyModeGroupAreaAccessIds != null && easyModeGroupAreaAccessIds.equals("")) {
			this.easyModeGroupAreaAccessIds = null;
		} else {
			this.easyModeGroupAreaAccessIds = easyModeGroupAreaAccessIds;
		}
	}
	
	public List<Long> getEasyModeGroupAreaAccessIdList() {
		return getAccessIdList(easyModeGroupAreaAccessIds);
	}
	
	public void setEasyModeGroupAreaAccessIdList(List<Long> keys) {
		setEasyModeGroupAreaAccessIds(getAccessIdList(keys));
	}
	
	private final String getAccessIdList(List<Long> keys) {
		if(keys == null || keys.isEmpty()) return null;
		StringBuilder sb = new StringBuilder();
		for(Long key:keys) {
			if(sb.length() > 0) sb.append(",");
			sb.append(key);
		}
		return sb.toString();
	}
	
	private final List<Long> getAccessIdList(String ids) {
		if(StringHelper.containsNonWhitespace(ids)) {
			String[] longStrArr = ids.split(",");
			List<Long> keys = new ArrayList<>(longStrArr.length);
			for(String longStr:longStrArr) {
				keys.add(Long.valueOf(longStr.trim()));
			}
			return keys;
		}
		return new ArrayList<>();
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

	public boolean isAssessmentMode() {
		return assessmentMode;
	}

	public void setAssessmentMode(boolean assessmentMode) {
		this.assessmentMode = assessmentMode;
	}

	public boolean isAssessmentModeViewResults() {
		return assessmentModeViewResults;
	}

	public void setAssessmentModeViewResults(boolean viewResults) {
		this.assessmentModeViewResults = viewResults;
	}

	public String getEasyModeAssessmentModeNodeId() {
		return easyModeAssessmentModeNodeId;
	}

	public void setEasyModeAssessmentModeNodeId(String nodeId) {
		this.easyModeAssessmentModeNodeId = nodeId;
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
		StringBuilder sb = new StringBuilder(512);

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
		
		
		if (getEasyModeGroupAccess() != null || getEasyModeGroupAccessIds() != null) {
			if (getEasyModeGroupAreaAccess() != null || getEasyModeGroupAreaAccessIds() != null) {
				if (needsAmpersand) sb.append(" & (");
				else sb.append(" (");
			} else if (needsAmpersand) {
				sb.append(" & ");
			}
			// Delimiter for more than one groups is a comma
			String[] groups = getEasyModeGroupAccessIds() == null ? 
					getEasyModeGroupAccess().split(",") : getEasyModeGroupAccessIds().split(",");
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
		if (getEasyModeGroupAreaAccess() != null || getEasyModeGroupAreaAccessIds() != null) {
			if ((getEasyModeGroupAccess() != null || getEasyModeGroupAccessIds() != null) && needsAmpersand) {
				sb.append("|");
			} else if (needsAmpersand) {
				sb.append(" & ");
			}
			// Delimiter for more than one group area is a comma
			String[] areas = getEasyModeGroupAreaAccessIds() == null ?
					getEasyModeGroupAreaAccess().split(",") : getEasyModeGroupAreaAccessIds().split(",");
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
			if (getEasyModeGroupAccess() != null || getEasyModeGroupAccessIds() != null) {
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
		if(isAssessmentMode()) {
			if (needsAmpersand) sb.append(" & ");
			
			if(isAssessmentModeViewResults()) {
				sb.append(" isAssessmentMode(\"").append(getEasyModeAssessmentModeNodeId()).append("\",true)");
			} else {
				sb.append(" isAssessmentMode(0)");
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
		// do not clear the conditionId
		this.easyModeAlwaysAllowCoachesAndAdmins = false;
		this.easyModeBeginDate = null;
		this.easyModeCoachesAndAdmins = false;
		this.easyModeCutValue = null;
		this.easyModeEndDate = null;
		this.easyModeGroupAccess = null;
		this.easyModeGroupAccessIds = null;
		this.easyModeGroupAreaAccess = null;
		this.easyModeGroupAreaAccessIds = null;
		this.easyModeNodePassedId = null;
		this.attributeConditions = null;
		this.assessmentMode = false;
		this.assessmentModeViewResults = false;
		this.easyModeAssessmentModeNodeId = null;
	}

	/**
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Condition clone() {
		Condition retVal = new Condition();
		retVal.condition = this.condition;
		retVal.conditionId = this.conditionId;
		retVal.easyModeAlwaysAllowCoachesAndAdmins = this.easyModeAlwaysAllowCoachesAndAdmins;
		retVal.easyModeBeginDate = this.easyModeBeginDate;
		retVal.easyModeCoachesAndAdmins = this.easyModeCoachesAndAdmins;
		retVal.easyModeCutValue = this.easyModeCutValue;
		retVal.easyModeEndDate = this.easyModeEndDate;
		retVal.easyModeGroupAccess = this.easyModeGroupAccess;
		retVal.easyModeGroupAccessIds = this.easyModeGroupAccessIds;
		retVal.easyModeGroupAreaAccess = this.easyModeGroupAreaAccess;
		retVal.easyModeGroupAreaAccessIds = this.easyModeGroupAreaAccessIds;
		retVal.easyModeNodePassedId = this.easyModeNodePassedId;
		retVal.expertMode = this.expertMode;
		retVal.condition = this.condition;
		retVal.attributeConditions = this.attributeConditions;
		retVal.attributeconditionsConnectorIsAND = this.attributeconditionsConnectorIsAND;
		retVal.assessmentMode = this.assessmentMode;
		retVal.assessmentModeViewResults = this.assessmentModeViewResults;
		retVal.easyModeAssessmentModeNodeId = this.easyModeAssessmentModeNodeId;
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
