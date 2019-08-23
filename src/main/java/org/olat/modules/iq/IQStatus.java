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

package org.olat.modules.iq;

import java.util.Calendar;
import java.util.Date;

import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.ims.qti.QTIConstants;
import org.olat.ims.qti.container.ItemContext;
import org.olat.ims.qti.container.SectionContext;
import org.olat.ims.qti.navigator.Info;
import org.olat.ims.qti.process.AssessmentInstance;

/**
 * Initial Date:  Mar 4, 2004
 *
 * @author Mike Stock
 */
public class IQStatus {

	private Translator translator;
	private String title;
	private float score, maxScore;
	private int questionPos, maxQuestions;
	private long assessStart, assessMaxdur;
	private long itemStart, itemMaxdur;
	private int itemAttemptsLeft;
	private int numberOfNotRatedAnswers;
	private Info info;
	private boolean isPreview;
	private boolean isOpen;
	private boolean isSurvey;
	private boolean isAssess;
	private boolean isSelfAssess;
	private Formatter formatter;
	private String questionProgressLabel = "";

	/**
	 * @param translator
	 */
	public IQStatus(Translator translator) {
		this.translator = translator;
		this.formatter = Formatter.getInstance(translator.getLocale());
		info = new Info();
	}

	/**
	 * Update this status object with the given assessment instance
	 * @param ai assessment instance
	 */
	public void update(AssessmentInstance ai) {
		if (ai == null) {
			return;
		}
		if (title == null) title = ai.getAssessmentContext().getTitle();
		
		isOpen = !ai.isClosed();
		isSurvey = ai.isSurvey();
		if (!isSurvey) {
			score = ai.getAssessmentContext().getScore();
			maxScore = ai.getAssessmentContext().getMaxScore();
			numberOfNotRatedAnswers = ai.getAssessmentContext().getNumberOfItemsWithNanValueScore();
		}

		assessStart = ai.getAssessmentContext().getTimeOfStart();
		assessMaxdur = ai.getAssessmentContext().getDurationLimit();
		SectionContext sc = ai.getAssessmentContext().getCurrentSectionContext();
		if (sc != null && sc.getCurrentItemContextPos() != -1) {
			ItemContext itc = sc.getCurrentItemContext();
			itemStart = itc.getTimeOfStart();
			itemMaxdur = itc.getDurationLimit();
			itemAttemptsLeft = (itc.getMaxAttempts() == -1) ? -1 : (itc.getMaxAttempts() - itc.getTimesAnswered());
		} else {
			itemMaxdur = -1;
			itemAttemptsLeft = -1;
		}
		
		questionPos = ai.getAssessmentContext().getItemPosWithinAssessment();
		maxQuestions = ai.getAssessmentContext().getItemContextCount();
		isAssess = ai.isAssess();
		isSelfAssess = ai.isSelfAssess();
		info = ai.getNavigator().getInfo();
	}
	
	/**
	 * @return true if maxscore is not set to 0
	 */
	public boolean hasMaxScore() { return maxScore != 0.0; }
	
	/**
	 * @return The max score formated as string (max fraction digits 2)
	 */
	public String getMaxScore() {
		return StringHelper.formatFloat(maxScore, 2);
	}

	/**
	 * @return The score formated as string (max fraction digits 2)
	 */
	public String getScore() {
		return StringHelper.formatFloat(score, 2);
	}

	/**
	 * @return The number of answers that could not be rated (e.g. questions of type essay)
	 */
	public int getNumberOfNotRatedAnswers() {
		return numberOfNotRatedAnswers;
	}

	/**
	 * @return The current question position. Does not make sense in all constellations
	 */
	public String getQuestionPos() {
		return Integer.toString(questionPos);
	}
	
	/**
	 * @return Number of questions in this assessment context.
	 */
	public String getMaxQuestions() {
		return Integer.toString(maxQuestions);
	}
	
	/**
	 * @return Title of this assessment.
	 */
	public String getTitle() {
		return title;
	}
	
	/**
	 * @return True if timelimit on assessment
	 */
	public boolean hasAssessTimeLimit() {
		return (assessMaxdur != -1);
	}
	
	/**
	 * @return Formatted time limit.
	 */
	public String getAssessTimeLimit() {
		if (!isOpen || !hasAssessTimeLimit()) return "";
		long due = assessStart + assessMaxdur;
		StringBuilder results = new StringBuilder();
		results.append(IQComponentRenderer.getFormattedLimit(assessMaxdur));
		results.append("&nbsp;(");
		results.append(translator.translate("timelimit.end"));
		results.append("&nbsp;");
		results.append(formatter.formatTimeShort(new Date(due)));
		results.append(")");
		//counter
		long remain = due - Calendar.getInstance().getTimeInMillis();
		if (remain < 0) remain = 0;
		results.append("&nbsp; : <span id='o_assessmentremain'>" + remain + "</span>");		
		return results.toString();
	}
	
	/**
	 * @return True if timelimit on current item
	 */
	public boolean hasItemTimeLimit() {
		return (itemMaxdur != -1);
	}

