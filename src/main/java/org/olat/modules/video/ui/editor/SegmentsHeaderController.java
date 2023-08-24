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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.stream.Collectors;

import org.olat.core.commons.services.color.ColorService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.modules.video.VideoSegment;
import org.olat.modules.video.VideoSegmentCategory;
import org.olat.modules.video.VideoSegments;
import org.olat.modules.video.model.VideoSegmentCategoryImpl;
import org.olat.modules.video.model.VideoSegmentImpl;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2023-01-30<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class SegmentsHeaderController extends FormBasicController {
	public final static Event SEGMENT_ADDED_EVENT = new Event("segment.added");
	public final static Event SEGMENT_DELETED_EVENT = new Event("segment.deleted");

	private final static long DEFAULT_DURATION = 5;
	private final long videoDurationInSeconds;
	private final boolean restrictedEdit;
	private VideoSegments segments;
	private String segmentId;
	private String currentTimeCode;
	private FormLink previousSegmentButton;
	private SelectionValues segmentsKV = new SelectionValues();
	private SingleSelection segmentsDropdown;
	private FormLink nextSegmentButton;
	private FormLink addSegmentButton;
	private FormLink commandsButton;
	private HeaderCommandsController commandsController;
	private CloseableCalloutWindowController ccwc;
	private final SimpleDateFormat timeFormat;
	@Autowired
	private ColorService colorService;

	public SegmentsHeaderController(UserRequest ureq, WindowControl wControl, long videoDurationInSeconds, boolean restrictedEdit) {
		super(ureq, wControl, "segments_header");
		this.videoDurationInSeconds = videoDurationInSeconds;
		this.restrictedEdit = restrictedEdit;

		timeFormat = new SimpleDateFormat("HH:mm:ss");
		timeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

		initForm(ureq);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(ccwc);
		removeAsListenerAndDispose(commandsController);
		commandsController = null;
		ccwc = null;
	}
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		previousSegmentButton = uifactory.addFormLink("previousSegment", "", "",
				formLayout, Link.BUTTON | Link.NONTRANSLATED | Link.LINK_CUSTOM_CSS);
		previousSegmentButton.setIconRightCSS("o_icon o_icon_back");
		previousSegmentButton.setForceOwnDirtyFormWarning(true);

		segmentsDropdown = uifactory.addDropdownSingleselect("segments", "", formLayout,
				segmentsKV.keys(), segmentsKV.values());
		segmentsDropdown.addActionListener(FormEvent.ONCHANGE);

		nextSegmentButton = uifactory.addFormLink("nextSegment", "", "",
				formLayout, Link.BUTTON | Link.NONTRANSLATED | Link.LINK_CUSTOM_CSS);
		nextSegmentButton.setIconRightCSS("o_icon o_icon_start");
		nextSegmentButton.setForceOwnDirtyFormWarning(true);

		addSegmentButton = uifactory.addFormLink("addSegment", "form.add", "form.add", formLayout, Link.BUTTON);
		addSegmentButton.setIconLeftCSS("o_icon o_icon-fw o_icon_add");
		addSegmentButton.setEnabled(!restrictedEdit);

		commandsButton = uifactory.addFormLink("commands", "", "", formLayout,
				Link.BUTTON | Link.NONTRANSLATED | Link.LINK_CUSTOM_CSS);
		commandsButton.setIconRightCSS("o_icon o_icon_commands");
		commandsButton.setEnabled(!restrictedEdit);
	}

	public void setSegments(VideoSegments segments) {
		this.segments = segments;
		setValues();
	}

	public VideoSegments getSegments() {
		return segments;
	}

	private void setValues() {
		segmentsKV = new SelectionValues();
		segments
				.getSegments()
				.stream()
				.sorted(new SegmentComparator())
				.forEach(s -> segments.getCategory(s.getCategoryId()).ifPresent(c ->
						segmentsKV.add(SelectionValues.entry(s.getId(),
								timeFormat.format(s.getBegin()) + " - " + c.getLabelAndTitle()))));
		flc.contextPut("hasSegments", !segmentsKV.isEmpty());
		segmentsDropdown.setKeysAndValues(segmentsKV.keys(), segmentsKV.values(), null);

		if (segments.getSegments().stream().noneMatch(s -> s.getId().equals(segmentId))) {
			segmentId = null;
		}
		if (segmentId == null && !segmentsKV.isEmpty()) {
			segmentId = segmentsKV.keys()[0];
		}
		if (segmentId != null) {
			segmentsDropdown.select(segmentId, true);
		}

		int selectedIndex = -1;
		for (int i = 0; i < segmentsKV.size(); i++) {
			if (segmentsKV.keys()[i].equals(segmentId)) {
				selectedIndex = i;
				break;
			}
		}
		if (selectedIndex != -1) {
			previousSegmentButton.setEnabled(selectedIndex > 0);
			nextSegmentButton.setEnabled(selectedIndex < (segmentsKV.size() - 1));
		}

		commandsButton.setEnabled(!segmentsKV.isEmpty() && !restrictedEdit);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (addSegmentButton == source) {
			doAddSegment(ureq);
		} else if (commandsButton == source) {
			doCommands(ureq);
		} else if (segmentsDropdown == source) {
			if (segmentsDropdown.isOneSelected()) {
				segmentId = segmentsDropdown.getSelectedKey();
				setValues();
				handleSegmentSelected(ureq);
			}
		} else if (nextSegmentButton == source) {
			doNextSegment(ureq);
		} else if (previousSegmentButton == source) {
			doPreviousSegment(ureq);
		}

		super.formInnerEvent(ureq, source, event);
	}

	private void handleSegmentSelected(UserRequest ureq) {
		getOptionalSegment()
				.ifPresent(s -> fireEvent(ureq, new SegmentSelectedEvent(s.getId(), s.getBegin().getTime(),
						s.getDuration())));
	}

	private void doNextSegment(UserRequest ureq) {
		String[] keys = segmentsDropdown.getKeys();
		for (int i = 0; i < keys.length; i++) {
			String key = keys[i];
			if (segmentId != null && segmentId.equals(key)) {
				int newIndex = i + 1;
				if (newIndex < keys.length) {
					segmentId = keys[newIndex];
					setValues();
					handleSegmentSelected(ureq);
				}
				break;
			}
		}
	}

	private void doPreviousSegment(UserRequest ureq) {
		String[] keys = segmentsDropdown.getKeys();
		for (int i = 0; i < keys.length; i++) {
			String key = keys[i];
			if (segmentId != null && segmentId.equals(key)) {
				int newIndex = i - 1;
				if (newIndex >= 0) {
					segmentId = keys[newIndex];
					setValues();
					handleSegmentSelected(ureq);
				}
				break;
			}
		}
	}

	private Optional<VideoSegment> getOptionalSegment() {
		if (segmentId == null) {
			return Optional.empty();
		}
		return segments.getSegments().stream().filter(s -> segmentId.equals(s.getId())).findFirst();
	}

	private void doAddSegment(UserRequest ureq) {
		List<VideoSegment> freeSegments = freeSegments();
		if (freeSegments.isEmpty()) {
			showInfo("form.segment.add.error");
		}

		long timeInSeconds = 0;
		if (currentTimeCode != null) {
			timeInSeconds = Math.round(Double.parseDouble(currentTimeCode));
		}
		VideoSegment freeSegment = closestSegment(freeSegments, timeInSeconds);
		if (freeSegment == null) {
			return;
		}

		VideoSegmentImpl newSegment = new VideoSegmentImpl();
		newSegment.setId(UUID.randomUUID().toString());
		newSegment.setBegin(new Date(timeInSeconds * 1000));
		newSegment.setDuration(DEFAULT_DURATION);
		fitSegment(newSegment, freeSegment);

		setCategory(newSegment);
		segments.getSegments().add(newSegment);
		segmentId = newSegment.getId();

		setValues();
		fireEvent(ureq, SEGMENT_ADDED_EVENT);
	}

	private void setCategory(VideoSegmentImpl newSegment) {
		if (segments.getCategories().isEmpty()) {
			VideoSegmentCategoryImpl category = new VideoSegmentCategoryImpl();
			category.setId(UUID.randomUUID().toString());
			category.setLabel(translate("form.segment.category.label.new"));
			category.setTitle(translate("form.segment.category.title.new"));
			category.setColor(colorService.getColors().get(0));
			segments.getCategories().add(category);
		}

		Set<String> usedCategoryIds = segments.getSegments().stream().map(VideoSegment::getCategoryId)
				.collect(Collectors.toSet());
		Optional<VideoSegmentCategory> unusedCategory = segments.getCategories().stream()
				.filter(c -> !usedCategoryIds.contains(c.getId())).findFirst();
		unusedCategory.ifPresentOrElse(
				c -> newSegment.setCategoryId(c.getId()),
				() -> newSegment.setCategoryId(segments.getCategories().get(0).getId())
		);
	}

	private void fitSegment(VideoSegment toFit, VideoSegment on) {
		if (on.getDuration() < toFit.getDuration()) {
			return;
		}

		long toFitBegin = toFit.getBegin().getTime() / 1000;
		long onBegin = on.getBegin().getTime() / 1000;
		long overlapLeft = onBegin - toFitBegin;
		if (overlapLeft > 0) {
			toFit.setBegin(new Date((toFitBegin + overlapLeft) * 1000));
			return;
		}

		long overlapRight = (toFitBegin + toFit.getDuration()) - (onBegin + on.getDuration());
		if (overlapRight > 0) {
			toFit.setBegin(new Date((toFitBegin - overlapRight) * 1000));
		}
	}

	static VideoSegment closestSegment(List<VideoSegment> segments, long t) {
		long minDist = Long.MAX_VALUE;
		VideoSegment bestCandidate = null;
		for (VideoSegment segment : segments) {
			long t0 = segment.getBegin().getTime() / 1000;
			long t1 = t0 + segment.getDuration();
			long dist = Math.abs((t0 + t1) / 2 - t);
			if (dist < minDist) {
				bestCandidate = segment;
				minDist = dist;
			}
		}
		return bestCandidate;
	}

	private List<VideoSegment> freeSegments() {
		List<VideoSegment> freeSegments = new ArrayList<>();
		long beginningOfFreeSpace = 0;
		for (VideoSegment segment : segments.getSegments().stream().sorted(new SegmentComparator()).toList()) {
			long start = segment.getBegin().getTime() / 1000;
			long duration = start - beginningOfFreeSpace;
			if (duration >= SegmentsHeaderController.DEFAULT_DURATION) {
				VideoSegment newSegment = new VideoSegmentImpl();
				newSegment.setBegin(new Date(beginningOfFreeSpace * 1000));
				newSegment.setDuration(duration);
				freeSegments.add(newSegment);
			}
			beginningOfFreeSpace = start + segment.getDuration();
		}

		if (videoDurationInSeconds > beginningOfFreeSpace) {
			long duration = videoDurationInSeconds - beginningOfFreeSpace;
			if (duration >= SegmentsHeaderController.DEFAULT_DURATION) {
				VideoSegment newSegment = new VideoSegmentImpl();
				newSegment.setBegin(new Date(beginningOfFreeSpace * 1000));
				newSegment.setDuration(duration);
				freeSegments.add(newSegment);
			}
		}

		return freeSegments;
	}

	private void doCommands(UserRequest ureq) {
		commandsController = new HeaderCommandsController(ureq, getWindowControl());
		listenTo(commandsController);
		ccwc = new CloseableCalloutWindowController(ureq, getWindowControl(), commandsController.getInitialComponent(),
				commandsButton.getFormDispatchId(), "", true, "");
		listenTo(ccwc);
		ccwc.activate();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (ccwc == source) {
			cleanUp();
		} else if (commandsController == source) {
			if (HeaderCommandsController.DELETE_EVENT.getCommand().equals(event.getCommand())) {
				doDeleteSegment(ureq);
			}
			ccwc.deactivate();
			cleanUp();
		}
	}

	private void doDeleteSegment(UserRequest ureq) {
		if (segmentId != null) {
			segments.getSegment(segmentId).ifPresent(s -> {
				segments.getSegments().remove(s);
				if (segments.getSegments().isEmpty()) {
					segmentId = null;
				} else {
					segmentId = segments.getSegments().get(0).getId();
				}
				setValues();
				fireEvent(ureq, SEGMENT_DELETED_EVENT);
			});
		}
	}

	public void setCurrentTimeCode(String currentTimeCode) {
		this.currentTimeCode = currentTimeCode;
	}

	public void setSegmentId(String segmentId) {
		this.segmentId = segmentId;
		if (segmentId != null) {
			setValues();
		}
	}
	public String getSegmentId() {
		return segmentId;
	}

	public void handleDeleted(String segmentId) {
		segments.getSegments().removeIf(s -> s.getId().equals(segmentId));
		setSegments(segments);
	}
}
