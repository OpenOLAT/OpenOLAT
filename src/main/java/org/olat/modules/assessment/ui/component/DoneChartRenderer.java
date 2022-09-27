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

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

/**
 * 
 * Initial date: 14 Feb 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class DoneChartRenderer extends DefaultComponentRenderer {

	@Override
	public void render(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {
		
		DoneChart doneChart = (DoneChart)source;
		
		sb.append("<span class=\"radial-progress radial-progress-pie o_assessment_chart\">");
		sb.append("<svg viewBox=\"0 0 32 32\">");
		sb.append("<circle r=\"16\" cx=\"16\" cy=\"16\" class=\"radial-bg\"></circle>");
		appendPieSlice(sb, "o_done_slice", doneChart.getDonePercent());
		sb.append("</svg>");
		sb.append("</span>");
	}
	
	private void appendPieSlice(StringOutput sb, String cssClass, int percent) {
		int percentCorr = percent == 100? 101: percent;
		sb.append("<circle r=\"16\" cx=\"16\" cy=\"16\" class=\"radial-bar ").append(cssClass).append("\" style=\"stroke-dasharray: ").append(percentCorr).append(" 100;\"></circle>");
	}
	
}
