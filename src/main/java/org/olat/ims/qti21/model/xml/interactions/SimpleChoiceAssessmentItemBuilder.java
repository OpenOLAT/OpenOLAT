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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.render.StringOutput;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.filter.FilterFactory;
import org.olat.ims.qti21.model.xml.ResponseIdentifierForFeedback;

import uk.ac.ed.ph.jqtiplus.node.content.basic.Block;
import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.ChoiceInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.Interaction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.SimpleChoice;
import uk.ac.ed.ph.jqtiplus.node.item.response.declaration.MapEntry;
import uk.ac.ed.ph.jqtiplus.node.item.response.declaration.Mapping;
import uk.ac.ed.ph.jqtiplus.node.item.response.declaration.ResponseDeclaration;
import uk.ac.ed.ph.jqtiplus.serialization.QtiSerializer;
import uk.ac.ed.ph.jqtiplus.types.Identifier;
import uk.ac.ed.ph.jqtiplus.value.IdentifierValue;
import uk.ac.ed.ph.jqtiplus.value.Orientation;
import uk.ac.ed.ph.jqtiplus.value.SingleValue;

/**
 * 
 * Initial date: 08.12.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public abstract class SimpleChoiceAssessmentItemBuilder extends ChoiceAssessmentItemBuilder implements ResponseIdentifierForFeedback {
	
	private static final Logger log = Tracing.createLoggerFor(SimpleChoiceAssessmentItemBuilder.class);

	protected int maxChoices;
	protected int minChoices;
	protected boolean shuffle;
	protected String question;
	protected List<String> cssClass;
	protected Orientation orientation;
	protected List<SimpleChoice> choices;
	protected Identifier responseIdentifier;
	protected ChoiceInteraction choiceInteraction;
	
	public SimpleChoiceAssessmentItemBuilder(AssessmentItem assessmentItem, QtiSerializer qtiSerializer) {
		super(assessmentItem, qtiSerializer);
	}
	
	@Override
	public void extract() {
		super.extract();
		extractChoiceInteraction();
		extractScoreEvaluationMode();
	}
	
	private void extractScoreEvaluationMode() {
		scoreMapping = getMapping(assessmentItem, choiceInteraction);
		boolean hasMapping = scoreMapping != null && !scoreMapping.isEmpty();
		scoreEvaluation = hasMapping ? ScoreEvaluation.perAnswer : ScoreEvaluation.allCorrectAnswers;
	}
	
	public static Map<Identifier, Double> getMapping(AssessmentItem item, ChoiceInteraction interaction) {
		Map<Identifier,Double> scoreMap = null;
		if(interaction != null) {
			ResponseDeclaration responseDeclaration = item.getResponseDeclaration(interaction.getResponseIdentifier());
			if(responseDeclaration != null) {
				Mapping mapping = responseDeclaration.getMapping();
				if(mapping != null && mapping.getMapEntries() != null && !mapping.getMapEntries().isEmpty() ) {
					scoreMap = new HashMap<>();
					for(MapEntry entry:mapping.getMapEntries()) {
						SingleValue sValue = entry.getMapKey();
						if(sValue instanceof IdentifierValue) {
							Identifier identifier = ((IdentifierValue)sValue).identifierValue();
							scoreMap.put(identifier, entry.getMappedValue());
						}
					}
				}
			}
		}
		return scoreMap;
	}
	
	private void extractChoiceInteraction() {
		try(StringOutput sb = new StringOutput()) {
			List<Block> blocks = assessmentItem.getItemBody().getBlocks();
			for(Block block:blocks) {
				if(block instanceof ChoiceInteraction) {
					choiceInteraction = (ChoiceInteraction)block;
					responseIdentifier = choiceInteraction.getResponseIdentifier();
					shuffle = choiceInteraction.getShuffle();
					break;
				} else if(block != null) {
					serializeJqtiObject(block, sb);
				}
			}
			question = sb.toString();
		} catch(IOException e) {
			log.error("", e);
		}
		
		choices = new ArrayList<>();
		if(choiceInteraction != null) {
			choices.addAll(choiceInteraction.getSimpleChoices());
			orientation = choiceInteraction.getOrientation();
			cssClass = choiceInteraction.getClassAttr();
			maxChoices = choiceInteraction.getMaxChoices();
			minChoices = choiceInteraction.getMinChoices();
		}
	}
	
	@Override
	public Identifier getResponseIdentifier() {
		return responseIdentifier;
	}

	@Override
	public List<Answer> getAnswers() {
		List<SimpleChoice> simpleChoices = getChoices();
		List<Answer> answers = new ArrayList<>(simpleChoices.size());
		for(SimpleChoice choice:simpleChoices) {
			String choiceContent =  getHtmlHelper().flowStaticString(choice.getFlowStatics());
			String label = FilterFactory.getHtmlTagAndDescapingFilter().filter(choiceContent);
			answers.add(new Answer(choice.getIdentifier(), label));
		}
		return answers;
	}
	
	@Override
	public Interaction getInteraction() {
		return choiceInteraction;
	}
	
	public ChoiceInteraction getChoiceInteraction() {
		return choiceInteraction;
	}

	@Override
	public int getMaxChoices() {
		return maxChoices;
	}

	@Override
	public void setMaxChoices(int maxChoices) {
		this.maxChoices = maxChoices;
	}

	@Override
	public int getMinChoices() {
		return minChoices;
	}

	@Override
	public void setMinChoices(int minChoices) {
		this.minChoices = minChoices;
	}

	public boolean isShuffle() {
		return shuffle;
	}
	
	public void setShuffle(boolean shuffle) {
		this.shuffle = shuffle;
	}
	
	public Orientation getOrientation() {
		return orientation;
	}
	
	public void setOrientation(Orientation orientation) {
		this.orientation = orientation;
	}
	
	public boolean hasClassAttr(String classAttr) {
		return cssClass != null && cssClass.contains(classAttr);
	}
	
	public void addClass(String classAttr) {
		if(cssClass == null) {
			cssClass = new ArrayList<>();
		} 
		if(!cssClass.contains(classAttr)) {
			cssClass.add(classAttr);
		}
	}
	
	public void removeClass(String classAttr) {
		if(cssClass != null) {
			cssClass.remove(classAttr);
		}
	}
	
	/**
	 * @return A copy of the list of blocks which make the question.
	 * 		The list is a copy and modification will not be persisted.
	 */
	public List<Block> getQuestionBlocks() {
		List<Block> blocks = assessmentItem.getItemBody().getBlocks();
		List<Block> questionBlocks = new ArrayList<>(blocks.size());
		for(Block block:blocks) {
			if(block instanceof ChoiceInteraction) {
				break;
			} else if(block != null) {
				questionBlocks.add(block);
			}
		}
		return questionBlocks;
	}
	
	/**
	 * Return the HTML block before the choice interaction as a string.
	 * 
	 * @return
	 */
	@Override
	public String getQuestion() {
		return question;
	}
	
	@Override
	public void setQuestion(String html) {
		this.question = html;
	}
	
	@Override
	public List<SimpleChoice> getChoices() {
		return choices;
	}
	
	public SimpleChoice getChoice(Identifier identifier) {
		for(SimpleChoice choice:choices) {
			if(choice.getIdentifier().equals(identifier)) {
				return choice;
			}
		}
		return null;
	}
	
	public void addSimpleChoice(SimpleChoice choice) {
		if(choices == null) {
			choices = new ArrayList<>();
		}
		choices.add(choice);
	}
	
	public void setSimpleChoices(List<SimpleChoice> choices) {
		this.choices = new ArrayList<>(choices);
	}
	
	public void clearSimpleChoices() {
		if(choices != null) {
			choices.clear();
		}
	}

	public enum ScoreEvaluation {
		perAnswer,
		allCorrectAnswers
	}
}
