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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.ims.qti21.model.xml.AssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.ScoreBuilder;
import org.olat.ims.qti21.model.xml.interactions.FIBAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.FIBAssessmentItemBuilder.AbstractEntry;
import org.olat.ims.qti21.model.xml.interactions.FIBAssessmentItemBuilder.NumericalEntry;
import org.olat.ims.qti21.model.xml.interactions.FIBAssessmentItemBuilder.TextEntry;
import org.olat.ims.qti21.model.xml.interactions.FIBAssessmentItemBuilder.TextEntryAlternative;
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

	private static final String[] yesnoKeys = new String[]{ "y", "n"};
	private static final String[] modeKeys = new String[]{
			ScoreEvaluation.allCorrectAnswers.name(), ScoreEvaluation.perAnswer.name(), "perAnswerAndAlternatives"
		};
	
	private TextElement minScoreEl;
	private TextElement maxScoreEl;
	private FormLayoutContainer scoreCont;
	private SingleSelection assessmentModeEl;
	private SingleSelection duplicateAllowedEl;

	private FIBAssessmentItemBuilder itemBuilder;
	private final List<FIBEntryWrapper> wrappers = new ArrayList<>();
	
	private int counter = 0;
	
	public FIBScoreController(UserRequest ureq, WindowControl wControl,
			FIBAssessmentItemBuilder itemBuilder, AssessmentItemRef itemRef, boolean restrictedEdit, boolean readOnly) {
		super(ureq, wControl, itemRef, restrictedEdit, readOnly);
		setTranslator(Util.createPackageTranslator(AssessmentTestEditorController.class, getLocale()));
		this.itemBuilder = itemBuilder;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormContextHelp("manual_user/tests/Configure_test_questions/#score");
		super.initForm(formLayout, listener, ureq);
		minScoreEl = uifactory.addTextElement("min.score", "min.score", 8, "0.0", formLayout);
		minScoreEl.setElementCssClass("o_sel_assessment_item_min_score");
		minScoreEl.setEnabled(false);
		
		ScoreBuilder maxScore = itemBuilder.getMaxScoreBuilder();
		String maxValue = maxScore == null ? "" : (maxScore.getScore() == null ? "" : maxScore.getScore().toString());
		maxScoreEl = uifactory.addTextElement("max.score", "max.score", 8, maxValue, formLayout);
		maxScoreEl.setElementCssClass("o_sel_assessment_item_max_score");
		maxScoreEl.setEnabled(!restrictedEdit && !readOnly);
		
		String[] yesnoValues = new String[]{ translate("yes"), translate("no") };
		duplicateAllowedEl = uifactory.addRadiosHorizontal("duplicate", "form.imd.duplicate.answers", formLayout, yesnoKeys, yesnoValues);
		duplicateAllowedEl.setElementCssClass("o_sel_assessment_item_fib_duplicate");
		duplicateAllowedEl.setEnabled(!restrictedEdit && !readOnly);
		duplicateAllowedEl.setVisible(hasSeveralTextEntryWithSharedAlternatives());
		duplicateAllowedEl.setHelpTextKey("form.imd.duplicate.answers.hint", null);
		if(itemBuilder.isAllowDuplicatedAnswers()) {
			duplicateAllowedEl.select(yesnoKeys[0], true);
		} else {
			duplicateAllowedEl.select(yesnoKeys[1], true);
		}
		
		String[] modeValues = new String[]{
				translate("form.score.assessment.all.correct"),
				translate("form.score.assessment.per.answer"),
				translate("form.score.assessment.per.answer.and.alternatives")
		};
		assessmentModeEl = uifactory.addRadiosHorizontal("assessment.mode", "form.score.assessment.mode", formLayout, modeKeys, modeValues);
		assessmentModeEl.addActionListener(FormEvent.ONCHANGE);
		assessmentModeEl.setEnabled(!restrictedEdit && !readOnly);
		if(itemBuilder.getScoreEvaluationMode() == ScoreEvaluation.perAnswer) {
			if(itemBuilder.alternativesWithSpecificScore()) {
				assessmentModeEl.select("perAnswerAndAlternatives", true);
			} else {
				assessmentModeEl.select(ScoreEvaluation.perAnswer.name(), true);
			}
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
		scoreCont.setVisible(assessmentModeEl.isSelected(1) || assessmentModeEl.isSelected(2));
		scoreCont.contextPut("withAlternatives", Boolean.valueOf(assessmentModeEl.isSelected(2)));

		// Submit Button
		FormLayoutContainer buttonsContainer = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsContainer.setRootForm(mainForm);
		buttonsContainer.setVisible(!readOnly);
		formLayout.add(buttonsContainer);
		uifactory.addFormSubmitButton("submit", buttonsContainer);
	}
	
	private boolean hasSeveralTextEntryWithSharedAlternatives() {
		int count = 0;
		
		List<AbstractEntry> entries = itemBuilder.getOrderedTextEntries();
		for(AbstractEntry entry:entries) {
			if(entry instanceof TextEntry) {
				count++;
			}
		}

		return count > 1 && itemBuilder.entriesSharesAlternatives();
	}

	@Override
	public void sync(UserRequest ureq, AssessmentItemBuilder assessmentItemBuilder) {
		if(itemBuilder == assessmentItemBuilder) {
			List<AbstractEntry> entries = itemBuilder.getOrderedTextEntries();
			for(AbstractEntry entry:entries) {
				FIBEntryWrapper wrapper = getTextEntryWrapper(entry);
				if(wrapper == null) {
					wrappers.add(createTextEntryWrapper(entry));
				} else if(entry instanceof TextEntry) {
					syncAlternatives(wrapper, (TextEntry)entry);
				}
			}
			
			//remove removed entry
			for(Iterator<FIBEntryWrapper> wrapperIt=wrappers.iterator(); wrapperIt.hasNext(); ) {
				Identifier responseIdentifier = wrapperIt.next().getEntry().getResponseIdentifier();
				if(itemBuilder.getTextEntry(responseIdentifier.toString()) == null) {
					wrapperIt.remove();
				}
			}
			
			//reorder the wrappers
			Map<AbstractEntry,FIBEntryWrapper> wrapperMap = new HashMap<>();
			for(FIBEntryWrapper wrapper:wrappers) {
				wrapperMap.put(wrapper.getEntry(), wrapper);
			}
			List<FIBEntryWrapper> reorderedWrappers = new ArrayList<>();
			for(AbstractEntry entry:entries) {
				FIBEntryWrapper wrapper = wrapperMap.get(entry);
				if(wrapper != null) {
					reorderedWrappers.add(wrapper);
					wrapperMap.remove(entry);
				}
			}
			
			if(wrapperMap.size() > 0) {//paranoid security
				for(FIBEntryWrapper wrapper:wrapperMap.values()) {
					if(!reorderedWrappers.contains(wrapper)) {
						reorderedWrappers.add(wrapper);
					}
				}
			}
			wrappers.clear();
			wrappers.addAll(reorderedWrappers);
			
			// duplicated only for text entry
			duplicateAllowedEl.setVisible(hasSeveralTextEntryWithSharedAlternatives());
		}
	}
	
	private void syncAlternatives(FIBEntryWrapper wrapper, TextEntry entry) {
		List<FIBAlternativeWrapper> alternativeWrappers = wrapper.getAlternatives();
		List<TextEntryAlternative> alternatives = entry.getAlternatives();
		
		for(Iterator<FIBAlternativeWrapper> it=alternativeWrappers.iterator(); it.hasNext(); ) {
			FIBAlternativeWrapper alternativeWrapper = it.next();
			
			boolean found = false;
			if(alternatives != null && !alternatives.isEmpty()) {
				for(TextEntryAlternative alternative:alternatives) {
					if(alternativeWrapper.getAlternative() == alternative) {
						found = true;
					}
				}
			}
			
			if(!found) {
				it.remove();
			}
		}
		
		if(alternatives != null && !alternatives.isEmpty()) {
			for(TextEntryAlternative alternative:alternatives) {
				boolean found = false;
				for(FIBAlternativeWrapper alternativeWrapper:alternativeWrappers) {
					if(alternativeWrapper.getAlternative() == alternative) {
						found = true;
					}
				}
				
				if(!found) {
					FIBAlternativeWrapper alternativeWrapper = createAlternativeWrapper(alternative);
					wrapper.getAlternatives().add(alternativeWrapper);
				}
			}
		}
	}
	
	private FIBEntryWrapper getTextEntryWrapper(AbstractEntry entry) {
		for(FIBEntryWrapper wrapper:wrappers) {
			if(wrapper.getEntry() == entry) {
				return wrapper;
			}
		}
		return null;
	}
	
	private FIBEntryWrapper createTextEntryWrapper(AbstractEntry entry) {
		String points = "";
		Double score = entry.getScore();
		if(score != null) {
			points = score.toString();
		}
		String pointElId = "points_" + counter++;
		TextElement pointEl = uifactory.addTextElement(pointElId, null, 5, points, scoreCont);
		pointEl.setDomReplacementWrapperRequired(false);
		pointEl.setDisplaySize(5);
		pointEl.setEnabled(!restrictedEdit && !readOnly);
		scoreCont.add(pointElId, pointEl);
		
		List<FIBAlternativeWrapper> alternativeWrappers = new ArrayList<>();
		if(entry instanceof TextEntry) {
			TextEntry textEntry = (TextEntry)entry;
			if(textEntry.getAlternatives() != null) {
				for(TextEntryAlternative alternative:textEntry.getAlternatives()) {
					FIBAlternativeWrapper alternativeWrapper = createAlternativeWrapper(alternative);
					alternativeWrappers.add(alternativeWrapper);
				}
			}
		}

		return new FIBEntryWrapper(entry, pointEl, alternativeWrappers);
	}
	
	private FIBAlternativeWrapper createAlternativeWrapper(TextEntryAlternative alternative) {
		String altPointElId = "points_" + counter++;
		String altScoreStr = Double.toString(alternative.getScore());
		TextElement altPointEl = uifactory.addTextElement(altPointElId, null, 5, altScoreStr, scoreCont);
		altPointEl.setDisplaySize(5);
		altPointEl.setEnabled(!restrictedEdit && !readOnly);
		scoreCont.add(altPointElId, altPointEl);
		return new FIBAlternativeWrapper(alternative, altPointEl);
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		allOk &= validateDouble(maxScoreEl);

		if(assessmentModeEl.isOneSelected() && (assessmentModeEl.isSelected(1) || assessmentModeEl.isSelected(2))) {
			boolean alternativeSpecificScore = assessmentModeEl.isSelected(2);
			for(FIBEntryWrapper wrapper:wrappers) {
				allOk &= validateDouble(wrapper.getPointsEl());
				if(alternativeSpecificScore && wrapper.getAlternatives() != null && !wrapper.getAlternatives().isEmpty()) {
					for(FIBAlternativeWrapper alternativeWrapper:wrapper.getAlternatives()) {
						allOk &= validateDouble(alternativeWrapper.getPointsEl());
					}
				}
			}
		}
		
		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(assessmentModeEl.isOneSelected()) {
			updateScoresUI();
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void updateScoresUI() {
		boolean perAnswer = assessmentModeEl.isSelected(1);
		boolean perAnswerAndVariants = assessmentModeEl.isSelected(2);
		scoreCont.setVisible(perAnswer || perAnswerAndVariants);
		scoreCont.contextPut("withAlternatives", Boolean.valueOf(perAnswerAndVariants));
		if(perAnswer || perAnswerAndVariants) {
			for(FIBEntryWrapper wrapper:wrappers) {
				AbstractEntry entry = wrapper.getEntry();
				Double points = entry.getScore();
				if(points != null && points.doubleValue() == -1.0d) {//replace the all answers score
					wrapper.getEntry().setScore(1.0d);
					wrapper.getPointsEl().setValue("1.0");
					points = Double.valueOf(1.0d);
				}
				
				if(entry instanceof TextEntry && wrapper.getAlternatives() != null && !wrapper.getAlternatives().isEmpty()) {
					for(FIBAlternativeWrapper alternativeWrapper:wrapper.getAlternatives()) {
						TextEntryAlternative alternative = alternativeWrapper.getAlternative();
						if(StringHelper.containsNonWhitespace(alternativeWrapper.getPointsEl().getValue())) {
							if(points != null && (alternative.getScore() == 1.0d || alternative.getScore() == -1.0d)) {
								alternative.setScore(points.doubleValue());
								alternativeWrapper.getPointsEl().setValue(points.toString());
							}
						} else {
							if(alternative.getScore() >= 0.0) {
								alternativeWrapper.getPointsEl().setValue(Double.toString(alternative.getScore()));
							} else if(points != null && points.doubleValue() == -1.0d) {
								alternative.setScore(points.doubleValue());
								alternativeWrapper.getPointsEl().setValue(points.toString());
							}
						}
					}
				}
			}
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(restrictedEdit || readOnly) return;
		
		super.formOK(ureq);
		String maxScoreValue = maxScoreEl.getValue();
		Double maxScore = Double.parseDouble(maxScoreValue);
		itemBuilder.setMaxScore(maxScore);
		itemBuilder.setMinScore(Double.valueOf(0.0d));
		
		if(duplicateAllowedEl.isVisible()) {
			boolean allowDuplicates = duplicateAllowedEl.isOneSelected() && duplicateAllowedEl.isSelected(0);
			itemBuilder.setAllowDuplicatedAnswers(allowDuplicates);
		} else {
			itemBuilder.setAllowDuplicatedAnswers(true);
		}
		
		if(assessmentModeEl.isOneSelected() && (assessmentModeEl.isSelected(1) || assessmentModeEl.isSelected(2))) {
			itemBuilder.setScoreEvaluationMode(ScoreEvaluation.perAnswer);
			boolean alternativeSpecificScore = assessmentModeEl.isSelected(2);
			
			for(FIBEntryWrapper wrapper:wrappers) {
				String pointsStr = wrapper.getPointsEl().getValue();
				Double points = Double.valueOf(pointsStr);
				wrapper.getEntry().setScore(points);
				if(wrapper.getAlternatives() != null && !wrapper.getAlternatives().isEmpty()) {
					if(alternativeSpecificScore) {
						for(FIBAlternativeWrapper alternativeWrapper:wrapper.getAlternatives()) {
							String scoreStr = alternativeWrapper.getPointsEl().getValue();
							double score = Double.parseDouble(scoreStr);
							alternativeWrapper.getAlternative().setScore(score);
						}
					} else {
						for(FIBAlternativeWrapper alternativeWrapper:wrapper.getAlternatives()) {
							alternativeWrapper.getAlternative().setScore(points.doubleValue());
						}
					}
				}
			}
		} else {
			itemBuilder.setScoreEvaluationMode(ScoreEvaluation.allCorrectAnswers);
		}

		fireEvent(ureq, new AssessmentItemEvent(AssessmentItemEvent.ASSESSMENT_ITEM_CHANGED, itemBuilder.getAssessmentItem(), null));
	}
	
	public final class FIBEntryWrapper {
		
		private final AbstractEntry entry;
		private final TextElement pointsEl;
		private final List<FIBAlternativeWrapper> alternatives;
		
		public FIBEntryWrapper(AbstractEntry entry, TextElement pointsEl, List<FIBAlternativeWrapper> alternatives) {
			this.entry = entry;
			this.pointsEl = pointsEl;
			this.alternatives = alternatives;
			pointsEl.setUserObject(this);
		}

		public String getSummary() {
			String summary;
			if(entry instanceof TextEntry) {
				summary = ((TextEntry)entry).getSolution();
			} else if(entry instanceof NumericalEntry) {
				Double solution = ((NumericalEntry)entry).getSolution();
				summary = solution == null ? "???" : solution.toString();
			} else {
				summary = "???";
			}
			return summary;
		}
		
		public TextElement getPointsEl() {
			return pointsEl;
		}
		
		public AbstractEntry getEntry() {
			return entry;
		}
		
		public List<FIBAlternativeWrapper> getAlternatives() {
			return alternatives;
		}
		
		@Override
		public int hashCode() {
			return entry.hashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			if(this == obj) {
				return true;
			}
			if(obj instanceof FIBEntryWrapper) {
				FIBEntryWrapper w = (FIBEntryWrapper)obj;
				return entry.getResponseIdentifier() != null
						&& entry.getResponseIdentifier().equals(w.entry.getResponseIdentifier());
			}
			return false;
		}
	}
	
	public final class FIBAlternativeWrapper {
		
		private final TextElement pointsEl;
		private final TextEntryAlternative alternative;
		
		public FIBAlternativeWrapper(TextEntryAlternative alternative, TextElement pointsEl) {
			this.pointsEl = pointsEl;
			this.alternative = alternative;
		}
		
		public String getSummary() {
			return alternative.getAlternative();
		}
		
		public TextElement getPointsEl() {
			return pointsEl;
		}
		
		public TextEntryAlternative getAlternative() {
			return alternative;
		}
	}
}
