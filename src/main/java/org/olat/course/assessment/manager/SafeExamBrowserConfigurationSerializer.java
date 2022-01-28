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
	
	public static String toPList(SafeExamBrowserConfiguration configuration) {
		try {
			PList plist = new PList();
			plist.add("showTaskBar", configuration.isShowTaskBar());
			// allowWlan
			plist.add("showReloadButton", configuration.isShowReloadButton());
			plist.add("showTime", configuration.isShowTimeClock());
			plist.add("showInputLanguage", configuration.isShowKeyboardLayout());
			plist.add("allowQuit", configuration.isAllowQuit());
			plist.add("quitURLConfirm", configuration.isQuitURLConfirm());
			plist.add("audioControlEnabled", configuration.isAudioControlEnabled());
			plist.add("audioMute", configuration.isAudioMute());
			plist.add("allowSpellCheck", configuration.isAllowSpellCheck());
			plist.add("browserWindowAllowReload", configuration.isBrowserWindowAllowReload());
			if(StringHelper.containsNonWhitespace(configuration.getPasswordToExit())) {
				plist.add("hashedQuitPassword", Encoder.sha256Exam(configuration.getPasswordToExit()));
			}
			plist.add("URLFilterEnable", configuration.isUrlFilter());
			plist.add("URLFilterEnableContentFilter", configuration.isUrlContentFilter());
			
			if(StringHelper.containsNonWhitespace(configuration.getAllowedUrlExpressions())
					|| StringHelper.containsNonWhitespace(configuration.getAllowedUrlRegex())
					|| StringHelper.containsNonWhitespace(configuration.getBlockedUrlExpressions())
					|| StringHelper.containsNonWhitespace(configuration.getBlockedUrlRegex())) {
				Element rulesEl = plist.addArray("URLFilterRules");
				addFilterRule(configuration.getAllowedUrlExpressions(), true, false, rulesEl, plist);
				addFilterRule(configuration.getAllowedUrlRegex(), true, true, rulesEl, plist);
				addFilterRule(configuration.getBlockedUrlExpressions(), false, false, rulesEl, plist);
				addFilterRule(configuration.getBlockedUrlRegex(), false, true, rulesEl, plist);
			}
			
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
	
	private static void addFilterRule(String expression, boolean allow, boolean regex, Element rulesEl, PList plist) {
		Element dictEl = plist.addDictToArray(rulesEl);
		plist.add(dictEl, "action", allow ? Integer.valueOf(1) : Integer.valueOf(0));
		plist.add(dictEl, "active", true);
		plist.add(dictEl, "expression", expression);
		plist.add(dictEl, "regex", regex);
	}
	
	public static String toJson(SafeExamBrowserConfiguration configuration) {
		try {
			JsonObject plist = new JsonObject();
			plist.addProperty("allowPreferencesWindow", false);
			plist.addProperty("allowQuit", configuration.isAllowQuit());
			plist.addProperty("allowSpellCheck", configuration.isAllowSpellCheck());
			plist.addProperty("audioControlEnabled", configuration.isAudioControlEnabled());
			plist.addProperty("audioMute", configuration.isAudioMute());
			plist.addProperty("browserWindowAllowReload", configuration.isBrowserWindowAllowReload());
			plist.addProperty("examSessionClearCookiesOnStart", false);
			if(StringHelper.containsNonWhitespace(configuration.getPasswordToExit())) {
				plist.addProperty("hashedQuitPassword", Encoder.sha256Exam(configuration.getPasswordToExit()));
			}
			plist.addProperty("quitURLConfirm", configuration.isQuitURLConfirm());
			plist.addProperty("sendBrowserExamKey", true);
			plist.addProperty("showInputLanguage", configuration.isShowKeyboardLayout());
			plist.addProperty("showReloadButton", configuration.isShowReloadButton());
			plist.addProperty("showTaskBar", configuration.isShowTaskBar());
			plist.addProperty("showTime", configuration.isShowTimeClock());
			plist.addProperty("startURL", configuration.getStartUrl());
			plist.addProperty("URLFilterEnable", false);
			plist.addProperty("URLFilterEnableContentFilter", false);
			if(StringHelper.containsNonWhitespace(configuration.getAllowedUrlExpressions())
					|| StringHelper.containsNonWhitespace(configuration.getAllowedUrlRegex())
					|| StringHelper.containsNonWhitespace(configuration.getBlockedUrlExpressions())
					|| StringHelper.containsNonWhitespace(configuration.getBlockedUrlRegex())) {
				JsonArray urlFilterRules = new JsonArray();
				plist.add("URLFilterRules", urlFilterRules);
				urlFilterRules.add(addFilterRule(true, configuration.getAllowedUrlExpressions(), false));
				urlFilterRules.add(addFilterRule(true, configuration.getAllowedUrlRegex(), true));
				urlFilterRules.add(addFilterRule(false, configuration.getBlockedUrlExpressions(), false));
				urlFilterRules.add(addFilterRule(false, configuration.getBlockedUrlRegex(), true));
			}
			return plist.toString();
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
	
	private static JsonObject addFilterRule(boolean allow, String expression, boolean regex) {
		JsonObject filter = new JsonObject();
		filter.addProperty("action", allow ? Integer.valueOf(1) : Integer.valueOf(0));
		filter.addProperty("active", true);
		filter.addProperty("expression", expression == null ? "" : expression);
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
