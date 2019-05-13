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

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.openxml.OpenXMLWorkbook;
import org.olat.core.util.openxml.OpenXMLWorksheet.Row;
import org.olat.ims.qti21.AssessmentResponse;
import org.olat.ims.qti21.manager.CorrectResponsesUtil;

import uk.ac.ed.ph.jqtiplus.internal.util.Pair;
import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.Interaction;
import uk.ac.ed.ph.jqtiplus.types.DataTypeBinder;
import uk.ac.ed.ph.jqtiplus.types.Identifier;

/**
 * 
 * Initial date: 28.04.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
abstract class AbstractAssociateInteractionArchive extends DefaultInteractionArchive {
	
	private static final Logger log = Tracing.createLoggerFor(AbstractAssociateInteractionArchive.class);

	@Override
	public int writeHeader1(AssessmentItem item, Interaction interaction, int itemNumber, int interactionNumber, Row dataRow, int col, OpenXMLWorkbook workbook) {
		int maxAssociation = getMaxAssociations(interaction);
		if(interactionNumber == 0) {
			String header = item.getTitle();
			dataRow.addCell(col, header, workbook.getStyles().getHeaderStyle());
		}
		col += Math.max(1, maxAssociation);
		return col;
	}

	@Override
	public int writeHeader2(AssessmentItem item, Interaction interaction, int itemNumber, int interactionNumber, Row dataRow, int col, OpenXMLWorkbook workbook) {
		int maxAssociation = getMaxAssociations(interaction);
		if(maxAssociation > 0) {
			for(int i=0; i<maxAssociation; i++) {
				String header = (itemNumber + 1) + "_A" + i;
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
		int maxAssociation = getMaxAssociations(interaction);
		
		String stringuifiedResponse = response == null ? null : response.getStringuifiedResponse();
		List<String> responsesAssociations = CorrectResponsesUtil.parseResponses(stringuifiedResponse);
		List<String> correctAnswers = CorrectResponsesUtil.getCorrectMultiplePairResponses(item, interaction, false);
		
		for(int i=0; i<maxAssociation; i++) {
			String association = null;
			String reverseAssociation = null;
			if(responsesAssociations.size() > i) {
				association = responsesAssociations.get(i);
				try {
					Pair<Identifier, Identifier> pair = DataTypeBinder.parsePair(association);
					reverseAssociation = pair.getSecond() + " " + pair.getFirst();
				} catch (Exception e) {
					log.error("", e);
					association = null;
				}
			}
			
			if(association == null) {
				col++;
			} else if(correctAnswers.contains(association) || correctAnswers.contains(reverseAssociation)) {
				dataRow.addCell(col++, association, workbook.getStyles().getCorrectStyle());
			} else {
				dataRow.addCell(col++, association, null);
			}
		}
		return col;
	}
	
	protected abstract int getMaxAssociations(Interaction associateInteraction);

}
