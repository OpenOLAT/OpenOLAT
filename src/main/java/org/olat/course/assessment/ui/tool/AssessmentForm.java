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
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
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
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.media.FileMediaResource;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.AssessmentModule;
import org.olat.course.nodes.AssessableCourseNode;
import org.olat.course.run.scoring.ScoreAccounting;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.assessment.Role;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.ui.event.AssessmentFormEvent;


/**
 * Initial Date:  Jun 24, 2004
 * 
 * <ul>
 * 	
 * </ul>
 *
 * @author gnaegi
 */
public class AssessmentForm extends FormBasicController {
	
	private static final String[] userVisibilityKeys = new String[]{ "visible", "hidden" };
	
	private TextElement score;
	private IntegerElement attempts;
	private StaticTextElement cutVal;
	private SingleSelection passed, userVisibility;
	private TextElement userComment, coachComment;
	private FormLayoutContainer docsLayoutCont;
	private FileElement uploadDocsEl;
	private FormSubmit submitButton;
	private FormLink saveAndDoneLink, reopenLink;
	private List<DocumentWrapper> assessmentDocuments;
	
	private DialogBoxController confirmDeleteDocCtrl;
	
	private final boolean hasScore, hasPassed, hasComment, hasIndividualAssessmentDocs, hasAttempts;
	private Float min, max, cut;

	private final UserCourseEnvironment coachCourseEnv;
	private final UserCourseEnvironment assessedUserCourseEnv;
	private final AssessableCourseNode assessableCourseNode;
	
	private int counter = 0;

	private Integer attemptsValue;
	private Float scoreValue;
	private String userCommentValue, coachCommentValue;
	
