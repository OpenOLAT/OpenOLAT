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
package org.olat.ims.qti21.model.xml;

import static org.olat.ims.qti21.QTI21Constants.MAXSCORE_IDENTIFIER;
import static org.olat.ims.qti21.QTI21Constants.MINSCORE_IDENTIFIER;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.olat.ims.qti21.QTI21Constants;

import uk.ac.ed.ph.jqtiplus.attribute.Attribute;
import uk.ac.ed.ph.jqtiplus.attribute.AttributeList;
import uk.ac.ed.ph.jqtiplus.node.AbstractNode;
import uk.ac.ed.ph.jqtiplus.node.expression.Expression;
import uk.ac.ed.ph.jqtiplus.node.expression.general.BaseValue;
import uk.ac.ed.ph.jqtiplus.node.expression.general.Variable;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.Lt;
import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.CorrectResponse;
import uk.ac.ed.ph.jqtiplus.node.outcome.declaration.OutcomeDeclaration;
import uk.ac.ed.ph.jqtiplus.node.shared.BaseTypeAndCardinality;
import uk.ac.ed.ph.jqtiplus.node.shared.FieldValue;
import uk.ac.ed.ph.jqtiplus.node.shared.declaration.DefaultValue;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentTest;
import uk.ac.ed.ph.jqtiplus.node.test.outcome.processing.OutcomeCondition;
import uk.ac.ed.ph.jqtiplus.node.test.outcome.processing.OutcomeConditionChild;
import uk.ac.ed.ph.jqtiplus.node.test.outcome.processing.OutcomeConditionExpressionChild;
import uk.ac.ed.ph.jqtiplus.node.test.outcome.processing.OutcomeIf;
import uk.ac.ed.ph.jqtiplus.node.test.outcome.processing.OutcomeRule;
import uk.ac.ed.ph.jqtiplus.node.test.outcome.processing.SetOutcomeValue;
import uk.ac.ed.ph.jqtiplus.types.Identifier;
import uk.ac.ed.ph.jqtiplus.value.Cardinality;
import uk.ac.ed.ph.jqtiplus.value.DirectedPairValue;
import uk.ac.ed.ph.jqtiplus.value.FloatValue;
import uk.ac.ed.ph.jqtiplus.value.IdentifierValue;
import uk.ac.ed.ph.jqtiplus.value.MultipleValue;
import uk.ac.ed.ph.jqtiplus.value.SingleValue;
import uk.ac.ed.ph.jqtiplus.value.Value;

