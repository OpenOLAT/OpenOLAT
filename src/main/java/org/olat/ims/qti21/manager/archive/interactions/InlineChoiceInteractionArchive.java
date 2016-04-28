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

import java.util.List;

import org.olat.core.util.StringHelper;
import org.olat.core.util.openxml.OpenXMLWorkbook;
import org.olat.core.util.openxml.OpenXMLWorksheet.Row;
import org.olat.ims.qti21.AssessmentResponse;
import org.olat.ims.qti21.manager.CorrectResponsesUtil;

import uk.ac.ed.ph.jqtiplus.node.content.basic.TextRun;
import uk.ac.ed.ph.jqtiplus.node.content.variable.TextOrVariable;
import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.InlineChoiceInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.Interaction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.InlineChoice;
import uk.ac.ed.ph.jqtiplus.types.Identifier;

/**
 * 
 * Initial date: 27.04.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class InlineChoiceInteractionArchive extends DefaultInteractionArchive {
	
	@Override
	public int writeHeader1(AssessmentItem item, Interaction interaction, int itemNumber, int interactionNumber, Row dataRow, int col, OpenXMLWorkbook workbook) {
		if(interactionNumber == 0) {
			String header = item.getTitle();
			dataRow.addCell(col, header, workbook.getStyles().getHeaderStyle());
		}

		col += 1;
		return col;
	}

	@Override
	public int writeHeader2(AssessmentItem item, Interaction interaction, int itemNumber, int interactionNumber, Row dataRow, int col, OpenXMLWorkbook workbook) {
		String header = (itemNumber + 1) + "_IC1";
		dataRow.addCell(col++, header, workbook.getStyles().getHeaderStyle());
		return col;
	}

	@Override
	public int writeInteractionData(AssessmentItem item, AssessmentResponse response, Interaction interaction, int itemNumber, Row dataRow, int col, OpenXMLWorkbook workbook) {
		InlineChoiceInteraction choiceInteraction = (InlineChoiceInteraction)interaction;
		
		List<Identifier> correctAnswers = CorrectResponsesUtil.getCorrectIdentifierResponses(item, interaction);
		String stringuifiedResponse = response == null ? null : response.getStringuifiedResponse();
		if(StringHelper.containsNonWhitespace(stringuifiedResponse)) {
			String inlineResponse = CorrectResponsesUtil.stripResponse(stringuifiedResponse);
			Identifier responseIdentifier = Identifier.assumedLegal(inlineResponse);
		
			boolean correct = false;
			for(Identifier correctAnswer:correctAnswers) {
				if(responseIdentifier.equals(correctAnswer)) {
					correct = true;
					break;
				}
			}
			
			InlineChoice selectedChoice = choiceInteraction.getInlineChoice(responseIdentifier);
			if(selectedChoice != null) {
				String value = getTextContent(selectedChoice);
				if(correct) {
					dataRow.addCell(col++, value, workbook.getStyles().getCorrectStyle());
				} else {
					dataRow.addCell(col++, value);
				}
			} else {
				col++;
			}
		} else {
			col++;
		}
		return col;
	}
	
	private String getTextContent(InlineChoice selectedChoice) {
		List<TextOrVariable> values = selectedChoice.getTextOrVariables();
		for(TextOrVariable value:values) {
			if(value instanceof TextRun) {
				return ((TextRun)value).getTextContent();
			}
		}
		return selectedChoice.getIdentifier().toString();
	}

}
