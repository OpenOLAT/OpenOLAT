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

import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.Interaction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.Choice;
import uk.ac.ed.ph.jqtiplus.types.Identifier;

/**
 * 
 * Initial date: 26.04.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public abstract class AbstractChoiceInteractionArchive extends DefaultInteractionArchive {
	
	public abstract List<? extends Choice> getChoices(Interaction interaction);

	@Override
	public int writeHeader1(AssessmentItem item, Interaction interaction, int itemNumber, int interactionNumber, Row dataRow, int col, OpenXMLWorkbook workbook) {
		int numOfChoices = getChoices(interaction).size();
		if(interactionNumber == 0) {
			String header = item.getTitle();
			dataRow.addCell(col, header, workbook.getStyles().getHeaderStyle());
		}

		col += numOfChoices;
		return col;
	}

	@Override
	public int writeHeader2(AssessmentItem item, Interaction interaction, int itemNumber, int interactionNumber, Row dataRow, int col, OpenXMLWorkbook workbook) {
		List<? extends Choice> choices = getChoices(interaction);
		for(int i=0; i<choices.size(); i++) {
			String header = (itemNumber + 1) + "_C" + i;
			dataRow.addCell(col++, header, workbook.getStyles().getHeaderStyle());
		}
		return col;
	}

	@Override
	public int writeInteractionData(AssessmentItem item, AssessmentResponse response, Interaction interaction, int itemNumber, Row dataRow, int col, OpenXMLWorkbook workbook) {
		String stringuifiedResponses = response == null ? null : response.getStringuifiedResponse();
		List<Identifier> correctAnswers = CorrectResponsesUtil.getCorrectIdentifierResponses(item, interaction);
		List<? extends Choice> choices = getChoices(interaction);
		if(StringHelper.containsNonWhitespace(stringuifiedResponses)) {
			for(int i=0; i<choices.size(); i++) {
				Identifier choiceIdentifier = choices.get(i).getIdentifier();
				if(stringuifiedResponses.contains("[" + choiceIdentifier + "]")) {
					if(correctAnswers.contains(choiceIdentifier)) {
						//the checked answer is correct
						dataRow.addCell(col++, "x", workbook.getStyles().getCorrectStyle());
					} else {
						dataRow.addCell(col++, "x");
					}
				}  else {
					col++;
				}
			}
		} else {
			col += choices.size();
		}
		return col;
	}
}
