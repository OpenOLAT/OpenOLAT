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
package org.olat.ims.qti21.manager;

import java.util.List;

/**
 * Based on the formatter of QTI 1.2
 * 
 * Initial date: 03.02.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ResponseFormater {
	
	public static String format(List<String> stringResponses) {
		StringBuilder sb = new StringBuilder(256);
		if (stringResponses == null || stringResponses.isEmpty()) {
			sb.append("[]");
		} else {
			for (String stringResponse :stringResponses) {
				sb.append("[").append(quoteSpecialQTIResultCharacters(stringResponse)).append("]");
			}
		}
		return sb.toString();
	}
	
	/**
	 * Qotes special characters used by the QTIResult answer formatting. Special
	 * characters are '\', '[', ']', '\t', '\n', '\r', '\f', '\a' and '\e'
	 * 
	 * @param string The string to be quoted
	 * @return The quoted string
	 */
	public static String quoteSpecialQTIResultCharacters(String string) {
		string = string.replaceAll("\\\\", "\\\\\\\\");
		string = string.replaceAll("\\[", "\\\\[");
		string = string.replaceAll("\\]", "\\\\]");
		string = string.replaceAll("\\t", "\\\\t");
		string = string.replaceAll("\\n", "\\\\n");
		string = string.replaceAll("\\r", "\\\\r");
		string = string.replaceAll("\\f", "\\\\f");
		string = string.replaceAll("\\a", "\\\\a");
		string = string.replaceAll("\\e", "\\\\e");
		return string;
	}

	/**
	 * Unquotes special characters in the QTIResult answer texts.
	 * 
	 * @param string
	 * @return The unquoted sting
	 */
	public static String unQuoteSpecialQTIResultCharacters(String string) {
		string = string.replaceAll("\\\\[", "\\[");
		string = string.replaceAll("\\\\]", "\\]");
		string = string.replaceAll("\\\\t", "\\t");
		string = string.replaceAll("\\\\n", "\\n");
		string = string.replaceAll("\\\\r", "\\r");
		string = string.replaceAll("\\\\f", "\\f");
		string = string.replaceAll("\\\\a", "\\a");
		string = string.replaceAll("\\\\e", "\\e");
		string = string.replaceAll("\\\\\\\\", "\\\\");
		return string;
	}

}
