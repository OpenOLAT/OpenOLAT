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

import org.olat.core.commons.services.color.ColorService;
import org.olat.core.commons.services.image.Size;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SliderElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRenderEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StickyActionColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.winmgr.JSCommand;
import org.olat.core.gui.media.ForbiddenMediaResource;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.modules.video.VideoFormat;
import org.olat.modules.video.VideoManager;
import org.olat.modules.video.VideoMeta;
import org.olat.modules.video.VideoModule;
import org.olat.modules.video.VideoSegmentCategory;
import org.olat.modules.video.VideoSegments;
import org.olat.modules.video.VideoTaskSession;
import org.olat.modules.video.ui.VideoSettingsController;
import org.olat.modules.video.ui.component.VideoTimeCellRenderer;
import org.olat.repository.RepositoryEntry;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2022-11-18<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class MasterController extends FormBasicController implements FlexiTableComponentDelegate {
	private static final String THUMBNAIL_JPG_SUFFIX = ".jpg";
	private static final String THUMBNAIL_BASE_FILE_NAME = "thumbnail_";
	private static final String CMD_TOOLS = "tools";
	private static final String SELECT_ACTION = "select";

	private final VFSContainer thumbnailsContainer;
	private final VFSLeaf videoFile;
	private final long videoFrameCount;
	private long videoDurationInMillis;
	private int fps;
	private final Size movieSize;

	private final TimelineDataSource timelineDataSource;
	private TimelineModel timelineModel;
	private FlexiTableElement timelineTableEl;

	private SliderElement zoomSlider;
	private FormLink zoomMinusButton;
	private FormLink zoomPlusButton;

	@Autowired
	private ColorService colorService;
	@Autowired
	private VideoManager videoManager;
	private int availableWidth;
	private CloseableCalloutWindowController ccwc;
	private ToolsController toolsController;
	private String currentTimeCode;
	private final boolean showVideoTrack;
	private final boolean showCategoryFilter;

	public MasterController(UserRequest ureq, WindowControl wControl, RepositoryEntry repositoryEntry,
							List<VideoTaskSession> sessions, String videoElementId, long durationInSeconds) {
		super(ureq, wControl, "master");
		flc.contextPut("videoElementId", videoElementId);
		thumbnailsContainer = videoManager.getThumbnailsContainer(repositoryEntry.getOlatResource());
		timelineDataSource = new TimelineDataSource(repositoryEntry.getOlatResource(), sessions, getTranslator());
		videoFile = videoManager.getMasterVideoFile(repositoryEntry.getOlatResource());
		showVideoTrack = videoFile != null;
		showCategoryFilter = sessions != null && !sessions.isEmpty();

		if (showVideoTrack) {
			videoFrameCount = videoManager.getVideoFrameCount(videoFile);
			videoDurationInMillis = videoManager.getVideoDuration(repositoryEntry.getOlatResource());
		} else {
			videoFrameCount = -1;
			videoDurationInMillis = TimelineModel.parsedExpenditureOfWork(repositoryEntry.getExpenditureOfWork());
		}
		if (videoDurationInMillis <= 0) {
			videoDurationInMillis = durationInSeconds * 1000L;
		}
		flc.contextPut("durationInSeconds", Double.toString((double) this.videoDurationInMillis / 1000.0));
		flc.contextPut("currentTimeInSeconds", "0.0");

		VideoMeta videoMetadata = videoManager.getVideoMetadata(repositoryEntry.getOlatResource());
		boolean isYoutube = videoMetadata.getVideoFormat() == VideoFormat.youtube;
		flc.contextPut("isYoutube", isYoutube);

		this.movieSize = new Size(90, 50, false);
		initForm(ureq);
		updateVisibility();
		initFilters(ureq);
		addTools();
	}

	private void cleanUp() {
		removeAsListenerAndDispose(ccwc);
		removeAsListenerAndDispose(toolsController);
		ccwc = null;
		toolsController = null;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TimelineCols.startTime, SELECT_ACTION,
				new VideoTimeCellRenderer()));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TimelineCols.type,
				(renderer, sb, val, row, source, ubu, translator) -> {
			if (val instanceof TimelineEventType) {
				sb.append("<i class=\"o_icon ").append(((TimelineEventType) val).getIcon()).append("\"></i>");
			}
		}));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TimelineCols.text, SELECT_ACTION,
				(renderer, sb, val, row, source, ubu, translator) -> {
			if (val instanceof String) {
				sb.append(((String) val));
			}
		}));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TimelineCols.color,
				(renderer, sb, val, row, source, ubu, translator) -> {
			if (val instanceof String) {
				sb.append("<div class=\"o_video_color_circle o_video_colored_area ").append((String) val).append("\">").append("</div>");
			}
		}));
		StickyActionColumnModel toolsColumn = new StickyActionColumnModel(TimelineCols.tools.i18nHeaderKey(),
				TimelineCols.tools.ordinal());
		toolsColumn.setIconHeader("o_icon o_icon_actions o_icon-fws o_icon-lg");
		toolsColumn.setColumnCssClass("o_icon-fws o_col_sticky_right o_col_action");
		columnModel.addFlexiColumnModel(toolsColumn);

		timelineModel = new TimelineModel(timelineDataSource, columnModel);
		String mediaUrl = registerMapper(ureq, new ThumbnailMapper());
		timelineModel.setMediaUrl(mediaUrl);
		timelineModel.setScaleFactor(0.1);
		timelineModel.setVideoLength(videoDurationInMillis);
		if (showVideoTrack) {
			timelineModel.setVisibleChannels(List.of(TimelineEventType.QUIZ, TimelineEventType.COMMENT,
					TimelineEventType.ANNOTATION, TimelineEventType.SEGMENT, TimelineEventType.CHAPTER,
					TimelineEventType.VIDEO));
		} else {
			timelineModel.setVisibleChannels(List.of(TimelineEventType.QUIZ, TimelineEventType.COMMENT,
					TimelineEventType.ANNOTATION, TimelineEventType.SEGMENT, TimelineEventType.CHAPTER));
		}
		fps = (int) (1000L * videoFrameCount / videoDurationInMillis);

		timelineTableEl = uifactory.addTableElement(getWindowControl(), "timelineEvents", timelineModel,
				10, true, getTranslator(), formLayout);

		timelineTableEl.setAvailableRendererTypes(FlexiTableRendererType.external, FlexiTableRendererType.classic);
		timelineTableEl.setRendererType(FlexiTableRendererType.external);
		timelineTableEl.setExternalRenderer(new TimelineRenderer(), "o_icon_timeline");
		timelineTableEl.setCustomizeColumns(false);

		zoomMinusButton = uifactory.addFormLink("zoomMinusButton", "", null, formLayout,
				Link.LINK_CUSTOM_CSS | Link.NONTRANSLATED);
		zoomMinusButton.setIconLeftCSS("o_icon o_icon_shrink");

		zoomSlider = uifactory.addSliderElement("zoomSlider", "", formLayout);
		zoomSlider.setMinValue(0);
		zoomSlider.setMaxValue(100);
		zoomSlider.setValue(0);

		zoomPlusButton = uifactory.addFormLink("zoomPlusButton", "", null, formLayout,
				Link.LINK_CUSTOM_CSS | Link.NONTRANSLATED);
		zoomPlusButton.setIconLeftCSS("o_icon o_icon_enlarge");
	}

	private void addTools() {
		for (TimelineRow timelineRow : timelineDataSource.getRows()) {
			String toolId = "tool_" + timelineRow.getId();
			FormLink toolLink = (FormLink) timelineTableEl.getFormComponent(toolId);
			if (toolLink == null) {
				toolLink = uifactory.addFormLink(toolId, CMD_TOOLS, "", timelineTableEl,
						Link.LINK | Link.NONTRANSLATED);
				toolLink.setTranslator(getTranslator());
				toolLink.setIconLeftCSS("o_icon o_icon_actions o_icon-fws o_icon-lg");
				toolLink.setTitle(translate("table.header.timeline.tools"));
			}
			toolLink.setUserObject(timelineRow);
			timelineRow.setToolLink(toolLink);
		}
	}

	private void initFilters(UserRequest ureq) {
		timelineTableEl.setSearchEnabled(false);
		List<FlexiTableExtendedFilter> filters = new ArrayList<>();

		SelectionValues typeKV = new SelectionValues();
		List.of(TimelineEventType.values())
				.forEach(type -> typeKV.add(SelectionValues.entry(type.name(), translate(type.getI18nKey()))));
		typeKV.sort(SelectionValues.VALUE_ASC);
		filters.add(new FlexiTableMultiSelectionFilter(translate(TimelineDataSource.TimelineFilter.TYPE.getI18nKey()),
				TimelineDataSource.TimelineFilter.TYPE.name(), typeKV, true));

		Translator videoTranslator = Util.createPackageTranslator(VideoSettingsController.class, ureq.getLocale());
		SelectionValues colorKV = new SelectionValues();
		for (String color : colorService.getColors()) {
			String style = VideoModule.getMarkerStyleFromColor(color);
			colorKV.add(SelectionValues.entry(style, videoTranslator.translate("video.marker.style.".concat(style))));
		}
		colorKV.sort(SelectionValues.VALUE_ASC);
		filters.add(new FlexiTableMultiSelectionFilter(translate(TimelineDataSource.TimelineFilter.COLOR.getI18nKey()),
				TimelineDataSource.TimelineFilter.COLOR.name(), colorKV, true));
		
		if(showCategoryFilter) {
			SelectionValues categoryKV = new SelectionValues();
			VideoSegments videoSegments = timelineDataSource.getVideoSegments();
			for(VideoSegmentCategory category:videoSegments.getCategories()) {
				categoryKV.add(SelectionValues.entry(category.getId(), category.getLabelAndTitle()));
			}
			filters.add(new FlexiTableMultiSelectionFilter(translate(TimelineDataSource.TimelineFilter.CATEGORY.getI18nKey()),
					TimelineDataSource.TimelineFilter.CATEGORY.name(), categoryKV, true));
		}
		
		timelineTableEl.setFilters(true, filters, true, false);
		timelineTableEl.expandFilters(true);
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (ccwc == source) {
			cleanUp();
		} else if (toolsController == source) {
			if (ToolsController.DELETE_EVENT.getCommand().equals(event.getCommand())) {
				TimelineRow row = toolsController.getRow();
				timelineDataSource.delete(row);
				addTools();
				timelineTableEl.reloadData();
				fireEvent(ureq, new TimelineEventDeletedEvent(row.getType(), row.getId()));
			}
			ccwc.deactivate();
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (zoomMinusButton == source) {
			doZoomOut();
		} else if (zoomPlusButton == source) {
			doZoomIn();
		} else if (timelineTableEl == source) {
			if ("ONCLICK".equals(event.getCommand())) {
				String questionId = ureq.getParameter("questionId");
				if (questionId != null) {
					timelineModel.select(questionId);
					timelineModel.getTimelineRow(TimelineEventType.QUIZ, questionId)
							.ifPresent(q -> fireEvent(ureq, new QuestionSelectedEvent(q.getId(), q.getStartTime())));
				}
				String annotationId = ureq.getParameter("annotationId");
				if (annotationId != null) {
					timelineModel.select(annotationId);
					timelineModel.getTimelineRow(TimelineEventType.ANNOTATION, annotationId)
									.ifPresent(a -> fireEvent(ureq, new AnnotationSelectedEvent(a.getId(),
											a.getStartTime(), a.getDuration() / 1000)));
				}
				String chapterId = ureq.getParameter("chapterId");
				if (chapterId != null) {
					timelineModel.select(chapterId);
					timelineModel.getTimelineRow(TimelineEventType.CHAPTER, chapterId)
							.ifPresent(c -> fireEvent(ureq, new ChapterSelectedEvent(c.getId(), c.getStartTime())));
				}
				String segmentId = ureq.getParameter("segmentId");
				if (segmentId != null) {
					timelineModel.select(segmentId);
					timelineModel.getTimelineRow(TimelineEventType.SEGMENT, segmentId)
							.ifPresent(s -> fireEvent(ureq, new SegmentSelectedEvent(s.getId(), s.getStartTime(),
									s.getDuration() / 1000)));
				}
				String commentId = ureq.getParameter("commentId");
				if (commentId != null) {
					timelineModel.select(commentId);
					timelineModel.getTimelineRow(TimelineEventType.COMMENT, commentId)
							.ifPresent(c -> fireEvent(ureq, new CommentSelectedEvent(c.getId(), c.getStartTime())));
				}
			} else if (event instanceof FlexiTableRenderEvent renderEvent) {
				if (FlexiTableRenderEvent.CHANGE_RENDER_TYPE.equals(event.getCommand())) {
					if (renderEvent.getRendererType() == FlexiTableRendererType.external) {
						flc.contextPut("currentTimeInSeconds", StringHelper.containsNonWhitespace(currentTimeCode) ? currentTimeCode : "0.0");
					}
					updateVisibility();
				}
			} else if (event instanceof SelectionEvent selectionEvent) {
				if (SELECT_ACTION.equals(selectionEvent.getCommand())) {
					TimelineRow timelineRow = timelineModel.getObject(selectionEvent.getIndex());
					doSelect(ureq, timelineRow);
				}
			} else if (event instanceof FlexiTableSearchEvent) {
				updateVisibility();
			}
		} else if (source instanceof FormLink formLink &&
				CMD_TOOLS.equals(formLink.getCmd()) && formLink.getUserObject() instanceof TimelineRow timelineRow) {
			doOpenTools(ureq, formLink, timelineRow);
		} else if (zoomSlider == source) {
			doZoom();
		}
	}

	private void updateVisibility() {
		boolean playHeadVisible = timelineTableEl.getRendererType() == FlexiTableRendererType.external && timelineModel.getRowCount() > 0;
		flc.contextPut("showPlayHead", playHeadVisible);
		zoomSlider.setVisible(playHeadVisible);
		zoomMinusButton.setVisible(playHeadVisible);
		zoomPlusButton.setVisible(playHeadVisible);
	}

	private void doSelect(UserRequest ureq, TimelineRow timelineRow) {
		switch (timelineRow.getType()) {
			case QUIZ -> fireEvent(ureq, new QuestionSelectedEvent(timelineRow.getId(), timelineRow.getStartTime()));
			case ANNOTATION -> fireEvent(ureq, new AnnotationSelectedEvent(timelineRow.getId(), timelineRow.getStartTime(),
					timelineRow.getDuration() / 1000));
			case CHAPTER -> fireEvent(ureq, new ChapterSelectedEvent(timelineRow.getId(), timelineRow.getStartTime()));
			case SEGMENT -> fireEvent(ureq, new SegmentSelectedEvent(timelineRow.getId(), timelineRow.getStartTime(),
					timelineRow.getDuration() / 1000));
			case COMMENT -> fireEvent(ureq, new CommentSelectedEvent(timelineRow.getId(), timelineRow.getStartTime()));
			case VIDEO -> {
			}
		}
	}

	private void doOpenTools(UserRequest ureq, FormLink formLink, TimelineRow timelineRow) {
		toolsController = new ToolsController(ureq, getWindowControl(), timelineRow);
		listenTo(toolsController);
		ccwc = new CloseableCalloutWindowController(ureq, getWindowControl(), toolsController.getInitialComponent(),
				formLink.getFormDispatchId(), "", true, "");
		listenTo(ccwc);
		ccwc.activate();
	}

	private void doZoomOut() {
		double value = zoomSlider.getValue();
		value = Double.max(value - 10, 0);
		if (value != zoomSlider.getValue()) {
			zoomSlider.setValue(value);
			doZoom();
		}
	}

	private void doZoomIn() {
		double value = zoomSlider.getValue();
		value = Double.min(value + 10, 100);
		if (value != zoomSlider.getValue()) {
			zoomSlider.setValue(value);
			doZoom();
		}
	}

	private void doZoom() {
		double value = zoomSlider.getValue();
		double exponent = (value - 50) / 50;
		double scaleFactor = Math.pow(10, exponent);
		timelineModel.setScaleFactor(scaleFactor);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		return new ArrayList<>();
	}
	
	public void setVisibleChannels(List<TimelineEventType> visibleChannels) {
		timelineModel.setVisibleChannels(visibleChannels);
	}

	public void setAvailableWidth(int availableWidth) {
		this.availableWidth = availableWidth;
		timelineModel.setAvailableWidth(availableWidth);
	}

	public int getAvailableWidth() {
		return availableWidth;
	}

	public void reload() {
		timelineDataSource.loadRows();
		timelineTableEl.reloadData();
		addTools();
		updateVisibility();
	}

	public void setCurrentTimeCode(String currentTimeCode) {
		this.currentTimeCode = currentTimeCode;
		flc.contextPut("currentTimeInSeconds", StringHelper.containsNonWhitespace(currentTimeCode) ? currentTimeCode : "0.0");
	}

	public void select(String id) {
		timelineModel.select(id);
		String typeClass = timelineModel.getActiveTypeAsClass();
		JSCommand command = new JSCommand(
				"try {" +
						"  jQuery('.o_video_timeline_box').removeClass('o_video_active');" +
						(typeClass != null ? "  jQuery('.o_video_timeline_box." + typeClass + "').addClass('o_video_active');" : "") +
						"  jQuery('.o_video_selected').removeClass('o_video_selected');" +
						"  jQuery('#o_video_event_" + id + "').addClass('o_video_selected');" +
						"} catch(e) {" +
						"  if (window.console) console.log(e);" +
						"}"
				);
		getWindowControl().getWindowBackOffice().sendCommandTo(command);
	}

	public void updateVideoDuration(String durationInSecondsString) {
		if (videoDurationInMillis >= 0 || timelineModel == null) {
			return;
		}

		try {
			long durationInSeconds = Math.round(Double.parseDouble(durationInSecondsString));
			if (durationInSeconds > 0L) {
				videoDurationInMillis = durationInSeconds * 1000L;
				fps = (int) (1000L * videoFrameCount / videoDurationInMillis);
				timelineModel.setVideoLength(videoDurationInMillis);
				flc.contextPut("durationInSeconds", durationInSecondsString);
			}
		} catch (NumberFormatException e) {
			logError("Cannot parse duration: " + durationInSecondsString, e);
		}
	}

	public void setTypeOnly(TimelineEventType type) {
		timelineModel.setActiveType(type);
		String typeClass = timelineModel.getActiveTypeAsClass();
		JSCommand command = new JSCommand(
				"try {" +
						"  jQuery('.o_video_timeline_box').removeClass('o_video_active');" +
						(typeClass != null ? "  jQuery('.o_video_timeline_box." + typeClass + "').addClass('o_video_active');" : "") +
						"  jQuery('.o_video_selected').removeClass('o_video_selected');" +
						"} catch(e) {" +
						"  if (window.console) console.log(e);" +
						"}"
		);
		getWindowControl().getWindowBackOffice().sendCommandTo(command);
	}

	private class ThumbnailMapper implements Mapper {
		@Override
		public MediaResource handle(String relPath, HttpServletRequest request) {
			int thumbnailKeywordIndex = relPath.indexOf(THUMBNAIL_BASE_FILE_NAME);
			int suffixIndex = relPath.indexOf(THUMBNAIL_JPG_SUFFIX);
			if (thumbnailKeywordIndex == -1 || suffixIndex == -1 || suffixIndex <= thumbnailKeywordIndex) {
				return new ForbiddenMediaResource();
			}
			VFSItem thumbnailFile = thumbnailsContainer.resolve(relPath);
			if (thumbnailFile instanceof VFSLeaf) {
				return new VFSMediaResource((VFSLeaf) thumbnailFile);
			}
			String secondString = relPath.substring(thumbnailKeywordIndex + THUMBNAIL_BASE_FILE_NAME.length(), suffixIndex);
			try {
				int second = Integer.parseInt(secondString);
				int frameNumber = fps * second;
				VFSLeaf outputLeaf = thumbnailsContainer.createChildLeaf(relPath);
				videoManager.getFrameWithFilter(videoFile, movieSize, frameNumber, videoFrameCount, outputLeaf);
				return new VFSMediaResource(outputLeaf);
			} catch (NumberFormatException e) {
				return new NotFoundMediaResource();
			}
		}
	}

	private static class ToolsController extends BasicController {
		private static final Event DELETE_EVENT = new Event("delete");
		private final Link deleteLink;
		private final TimelineRow row;

		protected ToolsController(UserRequest ureq, WindowControl wControl, TimelineRow row) {
			super(ureq, wControl);

			this.row = row;

			VelocityContainer mainVC = createVelocityContainer("table_row_tools");
			deleteLink = LinkFactory.createLink("delete", "delete", getTranslator(), mainVC, this,
					Link.LINK);
			deleteLink.setIconLeftCSS("o_icon o_icon-fw o_icon_delete_item");
			mainVC.put("delete", deleteLink);

			putInitialPanel(mainVC);
		}

		public TimelineRow getRow() {
			return row;
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			if (deleteLink == source) {
				fireEvent(ureq, DELETE_EVENT);
			}
		}
	}
}
