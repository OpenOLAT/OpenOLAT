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

import static org.olat.ims.qti21.model.xml.AssessmentItemFactory.findBaseValueInExpression;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.olat.ims.qti21.QTI21Constants;
import org.olat.ims.qti21.model.IdentifierGenerator;

import uk.ac.ed.ph.jqtiplus.attribute.value.StringAttribute;
import uk.ac.ed.ph.jqtiplus.node.content.basic.FlowStatic;
import uk.ac.ed.ph.jqtiplus.node.expression.Expression;
import uk.ac.ed.ph.jqtiplus.node.expression.general.BaseValue;
import uk.ac.ed.ph.jqtiplus.node.expression.general.Variable;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.And;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.Equal;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.Gt;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.Gte;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.Lt;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.Lte;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.Match;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.Member;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.Not;
import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.ModalFeedback;
import uk.ac.ed.ph.jqtiplus.node.item.response.processing.ResponseCondition;
import uk.ac.ed.ph.jqtiplus.node.item.response.processing.ResponseIf;
import uk.ac.ed.ph.jqtiplus.node.item.response.processing.ResponseRule;
import uk.ac.ed.ph.jqtiplus.node.item.response.processing.SetOutcomeValue;
import uk.ac.ed.ph.jqtiplus.types.ComplexReferenceIdentifier;
import uk.ac.ed.ph.jqtiplus.types.Identifier;
import uk.ac.ed.ph.jqtiplus.value.BaseType;
import uk.ac.ed.ph.jqtiplus.value.FloatValue;
import uk.ac.ed.ph.jqtiplus.value.IdentifierValue;
import uk.ac.ed.ph.jqtiplus.value.IntegerValue;

