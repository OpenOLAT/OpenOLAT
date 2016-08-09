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

import org.olat.core.util.StringHelper;
import org.olat.core.util.openxml.OpenXMLWorkbook;
import org.olat.core.util.openxml.OpenXMLWorksheet.Row;
import org.olat.ims.qti21.AssessmentResponse;
import org.olat.ims.qti21.manager.CorrectResponsesUtil;
import org.olat.ims.qti21.model.xml.interactions.FIBAssessmentItemBuilder.AbstractEntry;

import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.Interaction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.TextEntryInteraction;

/**
 * 
 * Initial date: 26.04.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TextEntryInteractionArchive extends DefaultInteractionArchive {

	@Override
	public int writeHeader2(AssessmentItem item, Interaction interaction, int itemNumber, int interactionNumber, Row dataRow, int col, OpenXMLWorkbook workbook) {
		String header = (itemNumber + 1) + "_FIB" + (interactionNumber + 1);
		dataRow.addCell(col++, header, workbook.getStyles().getHeaderStyle());
		return col;
	}

	@Override
	public int writeInteractionData(AssessmentItem item, AssessmentResponse response, Interaction interaction,
			int itemNumber, Row dataRow, int col, OpenXMLWorkbook workbook) {
		
		String stringuifiedResponses = response == null ? null : response.getStringuifiedResponse();
		if(StringHelper.containsNonWhitespace(stringuifiedResponses)) {
			TextEntryInteraction textEntryInteraction = (TextEntryInteraction)interaction;
			AbstractEntry correctAnswers = CorrectResponsesUtil.getCorrectTextResponses(item, textEntryInteraction);
			stringuifiedResponses = CorrectResponsesUtil.stripResponse(stringuifiedResponses);

			boolean correct = correctAnswers.match(stringuifiedResponses);
			if(correct) {
				dataRow.addCell(col++, stringuifiedResponses, workbook.getStyles().getCorrectStyle());
			} else {
				dataRow.addCell(col++, stringuifiedResponses, null);
			}
		} else {
			col++;
		}
		return col;
	}
}
