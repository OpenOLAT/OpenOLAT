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
package org.olat.core.gui.components.indicators;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: Oct 29, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class IndicatorRenderer extends DefaultComponentRenderer{

	@Override
	public void renderComponent(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu,
			Translator translator, RenderResult renderResult, String[] args) {
		IndicatorComponent cmp = (IndicatorComponent) source;
		
		sb.append("<div class='o_indicator'>");
		if (StringHelper.containsNonWhitespace(cmp.getLabel())) {
			sb.append("<div class='o_indicator_label'>");
			sb.append(cmp.getLabel());
			sb.append("</div>");
		}
		if (cmp.getFigure() != null && cmp.getFigure().isVisible()) {
			sb.append("<div class='o_indicator_figure'>");
			renderer.render(cmp.getFigure(), sb, null);
			cmp.getFigure().setDirty(false);
			sb.append("</div>");
		}
		sb.append("</div>");
	}

}
