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
package org.olat.core.commons.services.doceditor.onlyoffice.manager;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.olat.core.commons.services.doceditor.DocEditor.Mode;

/**
 * 
 * Initial date: 15 Apr 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class Formats {
	
	// see https://api.onlyoffice.com/editors/config/
	// see https://helpcenter.onlyoffice.com/ONLYOFFICE-Editors/ONLYOFFICE-Document-Editor/HelpfulHints/SupportedFormats.aspx
	// see https://helpcenter.onlyoffice.com/ONLYOFFICE-Editors/ONLYOFFICE-Spreadsheet-Editor/HelpfulHints/SupportedFormats.aspx
	// see https://helpcenter.onlyoffice.com/ONLYOFFICE-Editors/ONLYOFFICE-Presentation-Editor/HelpfulHints/SupportedFormats.aspx
	
	private static List<String> TEXT_EDIT = Collections.singletonList("docx");
	private static List<String> TEXT_VIEW = Arrays.asList(
			"docm",
			"dotx",
			"odt",
			"ott",
			"rtf",
			"txt",
			"doc",
			"dot",
			"dotm",
			"epub",
			"fodt",
			"mht",
			"pdf",
			"djvu",
			"xps"
		);
	private static List<String> SPREADSHEET_EDIT = Collections.singletonList("xlsx");
	private static List<String> SPREADSHEET_VIEW = Arrays.asList(
			"csv",
			"ods",
			"ots",
			"xltx",
			"fods",
			"xls",
			"xlsm",
			"xlt",
			"xltm"
		);
	private static List<String> PRESENTATION_EDIT = Collections.singletonList("pptx");
	private static List<String> PRESENTATION_VIEW = Arrays.asList(
			"fodp",
			"odp",
			"otp",
			"pot",
			"potm",
			"potx",
			"ppsx",
			"pps",
			"ppsm",
			"ppt",
			"pptm"
		);

	public static boolean isSupportedFormat(String suffix, Mode mode) {
		String lowerSuffix = suffix.toLowerCase();
		if (TEXT_EDIT.contains(lowerSuffix))         return true;
		if (SPREADSHEET_EDIT.contains(lowerSuffix))  return true;
		if (PRESENTATION_EDIT.contains(lowerSuffix)) return true;
		if (Mode.VIEW == mode || Mode.EMBEDDED == mode) {
			if (TEXT_VIEW.contains(lowerSuffix))         return true;
			if (SPREADSHEET_VIEW.contains(lowerSuffix))  return true;
			if (PRESENTATION_VIEW.contains(lowerSuffix)) return true;
		}
		return false;
	}
	
	public static String getEditorType(String suffix) {
		String lowerSuffix = suffix.toLowerCase();
		if (TEXT_EDIT.contains(lowerSuffix))         return "text";
		if (TEXT_VIEW.contains(lowerSuffix))         return "text";
		if (SPREADSHEET_EDIT.contains(lowerSuffix))  return "spreadsheet";
		if (SPREADSHEET_VIEW.contains(lowerSuffix))  return "spreadsheet";
		if (PRESENTATION_EDIT.contains(lowerSuffix)) return "presentation";
		if (PRESENTATION_VIEW.contains(lowerSuffix)) return "presentation";
		return null;
	}

}
