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
package org.olat.modules.quality.analysis.ui;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.modules.forms.RubricRating;
import org.olat.modules.forms.ui.EvaluationFormFormatter;
import org.olat.modules.forms.ui.RubricAvgRenderer;
import org.olat.modules.quality.analysis.HeatMapStatistic;

/**
 * 
 * Initial date: 14.09.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class HeatMapRenderer implements FlexiCellRenderer {
	
	private static final int MAX_CIRCLE_SIZE = 18;
	private final int maxCount;
	private final boolean variableSize;
	
	public static HeatMapRenderer fixedSize() {
		return new HeatMapRenderer(0, false);
	}
	
	public static HeatMapRenderer variableSize(int maxCount) {
		return new HeatMapRenderer(maxCount, true);
	}

	private HeatMapRenderer(int maxCount, boolean variableSize) {
		this.maxCount = maxCount;
		this.variableSize = variableSize;
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator translator) {
		if (cellValue instanceof HeatMapStatistic) {
			HeatMapStatistic statistic = (HeatMapStatistic) cellValue;
			if (statistic.getCount() == null || statistic.getAvg() == null || Double.isNaN(statistic.getAvg())) return;
			
			target.append("<div class='o_circle_container'>");
			target.append("<div class='o_circle_box' style='width:").append(MAX_CIRCLE_SIZE).append("px;'>");
			target.append("<div class='o_circle ");
			target.append(getColorCss(statistic));
			target.append("'");
			appendSize(target, statistic.getCount());
			target.append(">");
			target.append("</div>");
			target.append("</div>");
			target.append("<div class='o_avg'>");
			target.append(EvaluationFormFormatter.formatDouble(statistic.getAvg()));
			target.append("</div>");
			target.append("</div>");
			
		}
	}

	public String getColorCss(HeatMapStatistic statistic) {
		RubricRating rating = statistic.getRating();
		String colorCss = RubricAvgRenderer.getRatingCssClass(rating);
		if (colorCss == null) {
			colorCss = "o_qual_hm_basecolor";
		}
		return colorCss;
	}

	private void appendSize(StringOutput target, Long count) {
		double size = MAX_CIRCLE_SIZE;
		if (variableSize) {
			double circleCount = count.doubleValue() <= maxCount? count.doubleValue(): maxCount;
			// The circle areas (not the diameter) are proportional to the count value.
			size = MAX_CIRCLE_SIZE * Math.sqrt(circleCount / maxCount);
		}
		target.append(" style='width: ").append(size).append("px; height: ").append(size).append("px'");
	}

}
