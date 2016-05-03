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
import org.olat.modules.qpool.QuestionType;

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
 * 
 * Initial date: 16.02.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public enum QTI21QuestionType {
	sc(true, "sc", QuestionType.SC),
	mc(true, "mc", QuestionType.MC),
	kprim(true, "kprim", QuestionType.KPRIM),
	fib(true, "fib", QuestionType.FIB),
	numerical(true, "numerical", QuestionType.NUMERICAL),
	hotspot(true, "hotspot", QuestionType.HOTSPOT),
	essay(true, "essay", QuestionType.ESSAY),
	unkown(false, null, null);
	
	private final String prefix;
	private final boolean editor;
	private final QuestionType poolQuestionType;
	
	private QTI21QuestionType(boolean editor, String prefix, QuestionType poolQuestionType) {
		this.editor = editor;
		this.prefix = prefix;
		this.poolQuestionType = poolQuestionType;
	}
	
	public boolean hasEditor() {
		return editor;
	}
	
	public String getPrefix() {
		return prefix;
	}
	
	public QuestionType getPoolQuestionType() {
		return poolQuestionType;
	}
	
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
			
			boolean fChoice = false;
			boolean fMatch = false;
			boolean fTextEntry = false;
			boolean fEssay = false;
			boolean fHotspot = false;
			boolean fUnkown = false;
			
			if(interactions != null && interactions.size() > 0) {
				for(Interaction interaction: interactions) {
					if(interaction instanceof ChoiceInteraction) {
						fChoice = true;
					} else if(interaction instanceof MatchInteraction) {
						fMatch = true;
					} else if(interaction instanceof ExtendedTextInteraction) {
						fEssay = true;
					} else if(interaction instanceof TextEntryInteraction) {
						fTextEntry = true;
					} else if(interaction instanceof HotspotInteraction) {
						fHotspot = true;
					}  else {
						fUnkown = true;
					}	
				}	
			}
			
			if(fUnkown) {
				return QTI21QuestionType.unkown;
			} else if(fChoice && !fMatch && !fTextEntry && !fEssay && !fHotspot && !fUnkown) {
				return getTypeOfChoice(item);
			} else if(!fChoice && fMatch && !fTextEntry && !fEssay && !fHotspot && !fUnkown) {
				return getTypeOfMatch(item);
			} else if(!fChoice && !fMatch && fTextEntry && !fEssay && !fHotspot && !fUnkown) {
				return getTypeOfTextEntryInteraction(item);
			} else if(!fChoice && !fMatch && !fTextEntry && fEssay && !fHotspot && !fUnkown) {
				return QTI21QuestionType.essay;
			} else if(!fChoice && !fMatch && !fTextEntry && !fEssay && fHotspot && !fUnkown) {
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
			int foundText = 0;
			int foundNumerical = 0;
			int foundUnkown = 0;
			for(ResponseDeclaration responseDeclaration:item.getResponseDeclarations()) {
				if(responseDeclaration.hasBaseType(BaseType.STRING)) {
					foundText++;
				} else if(responseDeclaration.hasBaseType(BaseType.FLOAT)) {
					foundNumerical++;
				} else {
					foundUnkown++;
				}
			}
		
			if(foundText == 0 && foundNumerical > 0 && foundUnkown == 0) {
				return QTI21QuestionType.numerical;
			}
			if(foundText > 0 && foundUnkown == 0) {
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
