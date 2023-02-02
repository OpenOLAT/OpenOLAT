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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.emptystate.EmptyStateConfig;
import org.olat.core.gui.components.emptystate.EmptyStateFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.AbstractFlexiTableRenderer;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.modules.video.VideoManager;
import org.olat.modules.video.VideoSegment;
import org.olat.modules.video.VideoSegments;
import org.olat.repository.RepositoryEntry;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2023-01-30<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class SegmentsController extends BasicController {
	public static final Event RELOAD_SEGMENTS_EVENT = new Event("video.edit.reload.segments");
	private final VelocityContainer mainVC;
	private final RepositoryEntry repositoryEntry;
	private final SegmentsHeaderController segmentsHeaderController;
	private final SegmentController segmentController;
	private VideoSegments segments;
	private VideoSegment segment;
	@Autowired
	private VideoManager videoManager;

	public SegmentsController(UserRequest ureq, WindowControl wControl, RepositoryEntry repositoryEntry,
							  String videoElementId, long videoDurationInSeconds) {
		super(ureq, wControl);
		this.repositoryEntry = repositoryEntry;
		mainVC = createVelocityContainer("segments");

		segments = videoManager.loadSegments(repositoryEntry.getOlatResource());
		segments.getSegments().sort(new SegmentComparator());
		segment = segments.getSegments().stream().findFirst().orElse(null);

		segmentsHeaderController = new SegmentsHeaderController(ureq, wControl, videoElementId,
				videoDurationInSeconds);
		segmentsHeaderController.setSegments(segments);
		listenTo(segmentsHeaderController);
		mainVC.put("header", segmentsHeaderController.getInitialComponent());

		segmentController = new SegmentController(ureq, wControl, segment, segments, videoDurationInSeconds);
		listenTo(segmentController);
		if (segment != null) {
			mainVC.put("segment", segmentController.getInitialComponent());
		} else {
			mainVC.remove("segment");
		}

		Translator tableTranslator = Util.createPackageTranslator(AbstractFlexiTableRenderer.class, ureq.getLocale());
		EmptyStateConfig emptyStateConfig = EmptyStateConfig
				.builder()
				.withIconCss("o_icon_empty_objects")
				.withIndicatorIconCss("o_icon_empty_indicator")
				.withMessageTranslated(tableTranslator.translate("default.tableEmptyMessage"))
				.build();
		EmptyStateFactory.create("emptyState", mainVC, this, emptyStateConfig);

		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (segmentController == source) {
			if (event == Event.DONE_EVENT) {
				segment = segmentController.getSegment();
				videoManager.saveSegments(segments, repositoryEntry.getOlatResource());
				segmentsHeaderController.setSegments(segments);
				reloadSegments(ureq);
				fireEvent(ureq, new SegmentSelectedEvent(segment.getId(), segment.getBegin().getTime()));
			}
		} else if (segmentsHeaderController == source) {
			if (event instanceof SegmentSelectedEvent segmentSelectedEvent) {
				segments.getSegments().stream()
						.filter(s -> s.getId().equals(segmentSelectedEvent.getSegmentId()))
						.findFirst().ifPresent(s -> {
							segmentController.setSegment(s);
							fireEvent(ureq, segmentSelectedEvent);
						});
			} else if (event == SegmentsHeaderController.SEGMENT_ADDED_EVENT ||
					event == SegmentsHeaderController.SEGMENT_DELETED_EVENT) {
				this.segments = segmentsHeaderController.getSegments();
				String newSegmentId = segmentsHeaderController.getSegmentId();
				showSegment(newSegmentId);
				segmentController.setSegment(segment);
				videoManager.saveSegments(segments, repositoryEntry.getOlatResource());
				reloadSegments(ureq);
				if (segment != null) {
					fireEvent(ureq, new SegmentSelectedEvent(segment.getId(), segment.getBegin().getTime()));
				}
			}
		}

		super.event(ureq, source, event);
	}

	private void reloadSegments(UserRequest ureq) {
		fireEvent(ureq, RELOAD_SEGMENTS_EVENT);
	}

	public void setCurrentTimeCode(String currentTimeCode) {
		segmentsHeaderController.setCurrentTimeCode(currentTimeCode);
	}

	public void showSegment(String segmentId) {
		this.segment = segments.getSegments().stream().filter(s -> s.getId().equals(segmentId)).findFirst()
				.orElse(null);
		if (segment != null) {
			segmentsHeaderController.setSegmentId(segment.getId());
			segmentController.setSegment(segment);
			mainVC.put("segment", segmentController.getInitialComponent());
		} else {
			segmentsHeaderController.setSegmentId(null);
			mainVC.remove("segment");
		}
	}

	public void handleDeleted(String segmentId) {
		segmentsHeaderController.handleDeleted(segmentId);
		String currentSegmentId = segmentsHeaderController.getSegmentId();
		showSegment(currentSegmentId);
	}

	public void sendSelectionEvent(UserRequest ureq) {
		if (segment != null) {
			fireEvent(ureq, new SegmentSelectedEvent(segment.getId(), segment.getBegin().getTime()));
		}
	}
}
