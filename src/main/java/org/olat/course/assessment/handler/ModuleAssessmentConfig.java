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
package org.olat.course.assessment.handler;

import org.olat.core.logging.OLATRuntimeException;
import org.olat.course.nodes.MSCourseNode;
import org.olat.modules.ModuleConfiguration;

/**
 * 
 * Initial date: 19 Aug 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public abstract class ModuleAssessmentConfig implements AssessmentConfig {
	
	protected final ModuleConfiguration config;
	
	public ModuleAssessmentConfig(ModuleConfiguration config) {
		this.config = config;
	}

	@Override
	public boolean isAssessable() {
		return true;
	}

	@Override
	public boolean isEvaluationPersisted() {
		return true;
	}

	@Override
	public boolean isEvaluationCalculated() {
		return false;
	}

	@Override
	public boolean hasScore() {
		return config.getBooleanSafe(MSCourseNode.CONFIG_KEY_HAS_SCORE_FIELD);
	}

	@Override
	public Float getMaxScore() {
		if (!hasScore()) {
			throw new OLATRuntimeException(ModuleAssessmentConfig.class, "getMaxScore not defined when hasScoreConfigured set to false", null);
		}
		return config.getFloatEntry(MSCourseNode.CONFIG_KEY_SCORE_MAX);
	}

	@Override
	public Float getMinScore() {
		if (!hasScore()) {
			throw new OLATRuntimeException(ModuleAssessmentConfig.class, "getMinScore not defined when hasScore set to false", null);
		}
		return config.getFloatEntry(MSCourseNode.CONFIG_KEY_SCORE_MIN);
	}
	
	@Override
	public boolean hasPassed() {
		return config.getBooleanSafe(MSCourseNode.CONFIG_KEY_HAS_PASSED_FIELD);
	}
	
	@Override
	public Float getCutValue() {
		if (!hasPassed()) {
			throw new OLATRuntimeException(ModuleAssessmentConfig.class, "getCutValue not defined when hasPassed set to false", null);
		}
		return config.getFloatEntry(MSCourseNode.CONFIG_KEY_PASSED_CUT_VALUE);
	}
	
	@Override
	public Mode getCompletionMode() {
		return Mode.none;
	}

	@Override
	public boolean hasComment() {
		return config.getBooleanSafe(MSCourseNode.CONFIG_KEY_HAS_COMMENT_FIELD);
	}
	
	@Override
	public boolean hasIndividualAsssessmentDocuments() {
		return config.getBooleanSafe(MSCourseNode.CONFIG_KEY_HAS_INDIVIDUAL_ASSESSMENT_DOCS);
	}

	@Override
	public boolean isAssessedBusinessGroups() {
		return false;
	}

}
