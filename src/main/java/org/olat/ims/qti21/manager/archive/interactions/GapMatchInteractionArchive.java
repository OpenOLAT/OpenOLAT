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
import java.util.Set;

import org.olat.core.util.openxml.OpenXMLWorkbook;
import org.olat.core.util.openxml.OpenXMLWorksheet.Row;
import org.olat.ims.qti21.AssessmentResponse;
import org.olat.ims.qti21.manager.CorrectResponsesUtil;

import uk.ac.ed.ph.jqtiplus.node.content.basic.TextRun;
import uk.ac.ed.ph.jqtiplus.node.content.variable.TextOrVariable;
import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.GapMatchInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.Interaction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.GapChoice;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.GapText;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.content.Gap;
import uk.ac.ed.ph.jqtiplus.types.Identifier;
import uk.ac.ed.ph.jqtiplus.utils.QueryUtils;

/**
 * 
 * Initial date: 27.04.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GapMatchInteractionArchive extends DefaultInteractionArchive  {
	
	@Override
	public int writeHeader1(AssessmentItem item, Interaction interaction, int itemNumber, int interactionNumber, Row dataRow, int col, OpenXMLWorkbook workbook) {
		if(interactionNumber == 0) {
			String header = item.getTitle();
			dataRow.addCell(col, header, workbook.getStyles().getHeaderStyle());
		}
		col += Math.max(1, getGaps(interaction).size());
		return col;
	}

	@Override
	public int writeHeader2(AssessmentItem item, Interaction interaction, int itemNumber, int interactionNumber, Row dataRow, int col, OpenXMLWorkbook workbook) {
		List<Gap> gaps = getGaps(interaction);
		if(gaps.size() > 0) {
			for(int i=0; i<gaps.size(); i++) {
				String header = (itemNumber + 1) + "_G" + (i + 1);
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
		
		List<Gap> gaps = getGaps(interaction);
		if(gaps.size() > 0) {
			GapMatchInteraction gapMatchInteraction = (GapMatchInteraction)interaction;
			
			Set<String> correctAnswers = CorrectResponsesUtil.getCorrectDirectPairResponses(item, interaction, false);
			String stringuifiedResponse = response == null ? null : response.getStringuifiedResponse();
			List<String> responses = CorrectResponsesUtil.parseResponses(stringuifiedResponse);
			
			for(Gap gap:gaps) {
				String gapIdentifier = gap.getIdentifier().toString();
				
				String gapResponse = null;
				for(String r:responses) {
					if(r.endsWith(gapIdentifier)) {
						gapResponse = r;
					}
				}
				
				if(gapResponse != null) {
					String[] gapResponsePair = gapResponse.split(" ");
					if(gapResponsePair.length > 1) {
						String gapResponseAssociation = gapResponsePair[0];
						Identifier gapResponseIdentifier = Identifier.assumedLegal(gapResponseAssociation);
						GapChoice choice = gapMatchInteraction.getGapChoice(gapResponseIdentifier);
						String value = null;
						if(choice != null) {
							value = getTextContent(choice);
						}
						
						boolean correct = correctAnswers.contains(gapResponse);
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
			}
			
		} else {
			col++;
		}
		
		return col;
	}
	
	private String getTextContent(GapChoice selectedChoice) {
		if(selectedChoice instanceof GapText) {
			List<TextOrVariable> values = ((GapText)selectedChoice).getTextOrVariables();
			for(TextOrVariable value:values) {
				if(value instanceof TextRun) {
					return ((TextRun)value).getTextContent();
				}
			}
		}
		return selectedChoice.getIdentifier().toString();
	}
	
	public List<Gap> getGaps(Interaction interaction) {
		return QueryUtils.search(Gap.class, interaction.getNodeGroups().getBlockStaticGroup().getBlockStatics());
	}
}
