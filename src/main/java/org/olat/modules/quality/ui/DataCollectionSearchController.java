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
package org.olat.modules.quality.ui;

import static org.olat.core.gui.components.util.SelectionValues.entry;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.ExtendedFlexiTableSearchController;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.modules.quality.QualityDataCollectionStatus;
import org.olat.modules.quality.QualityDataCollectionTopicType;
import org.olat.modules.quality.QualityDataCollectionViewSearchParams;
import org.olat.modules.quality.QualityService;
import org.olat.modules.quality.generator.QualityGenerator;
import org.olat.modules.quality.generator.QualityGeneratorRef;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.model.RepositoryEntryRefImpl;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 28 Aug 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class DataCollectionSearchController extends FormBasicController implements ExtendedFlexiTableSearchController {

	private MultipleSelectionElement fromEntriesEl;
	private TextElement titleEl;
	private MultipleSelectionElement topicTypeEl;
	private TextElement topicEl;
	private TextElement keyEl;
	private MultipleSelectionElement generatorsEl;
	private DateChooser startAfterEl;
	private DateChooser startBeforeEl;
	private DateChooser deadlineAfterEl;
	private DateChooser deadlineBeforeEl;
	private MultipleSelectionElement statusEl;
	private FormLink searchButton;
	
	private final QualityDataCollectionViewSearchParams defaultSearchParams;
	private boolean enabled = true;
	
	@Autowired
	private QualityService qualityService;

	public DataCollectionSearchController(UserRequest ureq, WindowControl wControl, Form mainForm,
			QualityDataCollectionViewSearchParams defaultSearchParams) {
		super(ureq, wControl, LAYOUT_CUSTOM, "data_collection_search", mainForm);
		this.defaultSearchParams = defaultSearchParams;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer leftContainer = FormLayoutContainer.createDefaultFormLayout("left_1", getTranslator());
		leftContainer.setRootForm(mainForm);
		formLayout.add(leftContainer);

		// LEFT part of form
		List<RepositoryEntry> formEntries = qualityService.getFormEntries(defaultSearchParams);
		SelectionValues formEntriesKV = new SelectionValues();
		formEntries.forEach(formEntry -> formEntriesKV.add(entry(formEntry.getKey().toString(), formEntry.getDisplayname())));
		formEntriesKV.sort(SelectionValues.VALUE_ASC);
		fromEntriesEl = uifactory.addCheckboxesDropdown("data.collection.form", "data.collection.form",
				leftContainer, formEntriesKV.keys(), formEntriesKV.values());
		fromEntriesEl.setNonSelectedText(translate("search.show.all"));
		
		titleEl = uifactory.addTextElement("data.collection.title", "data.collection.title", 255, "", leftContainer);
		titleEl.setFocus(true);
		
		SelectionValues topicTypeKV = new SelectionValues();
		Arrays.stream(QualityDataCollectionTopicType.values())
				.forEach(type -> topicTypeKV.add(entry(type.name(), translate(type.getI18nKey()))));
		topicTypeEl = uifactory.addCheckboxesDropdown("data.collection.topic.type", "data.collection.topic.type",
				leftContainer, topicTypeKV.keys(), topicTypeKV.values());
		topicTypeEl.setNonSelectedText(translate("search.show.all"));
		
		topicEl = uifactory.addTextElement("data.collection.topic", "data.collection.topic", 255, "", leftContainer);
		
		keyEl = uifactory.addTextElement("data.collection.id", "data.collection.id", 64, "", leftContainer);
		
		List<QualityGenerator> generators = qualityService.getGenerators(defaultSearchParams);
		if (!generators.isEmpty()) {
			SelectionValues generatorsKV = new SelectionValues();
			generators.forEach(generator -> generatorsKV.add(entry(generator.getKey().toString(), generator.getTitle())));
			generatorsKV.sort(SelectionValues.VALUE_ASC);
			generatorsEl = uifactory.addCheckboxesDropdown("data.collection.generator.title",
					"data.collection.generator.title", leftContainer, generatorsKV.keys(), generatorsKV.values());
		}
		
		// RIGHT part of form
		FormLayoutContainer rightContainer = FormLayoutContainer.createDefaultFormLayout("right_1", getTranslator());
		rightContainer.setRootForm(mainForm);
		formLayout.add(rightContainer);
		
		startAfterEl = uifactory.addDateChooser("data.collection.start.after", null, rightContainer);
		startAfterEl.setDateChooserTimeEnabled(true);

		startBeforeEl = uifactory.addDateChooser("data.collection.start.before", null, rightContainer);
		startBeforeEl.setDateChooserTimeEnabled(true);
		
		deadlineAfterEl = uifactory.addDateChooser("data.collection.deadline.after", null, rightContainer);
		deadlineAfterEl.setDateChooserTimeEnabled(true);

		deadlineBeforeEl = uifactory.addDateChooser("data.collection.deadline.before", null, rightContainer);
		deadlineBeforeEl.setDateChooserTimeEnabled(true);
		
		SelectionValues statusKV = new SelectionValues();
		statusKV.add(entry(QualityDataCollectionStatus.PREPARATION.name(), translate("data.collection.status.preparation")));
		statusKV.add(entry(QualityDataCollectionStatus.READY.name(), translate("data.collection.status.ready")));
		statusKV.add(entry(QualityDataCollectionStatus.RUNNING.name(), translate("data.collection.status.running")));
		statusKV.add(entry(QualityDataCollectionStatus.FINISHED.name(), translate("data.collection.status.finished")));
		statusEl = uifactory.addCheckboxesHorizontal("data.collection.status", rightContainer, statusKV.keys(), statusKV.values());


		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("button_layout", getTranslator());
		formLayout.add(buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
		searchButton = uifactory.addFormLink("search", buttonLayout, Link.BUTTON);
		searchButton.setCustomEnabledLinkCSS("btn btn-primary");
	}
	
	@Override
	public void setEnabled(boolean enable) {
		this.enabled = enable;
	}

	@Override
	public List<String> getConditionalQueries() {
		return Collections.emptyList();
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(enabled && source == searchButton) {
			fireSearchEvent(ureq);
		}
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		if(!enabled) return true;
		
		boolean allOk = super.validateFormLogic(ureq);
		
		allOk &= QualityUIFactory.validateLong(keyEl, 0, Long.MAX_VALUE);
		
		return allOk;
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if (enabled) {
			fireSearchEvent(ureq);
		}
	}
	
	private void fireSearchEvent(UserRequest ureq) {
		SearchEvent se = new SearchEvent();
		
		if (fromEntriesEl.isAtLeastSelected(1)) {
			List<RepositoryEntryRefImpl> formEntryRefs = fromEntriesEl.getSelectedKeys().stream()
					.map(Long::valueOf)
					.map(RepositoryEntryRefImpl::new)
					.collect(Collectors.toList());
			se.setFormEntryRefs(formEntryRefs);
		}
		
		String title = titleEl.getValue();
		if (StringHelper.containsNonWhitespace(title)) {
			se.setTitle(title);
		}
		
		if (topicTypeEl.isAtLeastSelected(1)) {
			List<QualityDataCollectionTopicType> topicTypes = topicTypeEl.getSelectedKeys().stream()
					.map(QualityDataCollectionTopicType::valueOf)
					.collect(Collectors.toList());
			se.setTopicTypes(topicTypes);
		}
		
		String topic = topicEl.getValue();
		if (StringHelper.containsNonWhitespace(topic)) {
			se.setTopic(topic);
		}
		
		if (generatorsEl != null && generatorsEl.isAtLeastSelected(1)) {
			List<? extends QualityGeneratorRef> generatorRefs = generatorsEl.getSelectedKeys().stream()
					.map(QualityGeneratorRef::of)
					.collect(Collectors.toList());
			se.setGeneratorRefs(generatorRefs);
		}
		
		if (statusEl.isAtLeastSelected(1)) {
			List<QualityDataCollectionStatus> status = statusEl.getSelectedKeys().stream()
					.map(QualityDataCollectionStatus::valueOf)
					.collect(Collectors.toList());
			se.setStatus(status);
		}
		
		String keyValue = keyEl.getValue();
		if (StringHelper.containsNonWhitespace(keyValue)) {
			try {
				Long key = Long.valueOf(keyValue);
				se.setDataCollectionRef(() -> key);
			} catch (NumberFormatException e) {
				// Sometime the form validation is skipped?!
				se.setDataCollectionRef(() -> -1l);
			}
		}
		
		se.setStartAfter(startAfterEl.getDate());
		se.setStartBefore(startBeforeEl.getDate());
		se.setDeadlineAfter(deadlineAfterEl.getDate());
		se.setDeadlineBefore(deadlineBeforeEl.getDate());
		
		fireEvent(ureq, se);
	}

	@Override
	protected void doDispose() {
		//
	}

}
