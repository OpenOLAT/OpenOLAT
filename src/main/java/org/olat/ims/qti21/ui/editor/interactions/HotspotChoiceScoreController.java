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

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.olat.core.commons.services.image.ImageService;
import org.olat.core.commons.services.image.Size;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.filter.FilterFactory;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.ims.qti21.model.xml.AssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.ScoreBuilder;
import org.olat.ims.qti21.model.xml.interactions.HotspotAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.SimpleChoiceAssessmentItemBuilder.ScoreEvaluation;
import org.olat.ims.qti21.ui.editor.AssessmentTestEditorController;
import org.olat.ims.qti21.ui.editor.SyncAssessmentItem;
import org.olat.ims.qti21.ui.editor.events.AssessmentItemEvent;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.ed.ph.jqtiplus.node.item.interaction.graphic.HotspotChoice;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentItemRef;
import uk.ac.ed.ph.jqtiplus.types.Identifier;

/**
 * 
 * Initial date: 08.12.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class HotspotChoiceScoreController extends AssessmentItemRefEditorController implements SyncAssessmentItem {
	
	private static final String[] modeKeys = new String[]{
			ScoreEvaluation.allCorrectAnswers.name(), ScoreEvaluation.perAnswer.name()
		};
	
	private TextElement minScoreEl;
	private TextElement maxScoreEl;
	private SingleSelection assessmentModeEl;
	private FormLayoutContainer scoreCont;
	private final List<HotspotChoiceWrapper> wrappers = new ArrayList<>();
	
	private HotspotAssessmentItemBuilder itemBuilder;
	
	private int counter = 0;
	private final File itemFile;
	private final String backgroundMapperUri;

	@Autowired
	private ImageService imageService;
	
	public HotspotChoiceScoreController(UserRequest ureq, WindowControl wControl, HotspotAssessmentItemBuilder itemBuilder,
			AssessmentItemRef itemRef, File itemFile, boolean restrictedEdit, boolean readOnly) {
		super(ureq, wControl, itemRef, restrictedEdit, readOnly);
		setTranslator(Util.createPackageTranslator(AssessmentTestEditorController.class, getLocale()));
		this.itemFile = itemFile;
		this.itemBuilder = itemBuilder;
		backgroundMapperUri = registerMapper(ureq, new BackgroundMapper(itemFile));
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		super.initForm(formLayout, listener, ureq);
		setFormContextHelp("Configure test questions#_tab_score");

		ScoreBuilder minScore = itemBuilder.getMinScoreBuilder();
		String minValue = minScore == null ? "" : (minScore.getScore() == null ? "" : minScore.getScore().toString());
		minScoreEl = uifactory.addTextElement("min.score", "min.score", 8, minValue, formLayout);
		minScoreEl.setElementCssClass("o_sel_assessment_item_min_score");
		minScoreEl.setEnabled(!restrictedEdit && !readOnly);
		
		ScoreBuilder maxScore = itemBuilder.getMaxScoreBuilder();
		String maxValue = maxScore == null ? "" : (maxScore.getScore() == null ? "" : maxScore.getScore().toString());
		maxScoreEl = uifactory.addTextElement("max.score", "max.score", 8, maxValue, formLayout);
		maxScoreEl.setElementCssClass("o_sel_assessment_item_max_score");
		maxScoreEl.setEnabled(!restrictedEdit && !readOnly);
		
		String[] modeValues = new String[]{
				translate("form.score.assessment.all.correct"),
				translate("form.score.assessment.per.answer")
		};
		assessmentModeEl = uifactory.addRadiosHorizontal("assessment.mode", "form.score.assessment.mode", formLayout, modeKeys, modeValues);
		assessmentModeEl.addActionListener(FormEvent.ONCHANGE);
		assessmentModeEl.setEnabled(!restrictedEdit && !readOnly);
		if(itemBuilder.getScoreEvaluationMode() == ScoreEvaluation.perAnswer) {
			assessmentModeEl.select(ScoreEvaluation.perAnswer.name(), true);
		} else {
			assessmentModeEl.select(ScoreEvaluation.allCorrectAnswers.name(), true);
		}
		
		String scorePage = velocity_root + "/hotspot_choices_score.html";
		scoreCont = FormLayoutContainer.createCustomFormLayout("scores", getTranslator(), scorePage);
		formLayout.add(scoreCont);
		scoreCont.setLabel(null, null);
		
		for(HotspotChoice choice:itemBuilder.getHotspotChoices()) {
			HotspotChoiceWrapper wrapper = createHotspotChoiceWrapper(choice);
			wrappers.add(wrapper);
		}
		scoreCont.contextPut("choices", wrappers);
		scoreCont.contextPut("mapperUri", backgroundMapperUri);
		scoreCont.setVisible(assessmentModeEl.isSelected(1));
		
		updateBackground();

		// Submit Button
		FormLayoutContainer buttonsContainer = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsContainer.setRootForm(mainForm);
		buttonsContainer.setVisible(!readOnly);
		formLayout.add(buttonsContainer);
		uifactory.addFormSubmitButton("submit", buttonsContainer);
	}
	
	@Override
	public void sync(UserRequest ureq, AssessmentItemBuilder assessmentItemBuilder) {
		if(itemBuilder == assessmentItemBuilder) {
			Set<Identifier> choiceIdentifiers = new HashSet<>();
			for(HotspotChoice choice:itemBuilder.getHotspotChoices()) {
				HotspotChoiceWrapper wrapper = getHotspotChoiceWrapper(choice);
				if(wrapper == null) {
					wrappers.add(createHotspotChoiceWrapper(choice));
				}
				choiceIdentifiers.add(choice.getIdentifier());
			}
			
			for(Iterator<HotspotChoiceWrapper> wrapperIt=wrappers.iterator(); wrapperIt.hasNext(); ) {
				HotspotChoiceWrapper wrapper = wrapperIt.next();
				if(!choiceIdentifiers.contains(wrapper.getChoice().getIdentifier())) {
					wrapperIt.remove();
				}
			}
			
			updateBackground();
		}
	}

	protected void updateBackground() {
		File backgroundImage = null;
		if(StringHelper.containsNonWhitespace(itemBuilder.getBackground())) {
			File itemDirectory = itemFile.getParentFile();
			Path backgroundPath = itemDirectory.toPath().resolve(itemBuilder.getBackground());
			if(Files.exists(backgroundPath)) {
				backgroundImage = backgroundPath.toFile();
			}
		}
		
		if(backgroundImage != null) {
			String filename = backgroundImage.getName();
			Size size = imageService.getSize(new LocalFileImpl(backgroundImage), null);
			scoreCont.contextPut("filename", filename);
			if(size != null) {
				if(size.getHeight() > 0) {
					scoreCont.contextPut("height", Integer.toString(size.getHeight()));
				} else {
					scoreCont.contextRemove("height");
				}
				if(size.getWidth() > 0) {
					scoreCont.contextPut("width", Integer.toString(size.getWidth()));
				} else {
					scoreCont.contextRemove("width");
				}
			}
		} else {
			scoreCont.contextRemove("filename");
		}

		List<HotspotWrapper> choiceWrappers = new ArrayList<>();
		List<HotspotChoice> choices = itemBuilder.getHotspotChoices();
		for(int i=0; i<choices.size(); i++) {
			HotspotChoice choice = choices.get(i);
			choiceWrappers.add(new HotspotWrapper(choice, itemBuilder));
		}

		scoreCont.contextPut("hotspots", choiceWrappers);
	}
	
	private HotspotChoiceWrapper createHotspotChoiceWrapper(HotspotChoice choice) {
		String points = "";
		Double score = itemBuilder.getMapping(choice.getIdentifier());
		if(score != null) {
			points = score.toString();
		} else if(itemBuilder.isCorrect(choice)) {
			points = "1";
		} else {
			points = "0";
		}
		String pointElId = "points_" + counter++;
		TextElement pointEl = uifactory.addTextElement(pointElId, null, 5, points, scoreCont);
		pointEl.setDisplaySize(5);
		pointEl.setEnabled(!restrictedEdit && !readOnly);
		scoreCont.add(pointElId, pointEl);
		return new HotspotChoiceWrapper(choice, pointEl);
	}
	
	private HotspotChoiceWrapper getHotspotChoiceWrapper(HotspotChoice choice) {
		for(HotspotChoiceWrapper wrapper:wrappers) {
			if(wrapper.getChoice() == choice) {
				return wrapper;
			}
		}
		return null;
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		allOk &= validateMinMaxScores(minScoreEl, maxScoreEl);

		if(assessmentModeEl.isOneSelected() && assessmentModeEl.isSelected(1)) {
			for(HotspotChoiceWrapper wrapper:wrappers) {
				allOk &= validateDouble(wrapper.getPointsEl());
			}
		}
		
		return allOk;
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
		if(restrictedEdit || readOnly) return;
		
		super.formOK(ureq);
		String maxScoreValue = maxScoreEl.getValue();
		Double maxScore = Double.parseDouble(maxScoreValue);
		itemBuilder.setMaxScore(maxScore);
		String minScoreValue = minScoreEl.getValue();
		Double minScore = Double.parseDouble(minScoreValue);
		itemBuilder.setMinScore(minScore);
		
		if(assessmentModeEl.isOneSelected() && assessmentModeEl.isSelected(1)) {
			itemBuilder.setScoreEvaluationMode(ScoreEvaluation.perAnswer);
			itemBuilder.clearMapping();
			for(HotspotChoiceWrapper wrapper:wrappers) {
				String pointsStr = wrapper.getPointsEl().getValue();
				Double points = Double.valueOf(pointsStr);
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
	
	public final class HotspotChoiceWrapper {
		
		private final String summary;
		private final HotspotChoice choice;
		private final TextElement pointsEl;
		
		public HotspotChoiceWrapper(HotspotChoice choice, TextElement pointsEl) {
			this.choice = choice;
			this.pointsEl = pointsEl;
			pointsEl.setUserObject(this);
			if(choice != null) {
				String answer = choice.getHotspotLabel();
				if(!StringHelper.containsNonWhitespace(answer)) {
					answer = choice.getLabel();
				}
				if(!StringHelper.containsNonWhitespace(answer)) {
					answer = choice.getIdentifier().toString();
				}
				
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
		
		public HotspotChoice getChoice() {
			return choice;
		}
	}
	
	private static class BackgroundMapper implements Mapper {
		
		private final File itemFile;
		
		public BackgroundMapper(File itemFile) {
			this.itemFile = itemFile;
		}

		@Override
		public MediaResource handle(String relPath, HttpServletRequest request) {
			if(StringHelper.containsNonWhitespace(relPath)) {
				if(relPath.startsWith("/")) {
					relPath = relPath.substring(1);
				}
				
				File backgroundFile = new File(itemFile.getParentFile(), relPath);
				return new VFSMediaResource(new LocalFileImpl(backgroundFile));
			}
			return new NotFoundMediaResource();
		}
	}
}
