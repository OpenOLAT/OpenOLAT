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
package org.olat.modules.topicbroker.ui.components;

import static org.olat.modules.grade.ui.GradeUIFactory.THREE_DIGITS;

import java.util.function.Function;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.modules.topicbroker.manager.MaxPrioritiesCriterion;
import org.olat.modules.topicbroker.manager.MaxPrioritiesCriterion.ConstantFunction;
import org.olat.modules.topicbroker.manager.MaxPrioritiesCriterion.LinearFunction;
import org.olat.modules.topicbroker.manager.MaxPrioritiesCriterion.LogarythmicFunction;

/**
 * 
 * Initial date: Jul 9, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class MaxPrioritiesCriterionChartRenderer extends DefaultComponentRenderer {

	@Override
	public void renderComponent(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu,
			Translator translator, RenderResult renderResult, String[] args) {
		MaxPrioritiesCriterionChart chart = (MaxPrioritiesCriterionChart)source;
		MaxPrioritiesCriterion criterion = chart.getCriterion();
		if (criterion == null || criterion.getMaxSelections() < 2) {
			return;
		}
		
		String cmpId = chart.getDispatchID();
		String xMin = THREE_DIGITS.format(1);
		String xMax = THREE_DIGITS.format(criterion.getMaxSelections());
		String yMin = THREE_DIGITS.format(0);
		String yMax = THREE_DIGITS.format(criterion.getMaxSelections());
		
		sb.append("<div id='d").append(cmpId).append("d3holder' class='d3chart' style='width:300px;height:300px'></div>\n")
		  .append("<script>\n")
		  .append("/* <![CDATA[ */ ")
		  .append("jQuery(function () { \n")
		  .append("var placeholderheight = jQuery('#d").append(cmpId).append("d3holder').height();\n")
		  .append("var placeholderwidth = jQuery('#d").append(cmpId).append("d3holder').width();\n");
		sb.append("var xMax = ").append(xMax).append(";\n");
		sb.append("var margin = {top: 5, right: 10, bottom: 40, left: 40},\n")
		  .append("    width = placeholderwidth - margin.left - margin.right,\n")
		  .append("    height = placeholderheight - margin.top - margin.bottom;\n");
		sb.append("var xScale = d3.scaleLinear()\n")
		  .append("    .domain([").append(xMin).append(", ").append(xMax).append("])\n")
		  .append("    .range([0, width]);\n");
		sb.append("var yScale = d3.scaleLinear()\n")
		  .append("    .domain([").append(yMin).append(", ").append(yMax).append("])\n")
		  .append("    .range([height, 0]);\n");

		sb.append("var xAxisTicks = xScale.ticks().filter(tick => Number.isInteger(tick));");
		sb.append("var xAxis = d3.axisBottom(xScale).tickValues(xAxisTicks).tickFormat(d3.format('d'));");
		
		sb.append("var yAxisTicks = [0,").append(yMax).append("];\n");
		sb.append("var yAxis = d3.axisLeft(yScale).tickValues(yAxisTicks).tickFormat( d => {")
		  .append("      if (d === ").append(yMin).append(") return \"").append(translator.translate("enrollment.strategy.function.chart.low")).append("\";\n")
		  .append("      else if (d === ").append(yMax).append(") return \"").append(translator.translate("enrollment.strategy.function.chart.high")).append("\";\n")
		  .append("      else return \"\";\n")
		  .append("    });");

		sb.append("var svg = d3.select('#d").append(cmpId).append("d3holder').append('svg')\n")
		  .append("    .attr('width', width + margin.left + margin.right)\n")
		  .append("    .attr('height', height + margin.top + margin.bottom)\n")
		  .append("    .append('g')\n")
		  .append("    .attr('transform', 'translate(' + margin.left + ',' + margin.top + ')');\n");
		
		sb.append("svg.append('g')\n")
		  .append("   .attr('class', 'x axis')\n")
		  .append("   .attr('transform', 'translate(0,' + height + ')')\n")
		  .append("   .call(xAxis)");
		sb.append("  .append('text')")
		  .append("    .attr('y', (margin.bottom / 2))")
		  .append("    .attr('x', (width / 2))")
		  .append("    .attr('dy', '1em')")
		  .append("    .attr('fill', '#000')")
		  .append("    .style('text-anchor', 'middle')")
		  .append("    .text('").append(translator.translate("enrollment.strategy.function.chart.priority")).append("')");
		sb.append(";\n");
		
		sb.append("svg.append('g')\n")
		  .append("    .attr('class', 'y axis')\n")
		  .append("    .call(yAxis)\n");
		sb.append("  .append('text')")
		  .append("    .attr('transform', 'rotate(-90)')")
		  .append("    .attr('y', 0 - margin.left)")
		  .append("    .attr('x', 0 - (height / 2))")
		  .append("    .attr('dy', '1em')")
		  .append("    .attr('fill', '#000')")
		  .append("    .style('text-anchor', 'middle')")
		  .append("    .text('").append(translator.translate("enrollment.strategy.function.chart.weight")).append("')");
		sb.append(";\n");

		sb.append("var line = d3.line()\n");
		sb.append("    .x(function(d) { return xScale(d[0]); }) \n");
		sb.append("    .y(function(d) { return yScale(d[1]); }) \n");
		sb.append(";\n");
		
		String drawLineFunction = """
				function drawLine(data, cssClass) {
					svg.append("path")
						.datum(data)
						.attr("class", cssClass)
						.attr("d", d3.line()
									.x(d => xScale(d.x))
									.y(d => yScale(d.y)));
				}
				""";
		
		sb.append(drawLineFunction);
		appendYLines(sb, criterion.getMaxSelections());
		appendFunctions(sb, criterion);
		
		sb.append("});\n")
		  .append("/* ]]> */")
		  .append("</script>\n");
	}
	
	private void appendFunctions(StringOutput sb, MaxPrioritiesCriterion criterion) {
		int xMax = criterion.getCriterionBreakPoint() != null? criterion.getCriterionBreakPoint(): criterion.getMaxSelections();
		appendFunction(sb, getYFunc(criterion.getCriterionFunction()), 1, xMax, "o_tb_function_chart_line");
		
		if (criterion.getCriterionBreakPoint() != null) {
			appendFunction(sb, getYFunc(criterion.getCriterionFunctionAfter()), criterion.getCriterionBreakPoint(), criterion.getMaxSelections(), "o_tb_function_chart_line");
		}
	}
	
	private void appendFunction(StringOutput sb, String yFunc, int xMin, int xMax, String cssClass) {
		sb.append("var data = d3.range(").append(xMin).append(",").append(xMax+1).append(",1).map(xVal => ({\n");
		sb.append("  x: xVal,\n");
		sb.append("  y: ").append(yFunc).append("\n");
		sb.append("}));");
		sb.append("drawLine(data, \"").append(cssClass).append("\");");
	}
	
	private String getYFunc(Function<Integer, Double> function) {
		if (function instanceof ConstantFunction constantFunction) {
			return getYFunc(constantFunction);
		} else if (function instanceof LinearFunction linearFunction) {
			return getYFunc(linearFunction);
		} else if (function instanceof LogarythmicFunction logarythmicFunction) {
			return getYFunc(logarythmicFunction);
		}
		return "";
	}

	private String getYFunc(ConstantFunction constantFunction) {
		return String.valueOf(constantFunction.getConstant());
	}

	private String getYFunc(LinearFunction linearFunction) {
		return linearFunction.getM() + "* (xVal - " + linearFunction.getLowerSortOrder() +") + " + linearFunction.getB();
	}

	private String getYFunc(LogarythmicFunction logarythmicFunction) {
		return logarythmicFunction.getA() + "* Math.log10(xVal) + " + logarythmicFunction.getB();
	}
	
	private void appendYLines(StringOutput sb, int maxSelections) {
		LinearFunction linearFunction = new LinearFunction(1, maxSelections, maxSelections);
		for (int i=1; i<maxSelections; i++) {
			Double yValue = linearFunction.apply(Integer.valueOf(i));
			appendFunction(sb, String.valueOf(yValue), 1, maxSelections, "o_tb_function_chart_line o_tb_help_line o_tb_dash");
		}
	}

}
