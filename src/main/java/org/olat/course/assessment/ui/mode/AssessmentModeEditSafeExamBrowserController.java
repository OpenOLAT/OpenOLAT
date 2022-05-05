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
package org.olat.course.assessment.ui.mode;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.helpers.Settings;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentMode;
import org.olat.course.assessment.AssessmentMode.Status;
import org.olat.course.assessment.AssessmentModeCoordinationService;
import org.olat.course.assessment.AssessmentModeManager;
import org.olat.course.assessment.AssessmentModule;
import org.olat.course.assessment.model.AssessmentModeManagedFlag;
import org.olat.course.assessment.model.SafeExamBrowserConfiguration;
import org.olat.course.nodes.CourseNode;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 26 janv. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentModeEditSafeExamBrowserController extends FormBasicController {

	private static final String[] onKeys = new String[]{ "on" };
	private static final String[] onValues = new String[]{ "" };

	private SingleSelection typeOfUseEl;
	private SingleSelection downloadConfigEl;
	private SingleSelection allowToExitEl;
	private SingleSelection quitUrlConfirmEl;
	private TextElement passwordToQuitEl;
	private SingleSelection enableReloadInExamEl;
	private SingleSelection showSebTaskListEl;
	private SingleSelection browserViewModeEl;
	private SingleSelection showTimeClockEl;
	private SingleSelection showReloadButtonEl;
	private SingleSelection showKeyboardLayoutEl;
	private SingleSelection showAudioOptionsEl;
	private SingleSelection audioMuteEl;
	
	private SingleSelection allowAudioCaptureEl;
	private SingleSelection allowVideoCaptureEl;
	private SingleSelection allowWlanEl;
	private SingleSelection allowSpellCheckEl;
	private SingleSelection allowZoomEl;
	
	private SingleSelection urlFilterEl;
	private SingleSelection urlContentFilterEl;
	private TextElement allowedExpressionsEl;
	private TextElement allowedRegexEl;
	private TextElement blockedExpressionsEl;
	private TextElement blockedRegexEl;
	private StaticTextElement safeExamBrowserConfigKeyEl;
	
	private TextElement safeExamBrowserKeyEl;
	private RichTextElement safeExamBrowserHintEl;
	private MultipleSelectionElement safeExamBrowserEl;
	
	private FormLayoutContainer sebConfigCont;
	
	private CloseableModalController cmc;
	private DialogBoxController confirmCtrl;
	
	private RepositoryEntry entry;
	private AssessmentMode assessmentMode;
	private final OLATResourceable courseOres;

	@Autowired
	private AssessmentModule assessmentModule;
	@Autowired
	private AssessmentModeManager assessmentModeMgr;
	@Autowired
	private AssessmentModeCoordinationService modeCoordinationService;
	
	public AssessmentModeEditSafeExamBrowserController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry entry, AssessmentMode assessmentMode) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		this.entry = entry;
		
		
		courseOres = OresHelper.clone(entry.getOlatResource());
		if(assessmentMode.getKey() == null) {
			this.assessmentMode = assessmentMode;
		} else {
			this.assessmentMode = assessmentModeMgr.getAssessmentModeById(assessmentMode.getKey());
		}
		initForm(ureq);
		updateUI();
	}
	
	public AssessmentMode getAssessmentMode() {
		return assessmentMode;
	}
	
	private boolean isEditable() {
		Status status = assessmentMode.getStatus();
		return status == null || status == Status.none;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
		FormLayoutContainer enableCont = FormLayoutContainer.createDefaultFormLayout("enable.container", getTranslator());
		formLayout.add(enableCont);
		
		enableCont.setElementCssClass("o_sel_assessment_mode_edit_form");
		setFormContextHelp("manual_user/e-assessment/Assessment_mode/");
		setFormDescription("form.mode.description");
		
		ICourse course = CourseFactory.loadCourse(courseOres);
		if(StringHelper.containsNonWhitespace(assessmentMode.getStartElement())) {
			CourseNode startElement = course.getRunStructure().getNode(assessmentMode.getStartElement());
			if(startElement == null) {
				setFormWarning("warning.missing.start.element");
			}
		}
		
		if(StringHelper.containsNonWhitespace(assessmentMode.getElementList())) {
			String elements = assessmentMode.getElementList();
			for(String element:elements.split(",")) {
				CourseNode node = course.getRunStructure().getNode(element);
				if(node == null) {
					setFormWarning("warning.missing.element");
				}
			}
		}

		Status status = assessmentMode.getStatus();
		boolean editable = isEditable() && !AssessmentModeManagedFlag.isManaged(assessmentMode, AssessmentModeManagedFlag.safeexambrowser);
		
		safeExamBrowserEl = uifactory.addCheckboxesHorizontal("safeexam", "mode.safeexambrowser", enableCont, onKeys, onValues);
		safeExamBrowserEl.select(onKeys[0], assessmentMode.isSafeExamBrowser());
		safeExamBrowserEl.addActionListener(FormEvent.ONCHANGE);
		safeExamBrowserEl.setEnabled(editable);
		
		SelectionValues typeOfUse = new SelectionValues();
		typeOfUse.add(SelectionValues.entry("keys", translate("mode.safeexambrowser.type.keys")));
		typeOfUse.add(SelectionValues.entry("inConfig", translate("mode.safeexambrowser.type.inOpenOlat")));
		typeOfUseEl = uifactory.addRadiosHorizontal("mode.safeexambrowser.typeofuse", enableCont, typeOfUse.keys(), typeOfUse.values());
		typeOfUseEl.setEnabled(editable);
		typeOfUseEl.addActionListener(FormEvent.ONCHANGE);
		
		SafeExamBrowserConfiguration sebConfig = assessmentMode.getSafeExamBrowserConfiguration();
		if(StringHelper.containsNonWhitespace(assessmentMode.getSafeExamBrowserKey())) {
			typeOfUseEl.select("keys", true);
		} else {
			typeOfUseEl.select("inConfig", true);
		}
		
		if(sebConfig == null) {
			// create a default configuration
			sebConfig = assessmentModule.getSafeExamBrowserConfigurationDefaultConfiguration();
		}
		
		// configuration
		SelectionValues trueFalseValues = new SelectionValues();
		trueFalseValues.add(SelectionValues.entry("true", translate("yes")));
		trueFalseValues.add(SelectionValues.entry("false", translate("no")));
		
		downloadConfigEl = uifactory.addRadiosHorizontal("mode.safeexambrowser.download.config", enableCont,
				trueFalseValues.keys(), trueFalseValues.values());
		downloadConfigEl.select(trueFalseKey(assessmentMode.isSafeExamBrowserConfigDownload()), true);
		downloadConfigEl.setEnabled(!AssessmentModeManagedFlag.isManaged(assessmentMode, AssessmentModeManagedFlag.safeexambrowser));
		
		sebConfigCont = FormLayoutContainer.createDefaultFormLayout("seb.config", getTranslator());
		sebConfigCont.setFormTitle(translate("mode.safeexambrowser.section.title"));
		formLayout.add(sebConfigCont);

		quitUrlConfirmEl = uifactory.addRadiosHorizontal("mode.safeexambrowser.confirm.exit", sebConfigCont,
				trueFalseValues.keys(), trueFalseValues.values());
		quitUrlConfirmEl.setEnabled(editable);
		quitUrlConfirmEl.select(trueFalseKey(sebConfig.isQuitURLConfirm()), true);
		
		allowToExitEl = uifactory.addRadiosHorizontal("mode.safeexambrowser.allow.toexit", sebConfigCont,
				trueFalseValues.keys(), trueFalseValues.values());
		allowToExitEl.addActionListener(FormEvent.ONCHANGE);
		allowToExitEl.setEnabled(editable);
		allowToExitEl.select(trueFalseKey(sebConfig.isAllowQuit()), true);
		
		String password = sebConfig.getPasswordToExit();
		passwordToQuitEl = uifactory.addTextElement("password.quit", "mode.safeexambrowser.password.exit", 255, password, sebConfigCont);
		passwordToQuitEl.setEnabled(editable);
		
		enableReloadInExamEl = uifactory.addRadiosHorizontal("mode.safeexambrowser.enable.reload", sebConfigCont,
				trueFalseValues.keys(), trueFalseValues.values());
		enableReloadInExamEl.setEnabled(editable);
		enableReloadInExamEl.select(trueFalseKey(sebConfig.isBrowserWindowAllowReload()), true);
		
		SelectionValues viewModeValues = new SelectionValues();
		viewModeValues.add(SelectionValues.entry(Integer.toString(SafeExamBrowserConfiguration.BROWSERVIEWMODE_WINDOW),
				translate("mode.safeexambrowser.browser.view.mode.window")));
		viewModeValues.add(SelectionValues.entry(Integer.toString(SafeExamBrowserConfiguration.BROWSERVIEWMODE_FULLSCREEN),
				translate("mode.safeexambrowser.browser.view.mode.fullscreen")));
		viewModeValues.add(SelectionValues.entry(Integer.toString(SafeExamBrowserConfiguration.BROWSERVIEWMODE_TOUCH),
				translate("mode.safeexambrowser.browser.view.mode.touch")));
		browserViewModeEl = uifactory.addRadiosHorizontal("mode.safeexambrowser.browser.view.mode", sebConfigCont,
				viewModeValues.keys(), viewModeValues.values());
		browserViewModeEl.setEnabled(editable);
		browserViewModeEl.select(Integer.toString(sebConfig.getBrowserViewMode() < 0 ? 0 : sebConfig.getBrowserViewMode()), true);
		
		showSebTaskListEl = uifactory.addRadiosHorizontal("mode.safeexambrowser.show.tasklist", sebConfigCont,
				trueFalseValues.keys(), trueFalseValues.values());
		showSebTaskListEl.addActionListener(FormEvent.ONCHANGE);
		showSebTaskListEl.setEnabled(editable);
		showSebTaskListEl.select(trueFalseKey(sebConfig.isShowTaskBar()), true);
		
		showReloadButtonEl = uifactory.addRadiosHorizontal("mode.safeexambrowser.show.reload.button", sebConfigCont,
				trueFalseValues.keys(), trueFalseValues.values());
		showReloadButtonEl.setEnabled(editable);
		showReloadButtonEl.select(trueFalseKey(sebConfig.isShowReloadButton()), true);
		
		showTimeClockEl = uifactory.addRadiosHorizontal("mode.safeexambrowser.show.timeclock", sebConfigCont,
				trueFalseValues.keys(), trueFalseValues.values());
		showTimeClockEl.setEnabled(editable);
		showTimeClockEl.select(trueFalseKey(sebConfig.isShowTimeClock()), true);
		
		showKeyboardLayoutEl = uifactory.addRadiosHorizontal("mode.safeexambrowser.show.keyboard", sebConfigCont,
				trueFalseValues.keys(), trueFalseValues.values());
		showKeyboardLayoutEl.setEnabled(editable);
		showKeyboardLayoutEl.select(trueFalseKey(sebConfig.isShowKeyboardLayout()), true);
		
		showAudioOptionsEl = uifactory.addRadiosHorizontal("mode.safeexambrowser.show.audio", sebConfigCont,
				trueFalseValues.keys(), trueFalseValues.values());
		showAudioOptionsEl.addActionListener(FormEvent.ONCHANGE);
		showAudioOptionsEl.setEnabled(editable);
		showAudioOptionsEl.select(trueFalseKey(sebConfig.isAudioControlEnabled()), true);
		
		allowWlanEl = uifactory.addRadiosHorizontal("mode.safeexambrowser.allow.wlan", sebConfigCont,
				trueFalseValues.keys(), trueFalseValues.values());
		allowWlanEl.setEnabled(editable);
		allowWlanEl.select(trueFalseKey(sebConfig.isAllowWlan()), true);
		
		audioMuteEl = uifactory.addRadiosHorizontal("mode.safeexambrowser.audio.mute", sebConfigCont,
				trueFalseValues.keys(), trueFalseValues.values());
		audioMuteEl.setEnabled(editable);
		audioMuteEl.select(trueFalseKey(sebConfig.isAudioMute()), true);
		
		allowAudioCaptureEl = uifactory.addRadiosHorizontal("mode.safeexambrowser.allow.audio.capture", sebConfigCont,
				trueFalseValues.keys(), trueFalseValues.values());
		allowAudioCaptureEl.setEnabled(editable);
		allowAudioCaptureEl.select(trueFalseKey(sebConfig.isAllowAudioCapture()), true);
		
		allowVideoCaptureEl = uifactory.addRadiosHorizontal("mode.safeexambrowser.allow.video.capture", sebConfigCont,
				trueFalseValues.keys(), trueFalseValues.values());
		allowVideoCaptureEl.setEnabled(editable);
		allowVideoCaptureEl.select(trueFalseKey(sebConfig.isAllowVideoCapture()), true);
		
		allowSpellCheckEl = uifactory.addRadiosHorizontal("mode.safeexambrowser.show.spellchecking", sebConfigCont,
				trueFalseValues.keys(), trueFalseValues.values());
		allowSpellCheckEl.setEnabled(editable);
		allowSpellCheckEl.select(trueFalseKey(sebConfig.isAllowSpellCheck()), true);
		
		allowZoomEl = uifactory.addRadiosHorizontal("mode.safeexambrowser.zoom", sebConfigCont,
				trueFalseValues.keys(), trueFalseValues.values());
		allowZoomEl.setEnabled(editable);
		allowZoomEl.select(trueFalseKey(sebConfig.isAllowZoomInOut()), true);
		
		urlFilterEl = uifactory.addRadiosHorizontal("mode.safeexambrowser.url.filter", sebConfigCont,
				trueFalseValues.keys(), trueFalseValues.values());
		urlFilterEl.addActionListener(FormEvent.ONCHANGE);
		urlFilterEl.setEnabled(editable);
		urlFilterEl.select(trueFalseKey(sebConfig.isUrlFilter()), true);
		
		urlContentFilterEl = uifactory.addRadiosHorizontal("mode.safeexambrowser.url.content.filter", sebConfigCont,
				trueFalseValues.keys(), trueFalseValues.values());
		urlContentFilterEl.setEnabled(editable);
		urlContentFilterEl.select(trueFalseKey(sebConfig.isUrlContentFilter()), true);
		
		allowedExpressionsEl = uifactory.addTextAreaElement("mode.safeexambrowser.url.filter.allow.exp", "mode.safeexambrowser.url.filter.allow.exp",
				2000, 2, 60, false, false, sebConfig.getAllowedUrlExpressions(), sebConfigCont);
		allowedRegexEl = uifactory.addTextAreaElement("mode.safeexambrowser.url.filter.allow.regex", "mode.safeexambrowser.url.filter.allow.regex",
				2000, 2, 60, false, false, sebConfig.getAllowedUrlRegex(), sebConfigCont);
		blockedExpressionsEl = uifactory.addTextAreaElement("mode.safeexambrowser.url.filter.blocked.exp", "mode.safeexambrowser.url.filter.blocked.exp",
				2000, 2, 60, false, false, sebConfig.getBlockedUrlExpressions(), sebConfigCont);
		blockedRegexEl = uifactory.addTextAreaElement("mode.safeexambrowser.url.filter.blocked.regex", "mode.safeexambrowser.url.filter.blocked.regex",
				2000, 2, 60, false, false, sebConfig.getBlockedUrlRegex(), sebConfigCont);
		
		FormLayoutContainer keyConfigCont = FormLayoutContainer.createDefaultFormLayout("key.config", getTranslator());
		formLayout.add(keyConfigCont);
		
		safeExamBrowserConfigKeyEl = uifactory.addStaticTextElement("mode.safeexambrowser.config.key", assessmentMode.getSafeExamBrowserConfigPListKey(), keyConfigCont);
		safeExamBrowserConfigKeyEl.setExampleKey("mode.safeexambrowser.config.key.hint", null);
		
		String key = assessmentMode.getSafeExamBrowserKey();
		safeExamBrowserKeyEl = uifactory.addTextAreaElement("safeexamkey", "mode.safeexambrowser.key", 16000, 6, 60, false, false, key, keyConfigCont);
		safeExamBrowserKeyEl.setMaxLength(16000);
		safeExamBrowserKeyEl.setVisible(assessmentMode.isSafeExamBrowser());
		safeExamBrowserKeyEl.setEnabled(status != Status.end
				&& !AssessmentModeManagedFlag.isManaged(assessmentMode, AssessmentModeManagedFlag.safeexambrowser));
		String hint = assessmentMode.getSafeExamBrowserHint();
		safeExamBrowserHintEl = uifactory.addRichTextElementForStringData("safeexamhint", "mode.safeexambrowser.hint",
				hint, 10, -1, false, null, null, keyConfigCont, ureq.getUserSession(), getWindowControl());
		safeExamBrowserHintEl.setVisible(assessmentMode.isSafeExamBrowser());
		safeExamBrowserHintEl.setEnabled(status != Status.end
				&& !AssessmentModeManagedFlag.isManaged(assessmentMode, AssessmentModeManagedFlag.safeexambrowser));
		
		FormLayoutContainer buttonCont = FormLayoutContainer.createButtonLayout("button", getTranslator());
		keyConfigCont.add(buttonCont);
		uifactory.addFormCancelButton("cancel", buttonCont, ureq, getWindowControl());
		if(status != Status.end && !AssessmentModeManagedFlag.isManaged(assessmentMode, AssessmentModeManagedFlag.safeexambrowser)) {
			uifactory.addFormSubmitButton("save", buttonCont);
		}
	}
	
	private void updateUI() {
		boolean enabled = safeExamBrowserEl.isSelected(0);
		boolean inConfig = typeOfUseEl.isOneSelected() && typeOfUseEl.isKeySelected("inConfig");
		boolean urlFilterEnabled = urlFilterEl.isOneSelected() && urlFilterEl.isKeySelected("true");
		boolean audioControl = showAudioOptionsEl.isOneSelected() && showAudioOptionsEl.isKeySelected("true");
		boolean taskBar = showSebTaskListEl.isOneSelected() && showSebTaskListEl.isKeySelected("true");
		boolean allowExit = allowToExitEl.isOneSelected() && allowToExitEl.isKeySelected("true");
		
		typeOfUseEl.setVisible(enabled);
		
		downloadConfigEl.setVisible(enabled && inConfig);
		
		// In configuration
		browserViewModeEl.setVisible(enabled && inConfig);
		quitUrlConfirmEl.setVisible(enabled && inConfig);
		allowToExitEl.setVisible(enabled && inConfig);
		passwordToQuitEl.setVisible(enabled && inConfig && allowExit);
		enableReloadInExamEl.setVisible(enabled && inConfig);
		
		showSebTaskListEl.setVisible(enabled && inConfig);
		showTimeClockEl.setVisible(enabled && inConfig && taskBar);
		showKeyboardLayoutEl.setVisible(enabled && inConfig && taskBar);
		showReloadButtonEl.setVisible(enabled && inConfig && taskBar);
		allowWlanEl.setVisible(enabled && inConfig && taskBar);
		
		showAudioOptionsEl.setVisible(enabled && inConfig);
		audioMuteEl.setVisible(enabled && inConfig && audioControl);
		
		allowAudioCaptureEl.setVisible(enabled && inConfig);
		allowVideoCaptureEl.setVisible(enabled && inConfig);
		allowSpellCheckEl.setVisible(enabled && inConfig);
		allowZoomEl.setVisible(enabled && inConfig);
		urlFilterEl.setVisible(enabled && inConfig);
		urlContentFilterEl.setVisible(enabled && inConfig && urlFilterEnabled);
		allowedExpressionsEl.setVisible(enabled && inConfig && urlFilterEnabled);
		allowedRegexEl.setVisible(enabled && inConfig && urlFilterEnabled);
		blockedExpressionsEl.setVisible(enabled && inConfig && urlFilterEnabled);
		blockedRegexEl.setVisible(enabled && inConfig && urlFilterEnabled);
		
		safeExamBrowserConfigKeyEl.setVisible(enabled && inConfig);
		sebConfigCont.setVisible(enabled && inConfig);

		// Keys
		safeExamBrowserKeyEl.setVisible(enabled && !inConfig);
		
		// Both
		safeExamBrowserHintEl.setVisible(enabled);
		if(enabled && !StringHelper.containsNonWhitespace(safeExamBrowserHintEl.getValue())) {
			safeExamBrowserHintEl.setValue(assessmentModule.getSafeExamBrowserHint());
		}
	}
	
	private String trueFalseKey(boolean val) {
		return val ? "true" : "false";
	}
	
	private SafeExamBrowserConfiguration getConfiguration() {
		SafeExamBrowserConfiguration configuration = new SafeExamBrowserConfiguration();
		configuration.setStartUrl(Settings.getServerContextPathURI());
		configuration.setAllowQuit(allowToExitEl.isKeySelected("true"));
		configuration.setQuitURLConfirm(quitUrlConfirmEl.isKeySelected("true"));
		if(StringHelper.containsNonWhitespace(passwordToQuitEl.getValue())) {
			configuration.setPasswordToExit(passwordToQuitEl.getValue());
		} else {
			configuration.setPasswordToExit(null);
		}
		configuration.setBrowserWindowAllowReload(enableReloadInExamEl.isKeySelected("true"));
		
		boolean showTaskBar = showSebTaskListEl.isKeySelected("true");
		configuration.setShowTaskBar(showTaskBar);
		if(showTaskBar) {
			configuration.setShowTimeClock(showTimeClockEl.isKeySelected("true"));
			configuration.setShowKeyboardLayout(showKeyboardLayoutEl.isKeySelected("true"));
			configuration.setShowReloadButton(showReloadButtonEl.isKeySelected("true"));
			configuration.setAllowWlan(allowWlanEl.isKeySelected("true"));
		} else {
			configuration.setShowTimeClock(true);
			configuration.setShowKeyboardLayout(true);
			configuration.setShowReloadButton(true);
			configuration.setAllowWlan(false);
		}
		
		boolean audioControl = showAudioOptionsEl.isKeySelected("true");
		configuration.setAudioControlEnabled(audioControl);
		if(audioControl) {
			configuration.setAudioMute(audioMuteEl.isKeySelected("true"));
		} else {
			configuration.setAudioMute(true);
		}
		configuration.setBrowserViewMode(Integer.parseInt(browserViewModeEl.getSelectedKey()));
		
		configuration.setAllowAudioCapture(allowAudioCaptureEl.isKeySelected("true"));
		configuration.setAllowVideoCapture(allowVideoCaptureEl.isKeySelected("true"));
		configuration.setAllowSpellCheck(allowSpellCheckEl.isKeySelected("true"));
		configuration.setAllowZoomInOut(allowZoomEl.isKeySelected("true"));
		
		configuration.setUrlFilter(urlFilterEl.isKeySelected("true"));
		configuration.setUrlContentFilter(urlContentFilterEl.isKeySelected("true"));
		configuration.setAllowedUrlExpressions(allowedExpressionsEl.getValue());
		configuration.setAllowedUrlRegex(allowedRegexEl.getValue());
		configuration.setBlockedUrlExpressions(blockedExpressionsEl.getValue());
		configuration.setBlockedUrlRegex(blockedRegexEl.getValue());
	
		return configuration;
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(confirmCtrl == source) {
			if(DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event)) {
				save(ureq, true);
			}
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(cmc);
		cmc = null;
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		safeExamBrowserKeyEl.clearError();
		if(safeExamBrowserEl.isAtLeastSelected(1) && this.typeOfUseEl.isKeySelected("keys")) {
			String value = safeExamBrowserKeyEl.getValue();
			if(!StringHelper.containsNonWhitespace(value)) {
				safeExamBrowserKeyEl.setErrorKey("form.legende.mandatory", null);
				allOk &= false;
			} else if(value.length() > safeExamBrowserKeyEl.getMaxLength()) {
				safeExamBrowserKeyEl.setErrorKey("form.error.toolong", new String[] { Integer.toString(safeExamBrowserKeyEl.getMaxLength()) } );
				allOk &= false;
			}
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(assessmentMode.getKey() != null) {
			assessmentMode = assessmentModeMgr.getAssessmentModeById(assessmentMode.getKey());
		}
		
		Status currentStatus = assessmentMode.getStatus();
		if(assessmentMode.isManualBeginEnd()) {
			//manual start don't change the status of the assessment
			save(ureq, false);
		} else {
			Status nextStatus = modeCoordinationService.evaluateStatus(assessmentMode.getBegin(), assessmentMode.getLeadTime(),
					assessmentMode.getEnd(), assessmentMode.getFollowupTime());
			if(currentStatus == nextStatus) {
				save(ureq, true);
			} else {
				String title = translate("confirm.status.change.title");
	
				String text;
				switch(nextStatus) {
					case none: text = translate("confirm.status.change.none"); break;
					case leadtime: text = translate("confirm.status.change.leadtime"); break;
					case assessment: text = translate("confirm.status.change.assessment"); break;
					case followup: text = translate("confirm.status.change.followup"); break;
					case end: text = translate("confirm.status.change.end"); break;
					default: text = "ERROR";
				}
				confirmCtrl = activateOkCancelDialog(ureq, title, text, confirmCtrl);
			}
		}
	}
	
	private void save(UserRequest ureq, boolean forceStatus) {
		if(assessmentMode.getKey() != null) {
			assessmentMode = assessmentModeMgr.getAssessmentModeById(assessmentMode.getKey());
		}

		boolean safeExamEnabled = safeExamBrowserEl.isAtLeastSelected(1);
		assessmentMode.setSafeExamBrowser(safeExamEnabled);
		if(safeExamEnabled) {
			if(typeOfUseEl.isSelected(0)) {
				assessmentMode.setSafeExamBrowserKey(safeExamBrowserKeyEl.getValue());
				assessmentMode.setSafeExamBrowserConfiguration(null);
			} else {
				assessmentMode.setSafeExamBrowserKey(null);
				assessmentMode.setSafeExamBrowserConfiguration(getConfiguration());
				assessmentMode.setSafeExamBrowserConfigDownload(downloadConfigEl.isOneSelected() && downloadConfigEl.isKeySelected("true"));
			}
			assessmentMode.setSafeExamBrowserHint(safeExamBrowserHintEl.getValue());
		} else {
			assessmentMode.setSafeExamBrowserKey(null);
			assessmentMode.setSafeExamBrowserHint(null);
			assessmentMode.setSafeExamBrowserConfiguration(null);
		}
	
		//mode need to be persisted for the following relations
		if(assessmentMode.getKey() == null) {
			assessmentMode = assessmentModeMgr.persist(assessmentMode);
		}

		assessmentMode = assessmentModeMgr.merge(assessmentMode, forceStatus);
		fireEvent(ureq, Event.CHANGED_EVENT);
		
		// Always update config key
		safeExamBrowserConfigKeyEl.setValue(assessmentMode.getSafeExamBrowserConfigPListKey());
		
		ChangeAssessmentModeEvent changedEvent = new ChangeAssessmentModeEvent(assessmentMode, entry);
		CoordinatorManager.getInstance().getCoordinator().getEventBus()
			.fireEventToListenersOf(changedEvent, ChangeAssessmentModeEvent.ASSESSMENT_MODE_ORES);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(safeExamBrowserEl == source || typeOfUseEl == source || urlFilterEl == source
				|| showSebTaskListEl == source || showAudioOptionsEl == source || allowToExitEl == source) {
			updateUI();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}