/**
 * 
 * Initial date: 09.12.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ModalFeedbackBuilder {
	
	private final ModalFeedback modalFeedback;
	private final AssessmentItem assessmentItem;
	
	private final ModalFeedbackType type;
	
	private String title;
	private String text;
	private Identifier identifier;
	private List<ModalFeedbackCondition> conditions;
	
	public ModalFeedbackBuilder(AssessmentItem assessmentItem, ModalFeedbackType type) {
		this.assessmentItem = assessmentItem;
		this.modalFeedback = null;
		this.type = type;
		identifier = IdentifierGenerator.newNumberAsIdentifier("Feedback");
	}
	
	public ModalFeedbackBuilder(AssessmentItem assessmentItem, ModalFeedback modalFeedback) {
		this.assessmentItem = assessmentItem;
		this.modalFeedback = modalFeedback;
		if(modalFeedback != null) {
			text = new AssessmentHtmlBuilder().flowStaticString(modalFeedback.getFlowStatics());
			StringAttribute titleAttr = modalFeedback.getAttributes().getStringAttribute(ModalFeedback.ATTR_TITLE_NAME);
			title = titleAttr == null ? null : titleAttr.getComputedValue();
			identifier = modalFeedback.getIdentifier();
			type = extract();
		} else {
			identifier = IdentifierGenerator.newNumberAsIdentifier("Feedback");
			type = null;
		}
	}
	
	private ModalFeedbackType extract() {
		if(isCorrectRule()) {
			return ModalFeedbackType.correct;
		}
		if(isIncorrectRule()) {
			return ModalFeedbackType.incorrect;
		}
		if(isEmptyRule()) {
			return ModalFeedbackType.empty;
		}
		if(isAnsweredRule()) {
			return ModalFeedbackType.answered;
		}
		if(isCorrectSolutionRule()) {
			return ModalFeedbackType.correctSolution;
		}
		if(isHint()) {
			return ModalFeedbackType.hint;
		}
		extractConditions();
		if(conditions != null && conditions.size() > 0) {
			return ModalFeedbackType.additional;
		}
		return ModalFeedbackType.unkown;
	}
	
	public void extractConditions() {
		ResponseCondition feedbackRule = findFeedbackResponseCondition(modalFeedback.getIdentifier(), QTI21Constants.FEEDBACKMODAL_IDENTIFIER);
		if(feedbackRule != null) {
			ResponseIf responseIf = feedbackRule.getResponseIf();
			if(responseIf != null && responseIf.getExpressions() != null
					&& responseIf.getExpressions().size() == 1
					&& responseIf.getExpressions().get(0) instanceof And
					&& responseIf.getResponseRules().size() == 1
					&& responseIf.getResponseRules().get(0) instanceof SetOutcomeValue) {
				And and = (And)responseIf.getExpression();
				List<Expression> conditionElements = and.getExpressions();
				List<ModalFeedbackCondition> extractedConditions = new ArrayList<>();
				for(Expression conditionElement:conditionElements) {
					ModalFeedbackCondition condition = extractCondition(conditionElement);
					if(condition != null) {
						extractedConditions.add(condition);
					}
				}
				conditions = extractedConditions;
			}
		}
	}
	
	private ModalFeedbackCondition extractCondition(Expression conditionElement) {
		ModalFeedbackOperatorAndExpressions operatorAndExpressions = extractOperatorCondition(conditionElement);
		if(operatorAndExpressions.getOperator() != null) {
			List<Expression> expressions = operatorAndExpressions.getExpressions();
			ModalFeedbackCondition.Variable variable = extractVariableCondition(expressions);
			String value = extractBaseValueCondition(expressions); 
			
			ModalFeedbackCondition condition = new ModalFeedbackCondition();
			condition.setVariable(variable);
			condition.setValue(value);
			condition.setOperator(operatorAndExpressions.getOperator());
			return condition;
		}
		
		return null;
	}
	
	private String extractBaseValueCondition(List<Expression> expressions) {
		BaseValue bValue = null;
		if(expressions.get(0) instanceof BaseValue) {
			bValue = (BaseValue)expressions.get(0);
		} else if(expressions.get(1) instanceof BaseValue) {
			bValue = (BaseValue)expressions.get(1);
		}
		if(bValue != null) {
			if(bValue.getBaseTypeAttrValue() == BaseType.IDENTIFIER) {
				IdentifierValue val = (IdentifierValue)bValue.getSingleValue();
				return val.identifierValue().toString();
			}
			if(bValue.getBaseTypeAttrValue() == BaseType.INTEGER) {
				IntegerValue val = (IntegerValue)bValue.getSingleValue();
				return Integer.toString(val.intValue());
			}
			if(bValue.getBaseTypeAttrValue() == BaseType.FLOAT) {
				FloatValue val = (FloatValue)bValue.getSingleValue();
				return Double.toString(val.doubleValue());
			}
		}
		
		return null;
	}
	
	private ModalFeedbackCondition.Variable extractVariableCondition(List<Expression> expressions) {
		Variable variable = null;
		if(expressions.get(0) instanceof Variable) {
			variable = (Variable)expressions.get(0);
		} else if(expressions.get(1) instanceof Variable) {
			variable = (Variable)expressions.get(1);
		}
		if(variable != null) {
			ComplexReferenceIdentifier varIdentifier = variable.getIdentifier();
			if(QTI21Constants.SCORE_CLX_IDENTIFIER.equals(varIdentifier)) {
				return ModalFeedbackCondition.Variable.score;
			} else if(QTI21Constants.NUM_ATTEMPTS_CLX_IDENTIFIER.equals(varIdentifier)) {
				return ModalFeedbackCondition.Variable.attempts;
			} else {
				return ModalFeedbackCondition.Variable.response;
			}
		}
		
		return null;
	}
	
	private ModalFeedbackOperatorAndExpressions extractOperatorCondition(Expression conditionElement) {
		ModalFeedbackCondition.Operator operator = null;
		List<Expression> expressions = conditionElement.getExpressions();
		if(conditionElement instanceof Gt) {
			operator =  ModalFeedbackCondition.Operator.bigger;
		} else if(conditionElement instanceof Gte) {
			operator = ModalFeedbackCondition.Operator.biggerEquals;
		} else if(conditionElement instanceof Equal || conditionElement instanceof Match || conditionElement instanceof Member) {
			operator = ModalFeedbackCondition.Operator.equals;
		} else if(conditionElement instanceof Lt) {
			operator = ModalFeedbackCondition.Operator.smaller;
		} else if(conditionElement instanceof Lte) {
			operator = ModalFeedbackCondition.Operator.smallerEquals;
		} else if(conditionElement instanceof Not) {
			if(conditionElement.getExpressions().size() == 1 && (conditionElement.getExpressions().get(0) instanceof Match || conditionElement.getExpressions().get(0) instanceof Member)) {
				operator = ModalFeedbackCondition.Operator.notEquals;
				expressions = conditionElement.getExpressions().get(0).getExpressions();
			}
		}
		return new ModalFeedbackOperatorAndExpressions(operator, expressions);
	}
	
	private static class ModalFeedbackOperatorAndExpressions {
		private final ModalFeedbackCondition.Operator operator;
		private final List<Expression> expressions;
		
		public ModalFeedbackOperatorAndExpressions(ModalFeedbackCondition.Operator operator, List<Expression> expressions) {
			this.operator = operator;
			this.expressions = expressions;
		}

		public ModalFeedbackCondition.Operator getOperator() {
			return operator;
		}

		public List<Expression> getExpressions() {
			return expressions;
		}
	}
	
	public boolean isCorrectRule() {
		ResponseCondition feedbackRule = findFeedbackResponseCondition(modalFeedback.getIdentifier(), QTI21Constants.FEEDBACKMODAL_IDENTIFIER);
		return findBaseValueInExpressionsOfResponseIf(feedbackRule, QTI21Constants.CORRECT_IDENTIFIER);
	}
	
	public boolean isIncorrectRule() {
		ResponseCondition feedbackRule = findFeedbackResponseCondition(modalFeedback.getIdentifier(), QTI21Constants.FEEDBACKMODAL_IDENTIFIER);
		return findBaseValueInExpressionsOfResponseIf(feedbackRule, QTI21Constants.INCORRECT_IDENTIFIER);
	}
	
	public boolean isEmptyRule() {
		ResponseCondition feedbackRule = findFeedbackResponseCondition(modalFeedback.getIdentifier(), QTI21Constants.FEEDBACKMODAL_IDENTIFIER);
		return findBaseValueInExpressionsOfResponseIf(feedbackRule, QTI21Constants.EMPTY_IDENTIFIER);
	}
	
	public boolean isAnsweredRule() {
		ResponseCondition feedbackRule = findFeedbackResponseCondition(modalFeedback.getIdentifier(), QTI21Constants.FEEDBACKMODAL_IDENTIFIER);
		return findBaseValueInExpressionsOfResponseIf(feedbackRule, QTI21Constants.ANSWERED_IDENTIFIER);
	}
	
	public boolean isCorrectSolutionRule() {
		ResponseCondition feedbackRule = findFeedbackResponseCondition(modalFeedback.getIdentifier(), QTI21Constants.CORRECT_SOLUTION_IDENTIFIER);
		boolean allOk = findBaseValueInExpressionsOfResponseIf(feedbackRule, QTI21Constants.INCORRECT_IDENTIFIER);
		allOk |= modalFeedback.getOutcomeIdentifier() != null
					&& QTI21Constants.CORRECT_SOLUTION_IDENTIFIER.equals(modalFeedback.getOutcomeIdentifier());
		return allOk;
	}
	
	public boolean isHint() {
		return modalFeedback.getIdentifier().equals(QTI21Constants.HINT_IDENTIFIER)
				&& modalFeedback.getOutcomeIdentifier().equals(QTI21Constants.HINT_FEEDBACKMODAL_IDENTIFIER);
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
	/**
	 * @return A copy of the list of elements or an empty list. The list is mean as read only.
	 */
	public List<FlowStatic> getTextFlowStatic() {
		return modalFeedback == null ? Collections.emptyList() : new ArrayList<>(modalFeedback.getFlowStatics());
	}
	
	public Identifier getIdentifier() {
		return identifier;
	}

	public void setIdentifier(Identifier identifier) {
		this.identifier = identifier;
	}
	
	public ModalFeedbackType getType() {
		return type;
	}
	
	public Identifier getModalFeedbackIdentifier() {
		return modalFeedback.getIdentifier();
	}
	
	public List<ModalFeedbackCondition> getFeedbackConditons() {
		return conditions;
	}
	
	public void setFeedbackConditions(List<ModalFeedbackCondition> conditions) {
		this.conditions = new ArrayList<>(conditions);
	}

	private ResponseCondition findFeedbackResponseCondition(Identifier feedbackIdentifier, Identifier outcomeValueIdentifier) {
		List<ResponseRule> responseRules = assessmentItem.getResponseProcessing().getResponseRules();
		for(ResponseRule responseRule:responseRules) {
			if(responseRule instanceof ResponseCondition) {
				if(findFeedbackRuleInSetOutcomeVariable(responseRule, feedbackIdentifier, outcomeValueIdentifier)) {
					return (ResponseCondition)responseRule;
				}
			}
		}
		return null;
	}
	
	private boolean findFeedbackRuleInSetOutcomeVariable(ResponseRule responseRule, Identifier feedbackIdentifier, Identifier outcomeValueIdentifier) {
		if(responseRule instanceof ResponseCondition) {
			ResponseCondition responseCondition = (ResponseCondition)responseRule;
			ResponseIf responseIf = responseCondition.getResponseIf();
			List<ResponseRule> ifResponseRules = responseIf.getResponseRules();
			for(ResponseRule ifResponseRule:ifResponseRules) {
				if(ifResponseRule instanceof SetOutcomeValue) {
					SetOutcomeValue setOutcomeValue = (SetOutcomeValue)ifResponseRule;
					if(outcomeValueIdentifier.equals(setOutcomeValue.getIdentifier()) && findBaseValueInExpression(setOutcomeValue.getExpression(), feedbackIdentifier)) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	private boolean findBaseValueInExpressionsOfResponseIf(ResponseCondition responseCondition, Identifier feedbackIdentifier) {
		if(responseCondition == null) return false;
		
		ResponseIf responseIf = responseCondition.getResponseIf();
		List<Expression> expressions = responseIf.getExpressions();
		for(Expression expression:expressions) {
			if(findBaseValueInExpression(expression, feedbackIdentifier)) {
				return true;
			}
		}
		return false;
	}
	

	
	public enum ModalFeedbackType {
		hint,
		correctSolution,
		correct,
		incorrect,
		empty,
		answered,
		additional,
		unkown
	}
}
