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
import org.olat.ims.qti21.model.IdentifierGenerator;

import uk.ac.ed.ph.jqtiplus.attribute.value.StringAttribute;
import uk.ac.ed.ph.jqtiplus.node.expression.Expression;
import uk.ac.ed.ph.jqtiplus.node.expression.general.BaseValue;
import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.ModalFeedback;
import uk.ac.ed.ph.jqtiplus.node.item.response.processing.ResponseCondition;
import uk.ac.ed.ph.jqtiplus.node.item.response.processing.ResponseIf;
import uk.ac.ed.ph.jqtiplus.node.item.response.processing.ResponseRule;
import uk.ac.ed.ph.jqtiplus.node.item.response.processing.SetOutcomeValue;
import uk.ac.ed.ph.jqtiplus.types.Identifier;
import uk.ac.ed.ph.jqtiplus.value.IdentifierValue;
import uk.ac.ed.ph.jqtiplus.value.SingleValue;

/**
 * 
 * Initial date: 09.12.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ModalFeedbackBuilder {
	
	private final ModalFeedback modalFeedback;
	private final AssessmentItem assessmentItem;
	
	private String title;
	private String text;
	private Identifier identifier;
	
	public ModalFeedbackBuilder(AssessmentItem assessmentItem, ModalFeedback modalFeedback) {
		this.assessmentItem = assessmentItem;
		this.modalFeedback = modalFeedback;
		if(modalFeedback != null) {
			text = new AssessmentHtmlBuilder().flowStaticString(modalFeedback.getFlowStatics());
			StringAttribute titleAttr = modalFeedback.getAttributes().getStringAttribute(ModalFeedback.ATTR_TITLE_NAME);
			title = titleAttr == null ? null : titleAttr.getComputedValue();
			identifier = modalFeedback.getIdentifier();
		} else {
			identifier = IdentifierGenerator.newNumberAsIdentifier("Feedback");
		}
	}
	
	public Identifier getModalFeedbackIdentifier() {
		return modalFeedback.getIdentifier();
	}
	
	public ResponseRule getResponseRule() {
		ResponseRule feedbackRule = findFeedbackRule(modalFeedback.getIdentifier());
		return feedbackRule;
	}
	
	public boolean isCorrectRule() {
		ResponseRule feedbackRule = findFeedbackRule(modalFeedback.getIdentifier());
		return findFeedbackRule(feedbackRule, QTI21Constants.CORRECT_IDENTIFIER);
	}
	
	public boolean isIncorrectRule() {
		ResponseRule feedbackRule = findFeedbackRule(modalFeedback.getIdentifier());
		return findFeedbackRule(feedbackRule, QTI21Constants.INCORRECT_IDENTIFIER);
	}
	
	public boolean isEmptyRule() {
		ResponseRule feedbackRule = findFeedbackRule(modalFeedback.getIdentifier());
		return findFeedbackRule(feedbackRule, QTI21Constants.EMPTY_IDENTIFIER);
	}
	
	public boolean isAnsweredRule() {
		ResponseRule feedbackRule = findFeedbackRule(modalFeedback.getIdentifier());
		return findFeedbackRule(feedbackRule, QTI21Constants.ANSWERED_IDENTIFIER);
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
	
	public Identifier getIdentifier() {
		return identifier;
	}

	public void setIdentifier(Identifier identifier) {
		this.identifier = identifier;
	}

	private ResponseRule findFeedbackRule(Identifier feedbackIdentifier) {
		List<ResponseRule> responseRules = assessmentItem.getResponseProcessing().getResponseRules();
		for(ResponseRule responseRule:responseRules) {
			if(responseRule instanceof ResponseCondition) {
				if(findFeedbackRuleInSetOutcomeVariable(responseRule, feedbackIdentifier)) {
					return responseRule;
				}
			}
		}
		return null;
	}
	
	private boolean findFeedbackRuleInSetOutcomeVariable(ResponseRule responseRule, Identifier feedbackIdentifier) {
		if(responseRule instanceof ResponseCondition) {
			ResponseCondition responseCondition = (ResponseCondition)responseRule;
			ResponseIf responseIf = responseCondition.getResponseIf();
			List<ResponseRule> ifResponseRules = responseIf.getResponseRules();
			for(ResponseRule ifResponseRule:ifResponseRules) {
				if(ifResponseRule instanceof SetOutcomeValue) {
					SetOutcomeValue setOutcomeValue = (SetOutcomeValue)ifResponseRule;
					if(findFeedbackRuleInExpression(setOutcomeValue.getExpression(), feedbackIdentifier)) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	private boolean findFeedbackRule(ResponseRule responseRule, Identifier id) {
		if(responseRule instanceof ResponseCondition) {
			ResponseCondition responseCondition = (ResponseCondition)responseRule;
			ResponseIf responseIf = responseCondition.getResponseIf();
			List<Expression> expressions = responseIf.getExpressions();
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
