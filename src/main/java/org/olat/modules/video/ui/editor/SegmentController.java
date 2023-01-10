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
package org.olat.modules.video.ui.editor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.FormCancel;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.DateUtils;
import org.olat.modules.video.VideoManager;
import org.olat.modules.video.VideoSegment;
import org.olat.modules.video.VideoSegmentCategory;
import org.olat.modules.video.VideoSegments;
import org.olat.modules.video.model.VideoSegmentCategoryImpl;
import org.olat.modules.video.model.VideoSegmentImpl;
import org.olat.modules.video.ui.component.SelectTimeCommand;
import org.olat.repository.RepositoryEntry;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2022-12-19<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class SegmentController extends FormBasicController {
	public static final Event RELOAD_SEGMENTS_EVENT = new Event("video.edit.reload.segments");

	@Autowired
	private VideoManager videoManager;

	private VideoSegments videoSegments;
	private String videoSegmentId;

	private FormLink previousSegmentButton;
	private SelectionValues segmentsKV;
	private SingleSelection segmentsDropdown;
	private FormLink nextSegmentButton;
	private FormLink addSegmentButton;
	private TextElement startEl;
	private TextElement endEl;
	private TextElement durationEl;
	private SingleSelection categoryDropdown;
	private FormLink categoryButton;
	private SelectionValues categoriesKV;
	private FormLink editCategoriesButton;
	private CloseableModalController cmc;
	private EditCategoriesController editCategoriesController;

	private FormSubmit saveButton;
	private FormCancel cancelButton;

	private final RepositoryEntry repositoryEntry;
	private final SimpleDateFormat timeFormat;
	private final String videoElementId;
	private String currentTimeCode;

	public SegmentController(UserRequest ureq, WindowControl wControl, RepositoryEntry repositoryEntry,
							 String videoElementId) {
		super(ureq, wControl, "video_segments");
		this.repositoryEntry = repositoryEntry;
		this.videoElementId = videoElementId;
		timeFormat = new SimpleDateFormat("HH:mm:ss");
		timeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

		segmentsKV = new SelectionValues();
		categoriesKV = new SelectionValues();

		initForm(ureq);
		loadModel();
	}

	private void cleanUp() {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(editCategoriesController);
		cmc = null;
		editCategoriesController = null;
	}

	public void setCurrentTimeCode(String currentTimeCode) {
		this.currentTimeCode = currentTimeCode;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		previousSegmentButton = uifactory.addFormLink("previousSegment", "", "",
				formLayout, Link.BUTTON | Link.NONTRANSLATED | Link.LINK_CUSTOM_CSS);
		previousSegmentButton.setIconRightCSS("o_icon o_icon_back");

		segmentsDropdown = uifactory.addDropdownSingleselect("segments", "", formLayout,
				segmentsKV.keys(), segmentsKV.values());
		segmentsDropdown.addActionListener(FormEvent.ONCHANGE);

		nextSegmentButton = uifactory.addFormLink("nextSegment", "", "",
				formLayout, Link.BUTTON | Link.NONTRANSLATED | Link.LINK_CUSTOM_CSS);
		nextSegmentButton.setIconRightCSS("o_icon o_icon_start");

		addSegmentButton = uifactory.addFormLink("addSegment", "form.segment.add",
				"form.segment.add", formLayout, Link.BUTTON);

		startEl = uifactory.addTextElement("start", "form.segment.startEnd", 8, "",
				formLayout);
		startEl.setMandatory(true);

		endEl = uifactory.addTextElement("end", "form.segment.startEnd", 8, "",
				formLayout);
		endEl.setMandatory(true);

		durationEl = uifactory.addTextElement("duration", "form.segment.duration", 3,
				"", formLayout);
		durationEl.setExampleKey("form.segment.duration.hint", null);
		durationEl.setMandatory(true);

//		categoryDropdown = uifactory.addDropdownSingleselect("category", "form.segment.category",
//				formLayout, categoriesKV.keys(), categoriesKV.values());

		categoryButton = uifactory.addFormLink("category", "", "form.segment.category",
				formLayout, Link.BUTTON | Link.NONTRANSLATED);
		categoryButton.setIconRightCSS("o_icon o_icon_caret o_video_segment_category_icon");
		categoryButton.setElementCssClass("o_video_segment_category");
		flc.contextPut("categoryOpen", false);
		flc.contextPut("categoriesAvailable", false);

		editCategoriesButton = uifactory.addFormLink("editCategories", "form.segment.category.edit",
				"form.segment.category.edit", formLayout, Link.BUTTON);

		saveButton = uifactory.addFormSubmitButton("save", formLayout);
		cancelButton = uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
	}

	private void loadModel() {
		videoSegments = videoManager.loadSegments(repositoryEntry.getOlatResource());
		if (videoSegmentId == null) {
			videoSegments.getSegments().stream().findFirst().ifPresent((s) -> {
				videoSegmentId = s.getId();
				selectStartTime();
			});
		}
		handleVideoSegmentsUpdated();

		categoriesKV = new SelectionValues();
		for (VideoSegmentCategory category : videoSegments.getCategories()) {
			categoriesKV.add(SelectionValues.entry(category.getId(), category.getLabelAndTitle()));
		}
	}

	private void handleVideoSegmentsUpdated() {
		flc.contextPut("hasSegments", !videoSegments.getSegments().isEmpty());
		segmentsKV = new SelectionValues();
		for (VideoSegment videoSegment : videoSegments.getSegments()) {
			videoSegments.getCategory(videoSegment.getCategoryId()).ifPresent((c) -> {
				segmentsKV.add(SelectionValues.entry(videoSegment.getId(), c.getLabelAndTitle()));
			});
		}
		segmentsDropdown.setKeysAndValues(segmentsKV.keys(), segmentsKV.values(), null);

		categoriesKV = new SelectionValues();
		for (VideoSegmentCategory category : videoSegments.getCategories()) {
			categoriesKV.add(SelectionValues.entry(category.getId(), category.getLabelAndTitle()));
		}

		setFieldValues();
	}

	private void setFieldValues() {
		if (videoSegmentId != null) {
			segmentsDropdown.select(videoSegmentId, true);
		}

		int selectedIndex = -1;
		for (int i = 0; i < segmentsKV.size(); i++) {
			if (segmentsKV.keys()[i] == videoSegmentId) {
				selectedIndex = i;
				break;
			}
		}
		if (selectedIndex != -1) {
			previousSegmentButton.setEnabled(selectedIndex > 0);
			nextSegmentButton.setEnabled(selectedIndex < (segmentsKV.size() - 1));
		}

		if (videoSegmentId != null) {
			videoSegments.getSegment(videoSegmentId).ifPresent((s) -> {
				startEl.setValue(timeFormat.format(s.getBegin()));
				Date end = DateUtils.addSeconds(s.getBegin(), (int)s.getDuration());
				endEl.setValue(timeFormat.format(end));
				durationEl.setValue(Long.toString(s.getDuration()));
				videoSegments.getCategory(s.getCategoryId()).ifPresent((c) -> {
					categoryButton.setI18nKey(c.getLabelAndTitle());
				});
			});
		}
	}
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (addSegmentButton == source) {
			doAddSegment(ureq);
		} else if (categoryButton == source) {
			doToggleCategory();
		} else if (editCategoriesButton == source) {
			doEditCategories(ureq);
		} else if (segmentsDropdown == source) {
			videoSegmentId = segmentsDropdown.getSelectedKey();
			fireSegmentSelectedEvent(ureq);
			selectStartTime();
			setFieldValues();
		} else if (nextSegmentButton == source) {
			String keys[] = segmentsDropdown.getKeys();
			for (int i = 0; i < keys.length; i++) {
				String key = keys[i];
				if (videoSegmentId != null && videoSegmentId.equals(key)) {
					int newIndex = i + 1;
					if (newIndex < keys.length) {
						videoSegmentId = keys[newIndex];
						fireSegmentSelectedEvent(ureq);
						selectStartTime();
						setFieldValues();
					}
					break;
				}
			}
		} else if (previousSegmentButton == source) {
			String keys[] = segmentsDropdown.getKeys();
			for (int i = 0; i < keys.length; i++) {
				String key = keys[i];
				if (videoSegmentId != null && videoSegmentId.equals(key)) {
					int newIndex = i - 1;
					if (newIndex >= 0) {
						videoSegmentId = keys[newIndex];
						fireSegmentSelectedEvent(ureq);
						selectStartTime();
						setFieldValues();
					}
					break;
				}
			}
		}
	}

	private void doAddSegment(UserRequest ureq) {
		VideoSegmentImpl newSegment = new VideoSegmentImpl();
		newSegment.setId(UUID.randomUUID().toString());
		newSegment.setDuration(5);
		if (currentTimeCode != null) {
			long time = Math.round(Double.parseDouble(currentTimeCode)) * 1000;
			newSegment.setBegin(new Date(time));
		} else {
			newSegment.setBegin(new Date(0));
		}
		if (videoSegments.getCategories().isEmpty()) {
			VideoSegmentCategoryImpl category = new VideoSegmentCategoryImpl();
			category.setId(UUID.randomUUID().toString());
			category.setLabel("OO");
			category.setTitle("Open Olat");
			videoSegments.getCategories().add(category);
		}
		newSegment.setCategoryId(videoSegments.getCategories().get(0).getId());
		videoSegments.getSegments().add(newSegment);
		videoSegmentId = newSegment.getId();
		selectStartTime();
		handleVideoSegmentsUpdated();
		reloadSegments(ureq);
	}

	private void doToggleCategory() {
		boolean categoryOpen = (boolean) flc.contextGet("categoryOpen");
		categoryOpen = !categoryOpen;
		flc.contextPut("categoryOpen", categoryOpen);
		flc.contextPut("categoriesAvailable", !categoriesKV.isEmpty());
		flc.contextPut("categories", videoSegments.getCategories());
		if (categoryOpen) {
			videoSegments.getSegment(videoSegmentId).ifPresent((s) -> {
				flc.contextPut("categoryId", s.getCategoryId());
			});
		}
	}

	private void doEditCategories(UserRequest ureq) {
		if (guardModalController(editCategoriesController)) {
			return;
		}

		editCategoriesController = new EditCategoriesController(ureq, getWindowControl(), videoSegments);
		listenTo(editCategoriesController);

		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				editCategoriesController.getInitialComponent(), true,
				translate("form.segment.editCategories"));
		listenTo(cmc);
		cmc.activate();
	}

	private void reloadSegments(UserRequest ureq) {
		fireEvent(ureq, RELOAD_SEGMENTS_EVENT);
	}

	private void fireSegmentSelectedEvent(UserRequest ureq) {
		fireEvent(ureq, new SegmentSelectedEvent(videoSegmentId));
	}

	@Override
	protected void formOK(UserRequest ureq) {
		videoManager.saveSegments(videoSegments, repositoryEntry.getOlatResource());
		loadModel();
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (editCategoriesController == source) {
			if (event == Event.DONE_EVENT) {

			}
			cmc.deactivate();
			cleanUp();
		} else if (cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void selectStartTime() {
		if (videoSegmentId == null) {
			return;
		}
		videoSegments.getSegment(videoSegmentId).ifPresent((s) -> {
			long timeInSeconds = s.getBegin().getTime() / 1000;
			SelectTimeCommand selectTimeCommand = new SelectTimeCommand(videoElementId, timeInSeconds);
			getWindowControl().getWindowBackOffice().sendCommandTo(selectTimeCommand);
		});
	}
}
