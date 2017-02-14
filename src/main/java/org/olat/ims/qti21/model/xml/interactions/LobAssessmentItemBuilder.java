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
package org.olat.ims.qti21.model.xml.interactions;

import java.util.List;

import org.olat.ims.qti21.QTI21Constants;
import org.olat.ims.qti21.model.xml.AssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.AssessmentItemFactory;

import uk.ac.ed.ph.jqtiplus.node.expression.general.BaseValue;
import uk.ac.ed.ph.jqtiplus.node.expression.general.Variable;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.IsNull;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.Not;
import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.ModalFeedback;
import uk.ac.ed.ph.jqtiplus.node.item.response.processing.ResponseCondition;
import uk.ac.ed.ph.jqtiplus.node.item.response.processing.ResponseElseIf;
import uk.ac.ed.ph.jqtiplus.node.item.response.processing.ResponseIf;
import uk.ac.ed.ph.jqtiplus.node.item.response.processing.ResponseRule;
import uk.ac.ed.ph.jqtiplus.node.item.response.processing.SetOutcomeValue;
import uk.ac.ed.ph.jqtiplus.node.outcome.declaration.OutcomeDeclaration;
import uk.ac.ed.ph.jqtiplus.serialization.QtiSerializer;
import uk.ac.ed.ph.jqtiplus.types.ComplexReferenceIdentifier;
import uk.ac.ed.ph.jqtiplus.types.Identifier;
import uk.ac.ed.ph.jqtiplus.value.BaseType;

/**
 * this is an abstract for the three CLOB / BLOB interactions: essay, upload and drawing.
 * 
 * 
 * Initial date: 08.01.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public abstract class LobAssessmentItemBuilder extends AssessmentItemBuilder {

	protected String question;
	protected Identifier responseIdentifier;
	
	public LobAssessmentItemBuilder(AssessmentItem assessmentItem, QtiSerializer qtiSerializer) {
		super(assessmentItem, qtiSerializer);
	}

	@Override
	public String getQuestion() {
		return question;
	}
	
	@Override
	public void setQuestion(String html) {
		this.question = html;
	}

	@Override
	protected void buildMainScoreRule(List<OutcomeDeclaration> outcomeDeclarations, List<ResponseRule> responseRules) {
		ResponseCondition rule = new ResponseCondition(assessmentItem.getResponseProcessing());
		responseRules.add(0, rule);
		buildMainEssayFeedbackRule(rule);
	}

	@Override
	protected void buildModalFeedbacksAndHints(List<OutcomeDeclaration> outcomeDeclarations, List<ResponseRule> responseRules) {
		super.buildModalFeedbacksAndHints(outcomeDeclarations, responseRules);
		ensureFeedbackBasicOutcomeDeclaration();
	}

	private void buildMainEssayFeedbackRule(ResponseCondition rule) {
		/*
		 <responseCondition>
			<responseIf>
				<isNull>
					<variable identifier="RESPONSE_1" />
				</isNull>
				<setOutcomeValue identifier="FEEDBACKBASIC">
					<baseValue baseType="identifier">
						empty
					</baseValue>
				</setOutcomeValue>
			</responseIf>
		</responseCondition>
		 */

		ResponseIf responseIf = new ResponseIf(rule);
		rule.setResponseIf(responseIf);
		
		{
			IsNull isNull = new IsNull(responseIf);
			responseIf.getExpressions().add(isNull);
			
			Variable variable = new Variable(isNull);
			variable.setIdentifier(ComplexReferenceIdentifier.parseString(responseIdentifier.toString()));
			isNull.getExpressions().add(variable);
			
			SetOutcomeValue feedbackOutcomeValue = new SetOutcomeValue(responseIf);
			feedbackOutcomeValue.setIdentifier(QTI21Constants.FEEDBACKBASIC_IDENTIFIER);
			responseIf.getResponseRules().add(feedbackOutcomeValue);
			
			BaseValue incorrectValue = new BaseValue(feedbackOutcomeValue);
			incorrectValue.setBaseTypeAttrValue(BaseType.IDENTIFIER);
			incorrectValue.setSingleValue(QTI21Constants.EMPTY_IDENTIFIER_VALUE);
			feedbackOutcomeValue.setExpression(incorrectValue);
		}
		
		ResponseElseIf responseElseIf = new ResponseElseIf(rule);
		rule.getResponseElseIfs().add(responseElseIf);
		
		{
			Not not = new Not(responseElseIf);
			responseElseIf.getExpressions().add(not);
			IsNull isNull = new IsNull(responseIf);
			not.getExpressions().add(isNull);

			Variable variable = new Variable(isNull);
			variable.setIdentifier(ComplexReferenceIdentifier.parseString(responseIdentifier.toString()));
			isNull.getExpressions().add(variable);
			
			SetOutcomeValue feedbackOutcomeValue = new SetOutcomeValue(responseIf);
			feedbackOutcomeValue.setIdentifier(QTI21Constants.FEEDBACKBASIC_IDENTIFIER);
			responseElseIf.getResponseRules().add(feedbackOutcomeValue);
			
			BaseValue answeredValue = new BaseValue(feedbackOutcomeValue);
			answeredValue.setBaseTypeAttrValue(BaseType.IDENTIFIER);
			answeredValue.setSingleValue(QTI21Constants.ANSWERED_IDENTIFIER_VALUE);
			feedbackOutcomeValue.setExpression(answeredValue);
		}
	}

	@Override
	protected void appendCorrectSolutionAndIncorrectModalFeedback(List<ModalFeedback> modalFeedbacks, List<ResponseRule> responseRules) {
		if(correctSolutionFeedback != null) {
			ModalFeedback modalFeedback = AssessmentItemFactory.createModalFeedback(assessmentItem,
					QTI21Constants.CORRECT_SOLUTION_IDENTIFIER, correctSolutionFeedback.getIdentifier(),
					correctSolutionFeedback.getTitle(), correctSolutionFeedback.getText());
			modalFeedbacks.add(modalFeedback);
		}
	}
	
	
}