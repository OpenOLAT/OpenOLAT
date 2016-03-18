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
import static org.olat.ims.qti21.model.xml.AssessmentItemFactory.appendHotspotInteraction;
import static org.olat.ims.qti21.model.xml.AssessmentItemFactory.createHotspotEntryResponseDeclarationSingle;
import static org.olat.ims.qti21.model.xml.AssessmentItemFactory.createResponseProcessing;

import java.util.List;

import javax.xml.transform.stream.StreamResult;

import org.olat.core.gui.render.StringOutput;
import org.olat.ims.qti21.model.IdentifierGenerator;
import org.olat.ims.qti21.model.QTI21QuestionType;
import org.olat.ims.qti21.model.xml.AssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.AssessmentItemFactory;

import uk.ac.ed.ph.jqtiplus.node.content.ItemBody;
import uk.ac.ed.ph.jqtiplus.node.content.basic.Block;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.object.Object;
import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.HotspotInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.graphic.HotspotChoice;
import uk.ac.ed.ph.jqtiplus.node.item.response.declaration.ResponseDeclaration;
import uk.ac.ed.ph.jqtiplus.node.item.response.processing.ResponseProcessing;
import uk.ac.ed.ph.jqtiplus.node.item.response.processing.ResponseRule;
import uk.ac.ed.ph.jqtiplus.node.outcome.declaration.OutcomeDeclaration;
import uk.ac.ed.ph.jqtiplus.serialization.QtiSerializer;
import uk.ac.ed.ph.jqtiplus.types.Identifier;

/**
 * 
 * Initial date: 16.03.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class HotspotAssessmentItemBuilder extends AssessmentItemBuilder {
	
	private String question;
	private Identifier responseIdentifier;
	private HotspotInteraction hotspotInteraction;
	
	public HotspotAssessmentItemBuilder(QtiSerializer qtiSerializer) {
		super(createAssessmentItem(), qtiSerializer);
	}
	
	public HotspotAssessmentItemBuilder(AssessmentItem assessmentItem, QtiSerializer qtiSerializer) {
		super(assessmentItem, qtiSerializer);
	}
	
	private static AssessmentItem createAssessmentItem() {
		AssessmentItem assessmentItem = AssessmentItemFactory.createAssessmentItem(QTI21QuestionType.hotspot, "Hotspot");
		
		//define the response
		Identifier responseDeclarationId = Identifier.assumedLegal("RESPONSE_1");
		Identifier correctResponseId = IdentifierGenerator.newNumberAsIdentifier("hc");
		
		ResponseDeclaration responseDeclaration = createHotspotEntryResponseDeclarationSingle(assessmentItem, responseDeclarationId, correctResponseId);
		assessmentItem.getNodeGroups().getResponseDeclarationGroup().getResponseDeclarations().add(responseDeclaration);
		
		//outcomes
		appendDefaultOutcomeDeclarations(assessmentItem, 1.0d);
				
		ItemBody itemBody = appendDefaultItemBody(assessmentItem);
		appendHotspotInteraction(itemBody, responseDeclarationId, correctResponseId);
		
		//response processing
		ResponseProcessing responseProcessing = createResponseProcessing(assessmentItem, responseDeclarationId);
		assessmentItem.getNodeGroups().getResponseProcessingGroup().setResponseProcessing(responseProcessing);
		
		return assessmentItem;
	}

	@Override
	protected void extract() {
		super.extract();
		extractHotspotInteraction();
	}
	
	private void extractHotspotInteraction() {
		StringOutput sb = new StringOutput();
		List<Block> blocks = assessmentItem.getItemBody().getBlocks();
		for(Block block:blocks) {
			if(block instanceof HotspotInteraction) {
				hotspotInteraction = (HotspotInteraction)block;
				responseIdentifier = hotspotInteraction.getResponseIdentifier();
				break;
			} else {
				qtiSerializer.serializeJqtiObject(block, new StreamResult(sb));
			}
		}
		question = sb.toString();
	}
	
	public String getBackground() {
		Object graphichObject = hotspotInteraction.getObject();
		if(graphichObject != null) {
			return graphichObject.getData();
		}
		return null;
	}
	
	public void setBackground(String data, String mimeType, int height, int width) {
		Object graphichObject = hotspotInteraction.getObject();
		if(graphichObject == null) {
			graphichObject = new Object(hotspotInteraction);
			hotspotInteraction.setObject(graphichObject);
		}
		graphichObject.setData(data);
		graphichObject.setType(mimeType);
		if(height > 0) {
			graphichObject.setHeight(Integer.toString(height));
		} else {
			graphichObject.setHeight(null);
		}
		if(width > 0) {
			graphichObject.setWidth(Integer.toString(width));
		} else {
			graphichObject.setWidth(null);
		}
	}

	@Override
	public QTI21QuestionType getQuestionType() {
		return QTI21QuestionType.hotspot;
	}

	@Override
	public String getQuestion() {
		return question;
	}

	@Override
	public void setQuestion(String question) {
		this.question = question;
	}
	
	public List<HotspotChoice> getHotspotChoices() {
		return hotspotInteraction.getHotspotChoices();
	}
	
	public HotspotChoice createHotspotChoice(String id) {
		HotspotChoice choice = new HotspotChoice(hotspotInteraction);
		choice.setFixed(Boolean.FALSE);
		choice.setIdentifier(Identifier.parseString(id));
		return choice;
	}

	@Override
	protected void buildItemBody() {
		//remove current blocks
		List<Block> blocks = assessmentItem.getItemBody().getBlocks();
		blocks.clear();

		//add question
		getHtmlHelper().appendHtml(assessmentItem.getItemBody(), question);
		
		//add interaction
		blocks.add(hotspotInteraction);
	}

	@Override
	protected void buildMainScoreRule(List<OutcomeDeclaration> outcomeDeclarations, List<ResponseRule> responseRules) {
		//
	}
	
	

}
