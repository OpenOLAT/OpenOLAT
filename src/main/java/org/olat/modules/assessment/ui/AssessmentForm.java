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

package org.olat.modules.assessment.ui;

import java.math.BigDecimal;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.IntegerElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.AssessmentModule;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.AssessmentService;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.ui.event.AssessmentFormEvent;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * Initial Date:  Jun 24, 2004
 *
 * @author gnaegi
 */
public class AssessmentForm extends FormBasicController {
	
	private TextElement score;
	private IntegerElement attempts;
	private SingleSelection passed;
	private TextElement userComment;
	private TextElement coachComment;
	private FormLink reopenLink;
	private FormLink intermediateSaveLink;
	private FormLink saveAndDoneButton;

	private Double min;
	private Double max;
	private Double cut;
	private Identity assessedIdentity;
	private AssessmentEntry assessmentEntry;
	private RepositoryEntry testEntry;
	private final AssessableResource assessableElement;
	
	@Autowired
	private AssessmentService assessmentService;
	
	/**
	 * Constructor for an assessment detail form. The form will be configured according
	 * to the assessable course node parameters
	 * @param name The form name
	 * @param assessableCourseNode The course node
	 * @param assessedIdentityWrapper The wrapped identity
	 * @param trans The package translator
	 */
	public AssessmentForm(UserRequest ureq, WindowControl wControl, Identity assessedIdentity, RepositoryEntry testEntry,
			AssessableResource assessableElement) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(AssessmentModule.class, getLocale(), getTranslator()));
		
		this.testEntry = testEntry;
		this.assessedIdentity = assessedIdentity;
		this.assessableElement = assessableElement;
		assessmentEntry = assessmentService.loadAssessmentEntry(assessedIdentity, testEntry, null, testEntry);

		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("assessment.edit.title", null);
		formLayout.setElementCssClass("o_sel_assessment_form");

		if (assessableElement.hasAttemptsConfigured()) {
			Integer attemptsValue = assessmentEntry == null ? null : assessmentEntry.getAttempts();
			attempts = uifactory.addIntegerElement("attempts", "form.attempts", (attemptsValue == null ? 0 : attemptsValue.intValue()), formLayout);
			attempts.setDisplaySize(3);
			attempts.setMinValueCheck(0, null);
			if (assessableElement.hasMaxAttemptsConfigured()) {
				attempts.setExampleKey("form.attempts.example", new String[] {Integer.toString(assessableElement.getMaxAttempts())});
			}
		}

		if (assessableElement.hasScoreConfigured()) {
			min = assessableElement.getMinScoreConfiguration();
			max = assessableElement.getMaxScoreConfiguration();
			if (assessableElement.hasPassedConfigured()) {
				cut = assessableElement.getCutValueConfiguration();
			}
			
			String minStr = AssessmentHelper.getRoundedScore(min);
			String maxStr = AssessmentHelper.getRoundedScore(max);
			uifactory.addStaticTextElement("minval", "form.min", ((min == null) ? translate("form.valueUndefined") : minStr), formLayout);
			uifactory.addStaticTextElement("maxval", "form.max", ((max == null) ? translate("form.valueUndefined") : maxStr), formLayout);

			// Use init variables from wrapper, already loaded from db
			score = uifactory.addTextElement("score","form.score" , 10, "", formLayout);
			score.setDisplaySize(4);
			score.setElementCssClass("o_sel_assessment_form_score");
			score.setExampleKey("form.score.rounded", null);
			if (assessmentEntry != null && assessmentEntry.getScore() != null) {
				score.setValue(AssessmentHelper.getRoundedScore(assessmentEntry.getScore()));
			} 
			// assessment overview with max score
			score.setRegexMatchCheck("(\\d+)||(\\d+\\.\\d{1,3})||(\\d+\\,\\d{1,3})", "form.error.wrongFloat");
		}

		if (assessableElement.hasPassedConfigured()) {
			if (cut != null) {
				// Display cut value if defined
				String val = ((cut == null) ? translate("form.valueUndefined") : AssessmentHelper.getRoundedScore(cut));
				uifactory.addStaticTextElement("cutval","form.cut", val, formLayout);
			}
			
			String[] trueFalseKeys = new String[] { "undefined", "true", "false" };
			String[] passedNotPassedValues = new String[] {
					translate("form.passed.undefined"),
					translate("form.passed.true"),
					translate("form.passed.false")
			};

			//passed = new StaticSingleSelectionElement("form.passed", trueFalseKeys, passedNotPassedValues);
			passed = uifactory.addRadiosVertical("passed", "form.passed", formLayout, trueFalseKeys, passedNotPassedValues);	
			passed.setElementCssClass("o_sel_assessment_form_passed");
			
			Boolean passedValue = assessmentEntry == null ? null : assessmentEntry.getPassed();
			passed.select(passedValue == null ? "undefined" :passedValue.toString(), true);
			// When cut value is defined, no manual passed possible
			passed.setEnabled(cut == null);
		}

		if (assessableElement.hasCommentConfigured()) {
			// Use init variables from db, not available from wrapper
			String comment = assessmentEntry == null ? null : assessmentEntry.getComment();
			if (comment == null) {
				comment = "";
			}
			userComment = uifactory.addTextAreaElement("usercomment", "form.usercomment", 2500, 5, 40, true, false, comment, formLayout);
		}

		String coachCommentValue = assessmentEntry == null ? null : assessmentEntry.getCoachComment();
		if (coachCommentValue == null) {
			coachCommentValue = "";
		}
		coachComment = uifactory.addTextAreaElement("coachcomment", "form.coachcomment", 2500, 5, 40, true, false, coachCommentValue, formLayout);
	
		//why does the TextElement not use its default error key??? 
		//userComment could be null for course elements of type Assessment (MSCourseNode)
		if(userComment!=null) {
			userComment.setNotLongerThanCheck(2500, "input.toolong");
		}
		if(coachComment!=null) {
			coachComment.setNotLongerThanCheck(2500, "input.toolong");
		}
		
		FormLayoutContainer buttonGroupLayout = FormLayoutContainer.createButtonLayout("buttonGroupLayout", getTranslator());
		formLayout.add(buttonGroupLayout);
		
		intermediateSaveLink = uifactory.addFormLink("save.intermediate", buttonGroupLayout, Link.BUTTON);
		
		saveAndDoneButton = uifactory.addFormLink("assessment.set.status.done", buttonGroupLayout, Link.BUTTON);
		saveAndDoneButton.setIconLeftCSS("o_icon o_icon-fw o_icon_status_done");
		saveAndDoneButton.setPrimary(true);
		
		reopenLink = uifactory.addFormLink("reopen", buttonGroupLayout, Link.BUTTON);
		reopenLink.setElementCssClass("o_sel_assessment_form_reopen");

		uifactory.addFormCancelButton("cancel", buttonGroupLayout, ureq, getWindowControl());
		updateStatus(assessmentEntry);
	}
	
	private void updateStatus(AssessmentEntry aEntry) {
		boolean closed = (aEntry != null && aEntry.getAssessmentStatus() == AssessmentEntryStatus.done);
		
		if(assessableElement.hasPassedConfigured()) {
			passed.setEnabled(!closed && cut == null);
		}
		
		if(assessableElement.hasScoreConfigured()) {
			score.setEnabled(!closed);
		}
		
		if(assessableElement.hasCommentConfigured()) {
			userComment.setEnabled(!closed);
		}
		coachComment.setEnabled(!closed);
			
		if (assessableElement.hasAttemptsConfigured()) {
			attempts.setEnabled(!closed);
		}
		
		intermediateSaveLink.setVisible(!closed);
		saveAndDoneButton.setVisible(!closed);
		reopenLink.setVisible(closed);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(intermediateSaveLink == source) {
			if(validateFormLogic(ureq)) {
				doUpdateAssessmentData(false);
				fireEvent(ureq, new AssessmentFormEvent(AssessmentFormEvent.ASSESSMENT_CHANGED, true));
			}
		} else if(saveAndDoneButton == source) {
			if(validateFormLogic(ureq)) {
				doUpdateAssessmentData(true);
				fireEvent(ureq, new AssessmentFormEvent(AssessmentFormEvent.ASSESSMENT_DONE, true));
			}
		} else if(reopenLink == source) {
			doReopen();
			fireEvent(ureq, new AssessmentFormEvent(AssessmentFormEvent.ASSESSMENT_CHANGED, false));
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formCancelled (UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		if (assessableElement.hasScoreConfigured()) {
			try {
				if(parseFloat(score) == null) {
					score.setErrorKey("form.error.wrongFloat", null);
					return false;
				}
			} catch (NumberFormatException e) {
				score.setErrorKey("form.error.wrongFloat", null);
				return false;
			}
			
			Float fscore = parseFloat(score);
			if ((min != null && fscore < min.floatValue()) 
					|| fscore < AssessmentHelper.MIN_SCORE_SUPPORTED) {
				score.setErrorKey("form.error.scoreOutOfRange", null);
				return false;
			}
			if ((max != null && fscore > max.floatValue())
					|| fscore > AssessmentHelper.MAX_SCORE_SUPPORTED) {
				score.setErrorKey("form.error.scoreOutOfRange", null);
				return false;
			}
		}	
		return true;
	}
	
	private Float parseFloat(TextElement textEl) throws NumberFormatException {
		String scoreStr = textEl.getValue();
		if(!StringHelper.containsNonWhitespace(scoreStr)) {
			return null;
		}
		int index = scoreStr.indexOf(',');
		if(index >= 0) {
			scoreStr = scoreStr.replace(',', '.');
			return Float.parseFloat(scoreStr);
		}
		return Float.parseFloat(scoreStr);
	}
	
	private void doReopen() {
		assessmentEntry = assessmentService.loadAssessmentEntry(assessedIdentity, testEntry, null, testEntry);
		if (assessmentEntry != null) {
			assessmentEntry.setAssessmentStatus(AssessmentEntryStatus.inReview);
			assessmentEntry = assessmentService.updateAssessmentEntry(assessmentEntry);
			updateStatus(assessmentEntry);
		}
	}
	
	protected void doUpdateAssessmentData(boolean asDone) {
		assessmentEntry = assessmentService.loadAssessmentEntry(assessedIdentity, testEntry, null, testEntry);
		if(assessmentEntry == null) {
			assessmentEntry = assessmentService.getOrCreateAssessmentEntry(assessedIdentity, null, testEntry, null, Boolean.TRUE, testEntry);
		}
		
		if (assessableElement.hasAttemptsConfigured()) {
			if(StringHelper.containsNonWhitespace(attempts.getValue())) {
				Integer attemptsValue = Integer.parseInt(attempts.getValue());
				assessmentEntry.setAttempts(attemptsValue);
			} else {
				assessmentEntry.setAttempts(null);
			}
		}
		
		if (assessableElement.hasScoreConfigured()) {
			if(StringHelper.containsNonWhitespace(score.getValue())) {
				BigDecimal scoreValue = new BigDecimal(score.getValue());
				assessmentEntry.setScore(scoreValue);
			}
		}
		
		if (assessableElement.hasPassedConfigured()) {
			if (assessableElement.getCutValueConfiguration() != null) {
				if (StringHelper.containsNonWhitespace(score.getValue())) {
					Double scoreValue = Double.valueOf(score.getValue());
					Boolean passed = scoreValue.doubleValue() >= assessableElement.getCutValueConfiguration().doubleValue()
							? Boolean.TRUE
							: Boolean.FALSE;
					assessmentEntry.setPassed(passed);
				} else {
					assessmentEntry.setPassed(null);
				}
			} else {
				String selected = passed.getSelectedKey();
				if("true".equals(selected)) {
					assessmentEntry.setPassed(Boolean.TRUE);
				} else if("false".equals(selected)) {
					assessmentEntry.setPassed(Boolean.FALSE);
				} else {
					assessmentEntry.setPassed(null);
				}
			}
		}
		
		if(asDone) {
			assessmentEntry.setAssessmentStatus(AssessmentEntryStatus.done);
		}
		assessmentEntry = assessmentService.updateAssessmentEntry(assessmentEntry);
		flc.setDirty(true);
	}
	
	public void reloadData() {
		assessmentEntry = assessmentService.loadAssessmentEntry(assessedIdentity, testEntry, null, testEntry);
		
		if (assessableElement.hasAttemptsConfigured()) {
			Integer attemptsValue = assessmentEntry.getAttempts();
			attempts.setIntValue(attemptsValue == null ? 0 : attemptsValue.intValue());
		}
		
		if (assessableElement.hasScoreConfigured()) {
			if (assessmentEntry.getScore() != null) {
				score.setValue(AssessmentHelper.getRoundedScore(assessmentEntry.getScore()));
			} 
		}
		
		if (assessableElement.hasPassedConfigured()) {
			Boolean passedValue = assessmentEntry.getPassed();
			passed.select(passedValue == null ? "undefined" : passedValue.toString(), true);
			passed.setEnabled(cut == null);
		}
		
		if (assessableElement.hasCommentConfigured()) {
			String comment = assessmentEntry.getComment() == null ? "" : assessmentEntry.getComment();
			userComment.setValue(comment);
		}
		String coachMsg = assessmentEntry.getCoachComment() == null ? "" : assessmentEntry.getCoachComment();
		coachComment.setValue(coachMsg);
		
		updateStatus(assessmentEntry);
	}
}
