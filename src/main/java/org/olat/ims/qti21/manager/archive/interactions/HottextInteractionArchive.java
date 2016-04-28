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

import org.olat.core.util.openxml.OpenXMLWorkbook;
import org.olat.core.util.openxml.OpenXMLWorksheet.Row;
import org.olat.ims.qti21.AssessmentResponse;
import org.olat.ims.qti21.manager.CorrectResponsesUtil;

import uk.ac.ed.ph.jqtiplus.node.QtiNode;
import uk.ac.ed.ph.jqtiplus.node.content.basic.InlineStatic;
import uk.ac.ed.ph.jqtiplus.node.content.basic.TextRun;
import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.Interaction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.content.Hottext;
import uk.ac.ed.ph.jqtiplus.types.Identifier;
import uk.ac.ed.ph.jqtiplus.utils.QueryUtils;

/**
 * 
 * Initial date: 27.04.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class HottextInteractionArchive extends DefaultInteractionArchive {

	@Override
	public int writeHeader1(AssessmentItem item, Interaction interaction, int itemNumber, int interactionNumber, Row dataRow, int col, OpenXMLWorkbook workbook) {
		int numOfChoices = getHottexts(interaction).size();
		if(interactionNumber == 0) {
			String header = item.getTitle();
			dataRow.addCell(col, header, workbook.getStyles().getHeaderStyle());
		}
		col += Math.max(1, numOfChoices);
		return col;
	}

	@Override
	public int writeHeader2(AssessmentItem item, Interaction interaction, int itemNumber, int interactionNumber, Row dataRow, int col, OpenXMLWorkbook workbook) {
		List<Hottext> texts = getHottexts(interaction);
		if(texts.size() > 0) {
			for(int i=0; i<texts.size(); i++) {
				String header = (itemNumber + 1) + "_HT" + (i + 1);
				dataRow.addCell(col++, header, workbook.getStyles().getHeaderStyle());
			}
		} else {
			col++;
		}
		return col;
	
	}

	@Override
	public int writeInteractionData(AssessmentItem item, AssessmentResponse response, Interaction interaction, int itemNumber, Row dataRow, int col, OpenXMLWorkbook workbook) {
		String stringuifiedResponse = response == null ? null : response.getStringuifiedResponse();
		List<String> responses = CorrectResponsesUtil.parseResponses(stringuifiedResponse);
		List<Identifier> correctAnswers = CorrectResponsesUtil.getCorrectIdentifierResponses(item, interaction);
		
		List<Hottext> texts =  getHottexts(interaction);
		for(int i=0; i<texts.size(); i++) {
			Hottext text = texts.get(i);
			Identifier textIdentifier = text.getIdentifier();

			//user select this hot text
			if(responses.contains(text.getIdentifier().toString())) {
				String content = getContent(text);
				//correct -> must be clicked
				boolean correct = correctAnswers.contains(textIdentifier);
				if(correct) {
					dataRow.addCell(col++, content, workbook.getStyles().getCorrectStyle());
				} else {
					dataRow.addCell(col++, content, null);
				}
			} else {
				//correct -> must not be clicked
				boolean correct = !correctAnswers.contains(textIdentifier);
				if(correct) {
					dataRow.addCell(col++, "", workbook.getStyles().getCorrectStyle());
				} else {
					col++;
				}
			}
		}
		return col;
	}
	
	private List<Hottext> getHottexts(Interaction interaction) {
		List<Hottext> texts = QueryUtils.search(Hottext.class, interaction);
		return texts;
	}
	
	private String getContent(Hottext association) {
		StringBuilder sb = new StringBuilder();
		List<InlineStatic> inlineStatics = association.getInlineStatics();
		for(InlineStatic inlineStatic:inlineStatics) {
			renderContent(sb, inlineStatic);
		}
		
		if(sb.length() > 0) {
			return sb.toString();
		}
		return association.getIdentifier().toString();
	}
	
	private void renderContent(StringBuilder sb, QtiNode node) {
		if(node instanceof TextRun) {
			sb.append(((TextRun)node).getTextContent());
		}
	}
}
