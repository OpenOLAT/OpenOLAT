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
import java.util.Date;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.modules.video.VideoManager;
import org.olat.modules.video.VideoSegment;
import org.olat.modules.video.VideoSegmentCategory;
import org.olat.modules.video.VideoSegments;
import org.olat.repository.RepositoryEntry;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2023-01-23<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class VideoSegmentController extends BasicController {

	private final VelocityContainer mainVC;
	private final RepositoryEntry repositoryEntry;

	@Autowired
	private VideoManager videoManager;
	private VideoSegments segments;
	private final long totalDurationInMillis;

	public VideoSegmentController(UserRequest ureq, WindowControl wControl, RepositoryEntry repositoryEntry,
								  long totalDuratrionInMillis) {
		super(ureq, wControl);

		this.repositoryEntry = repositoryEntry;
		this.totalDurationInMillis = totalDuratrionInMillis;
		mainVC = createVelocityContainer("video_segments");

		putInitialPanel(mainVC);
	}

	public void loadSegments() {
		segments = videoManager.loadSegments(repositoryEntry.getOlatResource());
		if (totalDurationInMillis > 0) {
			mainVC.contextPut("segments", segments.getSegments().stream().map(this::mapToSegment).toList());
		} else {
			mainVC.contextPut("segments", new ArrayList<>());
		}
	}

	private Segment mapToSegment(VideoSegment s) {
		String color = segments.getCategory(s.getCategoryId()).map(VideoSegmentCategory::getColor).orElse("o_video_marker_gray");
		String label = segments.getCategory(s.getCategoryId()).map(VideoSegmentCategory::getLabel).orElse("-");
		return new Segment(s.getBegin(), s.getDuration(), totalDurationInMillis, color, label);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {

	}

	public static class Segment {

		private final String width;
		private final String left;
		private final String color;
		private final String label;

		public Segment(Date begin, long durationInSeconds, long totalDurationInMillis, String color, String label) {
			double width = (durationInSeconds * 1000.0) / totalDurationInMillis;
			this.width = String.format("%.2f%%", width * 100);
			double left = (double) begin.getTime() / totalDurationInMillis;
			this.left = String.format("%.2f%%", left * 100);
			this.color = color;
			this.label = label;
		}

		public String getLeft() {
			return left;
		}

		public String getWidth() {
			return width;
		}

		public String getColor() {
			return color;
		}

		public String getLabel() { return label; }
	}
}
