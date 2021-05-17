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

import static org.olat.ims.qti21.model.xml.AssessmentItemFactory.appendDefaultItemBody;
import static org.olat.ims.qti21.model.xml.AssessmentItemFactory.appendDefaultOutcomeDeclarations;
import static org.olat.ims.qti21.model.xml.AssessmentItemFactory.appendUploadInteraction;
import static org.olat.ims.qti21.model.xml.AssessmentItemFactory.createResponseProcessing;
import static org.olat.ims.qti21.model.xml.AssessmentItemFactory.createUploadResponseDeclaration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.logging.Tracing;
import org.olat.ims.qti21.QTI21Constants;
import org.olat.ims.qti21.model.QTI21QuestionType;
import org.olat.ims.qti21.model.xml.AssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.AssessmentItemFactory;

import uk.ac.ed.ph.jqtiplus.node.content.ItemBody;
import uk.ac.ed.ph.jqtiplus.node.content.basic.Block;
import uk.ac.ed.ph.jqtiplus.node.expression.general.BaseValue;
import uk.ac.ed.ph.jqtiplus.node.expression.general.Variable;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.And;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.IsNull;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.Not;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.Or;
import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.ModalFeedback;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.UploadInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.response.declaration.ResponseDeclaration;
import uk.ac.ed.ph.jqtiplus.node.item.response.processing.ResponseCondition;
import uk.ac.ed.ph.jqtiplus.node.item.response.processing.ResponseElseIf;
import uk.ac.ed.ph.jqtiplus.node.item.response.processing.ResponseIf;
import uk.ac.ed.ph.jqtiplus.node.item.response.processing.ResponseProcessing;
import uk.ac.ed.ph.jqtiplus.node.item.response.processing.ResponseRule;
import uk.ac.ed.ph.jqtiplus.node.item.response.processing.SetOutcomeValue;
import uk.ac.ed.ph.jqtiplus.node.outcome.declaration.OutcomeDeclaration;
import uk.ac.ed.ph.jqtiplus.serialization.QtiSerializer;
import uk.ac.ed.ph.jqtiplus.types.ComplexReferenceIdentifier;
import uk.ac.ed.ph.jqtiplus.types.Identifier;
import uk.ac.ed.ph.jqtiplus.value.BaseType;

