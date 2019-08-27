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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.ims.qti21.model.xml.AssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.SimpleChoiceAssessmentItemBuilder.ScoreEvaluation;

import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.Interaction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.Choice;
import uk.ac.ed.ph.jqtiplus.serialization.QtiSerializer;
import uk.ac.ed.ph.jqtiplus.types.Identifier;

/**
 * 
 * Initial date: 19.04.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public abstract class ChoiceAssessmentItemBuilder extends AssessmentItemBuilder {
	
	protected ScoreEvaluation scoreEvaluation;
	protected Map<Identifier,Double> scoreMapping;
	
	public ChoiceAssessmentItemBuilder(AssessmentItem assessmentItem, QtiSerializer qtiSerializer) {
		super(assessmentItem, qtiSerializer);
	}
	
	public abstract Interaction getInteraction();
	
	public ScoreEvaluation getScoreEvaluationMode() {
		return scoreEvaluation;
	}
	
	public void setScoreEvaluationMode(ScoreEvaluation scoreEvaluation) {
		this.scoreEvaluation = scoreEvaluation;
	}
	
	public Double getMapping(Identifier identifier) {
		Double score = null;
		if(scoreMapping != null) {
			score = scoreMapping.get(identifier);
		}
		return score;
	}
	
	public void clearMapping() {
		if(scoreMapping != null) {
			scoreMapping.clear();
		}
	}
	
	public void setMapping(Identifier identifier, Double score) {
		if(scoreMapping == null) {
			scoreMapping = new HashMap<>();
		}
		scoreMapping.put(identifier, score);
	}
	
	public abstract boolean scoreOfCorrectAnswerWarning();
	
	public abstract int getMaxPossibleCorrectAnswers();
	
	public abstract int getMaxChoices();
	
	public abstract void setMaxChoices(int choices);
	
	public abstract int getMinChoices();
	
	public abstract void setMinChoices(int choices);

	public abstract boolean isCorrect(Choice choice);
	
	public abstract List<? extends Choice> getChoices();
	
	public Choice getChoice(Identifier identifier) {
		List<? extends Choice> choices = getChoices();
		if(choices != null) {
			for(Choice choice:choices) {
				if(choice.getIdentifier().equals(identifier)) {
					return choice;
				}
			}
		}
		return null;
	}
}
