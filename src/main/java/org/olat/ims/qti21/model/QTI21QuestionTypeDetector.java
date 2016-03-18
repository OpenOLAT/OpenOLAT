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
package org.olat.ims.qti21.model;

import java.util.List;

import org.olat.ims.qti21.QTI21Constants;

import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.ChoiceInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.ExtendedTextInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.HotspotInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.Interaction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.MatchInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.TextEntryInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.response.declaration.ResponseDeclaration;
import uk.ac.ed.ph.jqtiplus.value.BaseType;
import uk.ac.ed.ph.jqtiplus.value.Cardinality;

/**
 * Retrieve the type of question (not interaction) of a recognized tool
 * vendor.
 * 
 * Initial date: 16.02.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21QuestionTypeDetector {
	
	public static String generateNewIdentifier(String identifier) {
		String newIdentifier = null;
		for(QTI21QuestionType type:QTI21QuestionType.values()) {
			String prefix = type.getPrefix();
			if(prefix != null && identifier.startsWith(prefix)) {
				newIdentifier = IdentifierGenerator.newAsString(prefix);
			}
		}
		
		if(newIdentifier == null) {
			newIdentifier = IdentifierGenerator.newAsString();
		}
		
		return newIdentifier;
	}
	
	public static QTI21QuestionType getType(AssessmentItem item) {

		if(QTI21Constants.TOOLNAME.equals(item.getToolName())) {
			//we have create this one
			List<Interaction> interactions = item.getItemBody().findInteractions();
			
			boolean choice = false;
			boolean match = false;
			boolean textEntry = false;
			boolean essay = false;
			boolean hotspot = false;
			boolean unkown = false;
			
			if(interactions != null && interactions.size() > 0) {
				for(Interaction interaction: interactions) {
					if(interaction instanceof ChoiceInteraction) {
						choice = true;
					} else if(interaction instanceof MatchInteraction) {
						match = true;
					} else if(interaction instanceof ExtendedTextInteraction) {
						essay = true;
					} else if(interaction instanceof TextEntryInteraction) {
						textEntry = true;
					} else if(interaction instanceof HotspotInteraction) {
						hotspot = true;
					}  else {
						unkown = true;
					}	
				}	
			}
			
			if(unkown) {
				return QTI21QuestionType.unkown;
			} else if(choice && !match && !textEntry && !essay && !hotspot && !unkown) {
				return getTypeOfChoice(item);
			} else if(!choice && match && !textEntry && !essay && !hotspot && !unkown) {
				return getTypeOfMatch(item);
			} else if(!choice && !match && textEntry && !essay && !hotspot && !unkown) {
				return getTypeOfTextEntryInteraction(item);
			} else if(!choice && !match && !textEntry && essay && !hotspot && !unkown) {
				return QTI21QuestionType.essay;
			} else if(!choice && !match && !textEntry && !essay && hotspot && !unkown) {
				return QTI21QuestionType.hotspot;
			} else {
				return QTI21QuestionType.unkown;
			}
		} else {
			return QTI21QuestionType.unkown;
		}
	}
	
	private static final QTI21QuestionType getTypeOfTextEntryInteraction(AssessmentItem item) {
		if(item.getResponseDeclarations().size() > 0) {
			int text = 0;
			int numerical = 0;
			int unkown = 0;
			for(ResponseDeclaration responseDeclaration:item.getResponseDeclarations()) {
				if(responseDeclaration.hasBaseType(BaseType.STRING)) {
					text++;
				} else if(responseDeclaration.hasBaseType(BaseType.FLOAT)) {
					numerical++;
				} else {
					unkown++;
				}
			}
		
			if(text == 0 && numerical > 0 && unkown == 0) {
				return QTI21QuestionType.numerical;
			}
			if(text > 0 && unkown == 0) {
				return QTI21QuestionType.fib;
			}
		}
		return QTI21QuestionType.unkown;
	}
	
	private static final QTI21QuestionType getTypeOfMatch(AssessmentItem item) {
		if(item.getResponseDeclarations().size() == 1) {
			ResponseDeclaration responseDeclaration = item.getResponseDeclarations().get(0);
			String responseIdentifier = responseDeclaration.getIdentifier().toString();
			Cardinality cardinalty = responseDeclaration.getCardinality();
			if(cardinalty.isMultiple()) {
				if(responseIdentifier.startsWith("KPRIM_")) {
					return QTI21QuestionType.kprim;
				} else {
					return QTI21QuestionType.unkown;
				}
			} else {
				return QTI21QuestionType.unkown;
			}
		} else {
			return QTI21QuestionType.unkown;
		}
	}
	
	private static final QTI21QuestionType getTypeOfChoice(AssessmentItem item) {
		if(item.getResponseDeclarations().size() == 1) {
			ResponseDeclaration responseDeclaration = item.getResponseDeclarations().get(0);
			Cardinality cardinalty = responseDeclaration.getCardinality();
			if(cardinalty.isSingle()) {
				return QTI21QuestionType.sc;
			} else if(cardinalty.isMultiple()) {
				return QTI21QuestionType.mc;
			} else {
				return QTI21QuestionType.unkown;
			}
		} else {
			return QTI21QuestionType.unkown;
		}
	}

}
