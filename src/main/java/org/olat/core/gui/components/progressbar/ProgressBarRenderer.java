/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/ 

package org.olat.core.gui.components.progressbar;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.components.progressbar.ProgressBar.BarColor;
import org.olat.core.gui.components.progressbar.ProgressBar.LabelFormat;
import org.olat.core.gui.components.progressbar.ProgressBar.LabelPosition;
import org.olat.core.gui.components.progressbar.ProgressBar.Marker;
import org.olat.core.gui.components.progressbar.ProgressBar.RenderSize;
import org.olat.core.gui.components.progressbar.ProgressBar.RenderStyle;
import org.olat.core.gui.components.progressbar.ProgressBar.SegmentLabelFormat;
import org.olat.core.gui.components.progressbar.ProgressBar.Segment;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;

/**
 * Renders the progress bar component. The same markup is used by the
 * ProgressBarCellRenderer in tables.
 * 
 * Initial Date: Feb 2, 2004
 * 
 * @author Andreas Ch. Kapp
 */
public class ProgressBarRenderer extends DefaultComponentRenderer {

	private static final long FULL = 10000;
	
	private record SegmentWidth(Segment segment, long start, long width) {
		//
	}
	
	@Override
	public void renderComponent(Renderer renderer, StringOutput target, Component source, URLBuilder urlBuilder, Translator translator,
			RenderResult renderResult, String[] args) {
		ProgressBar bar = (ProgressBar) source;
		ProgressBarCallback callback = bar.getProgressCallback();
		if(callback != null) {
			bar.setMax(callback.getMax());
			bar.setSegments(callback.getSegments());
		}
		renderProgressBar(target, bar, "o_c" + bar.getDispatchID());
	}
	
	/**
	 * @param domId The id of the outer element or null
	 */
	public static void renderProgressBar(StringOutput target, ProgressBar bar, String domId) {
		render(target, bar, domId, bar::getBarTranslator);
	}
	
	static void render(StringOutput target, ProgressBar bar, String domId, Supplier<Translator> barTranslator) {
		boolean horizontal = bar.getRenderStyle() == RenderStyle.horizontal;
		boolean inline = bar.getRenderSize() == RenderSize.inline;
		boolean labelAfter = inline || bar.getLabelPosition() == LabelPosition.after || (horizontal && bar.getRenderSize() == RenderSize.small);
		
		target.append("<span class='o_progress")
			.append(" o_progress_inline", inline)
			.append(" o_progress_label_after", labelAfter && hasLabel(bar))
			.append(" " + bar.getCssClass(), StringHelper.containsNonWhitespace(bar.getCssClass()))
			.append("'");
		if(StringHelper.containsNonWhitespace(domId)) {
			target.append(" id='").append(domId).append("'");
		}
		renderAria(target, bar, barTranslator);
		target.append(">");
		
		if (horizontal) {
			renderHorizontal(target, bar, labelAfter);
		} else {
			renderRadial(target, bar, domId, labelAfter);
		}
		if (labelAfter) {
			renderLabel(target, bar, "o_progress_label_wrapper");
		}
		target.append("</span>");
	}
	
	private static void renderHorizontal(StringOutput target, ProgressBar bar, boolean labelAfter) {
		target.append("<span class='progress")
			.append(" o_progress-inline", bar.getRenderSize() == RenderSize.inline)
			.append(" o_progress-sm", bar.getRenderSize() == RenderSize.small)
			.append(" o_progress-lg", bar.getRenderSize() == RenderSize.large)
			.append(" o_progress_with_markers", !bar.getMarkers().isEmpty())
			.append(" o_progress_segment_labels", bar.getSegmentLabelFormat() != SegmentLabelFormat.none)
			.append("'>");
		
		for (SegmentWidth segmentWidth : getSegmentWidths(bar)) {
			Segment segment = segmentWidth.segment();
			String colorCssClass = getColorCssClass(segment.color());
			target.append("<span class='progress-bar")
				.append(" " + colorCssClass, !colorCssClass.isEmpty())
				.append(" o_progress_bar_first", segmentWidth.start() == 0)
				.append(" o_progress_bar_full", segmentWidth.start() + segmentWidth.width() == FULL)
				.append(" progress-bar-striped active", bar.isProgressAnimationEnabled())
				.append(" " + segment.cssClass(), StringHelper.containsNonWhitespace(segment.cssClass()))
				.append("' style='width:").append(toPercent(segmentWidth.width())).append("%;' title=\"")
				.append(getSegmentTitle(segmentWidth)).append("\" aria-hidden='true'>");
			if (StringHelper.containsNonWhitespace(segment.label())) {
				if (bar.getSegmentLabelFormat() == SegmentLabelFormat.label) {
					target.append(StringHelper.escapeHtml(segment.label()));
				} else if (bar.getSegmentLabelFormat() == SegmentLabelFormat.valueAndLabel) {
					target.append(StringHelper.escapeHtml(getSegmentText(segment)));
				}
			}
			target.append("</span>");
		}
		
		float base = getBase(bar);
		for (Marker marker : bar.getMarkers()) {
			target.append("<span class='").append(getMarkerCssClass(marker)).append("' style='left:").append(toPercent(toHundredths(marker.value(), base)))
				.append("%;' title=\"").append(StringHelper.escapeHtml(getMarkerText(marker))).append("\" aria-hidden='true'></span>");
		}
		
		if (!labelAfter) {
			renderLabel(target, bar, "o_progress_label_wrapper");
		}
		target.append("</span>");
	}
	
