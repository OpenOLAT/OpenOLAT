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
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.render.ValidationResult;

/**
 * 
 * Initial date: 20 déc. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RadarChartComponent extends DefaultD3Component {
	
	private static final ComponentRenderer renderer = new RadarChartComponentRenderer();
	
	private int levels;
	private Format format;
	private double maxValue;
	private boolean showLegend;
	
	private List<String> axis;
	private List<RadarSeries> series;
	
	public RadarChartComponent(String name) {
		super(name);
	}
	
	public List<RadarSeries> getSeries() {
		return series;
	}

	public void setSeries(List<RadarSeries> series) {
		this.series = series;
	}

	public int getLevels() {
		return levels;
	}

	public void setLevels(int levels) {
		this.levels = levels;
	}

	public double getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(double maxValue) {
		this.maxValue = maxValue;
	}

	public boolean isShowLegend() {
		return showLegend;
	}

	public void setShowLegend(boolean showLegend) {
		this.showLegend = showLegend;
	}

	public Format getFormat() {
		return format == null ? Format.percent : format;
	}

	public void setFormat(Format format) {
		this.format = format;
	}

	public List<String> getAxis() {
		return axis;
	}

	public void setAxis(List<String> axis) {
		this.axis = axis;
	}

	@Override
	public void validate(UserRequest ureq, ValidationResult vr) {
		super.validate(ureq, vr);

		vr.getJsAndCSSAdder().addRequiredStaticJsFile("js/jquery/openolat/jquery.statistics.radarchart.js");
	}
	
	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return renderer;
	}
	
	public enum Format {
		
		percent("%"),
		integer(".1r");
		
		private final String format;
		
		private Format(String format) {
			this.format = format;
		}
		
		public String format() {
			return format;
		}
		
	}
}
