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

import org.olat.core.util.StringHelper;
import org.olat.ims.qti21.QTI21Constants;
import org.olat.modules.qpool.QuestionType;

import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.ChoiceInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.DrawingInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.EndAttemptInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.ExtendedTextInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.HotspotInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.HottextInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.InlineChoiceInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.Interaction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.MatchInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.OrderInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.TextEntryInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.UploadInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.response.declaration.ResponseDeclaration;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;
import uk.ac.ed.ph.jqtiplus.value.BaseType;
import uk.ac.ed.ph.jqtiplus.value.Cardinality;

/**
 * 
 * Initial date: 16.02.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public enum QTI21QuestionType {
	sc(true, "sc", "o_mi_qtisc", QuestionType.SC),
	mc(true, "mc", "o_mi_qtimc", QuestionType.MC),
	kprim(true, "kprim", "o_mi_qtikprim", QuestionType.KPRIM),
	match(true, "match", "o_mi_qtimatch", QuestionType.MATCH),
	matchdraganddrop(true, "matchdraganddrop", "o_mi_qtimatch_draganddrop", QuestionType.MATCHDRAGANDDROP),
	matchtruefalse(true, "matchtruefalse", "o_mi_qtimatch_truefalse", QuestionType.MATCHTRUEFALSE),
	fib(true, "fib", "o_mi_qtifib", QuestionType.FIB),
	numerical(true, "numerical", "o_mi_qtinumerical", QuestionType.NUMERICAL),
	hotspot(true, "hotspot", "o_mi_qtihotspot", QuestionType.HOTSPOT),
	essay(true, "essay", "o_mi_qtiessay", QuestionType.ESSAY),
	upload(true, "upload", "o_mi_qtiupload", QuestionType.UPLOAD),
	drawing(true, "drawing", "o_mi_qtidrawing", QuestionType.DRAWING),
	hottext(true, "hottext", "o_mi_qtihottext", QuestionType.HOTTEXT),
	order(true, "order", "o_mi_qtiorder", QuestionType.ORDER),
	inlinechoice(true, "inlinechoice", "o_mi_qtiinlinechoice", QuestionType.INLINECHOICE),
	unkown(false, "unkown", "o_mi_qtiunkown", null);
	
	private final String prefix;
	private final boolean editor;
	private final String cssClass;
	private final QuestionType poolQuestionType;
	
	private QTI21QuestionType(boolean editor, String prefix, String cssClass, QuestionType poolQuestionType) {
		this.editor = editor;
		this.prefix = prefix;
		this.cssClass = cssClass;
		this.poolQuestionType = poolQuestionType;
	}
	
	public boolean hasEditor() {
		return editor;
	}
	
	public String getPrefix() {
		return prefix;
	}
	
	public String getCssClass() {
		return cssClass;
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
	
	public static QTI21QuestionType getType(ResolvedAssessmentItem resolvedAssessmentItem) {
		AssessmentItem item = resolvedAssessmentItem.getItemLookup().extractAssumingSuccessful();
		if(item != null) {
			return getType(item);
		}
		return QTI21QuestionType.unkown;
	}
	
	public static QTI21QuestionType getType(AssessmentItem item) {
		if(QTI21Constants.TOOLNAME.equals(item.getToolName())) {
			return getTypeRelax(item);
		}
		return QTI21QuestionType.unkown;
	}
	
	public static QTI21QuestionType getTypeRelax(AssessmentItem item) {
		if(item == null) return QTI21QuestionType.unkown;
		
		//we have create this one
		List<Interaction> interactions = item.getItemBody().findInteractions();
		
		boolean fChoice = false;
		boolean fMatch = false;
		boolean fTextEntry = false;
		boolean fEssay = false;
		boolean fHotspot = false;
		boolean fUpload = false;
		boolean fDrawing = false;
		boolean fHottext = false;
		boolean fOrder = false;
		boolean fInlineChoice = false;
		boolean fUnkown = false;

		if(interactions != null && !interactions.isEmpty()) {
			for(Interaction interaction: interactions) {
				if(interaction instanceof ChoiceInteraction) {
					fChoice = true;
				} else if(interaction instanceof MatchInteraction) {
					fMatch = true;
				} else if(interaction instanceof ExtendedTextInteraction) {
					fEssay = true;
				} else if(interaction instanceof UploadInteraction) {
					fUpload = true;
				}  else if(interaction instanceof DrawingInteraction) {
					fDrawing = true;
				} else if(interaction instanceof TextEntryInteraction) {
					fTextEntry = true;
				} else if(interaction instanceof HotspotInteraction) {
					fHotspot = true;
				} else if(interaction instanceof HottextInteraction) {
					fHottext = true;
				} else if(interaction instanceof OrderInteraction) {
					fOrder = true;
				} else if(interaction instanceof InlineChoiceInteraction) {
					fInlineChoice = true;
				} else if(interaction instanceof EndAttemptInteraction) {
					//ignore
				}   else {
					fUnkown = true;
				}	
			}	
		}
		
		if(fUnkown) {
			return QTI21QuestionType.unkown;
		} else if(fChoice && !fMatch && !fTextEntry && !fEssay && !fUpload && !fDrawing && !fHotspot && !fHottext && !fOrder && !fInlineChoice && !fUnkown) {
			return getTypeOfChoice(item, interactions);
		} else if(!fChoice && fMatch && !fTextEntry && !fEssay && !fUpload && !fDrawing && !fHotspot && !fHottext && !fOrder && !fInlineChoice && !fUnkown) {
			return getTypeOfMatch(item, interactions);
		} else if(!fChoice && !fMatch && fTextEntry && !fEssay && !fUpload && !fDrawing && !fHotspot && !fHottext && !fOrder && !fInlineChoice && !fUnkown) {
			return getTypeOfTextEntryInteraction(item);
		} else if(!fChoice && !fMatch && !fTextEntry && fEssay && !fUpload && !fDrawing && !fHotspot && !fHottext && !fOrder && !fInlineChoice && !fUnkown) {
			return QTI21QuestionType.essay;
		} else if(!fChoice && !fMatch && !fTextEntry && !fEssay && fUpload && !fDrawing && !fHotspot && !fHottext && !fOrder && !fInlineChoice && !fUnkown) {
			return QTI21QuestionType.upload;
		} else if(!fChoice && !fMatch && !fTextEntry && !fEssay && !fUpload && fDrawing && !fHotspot && !fHottext && !fOrder && !fInlineChoice && !fUnkown) {
			return QTI21QuestionType.drawing;
		} else if(!fChoice && !fMatch && !fTextEntry && !fEssay && !fUpload && !fDrawing && fHotspot && !fHottext && !fOrder && !fInlineChoice && !fUnkown) {
			return QTI21QuestionType.hotspot;
		} else if(!fChoice && !fMatch && !fTextEntry && !fEssay && !fUpload && !fDrawing && !fHotspot && fHottext && !fOrder && !fInlineChoice && !fUnkown) {
			return QTI21QuestionType.hottext;
		} else if(!fChoice && !fMatch && !fTextEntry && !fEssay && !fUpload && !fDrawing && !fHotspot && !fHottext && fOrder && !fInlineChoice && !fUnkown) {
			return QTI21QuestionType.order;
		} else if(!fChoice && !fMatch && !fTextEntry && !fEssay && !fUpload && !fDrawing && !fHotspot && !fHottext && !fOrder && fInlineChoice && !fUnkown) {
			return QTI21QuestionType.inlinechoice;
		} else {
			return QTI21QuestionType.unkown;
		}
	}
	
	private static final QTI21QuestionType getTypeOfTextEntryInteraction(AssessmentItem item) {
		if(!item.getResponseDeclarations().isEmpty()) {
			int foundText = 0;
			int foundNumerical = 0;
			int foundUnkown = 0;
			for(ResponseDeclaration responseDeclaration:item.getResponseDeclarations()) {
				if(responseDeclaration.hasBaseType(BaseType.STRING)) {
					foundText++;
				} else if(responseDeclaration.hasBaseType(BaseType.FLOAT)) {
					foundNumerical++;
				} else if(!responseDeclaration.getIdentifier().equals(QTI21Constants.HINT_REQUEST_IDENTIFIER)) {
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
	
	public static final QTI21QuestionType getTypeOfMatch(AssessmentItem item, List<Interaction> interactions) {
		Interaction interaction = interactions.get(0);
		return getTypeOfMatch(item, interaction);
	}

	public static final QTI21QuestionType getTypeOfMatch(AssessmentItem item, Interaction interaction) {
		if(item.getResponseDeclaration(interaction.getResponseIdentifier()) != null) {
			ResponseDeclaration responseDeclaration = item.getResponseDeclaration(interaction.getResponseIdentifier());
			String responseIdentifier = responseDeclaration.getIdentifier().toString();
			Cardinality cardinalty = responseDeclaration.getCardinality();
			if(hasClass(interaction, QTI21Constants.CSS_MATCH_DRAG_AND_DROP)) {
				return QTI21QuestionType.matchdraganddrop;
			} else if(hasClass(interaction, QTI21Constants.CSS_MATCH_TRUE_FALSE)) {
				return QTI21QuestionType.matchtruefalse;
			} else if(cardinalty.isMultiple()) {
				if(hasClass(interaction, QTI21Constants.CSS_MATCH_KPRIM)) {
					return QTI21QuestionType.kprim;
				} else if(responseIdentifier.startsWith("KPRIM_")) {
					return QTI21QuestionType.kprim;
				} else {
					return QTI21QuestionType.match;
				}
			} else {
				return QTI21QuestionType.match;
			}
		} else {
			return QTI21QuestionType.unkown;
		}
	}
	
	private static final QTI21QuestionType getTypeOfChoice(AssessmentItem item, List<Interaction> interactions) {
		Interaction interaction = interactions.get(0);
		if(item.getResponseDeclaration(interaction.getResponseIdentifier()) != null) {
			ResponseDeclaration responseDeclaration = item.getResponseDeclaration(interaction.getResponseIdentifier());
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
	
	public static final boolean hasClass(Interaction interaction, String cssClass) {
		if(interaction == null || cssClass == null) return false;
		
		List<String> cssClasses = interaction.getClassAttr();
		return cssClasses != null && !cssClasses.isEmpty() && cssClasses.contains(cssClass);
	}
	
	/**
	 * 
	 * @param val The value to identify
	 * @return The question type if recognize or unknown
	 */
	public static final QTI21QuestionType safeValueOf(String val) {
		if(StringHelper.containsNonWhitespace(val)) {
			for(QTI21QuestionType type:values()) {
				if(type.name().equals(val)) {
					return type;
				}
			}
		}
		return QTI21QuestionType.unkown;
	}
}
