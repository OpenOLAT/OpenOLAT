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
package org.olat.course.assessment.manager;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Encoder;
import org.olat.core.util.StringHelper;
import org.olat.core.util.xml.PList;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.course.assessment.AssessmentModule;
import org.olat.course.assessment.model.SafeExamBrowserConfiguration;
import org.w3c.dom.Element;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.ExplicitTypePermission;

/**
 * Serialize the 
 * Initial date: 25 janv. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SafeExamBrowserConfigurationSerializer {
	
	private static final String BACKSLASH_SUBSTITUTE = "\u063C\u308C\u4BC2";
	
	private static final Logger log = Tracing.createLoggerFor(SafeExamBrowserConfigurationSerializer.class);
	private static final XStream xstream = XStreamHelper.createXStreamInstance();
	static {
		Class<?>[] types = new Class[] {
				SafeExamBrowserConfiguration.class
			};
		xstream.addPermission(new ExplicitTypePermission(types));
	}
	
	private SafeExamBrowserConfigurationSerializer() {
		//
	}
	
	public static String toPList(SafeExamBrowserConfiguration configuration, AssessmentModule assessmentModule) {
		try {
			PList plist = new PList();
			plist.add("showTaskBar", configuration.isShowTaskBar());
			plist.add("showReloadButton", configuration.isShowReloadButton());
			plist.add("showTime", configuration.isShowTimeClock());
			plist.add("showInputLanguage", configuration.isShowKeyboardLayout());
			plist.add("allowQuit", configuration.isAllowQuit());
			if(StringHelper.containsNonWhitespace(configuration.getLinkToQuit())) {
				plist.add("quitURL", configuration.getLinkToQuit());
			} else {
				plist.add("quitURL", assessmentModule.getShadowQuitURL());
			}
			plist.add("quitURLConfirm", configuration.isQuitURLConfirm());
			plist.add("audioControlEnabled", configuration.isAudioControlEnabled());
			plist.add("audioMute", configuration.isAudioMute());
			plist.add("allowAudioCapture", configuration.isAllowAudioCapture());
			plist.add("allowVideoCapture", configuration.isAllowVideoCapture());
			plist.add("allowWlan", configuration.isAllowWlan());
			plist.add("allowSpellCheck", configuration.isAllowSpellCheck());
			plist.add("enableZoomPage", configuration.isAllowZoomInOut());
			plist.add("enableZoomText", configuration.isAllowZoomInOut());
			plist.add("browserWindowAllowReload", configuration.isBrowserWindowAllowReload());
			if(StringHelper.containsNonWhitespace(configuration.getPasswordToExit())) {
				plist.add("hashedQuitPassword", Encoder.sha256Exam(configuration.getPasswordToExit()));
			}
			plist.add("browserViewMode", configuration.getBrowserViewMode() < 0 ? 0 : configuration.getBrowserViewMode());
			plist.add("browserWindowWebView", 3);
			plist.add("URLFilterEnable", configuration.isUrlFilter());
			plist.add("URLFilterEnableContentFilter", configuration.isUrlContentFilter());
			plist.addArray("URLFilterIgnoreList");
			plist.add("URLFilterMessage", 1);
			plist.add("urlFilterRegex", isUrlFilterRegex(configuration));

			if(StringHelper.containsNonWhitespace(configuration.getAllowedUrlExpressions())
					|| StringHelper.containsNonWhitespace(configuration.getAllowedUrlRegex())
					|| StringHelper.containsNonWhitespace(configuration.getBlockedUrlExpressions())
					|| StringHelper.containsNonWhitespace(configuration.getBlockedUrlRegex())) {
				Element rulesEl = plist.addArray("URLFilterRules");
				addFilterRules(configuration.getAllowedUrlExpressions(), true, false, rulesEl, plist);
				addFilterRules(configuration.getAllowedUrlRegex(), true, true, rulesEl, plist);
				addFilterRules(configuration.getBlockedUrlExpressions(), false, false, rulesEl, plist);
				addFilterRules(configuration.getBlockedUrlRegex(), false, true, rulesEl, plist);
			}
			plist.add("urlFilterTrustedContent", isUrlFilterTrustedContent(configuration));
			
			plist.add("startURL", configuration.getStartUrl());
			plist.add("sendBrowserExamKey", true);
			plist.add("examSessionClearCookiesOnStart", false);
			plist.add("allowPreferencesWindow", false);
			return plist.toPlistString();
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
	
	private static void addFilterRules(String expression, boolean allow, boolean regex, Element rulesEl, PList plist) {
		String[] expressions = expression.split("\r?\n");
		for(String exp:expressions) {
			exp = exp == null ? null : exp.trim();
			if(StringHelper.containsNonWhitespace(expression)) {
				addFilterRule(exp, allow, regex, rulesEl, plist);
			}
		}
	}
	
	private static void addFilterRule(String expression, boolean allow, boolean regex, Element rulesEl, PList plist) {
		Element dictEl = plist.addDictToArray(rulesEl);
		plist.add(dictEl, "action", allow ? Integer.valueOf(1) : Integer.valueOf(0));
		plist.add(dictEl, "active", true);
		plist.add(dictEl, "expression", expression);
		plist.add(dictEl, "regex", regex);
	}
	
	public static String toJson(SafeExamBrowserConfiguration configuration, AssessmentModule assessmentModule) {
		try {
			JsonObject plist = new JsonObject();
			plist.addProperty("allowAudioCapture", configuration.isAllowAudioCapture());
			plist.addProperty("allowPreferencesWindow", false);
			plist.addProperty("allowQuit", configuration.isAllowQuit());
			plist.addProperty("allowSpellCheck", configuration.isAllowSpellCheck());
			plist.addProperty("allowVideoCapture", configuration.isAllowVideoCapture());
			plist.addProperty("allowWlan", configuration.isAllowWlan());
			plist.addProperty("audioControlEnabled", configuration.isAudioControlEnabled());
			plist.addProperty("audioMute", configuration.isAudioMute());
			plist.addProperty("browserViewMode", configuration.getBrowserViewMode() < 0 ? 0 : configuration.getBrowserViewMode());
			plist.addProperty("browserWindowAllowReload", configuration.isBrowserWindowAllowReload());
			plist.addProperty("browserWindowWebView", 3);
			plist.addProperty("enableZoomPage", configuration.isAllowZoomInOut());
			plist.addProperty("enableZoomText", configuration.isAllowZoomInOut());
			plist.addProperty("examSessionClearCookiesOnStart", false);
			if(StringHelper.containsNonWhitespace(configuration.getPasswordToExit())) {
				plist.addProperty("hashedQuitPassword", Encoder.sha256Exam(configuration.getPasswordToExit()));
			}
			if(StringHelper.containsNonWhitespace(configuration.getLinkToQuit())) {
				plist.addProperty("quitURL", configuration.getLinkToQuit());
			} else {
				plist.addProperty("quitURL", assessmentModule.getShadowQuitURL());
			}
			plist.addProperty("quitURLConfirm", configuration.isQuitURLConfirm());
			plist.addProperty("sendBrowserExamKey", true);
			plist.addProperty("showInputLanguage", configuration.isShowKeyboardLayout());
			plist.addProperty("showReloadButton", configuration.isShowReloadButton());
			plist.addProperty("showTaskBar", configuration.isShowTaskBar());
			plist.addProperty("showTime", configuration.isShowTimeClock());
			plist.addProperty("startURL", configuration.getStartUrl());
			plist.addProperty("URLFilterEnable", configuration.isUrlFilter());
			plist.addProperty("URLFilterEnableContentFilter", configuration.isUrlContentFilter());
			plist.add("URLFilterIgnoreList", new JsonArray());
			plist.addProperty("URLFilterMessage", Integer.valueOf(1));
			plist.addProperty("urlFilterRegex", isUrlFilterRegex(configuration));
			
			if(StringHelper.containsNonWhitespace(configuration.getAllowedUrlExpressions())
					|| StringHelper.containsNonWhitespace(configuration.getAllowedUrlRegex())
					|| StringHelper.containsNonWhitespace(configuration.getBlockedUrlExpressions())
					|| StringHelper.containsNonWhitespace(configuration.getBlockedUrlRegex())) {
				JsonArray urlFilterRules = new JsonArray();
				plist.add("URLFilterRules", urlFilterRules);
				addFilterRules(true, configuration.getAllowedUrlExpressions(), false, urlFilterRules);
				addFilterRules(true, configuration.getAllowedUrlRegex(), true, urlFilterRules);
				addFilterRules(false, configuration.getBlockedUrlExpressions(), false, urlFilterRules);
				addFilterRules(false, configuration.getBlockedUrlRegex(), true, urlFilterRules);
			}

			plist.addProperty("urlFilterTrustedContent", isUrlFilterTrustedContent(configuration));

			String jsonPList = plist.toString();
			jsonPList = jsonPList.replace(BACKSLASH_SUBSTITUTE, "\\");
			return jsonPList;
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
	
	private static boolean isUrlFilterRegex(SafeExamBrowserConfiguration configuration) {
		return StringHelper.containsNonWhitespace(configuration.getAllowedUrlRegex())
				|| StringHelper.containsNonWhitespace(configuration.getBlockedUrlRegex());
	}
	
	private static boolean isUrlFilterTrustedContent(SafeExamBrowserConfiguration configuration) {
		return configuration.isUrlFilter() || configuration.isUrlContentFilter();
	}
	
	private static void addFilterRules(boolean allow, String expression, boolean regex, JsonArray urlFilterRules) {
		String[] expressions = expression.split("\r?\n");
		for(String exp:expressions) {
			exp = exp == null ? null : exp.trim();
			if(StringHelper.containsNonWhitespace(expression)) {
				urlFilterRules.add(addFilterRule(allow, exp, regex));
			}
		}
	}
	
	private static JsonObject addFilterRule(boolean allow, String expression, boolean regex) {
		JsonObject filter = new JsonObject();
		filter.addProperty("action", allow ? Integer.valueOf(1) : Integer.valueOf(0));
		filter.addProperty("active", true);
		filter.addProperty("expression", expression == null ? "" : expression.replace("\\", BACKSLASH_SUBSTITUTE));
		filter.addProperty("regex", regex);
		return filter;
	}
	
	
	
	public static SafeExamBrowserConfiguration fromXml(String xstreamXml) {
		return (SafeExamBrowserConfiguration)XStreamHelper.readObject(xstream, xstreamXml);
	}
	
	public static String toXml(SafeExamBrowserConfiguration configuration) {
		return xstream.toXML(configuration);
	}
}
