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
package org.olat.ims.qti21.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.SpacerElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.ims.qti21.QTI21AssessmentResultsOptions;
import org.olat.ims.qti21.QTI21DeliveryOptions;
import org.olat.ims.qti21.QTI21DeliveryOptions.TestType;
import org.olat.ims.qti21.QTI21Module;
import org.olat.ims.qti21.QTI21Service;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.ui.settings.ReloadSettingsEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 05.01.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21DeliveryOptionsController extends FormBasicController implements Activateable2 {
	
	private static final String[] onKeys = new String[]{ "on" };
	private static final String[] onValues = new String[]{ "" };
	private static final String[] settingTypeKeys = new String[]{ TestType.summative.name(), TestType.formative.name() };
	private static final String[] resultsOptionsKeys = new String[] { 
			QTI21AssessmentResultsOptions.METADATA, QTI21AssessmentResultsOptions.SECTION_SUMMARY,
			QTI21AssessmentResultsOptions.QUESTION_SUMMARY,
			QTI21AssessmentResultsOptions.USER_SOLUTIONS, QTI21AssessmentResultsOptions.CORRECT_SOLUTIONS
		};

	private FormLink chooseProfileButton;
	private SingleSelection settingTypeEl;
	private MultipleSelectionElement showTitlesEl;
	private MultipleSelectionElement showMenuEl;
	private MultipleSelectionElement personalNotesEl;
	private MultipleSelectionElement showFeedbacksEl;
	private MultipleSelectionElement enableCancelEl;
	private MultipleSelectionElement enableSuspendEl;
	private MultipleSelectionElement limitAttemptsEl;
	private MultipleSelectionElement blockAfterSuccessEl;
	private MultipleSelectionElement displayQuestionProgressEl;
	private MultipleSelectionElement displayScoreProgressEl;
	private MultipleSelectionElement displayMaxScoreItemEl;
	private MultipleSelectionElement allowAnonymEl;
	private MultipleSelectionElement hideLmsEl;
	private MultipleSelectionElement digitalSignatureEl;
	private MultipleSelectionElement digitalSignatureMailEl;
	private MultipleSelectionElement showResultsOnFinishEl;
	private MultipleSelectionElement assessmentResultsOnFinishEl;
	private TextElement maxAttemptsEl;
	
	private boolean changes;
	private final boolean readOnly;
	private final RepositoryEntry testEntry;
	private final QTI21DeliveryOptions deliveryOptions;
	
	@Autowired
	private QTI21Module qtiModule;
	@Autowired
	private QTI21Service qtiService;
	
	public QTI21DeliveryOptionsController(UserRequest ureq, WindowControl wControl, RepositoryEntry testEntry, boolean readOnly) {
		super(ureq, wControl);
		this.readOnly = readOnly;
		this.testEntry = testEntry;
		deliveryOptions = qtiService.getDeliveryOptions(testEntry);
		initForm(ureq);
		applyDeliveryOptions(deliveryOptions);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("tab.options");
		setFormContextHelp("Test editor QTI 2.1 in detail#details_testeditor_options");
		formLayout.setElementCssClass("o_sel_qti_resource_options");
		setFormInfo("settings.choose.descr");
		
		//choose profile
		String profilePage = velocity_root + "/profile.html";
		FormLayoutContainer profileCont = FormLayoutContainer.createCustomFormLayout("profile", getTranslator(), profilePage);
		profileCont.setLabel("settings.profile", null);
		formLayout.add(profileCont);
		
		String[] settingTypeValues = new String[]{ 
				translate("qti.form.setting.summative"), translate("qti.form.setting.formative")
		};
		settingTypeEl = uifactory.addDropdownSingleselect("settings.type", "settings.type", null, profileCont, settingTypeKeys, settingTypeValues, null);
		settingTypeEl.setDomReplacementWrapperRequired(false);
		settingTypeEl.enableNoneSelection(translate("qti.form.setting.choose"));
		settingTypeEl.setEnabled(!readOnly);
		
		chooseProfileButton = uifactory.addFormLink("settings.choose.profile", profileCont, Link.BUTTON);
		chooseProfileButton.setEnabled(!readOnly);
		
		SpacerElement profilSpacer = uifactory.addSpacerElement("profile.spacer", formLayout, false);
		profilSpacer.setEnabled(!readOnly);

		limitAttemptsEl = uifactory.addCheckboxesHorizontal("limitAttempts", "qti.form.limit.attempts", formLayout, onKeys, onValues);
		limitAttemptsEl.addActionListener(FormEvent.ONCLICK);
		limitAttemptsEl.setEnabled(!readOnly);
		
		maxAttemptsEl = uifactory.addTextElement("maxAttempts", "qti.form.attempts", 8, "", formLayout);	
		maxAttemptsEl.setDisplaySize(2);
		maxAttemptsEl.setEnabled(!readOnly);
		
		blockAfterSuccessEl = uifactory.addCheckboxesHorizontal("blockAfterSuccess", "qti.form.block.afterSuccess", formLayout, onKeys, onValues);
		blockAfterSuccessEl.setEnabled(!readOnly);

		allowAnonymEl = uifactory.addCheckboxesHorizontal("allowAnonym", "qti.form.allow.anonym", formLayout, onKeys, onValues);
		allowAnonymEl.setHelpText(translate("qti.form.allow.anonym.hint"));
		allowAnonymEl.setHelpUrlForManualPage("Test editor QTI 2.1 in detail#details_testeditor_test_konf_kurs");
		allowAnonymEl.setEnabled(!readOnly);
	
		hideLmsEl = uifactory.addCheckboxesHorizontal("hide.lms", "qti.form.hide.lms", formLayout, onKeys, onValues);
		hideLmsEl.setEnabled(!readOnly);
		
		showTitlesEl = uifactory.addCheckboxesHorizontal("showTitles", "qti.form.questiontitle", formLayout, onKeys, onValues);
		showTitlesEl.setEnabled(!readOnly);

		showMenuEl = uifactory.addCheckboxesHorizontal("showMenu", "qti.form.menudisplay", formLayout, onKeys, onValues);
		showMenuEl.setElementCssClass("o_sel_qti_show_menu");
		showMenuEl.setEnabled(!readOnly);

		personalNotesEl = uifactory.addCheckboxesHorizontal("personalNotes", "qti.form.auto.memofield", formLayout, onKeys, onValues);
		personalNotesEl.setHelpText(translate("qti.form.auto.memofield.hint"));
		personalNotesEl.setHelpUrlForManualPage("Test editor QTI 2.1 in detail#details_testeditor_test_konf_kurs");
		personalNotesEl.setElementCssClass("o_sel_qti_personal_notes");
		personalNotesEl.setEnabled(!readOnly);

		displayQuestionProgressEl = uifactory.addCheckboxesHorizontal("questionProgress", "qti.form.questionprogress", formLayout, onKeys, onValues);
		displayQuestionProgressEl.setElementCssClass("o_sel_qti_progress_questions");
		displayQuestionProgressEl.setEnabled(!readOnly);

		displayScoreProgressEl = uifactory.addCheckboxesHorizontal("scoreProgress", "qti.form.scoreprogress", formLayout, onKeys, onValues);
		displayScoreProgressEl.setElementCssClass("o_sel_qti_progress_score");
		displayScoreProgressEl.setEnabled(!readOnly);
		
		displayMaxScoreItemEl = uifactory.addCheckboxesHorizontal("maxScoreItem", "qti.form.max.score.item", formLayout, onKeys, onValues);
		displayMaxScoreItemEl.setElementCssClass("o_sel_qti_progress_max_score_item");
		displayMaxScoreItemEl.setEnabled(!readOnly);

		enableSuspendEl = uifactory.addCheckboxesHorizontal("suspend", "qti.form.enablesuspend", formLayout, onKeys, onValues);
		enableSuspendEl.setElementCssClass("o_sel_qti_enable_suspend");
		enableSuspendEl.setEnabled(!readOnly);

		enableCancelEl = uifactory.addCheckboxesHorizontal("cancel", "qti.form.enablecancel", formLayout, onKeys, onValues);
		enableCancelEl.setElementCssClass("o_sel_qti_enable_cancel");
		enableCancelEl.setEnabled(!readOnly);
		
		digitalSignatureEl = uifactory.addCheckboxesHorizontal("digital.signature", "digital.signature.test.option", formLayout, onKeys, onValues);
		digitalSignatureEl.setVisible(qtiModule.isDigitalSignatureEnabled());
		digitalSignatureEl.addActionListener(FormEvent.ONCHANGE);
		digitalSignatureEl.setEnabled(!readOnly);
		digitalSignatureMailEl = uifactory.addCheckboxesHorizontal("digital.signature.mail", "digital.signature.mail.test.option", formLayout, onKeys, onValues);
		digitalSignatureMailEl.setEnabled(!readOnly);
		
		showFeedbacksEl = uifactory.addCheckboxesHorizontal("showFeedbacks", "qti.form.showfeedbacks", formLayout, onKeys, onValues);
		showFeedbacksEl.setElementCssClass("o_sel_qti_show_feedbacks");
		showFeedbacksEl.setEnabled(!readOnly);
		
		showResultsOnFinishEl = uifactory.addCheckboxesHorizontal("resultOnFiniish", "qti.form.results.onfinish", formLayout, onKeys, onValues);
		showResultsOnFinishEl.addActionListener(FormEvent.ONCHANGE);
		showResultsOnFinishEl.setElementCssClass("o_sel_qti_show_results");
		showResultsOnFinishEl.setEnabled(!readOnly);
		
		String[] resultsOptionsValues = new String[] {
				translate("qti.form.summary.metadata"), translate("qti.form.summary.sections"),
				translate("qti.form.summary.questions.metadata"),
				translate("qti.form.summary.responses"), translate("qti.form.summary.solutions")
		};
		assessmentResultsOnFinishEl = uifactory.addCheckboxesVertical("typeResultOnFiniish", "qti.form.summary", formLayout,
				resultsOptionsKeys, resultsOptionsValues, 1);
		assessmentResultsOnFinishEl.setElementCssClass("o_sel_qti_show_results_options");
		assessmentResultsOnFinishEl.setHelpText(translate("qti.form.summary.help"));
		assessmentResultsOnFinishEl.setHelpUrlForManualPage("Test editor QTI 2.1 in detail#overview_results");
		assessmentResultsOnFinishEl.setEnabled(!readOnly);
		
		FormLayoutContainer buttonsLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsLayout.setRootForm(mainForm);
		formLayout.add(buttonsLayout);
		if(!readOnly) {
			uifactory.addFormSubmitButton("save", buttonsLayout);
		}
	}
	
	private void applyDeliveryOptions(QTI21DeliveryOptions options) {
		String maxAttemptsValue = "";
		int maxAttempts =  options.getMaxAttempts();
		if(maxAttempts > 0) {
			limitAttemptsEl.select(onKeys[0], true);
			maxAttemptsValue = Integer.toString(maxAttempts);
		} else {
			limitAttemptsEl.uncheckAll();
		}
		maxAttemptsEl.setValue(maxAttemptsValue);
		maxAttemptsEl.setVisible(maxAttempts > 0);
		
		applyMultipleSelection(blockAfterSuccessEl, options.isBlockAfterSuccess());
		applyMultipleSelection(allowAnonymEl, options.isAllowAnonym());
		applyMultipleSelection(hideLmsEl, options.isHideLms());
		applyMultipleSelection(showTitlesEl, options.isShowTitles());
		applyMultipleSelection(showMenuEl, options.isShowMenu());
		applyMultipleSelection(personalNotesEl, options.isPersonalNotes());
		applyMultipleSelection(displayQuestionProgressEl, options.isDisplayQuestionProgress());
		applyMultipleSelection(displayScoreProgressEl, options.isDisplayScoreProgress());
		applyMultipleSelection(displayMaxScoreItemEl, options.isDisplayMaxScoreItem());
		applyMultipleSelection(enableSuspendEl, options.isEnableSuspend());
		applyMultipleSelection(enableCancelEl, options.isEnableCancel());
		applyMultipleSelection(showFeedbacksEl, !options.isHideFeedbacks());
		
		if(options.isShowAssessmentResultsOnFinish()) {
			showResultsOnFinishEl.select(onKeys[0], true);
		}
		QTI21AssessmentResultsOptions resultsOptions = options.getAssessmentResultsOptions();
		if(!resultsOptions.none()) {
			assessmentResultsOnFinishEl.uncheckAll();
			if(resultsOptions.isMetadata()) {
				assessmentResultsOnFinishEl.select(resultsOptionsKeys[0], true);
			}
			if(resultsOptions.isSectionSummary()) {
				assessmentResultsOnFinishEl.select(resultsOptionsKeys[1], true);
			}
			if(resultsOptions.isQuestionSummary()) {
				assessmentResultsOnFinishEl.select(resultsOptionsKeys[2], true);
			}
			if(resultsOptions.isUserSolutions()) {
				assessmentResultsOnFinishEl.select(resultsOptionsKeys[3], true);
			}
			if(resultsOptions.isCorrectSolutions()) {
				assessmentResultsOnFinishEl.select(resultsOptionsKeys[4], true);
			}
		} else {
			assessmentResultsOnFinishEl.uncheckAll();
		}
		assessmentResultsOnFinishEl.setVisible(showResultsOnFinishEl.isAtLeastSelected(1));
		
		boolean digitalSignature = options.isDigitalSignature();
		applyMultipleSelection(digitalSignatureEl, digitalSignature);
		
		boolean digitalSignatureSendMail = options.isDigitalSignatureMail();
		applyMultipleSelection(digitalSignatureMailEl, digitalSignatureSendMail);
		digitalSignatureMailEl.setVisible(qtiModule.isDigitalSignatureEnabled() && digitalSignatureEl.isAtLeastSelected(1));
	}
	
	private void applyMultipleSelection(MultipleSelectionElement element, boolean option) {
		if(option) {
			element.select(onKeys[0], true);
		} else {
			element.uncheckAll();
		}
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		//
	}
	
	public boolean hasChanges() {
		return changes;
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		if(limitAttemptsEl.isAtLeastSelected(1)) {
			maxAttemptsEl.clearError();
			if(StringHelper.containsNonWhitespace(maxAttemptsEl.getValue())) {
				try {
					Integer.parseInt(maxAttemptsEl.getValue());
				} catch(NumberFormatException e) {
					maxAttemptsEl.setErrorKey("form.error.nointeger", null);
					allOk &= false;
				}
			} else {
				maxAttemptsEl.setErrorKey("form.legende.mandatory", null);
				allOk &= false;
			}
		}

		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(limitAttemptsEl == source) {
			maxAttemptsEl.setVisible(limitAttemptsEl.isAtLeastSelected(1));
		} else if(digitalSignatureEl == source) {
			digitalSignatureMailEl.setVisible(digitalSignatureEl.isAtLeastSelected(1));
		} else if(showResultsOnFinishEl == source) {
			assessmentResultsOnFinishEl.setVisible(showResultsOnFinishEl.isAtLeastSelected(1));
		} else if(chooseProfileButton == source) {
			if(settingTypeEl.isOneSelected()) {
				String selectedType = settingTypeEl.getSelectedKey();
				if(TestType.formative.name().equals(selectedType)) {
					applyDeliveryOptions(QTI21DeliveryOptions.formativeSettings());
				} else if(TestType.summative.name().equals(selectedType)) {
					applyDeliveryOptions(QTI21DeliveryOptions.summativeSettings());
				}
			}
			settingTypeEl.select(SingleSelection.NO_SELECTION_KEY, true);
		}
		super.formInnerEvent(ureq, source, event);
	}
	

	@Override
	protected void formOK(UserRequest ureq) {
		if(limitAttemptsEl.isAtLeastSelected(1)) {
			deliveryOptions.setMaxAttempts(Integer.parseInt(maxAttemptsEl.getValue()));
		} else {
			deliveryOptions.setMaxAttempts(0);
		}
		deliveryOptions.setBlockAfterSuccess(blockAfterSuccessEl.isAtLeastSelected(1));
		deliveryOptions.setShowMenu(showMenuEl.isAtLeastSelected(1));
		deliveryOptions.setShowTitles(showTitlesEl.isAtLeastSelected(1));
		deliveryOptions.setPersonalNotes(personalNotesEl.isAtLeastSelected(1));
		deliveryOptions.setEnableCancel(enableCancelEl.isAtLeastSelected(1));
		deliveryOptions.setEnableSuspend(enableSuspendEl.isAtLeastSelected(1));
		deliveryOptions.setDisplayQuestionProgress(displayQuestionProgressEl.isAtLeastSelected(1));
		deliveryOptions.setDisplayScoreProgress(displayScoreProgressEl.isAtLeastSelected(1));
		deliveryOptions.setDisplayMaxScoreItem(displayMaxScoreItemEl.isAtLeastSelected(1));
		deliveryOptions.setAllowAnonym(allowAnonymEl.isAtLeastSelected(1));
		deliveryOptions.setHideLms(hideLmsEl.isAtLeastSelected(1));
		deliveryOptions.setHideFeedbacks(!showFeedbacksEl.isAtLeastSelected(1));//reverse logic for compatibility

		if(showResultsOnFinishEl.isAtLeastSelected(1)) {
			QTI21AssessmentResultsOptions resultsOptions = new QTI21AssessmentResultsOptions(
					assessmentResultsOnFinishEl.isSelected(0), assessmentResultsOnFinishEl.isSelected(1),
					assessmentResultsOnFinishEl.isSelected(2),
					assessmentResultsOnFinishEl.isSelected(3), assessmentResultsOnFinishEl.isSelected(4));
			deliveryOptions.setAssessmentResultsOptions(resultsOptions);
			deliveryOptions.setShowAssessmentResultsOnFinish(true);
		} else {
			deliveryOptions.setAssessmentResultsOptions(QTI21AssessmentResultsOptions.noOptions());
			deliveryOptions.setShowAssessmentResultsOnFinish(false);
		}
		deliveryOptions.setShowResultsOnFinish(null);// nullify old stuff

		if(qtiModule.isDigitalSignatureEnabled() && digitalSignatureEl.isAtLeastSelected(1)) {
			deliveryOptions.setDigitalSignature(true);
			deliveryOptions.setDigitalSignatureMail(digitalSignatureMailEl.isAtLeastSelected(1));
		} else {
			deliveryOptions.setDigitalSignature(false);
			deliveryOptions.setDigitalSignatureMail(false);
		}
		
		qtiService.setDeliveryOptions(testEntry, deliveryOptions);
		changes = true;
		fireEvent(ureq, new ReloadSettingsEvent());
	}
}