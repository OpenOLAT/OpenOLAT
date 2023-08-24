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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CalloutSettings;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.DateUtils;
import org.olat.core.util.StringHelper;
import org.olat.modules.video.VideoSegment;
import org.olat.modules.video.VideoSegments;

/**
 * Initial date: 2022-12-19<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class SegmentController extends FormBasicController {
	private final static long MIN_DURATION = 5;

	private final long videoDurationInSeconds;
	private VideoSegment segment;
	private final VideoSegments segments;
	private TextElement startEl;
	private TextElement endEl;
	private TextElement durationEl;
	private FormLink categoryButton;
	private FormLink editCategoriesButton;
	private CloseableModalController cmc;
	private EditCategoriesController editCategoriesController;
	private final SimpleDateFormat timeFormat;
	private CloseableCalloutWindowController ccwc;
	private SegmentCategoryController segmentCategoryController;
	private final String videoElementId;
	private final boolean restrictedEdit;

	public SegmentController(UserRequest ureq, WindowControl wControl, VideoSegment segment,
							 VideoSegments segments, long videoDurationInSeconds, String videoElementId, boolean restrictedEdit) {
		super(ureq, wControl, "segment");
		this.segment = segment;
		this.segments = segments;
		this.videoDurationInSeconds = videoDurationInSeconds;
		this.videoElementId = videoElementId;
		this.restrictedEdit = restrictedEdit;

		flc.contextPut("videoDurationInSeconds", videoDurationInSeconds);

		timeFormat = new SimpleDateFormat("HH:mm:ss");
		timeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

		initForm(ureq);
		setValues();
	}

	public void setSegment(VideoSegment segment) {
		this.segment = segment;
		setValues();
	}

	public VideoSegment getSegment() {
		return segment;
	}

	private void cleanUp() {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(ccwc);
		removeAsListenerAndDispose(editCategoriesController);
		removeAsListenerAndDispose(segmentCategoryController);
		cmc = null;
		ccwc = null;
		editCategoriesController = null;
		segmentCategoryController = null;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		startEl = uifactory.addTextElement("start", "form.segment.startEnd", 8, "",
				formLayout);
		startEl.setMandatory(true);
		startEl.setEnabled(!restrictedEdit);

		ApplyPositionButtonController startApplyPositionButtonController = new ApplyPositionButtonController(ureq,
				getWindowControl(), startEl.getFormDispatchId(), videoElementId, mainForm.getDispatchFieldId(), restrictedEdit);
		flc.put("startApplyPosition", startApplyPositionButtonController.getInitialComponent());

		endEl = uifactory.addTextElement("end", "form.segment.startEnd", 8, "",
				formLayout);
		endEl.setMandatory(true);
		endEl.setEnabled(!restrictedEdit);

		ApplyPositionButtonController endApplyPositionButtonController = new ApplyPositionButtonController(ureq,
				getWindowControl(), endEl.getFormDispatchId(), videoElementId, mainForm.getDispatchFieldId(), restrictedEdit);
		flc.put("endApplyPosition", endApplyPositionButtonController.getInitialComponent());

		durationEl = uifactory.addTextElement("duration", "form.segment.duration", 10,
				"", formLayout);
		durationEl.setExampleKey("form.segment.duration.hint", null);
		durationEl.setMandatory(true);
		durationEl.setEnabled(!restrictedEdit);

		categoryButton = uifactory.addFormLink("category", "", "form.segment.category",
				formLayout, Link.BUTTON | Link.NONTRANSLATED);
		categoryButton.setIconRightCSS("o_icon o_icon_caret o_video_segment_category_icon");
		categoryButton.setElementCssClass("o_video_segment_category");
		categoryButton.setEnabled(!restrictedEdit);

		editCategoriesButton = uifactory.addFormLink("editCategories", "form.segment.category.edit",
				"form.segment.category.edit", formLayout, Link.BUTTON);

		uifactory.addFormSubmitButton("save", formLayout);
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
	}

	private void setValues() {
		flc.contextPut("overlapError", false);

		if (segment == null) {
			return;
		}

		startEl.setValue(timeFormat.format(segment.getBegin()));
		Date end = DateUtils.addSeconds(segment.getBegin(), (int)segment.getDuration());
		endEl.setValue(timeFormat.format(end));
		durationEl.setValue(Long.toString(segment.getDuration()));
		segments.getCategory(segment.getCategoryId())
				.ifPresent(c -> categoryButton.setI18nKey(c.getLabelAndTitle()));
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (categoryButton == source) {
			doSelectCategory(ureq);
		} else if (editCategoriesButton == source) {
			doEditCategories(ureq);
		}
	}

	private void doSelectCategory(UserRequest ureq) {
		if (segment == null) {
			return;
		}

		segmentCategoryController = new SegmentCategoryController(ureq, getWindowControl(), segments, segment);
		listenTo(segmentCategoryController);
		ccwc = new CloseableCalloutWindowController(ureq, getWindowControl(),
				segmentCategoryController.getInitialComponent(), categoryButton.getFormDispatchId(), "",
				true, "", new CalloutSettings(false));
		listenTo(ccwc);
		ccwc.activate();
	}

	private void doEditCategories(UserRequest ureq) {
		if (guardModalController(editCategoriesController)) {
			return;
		}

		editCategoriesController = new EditCategoriesController(ureq, getWindowControl(), segments, restrictedEdit);
		listenTo(editCategoriesController);

		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				editCategoriesController.getInitialComponent(), true,
				translate("form.segment.editCategories"));
		listenTo(cmc);
		cmc.activate();
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		allOk &= validateTime(startEl);
		allOk &= validateTime(endEl);
		allOk &= validateDuration();

		return allOk;
	}

	private boolean validateTime(TextElement timeEl) {
		boolean allOk = true;
		timeEl.clearError();
		if (!StringHelper.containsNonWhitespace(timeEl.getValue())) {
			timeEl.setErrorKey("form.legende.mandatory");
			allOk = false;
		} else {
			try {
				long timeInSeconds = timeFormat.parse(timeEl.getValue()).getTime() / 1000;
				if (timeInSeconds < 0 || (videoDurationInSeconds > 0 && timeInSeconds > videoDurationInSeconds)) {
					timeEl.setErrorKey("form.error.timeNotValid");
					allOk = false;
				}
				allOk &= validateNoOverlap();
			} catch (Exception e) {
				timeEl.setErrorKey("form.error.timeFormat");
				allOk = false;
			}
		}
		return allOk;
	}

	private boolean validateNoOverlap() {
		flc.contextPut("overlapError", false);
		try {
			long startNew = timeFormat.parse(startEl.getValue()).getTime();
			long endNew = timeFormat.parse(endEl.getValue()).getTime();
			return segments.getSegments().stream().allMatch(s -> {
				if (s.getId().equals(segment.getId())) {
					return true;
				}
				long startTest = s.getBegin().getTime();
				long endTest = startTest + s.getDuration() * 1000;
				boolean valid = startNew >= endTest || endNew <= startTest;
				if (!valid) {
					flc.contextPut("overlapError", true);
				}
				return valid;
			});
		} catch (Exception e) {
			return true;
		}
	}

	private boolean validateDuration() {
		boolean allOk = true;
		durationEl.clearError();
		if (!StringHelper.containsNonWhitespace(durationEl.getValue())) {
			durationEl.setErrorKey("form.legende.mandatory");
			allOk = false;
		} else if (!StringHelper.isLong(durationEl.getValue())) {
			durationEl.setErrorKey("form.error.nointeger");
			allOk = false;
		} else if (Long.parseLong(durationEl.getValue()) < MIN_DURATION) {
			durationEl.setErrorKey("form.error.timeNotValid");
			allOk = false;
		}
		return allOk;
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if (segment == null) {
			return;
		}

		try {
			segment.setBegin(timeFormat.parse(startEl.getValue()));
			segment.setDuration(Long.parseLong(durationEl.getValue()));
			fireEvent(ureq, Event.DONE_EVENT);
		} catch (ParseException e) {
			logError("", e);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (editCategoriesController == source) {
			if (event == Event.DONE_EVENT) {
				setValues();
				fireEvent(ureq, Event.DONE_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		} else if (cmc == source) {
			cleanUp();
		} else if (ccwc == source) {
			cleanUp();
		} else if (segmentCategoryController == source) {
			if (event == Event.DONE_EVENT) {
				setValues();
			}
			ccwc.deactivate();
			cleanUp();
		}
		super.event(ureq, source, event);
	}
}
