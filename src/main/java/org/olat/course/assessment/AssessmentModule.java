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

package org.olat.course.assessment;

import java.util.UUID;

import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.helpers.Settings;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.course.assessment.model.SafeExamBrowserConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Description:<br>
 * This is a PublishEvent listener, and triggers the update of the EfficiencyStatements 
 * for the published course. It only considers the events from the same JVM.
 * 
 * <P>
 * Initial Date: 11.08.2006 <br>
 * 
 * @author patrickb
 */
@Service("assessmentModule")
public class AssessmentModule extends AbstractSpringModule {
	
	private static final String SEB_SHOWTASKBAR = "safe.exam.browser.show.task.bar";
	private static final String SEB_SHOWRELOADBUTTON = "safe.exam.browser.show.reload.button";
	private static final String SEB_SHOWTIME = "safe.exam.browser.show.time";
	private static final String SEB_SHOWINPUTLANGUAGE = "safe.exam.browser.show.input.language";
	private static final String SEB_ALLOWQUIT = "safe.exam.browser.allow.quit";
	private static final String SEB_QUITURL = "safe.exam.browser.quit.url";
	private static final String SEB_QUITURLCONFIRM = "safe.exam.browser.quit.url.confirm";
	private static final String SEB_AUDIOCONTROLENABLED = "safe.exam.browser.audio.control.enabled";
	private static final String SEB_AUDIOMUTE = "safe.exam.browser.audio.mute";
	private static final String SEB_ALLOWSPELLCHECK = "safe.exam.browser.allow.spell.check";
	private static final String SEB_BROWSERWINDOWALLOWRELOAD = "safe.exam.browser.browser.window.allow.reload";
	private static final String SEB_ALLOWZOOMINOUT = "safe.exam.browser.allow.zoom.in.out";
	private static final String SEB_URLFILTER = "safe.exam.browser.url.filter";
	private static final String SEB_URLCONTENTFILTER = "safe.exam.browser.url.content.filter";
	private static final String SEB_ALLOWEDURLEXPRESSIONS = "safe.exam.browser.allowed.url.expressions";
	private static final String SEB_ALLOWEDURLREGEX = "safe.exam.browser.allowed.url.regex";
	private static final String SEB_BLOCKEDURLEXPRESSIONS = "safe.exam.browser.blocked.url.expressions";
	private static final String SEB_BLOCKEDURLREGEX = "safe.exam.browser.blocked.url.regex";
	private static final String SEB_HINT = "safe.exam.browser.hint";
	private static final String SEB_ALLOWWLAN = "safe.exam.browser.allow.wlan";
	private static final String SEB_ALLOWAUDIOCAPTURE = "safe.exam.browser.allow.audio.capture";
	private static final String SEB_ALLOWVIDEOCAPTURE = "safe.exam.browser.allow.video.capture";
	private static final String SEB_VIEWMODE = "safe.exam.browser.view.mode";
	
	private static final String MANAGED_ASSESSMENT_MODES_ENABLED = "managedAssessmentModes";
	
	@Value("${assessment.mode:enabled}")
	private String assessmentModeEnabled;
	@Value("${assessment.mode.managed}")
	private boolean managedAssessmentModes;
	
