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
package org.olat.core.util.docxToMarkdown;

import java.util.Map;

/**
 * Numbering definition parsed from numbering.xml.
 * Maps indent level (ilvl) to whether the list is ordered or unordered.
 *
 * @param orderedByLevel map from ilvl (0-based) to ordered flag
 * @author gnaegi, https://www.frentix.com
 */
record DocxNumberingDef(Map<Integer, Boolean> orderedByLevel) {

	public boolean isOrdered(int ilvl) {
		return orderedByLevel.getOrDefault(ilvl, false);
	}
}
