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

package org.olat.course.assessment.ui.tool;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.NavigableSet;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.Logger;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.dropdown.DropdownItem;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.IntegerElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.media.FileMediaResource;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.gui.render.DomWrapperElement;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.AssessmentModule;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.STCourseNode;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.assessment.Role;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.ui.AssessedIdentityListController;
import org.olat.modules.assessment.ui.event.AssessmentFormEvent;
import org.olat.modules.grade.GradeModule;
import org.olat.modules.grade.GradeScale;
import org.olat.modules.grade.GradeScoreRange;
import org.olat.modules.grade.GradeService;
import org.olat.modules.grade.ui.GradeUIFactory;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * Initial Date:  Jun 24, 2004
 *
 * @author gnaegi
 */
public class AssessmentForm extends FormBasicController {
	
	private static final Logger log = Tracing.createLoggerFor(AssessmentForm.class);
	
	private static final String CMD_INTERMEDIATE_VISIBLE = "intermediate.visible";
	private static final String CMD_INTERMEDIATE_HIDDEN = "intermediate.hidden";
	private static final String CMD_DONE_VISIBLE = "done.visible";
	private static final String CMD_DONE_HIDDEN = "done.hidden";
	
	private StaticTextElement statusEl;
	private StaticTextElement userVisibilityEl;
	private TextElement score;
	private FormLayoutContainer gradeCont;
	private StaticTextElement gradeEl;
	private FormLink gradeApplyLink;
	private IntegerElement attempts;
	private StaticTextElement cutVal;
	private SingleSelection passed;
	private TextElement userComment;
	private TextElement coachComment;
	private FormLayoutContainer docsLayoutCont;
	private FileElement uploadDocsEl;
	private FormLink reopenLink;
	private FormLink intermediateSaveLink;
	private DropdownItem intermediateSaveDropdown;
	private FormLink intermediateSaveUserVisibilityLink;
	private FormLink saveAndDoneLink;
	private DropdownItem saveAndDoneDropdown;
	private FormLink saveAndDoneAdditionalLink;
	private List<DocumentWrapper> assessmentDocuments;
	
	private DialogBoxController confirmDeleteDocCtrl;
	
	private final AssessmentConfig assessmentConfig;
	private final boolean hasScore, hasGrade, autoGrade, hasPassed, hasComment, hasIndividualAssessmentDocs, hasAttempts;
	private Float min, max, cut;
	private final Integer maxAttempts;

	private final UserCourseEnvironment coachCourseEnv;
	private final UserCourseEnvironment assessedUserCourseEnv;
	private final CourseNode courseNode;
	
	private int counter = 0;

