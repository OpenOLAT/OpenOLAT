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

import java.util.UUID;

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
import org.olat.core.helpers.Settings;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.assessment.AssessmentModule;
import org.olat.course.assessment.SafeExamBrowserEnabled;
import org.olat.course.assessment.model.SafeExamBrowserConfiguration;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 26 janv. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public abstract class AbstractEditSafeExamBrowserController extends FormBasicController {

	private static final String[] onKeys = new String[]{ "on" };
	private static final String[] onValues = new String[]{ "" };

	protected SingleSelection typeOfUseEl;
	protected SingleSelection downloadConfigEl;
	protected SingleSelection allowToExitEl;
	protected SingleSelection linkToQuitEl;
	protected SingleSelection quitUrlConfirmEl;
	protected TextElement passwordToQuitEl;
	protected SingleSelection enableReloadInExamEl;
	protected SingleSelection showSebTaskListEl;
	protected SingleSelection browserViewModeEl;
	protected SingleSelection showTimeClockEl;
	protected SingleSelection showReloadButtonEl;
	protected SingleSelection showKeyboardLayoutEl;
	protected SingleSelection showAudioOptionsEl;
	protected SingleSelection audioMuteEl;
	
	protected SingleSelection allowAudioCaptureEl;
	protected SingleSelection allowVideoCaptureEl;
	protected SingleSelection allowWlanEl;
	protected SingleSelection allowSpellCheckEl;
	protected SingleSelection allowZoomEl;
	
	protected SingleSelection urlFilterEl;
	protected SingleSelection urlContentFilterEl;
	protected TextElement allowedExpressionsEl;
	protected TextElement allowedRegexEl;
	protected TextElement blockedExpressionsEl;
	protected TextElement blockedRegexEl;
	protected StaticTextElement safeExamBrowserConfigKeyEl;
	
	protected TextElement safeExamBrowserKeyEl;
	protected RichTextElement safeExamBrowserHintEl;
	protected MultipleSelectionElement safeExamBrowserEl;
	
	protected FormLayoutContainer sebConfigCont;
	
	private SafeExamBrowserEnabled configuration;
	
	@Autowired
	private AssessmentModule assessmentModule;
	
	public AbstractEditSafeExamBrowserController(UserRequest ureq, WindowControl wControl,
			SafeExamBrowserEnabled configuration) {
		super(ureq, wControl, LAYOUT_BAREBONE, Util
				.createPackageTranslator(AbstractEditSafeExamBrowserController.class, ureq.getLocale()));
		this.configuration = configuration;
	}
	
	protected abstract boolean isEditable();

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		SafeExamBrowserConfiguration sebConfig = configuration.getSafeExamBrowserConfiguration();
		if(sebConfig == null) {
			// create a default configuration
			sebConfig = assessmentModule.getSafeExamBrowserConfigurationDefaultConfiguration();
		}
		
		FormLayoutContainer enableCont = FormLayoutContainer.createDefaultFormLayout("enable.container", getTranslator());
		formLayout.add(enableCont);
		initSafeExamBrowserForm(enableCont);
		
		sebConfigCont = FormLayoutContainer.createDefaultFormLayout("seb.config", getTranslator());
		sebConfigCont.setFormTitle(translate("mode.safeexambrowser.section.title"));
		formLayout.add(sebConfigCont);
		initConfigurationForm(sebConfigCont, sebConfig);
		
		FormLayoutContainer keyConfigCont = FormLayoutContainer.createDefaultFormLayout("key.config", getTranslator());
		formLayout.add(keyConfigCont);
		initKeyForm(keyConfigCont, ureq);
	}
	
	protected void initSafeExamBrowserForm(FormItemContainer enableCont) {
		boolean editable = isEditable();

		safeExamBrowserEl = uifactory.addCheckboxesHorizontal("safeexam", "mode.safeexambrowser", enableCont, onKeys, onValues);
		safeExamBrowserEl.select(onKeys[0], configuration.isSafeExamBrowser());
		safeExamBrowserEl.addActionListener(FormEvent.ONCHANGE);
		safeExamBrowserEl.setEnabled(editable);
		
		SelectionValues typeOfUse = new SelectionValues();
		typeOfUse.add(SelectionValues.entry("keys", translate("mode.safeexambrowser.type.keys")));
		typeOfUse.add(SelectionValues.entry("inConfig", translate("mode.safeexambrowser.type.inOpenOlat")));
		typeOfUseEl = uifactory.addRadiosHorizontal("mode.safeexambrowser.typeofuse", enableCont, typeOfUse.keys(), typeOfUse.values());
		typeOfUseEl.setEnabled(editable);
		typeOfUseEl.addActionListener(FormEvent.ONCHANGE);
		
		if(StringHelper.containsNonWhitespace(configuration.getSafeExamBrowserKey())) {
			typeOfUseEl.select("keys", true);
		} else {
			typeOfUseEl.select("inConfig", true);
		}

		// configuration
		SelectionValues trueFalseValues = new SelectionValues();
		trueFalseValues.add(SelectionValues.entry("true", translate("yes")));
		trueFalseValues.add(SelectionValues.entry("false", translate("no")));
		
		downloadConfigEl = uifactory.addRadiosHorizontal("mode.safeexambrowser.download.config", enableCont,
				trueFalseValues.keys(), trueFalseValues.values());
		downloadConfigEl.select(trueFalseKey(configuration.isSafeExamBrowserConfigDownload()), true);
	}	

	protected void initConfigurationForm(FormItemContainer sebConfigCont, SafeExamBrowserConfiguration sebConfig ) {
		boolean editable = isEditable();
		
		SelectionValues trueFalseValues = new SelectionValues();
		trueFalseValues.add(SelectionValues.entry("true", translate("yes")));
		trueFalseValues.add(SelectionValues.entry("false", translate("no")));

		allowToExitEl = uifactory.addRadiosHorizontal("mode.safeexambrowser.allow.toexit", sebConfigCont,
				trueFalseValues.keys(), trueFalseValues.values());
		allowToExitEl.addActionListener(FormEvent.ONCHANGE);
		allowToExitEl.setEnabled(editable);
		allowToExitEl.select(trueFalseKey(sebConfig.isAllowQuit()), true);
		
		String password = sebConfig.getPasswordToExit();
		passwordToQuitEl = uifactory.addTextElement("password.quit", "mode.safeexambrowser.password.exit", 255, password, sebConfigCont);
		passwordToQuitEl.setEnabled(editable);
		
		linkToQuitEl = uifactory.addRadiosHorizontal("mode.safeexambrowser.link.to.quit", sebConfigCont,
				trueFalseValues.keys(), trueFalseValues.values());
		linkToQuitEl.addActionListener(FormEvent.ONCHANGE);
		linkToQuitEl.setEnabled(editable);
		String quitLink = sebConfig.getLinkToQuit();
		linkToQuitEl.select(trueFalseKey(StringHelper.containsNonWhitespace(quitLink)), true);	
		if (StringHelper.containsNonWhitespace(quitLink)) {
			linkToQuitEl.setExampleKey("noTransOnlyParam", new String[] { quitLink });
		}
		
		quitUrlConfirmEl = uifactory.addRadiosHorizontal("mode.safeexambrowser.confirm.exit", sebConfigCont,
				trueFalseValues.keys(), trueFalseValues.values());
		quitUrlConfirmEl.setEnabled(editable);
		quitUrlConfirmEl.select(trueFalseKey(sebConfig.isQuitURLConfirm()), true);
		
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
		
		allowWlanEl = uifactory.addRadiosHorizontal("mode.safeexambrowser.allow.wlan", sebConfigCont,
				trueFalseValues.keys(), trueFalseValues.values());
		allowWlanEl.setEnabled(editable);
		allowWlanEl.select(trueFalseKey(sebConfig.isAllowWlan()), true);
		
		showAudioOptionsEl = uifactory.addRadiosHorizontal("mode.safeexambrowser.show.audio", sebConfigCont,
				trueFalseValues.keys(), trueFalseValues.values());
		showAudioOptionsEl.addActionListener(FormEvent.ONCHANGE);
		showAudioOptionsEl.setEnabled(editable);
		showAudioOptionsEl.select(trueFalseKey(sebConfig.isAudioControlEnabled()), true);
		
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
		allowedExpressionsEl.setHelpText(translate("mode.safeexambrowser.url.filter.allow.exp.hint"));
		allowedRegexEl = uifactory.addTextAreaElement("mode.safeexambrowser.url.filter.allow.regex", "mode.safeexambrowser.url.filter.allow.regex",
				2000, 2, 60, false, false, sebConfig.getAllowedUrlRegex(), sebConfigCont);
		blockedExpressionsEl = uifactory.addTextAreaElement("mode.safeexambrowser.url.filter.blocked.exp", "mode.safeexambrowser.url.filter.blocked.exp",
				2000, 2, 60, false, false, sebConfig.getBlockedUrlExpressions(), sebConfigCont);
		blockedExpressionsEl.setHelpText(translate("mode.safeexambrowser.url.filter.blocked.exp.hint"));
		blockedRegexEl = uifactory.addTextAreaElement("mode.safeexambrowser.url.filter.blocked.regex", "mode.safeexambrowser.url.filter.blocked.regex",
				2000, 2, 60, false, false, sebConfig.getBlockedUrlRegex(), sebConfigCont);
	}

	protected void initKeyForm(FormItemContainer keyConfigCont, UserRequest ureq) {
		safeExamBrowserConfigKeyEl = uifactory.addStaticTextElement("mode.safeexambrowser.config.key", configuration.getSafeExamBrowserConfigPListKey(), keyConfigCont);
		safeExamBrowserConfigKeyEl.setExampleKey("mode.safeexambrowser.config.key.hint", null);
		
		String key = configuration.getSafeExamBrowserKey();
		safeExamBrowserKeyEl = uifactory.addTextAreaElement("safeexamkey", "mode.safeexambrowser.key", 16000, 6, 60, false, false, key, keyConfigCont);
		safeExamBrowserKeyEl.setMaxLength(16000);
		safeExamBrowserKeyEl.setVisible(configuration.isSafeExamBrowser());

		String hint = configuration.getSafeExamBrowserHint();
		safeExamBrowserHintEl = uifactory.addRichTextElementForStringData("safeexamhint", "mode.safeexambrowser.hint",
				hint, 10, -1, false, null, null, keyConfigCont, ureq.getUserSession(), getWindowControl());
		safeExamBrowserHintEl.setVisible(configuration.isSafeExamBrowser());
	}
	
	protected void updateUI() {
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
		if(linkToQuitEl.isKeySelected("true")) {
			if(!StringHelper.containsNonWhitespace(configuration.getLinkToQuit())) {
				String linkToQuit = Settings.getServerContextPathURI() + "/" + UUID.randomUUID().toString();
				configuration.setLinkToQuit(linkToQuit);
				linkToQuitEl.setExampleKey("noTransOnlyParam", new String[] { linkToQuit });
			}
		} else {
			configuration.setLinkToQuit(null);
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
		
		boolean urlFilter = urlFilterEl.isKeySelected("true");
		configuration.setUrlFilter(urlFilter);
		boolean urlContentFilter = urlFilter && urlContentFilterEl.isKeySelected("true");
		configuration.setUrlContentFilter(urlContentFilter);
		if(urlFilter) {
			configuration.setAllowedUrlExpressions(allowedExpressionsEl.getValue());
			configuration.setAllowedUrlRegex(allowedRegexEl.getValue());
			configuration.setBlockedUrlExpressions(blockedExpressionsEl.getValue());
			configuration.setBlockedUrlRegex(blockedRegexEl.getValue());
		} else {
			configuration.setAllowedUrlExpressions("");
			configuration.setAllowedUrlRegex("");
			configuration.setBlockedUrlExpressions("");
			configuration.setBlockedUrlRegex("");
		}
	
		return configuration;
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		safeExamBrowserKeyEl.clearError();
		if(safeExamBrowserEl.isAtLeastSelected(1) && this.typeOfUseEl.isKeySelected("keys")) {
			String value = safeExamBrowserKeyEl.getValue();
			if(!StringHelper.containsNonWhitespace(value)) {
				safeExamBrowserKeyEl.setErrorKey("form.legende.mandatory");
				allOk &= false;
			} else if(value.length() > safeExamBrowserKeyEl.getMaxLength()) {
				safeExamBrowserKeyEl.setErrorKey("form.error.toolong", Integer.toString(safeExamBrowserKeyEl.getMaxLength()));
				allOk &= false;
			}
		}
		
		return allOk;
	}

	protected void commit(SafeExamBrowserEnabled configuration) {
		this.configuration = configuration;
		
		boolean safeExamEnabled = safeExamBrowserEl.isAtLeastSelected(1);
		configuration.setSafeExamBrowser(safeExamEnabled);
		if(safeExamEnabled) {
			if(typeOfUseEl.isSelected(0)) {
				configuration.setSafeExamBrowserKey(safeExamBrowserKeyEl.getValue());
				configuration.setSafeExamBrowserConfiguration(null);
			} else {
				configuration.setSafeExamBrowserKey(null);
				configuration.setSafeExamBrowserConfiguration(getConfiguration());
				configuration.setSafeExamBrowserConfigDownload(downloadConfigEl.isOneSelected() && downloadConfigEl.isKeySelected("true"));
			}
			configuration.setSafeExamBrowserHint(safeExamBrowserHintEl.getValue());
		} else {
			configuration.setSafeExamBrowserKey(null);
			configuration.setSafeExamBrowserHint(null);
			configuration.setSafeExamBrowserConfiguration(null);
		}
	
		// Always update config key
		safeExamBrowserConfigKeyEl.setValue(configuration.getSafeExamBrowserConfigPListKey());
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