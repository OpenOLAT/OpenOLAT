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

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.logging.AssertException;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BarChartComponent extends AbstractD3Component {
	
	private static final ComponentRenderer renderer = new BarChartComponentRenderer();

	private Scale yScale = Scale.plain;
	private List<BarSeries> seriesList = new ArrayList<>();
	
	private String defaultBarClass = "bar_default";
	private String yLegend;
	
	public BarChartComponent(String name) {
		super(name);
	}

	public String getDefaultBarClass() {
		return defaultBarClass;
	}

	public void setDefaultBarClass(String defaultBarClass) {
		this.defaultBarClass = defaultBarClass;
	}

	public String getYLegend() {
		return yLegend;
	}

	public void setYLegend(String yLegend) {
		this.yLegend = yLegend;
	}
	
	public Scale getYScale() {
		return yScale;
	}

	public void setYScale(Scale yScale) {
		if(yScale == Scale.plain || yScale == Scale.percent) {
			this.yScale = yScale;
		} else {
			throw new AssertException("Scale not supported by bar chart: " + yScale);
		}
	}
	
	public List<BarSeries> getSeries() {
		return seriesList;
	}
	
	public void addSeries(BarSeries... series) {
		if(series != null && series.length > 0 && series[0] != null) {
			for(BarSeries s:series) {
				seriesList.add(s);
			}
		}
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return renderer;
	}
}
