package org.olat.ims.qti21.model.xml.items;

import static org.olat.ims.qti21.model.xml.AssessmentItemFactory.appendDefaultItemBody;
import static org.olat.ims.qti21.model.xml.AssessmentItemFactory.appendDefaultOutcomeDeclarations;
import static org.olat.ims.qti21.model.xml.AssessmentItemFactory.appendExtendedTextInteraction;
import static org.olat.ims.qti21.model.xml.AssessmentItemFactory.createExtendedTextResponseDeclaration;
import static org.olat.ims.qti21.model.xml.AssessmentItemFactory.createResponseProcessing;

import java.util.List;

import javax.xml.transform.stream.StreamResult;

import org.olat.core.gui.render.StringOutput;
import org.olat.ims.qti21.model.xml.AssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.AssessmentItemFactory;

import uk.ac.ed.ph.jqtiplus.node.content.ItemBody;
import uk.ac.ed.ph.jqtiplus.node.content.basic.Block;
import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.ExtendedTextInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.response.declaration.ResponseDeclaration;
import uk.ac.ed.ph.jqtiplus.node.item.response.processing.ResponseProcessing;
import uk.ac.ed.ph.jqtiplus.node.item.response.processing.ResponseRule;
import uk.ac.ed.ph.jqtiplus.serialization.QtiSerializer;
import uk.ac.ed.ph.jqtiplus.types.Identifier;

/**
 * 
 * Initial date: 08.01.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EssayAssessmentItemBuilder extends AssessmentItemBuilder {

	private String question;
	private Identifier responseIdentifier;
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
		appendDefaultOutcomeDeclarations(assessmentItem);
		
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
	
	public ExtendedTextInteraction getExtendedTextInteraction() {
		return extendedTextInteraction;
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
		//
	}

}
