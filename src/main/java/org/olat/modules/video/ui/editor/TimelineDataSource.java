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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DefaultResultInfos;
import org.olat.core.commons.persistence.ResultInfos;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataSourceDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.modules.video.VideoAssessmentService;
import org.olat.modules.video.VideoManager;
import org.olat.modules.video.VideoMarker;
import org.olat.modules.video.VideoMarkers;
import org.olat.modules.video.VideoQuestion;
import org.olat.modules.video.VideoQuestions;
import org.olat.modules.video.VideoSegment;
import org.olat.modules.video.VideoSegments;
import org.olat.modules.video.VideoTaskSegmentSelection;
import org.olat.modules.video.VideoTaskSession;
import org.olat.modules.video.ui.VideoChapterTableRow;
import org.olat.resource.OLATResource;

/**
 * Initial date: 2022-11-21<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class TimelineDataSource implements FlexiTableDataSourceDelegate<TimelineRow> {

	private final OLATResource olatResource;
	private final List<VideoTaskSession> taskSessions;
	private final VideoManager videoManager;
	private final VideoAssessmentService videoAssessmentService;

	private List<TimelineRow> rows = new ArrayList<>();
	private List<FlexiTableFilter> filters;
	private List<TimelineRow> filteredRows = new ArrayList<>();

	public TimelineDataSource(OLATResource olatResource, List<VideoTaskSession> taskSessions) {
		this.olatResource = olatResource;
		this.taskSessions = taskSessions == null ? List.of() : taskSessions;
		videoManager = CoreSpringFactory.getImpl(VideoManager.class);
		videoAssessmentService = CoreSpringFactory.getImpl(VideoAssessmentService.class);
		loadRows();
	}

	public void loadRows() {
		rows = new ArrayList<>();

		VideoQuestions videoQuestions = videoManager.loadQuestions(olatResource);
		for (VideoQuestion videoQuestion : videoQuestions.getQuestions()) {
			rows.add(new TimelineRow(videoQuestion.getId(), videoQuestion.getBegin().getTime(), 1000,
					TimelineEventType.QUIZ, videoQuestion.getTitle(), videoQuestion.getStyle()));
		}

		VideoSegments videoSegments = videoManager.loadSegments(olatResource);
		for (VideoSegment videoSegment : videoSegments.getSegments()) {
			videoSegments.getCategory(videoSegment.getCategoryId()).ifPresent((c) -> rows.add(
					new TimelineRow(videoSegment.getId(), videoSegment.getBegin().getTime(),
					videoSegment.getDuration() * 1000, TimelineEventType.SEGMENT, c.getLabelAndTitle(),
					c.getColor())
			));
		}

		VideoMarkers videoMarkers = videoManager.loadMarkers(olatResource);
		for (VideoMarker videoMarker : videoMarkers.getMarkers()) {
			rows.add(new TimelineRow(videoMarker.getId(), videoMarker.getBegin().getTime(),
					videoMarker.getDuration() * 1000, TimelineEventType.ANNOTATION, videoMarker.getText(),
					videoMarker.getStyle()));
		}

		List<VideoChapterTableRow> chapters = videoManager.loadChapters(olatResource);
		chapters.forEach((ch) -> {
			String chapterId = generateChapterId(ch);
			long durationInMillis = ch.getEnd().getTime() - ch.getBegin().getTime();
			rows.add(new TimelineRow(chapterId, ch.getBegin().getTime(), durationInMillis,
					TimelineEventType.CHAPTER, ch.getChapterName(), "o_video_marker_gray"));
		});
		
		if(taskSessions != null && !taskSessions.isEmpty()) {
			List<VideoTaskSegmentSelection> segmentsSelections = videoAssessmentService.getTaskSegmentSelections(taskSessions);
			segmentsSelections.forEach(sel -> {
				TimelineEventType type= sel.isCorrect() ? TimelineEventType.CORRECT : TimelineEventType.INCORRECT;
				String color = sel.isCorrect() ? "o_selection_correct" : "o_selection_incorrect";
				rows.add(new TimelineRow("selection-" + sel.getKey(), sel.getTime(), 1000l, type, "", color));
			});
		}

		rows.sort(Comparator.comparing(TimelineRow::getStartTime));

		applyFilters();
	}

	public List<TimelineRow> getRows() {
		return rows;
	}

	private void applyFilters() {
		filteredRows = rows.stream().filter(r -> {
			if (filters == null) {
				return true;
			}
			for (FlexiTableFilter filter : filters) {
				boolean matchFound = false;
				if (filter instanceof FlexiTableMultiSelectionFilter multiSelectionFilter) {
					if (multiSelectionFilter.getValues() == null || multiSelectionFilter.getValues().isEmpty()) {
						continue;
					}
					if (TimelineFilter.TYPE.name().equals(filter.getFilter())) {
						for (String typeString : multiSelectionFilter.getValues()) {
							if (r.getType().name().equals(typeString)) {
								matchFound = true;
								break;
							}
						}
					} else if (TimelineFilter.COLOR.name().equals(filter.getFilter())) {
						for (String colorString : multiSelectionFilter.getValues()) {
							if (r.getColor().equals(colorString)) {
								matchFound = true;
								break;
							}
						}
					}
				}
				if (!matchFound) {
					return false;
				}
			}
			return true;
		}).collect(Collectors.toList());
	}

	public static String generateChapterId(VideoChapterTableRow chapter) {
		return "c_" + chapter.getBegin().hashCode() + "_" + chapter.getEnd().hashCode();
	}

	@Override
	public int getRowCount() {
		return filteredRows.size();
	}

	@Override
	public List<TimelineRow> reload(List<TimelineRow> rows) {
		return Collections.emptyList();
	}

	@Override
	public ResultInfos<TimelineRow> getRows(String query, List<FlexiTableFilter> filters, int firstResult,
											int maxResults, SortKey... orderBy) {
		this.filters = filters;
		applyFilters();
		List<TimelineRow> resultRows = filteredRows.subList(firstResult, filteredRows.size());
		return new DefaultResultInfos<>(firstResult + resultRows.size(),
				-1, resultRows);
	}

	public void delete(TimelineRow row) {
		switch (row.getType()) {
			case QUIZ -> {
				VideoQuestions videoQuestions = videoManager.loadQuestions(olatResource);
				videoQuestions.getQuestions().stream().filter(q -> row.getId().equals(q.getId())).findFirst()
						.ifPresent(q -> videoQuestions.getQuestions().remove(q));
				videoManager.saveQuestions(videoQuestions, olatResource);
			}
			case ANNOTATION -> {
				VideoMarkers videoMarkers = videoManager.loadMarkers(olatResource);
				videoMarkers.getMarkers().stream().filter(m -> row.getId().equals(m.getId())).findFirst()
						.ifPresent(m -> videoMarkers.getMarkers().remove(m));
				videoManager.saveMarkers(videoMarkers, olatResource);
			}
			case SEGMENT -> {
				VideoSegments videoSegments = videoManager.loadSegments(olatResource);
				videoSegments.getSegments().stream().filter(s -> row.getId().equals(s.getId())).findFirst()
						.ifPresent(s -> videoSegments.getSegments().remove(s));
				videoManager.saveSegments(videoSegments, olatResource);
			}
			case CHAPTER -> {
				List<VideoChapterTableRow> chapters = videoManager.loadChapters(olatResource);
				chapters.stream().filter(c -> row.getStartTime() == c.getBegin().getTime()).findFirst()
						.ifPresent(chapters::remove);
				videoManager.saveChapters(chapters, olatResource);
			}
			default -> {
				//
			}
		}
		loadRows();
	}

	public enum TimelineFilter {
		TYPE("filter.type"),
		COLOR("filter.color");

		private final String i18nKey;

		TimelineFilter(String i18nKey) {
			this.i18nKey = i18nKey;
		}

		public String getI18nKey() {
			return i18nKey;
		}
	}
}
