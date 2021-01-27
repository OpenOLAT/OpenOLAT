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
package org.olat.course.nodes.iq;

import java.io.File;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.fileresource.FileResourceManager;
import org.olat.ims.qti21.QTI21DeliveryOptions;
import org.olat.ims.qti21.QTI21Module;
import org.olat.ims.qti21.QTI21Service;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.ed.ph.jqtiplus.node.test.AssessmentTest;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentTest;

/**
 * 
 * Initial date: 26.06.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21EditLayoutForm extends FormBasicController {
	
	private static final String[] onKeys = new String[]{ "on" };
	private static final String[] onValues = new String[]{ "" };
	private static final String[] configKeys = new String[]{ "repo", "node" };

	private SingleSelection configEl;
	private SelectionElement fullWindowEl;
	private MultipleSelectionElement showTitlesEl;
	private MultipleSelectionElement showMenuEl;
	private MultipleSelectionElement personalNotesEl;
	private MultipleSelectionElement enableCancelEl;
	private MultipleSelectionElement enableSuspendEl;
	private MultipleSelectionElement limitAttemptsEl;
	private MultipleSelectionElement blockAfterSuccessEl;
	private MultipleSelectionElement displayQuestionProgressEl;
	private MultipleSelectionElement displayScoreProgressEl;
	private MultipleSelectionElement displayMaxScoreItemEl;
	private MultipleSelectionElement allowAnonymEl;
	private MultipleSelectionElement showFeedbacksEl;
	private MultipleSelectionElement digitalSignatureEl;
	private MultipleSelectionElement digitalSignatureMailEl;

	private FormLayoutContainer maxTimeCont;
	private MultipleSelectionElement maxTimeEl;
	private TextElement maxTimeHourEl;
	private TextElement maxTimeMinuteEl;
	
	private TextElement maxAttemptsEl;
	
	private final RepositoryEntry testEntry;
	private final ModuleConfiguration modConfig;
	private final QTI21DeliveryOptions deliveryOptions;

	@Autowired
	private QTI21Module qtiModule;
	@Autowired
	private QTI21Service qtiService;
	
	public QTI21EditLayoutForm(UserRequest ureq, WindowControl wControl, ModuleConfiguration modConfig,
			RepositoryEntry testEntry, QTI21DeliveryOptions deliveryOptions) {
		super(ureq, wControl);
		this.modConfig = modConfig;
		this.testEntry = testEntry;
		this.deliveryOptions = deliveryOptions;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_qti_21_layout_configuration");
		
		String[] configValues = new String[]{ translate("qti.form.setting.repo"), translate("qti.form.setting.node") };
		configEl = uifactory.addRadiosHorizontal("config", null, formLayout, configKeys, configValues);
		configEl.setElementCssClass("o_qti_21_configuration_settings");
		configEl.addActionListener(FormEvent.ONCHANGE);
		boolean configRef = modConfig.getBooleanSafe(IQEditController.CONFIG_KEY_CONFIG_REF, false);
		String key = configRef ? configKeys[0] : configKeys[1];
		configEl.select(key, true);
		
		//time limits
		long timeLimits = configRef ? getMaxTimeLimit() :
			modConfig.getIntegerSafe(IQEditController.CONFIG_KEY_TIME_LIMIT, -1);
		String timeMaxHour = "";
		String timeMaxMinute = "";
		if(timeLimits > 0) {
			timeMaxHour = Long.toString(timeLimits / 3600);
			timeMaxMinute = Long.toString((timeLimits % 3600) / 60);
		}
		maxTimeEl = uifactory.addCheckboxesVertical("time.limit.enable", "time.limit.max", formLayout, onKeys, onValues, 1);
		maxTimeEl.addActionListener(FormEvent.ONCHANGE);
		maxTimeEl.setEnabled(!configRef);
		if(timeLimits > 0) {
			maxTimeEl.select(onKeys[0], true);
		}
		
		String page = velocity_root + "/max_time_limit.html";
		maxTimeCont = FormLayoutContainer.createCustomFormLayout("time.limit.cont", getTranslator(), page);
		maxTimeCont.setVisible(maxTimeEl.isAtLeastSelected(1));
		formLayout.add(maxTimeCont);
		
		maxTimeHourEl = uifactory.addTextElement("time.limit.hour", "time.limit.max", 4, timeMaxHour, maxTimeCont);
		maxTimeHourEl.setDomReplacementWrapperRequired(false);
		maxTimeHourEl.setDisplaySize(4);
		maxTimeHourEl.setEnabled(!configRef);
		
		maxTimeMinuteEl = uifactory.addTextElement("time.limit.minute", "time.limit.max", 4, timeMaxMinute, maxTimeCont);
		maxTimeMinuteEl.setDomReplacementWrapperRequired(false);
		maxTimeMinuteEl.setDisplaySize(4);
		maxTimeMinuteEl.setEnabled(!configRef);
		
		//max attempts
		limitAttemptsEl = uifactory.addCheckboxesHorizontal("limitAttempts", "qti.form.limit.attempts", formLayout, onKeys, onValues);
		limitAttemptsEl.addActionListener(FormEvent.ONCLICK);
		limitAttemptsEl.setEnabled(!configRef);
		String maxAttemptsValue = "";
		int maxAttempts = configRef ? deliveryOptions.getMaxAttempts() :
				modConfig.getIntegerSafe(IQEditController.CONFIG_KEY_ATTEMPTS, deliveryOptions.getMaxAttempts());
		if(maxAttempts > 0) {
			limitAttemptsEl.select(onKeys[0], true);
			maxAttemptsValue = Integer.toString(maxAttempts);
		}
		maxAttemptsEl = uifactory.addTextElement("maxAttempts", "qti.form.attempts", 8, maxAttemptsValue, formLayout);	
		maxAttemptsEl.setDisplaySize(2);
		maxAttemptsEl.setMandatory(true);
		maxAttemptsEl.setEnabled(!configRef);
		maxAttemptsEl.setVisible(maxAttempts > 0);
		
		boolean blockAfterSuccess = configRef ? deliveryOptions.isBlockAfterSuccess() :
				modConfig.getBooleanSafe(IQEditController.CONFIG_KEY_BLOCK_AFTER_SUCCESS, deliveryOptions.isBlockAfterSuccess());
		blockAfterSuccessEl = uifactory.addCheckboxesHorizontal("blockAfterSuccess", "qti.form.block.afterSuccess", formLayout, onKeys, onValues);
		blockAfterSuccessEl.setEnabled(!configRef);
		if(blockAfterSuccess) {
			blockAfterSuccessEl.select(onKeys[0], true);
		}
		
		boolean allowAnonym = configRef ? deliveryOptions.isAllowAnonym() :
				modConfig.getBooleanSafe(IQEditController.CONFIG_ALLOW_ANONYM, deliveryOptions.isAllowAnonym());
		allowAnonymEl = uifactory.addCheckboxesHorizontal("allowAnonym", "qti.form.allow.anonym", formLayout, onKeys, onValues);
		allowAnonymEl.setEnabled(!configRef);
		if(allowAnonym) {
			allowAnonymEl.select(onKeys[0], true);
		}
		
		boolean fullWindow = configRef ? deliveryOptions.isHideLms() :
				modConfig.getBooleanSafe(IQEditController.CONFIG_FULLWINDOW, deliveryOptions.isHideLms());
		fullWindowEl = uifactory.addCheckboxesHorizontal("fullwindow", "qti.form.fullwindow", formLayout, new String[]{"x"}, new String[]{""});
		fullWindowEl.setElementCssClass("o_qti_21_configuration_full_window");
		fullWindowEl.setEnabled(!configRef);
		if(fullWindow) {
			fullWindowEl.select("x", fullWindow);
		}
		
		boolean digitalSignature = configRef ? deliveryOptions.isDigitalSignature() :
				modConfig.getBooleanSafe(IQEditController.CONFIG_DIGITAL_SIGNATURE, deliveryOptions.isDigitalSignature());
		digitalSignatureEl = uifactory.addCheckboxesHorizontal("digital.signature", "digital.signature", formLayout, new String[]{"x"}, new String[]{""});
		digitalSignatureEl.setEnabled(!configRef);
		if(digitalSignature) {
			digitalSignatureEl.select("x", digitalSignature);
		}
		digitalSignatureEl.setVisible(qtiModule.isDigitalSignatureEnabled());
		digitalSignatureEl.addActionListener(FormEvent.ONCHANGE);
		
		boolean digitalSignatureSendMail = configRef ? deliveryOptions.isDigitalSignatureMail() :
				modConfig.getBooleanSafe(IQEditController.CONFIG_DIGITAL_SIGNATURE_SEND_MAIL, deliveryOptions.isDigitalSignatureMail());
		digitalSignatureMailEl = uifactory.addCheckboxesHorizontal("digital.signature.mail", "digital.signature.mail", formLayout, new String[]{"x"}, new String[]{""});
		digitalSignatureMailEl.setEnabled(!configRef);
		if(digitalSignatureSendMail) {
			digitalSignatureMailEl.select("x", digitalSignatureSendMail);
		}
		digitalSignatureMailEl.setVisible(qtiModule.isDigitalSignatureEnabled() && digitalSignatureEl.isAtLeastSelected(1));

		boolean showTitles = configRef ? deliveryOptions.isShowTitles() :
				modConfig.getBooleanSafe(IQEditController.CONFIG_KEY_QUESTIONTITLE, deliveryOptions.isShowTitles());
		showTitlesEl = uifactory.addCheckboxesHorizontal("showTitles", "qti.form.questiontitle", formLayout, onKeys, onValues);
		showTitlesEl.setEnabled(!configRef);
		if(showTitles) {
			showTitlesEl.select(onKeys[0], true);
		}
		
		boolean showMenu = configRef ? deliveryOptions.isShowMenu() :
				modConfig.getBooleanSafe(IQEditController.CONFIG_KEY_ENABLEMENU, deliveryOptions.isShowMenu());
		showMenuEl = uifactory.addCheckboxesHorizontal("showmenu", "qti.form.menuenable", formLayout, onKeys, onValues);
		showMenuEl.setEnabled(!configRef);
		if(showMenu) {
			showMenuEl.select(onKeys[0], true);
		}
		
		boolean personalNotes = configRef ? deliveryOptions.isPersonalNotes() :
				modConfig.getBooleanSafe(IQEditController.CONFIG_KEY_MEMO, deliveryOptions.isPersonalNotes());
		personalNotesEl = uifactory.addCheckboxesHorizontal("personalNotes", "qti.form.auto.memofield", formLayout, onKeys, onValues);
		personalNotesEl.setEnabled(!configRef);
		if(personalNotes) {
			personalNotesEl.select(onKeys[0], true);
		}

		boolean questionProgress = configRef ? deliveryOptions.isDisplayQuestionProgress() :
				modConfig.getBooleanSafe(IQEditController.CONFIG_KEY_QUESTIONPROGRESS, deliveryOptions.isDisplayQuestionProgress());
		displayQuestionProgressEl = uifactory.addCheckboxesHorizontal("questionProgress", "qti.form.questionprogress", formLayout, onKeys, onValues);
		displayQuestionProgressEl.setEnabled(!configRef);
		if(questionProgress) {
			displayQuestionProgressEl.select(onKeys[0], true);
		}
		
		boolean questionScore = configRef ? deliveryOptions.isDisplayScoreProgress() :
				modConfig.getBooleanSafe(IQEditController.CONFIG_KEY_SCOREPROGRESS, deliveryOptions.isDisplayScoreProgress());
		displayScoreProgressEl = uifactory.addCheckboxesHorizontal("scoreProgress", "qti.form.scoreprogress", formLayout, onKeys, onValues);
		displayScoreProgressEl.setEnabled(!configRef);
		if(questionScore) {
			displayScoreProgressEl.select(onKeys[0], true);
		}
		
		boolean questionMaxScore = configRef ? deliveryOptions.isDisplayMaxScoreItem() :
			modConfig.getBooleanSafe(IQEditController.CONFIG_KEY_QUESTION_MAX_SCORE, deliveryOptions.isDisplayMaxScoreItem());
		displayMaxScoreItemEl = uifactory.addCheckboxesHorizontal("scoreMaxItem", "qti.form.max.score.item", formLayout, onKeys, onValues);
		displayMaxScoreItemEl.setEnabled(!configRef);
		if(questionMaxScore) {
			displayMaxScoreItemEl.select(onKeys[0], true);
		}

		boolean enableSuspend = configRef ? deliveryOptions.isEnableSuspend() :
				modConfig.getBooleanSafe(IQEditController.CONFIG_KEY_ENABLESUSPEND, deliveryOptions.isEnableSuspend());
		enableSuspendEl = uifactory.addCheckboxesHorizontal("suspend", "qti.form.enablesuspend", formLayout, onKeys, onValues);
		enableSuspendEl.setElementCssClass("o_sel_qti_enable_suspend");
		enableSuspendEl.setEnabled(!configRef);
		if(enableSuspend) {
			enableSuspendEl.select(onKeys[0], true);
		}

		boolean enableCancel = configRef ? deliveryOptions.isEnableCancel() :
				modConfig.getBooleanSafe(IQEditController.CONFIG_KEY_ENABLECANCEL, deliveryOptions.isEnableCancel());
		enableCancelEl = uifactory.addCheckboxesHorizontal("cancel", "qti.form.enablecancel", formLayout, onKeys, onValues);
		enableCancelEl.setElementCssClass("o_sel_qti_enable_cancel");
		enableCancelEl.setEnabled(!configRef);
		if(enableCancel) {
			enableCancelEl.select(onKeys[0], true);
		}
		
		boolean hideFeedbacks = configRef ? deliveryOptions.isHideFeedbacks() :
			modConfig.getBooleanSafe(IQEditController.CONFIG_KEY_HIDE_FEEDBACKS, deliveryOptions.isHideFeedbacks());
		showFeedbacksEl = uifactory.addCheckboxesHorizontal("showFeedbacks", "qti.form.showfeedbacks", formLayout, onKeys, onValues);
		showFeedbacksEl.setElementCssClass("o_sel_qti_show_feedbacks");
		showFeedbacksEl.setEnabled(!configRef);
		if(!hideFeedbacks) {
			showFeedbacksEl.select(onKeys[0], true);
		}
		
		if(!configRef) {
			uifactory.addFormSubmitButton("submit", formLayout);
		}
		update();
	}
	
	/**
	 * @return The time limit of the assessment test in seconds or -1 if not configured
	 */
	private long getMaxTimeLimit() {
		FileResourceManager frm = FileResourceManager.getInstance();
		File unzippedDirRoot = frm.unzipFileResource(testEntry.getOlatResource());
		ResolvedAssessmentTest resolvedAssessmentTest = qtiService.loadAndResolveAssessmentTest(unzippedDirRoot, false, false);
		AssessmentTest assessmentTest = resolvedAssessmentTest.getRootNodeLookup().extractIfSuccessful();
		long timeLimit = -1;
		if(assessmentTest != null && assessmentTest.getTimeLimits() != null && assessmentTest.getTimeLimits().getMaximum() != null) {
			timeLimit = assessmentTest.getTimeLimits().getMaximum().longValue();
		}
		return timeLimit;
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		
		configEl.clearError();
		if(!configEl.isOneSelected()) {
			configEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		if(limitAttemptsEl.isAtLeastSelected(1)) {
			maxAttemptsEl.clearError();
			if(StringHelper.containsNonWhitespace(maxAttemptsEl.getValue())) {
				try {
					int val = Integer.parseInt(maxAttemptsEl.getValue());
					if(val <= 0) {
						maxAttemptsEl.setErrorKey("form.error.nointeger", null);
					}
				} catch(NumberFormatException e) {
					maxAttemptsEl.setErrorKey("form.error.nointeger", null);
					allOk &= false;
				}
			} else {
				maxAttemptsEl.setErrorKey("form.legende.mandatory", null);
				allOk &= false;
			}
		}
		
		maxTimeCont.clearError();
		if(maxTimeEl.isAtLeastSelected(1)) {
			allOk &= validateTime(maxTimeHourEl);
			allOk &= validateTime(maxTimeMinuteEl);
		}

		return allOk & super.validateFormLogic(ureq);
	}
	
	private boolean validateTime(TextElement timeEl) {
		boolean allOk = true;
		if(StringHelper.containsNonWhitespace(timeEl.getValue())) {
			try {
				double val = Long.parseLong(timeEl.getValue());
				if(val < 0l) {
					maxTimeCont.setErrorKey("form.error.nointeger", null);
					allOk &= false;
				}
			} catch (NumberFormatException e) {
				maxTimeCont.setErrorKey("form.error.nointeger", null);
				allOk &= false;
			}
		}
		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(limitAttemptsEl == source) {
			update();
		} else if(digitalSignatureEl == source) {
			digitalSignatureMailEl.setVisible(digitalSignatureEl.isAtLeastSelected(1));
		} else if(configEl == source) {
			if(configEl.isOneSelected()) {
				modConfig.setBooleanEntry(IQEditController.CONFIG_KEY_CONFIG_REF, configEl.isSelected(0));
				if(configEl.isSelected(1)) {// manual configuration
					persistConfiguration();
					long maxTime = getMaxTimeLimit();
					if(maxTime > 0) {
						modConfig.setIntValue(IQEditController.CONFIG_KEY_TIME_LIMIT, (int)maxTime);
					} else {
						modConfig.remove(IQEditController.CONFIG_KEY_TIME_LIMIT);
					}
				}
				fireEvent(ureq, Event.DONE_EVENT);
			}
		} else if(maxTimeEl == source) {
			maxTimeCont.setVisible(maxTimeEl.isAtLeastSelected(1));
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void update() {
		maxAttemptsEl.setVisible(limitAttemptsEl.isAtLeastSelected(1));
	}

	@Override
	protected void formOK(UserRequest ureq) {
		persistConfiguration();
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	protected void persistConfiguration() {
		modConfig.setBooleanEntry(IQEditController.CONFIG_KEY_CONFIG_REF, configEl.isSelected(0));
		modConfig.setBooleanEntry(IQEditController.CONFIG_FULLWINDOW, fullWindowEl.isSelected(0));
		if(limitAttemptsEl.isSelected(0)) {
			int maxAttempts = Integer.parseInt(maxAttemptsEl.getValue());
			modConfig.setIntValue(IQEditController.CONFIG_KEY_ATTEMPTS, maxAttempts);
		} else {
			modConfig.setIntValue(IQEditController.CONFIG_KEY_ATTEMPTS, 0);
		}
		modConfig.setBooleanEntry(IQEditController.CONFIG_KEY_BLOCK_AFTER_SUCCESS, blockAfterSuccessEl.isSelected(0));
		modConfig.setBooleanEntry(IQEditController.CONFIG_KEY_ENABLEMENU, showMenuEl.isSelected(0));
		modConfig.setBooleanEntry(IQEditController.CONFIG_KEY_QUESTIONTITLE, showTitlesEl.isSelected(0));
		modConfig.setBooleanEntry(IQEditController.CONFIG_KEY_MEMO, personalNotesEl.isSelected(0));
		modConfig.setBooleanEntry(IQEditController.CONFIG_KEY_ENABLECANCEL, enableCancelEl.isSelected(0));
		modConfig.setBooleanEntry(IQEditController.CONFIG_KEY_ENABLESUSPEND, enableSuspendEl.isSelected(0));
		modConfig.setBooleanEntry(IQEditController.CONFIG_KEY_QUESTIONPROGRESS, displayQuestionProgressEl.isSelected(0));
		modConfig.setBooleanEntry(IQEditController.CONFIG_KEY_SCOREPROGRESS, displayScoreProgressEl.isSelected(0));
		modConfig.setBooleanEntry(IQEditController.CONFIG_KEY_QUESTION_MAX_SCORE, displayMaxScoreItemEl.isSelected(0));
		modConfig.setBooleanEntry(IQEditController.CONFIG_KEY_HIDE_FEEDBACKS, !showFeedbacksEl.isSelected(0));
		modConfig.setBooleanEntry(IQEditController.CONFIG_ALLOW_ANONYM, allowAnonymEl.isSelected(0));
		
		if(qtiModule.isDigitalSignatureEnabled() && digitalSignatureEl.isSelected(0)) {
			modConfig.setBooleanEntry(IQEditController.CONFIG_DIGITAL_SIGNATURE, true);
			modConfig.setBooleanEntry(IQEditController.CONFIG_DIGITAL_SIGNATURE_SEND_MAIL, digitalSignatureMailEl.isSelected(0));
		} else {
			modConfig.setBooleanEntry(IQEditController.CONFIG_DIGITAL_SIGNATURE, false);
			modConfig.setBooleanEntry(IQEditController.CONFIG_DIGITAL_SIGNATURE_SEND_MAIL, false);
		}
		
		if(maxTimeEl.isAtLeastSelected(1)) {
			int maxTime = 0;
			if(StringHelper.containsNonWhitespace(maxTimeHourEl.getValue())) {
				maxTime += Integer.parseInt(maxTimeHourEl.getValue()) * 3600;
			}
			if(StringHelper.containsNonWhitespace(maxTimeMinuteEl.getValue())) {
				maxTime += Integer.parseInt(maxTimeMinuteEl.getValue()) * 60;
			}
			if(maxTime > 0) {
				modConfig.setIntValue(IQEditController.CONFIG_KEY_TIME_LIMIT, maxTime);
			} else {
				modConfig.remove(IQEditController.CONFIG_KEY_TIME_LIMIT);
			}
		} else {
			modConfig.remove(IQEditController.CONFIG_KEY_TIME_LIMIT);
		}
	}
}
