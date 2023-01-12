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

import java.util.List;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormJSHelper;
import org.olat.core.gui.components.form.flexible.impl.NameValuePair;
import org.olat.core.gui.components.form.flexible.impl.elements.table.AbstractFlexiTableRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableElementImpl;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;

/**
 * Initial date: 2022-11-29<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class TimelineRenderer extends AbstractFlexiTableRenderer {

	@Override
	protected void renderUserOptions(Renderer renderer, StringOutput sb, FlexiTableElementImpl ftE, URLBuilder ubu,
									 Translator translator, RenderResult renderResult) {
		Form theForm = ftE.getRootForm();

		FormItem minusButton = theForm.getFormItemContainer().getFormComponent("zoomMinusButton");
		FormItem plusButton = theForm.getFormItemContainer().getFormComponent("zoomPlusButton");
		FormItem zoomSlider = theForm.getFormItemContainer().getFormComponent("zoomSlider");

		if (ftE.getRendererType() == FlexiTableRendererType.external) {
			sb.append("<div class='o_video_timeline_tools'>");
			renderFormItem(renderer, sb, minusButton, ubu, translator, renderResult, null);
			sb.append("<span class='o_video_timeline_slider'>");
			renderFormItem(renderer, sb, zoomSlider, ubu, translator, renderResult, null);
			sb.append("</span>");
			renderFormItem(renderer, sb, plusButton, ubu, translator, renderResult, null);
			sb.append("</div>");
		}
	}

	@Override
	public void renderComponent(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
					   RenderResult renderResult, String[] args) {

		FlexiTableComponent ftC = (FlexiTableComponent) source;
		FlexiTableElementImpl ftE = ftC.getFormItem();
		String id = ftC.getFormDispatchId();

		renderHeaders(renderer, sb, ftE, ubu, translator, renderResult, args);

		if (ftE.getTableDataModel().getRowCount() == 0 && StringHelper.containsNonWhitespace(ftE.getEmtpyTableMessageKey())) {
			renderEmptyState(renderer, sb, ubu, translator, renderResult, ftE);
		} else {
			// render wrapper
			String wrapperCss = null;
			if (ftE.getCssDelegate() != null) {
				wrapperCss = ftE.getCssDelegate().getWrapperCssClass(FlexiTableRendererType.external);
			}
			if (!StringHelper.containsNonWhitespace(wrapperCss)) {
				wrapperCss = "o_table_wrapper o_table_flexi";
			}
			sb.append("<div class=\"").append(wrapperCss)
					.append(" o_table_edit", ftE.isEditMode());
			String css = ftE.getElementCssClass();
			if (css != null) {
				sb.append(" ").append(css);
			}
			sb.append(" o_rendertype_user o_rendertype_timeline\">");

			// render body
			String tableCss = null;
			if (ftE.getCssDelegate() != null) {
				tableCss = ftE.getCssDelegate().getTableCssClass(FlexiTableRendererType.custom);
			}
			if (!StringHelper.containsNonWhitespace(tableCss)) {
				tableCss = "o_table_body";
			}
			sb.append("<div class='").append(tableCss).append("'>");
			renderBody(renderer, sb, ftC, ubu, translator, renderResult);
			sb.append("</div>");

//			renderTreeButtons(sb, ftC, translator);
//			if (ftE.getDefaultPageSize() > 0) {
//				renderPagesLinks(sb, ftC, translator);
//			}
			sb.append("</div>");
		}
		
		setHeadersRendered(ftE);

		//source
		if (source.isEnabled()) {
			FormJSHelper.appendFlexiFormDirty(sb, ftE.getRootForm(), id);
		}
	}

	@Override
	protected void renderBody(Renderer renderer, StringOutput target, FlexiTableComponent ftC,
							  URLBuilder ubu, Translator translator, RenderResult renderResult) {
		ftC.getFormItem().getTableDataModel().getTableColumnModel();
		renderTimeline(renderer, target, ftC);
	}

	private void renderTimeline(Renderer renderer, StringOutput s, FlexiTableComponent ftC) {
		FlexiTableDataModel dataModel = ftC.getFormItem().getTableDataModel();
		if (!(dataModel instanceof TimelineModel)) {
			return;
		}
		TimelineModel timelineModel = (TimelineModel) dataModel;
		s.append("<div style=\"height: 8px;\"></div>");
		s.append("<div class=\"o_video_timeline\">");
		renderChannelLabels(s, timelineModel);
		renderChannels(renderer, s, ftC, timelineModel);
		s.append("</div>");
		renderPostprocessing(s);
	}

	private void renderChannelLabels(StringOutput s, TimelineModel timelineModel) {
		s.append("<div class=\"o_video_channel_labels\">");
		renderChannelLabel(s, TimelineEventType.QUIZ, timelineModel);
		renderChannelLabel(s, TimelineEventType.ANNOTATION, timelineModel);
		renderChannelLabel(s, TimelineEventType.VIDEO, timelineModel);
		renderChannelLabel(s, TimelineEventType.CHAPTER, timelineModel);
		renderChannelLabel(s, TimelineEventType.SEGMENT, timelineModel);
		s.append("</div>");
	}

	private void renderChannelLabel(StringOutput s, TimelineEventType type, TimelineModel timelineModel) {
		int height = getChannelHeight(type, timelineModel);
		int lineHeight = height - 10;
		s
				.append("<div style=\"height: ")
				.append(height)
				.append("px; line-height: ")
				.append(lineHeight)
				.append("px;\" class=\"o_video_channel_label o_video_timeline_box\">")
				.append("<i class=\"o_icon o_icon_lg ")
				.append(type.getIcon())
				.append("\"></i>")
				.append("</div>");
	}

	private int getChannelHeight(TimelineEventType type, TimelineModel timelineModel) {
		if (type == TimelineEventType.VIDEO) {
			return 50;
		}
		List<TimelineRow> events = timelineModel.getEventsByType(type);
		int nbLanes = Integer.max(TimelineModel.getNumberOfLanes(events), 1);
		int height = 10 + nbLanes * 20 + 5 * (nbLanes - 1);
		return height;
	}

	private void renderChannels(Renderer renderer, StringOutput s, FlexiTableComponent ftC, TimelineModel timelineModel) {
		s.append("<div class=\"o_video_channels\">");
		renderChannel(s, TimelineEventType.QUIZ, ftC, timelineModel);
		renderChannel(s, TimelineEventType.ANNOTATION, ftC, timelineModel);
		renderVideoChannel(s, ftC, timelineModel);
		renderChannel(s, TimelineEventType.CHAPTER, ftC, timelineModel);
		renderChannel(s, TimelineEventType.SEGMENT, ftC, timelineModel);
		s.append("</div>");
	}

	private void renderVideoChannel(StringOutput s, FlexiTableComponent ftC, TimelineModel timelineModel) {
		int channelWidth = timelineModel.getChannelWidth();
		long videoLength = timelineModel.getVideoLength();
		int thumbnailWidth = 89;
		int nbThumbnails = channelWidth / 89;
		s.append("<div style=\"background-color: black; width: ").append(channelWidth)
				.append("px; height: 50px;\" class=\"o_video_channel o_video_timeline_box\">");

		int accumulatedWidth = 0;
		for (int i = 0; i <= nbThumbnails; i++) {
			int second = (int) Math.round((videoLength * ((i + 0.5d) * thumbnailWidth) / channelWidth) / 1000d);
			int width = Math.min(thumbnailWidth, channelWidth - accumulatedWidth);
			String url = timelineModel.getMediaUrl() + "/thumbnail_" + second + ".jpg";
			if (width > 0) {
				s.append("<div style=\" background-image: url('")
						.append(url)
						.append("'); display: inline-block; width: ").append(width)
						.append("px; height: 50px;\"></div>");
			}
			accumulatedWidth += width;
		}

		s.append("</div>");
	}

	private void renderChannel(StringOutput s, TimelineEventType type, FlexiTableComponent ftC,
							   TimelineModel timelineModel) {
		List<TimelineRow> timelineEvents = timelineModel.getEventsByType(type);
		int height = getChannelHeight(type, timelineModel);
		s.append("<div style=\"width: ").append(timelineModel.getChannelWidth())
				.append("px; height: ").append(height).append("px;\" class=\"o_video_channel o_video_timeline_box\">");
		renderTimelineEvents(s, type, ftC, timelineModel, timelineEvents);
		s.append("</div>");
	}

	private void renderTimelineEvents(StringOutput s, TimelineEventType type, FlexiTableComponent ftC,
									  TimelineModel timelineModel,
									  List<TimelineRow> timelineEvents) {
		FlexiTableElementImpl ftE = ftC.getFormItem();
		Form theForm = ftE.getRootForm();

		List<List<TimelineRow>> eventsByLanes = TimelineModel.distributeToLanes(timelineEvents);
		int y = 5;
		for (List<TimelineRow> eventsByLane : eventsByLanes) {
			for (TimelineRow event : eventsByLane) {
				switch (type) {
					case QUIZ:
						renderQuestion(s, ftC, theForm, timelineModel, event, y, "questionId");
						break;
					case ANNOTATION:
						renderEvent(s, ftC, theForm, timelineModel, event, y, "o_video_annotation", "annotationId");
						break;
					case CHAPTER:
						renderEvent(s, ftC, theForm, timelineModel, event, y, "o_video_chapter", "chapterId");
						break;
					case SEGMENT:
						renderEvent(s, ftC, theForm, timelineModel, event, y, "o_video_segment", "segmentId");
						break;
				}
			}
			y += 20 + 5;
		}
	}

	private void renderQuestion(StringOutput s, FlexiTableComponent ftC, Form form, TimelineModel timelineModel,
								TimelineRow event, int y, String idParameterName) {
		long x = event.getStartTime() * timelineModel.getChannelWidth() / timelineModel.getVideoLength();
		if (x == 0) {
			x += 1;
		}
		s.append("<div class=\"o_video_question ").append(event.getColor()).append("\" style=\"left: ").append(x - 11).append("px; top: ").append(y - 1)
				.append("px; \" onclick=\"")
				.append("jQuery('.o_video_selected').removeClass('o_video_selected'); ")
				.append("jQuery(this).addClass('o_video_selected'); ")
				.append(FormJSHelper.getXHRFnCallFor(form, ftC.getFormDispatchId(), 1,
						false, false, false,
						new NameValuePair(idParameterName, event.getId())))
				.append("\">")
				.append("<svg style=\"\" viewBox=\"-64 -64 640 640\">" +
						"<path d=\"M284.3 11.7c-15.6-15.6-40.9-15.6-56.6 0l-216 216c-15.6 15.6-15.6 40.9 0 56.6l216 216c15.6 15.6 40.9 15.6 56.6 0l216-216c15.6-15.6 15.6-40.9 0-56.6l-216-216z\"/>" +
						"</svg>")
				.append("</div>");
	}

	private void renderEvent(StringOutput s, FlexiTableComponent ftC, Form form, TimelineModel timelineModel,
							 TimelineRow event, int y, String cssClass, String idParameterName) {
		long x = event.getStartTime() * timelineModel.getChannelWidth() / timelineModel.getVideoLength();
		long width = event.getDuration() * timelineModel.getChannelWidth() / timelineModel.getVideoLength();
		if (x == 0) {
			x += 1;
			width -= 1;
		}
		s.append("<div class=\"o_video_timeline_event ").append(cssClass).append(" ").append(event.isSelected() ? "o_video_selected " : "")
				.append(event.getColor())
				.append("\" style=\"left: ").append(x).append("px; width: ").append(width).append("px; ")
				.append("top: ").append(y).append("px;")
				.append("\" onclick=\"")
				.append("jQuery('.o_video_selected').removeClass('o_video_selected'); ")
				.append("jQuery(this).addClass('o_video_selected'); ")
				.append(FormJSHelper.getXHRFnCallFor(form, ftC.getFormDispatchId(), 1,
						false, false, false,
						new NameValuePair(idParameterName, event.getId())))
				.append(";\"")
				.append(">").append(event.getText()).append("</div>");
	}

	private void renderPostprocessing(StringOutput stringOutput) {
		// Annotations are typically rich text. We only want to render plain text in the timeline events.
		stringOutput
				.append("<script>")
				.append("function stripAnnotations() {")
				.append("  jQuery('.o_video_annotation').each(function() { jQuery(this).text(jQuery(this).text()); });")
				.append("}\n")
				.append("stripAnnotations();\n")
				.append("</script>");
	}

	@Override
	protected void renderHeaders(StringOutput target, FlexiTableComponent ftC, Translator translator) {
	}

	@Override
	protected void renderRow(Renderer renderer, StringOutput target, FlexiTableComponent ftC, String rowIdPrefix, int row, URLBuilder ubu, Translator translator, RenderResult renderResult) {
	}

	@Override
	protected void renderFooter(Renderer renderer, StringOutput target, FlexiTableComponent ftC, URLBuilder ubu, Translator translator, RenderResult renderResult) {
	}
}
