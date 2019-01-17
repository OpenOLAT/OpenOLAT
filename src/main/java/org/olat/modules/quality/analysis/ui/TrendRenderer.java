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

import org.olat.core.gui.components.table.IconCssCellRenderer;
import org.olat.modules.forms.RubricRating;
import org.olat.modules.forms.ui.EvaluationFormFormatter;
import org.olat.modules.forms.ui.RubricAvgRenderer;
import org.olat.modules.quality.analysis.Trend;
import org.olat.modules.quality.analysis.Trend.DIRECTION;

/**
 * 
 * Initial date: 17 Jan 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class TrendRenderer  extends IconCssCellRenderer {

	@Override
	protected String getCssClass(Object val) {
		if (val instanceof Trend) {
			Trend trend = (Trend) val;
			StringBuilder sb = new StringBuilder();
			sb.append("o_icon o_icon-lg o_icon_qual_ana_trend_arrow");
			sb.append(" ").append(getDirectionCssClass(trend.getDirection()));
			sb.append(" ").append(getRatingCssClass(trend.getRating()));
			return sb.toString();
		}
		return null;
	}

	private String getDirectionCssClass(DIRECTION direction) {
		switch(direction) {
			case UP: return "o_qual_ana_trend_up";
			case DOWN: return "o_qual_ana_trend_down";
			case EQUAL:
			default: return "";
		}
	}

	private Object getRatingCssClass(RubricRating rating) {
		String colorCss = RubricAvgRenderer.getRatingCssClass(rating);
		if (colorCss == null) {
			colorCss = "o_qual_ana_unrated";
		}
		return colorCss;
	}

	@Override
	protected String getCellValue(Object val) {
		if (val instanceof Trend) {
			Trend trend = (Trend) val;
			return EvaluationFormFormatter.formatDouble(trend.getAvg());
		}
		return null;
	}

	@Override
	protected String getHoverText(Object val) {
		return null;
	}

}