	private static void renderRadial(StringOutput target, ProgressBar bar, String domId, boolean labelAfter) {
		long percent = getPercent(bar);
		BarColor color = bar.getSegments().isEmpty() ? BarColor.neutral : bar.getSegments().get(0).color();
		String radialColorCssClass = getRadialColorCssClass(color);
		target.append("<span class='radial-progress")
			.append(" radial-progress-pie", bar.getRenderStyle() == RenderStyle.pie)
			.append(" radial-progress-inline", bar.getRenderSize() == RenderSize.inline)
			.append(" radial-progress-sm", bar.getRenderSize() == RenderSize.small)
			.append(" radial-progress-lg", bar.getRenderSize() == RenderSize.large)
			.append(" " + radialColorCssClass, StringHelper.containsNonWhitespace(radialColorCssClass))
			.append("' data-progress='").append(bar.isProgressAnimationEnabled() ? 0 : percent)
			.append("' title='").append(percent).append("%'>")
			.append("<svg viewBox='0 0 32 32' aria-hidden='true'>")
			.append("<circle r='16' cx='16' cy='16' class='radial-bg'/>")
			.append("<circle r='16' cx='16' cy='16' class='radial-bar'/>", bar.getSegments().size() <= 1);
		if (bar.getSegments().size() > 1) {
			renderRadialSegments(target, bar);
		}
		renderRadialMarkers(target, bar);
		target.append("</svg>");
		
		if (!labelAfter && hasLabel(bar)) {
			LabelFormat format = bar.getLabelFormat();
			target.append("<span class='percentage'><span class='centeredWrapper'>");
			if (format == LabelFormat.percent || format == LabelFormat.percentAndValue) {
				target.append("<span class='number'><span>").append(percent).append("%</span></span>");
			}
			boolean value = format == LabelFormat.value || format == LabelFormat.percentAndValue;
			if (value || StringHelper.containsNonWhitespace(bar.getInfo())) {
				target.append("<span class='addon text-muted'>");
				if (value) {
					target.append("<span class='o_progress_label'>").append(getValueText(bar)).append("</span>");
				}
				renderInfo(target, bar);
				target.append("</span>");
			}
			target.append("</span></span>");
		}
		target.append("</span>");
		
		if (bar.isProgressAnimationEnabled() && StringHelper.containsNonWhitespace(domId)) {
			target.append("<script>")
				.append("\"use strict\";")
				.append("setTimeout(function() {")
				.append("	o_animateRadialProgress('#").append(domId).append(" .radial-progress', ").append(percent).append(");")
				.append("});")
				.append("</script>");
		}
	}
	
	private static void renderRadialSegments(StringOutput target, ProgressBar bar) {
		for (SegmentWidth segmentWidth : getSegmentWidths(bar)) {
			Segment segment = segmentWidth.segment();
			target.append("<circle r='16' cx='16' cy='16' pathLength='100' class='radial-segment radial-segment-")
				.append((segment.color() == null ? BarColor.neutral : segment.color()).name())
				.append(" " + segment.cssClass(), StringHelper.containsNonWhitespace(segment.cssClass()))
				.append("' stroke-dasharray='").append(toPercent(segmentWidth.width())).append(" 100'")
				.append(" stroke-dashoffset='-").append(toPercent(segmentWidth.start())).append("'><title>")
				.append(getSegmentTitle(segmentWidth)).append("</title></circle>");
		}
	}
	