	private Integer attemptsValue;
	private Float scoreValue;
	private String userCommentValue, coachCommentValue;
	private Boolean userVisibilityValue;
	private GradeScale gradeScale;
	private NavigableSet<GradeScoreRange> gradeScoreRanges;
	private boolean gradeApplied;
	
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	@Autowired
	private GradeModule gradeModule;
	@Autowired
	private GradeService gradeService;


	
	/**
	 * Constructor for an assessment detail form. The form will be configured according
	 * to the assessable course node parameters
	 * @param name The form name
	 * @param courseNode The course node
	 * @param assessedIdentityWrapper The wrapped identity
	 * @param trans The package translator
	 */
	public AssessmentForm(UserRequest ureq, WindowControl wControl, CourseNode courseNode,
			UserCourseEnvironment coachCourseEnv, UserCourseEnvironment assessedUserCourseEnv) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		setTranslator(Util.createPackageTranslator(AssessmentModule.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(CourseNode.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(GradeUIFactory.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(AssessedIdentityListController.class, getLocale(), getTranslator()));
		
		assessmentConfig = courseAssessmentService.getAssessmentConfig(courseNode);
		hasAttempts = assessmentConfig.hasAttempts();
		hasScore = Mode.none != assessmentConfig.getScoreMode();
		hasGrade = hasScore && assessmentConfig.hasGrade() && gradeModule.isEnabled();
		autoGrade = assessmentConfig.isAutoGrade();
		gradeApplied = autoGrade;
		hasPassed = Mode.none != assessmentConfig.getPassedMode();
		hasComment = assessmentConfig.hasComment();
		hasIndividualAssessmentDocs = assessmentConfig.hasIndividualAsssessmentDocuments();
		maxAttempts = assessmentConfig.hasMaxAttempts()? assessmentConfig.getMaxAttempts(): null;
		
		this.coachCourseEnv = coachCourseEnv;
		this.assessedUserCourseEnv = assessedUserCourseEnv;
		this.courseNode = courseNode;

		initForm(ureq);
	}

	public boolean isAttemptsDirty() {
		if(hasAttempts) {
			if(attemptsValue == null) {
				return attempts.getIntValue() > 0;
			} else {
				return attemptsValue.intValue() != attempts.getIntValue();
			}
		}
		return false;
	}
	
	public int getAttempts() {
		return attempts.getIntValue();
	}

	public Float getCut() {
		return cut;
	}

	public StaticTextElement getCutVal() {
		return cutVal;
	}

	public boolean isHasAttempts() {
		return hasAttempts;
	}

	public boolean isHasComment() {
		return hasComment;
	}

	public boolean isHasPassed() {
		return hasPassed;
	}

	public boolean isHasScore() {
		return hasScore;
	}

	public SingleSelection getPassed() {
		return passed;
	}

	public boolean isScoreDirty() {
		if (!hasScore) return false;
		if (scoreValue == null) {
			return StringHelper.containsNonWhitespace(score.getValue());
		}
		return parseFloat(score) != scoreValue.floatValue();
	}
	
	public Float getScore() {
		return parseFloat(score);
	}

	public boolean isUserCommentDirty () {
		if(!hasComment) {
			return false;
		}
		
		if(!StringHelper.containsNonWhitespace(userComment.getValue()) && !StringHelper.containsNonWhitespace(userCommentValue)) {
			return false;
		}
		return !userComment.getValue().equals(userCommentValue);
	}
	public TextElement getUserComment() {
		return userComment;
	}
	
	public boolean isCoachCommentDirty () {
		if(!StringHelper.containsNonWhitespace(coachComment.getValue()) && !StringHelper.containsNonWhitespace(coachCommentValue)) {
			return false;
		}
		return !coachComment.getValue().equals(coachCommentValue);
	}
	
	public TextElement getCoachComment() {
		return coachComment;
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == confirmDeleteDocCtrl) {
			if(DialogBoxUIFactory.isOkEvent(event) || DialogBoxUIFactory.isYesEvent(event)) {
				File documentToDelete = (File)confirmDeleteDocCtrl.getUserObject();
				doDeleteAssessmentDocument(documentToDelete);
				reloadAssessmentDocs();
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == score) {
			updateGradeUI();
		} else if (source == gradeApplyLink) {
			doApplyGrade();
		} else if(intermediateSaveLink == source || intermediateSaveUserVisibilityLink == source) {
			if(validateFormLogic(ureq)) {
				FormLink link = (FormLink)source;
				boolean visible = CMD_INTERMEDIATE_VISIBLE.equals(link.getCmd());
				doUpdateAssessmentData(false, visible);
				fireEvent(ureq, new AssessmentFormEvent(AssessmentFormEvent.ASSESSMENT_CHANGED, true));
			}
		} else if(saveAndDoneLink == source || saveAndDoneAdditionalLink == source) {
			if(validateFormLogic(ureq)) {
				FormLink link = (FormLink)source;
				boolean visible = CMD_DONE_VISIBLE.equals(link.getCmd());
				doUpdateAssessmentData(true, visible);
				fireEvent(ureq, new AssessmentFormEvent(AssessmentFormEvent.ASSESSMENT_DONE, true));
			}
		} else if(reopenLink == source) {
			doReopen();
			fireEvent(ureq, new AssessmentFormEvent(AssessmentFormEvent.ASSESSMENT_REOPEN, false));
		} else if(uploadDocsEl == source) {
			if(uploadDocsEl.getUploadFile() != null && StringHelper.containsNonWhitespace(uploadDocsEl.getUploadFileName())) {
				courseAssessmentService.addIndividualAssessmentDocument(courseNode,
						uploadDocsEl.getUploadFile(), uploadDocsEl.getUploadFileName(), assessedUserCourseEnv,
						getIdentity());
				reloadAssessmentDocs();
				uploadDocsEl.reset();
			}
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			Object uobject = link.getUserObject();
			if(link.getCmd() != null && link.getCmd().startsWith("delete_doc_") && uobject instanceof DocumentWrapper) {
				DocumentWrapper wrapper = (DocumentWrapper)uobject;
				doConfirmDeleteAssessmentDocument(ureq, wrapper.getDocument());
			}
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
		boolean allOk = super.validateFormLogic(ureq);
		
		if (score != null) {
			score.clearError();
		}
		if (hasScore && score.isEnabled()) {
			Float fscore = null;
			try {
				fscore = parseFloat(score);
				if(fscore == null) {
					score.setErrorKey("form.error.wrongFloat", null);
					allOk &= false;
				}
			} catch (NumberFormatException e) {
				score.setErrorKey("form.error.wrongFloat", null);
				allOk &= false;
			}
			
			if(fscore != null) {
				if ((min != null && fscore < min.floatValue()) 
						|| fscore < AssessmentHelper.MIN_SCORE_SUPPORTED) {
					score.setErrorKey("form.error.scoreOutOfRange", null);
					allOk &= false;
				}
				if ((max != null && fscore > max.floatValue())
						|| fscore > AssessmentHelper.MAX_SCORE_SUPPORTED) {
					score.setErrorKey("form.error.scoreOutOfRange", null);
					allOk &= false;
				}
			}
		}
		
		if(attempts != null) {
			attempts.clearError();
			allOk &= attempts.validateIntValue();
		}
		return allOk;
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
		ScoreEvaluation scoreEval = assessedUserCourseEnv.getScoreAccounting().evalCourseNode(courseNode);
		if (scoreEval != null) {
			ScoreEvaluation reopenedEval = new ScoreEvaluation(scoreEval.getScore(), scoreEval.getGrade(),
					scoreEval.getPerformanceClassIdent(), scoreEval.getPassed(), AssessmentEntryStatus.inReview,
					scoreEval.getUserVisible(), scoreEval.getCurrentRunStartDate(), scoreEval.getCurrentRunCompletion(),
					scoreEval.getCurrentRunStatus(), scoreEval.getAssessmentID());
			courseAssessmentService.updateScoreEvaluation(courseNode, reopenedEval, assessedUserCourseEnv,
					getIdentity(), false, Role.coach);
			updateStatus(reopenedEval);
		}
	}
	
	private void doConfirmDeleteAssessmentDocument(UserRequest ureq, File document) {
		String title = translate("warning.assessment.docs.delete.title");
		String text = translate("warning.assessment.docs.delete.text",
				new String[] { StringHelper.escapeHtml(document.getName()) });
		confirmDeleteDocCtrl = activateOkCancelDialog(ureq, title, text, confirmDeleteDocCtrl);
		confirmDeleteDocCtrl.setUserObject(document);
	}
	
	private void doDeleteAssessmentDocument(File document) {
		courseAssessmentService.removeIndividualAssessmentDocument(courseNode, document,
				assessedUserCourseEnv, getIdentity());
	}
	
	protected void doUpdateAssessmentData(boolean setAsDone, boolean visibility) {
		Float updatedScore = null;
		GradeScoreRange gradeScoreRange = null;
		String updateGrade = null;
		String updatePerformanceClassIdent = null;
		Boolean updatedPassed = null;

		if (isHasAttempts() && isAttemptsDirty()) {
			int updatedAttempts = getAttempts();
			Date lastAttempt = assessedUserCourseEnv.getScoreAccounting().evalCourseNode(courseNode).getLastAttempt();
			lastAttempt = updatedAttempts > 0? lastAttempt: null;
			courseAssessmentService.updateAttempts(courseNode, Integer.valueOf(updatedAttempts), lastAttempt,
					assessedUserCourseEnv, getIdentity(), Role.coach);
		}

		if (isHasScore()) {
			if(isScoreDirty()) {
				updatedScore = getScore();
			} else {
				updatedScore = scoreValue;
			}
		}
		
		if (hasGrade && gradeApplied && score != null) {
			gradeScoreRange = gradeService.getGradeScoreRange(getGradeScoreRanges(), updatedScore);
			updateGrade = gradeScoreRange.getGrade();
			updatePerformanceClassIdent = gradeScoreRange.getPerformanceClassIdent();
		}
		
		if (isHasPassed()) {
			if (hasGrade) {
				if (gradeApplied && gradeScoreRange != null) {
					updatedPassed = Boolean.valueOf(gradeScoreRange.isPassed());
				}
			} else if (getCut() != null && getScore() != null) {
				updatedPassed = updatedScore.floatValue() >= getCut().floatValue() ? Boolean.TRUE : Boolean.FALSE;
			} else {
				//"passed" info was changed or not 
				String selectedKeyString = getPassed().getSelectedKey();
				if("true".equalsIgnoreCase(selectedKeyString) || "false".equalsIgnoreCase(selectedKeyString)) {
					updatedPassed = Boolean.valueOf(selectedKeyString);
				}			
			}
		}
		
		userVisibilityValue = Boolean.valueOf(visibility);
		
		// Update score,passed properties in db
		ScoreEvaluation scoreEval;
		if(setAsDone) {
			scoreEval = new ScoreEvaluation(updatedScore, updateGrade, updatePerformanceClassIdent, updatedPassed,
					AssessmentEntryStatus.done, userVisibilityValue, null, null, null, null);
		} else {
			scoreEval = new ScoreEvaluation(updatedScore, updateGrade, updatePerformanceClassIdent, updatedPassed,
					null, userVisibilityValue, null, null, null, null);
		}
		courseAssessmentService.updateScoreEvaluation(courseNode, scoreEval, assessedUserCourseEnv,
				getIdentity(), false, Role.coach);

		if (isHasComment() && isUserCommentDirty()) {
			String newComment = getUserComment().getValue();
			courseAssessmentService.updatedUserComment(courseNode, newComment, assessedUserCourseEnv, getIdentity());
		}

		if (isCoachCommentDirty()) {
			String newCoachComment = getCoachComment().getValue();
			courseAssessmentService.updateCoachComment(courseNode, newCoachComment, assessedUserCourseEnv);
		}
	}
	
	private void reloadAssessmentDocs() {
		if(docsLayoutCont == null) return;
		
		List<File> documents = courseAssessmentService.getIndividualAssessmentDocuments(courseNode,
				assessedUserCourseEnv);
		List<DocumentWrapper> wrappers = new ArrayList<>(documents.size());
		for (File document : documents) {
			DocumentWrapper wrapper = new DocumentWrapper(document);
			wrappers.add(wrapper);
			
			FormLink deleteButton = uifactory.addFormLink("delete_doc_" + (++counter), "delete", null, docsLayoutCont, Link.BUTTON_XSMALL);
			deleteButton.setEnabled(true);  
			deleteButton.setVisible(true);
			wrapper.setDeleteButton(deleteButton);
		}
		docsLayoutCont.contextPut("documents", wrappers);
		assessmentDocuments = wrappers;
	}
	
	private void updateStatus(ScoreEvaluation scoreEval) {
		boolean closed = (scoreEval != null && scoreEval.getAssessmentStatus() == AssessmentEntryStatus.done);
		
		if(hasPassed) {
			passed.setEnabled(!closed && !hasGrade && cut == null && !coachCourseEnv.isCourseReadOnly());
		}
		
		if(hasScore) {
			score.setEnabled(!closed && !coachCourseEnv.isCourseReadOnly());
		}
		
		if(gradeApplyLink != null) {
			gradeApplyLink.setEnabled(!closed && !coachCourseEnv.isCourseReadOnly());
		}
		
		if(hasComment) {
			userComment.setEnabled(!closed && !coachCourseEnv.isCourseReadOnly());
		}
		if(hasIndividualAssessmentDocs) {
			uploadDocsEl.setVisible(!closed && !coachCourseEnv.isCourseReadOnly());
		}
		
		coachComment.setEnabled(!closed && !coachCourseEnv.isCourseReadOnly());
			
		if (hasAttempts) {
			attempts.setEnabled(!closed && !coachCourseEnv.isCourseReadOnly());
		}
		
		if(assessmentDocuments != null) {
			for(DocumentWrapper assessmentDoc:assessmentDocuments) {
				FormLink deleteButton = assessmentDoc.getDeleteButton();
				if(deleteButton != null) {
					deleteButton.setEnabled(!closed && !coachCourseEnv.isCourseReadOnly());
					deleteButton.setVisible(!closed && !coachCourseEnv.isCourseReadOnly());
				}
			}
		}
		
		saveAndDoneLink.setVisible(!closed && !coachCourseEnv.isCourseReadOnly());
		if (saveAndDoneDropdown != null) {
			saveAndDoneDropdown.setVisible(!closed && !coachCourseEnv.isCourseReadOnly());
		}
		if (saveAndDoneAdditionalLink != null) {
			saveAndDoneAdditionalLink.setVisible(!closed && !coachCourseEnv.isCourseReadOnly());
		}
		intermediateSaveLink.setVisible(!closed && !coachCourseEnv.isCourseReadOnly());
		if (intermediateSaveDropdown != null) {
			intermediateSaveDropdown.setVisible(!closed && !coachCourseEnv.isCourseReadOnly());
		}
		if (intermediateSaveUserVisibilityLink != null) {
			intermediateSaveUserVisibilityLink.setVisible(!closed && !coachCourseEnv.isCourseReadOnly());
		}
		reopenLink.setVisible(closed && !coachCourseEnv.isCourseReadOnly());
		flc.setDirty(true);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
		ScoreEvaluation scoreEval = assessedUserCourseEnv.getScoreAccounting().evalCourseNode(courseNode);
		if (scoreEval == null) {
			scoreEval = ScoreEvaluation.EMPTY_EVALUATION;
		}
		
		FormLayoutContainer generalCont = FormLayoutContainer.createDefaultFormLayout("general", getTranslator());
		generalCont.setRootForm(mainForm);
		formLayout.add("general", generalCont);
		
		String statusText = new AssessmentStatusCellRenderer(getTranslator(), true).render(scoreEval.getAssessmentStatus());
		statusEl = uifactory.addStaticTextElement("status", statusText, generalCont);
		statusEl.setDomWrapperElement(DomWrapperElement.div);
		
		String userVisibilityText = new UserVisibilityCellRenderer(true).render(scoreEval.getUserVisible(), getTranslator());
		userVisibilityEl = uifactory.addStaticTextElement("user.visibility", userVisibilityText, generalCont);
		userVisibilityEl.setDomWrapperElement(DomWrapperElement.div);
		
		FormLayoutContainer assessmentCont = FormLayoutContainer.createDefaultFormLayout("assessment", getTranslator());
		assessmentCont.setElementCssClass("o_sel_assessment_form");
		assessmentCont.setFormTitle(translate("personal.title"));
		assessmentCont.setRootForm(mainForm);
		formLayout.add("assessment", assessmentCont);
		
		if (hasAttempts) {
			attemptsValue = courseAssessmentService.getAttempts(courseNode, assessedUserCourseEnv);
			if(attemptsValue == null) {
				attemptsValue = Integer.valueOf(0);
			}
			attempts = uifactory.addIntegerElement("attempts", "form.attempts", attemptsValue.intValue(), assessmentCont);
			attempts.setDisplaySize(3);
			attempts.setMinValueCheck(0, null);
			if (maxAttempts != null) {
				attempts.setExampleKey("form.attempts.example", new String[] {Integer.toString(maxAttempts)});
			}
		}

		if (hasScore) {
			min = assessmentConfig.getMinScore();
			max = assessmentConfig.getMaxScore();
			if (hasPassed && !hasGrade) {
				cut = assessmentConfig.getCutValue();
			}
			
			String scoreMinMax = AssessmentHelper.getMinMax(getTranslator(), min, max);
			if (scoreMinMax != null) {
				uifactory.addStaticTextElement("score.min.max", scoreMinMax, assessmentCont);
			}
			
			// Use init variables from wrapper, already loaded from db
			scoreValue = scoreEval.getScore();
			score = uifactory.addTextElement("score","form.score" , 10, "", assessmentCont);
			score.setDisplaySize(4);
			score.setElementCssClass("o_sel_assessment_form_score");
			score.setExampleKey("form.score.rounded", null);
			if (scoreValue != null) {
				score.setValue(AssessmentHelper.getRoundedScore(scoreValue));
			} 
			// assessment overview with max score
			score.setRegexMatchCheck("(\\d+)||(\\d+\\.\\d{1,3})||(\\d+\\,\\d{1,3})", "form.error.wrongFloat");
			if (hasGrade) {
				score.addActionListener(FormEvent.ONCHANGE);
			}
		}
		
		if (hasGrade) {
			gradeCont = FormLayoutContainer.createButtonLayout("gradeCont", getTranslator());
			gradeCont.setElementCssClass("o_inline_cont");
			gradeCont.setLabel("grade", null);
			gradeCont.setRootForm(mainForm);
			assessmentCont.add(gradeCont);
			
			gradeEl = uifactory.addStaticTextElement("grade", "", gradeCont);
			gradeEl.setDomWrapperElement(DomWrapperElement.span);
			
			gradeApplyLink = uifactory.addFormLink("grade.apply", gradeCont, Link.BUTTON);
			
			if (!autoGrade) {
				gradeApplied = StringHelper.containsNonWhitespace(scoreEval.getGrade());
			}
		}
		
		if (hasPassed) {
			if (cut != null) {
				// Display cut value if defined
				cutVal = uifactory.addStaticTextElement(
						"cutval","form.cut" ,
						((cut == null) ? translate("form.valueUndefined") : AssessmentHelper.getRoundedScore(cut)),
						assessmentCont
				);
			}
			
			String[] trueFalseKeys = new String[] { "undefined", "true", "false" };
			String[] passedNotPassedValues = new String[] {
					translate("form.passed.undefined"),
					translate("form.passed.true"),
					translate("form.passed.false")
			};

			passed = uifactory.addRadiosVertical("passed", "form.passed", assessmentCont, trueFalseKeys, passedNotPassedValues);	
			passed.setElementCssClass("o_sel_assessment_form_passed");
			
			Boolean passedValue = scoreEval.getPassed();
			passed.select(passedValue == null ? "undefined" :passedValue.toString(), true);
			passed.setEnabled(!hasGrade && cut == null);
		}

		if (hasComment) {
			userCommentValue = courseAssessmentService.getUserComment(courseNode, assessedUserCourseEnv);
			userComment = uifactory.addTextAreaElement("usercomment", "form.usercomment", 2500, 5, 40, true, false, userCommentValue, assessmentCont);
			userComment.setNotLongerThanCheck(2500, "input.toolong");
		}
		
		if(hasIndividualAssessmentDocs) {
			String mapperUri = registerCacheableMapper(ureq, null, new DocumentMapper());
			String page = velocity_root + "/individual_assessment_docs.html"; 
			docsLayoutCont = FormLayoutContainer.createCustomFormLayout("form.individual.assessment.docs", getTranslator(), page);
			docsLayoutCont.setLabel("form.individual.assessment.docs", null);
			docsLayoutCont.contextPut("mapperUri", mapperUri);
			assessmentCont.add(docsLayoutCont);

			uploadDocsEl = uifactory.addFileElement(getWindowControl(), getIdentity(), "form.upload", null, assessmentCont);
			uploadDocsEl.addActionListener(FormEvent.ONCHANGE);
		}
		
		coachCommentValue = courseAssessmentService.getCoachComment(courseNode, assessedUserCourseEnv);
		coachComment = uifactory.addTextAreaElement("coachcomment", "form.coachcomment", 2500, 5, 40, true, false, coachCommentValue, assessmentCont);
		coachComment.setNotLongerThanCheck(2500, "input.toolong");
		
		boolean canChangeUserVisibility = coachCourseEnv.isAdmin()
				|| coachCourseEnv.getCourseEnvironment().getRunStructure().getRootNode().getModuleConfiguration().getBooleanSafe(STCourseNode.CONFIG_COACH_USER_VISIBILITY);
		
		userVisibilityValue = scoreEval.getUserVisible();
		if (userVisibilityValue == null) {
			userVisibilityValue = assessmentConfig.getInitialUserVisibility(true, !canChangeUserVisibility);
		}
		
		FormLayoutContainer buttonGroupLayout = FormLayoutContainer.createCustomFormLayout("buttons", getTranslator(), velocity_root + "/assessment_edit_buttons.html");
		assessmentCont.add(buttonGroupLayout);
		
		String intermediateName = "save.intermediate";
		String intermediateSaveIconCSS = "o_icon o_icon-fw o_icon_results_hidden";
		String cmdIntermediate = CMD_INTERMEDIATE_HIDDEN;
		String toggleUserVisibility = "save.intermediate.set.visible";
		String intermediateSaveUserVisivilityIconCSS = "o_icon o_icon-fw o_icon_results_visible";
		String cmdIntermediateToggle = CMD_INTERMEDIATE_VISIBLE;
		if (scoreEval.getUserVisible() != null && scoreEval.getUserVisible().booleanValue()) {
			intermediateSaveIconCSS = "o_icon o_icon-fw o_icon_results_visible";
			cmdIntermediate = CMD_INTERMEDIATE_VISIBLE;
			intermediateSaveUserVisivilityIconCSS = "o_icon o_icon-fw o_icon_results_hidden";
			cmdIntermediateToggle = CMD_INTERMEDIATE_HIDDEN;
			toggleUserVisibility = "save.intermediate.set.hidden";
		} else if (!canChangeUserVisibility && scoreEval.getUserVisible() == null && userVisibilityValue.booleanValue()) {
			intermediateName = "save.intermediate.set.visible";
			intermediateSaveIconCSS = "o_icon o_icon-fw o_icon_results_visible";
			cmdIntermediate = CMD_INTERMEDIATE_VISIBLE;
		}
		
		intermediateSaveLink = uifactory.addFormLink("save.intermediate", cmdIntermediate, intermediateName, null, buttonGroupLayout, Link.BUTTON);
		intermediateSaveLink.setElementCssClass("o_sel_assessment_form_save_and_close");
		intermediateSaveLink.setIconLeftCSS(intermediateSaveIconCSS);
		
		if (canChangeUserVisibility) {
			intermediateSaveDropdown = uifactory.addDropdownMenu("save.intermediate.more", null, buttonGroupLayout, getTranslator());
			intermediateSaveDropdown.setOrientation(DropdownOrientation.right);
			
			intermediateSaveUserVisibilityLink = uifactory.addFormLink(toggleUserVisibility, cmdIntermediateToggle, toggleUserVisibility, null, buttonGroupLayout, Link.LINK);
			intermediateSaveUserVisibilityLink.setIconLeftCSS(intermediateSaveUserVisivilityIconCSS);
			intermediateSaveDropdown.addElement(intermediateSaveUserVisibilityLink);
		}
		
		String doneName = "assessment.set.status.done.visible";
		String doneIconCSS = "o_icon o_icon-fw o_icon_results_visible";
		String doneCmd = CMD_DONE_VISIBLE;
		String doneAddName = "assessment.set.status.done";
		String donAddIconCSS = "o_icon o_icon-fw o_icon_results_hidden";
		String doneAddCmd = CMD_DONE_HIDDEN;
		if (scoreEval.getUserVisible() != null && scoreEval.getUserVisible().booleanValue()) {
			doneName = "assessment.set.status.done";
			doneAddName = "assessment.set.status.done.hidden";
		} else if (scoreEval.getUserVisible() != null && !scoreEval.getUserVisible().booleanValue() && !canChangeUserVisibility) {
			doneName = "assessment.set.status.done";
			doneIconCSS = "o_icon o_icon-fw o_icon_results_hidden";
			doneCmd = CMD_DONE_HIDDEN;
		} else if (scoreEval.getUserVisible() == null) {
			if (!userVisibilityValue.booleanValue()) {
				doneName = "assessment.set.status.done";
				doneIconCSS = "o_icon o_icon-fw o_icon_results_hidden";
				doneCmd = CMD_DONE_HIDDEN;
				doneAddName = "assessment.set.status.done.visible";
				donAddIconCSS = "o_icon o_icon-fw o_icon_results_visible";
				doneAddCmd = CMD_DONE_VISIBLE;
			}
		}
		saveAndDoneLink = uifactory.addFormLink("save.done", doneCmd, doneName, null, buttonGroupLayout, Link.BUTTON);
		saveAndDoneLink.setElementCssClass("o_sel_assessment_form_save_and_done");
		saveAndDoneLink.setIconLeftCSS(doneIconCSS);
		saveAndDoneLink.setPrimary(true);
		
		if (canChangeUserVisibility) {
			saveAndDoneDropdown = uifactory.addDropdownMenu("save.done.more", null, buttonGroupLayout, getTranslator());
			saveAndDoneDropdown.setOrientation(DropdownOrientation.right);
			saveAndDoneDropdown.setPrimary(true);
			
			saveAndDoneAdditionalLink = uifactory.addFormLink("save.done.add", doneAddCmd, doneAddName, null, buttonGroupLayout, Link.LINK);
			saveAndDoneAdditionalLink.setIconLeftCSS(donAddIconCSS);
			saveAndDoneDropdown.addElement(saveAndDoneAdditionalLink);
		}
		
		reopenLink = uifactory.addFormLink("reopen", buttonGroupLayout, Link.BUTTON);
		reopenLink.setElementCssClass("o_sel_assessment_form_reopen");
		reopenLink.setIconLeftCSS("o_icon o_icon_status_in_review o_icon-fw");

		uifactory.addFormCancelButton("cancel", buttonGroupLayout, ureq, getWindowControl());

		reloadAssessmentDocs();
		updateStatus(scoreEval);
		updateGradeUI();
	}

	private void doApplyGrade() {
		gradeApplied = true;
		updateGradeUI();
	}
	
	private void updateGradeUI() {
		if (!hasGrade) return;
		
		GradeScoreRange gradeScoreRange = null;
		String grade = null;
		String performanceClassIdent = null;
		try {
			Float newScore = Float.valueOf(score.getValue().replace(',', '.'));
			gradeScoreRange = gradeService.getGradeScoreRange(getGradeScoreRanges(), newScore);
			grade = gradeScoreRange.getGrade();
			performanceClassIdent = gradeScoreRange.getPerformanceClassIdent();
		} catch (NumberFormatException e) {
			//
		} catch (Exception e) {
			log.error("", e);
		}
		setGradeValue(grade, performanceClassIdent);
		
		if (passed != null) {
			if (gradeScoreRange == null) {
				passed.select("undefined", true);
			} else if (gradeScoreRange.isPassed()) {
				passed.select("true", true);
			} else {
				passed.select("false", true);
			}
		}
	}
	
	private void setGradeValue(String grade, String performanceClassIdent) {
		if (StringHelper.containsNonWhitespace(grade) && getGradeScale() != null) {
			String translatedGrade = GradeUIFactory.translatePerformanceClass(getTranslator(), performanceClassIdent, grade);
			String translateGradeSystem = GradeUIFactory.translateGradeSystem(getTranslator(), getGradeScale().getGradeSystem());
			String gradeValue = translate("grade.with.system", translatedGrade, translateGradeSystem);
			if (!gradeApplied) {
				gradeValue = translate("grade.not.applied", gradeValue);
			}
			gradeEl.setValue(gradeValue);
		} else {
			gradeEl.setValue("");
		}
		
		gradeApplyLink.setVisible(!gradeApplied && StringHelper.containsNonWhitespace(gradeEl.getValue())
				&& (coachCourseEnv.isAdmin() || coachCourseEnv.getCourseEnvironment().getRunStructure().getRootNode().getModuleConfiguration().getBooleanSafe(STCourseNode.CONFIG_COACH_GRADE_APPLY)));
	}

	private NavigableSet<GradeScoreRange> getGradeScoreRanges() {
		if (gradeScoreRanges == null) {
			gradeScoreRanges = gradeService.getGradeScoreRanges(getGradeScale(), getLocale());
		}
		return gradeScoreRanges;
	}
	
	private GradeScale getGradeScale() {
		if (gradeScale == null) {
			gradeScale = gradeService.getGradeScale(
					assessedUserCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry(),
					courseNode.getIdent());
		}
		return gradeScale;
	}

	public static class DocumentWrapper {
		
		private final File document;
		private FormLink deleteButton;
		
		public DocumentWrapper(File document) {
			this.document = document;
		}
		
		public String getFilename() {
			return document.getName();
		}
		
		public String getLabel() {
			return document.getName() + " (" + Formatter.formatBytes(document.length()) + ")";
		}
		
		public File getDocument() {
			return document;
		}

		public FormLink getDeleteButton() {
			return deleteButton;
		}

		public void setDeleteButton(FormLink deleteButton) {
			this.deleteButton = deleteButton;
			deleteButton.setUserObject(this);
		}
	}
	
	public class DocumentMapper implements Mapper {

		@Override
		public MediaResource handle(String relPath, HttpServletRequest request) {
			if(StringHelper.containsNonWhitespace(relPath)) {
				if(relPath.startsWith("/")) {
					relPath = relPath.substring(1, relPath.length());
				}
			
				@SuppressWarnings("unchecked")
				List<DocumentWrapper> wrappers = (List<DocumentWrapper>)docsLayoutCont.contextGet("documents");
				if(wrappers != null) {
					for(DocumentWrapper wrapper:wrappers) {
						if(relPath.equals(wrapper.getFilename())) {
							return new FileMediaResource(wrapper.getDocument(), true);
						}
					}
				}
			}
			return new NotFoundMediaResource();
		}
		
	}
}
