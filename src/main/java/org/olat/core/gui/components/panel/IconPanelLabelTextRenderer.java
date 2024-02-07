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
package org.olat.core.gui.components.panel;

import java.util.List;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.components.panel.IconPanelLabelTextContent.LabelText;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 16 Nov 2022<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class IconPanelLabelTextRenderer extends DefaultComponentRenderer {

	@Override
	public void renderComponent(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu,
			Translator translator, RenderResult renderResult, String[] args) {
		
		IconPanelLabelTextContent content = (IconPanelLabelTextContent)source;
		List<LabelText> labelTexts = content.getLabelTexts();
		
		sb.append("<div class='o_form'>");
		if (StringHelper.containsNonWhitespace(content.getWarning())) {
			sb.append("<div class='o_warning'>");
			sb.append(content.getWarning());
			sb.append("</div>");
		}
		
		if (labelTexts != null && !labelTexts.isEmpty()) {
			for (int i = 0; i < labelTexts.size(); i++) {
				LabelText labelText = labelTexts.get(i);
				String id = "o_text_label_" + i;
				sb.append("<div class='form-horizontal'>");
				sb.append("<label class='control-label col-sm-3' for=\"").append(id).append("\">");
				if (StringHelper.containsNonWhitespace(labelText.label())) {
					sb.append(labelText.label());
				}
				sb.append("</label>");
				sb.append("</div>");
				sb.append("<div class='form-control-static col-sm-9' id=\"").append(id).append("\">");
				if (StringHelper.containsNonWhitespace(labelText.text())) {
					sb.append(labelText.text());
				}
				sb.append("</div>");
			}
		}
		sb.append("</div>");
		
		content.setDirty(false);
	}

}
