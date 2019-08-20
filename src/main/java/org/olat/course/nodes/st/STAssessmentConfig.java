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
package org.olat.course.nodes.st;

import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.util.StringHelper;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.run.scoring.ScoreCalculator;

/**
 * 
 * Initial date: 19 Aug 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class STAssessmentConfig implements AssessmentConfig {
	
	private final ScoreCalculator scoreCalculator;

	public STAssessmentConfig(ScoreCalculator scoreCalculator) {
		this.scoreCalculator = scoreCalculator;
	}

	@Override
	public boolean hasScoreConfigured() {
		if (scoreCalculator != null && StringHelper.containsNonWhitespace(scoreCalculator.getScoreExpression())) {
			return true;
		}
		return false;
	}

	@Override
	public Float getMaxScoreConfiguration() {
		throw new OLATRuntimeException(STAssessmentConfig.class, "Max score never defined for ST nodes", null);
	}

	@Override
	public Float getMinScoreConfiguration() {
		throw new OLATRuntimeException(STAssessmentConfig.class, "Min score never defined for ST nodes", null);
	}

	@Override
	public boolean hasPassedConfigured() {
		if (scoreCalculator != null && StringHelper.containsNonWhitespace(scoreCalculator.getPassedExpression())) {
			return true;
		}
		return false;
	}
	
	@Override
	public Float getCutValueConfiguration() {
		throw new OLATRuntimeException(STAssessmentConfig.class, "Cut value never defined for ST nodes", null);
	}

	@Override
	public boolean hasCompletion() {
		return false;
	}

	@Override
	public boolean hasAttemptsConfigured() {
		return false;
	}

	@Override
	public boolean hasCommentConfigured() {
		return false;
	}

	@Override
	public boolean hasIndividualAsssessmentDocuments() {
		return false;
	}

	@Override
	public boolean hasStatusConfigured() {
		return false;
	}

	@Override
	public boolean isAssessedBusinessGroups() {
		return false;
	}

	@Override
	public boolean isEditableConfigured() {
		// ST nodes never editable, data generated on the fly
		return false;
	}
}
