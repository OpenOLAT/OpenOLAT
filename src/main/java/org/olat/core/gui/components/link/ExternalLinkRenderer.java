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
package org.olat.core.gui.components.link;

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
 * Initial date: 07.01.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ExternalLinkRenderer extends DefaultComponentRenderer {

	@Override
	public void render(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu,
			Translator translator, RenderResult renderResult, String[] args) {
		ExternalLink link = (ExternalLink)source;
		
		//class
		sb.append("<a class=\"");
		if (!link.isEnabled()) {
			sb.append("o_disabled");
		}
		if(StringHelper.containsNonWhitespace(link.getElementCssClass())) {
			sb.append(" ").append(link.getElementCssClass());
		}
		sb.append("\" ");
		if(link.isEnabled())  {
			sb.append(" href=\"").append(link.getUrl()).append("\"");
		}
		if(StringHelper.containsNonWhitespace(link.getTarget())) {
			sb.append(" target=\"").append(link.getTarget()).append("\"");
		}
		if(StringHelper.containsNonWhitespace(link.getTooltip())) {
			sb.append(" title=\"").append(link.getTooltip()).append("\"");
		}
		if(StringHelper.containsNonWhitespace(link.getTooltip())) {
			sb.append(" rel=\"noopener noreferrer\"");
		}
		sb.append(">");
		
		if(StringHelper.containsNonWhitespace(link.getIconLeftCSS())) {
			sb.append("<i class=\"").append(link.getIconLeftCSS()).append("\"> </i> ");
		}
		if(StringHelper.containsNonWhitespace(link.getName())) {
			sb.append("<span>").append(link.getName()).append("</span>");
		}
		sb.append("</a>");
	}
}
