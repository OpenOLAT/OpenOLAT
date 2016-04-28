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

import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.GraphicGapMatchInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.Interaction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.graphic.AssociableHotspot;

/**
 * 
 * Initial date: 27.04.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GraphicGapMatchInteractionArchive extends DefaultInteractionArchive {

	@Override
	public int writeHeader1(AssessmentItem item, Interaction interaction, int itemNumber, int interactionNumber, Row dataRow, int col, OpenXMLWorkbook workbook) {
		if(interactionNumber == 0) {
			String header = item.getTitle();
			dataRow.addCell(col++, header, workbook.getStyles().getHeaderStyle());
		}

		GraphicGapMatchInteraction gapMatchInteraction = (GraphicGapMatchInteraction)interaction;
		List<AssociableHotspot> spots = gapMatchInteraction.getAssociableHotspots();
		if(spots.size() > 0) {
			col += (spots.size() - 1);
		}
		return col;
	}

	@Override
	public int writeHeader2(AssessmentItem item, Interaction interaction, int itemNumber, int interactionNumber,
			Row dataRow, int col, OpenXMLWorkbook workbook) {

		GraphicGapMatchInteraction gapMatchInteraction = (GraphicGapMatchInteraction)interaction;
		List<AssociableHotspot> spots = gapMatchInteraction.getAssociableHotspots();
		if(spots.size() > 0) {
			for(int i=0; i<spots.size(); i++) {
				String header = (itemNumber + 1) + "_GG" + (i + 1);
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
		GraphicGapMatchInteraction gapMatchInteraction = (GraphicGapMatchInteraction)interaction;
		
		String stringuifiedResponse = response == null ? null : response.getStringuifiedResponse();
		List<String> responses = CorrectResponsesUtil.parseResponses(stringuifiedResponse);
		
		Set<String> rightAnswers = CorrectResponsesUtil.getCorrectDirectPairResponses(item, interaction, false);
		List<AssociableHotspot> spots = gapMatchInteraction.getAssociableHotspots();
		for(AssociableHotspot spot:spots) {
			String spotIdentifier = spot.getIdentifier().toString();
			
			String spotResponse = null;
			for(String r:responses) {
				if(r.endsWith(spotIdentifier)) {
					spotResponse = r;
				}
			}
			
			if(spotResponse != null) {
				String[] spotResponsePair = spotResponse.split(" ");
				if(spotResponsePair.length > 1) {
					String stopResponseAssociation = spotResponsePair[0];
					boolean correct = rightAnswers.contains(spotResponse);
					if(correct) {
						dataRow.addCell(col++, stopResponseAssociation, workbook.getStyles().getCorrectStyle());
					} else {
						dataRow.addCell(col++, stopResponseAssociation);
					}
				} else {
					col++;
				}
			} else {
				col++;
			}
		}

		return col;
	}
}