	private static List<SegmentWidth> getSegmentWidths(ProgressBar bar) {
		List<SegmentWidth> segmentWidths = new ArrayList<>();
		float base = getBase(bar);
		double cumulated = 0;
		long previous = 0;
		for (Segment segment : bar.getSegments()) {
			cumulated += segment.value();
			long current = toHundredths(cumulated, base);
			if (current > previous) {
				segmentWidths.add(new SegmentWidth(segment, previous, current - previous));
			}
			previous = current;
		}
		return segmentWidths;
	}
	
	private static String getSegmentTitle(SegmentWidth segmentWidth) {
		if (StringHelper.containsNonWhitespace(segmentWidth.segment().label())) {
			return StringHelper.escapeHtml(getSegmentText(segmentWidth.segment()));
		}
		return toPercent(segmentWidth.width()) + "%";
	}
	
	private static void renderRadialMarkers(StringOutput target, ProgressBar bar) {
		float base = getBase(bar);
		double inner = bar.getRenderStyle() == RenderStyle.pie ? 0 : 11;
		for (Marker marker : bar.getMarkers()) {
			double angle = 2 * Math.PI * toHundredths(marker.value(), base) / FULL;
			double cos = Math.cos(angle);
			double sin = Math.sin(angle);
			target.append("<line class='").append(getMarkerCssClass(marker)).append("' x1='").append(toCoordinate(16 + inner * cos))
				.append("' y1='").append(toCoordinate(16 + inner * sin))
				.append("' x2='").append(toCoordinate(16 + 16 * cos))
				.append("' y2='").append(toCoordinate(16 + 16 * sin))
				.append("'><title>").append(StringHelper.escapeHtml(getMarkerText(marker))).append("</title></line>");
		}
	}
	
	static String toCoordinate(double value) {
		return String.valueOf(Math.round(value * 100) / 100d);
	}
	
	private static void renderLabel(StringOutput target, ProgressBar bar, String cssClass) {
		if (!hasLabel(bar)) {
			return;
		}
		
		target.append("<span class='").append(cssClass).append("'>");
		switch (bar.getLabelFormat()) {
			case percent -> target.append(getPercent(bar)).append("%");
			case value -> target.append("<span class='o_progress_label'>").append(getValueText(bar)).append("</span>");
			case percentAndValue -> target.append(getPercent(bar)).append("% (<span class='o_progress_label'>").append(getValueText(bar)).append("</span>)");
			default -> {
				//
			}
		}
		renderInfo(target, bar);
		target.append("</span>");
	}
	
	private static void renderInfo(StringOutput target, ProgressBar bar) {
		if (StringHelper.containsNonWhitespace(bar.getInfo())) {
			target.append("<span class='o_progress_info'>").append(bar.getInfo()).append("</span>");
		}
	}
	
	private static boolean hasLabel(ProgressBar bar) {
		return bar.getLabelFormat() != LabelFormat.none
				|| (bar.getRenderStyle() != RenderStyle.horizontal && StringHelper.containsNonWhitespace(bar.getInfo()));
	}
	
	private static void renderAria(StringOutput target, ProgressBar bar, Supplier<Translator> barTranslator) {
		if (bar.isAriaHidden()) {
			target.append(" aria-hidden='true'");
			return;
		}
		
		List<Segment> segments = bar.getSegments();
		if (segments.size() <= 1 && bar.getMarkers().isEmpty()) {
			String label = bar.getAriaLabel();
			if (!StringHelper.containsNonWhitespace(label) && !segments.isEmpty()) {
				label = segments.get(0).label();
			}
			if (!StringHelper.containsNonWhitespace(label)) {
				label = barTranslator.get().translate("progress.aria.label");
			}
			target.append(" role='progressbar' aria-label=\"").append(StringHelper.escapeHtml(label)).append("\"");
			if (!bar.isNoMax()) {
				target.append(" aria-valuemin='0' aria-valuemax='100' aria-valuenow='").append(getPercent(bar)).append("'");
			}
			target.append(" aria-valuetext=\"").append(StringHelper.escapeHtml(getAriaValueText(bar, barTranslator))).append("\"");
		} else {
			target.append(" role='img' aria-label=\"").append(StringHelper.escapeHtml(getAriaLabel(bar, barTranslator))).append("\"");
		}
	}
	
