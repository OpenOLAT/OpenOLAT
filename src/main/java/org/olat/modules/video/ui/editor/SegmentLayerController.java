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

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.winmgr.functions.FunctionCommand;
import org.olat.course.nodes.video.VideoRunController;
import org.olat.modules.video.VideoManager;
import org.olat.modules.video.VideoSegment;
import org.olat.modules.video.VideoSegments;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2023-03-21<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class SegmentLayerController extends BasicController {
	private final VelocityContainer mainVC;
	private final RepositoryEntry repositoryEntry;
	private final long totalDurationInMillis;
	private VideoSegments segments;
	@Autowired
	private VideoManager videoManager;
	private String segmentId;

	protected SegmentLayerController(UserRequest ureq, WindowControl wControl, RepositoryEntry repositoryEntry,
									 String videoElementId, long totalDurationInMillis) {
		super(ureq, wControl);

		this.repositoryEntry = repositoryEntry;
		this.totalDurationInMillis = totalDurationInMillis;

		mainVC = createVelocityContainer("segment_layer");
		mainVC.contextPut("videoElementId", videoElementId);
		putInitialPanel(mainVC);

		clearSegments();
	}

	public void loadSegments() {
		segments = videoManager.loadSegments(repositoryEntry.getOlatResource());
		List<VideoRunController.RuntimeSegment> runtimeSegments = segments.getSegments().stream().map(s -> mapToRuntimeSegment(s, segments)).toList();
		mainVC.contextPut("segments", runtimeSegments);
	}

	private VideoRunController.RuntimeSegment mapToRuntimeSegment(VideoSegment segment, VideoSegments segments) {
		return segments.getCategory(segment.getCategoryId()).map(c ->
			VideoRunController.RuntimeSegment.valueOf(c, segment, totalDurationInMillis,
					translate("segment.duration", Long.toString(segment.getDuration())))
		).orElse(null);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	public void hideSegment() {
		if (segmentId != null) {
			setSegmentVisible(segmentId, false);
			segmentId = null;
		}
	}

	private void setSegmentVisible(String segmentId, boolean visible) {
		segments.getSegment(segmentId).map(s -> segments.getSegments().indexOf(s))
				.ifPresent(segmentIndex -> {
					String elementId = "vt-marker-" + (segmentIndex + 1);
					getWindowControl().getWindowBackOffice().sendCommandTo(FunctionCommand.showTooltip(elementId, visible));
				});
	}

	public void setSegment(String id) {
		if (segmentId == null) {
			segmentId = id;
			setSegmentVisible(segmentId, true);
		} else if (!segmentId.equals(id)) {
			setSegmentVisible(segmentId, false);
			segmentId = id;
			setSegmentVisible(segmentId, true);
		}
	}

	public void clearSegments() {
		mainVC.contextPut("segments", new ArrayList<>());
		segmentId = null;
	}

	public void setSegments() {
		loadSegments();
	}
}
