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
package org.olat.course.assessment.model;

/**
 * 
 * Initial date: 25 janv. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SafeExamBrowserConfiguration {
	
	private String startUrl;
	
	private boolean allowQuit= true;
	private boolean quitURLConfirm;
	private String passwordToExit;
	private boolean browserWindowAllowReload;
	private boolean showReloadButton;
	private boolean showTaskBar;
	private boolean showTimeClock;
	private boolean showKeyboardLayout;
	private boolean audioControlEnabled;
	private boolean audioMute;
	private boolean allowSpellCheck;
	private boolean allowZoomInOut;
	private boolean allowTextSearch;
	
	private boolean urlFilter;
	private boolean urlContentFilter;
	private String allowedUrlExpressions;
	private String allowedUrlRegex;
	private String blockedUrlExpressions;
	private String blockedUrlRegex;

	public String getStartUrl() {
		return startUrl;
	}

	public void setStartUrl(String startUrl) {
		this.startUrl = startUrl;
	}
	
	public boolean isAllowQuit() {
		return allowQuit;
	}

	public void setAllowQuit(boolean allowQuit) {
		this.allowQuit = allowQuit;
	}
	
	public boolean isQuitURLConfirm() {
		return quitURLConfirm;
	}

	public void setQuitURLConfirm(boolean quitURLConfirm) {
		this.quitURLConfirm = quitURLConfirm;
	}

	public String getPasswordToExit() {
		return passwordToExit;
	}
	
	public void setPasswordToExit(String passwordToExit) {
		this.passwordToExit = passwordToExit;
	}
	
	public boolean isBrowserWindowAllowReload() {
		return browserWindowAllowReload;
	}

	public void setBrowserWindowAllowReload(boolean browserWindowAllowReload) {
		this.browserWindowAllowReload = browserWindowAllowReload;
	}

	public boolean isShowReloadButton() {
		return showReloadButton;
	}

	public void setShowReloadButton(boolean showReloadButton) {
		this.showReloadButton = showReloadButton;
	}

	public boolean isShowTaskBar() {
		return showTaskBar;
	}

	public void setShowTaskBar(boolean showTaskBar) {
		this.showTaskBar = showTaskBar;
	}

	public boolean isShowTimeClock() {
		return showTimeClock;
	}
	
	public void setShowTimeClock(boolean showTimeClock) {
		this.showTimeClock = showTimeClock;
	}
	
	public boolean isShowKeyboardLayout() {
		return showKeyboardLayout;
	}
	
	public void setShowKeyboardLayout(boolean showKeyboardLayout) {
		this.showKeyboardLayout = showKeyboardLayout;
	}
	
	public boolean isAudioControlEnabled() {
		return audioControlEnabled;
	}

	public void setAudioControlEnabled(boolean audioControlEnabled) {
		this.audioControlEnabled = audioControlEnabled;
	}

	public boolean isAudioMute() {
		return audioMute;
	}

	public void setAudioMute(boolean audioMute) {
		this.audioMute = audioMute;
	}

	public boolean isAllowSpellCheck() {
		return allowSpellCheck;
	}
	
	public void setAllowSpellCheck(boolean allowSpellCheck) {
		this.allowSpellCheck = allowSpellCheck;
	}
	
	public boolean isAllowZoomInOut() {
		return allowZoomInOut;
	}
	
	public void setAllowZoomInOut(boolean allowZoomInOut) {
		this.allowZoomInOut = allowZoomInOut;
	}
	
	public boolean isAllowTextSearch() {
		return allowTextSearch;
	}
	
	public void setAllowTextSearch(boolean allowTextSearch) {
		this.allowTextSearch = allowTextSearch;
	}

	public boolean isUrlFilter() {
		return urlFilter;
	}

	public void setUrlFilter(boolean urlFilter) {
		this.urlFilter = urlFilter;
	}

	public boolean isUrlContentFilter() {
		return urlContentFilter;
	}

	public void setUrlContentFilter(boolean urlContentFilter) {
		this.urlContentFilter = urlContentFilter;
	}

	public String getAllowedUrlExpressions() {
		return allowedUrlExpressions;
	}

	public void setAllowedUrlExpressions(String allowedUrlExpressions) {
		this.allowedUrlExpressions = allowedUrlExpressions;
	}

	public String getAllowedUrlRegex() {
		return allowedUrlRegex;
	}

	public void setAllowedUrlRegex(String allowedUrlRegex) {
		this.allowedUrlRegex = allowedUrlRegex;
	}

	public String getBlockedUrlExpressions() {
		return blockedUrlExpressions;
	}

	public void setBlockedUrlExpressions(String blockedUrlExpressions) {
		this.blockedUrlExpressions = blockedUrlExpressions;
	}

	public String getBlockedUrlRegex() {
		return blockedUrlRegex;
	}

	public void setBlockedUrlRegex(String blockedUrlRegex) {
		this.blockedUrlRegex = blockedUrlRegex;
	}
}
