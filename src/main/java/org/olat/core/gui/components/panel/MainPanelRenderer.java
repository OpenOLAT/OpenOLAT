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
package org.olat.core.gui.components.panel;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

/**
 * 
 * Initial date: 28.01.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MainPanelRenderer extends PanelRenderer {

	@Override
	public void renderComponent(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu,
			Translator translator, RenderResult renderResult, String[] args) {
		String cssClass = ((MainPanel)source).getCssClass();
		
		sb.append("<div id='o_main' class='row ").append(cssClass, cssClass != null).append("'>\n")
		  .append("<div id='o_main_center' class='split-pane'>\n")
		  .append("<div id='o_main_center_content' class='clearfix'>\n")
		  .append("<a id='o_content' aria-hidden='true'></a>\n")
		  .append("<div id='o_main_center_content_inner'>\n");
		
		super.renderComponent(renderer, sb, source, ubu, translator, renderResult, args);
		
		sb.append("</div>\n")
		  .append("</div>\n")
		  .append("</div>\n")
		  .append("</div>\n");
	}
}
