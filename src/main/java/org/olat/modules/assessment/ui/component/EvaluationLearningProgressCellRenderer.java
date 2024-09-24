/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.assessment.ui.component;

import java.util.Locale;

import org.olat.course.run.scoring.AssessmentEvaluation;

/**
 * 
 * Initial date: 24 Sep 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class EvaluationLearningProgressCellRenderer extends AbstractLearningProgressCellRenderer {

	public EvaluationLearningProgressCellRenderer(Locale locale) {
		super(locale);
	}
	
	public EvaluationLearningProgressCellRenderer(Locale locale, boolean chartVisible, boolean labelVisible) {
		super(locale, chartVisible, labelVisible);
	}

	@Override
	protected AssessmentEvaluation getAssessmentEvaluation(Object cellValue) {
		if (cellValue instanceof AssessmentEvaluation evaluation) {
			return evaluation;
		}
		return null;
	}

	@Override
	protected float getActual(Object cellValue) {
		if (cellValue instanceof AssessmentEvaluation evaluation) {
			if (evaluation.getCompletion() != null) {
				return evaluation.getCompletion().floatValue();
			}
		}
		return 0;
	}

}
