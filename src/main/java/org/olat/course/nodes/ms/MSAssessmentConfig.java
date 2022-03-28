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
package org.olat.course.nodes.ms;

import org.olat.core.logging.OLATRuntimeException;
import org.olat.course.assessment.handler.ModuleAssessmentConfig;
import org.olat.course.nodes.MSCourseNode;
import org.olat.modules.ModuleConfiguration;

/**
 * 
 * Initial date: 19 Aug 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class MSAssessmentConfig extends ModuleAssessmentConfig {

	public MSAssessmentConfig(ModuleConfiguration config) {
		super(config);
	}

	@Override
	public Mode getScoreMode() {
		String scoreKey = config.getStringValue(MSCourseNode.CONFIG_KEY_SCORE);
		return !MSCourseNode.CONFIG_VALUE_SCORE_NONE.equals(scoreKey)? Mode.setByNode: Mode.none;
	}
	
	@Override
	public Float getMaxScore() {
		if (Mode.none == getScoreMode()) {
			throw new OLATRuntimeException(MSAssessmentConfig.class, "getMaxScore not defined when hasScoreConfigured set to false", null);
		}
		return MSCourseNode.getMinMax(config).getMax();
	}
	
	@Override
	public Float getMinScore() {
		if (Mode.none == getScoreMode()) {
			throw new OLATRuntimeException(MSAssessmentConfig.class, "getMinScore not defined when hasScoreConfigured set to false", null);
		}
		return MSCourseNode.getMinMax(config).getMin();
	}

	@Override
	public boolean hasAttempts() {
		return false;
	}

	@Override
	public boolean hasStatus() {
		return false;
	}

	@Override
	public Boolean getInitialUserVisibility(boolean done, boolean coachCanNotEdit) {
		return coachCanNotEdit? Boolean.FALSE: Boolean.TRUE;
	}

	@Override
	public boolean isEditable() {
		// manual scoring fields can be edited manually
		return true;
	}

	@Override
	public boolean isBulkEditable() {
		return true;
	}

	@Override
	public boolean hasEditableDetails() {
		return config.getBooleanSafe(MSCourseNode.CONFIG_KEY_EVAL_FORM_ENABLED);
	}

}
