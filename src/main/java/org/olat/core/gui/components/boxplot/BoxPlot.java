/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.core.gui.components.boxplot;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.AbstractComponent;
import org.olat.core.gui.components.ComponentRenderer;

/**
 * Simple box-plot graphic to visualize a range of data points on a normalized
 * scale.
 * 
 * Initial date: 05. April 2024<br>
 * 
 * @author gnaegi@frentix.com, https://www.frentix.com
 *
 */
public class BoxPlot extends AbstractComponent {
	private static final ComponentRenderer RENDERER = new BoxPlotRenderer();

	private String cssClass;
	private int maxAbsolute;
	private float minValue;
	private float maxValue;
	private float average;
	private float firstQuartile;
	private float thirdQuartile;
	private float median;

	// Render width and height, default values
	private int width = 160;
	private int height = 16; // ~ line height
	private boolean responsiveScaling = false;

	/**
	 * Use this constructor if you have less then 10 data points in your sample.
	 * 
	 * @param name        The component name
	 * @param maxAbsolute The max of the value scale that represents 100%
	 * @param minValues   The measured minimum value
	 * @param maxValues   The measured maximum value
	 * @param average     The calculated average
	 * @param cssClass    The css class used for coloring the values
	 */

	public BoxPlot(String name, int maxAbsolute, float minValue, float maxValue, float average, String cssClass) {
		this(name, maxAbsolute, minValue, maxValue, average, -1, -1, -1, cssClass);
	}

	/**
	 * Use this constructor if you have more then 10 data points in your sample.
	 * 
	 * @param name          The component name
	 * @param maxAbsolute   The max of the value scale that represents 100%
	 * @param minValues     The measured minimum value
	 * @param maxValues     The measured maximum value
	 * @param average       The calculated average
	 * @param firstQuartile The calculated first quartile of the data or -1 if not
	 *                      available
	 * @param thirdQuartile The calculated third quartile of the data or -1 if not
	 *                      available
	 * @param median        The calculated median of the data or -1 if not available
	 * @param cssClass      The css class used for coloring the values
	 */

	public BoxPlot(String name, int maxAbsolute, float minValue, float maxValue, float average, float firstQuartile,
			float thirdQuartile, float median, String cssClass) {
		super(name);
		this.maxAbsolute = maxAbsolute;
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.average = average;
		this.firstQuartile = firstQuartile;
		this.thirdQuartile = thirdQuartile;
		this.median = median;
		this.cssClass = cssClass;
		setDomReplacementWrapperRequired(false);
	}

	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		//
	}

	public String getCssClass() {
		return cssClass;
	}

	public int getMaxAbsolute() {
		return maxAbsolute;
	}

	public float getMinValue() {
		return minValue;
	}

	public float getMaxValue() {
		return maxValue;
	}

	public float getAverage() {
		return average;
	}

	public float getFirstQuartile() {
		return firstQuartile;
	}

	public float getThirdQuartile() {
		return thirdQuartile;
	}

	public float getMedian() {
		return median;
	}

	/**
	 * @return the width of the rendered component
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * @param width Override the width of the rendered component, default 160px. If
	 *              you want to have a responsive graphic, use the responsiveScaling
	 *              variable and use the with as the base width.
	 */
	public void setWidth(int width) {
		if (width > 100) {
			this.width = width;			
		}
	}

	/**
	 * @return the height of the rendered component
	 */
	public int getHeight() {
		return height;			
	}

	/**
	 * @param width Override the height of the rendered component, default 16px
	 */
	public void setHeight(int height) {
		if (height >= 10) {
			this.height = height;
		}
	}

	/**
	 * @return true: don't add width to SVG, width defined by outer DOM element;
	 *         false: set defined width to SVG
	 */
	public boolean isResponsiveScaling() {
		return responsiveScaling;
	}

	/**
	 * 
	 * @param responsiveScaling true: don't add width to SVG, width defined by outer
	 *                          DOM element; false: set defined width to SVG
	 */
	public void setResponsiveScaling(boolean responsiveScaling) {
		this.responsiveScaling = responsiveScaling;
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}
}