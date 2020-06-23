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
package org.olat.core.util.filter.impl;

import org.olat.core.util.filter.Filter;

/**
 * 
 * Initial date: 21 nov. 2017<br>
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class XMLValidEntityFilter implements Filter {

	@Override
	public String filter(String str) {
		if (str == null)
			return null;

		int firstAmp = str.indexOf('&');
		if (firstAmp < 0) {
			return str;
		} else {
			StringBuilder writer = new StringBuilder(str.length() + 100);

			writer.append(str, 0, firstAmp);
			int len = str.length();
			for (int i = firstAmp; i < len; i++) {
				char c = str.charAt(i);
				if (c == '&') {
					int nextIdx = i + 1;
					int semiColonIdx = str.indexOf(';', nextIdx);
					if (semiColonIdx == -1) {
						writer.append(c);
						continue;
					}
					int amphersandIdx = str.indexOf('&', i + 1);
					if (amphersandIdx != -1 && amphersandIdx < semiColonIdx) {
						// Then the text looks like &...&...;
						writer.append(c);
						continue;
					}

					String entityContent = str.substring(nextIdx, semiColonIdx);
					int entityContentLen = entityContent.length();
					if (entityContentLen > 1 && entityContentLen < 5 && entityContent.charAt(0) == '#') {
						// escaped value content is an integer (decimal or hexadecimal)
						int entityValue = -1;
						char isHexChar = entityContent.charAt(1);
						try {
							switch (isHexChar) {
								case 'X':
								case 'x': {
									entityValue = Integer.parseInt(entityContent.substring(2), 16);
									break;
								}
								default: {
									entityValue = Integer.parseInt(entityContent.substring(1), 10);
								}
							}
							if (entityValue == 0x9 || entityValue == 0xA || entityValue == 0xD) {
								writer.append('&').append(entityContent).append(';');
							} else if (entityValue < 0x20) {
								//skip them
							} else {
								writer.append('&').append(entityContent).append(';');
							}
						} catch (NumberFormatException e) {
							writer.append('&').append(entityContent).append(';');
						}
						i = semiColonIdx; // move index up to the semi-colon
					} else {
						writer.append(c);
					}
				} else {
					writer.append(c);
				}
			}
			return writer.toString();
		}
	}
}
