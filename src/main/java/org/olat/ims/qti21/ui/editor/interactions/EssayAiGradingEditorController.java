/**
 * <a href="https://www.openolat.org">
 * OpenOlat - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.ims.qti21.ui.editor.interactions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.services.ai.AiEssayGradingService;
import org.olat.core.commons.services.ai.AiModule;
import org.olat.core.commons.services.ai.essay.AiBloomLevel;
import org.olat.core.commons.services.ai.essay.EssayAiGrading;
import org.olat.core.commons.services.ai.essay.EssayAiGradingFileStore;
import org.olat.core.commons.services.ai.essay.EssayFormativeFeedbackService;
import org.olat.core.commons.services.ai.essay.EssayItemDraft;
import org.olat.core.commons.services.ai.ui.AiEssayGradingTestController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextAreaElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.ims.qti21.model.QTI21QuestionType;
import org.olat.ims.qti21.model.xml.interactions.EssayAssessmentItemBuilder;
import org.olat.ims.qti21.ui.editor.AssessmentTestEditorController;
import org.olat.ims.qti21.ui.editor.events.AssessmentItemEvent;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 *
 * Editor for the {@link EssayAiGrading} metadata of a QTI essay item.
 * Shown as the "AI Feedback" tab inside the QTI essay editor whenever the
 * AI essay-grading feature is admin-enabled. Loads the existing row by
 * {@code (assessmentItemIdentifier, repositoryEntryKey)} and on save
 * recomputes the integrity {@code contentHash}, then either creates a
 * fresh row (manual-essay → AI-essay upgrade) or updates the existing one.
 * <p>
 * Cross-field validation:
 * <ul>
 *   <li>key-point weights sum to 1.0 ± 0.01</li>
 *   <li>rubric-criteria weights sum to 1.0 ± 0.01</li>
 *   <li>{@code languageTag} is a non-empty BCP-47 (verified via
 *       {@link Locale#forLanguageTag(String)})</li>
 * </ul>
 *
 * Initial date: 2026-04-20<br>
 *
 * @author Florian Gnägi, gnaegi, https://www.frentix.com
 *
 */
public class EssayAiGradingEditorController extends FormBasicController {

	private static final Logger log = Tracing.createLoggerFor(EssayAiGradingEditorController.class);

	private static final double WEIGHT_SUM_TOLERANCE = 0.01;
	private static final ObjectMapper MAPPER = new ObjectMapper();
	private static final String DIFFICULTY_UNKNOWN_KEY = "unknown";

	private TextElement learningObjectiveEl;
	private TextAreaElement referenceExcerptEl;
	private TextAreaElement modelAnswerEl;
	private TextAreaElement gradingHintsEl;
	private SingleSelection bloomLevelEl;
	private TextElement languageTagEl;
	private SingleSelection difficultyEl;

	private FormLayoutContainer keyPointsContainer;
	private FormLink addKeyPointButton;
	private final List<KeyPointRow> keyPointRows = new ArrayList<>();
	private int keyPointRowCounter;

	private FormLayoutContainer rubricCriteriaContainer;
	private FormLink addRubricCriterionButton;
	private final List<RubricCriterionRow> rubricCriterionRows = new ArrayList<>();
	private int rubricCriterionRowCounter;

	private FormLayoutContainer misconceptionsContainer;
	private FormLink addMisconceptionButton;
	private final List<MisconceptionRow> misconceptionRows = new ArrayList<>();
	private int misconceptionRowCounter;

	private final EssayAssessmentItemBuilder itemBuilder;
	private final File questionDir;
	private final boolean readOnly;

	private EssayAiGrading grading;

	private FormLink testFeedbackButton;
	private CloseableModalController cmc;
	private AiEssayGradingTestController testFeedbackCtrl;

	@Autowired
	private EssayAiGradingFileStore essayAiGradingFileStore;
	@Autowired
	private AiEssayGradingService aiEssayGradingService;
	@Autowired
	private AiModule aiModule;

