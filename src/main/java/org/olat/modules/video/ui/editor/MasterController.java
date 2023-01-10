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
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.media.ForbiddenMediaResource;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.modules.video.VideoManager;
import org.olat.modules.video.VideoMeta;
import org.olat.modules.video.VideoModule;
import org.olat.modules.video.ui.VideoSettingsController;
import org.olat.resource.OLATResource;

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

	private final String videoElementId;
	private VFSContainer thumbnailsContainer;
	private VFSLeaf videoFile;
	private long videoFrameCount;
	private final long videoDurationInMillis;
	private int fps;
	private Size movieSize;

	private final VideoMeta videoMetadata;
	private TimelineDataSource timelineDataSource;
	private TimelineModel timelineModel;
	private FlexiTableElement timelineTableEl;

	private SliderElement zoomSlider;
	private FormLink zoomMinusButton;
	private FormLink zoomPlusButton;

	@Autowired
	private VideoModule videoModule;
	@Autowired
	private VideoManager videoManager;
	private int availableWidth;
	private int pageSize;

	public MasterController(UserRequest ureq, WindowControl wControl, OLATResource olatResource,
							VideoMeta videoMetadata, String videoElementId) {
		super(ureq, wControl, "master");
		flc.contextPut("videoElementId", videoElementId);
		this.videoElementId = videoElementId;
		thumbnailsContainer = videoManager.getThumbnailsContainer(olatResource);
		timelineDataSource = new TimelineDataSource(olatResource);
		this.videoMetadata = videoMetadata;
		this.videoFile = videoManager.getMasterVideoFile(olatResource);
		this.videoFrameCount = videoManager.getVideoFrameCount(videoFile);
		this.videoDurationInMillis = videoManager.getVideoDuration(olatResource);
		this.movieSize = new Size(90, 50, false);
		initForm(ureq);
		loadModel();
		initFilters(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TimelineCols.startTime));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TimelineCols.type,
				(renderer, sb, val, row, source, ubu, translator) -> {
			if (val instanceof TimelineEventType) {
				sb.append("<i class=\"o_icon ").append(((TimelineEventType) val).getIcon()).append("\"></i>");
			}
		}));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TimelineCols.text,
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

		timelineModel = new TimelineModel(timelineDataSource, columnModel);
		String mediaUrl = registerMapper(ureq, new ThumbnailMapper());
		timelineModel.setMediaUrl(mediaUrl);
		timelineModel.setScaleFactor(0.1);
//		try {
//			String duration = videoMetadata.getLength();
//			if (duration.indexOf(':') == duration.lastIndexOf(':')) {
//				duration = "00:" + duration;
//			}
//			timelineModel.setVideoLength(VideoHelper.parseTimeToSeconds(duration) * 1000);
			timelineModel.setVideoLength(videoDurationInMillis);
//		} catch (ParseException e) {
//			timelineModel.setVideoLength(1000);
//		}
		fps = (int) (1000L * videoFrameCount / videoDurationInMillis);

		timelineTableEl = uifactory.addTableElement(getWindowControl(), "timelineEvents", timelineModel,
				10, true, getTranslator(), formLayout);

		timelineTableEl.setAvailableRendererTypes(FlexiTableRendererType.external, FlexiTableRendererType.classic);
		timelineTableEl.setRendererType(FlexiTableRendererType.external);
		timelineTableEl.setExternalRenderer(new TimelineRenderer(), "o_icon_fa6_timeline");
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

	private void loadModel() {

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
		for (String color : videoModule.getMarkerStyles()) {
			colorKV.add(SelectionValues.entry(color, videoTranslator.translate("video.marker.style.".concat(color))));
		}
		colorKV.sort(SelectionValues.VALUE_ASC);
		filters.add(new FlexiTableMultiSelectionFilter(translate(TimelineDataSource.TimelineFilter.COLOR.getI18nKey()),
				TimelineDataSource.TimelineFilter.COLOR.name(), colorKV, true));

		timelineTableEl.setFilters(true, filters, true, false);
		timelineTableEl.expandFilters(true);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source instanceof FormLink) {
			FormLink button = (FormLink) source;
			if ("zoomMinusButton".equals(button.getCmd())) {
				double value = zoomSlider.getValue();
				value = Double.max(value - 10, 0);
				zoomSlider.setValue(value);
				doZoom();
			} else if ("zoomPlusButton".equals(button.getCmd())) {
				double value = zoomSlider.getValue();
				value = Double.min(value + 10, 100);
				zoomSlider.setValue(value);
				doZoom();
			}
		} else if (source == timelineTableEl) {
			if (event instanceof FormEvent) {
				if (event.getCommand() == "ONCLICK") {
					String annotationId = ureq.getParameter("annotationId");
					if (annotationId != null) {
						fireEvent(ureq, new AnnotationSelectedEvent(annotationId));
					}
					String chapterId = ureq.getParameter("chapterId");
					if (chapterId != null) {
						timelineModel.getChapterRow(chapterId).ifPresent((c) -> {
							fireEvent(ureq, new ChapterSelectedEvent(c.getId(), c.getStartTime()));
						});
					}
				}
			}
		} else if (source.getName().equals("zoomSlider")) {
			doZoom();
		} else if (event instanceof FlexiTableRenderEvent) {
			FlexiTableRenderEvent flexiTableRenderEvent = (FlexiTableRenderEvent) event;
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
		List<Component> components = new ArrayList<>();
		return components;
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
}
