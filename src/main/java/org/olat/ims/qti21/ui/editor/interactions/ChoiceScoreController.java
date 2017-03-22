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
package org.olat.ims.qti21.ui.editor.interactions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Formatter;
import org.olat.core.util.Util;
import org.olat.core.util.filter.FilterFactory;
import org.olat.ims.qti21.model.xml.AssessmentHtmlBuilder;
import org.olat.ims.qti21.model.xml.AssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.ScoreBuilder;
import org.olat.ims.qti21.model.xml.interactions.ChoiceAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.SimpleChoiceAssessmentItemBuilder.ScoreEvaluation;
import org.olat.ims.qti21.ui.editor.AssessmentTestEditorController;
import org.olat.ims.qti21.ui.editor.SyncAssessmentItem;
import org.olat.ims.qti21.ui.editor.events.AssessmentItemEvent;

import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.Choice;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.SimpleChoice;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.content.Hottext;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentItemRef;
import uk.ac.ed.ph.jqtiplus.types.Identifier;

/**
 * 
 * Initial date: 08.12.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ChoiceScoreController extends AssessmentItemRefEditorController implements SyncAssessmentItem {
	
	private static final String[] modeKeys = new String[]{
			ScoreEvaluation.allCorrectAnswers.name(), ScoreEvaluation.perAnswer.name()
		};
	
	private TextElement minScoreEl;
	private TextElement maxScoreEl;
	private SingleSelection assessmentModeEl;
	private FormLayoutContainer scoreCont;
	private final List<ChoiceWrapper> wrappers = new ArrayList<>();
	
	private final ChoiceAssessmentItemBuilder itemBuilder;
	
	private int counter = 0;
	private final String contextHelpUrl;
	
	public ChoiceScoreController(UserRequest ureq, WindowControl wControl,
			ChoiceAssessmentItemBuilder itemBuilder, AssessmentItemRef itemRef, boolean restrictedEdit,
			String contextHelpUrl) {
		super(ureq, wControl, itemRef, restrictedEdit);
		setTranslator(Util.createPackageTranslator(AssessmentTestEditorController.class, getLocale()));
		this.itemBuilder = itemBuilder;
		this.contextHelpUrl = contextHelpUrl;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormContextHelp(contextHelpUrl);
		super.initForm(formLayout, listener, ureq);
		
		minScoreEl = uifactory.addTextElement("min.score", "min.score", 8, "0.0", formLayout);
		minScoreEl.setEnabled(false);
		minScoreEl.setEnabled(!restrictedEdit);
		
		ScoreBuilder maxScore = itemBuilder.getMaxScoreBuilder();
		String maxValue = maxScore == null ? "" : (maxScore.getScore() == null ? "" : maxScore.getScore().toString());
		maxScoreEl = uifactory.addTextElement("max.score", "max.score", 8, maxValue, formLayout);
		maxScoreEl.setEnabled(!restrictedEdit);
		
		String[] modeValues = new String[]{
				translate("form.score.assessment.all.correct"),
				translate("form.score.assessment.per.answer")
		};
		assessmentModeEl = uifactory.addRadiosHorizontal("assessment.mode", "form.score.assessment.mode", formLayout, modeKeys, modeValues);
		assessmentModeEl.setEnabled(!restrictedEdit);
		assessmentModeEl.addActionListener(FormEvent.ONCHANGE);
		if(itemBuilder.getScoreEvaluationMode() == ScoreEvaluation.perAnswer) {
			assessmentModeEl.select(ScoreEvaluation.perAnswer.name(), true);
		} else {
			assessmentModeEl.select(ScoreEvaluation.allCorrectAnswers.name(), true);
		}
		
		String scorePage = velocity_root + "/choices_score.html";
		scoreCont = FormLayoutContainer.createCustomFormLayout("scores", getTranslator(), scorePage);
		formLayout.add(scoreCont);
		scoreCont.setLabel(null, null);
		
		for(Choice choice:itemBuilder.getChoices()) {
			ChoiceWrapper wrapper = createChoiceWrapper(choice);
			wrappers.add(wrapper);
		}
		scoreCont.contextPut("choices", wrappers);
		scoreCont.contextPut("restrictedEdit", restrictedEdit);
		scoreCont.setVisible(assessmentModeEl.isSelected(1));

		// Submit Button
		FormLayoutContainer buttonsContainer = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsContainer.setRootForm(mainForm);
		formLayout.add(buttonsContainer);
		uifactory.addFormSubmitButton("submit", buttonsContainer);
	}
	
	@Override
	public void sync(UserRequest ureq, AssessmentItemBuilder assessmentItemBuilder) {
		if(itemBuilder == assessmentItemBuilder) {
			for(Choice choice:itemBuilder.getChoices()) {
				ChoiceWrapper wrapper = getChoiceWrapper(choice);
				if(wrapper == null) {
					wrappers.add(createChoiceWrapper(choice));
				}
			}
			
			for(Iterator<ChoiceWrapper> wrapperIt=wrappers.iterator(); wrapperIt.hasNext(); ) {
				Identifier choiceIdentifier = wrapperIt.next().getChoice().getIdentifier();
				if(itemBuilder.getChoice(choiceIdentifier) == null) {
					wrapperIt.remove();
				}
			}
		}
	}
	
	private ChoiceWrapper createChoiceWrapper(Choice choice) {
		String points = "";
		Double score = itemBuilder.getMapping(choice.getIdentifier());
		if(score != null) {
			points = score.toString();
		}
		String pointElId = "points_" + counter++;
		TextElement pointEl = uifactory.addTextElement(pointElId, null, 5, points, scoreCont);
		pointEl.setDisplaySize(5);
		pointEl.setEnabled(!restrictedEdit);
		scoreCont.add(pointElId, pointEl);
		return new ChoiceWrapper(choice, pointEl);
	}
	
	private ChoiceWrapper getChoiceWrapper(Choice choice) {
		for(ChoiceWrapper wrapper:wrappers) {
			if(wrapper.getChoice().getIdentifier().equals(choice.getIdentifier())) {
				return wrapper;
			}
		}
		return null;
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		allOk &= validateDouble(maxScoreEl);

		if(assessmentModeEl.isOneSelected() && assessmentModeEl.isSelected(1)) {
			for(ChoiceWrapper wrapper:wrappers) {
				allOk &= validateDouble(wrapper.getPointsEl());
			}
		}
		
		return allOk & super.validateFormLogic(ureq);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(assessmentModeEl.isOneSelected()) {
			scoreCont.setVisible(assessmentModeEl.isSelected(1));
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(restrictedEdit) return;
		
		super.formOK(ureq);
		String maxScoreValue = maxScoreEl.getValue();
		Double maxScore = Double.parseDouble(maxScoreValue);
		itemBuilder.setMaxScore(maxScore);
		itemBuilder.setMinScore(new Double(0d));
		
		if(assessmentModeEl.isOneSelected() && assessmentModeEl.isSelected(1)) {
			itemBuilder.setScoreEvaluationMode(ScoreEvaluation.perAnswer);
			itemBuilder.clearMapping();
			for(ChoiceWrapper wrapper:wrappers) {
				String pointsStr = wrapper.getPointsEl().getValue();
				Double points = new Double(pointsStr);
				itemBuilder.setMapping(wrapper.getChoice().getIdentifier(), points);
			}
		} else {
			itemBuilder.setScoreEvaluationMode(ScoreEvaluation.allCorrectAnswers);
			itemBuilder.clearMapping();
		}

		fireEvent(ureq, new AssessmentItemEvent(AssessmentItemEvent.ASSESSMENT_ITEM_CHANGED, itemBuilder.getAssessmentItem(), null));
	}

	@Override
	protected void doDispose() {
		//
	}
	
	public final class ChoiceWrapper {
		
		private final String summary;
		private final Choice choice;
		private final TextElement pointsEl;
		
		public ChoiceWrapper(Choice choice, TextElement pointsEl) {
			this.choice = choice;
			this.pointsEl = pointsEl;
			pointsEl.setUserObject(this);
			if(choice instanceof SimpleChoice) {
				String answer = new AssessmentHtmlBuilder().flowStaticString(((SimpleChoice)choice).getFlowStatics());
				answer = FilterFactory.getHtmlTagAndDescapingFilter().filter(answer);
				answer = answer.trim();
				summary = Formatter.truncate(answer, 128);
			} else if(choice instanceof Hottext) {
				String answer = new AssessmentHtmlBuilder().inlineStaticString(((Hottext)choice).getInlineStatics());
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
		
		public Choice getChoice() {
			return choice;
		}
	}
}
