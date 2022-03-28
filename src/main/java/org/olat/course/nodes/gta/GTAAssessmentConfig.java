/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.course.nodes.gta;

import org.olat.course.assessment.handler.ModuleAssessmentConfig;
import org.olat.course.nodes.GTACourseNode;
import org.olat.modules.ModuleConfiguration;

/**
 * 
 * Initial date: 19 Aug 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class GTAAssessmentConfig extends ModuleAssessmentConfig {

	private final boolean hasNoGrading;

	public GTAAssessmentConfig(ModuleConfiguration config) {
		super(config);
		this.hasNoGrading = !config.getBooleanSafe(GTACourseNode.GTASK_GRADING);
		
	}

	@Override
	public Mode getScoreMode() {
		if (hasNoGrading) return Mode.none;
		
		return super.getScoreMode();
	}
	
	@Override
	public Mode getPassedMode() {
		if (hasNoGrading) return Mode.none;
		
		return super.getPassedMode();
	}

	@Override
	public boolean hasAttempts(){
		return true;
	}
	
	@Override
	public boolean hasComment() {
		if (hasNoGrading) return false;
		
		return super.hasComment();
	}
	
	@Override
	public boolean hasIndividualAsssessmentDocuments() {
		if (hasNoGrading) return false;
		
		return super.hasIndividualAsssessmentDocuments();
	}

	@Override
	public boolean hasStatus() {
		return true; // Task Course node has always a status-field
	}

	@Override
	public Boolean getInitialUserVisibility(boolean done, boolean coachCanNotEdit) {
		return coachCanNotEdit? Boolean.FALSE: Boolean.TRUE;
	}

	@Override
	public boolean isAssessedBusinessGroups() {
		return GTAType.group.name().equals(config.getStringValue(GTACourseNode.GTASK_TYPE));
	}

	@Override
	public boolean isEditable() {
		return true;
	}

	@Override
	public boolean isBulkEditable() {
		return true;
	}

	@Override
	public boolean hasEditableDetails() {
		return config.getBooleanSafe(GTACourseNode.GTASK_ASSIGNMENT)
				|| config.getBooleanSafe(GTACourseNode.GTASK_SUBMIT)
				|| config.getBooleanSafe(GTACourseNode.GTASK_REVIEW_AND_CORRECTION)
				|| config.getBooleanSafe(GTACourseNode.GTASK_REVISION_PERIOD);
	}
	
}