	@Value("${safe.exam.browser.view.mode:0}")
	private int safeExamBrowserViewMode;
	@Value("${safe.exam.browser.show.task.bar:true}")
	private String safeExamBrowserShowTaskBar;
	@Value("${safe.exam.browser.show.reload.button:true}")
	private String safeExamBrowserShowReloadButton;
	@Value("${safe.exam.browser.show.time:true}")
	private String safeExamBrowserShowTime;
	@Value("${safe.exam.browser.show.input.language:true}")
	private String safeExamBrowserShowInputLanguage;
	@Value("${safe.exam.browser.allow.quit:true}")
	private String safeExamBrowserAllowQuit;
	@Value("${safe.exam.browser.quit.url:true}")
	private String safeExamBrowserQuitUrl;
	@Value("${safe.exam.browser.quit.url.confirm:true}")
	private String safeExamBrowserQuitURLConfirm;
	@Value("${safe.exam.browser.audio.control.enabled:true}")
	private String safeExamBrowserAudioControlEnabled;
	@Value("${safe.exam.browser.audio.mute:true}")
	private String safeExamBrowserAudioMute;
	@Value("${safe.exam.browser.allow.spell.check:true}")
	private String safeExamBrowserAllowSpellCheck;
	@Value("${safe.exam.browser.browser.window.allow.reload:true}")
	private String safeExamBrowserBrowserWindowAllowReload;
	@Value("${safe.exam.browser.allow.zoom.in.out:true}")
	private String safeExamBrowserAllowZoomInOut;
	@Value("${safe.exam.browser.allow.wlan:true}")
	private String safeExamBrowserAllowWlan;
	@Value("${safe.exam.browser.allow.audio.capture:true}")
	private String safeExamBrowserAllowAudioCapture;
	@Value("${safe.exam.browser.allow.video.capture:true}")
	private String safeExamBrowserAllowVideoCapture;

	@Value("${safe.exam.browser.url.filter:true}")
	private String safeExamBrowserUrlFilter;
	@Value("${safe.exam.browser.url.content.filter:true}")
	private String safeExamBrowserUrlContentFilter;

	@Value("${safe.exam.browser.allowed.url.expressions}")
	private String safeExamBrowserAllowedUrlExpressions;
	@Value("${safe.exam.browser.allowed.url.regex}")
	private String safeExamBrowserAllowedUrlRegex;
	@Value("${safe.exam.browser.blocked.url.expressions}")
	private String safeExamBrowserBlockedUrlExpressions;
	@Value("${safe.exam.browser.blocked.url.regex}")
	private String safeExamBrowserBlockedUrlRegex;

	@Value("${safe.exam.browser.hint}")
	private String safeExamBrowserHint;	
	
	@Value("${safe.exam.browser.download.url}")
	private String safeExamBrowserDownloadUrl;	

