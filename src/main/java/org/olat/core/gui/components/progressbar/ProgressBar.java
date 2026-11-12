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

import java.util.List;
import java.util.Locale;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.AbstractComponent;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.form.flexible.FormBaseComponent;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;

/**
 * A horizontal, radial or pie progress bar. The segments are the only data:
 * a single value is one segment, the total is the sum of all segments.
 * Markers are vertical lines at a value, e.g. a minimum.
 * 
 * Initial Date: Feb 2, 2004
 * 
 * @author Andreas
 */
public class ProgressBar extends AbstractComponent implements FormBaseComponent {
	private static final ComponentRenderer RENDERER = new ProgressBarRenderer();
	
	public enum RenderStyle {horizontal, radial, pie}
	public enum RenderSize {inline, small, medium, large}
	public enum BarColor {primary, success, info, warning, danger, passed, failed, neutral }
	public enum LabelFormat {none, percent, value, percentAndValue}
	public enum LabelPosition {auto, after}
	public enum SegmentLabelFormat {none, label, valueAndLabel}
	
	public record Segment(float value, BarColor color, String cssClass, String label) {
	
		public Segment(float value, BarColor color, String label) {
			this(value, color, null, label);
		}
	}
	
	public record Marker(float value, String label) {
		//
	}
	
	private final ProgressBarItem formItem;
	private final Locale locale;
	private Translator barTranslator;
	private float max;
	private boolean noMax = false;
	private List<Segment> segments = List.of();
	private List<Marker> markers = List.of();
	private LabelFormat labelFormat = LabelFormat.none;
	private RenderStyle renderStyle = RenderStyle.horizontal;
	private RenderSize renderSize = RenderSize.medium;
	private LabelPosition labelPosition = LabelPosition.auto;
	private boolean progressAnimationEnabled = false;
	private SegmentLabelFormat segmentLabelFormat = SegmentLabelFormat.none;
	private boolean ariaHidden = false;
	private String ariaLabel;
	private String unitLabel;
	private String info;
	private String cssClass;
	
	private ProgressBarCallback progressCallback;
	
	ProgressBar(String name, Locale locale) {
		super(name);
		this.formItem = null;
		this.locale = locale;
		setDomReplacementWrapperRequired(false);
	}
	
	ProgressBar(String name, ProgressBarItem formItem) {
		super(name);
		this.formItem = formItem;
		this.locale = null;
		setDomReplacementWrapperRequired(false);
	}
	
	Translator getBarTranslator() {
		if (barTranslator == null) {
			Locale barLocale = locale != null ? locale : formItem.getTranslator().getLocale();
			barTranslator = Util.createPackageTranslator(ProgressBar.class, barLocale);
		}
		return barTranslator;
	}
	
	@Override
	public String getFormDispatchId() {
		return DISPPREFIX.concat(super.getDispatchID());
	}
	
	@Override
	public ProgressBarItem getFormItem() {
		return formItem;
	}
	
	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		//
	}
	
	public float getMax() {
		return max;
	}
	
	public void setMax(float max) {
		this.max = max;
		setDirty(true);
	}
	
	public boolean isNoMax() {
		return noMax;
	}
	
	/**
	 * @param noMax If true, the bar has no max limit. The value label shows no "/max".
	 */
	public void setNoMax(boolean noMax) {
		this.noMax = noMax;
		setDirty(true);
	}
	
	public List<Segment> getSegments() {
		return segments;
	}
	
	public void setSegments(List<Segment> segments) {
		this.segments = segments == null ? List.of() : List.copyOf(segments);
		setDirty(true);
	}
	
	public List<Marker> getMarkers() {
		return markers;
	}
	
	public void setMarkers(List<Marker> markers) {
		this.markers = markers == null ? List.of() : List.copyOf(markers);
		setDirty(true);
	}
	
	public float getTotal() {
		float total = 0;
		for (Segment segment : segments) {
			total += segment.value();
		}
		return total;
	}
	
	public LabelFormat getLabelFormat() {
		return labelFormat;
	}
	
	public void setLabelFormat(LabelFormat labelFormat) {
		this.labelFormat = labelFormat == null ? LabelFormat.none : labelFormat;
		setDirty(true);
	}
	
	public String getUnitLabel() {
		return unitLabel;
	}
	
	public void setUnitLabel(String unitLabel) {
		this.unitLabel = unitLabel;
		setDirty(true);
	}
	
	public String getInfo() {
		return info;
	}
	
	public void setInfo(String info) {
		this.info = info;
		setDirty(true);
	}
	
	public String getCssClass() {
		return cssClass;
	}
	
	public void setCssClass(String cssClass) {
		this.cssClass = cssClass;
		setDirty(true);
	}
	
	public RenderStyle getRenderStyle() {
		return renderStyle;
	}
	
	public void setRenderStyle(RenderStyle renderStyle) {
		this.renderStyle = renderStyle;
		setDirty(true);
	}
	
	public RenderSize getRenderSize() {
		return renderSize;
	}
	
	public void setRenderSize(RenderSize renderSize) {
		this.renderSize = renderSize;
		setDirty(true);
	}
	
	public LabelPosition getLabelPosition() {
		return labelPosition;
	}
	
	/**
	 * @param labelPosition auto: after inline and small horizontal bars, inside
	 *                      all others
	 */
	public void setLabelPosition(LabelPosition labelPosition) {
		this.labelPosition = labelPosition;
		setDirty(true);
	}
	
	public boolean isProgressAnimationEnabled() {
		return progressAnimationEnabled;
	}
	
	public void setProgressAnimationEnabled(boolean progressAnimationEnabled) {
		this.progressAnimationEnabled = progressAnimationEnabled;
		setDirty(true);
	}
	
	public SegmentLabelFormat getSegmentLabelFormat() {
		return segmentLabelFormat;
	}
	
	/**
	 * @param segmentLabelFormat The text inside the segments of a horizontal
	 *                           bar. The tooltip shows always the value and the
	 *                           label.
	 */
	public void setSegmentLabelFormat(SegmentLabelFormat segmentLabelFormat) {
		this.segmentLabelFormat = segmentLabelFormat;
		setDirty(true);
	}
	
	public boolean isAriaHidden() {
		return ariaHidden;
	}
	
	/**
	 * @param ariaHidden Hide a decorative bar from screen readers if the same
	 *                   information is shown as text.
	 */
	public void setAriaHidden(boolean ariaHidden) {
		this.ariaHidden = ariaHidden;
		setDirty(true);
	}
	
	public String getAriaLabel() {
		return ariaLabel;
	}
	
	public void setAriaLabel(String ariaLabel) {
		this.ariaLabel = ariaLabel;
		setDirty(true);
	}
	
	public ProgressBarCallback getProgressCallback() {
		return progressCallback;
	}
	
	public void setProgressCallback(ProgressBarCallback progressCallback) {
		this.progressCallback = progressCallback;
	}
	
	@Override
	public boolean isDirty() {
		if(progressCallback != null) {
			return true;
		}
		return super.isDirty();
	}
	
	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}
}
