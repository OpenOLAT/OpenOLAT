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

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Formatter;

/**
 * 
 * Initial date: 05. April 2024<br>
 * @author gnaegi@frentix.com, https://www.frentix.com
 *
 */
public class BoxPlotRenderer extends DefaultComponentRenderer {

	@Override
	public void renderComponent(Renderer renderer, StringOutput target, Component source, URLBuilder urlBuilder, Translator translator,
			RenderResult renderResult, String[] args) {

		BoxPlot boxPlot = (BoxPlot) source;
				
		String compId = "o_c" + boxPlot .getDispatchID();	
		target.append("<svg id='").append(compId).append("' class='o_bp ")
			.append(boxPlot.getCssClass() + " ", boxPlot.getCssClass() != null)
			.append(boxPlot.getElementCssClass(), boxPlot.getElementCssClass() != null)
			.append("' viewBox='0 0 ")
			.append(boxPlot.getWidth()).append(" ")
			.append(boxPlot.getHeight()).append("'");
		if(!boxPlot.isResponsiveScaling()) {		
			// Do not add the box width for responsive elements that should scale with the outer box
			target.append(" width='").append(boxPlot.getWidth()).append("px'");
			
		}
		// i11y
		target.append(boxPlot.getHeight()).append(" role='img'>");
				
		// 1) Background axis
		float axisHeight = boxPlot.getHeight()/2;
		int axisX1 = 0;
		int axisX2 = boxPlot.getWidth();
		// horizontal line
		renderLine(target, "o_bp_a_l", axisX1, axisHeight, axisX2, axisHeight);
		// vertical star and end whisker
		float axisW1 = axisX1 + 0.5f;
		float axisW2 = axisX2 - 0.5f;
		int axisH = Math.round(axisHeight / 2);
		renderLine(target, "o_bp_a_s", axisW1, axisH, axisW1, boxPlot.getHeight() - axisH);
		renderLine(target, "o_bp_a_e", axisW2, axisH, axisW2, boxPlot.getHeight() - axisH);
		
		// 2) Value range bar
		float valX = normalize(boxPlot.getMinValue(), boxPlot);
		float valX2 = normalize(boxPlot.getMaxValue(), boxPlot);	
		if (valX >=0 && valX2 >= 0) {
			//horizontal line
			renderLine(target, "o_bp_v_l", valX, axisHeight, valX2, axisHeight);
			// vertical start and end whisker rendered at the very end, see 6)
		}		

		// 3) Quartile box
		if (boxPlot.getFirstQuartile() >= 0 && boxPlot.getThirdQuartile() > 0) {			
			float qX1 = normalize(boxPlot.getFirstQuartile(), boxPlot);
			float qX2 = normalize(boxPlot.getThirdQuartile(), boxPlot);
			
			renderRect(target, "o_bp_v_b", qX1, 0, (qX2-qX1), boxPlot.getHeight());

			// 4) Median
			if (boxPlot.getMedian() >= 0) {
				float mX = normalize(boxPlot.getMedian(), boxPlot);
				renderLine(target, "o_bp_v_m", mX, 0, mX, boxPlot.getHeight());
			}	
		}

		// 5) Average cross
		if (boxPlot.getAverage() >=0) {
			// compensate + 1 for top/bottom triangle of bar
			float avHalfSize = (axisHeight / 2);
			float avX = (normalize(boxPlot.getAverage(), boxPlot) - avHalfSize); 
			float avX2 = (normalize(boxPlot.getAverage(), boxPlot) + avHalfSize); 
			renderLine(target, "o_bp_v_a", avX, avHalfSize, avX2, (boxPlot.getHeight() - avHalfSize));
			renderLine(target, "o_bp_v_a", avX, (boxPlot.getHeight() - avHalfSize) , avX2, avHalfSize);

			// Accessibility: read the average only as percentage
			if (translator != null) {
				int avPercent = Math.round(boxPlot.getAverage() / boxPlot.getMaxAbsolute() * 100);						
				target.append("<title>").append(translator.translate("average")).append(":").append(avPercent).append("%</title>");
			}
		}		
		
		// 6) Vertical start and end whisker for value range. Rendered at the end to be on top layer, always wins
		if (valX >=0 && valX2 >= 0) {
			float valW1 = valX + 0.5f;
			float valW2 = valX2 - 0.5f;
			renderLine(target, "o_bp_v_s", valW1, 0, valW1, boxPlot.getHeight());
			renderLine(target, "o_bp_v_e", valW2, 0, valW2, boxPlot.getHeight());
		}

		
		
		target.append("</svg>");
	}

	/**
	 * Helper to normalize the actual values to values that match the desired width
	 * of the graphic.
	 * 
	 * @param inputValue
	 * @param boxPlot
	 * @return
	 */
	private float normalize(float inputValue, BoxPlot boxPlot) {
		if (inputValue >= 0 && boxPlot.getMaxAbsolute() > 0) {
			return (inputValue / boxPlot.getMaxAbsolute() * boxPlot.getWidth());		
		}
		return -1;
	}
	
	
	/**
	 * Helper to render an SVG line element
	 * 
	 * @param target
	 * @param css
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 */
	private void renderLine(StringOutput target, String css, float x1, float y1, float x2, float y2) {
		target.append("<line class='").append(css)
			.append("' x1='").append(Formatter.roundToString(x1,1))
			.append("' y1='").append(Formatter.roundToString(y1,1))
			.append("' x2='").append(Formatter.roundToString(x2,1))
			.append("' y2='").append(Formatter.roundToString(y2,1))
			.append("'/>");		
	}

	/**
	 * Helper to render an SVG rect element
	 * 
	 * @param target
	 * @param css
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	private void renderRect(StringOutput target, String css, float x, float y, float width, int height) {
		target.append("<rect class='").append(css)
			.append("' x='").append(Formatter.roundToString(x,1))
			.append("' y='").append(Formatter.roundToString(y,1))
			.append("' width='").append(Formatter.roundToString(width, 1))
			.append("' height='").append(height)
			.append("' rx='1' ry='1'/>"); // we don't want people to cut themselves on too sharp corners, do we?
	}

	
}