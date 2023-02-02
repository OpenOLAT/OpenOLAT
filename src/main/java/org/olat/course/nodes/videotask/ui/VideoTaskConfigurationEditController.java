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
package org.olat.course.nodes.videotask.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.StringHelper;
import org.olat.course.editor.NodeEditController;
import org.olat.course.nodes.CourseNode;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.video.VideoAssessmentService;
import org.olat.modules.video.VideoManager;
import org.olat.modules.video.VideoSegmentCategory;
import org.olat.modules.video.VideoSegments;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 17 janv. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class VideoTaskConfigurationEditController extends FormBasicController {

	private static final String ATTEMPTS_ENABLED = "true";
	private static final String ATTEMPTS_DISABLED = "false";
	
	private TextElement attemptsEl;
	private SingleSelection modeEl;
	private SingleSelection enableAttemptsEl;
	private MultipleSelectionElement videoElements;
	private MultipleSelectionElement categoriesEl;
	private SingleSelection sortCategoriesEl;
	private SingleSelection attemptsPerSegmentEl;
	
	private String currentMode;
	private final String subIdent;
	private final RepositoryEntry entry;
	private final VideoSegments segments;
	private final ModuleConfiguration config;

	private CloseableModalController cmc;
	private ConfirmChangeModeController confirmChangeModeCtrl;

	@Autowired
	private VideoManager videoManager;
	@Autowired
	private VideoAssessmentService videoAssessmentService;
	
	public VideoTaskConfigurationEditController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry videoEntry, RepositoryEntry entry, CourseNode courseNode) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		this.entry = entry;
		subIdent = courseNode.getIdent();
		config = courseNode.getModuleConfiguration();
		currentMode = config.getStringValue(VideoTaskEditController.CONFIG_KEY_MODE);
		segments = videoManager.loadSegments(videoEntry.getOlatResource());
		
		initForm(ureq);
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer featuresCont = uifactory.addDefaultFormLayout("features", null, formLayout);
		initFeatures(featuresCont);
		
		FormLayoutContainer modesCont = uifactory.addDefaultFormLayout("modes", null, formLayout);
		modesCont.setFormTitle(translate("video.modes"));
		initModes(modesCont);
		
		FormLayoutContainer optionsCont = uifactory.addDefaultFormLayout("options", null, formLayout);
		optionsCont.setFormTitle(translate("video.options"));
		initOptions(optionsCont);
	}
	
	private void initFeatures(FormItemContainer formLayout) {
		SelectionValues elementsValues = new SelectionValues();
		elementsValues.add(SelectionValues.entry(VideoTaskEditController.CONFIG_KEY_SEGMENTS, translate("video.config.elements.segments")));
		elementsValues.add(SelectionValues.entry(VideoTaskEditController.CONFIG_KEY_ANNOTATIONS, translate("video.config.elements.annotations")));
		elementsValues.add(SelectionValues.entry(VideoTaskEditController.CONFIG_KEY_QUESTIONS, translate("video.config.elements.questions")));
		videoElements = uifactory.addCheckboxesVertical("videoElements", "video.config.elements", formLayout,
				elementsValues.keys(), elementsValues.values(), 1);
		videoElements.select(VideoTaskEditController.CONFIG_KEY_ANNOTATIONS, config.getBooleanSafe(VideoTaskEditController.CONFIG_KEY_ANNOTATIONS, true));
		videoElements.select(VideoTaskEditController.CONFIG_KEY_QUESTIONS, config.getBooleanSafe(VideoTaskEditController.CONFIG_KEY_QUESTIONS, true));
		videoElements.select(VideoTaskEditController.CONFIG_KEY_SEGMENTS, true);
		videoElements.setEnabled(VideoTaskEditController.CONFIG_KEY_SEGMENTS, false);

		SelectionValues categoriesValues = getCategoriesSelectionValues();
		categoriesEl = uifactory.addCheckboxesVertical("video.config.categories", "video.config.categories", formLayout,
				categoriesValues.keys(), categoriesValues.values(), 1);
		List<String> categories = config.getList(VideoTaskEditController.CONFIG_KEY_CATEGORIES, String.class);
		if(categories != null) {
			for(String category:categories) {
				if(categoriesValues.containsKey(category)) {
					categoriesEl.select(category, true);
				}
			}
		}
		categoriesEl.setVisible(!categoriesValues.isEmpty());
		
		SelectionValues sortValues = new SelectionValues();
		sortValues.add(SelectionValues.entry(VideoTaskEditController.CONFIG_KEY_SORT_CATEGORIES_PRESET, translate("video.config.categories.sort.preset")));
		sortValues.add(SelectionValues.entry(VideoTaskEditController.CONFIG_KEY_SORT_CATEGORIES_ALPHABETICAL, translate("video.config.categories.sort.alphabetical")));
		sortCategoriesEl = uifactory.addRadiosVertical("video.config.categories.sort", "video.config.categories.sort", formLayout,
				sortValues.keys(), sortValues.values());
		sortCategoriesEl.addActionListener(FormEvent.ONCHANGE);
		String sort = config.getStringValue(VideoTaskEditController.CONFIG_KEY_SORT_CATEGORIES, VideoTaskEditController.CONFIG_KEY_SORT_CATEGORIES_PRESET);
		if(sortValues.containsKey(sort)) {
			sortCategoriesEl.select(sort, true);
		} else {
			sortCategoriesEl.select(VideoTaskEditController.CONFIG_KEY_SORT_CATEGORIES_PRESET, true);
		}
		sortCategoriesEl.setVisible(categoriesValues.size() > 1);
	}
	
	private void initModes(FormItemContainer formLayout) {
		SelectionValues modeValues = new SelectionValues();
		
		modeValues.add(SelectionValues.entry(VideoTaskEditController.CONFIG_KEY_MODE_PRACTICE_ASSIGN_TERMS,
				translate("mode.practice.assign.terms"), translate("mode.practice.assign.terms.desc"), null, null, true));
		modeValues.add(SelectionValues.entry(VideoTaskEditController.CONFIG_KEY_MODE_PRACTICE_IDENTIFY_SITUATIONS,
				translate("mode.practice.identify.situations"), translate("mode.practice.identify.situations.desc"), null, null, true));
		modeValues.add(SelectionValues.entry(VideoTaskEditController.CONFIG_KEY_MODE_TEST_IDENTIFY_SITUATIONS,
				translate("mode.test.identify.situations"), translate("mode.test.identify.situations.desc"), null, null, true));
		
		modeEl = uifactory.addCardSingleSelectHorizontal("modes", "video.modes", formLayout, modeValues);
		modeEl.addActionListener(FormEvent.ONCHANGE);
		String val = config.getStringValue(VideoTaskEditController.CONFIG_KEY_MODE, VideoTaskEditController.CONFIG_KEY_MODE_DEFAULT);
		if(modeValues.containsKey(val)) {
			modeEl.select(val, true);
		}
	}
	
	private void initOptions(FormItemContainer formLayout) {
		int attempts = config.getIntegerSafe(VideoTaskEditController.CONFIG_KEY_ATTEMPTS, 0);
		
		SelectionValues enableValues = new SelectionValues();
		enableValues.add(SelectionValues.entry(ATTEMPTS_ENABLED, translate("yes")));
		enableValues.add(SelectionValues.entry(ATTEMPTS_DISABLED, translate("no")));
		enableAttemptsEl = uifactory.addRadiosHorizontal("attempts.enable", "attempts.enable", formLayout,
				enableValues.keys(), enableValues.values());
		enableAttemptsEl.select(attempts > 0 ? ATTEMPTS_ENABLED : ATTEMPTS_DISABLED, true);
		enableAttemptsEl.addActionListener(FormEvent.ONCHANGE);
		
		attemptsEl = uifactory.addTextElement("num.of.attempts", 4, Integer.toString(attempts), formLayout);

		int attemptsPerSegmentInt = config.getIntegerSafe(VideoTaskEditController.CONFIG_KEY_ATTEMPTS_PER_SEGMENT,
				VideoTaskEditController.CONFIG_KEY_ATTEMPTS_PER_SEGMENT_DEFAULT);
		String attemptsPerSegment = Integer.toString(attemptsPerSegmentInt);
		
		SelectionValues attemptsSegmentsValues = new SelectionValues();
		for(int i=1; i<=5; i++) {
			String numOf = Integer.toString(i);
			attemptsSegmentsValues.add(SelectionValues.entry(numOf, numOf));
		}
		if(!attemptsSegmentsValues.containsKey(attemptsPerSegment)) {
			attemptsSegmentsValues.add(SelectionValues.entry(attemptsPerSegment, attemptsPerSegment));
		}
		attemptsPerSegmentEl = uifactory.addDropdownSingleselect("num.of.attempts.per.segment", formLayout,
				attemptsSegmentsValues.keys(), attemptsSegmentsValues.values());
		attemptsPerSegmentEl.setHelpText(translate("num.of.attempts.per.segment.info"));
		if(attemptsSegmentsValues.containsKey(attemptsPerSegment)) {
			attemptsPerSegmentEl.select(attemptsPerSegment, true);
		}
	
		FormLayoutContainer buttonsCont = uifactory.addButtonsFormLayout("buttons", null, formLayout);
		uifactory.addFormSubmitButton("save", buttonsCont);
	}
	
	private SelectionValues getCategoriesSelectionValues() {
		SelectionValues categoriesValues = new SelectionValues();
		if(segments != null && segments.getCategories() != null) {
			List<VideoSegmentCategory> allCategories = segments.getCategories();
			if(sortCategoriesEl == null || !sortCategoriesEl.isOneSelected()) {
				VideoTaskHelper.sortCategories(allCategories, config, getLocale());
			} else {
				VideoTaskHelper.sortCategories(allCategories, sortCategoriesEl.getSelectedKey(), getLocale());
			}
			for(VideoSegmentCategory category:allCategories) {
				categoriesValues.add(SelectionValues.entry(category.getId(), category.getLabelAndTitle()));
			}
		}
		return categoriesValues;
	}
	
	private void updateUI() {
		boolean attemptsEnabled = enableAttemptsEl.isOneSelected() && ATTEMPTS_ENABLED.equals(enableAttemptsEl.getSelectedKey());
		attemptsEl.setVisible(attemptsEnabled);
		
		String mode = modeEl.getSelectedKey();
		if(VideoTaskEditController.CONFIG_KEY_MODE_TEST_IDENTIFY_SITUATIONS.equals(mode)) {
			videoElements.select(VideoTaskEditController.CONFIG_KEY_QUESTIONS, false);
			videoElements.setEnabled(VideoTaskEditController.CONFIG_KEY_QUESTIONS, false);
		} else {
			videoElements.setEnabled(VideoTaskEditController.CONFIG_KEY_QUESTIONS, true);
		}
		
		if(VideoTaskEditController.CONFIG_KEY_MODE_TEST_IDENTIFY_SITUATIONS.equals(mode)
				|| VideoTaskEditController.CONFIG_KEY_MODE_PRACTICE_IDENTIFY_SITUATIONS.equals(mode)) {
			attemptsPerSegmentEl.setEnabled(true);
		} else {
			attemptsPerSegmentEl.select("3", true);
			attemptsPerSegmentEl.setEnabled(false);
		}
		
		attemptsPerSegmentEl.setVisible(VideoTaskEditController.CONFIG_KEY_MODE_PRACTICE_ASSIGN_TERMS.equals(mode));
	}
	
	private void updateCategoriesUI() {
		SelectionValues selectionValues = getCategoriesSelectionValues();
		categoriesEl.setKeysAndValues(selectionValues.keys(), selectionValues.values());
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(confirmChangeModeCtrl == source) {
			if(event == Event.DONE_EVENT) {
				updateConfig(ureq, true);
			} else {
				markDirty();
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(confirmChangeModeCtrl);
		removeAsListenerAndDispose(cmc);
		confirmChangeModeCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(enableAttemptsEl == source || modeEl == source) {
			updateUI();
		} else if(sortCategoriesEl == source) {
			updateCategoriesUI();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		modeEl.clearError();
		enableAttemptsEl.clearError();
		if(!modeEl.isOneSelected()) {
			modeEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		if(modeEl.isOneSelected() && ATTEMPTS_ENABLED.equals(enableAttemptsEl.getSelectedKey())) {
			allOk &= validateInt(attemptsEl);
		}

		categoriesEl.clearError();
		if(categoriesEl.isVisible() && categoriesEl.getSelectedKeys().isEmpty()) {
			categoriesEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		return allOk;
	}
	
	private boolean validateInt(TextElement el) {
		boolean allOk = true;
		
		if(el.isVisible()) {
			String value = el.getValue();
			if(StringHelper.containsNonWhitespace(value)) {
				try {
					if(Integer.parseInt(value) <= 0) {
						allOk &= false;
						el.setErrorKey("form.error.positive.integer");
					}
				} catch(Exception e) {
					allOk &= false;
					el.setErrorKey("form.error.nointeger");
				}
			} else {
				el.setErrorKey("form.legende.mandatory");
				allOk &= false;
			}
		}

		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String mode = modeEl.getSelectedKey();
		if(currentMode == null || currentMode.equals(mode) ||
			((currentMode.equals(VideoTaskEditController.CONFIG_KEY_MODE_PRACTICE_ASSIGN_TERMS) || currentMode.equals(VideoTaskEditController.CONFIG_KEY_MODE_PRACTICE_IDENTIFY_SITUATIONS))
				&& (mode.equals(VideoTaskEditController.CONFIG_KEY_MODE_PRACTICE_ASSIGN_TERMS) ||  mode.equals(VideoTaskEditController.CONFIG_KEY_MODE_PRACTICE_IDENTIFY_SITUATIONS)))
			|| videoAssessmentService.countTaskSessions(entry, subIdent) == 0l) {
			updateConfig(ureq, false);
		} else {
			doConfirmChangeMde(ureq);
		}
	}
	
	private void doConfirmChangeMde(UserRequest ureq) {
		long sessions = videoAssessmentService.countTaskSessions(entry, subIdent);
		confirmChangeModeCtrl = new ConfirmChangeModeController(ureq, getWindowControl(), entry, subIdent, currentMode, sessions);
		listenTo(confirmChangeModeCtrl);
		
		String title = translate("change.mode.title");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), confirmChangeModeCtrl.getInitialComponent(), title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void updateConfig(UserRequest ureq, boolean triggersEvent) {
		currentMode = modeEl.getSelectedKey();
		config.setStringValue(VideoTaskEditController.CONFIG_KEY_MODE, currentMode);
		
		if(enableAttemptsEl.isOneSelected() && ATTEMPTS_ENABLED.equals(enableAttemptsEl.getSelectedKey())) {
			int attempts = Integer.parseInt(attemptsEl.getValue());
			config.setIntValue(VideoTaskEditController.CONFIG_KEY_ATTEMPTS, attempts);
		} else {
			config.remove(VideoTaskEditController.CONFIG_KEY_ATTEMPTS);
		}
		
		if(attemptsPerSegmentEl.isVisible() && attemptsPerSegmentEl.isOneSelected()
				&& VideoTaskEditController.CONFIG_KEY_MODE_PRACTICE_ASSIGN_TERMS.equals(currentMode)) {
			int attemptsPerSegment = Integer.parseInt(attemptsPerSegmentEl.getSelectedKey());
			config.setIntValue(VideoTaskEditController.CONFIG_KEY_ATTEMPTS_PER_SEGMENT, attemptsPerSegment);
		} else {
			config.remove(VideoTaskEditController.CONFIG_KEY_ATTEMPTS_PER_SEGMENT);
		}
		
		List<String> categories = new ArrayList<>(categoriesEl.getSelectedKeys());
		config.setList(VideoTaskEditController.CONFIG_KEY_CATEGORIES, categories);
		
		String sort = sortCategoriesEl.getSelectedKey();
		config.setStringValue(VideoTaskEditController.CONFIG_KEY_SORT_CATEGORIES, sort);

		fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
		if(triggersEvent) {
			fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_REFRESH_EVENT);
		}
	}

}
