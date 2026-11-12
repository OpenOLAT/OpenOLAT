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
package org.olat.core.gui.components.progressbar;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Locale;

import org.junit.Test;
import org.olat.core.gui.components.progressbar.ProgressBar.BarColor;
import org.olat.core.gui.components.progressbar.ProgressBar.LabelFormat;
import org.olat.core.gui.components.progressbar.ProgressBar.LabelPosition;
import org.olat.core.gui.components.progressbar.ProgressBar.Marker;
import org.olat.core.gui.components.progressbar.ProgressBar.RenderSize;
import org.olat.core.gui.components.progressbar.ProgressBar.RenderStyle;
import org.olat.core.gui.components.progressbar.ProgressBar.Segment;
import org.olat.core.gui.components.progressbar.ProgressBar.SegmentLabelFormat;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.translator.Translator;

/**
 * 
 * Initial date: 10 Nov 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class ProgressBarRendererTest {
	
	@Test
	public void shouldRoundSegmentsCumulatively() {
		ProgressBar progressBar = createProgressBar(3);
		progressBar.setSegments(List.of(
				new Segment(1, BarColor.success, null),
				new Segment(1, BarColor.warning, null),
				new Segment(1, BarColor.danger, null)));
		
		String html = render(progressBar);
		
		assertThat(html)
				.contains("progress-bar-success o_progress_bar_first' style='width:33.33%;'")
				.contains("progress-bar-warning' style='width:33.34%;'")
				.contains("progress-bar-danger o_progress_bar_full' style='width:33.33%;'");
	}
	
	@Test
	public void shouldNotExceedFullWidth() {
		ProgressBar progressBar = createProgressBar(10);
		progressBar.setSegments(List.of(
				new Segment(8, BarColor.success, null),
				new Segment(8, BarColor.warning, null)));
		
		String html = render(progressBar);
		
		assertThat(html)
				.contains("width:80%;")
				.contains("width:20%;");
	}
	
	@Test
	public void shouldRenderZeroMaxWithoutNaN() {
		ProgressBar progressBar = createProgressBar(0);
		ProgressBarFactory.setValue(progressBar, 5, BarColor.neutral);
		progressBar.setLabelFormat(LabelFormat.percent);
		
		String html = render(progressBar);
		
		assertThat(html)
				.doesNotContain("NaN")
				.doesNotContain("progress-bar ")
				.contains("aria-valuenow='0'");
	}
	
	@Test
	public void shouldRenderValueWithoutMax() {
		ProgressBar progressBar = createProgressBar(0);
		progressBar.setNoMax(true);
		progressBar.setUnitLabel("MB");
		progressBar.setLabelFormat(LabelFormat.value);
		ProgressBarFactory.setValue(progressBar, 12, BarColor.neutral);
		
		String html = render(progressBar);
		
		assertThat(html)
				.contains("12 MB")
				.doesNotContain("12/")
				.doesNotContain("aria-valuemax");
	}
	
	@Test
	public void shouldRenderLabelAfterMediumBar() {
		ProgressBar progressBar = createProgressBar(100);
		progressBar.setLabelFormat(LabelFormat.value);
		progressBar.setLabelPosition(LabelPosition.after);
		ProgressBarFactory.setValue(progressBar, 40, BarColor.neutral);
		
		String html = render(progressBar);
		
		assertThat(html)
				.contains("o_progress_label_after")
				.contains("<span class='progress'>")
				.contains("</span><span class='o_progress_label_wrapper'>");
	}
	
	@Test
	public void shouldRenderSegmentLabelsInSegments() {
		ProgressBar progressBar = createProgressBar(10);
		progressBar.setSegments(List.of(new Segment(6, BarColor.success, "active"), new Segment(2, BarColor.warning, null)));
		progressBar.setSegmentLabelFormat(SegmentLabelFormat.valueAndLabel);
		
		String html = render(progressBar);
		
		assertThat(html)
				.contains("o_progress_segment_labels")
				.contains("aria-hidden='true'>6 active</span>")
				.contains("aria-hidden='true'></span>");
	}
	
	@Test
	public void shouldRenderOnlyLabelInSegments() {
		ProgressBar progressBar = createProgressBar(10);
		progressBar.setSegments(List.of(new Segment(6, BarColor.success, "A")));
		progressBar.setSegmentLabelFormat(SegmentLabelFormat.label);
		
		String html = render(progressBar);
		
		assertThat(html).contains("title=\"6 A\" aria-hidden='true'>A</span>");
	}
	
	@Test
	public void shouldSizeInnerWidthInPercentOfBar() {
		ProgressBar progressBar = createProgressBar(100);
		ProgressBarFactory.setValue(progressBar, 40, BarColor.neutral);
		
		String html = render(progressBar);
		
		assertThat(html)
				.contains("<span class='progress'>")
				.contains("style='width:40%;'");
	}
	
	@Test
	public void shouldRenderSingleValueAsProgressbar() {
		ProgressBar progressBar = createProgressBar(100);
		ProgressBarFactory.setValue(progressBar, 40, BarColor.neutral);
		
		String html = render(progressBar);
		
		assertThat(html)
				.contains("role='progressbar'")
				.contains("aria-label=\"Progress\"")
				.contains("aria-valuemin='0' aria-valuemax='100' aria-valuenow='40'");
	}
	
	@Test
	public void shouldRenderStackedBarAsImage() {
		ProgressBar progressBar = createProgressBar(10);
		progressBar.setSegments(List.of(
				new Segment(6, BarColor.success, "active"),
				new Segment(2, BarColor.warning, "pending")));
		progressBar.setMarkers(List.of(new Marker(10, "minimum")));
		
		String html = render(progressBar);
		
		assertThat(html)
				.contains("role='img' aria-label=\"6 active, 2 pending of 10, minimum 10\"")
				.contains("title=\"6 active\"")
				.contains("class='o_progress_marker' style='left:100%;' title=\"minimum 10\" aria-hidden='true'")
				.contains("o_progress_with_markers")
				.doesNotContain("role='progressbar'");
	}
	
	@Test
	public void shouldSkipEmptySegmentsWithoutLabelInStackedName() {
		ProgressBar progressBar = createProgressBar(10);
		progressBar.setSegments(List.of(
				new Segment(6, BarColor.success, "active"),
				new Segment(0, BarColor.danger, "failed"),
				new Segment(0, BarColor.info, null)));
		
		String html = render(progressBar);
		
		assertThat(html).contains("aria-label=\"6 active, 0 failed of 10\"");
	}
	
	@Test
	public void shouldUseAriaLabelAsName() {
		ProgressBar progressBar = createProgressBar(100);
		ProgressBarFactory.setValue(progressBar, 40, BarColor.neutral);
		progressBar.setAriaLabel("Course progress");
		
		String html = render(progressBar);
		
		assertThat(html).contains("role='progressbar' aria-label=\"Course progress\"");
	}
	
	@Test
	public void shouldReadValueOfMaxWithUnitLabel() {
		ProgressBar progressBar = createProgressBar(50);
		progressBar.setUnitLabel("participants");
		ProgressBarFactory.setValue(progressBar, 3, BarColor.primary);
		
		String html = render(progressBar);
		
		assertThat(html)
				.contains("aria-valuenow='6'")
				.contains("aria-valuetext=\"3 of 50 participants\"");
	}
	
	@Test
	public void shouldPrefixStackedNameWithAriaLabel() {
		ProgressBar progressBar = createProgressBar(10);
		progressBar.setAriaLabel("Lectures");
		progressBar.setSegments(List.of(
				new Segment(8, BarColor.success, "attended"),
				new Segment(2, BarColor.danger, "absent")));
		
		String html = render(progressBar);
		
		assertThat(html).contains("role='img' aria-label=\"Lectures: 8 attended, 2 absent of 10\"");
	}
	
	@Test
	public void shouldRenderRadialMarkersAsLines() {
		ProgressBar progressBar = createProgressBar(100);
		progressBar.setRenderStyle(RenderStyle.radial);
		ProgressBarFactory.setValue(progressBar, 65, BarColor.success);
		progressBar.setMarkers(List.of(new Marker(25, "minimum")));
		
		String html = render(progressBar);
		
		assertThat(html)
				.contains("<line class='o_progress_marker' x1='16.0' y1='27.0' x2='16.0' y2='32.0'><title>minimum 25</title></line>")
				.contains("role='img' aria-label=\"65 of 100, minimum 25\"");
	}
	
	@Test
	public void shouldRenderRadialSegmentsAsArcs() {
		ProgressBar progressBar = createProgressBar(3);
		progressBar.setRenderStyle(RenderStyle.pie);
		progressBar.setSegments(List.of(
				new Segment(1, BarColor.success, null),
				new Segment(2, BarColor.warning, "pending")));
		
		String html = render(progressBar);
		
		assertThat(html)
				.doesNotContain("radial-bar")
				.contains("class='radial-segment radial-segment-success' stroke-dasharray='33.33 100' stroke-dashoffset='-0'><title>33.33%</title>")
				.contains("class='radial-segment radial-segment-warning' stroke-dasharray='66.67 100' stroke-dashoffset='-33.33'><title>2 pending</title>");
	}
	
	@Test
	public void shouldClampValueTextToMax() {
		ProgressBar progressBar = createProgressBar(0);
		progressBar.setLabelFormat(LabelFormat.value);
		ProgressBarFactory.setValue(progressBar, 1, BarColor.neutral);
		
		String html = render(progressBar);
		
		assertThat(html)
				.contains("0/0")
				.doesNotContain("1/0");
	}
	
	@Test
	public void shouldShowInfoOfHorizontalBarOnlyWithLabel() {
		ProgressBar progressBar = createProgressBar(100);
		progressBar.setRenderSize(RenderSize.small);
		progressBar.setInfo("12pt");
		ProgressBarFactory.setValue(progressBar, 40, BarColor.neutral);
		
		String html = render(progressBar);
		
		assertThat(html).doesNotContain("12pt");
	}
	
	@Test
	public void shouldHideBarFromScreenReaders() {
		ProgressBar progressBar = createProgressBar(100);
		ProgressBarFactory.setValue(progressBar, 40, BarColor.neutral);
		progressBar.setAriaHidden(true);
		
		String html = render(progressBar);
		
		assertThat(html)
				.contains("aria-hidden='true'>")
				.doesNotContain("role=");
	}
	
	@Test
	public void shouldReadPercentWithUnitLabel() {
		ProgressBar progressBar = createProgressBar(100);
		progressBar.setUnitLabel("%");
		progressBar.setLabelFormat(LabelFormat.percent);
		ProgressBarFactory.setValue(progressBar, 45, BarColor.neutral);
		
		String html = render(progressBar);
		
		assertThat(html).contains("aria-valuetext=\"45%\"");
	}
	
	@Test
	public void shouldReuseCellTranslatorOfSameLocale() {
		Translator translator = mock(Translator.class);
		when(translator.getLocale()).thenReturn(Locale.ENGLISH);
		
		assertThat(ProgressBarCellRenderer.getTranslator(translator, ProgressBar.class, Locale.ENGLISH)).isSameAs(translator);
	}
	
	@Test
	public void shouldFormatPercent() {
		assertThat(ProgressBarRenderer.toPercent(0)).isEqualTo("0");
		assertThat(ProgressBarRenderer.toPercent(5)).isEqualTo("0.05");
		assertThat(ProgressBarRenderer.toPercent(1250)).isEqualTo("12.50");
		assertThat(ProgressBarRenderer.toPercent(10000)).isEqualTo("100");
	}
	
	private ProgressBar createProgressBar(float max) {
		ProgressBar progressBar = ProgressBarFactory.createProgressBar("progress", Locale.ENGLISH);
		progressBar.setMax(max);
		return progressBar;
	}
	
	private String render(ProgressBar progressBar) {
		Translator translator = mock(Translator.class);
		when(translator.translate("progress.aria.label")).thenReturn("Progress");
		when(translator.translate(eq("progress.aria.of"), any(String[].class)))
				.thenAnswer(invocation -> invocation.getArgument(1) + " of " + invocation.getArgument(2));
		StringOutput target = new StringOutput();
		ProgressBarRenderer.render(target, progressBar, "o_c1", () -> translator);
		return target.toString();
	}
	
}
