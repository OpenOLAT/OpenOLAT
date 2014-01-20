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

import org.olat.core.gui.components.ComponentRenderer;

/**
 * 
 * Make an histogram from a list of values (doubles or longs but not booth)
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class HistogramComponent extends AbstractD3Component {
	
	private static final HistogramComponentRenderer RENDERER = new HistogramComponentRenderer();
	
	private double[] doubleValues;
	private long[] longValues;
	
	private double maxValue;
	private String defaultBarClass = "bar_default";
	
	private Scale xScale;
	private String yLegend;
	
	private double cutValue;
	private String lowBarClass;
	private String highBarClass;
	
	public HistogramComponent(String name) {
		super(name);
	}

	public String getDefaultBarClass() {
		return defaultBarClass;
	}

	public void setDefaultBarClass(String defaultBarClass) {
		this.defaultBarClass = defaultBarClass;
	}

	public double getCutValue() {
		return cutValue;
	}

	public String getLowBarClass() {
		return lowBarClass;
	}

	public String getHighBarClass() {
		return highBarClass;
	}
	
	/**
	 * Set a cut value for the x axis. Value lower than the cut value
	 * will get the lowBarClass, and bigger the highBarClass.
	 * 
	 * @param lowBarClass
	 * @param cutValue
	 * @param highBarClass
	 */
	public void setCutValue(String lowBarClass, double cutValue, String highBarClass) {
		this.cutValue = cutValue;
		this.lowBarClass = lowBarClass;
		this.highBarClass = highBarClass;
	}

	public Scale getXScale() {
		return xScale;
	}

	public void setXScale(Scale xScale) {
		this.xScale = xScale;
	}

	public String getYLegend() {
		return yLegend;
	}

	public void setYLegend(String yLegend) {
		this.yLegend = yLegend;
	}

	public double getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(double maxValue) {
		this.maxValue = maxValue;
	}

	public double[] getDoubleValues() {
		return doubleValues;
	}

	public void setDoubleValues(double[] doubleValues) {
		this.doubleValues = doubleValues;
	}

	public long[] getLongValues() {
		return longValues;
	}

	public void setLongValues(long[] longValues) {
		this.longValues = longValues;
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}
}
