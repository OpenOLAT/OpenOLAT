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
package org.olat.core.gui.components.chart;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.chart.RadarChartComponent.Format;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;

/**
 * 
 * Initial date: 20 déc. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RadarChartElement extends FormItemImpl {
	
	private final RadarChartComponent component;
	
	public RadarChartElement(String name) {
		super(name);
		component = new RadarChartComponent(name);
	}

	@Override
	protected Component getFormItemComponent() {
		return component;
	}
	
	public List<RadarSeries> getSeries() {
		return component.getSeries();
	}

	public void setSeries(List<RadarSeries> series) {
		component.setSeries(series);
	}
	
	public int getLevels() {
		return component.getLevels();
	}

	public void setLevels(int levels) {
		component.setLevels(levels);
	}

	public double getMaxValue() {
		return component.getMaxValue();
	}

	public void setMaxValue(double maxValue) {
		component.setMaxValue(maxValue);
	}

	public boolean isShowLegend() {
		return component.isShowLegend();
	}

	public void setShowLegend(boolean showLegend) {
		component.setShowLegend(showLegend);
	}
	
	public Format getFormat() {
		return component.getFormat();
	}

	public void setFormat(Format format) {
		component.setFormat(format);
	}
	
	public List<String> getAxis() {
		return component.getAxis();
	}
	
	public void setAxis(List<String> axis) {
		component.setAxis(axis);
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
