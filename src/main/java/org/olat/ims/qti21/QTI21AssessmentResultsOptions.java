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
package org.olat.ims.qti21;

import java.beans.Transient;
import java.util.Collection;

import org.olat.core.util.StringHelper;
import org.olat.ims.qti21.QTI21DeliveryOptions.ShowResultsOnFinish;

/**
 * 
 * Initial date: 24 févr. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21AssessmentResultsOptions {
	
	public static final String METADATA = "metadata";
	public static final String SECTION_SUMMARY = "sectionsSummary";
	public static final String QUESTION_SUMMARY = "questionSummary";
	public static final String USER_SOLUTIONS = "userSolutions";
	public static final String CORRECT_SOLUTIONS = "correctSolutions";
	
	private final boolean metadata;
	private final boolean sectionSummary;
	private final boolean questionSummary;
	private final boolean userSolutions;
	private final boolean correctSolutions;
	
	/** Not used but need to say for XStream deserialization */
	private boolean questions;
	
	public QTI21AssessmentResultsOptions(boolean metadata, boolean sectionSummary,
			boolean questionSummary, boolean userSolutions, boolean correctSolutions) {
		this.metadata = metadata;
		this.sectionSummary = sectionSummary;
		this.questionSummary = questionSummary;
		this.userSolutions = userSolutions;
		this.correctSolutions = correctSolutions;
	}

	public boolean isMetadata() {
		return metadata;
	}

	public boolean isSectionSummary() {
		return sectionSummary;
	}
	
	public boolean isQuestionSummary() {
		return questionSummary;
	}

	public boolean isQuestions() {
		return questions;
	}

	public boolean isUserSolutions() {
		return userSolutions;
	}

	public boolean isCorrectSolutions() {
		return correctSolutions;
	}
	
	@Override
	public QTI21AssessmentResultsOptions clone() {
		return new QTI21AssessmentResultsOptions(metadata, sectionSummary, questionSummary, userSolutions, correctSolutions);
	}

	@Transient
	public boolean none() {
		return !metadata && !sectionSummary && !questionSummary && !userSolutions && !correctSolutions;
	}

	@Transient
	public static final QTI21AssessmentResultsOptions allOptions() {
		return new QTI21AssessmentResultsOptions(true, true, true, true, true);
	}

	@Transient
	public static final QTI21AssessmentResultsOptions noOptions() {
		return new QTI21AssessmentResultsOptions(false, false, false, false, false);
	}

	@Transient
	public static final QTI21AssessmentResultsOptions convert(ShowResultsOnFinish showResults) {
		QTI21AssessmentResultsOptions options;
		if(showResults == null) {
			options = noOptions();
		} else {
			switch(showResults) {
				case none: options = noOptions(); break;
				case compact: options = new QTI21AssessmentResultsOptions(true, false, false, false, false); break;
				case sections: options = new QTI21AssessmentResultsOptions(true, true, false, false, false); break;
				case details: options = allOptions(); break;
				default: options = noOptions();
			}
		}
		return options;
	}
	
	public static QTI21AssessmentResultsOptions parseString(String value) {
		if(StringHelper.containsNonWhitespace(value)) {
			switch(value) {
				case QTI21Constants.QMD_ENTRY_SUMMARY_NONE: return noOptions();
				case QTI21Constants.QMD_ENTRY_SUMMARY_COMPACT: return new QTI21AssessmentResultsOptions(true, false, false, false, false);
				case QTI21Constants.QMD_ENTRY_SUMMARY_SECTION: return new QTI21AssessmentResultsOptions(true, true, false, false, false);
				case QTI21Constants.QMD_ENTRY_SUMMARY_DETAILED: return allOptions();
				default: {
					boolean metadata = value.contains(METADATA);
					boolean sections = value.contains(SECTION_SUMMARY);
					boolean questionSummary = value.contains(QUESTION_SUMMARY);
					boolean userSolutions = value.contains(USER_SOLUTIONS);
					boolean correctSolutions = value.contains(CORRECT_SOLUTIONS);
					return new QTI21AssessmentResultsOptions(metadata, sections, questionSummary, userSolutions, correctSolutions);
				}
			}
		}
		return noOptions();
	}
	
	public static String toString(Collection<String> selections) {
		StringBuilder sb = new StringBuilder();
		for(String selection:selections) {
			if(sb.length() > 0) sb.append(",");
			sb.append(selection);
		}
		return sb.toString();
	}
}
