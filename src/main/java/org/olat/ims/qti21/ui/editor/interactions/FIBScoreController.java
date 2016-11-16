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
import java.util.Map;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.ims.qti21.model.xml.AssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.ScoreBuilder;
import org.olat.ims.qti21.model.xml.interactions.FIBAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.FIBAssessmentItemBuilder.AbstractEntry;
import org.olat.ims.qti21.model.xml.interactions.FIBAssessmentItemBuilder.NumericalEntry;
import org.olat.ims.qti21.model.xml.interactions.FIBAssessmentItemBuilder.TextEntry;
import org.olat.ims.qti21.model.xml.interactions.SimpleChoiceAssessmentItemBuilder.ScoreEvaluation;
import org.olat.ims.qti21.ui.editor.AssessmentTestEditorController;
import org.olat.ims.qti21.ui.editor.SyncAssessmentItem;
import org.olat.ims.qti21.ui.editor.events.AssessmentItemEvent;

import uk.ac.ed.ph.jqtiplus.node.test.AssessmentItemRef;
import uk.ac.ed.ph.jqtiplus.types.Identifier;

/**
 * 
 * Initial date: 25.02.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FIBScoreController extends AssessmentItemRefEditorController implements SyncAssessmentItem {
	
	private static final String[] modeKeys = new String[]{
			ScoreEvaluation.allCorrectAnswers.name(), ScoreEvaluation.perAnswer.name()
		};
	
	private TextElement minScoreEl;
	private TextElement maxScoreEl;
	private FormLayoutContainer scoreCont;
	private SingleSelection assessmentModeEl;

	private FIBAssessmentItemBuilder itemBuilder;
	private final List<TextEntryWrapper> wrappers = new ArrayList<>();
	
	private int counter = 0;
	
	public FIBScoreController(UserRequest ureq, WindowControl wControl,
			FIBAssessmentItemBuilder itemBuilder, AssessmentItemRef itemRef, boolean restrictedEdit) {
		super(ureq, wControl, itemRef, restrictedEdit);
		setTranslator(Util.createPackageTranslator(AssessmentTestEditorController.class, getLocale()));
		this.itemBuilder = itemBuilder;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormContextHelp("Test editor QTI 2.1 in detail#details_testeditor_score");
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
		assessmentModeEl.addActionListener(FormEvent.ONCHANGE);
		assessmentModeEl.setEnabled(!restrictedEdit);
		if(itemBuilder.getScoreEvaluationMode() == ScoreEvaluation.perAnswer) {
			assessmentModeEl.select(ScoreEvaluation.perAnswer.name(), true);
		} else {
			assessmentModeEl.select(ScoreEvaluation.allCorrectAnswers.name(), true);
		}
		
		String scorePage = velocity_root + "/fib_score.html";
		scoreCont = FormLayoutContainer.createCustomFormLayout("scores", getTranslator(), scorePage);
		formLayout.add(scoreCont);
		scoreCont.setLabel(null, null);
		
		for(AbstractEntry entry:itemBuilder.getOrderedTextEntries()) {
			wrappers.add(createTextEntryWrapper(entry));
		}
		scoreCont.contextPut("choices", wrappers);
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
			List<AbstractEntry> entries = itemBuilder.getOrderedTextEntries();
			for(AbstractEntry entry:entries) {
				TextEntryWrapper wrapper = getTextEntryWrapper(entry);
				if(wrapper == null) {
					wrappers.add(createTextEntryWrapper(entry));
				}
			}
			
			//remove removed entry
			for(Iterator<TextEntryWrapper> wrapperIt=wrappers.iterator(); wrapperIt.hasNext(); ) {
				Identifier responseIdentifier = wrapperIt.next().getEntry().getResponseIdentifier();
				if(itemBuilder.getTextEntry(responseIdentifier.toString()) == null) {
					wrapperIt.remove();
				}
			}
			
			//reorder the wrappers
			Map<AbstractEntry,TextEntryWrapper> wrapperMap = wrappers.stream()
					.collect(Collectors.toMap(w -> w.getEntry(), w -> w));
			List<TextEntryWrapper> reorderedWrappers = new ArrayList<>();
			for(AbstractEntry entry:entries) {
				TextEntryWrapper wrapper = wrapperMap.get(entry);
				if(wrapper != null) {
					reorderedWrappers.add(wrapper);
					wrapperMap.remove(entry);
				}
			}
			
			if(wrapperMap.size() > 0) {//paranoid security
				reorderedWrappers.addAll(wrapperMap.values());
			}
			wrappers.clear();
			wrappers.addAll(reorderedWrappers);
		}
	}
	
	private TextEntryWrapper getTextEntryWrapper(AbstractEntry entry) {
		for(TextEntryWrapper wrapper:wrappers) {
			if(wrapper.getEntry() == entry) {
				return wrapper;
			}
		}
		return null;
	}
	
	private TextEntryWrapper createTextEntryWrapper(AbstractEntry entry) {
		String points = "";
		Double score = entry.getScore();
		if(score != null) {
			points = score.toString();
		}
		String pointElId = "points_" + counter++;
		TextElement pointEl = uifactory.addTextElement(pointElId, null, 5, points, scoreCont);
		pointEl.setDisplaySize(5);
		pointEl.setEnabled(!restrictedEdit);
		scoreCont.add(pointElId, pointEl);
		return new TextEntryWrapper(entry, pointEl);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		allOk &= validateDouble(maxScoreEl);

		if(assessmentModeEl.isOneSelected() && assessmentModeEl.isSelected(1)) {
			for(TextEntryWrapper wrapper:wrappers) {
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
			for(TextEntryWrapper wrapper:wrappers) {
				String pointsStr = wrapper.getPointsEl().getValue();
				Double points = new Double(pointsStr);
				wrapper.getEntry().setScore(points);
			}
		} else {
			itemBuilder.setScoreEvaluationMode(ScoreEvaluation.allCorrectAnswers);
		}

		fireEvent(ureq, new AssessmentItemEvent(AssessmentItemEvent.ASSESSMENT_ITEM_CHANGED, itemBuilder.getAssessmentItem(), null));
	}

	@Override
	protected void doDispose() {
		//
	}
	
	public final class TextEntryWrapper {
		
		private final String summary;
		private final AbstractEntry entry;
		private final TextElement pointsEl;
		
		public TextEntryWrapper(AbstractEntry entry, TextElement pointsEl) {
			this.entry = entry;
			this.pointsEl = pointsEl;
			pointsEl.setUserObject(this);
			if(entry instanceof TextEntry) {
				summary = ((TextEntry)entry).getSolution();
			} else if(entry instanceof NumericalEntry) {
				Double solution = ((NumericalEntry)entry).getSolution();
				summary = solution == null ? "???" : solution.toString();
			} else {
				summary = "???";
			}
		}
		
		public String getSummary() {
			return summary;
		}
		
		public TextElement getPointsEl() {
			return pointsEl;
		}
		
		public AbstractEntry getEntry() {
			return entry;
		}
	}
}
