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

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataSourceModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataSourceDelegate;

/**
 * Initial date: 2022-11-21<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class TimelineModel extends DefaultFlexiTableDataSourceModel<TimelineRow> {

	// Video length in milliseconds
	private long videoLength;

	// A logical scale factor used to set the level of detail and the size of returned
	// assets.
	//
	// The scale factor of 1 is the default. It is used to show a video timeline in a meaningful way.
	//
	// A factor of 10 is the most detailed scale factor. It should allow a user to recognize and find scene details.
	//
	// A factor of 0.1 is the least detailed scale factor. It should give an overview of the video.
	//
	private double scaleFactor;

	// The available width for the timeline.
	private int availableWidth;
	private String mediaUrl;

	public TimelineModel(FlexiTableDataSourceDelegate<TimelineRow> dataSource, FlexiTableColumnModel columnModel) {
		super(dataSource, columnModel);
	}

	@Override
	public Object getValueAt(int row, int col) {
		TimelineRow timelineRow = getObject(row);
		return switch (TimelineCols.values()[col]) {
			case startTime -> durationString(timelineRow.getStartTime());
			case type -> timelineRow.getType();
			case text -> timelineRow.getText();
			case color -> timelineRow.getColor();
		};
	}

	private String durationString(long timeInMillis) {
		Duration duration = Duration.ofMillis(timeInMillis);
		return String.format("%02d:%02d:%02d", duration.toHours(), duration.toMinutesPart(), duration.toSecondsPart());
	}

	@Override
	public DefaultFlexiTableDataSourceModel<TimelineRow> createCopyWithEmptyList() {
		return new TimelineModel(getSourceDelegate(), getTableColumnModel());
	}

	public long getVideoLength() {
		return videoLength;
	}

	public void setVideoLength(long videoLength) {
		this.videoLength = videoLength;
	}

	public double getScaleFactor() {
		return scaleFactor;
	}

	public void setScaleFactor(double scaleFactor) {
		this.scaleFactor = scaleFactor;
	}

	public List<TimelineRow> getEventsByType(TimelineEventType type) {
		return getObjects()
				.stream()
				.filter(o -> o.getType() == type)
				.sorted((a, b) -> (int)(a.getStartTime() - b.getStartTime()))
				.collect(Collectors.toList());
	}

	public int getAvailableWidth() {
		return availableWidth;
	}

	public void setAvailableWidth(int availableWidth) {
		this.availableWidth = availableWidth;
	}

	public int getChannelWidth() {
		// First deduct the width of the o_video_channel_label plus the gap between label and channel payload
		int availableWidthWithoutLabelColumn = availableWidth - (50 + 5);
		if (availableWidthWithoutLabelColumn < 400) {
			return 400;
		}

		// In the fully zoomed-out view the available viewport width matches up with the video length:
		double minWidth = availableWidthWithoutLabelColumn;

		// The fully zoomed-in view is the width required to render a filmstrip of all thumbnails. It must be at least
		// twice the minWidth and must not exceed 20 times the available width.
		double maxWidth = videoLength / 1000d / 10d * 50d;

		maxWidth = Math.max(maxWidth, minWidth * 2);
		maxWidth = Math.min(maxWidth, availableWidthWithoutLabelColumn * 20);

		double a = (maxWidth - minWidth) / 9.9;
		double b = (100 * minWidth - maxWidth) / 99;
		return (int) (a * scaleFactor + b);
	}

	public static int getNumberOfLanes(List<TimelineRow> events) {
		List<Pair<Long, Integer>> counterEvents = new ArrayList<>();
		events.forEach((event) -> {
			counterEvents.add(Pair.of(event.getStartTime(), 1));
			counterEvents.add(Pair.of(event.getStartTime() + event.getDuration(), -1));
		});
		counterEvents.sort((a, b) -> {
			if (a.getLeft() < b.getLeft()) {
				return -1;
			}
			if (a.getLeft() > b.getLeft()) {
				return 1;
			}
			return a.getRight() - b.getRight();
		});
		final int[] nbLanes = {0};
		final int[] maxLanes = {0};
		counterEvents.forEach((event) -> {
			nbLanes[0] += event.getRight();
			if (nbLanes[0] > maxLanes[0]) {
				maxLanes[0] = nbLanes[0];
			}
		});
		return maxLanes[0];
	}

	public static List<List<TimelineRow>> distributeToLanes(List<TimelineRow> events) {
		List<List<TimelineRow>> result = new ArrayList<>();
		List<Pair<Long, TimelineRow>> counterEvents = new ArrayList<>();
		events.forEach((event) -> {
			counterEvents.add(Pair.of(event.getStartTime(), event));
			counterEvents.add(Pair.of(event.getStartTime() + event.getDuration(), null));
		});
		counterEvents.sort((a, b) -> {
			if (a.getLeft() < b.getLeft()) {
				return -1;
			}
			if (a.getLeft() > b.getLeft()) {
				return 1;
			}
			return (a.getRight() != null ? 1 : -1) - (b.getRight() != null ? 1 : -1);
		});
		final int[] nbLanes = {0};
		counterEvents.forEach((event) -> {
			nbLanes[0] += (event.getRight() != null ? 1 : -1);
			if (event.getRight() != null) {
				if (result.size() < nbLanes[0]) {
					result.add(new ArrayList<>());
				}
				long currentTime = event.getLeft();
				for (List<TimelineRow> lane : result) {
					if (lane.isEmpty()) {
						lane.add(event.getRight());
						break;
					}
					TimelineRow lastEvent = lane.get(lane.size() - 1);
					if (currentTime >= (lastEvent.getStartTime() + lastEvent.getDuration())) {
						lane.add(event.getRight());
						break;
					}
				}
			}
		});
		return result;
	}

	public Optional<TimelineRow> getChapterRow(String chapterId) {
		return getObjects()
				.stream()
				.filter((r) -> r.getType() == TimelineEventType.CHAPTER && r.getId().equals(chapterId))
				.findFirst();
	}

	public Optional<TimelineRow> getQuestionRow(String questionId) {
		return getObjects()
				.stream()
				.filter((r) -> r.getType() == TimelineEventType.QUIZ && r.getId().equals(questionId))
				.findFirst();
	}

	public void setMediaUrl(String mediaUrl) {
		this.mediaUrl = mediaUrl;
	}

	public String getMediaUrl() {
		return mediaUrl;
	}
}