	public EssayAiGradingEditorController(UserRequest ureq, WindowControl wControl,
			EssayAssessmentItemBuilder itemBuilder, File questionDir, boolean readOnly) {
		super(ureq, wControl, LAYOUT_DEFAULT_2_10);
		setTranslator(Util.createPackageTranslator(AssessmentTestEditorController.class, getLocale()));
		this.itemBuilder = itemBuilder;
		this.questionDir = questionDir;
		this.readOnly = readOnly;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		grading = essayAiGradingFileStore.load(questionDir);

		learningObjectiveEl = uifactory.addTextElement("ai.grading.learning.objective",
				"ai.grading.learning.objective", 1024,
				value(grading == null ? null : grading.getLearningObjective()), formLayout);
		learningObjectiveEl.setEnabled(!readOnly);

		referenceExcerptEl = uifactory.addTextAreaElement("ai.grading.reference.excerpt",
				"ai.grading.reference.excerpt", -1, 6, 80, true, false,
				value(grading == null ? null : grading.getReferenceExcerpt()), formLayout);
		referenceExcerptEl.setEnabled(!readOnly);

		modelAnswerEl = uifactory.addTextAreaElement("ai.grading.model.answer",
				"ai.grading.model.answer", -1, 8, 80, true, false,
				value(grading == null ? null : grading.getModelAnswer()), formLayout);
		modelAnswerEl.setEnabled(!readOnly);

		gradingHintsEl = uifactory.addTextAreaElement("ai.grading.hints",
				"ai.grading.hints", -1, 4, 80, true, false,
				value(grading == null ? null : grading.getGradingHints()), formLayout);
		gradingHintsEl.setEnabled(!readOnly);

		SelectionValues bloomKeys = new SelectionValues();
		for (AiBloomLevel level : AiBloomLevel.values()) {
			bloomKeys.add(SelectionValues.entry(level.name(),
					translate("ai.grading.bloom." + level.name())));
		}
		bloomLevelEl = uifactory.addDropdownSingleselect("ai.grading.bloom",
				"ai.grading.bloom", formLayout, bloomKeys.keys(), bloomKeys.values(), null);
		bloomLevelEl.setEnabled(!readOnly);
		String currentBloom = grading == null ? null : grading.getBloomLevel();
		if (currentBloom != null && bloomKeys.containsKey(currentBloom)) {
			bloomLevelEl.select(currentBloom, true);
		} else {
			bloomLevelEl.select(AiBloomLevel.UNDERSTAND.name(), true);
		}

		languageTagEl = uifactory.addTextElement("ai.grading.language.tag",
				"ai.grading.language.tag", 16,
				value(grading == null ? null : grading.getLanguage()), formLayout);
		languageTagEl.setEnabled(!readOnly);

		SelectionValues difficultyKeys = new SelectionValues();
		difficultyKeys.add(SelectionValues.entry(DIFFICULTY_UNKNOWN_KEY,
				translate("ai.grading.difficulty.unknown")));
		for (int i = 1; i <= 5; i++) {
			String key = String.valueOf(i);
			difficultyKeys.add(SelectionValues.entry(key, key));
		}
		difficultyEl = uifactory.addDropdownSingleselect("ai.grading.difficulty",
				"ai.grading.difficulty", formLayout,
				difficultyKeys.keys(), difficultyKeys.values(), null);
		difficultyEl.setEnabled(!readOnly);
		Integer currentDifficulty = grading == null ? null : grading.getDifficulty();
		String difficultyKey = currentDifficulty == null
				? DIFFICULTY_UNKNOWN_KEY : String.valueOf(currentDifficulty);
		if (difficultyKeys.containsKey(difficultyKey)) {
			difficultyEl.select(difficultyKey, true);
		} else {
			difficultyEl.select(DIFFICULTY_UNKNOWN_KEY, true);
		}

		// Key points repeating rows
		keyPointsContainer = FormLayoutContainer.createCustomFormLayout("ai.grading.keypoints",
				getTranslator(), velocity_root + "/ai_grading_keypoints.html");
		keyPointsContainer.setLabel("ai.grading.keypoints", null);
		keyPointsContainer.setRootForm(mainForm);
		formLayout.add(keyPointsContainer);
		keyPointsContainer.contextPut("rows", keyPointRows);
		addKeyPointButton = uifactory.addFormLink("ai.grading.keypoint.add",
				keyPointsContainer, Link.BUTTON);
		addKeyPointButton.setIconLeftCSS("o_icon o_icon-lg o_icon-fw o_icon_add");
		addKeyPointButton.setVisible(!readOnly);
		List<EssayItemDraft.KeyPoint> keyPoints = parseKeyPoints(
				grading == null ? null : grading.getKeyPointsJson());
		for (EssayItemDraft.KeyPoint kp : keyPoints) {
			addKeyPointRow(kp);
		}
		if (keyPointRows.isEmpty() && !readOnly) {
			// Always show at least one row so authors can fill it in.
			addKeyPointRow(null);
		}

		// Rubric criteria repeating rows
		rubricCriteriaContainer = FormLayoutContainer.createCustomFormLayout("ai.grading.rubric",
				getTranslator(), velocity_root + "/ai_grading_rubric.html");
		rubricCriteriaContainer.setLabel("ai.grading.rubric", null);
		rubricCriteriaContainer.setRootForm(mainForm);
		formLayout.add(rubricCriteriaContainer);
		rubricCriteriaContainer.contextPut("rows", rubricCriterionRows);
		addRubricCriterionButton = uifactory.addFormLink("ai.grading.rubric.add",
				rubricCriteriaContainer, Link.BUTTON);
		addRubricCriterionButton.setIconLeftCSS("o_icon o_icon-lg o_icon-fw o_icon_add");
		addRubricCriterionButton.setVisible(!readOnly);
		List<EssayItemDraft.RubricCriterion> rubricCriteria = parseRubricCriteria(
				grading == null ? null : grading.getRubricCriteriaJson());
		for (EssayItemDraft.RubricCriterion rc : rubricCriteria) {
			addRubricCriterionRow(rc);
		}
		if (rubricCriterionRows.isEmpty() && !readOnly) {
			addRubricCriterionRow(null);
		}

		// Common misconceptions repeating rows
		misconceptionsContainer = FormLayoutContainer.createCustomFormLayout("ai.grading.misconceptions",
				getTranslator(), velocity_root + "/ai_grading_misconceptions.html");
		misconceptionsContainer.setLabel("ai.grading.misconceptions", null);
		misconceptionsContainer.setRootForm(mainForm);
		formLayout.add(misconceptionsContainer);
		misconceptionsContainer.contextPut("rows", misconceptionRows);
		addMisconceptionButton = uifactory.addFormLink("ai.grading.misconception.add",
				misconceptionsContainer, Link.BUTTON);
		addMisconceptionButton.setIconLeftCSS("o_icon o_icon-lg o_icon-fw o_icon_add");
		addMisconceptionButton.setVisible(!readOnly);
		List<String> misconceptions = parseMisconceptions(
				grading == null ? null : grading.getCommonMisconceptionsJson());
		for (String text : misconceptions) {
			addMisconceptionRow(text);
		}

		// Submit
		FormLayoutContainer buttonsContainer = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsContainer.setRootForm(mainForm);
		buttonsContainer.setVisible(!readOnly);
		formLayout.add(buttonsContainer);
		uifactory.addFormSubmitButton("submit", buttonsContainer);
		testFeedbackButton = uifactory.addFormLink("test.feedback", "ai.grading.test.feedback",
				null, buttonsContainer, Link.BUTTON);
		testFeedbackButton.setGhost(true);
		testFeedbackButton.setIconLeftCSS("o_icon o_icon-lg o_icon-fw o_icon_ai");
		testFeedbackButton.setVisible(!readOnly);
	}

