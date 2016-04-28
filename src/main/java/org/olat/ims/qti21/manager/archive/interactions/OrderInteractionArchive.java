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
import org.olat.ims.qti21.manager.archive.SimpleContentRenderer;

import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.Interaction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.OrderInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.SimpleChoice;
import uk.ac.ed.ph.jqtiplus.types.Identifier;

/**
 * 
 * Initial date: 27.04.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OrderInteractionArchive extends DefaultInteractionArchive {

	@Override
	public int writeHeader1(AssessmentItem item, Interaction interaction, int itemNumber, int interactionNumber, Row dataRow, int col, OpenXMLWorkbook workbook) {
		if(interactionNumber == 0) {
			String header = item.getTitle();
			dataRow.addCell(col, header, workbook.getStyles().getHeaderStyle());
		}
		
		OrderInteraction orderInteraction = (OrderInteraction)interaction;
		int numOfChoices = orderInteraction.getSimpleChoices().size();
		col += Math.max(1, numOfChoices);
		return col;
	}

	@Override
	public int writeHeader2(AssessmentItem item, Interaction interaction, int itemNumber, int interactionNumber, Row dataRow, int col, OpenXMLWorkbook workbook) {
		OrderInteraction orderInteraction = (OrderInteraction)interaction;
		int numOfChoices = orderInteraction.getSimpleChoices().size();
		if(numOfChoices > 0) {
			for(int i=0; i<numOfChoices; i++) {
				String header = (itemNumber + 1) + "_O" + (i + 1);
				dataRow.addCell(col++, header, workbook.getStyles().getHeaderStyle());
			}
		} else {
			col++;
		}
		return col;
	}

	@Override
	public int writeInteractionData(AssessmentItem item, AssessmentResponse response, Interaction interaction,
			int itemNumber, Row dataRow, int col, OpenXMLWorkbook workbook) {
		OrderInteraction orderInteraction = (OrderInteraction)interaction;
		List<SimpleChoice> choices = orderInteraction.getSimpleChoices();
		
		if(choices.size() > 0) {
			String stringuifiedResponse = response == null ? null : response.getStringuifiedResponse();
			List<String> responses = CorrectResponsesUtil.parseResponses(stringuifiedResponse);
			List<Identifier> correctAnswers = CorrectResponsesUtil.getCorrectOrderedIdentifierResponses(item, interaction);
			
			for(int i=0; i<choices.size(); i++) {
				String currentResponse = null;
				String currentResponseText = null;
				if(responses.size() > i) {
					currentResponse = currentResponseText = responses.get(i);
					SimpleChoice selectedChoice = orderInteraction.getSimpleChoice(Identifier.assumedLegal(currentResponse));
					if(selectedChoice != null) {
						currentResponseText = getContent(selectedChoice);
					}
				}
				
				String correctAnswer = null;
				if(correctAnswers.size() > i) {
					correctAnswer = correctAnswers.get(i).toString();
				}
				
				if(correctAnswer != null && correctAnswer.equals(currentResponse)) {
					dataRow.addCell(col++, currentResponseText, workbook.getStyles().getCorrectStyle());
				} else {
					dataRow.addCell(col++, currentResponseText, null);
				}
			}
		} else {
			col++;
		}
		return col;
	}
	
	private String getContent(SimpleChoice choice) {
		String content = SimpleContentRenderer.renderFlowStatics(choice.getFlowStatics());
		if(StringHelper.containsNonWhitespace(content)) {
			return content;
		}
		return choice.getIdentifier().toString();
	}
}