/**
 * 
 * Initial date: 8 nov. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UploadAssessmentItemBuilder extends AssessmentItemBuilder {
	
	private static final Logger log = Tracing.createLoggerFor(UploadAssessmentItemBuilder.class);

	private String question;
	private List<UploadInteraction> uploadInteractions;
	
	public UploadAssessmentItemBuilder(String title, QtiSerializer qtiSerializer) {
		super(createAssessmentItem(title), qtiSerializer);
	}
	
	public UploadAssessmentItemBuilder(AssessmentItem assessmentItem, QtiSerializer qtiSerializer) {
		super(assessmentItem, qtiSerializer);
	}
	
	private static AssessmentItem createAssessmentItem(String title) {
		AssessmentItem assessmentItem = AssessmentItemFactory.createAssessmentItem(QTI21QuestionType.upload, title);
		
		//define the response
		Identifier responseDeclarationId = Identifier.assumedLegal("RESPONSE_1");
		ResponseDeclaration responseDeclaration = createUploadResponseDeclaration(assessmentItem, responseDeclarationId);
		assessmentItem.getNodeGroups().getResponseDeclarationGroup().getResponseDeclarations().add(responseDeclaration);
	
		//outcomes
		appendDefaultOutcomeDeclarations(assessmentItem, 1.0d);
		
		ItemBody itemBody = appendDefaultItemBody(assessmentItem);
		appendUploadInteraction(itemBody, responseDeclarationId);
		
		//response processing
		ResponseProcessing responseProcessing = createResponseProcessing(assessmentItem, responseDeclarationId);
		assessmentItem.getNodeGroups().getResponseProcessingGroup().setResponseProcessing(responseProcessing);
		return assessmentItem;
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
	public QTI21QuestionType getQuestionType() {
		return QTI21QuestionType.upload;
	}
	
	/**
	 * @return A copy of the list of blocks which make the question.
	 * 		The list is a copy and modification will not be persisted.
	 */
	public List<Block> getQuestionBlocks() {
		List<Block> blocks = assessmentItem.getItemBody().getBlocks();
		List<Block> questionBlocks = new ArrayList<>(blocks.size());
		for(Block block:blocks) {
			if(block instanceof UploadInteraction) {
				break;
			} else {
				questionBlocks.add(block);
			}
		}
		return questionBlocks;
	}
	
	public int getNumberOfUploadInteractions() {
		return uploadInteractions.size();
	}
	
	public void setNumberOfUploadInteractions(int num) {
		if(num > uploadInteractions.size()) {
			for(int i=uploadInteractions.size(); i<num; i++) {
				Identifier responseDeclarationId = generateResponseDeclarationId(i + 1);
				UploadInteraction uploadInteraction = new UploadInteraction(assessmentItem.getItemBody());
				uploadInteraction.setResponseIdentifier(responseDeclarationId);
				uploadInteractions.add(uploadInteraction);
			}
		} else if(num < uploadInteractions.size()) {
			for(int i=uploadInteractions.size(); i>num; i--) {
				uploadInteractions.remove(i - 1);
			}
		}
	}
	
	private Identifier generateResponseDeclarationId(int i) {
		Identifier responseDeclarationId = Identifier.assumedLegal("RESPONSE_" + i);
		for(int j=100; j<1000;) {
			if(responseIdentifierInUse(responseDeclarationId)) {
				responseDeclarationId = Identifier.assumedLegal("RESPONSE_" + j);
			} else {
				break;
			}
		}
		return responseDeclarationId;
	}
	
	private boolean responseIdentifierInUse(Identifier identifier) {
		for(int i=uploadInteractions.size(); i-->0;) {
			if(identifier.equals(uploadInteractions.get(i).getResponseIdentifier())) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void extract() {
		super.extract();
		extractExtendedTextInteraction();
	}
	
	private void extractExtendedTextInteraction() {
		uploadInteractions = new ArrayList<>();
		
		// question and after upload area with the list of interactions
		boolean uploadArea = false;
		try(StringOutput sb = new StringOutput()) {
			List<Block> blocks = assessmentItem.getItemBody().getBlocks();
			for(Block block:blocks) {
				if(block instanceof UploadInteraction) {
					uploadInteractions.add((UploadInteraction)block);
					uploadArea = true;
				} else if(!uploadArea) {
					serializeJqtiObject(block, sb);
				}
			}
			question = sb.toString();
		} catch(IOException e) {
			log.error("", e);
		}
	}

	@Override
	protected void buildResponseAndOutcomeDeclarations() {
		for(UploadInteraction uploadInteraction:uploadInteractions) {
			Identifier responseIdentifier = uploadInteraction.getResponseIdentifier();
			ResponseDeclaration responseDeclaration =
					createUploadResponseDeclaration(assessmentItem, responseIdentifier);
			assessmentItem.getResponseDeclarations().add(responseDeclaration);
		}
	}
	
	@Override
	protected void buildItemBody() {
		//remove current blocks
		List<Block> blocks = assessmentItem.getItemBody().getBlocks();
		blocks.clear();

		//add question
		getHtmlHelper().appendHtml(assessmentItem.getItemBody(), question);
		
		//add interactions
		for(UploadInteraction uploadInteraction:uploadInteractions) {
			blocks.add(uploadInteraction);
		}
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
			And and = new And(responseIf);
			responseIf.getExpressions().add(and);
			for(UploadInteraction uploadInteraction:uploadInteractions) {
				IsNull isNull = new IsNull(responseIf);
				and.getExpressions().add(isNull);
				
				Variable variable = new Variable(isNull);
				variable.setIdentifier(ComplexReferenceIdentifier.parseString(uploadInteraction.getResponseIdentifier().toString()));
				isNull.getExpressions().add(variable);
			}
			
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
			Or or = new Or(responseIf);
			responseElseIf.getExpressions().add(or);
			for(UploadInteraction uploadInteraction:uploadInteractions) {
				Not not = new Not(or);
				or.getExpressions().add(not);
				
				IsNull isNull = new IsNull(responseIf);
				not.getExpressions().add(isNull);
				
				Variable variable = new Variable(isNull);
				variable.setIdentifier(ComplexReferenceIdentifier.parseString(uploadInteraction.getResponseIdentifier().toString()));
				isNull.getExpressions().add(variable);
			}

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