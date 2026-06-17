/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.selectus.ui.components;

import java.util.List;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

/**
 * 
 * Initial date: 28 oct. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class StackedProgressBarRenderer extends DefaultComponentRenderer {

	@Override
	public void renderComponent(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {
		
		StackedProgressBar bar = (StackedProgressBar)source;
		sb.append("<div id='o_c").append(bar.getDispatchID()).append("' class='progress' style='width:100%;' title=''>");
		
		double totalWidth = bar.getWidth();
		if(totalWidth <= 0.0d) {
			totalWidth = 1.0d;
		}
		
		List<BarItem> items = bar.getItems();
		for(BarItem item:items) {
			double itemWidth = item.getWidth();
			if(itemWidth <= 0.0d) {
				continue;
			}

			long valuenow = Math.round(item.getWidth());
			double itemWidthPercent = (itemWidth / totalWidth) * 100.0d;
			long widthPercent = Math.round(itemWidthPercent);
			sb.append("<div class='progress-bar ").append(item.getCssClass()).append("' role='progressbar' ")
			  .append(" title='").append(item.getLabel()).append("'")
			  .append(" aria-valuenow='").append(valuenow).append("' aria-valuemin='0' style='width: ").append(widthPercent).append("%;'>")
			  .append("<span>").append(item.getLabel()).append("</span>")
			  .append("</div>");
		}
		sb.append("</div>");
	}
	
	

}