	/**
	 * @return True if time is up, 
	 */
	public boolean hasItemTimeLimitExeeded() {
		if (!isOpen || !hasItemTimeLimit()) return false;
		long due = itemStart + itemMaxdur;
		if (due < Calendar.getInstance().getTimeInMillis()) return true;
		return false;
	}

	/**
	 * @return Formatted time limit.
	 */
	public String getItemTimeLimit() {
		if (!isOpen || !hasItemTimeLimit()) return "";
		long due = itemStart + itemMaxdur;
		StringBuilder results = new StringBuilder();
		results.append(IQComponentRenderer.getFormattedLimit(itemMaxdur));
		results.append("&nbsp(");
		results.append(translator.translate("timelimit.end"));
		results.append("&nbsp;");
		results.append(formatter.formatTimeShort(new Date(due)));
		results.append(")");
		//counter
		long remain = due - Calendar.getInstance().getTimeInMillis();
		if (remain < 0) remain = 0;
		results.append("&nbsp; : <span id='o_itemremain'>" + remain + "</span>");		
		return results.toString();
	}
	
	/**
	 * @return True if attempts limit on actual item.
	 */
	public boolean hasItemAttemptsLimit() {
		return (itemAttemptsLeft != -1);
	}
	
	/**
	 * @return Item attempts left
	 */
	public String getItemAttemptsLeft() {
		return translator.translate("attemptsleft", new String[] { "" + itemAttemptsLeft });
	}
	
	/**
	 * @return True if user has no more attempts
	 */
	public boolean hasAttemptsExeeded() {
		return (itemAttemptsLeft == 0 ? true : false);
	}

	
	/**
	 * @return true if assessment instance is not closed
	 */
	public boolean isOpen() { return isOpen; }
	/**
	 * @return true if assessment instance is not closed
	 */
	public boolean isClosed() { return !isOpen; }
	/**
	 * @return true if of type survey (questionnaire)
	 */
	public boolean isSurvey() { return isSurvey; }
	/**
	 * @return true if of type assess (test)
	 */
	public boolean isAssess() { return isAssess; }
	/**
	 * @return true if of type self-assess (self-test)
	 */
	public boolean isSelfAssess() { return isSelfAssess; }
	/**
	 * @return true if in preview mode
	 */
	public boolean isPreview() { return isPreview; }
	/**
	 * @param isPreview true: preview mode enabled, false: preview mode disabled
	 */
	public void setPreview(boolean isPreview) { this.isPreview = isPreview; }

	/**
	 * @return true if an error message is available
	 */
	public boolean hasError() { return info.containsError(); }
	/**
	 * @return true if a user message is available
	 */
	public boolean hasMessage() { return info.containsMessage(); }
	
	/**
	 * @return the user message if set
	 */
	public String getMessage() {
		switch (info.getMessage()) {
			case QTIConstants.MESSAGE_ASSESSMENT_SUBMITTED:
				return translator.translate("MESSAGE_ASSESSMENT_SUBMITTED");
			case QTIConstants.MESSAGE_ASSESSMENT_CANCELED:
				return translator.translate("MESSAGE_ASSESSMENT_CANCELED");
			case QTIConstants.MESSAGE_ITEM_SUBMITTED :
				return translator.translate("MESSAGE_ITEM_SUBMITTED");
			case QTIConstants.MESSAGE_SECTION_SUBMITTED :
				return translator.translate("MESSAGE_SECTION_SUBMITTED");
			case QTIConstants.MESSAGE_SECTION_INFODEMANDED : // for menu item navigator
				return translator.translate("MESSAGE_SECTION_INFODEMANDED");
			case QTIConstants.MESSAGE_ASSESSMENT_INFODEMANDED : // at the start of the test
				return translator.translate("MESSAGE_ASSESSMENT_INFODEMANDED");
		}
		return "";
	}
	
	/**
	 * @return the error messages if set
	 */
	public String getError() {
		switch (info.getError()) {
			case QTIConstants.ERROR_ASSESSMENT_OUTOFTIME :
				return translator.translate("ERROR_ASSESSMENT_OUTOFTIME");

			case QTIConstants.ERROR_ITEM_OUTOFTIME :
				return translator.translate("ERROR_ITEM_OUTOFTIME");

			case QTIConstants.ERROR_SECTION_OUTOFTIME :
				return translator.translate("ERROR_SECTION_OUTOFTIME");

			case QTIConstants.ERROR_SUBMITTEDITEM_OUTOFTIME :
				return translator.translate("ERROR_SUBMITTEDITEM_OUTOFTIME");

			case QTIConstants.ERROR_SUBMITTEDSECTION_OUTOFTIME :
				return translator.translate("ERROR_SUBMITTEDSECTION_OUTOFTIME");

			case QTIConstants.ERROR_SUBMITTEDITEM_TOOMANYATTEMPTS :
				return translator.translate("ERROR_SUBMITTEDITEM_TOOMANYATTEMPTS");
				
			case QTIConstants.ERROR_SECTION_PART_OUTOFTIME :
				return translator.translate("ERROR_SECTION_PART_OUTOFTIME");
		}
		return "";
	}
	
	public void setQuestionProgressLabel(String label) {
		questionProgressLabel = label;
	}
	
	public String getQuestionProgressLabel() {
		return questionProgressLabel;
	}
}