	@Autowired
	public AssessmentModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}

	@Override
	protected void initFromChangedProperties() {
		updateProperties();
	}

	@Override
	public void init() {
		updateProperties();
	}
	
	private void updateProperties() {
		String enabledObj = getStringPropertyValue("assessment.mode", true);
		if(StringHelper.containsNonWhitespace(enabledObj)) {
			assessmentModeEnabled = enabledObj;
		}
		
		String managedModes = getStringPropertyValue(MANAGED_ASSESSMENT_MODES_ENABLED, true);
		if(StringHelper.containsNonWhitespace(managedModes)) {
			managedAssessmentModes = "true".equals(managedModes);
		}
		
		String safeExamBrowserViewModeObj = getStringPropertyValue(SEB_VIEWMODE, Integer.toString(safeExamBrowserViewMode));
		safeExamBrowserViewMode = Integer.parseInt(safeExamBrowserViewModeObj);
		
		safeExamBrowserShowTaskBar = getStringPropertyValue(SEB_SHOWTASKBAR, safeExamBrowserShowTaskBar);
		safeExamBrowserShowReloadButton = getStringPropertyValue(SEB_SHOWRELOADBUTTON, safeExamBrowserShowReloadButton);
		safeExamBrowserShowTime = getStringPropertyValue(SEB_SHOWTIME, safeExamBrowserShowTime);
		
		safeExamBrowserShowInputLanguage = getStringPropertyValue(SEB_SHOWINPUTLANGUAGE, safeExamBrowserShowInputLanguage);
		safeExamBrowserAllowQuit = getStringPropertyValue(SEB_ALLOWQUIT, safeExamBrowserAllowQuit);
		safeExamBrowserQuitUrl = getStringPropertyValue(SEB_QUITURL, safeExamBrowserQuitUrl);
		safeExamBrowserQuitURLConfirm = getStringPropertyValue(SEB_QUITURLCONFIRM, safeExamBrowserQuitURLConfirm);
		safeExamBrowserAudioControlEnabled = getStringPropertyValue(SEB_AUDIOCONTROLENABLED, safeExamBrowserAudioControlEnabled);
		safeExamBrowserAudioMute = getStringPropertyValue(SEB_AUDIOMUTE, safeExamBrowserAudioMute);
		safeExamBrowserAllowSpellCheck = getStringPropertyValue(SEB_ALLOWSPELLCHECK, safeExamBrowserAllowSpellCheck);
		safeExamBrowserAllowWlan = getStringPropertyValue(SEB_ALLOWWLAN, safeExamBrowserAllowWlan);
		safeExamBrowserAllowAudioCapture = getStringPropertyValue(SEB_ALLOWAUDIOCAPTURE, safeExamBrowserAllowAudioCapture);
		safeExamBrowserAllowVideoCapture = getStringPropertyValue(SEB_ALLOWVIDEOCAPTURE, safeExamBrowserAllowVideoCapture);

		safeExamBrowserBrowserWindowAllowReload = getStringPropertyValue(SEB_BROWSERWINDOWALLOWRELOAD, safeExamBrowserBrowserWindowAllowReload);
		
		safeExamBrowserAllowZoomInOut = getStringPropertyValue(SEB_ALLOWZOOMINOUT, safeExamBrowserAllowZoomInOut);

		safeExamBrowserUrlFilter = getStringPropertyValue(SEB_URLFILTER, safeExamBrowserUrlFilter);
		safeExamBrowserUrlContentFilter = getStringPropertyValue(SEB_URLCONTENTFILTER, safeExamBrowserUrlContentFilter);
		safeExamBrowserAllowedUrlExpressions = getStringPropertyValue(SEB_ALLOWEDURLEXPRESSIONS, safeExamBrowserAllowedUrlExpressions);
		safeExamBrowserAllowedUrlRegex = getStringPropertyValue(SEB_ALLOWEDURLREGEX, safeExamBrowserAllowedUrlRegex);
		safeExamBrowserBlockedUrlExpressions = getStringPropertyValue(SEB_BLOCKEDURLEXPRESSIONS, safeExamBrowserBlockedUrlExpressions);
		safeExamBrowserBlockedUrlRegex = getStringPropertyValue(SEB_BLOCKEDURLREGEX, safeExamBrowserBlockedUrlRegex);
		
		safeExamBrowserHint = getStringPropertyValue(SEB_HINT, safeExamBrowserHint);
	}

	public boolean isAssessmentModeEnabled() {
		return "enabled".equals(assessmentModeEnabled);
	}

	public void setAssessmentModeEnabled(boolean enabled) {
		assessmentModeEnabled = enabled ? "enabled" : "disabled";
		setStringProperty("assessment.mode", assessmentModeEnabled, true);
	}
	
	public boolean isManagedAssessmentModes() {
		return managedAssessmentModes;
	}

	public void setManagedAssessmentModes(boolean enabled) {
		managedAssessmentModes = enabled;
		setStringProperty(MANAGED_ASSESSMENT_MODES_ENABLED, Boolean.toString(enabled), true);
	}
	
	public String getSafeExamBrowserDownloadUrl() {
		return safeExamBrowserDownloadUrl;
	}

	public SafeExamBrowserConfiguration getSafeExamBrowserConfigurationDefaultConfiguration() {
		SafeExamBrowserConfiguration config = new SafeExamBrowserConfiguration();
		config.setStartUrl(Settings.getServerContextPathURI());
		config.setBrowserViewMode(getSafeExamBrowserViewMode());
		config.setShowTaskBar(isSafeExamBrowserShowTaskBar());
		config.setBrowserWindowAllowReload(isSafeExamBrowserBrowserWindowAllowReload());
		config.setShowReloadButton(isSafeExamBrowserShowReloadButton());
		config.setShowTimeClock(isSafeExamBrowserShowTime());
		config.setShowKeyboardLayout(isSafeExamBrowserShowInputLanguage());
		config.setAllowWlan(isSafeExamBrowserAllowWlan());
		config.setAllowQuit(isSafeExamBrowserAllowQuit());
		if(isSafeExamBrowserQuitURL()) {
			String linkToQuit = Settings.getServerContextPathURI() + "/" + UUID.randomUUID().toString();
			config.setLinkToQuit(linkToQuit);
		}
		config.setQuitURLConfirm(isSafeExamBrowserQuitURLConfirm());
		
		config.setAudioControlEnabled(isSafeExamBrowserAudioControlEnabled());
		config.setAudioMute(isSafeExamBrowserAudioMute());
		
		config.setAllowAudioCapture(isSafeExamBrowserAllowAudioCapture());
		config.setAllowVideoCapture(isSafeExamBrowserAllowVideoCapture());
		config.setAllowSpellCheck(isSafeExamBrowserAllowSpellCheck());
		config.setAllowZoomInOut(isSafeExamBrowserAllowZoomInOut());

		config.setUrlFilter(isSafeExamBrowserUrlFilter());
		config.setUrlContentFilter(isSafeExamBrowserUrlContentFilter());
		config.setAllowedUrlExpressions(getSafeExamBrowserAllowedUrlExpressions());
		config.setAllowedUrlRegex(getSafeExamBrowserAllowedUrlRegex());
		config.setBlockedUrlExpressions(getSafeExamBrowserBlockedUrlExpressions());
		config.setBlockedUrlRegex(getSafeExamBrowserBlockedUrlRegex());

		safeExamBrowserShowReloadButton = getStringPropertyValue(SEB_SHOWRELOADBUTTON, safeExamBrowserShowReloadButton);
		
		return config;
	}

	public boolean isSafeExamBrowserShowTaskBar() {
		return "true".equals(safeExamBrowserShowTaskBar);
	}

	public void setSafeExamBrowserShowTaskBar(boolean showTaskBar) {
		safeExamBrowserShowTaskBar = showTaskBar ? "true" : "false";
		setStringProperty(SEB_SHOWTASKBAR, safeExamBrowserShowTaskBar, true);
	}

	public boolean isSafeExamBrowserShowReloadButton() {
		return "true".equals(safeExamBrowserShowReloadButton);
	}

	public void setSafeExamBrowserShowReloadButton(boolean showReloadButton) {
		safeExamBrowserShowReloadButton = showReloadButton ? "true" : "false";
		setStringProperty(SEB_SHOWRELOADBUTTON, safeExamBrowserShowReloadButton, true);
	}

	public boolean isSafeExamBrowserShowTime() {
		return "true".equals(safeExamBrowserShowTime);
	}

	public void setSafeExamBrowserShowTime(boolean showTime) {
		safeExamBrowserShowTime = showTime ? "true" : "false";
		setStringProperty(SEB_SHOWTIME, safeExamBrowserShowTime, true);
	}
	
	public boolean isSafeExamBrowserShowInputLanguage() {
		return "true".equals(safeExamBrowserShowInputLanguage);
	}

	public void setSafeExamBrowserShowInputLanguage(boolean showInputLanguage) {
		safeExamBrowserShowInputLanguage = showInputLanguage ? "true" : "false";
		setStringProperty(SEB_SHOWINPUTLANGUAGE, safeExamBrowserShowInputLanguage, true);
	}

	public boolean isSafeExamBrowserAllowQuit() {
		return "true".equals(safeExamBrowserAllowQuit);
	}

	public void setSafeExamBrowserAllowQuit(boolean allowQuit) {
		safeExamBrowserAllowQuit = allowQuit ? "true" : "false";
		setStringProperty(SEB_ALLOWQUIT, safeExamBrowserAllowQuit, true);
	}
	
	public boolean isSafeExamBrowserQuitURL() {
		return "true".equals(safeExamBrowserQuitUrl);
	}

	public void setSafeExamBrowserQuitURL(boolean quitUrl) {
		safeExamBrowserQuitUrl = quitUrl ? "true" : "false";
		setStringProperty(SEB_QUITURL, safeExamBrowserQuitUrl, true);
	}

	public boolean isSafeExamBrowserQuitURLConfirm() {
		return "true".equals(safeExamBrowserQuitURLConfirm);
	}

	public void setSafeExamBrowserQuitURLConfirm(boolean quitURLConfirm) {
		safeExamBrowserQuitURLConfirm = quitURLConfirm ? "true" : "false";
		setStringProperty(SEB_QUITURLCONFIRM, safeExamBrowserQuitURLConfirm, true);
	}

	public boolean isSafeExamBrowserAudioControlEnabled() {
		return "true".equals(safeExamBrowserAudioControlEnabled);
	}

	public void setSafeExamBrowserAudioControlEnabled(boolean audioControlEnabled) {
		safeExamBrowserAudioControlEnabled = audioControlEnabled ? "true" : "false";
		setStringProperty(SEB_AUDIOCONTROLENABLED, safeExamBrowserAudioControlEnabled, true);
	}

	public boolean isSafeExamBrowserAudioMute() {
		return "true".equals(safeExamBrowserAudioMute);
	}

	public void setSafeExamBrowserAudioMute(boolean audioMute) {
		safeExamBrowserAudioMute = audioMute ? "true" : "false";
		setStringProperty(SEB_AUDIOMUTE, safeExamBrowserAudioMute, true);
	}
	
	public int getSafeExamBrowserViewMode() {
		return safeExamBrowserViewMode;
	}

	public void setSafeExamBrowserViewMode(int viewMode) {
		this.safeExamBrowserViewMode = viewMode;
		setStringProperty(SEB_VIEWMODE, Integer.toString(viewMode), true);
	}

	public boolean isSafeExamBrowserAllowWlan() {
		return "true".equals(safeExamBrowserAllowWlan);
	}

	public void setSafeExamBrowserAllowWlan(boolean allowWlan) {
		safeExamBrowserAllowWlan = allowWlan ? "true" : "false";
		setStringProperty(SEB_ALLOWWLAN, safeExamBrowserAllowWlan, true);
	}

	public boolean isSafeExamBrowserAllowAudioCapture() {
		return "true".equals(safeExamBrowserAllowAudioCapture);
	}

	public void setSafeExamBrowserAllowAudioCapture(boolean allowAudioCapture) {
		safeExamBrowserAllowAudioCapture = allowAudioCapture ? "true" : "false";
		setStringProperty(SEB_ALLOWAUDIOCAPTURE, safeExamBrowserAllowAudioCapture, true);
	}

	public boolean isSafeExamBrowserAllowVideoCapture() {
		return "true".equals(safeExamBrowserAllowVideoCapture);
	}

	public void setSafeExamBrowserAllowVideoCapture(boolean allowVideoCapture) {
		safeExamBrowserAllowVideoCapture = allowVideoCapture ? "true" : "false";
		setStringProperty(SEB_ALLOWVIDEOCAPTURE, safeExamBrowserAllowVideoCapture, true);
	}

	public boolean isSafeExamBrowserAllowSpellCheck() {
		return "true".equals(safeExamBrowserAllowSpellCheck);
	}

	public void setSafeExamBrowserAllowSpellCheck(boolean allowSpellCheck) {
		safeExamBrowserAllowSpellCheck = allowSpellCheck ? "true" : "false";
		setStringProperty(SEB_ALLOWSPELLCHECK, safeExamBrowserAllowSpellCheck, true);
	}

	public boolean isSafeExamBrowserBrowserWindowAllowReload() {
		return "true".equals(safeExamBrowserBrowserWindowAllowReload);
	}

	public void setSafeExamBrowserBrowserWindowAllowReload(boolean browserWindowAllowReload) {
		safeExamBrowserBrowserWindowAllowReload = browserWindowAllowReload ? "true" : "false";
		setStringProperty(SEB_BROWSERWINDOWALLOWRELOAD, safeExamBrowserBrowserWindowAllowReload, true);
	}

	public boolean isSafeExamBrowserAllowZoomInOut() {
		return "true".equals(safeExamBrowserAllowZoomInOut);
	}

	public void setSafeExamBrowserAllowZoomInOut(boolean allowZoomInOut) {
		this.safeExamBrowserAllowZoomInOut = allowZoomInOut ? "true" : "false";
		setStringProperty(SEB_ALLOWZOOMINOUT, safeExamBrowserAllowZoomInOut, true);
	}

	public boolean isSafeExamBrowserUrlFilter() {
		return "true".equals(safeExamBrowserUrlFilter);
	}

	public void setSafeExamBrowserUrlFilter(boolean urlFilter) {
		safeExamBrowserUrlFilter = urlFilter ? "true" : "false";
		setStringProperty(SEB_URLFILTER, safeExamBrowserUrlFilter, true);
	}

	public boolean isSafeExamBrowserUrlContentFilter() {
		return "true".equals(safeExamBrowserUrlContentFilter);
	}

	public void setSafeExamBrowserUrlContentFilter(boolean urlContentFilter) {
		safeExamBrowserUrlContentFilter = urlContentFilter ? "true" : "false";
		setStringProperty(SEB_URLCONTENTFILTER, safeExamBrowserUrlContentFilter, true);
	}

	public String getSafeExamBrowserAllowedUrlExpressions() {
		return safeExamBrowserAllowedUrlExpressions;
	}

	public void setSafeExamBrowserAllowedUrlExpressions(String allowedUrlExpressions) {
		safeExamBrowserAllowedUrlExpressions = allowedUrlExpressions;
		setStringProperty(SEB_ALLOWEDURLEXPRESSIONS, safeExamBrowserAllowedUrlExpressions, true);
	}

	public String getSafeExamBrowserAllowedUrlRegex() {
		return safeExamBrowserAllowedUrlRegex;
	}

	public void setSafeExamBrowserAllowedUrlRegex(String allowedUrlRegex) {
		safeExamBrowserAllowedUrlRegex = allowedUrlRegex;
		setStringProperty(SEB_ALLOWEDURLREGEX, safeExamBrowserAllowedUrlRegex, true);
	}

	public String getSafeExamBrowserBlockedUrlExpressions() {
		return safeExamBrowserBlockedUrlExpressions;
	}

	public void setSafeExamBrowserBlockedUrlExpressions(String blockedUrlExpressions) {
		safeExamBrowserBlockedUrlExpressions = blockedUrlExpressions;
		setStringProperty(SEB_BLOCKEDURLEXPRESSIONS, safeExamBrowserBlockedUrlExpressions, true);
	}

	public String getSafeExamBrowserBlockedUrlRegex() {
		return safeExamBrowserBlockedUrlRegex;
	}

	public void setSafeExamBrowserBlockedUrlRegex(String blockedUrlRegex) {
		safeExamBrowserBlockedUrlRegex = blockedUrlRegex;
		setStringProperty(SEB_BLOCKEDURLREGEX, safeExamBrowserBlockedUrlRegex, true);
	}

	public String getSafeExamBrowserHint() {
		return safeExamBrowserHint;
	}

	public void setSafeExamBrowserHint(String hint) {
		safeExamBrowserHint = hint;
		setStringProperty(SEB_HINT, safeExamBrowserHint, true);
	}
}