	private String currentItemIdentifier() {
		if (itemBuilder == null || itemBuilder.getAssessmentItem() == null
				|| itemBuilder.getAssessmentItem().getIdentifier() == null) {
			return null;
		}
		return itemBuilder.getAssessmentItem().getIdentifier();
	}

	private void addKeyPointRow(EssayItemDraft.KeyPoint kp) {
		String suffix = String.valueOf(++keyPointRowCounter);
		KeyPointRow row = new KeyPointRow();
		row.id = kp == null ? null : kp.id();
		row.text = uifactory.addTextAreaElement("ai.grading.keypoint.text." + suffix,
				null, -1, 3, 80, true, false,
				kp == null ? "" : value(kp.text()), keyPointsContainer);
		row.text.setEnabled(!readOnly);
		row.weight = uifactory.addTextElement("ai.grading.keypoint.weight." + suffix,
				null, 8, kp == null ? "" : Double.toString(kp.weight()), keyPointsContainer);
		row.weight.setEnabled(!readOnly);
		boolean requiredFlag = kp == null ? true : kp.isRequiredEffective();
		row.required = uifactory.addCheckboxesHorizontal("ai.grading.keypoint.required." + suffix,
				null, keyPointsContainer, new String[]{"on"},
				new String[]{translate("ai.grading.keypoint.required")});
		row.required.select("on", requiredFlag);
		row.required.setEnabled(!readOnly);
		row.removeButton = uifactory.addFormLink("ai.grading.keypoint.remove." + suffix,
				"ai.grading.keypoint.remove", "ai.grading.keypoint.remove",
				keyPointsContainer, Link.BUTTON);
		row.removeButton.setIconLeftCSS("o_icon o_icon-lg o_icon-fw o_icon_delete");
		row.removeButton.setUserObject(row);
		row.removeButton.setVisible(!readOnly);
		keyPointRows.add(row);
	}

