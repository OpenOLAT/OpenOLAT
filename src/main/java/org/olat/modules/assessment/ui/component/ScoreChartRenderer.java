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
package org.olat.modules.assessment.ui.component;

import java.util.Map;
import java.util.Map.Entry;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.course.assessment.AssessmentHelper;

/**
 * 
 * Initial date: 16 Feb 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ScoreChartRenderer extends DefaultComponentRenderer {

	@Override
	public void render(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {
		
		ScoreChart scoreChart = (ScoreChart)source;
		String cmpId = scoreChart.getDispatchID();
		
		Map<Integer,Long> scoreToCount = scoreChart.getScoreToCount();
		int minScore = getMinScore(scoreChart.getMinScore(), scoreToCount);
		int maxScore = getMaxScore(scoreChart.getMaxScore(), scoreToCount);
		String data = getData(minScore, maxScore, scoreToCount);
		double scoreAvg = getScoreAvg(scoreToCount);
		long yMax = getYMax(scoreToCount);
		
		sb.append("<div id='d").append(cmpId).append("d3holder' class='d3chart' style='width:400px;height:80px'></div>\n")
		  .append("<script>\n")
		  .append("/* <![CDATA[ */ ")
		  .append("jQuery(function () {\n")
		  .append("var placeholderheight = jQuery('#d").append(cmpId).append("d3holder').height();\n")
		  .append("var placeholderwidth = jQuery('#d").append(cmpId).append("d3holder').width();\n")
		  .append("var data = [").append(data).append("];\n");

		sb.append("var margin = {top: 5, right: 50, bottom: 20, left: 40},\n")
		  .append("    width = placeholderwidth - margin.left - margin.right,\n")
		  .append("    height = placeholderheight - margin.top - margin.bottom;\n")
		  .append("\n")
		  .append("var x = d3.scaleBand()\n")
		  .append("    .domain(data.map(function(d) { return d[0]; }))\n")
		  .append("    .range([0, width]).paddingInner(0.1).paddingOuter(0);\n")
		  .append("\n")
		  .append("var y = d3.scaleLinear()\n")
		  .append("    .domain([0, ").append(yMax).append("])\n")
		  .append("    .range([height, 0]);\n")
		  .append("\n")
		  .append("var xTicks = [").append(minScore).append(",").append(maxScore).append("];\n")
		  .append("var xAxis = d3.axisBottom(x).tickFormat( function(d) { return d } ).tickValues(xTicks);\n")
		  .append("\n")
		  .append("var yTicks = [0,").append(yMax).append("];\n")
		  .append("var yAxis = d3.axisLeft(y).tickFormat( function(d) { return d } ).tickValues(yTicks);\n");

		sb.append("\n")
		  .append("var svg = d3.select('#d").append(cmpId).append("d3holder').append('svg')\n")
		  .append("    .attr('width', width + margin.left + margin.right)\n")
		  .append("    .attr('height', height + margin.top + margin.bottom)\n")
		  .append("    .append('g')\n")
		  .append("    .attr('transform', 'translate(' + margin.left + ',' + margin.top + ')');\n");

		sb.append("svg.append('g')\n")
		  .append("   .attr('class', 'x axis')\n")
		  .append("   .attr('transform', 'translate(0,' + height + ')')\n")
		  .append("   .call(xAxis)\n");
		sb.append("  .append('text')\n")
		  .append("    .attr('y', (5))\n")
		  .append("    .attr('x', (width / 2))\n")
		  .append("    .attr('dy', '1em')\n")
		  .append("    .attr('fill', '#000')\n")
		  .append("    .style('text-anchor', 'middle')\n")
		  .append("    .text('").append(translator.translate("score.chart.legend", AssessmentHelper.getRoundedScore(Double.valueOf(scoreAvg)))).append("')\n");
		sb.append(";\n");
		
		sb.append("svg.append('g')\n")
		  .append("    .attr('class', 'y axis')\n")
		  .append("    .call(yAxis)\n");
		sb.append(";\n");
		
		sb.append("svg.selectAll()\n");
		sb.append("    .data(data)\n");
		sb.append("    .enter()\n");
		sb.append("    .append('rect')\n");
		sb.append("    .attr('class', 'bar bar_default')\n");
		sb.append("    .attr('x', (d) => x(d[0]))\n");
		sb.append("    .attr('y', (d) => y(d[1]))\n");
		sb.append("    .attr('height', (d) => height - y(d[1]))\n");
		sb.append("    .attr('width', x.bandwidth());");

		sb.append("});\n")
		  .append("/* ]]> */")
		  .append("</script>\n");
	}

	private int getMinScore(Double minScore, Map<Integer, Long> scoreToCount) {
		if (minScore != null) {
			return (int)Math.floor(minScore.doubleValue());
		}
		int min = scoreToCount.keySet().stream().min(Integer::compareTo).orElse(Integer.valueOf(0)).intValue();
		return min > 0? 0: min;
	}

	private int getMaxScore(Double maxScore, Map<Integer, Long> scoreToCount) {
		if (maxScore != null) {
			return (int)Math.ceil(maxScore.doubleValue());
		}
		return scoreToCount.keySet().stream().max(Integer::compareTo).orElse(Integer.valueOf(10)).intValue();
	}

	private String getData(int min, int max, Map<Integer,Long> scoreToCount) {
		StringBuilder sb = new StringBuilder();
		for (int i = min; i <= max; i++) {
			Long count = scoreToCount.getOrDefault(Integer.valueOf(i), Long.valueOf(0));
			sb.append("[\"").append(i).append("\",").append(count).append("]");
			if (i != max) {
				sb.append(",");
			}
		}
		return sb.toString();
	}
	
	private double getScoreAvg(Map<Integer, Long> scoreToCount) {
		int count = 0;
		int sum = 0;
		for (Entry<Integer, Long> entry : scoreToCount.entrySet()) {
			count += entry.getValue();
			sum += entry.getValue() * entry.getKey();
		}
		return count > 0? (double)sum/count: 0;
	}

	private long getYMax(Map<Integer,Long> scoreToCount) {
		return scoreToCount.values().stream().max(Long::compareTo).orElse(Long.valueOf(1)).longValue();
	}

}
