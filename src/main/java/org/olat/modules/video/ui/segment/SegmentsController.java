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
package org.olat.modules.video.ui.segment;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.course.nodes.video.VideoRunController;
import org.olat.modules.video.VideoManager;
import org.olat.modules.video.VideoSegment;
import org.olat.modules.video.VideoSegmentCategory;
import org.olat.modules.video.VideoSegments;
import org.olat.repository.RepositoryEntry;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2023-03-16<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class SegmentsController extends BasicController {

	@Autowired
	private VideoManager videoManager;

	public SegmentsController(UserRequest ureq, WindowControl wControl,
							  RepositoryEntry videoEntry, String videoElementId, long totalDurationInMillis) {
		super(ureq, wControl);

		VelocityContainer segmentsVC = createVelocityContainer("display_segments");

		VideoSegments segments = videoManager.loadSegments(videoEntry.getOlatResource());
		List<VideoSegment> segmentsList = segments.getSegments();
		List<VideoRunController.RuntimeSegment> markers = new ArrayList<>(segmentsList.size());
		for (VideoSegment videoSegment : segmentsList) {
			VideoSegmentCategory category = segments.getCategory(videoSegment.getCategoryId()).orElse(null);
			String durationString = translate("duration.description", Long.toString(videoSegment.getDuration()));
			VideoRunController.RuntimeSegment solution = VideoRunController.RuntimeSegment.valueOf(category, videoSegment, totalDurationInMillis, durationString);
			markers.add(solution);
		}
		segmentsVC.contextPut("segments", markers);
		segmentsVC.contextPut("videoElementId", videoElementId);

		putInitialPanel(segmentsVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
}
