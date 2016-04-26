package org.olat.ims.qti21.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.core.util.StringHelper;

import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.CorrectResponse;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.ChoiceInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.Interaction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.MatchInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.response.declaration.MapEntry;
import uk.ac.ed.ph.jqtiplus.node.item.response.declaration.ResponseDeclaration;
import uk.ac.ed.ph.jqtiplus.node.shared.FieldValue;
import uk.ac.ed.ph.jqtiplus.types.Identifier;
import uk.ac.ed.ph.jqtiplus.value.Cardinality;
import uk.ac.ed.ph.jqtiplus.value.DirectedPairValue;
import uk.ac.ed.ph.jqtiplus.value.IdentifierValue;
import uk.ac.ed.ph.jqtiplus.value.MultipleValue;
import uk.ac.ed.ph.jqtiplus.value.SingleValue;
import uk.ac.ed.ph.jqtiplus.value.StringValue;
import uk.ac.ed.ph.jqtiplus.value.Value;

/**
 * This is a set of methods to analyse the stringuified responses
 * save in the database.
 * 
 * Initial date: 26.04.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CorrectResponsesUtil {
	
	/**
	 * Remove the leading and trailing [ ] if exists.
	 * @param stringuifiedResponses
	 * @return
	 */
	public static final String stripResponse(String stringuifiedResponses) {
		if(stringuifiedResponses != null) {
			if(stringuifiedResponses.startsWith("[")) {
				stringuifiedResponses = stringuifiedResponses.substring(1, stringuifiedResponses.length());
			}
			if(stringuifiedResponses.endsWith("]")) {
				stringuifiedResponses = stringuifiedResponses.substring(0, stringuifiedResponses.length() - 1);
			}
		}
		return stringuifiedResponses;
	}
	
	/**
	 * Calculate the list of correct responses found in the response of the assessed user.
	 * 
	 * @param item
	 * @param choiceInteraction
	 * @param stringuifiedResponse
	 * @return
	 */
	public static final List<Identifier> getCorrectAnsweredResponses(AssessmentItem assessmentItem, ChoiceInteraction choiceInteraction, String stringuifiedResponse) {
		List<Identifier> correctAnsweredResponses;
		if(StringHelper.containsNonWhitespace(stringuifiedResponse)) {
			List<Identifier> correctResponses = getCorrectIdentifierResponses(assessmentItem, choiceInteraction);
			correctAnsweredResponses = new ArrayList<>(correctResponses.size());
			for(Identifier correctResponse:correctResponses) {
				String correctIdentifier = correctResponse.toString();
				boolean correct = stringuifiedResponse.contains("[" + correctIdentifier + "]");
				if(correct) {
					correctAnsweredResponses.add(correctResponse);
				}
			}
		} else {
			correctAnsweredResponses = Collections.emptyList();
		}
		return correctAnsweredResponses;
	}
	
	/**
	 * Search the correct responses defined in response declaration of type Identifier.
	 * 
	 * @param assessmentItem
	 * @param interaction
	 * @return
	 */
	public static final List<Identifier> getCorrectIdentifierResponses(AssessmentItem assessmentItem, Interaction interaction) {
		List<Identifier> correctAnswers = new ArrayList<>(5);
		
		ResponseDeclaration responseDeclaration = assessmentItem.getResponseDeclaration(interaction.getResponseIdentifier());
		if(responseDeclaration != null && responseDeclaration.getCorrectResponse() != null) {
			CorrectResponse correctResponse = responseDeclaration.getCorrectResponse();
			if(correctResponse.getCardinality().isOneOf(Cardinality.SINGLE)) {
				List<FieldValue> values = correctResponse.getFieldValues();
				Value value = FieldValue.computeValue(Cardinality.SINGLE, values);
				if(value instanceof IdentifierValue) {
					IdentifierValue identifierValue = (IdentifierValue)value;
					Identifier correctAnswer = identifierValue.identifierValue();
					correctAnswers.add(correctAnswer);
				}
				
			} else if(correctResponse.getCardinality().isOneOf(Cardinality.MULTIPLE)) {
				Value value = FieldValue.computeValue(Cardinality.MULTIPLE, correctResponse.getFieldValues());
				if(value instanceof MultipleValue) {
					MultipleValue multiValue = (MultipleValue)value;
					for(SingleValue sValue:multiValue.getAll()) {
						if(sValue instanceof IdentifierValue) {
							IdentifierValue identifierValue = (IdentifierValue)sValue;
							Identifier correctAnswer = identifierValue.identifierValue();
							correctAnswers.add(correctAnswer);
						}
					}
				}
			}
		}
		
		return correctAnswers;
	}
	
	/**
	 * The list of correct associations
	 * @param assessmentItem
	 * @param interaction
	 * @return A list of string with [ and ] before and after!
	 */
	public static final Set<String> getCorrectKPrimResponses(AssessmentItem assessmentItem, MatchInteraction interaction) {
		ResponseDeclaration responseDeclaration = assessmentItem.getResponseDeclaration(interaction.getResponseIdentifier());
		
		//readable responses
		Set<String> rightResponses = new HashSet<>();
		List<MapEntry> mapEntries = responseDeclaration.getMapping().getMapEntries();
		for(MapEntry mapEntry:mapEntries) {
			SingleValue mapKey = mapEntry.getMapKey();
			if(mapKey instanceof DirectedPairValue) {
				DirectedPairValue pairValue = (DirectedPairValue)mapKey;
				String source = pairValue.sourceValue().toString();
				String destination = pairValue.destValue().toString();
				rightResponses.add("[" + source + " " + destination + "]");
			}
		}
		
		return rightResponses;
	}

	
	public static final TextEntry getCorrectTextResponses(AssessmentItem assessmentItem, Interaction interaction) {
		ResponseDeclaration responseDeclaration = assessmentItem.getResponseDeclaration(interaction.getResponseIdentifier());
		
		boolean caseSensitive = true;
		List<String> alternatives = new ArrayList<>();
		List<MapEntry> mapEntries = responseDeclaration.getMapping().getMapEntries();
		for(MapEntry mapEntry:mapEntries) {
			SingleValue mapKey = mapEntry.getMapKey();
			if(mapKey instanceof StringValue) {
				String value = ((StringValue)mapKey).stringValue();
				alternatives.add(value);
			}
			
			caseSensitive &= mapEntry.getCaseSensitive();
		}
		return new TextEntry(alternatives, caseSensitive);
	}
	
	public static class TextEntry {
		
		private boolean caseSensitive;
		private List<String> alternatives;
		
		public TextEntry(List<String> alternatives, boolean caseSensitive) {
			this.alternatives = alternatives;
			this.caseSensitive = caseSensitive;
		}
		
		public boolean isCaseSensitive() {
			return caseSensitive;
		}

		public List<String> getAlternatives() {
			return alternatives;
		}
		
		public boolean isCorrect(String response) {
			for(String alternative:alternatives) {
				if(caseSensitive) {
					if(alternative.equals(response)) {
						return true;
					}
				} else if(alternative.equalsIgnoreCase(response)) {
					return true;
				}
			}
			return false;
		}
	}
}
