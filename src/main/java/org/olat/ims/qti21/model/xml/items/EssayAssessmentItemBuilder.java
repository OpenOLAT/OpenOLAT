package org.olat.ims.qti21.model.xml.items;

import static org.olat.ims.qti21.model.xml.AssessmentItemFactory.appendDefaultItemBody;
import static org.olat.ims.qti21.model.xml.AssessmentItemFactory.appendDefaultOutcomeDeclarations;
import static org.olat.ims.qti21.model.xml.AssessmentItemFactory.appendExtendedTextInteraction;
import static org.olat.ims.qti21.model.xml.AssessmentItemFactory.createExtendedTextResponseDeclaration;
import static org.olat.ims.qti21.model.xml.AssessmentItemFactory.createResponseProcessing;

import java.util.List;

import javax.xml.transform.stream.StreamResult;

import org.olat.core.gui.render.StringOutput;
import org.olat.ims.qti21.QTI21Constants;
import org.olat.ims.qti21.model.xml.AssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.AssessmentItemFactory;

import uk.ac.ed.ph.jqtiplus.node.content.ItemBody;
import uk.ac.ed.ph.jqtiplus.node.content.basic.Block;
import uk.ac.ed.ph.jqtiplus.node.expression.general.BaseValue;
import uk.ac.ed.ph.jqtiplus.node.expression.general.Variable;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.IsNull;
import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.ExtendedTextInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.response.declaration.ResponseDeclaration;
import uk.ac.ed.ph.jqtiplus.node.item.response.processing.ResponseCondition;
import uk.ac.ed.ph.jqtiplus.node.item.response.processing.ResponseIf;
import uk.ac.ed.ph.jqtiplus.node.item.response.processing.ResponseProcessing;
import uk.ac.ed.ph.jqtiplus.node.item.response.processing.ResponseRule;
import uk.ac.ed.ph.jqtiplus.node.item.response.processing.SetOutcomeValue;
import uk.ac.ed.ph.jqtiplus.serialization.QtiSerializer;
import uk.ac.ed.ph.jqtiplus.types.ComplexReferenceIdentifier;
import uk.ac.ed.ph.jqtiplus.types.Identifier;
import uk.ac.ed.ph.jqtiplus.value.BaseType;

/**
 * 
 * Initial date: 08.01.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EssayAssessmentItemBuilder extends AssessmentItemBuilder {

	private String question;
	protected Identifier responseIdentifier;
	private ExtendedTextInteraction extendedTextInteraction;
	
	public EssayAssessmentItemBuilder(QtiSerializer qtiSerializer) {
		super(createAssessmentItem(), qtiSerializer);
	}
	
	public EssayAssessmentItemBuilder(AssessmentItem assessmentItem, QtiSerializer qtiSerializer) {
		super(assessmentItem, qtiSerializer);
	}
	
	private static AssessmentItem createAssessmentItem() {
		AssessmentItem assessmentItem = AssessmentItemFactory.createAssessmentItem("Essay");
		
		//define the response
		Identifier responseDeclarationId = Identifier.assumedLegal("RESPONSE_1");
		ResponseDeclaration responseDeclaration = createExtendedTextResponseDeclaration(assessmentItem, responseDeclarationId);
		assessmentItem.getNodeGroups().getResponseDeclarationGroup().getResponseDeclarations().add(responseDeclaration);
	
		//outcomes
		appendDefaultOutcomeDeclarations(assessmentItem, 1.0d);
		
		ItemBody itemBody = appendDefaultItemBody(assessmentItem);
		appendExtendedTextInteraction(itemBody, responseDeclarationId);
		
		//response processing
		ResponseProcessing responseProcessing = createResponseProcessing(assessmentItem, responseDeclarationId);
		assessmentItem.getNodeGroups().getResponseProcessingGroup().setResponseProcessing(responseProcessing);
		return assessmentItem;
	}
	
	@Override
	public void extract() {
		super.extract();
		extractExtendedTextInteraction();
	}
	
	private void extractExtendedTextInteraction() {
		StringOutput sb = new StringOutput();
		List<Block> blocks = assessmentItem.getItemBody().getBlocks();
		for(Block block:blocks) {
			if(block instanceof ExtendedTextInteraction) {
				extendedTextInteraction = (ExtendedTextInteraction)block;
				responseIdentifier = extendedTextInteraction.getResponseIdentifier();
				break;
			} else {
				qtiSerializer.serializeJqtiObject(block, new StreamResult(sb));
			}
		}
		question = sb.toString();
	}
	
	public String getQuestion() {
		return question;
	}
	
	public void setQuestion(String html) {
		this.question = html;
	}
	
	public Integer getExpectedLength() {
		return extendedTextInteraction.getExpectedLength();
	}
	
	public void setExpectedLength(Integer length) {
		extendedTextInteraction.setExpectedLength(length);
	}
	
	public Integer getExpectedLines() {
		return extendedTextInteraction.getExpectedLines();
	}
	
	public void setExpectedLines(Integer lines) {
		extendedTextInteraction.setExpectedLines(lines);
	}
	
	public ExtendedTextInteraction getExtendedTextInteraction() {
		return extendedTextInteraction;
	}
	
	public Integer getMinStrings() {
		return extendedTextInteraction.getMinStrings();
	}
	
	public void setMinStrings(Integer minStrings) {
		extendedTextInteraction.setMinStrings(minStrings);
	}
	
	public Integer getMaxStrings() {
		return extendedTextInteraction.getMaxStrings();
	}
	
	public void setMaxStrings(Integer maxStrings) {
		extendedTextInteraction.setMaxStrings(maxStrings);
	}
	
	@Override
	protected void buildResponseDeclaration() {
		ResponseDeclaration responseDeclaration =
				createExtendedTextResponseDeclaration(assessmentItem, responseIdentifier);
		assessmentItem.getResponseDeclarations().add(responseDeclaration);
	}
	
	@Override
	protected void buildItemBody() {
		//remove current blocks
		List<Block> blocks = assessmentItem.getItemBody().getBlocks();
		blocks.clear();

		//add question
		getHtmlHelper().appendHtml(assessmentItem.getItemBody(), question);
		
		//add interaction
		blocks.add(extendedTextInteraction);
	}

	@Override
	protected void buildMainScoreRule(List<ResponseRule> responseRules) {
		ResponseCondition rule = new ResponseCondition(assessmentItem.getResponseProcessing());
		responseRules.add(0, rule);
		buildMainEssayFeedbackRule(rule);
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
			
			SetOutcomeValue incorrectOutcomeValue = new SetOutcomeValue(responseIf);
			incorrectOutcomeValue.setIdentifier(QTI21Constants.FEEDBACKBASIC_IDENTIFIER);
			responseIf.getResponseRules().add(incorrectOutcomeValue);
			
			BaseValue incorrectValue = new BaseValue(incorrectOutcomeValue);
			incorrectValue.setBaseTypeAttrValue(BaseType.IDENTIFIER);
			incorrectValue.setSingleValue(QTI21Constants.EMPTY_IDENTIFIER_VALUE);
			incorrectOutcomeValue.setExpression(incorrectValue);
		}
	}

}