	private static String getAriaValueText(ProgressBar bar, Supplier<Translator> barTranslator) {
		if (bar.isNoMax()) {
			return getValueText(bar);
		}
		return switch (bar.getLabelFormat()) {
			case value -> getValueText(bar);
			case percentAndValue -> getPercent(bar) + "% (" + getValueText(bar) + ")";
			case none -> StringHelper.containsNonWhitespace(bar.getUnitLabel())
					? barTranslator.get().translate("progress.aria.of", String.valueOf(Math.round(Math.min(bar.getTotal(), bar.getMax()))), getMaxText(bar))
					: getPercent(bar) + "%";
			default -> getPercent(bar) + "%";
		};
	}
	
	static String getAriaLabel(ProgressBar bar, Supplier<Translator> barTranslator) {
		List<String> segmentTexts = new ArrayList<>();
		for (Segment segment : bar.getSegments()) {
			if (segment.value() > 0 || StringHelper.containsNonWhitespace(segment.label())) {
				segmentTexts.add(getSegmentText(segment));
			}
		}
		String label = String.join(", ", segmentTexts);
		if (!bar.isNoMax()) {
			label = barTranslator.get().translate("progress.aria.of", label, getMaxText(bar));
		}
		
		for (Marker marker : bar.getMarkers()) {
			label += ", " + getMarkerText(marker);
		}
		if (StringHelper.containsNonWhitespace(bar.getAriaLabel())) {
			label = bar.getAriaLabel() + ": " + label;
		}
		return label;
	}
	
	private static String getSegmentText(Segment segment) {
		return StringHelper.containsNonWhitespace(segment.label())
				? Math.round(segment.value()) + " " + segment.label()
				: String.valueOf(Math.round(segment.value()));
	}
	
	private static String getMarkerText(Marker marker) {
		return StringHelper.containsNonWhitespace(marker.label())
				? marker.label() + " " + Math.round(marker.value())
				: String.valueOf(Math.round(marker.value()));
	}
	
	private static String getMaxText(ProgressBar bar) {
		String max = String.valueOf(Math.round(bar.getMax()));
		return StringHelper.containsNonWhitespace(bar.getUnitLabel()) ? max + " " + bar.getUnitLabel() : max;
	}
	
	private static String getValueText(ProgressBar bar) {
		float total = bar.isNoMax() ? bar.getTotal() : Math.min(bar.getTotal(), bar.getMax());
		String value = String.valueOf(Math.round(total));
		if (!bar.isNoMax()) {
			value += "/" + Math.round(bar.getMax());
		}
		if (StringHelper.containsNonWhitespace(bar.getUnitLabel())) {
			value += " " + bar.getUnitLabel();
		}
		return value;
	}
	
	private static float getBase(ProgressBar bar) {
		return bar.isNoMax() ? bar.getTotal() : bar.getMax();
	}
	
	static long getPercent(ProgressBar bar) {
		return Math.round(toHundredths(bar.getTotal(), getBase(bar)) / 100d);
	}
	
	static long toHundredths(double value, float base) {
		if (base <= 0) {
			return 0;
		}
		return Math.max(0, Math.min(FULL, Math.round(value * FULL / base)));
	}
	
	static String toPercent(long hundredths) {
		long fraction = hundredths % 100;
		if (fraction == 0) {
			return String.valueOf(hundredths / 100);
		}
		return (hundredths / 100) + (fraction < 10 ? ".0" : ".") + fraction;
	}
	
	private static String getColorCssClass(BarColor color) {
		return switch (color == null ? BarColor.neutral : color) {
			case primary -> "";
			case success -> "progress-bar-success";
			case info -> "progress-bar-info";
			case warning -> "progress-bar-warning";
			case danger -> "progress-bar-danger";
			case passed -> "o_progress-bar-passed";
			case failed -> "o_progress-bar-failed";
			case neutral -> "o_progress-bar-neutral";
		};
	}
	
	private static String getMarkerCssClass(Marker marker) {
		return marker.color() == BarColor.danger ? "o_progress_marker" : "o_progress_marker o_progress_marker_" + marker.color().name();
	}
	
	private static String getRadialColorCssClass(BarColor color) {
		return switch (color == null ? BarColor.neutral : color) {
			case success -> "radial-progress-success";
			case info -> "radial-progress-info";
			case warning -> "radial-progress-warning";
			case danger -> "radial-progress-danger";
			case neutral -> "radial-progress-neutral";
			default -> null;
		};
	}
}
