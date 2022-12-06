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
package org.olat.core.gui.components.text;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;

/**
 * Description:<br>
 * This renderer renders a simple text component either as span or div tag.
 * Optionally a CSS class is added.
 * <p>
 * When the text component returns a NULL value, nothing will be rendered at
 * all. An empty string will render an empty span or div tag.
 * 
 * <P>
 * Initial Date: 10.11.2009 <br>
 * 
 * @author gnaegi
 */
class TextComponentRenderer extends DefaultComponentRenderer {

	@Override
	public void renderComponent(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {
		TextComponent comp = (TextComponent) source;
		String text = comp.getDisplayText();
		if (text != null) {
			// Add a wrapper with a CSS class if necessary
			String cssClass = comp.getCssClass();
			String elementCssClasss = comp.getElementCssClass();
			String tag = comp.getSpanAsDomReplaceable() ? "span" : "div";
			// In any case render a span or div. If in ajax mode, another span/div
			// will be wrapped around this to identify the component.
			sb.append("<").append(tag);
			// Add optional css class
			if (cssClass != null || elementCssClasss != null) {
				sb.append(" class='");
				if(StringHelper.containsNonWhitespace(cssClass)) {
					sb.append(cssClass);
				}
				if(StringHelper.containsNonWhitespace(elementCssClasss)) {
					sb.append(" ").append(elementCssClasss);
				}
				sb.append("'");
			}
			if(!comp.isDomReplacementWrapperRequired()) {
				sb.append(" id='").append(comp.getDispatchID()).append("'");
			}
			sb.append(">")
			  .append(text)
			  .append("</").append(tag).append(">");
		}
	}
}