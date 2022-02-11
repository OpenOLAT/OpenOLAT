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
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.assessment.AssessmentModule;
import org.olat.course.assessment.model.SafeExamBrowserConfiguration;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 26 janv. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SafeExamBrowserAdminController extends FormBasicController {

	private SingleSelection allowToExitEl;
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
	private SingleSelection allowSpellCheckEl;
	private SingleSelection allowZoomEl;
	private SingleSelection allowAudioCaptureEl;
	private SingleSelection allowVideoCaptureEl;
	
	private SingleSelection urlFilterEl;
	private SingleSelection urlContentFilterEl;
	private TextElement allowedExpressionsEl;
	private TextElement allowedRegexEl;
	private TextElement blockedExpressionsEl;
	private TextElement blockedRegexEl;
	
	private TextElement safeExamBrowserHintEl;
	
	@Autowired
	private AssessmentModule assessmentModule;
	
	public SafeExamBrowserAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);

		initForm(ureq);
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_assessment_mode_template_form");
		setFormContextHelp("Assessment mode");
		
		setFormTitle("admin.assessment.mode.seb.title");

		// configuration
		SelectionValues trueFalseValues = new SelectionValues();
		trueFalseValues.add(SelectionValues.entry("true", translate("yes")));
		trueFalseValues.add(SelectionValues.entry("false", translate("no")));
		
		askUserToConfirmQuitEl = uifactory.addRadiosHorizontal("mode.safeexambrowser.confirm.exit", formLayout,
				trueFalseValues.keys(), trueFalseValues.values());
		askUserToConfirmQuitEl.select(trueFalseKey(assessmentModule.isSafeExamBrowserQuitURLConfirm()), true);
		
		allowToExitEl = uifactory.addRadiosHorizontal("mode.safeexambrowser.allow.toexit", formLayout,
				trueFalseValues.keys(), trueFalseValues.values());
		allowToExitEl.select(trueFalseKey(assessmentModule.isSafeExamBrowserAllowQuit()), true);
		
		enableReloadInExamEl = uifactory.addRadiosHorizontal("mode.safeexambrowser.enable.reload", formLayout,
				trueFalseValues.keys(), trueFalseValues.values());
		enableReloadInExamEl.select(trueFalseKey(assessmentModule.isSafeExamBrowserBrowserWindowAllowReload()), true);
		
		SelectionValues viewModeValues = new SelectionValues();
		viewModeValues.add(SelectionValues.entry(Integer.toString(SafeExamBrowserConfiguration.BROWSERVIEWMODE_WINDOW),
				translate("mode.safeexambrowser.browser.view.mode.window")));
		viewModeValues.add(SelectionValues.entry(Integer.toString(SafeExamBrowserConfiguration.BROWSERVIEWMODE_FULLSCREEN),
				translate("mode.safeexambrowser.browser.view.mode.fullscreen")));
		viewModeValues.add(SelectionValues.entry(Integer.toString(SafeExamBrowserConfiguration.BROWSERVIEWMODE_TOUCH),
				translate("mode.safeexambrowser.browser.view.mode.touch")));
		browserViewModeEl = uifactory.addRadiosHorizontal("mode.safeexambrowser.browser.view.mode", formLayout,
				viewModeValues.keys(), viewModeValues.values());
		browserViewModeEl.select(Integer.toString(assessmentModule.getSafeExamBrowserViewMode()), true);
		
		showTaskBarEl = uifactory.addRadiosHorizontal("mode.safeexambrowser.show.tasklist", formLayout,
				trueFalseValues.keys(), trueFalseValues.values());
		showTaskBarEl.addActionListener(FormEvent.ONCHANGE);
		showTaskBarEl.select(trueFalseKey(assessmentModule.isSafeExamBrowserShowTaskBar()), true);
		
		showReloadButtonEl = uifactory.addRadiosHorizontal("mode.safeexambrowser.show.reload.button", formLayout,
				trueFalseValues.keys(), trueFalseValues.values());
		showReloadButtonEl.select(trueFalseKey(assessmentModule.isSafeExamBrowserShowReloadButton()), true);
		
		showTimeEl = uifactory.addRadiosHorizontal("mode.safeexambrowser.show.timeclock", formLayout,
				trueFalseValues.keys(), trueFalseValues.values());
		showTimeEl.select(trueFalseKey(assessmentModule.isSafeExamBrowserShowTime()), true);
		
		showInputLanguageEl = uifactory.addRadiosHorizontal("mode.safeexambrowser.show.keyboard", formLayout,
				trueFalseValues.keys(), trueFalseValues.values());
		showInputLanguageEl.select(trueFalseKey(assessmentModule.isSafeExamBrowserShowInputLanguage()), true);
		
		allowWlanEl = uifactory.addRadiosHorizontal("mode.safeexambrowser.allow.wlan", formLayout,
				trueFalseValues.keys(), trueFalseValues.values());
		allowWlanEl.select(trueFalseKey(assessmentModule.isSafeExamBrowserAllowWlan()), true);
		
		audioControlEnabledEl = uifactory.addRadiosHorizontal("mode.safeexambrowser.show.audio", formLayout,
				trueFalseValues.keys(), trueFalseValues.values());
		audioControlEnabledEl.addActionListener(FormEvent.ONCHANGE);
		audioControlEnabledEl.select(trueFalseKey(assessmentModule.isSafeExamBrowserAudioControlEnabled()), true);
		
		audioMuteEl  = uifactory.addRadiosHorizontal("mode.safeexambrowser.audio.mute", formLayout,
				trueFalseValues.keys(), trueFalseValues.values());
		audioMuteEl.select(trueFalseKey(assessmentModule.isSafeExamBrowserAudioMute()), true);
		
		allowAudioCaptureEl = uifactory.addRadiosHorizontal("mode.safeexambrowser.allow.audio.capture", formLayout,
				trueFalseValues.keys(), trueFalseValues.values());
		allowAudioCaptureEl.select(trueFalseKey(assessmentModule.isSafeExamBrowserAllowAudioCapture()), true);
		
		allowVideoCaptureEl = uifactory.addRadiosHorizontal("mode.safeexambrowser.allow.video.capture", formLayout,
				trueFalseValues.keys(), trueFalseValues.values());
		allowVideoCaptureEl.select(trueFalseKey(assessmentModule.isSafeExamBrowserAllowVideoCapture()), true);
		
		allowSpellCheckEl = uifactory.addRadiosHorizontal("mode.safeexambrowser.show.spellchecking", formLayout,
				trueFalseValues.keys(), trueFalseValues.values());
		allowSpellCheckEl.select(trueFalseKey(assessmentModule.isSafeExamBrowserAllowSpellCheck()), true);
		
		allowZoomEl = uifactory.addRadiosHorizontal("mode.safeexambrowser.zoom", formLayout,
				trueFalseValues.keys(), trueFalseValues.values());
		allowZoomEl.select(trueFalseKey(assessmentModule.isSafeExamBrowserAllowZoomInOut()), true);
		
		urlFilterEl = uifactory.addRadiosHorizontal("mode.safeexambrowser.url.filter", formLayout,
				trueFalseValues.keys(), trueFalseValues.values());
		urlFilterEl.addActionListener(FormEvent.ONCHANGE);
		urlFilterEl.select(trueFalseKey(assessmentModule.isSafeExamBrowserUrlFilter()), true);
		
		urlContentFilterEl = uifactory.addRadiosHorizontal("mode.safeexambrowser.url.content.filter", formLayout,
				trueFalseValues.keys(), trueFalseValues.values());
		urlContentFilterEl.select(trueFalseKey(assessmentModule.isSafeExamBrowserUrlContentFilter()), true);
		
		allowedExpressionsEl = uifactory.addTextAreaElement("mode.safeexambrowser.url.filter.allow.exp", "mode.safeexambrowser.url.filter.allow.exp",
				2000, 2, 60, false, false, assessmentModule.getSafeExamBrowserAllowedUrlExpressions(), formLayout);
		allowedRegexEl = uifactory.addTextAreaElement("mode.safeexambrowser.url.filter.allow.regex", "mode.safeexambrowser.url.filter.allow.regex",
				2000, 2, 60, false, false, assessmentModule.getSafeExamBrowserAllowedUrlRegex(), formLayout);
		blockedExpressionsEl = uifactory.addTextAreaElement("mode.safeexambrowser.url.filter.blocked.exp", "mode.safeexambrowser.url.filter.blocked.exp",
				2000, 2, 60, false, false, assessmentModule.getSafeExamBrowserBlockedUrlExpressions(), formLayout);
		blockedRegexEl = uifactory.addTextAreaElement("mode.safeexambrowser.url.filter.blocked.regex", "mode.safeexambrowser.url.filter.blocked.regex",
				2000, 2, 60, false, false, assessmentModule.getSafeExamBrowserBlockedUrlRegex(), formLayout);

		String hint = null;

		safeExamBrowserHintEl = uifactory.addRichTextElementForStringData("safeexamhint", "mode.safeexambrowser.hint",
				hint, 10, -1, false, null, null, formLayout, ureq.getUserSession(), getWindowControl());
		
		FormLayoutContainer buttonCont = FormLayoutContainer.createButtonLayout("button", getTranslator());
		formLayout.add(buttonCont);
		uifactory.addFormSubmitButton("save", buttonCont);
	}
	
	private void updateUI() {
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
	protected void formOK(UserRequest ureq) {
		assessmentModule.setSafeExamBrowserViewMode(Integer.parseInt(browserViewModeEl.getSelectedKey()));
		
		boolean showTaskBar = showTaskBarEl.isOneSelected() && showTaskBarEl.isKeySelected("true");
		assessmentModule.setSafeExamBrowserShowTaskBar(showTaskBar);
		if(showTaskBar) {
			assessmentModule.setSafeExamBrowserShowReloadButton(showReloadButtonEl.isOneSelected() && showReloadButtonEl.isKeySelected("true"));
			assessmentModule.setSafeExamBrowserShowTime(showTimeEl.isOneSelected() && showTimeEl.isKeySelected("true"));
			assessmentModule.setSafeExamBrowserShowInputLanguage(showInputLanguageEl.isOneSelected() && showInputLanguageEl.isKeySelected("true"));
			assessmentModule.setSafeExamBrowserAllowWlan(allowWlanEl.isOneSelected() && allowWlanEl.isKeySelected("true"));
		} else {
			assessmentModule.setSafeExamBrowserShowReloadButton(true);
			assessmentModule.setSafeExamBrowserShowTime(true);
			assessmentModule.setSafeExamBrowserShowInputLanguage(true);
			assessmentModule.setSafeExamBrowserAllowWlan(true);
		}
		
		assessmentModule.setSafeExamBrowserAllowQuit(allowToExitEl.isOneSelected() && allowToExitEl.isKeySelected("true"));
		assessmentModule.setSafeExamBrowserQuitURLConfirm(askUserToConfirmQuitEl.isOneSelected() && askUserToConfirmQuitEl.isKeySelected("true"));
		
		boolean audioControlEnabled = audioControlEnabledEl.isOneSelected() && audioControlEnabledEl.isKeySelected("true");
		assessmentModule.setSafeExamBrowserAudioControlEnabled(audioControlEnabled);
		if(audioControlEnabled) {
			assessmentModule.setSafeExamBrowserAudioMute(audioMuteEl.isOneSelected() && audioMuteEl.isKeySelected("true"));
		} else {
			assessmentModule.setSafeExamBrowserAudioMute(true);
		}
		assessmentModule.setSafeExamBrowserAllowAudioCapture(allowAudioCaptureEl.isOneSelected() && allowAudioCaptureEl.isKeySelected("true"));
		assessmentModule.setSafeExamBrowserAllowVideoCapture(allowVideoCaptureEl.isOneSelected() && allowVideoCaptureEl.isKeySelected("true"));

		assessmentModule.setSafeExamBrowserAllowSpellCheck(allowSpellCheckEl.isOneSelected() && allowSpellCheckEl.isKeySelected("true"));
		assessmentModule.setSafeExamBrowserAllowZoomInOut(allowZoomEl.isOneSelected() && allowZoomEl.isKeySelected("true"));
		assessmentModule.setSafeExamBrowserBrowserWindowAllowReload(enableReloadInExamEl.isOneSelected() && enableReloadInExamEl.isKeySelected("true"));
		assessmentModule.setSafeExamBrowserUrlFilter(urlFilterEl.isOneSelected() && urlFilterEl.isKeySelected("true"));
		assessmentModule.setSafeExamBrowserUrlContentFilter(urlContentFilterEl.isOneSelected() && urlContentFilterEl.isKeySelected("true"));
		
		assessmentModule.setSafeExamBrowserHint(safeExamBrowserHintEl.getValue());
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(urlFilterEl == source || showTaskBarEl == source || audioControlEnabledEl == source) {
			updateUI();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}