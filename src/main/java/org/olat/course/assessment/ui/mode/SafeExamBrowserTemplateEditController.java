/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.assessment.ui.mode;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.course.assessment.AssessmentModeManager;
import org.olat.course.assessment.SafeExamBrowserTemplate;
import org.olat.course.assessment.model.SafeExamBrowserConfiguration;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * Initial date: 19 Feb 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class SafeExamBrowserTemplateEditController extends FormBasicController {

	private TextElement nameEl;
	private FormToggle activeEl;

	private SingleSelection allowToExitEl;
	private TextElement passwordToQuitEl;
	private SingleSelection linkToQuitEl;
	private SingleSelection askUserToConfirmQuitEl;
	private SingleSelection enableReloadInExamEl;
	private SingleSelection browserViewModeEl;
	private SingleSelection showTaskBarEl;
	private SingleSelection showReloadButtonEl;
	private SingleSelection showTimeEl;
	private SingleSelection showInputLanguageEl;
	private SingleSelection allowWlanEl;
	private SingleSelection audioControlEnabledEl;
	private SingleSelection audioMuteEl;
	private SingleSelection allowAudioCaptureEl;
	private SingleSelection allowVideoCaptureEl;
	private SingleSelection allowSpellCheckEl;
	private SingleSelection allowZoomEl;
	private SingleSelection urlFilterEl;
	private SingleSelection urlContentFilterEl;
	private TextElement allowedExpressionsEl;
	private TextElement allowedRegexEl;
	private TextElement blockedExpressionsEl;
	private TextElement blockedRegexEl;
	private SafeExamBrowserTemplate sebTemplate;

	@Autowired
	private AssessmentModeManager assessmentModeManager;

	public SafeExamBrowserTemplateEditController(UserRequest ureq, WindowControl wControl, SafeExamBrowserTemplate sebTemplate) {
		super(ureq, wControl);
		this.sebTemplate = sebTemplate;

		initForm(ureq);
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		SafeExamBrowserConfiguration config = null;
		if (sebTemplate != null) {
			config = sebTemplate.getSafeExamBrowserConfiguration();
		}
		if (config == null) {
			config = new SafeExamBrowserConfiguration();
		}

		String name = sebTemplate != null ? sebTemplate.getName() : "";
		nameEl = uifactory.addTextElement("seb.template.name", "seb.template.name", 255, name, formLayout);
		nameEl.setMandatory(true);

		activeEl = uifactory.addToggleButton("seb.template.active", "seb.template.active", translate("on"), translate("off"), formLayout);
		activeEl.toggle(sebTemplate == null || sebTemplate.isActive());
		activeEl.setEnabled(sebTemplate == null || !sebTemplate.isDefault());

		uifactory.addSpacerElement("spacer", formLayout, false);

		SelectionValues trueFalseValues = new SelectionValues();
		trueFalseValues.add(SelectionValues.entry("true", translate("yes")));
		trueFalseValues.add(SelectionValues.entry("false", translate("no")));

		allowToExitEl = uifactory.addRadiosHorizontal("mode.safeexambrowser.allow.toexit", formLayout,
				trueFalseValues.keys(), trueFalseValues.values());
		allowToExitEl.addActionListener(FormEvent.ONCHANGE);
		allowToExitEl.select(trueFalseKey(config.isAllowQuit()), true);

		passwordToQuitEl = uifactory.addTextElement("password.quit", "mode.safeexambrowser.password.exit", 255, config.getPasswordToExit(), formLayout);

		linkToQuitEl = uifactory.addRadiosHorizontal("mode.safeexambrowser.link.to.quit", formLayout,
				trueFalseValues.keys(), trueFalseValues.values());
		linkToQuitEl.select(trueFalseKey(StringHelper.containsNonWhitespace(config.getLinkToQuit())), true);

		askUserToConfirmQuitEl = uifactory.addRadiosHorizontal("mode.safeexambrowser.confirm.exit", formLayout,
				trueFalseValues.keys(), trueFalseValues.values());
		askUserToConfirmQuitEl.select(trueFalseKey(config.isQuitURLConfirm()), true);

		enableReloadInExamEl = uifactory.addRadiosHorizontal("mode.safeexambrowser.enable.reload", formLayout,
				trueFalseValues.keys(), trueFalseValues.values());
		enableReloadInExamEl.select(trueFalseKey(config.isBrowserWindowAllowReload()), true);

		SelectionValues viewModeValues = new SelectionValues();
		viewModeValues.add(SelectionValues.entry(Integer.toString(SafeExamBrowserConfiguration.BROWSERVIEWMODE_WINDOW),
				translate("mode.safeexambrowser.browser.view.mode.window")));
		viewModeValues.add(SelectionValues.entry(Integer.toString(SafeExamBrowserConfiguration.BROWSERVIEWMODE_FULLSCREEN),
				translate("mode.safeexambrowser.browser.view.mode.fullscreen")));
		viewModeValues.add(SelectionValues.entry(Integer.toString(SafeExamBrowserConfiguration.BROWSERVIEWMODE_TOUCH),
				translate("mode.safeexambrowser.browser.view.mode.touch")));
		browserViewModeEl = uifactory.addRadiosHorizontal("mode.safeexambrowser.browser.view.mode", formLayout,
				viewModeValues.keys(), viewModeValues.values());
		browserViewModeEl.select(Integer.toString(config.getBrowserViewMode()), true);

		showTaskBarEl = uifactory.addRadiosHorizontal("mode.safeexambrowser.show.tasklist", formLayout,
				trueFalseValues.keys(), trueFalseValues.values());
		showTaskBarEl.addActionListener(FormEvent.ONCHANGE);
		showTaskBarEl.select(trueFalseKey(config.isShowTaskBar()), true);

		showReloadButtonEl = uifactory.addRadiosHorizontal("mode.safeexambrowser.show.reload.button", formLayout,
				trueFalseValues.keys(), trueFalseValues.values());
		showReloadButtonEl.select(trueFalseKey(config.isShowReloadButton()), true);

		showTimeEl = uifactory.addRadiosHorizontal("mode.safeexambrowser.show.timeclock", formLayout,
				trueFalseValues.keys(), trueFalseValues.values());
		showTimeEl.select(trueFalseKey(config.isShowTimeClock()), true);

		showInputLanguageEl = uifactory.addRadiosHorizontal("mode.safeexambrowser.show.keyboard", formLayout,
				trueFalseValues.keys(), trueFalseValues.values());
		showInputLanguageEl.select(trueFalseKey(config.isShowKeyboardLayout()), true);

		allowWlanEl = uifactory.addRadiosHorizontal("mode.safeexambrowser.allow.wlan", formLayout,
				trueFalseValues.keys(), trueFalseValues.values());
		allowWlanEl.select(trueFalseKey(config.isAllowWlan()), true);

		audioControlEnabledEl = uifactory.addRadiosHorizontal("mode.safeexambrowser.show.audio", formLayout,
				trueFalseValues.keys(), trueFalseValues.values());
		audioControlEnabledEl.addActionListener(FormEvent.ONCHANGE);
		audioControlEnabledEl.select(trueFalseKey(config.isAudioControlEnabled()), true);

		audioMuteEl = uifactory.addRadiosHorizontal("mode.safeexambrowser.audio.mute", formLayout,
				trueFalseValues.keys(), trueFalseValues.values());
		audioMuteEl.select(trueFalseKey(config.isAudioMute()), true);

		allowAudioCaptureEl = uifactory.addRadiosHorizontal("mode.safeexambrowser.allow.audio.capture", formLayout,
				trueFalseValues.keys(), trueFalseValues.values());
		allowAudioCaptureEl.select(trueFalseKey(config.isAllowAudioCapture()), true);

		allowVideoCaptureEl = uifactory.addRadiosHorizontal("mode.safeexambrowser.allow.video.capture", formLayout,
				trueFalseValues.keys(), trueFalseValues.values());
		allowVideoCaptureEl.select(trueFalseKey(config.isAllowVideoCapture()), true);

		allowSpellCheckEl = uifactory.addRadiosHorizontal("mode.safeexambrowser.show.spellchecking", formLayout,
				trueFalseValues.keys(), trueFalseValues.values());
		allowSpellCheckEl.select(trueFalseKey(config.isAllowSpellCheck()), true);

		allowZoomEl = uifactory.addRadiosHorizontal("mode.safeexambrowser.zoom", formLayout,
				trueFalseValues.keys(), trueFalseValues.values());
		allowZoomEl.select(trueFalseKey(config.isAllowZoomInOut()), true);

		urlFilterEl = uifactory.addRadiosHorizontal("mode.safeexambrowser.url.filter", formLayout,
				trueFalseValues.keys(), trueFalseValues.values());
		urlFilterEl.addActionListener(FormEvent.ONCHANGE);
		urlFilterEl.select(trueFalseKey(config.isUrlFilter()), true);

		urlContentFilterEl = uifactory.addRadiosHorizontal("mode.safeexambrowser.url.content.filter", formLayout,
				trueFalseValues.keys(), trueFalseValues.values());
		urlContentFilterEl.select(trueFalseKey(config.isUrlContentFilter()), true);

		allowedExpressionsEl = uifactory.addTextAreaElement("mode.safeexambrowser.url.filter.allow.exp", "mode.safeexambrowser.url.filter.allow.exp",
				2000, 2, 60, false, false, config.getAllowedUrlExpressions(), formLayout);
		allowedRegexEl = uifactory.addTextAreaElement("mode.safeexambrowser.url.filter.allow.regex", "mode.safeexambrowser.url.filter.allow.regex",
				2000, 2, 60, false, false, config.getAllowedUrlRegex(), formLayout);
		blockedExpressionsEl = uifactory.addTextAreaElement("mode.safeexambrowser.url.filter.blocked.exp", "mode.safeexambrowser.url.filter.blocked.exp",
				2000, 2, 60, false, false, config.getBlockedUrlExpressions(), formLayout);
		blockedRegexEl = uifactory.addTextAreaElement("mode.safeexambrowser.url.filter.blocked.regex", "mode.safeexambrowser.url.filter.blocked.regex",
				2000, 2, 60, false, false, config.getBlockedUrlRegex(), formLayout);

		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsCont.setRootForm(mainForm);
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("ok", sebTemplate != null ? "save" : "add", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}

	private void updateUI() {
		boolean allowExit = allowToExitEl.isOneSelected() && allowToExitEl.isKeySelected("true");
		passwordToQuitEl.setVisible(allowExit);

		boolean urlFilterEnabled = urlFilterEl.isOneSelected() && urlFilterEl.isKeySelected("true");
		urlContentFilterEl.setVisible(urlFilterEnabled);
		allowedExpressionsEl.setVisible(urlFilterEnabled);
		allowedRegexEl.setVisible(urlFilterEnabled);
		blockedExpressionsEl.setVisible(urlFilterEnabled);
		blockedRegexEl.setVisible(urlFilterEnabled);

		boolean audioControlEnabled = audioControlEnabledEl.isOneSelected() && audioControlEnabledEl.isKeySelected("true");
		audioMuteEl.setVisible(audioControlEnabled);

		boolean showTaskBar = showTaskBarEl.isOneSelected() && showTaskBarEl.isKeySelected("true");
		showReloadButtonEl.setVisible(showTaskBar);
		showTimeEl.setVisible(showTaskBar);
		showInputLanguageEl.setVisible(showTaskBar);
		allowWlanEl.setVisible(showTaskBar);
	}

	private String trueFalseKey(boolean val) {
		return val ? "true" : "false";
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		nameEl.clearError();
		if (!StringHelper.containsNonWhitespace(nameEl.getValue())) {
			nameEl.setErrorKey("form.mandatory.hover");
			allOk = false;
		} else if (nameEl.getValue().length() > 255) {
			nameEl.setErrorKey("form.error.toolong", "255");
			allOk = false;
		}

		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if (sebTemplate == null) {
			sebTemplate = assessmentModeManager.createSafeExamBrowserTemplate(nameEl.getValue());
		} else {
			sebTemplate.setName(nameEl.getValue());
		}
		sebTemplate.setActive(activeEl.isOn());
		
		SafeExamBrowserConfiguration config = new SafeExamBrowserConfiguration();
		config.setAllowQuit(isSelected(allowToExitEl));
		if(StringHelper.containsNonWhitespace(passwordToQuitEl.getValue())) {
			config.setPasswordToExit(passwordToQuitEl.getValue());
		} else {
			config.setPasswordToExit(null);
		}
		config.setLinkToQuit(isSelected(linkToQuitEl) ? "true" : null);
		config.setQuitURLConfirm(isSelected(askUserToConfirmQuitEl));
		config.setBrowserWindowAllowReload(isSelected(enableReloadInExamEl));
		config.setBrowserViewMode(Integer.parseInt(browserViewModeEl.getSelectedKey()));

		boolean showTaskBar = isSelected(showTaskBarEl);
		config.setShowTaskBar(showTaskBar);
		if (showTaskBar) {
			config.setShowReloadButton(isSelected(showReloadButtonEl));
			config.setShowTimeClock(isSelected(showTimeEl));
			config.setShowKeyboardLayout(isSelected(showInputLanguageEl));
			config.setAllowWlan(isSelected(allowWlanEl));
		} else {
			config.setShowReloadButton(true);
			config.setShowTimeClock(true);
			config.setShowKeyboardLayout(true);
			config.setAllowWlan(true);
		}

		boolean audioControlEnabled = isSelected(audioControlEnabledEl);
		config.setAudioControlEnabled(audioControlEnabled);
		if (audioControlEnabled) {
			config.setAudioMute(isSelected(audioMuteEl));
		} else {
			config.setAudioMute(true);
		}
		config.setAllowAudioCapture(isSelected(allowAudioCaptureEl));
		config.setAllowVideoCapture(isSelected(allowVideoCaptureEl));

		config.setAllowSpellCheck(isSelected(allowSpellCheckEl));
		config.setAllowZoomInOut(isSelected(allowZoomEl));

		boolean urlFilter = isSelected(urlFilterEl);
		config.setUrlFilter(urlFilter);
		boolean urlContentFilter = urlFilter && isSelected(urlContentFilterEl);
		config.setUrlContentFilter(urlContentFilter);
		if (urlFilter) {
			config.setAllowedUrlExpressions(allowedExpressionsEl.getValue());
			config.setAllowedUrlRegex(allowedRegexEl.getValue());
			config.setBlockedUrlExpressions(blockedExpressionsEl.getValue());
			config.setBlockedUrlRegex(blockedRegexEl.getValue());
		}
		sebTemplate.setSafeExamBrowserConfiguration(config);

		assessmentModeManager.updateSafeExamBrowserTemplate(sebTemplate);
		fireEvent(ureq, Event.DONE_EVENT);
	}

	private boolean isSelected(SingleSelection el) {
		return el.isOneSelected() && el.isKeySelected("true");
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (urlFilterEl == source || showTaskBarEl == source || audioControlEnabledEl == source
				|| allowToExitEl == source) {
			updateUI();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