	private void addRubricCriterionRow(EssayItemDraft.RubricCriterion rc) {
		String suffix = String.valueOf(++rubricCriterionRowCounter);
		RubricCriterionRow row = new RubricCriterionRow();
		row.id = rc == null ? null : rc.id();
		row.name = uifactory.addTextElement("ai.grading.rubric.name." + suffix,
				null, 128, rc == null ? "" : value(rc.name()), rubricCriteriaContainer);
		row.name.setEnabled(!readOnly);
		row.descriptor = uifactory.addTextAreaElement("ai.grading.rubric.descriptor." + suffix,
				null, -1, 2, 80, true, false,
				rc == null ? "" : value(rc.descriptor()), rubricCriteriaContainer);
		row.descriptor.setEnabled(!readOnly);
		row.weight = uifactory.addTextElement("ai.grading.rubric.weight." + suffix,
				null, 8, rc == null ? "" : Double.toString(rc.weight()), rubricCriteriaContainer);
		row.weight.setEnabled(!readOnly);
		SelectionValues scopeKeys = new SelectionValues();
		for (EssayItemDraft.RubricScope scope : EssayItemDraft.RubricScope.values()) {
			scopeKeys.add(SelectionValues.entry(scope.name(),
					translate("ai.grading.rubric.scope." + scope.name())));
		}
		row.scope = uifactory.addRadiosHorizontal("ai.grading.rubric.scope." + suffix,
				null, rubricCriteriaContainer, scopeKeys.keys(), scopeKeys.values());
		row.scope.setEnabled(!readOnly);
		EssayItemDraft.RubricScope currentScope = rc == null
				? EssayItemDraft.RubricScope.CONTENT : rc.scope();
		if (currentScope == null) {
			currentScope = EssayItemDraft.RubricScope.CONTENT;
		}
		row.scope.select(currentScope.name(), true);
		row.removeButton = uifactory.addFormLink("ai.grading.rubric.remove." + suffix,
				"ai.grading.rubric.remove", "ai.grading.rubric.remove",
				rubricCriteriaContainer, Link.BUTTON);
		row.removeButton.setIconLeftCSS("o_icon o_icon-lg o_icon-fw o_icon_delete");
		row.removeButton.setUserObject(row);
		row.removeButton.setVisible(!readOnly);
		rubricCriterionRows.add(row);
	}

