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
package org.olat.ims.qti21.manager.archive.interactions;

import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.openxml.OpenXMLWorkbook;
import org.olat.core.util.openxml.OpenXMLWorksheet.Row;
import org.olat.ims.qti21.AssessmentResponse;
import org.olat.ims.qti21.manager.CorrectResponsesUtil;

import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.Interaction;

/**
 * 
 * Initial date: 26.04.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ExtendedTextInteractionArchive extends DefaultInteractionArchive {

	@Override
	public int writeInteractionData(AssessmentItem item, AssessmentResponse response, Interaction interaction, int itemNumber, Row dataRow, int col, OpenXMLWorkbook workbook, Translator translator) {
		String stringuifiedResponses = response == null ? null : response.getStringuifiedResponse();
		stringuifiedResponses = CorrectResponsesUtil.stripResponse(stringuifiedResponses);
		if(StringHelper.containsNonWhitespace(stringuifiedResponses)) {
			stringuifiedResponses = stringuifiedResponses.replace("\\r\\n", "\n");
			// Excel limitations: https://support.microsoft.com/en-us/office/excel-specifications-and-limits-1672b34d-7043-467e-8e27-269d656771c3
			if (stringuifiedResponses.length() > 32767 || stringuifiedResponses.lines().count() > 253) {
				stringuifiedResponses = translator.translate("archive.excel.limitation");
			}
			dataRow.addCell(col++, stringuifiedResponses);
		} else {
			col++;
		}
		return col;
	}
}
