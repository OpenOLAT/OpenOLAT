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
package org.olat.course.nodes.ta;

import org.olat.course.assessment.handler.ModuleAssessmentConfig;
import org.olat.course.nodes.TACourseNode;
import org.olat.modules.ModuleConfiguration;

/**
 * 
 * Initial date: 19 Aug 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class TAAssessmentConfig extends ModuleAssessmentConfig {

	private final boolean hasNoScoring;

	public TAAssessmentConfig(ModuleConfiguration config) {
		super(config);
		this.hasNoScoring = !config.getBooleanSafe(TACourseNode.CONF_SCORING_ENABLED);
	}

	@Override
	public Mode getScoreMode() {
		if (hasNoScoring) return Mode.none;
		
		return super.getScoreMode();
	}
	
	@Override
	public Mode getPassedMode() {
		if (hasNoScoring) return Mode.none;
		
		return super.getPassedMode();
	}

	@Override
	public boolean hasAttempts() {
		return true;
	}
	
	@Override
	public boolean hasComment() {
		if (hasNoScoring) return false;
		
		return super.hasComment();
	}

	@Override
	public boolean hasIndividualAsssessmentDocuments() {
		return false;
	}

	@Override
	public boolean hasStatus() {
		return true; // Task Course node has always a status-field
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
		return config.getBooleanSafe(TACourseNode.CONF_TASK_ENABLED)
				|| config.getBooleanSafe(TACourseNode.CONF_DROPBOX_ENABLED)
				|| config.getBooleanSafe(TACourseNode.CONF_RETURNBOX_ENABLED);
	}

}
