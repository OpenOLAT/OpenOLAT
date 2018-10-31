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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.core.util.StringHelper;
import org.olat.core.util.openxml.OpenXMLWorkbook;
import org.olat.core.util.openxml.OpenXMLWorksheet.Row;
import org.olat.ims.qti21.AssessmentResponse;
import org.olat.ims.qti21.QTI21Constants;
import org.olat.ims.qti21.manager.CorrectResponsesUtil;
import org.olat.ims.qti21.manager.archive.SimpleContentRenderer;
import org.olat.ims.qti21.model.QTI21QuestionType;

import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.Interaction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.MatchInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.SimpleAssociableChoice;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.SimpleMatchSet;

/**
 * 
 * Initial date: 26.04.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MatchInteractionArchive extends DefaultInteractionArchive {

	@Override
	public int writeHeader1(AssessmentItem item, Interaction interaction, int itemNumber, int interactionNumber, Row dataRow, int col, OpenXMLWorkbook workbook) {
		if(interactionNumber == 0) {
			String header = item.getTitle();
			dataRow.addCell(col++, header, workbook.getStyles().getHeaderStyle());
		}
		MatchInteraction matchInteraction = (MatchInteraction)interaction;
		int numOfChoices = matchInteraction.getSimpleMatchSets().get(1).getSimpleAssociableChoices().size();
		if(numOfChoices > 0) {
			col += (numOfChoices - 1);
		}
		return col;
	}

	@Override
	public int writeHeader2(AssessmentItem item, Interaction interaction, int itemNumber, int interactionNumber, Row dataRow, int col, OpenXMLWorkbook workbook) {
		MatchInteraction matchInteraction = (MatchInteraction)interaction;

		boolean kprim = matchInteraction.getResponseIdentifier().toString().startsWith("KPRIM_")
				|| QTI21QuestionType.hasClass(matchInteraction, QTI21Constants.CSS_MATCH_KPRIM);
		String fix = kprim ? "_KP" : "_K";
		
		int numOfChoices = matchInteraction.getSimpleMatchSets().get(1).getSimpleAssociableChoices().size();
		if(numOfChoices > 0) {
			for(int i=0; i<numOfChoices; i++) {
				String header = (itemNumber + 1) + fix + (i + 1);
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
		MatchInteraction matchInteraction = (MatchInteraction)interaction;
		String stringuifiedResponse = response == null ? null : response.getStringuifiedResponse();
		if(!StringHelper.containsNonWhitespace(stringuifiedResponse)) {
			col += matchInteraction.getSimpleMatchSets().get(1).getSimpleAssociableChoices().size();
		} else {
			boolean kprim = matchInteraction.getResponseIdentifier().toString().startsWith("KPRIM_")
					|| QTI21QuestionType.hasClass(matchInteraction, QTI21Constants.CSS_MATCH_KPRIM);
			
			Set<String> correctAnswers = CorrectResponsesUtil.getCorrectDirectPairResponses(item, matchInteraction, false);
			List<String> responses = CorrectResponsesUtil.parseResponses(stringuifiedResponse);
			
			SimpleMatchSet sourceMatchSet = matchInteraction.getSimpleMatchSets().get(0);
			SimpleMatchSet targetMatchSet = matchInteraction.getSimpleMatchSets().get(1);
			
			for(SimpleAssociableChoice choice:targetMatchSet.getSimpleAssociableChoices()) {
				String choiceIdentifier = choice.getIdentifier().toString();
				
				if(kprim) {
					String markerCorrect = choiceIdentifier + " correct";
					String markerWrong = choiceIdentifier + " wrong";
					
					boolean isCorrectRight = correctAnswers.contains(markerCorrect);
					String rightFlag = isCorrectRight ? markerCorrect : markerWrong;
					String wrongFlag = isCorrectRight ? markerWrong : markerCorrect;
					
					String value = null;
					if(stringuifiedResponse.contains(markerCorrect)) {
						value = "+";
					} else if(stringuifiedResponse.contains(markerWrong)) {
						value = "-";
					}
					
					
					if(stringuifiedResponse.indexOf(rightFlag) >= 0) {
						dataRow.addCell(col++, value, workbook.getStyles().getCorrectStyle());
					} else if(stringuifiedResponse.indexOf(wrongFlag) >= 0) {
						dataRow.addCell(col++, value, null);
					} else {
						col++;
					}
				} else {
					Set<String> choiceResponses = new HashSet<>();
					for(String r:responses) {
						if(r.endsWith(choiceIdentifier)) {
							choiceResponses.add(r);
						}
					}
					
					// defined correct answers
					Set<String> choiceCorrectAnswers = new HashSet<>();
					for(String a:correctAnswers) {
						if(a.endsWith(choiceIdentifier)) {
							choiceCorrectAnswers.add(a);
						}
					}
					
					if(!choiceResponses.isEmpty()) {
						boolean correct = choiceResponses.containsAll(choiceCorrectAnswers)
								&& choiceCorrectAnswers.containsAll(choiceResponses);
						
						StringBuilder value = new StringBuilder(64);
						for(SimpleAssociableChoice association:sourceMatchSet.getSimpleAssociableChoices()) {
							for(String choiceResponse:choiceResponses) {
								if(choiceResponse.startsWith(association.getIdentifier().toString())) {
									if(value.length() > 0) value.append(", ");
									
									String val = getContent(association);
									if(val != null) {
										value.append(val);
									}
									
								}
							}
						}
						
						if(correct) {
							dataRow.addCell(col++, value.toString(), workbook.getStyles().getCorrectStyle());
						} else {
							dataRow.addCell(col++, value.toString(), null);
						}
					} else {
						col++;
					}
				}
			}
		}
		return col;
	}
	
	private String getContent(SimpleAssociableChoice association) {
		String content = SimpleContentRenderer.renderFlowStatics(association.getFlowStatics());
		if(StringHelper.containsNonWhitespace(content)) {
			return content;
		}
		return association.getIdentifier().toString();
	}
}