	/**
	 * Constructor for an assessment detail form. The form will be configured according
	 * to the assessable course node parameters
	 * @param name The form name
	 * @param assessableCourseNode The course node
	 * @param assessedIdentityWrapper The wrapped identity
	 * @param trans The package translator
	 */
	public AssessmentForm(UserRequest ureq, WindowControl wControl, AssessableCourseNode assessableCourseNode,
			UserCourseEnvironment coachCourseEnv, UserCourseEnvironment assessedUserCourseEnv) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(AssessmentModule.class, getLocale(), getTranslator()));
		
		hasAttempts = assessableCourseNode.hasAttemptsConfigured();
		hasScore = assessableCourseNode.hasScoreConfigured();
		hasPassed = assessableCourseNode.hasPassedConfigured();
		hasComment = assessableCourseNode.hasCommentConfigured();
		hasIndividualAssessmentDocs = assessableCourseNode.hasIndividualAsssessmentDocuments();
		
		this.coachCourseEnv = coachCourseEnv;
		this.assessedUserCourseEnv = assessedUserCourseEnv;
		this.assessableCourseNode = assessableCourseNode;

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
		if(saveAndDoneLink == source) {
			if(validateFormLogic(ureq)) {
				doUpdateAssessmentData(true);
				fireEvent(ureq, new AssessmentFormEvent(AssessmentFormEvent.ASSESSMENT_DONE, true));
			}
		} else if(reopenLink == source) {
			doReopen();
			fireEvent(ureq, new AssessmentFormEvent(AssessmentFormEvent.ASSESSMENT_REOPEN, false));
		} else if(uploadDocsEl == source) {
			if(uploadDocsEl.getUploadFile() != null && StringHelper.containsNonWhitespace(uploadDocsEl.getUploadFileName())) {
				assessableCourseNode.addIndividualAssessmentDocument(uploadDocsEl.getUploadFile(), uploadDocsEl.getUploadFileName(),
						assessedUserCourseEnv, getIdentity());
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
		doUpdateAssessmentData(false);
		fireEvent(ureq, new AssessmentFormEvent(AssessmentFormEvent.ASSESSMENT_CHANGED, true));
	}

	@Override
	protected void formCancelled (UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		
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
		
		userVisibility.clearError();
		if(!userVisibility.isOneSelected()) {
			userVisibility.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		if(attempts != null) {
			attempts.clearError();
			allOk &= attempts.validateIntValue();
		}
		return allOk & super.validateFormLogic(ureq);
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
		ScoreEvaluation scoreEval = assessedUserCourseEnv.getScoreAccounting().evalCourseNode(assessableCourseNode);
		if (scoreEval != null) {
			ScoreEvaluation reopenedEval = new ScoreEvaluation(scoreEval.getScore(), scoreEval.getPassed(),
					AssessmentEntryStatus.inReview, scoreEval.getUserVisible(), scoreEval.getFullyAssessed(),
					scoreEval.getCurrentRunCompletion(), scoreEval.getCurrentRunStatus(), scoreEval.getAssessmentID());
			assessableCourseNode.updateUserScoreEvaluation(reopenedEval, assessedUserCourseEnv, getIdentity(), false, Role.coach);
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
		assessableCourseNode.removeIndividualAssessmentDocument(document, assessedUserCourseEnv, getIdentity());
	}
	
	protected void doUpdateAssessmentData(boolean setAsDone) {
		Float updatedScore = null;
		Boolean updatedPassed = null;

		if (isHasAttempts() && isAttemptsDirty()) {
			assessableCourseNode.updateUserAttempts(new Integer(getAttempts()), assessedUserCourseEnv, getIdentity(), Role.coach);
		}

		if (isHasScore()) {
			if(isScoreDirty()) {
				updatedScore = getScore();
			} else {
				updatedScore = scoreValue;
			}
		}
		
		if (isHasPassed()) {
			if (getCut() != null && getScore() != null) {
				updatedPassed = updatedScore.floatValue() >= getCut().floatValue() ? Boolean.TRUE : Boolean.FALSE;
			} else {
				//"passed" info was changed or not 
				String selectedKeyString = getPassed().getSelectedKey();
				if("true".equalsIgnoreCase(selectedKeyString) || "false".equalsIgnoreCase(selectedKeyString)) {
					updatedPassed = Boolean.valueOf(selectedKeyString);
				}			
			}
		}
		
		boolean visibility = userVisibility.isSelected(0);
		// Update score,passed properties in db
		ScoreEvaluation scoreEval;
		if(setAsDone) {
			scoreEval = new ScoreEvaluation(updatedScore, updatedPassed, AssessmentEntryStatus.done, visibility, true, null, null, null);
		} else {
			scoreEval = new ScoreEvaluation(updatedScore, updatedPassed, null, visibility, null, null, null, null);
		}
		assessableCourseNode.updateUserScoreEvaluation(scoreEval, assessedUserCourseEnv, getIdentity(), false, Role.coach);

		if (isHasComment() && isUserCommentDirty()) {
			String newComment = getUserComment().getValue();
			// Update properties in db
			assessableCourseNode.updatedUserComment(newComment, assessedUserCourseEnv, getIdentity());
		}

		if (isCoachCommentDirty()) {
			String newCoachComment = getCoachComment().getValue();
			// Update properties in db
			assessableCourseNode.updateCoachComment(newCoachComment, assessedUserCourseEnv);
		}
	}
	
	/**
	 * Reload the data in the controller
	 * @param updateScoreAccounting Force a recalculation of the whole scoring in the course for the assessed user.
	 */
	public void reloadData() {
		ScoreAccounting scoreAccounting = assessedUserCourseEnv.getScoreAccounting();
		scoreAccounting.evaluateAll(true);
		ScoreEvaluation scoreEval = scoreAccounting.evalCourseNode(assessableCourseNode);
		if (scoreEval == null) scoreEval = new ScoreEvaluation(null, null);
		
		if (hasAttempts) {
			attemptsValue = assessableCourseNode.getUserAttempts(assessedUserCourseEnv);
			attempts.setIntValue(attemptsValue == null ? 0 : attemptsValue.intValue());
		}
		
		if (hasScore) {
			scoreValue = scoreEval.getScore();
			if (scoreValue != null) {
				score.setValue(AssessmentHelper.getRoundedScore(scoreValue));
			} 
		}
		
		if (hasPassed) {
			Boolean passedValue = scoreEval.getPassed();
			passed.select(passedValue == null ? "undefined" : passedValue.toString(), true);
			passed.setEnabled(cut == null);
			passed.getComponent().setDirty(true);//force the dirty
		}
		
		if(hasComment) {
			userCommentValue = assessableCourseNode.getUserComment(assessedUserCourseEnv);
			userComment.setValue(userCommentValue);
		}
		
		reloadAssessmentDocs();
		updateStatus(scoreEval);
	}
	
	private void reloadAssessmentDocs() {
		if(docsLayoutCont == null) return;
		
		List<File> documents = assessableCourseNode.getIndividualAssessmentDocuments(assessedUserCourseEnv);
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
			passed.setEnabled(!closed && cut == null && !coachCourseEnv.isCourseReadOnly());
		}
		
		if(hasScore) {
			score.setEnabled(!closed && !coachCourseEnv.isCourseReadOnly());
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
		
		submitButton.setVisible(!coachCourseEnv.isCourseReadOnly());
		saveAndDoneLink.setVisible(!closed && !coachCourseEnv.isCourseReadOnly());
		reopenLink.setVisible(closed && !coachCourseEnv.isCourseReadOnly());
		flc.setDirty(true);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("form.title", null);
		formLayout.setElementCssClass("o_sel_assessment_form");
		
		ScoreEvaluation scoreEval = assessedUserCourseEnv.getScoreAccounting().evalCourseNode(assessableCourseNode);
		if (scoreEval == null) {
			scoreEval = ScoreEvaluation.EMPTY_EVALUATION;
		}

		if (hasAttempts) {
			attemptsValue = assessableCourseNode.getUserAttempts(assessedUserCourseEnv);
			if(attemptsValue == null) {
				attemptsValue = new Integer(0);
			}
			attempts = uifactory.addIntegerElement("attempts", "form.attempts", attemptsValue.intValue(), formLayout);
			attempts.setDisplaySize(3);
			attempts.setMinValueCheck(0, null);
		}

		if (hasScore) {
			min = assessableCourseNode.getMinScoreConfiguration();
			max = assessableCourseNode.getMaxScoreConfiguration();
			if (hasPassed) {
				cut = assessableCourseNode.getCutValueConfiguration();
			}
			
			String minStr = AssessmentHelper.getRoundedScore(min);
			String maxStr = AssessmentHelper.getRoundedScore(max);
			uifactory.addStaticTextElement("minval", "form.min", ((min == null) ? translate("form.valueUndefined") : minStr), formLayout);
			uifactory.addStaticTextElement("maxval", "form.max", ((max == null) ? translate("form.valueUndefined") : maxStr), formLayout);

			// Use init variables from wrapper, already loaded from db
			scoreValue = scoreEval.getScore();
			score = uifactory.addTextElement("score","form.score" , 10, "", formLayout);
			score.setDisplaySize(4);
			score.setElementCssClass("o_sel_assessment_form_score");
			score.setExampleKey("form.score.rounded", null);
			if (scoreValue != null) {
				score.setValue(AssessmentHelper.getRoundedScore(scoreValue));
			} 
			// assessment overview with max score
			score.setRegexMatchCheck("(\\d+)||(\\d+\\.\\d{1,3})||(\\d+\\,\\d{1,3})", "form.error.wrongFloat");
		}

		if (hasPassed) {
			if (cut != null) {
				// Display cut value if defined
				cutVal = uifactory.addStaticTextElement(
						"cutval","form.cut" ,
						((cut == null) ? translate("form.valueUndefined") : AssessmentHelper.getRoundedScore(cut)),
						formLayout
				);
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
			
			Boolean passedValue = scoreEval.getPassed();
			passed.select(passedValue == null ? "undefined" :passedValue.toString(), true);
			// When cut value is defined, no manual passed possible
			passed.setEnabled(cut == null);
		}

		if (hasComment) {
			// Use init variables from db, not available from wrapper
			userCommentValue = assessableCourseNode.getUserComment(assessedUserCourseEnv);
			userComment = uifactory.addTextAreaElement("usercomment", "form.usercomment", 2500, 5, 40, true, false, userCommentValue, formLayout);
			userComment.setNotLongerThanCheck(2500, "input.toolong");
		}
		
		if(hasIndividualAssessmentDocs) {
			String mapperUri = registerCacheableMapper(ureq, null, new DocumentMapper());
			String page = velocity_root + "/individual_assessment_docs.html"; 
			docsLayoutCont = FormLayoutContainer.createCustomFormLayout("form.individual.assessment.docs", getTranslator(), page);
			docsLayoutCont.setLabel("form.individual.assessment.docs", null);
			docsLayoutCont.contextPut("mapperUri", mapperUri);
			formLayout.add(docsLayoutCont);

			uploadDocsEl = uifactory.addFileElement(getWindowControl(), "form.upload", null, formLayout);
			uploadDocsEl.addActionListener(FormEvent.ONCHANGE);
		}
		
		coachCommentValue = assessableCourseNode.getCoachComment(assessedUserCourseEnv);
		coachComment = uifactory.addTextAreaElement("coachcomment", "form.coachcomment", 2500, 5, 40, true, false, coachCommentValue, formLayout);
		coachComment.setNotLongerThanCheck(2500, "input.toolong");
		
		String[] userVisibilityValues = new String[]{ translate("user.visibility.visible"), translate("user.visibility.hidden") };
		userVisibility = uifactory.addRadiosHorizontal("user.visibility", "user.visibility", formLayout, userVisibilityKeys, userVisibilityValues);
		if(scoreEval.getUserVisible() == null || scoreEval.getUserVisible().booleanValue()) {
			userVisibility.select(userVisibilityKeys[0], true);
		} else {
			userVisibility.select(userVisibilityKeys[1], true);
		}
		
		FormLayoutContainer buttonGroupLayout = FormLayoutContainer.createButtonLayout("buttonGroupLayout", getTranslator());
		formLayout.add(buttonGroupLayout);
		
		submitButton = uifactory.addFormSubmitButton("save", buttonGroupLayout);
		submitButton.setElementCssClass("o_sel_assessment_form_save_and_close");

		saveAndDoneLink = uifactory.addFormLink("save.done", buttonGroupLayout, Link.BUTTON);
		saveAndDoneLink.setElementCssClass("o_sel_assessment_form_save_and_done");
		saveAndDoneLink.setIconLeftCSS("o_icon o_icon_status_done o_icon-fw");
		
		reopenLink = uifactory.addFormLink("reopen", buttonGroupLayout, Link.BUTTON);
		reopenLink.setElementCssClass("o_sel_assessment_form_reopen");
		reopenLink.setIconLeftCSS("o_icon o_icon_status_in_review o_icon-fw");

		uifactory.addFormCancelButton("cancel", buttonGroupLayout, ureq, getWindowControl());

		reloadAssessmentDocs();
		updateStatus(scoreEval);
	}

	@Override
	protected void doDispose() {
		//
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