/**
 * 
 * This is an helper class which extract data from the java model of QtiWorks.
 * 
 * Initial date: 05.08.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface QtiNodesExtractor {
	
	public static Double extractMaxScore(AssessmentItem assessmentItem) {
		return getOutcomeDeclarationDefaultFloatValue(assessmentItem.getOutcomeDeclaration(MAXSCORE_IDENTIFIER));
	}
	
	public static Double extractMinScore(AssessmentItem assessmentItem) {
		return getOutcomeDeclarationDefaultFloatValue(assessmentItem.getOutcomeDeclaration(MINSCORE_IDENTIFIER));
	}
	
	public static Double extractMaxScore(AssessmentTest assessmentTest) {
		return getOutcomeDeclarationDefaultFloatValue(assessmentTest.getOutcomeDeclaration(MAXSCORE_IDENTIFIER));
	}
	
	public static Double extractMinScore(AssessmentTest assessmentTest) {
		return getOutcomeDeclarationDefaultFloatValue(assessmentTest.getOutcomeDeclaration(MINSCORE_IDENTIFIER));
	}
	
	public static Double getOutcomeDeclarationDefaultFloatValue(OutcomeDeclaration outcomeDeclaration) {
		Double doubleValue = null;
		if(outcomeDeclaration != null) {
			DefaultValue defaultValue = outcomeDeclaration.getDefaultValue();
			if(defaultValue != null) {
				Value evaluatedValue = defaultValue.evaluate();
				if(evaluatedValue instanceof FloatValue) {
					doubleValue = Double.valueOf(((FloatValue)evaluatedValue).doubleValue());
				}
			}
		}
		return doubleValue;
	}
	
	public static Identifier getIdentifierFromCorrectResponse(CorrectResponse correctResponse) {
		BaseTypeAndCardinality responseDeclaration = correctResponse.getParent();
		if(responseDeclaration.hasCardinality(Cardinality.SINGLE)) {
			Value value = FieldValue.computeValue(Cardinality.SINGLE, correctResponse.getFieldValues());
			if(value instanceof IdentifierValue) {
				IdentifierValue identifierValue = (IdentifierValue)value;
				return identifierValue.identifierValue();
			}
		}
		return null;
	}
	
	public static void extractIdentifiersFromCorrectResponse(CorrectResponse correctResponse, List<Identifier> correctAnswers) {
		BaseTypeAndCardinality responseDeclaration = correctResponse.getParent();
		if(responseDeclaration.hasCardinality(Cardinality.MULTIPLE)) {
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
		} else if(responseDeclaration.hasCardinality(Cardinality.SINGLE)) {
			Value value = FieldValue.computeValue(Cardinality.SINGLE, correctResponse.getFieldValues());
			if(value instanceof IdentifierValue) {
				IdentifierValue identifierValue = (IdentifierValue)value;
				correctAnswers.add(identifierValue.identifierValue());
			}
		}
	}
	
	public static void extractIdentifiersFromCorrectResponse(CorrectResponse correctResponse, Map<Identifier,List<Identifier>> correctAnswers) {
		if(correctResponse != null) {
			List<FieldValue> values = correctResponse.getFieldValues();
			for(FieldValue value:values) {
				SingleValue sValue = value.getSingleValue();
				if(sValue instanceof DirectedPairValue) {
					DirectedPairValue dpValue = (DirectedPairValue)sValue;
					Identifier sourceId = dpValue.sourceValue();
					Identifier targetId = dpValue.destValue();
					List<Identifier> targetIds = correctAnswers.get(sourceId);
					if(targetIds == null) {
						targetIds = new ArrayList<>();
						correctAnswers.put(sourceId, targetIds);
					}
					targetIds.add(targetId);
				}
			}
		}
	}
	
	public static Double extractCutValue(AssessmentTest assessmentTest) {
		Double cutValue = null;
		if(assessmentTest.getOutcomeProcessing() != null) {
			List<OutcomeRule> outcomeRules = assessmentTest.getOutcomeProcessing().getOutcomeRules();
			for(OutcomeRule outcomeRule:outcomeRules) {
				if(outcomeRule instanceof OutcomeCondition) {
					OutcomeCondition outcomeCondition = (OutcomeCondition)outcomeRule;
					boolean findIf = findSetOutcomeValue(outcomeCondition.getOutcomeIf(), QTI21Constants.PASS_IDENTIFIER);
					boolean findElse = findSetOutcomeValue(outcomeCondition.getOutcomeElse(), QTI21Constants.PASS_IDENTIFIER);
					if(findIf && findElse) {
						cutValue = extractCutValue(outcomeCondition.getOutcomeIf());
					}
				}
			}
		}
		return cutValue;
	}
	
	public static boolean findSetOutcomeValue(OutcomeConditionChild outcomeConditionChild, Identifier identifier) {
		if(outcomeConditionChild == null
				|| outcomeConditionChild.getOutcomeRules() == null
				|| outcomeConditionChild.getOutcomeRules().isEmpty()) return false;
		
		List<OutcomeRule> outcomeRules = outcomeConditionChild.getOutcomeRules();
		for(OutcomeRule outcomeRule:outcomeRules) {
			SetOutcomeValue setOutcomeValue = (SetOutcomeValue)outcomeRule;
			if(identifier.equals(setOutcomeValue.getIdentifier())) {
				return true;
			}
		}
		
		return false;
	}
	
	public static boolean findLtValue(OutcomeConditionExpressionChild outcomeIfElse, Identifier identifier) {
		if(outcomeIfElse == null
				|| outcomeIfElse.getOutcomeRules() == null
				|| outcomeIfElse.getOutcomeRules().isEmpty()) return false;
		
		List<Expression> expressions = outcomeIfElse.getExpressions();
		for(Expression expression:expressions) {
			if(expression instanceof Lt) {
				Lt lt = (Lt)expression;
				for(Expression ltExpression:lt.getExpressions()) {
					if(ltExpression instanceof Variable) {
						Variable variable = (Variable)ltExpression;
						if(identifier.toString().equals(variable.getIdentifier().toString())) {
							return true;
						}
					}
				}
			}
			
		}
		
		return false;
	}
	
	public static Double extractCutValue(OutcomeIf outcomeIf) {
		if(outcomeIf != null && !outcomeIf.getExpressions().isEmpty()) {
			Expression gte = outcomeIf.getExpressions().get(0);
			if(gte.getExpressions().size() > 1) {
				Expression baseValue = gte.getExpressions().get(1);
				if(baseValue instanceof BaseValue) {
					BaseValue value = (BaseValue)baseValue;
					if(value.getSingleValue() instanceof FloatValue) {
						return ((FloatValue)value.getSingleValue()).doubleValue();
					}
				}
			}
		}
		return null;
	}
	
	public static String extractId(AbstractNode node) {
		AttributeList attributes = node.getAttributes();
		if(attributes != null) {
			for(int i=attributes.size(); i-->0; ) {
				Attribute<?> attr = attributes.get(i);
				if("id".equals(attr.getLocalName())) {
					Object val = attr.getValue();
					if(val instanceof String) {
						return (String)val;
					}
				}
			}
		}
		return null;
	}
}
