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

import java.util.List;

import org.olat.ims.qti21.QTI21Constants;

import uk.ac.ed.ph.jqtiplus.node.expression.Expression;
import uk.ac.ed.ph.jqtiplus.node.expression.general.BaseValue;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentTest;
import uk.ac.ed.ph.jqtiplus.node.test.TestFeedback;
import uk.ac.ed.ph.jqtiplus.node.test.outcome.processing.OutcomeCondition;
import uk.ac.ed.ph.jqtiplus.node.test.outcome.processing.OutcomeIf;
import uk.ac.ed.ph.jqtiplus.node.test.outcome.processing.OutcomeRule;
import uk.ac.ed.ph.jqtiplus.node.test.outcome.processing.SetOutcomeValue;
import uk.ac.ed.ph.jqtiplus.types.Identifier;
import uk.ac.ed.ph.jqtiplus.value.IdentifierValue;
import uk.ac.ed.ph.jqtiplus.value.SingleValue;

/**
 * 
 * Initial date: 11.12.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TestFeedbackBuilder {
	
	private final TestFeedback testFeedback;
	private final AssessmentTest assessmentTest;
	
	private String title;
	private String text;
	private Identifier identifier;
	
	public TestFeedbackBuilder(AssessmentTest assessmentTest, TestFeedback testFeedback) {
		this.assessmentTest = assessmentTest;
		this.testFeedback = testFeedback;
		if(testFeedback != null) {
			text = new AssessmentHtmlBuilder().flowStaticString(testFeedback.getChildren());
			title = testFeedback.getTitle();
			identifier = testFeedback.getOutcomeValue();
		}
	}
	
	public Identifier getModalFeedbackIdentifier() {
		return testFeedback.getOutcomeValue();
	}
	
	public OutcomeRule getResponseRule() {
		OutcomeRule feedbackRule = findFeedbackRule(testFeedback.getOutcomeValue());
		return feedbackRule;
	}
	
	public boolean isPassedRule() {
		OutcomeRule feedbackRule = findFeedbackRule(testFeedback.getOutcomeValue());
		return findFeedbackRule(feedbackRule, QTI21Constants.CORRECT_IDENTIFIER);
	}
	
	public boolean isFailedRule() {
		OutcomeRule feedbackRule = findFeedbackRule(testFeedback.getOutcomeValue());
		return findFeedbackRule(feedbackRule, QTI21Constants.INCORRECT_IDENTIFIER);
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
	
	public Identifier getIdentifier() {
		return identifier;
	}

	public void setIdentifier(Identifier identifier) {
		this.identifier = identifier;
	}

	private OutcomeRule findFeedbackRule(Identifier feedbackIdentifier) {
		List<OutcomeRule> outcomeRules = assessmentTest.getOutcomeProcessing().getOutcomeRules();
		for(OutcomeRule outcomeRule:outcomeRules) {
			if(outcomeRule instanceof OutcomeCondition) {
				if(findFeedbackRuleInSetOutcomeVariable(outcomeRule, feedbackIdentifier)) {
					return outcomeRule;
				}
			}
		}
		return null;
	}
	
	private boolean findFeedbackRuleInSetOutcomeVariable(OutcomeRule responseRule, Identifier feedbackIdentifier) {
		if(responseRule instanceof OutcomeCondition) {
			OutcomeCondition outcomeCondition = (OutcomeCondition)responseRule;
			OutcomeIf outcomeIf = outcomeCondition.getOutcomeIf();
			List<OutcomeRule> ifOutcomeRules = outcomeIf.getOutcomeRules();
			for(OutcomeRule ifOutcomeRule:ifOutcomeRules) {
				if(ifOutcomeRule instanceof SetOutcomeValue) {
					SetOutcomeValue setOutcomeValue = (SetOutcomeValue)ifOutcomeRule;
					if(findFeedbackRuleInExpression(setOutcomeValue.getExpression(), feedbackIdentifier)) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	private boolean findFeedbackRule(OutcomeRule outcomeRule, Identifier id) {
		if(outcomeRule instanceof OutcomeCondition) {
			OutcomeCondition outcomeCondition = (OutcomeCondition)outcomeRule;
			OutcomeIf outcomeIf = outcomeCondition.getOutcomeIf();
			List<Expression> expressions = outcomeIf.getExpressions();
			if(findFeedbackRuleInExpression(expressions, id)) {
				return true;
			}
		}
		return false;
	}

	private boolean findFeedbackRuleInExpression(List<Expression> expressions, Identifier feedbackIdentifier) {
		for(Expression expression:expressions) {
			if(findFeedbackRuleInExpression(expression, feedbackIdentifier)) {
				return true;
			}
		}
		return false;
	}
	
	private boolean findFeedbackRuleInExpression(Expression expression, Identifier feedbackIdentifier) {
		if(expression instanceof BaseValue) {
			BaseValue bValue = (BaseValue)expression;
			SingleValue sValue = bValue.getSingleValue();
			if(sValue instanceof IdentifierValue) {
				IdentifierValue iValue = (IdentifierValue)sValue;
				if(feedbackIdentifier.equals(iValue.identifierValue())) {
					return true;
				}
			}
		} else {
			List<Expression> childExpressions = expression.getExpressions();
			for(Expression childExpression:childExpressions) {
				if(findFeedbackRuleInExpression(childExpression, feedbackIdentifier)) {
					return true;
				}
			}
		}
		return false;
	}
}
