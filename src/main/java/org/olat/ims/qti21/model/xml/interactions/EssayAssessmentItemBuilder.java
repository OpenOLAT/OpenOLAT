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
import static org.olat.ims.qti21.model.xml.AssessmentItemFactory.appendExtendedTextInteraction;
import static org.olat.ims.qti21.model.xml.AssessmentItemFactory.createExtendedTextResponseDeclaration;
import static org.olat.ims.qti21.model.xml.AssessmentItemFactory.createResponseProcessing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.render.StringOutput;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.ims.qti21.QTI21Constants;
import org.olat.ims.qti21.model.QTI21QuestionType;
import org.olat.ims.qti21.model.xml.AssessmentItemFactory;

import uk.ac.ed.ph.jqtiplus.node.content.ItemBody;
import uk.ac.ed.ph.jqtiplus.node.content.basic.Block;
import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.ExtendedTextInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.response.declaration.ResponseDeclaration;
import uk.ac.ed.ph.jqtiplus.node.item.response.processing.ResponseProcessing;
import uk.ac.ed.ph.jqtiplus.serialization.QtiSerializer;
import uk.ac.ed.ph.jqtiplus.types.Identifier;

/**
 * 
 * Initial date: 08.01.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EssayAssessmentItemBuilder extends LobAssessmentItemBuilder {
	
	private static final Logger log = Tracing.createLoggerFor(EssayAssessmentItemBuilder.class);

	private ExtendedTextInteraction extendedTextInteraction;
	
	public EssayAssessmentItemBuilder(String title, QtiSerializer qtiSerializer) {
		super(createAssessmentItem(title), qtiSerializer);
	}
	
	public EssayAssessmentItemBuilder(AssessmentItem assessmentItem, QtiSerializer qtiSerializer) {
		super(assessmentItem, qtiSerializer);
	}
	
	private static AssessmentItem createAssessmentItem(String title) {
		AssessmentItem assessmentItem = AssessmentItemFactory.createAssessmentItem(QTI21QuestionType.essay, title);
		
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
	public QTI21QuestionType getQuestionType() {
		return QTI21QuestionType.essay;
	}
	
	/**
	 * @return A copy of the list of blocks which make the question.
	 * 		The list is a copy and modification will not be persisted.
	 */
	public List<Block> getQuestionBlocks() {
		List<Block> blocks = assessmentItem.getItemBody().getBlocks();
		List<Block> questionBlocks = new ArrayList<>(blocks.size());
		for(Block block:blocks) {
			if(block instanceof ExtendedTextInteraction) {
				break;
			} else {
				questionBlocks.add(block);
			}
		}
		return questionBlocks;
	}
	
	@Override
	public void extract() {
		super.extract();
		extractExtendedTextInteraction();
	}
	
	private void extractExtendedTextInteraction() {
		try(StringOutput sb = new StringOutput()) {
			List<Block> blocks = assessmentItem.getItemBody().getBlocks();
			for(Block block:blocks) {
				if(block instanceof ExtendedTextInteraction) {
					extendedTextInteraction = (ExtendedTextInteraction)block;
					responseIdentifier = extendedTextInteraction.getResponseIdentifier();
					break;
				} else {
					serializeJqtiObject(block, sb);
				}
			}
			question = sb.toString();
		} catch(IOException e) {
			log.error("", e);
		}
	}
	
	public String getPlaceholder() {
		return extendedTextInteraction.getPlaceholderText();
	}
	
	public void setPlaceholder(String placeholder) {
		if(StringHelper.containsNonWhitespace(placeholder)) {
			extendedTextInteraction.setPlaceholderText(placeholder);
		} else {
			extendedTextInteraction.setPlaceholderText(null);
		}
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
	
	public boolean isRichTextFormating() {
		List<String> classes = extendedTextInteraction.getClassAttr();
		return classes != null && classes.contains(QTI21Constants.CSS_ESSAY_RICHTEXT);
	}
	
	public void setRichTextFormating(boolean enabled) {
		enableClassFeature(enabled, QTI21Constants.CSS_ESSAY_RICHTEXT);
	}
	
	public boolean isCopyPasteDisabled() {
		List<String> classes = extendedTextInteraction.getClassAttr();
		return classes != null && classes.contains(QTI21Constants.CSS_ESSAY_DISABLE_COPYPASTE);
	}

	public void setCopyPasteDisabled(boolean copyPasteDisabled) {
		enableClassFeature(copyPasteDisabled, QTI21Constants.CSS_ESSAY_DISABLE_COPYPASTE);
	}
	
	private void enableClassFeature(boolean enable, String feature) {
		List<String> cssClassses = extendedTextInteraction.getClassAttr();
		cssClassses = cssClassses == null ? new ArrayList<>() : new ArrayList<>(cssClassses);
		if(enable) {
			if(!cssClassses.contains(feature)) {
				cssClassses.add(feature);
			}
		} else {
			cssClassses.remove(feature);
		}
		extendedTextInteraction.setClassAttr(cssClassses);
	}

	@Override
	protected void buildResponseAndOutcomeDeclarations() {
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
}