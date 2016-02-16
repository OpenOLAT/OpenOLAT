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
package org.olat.ims.qti21.ui.editor;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.filter.FilterFactory;
import org.olat.ims.qti21.model.xml.AssessmentHtmlBuilder;
import org.olat.ims.qti21.model.xml.ScoreBuilder;
import org.olat.ims.qti21.model.xml.interactions.ChoiceAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.ChoiceAssessmentItemBuilder.ScoreEvaluation;
import org.olat.ims.qti21.ui.editor.events.AssessmentItemEvent;

import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.SimpleChoice;

/**
 * 
 * Initial date: 08.12.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ChoiceScoreController extends FormBasicController {
	
	private static final String[] modeKeys = new String[]{
			ScoreEvaluation.allCorrectAnswers.name(), ScoreEvaluation.perAnswer.name()
		};
	
	private TextElement minScoreEl;
	private TextElement maxScoreEl;
	private SingleSelection assessmentModeEl;
	private final List<SimpleChoiceWrapper> wrappers = new ArrayList<>();
	
	private ChoiceAssessmentItemBuilder itemBuilder;
	
	private int counter = 0;
	
	public ChoiceScoreController(UserRequest ureq, WindowControl wControl, ChoiceAssessmentItemBuilder itemBuilder) {
		super(ureq, wControl);
		this.itemBuilder = itemBuilder;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		minScoreEl = uifactory.addTextElement("min.score", "min.score", 8, "0.0", formLayout);
		minScoreEl.setEnabled(false);
		
		ScoreBuilder maxScore = itemBuilder.getMaxScoreBuilder();
		String maxValue = maxScore == null ? "" : (maxScore.getScore() == null ? "" : maxScore.getScore().toString());
		maxScoreEl = uifactory.addTextElement("max.score", "max.score", 8, maxValue, formLayout);
		
		String[] modeValues = new String[]{
				translate("form.score.assessment.all.correct"),
				translate("form.score.assessment.per.answer")
		};
		assessmentModeEl = uifactory.addRadiosHorizontal("assessment.mode", "form.score.assessment.mode", formLayout, modeKeys, modeValues);
		if(itemBuilder.getScoreEvaluationMode() == ScoreEvaluation.perAnswer) {
			assessmentModeEl.select(ScoreEvaluation.perAnswer.name(), true);
		} else {
			assessmentModeEl.select(ScoreEvaluation.allCorrectAnswers.name(), true);
		}
		
		String scorePage = velocity_root + "/choices_score.html";
		FormLayoutContainer scoreCont = FormLayoutContainer.createCustomFormLayout("scores", getTranslator(), scorePage);
		formLayout.add(scoreCont);
		scoreCont.setLabel(null, null);
		
		for(SimpleChoice choice:itemBuilder.getSimpleChoices()) {
			String points = "";
			Double score = itemBuilder.getMapping(choice.getIdentifier());
			if(score != null) {
				points = score.toString();
			}
			String pointElId = "points_" + counter++;
			TextElement pointEl = uifactory.addTextElement(pointElId, null, 5, points, scoreCont);
			pointEl.setDisplaySize(5);
			scoreCont.add(pointElId, pointEl);
			wrappers.add(new SimpleChoiceWrapper(choice, pointEl));
		}
		scoreCont.contextPut("choices", wrappers);

		// Submit Button
		FormLayoutContainer buttonsContainer = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsContainer.setRootForm(mainForm);
		formLayout.add(buttonsContainer);
		uifactory.addFormSubmitButton("submit", buttonsContainer);
	}
	
	

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		allOk &= validateDouble(maxScoreEl);

		if(assessmentModeEl.isOneSelected() && assessmentModeEl.isSelected(1)) {
			for(SimpleChoiceWrapper wrapper:wrappers) {
				allOk &= validateDouble(wrapper.getPointsEl());
			}
		}
		
		return allOk & super.validateFormLogic(ureq);
	}
	
	private boolean validateDouble(TextElement el) {
		boolean allOk = true;
		
		String value = el.getValue();
		if(!StringHelper.containsNonWhitespace(value)) {
			el.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		} else {
			try {
				Double.parseDouble(value);
			} catch (NumberFormatException e) {
				el.setErrorKey("error.double", null);
				allOk &= false;
			}
		}
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String maxScoreValue = maxScoreEl.getValue();
		Double maxScore = Double.parseDouble(maxScoreValue);
		itemBuilder.setMaxScore(maxScore);
		itemBuilder.setMinScore(new Double(0d));
		
		if(assessmentModeEl.isOneSelected() && assessmentModeEl.isSelected(1)) {
			itemBuilder.setScoreEvaluationMode(ScoreEvaluation.perAnswer);
			itemBuilder.clearMapping();
			for(SimpleChoiceWrapper wrapper:wrappers) {
				String pointsStr = wrapper.getPointsEl().getValue();
				Double points = new Double(pointsStr);
				itemBuilder.setMapping(wrapper.getChoice().getIdentifier(), points);
			}
		} else {
			itemBuilder.setScoreEvaluationMode(ScoreEvaluation.allCorrectAnswers);
			itemBuilder.clearMapping();
		}

		fireEvent(ureq, new AssessmentItemEvent(AssessmentItemEvent.ASSESSMENT_ITEM_CHANGED, itemBuilder.getAssessmentItem()));
	}

	@Override
	protected void doDispose() {
		//
	}
	
	public final class SimpleChoiceWrapper {
		
		private final String summary;
		private final SimpleChoice choice;
		private final TextElement pointsEl;
		
		public SimpleChoiceWrapper(SimpleChoice choice, TextElement pointsEl) {
			this.choice = choice;
			this.pointsEl = pointsEl;
			pointsEl.setUserObject(this);
			if(choice != null) {
				String answer = new AssessmentHtmlBuilder().flowStaticString(choice.getFlowStatics());
				answer = FilterFactory.getHtmlTagAndDescapingFilter().filter(answer);
				answer = answer.trim();
				summary = Formatter.truncate(answer, 128);
			} else {
				summary = "";
			}
		}
		
		public boolean isCorrect() {
			return itemBuilder.isCorrect(choice);
		}
		
		public String getSummary() {
			return summary;
		}
		
		public TextElement getPointsEl() {
			return pointsEl;
		}
		
		public SimpleChoice getChoice() {
			return choice;
		}
	}
}