	private void addMisconceptionRow(String text) {
		String suffix = String.valueOf(++misconceptionRowCounter);
		MisconceptionRow row = new MisconceptionRow();
		row.text = uifactory.addTextElement("ai.grading.misconception.text." + suffix,
				null, 1024, value(text), misconceptionsContainer);
		row.text.setEnabled(!readOnly);
		row.removeButton = uifactory.addFormLink("ai.grading.misconception.remove." + suffix,
				"ai.grading.misconception.remove", "ai.grading.misconception.remove",
				misconceptionsContainer, Link.BUTTON);
		row.removeButton.setIconLeftCSS("o_icon o_icon-lg o_icon-fw o_icon_delete");
		row.removeButton.setUserObject(row);
		row.removeButton.setVisible(!readOnly);
		misconceptionRows.add(row);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == addKeyPointButton) {
			addKeyPointRow(null);
			keyPointsContainer.setDirty(true);
		} else if (source == addRubricCriterionButton) {
			addRubricCriterionRow(null);
			rubricCriteriaContainer.setDirty(true);
		} else if (source == addMisconceptionButton) {
			addMisconceptionRow(null);
			misconceptionsContainer.setDirty(true);
		} else if (source == testFeedbackButton) {
			doOpenTestFeedback(ureq);
		} else if (source instanceof FormLink fl && fl.getUserObject() instanceof KeyPointRow row) {
			keyPointRows.remove(row);
			keyPointsContainer.setDirty(true);
		} else if (source instanceof FormLink fl && fl.getUserObject() instanceof RubricCriterionRow row) {
			rubricCriterionRows.remove(row);
			rubricCriteriaContainer.setDirty(true);
		} else if (source instanceof FormLink fl && fl.getUserObject() instanceof MisconceptionRow row) {
			misconceptionRows.remove(row);
			misconceptionsContainer.setDirty(true);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == cmc) {
			cleanupTestModal();
		} else if (source == testFeedbackCtrl) {
			// nothing — the inner controller renders results inside the modal.
		}
		super.event(ureq, source, event);
	}

	private void doOpenTestFeedback(UserRequest ureq) {
		cleanupTestModal();
		EssayAiGrading transientGrading = buildTransientGrading();
		String spiId = aiModule.getEssayGradingSpiId();
		String modelName = aiModule.getEssayGradingModel();
		testFeedbackCtrl = new AiEssayGradingTestController(ureq, getWindowControl(),
				spiId, modelName, aiEssayGradingService, transientGrading);
		listenTo(testFeedbackCtrl);
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				testFeedbackCtrl.getInitialComponent(), true,
				translate("ai.grading.test.feedback.title"));
		listenTo(cmc);
		cmc.activate();
	}

	private void cleanupTestModal() {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(testFeedbackCtrl);
		cmc = null;
		testFeedbackCtrl = null;
	}

	/**
	 * Build an in-memory {@link EssayAiGrading} from the current form values
	 * without persisting it. Mirrors the field-collection logic of {@link #formOK}
	 * but skips {@code essayAiGradingFileStore.save(...)} and the
	 * {@code AssessmentItemEvent} fire.
	 */
	private EssayAiGrading buildTransientGrading() {
		EssayAiGrading g = new EssayAiGrading();
		String itemIdentifier = currentItemIdentifier();
		g.setAssessmentItemIdentifier(itemIdentifier == null
				? "ai-test-" + System.currentTimeMillis() : itemIdentifier);
		g.setLearningObjective(trim(learningObjectiveEl.getValue()));
		g.setReferenceExcerpt(value(referenceExcerptEl.getValue()));
		g.setModelAnswer(value(modelAnswerEl.getValue()));
		g.setGradingHints(value(gradingHintsEl.getValue()));
		g.setBloomLevel(bloomLevelEl.isOneSelected() ? bloomLevelEl.getSelectedKey()
				: AiBloomLevel.UNDERSTAND.name());
		g.setLanguage(trim(languageTagEl.getValue()));
		g.setDifficulty(parseDifficultyKey(difficultyEl.getSelectedKey()));
		g.setKeyPointsJson(toJson(collectKeyPoints()));
		g.setRubricCriteriaJson(toJson(collectRubricCriteria()));
		g.setCommonMisconceptionsJson(toJson(collectMisconceptions()));
		g.setContentHash("editor-test");
		return g;
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		// Language tag
		languageTagEl.clearError();
		String tag = languageTagEl.getValue();
		if (!StringHelper.containsNonWhitespace(tag) || !isValidLanguageTag(tag.trim())) {
			languageTagEl.setErrorKey("ai.grading.error.language.tag");
			allOk = false;
		}

		// Bloom level
		bloomLevelEl.clearError();
		if (!bloomLevelEl.isOneSelected()) {
			bloomLevelEl.setErrorKey("form.legende.mandatory");
			allOk = false;
		}

		// Mandatory text fields
		allOk &= validateMandatoryText(referenceExcerptEl);
		allOk &= validateMandatoryText(modelAnswerEl);
		allOk &= validateMandatoryText(learningObjectiveEl);

		// Key-point validation
		allOk &= validateKeyPoints();
		allOk &= validateRubricCriteria();

		return allOk;
	}

	private boolean validateMandatoryText(FormItem element) {
		if (element instanceof TextElement te) {
			te.clearError();
			if (!StringHelper.containsNonWhitespace(te.getValue())) {
				te.setErrorKey("form.legende.mandatory");
				return false;
			}
			return true;
		}
		if (element instanceof TextAreaElement ta) {
			ta.clearError();
			if (!StringHelper.containsNonWhitespace(ta.getValue())) {
				ta.setErrorKey("form.legende.mandatory");
				return false;
			}
		}
		return true;
	}

	private boolean validateKeyPoints() {
		boolean allOk = true;
		double sum = 0.0;
		boolean anyValid = false;
		for (KeyPointRow row : keyPointRows) {
			row.text.clearError();
			row.weight.clearError();
			boolean hasText = StringHelper.containsNonWhitespace(row.text.getValue());
			Double weight = parseDouble(row.weight.getValue());
			if (hasText) {
				anyValid = true;
				if (weight == null || weight < 0.0 || weight > 1.0) {
					row.weight.setErrorKey("ai.grading.error.weight.range");
					allOk = false;
				} else {
					sum += weight;
				}
			} else if (weight != null) {
				// User entered weight but no text — flag
				row.text.setErrorKey("form.legende.mandatory");
				allOk = false;
			}
		}
		if (!anyValid) {
			// At least one key point is required
			if (!keyPointRows.isEmpty()) {
				keyPointRows.get(0).text.setErrorKey("form.legende.mandatory");
			}
			allOk = false;
		} else if (Math.abs(sum - 1.0) > WEIGHT_SUM_TOLERANCE) {
			if (!keyPointRows.isEmpty()) {
				keyPointRows.get(0).weight.setErrorKey("ai.grading.error.weight.sum");
			}
			allOk = false;
		}
		return allOk;
	}

	private boolean validateRubricCriteria() {
		boolean allOk = true;
		double sum = 0.0;
		boolean anyValid = false;
		for (RubricCriterionRow row : rubricCriterionRows) {
			row.name.clearError();
			row.weight.clearError();
			boolean hasName = StringHelper.containsNonWhitespace(row.name.getValue());
			Double weight = parseDouble(row.weight.getValue());
			if (hasName) {
				anyValid = true;
				if (weight == null || weight < 0.0 || weight > 1.0) {
					row.weight.setErrorKey("ai.grading.error.weight.range");
					allOk = false;
				} else {
					sum += weight;
				}
			} else if (weight != null) {
				row.name.setErrorKey("form.legende.mandatory");
				allOk = false;
			}
		}
		if (!anyValid) {
			if (!rubricCriterionRows.isEmpty()) {
				rubricCriterionRows.get(0).name.setErrorKey("form.legende.mandatory");
			}
			allOk = false;
		} else if (Math.abs(sum - 1.0) > WEIGHT_SUM_TOLERANCE) {
			if (!rubricCriterionRows.isEmpty()) {
				rubricCriterionRows.get(0).weight.setErrorKey("ai.grading.error.weight.sum");
			}
			allOk = false;
		}
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if (readOnly) return;
		String itemIdentifier = currentItemIdentifier();
		if (itemIdentifier == null || questionDir == null) {
			log.warn("Cannot persist EssayAiGrading: itemIdentifier={} questionDir={}",
					itemIdentifier, questionDir);
			return;
		}

		if (grading == null) {
			grading = new EssayAiGrading();
		}
		grading.setAssessmentItemIdentifier(itemIdentifier);

		grading.setLearningObjective(trim(learningObjectiveEl.getValue()));
		grading.setReferenceExcerpt(value(referenceExcerptEl.getValue()));
		grading.setModelAnswer(value(modelAnswerEl.getValue()));
		grading.setGradingHints(value(gradingHintsEl.getValue()));
		grading.setBloomLevel(bloomLevelEl.getSelectedKey());
		grading.setLanguage(trim(languageTagEl.getValue()));
		grading.setDifficulty(parseDifficultyKey(difficultyEl.getSelectedKey()));
		grading.setKeyPointsJson(toJson(collectKeyPoints()));
		grading.setRubricCriteriaJson(toJson(collectRubricCriteria()));
		grading.setCommonMisconceptionsJson(toJson(collectMisconceptions()));

		// Recompute integrity hash on every save.
		grading.setContentHash(EssayFormativeFeedbackService.computeContentHash(grading));

		// Persist as ai-grading.json next to the QTI item XML. The export
		// hook re-injects the <ooExt:aiGrading/> marker with the current
		// hash on QTI export.
		essayAiGradingFileStore.save(questionDir, grading);

		fireEvent(ureq, new AssessmentItemEvent(AssessmentItemEvent.ASSESSMENT_ITEM_CHANGED,
				itemBuilder.getAssessmentItem(), QTI21QuestionType.essay));
	}

	private List<EssayItemDraft.KeyPoint> collectKeyPoints() {
		List<EssayItemDraft.KeyPoint> result = new ArrayList<>();
		int idx = 1;
		for (KeyPointRow row : keyPointRows) {
			String text = row.text.getValue();
			if (!StringHelper.containsNonWhitespace(text)) continue;
			Double weight = parseDouble(row.weight.getValue());
			boolean required = row.required.isAtLeastSelected(1);
			String id = StringHelper.containsNonWhitespace(row.id) ? row.id : "kp" + idx;
			result.add(new EssayItemDraft.KeyPoint(id, text.trim(),
					weight == null ? 0.0 : weight, required));
			idx++;
		}
		return result;
	}

	private List<EssayItemDraft.RubricCriterion> collectRubricCriteria() {
		List<EssayItemDraft.RubricCriterion> result = new ArrayList<>();
		int idx = 1;
		for (RubricCriterionRow row : rubricCriterionRows) {
			String name = row.name.getValue();
			if (!StringHelper.containsNonWhitespace(name)) continue;
			Double weight = parseDouble(row.weight.getValue());
			EssayItemDraft.RubricScope scope = row.scope.isOneSelected()
					? EssayItemDraft.RubricScope.valueOf(row.scope.getSelectedKey())
					: EssayItemDraft.RubricScope.CONTENT;
			String id = StringHelper.containsNonWhitespace(row.id) ? row.id : "c" + idx;
			result.add(new EssayItemDraft.RubricCriterion(id, name.trim(),
					trim(row.descriptor.getValue()),
					weight == null ? 0.0 : weight, scope));
			idx++;
		}
		return result;
	}

	private List<String> collectMisconceptions() {
		List<String> result = new ArrayList<>();
		for (MisconceptionRow row : misconceptionRows) {
			String text = row.text.getValue();
			if (StringHelper.containsNonWhitespace(text)) {
				result.add(text.trim());
			}
		}
		return result;
	}

	private static List<EssayItemDraft.KeyPoint> parseKeyPoints(String json) {
		if (!StringHelper.containsNonWhitespace(json)) {
			return Collections.emptyList();
		}
		try {
			return MAPPER.readValue(json, new TypeReference<List<EssayItemDraft.KeyPoint>>() {});
		} catch (IOException e) {
			log.warn("Failed to parse key points JSON: {}", e.getMessage());
			return Collections.emptyList();
		}
	}

	private static List<EssayItemDraft.RubricCriterion> parseRubricCriteria(String json) {
		if (!StringHelper.containsNonWhitespace(json)) {
			return Collections.emptyList();
		}
		try {
			return MAPPER.readValue(json, new TypeReference<List<EssayItemDraft.RubricCriterion>>() {});
		} catch (IOException e) {
			log.warn("Failed to parse rubric criteria JSON: {}", e.getMessage());
			return Collections.emptyList();
		}
	}

	private static List<String> parseMisconceptions(String json) {
		if (!StringHelper.containsNonWhitespace(json)) {
			return Collections.emptyList();
		}
		try {
			return MAPPER.readValue(json, new TypeReference<List<String>>() {});
		} catch (IOException e) {
			log.warn("Failed to parse misconceptions JSON: {}", e.getMessage());
			return Collections.emptyList();
		}
	}

	private static String toJson(Object o) {
		try {
			return MAPPER.writeValueAsString(o);
		} catch (IOException e) {
			log.warn("Failed to serialize JSON: {}", e.getMessage());
			return "[]";
		}
	}

	private static String value(String s) {
		return s == null ? "" : s;
	}

	private static String trim(String s) {
		return s == null ? null : s.trim();
	}

	private static Integer parseInt(String s) {
		if (!StringHelper.containsNonWhitespace(s)) return null;
		try {
			return Integer.parseInt(s.trim());
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private static Integer parseDifficultyKey(String key) {
		if (key == null || DIFFICULTY_UNKNOWN_KEY.equals(key)) {
			return null;
		}
		return parseInt(key);
	}

	private static Double parseDouble(String s) {
		if (!StringHelper.containsNonWhitespace(s)) return null;
		try {
			return Double.parseDouble(s.trim().replace(',', '.'));
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/**
	 * BCP-47 validity check: parses the tag with {@link Locale#forLanguageTag(String)}
	 * and verifies the result has a non-empty primary language. Empty/invalid
	 * tags resolve to {@code Locale.ROOT} ("") and are rejected.
	 */
	static boolean isValidLanguageTag(String tag) {
		if (!StringHelper.containsNonWhitespace(tag)) {
			return false;
		}
		Locale parsed = Locale.forLanguageTag(tag.trim());
		return StringHelper.containsNonWhitespace(parsed.getLanguage());
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		// not used — no cancel button.
	}

	public static final class KeyPointRow {
		String id;
		TextAreaElement text;
		TextElement weight;
		org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement required;
		FormLink removeButton;

		public TextAreaElement getText() { return text; }
		public TextElement getWeight() { return weight; }
		public org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement getRequired() { return required; }
		public FormLink getRemoveButton() { return removeButton; }
	}

	public static final class RubricCriterionRow {
		String id;
		TextElement name;
		TextAreaElement descriptor;
		TextElement weight;
		SingleSelection scope;
		FormLink removeButton;

		public TextElement getName() { return name; }
		public TextAreaElement getDescriptor() { return descriptor; }
		public TextElement getWeight() { return weight; }
		public SingleSelection getScope() { return scope; }
		public FormLink getRemoveButton() { return removeButton; }
	}

	public static final class MisconceptionRow {
		TextElement text;
		FormLink removeButton;

		public TextElement getText() { return text; }
		public FormLink getRemoveButton() { return removeButton; }
	}
}
