/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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

import java.util.Locale;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.media.ServletUtil;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.course.assessment.AssessmentModule;
import org.olat.course.assessment.manager.SafeExamBrowserValidator;
import org.olat.course.assessment.model.SafeExamBrowserCheck;
import org.olat.course.assessment.model.SafeExamBrowserProbe;
import org.olat.course.assessment.model.SafeExamBrowserProbe.Source;
import org.olat.course.assessment.model.SafeExamBrowserStatus;

/**
 * Shared code of the assessment mode and assessment inspection guards
 * for the Safe Exam Browser check chain and its dialogs.
 * 
 * Initial date: 16 nov. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 */
public class SafeExamBrowserGuardHelper {
	
	private static final Logger log = Tracing.createLoggerFor(SafeExamBrowserGuardHelper.class);
	
	public static final String CMD_CHECK_SEB_KEYS = "checkSEBKeys";
	public static final String CMD_CHECK_SEB_HEADERS = "checkSEBHeaders";
	
	private SafeExamBrowserGuardHelper() {
		//
	}
	
	/**
	 * @param command The command sent by the browser check
	 * @return The source of the answer or null if the command is not a browser check
	 */
	public static Source toSource(String command) {
		if(CMD_CHECK_SEB_KEYS.equals(command)) {
			return Source.JS_API;
		}
		if(CMD_CHECK_SEB_HEADERS.equals(command)) {
			return Source.HEADERS;
		}
		return null;
	}
	
	/**
	 * Collect the answer of the browser.
	 * 
	 * @param ureq The user request
	 * @param source The source of the answer
	 * @return The probe
	 */
	public static SafeExamBrowserProbe probe(UserRequest ureq, Source source) {
		if(source == null || source == Source.NONE) {
			return SafeExamBrowserProbe.NONE;
		}
		
		String configKey = ureq.getParameter("configKey");
		String url = ureq.getParameter("urlForKeyHash");
		String browserExamKey = ureq.getParameter("browserExamKey");
		String browserVersion = ureq.getParameter("browserVersion");
		log.info("SEB requests parameters - source: {}, configkey: {}, url: {}, browser exam key: {}, browser version: {} (User-Agent: {})",
				source, configKey, url, browserExamKey, browserVersion, ServletUtil.getUserAgent(ureq.getHttpReq()));
		return new SafeExamBrowserProbe(source, configKey, browserExamKey, url, browserVersion, ureq.getHttpReq());
	}
	
	public static SafeExamBrowserCheck check(SafeExamBrowserCheck previous, SafeExamBrowserProbe probe,
			String safeExamBrowserKeys, String configurationKey, AssessmentModule assessmentModule) {
		return SafeExamBrowserValidator.check(previous, probe, safeExamBrowserKeys, configurationKey,
				assessmentModule.isSafeExamBrowserEnforceMinimalVersion(), assessmentModule::getSafeExamBrowserMinimalVersion);
	}
	
	/**
	 * Build the dialog matching the result of the check chain.
	 * 
	 * @param check The result of the check chain
	 * @param configDownloadAllowed true if the configuration file of the resource can be downloaded
	 * @param assessmentModule The module (download URL, minimal versions)
	 * @param translator The translator with the keys seb.* (own values for assessment modes and inspections)
	 * @return The dialog, or null if no dialog is needed
	 */
	public static SafeExamBrowserDialog dialog(SafeExamBrowserCheck check, boolean configDownloadAllowed,
			AssessmentModule assessmentModule, Translator translator) {
		if(check == null || check.isPassed()) {
			return null;
		}
		
		String cssStatus = check.status().name().toLowerCase(Locale.ENGLISH);
		return switch(check.status()) {
			case PENDING -> new SafeExamBrowserDialog(cssStatus, null,
					translator.translate("seb.check.pending"), false, false);
			case MISSING -> new SafeExamBrowserDialog(cssStatus, translator.translate("seb.missing.title"),
					translator.translate(configDownloadAllowed ? "seb.missing.desc.config" : "seb.missing.desc"),
					true, configDownloadAllowed);
			case OUTDATED -> new SafeExamBrowserDialog(cssStatus, translator.translate("seb.version.title"),
					translator.translate("seb.version.desc", StringHelper.escapeHtml(getMinimalVersion(check, assessmentModule))),
					false, false);
			case WRONG_CONFIG -> new SafeExamBrowserDialog(cssStatus, translator.translate("seb.config.title"),
					translator.translate("seb.config.desc.supervision"),
					false, false);
			default -> null;
		};
	}
	
	/**
	 * The minimal version for the operating system of the browser. If the operating system
	 * is not known (Safe Exam Browser without JavaScript API), the highest configured
	 * minimal version, the only one which is sufficient for all operating systems.
	 */
	private static String getMinimalVersion(SafeExamBrowserCheck check, AssessmentModule assessmentModule) {
		if(StringHelper.containsNonWhitespace(check.minimalVersion())) {
			return check.minimalVersion();
		}

		String highest = null;
		for(String version:new String[] {
				assessmentModule.getSafeExamBrowserMinimalVersionWin(),
				assessmentModule.getSafeExamBrowserMinimalVersionMac(),
				assessmentModule.getSafeExamBrowserMinimalVersionIos() }) {
			if(StringHelper.containsNonWhitespace(version)
					&& (highest == null || !SafeExamBrowserValidator.isBrowserVersionAllowed(highest, version))) {
				highest = version;
			}
		}
		return highest == null ? "-" : highest;
	}
	
	/**
	 * The data of the dialog for the templates.
	 * 
	 * @param status The status in lower case (pending, missing, outdated, wrong_config)
	 * @param title The title (already translated)
	 * @param description The description (already translated)
	 * @param sebDownload true if the download button of the Safe Exam Browser is visible
	 * @param configDownload true if the download button of the configuration file is visible
	 */
	public record SafeExamBrowserDialog(String status, String title, String description,
			boolean sebDownload, boolean configDownload) {
		
		public boolean isPending() {
			return SafeExamBrowserStatus.PENDING.name().equalsIgnoreCase(status);
		}
		
		public boolean isButtons() {
			return sebDownload || configDownload;
		}
	}
}
