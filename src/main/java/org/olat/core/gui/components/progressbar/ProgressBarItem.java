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

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;
import org.olat.core.gui.components.progressbar.ProgressBar.LabelFormat;
import org.olat.core.gui.components.progressbar.ProgressBar.LabelPosition;
import org.olat.core.gui.components.progressbar.ProgressBar.Marker;
import org.olat.core.gui.components.progressbar.ProgressBar.RenderSize;
import org.olat.core.gui.components.progressbar.ProgressBar.RenderStyle;
import org.olat.core.gui.components.progressbar.ProgressBar.SegmentLabelFormat;
import org.olat.core.gui.components.progressbar.ProgressBar.Segment;

/**
 * 
 * Initial date: 03.03.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ProgressBarItem extends FormItemImpl {
	
	private final ProgressBar component;
	
	public ProgressBarItem(String name) {
		super(name);
		component = new ProgressBar(name, this);
	}
	
	@Override
	protected Component getFormItemComponent() {
		return component;
	}
	
	public float getMax() {
		return component.getMax();
	}
	
	public void setMax(float max) {
		component.setMax(max);
	}
	
	public boolean isNoMax() {
		return component.isNoMax();
	}
	
	public void setNoMax(boolean noMax) {
		component.setNoMax(noMax);
	}
	
	public List<Segment> getSegments() {
		return component.getSegments();
	}
	
	public void setSegments(List<Segment> segments) {
		component.setSegments(segments);
	}
	
	public List<Marker> getMarkers() {
		return component.getMarkers();
	}
	
	public void setMarkers(List<Marker> markers) {
		component.setMarkers(markers);
	}
	
	public float getTotal() {
		return component.getTotal();
	}
	
	public LabelFormat getLabelFormat() {
		return component.getLabelFormat();
	}
	
	public void setLabelFormat(LabelFormat labelFormat) {
		component.setLabelFormat(labelFormat);
	}
	
	public String getUnitLabel() {
		return component.getUnitLabel();
	}
	
	public void setUnitLabel(String unitLabel) {
		component.setUnitLabel(unitLabel);
	}
	
	public String getInfo() {
		return component.getInfo();
	}
	
	public void setInfo(String info) {
		component.setInfo(info);
	}
	
	public String getCssClass() {
		return component.getCssClass();
	}
	
	public void setCssClass(String cssClass) {
		component.setCssClass(cssClass);
	}
	
	public RenderStyle getRenderStyle() {
		return component.getRenderStyle();
	}
	
	public void setRenderStyle(RenderStyle renderStyle) {
		component.setRenderStyle(renderStyle);
	}
	
	public RenderSize getRenderSize() {
		return component.getRenderSize();
	}
	
	public void setRenderSize(RenderSize renderSize) {
		component.setRenderSize(renderSize);
	}
	
	public boolean isProgressAnimationEnabled() {
		return component.isProgressAnimationEnabled();
	}
	
	public void setProgressAnimationEnabled(boolean progressAnimationEnabled) {
		component.setProgressAnimationEnabled(progressAnimationEnabled);
	}
	
	public LabelPosition getLabelPosition() {
		return component.getLabelPosition();
	}
	
	public void setLabelPosition(LabelPosition labelPosition) {
		component.setLabelPosition(labelPosition);
	}
	
	public SegmentLabelFormat getSegmentLabelFormat() {
		return component.getSegmentLabelFormat();
	}
	
	public void setSegmentLabelFormat(SegmentLabelFormat segmentLabelFormat) {
		component.setSegmentLabelFormat(segmentLabelFormat);
	}
	
	public boolean isAriaHidden() {
		return component.isAriaHidden();
	}
	
	public void setAriaHidden(boolean ariaHidden) {
		component.setAriaHidden(ariaHidden);
	}
	
	public String getAriaLabel() {
		return component.getAriaLabel();
	}
	
	public void setAriaLabel(String ariaLabel) {
		component.setAriaLabel(ariaLabel);
	}
	
	public ProgressBarCallback getProgressCallback() {
		return component.getProgressCallback();
	}
	
	public void setProgressCallback(ProgressBarCallback progressCallback) {
		component.setProgressCallback(progressCallback);
	}
	
	@Override
	public String getForId() {
		return null;
	}
	
	@Override
	protected void rootFormAvailable() {
		//
	}
	
	@Override
	public void evalFormRequest(UserRequest ureq) {
		//
	}
	
	@Override
	public void reset() {
		//
	}
}
