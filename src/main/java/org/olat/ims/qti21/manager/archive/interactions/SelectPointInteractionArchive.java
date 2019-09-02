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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.olat.core.util.openxml.OpenXMLWorkbook;
import org.olat.core.util.openxml.OpenXMLWorksheet.Row;
import org.olat.ims.qti21.AssessmentResponse;
import org.olat.ims.qti21.manager.CorrectResponsesUtil;

import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.Interaction;
import uk.ac.ed.ph.jqtiplus.node.item.response.declaration.AreaMapEntry;
import uk.ac.ed.ph.jqtiplus.node.item.response.declaration.AreaMapping;
import uk.ac.ed.ph.jqtiplus.node.item.response.declaration.ResponseDeclaration;
import uk.ac.ed.ph.jqtiplus.value.PointValue;

/**
 * 
 * Initial date: 27.04.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SelectPointInteractionArchive extends DefaultInteractionArchive {

	@Override
	public int writeHeader1(AssessmentItem item, Interaction interaction, int itemNumber, int interactionNumber, Row dataRow, int col, OpenXMLWorkbook workbook) {
		if(interactionNumber == 0) {
			String header = item.getTitle();
			dataRow.addCell(col++, header, workbook.getStyles().getHeaderStyle());
		} else {
			col++;
		}
		List<AreaMapEntry> areaMapEntries = getAreaMapEntries(item, interaction);
		if(areaMapEntries.size() > 0) {
			col += (areaMapEntries.size() - 1);
		}
		return col;
	}

	@Override
	public int writeHeader2(AssessmentItem item, Interaction interaction, int itemNumber, int interactionNumber, Row dataRow, int col, OpenXMLWorkbook workbook) {
		List<AreaMapEntry> areaMapEntries = getAreaMapEntries(item, interaction);
		if(areaMapEntries.isEmpty()) {
			col++;
		} else {
			for(AreaMapEntry areaMapEntry:areaMapEntries) {
				dataRow.addCell(col++, areaMapEntry.getShape().toQtiString());
			}
		}
		return col;
	}

	@Override
	public int writeInteractionData(AssessmentItem item, AssessmentResponse response, Interaction interaction, int itemNumber, Row dataRow, int col, OpenXMLWorkbook workbook) {
		List<AreaMapEntry> areaMapEntries = getAreaMapEntries(item, interaction);
		if(areaMapEntries.isEmpty()) {
			col++;
		} else {
			String stringuifiedResponse = response == null ? null : response.getStringuifiedResponse();
			List<String> responses = CorrectResponsesUtil.parseResponses(stringuifiedResponse);
			List<PointValue> responsePoints =  CorrectResponsesUtil.parseResponses(responses);
			
			List<Integer> freeSlots = new ArrayList<>();
			for(AreaMapEntry areaMapEntry:areaMapEntries) {
				int[] coords = CorrectResponsesUtil.convertCoordinates(areaMapEntry.getCoordinates());
				
				PointValue foundPoints = null;
				for(PointValue responsePoint:responsePoints) {
					if(areaMapEntry.getShape().isInside(coords, responsePoint)) {
						foundPoints = responsePoint;
						responsePoints.remove(responsePoint);
						break;
					}
				}
				
				if(foundPoints != null) {
					dataRow.addCell(col++, foundPoints.toQtiString(), workbook.getStyles().getCorrectStyle());
				} else {
					freeSlots.add(new Integer(col));
					dataRow.addCell(col++, "", null);
				}
			}
			
			//fill the rest
			for(PointValue responsePoint: responsePoints) {
				if(freeSlots.size() > 0) {
					Integer slot = freeSlots.get(0);
					dataRow.addCell(slot, responsePoint.toQtiString(), null);
				}
			}
		}
		return col++;
	}
	
	private List<AreaMapEntry> getAreaMapEntries(AssessmentItem item, Interaction interaction) {
		ResponseDeclaration responseDeclaration = item.getResponseDeclaration(interaction.getResponseIdentifier());
		if(responseDeclaration != null && responseDeclaration.getAreaMapping() != null) {
			AreaMapping mapping = responseDeclaration.getAreaMapping();
			if(mapping != null) {
				return mapping.getAreaMapEntries();
			}
		}
		return Collections.emptyList();
	}
}
