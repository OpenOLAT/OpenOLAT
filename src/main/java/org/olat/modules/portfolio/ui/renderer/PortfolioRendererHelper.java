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
package org.olat.modules.portfolio.ui.renderer;

import org.olat.core.gui.translator.Translator;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.modules.portfolio.AssessmentSection;

/**
 * 
 * Initial date: 23.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PortfolioRendererHelper {
	
	public static String getFormattedResult(AssessmentSection assessmentSection, Translator translator) {
		String val = "";
		if(assessmentSection != null) {
			if(assessmentSection.getScore() != null) {
				if(assessmentSection.getPassed() != null) {
					if(assessmentSection.getPassed().booleanValue()) {
						val = translator.translate("table.grading.passed.points",
								AssessmentHelper.getRoundedScore(assessmentSection.getScore()));
					} else {
						val = translator.translate("table.grading.failed.points",
								AssessmentHelper.getRoundedScore(assessmentSection.getScore()));
					}
				} else {
					val = translator.translate("table.grading.points",
							AssessmentHelper.getRoundedScore(assessmentSection.getScore()));
				}
			} else if(assessmentSection.getPassed() != null) {
				if(assessmentSection.getPassed().booleanValue()) {
					val = translator.translate("passed.true");
				} else {
					val = translator.translate("passed.false");
				}
			}
		}
		return val;
	}

}
