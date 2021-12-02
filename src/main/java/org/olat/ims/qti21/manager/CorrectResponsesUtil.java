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
package org.olat.ims.qti21.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.DoubleAdder;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.ims.qti21.model.xml.interactions.FIBAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.FIBAssessmentItemBuilder.AbstractEntry;
import org.olat.ims.qti21.model.xml.interactions.FIBAssessmentItemBuilder.NumericalEntry;
import org.olat.ims.qti21.model.xml.interactions.FIBAssessmentItemBuilder.TextEntry;

import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.CorrectResponse;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.ChoiceInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.Interaction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.OrderInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.TextEntryInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.Choice;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.SimpleChoice;
import uk.ac.ed.ph.jqtiplus.node.item.response.declaration.MapEntry;
import uk.ac.ed.ph.jqtiplus.node.item.response.declaration.Mapping;
import uk.ac.ed.ph.jqtiplus.node.item.response.declaration.ResponseDeclaration;
import uk.ac.ed.ph.jqtiplus.node.shared.FieldValue;
import uk.ac.ed.ph.jqtiplus.types.Identifier;
import uk.ac.ed.ph.jqtiplus.value.AbstractPairValue;
import uk.ac.ed.ph.jqtiplus.value.BaseType;
import uk.ac.ed.ph.jqtiplus.value.Cardinality;
import uk.ac.ed.ph.jqtiplus.value.DirectedPairValue;
import uk.ac.ed.ph.jqtiplus.value.IdentifierValue;
import uk.ac.ed.ph.jqtiplus.value.IntegerValue;
import uk.ac.ed.ph.jqtiplus.value.MultipleValue;
import uk.ac.ed.ph.jqtiplus.value.OrderedValue;
import uk.ac.ed.ph.jqtiplus.value.PairValue;
import uk.ac.ed.ph.jqtiplus.value.PointValue;
import uk.ac.ed.ph.jqtiplus.value.SingleValue;
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
	
	private static final Logger log = Tracing.createLoggerFor(CorrectResponsesUtil.class);
	
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
	 * Parse response in the form [34 45][test] and return "23 45", test.
	 * @return
	 */
	public static final List<String> parseResponses(String stringuifiedResponse) {
		List<String> responses = new ArrayList<>();
		if(StringHelper.containsNonWhitespace(stringuifiedResponse)) {
			StringBuilder sb = new StringBuilder();
			int numOfChars = stringuifiedResponse.length();
			for(int i=0;i<numOfChars; i++) {
				char ch = stringuifiedResponse.charAt(i);
				if(ch == '[') {
					sb = new StringBuilder();
				} else if(ch == ']') {
					responses.add(sb.toString());
				} else {
					sb.append(ch);
				}
			}
		}
		return responses;
	}
	
	/**
	 * The method ignore the wrong formatted coordinates
	 * @param responses
	 * @return
	 */
	public static final List<PointValue> parseResponses(List<String> responses) {
		List<PointValue> points = new ArrayList<>();
		for(String response:responses) {
			if(StringHelper.containsNonWhitespace(response)) {
				try {
					PointValue pointValue = PointValue.parseString(response);
					points.add(pointValue);
				} catch (Exception e) {
					log.error("", e);
				}
			}
		}
		return points;
	}
	
	public static final int[] convertCoordinates(final List<Integer> coords) {
        final int[] result = new int[coords.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = coords.get(i).intValue();
        }

        return result;
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
		return getCorrectIdentifierResponses(assessmentItem, interaction.getResponseIdentifier());
	}
	
	public static final List<Identifier> getCorrectIdentifierResponses(AssessmentItem assessmentItem, Identifier responseIdentifier) {
		List<Identifier> correctAnswers = new ArrayList<>(5);
		ResponseDeclaration responseDeclaration = assessmentItem.getResponseDeclaration(responseIdentifier);
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
	
	public static final List<Integer> getCorrectIntegerResponses(AssessmentItem assessmentItem, Interaction interaction) {
		List<Integer> correctAnswers = new ArrayList<>(5);
		
		ResponseDeclaration responseDeclaration = assessmentItem.getResponseDeclaration(interaction.getResponseIdentifier());
		if(responseDeclaration != null && responseDeclaration.getCorrectResponse() != null) {
			CorrectResponse correctResponse = responseDeclaration.getCorrectResponse();
			if(correctResponse.getCardinality().isOneOf(Cardinality.SINGLE)) {
				List<FieldValue> values = correctResponse.getFieldValues();
				Value value = FieldValue.computeValue(Cardinality.SINGLE, values);
				if(value instanceof IntegerValue) {
					IntegerValue identifierValue = (IntegerValue)value;
					correctAnswers.add(identifierValue.intValue());
				}
				
			} else if(correctResponse.getCardinality().isOneOf(Cardinality.MULTIPLE)) {
				Value value = FieldValue.computeValue(Cardinality.MULTIPLE, correctResponse.getFieldValues());
				if(value instanceof MultipleValue) {
					MultipleValue multiValue = (MultipleValue)value;
					for(SingleValue sValue:multiValue.getAll()) {
						if(sValue instanceof IntegerValue) {
							IntegerValue identifierValue = (IntegerValue)value;
							correctAnswers.add(identifierValue.intValue());
						}
					}
				}
			}
		}
		
		return correctAnswers;
	}
	
	public static final List<SimpleChoice> getCorrectOrderedChoices(AssessmentItem assessmentItem, OrderInteraction interaction) {
		List<Identifier> orderedIdentifiers = getCorrectOrderedIdentifierResponses(assessmentItem, interaction);
		
		List<SimpleChoice> choices = new ArrayList<>(interaction.getSimpleChoices());
		List<SimpleChoice> orderedChoices = new ArrayList<>(choices.size());
		for(Identifier identifier:orderedIdentifiers) {
			for(SimpleChoice choice:choices) {
				if(identifier.equals(choice.getIdentifier())) {
					orderedChoices.add(choice);
					choices.remove(choice);
					break;
				}
			}
		}
		
		if(!choices.isEmpty()) {
			orderedChoices.addAll(choices);
		}
		return orderedChoices;
	}

	public static final List<Identifier> getCorrectOrderedIdentifierResponses(AssessmentItem assessmentItem, Interaction interaction) {
		List<Identifier> correctAnswers = new ArrayList<>(5);
		
		ResponseDeclaration responseDeclaration = assessmentItem.getResponseDeclaration(interaction.getResponseIdentifier());
		if(responseDeclaration != null && responseDeclaration.getCorrectResponse() != null) {
			CorrectResponse correctResponse = responseDeclaration.getCorrectResponse();
			if(correctResponse.getCardinality().isOneOf(Cardinality.ORDERED)) {
				List<FieldValue> values = correctResponse.getFieldValues();
				Value value = FieldValue.computeValue(Cardinality.ORDERED, values);
				if(value instanceof OrderedValue) {
					OrderedValue multiValue = (OrderedValue)value;
					multiValue.forEach(oValue -> {
						if(oValue instanceof IdentifierValue) {
							IdentifierValue identifierValue = (IdentifierValue)oValue;
							Identifier correctAnswer = identifierValue.identifierValue();
							correctAnswers.add(correctAnswer);
						}
						
					});
				}
			}
		}

		return correctAnswers;
	}
	
	public static final List<String> getCorrectMultiplePairResponses(AssessmentItem assessmentItem, Interaction interaction, boolean withDelimiter) {
		final List<String> correctAnswers = new ArrayList<>(5);
		
		ResponseDeclaration responseDeclaration = assessmentItem.getResponseDeclaration(interaction.getResponseIdentifier());
		if(responseDeclaration != null && responseDeclaration.getCorrectResponse() != null) {
			CorrectResponse correctResponse = responseDeclaration.getCorrectResponse();
			if(correctResponse.getCardinality().isOneOf(Cardinality.MULTIPLE)) {
				List<FieldValue> values = correctResponse.getFieldValues();
				Value value = FieldValue.computeValue(Cardinality.MULTIPLE, values);
				if(value instanceof MultipleValue) {
					MultipleValue multiValue = (MultipleValue)value;
					multiValue.forEach(oValue -> {
						if(oValue instanceof PairValue) {
							PairValue pairValue = (PairValue)oValue;
							String source = pairValue.sourceValue().toString();
							String destination = pairValue.destValue().toString();
							if(withDelimiter) {
								correctAnswers.add("[" + source + " " + destination + "]");
							} else {
								correctAnswers.add(source + " " + destination);
							}
						}
					});
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
	public static final Set<String> getCorrectDirectPairResponses(AssessmentItem assessmentItem, Interaction interaction, boolean withDelimiter) {
		ResponseDeclaration responseDeclaration = assessmentItem.getResponseDeclaration(interaction.getResponseIdentifier());

		Set<String> correctAnswers = new HashSet<>();
		//readable responses
		if(responseDeclaration != null && responseDeclaration.getCorrectResponse() != null) {
			CorrectResponse correctResponse = responseDeclaration.getCorrectResponse();
			if(correctResponse.getCardinality().isOneOf(Cardinality.MULTIPLE)) {
				List<FieldValue> values = correctResponse.getFieldValues();
				Value value = FieldValue.computeValue(Cardinality.MULTIPLE, values);
				if(value instanceof MultipleValue) {
					MultipleValue multiValue = (MultipleValue)value;
					multiValue.forEach(oValue -> {
						if(oValue instanceof DirectedPairValue) {
							DirectedPairValue pairValue = (DirectedPairValue)oValue;
							String source = pairValue.sourceValue().toString();
							String destination = pairValue.destValue().toString();
							if(withDelimiter) {
								correctAnswers.add("[" + source + " " + destination + "]");
							} else {
								correctAnswers.add(source + " " + destination);
							}
						}
					});
				}
			} 
		}
		
		return correctAnswers;
	}
	
	public static final AbstractEntry getCorrectTextResponses(AssessmentItem assessmentItem, TextEntryInteraction interaction) {
		ResponseDeclaration responseDeclaration = assessmentItem.getResponseDeclaration(interaction.getResponseIdentifier());
		if(responseDeclaration == null) {
			log.error("Missing declaration for interaction: {} of assessment item: {}", interaction, assessmentItem);
			return null;
		} else if(responseDeclaration.hasBaseType(BaseType.STRING) && responseDeclaration.hasCardinality(Cardinality.SINGLE)) {
			TextEntry textEntry = new TextEntry(interaction);
			FIBAssessmentItemBuilder.extractTextEntrySettingsFromResponseDeclaration(textEntry, responseDeclaration, new AtomicInteger(), new DoubleAdder());
			return textEntry;
		} else if(responseDeclaration.hasBaseType(BaseType.FLOAT) && responseDeclaration.hasCardinality(Cardinality.SINGLE)) {
			NumericalEntry numericalEntry = new NumericalEntry(interaction);
			FIBAssessmentItemBuilder.extractNumericalEntrySettings(assessmentItem, numericalEntry, responseDeclaration, new AtomicInteger(), new DoubleAdder());
			return numericalEntry;
		}
		return null;
	}
	
	public static final Double getMappedValue(AssessmentItem assessmentItem, Interaction interaction, Choice choice) {
		ResponseDeclaration responseDeclaration = assessmentItem.getResponseDeclaration(interaction.getResponseIdentifier());
		if(responseDeclaration != null && responseDeclaration.getMapping() != null) {
			Mapping mapping = responseDeclaration.getMapping();
			if(mapping != null && mapping.getMapEntries() != null) {
				for(MapEntry entry:mapping.getMapEntries()) {
					SingleValue sValue = entry.getMapKey();
					if(sValue instanceof IdentifierValue) {
						Identifier identifier = ((IdentifierValue)sValue).identifierValue();
						if(identifier.equals(choice.getIdentifier())) {
							return entry.getMappedValue();
						}
					}
				}
			}
		}
		return null;
	}
	
	public static final Double getMappedValue(AssessmentItem assessmentItem, Interaction interaction, Identifier source, Identifier destination) {
		ResponseDeclaration responseDeclaration = assessmentItem.getResponseDeclaration(interaction.getResponseIdentifier());
		if(responseDeclaration != null && responseDeclaration.getMapping() != null) {
			Mapping mapping = responseDeclaration.getMapping();
			if(mapping != null && mapping.getMapEntries() != null) {
				for(MapEntry entry:mapping.getMapEntries()) {
					SingleValue sValue = entry.getMapKey();
					if(sValue instanceof AbstractPairValue) {
						Identifier sourceIdentifier = ((AbstractPairValue)sValue).sourceValue();
						Identifier destIdentifier = ((AbstractPairValue)sValue).destValue();
						if(source.equals(sourceIdentifier) && destination.equals(destIdentifier)) {
							return Double.valueOf(entry.getMappedValue());
						}
					}
				}
			}
		}
		return null;
	}
}
